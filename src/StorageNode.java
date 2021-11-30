import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.LinkedList;

import static java.lang.Integer.parseInt;

/**
 * Class for the Storage Nodes.
 *
 * @author Olga Silva & Samuel Correia
 */
public class StorageNode {

    private final String directoryIp;
    private final int directoryPort;
    private String nodeIp;
    private final int nodePort;
    private final String fileName;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    //colocadas a final, a não ser que futuramente seja necessário alterar nalguma parte do código

    public static final int DATALENGTH = 1000000;
    public static final int DEFAULTBLOCKLENGTH = 100;

    private CloudByte[] data = new CloudByte[DATALENGTH];
    private ErrorDetectionThread[] errorDetectionThreads;
    private static final int NUMERRORDETECTIONTHREADS = 2;

    public StorageNode(String ipAddress, int directoryPort, int requestPort, String fileName) {
        this.directoryIp = ipAddress;
        this.directoryPort = directoryPort;
        this.nodePort = requestPort;
        this.fileName = fileName;
        registerInDirectory();
        getData();
        startErrorDetection(); // entra em loop
        new ListenerThread(this).start();
        startAcceptingClients();
    }

    /**
     * Registers the StorageNode in the Directory.
     */
    private void registerInDirectory() {
        try {
            socket = new Socket(directoryIp, directoryPort);
            nodeIp = socket.getLocalAddress().getHostAddress();
            String message = "INSC " + nodeIp + " " + nodePort;

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(message);
        } catch (UnknownHostException e) {
            System.err.println("Error while establishing the connection to the directory.");
            System.err.println("Couldn't register to directory. Ending.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error while creating the socket to the directory.");
            System.err.println("Couldn't register to directory. Ending.");
            System.exit(1);
        }
        System.out.println("Successfully registered in the directory.");
    }

    private void getData() {
        File file = new File(fileName);
        if (file.isFile())
            getDataFromFile(file);
        else
            getDataFromNodes();
    }

    /**
     * Uploads data from existing file.
     */
    private void getDataFromFile(File file) {
        try {
            byte[] fileContents = Files.readAllBytes(file.toPath());
            for (int i = 0; i < DATALENGTH; i++)
                data[i] = new CloudByte(fileContents[i]);
            System.out.println("Data uploaded from file.");
        } catch (IOException e) {
            System.err.println("Error reading data from file.");
            System.out.println("Attempting to get data from other nodes...");
            getDataFromNodes();
        }
    }

    /**
     * Downloads data from other StorageNodes
     */
    private void getDataFromNodes() {
        System.out.println("Obtaining data from nodes...");
        LinkedList<String> nodes = new LinkedList();
        try {
            nodes = getNodes();
        } catch (IOException e) {
            System.err.println("Couldn't acquire nodes to obtain data. Ending.");
            System.exit(1);
        }
        SynchronizedList<ByteBlockRequest> list = new SynchronizedList();
        for (int i = 0; i < DATALENGTH; i += DEFAULTBLOCKLENGTH)
            list.put(new ByteBlockRequest(i, DEFAULTBLOCKLENGTH));

        int numOfNodes = nodes.size();
        ByteBlockRequesterThread[] bbrtArray = new ByteBlockRequesterThread[numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            String[] args = nodes.get(i).split(" ");
            String ip = args[1];
            int port = parseInt(args[2]);
            bbrtArray[i] = new ByteBlockRequesterThread(list, ip, port, this);
            bbrtArray[i].start();
        }
        try {
            for (ByteBlockRequesterThread bbrt : bbrtArray) {
                bbrt.join(); //isto
            }
        } catch (InterruptedException e) {
            System.err.println("StorageNode: Interrupted while joining the ByteBlockRequesterThreads.");
        }
        System.out.println("Data successfully obtained from nodes.");
    }

    private LinkedList<String> getNodes() throws IOException {
        out.println("nodes");
        LinkedList<String> nodes = new LinkedList();
        while (true) {
            String line = in.readLine();
            if (line.equals("end"))
                break;
            nodes.add(line);
        }
        nodes.removeIf(s -> s.equals("node " + nodeIp + " " + nodePort)); //remove-se a si próprio
        System.out.println("Nodes to connect in order to obtain data: " + nodes);
        return nodes;
    }

    /**
     * Starts error detection threads.
     */
    private void startErrorDetection() {
        errorDetectionThreads = new ErrorDetectionThread[NUMERRORDETECTIONTHREADS];
        ByteLocker bl = new ByteLocker();
        for (int i = 0; i < NUMERRORDETECTIONTHREADS; i++) {
            int startIndex = (DATALENGTH / NUMERRORDETECTIONTHREADS) * i;
            errorDetectionThreads[i] = new ErrorDetectionThread(this, startIndex, bl);
            errorDetectionThreads[i].start();
        }
    }

    /**
     * Detects errors in bytes position through position+length.
     *
     * @param position
     * @param length
     */
    void errorDetection(int position, int length) {
        for (int i = position; i < position + length; i++) {
            CloudByte cb = data[i];
            if (!cb.isParityOk()) {
                System.err.println("Error detected in byte " + i + ".");
                errorCorrection(i);
            }
        }
    }

    /**
     * Corrects error in byte given its position.
     *
     * @param index
     */
    void errorCorrection(int index) {
        LinkedList<String> nodes = new LinkedList();

        try {
            nodes = getNodes();
        } catch (IOException e) {
            System.err.println("Couldn't acquire nodes to correct detected errors.");
            return;
        }
        CountDownLatch cdl = new CountDownLatch(2);
        ByteBlockRequest bbr = new ByteBlockRequest(index, 1);
        int numOfNodes = nodes.size();

        ErrorCorrectionThread[] ectArray = new ErrorCorrectionThread[numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            String[] args = nodes.get(i).split(" ");
            String ip = args[1];
            int port = parseInt(args[2]);
            ectArray[i] = new ErrorCorrectionThread(ip, port, this, cdl, bbr);
            ectArray[i].start();
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            System.err.println("StorageNode: Interrupted while awaiting the ErrorCorrectionThreads.");
        }

        CloudByte[] cbs = new CloudByte[2];
        int i=0;
        for(ErrorCorrectionThread ect : ectArray) {
            if(ect.getReceivedByte() != null) {
                cbs[i] = ect.getReceivedByte();
                i++;
            }
        }

        if(!cbs[0].equals(cbs[1]))
            System.err.println("StorageNode: Couldn't correct the detected error.");
        else {
            setEllement(index, cbs[0]);
            System.out.println("Byte " + index + " was successfully corrected.");
        }

    }

    /**
     * Accepts queries from clients.
     *
     * @throws IOException
     */
    private void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(nodePort);
            try {
                System.out.println("Waiting for clients...");
                while (true) {
                    Socket socket = ss.accept(); // tanto para cliente GUI como para ByteBlockRequesterThreads
                    System.out.println("New client connection established with: " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
                    new DealWithRequestsNode(socket, this).start();
                }
            } finally {
                ss.close();
            }
        } catch (IOException e) {
            System.err.println("Error while opening the server socket.");
        }
    }

    /**
     * Gets CloudByte given its index.
     *
     * @param index
     * @return CloudByte at the given index.
     */
    synchronized CloudByte getElementFromData(int index) {
        return data[index];
    }

    /**
     * Sets a CloudByte at the given index.
     *
     * @param array
     * @param startIndex
     * @param length
     */
    synchronized void setDataWithArray(CloudByte[] array, int startIndex, int length) {
        for (int i = 0; i < length; i++)
            data[startIndex + i] = array[i];
    }

    synchronized void setEllement(int index, CloudByte cb) {
        data[index] = cb;
    }

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4)
            throw new IllegalArgumentException("Invalid arguments!");
        if(parseInt(args[1]) < 0 || parseInt(args[2]) < 0)
            throw new IllegalArgumentException("Invalid port number!");

        StorageNode storageNode = (args.length == 4) ? new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), args[3]) :
                new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), "");
    }
}
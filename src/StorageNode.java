import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static java.lang.Integer.parseInt;

/**
 * Class for the Storage Nodes.
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

    private CloudByte[] data;
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
        new listenThread().start();
        startAcceptingClients();
    }

    private void getData(){
        File file = new File(fileName);
        if(file.isFile())
            getDataFromFile(file);
        else
            getDataFromNodes();
    }

    /**
     * Gets CloudByte given its index.
     * @param index
     * @return CloudByte at the given index.
     */
    synchronized CloudByte getElementFromData(int index) {
        return data[index];
    }

    /**
     * Sets a CloudByte at the given index.
     * @param index
     * @param cloudByte
     */
    synchronized void setElementData(int index, CloudByte cloudByte) {
        this.data[index] = cloudByte;
    }

    /**
     * Starts error detection threads.
     */
    private void startErrorDetection() {
        errorDetectionThreads = new ErrorDetectionThread[NUMERRORDETECTIONTHREADS];
        for(int i=0; i<NUMERRORDETECTIONTHREADS; i++) {
            int startIndex = (DATALENGTH/NUMERRORDETECTIONTHREADS)*i;
            errorDetectionThreads[i] = new ErrorDetectionThread(this, startIndex);
            errorDetectionThreads[i].start();
        }
    }

    /**
     * Console reader. Checks for valid commands inputted into the console.
     * Valid commands include:
     * ERROR x : Where x is the target byte to be corrupted
     */
    private class listenThread extends Thread {

        @Override
        public void run() {
            Scanner sc = new Scanner(System.in);
            while(true){ //isto pode precisar de um try catch (espera por coisas serem inseridas na consola)
                //talvez fazer try catch do sc.next();
                String command = sc.nextLine();
                String[] args = command.split(" ");
                if(args.length == 2) {
                    try {
                        int position = parseInt(args[1]);
                        if (args[0].equals("ERROR") && position >= 0 && position < DATALENGTH) {
                            CloudByte cb = data[position];
                            cb.makeByteCorrupt();
                            System.out.println("Error injected in position " + position + " : " + cb);
                        } else System.err.println("Command not found.");
                    } catch (NumberFormatException e) { System.err.println("Invalid command arguments."); }
                } else System.err.println("Command not found.");
            }
        }
    }

    /**
     * Uploads data from existing file.
     */
    private void getDataFromFile(File file) {
            try {
                data = new CloudByte[DATALENGTH];
                byte[] fileContents = Files.readAllBytes(file.toPath());
                for(int i = 0; i < DATALENGTH; i++)
                    data[i] = new CloudByte(fileContents[i]);
                System.out.println("Data uploaded from file.");
            } catch (IOException e) {
                System.err.println("Error reading data from file.");
                System.out.println("Attempting to get data from other nodes...");
                getDataFromNodes();
            }
    }

    private LinkedList<String> getNodes() throws IOException {
        out.println("nodes");
        LinkedList<String> nodes = new LinkedList();
        while(true){
            String line = in.readLine();
            if(line.equals("end"))
                break;
            nodes.add(line);
        }
        return nodes;
    }

    /**
     * Downloads data from other StorageNodes
     */
    private void getDataFromNodes(){
        LinkedList<String> nodes = new LinkedList();

        try {
            nodes = getNodes();
        } catch (IOException e) {
            System.err.println("Couldn't acquire nodes to obtain data. Ending.");
            System.exit(1);
        }

        SynchronizedList<ByteBlockRequest> list = new SynchronizedList();
        for(int i = 0; i<DATALENGTH; i+=DEFAULTBLOCKLENGTH) {
            try {
                list.put(new ByteBlockRequest(i, DEFAULTBLOCKLENGTH));
            } catch (InterruptedException e) {
                System.err.println("Interrupted while adding ByteBlockRequest to list. Ending.");
                System.exit(1); //ATENÇÃO A ESTE SYSTEM EXIT.
            }
        }

        CountDownLatch cdl = new CountDownLatch(DATALENGTH/DEFAULTBLOCKLENGTH);

        for(String line : nodes){
            String[] args = line.split(" ");
            String ip = args[1];
            int port = parseInt(args[2]);
            if(!(ip.equals(nodeIp) && port == nodePort))
                new ByteBlockRequesterThread(cdl, list, ip, port, data).start();
        }

        try {
            cdl.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //mecanismo para após o await? Usar thread pools?
        //System.exit(1); em caso de erro
    }

    /**
     * Registers the StorageNode in the Directory.
     */
    private void registerInDirectory() {
        try {
            socket = new Socket(directoryIp, directoryPort);
            nodeIp = socket.getLocalAddress().getHostAddress();
            String message = "INSC " + nodeIp + " " + nodePort;

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
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
    }

    /**
     * Detects errors in bytes position through position+length.
     * @param position
     * @param length
     */
    private void errorDetection(int position, int length) {
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
     * @param position
     */
    void errorCorrection(int position) {
        //TODO
    }

    public static void main(String[] args) {
        if(args.length < 3 || args.length > 4)
            throw new IllegalArgumentException("Invalid arguments!");
        StorageNode storageNode = (args.length == 4) ? new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), args[3]) :
                new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), null);
        // TODO
    }

    /**
     * Nested Class to deal with client queries and node queries.
     */
    private class DealWithRequests extends Thread{
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket socket;

        public DealWithRequests(Socket socket) throws IOException {
            this.socket = socket;
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        }

        private void serve() throws ClassNotFoundException, IOException {
            while (true) {
                ByteBlockRequest bbr = (ByteBlockRequest) in.readObject();

                int startIndex = bbr.getStartIndex();
                int length = bbr.getLength();

                errorDetection(startIndex, length);

                CloudByte[] requestedData = new CloudByte[length];
                for(int i = 0; i < length; i++)
                    requestedData[i] = data[i+startIndex];

                out.writeObject(requestedData);
            }
        }

        @Override
        public void run() {
            try {
                serve();
            } catch (IOException e) {
                System.err.println("Client disconnected unexpectedly.");
            }
            catch (ClassNotFoundException e) {
                System.err.println("Error while reading request.");
            }
        }
    }

    /**
     * Accepts queries from clients.
     * @throws IOException
     */
    private void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(nodePort);
            try {
                System.out.println("Waiting for clients...");
                while(true) {
                    Socket socket = ss.accept(); // tanto para cliente GUI como para ByteBlockRequesterThreads
                    System.out.println("New client connection established.");
                    new DealWithRequests(socket).start();
                }
            } finally {
                ss.close();
            }
        } catch (IOException e) {
            System.err.println("Error while opening the server socket.");
        }
    }
}
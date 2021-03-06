package StorageNodePackage;

import DataStructures.*;

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
    private BufferedReader in;
    private PrintWriter out;

    public static final int DATA_LENGTH = 1000000;
    public static final int DEFAULT_BLOCK_LENGTH = 100;

    private final CloudByte[] data = new CloudByte[DATA_LENGTH];
    private static final int NUM_ERROR_DETECTION_THREADS = 2;

    public StorageNode(String ipAddress, int directoryPort, int requestPort, String fileName) {
        this.directoryIp = ipAddress;
        this.directoryPort = directoryPort;
        this.nodePort = requestPort;
        this.fileName = fileName;
        registerInDirectory();
        getData();
        startErrorDetection();
        new ErrorInjectionThread(this).start();
        startAcceptingClients();
    }

    synchronized CloudByte getElementFromData(int index) {
        return data[index];
    }

    synchronized void setElement(int index, CloudByte cb) {
        data[index] = cb;
    }

    private void registerInDirectory() {
        try {
            Socket socket = new Socket(directoryIp, directoryPort);
            nodeIp = socket.getLocalAddress().getHostAddress();
            String message = "INSC " + nodeIp + " " + nodePort;

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(message);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error while establishing the connection to the directory.\nCouldn't register to directory");
        } catch (IOException e) {
            throw new RuntimeException("Error while creating the socket to the directory.\nCouldn't register to directory.");
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

    private void getDataFromFile(File file) {
        System.out.println("Obtaining data from file...");
        try {
            byte[] fileContents = Files.readAllBytes(file.toPath());
            for (int i = 0; i < DATA_LENGTH; i++)
                data[i] = new CloudByte(fileContents[i]);
            System.out.println("Data uploaded from file.");
        } catch (IOException e) {
            System.err.println("Error reading data from file.");
            System.out.println("Attempting to get data from other nodes...");
            getDataFromNodes();
        }
    }

    private void getDataFromNodes() {
        System.out.println("Obtaining data from nodes...");
        LinkedList<String> nodes;
        try {
            nodes = getNodes();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't acquire nodes to obtain data.");
        }
        SynchronizedList<ByteBlockRequest> list = new SynchronizedList<>();
        for (int i = 0; i < DATA_LENGTH; i += DEFAULT_BLOCK_LENGTH)
            list.put(new ByteBlockRequest(i, DEFAULT_BLOCK_LENGTH));

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
                bbrt.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while joining the ByteBlockRequesterThreads.");
        }
        System.out.println("Data successfully obtained from nodes.");
    }

    private LinkedList<String> getNodes() throws IOException {
        out.println("nodes");
        LinkedList<String> nodes = new LinkedList<>();
        while (true) {
            String line = in.readLine();
            if (line.equals("end"))
                break;
            nodes.add(line);
        }
        nodes.removeIf(s -> s.equals("node " + nodeIp + " " + nodePort));
        System.out.println("Nodes to connect in order to obtain data: " + nodes);
        return nodes;
    }

    private void startErrorDetection() {
        ErrorDetectionThread[] errorDetectionThreads = new ErrorDetectionThread[NUM_ERROR_DETECTION_THREADS];
        ByteLocker bl = new ByteLocker();
        for (int i = 0; i < NUM_ERROR_DETECTION_THREADS; i++) {
            int startIndex = (DATA_LENGTH / NUM_ERROR_DETECTION_THREADS) * i;
            errorDetectionThreads[i] = new ErrorDetectionThread(this, startIndex, bl);
            errorDetectionThreads[i].start();
        }
    }

    /**
     * Detects errors in bytes at "position" through "position"+"length". Used in client queries.
     */
    void errorDetection(int position, int length) {
        for (int i = position; i < position + length; i++) {
            CloudByte cb = data[i];
            if (!cb.isParityOk()) {
                System.err.println("Error detected in byte " + i + ": " + cb);
                errorCorrection(i);
            }
        }
    }

    void errorCorrection(int index) {
        LinkedList<String> nodes;
        try {
            nodes = getNodes();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't acquire nodes to correct detected errors.");
        }

        CountDownLatch cdl = new CountDownLatch(2);
        ByteBlockRequest bbr = new ByteBlockRequest(index, 1);

        int numOfNodes = nodes.size();
        if(numOfNodes < 2)
            throw new RuntimeException("Correction couldn't be established due to insufficient number of nodes.");

        ErrorCorrectionThread[] ectArray = new ErrorCorrectionThread[numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            String[] args = nodes.get(i).split(" ");
            String ip = args[1];
            int port = parseInt(args[2]);
            try {
                ectArray[i] = new ErrorCorrectionThread(ip, port, cdl, bbr);
                ectArray[i].start();
            } catch (IOException e) {
                System.err.println("An ErrorCorrectionThread failed while connecting to its corresponding node.");
            }
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while awaiting the ErrorCorrectionThreads.");
        }
        correctByteWithOtherNodes(index, ectArray);
    }

    private void correctByteWithOtherNodes(int index, ErrorCorrectionThread[] ectArray) {
        CloudByte[] cbs = new CloudByte[2];
        int i=0;
        for(ErrorCorrectionThread ect : ectArray) {
            if(ect.getReceivedByte() != null) {
                cbs[i] = ect.getReceivedByte();
                i++;
            }
        }

        if(!cbs[0].equals(cbs[1]))
            throw new RuntimeException("Couldn't correct the detected error.");
        else {
            setElement(index, cbs[0]);
            System.out.println("Error corrected in byte " + index + ": " + cbs[0]);
        }
    }

    private void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(nodePort);
            System.out.println("Waiting for clients...");
            while(!ss.isClosed()) {
                try {
                    Socket socket = ss.accept();
                    new DealWithRequestsNode(socket, this).start();
                    System.out.println("New connection established.");
                } catch (IOException e){
                    System.err.println("An error occurred while establishing the connection to client/node.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while opening the server socket.");
        }
    }

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4)
            throw new IllegalArgumentException("Invalid arguments!");
        int dPort = parseInt(args[1]);
        int nPort = parseInt(args[2]);
        if(dPort < 0 || nPort < 0)
            throw new IllegalArgumentException("Invalid port number!");
        if(args.length == 4)
            new StorageNode(args[0], dPort, nPort, args[3]);
        else
            new StorageNode(args[0], dPort, nPort, "");
    }
}
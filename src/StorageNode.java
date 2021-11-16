import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

/**
 * Class for the Storage Nodes.
 * @author Olga Silva & Samuel Correia
 */
public class StorageNode {

    private final String directoryIp;
    private final int directoryPort;
    private final int nodePort;
    private final String fileName;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    //colocadas a final, a não ser que futuramente seja necessário alterar nalguma parte do código

    public static final int DATALENGTH = 1000000;

    private CloudByte[] data;
    private ErrorDetectionThread[] errorDetectionThreads;
    private static final int NUMERRORDETECTIONTHREADS = 2;

    public StorageNode(String ipAddress, int directoryPort, int requestPort, String fileName) {
        this.directoryIp = ipAddress;
        this.directoryPort = directoryPort;
        this.nodePort = requestPort;
        this.fileName = fileName;

        this.registerInDirectory();

        if(new File(fileName).isFile())
            this.getDataFromFile();
        else
            this.getDataFromNodes();

        this.startErrorDetection(); // entra em loop

        Thread listener = new listenThread();
        listener.start();

        startAcceptingClients();

    }

    /**
     * Gets CloudByte given its index.
     * @param index
     * @return CloudByte at the given index.
     */
    public CloudByte getElementFromData(int index) {
        return data[index];
    }

    /**
     * Sets a CloudByte at the given index.
     * @param index
     * @param cloudByte
     */
    public void setElementData(int index, CloudByte cloudByte) {
        this.data[index] = cloudByte;
    }

    /**
     * Starts error detection threads.
     */
    private void startErrorDetection() {
        errorDetectionThreads = new ErrorDetectionThread[NUMERRORDETECTIONTHREADS];
        for(int i=0; i<NUMERRORDETECTIONTHREADS; i++) {
            int startIndex = (DATALENGTH/NUMERRORDETECTIONTHREADS)*i;
            System.out.println(startIndex);
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
    public void getDataFromFile() {
            try {
                data = new CloudByte[DATALENGTH];
                byte[] fileContents = Files.readAllBytes(new File(fileName).toPath());
                for(int i = 0; i < DATALENGTH; i++)
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
    public void getDataFromNodes(){
        //TODO
        //System.exit(1); em caso de erro
    }

    /**
     * Registers the StorageNode in the Directory.
     */
    public void registerInDirectory() {
        try {
            String myIp = InetAddress.getLocalHost().getHostAddress();
            InetAddress directoryIpAddr = InetAddress.getByName(directoryIp);

            String message = "INSC " + myIp + " " + nodePort;
            System.out.println(message);
            socket = new Socket(directoryIpAddr, directoryPort);

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
    public void errorDetection(int position, int length) {
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
    public void errorCorrection(int position) {
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
     * Nested Class to deal with client queries.
     */
    private class DealWithClient extends Thread{
        private BufferedReader in;
        private ObjectOutputStream out;
        private Socket socket;

        public DealWithClient(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
        }

        private void serve() throws IOException {
            while (true) {
                String str = in.readLine();

                String args[] = str.split(" ");
                int startIndex = parseInt(args[0]);
                int length = parseInt(args[1]);

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
        }
    }

    /**
     * Accepts queries from clients.
     * @throws IOException
     */
    public void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(nodePort);
            try {
                System.out.println("Waiting for clients...");
                while(true) {
                    Socket socket = ss.accept();
                    System.out.println("New client connection established.");
                    new DealWithClient(socket).start();
                }
            } finally {
                ss.close();
            }
        } catch (IOException e) {
            System.err.println("Error while opening the server socket to the directory.");
        }
    }

}
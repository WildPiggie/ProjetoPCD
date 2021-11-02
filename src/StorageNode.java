import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class StorageNode {

    private String ipAddress;
    private int directoryPort;
    private int requestPort;
    private String fileName;

    private static final int DATALENGTH = 1000000;

    private CloudByte[] data;
    //private ErrorDetectionThread[] thread; FASE 5


    public StorageNode(String ipAddress, int directoryPort, int requestPort, String fileName) {
        this.ipAddress = ipAddress;
        this.directoryPort = directoryPort;
        this.requestPort = requestPort;
        this.fileName = fileName;

        if(fileName != null)
            this.getDataFromFile();
        else
            this.getDataFromNodes();
        
        this.registerInDirectory();

        Thread listener = new listenThread();
        listener.start();
    }



    private class listenThread extends Thread{

        @Override
        public void run() {
            Scanner sc = new Scanner(System.in);
            while(true){
                //talvez fazer try catch do sc.next();
                String command = sc.nextLine();
                String[] args = command.split(" ");
                if(args.length == 2) {
                    try {
                        int position = parseInt(args[1]);
                        if (args[0].equals("ERROR") && position >= 0 && position < DATALENGTH) {
                            //System.out.println("Antes: " + data[position]);
                            data[position].makeByteCorrupt();
                            System.out.println("Error injected in position " + position);
                            //System.out.println("Depois: " + data[position]);
                        } else System.err.println("Command not found.");
                    } catch (NumberFormatException e) { System.err.println("Invalid command arguments."); }
                } else System.err.println("Command not found.");
            }
            //sc.close();
        }
    }

    /**
     * Uploads data from existing file or other StorageNodes.
     */
    public void getDataFromFile() {

            try {
                data = new CloudByte[DATALENGTH];
                byte[] fileContents = Files.readAllBytes(new File(fileName).toPath());
                for(int i=0; i<DATALENGTH; i++)
                    data[i] = new CloudByte(fileContents[i]);
                System.out.println("Data uploaded from file.");
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }

            /* quem verifica isto são 2 processos ligeiros
            for(byte b : fileContents) {
                CloudByte cb = new CloudByte(b);
                if(!cb.isParityOk()) {
                    // existe problema nesta instância
                }
            }*/
    }

    public void getDataFromNodes(){
        //TODO
    }

    /**
     * Registers the StorageNode in the Directory.
     */
    public void registerInDirectory() {

        String message = "INCS " + ipAddress + " 8081" ;

        // TODO
    }

    /**
     * Answers the queries from remote clients.
     * @return
     */
    public String anwerQuery() {
        // TODO
        return null;
    }


    public static void main(String[] args) {

        StorageNode storageNode;

        if(args.length < 3 || args.length > 4)
            throw new IllegalArgumentException("Invalid arguments!");

        storageNode = (args.length == 4) ? new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), args[3]) :
                new StorageNode(args[0], parseInt(args[1]), parseInt(args[2]), null);

        // TODO

    }

}

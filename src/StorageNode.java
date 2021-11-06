import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

/**
 * Class for the Storage Nodes.
 * @author Olga Silva & Samuel Correia
 */
public class StorageNode {

    private final String ipAddress;
    private final int directoryPort;
    private final int requestPort;
    private final String fileName;

    //colocadas a final, a não ser que futuramente seja necessário alterar nalguma parte do código

    private static final int DATALENGTH = 1000000;

    private CloudByte[] data;
    //private ErrorDetectionThread[] thread; FASE 5


    public StorageNode(String ipAddress, int directoryPort, int requestPort, String fileName) {
        this.ipAddress = ipAddress;
        this.directoryPort = directoryPort;
        this.requestPort = requestPort;
        this.fileName = fileName;

        this.registerInDirectory();

        if(fileName != null)
            this.getDataFromFile();
        else
            this.getDataFromNodes();

        this.startErrorDetection();

        Thread listener = new listenThread();
        listener.start();
    }

    private void startErrorDetection() {

        //TODO
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
     * Uploads data from existing file.
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

    /**
     * Downloads data from other StorageNodes
     */
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
     * @return answer String provided
     */
    public String answerQuery() {
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

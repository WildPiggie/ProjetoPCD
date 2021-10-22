import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.lang.Integer.parseInt;

public class StorageNode {

    private String ipAddress;
    private int directoryPort;
    private int requestPort;
    private String fileName;

    private static final int DATALENGTH = 1000000;

    private CloudByte[] data;
    private Thread thread;


    public StorageNode(String ipAddress, int directoryPort,int requestPort, String fileName) {
        this.ipAddress = ipAddress;
        this.directoryPort = directoryPort;
        this.requestPort = requestPort;
        this.fileName = fileName;
    }

    /**
     * Uploads data from existing file or other StorageNodes.
     */
    public void dataUpload() {
        if(fileName != null) {
            try {
                data = new CloudByte[DATALENGTH];
                byte[] fileContents = Files.readAllBytes(new File(fileName).toPath());
                for(int i=0; i<1000000; i++)
                    data[i] = new CloudByte(fileContents[i]);

            } catch (IOException e) {
                e.printStackTrace();
            }

            /* quem verifica isto são 2 processos ligeiros
            for(byte b : fileContents) {
                CloudByte cb = new CloudByte(b);
                if(!cb.isParityOk()) {
                    // existe problema nesta instância
                }
            }*/

        } else {
            // descarregamento de dados a partir de outro ficheiro
        }
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

        storageNode.dataUpload();
        storageNode.registerInDirectory();

        // TODO

    }

}

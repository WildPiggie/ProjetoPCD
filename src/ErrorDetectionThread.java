/**
 * Class for the Error Detection Thread. Used by the nodes in order to check for errors in their data.
 * @author Olga Silva & Samuel Correia
 */
public class ErrorDetectionThread extends Thread {

    private StorageNode storageNode;
    private int startIndex;
    private static final int DATALENGTH = 1000000;

    public ErrorDetectionThread(StorageNode storageNode, int startIndex) {
        this.storageNode = storageNode;
        this.startIndex = startIndex;
    }

    @Override
    public void run() {
        int i = startIndex;
        while(!interrupted()) {
            CloudByte cb = storageNode.getElementFromData(i);
            if(!cb.isParityOk()) {
                System.err.println("Error detected in byte " + i+1 + ".");
                storageNode.errorCorrection(i);
            }
            if(++i == DATALENGTH) i = 0;
        }
    }
}

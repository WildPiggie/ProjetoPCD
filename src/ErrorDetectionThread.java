/**
 * Class for the Error Detection Thread. Used by the nodes in order to check for errors in their data.
 * @author Olga Silva & Samuel Correia
 */
public class ErrorDetectionThread extends Thread {

    private StorageNode storageNode;
    private int startIndex;

    public ErrorDetectionThread(StorageNode storageNode, int startIndex) {
        this.storageNode = storageNode;
        this.startIndex = startIndex;
    }

    @Override
    public void run() {
        int i = startIndex;
        while(!interrupted()) {
            CloudByte cb = storageNode.getElementFromData(i);
            try {
                sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Error detection thread interrupted while sleeping.");
            }
            if(!cb.isParityOk()) {
                System.err.println("Error detected in byte " + i + ".");
                storageNode.errorCorrection(i);
            }
            if(++i == StorageNode.DATALENGTH) i = 0;
        }
    }
}

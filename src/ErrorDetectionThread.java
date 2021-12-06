/**
 * Class for the Error Detection Thread. Used by the nodes in order to check for errors in their data.
 *
 * @author Olga Silva & Samuel Correia
 */
public class ErrorDetectionThread extends Thread {

    private StorageNode storageNode;
    private int startIndex;
    private ByteLocker bl;

    public ErrorDetectionThread(StorageNode storageNode, int startIndex, ByteLocker bl) {
        this.storageNode = storageNode;
        this.startIndex = startIndex;
        this.bl = bl;
    }

    @Override
    public void run() {
        int i = startIndex;
        while (!interrupted()) {
            CloudByte cb = storageNode.getElementFromData(i);
            try {
                sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Error detection thread interrupted while sleeping.");
                return;
            }

            if (!cb.isParityOk() && bl.lock(i)) {
                System.err.println("Error detected in byte " + i + ": " + cb);
                storageNode.errorCorrection(i);
                bl.unlock(i);
            }

            if (++i == StorageNode.DATALENGTH) i = 0;
        }
    }
}

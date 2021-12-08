/**
 * Used by the nodes in order to check for corrupted CloudBytes in their data.
 * Sequentially looks through all CloudBytes of a given node, detecting any errors that may exist.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ErrorDetectionThread extends Thread {

    private final StorageNode storageNode;
    private final int startIndex;
    private final ByteLocker bl;

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
                sleep(1); //Sleep induced to facilitate the observation of errors being detected.
            } catch (InterruptedException e) {
                System.err.println("Error detection thread interrupted while sleeping.");
                return;
            }
            if (!cb.isParityOk() && bl.lock(i)) {
                System.err.println("Error detected in byte " + i + ": " + cb);
                storageNode.errorCorrection(i);
                bl.unlock(i);
            }
            if (++i == StorageNode.DATA_LENGTH) i = 0;
        }
    }
}

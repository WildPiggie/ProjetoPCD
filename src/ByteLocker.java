import java.util.ArrayList;

/**
 * Explanation
 *
 */

public class ByteLocker {

    private ArrayList<Integer> lockedByte = new ArrayList();

    public synchronized boolean lock(int index) {
        if(lockedByte.contains(index))
            return false;
        return lockedByte.add(index);
    }

    public synchronized void unlock(int index) {
        lockedByte.remove(index);
    }
}

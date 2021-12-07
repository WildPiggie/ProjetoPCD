import java.util.ArrayList;

/**
 *
 * Used to avoid that two or more threads try to correct the same CloudByte of a certain StorageNode.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ByteLocker {

    private final ArrayList lockedByte = new ArrayList<Integer>();

    public synchronized boolean lock(int index) {
        if(lockedByte.contains(index))
            return false;
        return lockedByte.add(index);
    }

    public synchronized void unlock(int index) {
        lockedByte.remove(index);
    }
}

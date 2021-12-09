package DataStructures;

import java.util.ArrayList;

/**
 * Holds a list containing indexes of all CloudBytes currently being corrected, of a given node.
 * Used to avoid that two or more threads try to correct the same CloudByte of a certain StorageNodePackage.StorageNode.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ByteLocker {

    private final ArrayList<Integer> lockedByte = new ArrayList<>();

    public synchronized boolean lock(int index) {
        if(lockedByte.contains(index))
            return false;
        return lockedByte.add(index);
    }

    public synchronized void unlock(int index) {
        System.out.println(lockedByte);
        lockedByte.remove((Integer)index);
        System.out.println(lockedByte);
    }
}

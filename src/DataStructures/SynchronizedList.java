package DataStructures;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simple implementation of a synchronized list.
 *
 * @author Olga Silva & Samuel Correia
 */

public class SynchronizedList<T> implements Iterable<T> {

    private final LinkedList<T> list = new LinkedList<>();

    public synchronized void put(T t) {
        list.add(t);
    }

    /**
     * Removes and returns the first element from the list if the list is not empty.
     * Otherwise, returns null.
     */
    public synchronized T takeIfNotEmpty() {
        if (list.isEmpty())
            return null;
        return list.remove();
    }

    public void remove(T t) {
        list.remove(t);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

}
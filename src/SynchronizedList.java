import java.util.Iterator;
import java.util.LinkedList;

public class SynchronizedList<T> implements Iterable<T> {

    private LinkedList<T> list = new LinkedList<T>();

    public synchronized void put(T t) {
        list.add(t);
    }

    public synchronized T takeIfNotEmpty() {
        if (list.isEmpty())
            return null;
        return list.remove();
    }

    public boolean remove(T t) {
        return list.remove(t);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

}
import java.util.Iterator;
import java.util.LinkedList;

public class SynchronizedList<T> implements Iterable<T> {

    private LinkedList<T> list = new LinkedList<T>();


    public synchronized void put(T t) {
        list.add(t);
    }

    /*
    public T take() {
        lock.lock();
        while (list.isEmpty()) {
            try {
                emptyList.await();
            } catch (InterruptedException e) {
                lock.unlock();
                return null;
            }
        }
        T res = list.remove();
        lock.unlock();
        return res;
    }*/

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
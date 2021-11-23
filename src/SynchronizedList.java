import java.util.LinkedList;

public class SynchronizedList<T> {

    private LinkedList<T> list = new LinkedList<T>();
    private int capacity;

    public SynchronizedList() {
        this.capacity = -1;
    }

    public SynchronizedList(int capacity) {
        if(capacity <= 0)
            throw new IllegalArgumentException();
        this.capacity = capacity;
    }

    public synchronized void put(T t) throws InterruptedException {
        while (list.size() == capacity)
            wait();
        list.add(t);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (list.isEmpty())
            wait();
        T res = list.remove();
        if(capacity != -1)
            notifyAll();
        return res;
    }

    public synchronized T takeIfNotEmpty() throws InterruptedException {
        if(list.isEmpty())
            return null;
        T res = list.remove();
        if(capacity != -1)
            notifyAll();
        return res;
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}
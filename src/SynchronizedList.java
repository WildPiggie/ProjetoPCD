import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedList<T> {

    private LinkedList<T> list = new LinkedList<T>();
    private int capacity;
    private Lock lock = new ReentrantLock();
    private Condition fullList = lock.newCondition();
    private Condition emptyList = lock.newCondition();

    public SynchronizedList() {
        this.capacity = -1;
    }

    public SynchronizedList(int capacity) {
        if(capacity <= 0)
            throw new IllegalArgumentException();
        this.capacity = capacity;
    }

    public void put(T t) throws InterruptedException {
        lock.lock();
        while (list.size() == capacity)
            fullList.await();
        list.add(t);
        emptyList.signalAll();
        lock.unlock();
    }

    public T take() throws InterruptedException {
        lock.lock();
        while (list.isEmpty())
            emptyList.await();
        T res = list.remove();
        if(capacity != -1)
            fullList.signalAll();
        lock.unlock();
        return res;
    }

    public T takeIfNotEmpty() {
        lock.lock();
        if(list.isEmpty()){
            lock.unlock();
            return null;
        }
        T res = list.remove();
        if(capacity != -1)
            fullList.signalAll();
        lock.unlock();
        return res;
    }

    //tive que separar o codigo porque a chamada ao isEmpty() nao e atomica
    public boolean isEmpty() {
        lock.lock();
        boolean empty = list.isEmpty();
        lock.unlock();
        return empty;
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}
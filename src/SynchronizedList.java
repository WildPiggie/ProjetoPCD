import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class SynchronizedList<T> implements Iterable<T> {

    private LinkedList<T> list = new LinkedList<T>();
    private Lock lock = new ReentrantLock();
    private Condition emptyList = lock.newCondition(); // preferivel a usar wait e notifyall?


    public void put(T t) {
        lock.lock();
        list.add(t);
        emptyList.signalAll();
        lock.unlock();
    }

    public T take() throws InterruptedException {
        lock.lock();
        while (list.isEmpty())
            emptyList.await();
        T res = list.remove();
        lock.unlock();
        return res;
    }

    public T takeIfNotEmpty() {
        lock.lock();
        if (list.isEmpty()) {
            lock.unlock();
            return null;
        }
        T res = list.remove();
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

    public boolean remove(T t) {
        return list.remove(t);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

}
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ByteLockerTest  {

    private ArrayList<Integer> lockedByte = new ArrayList();

    private synchronized void lock(int index) throws InterruptedException { //pode ser boolean se quisermos dar skip a bytes que estão a ser trabalhados
        while(lockedByte.contains(index))
            wait();
        lockedByte.add(index);
    }

    private synchronized void unlock(int index) throws InterruptedException {
        lockedByte.remove(index);
        notifyAll();
    }
}

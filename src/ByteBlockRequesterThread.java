import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ByteBlockRequesterThread extends Thread {

    private CountDownLatch cdl;
    private String ip;
    private int port;
    private Socket socket;
    //falta os in out
    private SynchronizedList list;

    public ByteBlockRequesterThread(CountDownLatch cdl, SynchronizedList list,String ip, int port) {
        this.cdl = cdl;
        this.list = list;
        this.ip = ip;
        this.port = port;
        //falta abrir os canais de comunicação
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            System.err.println("Error ...");
        }
    }

    @Override
    public void run() {
        while(!interrupted()) {
            //TODO
            try {
                list.take();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cdl.countDown();
        }
    }
}

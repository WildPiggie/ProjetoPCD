import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ByteBlockRequesterThread extends Thread {

    private String ip;
    private int port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SynchronizedList<ByteBlockRequest> list;
    private CloudByte[] data;
    private int counter = 0;


    public ByteBlockRequesterThread(SynchronizedList list,String ip, int port, CloudByte[] data) {
        this.list = list;
        this.ip = ip;
        this.port = port;
        this.data = data;

        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("ByteBlockRequesterThread: Error while connecting to node.");
        }
    }

    @Override
    public void run() {
        while(!interrupted()) { //!isEmpty()?
            try {
                ByteBlockRequest bbr = list.takeIfNotEmpty();
                if(bbr == null) break;
                out.writeObject(bbr);

                CloudByte[] cb = (CloudByte[]) in.readObject();

                synchronized (data) {  // mecanismo de coordenação?
                    for(int i = 0; i < bbr.getLength(); i++)
                        data[bbr.getStartIndex()+i] = cb[i];
                }

            } catch (InterruptedException e ) {
                System.err.println("ByteBlockRequesterThread: Interrupted while getting a ByteBlockRequest from list.");
                break;
            } catch (IOException e) {
                System.err.println("ByteBlockRequesterThread: Error while sending ByteBlockRequest.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            counter++;
        }
        System.out.println("Node " + ip + ":" + port + " obtained " + counter + " blocks.");
    }
}

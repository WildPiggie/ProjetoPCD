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
    private StorageNode node;
    private int counter = 0;


    public ByteBlockRequesterThread(SynchronizedList list,String ip, int port, StorageNode node) {
        this.list = list;
        this.ip = ip;
        this.port = port;
        this.node = node;

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

                node.setDataWithArray(cb, bbr.getStartIndex(), bbr.getLength());

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

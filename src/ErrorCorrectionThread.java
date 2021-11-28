import java.io.*;
import java.net.Socket;

public class ErrorCorrectionThread extends Thread {

    private String ip;
    private int port;
    private StorageNode node;
    private CountDownLatch cdl;
    private ByteBlockRequest bbr;
    private CloudByte receivedByte;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public ErrorCorrectionThread(String ip, int port, StorageNode node, CountDownLatch cdl, ByteBlockRequest bbr) {
        this.ip = ip;
        this.port = port;
        this.node = node;
        this.cdl = cdl;
        this.bbr = bbr;
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("ByteBlockRequesterThread: Error while connecting to node.");
        }
    }

    public CloudByte getReceivedByte(){
        return receivedByte;
    }

    @Override
    public void run() {
        try {
            out.writeObject(bbr);
            CloudByte[] cb = (CloudByte[]) in.readObject();
            receivedByte = cb[0];
        } catch (IOException e) {
            System.err.println("ErrorCorrectionThread: Error while sending ByteBlockRequest.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        cdl.countDown();
    }
}
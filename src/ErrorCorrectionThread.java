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
    }

    @Override
    public void run() {
        try {
            createSocket();
        } catch (IOException e) {
            System.err.println("Error while connecting to node.");
            return;
        }
        obtainByte();
    }

    private void createSocket() throws IOException {
        socket = new Socket(ip, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * uhgfyx
     */
    private void obtainByte() {
        try {
            out.writeObject(bbr);
            CloudByte[] cb = (CloudByte[]) in.readObject();
            receivedByte = cb[0];
        } catch (IOException e) {
            System.err.println("Error while sending or receiving ByteBlockRequest.");
            return;
        } catch (ClassNotFoundException e) {
            System.err.println("Error while receiving ByteBlockRequest.");
            return;
        }
        cdl.countDown();
    }

    public CloudByte getReceivedByte(){
        return receivedByte;
    }
}
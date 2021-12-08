import java.io.*;
import java.net.Socket;

/**
 * Thread used to handle the correction of a corrupted CloudByte.
 * It searches for the correct CloudByte on its corresponding node and stores it in a variable
 * to later be used in correction.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ErrorCorrectionThread extends Thread {

    private final CountDownLatch cdl;
    private final ByteBlockRequest bbr;
    private CloudByte receivedByte;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;


    public ErrorCorrectionThread(String ip, int port, CountDownLatch cdl, ByteBlockRequest bbr) throws IOException {
        this.cdl = cdl;
        this.bbr = bbr;
        Socket socket = new Socket(ip, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Thread that handles client and node queries.
 *
 * @author Olga Silva & Samuel Correia
 */

public class DealWithRequestsNode extends Thread {
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final StorageNode node;

    public DealWithRequestsNode(Socket socket, StorageNode node) throws IOException {
        this.node = node;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    private void serve() throws ClassNotFoundException, IOException {
        while(!isInterrupted()) {
            ByteBlockRequest bbr = (ByteBlockRequest) in.readObject();

            int startIndex = bbr.getStartIndex();
            int length = bbr.getLength();

            node.errorDetection(startIndex, length);

            CloudByte[] requestedData = new CloudByte[length];
            for (int i = 0; i < length; i++)
                requestedData[i] = node.getElementFromData(i + startIndex);

            out.writeObject(requestedData);
        }
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (IOException e) {
            System.err.println("Client disconnected unexpectedly.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error while reading request.");
        }
    }
}
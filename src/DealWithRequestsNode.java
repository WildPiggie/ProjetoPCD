import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Nested Class to deal with client queries and node queries.
 */
public class DealWithRequestsNode extends Thread {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private StorageNode node;

    public DealWithRequestsNode(Socket socket, StorageNode node) throws IOException {
        this.socket = socket;
        this.node = node;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    private void serve() throws ClassNotFoundException, IOException {
        while (true) {
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
package StorageNodePackage;

import DataStructures.ByteBlockRequest;
import DataStructures.CloudByte;
import DataStructures.SynchronizedList;

import java.io.*;
import java.net.Socket;

/**
 * Thread used to handle a DataStructures.ByteBlockRequest retrieved from a shared list. It takes the DataStructures.ByteBlockRequest from the list,
 * sends it to its corresponding node and receives the corresponding block of CloudBytes.
 * Used when data is obtained through other nodes.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ByteBlockRequesterThread extends Thread {

    private final String ip;
    private final int port;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final SynchronizedList<ByteBlockRequest> list;
    private final StorageNode node;
    private int counter = 0;


    public ByteBlockRequesterThread(SynchronizedList<ByteBlockRequest> list, String ip, int port, StorageNode node) {
        this.list = list;
        this.ip = ip;
        this.port = port;
        this.node = node;

        try {
            Socket socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("StorageNodePackage.ByteBlockRequesterThread: Error while connecting to node.");
        }
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                ByteBlockRequest bbr = list.takeIfNotEmpty();
                if (bbr == null) break;
                out.writeObject(bbr);

                CloudByte[] cb = (CloudByte[]) in.readObject();
                for (int i = 0; i < cb.length; i++)
                    node.setElement(bbr.getStartIndex() + i, cb[i]);

            } catch (IOException e) {
                System.err.println("Error while sending or receiving DataStructures.ByteBlockRequest.");
                break;
            } catch (ClassNotFoundException e) {
                System.err.println("Error while receiving DataStructures.ByteBlockRequest.");
                break;
            }
            counter++;
        }
        System.out.println("Node " + ip + ":" + port + " obtained " + counter + " blocks.");
    }
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Integer.parseInt;

/**
 * Class used to manage node registration and queries about other nodes.
 * Nodes can use the directory to register themselves to the directory database
 * and discover other nodes that are also registered.
 *
 * @author Olga Silva & Samuel Correia
 */

public class Directory {

    private final int port;
    public SynchronizedList<String> nodes;

    public Directory(int port) {
        this.port = port;
        nodes = new SynchronizedList<>();
        startAcceptingClients();
    }

    private void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Directory launched. \nWaiting for nodes...");
            while(!ss.isClosed()) {
                try {
                    Socket socket = ss.accept();
                    new DealWithRequestsDir(socket, this).start();
                    System.out.println("New connection established.");
                } catch (IOException e){
                    System.err.println("An error occurred while establishing the connection to the node.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while opening the server socket.");
        }
    }

    public static void main(String[] args) {
        if(args.length != 1)
            throw new IllegalArgumentException("Invalid arguments!");
        if(parseInt(args[0]) < 0)
            throw new IllegalArgumentException("Invalid port number!");
        new Directory(parseInt(args[0]));
    }
}

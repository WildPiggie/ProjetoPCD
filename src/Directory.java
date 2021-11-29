import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Integer.parseInt;

public class Directory {

    private int port;
    public SynchronizedList<String> nodes;

    public Directory(int port) {
        this.port = port;
        nodes = new SynchronizedList<>();
        startAcceptingClients();
    }

    private void startAcceptingClients() {
        try {
            ServerSocket ss = new ServerSocket(port);
            try {
                System.out.println("Waiting for nodes...");
                while (true) {
                    Socket socket = ss.accept();
                    new DealWithRequestsDir(socket, this);
                    System.out.println("New node connection established with: " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
                }
            } finally {
                ss.close();
            }
        } catch (IOException e) {
            System.err.println("Error while opening the server socket.");
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) throw new IllegalArgumentException("Invalid arguments!");

        new Directory(parseInt(args[0]));
    }
}

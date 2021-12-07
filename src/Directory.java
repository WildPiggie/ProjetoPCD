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
                System.out.println("Directory launched. \nWaiting for nodes...");
                while (true) {
                    Socket socket = ss.accept();
                    System.out.println("New connection established with..."); // meter algo aqui
                    new DealWithRequestsDir(socket, this).start();
                }
            } finally {
                ss.close();
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

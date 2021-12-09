package DirectoryPackage;

import java.io.*;
import java.net.Socket;

/**
 * Thread that handles registrations and queries done to the directory.
 *
 * @author Olga Silva & Samuel Correia
 */

public class DealWithRequestsDir extends Thread {
    private final BufferedReader in;
    private final PrintWriter out;
    private final Directory dir;
    private String currentNode;

    public DealWithRequestsDir(Socket socket, Directory dir) throws IOException {
        this.dir = dir;
        currentNode = null;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    private void serve() throws ClassNotFoundException, IOException {
        while (!isInterrupted()) {
            String msg = in.readLine();
            if(msg.equals("nodes")) {
                sendRegisteredNodes();
            } else {
                String[] args = msg.split(" ");
                if(args.length == 3 && args[0].equals("INSC")) {
                    registerNode(args);
                } else
                    System.err.println("Invalid command!");
            }
        }
    }

    private void sendRegisteredNodes() {
        for(String node : dir.nodes)
            out.println(node);
        out.println("end");
    }

    private void registerNode(String[] args) {
        currentNode = "node " + args[1] + " " + args[2];
        dir.nodes.put(currentNode);
        System.out.println("Node " + args[1] + ":" + args[2] + " registered.");
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (IOException e) {
            System.err.println("StorageNodePackage.StorageNode disconnected. Removing " + currentNode + " from directory database.");
            dir.nodes.remove(currentNode);
        } catch (ClassNotFoundException e) {
            System.err.println("Error while reading request.");
        }
    }

}

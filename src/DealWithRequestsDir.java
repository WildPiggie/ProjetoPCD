import com.sun.jdi.request.StepRequest;

import java.io.*;
import java.net.Socket;

public class DealWithRequestsDir extends Thread {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private Directory dir;
    private String currentNode;

    public DealWithRequestsDir(Socket socket, Directory dir) throws IOException {
        this.socket = socket;
        this.dir = dir;
        currentNode = null;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    private void serve() throws ClassNotFoundException, IOException {
        while (true) {
            String msg = in.readLine();

            if(msg.equals("nodes")) {
                for(String node : dir.nodes)
                    out.println(node);
                out.println("end");
            } else {
                String[] args = msg.split(" ");
                if(args.length == 3 && args[0].equals("INSC") ) {
                    currentNode = "node " + args[1] + " " + args[2];
                    dir.nodes.put(currentNode);
                    System.out.println("Node " + args[1] + ":" + args[2] + " registered.");
                } else
                    System.err.println("Invalid command!");
            }
        }
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (IOException e) {
            System.err.println("StorageNode disconnected. Removing " + currentNode + " from directory database.");
            dir.nodes.remove(currentNode);
        } catch (ClassNotFoundException e) {
            System.err.println("Error while reading request.");
        }
    }

}

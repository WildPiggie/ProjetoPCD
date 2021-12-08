import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

/**
 * Class used for the client interface and fulfilling client queries.
 * Allows for manual data queries on a certain Node.
 *
 * @author Olga Silva & Samuel Correia
 */
public class DataClient {

    private static final int TF_COLUMNS = 10;
    private final JFrame frame;
    private JTextField position;
    private JTextField length;
    private JTextArea answer;
    private final String nodeIpAddress;
    private final int nodePort;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public DataClient(String nodeIpAddress, int nodePort) {
        this.nodeIpAddress = nodeIpAddress;
        this.nodePort = nodePort;

        connectToNode();

        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addFrameContent();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(700, 200);
        frame.setLocation(screenSize.width / 2 - frame.getWidth() / 2, screenSize.height / 2 - frame.getHeight() / 2);
        frame.setVisible(true);
    }

    private void connectToNode() {
        try {
            Socket socket = new Socket(InetAddress.getByName(nodeIpAddress), nodePort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to node while creating socket and/or streams.");
        }
    }

    private void addFrameContent() {
        frame.setLayout(new GridLayout(2, 1));

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 25));

        panel.add((new JLabel("Position:")));

        position = new JTextField(TF_COLUMNS);
        panel.add(position);

        panel.add(new JLabel("Length: "));

        length = new JTextField(TF_COLUMNS);
        panel.add(length);

        JButton search = new JButton("Search");
        search.addActionListener(a -> {
            int numPosition, numLength;
            try {
                numPosition = parseInt(position.getText());
                numLength = parseInt(length.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Wrong arguments!");
                return;
            }
            if (numPosition < 0 || numPosition >= StorageNode.DATA_LENGTH) {
                JOptionPane.showMessageDialog(frame, "Invalid position!");
                return;
            }
            if (numLength <= 0 || numPosition + numLength > StorageNode.DATA_LENGTH) {
                JOptionPane.showMessageDialog(frame, "Invalid length!");
                return;
            }
            try {
                out.writeObject(new ByteBlockRequest(numPosition, numLength));
            } catch (IOException e) {
                System.err.println("Error sending request.");
            }
            try {
                CloudByte[] data = (CloudByte[]) in.readObject();
                answer.setText(Arrays.toString(data));
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error reading received object.");
            }
        });

        panel.add(search);
        frame.add(panel);
        answer = new JTextArea("Answers will appear here...");
        answer.setEditable(false);
        answer.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(answer);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setSize(700, 100);
        frame.add(scroll, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        if (args.length != 2)
            throw new IllegalArgumentException("Invalid arguments!");
        if(parseInt(args[1]) < 0)
            throw new IllegalArgumentException("Invalid port number!");

        new DataClient(args[0], parseInt(args[1]));
    }
}
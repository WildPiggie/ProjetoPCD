import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

/**
 * Class for the DataClient interface. Allows for data queries on a certain Node.
 *
 * @author Olga Silva & Samuel Correia
 */
public class DataClient {

    private static final int TF_COLUMNS = 10;
    private JFrame frame;
    private JTextField position;
    private JTextField length;
    private JTextArea answer;
    private String nodeIpAddress;
    private int nodePort;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public DataClient(String nodeIpAddress, int nodePort) {
        this.nodeIpAddress = nodeIpAddress;
        this.nodePort = nodePort;

        connectToNode();

        //Interface
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        addFrameContent();

        //frame.setResizable(false);
        //frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(700, 200);
        frame.setLocation(screenSize.width / 2 - frame.getWidth() / 2, screenSize.height / 2 - frame.getHeight() / 2);
        frame.setVisible(true);
    }

    private void connectToNode() {
        try {
            socket = new Socket(InetAddress.getByName(nodeIpAddress), nodePort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error connecting to node while creating socket and/or streams.");
            System.exit(1);
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
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                int numPosition = 0;
                int numLength = 0;
                try {
                    numPosition = parseInt(position.getText());
                    numLength = parseInt(length.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Wrong arguments!");
                    return;
                }
                if (numPosition < 0 || numPosition >= StorageNode.DATALENGTH) {
                    JOptionPane.showMessageDialog(frame, "Invalid position!");
                    return;
                }
                if (numLength <= 0 || numPosition + numLength > StorageNode.DATALENGTH) {
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

        DataClient dt = new DataClient(args[0], parseInt(args[1]));
    }
}
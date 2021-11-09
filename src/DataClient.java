import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Integer.parseInt;

/**
 * Class for the DataClient interface. Allows for data queries on a certain Node.
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
    private PrintWriter out;

    public DataClient(String nodeIpAddress, int nodePort){
        this.nodeIpAddress = nodeIpAddress;
        this.nodePort = nodePort;

        connectToNode();

        //Interface
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();

        //frame.setResizable(false);
        //frame.pack();

        frame.setSize(700, 200);
        frame.setVisible(true);
    }

    private void connectToNode() {
        try {
            socket = new Socket(InetAddress.getByName(nodeIpAddress), nodePort);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFrameContent() {

        frame.setLayout(new GridLayout(2,1));

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add((new JLabel("Position:")));

        position = new JTextField(TF_COLUMNS);
        panel.add(position);

        panel.add(new JLabel("Length: "));

        length = new JTextField(TF_COLUMNS);
        panel.add(length);

        JButton search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int numPosition = parseInt(position.getText());
                int numLength = parseInt(length.getText());

                if(numPosition <= 0 || numPosition > StorageNode.DATALENGTH) {
                    JOptionPane.showMessageDialog(frame, "Invalid position!");
                    return;
                }
                if(numLength < 0 || numPosition + numLength - 1 > StorageNode.DATALENGTH) {
                    JOptionPane.showMessageDialog(frame, "Invalid position!");
                    return;
                }

                // Fase 1
                // answer.setText("Position: " + numPosition + " Length: " + numLength);

                // Fase 5
                String request = numPosition + " " + numLength;
                out.println(request);

                try {
                    CloudByte[] data = (CloudByte[]) in.readObject();
                    String output = "";
                    for(int i = 0; i< data.length; i++) {
                        output+= data[i].toString() + " ";
                    }
                    answer.setText(output);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }

                // envia pedido pro node
                // recebe os dados
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

        if(args.length != 2)
            throw new IllegalArgumentException("Invalid arguments!");

        DataClient dt = new DataClient(args[0], parseInt(args[1]));
    }
}

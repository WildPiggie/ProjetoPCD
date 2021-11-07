import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    public DataClient(String ipAddress, int port){

        //Interface

        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();

        //frame.setResizable(false);
        //frame.pack();

        frame.setSize(700, 200);

        frame.setVisible(true);
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
                // Fase 1
                answer.setText("Position: " + position.getText() + " Length: " + length.getText());

                // Fase 3
                // storageNode.detectError(position, length);
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

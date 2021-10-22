import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataClient {

    private static final int TF_COLUMNS = 10;
    private JFrame frame;
    private JTextField position;
    private JTextField length;
    private JTextArea answer;

    public DataClient(String ipAddress, int port){

        //Interface

        frame = new JFrame("Cliente");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();

        //frame.setResizable(false);
        //frame.pack();

        frame.setSize(700, 110);

        frame.setVisible(true);
    }

    private void addFrameContent() {

        frame.setLayout(new GridLayout(2,1));

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add((new JLabel("Posição a consultar:")));

        position = new JTextField(TF_COLUMNS);
        panel.add(position);

        panel.add(new JLabel("Comprimento: "));

        length = new JTextField(TF_COLUMNS);
        panel.add(length);

        JButton search = new JButton("Consultar");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Fase 1
                answer.setText("Posição: " + position.getText() + " Comprimento: " + length.getText());
            }
        });
        panel.add(search);

        frame.add(panel);

        answer = new JTextArea("Respostas aparecerão aqui...");
        answer.setEditable(false);
        frame.add(answer, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        //args[0] = ip
        //args[1] = porto
        DataClient dt = new DataClient("123.123.123.123", 1);
    }
}

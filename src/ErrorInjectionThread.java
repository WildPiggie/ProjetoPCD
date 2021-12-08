import java.util.Scanner;

import static java.lang.Integer.parseInt;

/**
 * Used to corrupt CloudBytes specified by the client through the console.
 * Example: ERROR "index"
 * Where "index" is the numeric index of the CloudByte to be corrupted.
 *
 * @author Olga Silva & Samuel Correia
 */

public class ErrorInjectionThread extends Thread {

    private final StorageNode node;

    public ErrorInjectionThread(StorageNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (!isInterrupted()) {
            String command = sc.nextLine();
            String[] args = command.split(" ");
            if (args.length == 2) {
                try {
                    int position = parseInt(args[1]);
                    if (args[0].equals("ERROR") && position >= 0 && position < StorageNode.DATA_LENGTH) {
                        CloudByte cb = node.getElementFromData(position);
                        cb.makeByteCorrupt();
                        System.out.println("Error injected in position " + position + " : " + cb);
                    } else System.err.println("Command not found.");
                } catch (NumberFormatException e) {
                    System.err.println("Invalid command arguments.");
                }
            } else System.err.println("Command not found.");
        }
    }
}
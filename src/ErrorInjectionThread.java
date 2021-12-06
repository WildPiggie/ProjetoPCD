import java.util.Scanner;

import static java.lang.Integer.parseInt;

/**
 * Console reader. Checks for valid commands inputted into the console.
 * Valid commands include:
 * ERROR x : Where x is the target byte to be corrupted
 */
public class ErrorInjectionThread extends Thread {

    private StorageNode node;

    public ErrorInjectionThread(StorageNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) { //isto pode precisar de um try catch (espera por coisas serem inseridas na consola)
            //talvez fazer try catch do sc.nextLine();
            String command = sc.nextLine();
            String[] args = command.split(" ");
            if (args.length == 2) {
                try {
                    int position = parseInt(args[1]);
                    if (args[0].equals("ERROR") && position >= 0 && position < node.DATALENGTH) {
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
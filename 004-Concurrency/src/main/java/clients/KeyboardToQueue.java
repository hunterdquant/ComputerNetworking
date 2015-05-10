package clients;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Thread that copies lines typed by the user into the message or command
 * queues.
 *
 * @author blad
 */
class KeyboardToQueue extends Thread {
  public static final String commandPrefix = "/";
  public static final String GET_CARD = "GET_CARD";
  private final BlockingQueue<String> commandQueue;
  private final Scanner fin;
  private final InputStream in;
  private final BlockingQueue<String> messageQueue;

  /**
   * Construct a new KeyboardToQueue instance. Set the scanner to scan the
   * keyboard and initialize the server and regular message queue values (these
   * are where server and regular messages are copied after a line is read).
   *
   * @param in           Where to read information from (permits redirection to a file
   *                     rather than hard coding the keyboard)
   * @param messageQueue The regular message queue for messages to be dispatched to all
   *                     members of the chat group
   * @param commandQueue The message queue for messages to be sent to the server.
   */
  public KeyboardToQueue(InputStream in,
                         BlockingQueue<String> messageQueue,
                         BlockingQueue<String> commandQueue) {
    this.in = in;
    this.messageQueue = messageQueue;
    this.commandQueue = commandQueue;
    this.fin = new Scanner(in);
  }

  @Override
  public void run() {
    String line;
    System.out.print("> ");
    try {

      //Fill with 5 cards initially
      for (int i = 0; i < 5; i++) {
        messageQueue.put(GET_CARD);
      }

      System.out.println("Type hit <index> to swap an element in the hand, or type done to exit.\n ");
      boolean canGetCard = true;
      int numOfNewCards = 0;
      while ((line = fin.nextLine().trim()) != null) {
        Scanner sc = new Scanner(line);
        String command = "";
        if (sc.hasNext()) command = sc.next().toLowerCase();
        if (line.startsWith(commandPrefix))
          commandQueue.put(line);
        else if (command.equals("done") || !canGetCard)
          commandQueue.put("quit");
        else if (command.equals("hit")) {
          numOfNewCards++;
          messageQueue.put("SWAP " + sc.next());
          if (numOfNewCards == 3) {
            canGetCard = false;
            System.out.println("Max cards swapped press enter to terminate. ");
          }
        }

        System.out.print("> ");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}

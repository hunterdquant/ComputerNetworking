package servers;

import cards.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import static servers.ThreadPoolEchoServer.MSG_GOODBYE;

/**
 * This class provides the logic to handle a single client. It is run on a thread that is spun up when the server accepts a
 * client connection. This class uses blocking IO to wait for client lines; this is possible because all other sockets
 * being used by the server are each serviced by a separate thread.
 */
public class HandleClient implements Runnable {
  /**
   * The processed (cooked) input and output from/to the client.
   */
  final private Scanner cin;
  final private PrintStream cout;

  /**
   * Either the socket we service or null
   */
  final private Socket client;
  
  /**
   * Deck visable to all threads.
   */
  private static Deck deck = new Deck();
  private static int deckCount = 1;
  private static int cardNum = 1;

  /**
   * Create a new <code>HandleClient</code> instance.
   * This constructor expects the streams for bidirectional communications and the client socket
   *
   * @param in     the input stream providing input to this server thread
   * @param out    the output stream taking output from this server thread
   * @param client socket; only used for logging messages so can be null.
   */
  public HandleClient(InputStream in, OutputStream out, Socket client) {
    this.cin = new Scanner(in);
    this.cout = new PrintStream(out);
    this.client = client;
  } // HandleClient

  /**
   * Create a new <code>HandleClient</code> instance.
   * This constructor expects the streams for bidirectional communications. This is a much easier interface to instantiate
   * into a test framework (no need for a distant connection).
   *
   * @param in  the input stream providing input to this server thread
   * @param out the output stream taking output from this server thread
   */
  public HandleClient(InputStream in, OutputStream out) {
    this(in, out, null);
  } // HandleClient

  /**
   * Creates a new <code>HandleClient</code> instance.
   *
   * @param client the <code>Socket</code> for the connection we service
   */
  public HandleClient(ThreadPoolEchoServer server, Socket client) throws IOException {
    this(client.getInputStream(), client.getOutputStream(), client);
  } // HandleClient

  /**
   * Main loop of the thread. Reads each message and echos it back.
   */
  public void run() {
    String clientMessage = "";
    while (cin.hasNextLine() &&
        (!(clientMessage = cin.nextLine()).equals(MSG_GOODBYE))) {

      //Sends back message with card details.
      if (clientMessage.equals("GET_CARD")) {
        String serialNum = " " + deckCount + "-" + cardNum;
        cardNum++;
        try {
          cout.printf(deck.deal().toString() + serialNum + " \n");
        } catch (DeckUnderflowException due) {
          //Get new deck and set counters 
          deck = new Deck();
          deckCount++;
          cardNum = 1;
          cout.printf(deck.deal().toString() + serialNum + " \n");
        }
      
      //Sends back the message along with an index for swapping.
      } else if (clientMessage.substring(0,4).equals("SWAP")){
        String serialNum = " " + deckCount + "-" + cardNum;
        cardNum++;
        try {
          cout.printf("SWAP " + clientMessage.substring(5,6) + " " + deck.deal().toString() + serialNum + " \n");
        } catch (DeckUnderflowException due) {
          //Get new deck and set counters 
          deck = new Deck();
          deckCount++;
          cardNum = 1;
          cout.printf("SWAP " + clientMessage + " " + deck.deal().toString() + serialNum + " \n");
        }
      }

    }

    if (!clientMessage.equals(MSG_GOODBYE)) {
      System.err.println("Error disconnect from " + client);
    } else {
      System.out.println("Disconnection from " + client);
    }

    cin.close();
    cout.close();
  }
}

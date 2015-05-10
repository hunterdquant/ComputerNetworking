package servers;

import java.io.IOException;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Scanner;


/**
 * A simple, single-threaded echo server with sequential servicing of
 * clients. That is, when a client connects, it is serviced to
 * completion before another client connection can be accepted. The
 * server cannot be stopped gracefully: use Ctrl-C to break the
 * running program in a terminal.
 *
 * "Echo server" means that it listens for a connection (on a
 * user-specifiable port), reads in the input on the wire, modifies
 * the information, and writes the modified information back to the
 * client, echoing the input it was given.
 */
public class TCPServer {

  /** Port number of distant machine */
  private int portNumber;

  /**
   * Creates a new <code>TCPServer</code> instance. TCPServer is
   * a listening echo server (it responds with a slightly modified
   * version of the same message it was sent).
   *
   * @param portNumber required port number where the server will
   * listen.
   */
  public TCPServer(int portNumber) {
    this.portNumber = portNumber;
  }

  /**
   * Processes the command-line parameters and then create and run
   * the TCPServer object.
   *
   * @param args a <code>String</code> value
   */
  public static void main(String[] args) {

    if (args.length != 1) { // one required command-line arguments
      System.err.println("TCPServer <port>");
      System.exit(1);
    }

    // process command-line arguments
    int port = Integer.parseInt(args[0]);

    // create a server object and run it
    TCPServer s = new TCPServer(port);
    s.run();
  }

  /**
   * Primary method of the server: Opens a listening socket on the
   * given port number (specified when the object was
   * constructed). It then loops forever, accepting connections from
   * clients.
   *
   * When a client connects, it is assumed to be sending messages, one per line. The server will process
   */
  public void run() {
    System.out.println("TCPServer begins " + portNumber);

    try {
      ServerSocket server = new ServerSocket(portNumber);
      System.out.println("Accepting connections on " + portNumber);

      Socket currClient;

      while ((currClient = server.accept()) != null) {
        System.out.println("Connection from " + currClient);

        Scanner cin = new Scanner(currClient.getInputStream());
        PrintStream cout = new PrintStream(currClient.getOutputStream());

        String clientMessage = "";

        while (cin.hasNextLine()) {
          clientMessage = cin.nextLine();
          System.out.println("Server saw \"" + clientMessage + "\"");
          cout.println("Server saw \"" + clientMessage + "\"");
        }

        System.out.println("Server closing connection from " + currClient);
        cout.close();
        cin.close();
      }
    } catch (IOException ioe) {

      // there was a standard input/output error (lower-level from uhe)
      ioe.printStackTrace();
      System.exit(1);
    }

    System.out.println("TCPServer terminates.");
  }
}

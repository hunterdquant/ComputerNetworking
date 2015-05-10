package clients;

import java.net.Socket;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;


/**
 * This is a first client program. It demonstrates how to create a
 * client socket and how to read and write information to it. This
 * program determines what machine and port to connect to by reading
 * the command-line. This is the model that will be followed
 * throughout this semester.
 */
public class TCPClient {

  /** Name of the machine where the server is running. */
  private String machineName;

  /** Port number of distant machine */
  private int portNumber;

  /**
   * Creates a new <code>TCPClient</code> instance. An instance
   * has both a machine and port to which it will connect. Both are
   * stored in the class and used to initialize the connection.
   *
   * @param machineName the name of the machine where an compatible
   * server is running.
   * @param portNumber the port number on the machine where the
   * compatible server is listening.
   */
  public TCPClient(String machineName, int portNumber) {
    this.machineName = machineName;
    this.portNumber = portNumber;
  }

  /**
   * Processes the command-line parameters and then create and run
   * the TCPClient object.
   *
   * @param args array of String; the command-line parameters.
   */
  public static void main(String[] args) {

    if (args.length != 2) { // two required command-line arguments
      System.err.println("TCPClient <machine> <port>");
      System.exit(1);
    }

    // process command-line arguments
    String machine = args[0];
    int port = Integer.parseInt(args[1]);

    // create an object and run it
    TCPClient t = new TCPClient(machine, port);
    t.run();
  }

  /**
   * Client program opens a socket to the server machine:port pair. It
   * then sends a line to the server and waits for the server to
   * respond. After the server responds, the client closes everything
   * down and terminates.
   */
  public void run() {
    System.out.println("TCPClient begins " + machineName + ":" +
      portNumber);

    try {

      // A Socket is an input/output pair from a client; it is connected,
      // at construction time, to a server socket somewhere.
      Socket socket = new Socket(machineName, portNumber);

      // A Socket has both an input and an output stream.
      Scanner sin = new Scanner(socket.getInputStream());
      PrintStream sout = new PrintStream(socket.getOutputStream());

      System.out.println("Sending \"Hello, World!\" to server");
      sout.println("Hello, World!");

      System.out.println("Receiving from server");

      String serverResponse = sin.nextLine();

      System.out.format("Client saw \"%s\"\n", serverResponse);

      sout.close();
      sin.close();
    } catch (UnknownHostException uhe) {

      // the host name provided could not be resolved
      uhe.printStackTrace();
      System.exit(1);
    } catch (IOException ioe) {

      // there was a standard input/output error (lower-level than uhe)
      ioe.printStackTrace();
      System.exit(1);
    }

    System.out.println("TCPClient terminates.");
  }
}

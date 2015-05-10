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
public class LoopingClient {

  /** Default port number; used if none is provided. */
  public final static int DEFAULT_PORT_NUMBER = 3939;

  /** Default machine name is the local machine; used if none provided. */
  public final static String DEFAULT_MACHINE_NAME = "localhost";

  /** Command-line switches */
  public final static String ARG_PORT = "--port";
  public final static String ARG_MACHINE = "--machine";

  /** Message op-codes */
  public final static String MSG_HELLO = "Hello";
  public final static String MSG_GOODBYE = "Goodbye";

  /** Commands */
  public final static String COMMAND_PREFIX = "\\";
  public final static String COMMAND_BYE = "\\bye";

  public final static String PROMPT = "Message> ";

  /** Scanner attached to keyboard for reading user input */
  private Scanner keyboard;

  /** Name of the machine where the server is running. */
  private String machineName;

  /** Port number of distant machine */
  private int portNumber;

  /**
   * Creates a new <code>LoopingClient</code> instance. An instance
   * has both a machine and port to which it will connect. Both are
   * stored in the class and used to initialize the connection.
   *
   * @param machineName the name of the machine where an compatible
   * server is running.
   * @param portNumber the port number on the machine where the
   * compatible server is listening.
   */
  public LoopingClient(String machineName, int portNumber) {
    this.machineName = machineName;
    this.portNumber = portNumber;
    this.keyboard = new Scanner(System.in);
  }

  /**
   * Processes the command-line parameters and then create and run
   * the LoopingClient object.
   *
   * @param args array of String; the command-line parameters.
   */
  public static void main(String[] args) {
    int port = DEFAULT_PORT_NUMBER;
    String machine = DEFAULT_MACHINE_NAME;

    /* Parsing parameters. argNdx will move forward across the
     * indices; remember for arguments that have their own parameters, you
     * must advance past the value for the argument too.
     */
    int argNdx = 0;

    while (argNdx < args.length) {
      String curr = args[argNdx];

      if (curr.equals(ARG_PORT)) {
        ++argNdx;

        String numberStr = args[argNdx];
        port = Integer.parseInt(numberStr);
      } else if (curr.equals(ARG_MACHINE)) {
        ++argNdx;
        machine = args[argNdx];
      } else {

        // if there is an unknown parameter, give usage and quit
        System.err.println("Unknown parameter \"" + curr + "\"");
        usage();
        System.exit(1);
      }

      ++argNdx;
    }

    LoopingClient fc = new LoopingClient(machine, port);
    fc.run();
  }

  /**
   * Client program opens a socket to the server machine:port
   * pair. It then sends the message "Hello", reads the line the
   * server is expected to respond with, and then sends
   * "Goodbye". After sending the final message it closes the socket
   * stream.
   */
  public void run() {

    try {
      Socket socket = new Socket(machineName, portNumber);
      PrintStream sout = new PrintStream(socket.getOutputStream());
      Scanner sin = new Scanner(socket.getInputStream());

      System.out.println("Receiving from server");

      String serverResponse = "";

      System.out.print(PROMPT);

      while (keyboard.hasNextLine()) {
        String msg = keyboard.nextLine();

        if (msg.equals(COMMAND_BYE))
          break;

        sout.println(msg);
        serverResponse = sin.nextLine();
        System.out.println(serverResponse);
        System.out.print(PROMPT);
      }

      sout.close();
      sin.close();
    } catch (UnknownHostException uhe) {

      // the host name provided could not be resolved
      uhe.printStackTrace();
      System.exit(1);
    } catch (IOException ioe) {

      // there was a standard input/output error (lower-level)
      ioe.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Print the usage message for the program on standard error stream.
   */
  private static void usage() {
    System.err.format("usage: java LoopingClient [options]\n" +
      "       where options:\n" + "       %s port\n" +
      "       %s machineName\n", ARG_PORT, ARG_MACHINE);
  }
}

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
public class FirstClient {

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

  /** Name of the machine where the server is running. */
  private String machineName;


  /** Port number of distant machine */
  private int portNumber;

  /**
   * Creates a new <code>FirstClient</code> instance. An instance
   * has both a machine and port to which it will connect. Both are
   * stored in the class and used to initialize the connection.
   *
   * @param machineName the name of the machine where an compatible
   * server is running.
   * @param portNumber the port number on the machine where the
   * compatible server is listening.
   */
  public FirstClient(String machineName, int portNumber) {
    this.machineName = machineName;
    this.portNumber = portNumber;
  }

  /**
   * Processes the command-line parameters and then create and run
   * the FirstClient object.
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

    FirstClient fc = new FirstClient(machine, port);
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

      System.out.format("Sending \"%s\"", MSG_HELLO);
      sout.println(MSG_HELLO);

      System.out.println("Receiving from server");

      String serverResponse = sin.nextLine();
      System.out.format("Client saw \"%s\"\n", serverResponse);

      System.out.format("Sending \"%s\"", MSG_GOODBYE);
      sout.println(MSG_GOODBYE);

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
    System.err.format("usage: java FirstClient [options]\n" +
      "       where options:\n" + "       %s port\n" +
      "       %s machineName\n", ARG_PORT, ARG_MACHINE);
  }
}

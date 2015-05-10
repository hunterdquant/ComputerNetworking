/*
 * Edited by: Hunter Quant
 * Assignment: p003 
 * Due date: 02/4/15
 */

package servers;

import java.io.IOException;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Scanner;
import java.util.HashMap;

/**
 * This program is a two term calculator server, which evaluates expressions for the client.
 * Single threaded and services clients sequentially. Recieves messages in HQPEP
 * (Hunter Quant's Prefix Expression Protocol) and then evaluates them. Can
 * recieve two command messages from client.
 *
 * Expression message format: EVAL <OPERATOR> INTEGER-:-INTEGER
 * OPERATOR = MOD, DIV, ADD, SUB, POW, or PROD
 * 
 * !server - Sends version information to the client.
 * !exit - Recieves final message from client, client then terminates.
 *
 * Command Message Format: CMD <ACTION>
 * ACTION = EXIT or SERVER
 */
public class TTCServer {

  /** Default port number; used if none is provided. */
  public final static int DEFAULT_PORT_NUMBER = 54321;
  /** Default machine name is the local machine; used if none provided. */
  public final static String DEFAULT_MACHINE_NAME = "localhost";

  /** Command-line switches */
  public final static String ARG_PORT = "--port";
  public final static String ARG_MACHINE = "--machine";

  /** Message op-codes */
  public final static String MSG_TERMINATE = "You have been terminated.";
  public final static String SERVER_VERSION = "QUANTUM-QUANTITY-QUANTIFIER-SERVER 2.3.1";


  /** Port number of distant machine */
  private int portNumber;

  /**
   * Creates a new <code>TTCServer</code> instance. TTCServer listens for messages from
   * the client to evaluate.
   *
   * @param portNumber required port number where the server will
   * listen.
   */
  public TTCServer(int portNumber) {
    
    this.portNumber = portNumber;
  }

  /**
   * the FirstClient object.
   *
   * @param args a <code>String</code> value
   */
  public static void main(String[] args) {
    
    int port = DEFAULT_PORT_NUMBER;

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
      } else {

        // if there is an unknown parameter, give usage and quit
        System.err.println("Unknown parameter \"" + curr + "\"");
        usage();
        System.exit(1);
      }

      ++argNdx;
    }

    TTCServer fc = new TTCServer(port);
    fc.run();
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

    try {

      ServerSocket server = new ServerSocket(portNumber);
      System.out.format("Server now accepting connections on port %d\n", portNumber);

      Socket client;

      while ((client = server.accept()) != null) {
        
        System.out.format("Connection from %s\n", client);

        Scanner cin = new Scanner(client.getInputStream());
        PrintStream cout = new PrintStream(client.getOutputStream());

        String clientMessage = "";
        
        while (cin.hasNextLine()) {

          clientMessage = cin.nextLine();
          Scanner msgScan = new Scanner(clientMessage);
          String serverResponse;

          //There are two commands the server can recieve.
          if (msgScan.next().equals("CMD")) {
            //Terminate connection with user or send the server version.
            cout.println(msgScan.next().equals("<EXIT>") ? MSG_TERMINATE : SERVER_VERSION);
          } else {
            //It's a EVAL message.
            //Send the result of the evaluated two term expression to the client.
            cout.println(evalExpression(msgScan));
          }
          System.out.printf("%s\n",clientMessage);
        }

        cout.close();
        cin.close();
      }

    } catch (IOException ioe) {

      // there was a standard input/output error (lower-level from uhe)
      ioe.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Evalutates the two term prefix expression.
   * Interprets the HQPEP message and evaulates.
   * 
   * @param msgScan a scanner open on the HQPEP message.
   */
  public int evalExpression(Scanner msgScan) {
    
    try {
      
      //Grab the operator
      String token = msgScan.next();

      //Split the string to get the integers
      String [] numsArray = msgScan.next().split("-:-");

      //Parse the integers
      int num1 = Integer.parseInt(numsArray[0]); 
      int num2 = Integer.parseInt(numsArray[1]);
      int result = 0;

      //Evaluate the expression.
      if (token.equals("<ADD>")) {
        result = num1 + num2;
      } else if (token.equals("<SUB>")) {
        result = num1 - num2; 
      } else if (token.equals("<DIV>")) {
        result = num1 / num2;
      } else if (token.equals("<MOD>")) {
        result = num1 % num2;
      } else if (token.equals("<PROD>")) {
        result = num1 * num2;
      } else {
        result = (int)Math.pow(num1, num2);
      }
      return result;

    //Why did you divide by zero?
    } catch (ArithmeticException ae) {

      ae.printStackTrace();
      System.out.println("WHAT HAVE YOU DONE!!!");
      return 2147483647;
    }
  }

  /**
   * Print the usage message for the program on standard error stream.
   */
  private static void usage() {
    System.err.print("usage: java FirstClient [options]\n" +
        "       where options:\n" + "       --port port\n");
  }
}

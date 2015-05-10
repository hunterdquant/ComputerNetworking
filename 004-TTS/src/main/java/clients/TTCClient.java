/*
 * Edited by: Hunter Quant
 * Assignment: p003
 * Due date: 02/4/15
 */

package clients;

import java.net.Socket;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.Stack;
import java.util.HashMap;
import java.util.EmptyStackException;


/**
 * This program is a two term calculator client that evaluates prefix expressions.
 * Reads the users input by command line and evaluates prefix expressions and has
 * three commands that can be entered. Sends messages to server using HQPEP
 * (Hunter Quant's Prefix Expression Protocol)
 *
 * Expression message format: EVAL <OPERATION> INTEGER-:-INTEGER
 * OPERATION = MOD, DIV, ADD, SUB, POW, or PROD.
 * 
 * !verbose - Shows all subexpressions as evaluated by the server.
 * !server - Requests the current version information from the server.
 * !exit - Sends final message to the server then the client terminates.
 *
 * Command message format: CMD <ACTION>
 * ACTION = EXIT or SERVER
 */
public class TTCClient {

  /** Default port number; used if none is provided. */
  public final static int DEFAULT_PORT_NUMBER = 54321;

  /** Default machine name is the local machine; used if none provided. */
  public final static String DEFAULT_MACHINE_NAME = "localhost";

  /** Command-line switches */
  public final static String ARG_PORT = "--port";
  public final static String ARG_MACHINE = "--machine";

  /** Message op-codes */
  public final static String MSG_HELLO = "Hello";
  public final static String MSG_TERMINATE = "You have been terminated.";

  public final static String PROMPT = "Message> ";

  /** Scanner attached to keyboard for reading user input */
  private Scanner keyboard;

  /** Name of the machine where the server is running. */
  private String machineName;

  /** Port number of distant machine */
  private int portNumber;

  /** Toggles verbose on and off */
  private boolean verboseToggle = false;

  /**
   * Creates a new <code>TTCClient</code> instance. TTCClient reads input from the user
   * of the proper format and sends HQPEP message to the server for evaluation.
   *
   * @param machineName the name of the machine where an compatible
   * server is running.
   * @param portNumber the port number on the machine where the
   * compatible server is listening.
   */
  public TTCClient(String machineName, int portNumber) {
    
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

    TTCClient fc = new TTCClient(machine, port);
    fc.run();
  }

  /**
   * Client program opens a socket to the server machine:port
   * pair. It then sends the message "Hello", reads the line the
   * server is expected to respond with, and then sends
   * CMD <EXIT>. After sending the final message it closes the socket
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

        //Process the users message. 
        msg = processInput(msg.toLowerCase().trim());

        //If the input has less than 4 characters, it's invalid.
        if (msg.length() < 4) {
          
          System.out.print("Invalid message.\n" + PROMPT);
          continue;
        }
        
        //Checks for commands.
        if (msg.substring(0,3).equals("CMD")) {
          
          //If verbose was activated redisplay prompt.
          if (msg.equals("CMD <VERBOSE>"))  {
            System.out.print(PROMPT);
            continue;
          }
          
          //Send the command to the server
          sout.println(msg);
          serverResponse = sin.nextLine();
          System.out.println(serverResponse);
          
          //Terminate if the server recieved the exit command
          if (serverResponse.equals(MSG_TERMINATE)) {
            System.out.println("Disconnecting from server.");
            System.exit(1);
          }
        } else {
          
          //If it's not a command evaluate the expression if possible.
          requestEval(msg, sout, sin);  
        }

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
   * Processes client input into a HQPEP formatted string.
   * Determines whether the input is a command or expression.
   * Returns formatted message for valid input.
   * Returns empty string if improperly formatted or verbose.
   * Returns the msg for strings that could be prefix expressions.
   *
   * @param msg the input message from the client to be evaluated.
   */
  private String processInput(String msg) {

    if (msg.charAt(0) == '!') {
      if (msg.equals("!verbose")) {
        verboseToggle = !(verboseToggle);
        System.out.println("Verbose mode is: " + (verboseToggle ? "on" : "off"));
        return "CMD <VERBOSE>";
      } else if (msg.equals("!exit")) {
        return "CMD <EXIT>";
      } else if (msg.equals("!server")) {
        return "CMD <SERVER>";
      }
      return "";
    }
    return msg;     
  }
  
  /**
   * Parses prefix expressions and sends subexpressions to the server.
   * If invalid strings are entered it throws a number formate exception.
   * Evaluates prefix expressions using a stack.
   *
   * @param msg the input message from the client to be evaluated
   * @param sout the printsteam that sends messages to the server.
   * @param sin the scanner that recieves messages from the server.
   */
  private void requestEval(String msg, PrintStream sout, Scanner sin) {
    
    Scanner sc = new Scanner(msg);
    Stack<String> temp = new Stack<String>();

    //Reverse the input to read it from the back
    String reversedMsg = ""; 
    while (sc.hasNext()) {
      
      temp.push(sc.next());
    }
    while (!temp.isEmpty()) {
      
      reversedMsg += temp.pop() + " ";
    }

    sc.close();
 
    //Map the operators to their commands.
    HashMap<String, String> operators = new HashMap<String, String>();
    operators.put("+", "<ADD>"); operators.put("-", "<SUB>"); operators.put("/", "<DIV>");
    operators.put("%", "<MOD>"); operators.put("*", "<PROD>"); operators.put("^", "<POW>");
    Stack<String> operands = new Stack<String>();
    try {

      Scanner expScan = new Scanner(reversedMsg);
      //Read through to prefix expression and have the server evaluate.
      while (expScan.hasNext()) {
        
        String token = expScan.next();

        //If the token is an operator evaluate what's on the stack.
        if (operators.containsKey(token)) {
          
          String num1 = operands.pop();
          String num2 = operands.pop();

          //For HQPEP message
          sout.println("EVAL " + operators.get(token) + " " + num1 + "-:-" + num2);
          String result = sin.nextLine();
          //Push the result on the stack
          operands.push(result);
          //If verbose mode is on, print the sub expression and result.
          if (verboseToggle) {
            System.out.println("Evaluating: " + token + " " + num1 + " " + num2 + " = " + result);  
          }

        } else {
         
          //Check to see if token is a integer. Catches number format exception.
          Integer.parseInt(token);
          operands.push(token);
        }
      }
      expScan.close();
      System.out.println(msg + " = " + operands.pop());
        
    } catch (NumberFormatException nfe) {
      
      nfe.printStackTrace();
      System.out.println("Invalid expression.");
      return;

    } catch (EmptyStackException ese) {
      
      ese.printStackTrace();
      System.out.println("Invalid expression.");
      return;
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

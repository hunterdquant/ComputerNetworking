package servers;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



/**
 * A modified EchoServer: This echo server uses the java.util.concurrent
 * framework to construct a thread pool, a collection of threads, each
 * of which can be running in a different part of the code at the same
 * time. Threads are constructed and when a client connects, a thread
 * (if one is available) is assigned to handle that client. Other
 * threads remain available for servicing additional clients.
 */
public class ThreadPoolEchoServer {
    /**
     * Default port number; used if none is provided.
     */
    public final static int DEFAULT_PORT_NUMBER = 6700 + ('q'-'a');
    /**
     * Default machine name is the local machine; used if none provided.
     */
    public final static String DEFAULT_MACHINE_NAME = "localhost";
    /**
     * Command-line switches
     */
    public final static String ARG_PORT = "--port";
    public final static String ARG_MACHINE = "--machine";
    /**
     * Message op-codes
     */
    public final static String MSG_HELLO = "Hello";
    public final static String MSG_GOODBYE = "Goodbye";

    private int portNumber;

  /** the number of threads to have running in the executor */
  private static final int NTHREADS = 2;

  /**
   * java.util.concurrent has a collection of different executors. An
   * executor is a way of starting a runnable object (a Thread-extending
   * or a Runnable-implementing class). Different executors have different
   * policies on how many threads they are permitted to start up and how
   * they handle them.
   *
   * The simplest Executor is the FixedThreadPool Executor. It is constructed
   * with a given number of threads, it creates that number of threads, and when
   * too many runnables are given to it, they are enqueued.  See the
   * package documentatino for more information on other executors.
   */
  private static final Executor exec = Executors.newFixedThreadPool(
      NTHREADS);

  public ThreadPoolEchoServer(int portNumber) {
      this.portNumber = portNumber;
  }

  /**
   * Processes the command-line parameters and then create and run the
   * ThreadPoolEchoServer object.
   *
   * @param args
   *          a <code>String</code> value
   */
  public static void main(String[] args) {
    int port = DEFAULT_PORT_NUMBER;

    /*
     * Parsing parameters. argNdx will move forward across the indices;
     * remember for arguments that have their own parameters, you must
     * advance past the value for the argument too.
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
        System.exit(1);
      }

      ++argNdx;
    }

    ThreadPoolEchoServer instance = new ThreadPoolEchoServer(port);
    instance.run();
  }

  /**
   * Run: the operation of the server is broken into two parts. Where in
   * the simple, single-threaded server, there were a pair of nested
   * loops in run, the loops have now been separated. Here, in the
   * server's run, is the loop for
   */
  public void run() {

    try {
      ServerSocket server = new ServerSocket(portNumber);
      System.out.println("Server now accepting connections on " + portNumber);

      Socket client = null;

      while ((client = server.accept()) != null) {
        HandleClient c = new HandleClient(this, client);
        exec.execute(c);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  } // run
}

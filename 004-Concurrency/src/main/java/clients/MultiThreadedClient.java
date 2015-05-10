package clients;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;


/**
 *
 */

public class MultiThreadedClient {
  /**
   * Default port number; used if none is provided.
   */
  public final static int DEFAULT_PORT_NUMBER = 6700 + ('q' - 'a');
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
  public static final String QUIT_COMMAND = "quit" ;

  /**
   * the executor (controller of threads)
   */
  private ExecutorService executor;
  /**
   * Name of the machine where the server is running.
   */
  private String machineName;


  /**
   * Port number of distant machine
   */
  private int portNumber;


  /**
   * Communication queue between the keyboard and the command
   * processor
   */
  private BlockingQueue<String> commandQueue;

  /**
   * Communication queue between the keyboard and the send port
   */
  private BlockingQueue<String> messageQueue;

  /**
   * The single socket used by this process; it is used for receiving
   * from any other "calling" process.
   */
  private Socket socket;

  private boolean running;

  /**
   * Creates a new <code>UDPPeer</code> instance. An instance
   * has both a machine and port to which it will connect. Both are
   * stored in the class and used to initialize the connection.
   *
   * @param machineName the name of the machine where an compatible
   *                    server is running.
   * @param portNumber  the port number on the machine where the
   *                    compatible server is listening.
   */
  public MultiThreadedClient(String machineName, int portNumber) {
    this.machineName = machineName;
    this.portNumber = portNumber;
  }

  /**
   * The main program. Processed command-line arguments, creates a
   * UDPPeer object and calls run().
   *
   * @param args provided command-line arguments
   */
  public static void main(String[] args) {
    int port = DEFAULT_PORT_NUMBER;
    String machine = DEFAULT_MACHINE_NAME;

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
      } else if (curr.equals(ARG_MACHINE)) {
        ++argNdx;
        machine = args[argNdx];
      } else {

        // if there is an unknown parameter, give usage and quit
        System.err.println("Unknown parameter \"" + curr + "\"");
        System.exit(1);
      }

      ++argNdx;
    }

    new MultiThreadedClient(machine, port).run();
  } // main

  /**
   * Is the main program still running?
   * @return running status
   */
  synchronized public boolean isRunning() {
    return running;
  }

  /**
   * The run method; Does all of the work
   */
  public void run() {

    // linked: use a linked list; queue: FIFO data structure
    // blocking: enhanced to permit waiting until there is something
    // to read or room to write into the queue
    this.commandQueue = new LinkedBlockingQueue<String>();
    this.messageQueue = new LinkedBlockingQueue<String>();


    // cached thread executor: reuse threads when possible, generate new
    // threads when necessary
    this.executor = Executors.newCachedThreadPool();

    try {
      this.socket = new Socket(machineName, portNumber);


      // KeyboardToQueue reads the keyboard and posts lines into
      // the message and command queues (all communication is through
      // safe queues so communication is thread safe)
      KeyboardToQueue keyboard = new KeyboardToQueue(System.in,
          messageQueue, commandQueue);

      // DatagramReceiver watches the wire; when something is ready it
      // is read and put on the screen.
      ReceiverThread receiverThread = new ReceiverThread(socket);

      // DatagramSender watches a message queue; when something is
      // ready it is copied to the wire.
      SenderThread senderThread = new SenderThread(socket, messageQueue);

      running = true;

      executor.execute(receiverThread);
      System.out.print(".");
      executor.execute(senderThread);
      System.out.print(".");
      executor.execute(keyboard);
      System.out.println(".");
      while (running) {
        String command = commandQueue.take();
        System.out.println(String.format("Command: %s", command));
        if (command.endsWith(QUIT_COMMAND)) {
          messageQueue.add(MSG_GOODBYE);
          running = false;
        }
      }
      executor.shutdown();
      System.exit(0);
    } catch (IOException e) {
      System.err.println("IO Error " + machineName);
      e.printStackTrace();
      System.exit(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

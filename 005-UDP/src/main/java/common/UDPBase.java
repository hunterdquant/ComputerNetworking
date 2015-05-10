package common;

import java.io.PrintStream;
import java.net.DatagramSocket;

/**
 * A reasonable base class for UDP (and other) networking classes. Encapsulates the command-line switches and some
 * simple level based logging.
 */
public abstract class UDPBase implements Runnable {
  /**
   * Default port number; used if none is provided.
   */
  public final static int DEFAULT_DISTANT_PORT_NUMBER = 8909;

  /**
   * Default machine name is the local machine; used if none provided.
   */
  public final static String DEFAULT_DISTANT_MACHINE_NAME = "localhost";

  /**
   * Default verbose level
   */
  public final static int DEFAULT_VERBOSE = 0;

  /**
   * Default initial listening behavior.
   */
  public final static boolean DEFAULT_LISTEN_FIRST = false;

  /**
   * The default log location.
   */
  public final static PrintStream DEFAULT_LOG = System.out;

  /**
   * Command-line switches
   */
  public final static String ARG_DISTANT_PORT = "--distantport";
  public final static String ARG_DISTANT_MACHINE = "--distantmachine";
  public final static String ARG_VERBOSE = "--verbose";
  public final static String ARG_LOCAL_PORT = "--localport";
  public final static String ARG_LISTEN_FIRST = "--listen";

  /**
   * Carriage-return, line-feed character pair; add to end of every message
   */
  public final static String crlf = "\r\n";
  public final static String formatMessage = "%s" + crlf;

  /**
   * Prefix for commands; useful to partition messages and commands entered on the same input channel.
   */
  public final static String commandPrefix = "/";

  /**
   * The default buffer (and therefore maximum datagram) size.
   */
  public final static int bufferSize = 32;

  /**
   * Is the peer listening (or talking)
   */
  protected boolean listening;

  /**
   * What port number am I using?
   */
  protected final int localPortNumber;

  /**
   * The log stream
   */
  protected PrintStream logStream;

  /**
   * Verbose output flag.
   */
  protected int verbose;
  /** Command-line switches */

  /**
   * The single socket used by this process; it is used for receiving from any other "calling" process.
   */
  protected DatagramSocket socket;

  /**
   * Bind to the given port and log with default settings.
   *
   * @param localPortNumber local port number to use
   */
  public UDPBase(int localPortNumber) {
    this(localPortNumber, DEFAULT_VERBOSE);
  }

  /**
   * Bind to the given local port. Log to default log stream with given verbosity.
   *
   * @param localPortNumber local port number to use
   * @param verbose         log-message verbosity
   */
  public UDPBase(int localPortNumber, int verbose) {
    this(localPortNumber, verbose, DEFAULT_LOG);
  }

  /**
   * The "real" constructor: all other constructors call this constructor after setting up the appropriate parameter
   * values. This is the cannonical place for initialization of the UDPBase.
   *
   * @param localPortNumber Local port for DatagramSocket
   * @param verbose         Logging level for print/println logging
   * @param logStream       Where to print the log
   */
  public UDPBase(int localPortNumber, int verbose, PrintStream logStream) {
    this.localPortNumber = localPortNumber;
    this.verbose = verbose;
    this.logStream = logStream;
    this.socket = null;
  }

  /**
   * The run method is the main method of the program this is associated with. Each subclass of this class should
   * provide its own definition. Note that socket is not initialized before run is called.
   */
  public abstract void run();

  /**
   * Increase the level of messages being logged.
   */
  public void moreVerbose() {
    verbose++;
  }

  /**
   * Decrease the level of messages being logged.
   */
  public void lessVerbose() {

    verbose--;
  }

  /**
   * Print the given string message to the logging stream iff the current verbosity is high enough to admit it.
   *
   * @param level minimum verbosity where this message is to be printed
   * @param msg   string to log
   */
  public void print(int level, String msg) {

    if (verbose >= level)
      logStream.print(msg);
  }

  /**
   * Print the given string message to the logging stream iff the current verbosity is high enough to admit it. If
   * printed, start a new line afterwards.
   *
   * @param level minimum verbosity where this message is to be printed
   * @param msg   string to log
   */
  public void println(int level, String msg) {

    if (verbose >= level)
      logStream.println(msg);
  }
} // UDPBase

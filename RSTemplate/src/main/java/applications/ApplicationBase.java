package applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Created by blad on 4/3/15.
 */
public class ApplicationBase {
  /**
   * Default "server" port number; used if none is provided.
   */
  public final static int DEFAULT_S_PORT_NUMBER = 8909;

  /**
   * Default machine name is the local machine; used if none provided.
   */
  public final static String DEFAULT_S_MACHINE_NAME = "localhost";

  public final static String DEFAULT_FILE = "shakespeare.txt";

  public final static long MAX_CHUNK = 1024L;

  public final static long TRANSPORT_HEADER_LENGTH = 0L;

  public final static long MAX_DATAGRAM_PACKET = MAX_CHUNK + TRANSPORT_HEADER_LENGTH;

  public static final int DEFAULT_NUMBER_OF_REQESTS = 250;

  /**
   * Command-line switches
   */
  public final static String ARG_LOCAL_PORT = "--localport";
  public final static String ARG_DISTANT_PORT = "--distantport";
  public final static String ARG_DISTANT_MACHINE = "--distantmachine";
  public static final String ARG_DELAY = "--delay";
  public static final String ARG_LOSE = "--lose";
  public static final String ARG_PACKET = "--packet";
  public static final String ARG_BYTE = "--byte";
  public static final String ARG_FILE = "--file";
  public static final String ARG_NUMBER_OF_REUESTS = "--requests";


  /**
   * Application fields.
   */
  // random number generater
  protected final Random random;
  // name of the shared input file
  protected String fileName;
  // file variable that supports seek and read
  protected RandomAccessFile testFile;
  // the reported length of the file
  protected long fileLength;

  // printDebugData flag: set true and recompile to see printDebugData output from different levels
  private static final boolean printDebugData = false;

  /**
   * Make a new application base with the given file name.
   * @param fileName
   */
  public ApplicationBase(String fileName) {
    this.fileName = fileName;
    this.random = new Random();
  }

  /**
   * Should debugging output be printed? Set ApplicationBase.printDebugData accordingly.
   * @return
   */
  public static boolean getPrintDebugData() {
    return printDebugData;
  }

  /**
   * Generate a long on the range [0, bound). Handle negative random values correctly.
   *
   * @param bound upper bound for non-negative random long
   * @return random non-negative long on the range [0, bound)
   */
  protected long nextLong(long bound) {
    long rlong = random.nextLong();
    long theRandom = (rlong % bound);
    while (theRandom < 0) theRandom += bound;
    return theRandom;
  }

  /**
   * Open a file in the build/resources hierarchy.
   */
  protected void openTheFile() {
    try {
      fileName = "build/resources/main/" + fileName;
      File file = new File(fileName);
      fileLength = file.length();
      testFile = new RandomAccessFile(fileName, "r");
    } catch (FileNotFoundException e) {
      System.err.println(String.format("Unable to open test data file %s", fileName));
      System.exit(1);
    }
  }

  /**
   * Parse a string with a long. Workaroud for problems with Long.parseLong. Does almost no error checking.
   * @param str string containing a number (we hope)
   * @return the long encoded in the string
   */
  protected long stupidParseLong(String str) {
    str = str.trim();
    long sign = 1;
    if (str.startsWith("-")) {
      sign = -1;
      str = str.substring(1).trim();
    }
    long retval = 0;
    while (!str.isEmpty()) {
      char digit = str.charAt(0);
      retval = 10 * retval + (digit - '0');
      str = str.substring(1);
    }
    return retval;
  }
}

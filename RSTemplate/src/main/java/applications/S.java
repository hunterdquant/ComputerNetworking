package applications;

import network.UnreliableNetworkBase;
import transport.STransport;

import java.io.IOException;

/**
 * Simulates the Server (S) object from exam question 2.2. The server side where you have to provide the transport layer
 * protocol to make it work. Created by blad on 3/31/15.
 */
public class S extends ApplicationBase {
  /**
   * What port number am I using?
   */
  protected final int localPortNumber;
  protected String chunkString;

  protected int chunkNumber;
  protected STransport transport;

  /**
   * Create a new S server with the given port. Does not return until a connection is made.
   *
   * @param localPortNumber the port an R connects to
   */
  private S(int localPortNumber, String fileName) {
    super(fileName);
    this.localPortNumber = localPortNumber;
    openTheFile();
    System.out.println(String.format("Waiting on %d", localPortNumber));
    this.transport = new STransport(localPortNumber);

  }

  /**
   * Parse the command-line arguments and construct an S object with the given settings. This function may not return,
   * instead ending the program if there is an error processing the command-line.
   * @param args the command-line arguments
   * @return an initialized S object listening on the given port
   */
  public static S processCommandLine(String[] args) {
    int localPort = DEFAULT_S_PORT_NUMBER;
    String fileName = DEFAULT_FILE;
    int delayBoundMilliseconds = 1000;
    double loseChance = 0.10;
    double corruptPacketChance = 0.10;
    double corruptByteChance = 0.50;

   /*
     * Parsing parameters. argNdx will move forward across the indices;
     * remember for arguments that have their own parameters, you must
     * advance past the value for the argument too.
     */
    int argNdx = 0;

    while (argNdx < args.length) {
      String curr = args[argNdx];

      if (curr.equals(ARG_LOCAL_PORT)) {
        ++argNdx;

        String numberStr = args[argNdx];
        localPort = Integer.parseInt(numberStr);
      } else if (curr.equals(ARG_DELAY)) {
        ++argNdx;
        delayBoundMilliseconds = Integer.parseInt(args[argNdx]);
      } else if (curr.equals(ARG_LOSE)) {
        ++argNdx;
        loseChance = Double.parseDouble(args[argNdx]);
      } else if (curr.equals(ARG_FILE)) {
        ++argNdx;
        fileName = args[argNdx];
      } else {
        // if there is an unknown parameter, give usage and quit
        System.err.println("Unknown parameter \"" + curr + "\"");
        System.exit(1);
      }

      ++argNdx;
    }
    S retVal = new S(localPort, fileName);
    UnreliableNetworkBase net = retVal.transport.getNetwork();
    net.setDelayBoundMilliseconds(delayBoundMilliseconds);
    net.setLoseChance(loseChance);
    net.setCorruptByteChance(corruptByteChance);
    net.setCorruptPacketChance(corruptPacketChance);
    return retVal;
  }

  /**
   * Run the program by making an S object and then running it.
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    S s = processCommandLine(args);
    s.run();
  }

  /**
   * Get the chunk starting at the given byte offset.
   * @param chunkNumber byte offset for start of chunk
   * @return the bytes from the given offset
   */
  public byte[] nextChunk(long chunkNumber) {
    int chunkLength = random.nextInt((int) MAX_CHUNK);
    byte[] chunkData = new byte[chunkLength];
    try {
      testFile.seek(chunkNumber);
      testFile.readFully(chunkData);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return chunkData;
  }

  /**
   * Run the S server. Wait until the transport layer delivers a request. Read the request number from the request,
   * pick a length that fits in a single packet, get the data from the input file, and send it by way of the transport
   * layer. Then do it all over again.
   */
  @SuppressWarnings("InfiniteLoopStatement")
  public void run() {
    while (true) {
      try {
        byte[] request = transport.receiveMsg();
        String requestAsString = new String(request);
        if (ApplicationBase.getPrintDebugData())
          System.out.printf("\"%s\" (%d, %d)", requestAsString, requestAsString.length(), request.length);
        // stupidParseLong seems necessary to avoid negative numbers.
        long chunkNumber = stupidParseLong(requestAsString);
        byte[] chunk = nextChunk(chunkNumber);
        transport.sendMsg(chunk);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

}

package network;

import applications.ApplicationBase;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by blad on 3/31/15.
 */
public class UnreliableNetworkBase extends DatagramSocket {

  /**
   * Local class that handles sending messages. Messages are passed in through a DelayQueue and taken in queue order.
   * When time to send them comes around, the message queue returns it and it is sent.
   */
  class SendingThread implements Runnable {
    private DelayQueue<DelayedMessage> delayedMessages;
    private ObjectOutputStream objectOutputStream;

    public SendingThread(DelayQueue<DelayedMessage> delayedMessages, ObjectOutputStream objectOutputStream) {
      this.delayedMessages = delayedMessages;
      this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {
      while (true) {
        try {
          DelayedMessage nextMessage = delayedMessages.poll();
          if (nextMessage != null) {
            if (ApplicationBase.getPrintDebugData())
              System.out.println(String.format("SendingThread nextMessage = %s", new String(nextMessage.packet)));
            objectOutputStream.writeObject(nextMessage.packet);
          }
        } catch (EOFException e) {
         System.exit(0);
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
  }
  /**
   * Local runnable class to handle receiving messages. Messages are taken from the wire and delivered to a
   * BlockingQueue so that the UnreliableNetworkBase.receive() method can return them.
   */
  class ReceivingThread implements Runnable {
    private BlockingQueue<byte[]> deliveredMessages;
    private ObjectInputStream objectInputStream;

    public ReceivingThread(BlockingQueue<byte[]> deliveredMessages, ObjectInputStream objectInputStream) {
      this.deliveredMessages = deliveredMessages;
      this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {
      while (true) {
        try {
          byte[] nextMessage = (byte[]) objectInputStream.readObject();
          if (ApplicationBase.getPrintDebugData())
            System.out.println("ReceivingThread nextMessage = " + new String(nextMessage));
          deliveredMessages.add(nextMessage);
        } catch (EOFException e) {
          System.exit(0);
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }

    }
  }

  /**
   * The message class. A message is a byte[]. It comes with a time to delay the sending of it. These are kept in a
   * DelayQueue.
   */
  class DelayedMessage implements Delayed {
    private long delayUntilTime;
    public final byte[] packet;

    private void setDelays(long delay, TimeUnit tu) {
      long delayInMilliseconds = TimeUnit.MILLISECONDS.convert(delay, tu);
      long createTime = System.currentTimeMillis();
      this.delayUntilTime = createTime + delayInMilliseconds;
    }

    public DelayedMessage(long delayInMilliseconds, byte[] packet) {
      this(delayInMilliseconds, TimeUnit.MILLISECONDS, packet);
    }

    @SuppressWarnings("SameParameterValue")
    public DelayedMessage(long delay, TimeUnit tu, byte[] packet) {
      setDelays(delay, tu);
      this.packet = packet;
    }

    /**
     * Returns the remaining delay associated with this object, in the given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate that the delay has already elapsed
     */
    @Override
    public long getDelay(TimeUnit unit) {
      long remainingMilliseconds = delayUntilTime - System.currentTimeMillis();
      return unit.convert(remainingMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
     * integer as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
     */
    @Override
    public int compareTo(Delayed o) {
      long rightMilliseconds = o.getDelay(TimeUnit.MILLISECONDS);
      long leftMilliseconds = getDelay(TimeUnit.MILLISECONDS);
      return (leftMilliseconds < rightMilliseconds) ? -1 : (leftMilliseconds == rightMilliseconds) ? 0 : 1;
    }

  }

  /**
   * Random number source for handling delay, loss, and the like.
   */
  protected int delayBoundMilliseconds = 1000;
  protected double loseChance = 0.10;
  protected double corruptPacketChance = 0.10;
  protected double corruptByteChance = 0.50;


  protected Random random = new Random();
  protected DelayQueue<DelayedMessage> delayedMessages = new DelayQueue<DelayedMessage>();
  protected BlockingQueue<byte[]> receivedMessages = new LinkedBlockingDeque<byte[]>();
  protected SendingThread sendingThread = null;
  protected ReceivingThread receivingThread = null;
  protected Socket connection = null;
  protected DelayedMessage lastMessageSent = null;

  /**
   * This constructor makes an UnreliableNetworkBase that listens on the given local listenPort. Only returns after a
   * connection is made.
   *
   * @param listenPort the port to listen on
   * @throws SocketException
   */
  public UnreliableNetworkBase(int listenPort) throws IOException {
    super();
    ServerSocket listener = new ServerSocket(listenPort);
    connection = listener.accept();
    setUpConnection(connection);
  }

  /**
   * This constructor makes an UnreliableNetworkBase that connects to another one listening at
   * distantMachineAddress:distantPort. Only returns when connection is made.
   *
   * @param distantPort           port number to connect to
   * @param distandMachineAddress macdine to connect to
   * @throws SocketException
   */
  public UnreliableNetworkBase(int distantPort, String distandMachineAddress) throws IOException {
    super();
    connection = new Socket(distandMachineAddress, distantPort);
    setUpConnection(connection);
  }

  /**
   * Initializes the threads and the like to handle the sending and receiving.
   * @param connection
   */
  private void setUpConnection(Socket connection) {
    try {
      InputStream is = connection.getInputStream();
      OutputStream os = connection.getOutputStream();

      sendingThread = new SendingThread(delayedMessages, new ObjectOutputStream(os));
      receivingThread = new ReceivingThread(receivedMessages, new ObjectInputStream(is));
      ExecutorService es = getExecutorService();
      es.execute(sendingThread);
      es.execute(receivingThread);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Use the corruption settings of this network to corrupt the data in the given packet.
   *
   * @param p packet of data to corrupt
   */
  protected void corruptMaybe(DatagramPacket p) {
    if (random.nextDouble() < corruptPacketChance) {
      p.getData()[0] = '*'; // at least one is corrupt
      for (int i = 1; i < p.getData().length; ++i) {
        if ((random.nextDouble() < corruptByteChance) && (p.getData()[i] != 0))
          p.getData()[i] = '*';
      }
    }
  }

  /**
   * Send the data with the given delay.
   * @param p data to send
   * @param msDelay delay before sending in ms
   */
  protected void send(DatagramPacket p, long msDelay) {
    int length = p.getLength();
    byte[] nextMessage = new byte[length];
    System.arraycopy(p.getData(), 0, nextMessage, 0, length);
    lastMessageSent = new DelayedMessage(msDelay, nextMessage);

    delayedMessages.add(this.lastMessageSent);
  }

  /**
   * Send the packet
   * @param p packet to be sent
   * @throws IOException if
   */
  @Override
  public void send(DatagramPacket p) throws IOException {
    if (random.nextDouble() >= loseChance) {
      // Our message must be delayed by at least this much
      long lastMessageDelayFromNow = (lastMessageSent != null) ? lastMessageSent.getDelay(TimeUnit.MILLISECONDS) : 0;
      long delay = random.nextInt(delayBoundMilliseconds) + lastMessageDelayFromNow;
      corruptMaybe(p);
      send(p, delay);
    } else {
      System.err.println(String.format("Network failed to send: %s", new String(p.getData())));
    }
  }

  /**
   * Receive information into the given packet. As described in {@link DatagramSocket}, if the datagram buffer is
   * too short for the data, the data is silently truncated.
   * @param p packet to hold incoming data
   * @throws IOException thrown if there is a problem reading the data
   */
  @Override
  public synchronized void receive(DatagramPacket p) throws IOException {
    try {
      int timeout = getSoTimeout();
      // SO_TIMEOUT of zero is no timeout for the socket
      if (timeout == 0) timeout = Integer.MAX_VALUE;

      byte[] nextMessage = receivedMessages.poll(timeout, TimeUnit.MILLISECONDS);
      if (nextMessage == null) return;
      int length = Math.min(nextMessage.length, p.getData().length);
      System.arraycopy(nextMessage, 0, p.getData(), 0, length);
      p.setLength(length);
      p.setAddress(connection.getInetAddress());
      p.setPort(connection.getPort());

      if (ApplicationBase.getPrintDebugData()) {
        System.out.println(String.format("receiving (%d) '%s'", length, new String(nextMessage)));
      }
    } catch (InterruptedException e) {
      throw new SocketTimeoutException();
    }
  }


  public int getDelayBoundMilliseconds() {
    return delayBoundMilliseconds;
  }

  public void setDelayBoundMilliseconds(int delayBoundMilliseconds) {
    this.delayBoundMilliseconds = delayBoundMilliseconds;
  }

  public double getLoseChance() {
    return loseChance;
  }

  public void setLoseChance(double loseChance) {
    this.loseChance = loseChance;
  }

  public double getCorruptPacketChance() {
    return corruptPacketChance;
  }

  public void setCorruptPacketChance(double corruptPacketChance) {
    this.corruptPacketChance = corruptPacketChance;
  }

  public double getCorruptByteChance() {
    return corruptByteChance;
  }

  public void setCorruptByteChance(double corruptByteChance) {
    this.corruptByteChance = corruptByteChance;
  }

  /**
   * The single executor service available to run the threads that really handle sending and receiving; this is the
   * singleton pattern.
   */
  private static ExecutorService executorService = null;

  /**
   * Get a reference to the one ExecutorService. Create if this is the very first time it has been referenced.
   *
   * @return the executor
   */
  public static ExecutorService getExecutorService() {
    if (executorService == null)
      executorService = Executors.newCachedThreadPool();
    return executorService;
  }

  /**
   * Set the executor. Permits a client of this class to provide its own executor to this class so that another one is
   * not created.
   *
   * @param executorService new value for the executor
   */
  public static void setExecutorService(ExecutorService executorService) {
    executorService = executorService;
  }

}


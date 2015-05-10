package transport;

import network.UnreliableNetworkBase;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by blad on 3/31/15.
 */
public class TransportBase {
  public static final int BUFFER_SIZE = 1024;
  //4 byte header for checksum and seqNum
  public static final int HEADER_SIZE = 4;
  protected UnreliableNetworkBase network;

  public void sendMsg(byte[] msg) throws IOException {
    int length = msg.length;
    byte[] buffer = new byte[length];
    System.arraycopy(msg, 0, buffer, 0, Math.min(buffer.length, length));
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.send(dp);
  }


  public byte[] receiveMsg() throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.receive(dp);
    byte[] retVal = new byte[dp.getLength()];
    System.arraycopy(buffer, 0, retVal, 0, dp.getLength());
    return retVal;
  }

  public UnreliableNetworkBase getNetwork() {
    return network;
  }
}

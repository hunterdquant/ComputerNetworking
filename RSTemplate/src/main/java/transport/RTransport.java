package transport;

import network.RNetwork;
import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

/**
 * Created by blad on 3/31/15.
 */
public class RTransport extends TransportBase {

  /**
   * seqNum - the expected pkt to be sent and returned
   * reMsg - A copy of the last sent message for retrans.
   **/
  private int seqNum = 0;
  private byte[] reMsg;

  public RTransport(String distantMachine, int distantPort) {
    try {
      this.network = new RNetwork(distantPort, distantMachine);
      this.network.setSoTimeout(1000);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Asks for retrans if there is improper delivery. 
   *
   * @throws SocketTimeoutException if the network socket reaches timeout. 
   **/
  public byte[] receiveMsg() throws IOException {

    //Receive data
    byte[] buffer = new byte[BUFFER_SIZE + HEADER_SIZE];
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.receive(dp);

    //While you have a corrupt pkt or incorrect seqNum
    while (isCorrupt(buffer) || buffer[2] != seqNum) {
      //Resend
      sendMsg(reMsg);
      //Recieve data
      buffer = new byte[BUFFER_SIZE + HEADER_SIZE];
      dp = new DatagramPacket(buffer, buffer.length);
      network.receive(dp);
    }
    //Build return message
    byte[] retVal = new byte[dp.getLength()-HEADER_SIZE];
    System.arraycopy(buffer, HEADER_SIZE, retVal, 0, dp.getLength()-HEADER_SIZE);
    //Flip seqNum
    seqNum = seqNum == 0 ? 1 : 0;
    return retVal;
  }
 
  public void sendMsg(byte[] msg) throws IOException {
    //Capture a copy of the sent message.
    reMsg = msg;

    //Build the message with seqNum
    int length = msg.length;
    byte[] buffer = new byte[length+HEADER_SIZE];
    System.arraycopy(msg, 0, buffer, HEADER_SIZE, msg.length);
    buffer[2] = (byte)seqNum;
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.send(dp);
  }

  /**
   * @return true if pkt is corrupt else false
   **/
  private boolean isCorrupt(byte [] msg) {
    //Calculate the sum and add the carry.
    int sum = 0;
    for (int i = HEADER_SIZE; i < msg.length; i++) {
      sum += msg[i];
      sum += (sum >> 16) > 0 ? 1 : 0;
    }
    if (sum == 0) return true;
    return (((msg[1] & 0xFF) | ((msg[0] << 8) & 0xFFFF)) & (sum & 0xFFFF)) != 0;
  }
}

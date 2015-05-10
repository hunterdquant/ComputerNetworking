package transport;

import network.SNetwork;
import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by blad on 3/31/15.
 */
public class STransport extends TransportBase {
 
  /**
   * seqNum - the sequence number to be recieved and sent
   **/
  private int seqNum = 0;

  public STransport(int listenPort) {
    try {
      this.network = new SNetwork(listenPort);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  
  public void sendMsg(byte[] msg) throws IOException {
    //Build pkt
    int length = msg.length;
    byte[] buffer = new byte[length+HEADER_SIZE];
    System.arraycopy(msg, 0, buffer, HEADER_SIZE, msg.length);
    //Build header
    buildHeader(buffer);
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.send(dp);
  }

  public byte[] receiveMsg() throws IOException {
    //Receive message
    byte[] buffer = new byte[BUFFER_SIZE];
    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    network.receive(dp);
    //Set seqNum
    seqNum = buffer[2];
    //Build return message
    byte[] retVal = new byte[dp.getLength()-HEADER_SIZE];
    System.arraycopy(buffer, HEADER_SIZE, retVal, 0, dp.getLength()-HEADER_SIZE);
    return retVal;
  }


  public void buildHeader(byte [] msg) {
    //Set seqNum in pkt
    msg[2] = (byte)seqNum;

    //Calculate the sum and add the carry
    int sum = 0;
    for (int i = HEADER_SIZE; i < msg.length; i++) {
      sum += msg[i];
      sum += (sum >> 16) > 0 ? 1 : 0;
    }
    //Negate the sum
    sum = ~sum;
    //Store the checksum in the header
    msg[1] = (byte)(sum & 0xFF);
    msg[0] = (byte)(sum >> 8 & 0xFF);
  } 
}

/*
 * Name: Hunter Quant
 * Email: quanthd197@potsdam.edu
 * Assignment: p005
 * Edited: 03/15/2015
 */

package io;

import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPInputStream extends InputStream {
  
  /**
   * Socket for retrieving input.
   */
  private DatagramSocket socket;
  /**
   * Index for our input buffer.
   */
  private int bufferIndex;
  /**
   * Buffer for input.
   */
  private byte[] buffer;
  /**
   * Boolean flag for fill call.
   */
  private boolean hasFillBeenCalled;
  /**
   * The size of the read buffer.
   */
  private final int BUFF_SIZE = 32;

  /**
   * Construct a new UDPInputStream object.
   *
   * @param socket A DatagramSocket for reading data from the network.
   */
  public UDPInputStream(DatagramSocket socket) {
    this.socket = socket;
    this.bufferIndex = 0;
    this.buffer = new byte[BUFF_SIZE];
  }

  /**
   * Reads a single character value.
   * @return A single int representation of a character.
   */
  public int read() {

    //If the buffer is empty and fill has not been called, fill it.
    if (isBufferEmpty() && !hasFillBeenCalled) fill();

    //After the call to fill if the buffer is empty or our index
    //has passed the length of our buffer, return -1.
    if (isBufferEmpty() || bufferIndex >= buffer.length) {
      //Set empty buffer.
      buffer = new byte[BUFF_SIZE];
      return -1;
    }
    //Return the character at bufferIndex and post increment.
    return buffer[bufferIndex++] & 0xff;
  }

  /**
   * @param b a byte array.
   * @param off an interger offset.
   * @param len the length of the buffer.
   * @throws IOException.
   */
  public int read(byte b[], int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    hasFillBeenCalled = false;

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte)c;

    int i = 1;
    for (; i < len ; i++) {
      c = read();
      if (c == -1) {
        break;
      }
      b[off + i] = (byte)c;
    }
    hasFillBeenCalled = false;
    return i;
  }

  /**
   * Fills the buffer if it is empty.
   */
  private void fill() {
    //Flag for fill call.
    hasFillBeenCalled = true;
    //Create new temporary buffer
    byte[] temp = new byte[BUFF_SIZE];

    try {
      //Create and fill a packet with temp buffer.
      DatagramPacket rcvPkt = new DatagramPacket(temp, temp.length);
      socket.receive(rcvPkt);

      //Set as a new array with pkt length.
      buffer = new byte[rcvPkt.getLength()];
      //Copy contents of pkt to the buffer.
      System.arraycopy(rcvPkt.getData(), 0, buffer, 0, buffer.length);
    } catch (IOException ioe) {
      ioe.printStackTrace();  
    }
    //Reset buffer index
    bufferIndex = 0;
  }
  
  /**
   * Checks to see if the input buffer is empty.
   * @return True is buffer is empty else false. 
   */
  private boolean isBufferEmpty() {
    //If any element is not 0 then the buffer is not empty.
    for (byte i : buffer) {
      if (i != 0) return false;
    }
    bufferIndex = 0;
    return true;
  }
}

/*
 * Name: Hunter Quant
 * Email: quanthd197@potsdam.edu
 * Assignment: p005
 * Edited: 03/15/2015
 */

package io;

import java.util.Arrays;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPOutputStream extends OutputStream {
  
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
   * The size of the read buffer.
   */
  private final int BUFF_SIZE = 32;
  /**
   * Portnumber to send to.
   */
  private int portNumber;
  /**
   * machineAddress to send to.
   */
  private InetAddress machineAddress;
  
  /**
   * Constructs a new UDPOutputStream object.
   *
   * @param socket A DatagreamSocket for sending messages.
   * @param address The specified address for the distant machine..
   * @param port The specified port for the distant machine.
   */
  public UDPOutputStream(DatagramSocket socket, InetAddress address, int port) {
    this.socket = socket;
    this.bufferIndex = 0;
    this.buffer = new byte[BUFF_SIZE];
    this.machineAddress = address;
    this.portNumber = port;
  }
  
  /**
   * @param c A integer representation of a character to be sent
   *          after the buffer is filled.
   * @throws IOException.
   */
  public void write(int c) throws IOException {
    //Flush the buffer is it is full or c is a new line character.
    if (bufferIndex >= buffer.length || c == '\n') {

      //Add that new line to the buffer.
      if (c == '\n' && !(bufferIndex >= buffer.length)) buffer[bufferIndex++] = '\n';
      flush();
    }
    //Add the character to the buffer.
    buffer[bufferIndex++] = (byte)c;
  }
  
  /**
   * Clears the buffer and sends it's contents.
   */
  public void flush() {
    //If the is a character in the buffer.
    if (bufferIndex > 0) {
      try {
        //Create our sending pkt and send.
        DatagramPacket sendPkt = new DatagramPacket(buffer, bufferIndex, machineAddress, portNumber);
        socket.send(sendPkt);
        //Reset the buffer.
        buffer = new byte[BUFF_SIZE];
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      //Reset bufferIndex.
      bufferIndex = 0;
    }
  }
}

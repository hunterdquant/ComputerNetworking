package clients;

import cards.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ReceiverThread extends Thread {
  final Socket socket;
  Scanner scanner;
  private Hand hand;

  public ReceiverThread(Socket socket) {
    this.hand = new Hand();
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      scanner = new Scanner(socket.getInputStream());
      String line;
      while ((line = scanner.nextLine()) != null) {
        
        //If the message is a swap message
        if (line.charAt(0) == 'S') {
          System.out.println(line);
          hand.remove(Integer.parseInt(line.substring(5,6)));
          hand.add(parseCard(line.substring(6)));
          System.out.println(hand);
          System.out.print("> ");

        //The message is a standard add message
        } else{
          System.out.println(line);
          hand.add(parseCard(line));
          System.out.println(hand);
          System.out.print("> ");
        }
      }
    } catch (SocketException e) {
      // output thread closed the socket
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  /**
   * Parses the server response into a card object
   *
   * @param line the server response.
   */
  private Card parseCard(String line) {

    //Split card value and suit
    String [] cardParams = line.trim().split(" ");
    String cardNum = cardParams[0];
    String cardSuit = cardParams[1];
    //Get the card value
    if (cardNum.equals("A")) cardNum = "1";
    else if (cardNum.equals("J")) cardNum = "11";
    else if (cardNum.equals("Q")) cardNum = "12";
    else if (cardNum .equals("K")) cardNum = "13";

    //return card with value and suit.
    if (cardSuit.equals("Diamond")) return new Card(
        Integer.parseInt(cardNum), Card.Suit.Diamond);
    else if (cardSuit.equals("Spade")) return new Card(
        Integer.parseInt(cardNum), Card.Suit.Spade);
    else if (cardSuit.equals("Club")) return new Card(
        Integer.parseInt(cardNum), Card.Suit.Club);
    else return new Card(Integer.parseInt(cardNum), Card.Suit.Heart);
  }
}

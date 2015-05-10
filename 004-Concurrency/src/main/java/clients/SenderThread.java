package clients;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SenderThread extends Thread {

  final private BlockingQueue<String> messageQueue;
  final private Socket socket;

  public SenderThread(Socket socket,
                      BlockingQueue<String> messageQueue) {
    this.messageQueue = messageQueue;
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      PrintStream pw = new PrintStream(socket.getOutputStream());
      //Get hand

      while (true) {
        String message = null;
        try {
          message = messageQueue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
          // there is no ready message; go back to top of loop
          continue;
        }

        pw.println(message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}

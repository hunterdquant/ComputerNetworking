package network;

import java.io.IOException;

/**
 * Created by blad on 3/31/15.
 */
public class SNetwork extends UnreliableNetworkBase {
  public SNetwork(int listenPort) throws IOException {
    super(listenPort);
  }
}

package network;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by blad on 3/31/15.
 */
public class RNetwork extends UnreliableNetworkBase {

  public RNetwork(int distantPort, String distandMachineAddress) throws IOException {
    super(distantPort, distandMachineAddress);
  }
}

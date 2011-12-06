/**
* 
* Copyright (C) 2004-2008 FhG Fokus
*
* This file is part of the FhG Fokus UPnP stack - an open source UPnP implementation
* with some additional features
*
* You can redistribute the FhG Fokus UPnP stack and/or modify it
* under the terms of the GNU General Public License Version 3 as published by
* the Free Software Foundation.
*
* For a license to use the FhG Fokus UPnP stack software under conditions
* other than those described here, or to purchase support for this
* software, please contact Fraunhofer FOKUS by e-mail at the following
* addresses:
*   upnpstack@fokus.fraunhofer.de
*
* The FhG Fokus UPnP stack is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>
* or write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/
package de.fraunhofer.fokus.upnp.gateway.tcp_tunnel.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class is used to create a tunnel TCP client and to sent many many bytes.
 * 
 * @author Alexander Koenig
 */
public class TCPClientTunnelTest extends Thread
{

  /** Socket for the connection */
  private Socket                    socket;

  /** Address of the server */
  private InetSocketAddress         serverSocketAddress;

  /** Inet address for the TCP tunnel */
  private InetAddress               ipTunnelInetAddress;

  private TCPTunnelNetworkInterface ipTunnelNetworkInterface;

  private IPTunnelSocket            tunnelSocket;

  private IPTunnelDatagramSocket    tunnelDatagramSocket;

  private boolean                   terminateThread = false;

  private EventThread               eventThread;

  /**
   * Creates a new instance of TCPClientTunnelManagement.
   * 
   * @param serverSocketAddress
   *          Address and port of the IP tunnel server
   * @param ipTunnelInetAddress
   *          The address used for the virtual IP tunnel
   */
  public TCPClientTunnelTest(InetSocketAddress serverSocketAddress, InetAddress ipTunnelInetAddress)
  {
    setName("TCPClientTunnelManagement");
    this.serverSocketAddress = serverSocketAddress;
    this.ipTunnelInetAddress = ipTunnelInetAddress;

    socket = new Socket();

    start();

    eventThread = new EventThread("TCPClientTunnel");
    EventThread.EVENT_THREAD_SLEEP_TIME = 1;
    eventThread.start();
  }

  public static void main(String[] args)
  {
    try
    {
      new TCPClientTunnelTest(new InetSocketAddress("localhost", 8888), InetAddress.getByName("0.0.0.1"));
    } catch (UnknownHostException e)
    {
    }
  }

  public void run()
  {
    long lastInterval = Portable.currentTimeMillis();
    long tcpBytes = 0;
    long udpBytes = 0;
    byte[] tcpReceiveBuffer = new byte[4096];

    while (!terminateThread)
    {
      // try to connect to server
      if (!socket.isConnected())
      {
        try
        {
          socket.connect(serverSocketAddress, 10000);

          System.out.println("Connected to " + IPHelper.toString(serverSocketAddress));

          // create virtual network interface
          ipTunnelNetworkInterface = new TCPTunnelNetworkInterface(socket);

          ipTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);
          eventThread.register(ipTunnelNetworkInterface);

          // set optional network interface parameters
          ipTunnelNetworkInterface.setMaximumSegmentSize(1400);
          ipTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
          ipTunnelNetworkInterface.setPacketGapTime(0);

          tunnelSocket = ipTunnelNetworkInterface.getIPTunnelSocketFactory().createIPTunnelSocket(1);
          tunnelSocket.connect(ipTunnelInetAddress.getHostAddress(), 1);

          tunnelDatagramSocket = ipTunnelNetworkInterface.getIPTunnelSocketFactory().createIPTunnelDatagramSocket(2);
        } catch (Exception e)
        {
          System.out.println("An error occured: " + e.getMessage());
          socket = new Socket();
        }
      }
      // try to read data from virtual TCP socket
      if (socket.isConnected() && tunnelSocket.isEstablished())
      {
        try
        {
          while (tunnelSocket.getInputStream().available() > 0)
          {
            tcpBytes += tunnelSocket.getInputStream().read(tcpReceiveBuffer);
          }
        } catch (Exception e)
        {
        }
        try
        {
          BinaryMessageObject message = null;
          do
          {
            message = SocketHelper.readBinaryMessage(null, tunnelDatagramSocket, 1);
            if (message != null)
            {
              udpBytes += message.getBody().length;
            }
          } while (message != null);
        } catch (Exception e)
        {
          // System.out.println("An error occured: " + e.getMessage());
        }
        long interval = Portable.currentTimeMillis() - lastInterval;
        if (interval > 3000)
        {
          System.out.println((int)(tcpBytes * 1000.0f / interval) / 1024 + " kbytes per second TCP");
          System.out.println((int)(udpBytes * 1000.0f / interval) / 1024 + " kbytes per second UDP");
          lastInterval = Portable.currentTimeMillis();
          tcpBytes = 0;
          udpBytes = 0;
        }
      }
      ThreadHelper.sleep(1);
    }
  }

  /** Terminates the forwarder module for the TCP tunnel. */
  public void terminate()
  {
    eventThread.terminate();
    terminateThread = true;

    if (ipTunnelNetworkInterface != null)
    {
      ipTunnelNetworkInterface.terminate();
    }
  }

}

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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelServerSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class is used to tunnel UPnP over a TCP connection. This class provides a TCP server.
 * 
 * @author Alexander Koenig
 */
public class TCPServerTunnelTest extends Thread
{

  /** Server socket to accept connections */
  private ServerSocket              serverSocket;

  private TCPTunnelNetworkInterface ipTunnelNetworkInterface;

  private IPTunnelServerSocket      tunnelServerSocket;

  private IPTunnelSocket            tunnelSocket;

  private IPTunnelDatagramSocket    tunnelDatagramSocket;

  /** Inet address for the TCP tunnel */
  private InetAddress               ipTunnelInetAddress;

  private boolean                   terminateThread = false;

  private boolean                   terminated      = false;

  private EventThread               eventThread;

  /**
   * @param messageForwarder
   * @param serverPort
   *          Listener port for the server
   * @param ipTunnelInetAddress
   *          Inet address used in the tunnel
   */
  public TCPServerTunnelTest(int serverPort, InetAddress ipTunnelInetAddress)
  {
    setName("VirtualTCPServer");
    this.ipTunnelInetAddress = ipTunnelInetAddress;
    eventThread = new EventThread("TCPServerTunnel");
    EventThread.EVENT_THREAD_SLEEP_TIME = 1;
    eventThread.start();

    try
    {
      serverSocket = new ServerSocket(serverPort);
      serverSocket.setSoTimeout(1000);
    } catch (IOException e)
    {
      System.out.println("An error occured: " + e.getMessage());
      System.exit(1);
    }
    start();
  }

  public static void main(String[] args)
  {
    try
    {
      new TCPServerTunnelTest(8888, InetAddress.getByName("0.0.0.1"));
    } catch (UnknownHostException e)
    {
    }
  }

  public void run()
  {
    System.out.println("\n  Started VirtualTCPServer. Wait for client to connect on port " +
      serverSocket.getLocalPort());
    byte[] udpSendBuffer = new byte[1300];
    for (int i = 0; i < udpSendBuffer.length; i++)
    {
      udpSendBuffer[i] = (byte)i;
    }
    DatagramPacket udpPacket = new DatagramPacket(udpSendBuffer, udpSendBuffer.length, ipTunnelInetAddress, 2);
    byte[] tcpSendBuffer = new byte[32768];
    for (int i = 0; i < tcpSendBuffer.length; i++)
    {
      tcpSendBuffer[i] = (byte)i;
    }
    while (!terminateThread)
    {
      if (ipTunnelNetworkInterface == null)
      {
        // try to accept one connection
        try
        {
          Socket clientSocket = serverSocket.accept();

          // create virtual network interface
          ipTunnelNetworkInterface = new TCPTunnelNetworkInterface(clientSocket);

          ipTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);
          eventThread.register(ipTunnelNetworkInterface);

          // set optional network interface parameters
          ipTunnelNetworkInterface.setMaximumSegmentSize(1400);
          ipTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
          ipTunnelNetworkInterface.setPacketGapTime(0);

          System.out.println("Client connected from " +
            IPHelper.toString((InetSocketAddress)clientSocket.getRemoteSocketAddress()));

          // create virtual TCP socket
          tunnelServerSocket = ipTunnelNetworkInterface.getIPTunnelSocketFactory().createIPTunnelServerSocket(1);
          // create virtual UDP socket
          tunnelDatagramSocket = ipTunnelNetworkInterface.getIPTunnelSocketFactory().createIPTunnelDatagramSocket(2);
        } catch (SocketTimeoutException ste)
        {
        } catch (Exception e)
        {
          System.out.println("An error occured: " + e.getMessage());
        }
      }
      if (tunnelSocket == null && tunnelServerSocket != null)
      {
        System.out.println("Tunnel server socket created. Wait for tunnel client...");
        // try to accept one tunnel connection
        try
        {
          tunnelSocket = tunnelServerSocket.accept();
          System.out.println("Tunnel client accepted");
        } catch (Exception e)
        {
          System.out.println("An error occured: " + e.getMessage());
        }
      }
      // send data through socket
      //      if (tunnelSocket != null)
      //      {
      //        try
      //        {
      //          tunnelSocket.getOutputStream().write(tcpSendBuffer, 0, tcpSendBuffer.length);
      //        } catch (IOException e)
      //        {
      //          System.out.println("An error occured: " + e.getMessage());
      //        }
      //      }
      // send data through socket
      if (tunnelDatagramSocket != null)
      {
        for (int i = 0; i < 30; i++)
        {
          try
          {
            tunnelDatagramSocket.send(udpPacket);
          } catch (Exception e)
          {
            System.out.println("An error occured: " + e.getMessage());
          }
        }
      }
      ThreadHelper.sleep(1);
    }
    terminated = true;
  }

  /** Terminates the management for the TCP tunnel. */
  public void terminate()
  {
    eventThread.terminate();
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(50);
      } catch (Exception e)
      {
      }
    }
    if (ipTunnelNetworkInterface != null)
    {
      ipTunnelNetworkInterface.terminate();
    }
    System.out.println("Terminated virtual TCP server");
  }

}

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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class connects to a local TCP server and sends many many bytes.
 * 
 * @author Alexander Koenig
 */
public class TCPClientTest extends Thread
{

  /** Socket for the connection */
  private Socket            clientSocket;

  /** Address of the server */
  private InetSocketAddress serverSocketAddress;

  //  private long              nextSend        = System.currentTimeMillis();

  private boolean           terminateThread = false;

  /**
   * Creates a new instance of TCPClientTest.
   * 
   * @param serverSocketAddress
   */
  public TCPClientTest(InetSocketAddress serverSocketAddress)
  {
    setName("TCPClientTunnelManagement");
    this.serverSocketAddress = serverSocketAddress;

    clientSocket = new Socket();
    try
    {
      clientSocket.setReceiveBufferSize(TCPTunnelNetworkInterface.DEFAULT_RECEIVE_WINDOW_SIZE);
    } catch (SocketException e)
    {
    }

    start();
  }

  public static void main(String[] args)
  {
    new TCPClientTest(new InetSocketAddress("localhost", 8888));
  }

  public void run()
  {
    byte[] sendBuffer = new byte[40000];
    for (int i = 0; i < sendBuffer.length; i++)
    {
      sendBuffer[i] = (byte)i;
    }

    long lastInterval = Portable.currentTimeMillis();
    long tcpBytes = 0;
    byte[] buffer = new byte[131072];

    while (!terminateThread)
    {
      // try to connect to server
      if (!clientSocket.isConnected())
      {
        try
        {
          clientSocket.connect(serverSocketAddress, 10000);
          clientSocket.setSoTimeout(50);

          System.out.println("Connected to " + IPHelper.toString(serverSocketAddress));
        } catch (Exception e)
        {
          System.out.println("An error occured: " + e.getMessage());
          clientSocket = new Socket();
        }
      }
      // send data through socket
      if (clientSocket != null)
      {
        try
        {
          clientSocket.getOutputStream().write(sendBuffer);
        } catch (IOException e)
        {
          System.out.println("An error occured: " + e.getMessage());
        }
      }
      // receive data from socket
      try
      {
        if (clientSocket != null && clientSocket.getInputStream().available() > 0)
        {
          tcpBytes += clientSocket.getInputStream().read(buffer);
        }
      } catch (Exception e)
      {
        System.out.println("An error occured: " + e.getMessage());
      }
      long interval = Portable.currentTimeMillis() - lastInterval;
      if (interval > 3000)
      {
        System.out.println((int)(tcpBytes * 1000.0f / interval) / 1024 + " kbytes per second TCP");
        lastInterval = Portable.currentTimeMillis();
        tcpBytes = 0;
      }
      ThreadHelper.sleep(1);
    }
  }

  /** Retrieves a string containing a timestamp */
  public static String timeStamp()
  {
    long time = System.currentTimeMillis();
    if (time % 1000 < 10)
    {
      return time / 1000 % 100 + ".00" + time % 1000 + ": ";
    } else if (time % 1000 < 100)
    {
      return time / 1000 % 100 + ".0" + time % 1000 + ": ";
    } else
    {
      return time / 1000 % 100 + "." + time % 1000 + ": ";
    }
  }

  /** Terminates the forwarder module for the TCP tunnel. */
  public void terminate()
  {
    terminateThread = true;
  }

}

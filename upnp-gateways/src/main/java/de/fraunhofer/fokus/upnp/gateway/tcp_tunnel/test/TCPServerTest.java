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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class opens a local TCP server and waits for clients.
 * 
 * @author Alexander Koenig
 */
public class TCPServerTest extends Thread
{

  /** Server socket to accept connections */
  private ServerSocket serverSocket;

  private Socket       clientSocket;

  private boolean      terminateThread = false;

  private boolean      terminated      = false;

  /**
   * Creates a new instance of TCPServerTest.
   * 
   * @param serverPort
   */
  public TCPServerTest(int serverPort)
  {
    setName("VirtualTCPServer");
    try
    {
      serverSocket = new ServerSocket();
      serverSocket.setReceiveBufferSize(TCPTunnelNetworkInterface.DEFAULT_RECEIVE_WINDOW_SIZE);
      serverSocket.bind(IPHelper.toSocketAddress("0.0.0.0:" + serverPort));
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
    new TCPServerTest(8888);
  }

  public void run()
  {
    System.out.println("\n  Started VirtualTCPServer. Wait for client to connect on port " +
      serverSocket.getLocalPort());

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
      if (clientSocket == null)
      {
        // try to accept one connection
        try
        {
          clientSocket = serverSocket.accept();
          Portable.println("Receive buffer is " + clientSocket.getReceiveBufferSize());
        } catch (SocketTimeoutException ste)
        {
        } catch (Exception e)
        {
          System.out.println("An error occured: " + e.getMessage());
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
    terminated = true;
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

  /** Terminates the management for the TCP tunnel. */
  public void terminate()
  {
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

    try
    {
      clientSocket.close();
      serverSocket.close();
    } catch (IOException e)
    {
    }

    System.out.println("Terminated virtual TCP server");
  }

}

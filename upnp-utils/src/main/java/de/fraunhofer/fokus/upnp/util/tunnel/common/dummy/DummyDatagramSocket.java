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
package de.fraunhofer.fokus.upnp.util.tunnel.common.dummy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class represents a DatagramSocket that sends packets into the nirvana and receives nothing. Useful for
 * debugging.
 * 
 * @author Alexander Koenig
 */
public class DummyDatagramSocket implements IDatagramSocket
{
  /** Virtual port used by this socket */
  private int           virtualPort;

  protected InetAddress hostAddress;

  protected int         socketTimeout = 0;

  protected boolean     closeSocket   = false;

  /**
   * Creates a new instance of DummyDatagramSocket.
   * 
   * @param port
   * @throws IOException
   */
  public DummyDatagramSocket(InetAddress hostAddress, int port) throws IOException
  {
    this.hostAddress = hostAddress;
    this.virtualPort = port;
    if (virtualPort == 0)
    {
      virtualPort = (int)(Portable.random() * 64000) + 1025;
    }
  }

  /** Closes the socket */
  public void close()
  {
    closeSocket = true;
  }

  /** Sends a packet over the socket */
  public void send(DatagramPacket p)
  {
  }

  /** Sends a packet over the socket with a fake source address */
  public void send(DatagramPacket p, InetSocketAddress sourceAddress)
  {
  }

  /** Waits for received packets */
  public void receive(DatagramPacket p) throws SocketException, SocketTimeoutException
  {
    long time = System.currentTimeMillis();
    // wait for a packet
    while (!closeSocket && (socketTimeout == 0 || System.currentTimeMillis() - time < socketTimeout))
    {
      try
      {
        Thread.sleep(Math.max(5, Math.min(50, getSoTimeout())));
      } catch (Exception ex)
      {
      }
    }
    if (closeSocket)
    {
      throw new SocketException("Socket closed");
    }
    throw new SocketTimeoutException("Operation timed out");
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /** Retrieves the local IP address of this socket (e.g., 192.168.x.y) */
  public InetAddress getLocalAddress()
  {
    return hostAddress;
  }

  /** Retrieves the local port of this socket */
  public int getLocalPort()
  {
    return virtualPort;
  }

  /** Retrieves the local IP address and port of this socket */
  public SocketAddress getLocalSocketAddress()
  {
    return new InetSocketAddress(getLocalAddress(), virtualPort);
  }

  /** Retrieves the SO_TIMEOUT, in milliseconds. */
  public int getSoTimeout()
  {
    return socketTimeout;
  }

  /** Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds. */
  public void setSoTimeout(int timeout)
  {
    socketTimeout = Math.max(0, timeout);
  }

}

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
package de.fraunhofer.fokus.upnp.util.tunnel.network_interface;

import java.net.InetSocketAddress;
import java.net.Socket;

import de.fraunhofer.fokus.upnp.util.tunnel.common.StreamPacketTunnel;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.AbstractIPTunnelNetworkInterface;

/**
 * This class is an implementation of the virtual IP tunnel that uses a TCP connection as tunnel.
 * 
 * @author Alexander Koenig
 */
public class TCPTunnelNetworkInterface extends AbstractIPTunnelNetworkInterface
{

  public static final int DEFAULT_RECEIVE_WINDOW_SIZE = 1048576;

  /** Socket used to transmit tunneled packets */
  private Socket          socket;

  /**
   * Creates a new instance of TCPTunnelNetworkInterface.java
   * 
   * @param socket
   *          The socket used for the tunnel
   */
  public TCPTunnelNetworkInterface(Socket socket)
  {
    // TCP can be modelled using in- and output streams
    packetTunnel = new StreamPacketTunnel(this);

    // initialize socket streams etc.
    initializeSocket(socket, false);
  }

  /** Starts the reconnection process */
  public void reconnect(Socket socket)
  {
    initializeSocket(socket, true);
  }

  /** Updates the socket associated with this interface */
  public void initializeSocket(Socket socket, boolean reconnect)
  {
    // close old socket
    try
    {
      if (this.socket != null)
      {
        this.socket.close();
        this.socket = null;
      }
    } catch (Exception e)
    {
    }

    // if (reconnect)
    // {
    // System.out.println("Reconnect socket for IP tunnel to " +
    // IPAddress.toString((InetSocketAddress)socket.getRemoteSocketAddress()));
    // }
    connectionTime = System.currentTimeMillis();
    lastReceivedPacketTime = System.currentTimeMillis();

    this.socket = socket;
    try
    {
      // set socket timeout to simplify termination
      socket.setSoTimeout(50);

      packetTunnel.resetBuffer();
      ((StreamPacketTunnel)packetTunnel).setInputStream(socket.getInputStream());
      ((StreamPacketTunnel)packetTunnel).setOutputStream(socket.getOutputStream());

      // convenience method for derived classes
      if (reconnect)
      {
        reconnectNetworkInterface();
      }
    } catch (Exception ex)
    {
      System.out.println("    The input or output stream could not be created: " + ex.getMessage());
      socket = null;
      packetTunnel.closeConnection();
    }
  }

  /** Removes the socket from this network interface */
  public Socket extractSocket()
  {
    Socket result = socket;

    // mark this network interfaces as unusable
    packetTunnel = null;
    socket = null;

    return result;
  }

  /**
   * Retrieves the remote socket address for the outer TCP connection.
   * 
   * @return
   */
  public InetSocketAddress getRemoteSocketAddress()
  {
    if (socket != null)
    {
      return (InetSocketAddress)socket.getRemoteSocketAddress();
    }

    return null;
  }

  /** Retrieves the socket used for IP tunneling */
  public Socket getSocket()
  {
    return socket;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.AIPTunnelNetworkInterface#closeOuterConnection()
   */
  protected void closeOuterConnection()
  {
    // close socket
    try
    {
      socket.close();
    } catch (Exception ex)
    {
    }
    socket = null;

    super.closeOuterConnection();
  }

}

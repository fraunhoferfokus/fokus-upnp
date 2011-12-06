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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ISendPacketTunnelListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ListenerPacketTunnel;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.AbstractIPTunnelNetworkInterface;

/**
 * This class does the actual work to send and receive tunneled IP messages over UDP.
 * 
 * @author Alexander Koenig
 */
public class UDPTunnelNetworkInterface extends AbstractIPTunnelNetworkInterface implements ISendPacketTunnelListener
{

  private DatagramSocket    outerTunnelDatagramSocket = null;

  private InetSocketAddress peerSocketAddress;

  private int               localPort;

  /**
   * Creates a new instance of UDPTunnelNetworkInterface.
   * 
   */
  public UDPTunnelNetworkInterface(int localPort, InetSocketAddress peerSocketAddress)
  {
    this.localPort = localPort;
    this.peerSocketAddress = peerSocketAddress;

    packetTunnel = new ListenerPacketTunnel(this, this);

    // initialize socket streams etc.
    initializeClient();
  }

  /** Updates the socket associated with this interface */
  public void initializeClient()
  {
    connectionTime = System.currentTimeMillis();
    lastReceivedPacketTime = System.currentTimeMillis();

    packetTunnel.resetBuffer();

    try
    {
      outerTunnelDatagramSocket = new DatagramSocket(localPort);
      outerTunnelDatagramSocket.setSoTimeout(30);
    } catch (Exception e)
    {
      outerTunnelDatagramSocket = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#sendPacket(byte[])
   */
  public void sendPacket(byte[] data) throws IOException
  {
    if (outerTunnelDatagramSocket != null)
    {
      BinaryMessageObject messageObject =
        new BinaryMessageObject(data,
          (InetSocketAddress)outerTunnelDatagramSocket.getLocalSocketAddress(),
          peerSocketAddress);

      SocketHelper.sendBinaryMessage(messageObject, outerTunnelDatagramSocket);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#closeConnection()
   */
  public void closeConnection()
  {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#isValidConnection()
   */
  public boolean isValidConnection()
  {
    return outerTunnelDatagramSocket != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.AbstractIPTunnelNetworkInterface#triggerEvents()
   */
  public void triggerEvents()
  {
    // try to receive packets from socket
    BinaryMessageObject receivedMessageObject = SocketHelper.readBinaryMessage(null, outerTunnelDatagramSocket, 30);
    // check if a valid message from the peer has been received
    if (receivedMessageObject != null && receivedMessageObject.getSourceAddress().equals(peerSocketAddress))
    {
      byte[] data = receivedMessageObject.getBody();
      packetTunnel.handleReceivedData(data, 0, data.length);
    }

    super.triggerEvents();
  }
}

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
import java.io.OutputStream;

import de.fraunhofer.fokus.upnp.util.tunnel.common.ISendPacketTunnelListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ListenerPacketTunnel;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.AbstractIPTunnelNetworkInterface;

/**
 * This class is an implementation of a tunnel over a serial connection.
 * 
 * @author Alexander Koenig
 */
public class SerialTunnelNetworkInterface extends AbstractIPTunnelNetworkInterface implements ISendPacketTunnelListener
{
  /** Output stream used to send data */
  private OutputStream outputStream;

  /**
   * Creates a new instance of SerialTunnelNetworkInterface.
   * 
   * @param outputStream
   *          The output stream used to send data
   * @param name
   *          Name for the interface
   * 
   */
  public SerialTunnelNetworkInterface(OutputStream outputStream, String name)
  {
    packetTunnel = new ListenerPacketTunnel(this, this);
    // serial connection handles stream byte order and error detection
    packetTunnel.setStreamTunnel(true);

    initializeConnection(outputStream, false);
  }

  /** Updates the socket associated with this interface */
  public void initializeConnection(OutputStream outputStream, boolean reconnect)
  {
    // if (reconnect)
    // {
    // System.out.println("Reconnect socket for IP tunnel to " +
    // IPAddress.toString((InetSocketAddress)socket.getRemoteSocketAddress()));
    // }

    connectionTime = System.currentTimeMillis();
    lastReceivedPacketTime = System.currentTimeMillis();

    this.outputStream = outputStream;

    packetTunnel.resetBuffer();

    // convenience method for derived classes
    if (reconnect)
    {
      reconnectNetworkInterface();
    }
  }

  /**
   * Event that data has been received from the serial connection.
   * 
   * 
   * @param data
   */
  public void dataReceived(byte[] data)
  {
    int handled = packetTunnel.handleReceivedData(data, 0, data.length);
    if (handled < data.length)
    {
      System.out.println("Could not process all received serial data");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#sendPacket(byte[])
   */
  public void sendPacket(byte[] data) throws IOException
  {
    if (outputStream == null)
    {
      return;
    }

    outputStream.write(data);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.AIPTunnelNetworkInterface#closeOuterConnection()
   */
  protected void closeOuterConnection()
  {
    if (outputStream != null)
    {
      try
      {
        outputStream.close();
      } catch (IOException e)
      {
      }
    }
    outputStream = null;

    super.closeOuterConnection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#closeConnection()
   */
  public void closeConnection()
  {
    closeOuterConnection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.ISendPacketTunnelListener#isValidConnection()
   */
  public boolean isValidConnection()
  {
    return outputStream != null;
  }

}

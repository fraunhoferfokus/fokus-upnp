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
package de.fraunhofer.fokus.upnp.util.tunnel.common;

import java.io.IOException;

/**
 * This class can be used to send and receive packets over a tunnel that receives packets via outer events. The data
 * received event listener class must forward received data to the tunnel for further processing. The send packet
 * listener is responsible to actually send byte arrays to the tunnel. The correct packet order for reception must be
 * maintained by the tunnel protocols. Among others, this can be used for tunnels over serial connections.
 * 
 * 
 * @author Alexander Koenig
 * 
 */
public class ListenerPacketTunnel extends AbstractPacketTunnel
{

  /** Listener that implements the actual send method */
  private ISendPacketTunnelListener sendPacketTunnelListener;

  /**
   * Creates a new instance of ListenerPacketTunnel.java
   * 
   * @param tunnelListener
   * @param sendPacketTunnelListener
   */
  public ListenerPacketTunnel(IPacketTunnelListener tunnelListener, ISendPacketTunnelListener sendPacketTunnelListener)
  {
    super(tunnelListener);

    this.sendPacketTunnelListener = sendPacketTunnelListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.APacketTunnel#sendInternal(byte[])
   */
  public void sendPacketInternal(byte[] data) throws IOException
  {
    if (sendPacketTunnelListener == null)
    {
      return;
    }
    sendPacketTunnelListener.sendPacket(data);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.APacketTunnel#isValidConnection()
   */
  public boolean isConnected()
  {
    return sendPacketTunnelListener != null && sendPacketTunnelListener.isValidConnection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.IPacketTunnel#closeConnection()
   */
  public void closeConnection()
  {
    if (sendPacketTunnelListener != null)
    {
      sendPacketTunnelListener.closeConnection();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    // nothing to do here
  }
}

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
package de.fraunhofer.fokus.upnp.gateway.tcp_tunnel;

import java.net.Socket;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.AbstractIPTunnelForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class can be used to forward messages over a TCP socket connection.
 * 
 * @author Alexander Koenig
 * 
 */
public class TCPTunnelForwarderModule extends AbstractIPTunnelForwarderModule
{

  /**
   * Creates a new instance of TCPTunnelForwarderModule.
   * 
   * @param messageForwarder
   *          The associated message forwarder
   * @param ipTunnelNetworkInterface
   *          The associated virtual network interface
   * @param upnpTunnelSocketStructure
   *          The associated socket structure
   * @param id
   *          Unique ID for this forwarder
   */
  public TCPTunnelForwarderModule(MessageForwarder messageForwarder,
    TCPTunnelNetworkInterface tcpTunnelNetworkInterface,
    UPnPTunnelSocketStructure ipTunnelSocketStructure,
    String id)
  {
    super(messageForwarder, tcpTunnelNetworkInterface, ipTunnelSocketStructure, id);
    this.moduleID = "TCPTunnel_" + id;
  }

  /**
   * Reconnects this forwarder module.
   * 
   * @param socket
   *          The new socket for the tunnel
   */
  public void reconnect(Socket socket)
  {
    ((TCPTunnelNetworkInterface)ipTunnelNetworkInterface).reconnect(socket);
  }

}

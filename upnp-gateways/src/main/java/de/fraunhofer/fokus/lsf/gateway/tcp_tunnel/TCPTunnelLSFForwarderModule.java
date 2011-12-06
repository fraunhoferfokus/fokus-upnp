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
package de.fraunhofer.fokus.lsf.gateway.tcp_tunnel;

import java.net.Socket;

import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.lsf.gateway.common.tunnel.TunnelLSFSocketStructure;
import de.fraunhofer.fokus.lsf.gateway.common.tunnel.TunnelLSFForwarderModule;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class can be used to forward LSF messages over an IP tunnel.
 * 
 * @author Alexander Koenig
 * 
 */
public class TCPTunnelLSFForwarderModule extends TunnelLSFForwarderModule
{

  /**
   * Creates a new instance of TunnelLSFForwarderModule.
   * 
   * @param messageForwarder
   * @param ipTunnelNetworkInterface
   * @param socketStructure
   */
  public TCPTunnelLSFForwarderModule(LSFMessageForwarder messageForwarder,
    TCPTunnelNetworkInterface ipTunnelNetworkInterface,
    TunnelLSFSocketStructure socketStructure,
    String connectionID)
  {
    super(messageForwarder, ipTunnelNetworkInterface, socketStructure, connectionID);
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

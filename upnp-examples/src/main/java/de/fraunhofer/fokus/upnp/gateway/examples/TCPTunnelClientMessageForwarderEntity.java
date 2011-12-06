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
package de.fraunhofer.fokus.upnp.gateway.examples;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.GatewayStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.network_interfaces.InetAddressManagement;
import de.fraunhofer.fokus.upnp.gateway.tcp_tunnel.TCPTunnelClientManagement;

/**
 * This class connects to a TCP tunnel server. It should use a different UPnP multicast domain to prevent multicast
 * loops.
 */
public class TCPTunnelClientMessageForwarderEntity extends TemplateEntity
{

  private MessageForwarder          messageForwarder;

  private InetAddressManagement     inetAddressManagement;

  private TCPTunnelClientManagement tcpClientTunnelManagement;

  /**
   * Creates a new instance of TCPTunnelClientMessageForwarderEntity.
   * 
   * StartupProperties: TCPTunnelServerAddress TCPTunnelServerPort TCPTunnelInetAddress NetworkInterfaceMulticastAddress
   * NetworkInterfaceMulticastPort
   * 
   */
  public TCPTunnelClientMessageForwarderEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    startupConfiguration = getStartupConfiguration();
    GatewayStartupConfiguration gatewayStartupConfiguration =
      (GatewayStartupConfiguration)startupConfiguration.getSingleGatewayStartupConfiguration();

    messageForwarder = new MessageForwarder(startupConfiguration);
    String tunnelServerAddress = gatewayStartupConfiguration.getProperty("TCPTunnelServerAddress", "adelphi");
    int tunnelServerPort = gatewayStartupConfiguration.getNumericProperty("TCPTunnelServerPort", 10000);
    String tunnelInetAddress = gatewayStartupConfiguration.getProperty("TCPTunnelInetAddress", "192.168.200.200");
    try
    {
      inetAddressManagement = new InetAddressManagement(messageForwarder);

      tcpClientTunnelManagement =
        new TCPTunnelClientManagement(messageForwarder,
          new InetSocketAddress(tunnelServerAddress, tunnelServerPort),
          InetAddress.getByName(tunnelInetAddress));
    } catch (Exception e)
    {
    }
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new TCPTunnelClientMessageForwarderEntity(args.length > 0 ? new UPnPStartupConfiguration(args[0]) : null);
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    inetAddressManagement.terminate();
    tcpClientTunnelManagement.terminate();
    messageForwarder.terminate();

    super.terminate();
  }

}

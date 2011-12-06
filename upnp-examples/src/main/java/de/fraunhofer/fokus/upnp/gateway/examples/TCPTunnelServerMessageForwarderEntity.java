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

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.GatewayStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.network_interfaces.InetAddressManagement;
import de.fraunhofer.fokus.upnp.gateway.tcp_tunnel.TCPTunnelServerManagement;

/**
 * This class works as server for clients that want to connect via a TCP tunnel.
 */
public class TCPTunnelServerMessageForwarderEntity extends TemplateEntity
{

  private MessageForwarder          messageForwarder;

  private InetAddressManagement     inetAddressManagement;

  private TCPTunnelServerManagement tcpServerTunnelManagement;

  /**
   * Creates a new instance of TCPTunnelServerMessageForwarderEntity.
   * 
   * StartupProperties: TCPTunnelServerPort TCPTunnelInetAddress
   */
  public TCPTunnelServerMessageForwarderEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    startupConfiguration = getStartupConfiguration();

    messageForwarder = new MessageForwarder(startupConfiguration);
    GatewayStartupConfiguration gatewayStartupConfiguration =
      messageForwarder.getGatewayStartupConfiguration("TCPTunnelServerManagement");

    int tunnelServerPort = gatewayStartupConfiguration.getNumericProperty("TCPTunnelServerPort", 10000);
    String tunnelInetAddress = gatewayStartupConfiguration.getProperty("TCPTunnelInetAddress", "192.168.200.200");
    try
    {
      inetAddressManagement = new InetAddressManagement(messageForwarder);

      tcpServerTunnelManagement =
        new TCPTunnelServerManagement(messageForwarder, tunnelServerPort, InetAddress.getByName(tunnelInetAddress));
    } catch (Exception e)
    {
      System.out.println("Could not start entity: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new TCPTunnelServerMessageForwarderEntity(args.length > 0 ? new UPnPStartupConfiguration(args[0]) : null);
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    inetAddressManagement.terminate();
    tcpServerTunnelManagement.terminate();
    messageForwarder.terminate();

    super.terminate();
  }

}

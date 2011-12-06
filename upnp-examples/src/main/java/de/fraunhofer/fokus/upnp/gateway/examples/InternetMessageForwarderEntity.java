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
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.gateway.network_interfaces.InetAddressManagement;

/**
 * This class works as UPnP message forwarder between the Internet and all local network interfaces.
 * 
 * @author alk Alexander Koenig
 * 
 */
public class InternetMessageForwarderEntity extends TemplateEntity
{

  private MessageForwarder      messageForwarder;

  private InetAddressManagement inetAddressManagement;

  private InternetManagement    internetManagement;

  /**
   * Creates a new instance of InternetMessageForwarderEntity.
   * 
   * StartupProperties: GlobalIPAddress NetworkInterfaceMulticastAddress
   * NetworkInterfaceMulticastPort
   * 
   */
  public InternetMessageForwarderEntity()
  {
    super();
    // start message forwarder
    messageForwarder = new MessageForwarder(getStartupConfiguration());

    // add interface for local network interfaces
    try
    {
      inetAddressManagement = new InetAddressManagement(messageForwarder);
    } catch (Exception e)
    {
      System.out.println("Could not start local network interface management: " + e.getMessage());
      System.exit(1);
    }

    // add interface for Internet
    GatewayStartupConfiguration gatewayStartupConfiguration =
      messageForwarder.getGatewayStartupConfiguration("InternetManagement");

    if (gatewayStartupConfiguration == null)
    {
      System.out.println("Missing gateway startup info for Internet.");
      System.exit(1);
    }
    String globalIPAddress = gatewayStartupConfiguration.getProperty("GlobalIPAddress");
    if (globalIPAddress == null)
    {
      System.out.println("Missing global IP address.");
      System.exit(1);
    }
    // load ports from startup configuration
    int ssdpDeviceReceptionPort =
      gatewayStartupConfiguration.getNumericProperty("SSDPDeviceReceptionPort",
        InternetManagementConstants.SSDP_DEVICE_PORT);
    int ssdpDeviceMSearchPort =
      gatewayStartupConfiguration.getNumericProperty("SSDPDeviceMSearchPort",
        InternetManagementConstants.SSDP_DEVICE_M_SEARCH_SEND_PORT);
    int gatewayServerPort =
      gatewayStartupConfiguration.getNumericProperty("GatewayServerPort",
        InternetManagementConstants.HTTP_DEVICE_REQUEST_PORT);
    try
    {
      internetManagement =
        new InternetManagement(messageForwarder,
          InetAddress.getByName(globalIPAddress),
          ssdpDeviceReceptionPort,
          ssdpDeviceMSearchPort,
          gatewayServerPort);
    } catch (Exception e)
    {
      System.out.println("Could not start Internet management: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new InternetMessageForwarderEntity();
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    inetAddressManagement.terminate();
    internetManagement.terminate();
    messageForwarder.terminate();

    super.terminate();
  }

}

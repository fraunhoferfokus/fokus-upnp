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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.net.DatagramSocket;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class handles the discovery of peer DeviceDirectoryDevices.
 * 
 * @author Alexander Koenig
 */
public class DDPeerDiscovery
{

  public static Logger          logger = Logger.getLogger("upnp");

  /** Reference to associated DeviceDirectoryDevice */
  private DeviceDirectoryDevice deviceDirectoryDevice;

  /**
   * Creates a new instance of DiscoveryService.
   * 
   * @param deviceDirectoryDevice
   *          The associated DeviceDirectoryDevice
   * 
   */
  public DDPeerDiscovery(DeviceDirectoryDevice deviceDirectoryDevice)
  {
    System.out.println("  Created DiscoveryService on port " +
      InternetManagementConstants.SSDP_DEVICE_DIRECTORY_DEVICE_M_SEARCH_PORT);

    this.deviceDirectoryDevice = deviceDirectoryDevice;
  }

  /**
   * Sends a M-SEARCH message.
   * 
   * @param message
   *          The M-SEARCH message object
   */
  public void sendSearchMessage(BinaryMessageObject message)
  {
    System.out.println("Send M-SEARCH request to " + IPHelper.toString(message.getDestinationAddress()));

    DatagramSocket socket =
      deviceDirectoryDevice.getSocketStructure(IPHelper.getLocalHostAddress()).getSSDPUnicastSocket();

    // send message packet
    for (int j = 0; j < UPnPDefaults.UDP_SEND_COUNT; j++)
    {
      SocketHelper.sendBinaryMessage(message, socket);
    }
  }
}

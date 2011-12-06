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
package de.fraunhofer.fokus.upnp.core_security.device;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.DeviceHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.device.DeviceNotifyAdvertiser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.ssdp.NotifyMessageBuilder;

/**
 * This class is responsible for the advertisement of a secured device.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredDeviceNotifyAdvertiser extends DeviceNotifyAdvertiser
{

  /** Reference to secured device. */
  private SecuredTemplateDevice securedDevice;

  /**
   * Creates a new instance of SecuredDeviceNotifyAdvertiser.
   * 
   * @param device
   */
  public SecuredDeviceNotifyAdvertiser(SecuredTemplateDevice device)
  {
    super(device);
    this.securedDevice = device;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceNotifyAdvertiser#sendNotifyMessagesToSocketStructure(java.lang.String,
   *      de.fhg.fokus.magic.upnp.device.DeviceHostAddressSocketStructure)
   */
  public void sendNotifyMessagesToSocketStructure(String messageType, DeviceHostAddressSocketStructure socketStructure)
  {
    Vector messages = new Vector();
    if (securedDevice.isAnonymousDiscovery())
    {
      messages =
        NotifyMessageBuilder.createRootDeviceMessage(device,
          socketStructure.getHTTPServerAddress(),
          messageType,
          device.getIPVersion());
    } else
    {
      messages =
        NotifyMessageBuilder.createAllMessages(device,
          socketStructure.getHTTPServerAddress(),
          messageType,
          device.getIPVersion());
    }
    // System.out.println("Created " + messages.size() + " messages for " + messageType);
    // send all messages to the current socket
    for (int i = 0; i < messages.size(); i++)
    {
      String currentMessage = (String)messages.elementAt(i);
      logger.info("sending " + (i + 1) + " NOTIFY message.");

      sendNotifyMessageToMulticastSocket(socketStructure.getSSDPMulticastSocket(),
        device.getSSDPMulticastSocketAddress(),
        currentMessage);
    }
  }

}

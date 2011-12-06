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
package de.fraunhofer.fokus.upnp.gateway.internet;

import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device.DDDeviceSubscribeModifier;
import de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device.DeviceDirectoryEntity;

/**
 * This class adds an Internet IForwarderModule to the MessageForwarder. This implicitly starts a
 * deviceDirectoryDevice entity, that is used for handling peer devices.
 * 
 * @author Alexander Koenig
 */
public class InternetManagement
{
  /** Reference to outer message forwarder */
  private MessageForwarder                   messageForwarder;

  /** Message processor for forwarded SUBSCRIBE messages */
  private DDDeviceSubscribeModifier          subscribeModifier;

  /** Socket structure that bundles all sockets to the Internet */
  private InternetHostAddressSocketStructure internetHostAddressSocketStructure;

  /** Device directory device that handles peer devices */
  private DeviceDirectoryEntity              deviceDirectoryEntity;

  /** Forwarder module to the Internet */
  private InternetForwarderModule            internetForwarderModule;

  /** Reference to global IP address */
  private InetAddress                        globalIPAddress;

  /**
   * Creates a new instance of InternetManagement and adds it to the message forwarder.
   * 
   * @param messageForwarder
   *          Reference to the message forwarder
   * @param globalIPAddress
   *          Address that is reachable from the Internet
   * @param ssdpDeviceReceptionPort
   *          Port to receive NOTIFY and M-SEARCH from the Internet
   * @param ssdpDeviceMSearchPort
   *          Port to send M-SEARCH to the Internet
   * @param gatewayServerPort
   *          Port to receive HTTP from the Internet
   * 
   */
  public InternetManagement(MessageForwarder messageForwarder,
    InetAddress globalIPAddress,
    int ssdpDeviceReceptionPort,
    int ssdpDeviceMSearchPort,
    int gatewayServerPort)
  {
    this.messageForwarder = messageForwarder;
    this.globalIPAddress = globalIPAddress;

    subscribeModifier = new DDDeviceSubscribeModifier(globalIPAddress.getHostName());

    // link SUBSCRIBE modifier to control point
    messageForwarder.getTemplateControlPoint().getBasicControlPoint().setSubscribeModifier(subscribeModifier);

    // sockets must be created first
    try
    {
      System.out.println();
      System.out.println("Create InternetHostAddressSocketStructure for global IP address " +
        globalIPAddress.getHostName() + "(" + globalIPAddress.getHostAddress() + ")");

      internetHostAddressSocketStructure =
        new InternetHostAddressSocketStructure(messageForwarder.getGatewayMessageManager(),
          globalIPAddress,
          ssdpDeviceReceptionPort,
          ssdpDeviceMSearchPort,
          gatewayServerPort);
    } catch (Exception e)
    {
    }
    internetForwarderModule = new InternetForwarderModule(messageForwarder, this);

    UPnPStartupConfiguration startupConfiguration = messageForwarder.getStartupConfiguration();
    // prevent keyboard thread for device entity
    startupConfiguration.setStartKeyboardThread(false);
    deviceDirectoryEntity = new DeviceDirectoryEntity(this, startupConfiguration);
    // add Internet forwarder module after device creation
    messageForwarder.addForwarderModule(internetForwarderModule);
  }

  /** Retrieves the DeviceDirectoryEntity */
  public DeviceDirectoryEntity getDeviceDirectoryEntity()
  {
    return deviceDirectoryEntity;
  }

  /**
   * Retrieves the InternetHostAddressSocketStructure.
   * 
   * @return The InternetHostAddressSocketStructure.
   */
  public InternetHostAddressSocketStructure getInternetHostAddressSocketStructure()
  {
    return internetHostAddressSocketStructure;
  }

  /**
   * Retrieves the messageForwarder.
   * 
   * @return The messageForwarder.
   */
  public MessageForwarder getMessageForwarder()
  {
    return messageForwarder;
  }

  /**
   * Retrieves the control point of the message forwarder.
   * 
   * @return The control point of the message forwarder
   */
  public TemplateControlPoint getMessageForwarderControlPoint()
  {
    return messageForwarder.getTemplateControlPoint();
  }

  /**
   * Retrieves the internetForwarderModule.
   * 
   * @return The internetForwarderModule.
   */
  public InternetForwarderModule getInternetForwarderModule()
  {
    return internetForwarderModule;
  }

  /**
   * Retrieves the globalIPAddress.
   * 
   * @return The globalIPAddress.
   */
  public InetAddress getGlobalIPAddress()
  {
    return globalIPAddress;
  }

  /**
   * Retrieves the subscribeModifier.
   * 
   * @return The subscribeModifier
   */
  public DDDeviceSubscribeModifier getSubscribeModifier()
  {
    return subscribeModifier;
  }

  /** Terminates the Internet management */
  public void terminate()
  {
    deviceDirectoryEntity.terminate();

    internetForwarderModule.terminate();
    // remove from message forwarder
    messageForwarder.removeForwarderModule(internetForwarderModule);

    // sockets must be closed last
    internetHostAddressSocketStructure.terminate();
  }

}

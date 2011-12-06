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
package de.fraunhofer.fokus.upnp.gateway.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.ssdp.NotifyMessageBuilder;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLExtension;
import de.fraunhofer.fokus.upnp.util.URLReplacement;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class helps to forwards NOTIFY messages between different network interfaces.
 * 
 * @author Alexander Koenig
 */
public class SSDPNotifyHelper
{

  /**
   * Changes the forwarder module that receives device description requests for this NOTIFY message.
   * 
   * @param message
   *          NOTIFY message that should be forwarded
   * @param forwarderSocketAddress
   *          The HTTP server socket address that should receive the resulting GET message
   * 
   */
  public static String changeNotifyLocation(String message, InetSocketAddress forwarderSocketAddress)
  {
    String currentLocation = HTTPMessageHelper.getHeaderLine(message, CommonConstants.LOCATION);
    if (currentLocation != null)
    {
      // replace location in message
      message =
        HTTPMessageHelper.replaceHeaderLine(message,
          CommonConstants.LOCATION,
          URLReplacement.replaceURLHost(currentLocation, IPHelper.toString(forwarderSocketAddress)));

      if (!message.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
      {
        message += CommonConstants.NEW_LINE;
      }
    }
    return message;
  }

  /**
   * Adds a forwarder module that receives device description requests for this NOTIFY message.
   * 
   * @param message
   *          NOTIFY message that should be forwarded
   * @param localForwarderModuleInterfaceID
   *          The ID of the interface that should forward the resulting GET message
   * @param forwarderInterfaceAddress
   *          The interface that should receive the resulting GET message
   * @param forwarderInterfacePort
   *          The port that should receive the resulting GET message *
   * 
   */
  public static String extendNotifyLocation(String message,
    String localForwarderModuleInterfaceID,
    InetSocketAddress forwarderSocketAddress)
  {
    // the location URL in the NOTIFY message is probably only reachable in the
    // original network, so we replace it with the IP address of the HTTP server of the gateway
    String result =
      URLExtension.extendLocation(message, localForwarderModuleInterfaceID, forwarderSocketAddress.getAddress()
        .getHostAddress(), forwarderSocketAddress.getPort());

    if (!result.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
    {
      result += CommonConstants.NEW_LINE;
    }

    return result;
  }

  /**
   * Allows the reception of GET requests over a new gateway HTTP server.
   * 
   * @param message
   *          NOTIFY message that should be forwarded
   * @param outgoingInterfaceID
   *          The ID of the interface that should forward the resulting GET message
   * @param forwarderInterfaceAddress
   *          The interface that should receive the resulting GET message
   * @param forwarderInterfacePort
   *          The port that should receive the resulting GET message *
   * 
   */
  public static String extendNotifyForForwarding(String message,
    String outgoingInterfaceID,
    String forwarderInterfaceAddress,
    int forwarderInterfacePort)
  {
    // the location URL in the NOTIFY message is probably only reachable in the
    // original network, so we replace it with the IP address of the HTTP server of the gateway
    String result =
      URLExtension.extendLocation(message, outgoingInterfaceID, forwarderInterfaceAddress, forwarderInterfacePort);

    if (!result.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
    {
      result += CommonConstants.NEW_LINE;
    }

    return result;
  }

  /**
   * Sends a NOTIFY message.
   * 
   * @param message
   *          NOTIFY message
   * @param socket
   *          Socket that should send the message
   * @param targetAddress
   *          The target address for the message
   * 
   */
  public static void sendNotify(String message, IDatagramSocket socket, InetSocketAddress targetAddress)
  {
    byte[] messageData = StringHelper.stringToByteArray(message);
    DatagramPacket packet =
      new DatagramPacket(messageData, messageData.length, targetAddress.getAddress(), targetAddress.getPort());

    try
    {
      for (int k = 0; k < UPnPDefaults.UDP_SEND_COUNT; k++)
      {
        socket.send(packet);
        Thread.sleep(UPnPDefaults.DEVICE_NOTIFY_MESSAGE_DELAY);
      }
    } catch (Exception ex)
    {
      System.out.println("Error while forwarding multicast packet:" + ex.getMessage());
    }
  }

  /**
   * Sends a NOTIFY message with a faked source address.
   * 
   * @param message
   *          NOTIFY message
   * @param socket
   *          Socket that should send the message
   * @param sourceAddress
   *          The faked source address for the message
   * @param targetAddress
   *          The target address for the message
   * 
   */
  public static void sendNotify(String message,
    IDatagramSocket socket,
    InetSocketAddress sourceAddress,
    InetSocketAddress targetAddress)
  {
    byte[] messageData = StringHelper.stringToByteArray(message);
    DatagramPacket packet =
      new DatagramPacket(messageData, messageData.length, targetAddress.getAddress(), targetAddress.getPort());

    try
    {
      for (int k = 0; k < UPnPDefaults.UDP_SEND_COUNT; k++)
      {
        socket.send(packet, sourceAddress);
        Thread.sleep(UPnPDefaults.DEVICE_NOTIFY_MESSAGE_DELAY);
      }
    } catch (Exception ex)
    {
      System.out.println("Error while forwarding multicast packet:" + ex.getMessage());
    }
  }

  /**
   * Creates a vector with all NOTIFY messages for a certain device that can be forwarded over an
   * forwarder module.
   * 
   * @param device
   *          The UPnP device
   * @param outgoingInterfaceID
   *          The ID of the module that should forward the resulting GET messages
   * @param forwarderInterfaceAddress
   *          The address that should receive the resulting GET messages
   * @return
   */
  public static Vector createNotifyMessagesForForwarding(AbstractDevice device,
    String outgoingInterfaceID,
    String forwarderInterfaceAddress,
    int forwarderInterfacePort)
  {
    // create NOTIFY messages
    Vector result =
      NotifyMessageBuilder.createAllMessages(device,
        device.getDeviceDescriptionSocketAddress(),
        SSDPConstant.SSDP_ALIVE,
        device.getIPVersion());

    if (result != null && result.size() > 0)
    {
      // change messages to allow proper forwarding of resulting GET message
      for (int j = 0; j < result.size(); j++)
      {
        String message = (String)result.elementAt(j);

        System.out.println("Generated message is " + HTTPMessageHelper.getHeaderDescription(message));

        message =
          SSDPNotifyHelper.extendNotifyForForwarding(message,
            outgoingInterfaceID,
            forwarderInterfaceAddress,
            forwarderInterfacePort);

        System.out.println("Modified message is " + HTTPMessageHelper.getHeaderDescription(message));

        // replace old with modified message
        result.remove(j);
        result.insertElementAt(message, j);
      }
    }
    return result;
  }

  /**
   * Creates a vector with all NOTIFY messages for a certain device that can be forwarded over an
   * forwarder module.
   * 
   * @param device
   *          The UPnP device
   * @param outgoingInterfaceID
   *          The ID of the module that should forward the resulting GET messages
   * @param forwarderInterfaceAddress
   *          The address that should receive the resulting GET messages
   * @return
   */
  public static Vector createForwarderModuleNotifyMessagesForRemoteDevice(CPDevice device,
    IForwarderModule forwarderModule)
  {
    // create NOTIFY messages
    Vector result =
      NotifyMessageBuilder.createAllMessages(device,
        device.getDeviceDescriptionSocketAddress(),
        SSDPConstant.SSDP_ALIVE,
        device.getIPVersion());

    // check if device is announced over the inetAddressForwarderModule that discovered the device
    if (device.getDeviceDescriptionSocketAddress().equals(forwarderModule.getHTTPServerSocketAddress()))
    {
      System.out.println("Device " + device.toDiscoveryString() + " is announced over discovery forwarder module");
      return result;
    }
    if (result != null && result.size() > 0)
    {
      // change messages to allow proper forwarding of resulting GET message
      for (int j = 0; j < result.size(); j++)
      {
        String message = (String)result.elementAt(j);

        System.out.println("Generated message is " + HTTPMessageHelper.getHeaderDescription(message));

        message = SSDPNotifyHelper.changeNotifyLocation(message, forwarderModule.getHTTPServerSocketAddress());

        System.out.println("Modified message is " + HTTPMessageHelper.getHeaderDescription(message));

        // replace old with modified message
        result.remove(j);
        result.insertElementAt(message, j);
      }
    }
    return result;
  }

  /**
   * Creates a vector with all NOTIFY messages for a local device that can be forwarded over an
   * forwarder module.
   * 
   * @param device
   *          The UPnP device
   * @param localForwarderModuleInterfaceID
   *          The ID of the module that should forward the resulting GET messages
   * @param forwarderInterfaceAddress
   *          The address that should receive the resulting GET messages
   * @return
   */
  public static Vector createForwarderModuleNotifyMessagesForLocalDevice(CPDevice device,
    String localForwarderModuleInterfaceID,
    IForwarderModule forwarderModule)
  {
    // create NOTIFY messages
    Vector result =
      NotifyMessageBuilder.createAllMessages(device,
        device.getDeviceDescriptionSocketAddress(),
        SSDPConstant.SSDP_ALIVE,
        device.getIPVersion());

    // change messages to allow proper forwarding of resulting GET message
    for (int j = 0; result != null && j < result.size(); j++)
    {
      String message = (String)result.elementAt(j);

      System.out.println("Generated message is " + HTTPMessageHelper.getHeaderDescription(message));

      message =
        extendNotifyLocation(message, localForwarderModuleInterfaceID, forwarderModule.getHTTPServerSocketAddress());

      System.out.println("Modified message is " + HTTPMessageHelper.getHeaderDescription(message));

      // replace old with modified message
      result.remove(j);
      result.insertElementAt(message, j);
    }
    return result;
  }

  /**
   * Creates a vector with all NOTIFY bye bye messages for a certain device that can be forwarded
   * over an forwarder module.
   * 
   * @param device
   *          The UPnP device
   * 
   * @return A vector with all byebye messages
   */
  public static Vector createNotifyByeByeMessages(AbstractDevice device)
  {
    // create NOTIFY messages
    Vector result =
      NotifyMessageBuilder.createAllMessages(device,
        device.getDeviceDescriptionSocketAddress(),
        SSDPConstant.SSDP_BYEBYE,
        device.getIPVersion());

    return result;
  }

}

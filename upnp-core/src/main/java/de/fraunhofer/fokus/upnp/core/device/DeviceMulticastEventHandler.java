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
package de.fraunhofer.fokus.upnp.core.device;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * Devices use this class to receive initial event value requests and to send responses.
 * 
 * @author Alexander Koenig
 * 
 */
public class DeviceMulticastEventHandler implements IEventListener
{

  // do not process the same received message within one second
  public static long       RECEIVED_MESSAGE_TIMEOUT = 1000;

  /** SSDPServer logger */
  static Logger            logger                   = Logger.getLogger("upnp.gena");

  /** Manager for received packets */
  private UDPPacketManager udpPacketManager;

  /** Associated device */
  private Device           device;

  /**
   * Creates a new instance of DeviceMulticastEventHandler
   * 
   * @param device
   *          The associated device
   */
  public DeviceMulticastEventHandler(Device device)
  {
    this.device = device;
    // start packet manager for event requests
    udpPacketManager =
      new UDPPacketManager("DeviceMulticastEventHandler [" + device.toString() + "]", RECEIVED_MESSAGE_TIMEOUT);

    device.getDeviceEventThread().register(this);
  }

  /**
   * Returns the service for an URL that contains the serviceID as URL path
   * 
   * @param parameterURL
   *          URL to be checked
   * 
   * @return the service for the URL or null
   */
  public static DeviceService getDeviceServiceByPathURL(URL parameterURL, Device device)
  {
    // tokenize path
    String deviceUDN = null;
    String shortenedServiceID = null;

    StringTokenizer pathTokenizer = new StringTokenizer(parameterURL.getPath(), "/");
    try
    {
      deviceUDN = URLHelper.escapedURLToString(pathTokenizer.nextToken());
      shortenedServiceID = URLHelper.escapedURLToString(pathTokenizer.nextToken());
    } catch (Exception e)
    {
      return null;
    }
    // wrong UDN
    if (!device.getUDN().equals(deviceUDN))
    {
      return null;
    }
    return getDeviceServiceByShortenedServiceID(shortenedServiceID, device);
  }

  /**
   * Returns the service for an URL that contains the serviceID as URL path
   * 
   * @param parameterURL
   *          URL to be checked
   * 
   * @return the service for the URL or null
   */
  public static DeviceService getDeviceServiceByShortenedServiceID(String shortenedServiceID, Device device)
  {
    DeviceService result = device.getDeviceServiceByShortenedID(shortenedServiceID);
    if (result != null)
    {
      return result;
    }
    // check embedded device services
    if (device.getEmbeddedDeviceTable() != null)
    {
      Device[] embeddedDevices = device.getEmbeddedDeviceTable();
      for (int i = 0; i < embeddedDevices.length; i++)
      {
        DeviceService embeddedService = getDeviceServiceByShortenedServiceID(shortenedServiceID, embeddedDevices[i]);
        if (embeddedService != null)
        {
          return embeddedService;
        }
      }
    }
    return null;
  }

  /** Processes received multicast requests for the current state variable values of a service. */
  public void processReceivedMessage(DeviceHostAddressSocketStructure socketStructure, BinaryMessageObject messageObject)
  {
    String content = messageObject.getBodyAsString();
    // only process INITIAL_EVENT request messages
    if (content.startsWith(CommonConstants.INITIAL_EVENT))
    {
      HTTPParser httpParser = new HTTPParser();
      httpParser.parse(content);
      // address that sent the request
      InetSocketAddress sourceAddress = messageObject.getSourceAddress();
      // retrieve URL from request
      URL parameterURL = httpParser.getRequestURL();

      // no URL found, ignore message
      if (parameterURL == null)
      {
        System.out.println("Ignore invalid INITIAL_EVENT message from " + IPHelper.toString(sourceAddress));
        return;
      }

      // check if service is present
      DeviceService service = getDeviceServiceByPathURL(parameterURL, device);

      // if the service is not found, this initial event request is probably for another device
      // that uses the same multicast address
      // we simply ignore the message
      if (service == null)
      {
        logger.info("service for " + parameterURL + " not found");
        System.out.println("Ignore INITIAL_EVENT message from " + IPHelper.toString(sourceAddress) + ": " +
          parameterURL.getPath() + " not found");
        return;
      }
      System.out.println("Received initial event request for service " + service.getShortenedServiceId());

      // get current event key for multicast events
      long currentEventKey = service.getMulticastEventKey();

      // build vectors with current event names and values
      Vector eventObjects = new Vector();

      service.fillInitialEventObjectList(eventObjects, null);

      // build message body
      byte[] messageBody = StringHelper.utf8StringToByteArray(GENAMessageBuilder.buildNotifyBody(eventObjects));

      String path = service.getMulticastEventDeliveryURL().getFile();
      String messageHeader =
        GENAMessageBuilder.buildNotify(path,
          sourceAddress.getHostName(),
          sourceAddress.getPort(),
          null,
          currentEventKey + "",
          messageBody.length + "");

      // send Initial-Event response to source address of request
      HTTPMessageObject notifyMessage =
        new HTTPMessageObject(messageHeader,
          messageBody,
          socketStructure.getMulticastEventSocketAddress(),
          sourceAddress);

      BinaryMessageObject responseMessage = notifyMessage.toBinaryMessage();

      // System.out.println("Send initial event response: [\n" +
      // responseMessage.getBodyAsUTF8String() +
      // "]");

      SocketHelper.sendBinaryMessage(responseMessage, socketStructure.getMulticastEventSocket());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    // check all associated multicast sockets for received packets
    Vector socketStructures = device.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      DeviceHostAddressSocketStructure socketStructure =
        (DeviceHostAddressSocketStructure)socketStructures.elementAt(i);

      // stay to one socket until all pending packets were read
      boolean packetFound = true;
      while (packetFound)
      {
        BinaryMessageObject message =
          SocketHelper.readBinaryMessage(udpPacketManager,
            socketStructure.getMulticastEventSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (message != null)
        {
          // set destination address
          message.setDestinationAddress((InetSocketAddress)socketStructure.getMulticastEventSocket()
            .getLocalSocketAddress());
          processReceivedMessage(socketStructure, message);
        } else
        {
          packetFound = false;
        }
      }
    }
    udpPacketManager.triggerEvents();
  }

  /** Close the multicast event handler. */
  public void terminate()
  {
    device.getDeviceEventThread().unregister(this);
  }

}

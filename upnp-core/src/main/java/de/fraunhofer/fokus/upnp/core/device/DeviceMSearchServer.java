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
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.ssdp.ISSDPMessageModifier;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageProcessorResult;
import de.fraunhofer.fokus.upnp.ssdp.MSearchResponseMessage;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * Devices use this class to receive M-SEARCH messages from control points.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class DeviceMSearchServer implements IEventListener
{

  // do not process the same received message within one second
  public static long                    RECEIVED_MESSAGE_TIMEOUT = 1000;

  /** SSDPServer logger */
  static Logger                         logger                   = Logger.getLogger("upnp.ssdp");

  /** Manager for received packets */
  private UDPPacketManager              udpPacketManager;

  /** Optional message modifier. */
  private ISSDPMessageModifier          ssdpMessageModifier;

  /** Message processor for M-SEARCH messages. */
  private DeviceMSearchMessageProcessor deviceMSearchMessageProcessor;

  /**
   * List of pending response messages. This is done to decouple creating and sending of response
   * messages.
   */
  private Vector                        responseMessages         = new Vector();

  /** Associated device */
  private Device                        device;

  /**
   * Constructor for SSDPServer class which starts the SSDP Server
   * 
   * @param device
   *          Associated device
   * @param messageProcessor
   *          Processor for received M-SEARCH messages
   * 
   */
  public DeviceMSearchServer(Device device, DeviceMSearchMessageProcessor messageProcessor)
  {
    // super("DeviceMSearchServer [" + device.toString() + "]");
    this.device = device;
    // start packet manager for M-SEARCH
    udpPacketManager =
      new UDPPacketManager("DeviceMSearchServer [" + device.toString() + "]", RECEIVED_MESSAGE_TIMEOUT);

    this.deviceMSearchMessageProcessor = messageProcessor;

    device.getDeviceEventThread().register(this);
  }

  public void processReceivedMessage(DeviceHostAddressSocketStructure socketStructure, BinaryMessageObject messageObject)
  {
    String messageString = messageObject.getBodyAsString();
    // only process M-SEARCH messages
    if (messageString.startsWith(CommonConstants.M_SEARCH))
    {
      // TemplateDevice.printMessage(DateTimeHelper.formatCurrentDateForDebug() + ": Received
      // M-SEARCH");

      // modify request if needed
      if (ssdpMessageModifier != null)
      {
        ssdpMessageModifier.modifyMSEARCHRequest(messageObject);
        messageString = messageObject.getBodyAsString();
      }
      // response packets differ for different network interfaces
      MSearchMessageProcessorResult result =
        deviceMSearchMessageProcessor.processMessage(socketStructure.getHTTPServerAddress(), messageString);

      if (result == null)
      {
        return;
      }
      // create list of packets that has to be sent
      for (int i = 0; i < result.getMessageCount(); i++)
      {
        String response = result.getResponseMessage(i);
        byte[] responseData = StringHelper.stringToByteArray(response);

        BinaryMessageObject responseMessageObject =
          new BinaryMessageObject(responseData, new InetSocketAddress(socketStructure.getHostAddress(),
            socketStructure.getSSDPUnicastSocket().getLocalPort()), messageObject.getSourceAddress());

        // try to modify response
        if (ssdpMessageModifier != null)
        {
          ssdpMessageModifier.modifyMSEARCHResponse(responseMessageObject);
        }

        // store response and metadata in response message vector
        MSearchResponseMessage responseMessage =
          new MSearchResponseMessage(socketStructure.getSSDPUnicastSocket(), responseMessageObject, result.getMXValue());

        // add packets to send list
        responseMessages.add(responseMessage);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    readPendingMessages();
    sendPendingResponseMessages();
    udpPacketManager.triggerEvents();
  }

  /**
   * Retrieves the ssdpMessageModifier.
   * 
   * @return The ssdpMessageModifier
   */
  public ISSDPMessageModifier getSSDPMessageModifier()
  {
    return ssdpMessageModifier;
  }

  /**
   * Sets the ssdpMessageModifier.
   * 
   * @param ssdpMessageModifier
   *          The new value for ssdpMessageModifier
   */
  public void setSSDPMessageModifier(ISSDPMessageModifier ssdpMessageModifier)
  {
    this.ssdpMessageModifier = ssdpMessageModifier;
  }

  /** Close the M-SEARCH server. */
  public void terminate()
  {
    device.getDeviceEventThread().unregister(this);
  }

  /** Reads multicast messages from all network interfaces */
  private void readPendingMessages()
  {
    // check all associated multicast sockets for received packets
    Vector socketStructures = device.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      DeviceHostAddressSocketStructure socketStructure =
        (DeviceHostAddressSocketStructure)socketStructures.elementAt(i);

      // stay to one socket until all pending packets were read
      boolean packetFound = true;
      int packetCount = 0;
      while (packetFound)
      {
        BinaryMessageObject message =
          SocketHelper.readBinaryMessage(udpPacketManager,
            socketStructure.getSSDPMulticastSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (message != null)
        {
          packetCount++;
          // set destination address
          message.setDestinationAddress(socketStructure.getSSDPMulticastSocketAddress());
          processReceivedMessage(socketStructure, message);
        } else
        {
          packetFound = false;
        }
      }
    }
  }

  /** Sends generated response messages for received M-SEARCH requests */
  private void sendPendingResponseMessages()
  {
    try
    {
      int i = 0;
      // run through all messages
      while (i < responseMessages.size())
      {
        MSearchResponseMessage message = (MSearchResponseMessage)responseMessages.elementAt(i);
        // check if message is in time for sending
        if (message.getResponseTime() < System.currentTimeMillis())
        {
          logger.info("Send M-SEARCH response message to " +
            IPHelper.toString(message.getResponsePacket().getDestinationAddress()));

          if (message.getSendCount() < UPnPDefaults.UDP_SEND_COUNT)
          {
            message.incSendCount();
            SocketHelper.sendBinaryMessage(message.getResponsePacket(), message.getResponseSocket());
          } else
          {
            responseMessages.remove(i);
          }
        } else
        {
          i++;
        }
      }
    } catch (Exception e)
    {
    }
  }

}

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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is used by control points to send M-SEARCH messages to an address and to parse the
 * unicast responses.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class CPMSearchClient implements IEventListener
{

  static Logger                  logger                   = Logger.getLogger("upnp.ssdp");

  /**
   * processor for processing response message for M-SEARCH message
   */
  private CPSSDPMessageProcessor mSearchResponseProcessor = null;

  private ControlPoint           controlPoint;

  /**
   * Constructor which starts the SSDP client.
   * 
   * @param controlPoint
   *          The associated control point
   * @param processor
   *          The response parser processor
   */
  public CPMSearchClient(ControlPoint controlPoint, CPSSDPMessageProcessor processor)
  {
    this.controlPoint = controlPoint;
    this.mSearchResponseProcessor = processor;
  }

  /**
   * Sends a M-SEARCH message to the UPnP multicast address.
   * 
   * @param message
   *          The M-SEARCH message
   */
  public void sendMessageToMulticast(String message)
  {
    sendMessage(message, controlPoint.getSSDPMulticastSocketAddress());
  }

  /**
   * Sends a M-SEARCH message to the UPnP multicast address on a certain socket structure.
   * 
   * @param message
   *          The M-SEARCH message
   */
  public void sendMessageToSocketStructureMulticast(String message,
    ControlPointHostAddressSocketStructure socketStructure)
  {
    sendMessageToSocketStructure(message, socketStructure, controlPoint.getSSDPMulticastSocketAddress());
  }

  /**
   * Sends a M-SEARCH message to a certain address.
   * 
   * @param message
   *          The M-SEARCH message
   * @param targetAddress
   *          The target address
   */
  public void sendMessage(String message, InetSocketAddress targetAddress)
  {
    // send to all associated M-SEARCH sockets
    Vector socketStructures = controlPoint.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure socketStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      sendMessageToSocketStructure(message, socketStructure, targetAddress);
    }
  }

  /**
   * Sends a M-SEARCH message to a certain address via a certain socket structure.
   * 
   * @param message
   *          The M-SEARCH message
   * @param address
   *          The target address
   * @param port
   *          The target port
   */
  public void sendMessageToSocketStructure(String message,
    ControlPointHostAddressSocketStructure socketStructure,
    InetSocketAddress targetAddress)
  {
    try
    {
      // create message packet
      byte[] msgBytes = StringHelper.stringToByteArray(message);
      DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length, targetAddress);

      // send message packet
      for (int j = 0; j < UPnPDefaults.UDP_SEND_COUNT; j++)
      {
        socketStructure.getSSDPUnicastSocket().send(packet);
        Thread.sleep(UPnPDefaults.CP_M_SEARCH_MESSAGE_DELAY);
      }
    } catch (Exception e)
    {
      logger.fatal("cannot send packet");
      logger.fatal("reason: " + e.getMessage());
    }
  }

  public void triggerEvents()
  {
    // check all associated unicast sockets for received packets
    Vector socketStructures = controlPoint.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure socketStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      // stick to one socket until all pending packets were read
      boolean packetFound = true;
      while (packetFound)
      {
        BinaryMessageObject message =
          SocketHelper.readBinaryMessage(null,
            socketStructure.getSSDPUnicastSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (message != null)
        {
          // TemplateControlPoint.printMessage(DateTimeHelper.formatCurrentDateForDebug() +
          // ":Received M-SEARCH response");

          mSearchResponseProcessor.processMessage(socketStructure, message);
        } else
        {
          packetFound = false;
        }
      }
    }
  }

}

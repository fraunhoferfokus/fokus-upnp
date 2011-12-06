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

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is used by control points to receive NOTIFY event messages via UDP, either over
 * unicast or multicast. This is a proprietary extension to speed up eventing.
 * 
 * @author Alexander Koenig
 * 
 */
public class CPUDPEventNotifyServer implements IEventListener
{

  /** GENA logger */
  static Logger                            logger = Logger.getLogger("upnp");

  private ControlPoint                     controlPoint;

  private CPEventMessageProcessor          eventMessageProcessor;

  private CPMulticastEventMessageProcessor multicastEventMessageProcessor;

  /**
   * Creates a new instance of CPUDPEventNotifyServer.
   * 
   * @param controlPoint
   *          The associated control point
   * @param processor
   *          The listener for incoming NOTIFY messages
   * @param multicastProcessor
   *          The listener for incoming multicast NOTIFY messages
   * 
   */
  public CPUDPEventNotifyServer(ControlPoint controlPoint,
    CPEventMessageProcessor processor,
    CPMulticastEventMessageProcessor multicastProcessor)
  {
    this.controlPoint = controlPoint;
    this.eventMessageProcessor = processor;
    this.multicastEventMessageProcessor = multicastProcessor;
  }

  /**
   * Processes the UDP event messages from one socket.
   * 
   * @param socket
   * @param socketAddress
   * @param unicastEventMessage
   */
  private void readMessagesFromSocket(DatagramSocket socket,
    InetSocketAddress socketAddress,
    boolean unicastEventMessage)
  {
    // stay to one socket until all pending packets were read
    boolean packetFound = true;
    while (packetFound)
    {
      BinaryMessageObject message = SocketHelper.readBinaryMessage(null, socket, SocketHelper.DEFAULT_SOCKET_TIMEOUT);

      if (message != null)
      {
        // create HTTP from binary message
        HTTPMessageObject httpMessage = message.toHTTPMessageObject();
        httpMessage.setDestinationAddress(socketAddress);

        if (unicastEventMessage)
        {
          // process message as if received over normal HTTP client
          HTTPMessageObject response = eventMessageProcessor.processMessage(httpMessage);
          response.setDestinationAddress(message.getSourceAddress());
          // send response to device
          SocketHelper.sendBinaryMessage(response.toBinaryMessage(), socket);
        } else
        {
          // use multicast event message handling
          multicastEventMessageProcessor.processMessage(httpMessage);
        }
      } else
      {
        packetFound = false;
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
    // check all associated multicast sockets for received packets
    Vector socketStructures = controlPoint.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure socketStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      // read normal UDP event messages
      if (socketStructure.getEventCallbackUDPServer() != null)
      {
        readMessagesFromSocket(socketStructure.getEventCallbackUDPServer(),
          socketStructure.getUDPServerSocketAddress(),
          true);
      }
      // read multicast UDP event messages
      if (socketStructure.getMulticastEventUDPServer() != null && multicastEventMessageProcessor != null)
      {
        readMessagesFromSocket(socketStructure.getMulticastEventUDPServer(),
          socketStructure.getMulticastUDPServerSocketAddress(),
          false);
      }
    }
  }

}

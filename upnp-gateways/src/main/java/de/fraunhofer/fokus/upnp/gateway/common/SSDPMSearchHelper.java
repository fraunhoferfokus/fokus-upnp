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

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLExtension;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class is used to answer M-SEARCH messages received on one network interface directly. It
 * also generates M-SEARCH response packets for devices that are connected on other network
 * interfaces. These response packets are modified in a way that consecutive GET requests are
 * forwarded over a proxy HTTP server. It is expected that the socket that should be used for
 * responding has the same IP address as the proxy HTTP server that will answer consecutive GET
 * requests.
 * 
 * @author Alexander Koenig
 * 
 */
public class SSDPMSearchHelper
{

  /**
   * Helper method that extends the location in a M-SEARCH response message.
   * 
   * @param message
   *          M-SEARCH response message
   * @param outgoingInterfaceID
   *          The ID of the interface that should forward the resulting GET message
   * @param proxySocketAddress
   *          The interface that should receive the resulting GET message
   * 
   */
  public static String extendMSearchResponseMessage(String message,
    String outgoingInterfaceID,
    InetSocketAddress proxySocketAddress)
  {
    // extend URL with HTTP server address of the forwarder module
    String result =
      URLExtension.extendLocation(message,
        outgoingInterfaceID,
        proxySocketAddress.getAddress().getHostAddress(),
        proxySocketAddress.getPort());

    if (!result.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
    {
      result += CommonConstants.NEW_LINE;
    }

    return result;
  }

  /**
   * Helper method that sends one M-SEARCH response message
   * 
   * @param responseMessage
   *          The M-SEARCH response message
   * @param responseSocket
   *          The socket that should be used for sending the response messages
   * @param replySocketAddress
   *          The destination address for the response messages
   * 
   */
  public static void sendMSearchResponse(String responseMessage,
    IDatagramSocket responseSocket,
    InetSocketAddress replySocketAddress)
  {
    try
    {
      byte[] responseMessageData = StringHelper.stringToByteArray(responseMessage);
      DatagramPacket responsePacket =
        new DatagramPacket(responseMessageData,
          responseMessageData.length,
          replySocketAddress.getAddress(),
          replySocketAddress.getPort());

      for (int k = 0; k < UPnPDefaults.UDP_SEND_COUNT; k++)
      {
        responseSocket.send(responsePacket);
        Thread.sleep(UPnPDefaults.DEVICE_M_SEARCH_RESPONSE_MESSAGE_DELAY);
      }
    } catch (Exception e)
    {
    }
  }

  /**
   * Helper method that sends one M-SEARCH message
   * 
   * @param message
   *          The M-SEARCH message
   * @param socket
   *          The socket that should be used for sending the message
   * @param destinationSocketAddress
   *          The destination address for the message
   * 
   */
  public static void sendMSearch(String message, IDatagramSocket socket, InetSocketAddress destinationSocketAddress)
  {
    // same functionality
    sendMSearchResponse(message, socket, destinationSocketAddress);
  }

}

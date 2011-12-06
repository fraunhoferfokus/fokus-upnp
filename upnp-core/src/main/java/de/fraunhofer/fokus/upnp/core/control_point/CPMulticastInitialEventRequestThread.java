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

import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is used by control points to request the current state variable values for services
 * that use multicast eventing.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class CPMulticastInitialEventRequestThread extends Thread
{

  private DatagramSocket datagramSocket;

  private CPService      service;

  public CPMulticastInitialEventRequestThread(CPService service)
  {
    this.service = service;
    start();
  }

  public void run()
  {
    try
    {
      datagramSocket = new DatagramSocket();
      InetSocketAddress address = service.getMulticastEventSocketAddress();
      if (address == null)
      {
        System.out.println("Try to request initial event, but multicast event address is unknown");
        service.initialEventMessageResponse(false);
        return;
      }

      String path =
        "/" + URLHelper.escapeURL(service.getCPDevice().getUDN()) + "/" +
          URLHelper.escapeURL(service.getShortenedServiceId());

      String request = GENAMessageBuilder.buildInitialEventRequest(path, address.getHostName(), address.getPort());

      // send request
      BinaryMessageObject message =
        new BinaryMessageObject(StringHelper.stringToByteArray(request),
          (InetSocketAddress)datagramSocket.getLocalSocketAddress(),
          address);

      SocketHelper.sendBinaryMessage(message, datagramSocket);

      // try to receive response message within 10 seconds
      BinaryMessageObject responseMessage = SocketHelper.readBinaryMessage(null, datagramSocket, 3000);

      if (responseMessage != null)
      {
        // System.out.println("Received response to initial event request");

        // create HTTP from binary message
        HTTPMessageObject httpResponseMessage = responseMessage.toHTTPMessageObject();
        httpResponseMessage.setDestinationAddress((InetSocketAddress)datagramSocket.getLocalSocketAddress());

        ControlPoint controlPoint = service.getCPDevice().getControlPoint();
        CPMulticastEventMessageProcessor messageProcessor = controlPoint.getCPMulticastEventMessageProcessor();
        if (messageProcessor != null)
        {
          messageProcessor.processInitialEventResponseMessage(httpResponseMessage);

          service.initialEventMessageResponse(true);
          return;
        }
      }
    } catch (Exception e)
    {
      System.out.println("Error requesting initial event: " + e.getMessage());
    }
    service.initialEventMessageResponse(false);
  }

}

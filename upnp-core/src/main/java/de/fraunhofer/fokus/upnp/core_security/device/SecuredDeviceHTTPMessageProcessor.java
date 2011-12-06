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

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class handles all received HTTP messages like GET, POST or NOTIFY for security aware
 * devices.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredDeviceHTTPMessageProcessor extends DeviceHTTPMessageProcessor
{
  /**
   * Creates a new instance of SecuredDeviceHTTPMessageProcessor.
   * 
   * @param device
   *          The associated device
   */
  public SecuredDeviceHTTPMessageProcessor(Device device)
  {
    super(device);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceHTTPMessageProcessor#processMessage(java.net.InetSocketAddress,
   *      de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    logger.info("new UPnP message received.");
    if (request.getHeader() == null)
    {
      return null;
    }
    InetSocketAddress serverAddress = request.getDestinationAddress();

    // modify request if needed
    if (messageModifier != null)
    {
      messageModifier.modifyHTTPRequest(request);
    }

    HTTPMessageObject result = null;

    // parse request
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(request);

    // process request in appropriate handler
    // GET or HEAD
    if (httpParser.isGETRequest() || httpParser.isHEADRequest())
    {
      logger.info("received and parsing GET or HEAD message header......");

      // System.out.println("Received " + HTTPMessageHelper.getHeaderDescription(header));

      result = SecuredDeviceGetMessageProcessor.processMessage(httpParser, device);
    }
    // POST or M-POST
    if (httpParser.isSOAPRequest())
    {
      logger.info("POST or MPOST message header succesfully parsed.");
      // handle request in security aware message processor
      result = SecuredDeviceControlMessageProcessor.processMessage(httpParser, device);
    }
    if (httpParser.isSUBSCRIBEMessage())
    {
      logger.info("SUBSCRIBE message header succesfully parsed.");
      result = SecuredDeviceSubscribeMessageProcessor.processSubscribe(httpParser, device);
    }
    if (httpParser.isRESUBSCRIBEMessage())
    {
      logger.info("RESUBSCRIBE message header succesfully parsed.");
      result = SecuredDeviceSubscribeMessageProcessor.processResubscribe(httpParser, device);
    }
    if (httpParser.isUNSUBSCRIBEMessage())
    {
      logger.info("UNSUBSCRIBE message header succesfully parsed.");
      result = SecuredDeviceSubscribeMessageProcessor.processUnsubscribe(httpParser, device);
    }
    if (result != null)
    {
      // set destination address for response message
      result.setDestinationAddress(request.getSourceAddress());

      if (messageModifier != null)
      {
        messageModifier.modifyHTTPResponse(result);
      }

      return result;
    }

    logger.warn("unknown UPnP message.");
    logger.warn("Message header = " + request.getHeader());

    return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
  }

}

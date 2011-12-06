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

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageModifier;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class handles all received HTTP messages like GET, POST or NOTIFY. It implements the
 * IHTTPMessageProcessor interface.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceHTTPMessageProcessor implements IHTTPMessageProcessor
{
  protected static Logger        logger = Logger.getLogger("upnp.http");

  protected Device               device;

  /** Optional modifier for sent and received messages */
  protected IHTTPMessageModifier messageModifier;

  /**
   * Creates a new instance of DeviceHTTPMessageProcessor.
   * 
   * @param device
   *          The associated device
   */
  public DeviceHTTPMessageProcessor(Device device)
  {
    this.device = device;
  }

  /**
   * Checks if the HTTP message is comprehensible and calls the appropriate message handler
   * 
   * @param request
   *          The HTTP request
   * 
   * @return The response message
   * 
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    // long startTime = HighResTimerHelper.getTimeStamp();
    if (logger.isInfoEnabled())
    {
      logger.info("new UPnP message received.");
    }
    if (request.getHeader() == null)
    {
      return null;
    }

    // System.out.println("Received request: [\n" + request.getHeader() + "\n" +
    // request.getBodyAsUTF8String());

    // add port to host if necessary
    request.setHeader(HTTPMessageHelper.tryAddPortToHost(request.getHeader(), request.getDestinationAddress().getPort()));

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

      result = DeviceGetMessageProcessor.processMessage(httpParser, device);
    }
    // POST or M-POST
    if (httpParser.isSOAPRequest())
    {
      if (logger.isInfoEnabled())
      {
        logger.info("POST or MPOST message header succesfully parsed.");
      }
      result = DeviceControlMessageProcessor.processControlMessage(httpParser, device);
    }
    if (httpParser.isSUBSCRIBEMessage())
    {
      logger.info("SUBSCRIBE message header succesfully parsed.");
      result = DeviceSubscribeMessageProcessor.processSubscribe(httpParser, device);
    }
    if (httpParser.isRESUBSCRIBEMessage())
    {
      logger.info("RESUBSCRIBE message header succesfully parsed.");
      result = DeviceSubscribeMessageProcessor.processResubscribe(httpParser, device);
    }
    if (httpParser.isUNSUBSCRIBEMessage())
    {
      logger.info("UNSUBSCRIBE message header succesfully parsed.");
      result = DeviceSubscribeMessageProcessor.processUnsubscribe(httpParser, device);
    }
    if (result != null)
    {
      // long endTime = HighResTimerHelper.getTimeStamp();
      // System.out.println("Time to handle message: " +
      // HighResTimerHelper.getMicroseconds(startTime, endTime));
      // System.out.println("Action request size: " + request.getSize());
      // System.out.println("Action response size: " + result.getSize());

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

    return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, request.getDestinationAddress());
  }

  /**
   * Sets the message modifier.
   * 
   * @param messageModifier
   *          The messageModifier to set
   */
  public void setMessageModifier(IHTTPMessageModifier messageModifier)
  {
    this.messageModifier = messageModifier;
  }
}

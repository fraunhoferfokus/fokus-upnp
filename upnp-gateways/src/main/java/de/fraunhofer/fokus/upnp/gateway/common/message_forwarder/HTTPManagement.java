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
package de.fraunhofer.fokus.upnp.gateway.common.message_forwarder;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLExtension;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is responsible for forwarding HTTP messages between different forwarder modules. It is
 * part of the central message forwarder.
 * 
 * @author Alexander Koenig
 */
public class HTTPManagement implements IEventListener
{

  private static Logger    logger = Logger.getLogger("upnp");

  private MessageForwarder messageForwarder;

  /** Creates a new instance of HTTPManagement. */
  public HTTPManagement(MessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
  }

  /**
   * Forwards a message to another HTTP server and returns the response
   * 
   * @param sourceModuleID
   *          The module that received the request
   * @param httpRequest
   *          The message
   * 
   * @return The HTTP response message
   */
  public HTTPMessageObject processHTTPRequest(String sourceModuleID, HTTPMessageObject httpRequest, boolean udpMessage)
  {
    String header = httpRequest.getHeader();
    // check header type
    if (header.startsWith(CommonConstants.GET) || header.startsWith(CommonConstants.HEAD) ||
      header.startsWith(HTTPConstant.POST) || header.startsWith(HTTPConstant.M_POST) ||
      header.startsWith(CommonConstants.SUBSCRIBE) || header.startsWith(CommonConstants.NOTIFY) ||
      header.startsWith(CommonConstants.UNSUBSCRIBE))
    {
      logger.info("received message header......");
      HTTPParser httpParser = new HTTPParser();
      httpParser.parse(header);
      if (httpParser.isHTTPMessage())
      {
        logger.info("Message header successfully parsed.");

        String encodedPath = httpParser.getHostPath();
        // decode path to retrieve original socket address and path
        Object[] pathElements = URLExtension.decodeGatewayURLPath(encodedPath);

        if (pathElements != null)
        {
          String outgoingModuleID = (String)pathElements[0];
          String targetAddress = (String)pathElements[1];
          int targetPort = ((Integer)pathElements[2]).intValue();
          String originalPath = (String)pathElements[3];

          String baseURL = targetAddress + ":" + targetPort + originalPath;
          // remove document from original path to get baseURL
          if (!baseURL.endsWith("/") && baseURL.lastIndexOf("/") != -1)
          {
            baseURL = baseURL.substring(0, baseURL.lastIndexOf("/"));
          }

          // apply changes to request if necessary
          modifyHTTPRequest(httpRequest, sourceModuleID, outgoingModuleID);

          // replace path in message with original path
          String modifiedHeader = httpRequest.getHeader();
          modifiedHeader = HTTPMessageHelper.replacePathInRequestLine(modifiedHeader, originalPath);
          modifiedHeader = HTTPMessageHelper.replaceHost(modifiedHeader, targetAddress + ":" + targetPort);

          logger.info("Changed header in forwarded message to:\n[" + modifiedHeader + "]");

          // save changed header
          httpRequest.setHeader(modifiedHeader);

          // System.out.println(" HTTPManagement: Forward " +
          // HTTPMessageHelper.getHeaderDescription(message.getHeader()));

          // retrieve module that should forward the request
          IForwarderModule forwarderModule = messageForwarder.getForwarderModuleByID(outgoingModuleID);
          InetSocketAddress targetSocketAddress = new InetSocketAddress(targetAddress, targetPort);
          // set destination for message
          httpRequest.setDestinationAddress(targetSocketAddress);

          HTTPMessageObject responseMessage = null;
          if (forwarderModule != null)
          {
            // forward message, either over UDP or TCP
            if (udpMessage)
            {
              responseMessage = forwarderModule.forwardHTTPOverUDPMessage(sourceModuleID, httpRequest);
            } else
            {
              responseMessage = forwarderModule.forwardHTTPRequest(sourceModuleID, httpRequest);
            }
          }
          if (responseMessage != null)
          {
            // System.out.println(" HTTPManagement: Response " +
            // HTTPMessageHelper.getHeaderDescription(responseMessage.getHeader()));

            // apply changes if necessary
            modifyHTTPResponse(httpRequest, responseMessage, sourceModuleID, outgoingModuleID, baseURL);

            // return response message to calling module
            return responseMessage;
          }
        } else
        {
          logger.error("Could not decode path for HTTP message forwarding: " + encodedPath);
        }
      }
    }
    return null;
  }

  /**
   * Forwards a message to another HTTP server and returns the response
   * 
   * @param sourceModuleID
   *          The module that received the request
   * @param httpRequest
   *          The message
   * 
   * @return The HTTP response message
   */
  public HTTPMessageObject processHTTPRequest(String sourceModuleID, HTTPMessageObject httpRequest)
  {
    return processHTTPRequest(sourceModuleID, httpRequest, false);
  }

  /**
   * Modifies a given HTTP request if necessary.
   * 
   * @param httpRequest
   *          The received message
   * @param sourceModuleID
   *          The ID of the module that received the request
   * @param forwarderModuleID
   *          The ID of the module that will forward the request
   */
  public void modifyHTTPRequest(HTTPMessageObject httpRequest, String sourceModuleID, String forwarderModuleID)
  {
    // check for GENA subscribe message, because callback URL must be redirected
    if (HTTPMessageHelper.isInitialSubscribe(httpRequest.getHeader()))
    {
      // this module will receive consecutive event messages
      IForwarderModule callbackModule = messageForwarder.getForwarderModuleByID(forwarderModuleID);

      // System.out.println("Received SUBSCRIBE message:\n["+header+"]");
      // callback is received by forwarderModule HTTP server and forwarded by the sourceModule
      String modifiedHeader =
        URLExtension.extendCallback(httpRequest.getHeader(),
          sourceModuleID,
          callbackModule.getHTTPServerAddress(),
          callbackModule.getHTTPServerPort());

      // extend UDP callback addresses in the same way
      modifiedHeader =
        URLExtension.extendUDPCallback(modifiedHeader,
          sourceModuleID,
          callbackModule.getHTTPServerAddress(),
          callbackModule.getHTTPOverUDPServerPort());

      // System.out.println("Changed header in forwarded SUBSCRIBE message to:\n["+header+"]");
      httpRequest.setHeader(modifiedHeader);
    }
    // check for SOAP requests that could contain URLs
    if (httpRequest.getHeader().startsWith(HTTPConstant.POST) ||
      httpRequest.getHeader().startsWith(HTTPConstant.M_POST))
    {
      String requestBodyString = StringHelper.byteArrayToUTF8String(httpRequest.getBody());
      // retrieve IP address of gateway server
      String gatewayHTTPServer = IPHelper.toString(httpRequest.getDestinationAddress());
      // check for URLs that are targeted to the gateway device
      // e.g., media server or POI items
      IForwarderModule forwarderModule = messageForwarder.getForwarderModuleByID(forwarderModuleID);
      // item requests can either be changed back or
      // redirected over this gateway
      // further messages must use the same address:port as this message
      String changedMessage =
        URLExtension.extendSoapRequestURLs(requestBodyString,
          gatewayHTTPServer,
          forwarderModuleID,
          forwarderModule.getHTTPServerAddress(),
          forwarderModule.getHTTPServerPort());

      if (!changedMessage.equals(requestBodyString))
      {
        System.out.println("Changed SOAP request to: [\n" + changedMessage + "]");
      }

      byte[] modifiedBody = StringHelper.utf8StringToByteArray(changedMessage);
      httpRequest.setBody(modifiedBody);
      // change content length in response header
      httpRequest.setHeader(HTTPMessageHelper.replaceContentLength(httpRequest.getHeader(), modifiedBody.length));
    }
  }

  /**
   * Modifies a given HTTP response if necessary.
   * 
   * @param httpRequest
   *          The associated request message
   * @param httpResponse
   *          The response message
   * @param sourceModuleID
   *          The ID of the module that received the request
   * @param forwarderModuleID
   *          The ID of the module that will forward the request
   * @param baseURL
   *          The baseURL retrieved from the original request
   * 
   */
  public void modifyHTTPResponse(HTTPMessageObject httpRequest,
    HTTPMessageObject httpResponse,
    String sourceModuleID,
    String forwarderModuleID,
    String baseURL)
  {
    if (httpResponse.getBody() == null)
    {
      return;
    }

    String responseBodyString = StringHelper.byteArrayToUTF8String(httpResponse.getBody());
    // check for XML and device description
    if (responseBodyString.startsWith(XMLConstant.XML_START) &&
      responseBodyString.indexOf(XMLConstant.XMLNS_DEVICE) != -1)
    {
      // device description found, replace URLs
      IForwarderModule sourceModule = messageForwarder.getForwarderModuleByID(sourceModuleID);
      // further messages must use the same address:port as this message
      String changedDeviceDescription =
        URLExtension.extendDeviceDescriptionURLs(responseBodyString,
          forwarderModuleID,
          sourceModule.getHTTPServerAddress(),
          sourceModule.getHTTPServerPort(),
          baseURL);

      // System.out.println("HTTPManagement: Replaced all URLs in DeviceDescription," +
      // " result is [\n"+changedDeviceDescription+"]");

      byte[] modifiedBody = StringHelper.utf8StringToByteArray(changedDeviceDescription);
      httpResponse.setBody(modifiedBody);
      // change content length in response header
      httpResponse.setHeader(HTTPMessageHelper.replaceContentLength(httpResponse.getHeader(), modifiedBody.length));
    }
    // check for SOAP responses that could contain URLs
    if (httpRequest.getHeader().startsWith(HTTPConstant.POST) ||
      httpRequest.getHeader().startsWith(HTTPConstant.M_POST))
    {
      String originalHTTPServer = IPHelper.toString(httpRequest.getDestinationAddress());
      // extend all URLs that are delivered via the device internal HTTP server
      // e.g., media server or POI items
      IForwarderModule sourceModule = messageForwarder.getForwarderModuleByID(sourceModuleID);
      // item requests must be redirected over this gateway
      String changedMessage =
        URLExtension.extendSoapResponseURLs(responseBodyString,
          originalHTTPServer,
          forwarderModuleID,
          sourceModule.getHTTPServerAddress(),
          sourceModule.getHTTPServerPort());

      byte[] modifiedBody = StringHelper.utf8StringToByteArray(changedMessage);
      httpResponse.setBody(modifiedBody);
      // change content length in response header
      httpResponse.setHeader(HTTPMessageHelper.replaceHeaderLine(httpResponse.getHeader(),
        CommonConstants.CONTENT_LENGTH,
        " " + Integer.toString(modifiedBody.length)));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    UDPPacketManager udpPacketManager = messageForwarder.getUDPPacketManager();
    // read UDP event messages from all modules
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      boolean readUDP = true;
      while (readUDP)
      {
        BinaryMessageObject binaryRequest =
          SocketHelper.readBinaryMessage(udpPacketManager,
            forwarderModules[i].getHTTPOverUDPSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (binaryRequest != null)
        {
          HTTPMessageObject httpRequest = binaryRequest.toHTTPMessageObject();
          System.out.println("      ForwarderModule.Received UDP msg: " +
            HTTPMessageHelper.getHeaderDescription(httpRequest.getHeader()) + ":" +
            httpRequest.getBodyAsString().length());

          HTTPMessageObject httpResponse = forwarderModules[i].receivedHTTPOverUDPMessage(httpRequest);

          // send response if possible
          if (httpResponse != null)
          {
            System.out.println("      ForwarderModule.Return UDP resp.: " +
              HTTPMessageHelper.getHeaderDescription(httpResponse.getHeader()) + ":" +
              httpResponse.getBodyAsString().length() + " to " + binaryRequest.getSourceAddress());

            BinaryMessageObject binaryResponse = httpResponse.toBinaryMessage();
            binaryResponse.setDestinationAddress(binaryRequest.getSourceAddress());
            SocketHelper.sendBinaryMessage(binaryResponse, forwarderModules[i].getHTTPOverUDPSocket());
          }
        } else
        {
          readUDP = false;
        }
      }
    }
  }

}

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
package de.fraunhofer.fokus.upnp.http;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class models the sending and receiving of one message via HTTP.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class HTTPMessageFlow
{

  private static Hashtable                  httpClientHashtable             = new Hashtable();

  private static HTTPOverMulticastUDPClient httpOverMulticastClient;

  private static Object                     lock                            = new Object();

  public static String                      MESSAGE_OPTION_KEY_BODY         = "Body";

  public static String                      MESSAGE_OPTION_KEY_MESSAGE_TYPE = "MessageType";

  public static String                      MESSAGE_OPTION_KEY_UDP_CLIENT   = "UDPClient";

  /**
   * Processes the response to a HTTP request.
   * 
   * @param messageOptions
   *          Message type that should be created. Implementation-dependent
   * @param targetURL
   *          URL that should receive the request
   * @param messageFlowImplementation
   *          Class that implements the methods to create the request and to process the response
   * @param response
   *          The received response
   * 
   * @return Null if the request could not be created. The response parser if the response message contains a HTTP error
   *         code. An implementation-dependent object if the request was successful.
   * 
   */
  private static Object processResponse(Hashtable messageOptions,
    URL targetURL,
    IHTTPMessageFlow messageFlowImplementation,
    HTTPMessageObject response)
  {
    // handle empty response
    if (response == null)
    {
      System.out.println("Response is empty for request to " + targetURL.toExternalForm());
      return null;
    }
    // parse response
    HTTPParser responseParser = messageFlowImplementation.createResponseParser(messageOptions, targetURL);
    responseParser.parse(response);

    if (!responseParser.isHTTPResponse())
    {
      System.out.println("Response header is empty or invalid for request to " + targetURL.toExternalForm());
      return null;
    }
    if (responseParser.isHTTPErrorResponse())
    {
      System.out.println("Request to " + targetURL.toExternalForm() + " returned HTTP error " +
        responseParser.getResponseCode() + ":" + responseParser.getResponseDescription());
      return responseParser;
    }

    return messageFlowImplementation.processResponse(messageOptions, targetURL, response, responseParser);
  }

  /**
   * Builds a HTTP request and sends it to a target URL.
   * 
   * @param messageOptions
   *          Message type that should be created. Implementation-dependent
   * @param targetURL
   *          URL that should receive the request
   * @param messageFlowImplementation
   *          Class that implements the methods to create the request and to process the response
   * 
   * @return Null if the request could not be created. The response parser if the response message contains a HTTP error
   *         code. An implementation-dependent object if the request was successful.
   * 
   */
  public static Object sendMessageAndProcessResponse(Hashtable messageOptions,
    URL targetURL,
    IHTTPMessageFlow messageFlowImplementation)
  {
    // build message
    HTTPMessageObject request = messageFlowImplementation.createRequest(messageOptions, targetURL);
    // check if request could not be built
    if (request == null)
    {
      return null;
    }
    // allow modification of request
    messageFlowImplementation.modifyRequest(messageOptions, targetURL, request);

    HTTPClient httpClient = null;
    synchronized(lock)
    {
      // check for existing HTTP client
      InetSocketAddress destinationAddress = request.getDestinationAddress();
      if (HTTPDefaults.PERSISTENT_CLIENT_CONNECTION)
      {
        if (httpClientHashtable.containsKey(destinationAddress))
        {
          // System.out.println("HTTPMessageFlow: Reuse existing HTTP client to access " +
          // targetURL);
          httpClient = (HTTPClient)httpClientHashtable.get(destinationAddress);
        }
        if (httpClient == null)
        {
          System.out.println("    HTTPMessageFlow: Create new HTTP client to access  " + targetURL);
          // send request to server
          httpClient = new HTTPClient(true);
          httpClientHashtable.put(destinationAddress, httpClient);
        }
      } else
      {
        System.out.println("    HTTPMessageFlow: Create new HTTP client to access  " + targetURL);

        // add connection close header to request
        request.setHeader(HTTPMessageHelper.addHeaderLine(request.getHeader(), HTTPConstant.CONNECTION_HEADER, "close"));

        // no persistence
        httpClient = new HTTPClient(false);
      }
    }
    // synchronize access within each HTTP client
    synchronized(httpClient.getLock())
    {
      httpClient.sendRequestAndWaitForResponse(request);

      // retrieve response
      HTTPMessageObject response = httpClient.getResponse();

      return processResponse(messageOptions, targetURL, messageFlowImplementation, response);
    }
  }

  /**
   * Builds a HTTP request and sends it to a target URL using multicast UDP.
   * 
   * @param messageOptions
   *          Message type that should be created. Implementation-dependent
   * @param targetURL
   *          URL that should receive the request
   * @param messageFlowImplementation
   *          Class that implements the methods to create the request
   * 
   * @return False if the request could not be created true otherwise.
   */
  public static boolean sendMessageOverMulticastUDP(Hashtable messageOptions,
    URL targetURL,
    IHTTPMessageFlow messageFlowImplementation)
  {
    // build message
    HTTPMessageObject request = messageFlowImplementation.createRequest(messageOptions, targetURL);
    // check if request could not be built
    if (request == null)
    {
      return false;
    }
    // allow modification of request
    messageFlowImplementation.modifyRequest(messageOptions, targetURL, request);

    synchronized(lock)
    {
      // send request to server
      if (httpOverMulticastClient == null)
      {
        httpOverMulticastClient = new HTTPOverMulticastUDPClient();
      }
    }
    synchronized(httpOverMulticastClient.getLock())
    {
      httpOverMulticastClient.sendRequest(request);
    }
    return true;
  }

  /**
   * Builds a HTTP request and sends it to a target URL using UDP.
   * 
   * @param messageOptions
   *          Message type that should be created. Implementation-dependent
   * @param targetURL
   *          URL that should receive the request
   * @param messageFlowImplementation
   *          Class that implements the methods to create the request
   * @param socket
   *          The socket that should be used to send the message
   * 
   */
  public static void sendMessageOverUDP(Hashtable messageOptions,
    URL targetURL,
    IHTTPMessageFlow messageFlowImplementation,
    DatagramSocket socket)
  {
    // build message
    HTTPMessageObject request = messageFlowImplementation.createRequest(messageOptions, targetURL);
    // check if request could not be built
    if (request == null)
    {
      return;
    }
    // allow modification of request
    messageFlowImplementation.modifyRequest(messageOptions, targetURL, request);

    // send request to server
    HTTPOverUDPClient httpClient = new HTTPOverUDPClient(socket);
    httpClient.sendRequest(request);
  }

  /**
   * Builds a HTTP request and sends it to a target URL using UDP.
   * 
   * @param messageOptions
   *          Message type that should be created. Implementation-dependent
   * @param targetURL
   *          URL that should receive the request
   * @param messageFlowImplementation
   *          Class that implements the methods to create the request and to process the response
   * 
   * @return Null if the request could not be created or the response is null. The response parser if the response
   *         message contains a HTTP error code. An implementation-dependent object if the request was successful.
   * 
   */
  public static Object sendMessageOverUDPAndProcessResponse(Hashtable messageOptions,
    URL targetURL,
    IHTTPMessageFlow messageFlowImplementation)
  {
    // build message
    HTTPMessageObject request = messageFlowImplementation.createRequest(messageOptions, targetURL);
    // check if request could not be built
    if (request == null)
    {
      return null;
    }
    // allow modification of request
    messageFlowImplementation.modifyRequest(messageOptions, targetURL, request);

    // check for existing client
    HTTPOverUDPClient httpClient = (HTTPOverUDPClient)messageOptions.get(MESSAGE_OPTION_KEY_UDP_CLIENT);
    if (httpClient == null)
    {
      httpClient = new HTTPOverUDPClient();
    }
    // send request to server
    httpClient.sendRequestAndWaitForResponse(request);

    // retrieve response
    HTTPMessageObject response = httpClient.getResponse();
    httpClient = null;

    return processResponse(messageOptions, targetURL, messageFlowImplementation, response);
  }

}

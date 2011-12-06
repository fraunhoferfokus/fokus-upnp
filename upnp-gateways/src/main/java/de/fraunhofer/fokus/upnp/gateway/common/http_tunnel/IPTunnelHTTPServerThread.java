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
package de.fraunhofer.fokus.upnp.gateway.common.http_tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessage;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.http.IHTTPStreamingMessageProcessor;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocket;

/**
 * This class handles reading the message input and sending the response to its associated socket.
 * 
 * @author tje, Alexander Koenig
 * @version 1.0
 */
public class IPTunnelHTTPServerThread extends Thread
{

  /** Socket for this connection */
  private IPTunnelSocket                 socket                    = null;

  /** Listener for incoming messages */
  private IHTTPMessageProcessor          messageProcessor          = null;

  /** Listener for incoming messages where the response should be streamed */
  private IHTTPStreamingMessageProcessor streamingMessageProcessor = null;

  /** Request */
  private HTTPMessageObject              request;

  /** Address of this server */
  private InetSocketAddress              serverAddress;

  private boolean                        terminated                = false;

  /**
   * Constructor of thread. The thread is not started.
   * 
   * @param socket
   *          connection to client
   * @param messageProcessor
   *          messagelistener
   * @throws NullPointerException
   *           if socket or messageProcessor is null
   */
  public IPTunnelHTTPServerThread(IPTunnelSocket socket, IHTTPMessageProcessor httpMessageProcessor)
  {
    super("HTTPServerThread " + socket.toString());
    this.socket = socket;
    serverAddress = socket.getLocalSocketAddress();
    this.messageProcessor = httpMessageProcessor;
  }

  /**
   * Constructor of thread. The thread is not started.
   * 
   * @param socket
   *          connection to client
   * @param streamingMessageProcessor
   *          The processor for the received message
   * @throws NullPointerException
   *           if socket or messageProcessor is null
   */
  public IPTunnelHTTPServerThread(IPTunnelSocket socket, IHTTPStreamingMessageProcessor streamingListener)
  {
    super("HTTPServerThread " + socket.toString());
    this.socket = socket;
    serverAddress = socket.getLocalSocketAddress();
    this.streamingMessageProcessor = streamingListener;
  }

  /**
   * method calls methods for get/process/send message/response and calls closing-method
   */
  public void run()
  {
    try
    {
      request = new HTTPMessageObject(null, null, socket.getDestinationSocketAddress(), serverAddress);
      socket.setSoTimeout(HTTPDefaults.TIMEOUT_FOR_RESEND * 1000);

      // always read the whole request
      HTTPMessage.getServerMessage(request, socket.getInputStream());

      // System.out.println("IPTunnelHTTPServer: Process tunneled request with size " +
      // requestHeader.length() + ":" + requestBody.length +
      // " from " +
      // socket.getDestinationAddress().getHostAddress() + ":" +
      // socket.getDestinationPort());

      // check whether the request should be received in one piece or in chunks
      if (messageProcessor != null)
      {
        // process all in one piece
        HTTPMessageObject response = messageProcessor.processMessage(request);
        if (response == null)
        {
          sendHTTPError();
        } else
        {
          sendResponse(response);
        }
      } else
      {
        // process response in chunks and send data as soon as it is available
        HTTPMessageObject response = streamingMessageProcessor.processMessage(serverAddress, request);
        if (response == null)
        {
          sendHTTPError();
        } else
        {
          sendResponse(response);
          // read data in chunks
          byte[] buffer = new byte[65536];
          int bytesRead = streamingMessageProcessor.readConsecutiveResponseBody(buffer, 1000);
          while (bytesRead != -1)
          {
            // send newly received data
            sendResponseData(buffer, bytesRead);
            bytesRead = streamingMessageProcessor.readConsecutiveResponseBody(buffer, 1000);
          }
        }
      }
    } catch (Exception ex)
    {
      System.out.println("Failed to process message:" + ex);
      ex.printStackTrace();
    } catch (OutOfMemoryError mem)
    {
      System.out.println("Client request too huge:" + mem);
    } finally
    {
      terminateInternal();
    }
    terminated = true;
  }

  /** This methods sends an error message for empty responses */
  private void sendHTTPError()
  {
    byte[] errorMessage = StringHelper.stringToByteArray(HTTPHeaderBuilder.buildHTTPError5xx());
    try
    {
      socket.getOutputStream().write(errorMessage);
    } catch (IOException e)
    {
      System.out.println("Response message cannot be send to " + socket.getDestinationAddress());
      System.out.println("reason: " + e.getMessage());
    }
  }

  /**
   * This method sends a response message.
   * 
   * @param response
   *          The response message
   */
  private void sendResponse(HTTPMessageObject response)
  {
    // System.out.println("Send response [\n" +
    // StringHelper.byteArrayToString(clientResponseMessage) + "]");
    byte[] responseData = response.toByteArray();

    try
    {
      socket.getOutputStream().write(responseData);
    } catch (IOException e)
    {
    }
  }

  /**
   * This method sends some bytes from a streamed response message.
   * 
   * @param data
   *          The data buffer
   * @param length
   *          The number of bytes that should be sent
   */
  private void sendResponseData(byte[] data, int length)
  {
    try
    {
      socket.getOutputStream().write(data, 0, length);
    } catch (IOException e)
    {
    }
  }

  /** Forces the termination of the HTTP server thread */
  public void terminate()
  {
    // force immediate termination
    socket.terminate(false);

    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
  }

  /** Terminates the socket normally by closing */
  private void terminateInternal()
  {
    socket.close();
    IPTunnelHTTPServer.connectionDone(socket);
  }

}

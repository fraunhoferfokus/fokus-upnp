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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.http.HTTPMessage;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocketFactory;

/**
 * This class represents a HTTPClient for tunneled connections.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class IPTunnelHTTPClient implements IHTTPClient
{
  /** Associated IPTunnel factory */
  private IPTunnelSocketFactory ipTunnelSocketFactory;

  /** Number of currently active clients */
  private static int            activeClients;

  private static boolean        debug              = false;

  /** Socket used for the connection */
  private IPTunnelSocket        socket             = null;

  /** Response header from this client */
  private HTTPMessageObject     responseMessage;

  private boolean               retryConnection    = true;

  /** The response body is read in fragments */
  private boolean               fragmentedResponse = false;

  /** Creates a new IPTunnelHTTPClient */
  public IPTunnelHTTPClient(IPTunnelSocketFactory ipTunnelSocketFactory)
  {
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#sendRequestAndWaitForResponse(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void sendRequestAndWaitForResponse(HTTPMessageObject request)
  {
    if (request == null)
    {
      return;
    }

    responseMessage = new HTTPMessageObject(null, null, request.getDestinationAddress(), request.getSourceAddress());

    // check if response message will be header only
    boolean expectHeaderOnlyResponse = HTTPMessageHelper.isHeaderOnlyResponseMessage(request.getHeader().toUpperCase());

    // cancel request if too many active clients
    if (activeClients > HTTPDefaults.MAX_ACTIVE_CLIENTS)
    {
      return;
    }

    ++activeClients;

    int numberOfRetries = 0;
    boolean success = false;
    byte[] requestData = request.toByteArray();
    while (!success && numberOfRetries < HTTPDefaults.MAX_CLIENT_FAILURES)
    {
      try
      {
        connect(request.getDestinationAddress());
        sendRequest(requestData);

        Thread.sleep(10);
        // Open the input stream after the whole output was sent.
        socket.setSoTimeout(HTTPDefaults.TIMEOUT_FOR_RESEND * 1000);
        // read and parse message
        HTTPMessage.getServerResponseMessage(responseMessage, socket.getInputStream(), false, expectHeaderOnlyResponse);
        /*
         * System.out.println("Result of request is [\n" + responseHeader + "\n" +
         * StringHelper.byteArrayToString(responseBody) + "]");
         */
        success = true;
      } catch (Exception e)
      { // Logging happened already in methods above
        if (retryConnection)
        {
          numberOfRetries++;
        } else
        {
          numberOfRetries = HTTPDefaults.MAX_CLIENT_FAILURES;
        }
      } catch (OutOfMemoryError mem)
      {
        numberOfRetries = HTTPDefaults.MAX_CLIENT_FAILURES;
      } finally
      {
        close();
      }
    }
    --activeClients;

    if (debug)
    {
      System.out.println("HTTPClient response header:" + responseMessage.getHeader());
      System.out.println("HTTPClient response body:" + StringHelper.byteArrayToUTF8String(responseMessage.getBody()));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#sendRequestAndReturnImmediately(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void sendRequestAndReturnImmediately(HTTPMessageObject request)
  {
    if (request == null)
    {
      return;
    }

    responseMessage = new HTTPMessageObject(null, null, request.getDestinationAddress(), request.getSourceAddress());

    // cancel request if too many active clients
    if (activeClients > HTTPDefaults.MAX_ACTIVE_CLIENTS)
    {
      return;
    }

    if (debug)
    {
      System.out.println("IPTunnelHTTPClient: Try to send request from " + socket.getLocalAddress().getHostAddress() +
        ":" + socket.getLocalPort() + " to " + IPHelper.toString(request.getDestinationAddress()) + " with size " +
        request.getSize());
    }

    ++activeClients;

    int numberOfRetries = 0;
    boolean success = false;
    byte[] requestData = request.toByteArray();
    // try to connect and send request
    while (!success && numberOfRetries < HTTPDefaults.MAX_CLIENT_FAILURES)
    {
      try
      {
        connect(request.getDestinationAddress());
        sendRequest(requestData);

        Thread.sleep(10);
        // Open the input stream after the whole output was sent.
        socket.setSoTimeout(HTTPDefaults.TIMEOUT_FOR_RESEND * 1000);
        // read response header and return
        HTTPMessage.getServerResponseMessage(responseMessage, socket.getInputStream(), true, false);

        fragmentedResponse = true;
        success = true;
      } catch (Exception e)
      { // Logging happened already in methods above
        if (retryConnection)
        {
          numberOfRetries++;
          close();
        } else
        {
          numberOfRetries = HTTPDefaults.MAX_CLIENT_FAILURES;
        }
      } catch (OutOfMemoryError mem)
      {
        numberOfRetries = HTTPDefaults.MAX_CLIENT_FAILURES;
      }
    }

    if (debug)
    {
      System.out.println("IPTunnelHTTPClient response header:" + responseMessage.getHeader());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#readConsecutiveResponseBody(byte[], long)
   */
  public int readConsecutiveResponseBody(byte[] buffer, long timeout) throws Exception
  {
    if (timeout == 0)
    {
      throw new Exception("Blocking read not allowed");
    }
    // check for closed socket
    if (socket.isClosed())
    {
      // signal end of stream
      return -1;
    }

    long time = System.currentTimeMillis();
    int result = -1;
    // offset in buffer
    int offset = 0;

    while (fragmentedResponse && System.currentTimeMillis() - time < timeout)
    {
      try
      {
        socket.setSoTimeout((int)(timeout / 2));
        int bytesRead = socket.getInputStream().read(buffer, offset, buffer.length - offset);

        // end of stream reached
        if (bytesRead == -1)
        {
          --activeClients;
          close();
          return result;
        }

        if (bytesRead > 0)
        {
          if (result == -1)
          {
            result = bytesRead;
            offset = bytesRead;
          } else
          {
            result += bytesRead;
            offset += bytesRead;
          }
        }
      } catch (SocketTimeoutException ste)
      {
        // do nothing for socket timeout
      } catch (Exception e)
      {
        // other exception, close socket and manage client termination
        --activeClients;
        close();
        return result;
      } catch (OutOfMemoryError mem)
      {
        --activeClients;
        close();
        return result;
      }
    }
    // no error occured, return number of read bytes
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#getResponse()
   */
  public HTTPMessageObject getResponse()
  {
    return responseMessage;
  }

  /**
   * Connects to the HTTP server.
   * 
   * @param destination
   *          The destination address
   * 
   * @throws IOException
   *           exception if connection is not possible
   * 
   */
  private void connect(InetSocketAddress destination) throws Exception
  {
    try
    {
      socket = ipTunnelSocketFactory.createIPTunnelSocket();
      socket.setSoTimeout(HTTPDefaults.TIMEOUT_FOR_RESEND * 1000);

      socket.connect(destination);
    } catch (UnknownHostException e)
    {
      retryConnection = false;
      throw e;
    } catch (IOException e)
    {
      throw e;
    } catch (Exception e)
    {
      retryConnection = false;
      throw e;
    }
  }

  /**
   * Sends the client request.
   * 
   * @param request
   *          The request
   * 
   * @throws IOException
   *           exception if connection is not possible
   * @throws NullPointerException
   *           if message is null
   * 
   */
  private void sendRequest(byte[] request) throws Exception
  {
    try
    {
      socket.getOutputStream().write(request);
    } catch (Exception e)
    {
      throw e;
    }
  }

  /**
   * Closes the client socket.
   */
  private void close()
  {
    try
    {
      if (socket != null)
      {
        if (socket.getOutputStream() != null)
        {
          socket.getOutputStream().close();
        }

        if (socket.getInputStream() != null)
        {
          socket.getInputStream().close();
        }

        socket.close();
      }
    } catch (Exception io)
    {
    }
  }

}

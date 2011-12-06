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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class represents a HTTPClient for the UPnP stack. It expects a port, a host and a request to
 * be sent. The response message is checked for header and body content which are both supplied to
 * the instance-caller.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class HTTPClient implements IHTTPClient
{
  /** HTTP client logger */
  private final static Logger  logger               = Logger.getLogger("upnp.http");

  /** Flag for debug output */
  private final static boolean debug                = false;

  /** Number of currently active clients */
  private static int           activeClients;

  /** Sync object */
  private Object               lock                 = new Object();

  /** Socket used for the connection */
  private Socket               socket               = null;

  /** Response message */
  private HTTPMessageObject    responseMessage;

  private boolean              retryConnection      = true;

  /** The response body is read in fragments */
  private boolean              fragmentedResponse   = false;

  /** The HTTP client should be reusable and not close the TCP socket connection */
  private boolean              persistentConnection = false;

  /** Flag to end pending request */
  private boolean              terminateClient      = false;

  /**
   * Creates a new instance of HTTPClient.
   * 
   * @param persistent
   *          Flag to make the client persistent
   */
  public HTTPClient(boolean persistent)
  {
    this.persistentConnection = persistent;
  }

  /**
   * Creates a new instance of HTTPClient.
   * 
   */
  public HTTPClient()
  {
    this.persistentConnection = false;
  }

  /** Retrieves the socked address this client is connected to. */
  public InetSocketAddress getRemoteSocketAddress()
  {
    if (socket != null && socket.isConnected())
    {
      return (InetSocketAddress)socket.getRemoteSocketAddress();
    }

    return null;
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
      logger.error("No message to be sent");
      return;
    }

    // create empty response message
    responseMessage = new HTTPMessageObject(null, null, request.getDestinationAddress(), request.getSourceAddress());

    // check if response message will be header only
    boolean expectHeaderOnlyResponse = HTTPMessageHelper.isHeaderOnlyResponseMessage(request.getHeader().toUpperCase());

    // System.out.println(activeClients + " active clients");

    // cancel request if too many active clients
    if (activeClients > HTTPDefaults.MAX_ACTIVE_CLIENTS)
    {
      System.out.println("Discard request due to too much stress");
      return;
    }
    activeClients++;

    // check if new request is targeted to another remote socket address
    if (socket != null && socket.isConnected() &&
      !((InetSocketAddress)socket.getRemoteSocketAddress()).equals(request.getDestinationAddress()))
    {
      System.out.println("Close existing socket due to new remote socket address");
      close();
    }
    int numberOfRetries = 1;

    // check if old socket is reused
    if (socket != null && socket.isConnected() &&
      ((InetSocketAddress)socket.getRemoteSocketAddress()).equals(request.getDestinationAddress()))
    {
      // System.out.println("Try to reuse HTTP client");
      // give one more retry because socket may already been closed from
      // the server side
      // this would be an unnecessary error
      numberOfRetries = 0;
    }

    boolean success = false;

    byte[] requestData = request.toByteArray();
    // try to send request a few times until success
    while (!success && !terminateClient && numberOfRetries < HTTPDefaults.MAX_CLIENT_FAILURES + 1)
    {
      try
      {
        // connect if needed
        if (socket == null)
        {
          // System.out.println("HTTPClient: Connect to " + request.getDestinationAddress());
          connect(request.getDestinationAddress());
        }
        // System.out.println("HTTPClient: Send request to " + request.getDestinationAddress());
        sendRequest(requestData);
        // System.out.println("HTTPClient: Wait for response from " +
        // request.getDestinationAddress());

        // read and parse response
        HTTPMessage.getServerResponseMessage(responseMessage, socket.getInputStream(), false, expectHeaderOnlyResponse);

        // System.out.println("Result of request is [\n" + responseMessage.getHeader() + "\n" +
        // responseMessage.getBodyAsString() + "]");

        success = true;
      } catch (Exception e)
      {
        // System.out.println("Error sending request:" + e.getMessage());

        if (retryConnection)
        {
          numberOfRetries++;
        }
        // close socket if an error occured
        close();
      } catch (OutOfMemoryError mem)
      {
        terminateClient = true;
        // close socket if an error occured
        close();
        logger.error("Out of memory reading input - giving up: " + mem);
      }
      if (!persistentConnection)
      {
        close();
      }
    }
    activeClients--;

    if (debug)
    {
      System.out.println("HTTPClient response header:" + responseMessage.getHeader());
      System.out.println("HTTPClient response body:" + StringHelper.byteArrayToUTF8String(responseMessage.getBody()));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#sendRequestAndReturnImmediately(int, java.lang.String,
   *      de.fhg.fokus.magic.http.HTTPMessageObject, int)
   */
  public void sendRequestAndReturnImmediately(HTTPMessageObject request)
  {
    if (request == null)
    {
      logger.error("no message to be sent");
      return;
    }

    responseMessage = new HTTPMessageObject(null, null, request.getDestinationAddress(), request.getSourceAddress());

    if (activeClients > HTTPDefaults.MAX_ACTIVE_CLIENTS)
    {
      System.out.println("Discard request due to too much stress");
      return;
    }
    activeClients++;

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
          logger.fatal("Communication with server failed", e);
        }
      } catch (OutOfMemoryError mem)
      {
        numberOfRetries = HTTPDefaults.MAX_CLIENT_FAILURES;
        logger.error("Out of memory reading input - giving up: " + mem);
      }
    }

    if (debug)
    {
      System.out.println("HTTPClient response header:" + responseMessage.getHeader());
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
      --activeClients;
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
        logger.error("Out of memory reading input - giving up: " + mem);
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
   *          Destination address
   * 
   * @throws IOException
   *           exception if connection is not possible
   * 
   */
  private void connect(InetSocketAddress destination) throws Exception
  {
    try
    {
      logger.debug(activeClients + " open client sockets");
      socket = new Socket();
      socket.connect(destination, 10000);
      socket.setSoTimeout(20);
    } catch (UnknownHostException e)
    {
      terminateClient = true;
      logger.fatal("unknown host = " + destination.getHostName());
      throw e;
    } catch (IOException e)
    {
      // we try again later
      logger.error("Could not open connection: " + e + ", host: " + IPHelper.toString(destination) + ", retrying");

      throw e;
    } catch (Exception e)
    {
      terminateClient = true;
      logger.error("Error in HTTP client: " + e.getMessage());
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
      // logger.debug("Send request [\n" + StringHelper.byteArrayToString(request) +
      // "]("+request.length+" bytes)\nsend to " + socket.getInetAddress());

      socket.getOutputStream().write(request);
    } catch (Exception e)
    {
      // we try again later
      logger.error("Error sending message to " + socket.getInetAddress(), e);
      throw e;
    }
  }

  /**
   * Retrieves the lock.
   * 
   * @return The lock
   */
  public Object getLock()
  {
    return lock;
  }

  /**
   * Closes the client socket.
   */
  private void close()
  {
    try
    {
      if (socket != null && socket.isConnected())
      {
        socket.close();
      }
    } catch (Exception io)
    {
      System.out.println("ERROR during socket close: " + io);
      logger.warn("Failed to close socket: " + io);
    }
    socket = null;
    // System.out.println("HTTPClient: Close");
  }

  /** Terminates the client */
  public void terminate()
  {
    terminateClient = true;
    close();
  }
}

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

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class represents a HTTP client that uses UDP as base protocol.
 * 
 * @author Alexander Koenig
 * 
 */
public class HTTPOverUDPClient implements IHTTPOverUDPClient
{
  /** HTTPclient logger */
  private final static Logger  logger          = Logger.getLogger("upnp.http");

  /** Flag for debug output */
  private final static boolean debug           = false;

  /** Number of currently active clients */
  private static int           activeClients;

  /** Socket used for the connection */
  private DatagramSocket       socket;

  /** Flag to use the socket just once */
  private boolean              oneTimeSocket;

  /** Response message */
  private HTTPMessageObject    responseMessage;

  private boolean              terminateClient = false;

  /**
   * Creates a new instance of HTTPOverUDPClient.
   * 
   */
  public HTTPOverUDPClient()
  {
    this(true);
  }

  /**
   * Creates a new instance of HTTPOverUDPClient.
   * 
   * @param oneTimeSocket
   */
  public HTTPOverUDPClient(boolean oneTimeSocket)
  {
    socket = null;
    this.oneTimeSocket = oneTimeSocket;
  }

  /**
   * Creates a new instance of HTTPOverUDPClient.
   * 
   * @param socket
   */
  public HTTPOverUDPClient(DatagramSocket socket)
  {
    this.socket = socket;
    oneTimeSocket = socket == null;
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
      logger.error("no message to be sent");
      return;
    }

    // cancel request if too many active clients
    if (activeClients > HTTPDefaults.MAX_ACTIVE_CLIENTS)
    {
      return;
    }
    activeClients++;

    int numberOfRetries = 0;
    boolean success = false;

    while (!success && !terminateClient && numberOfRetries < HTTPDefaults.MAX_CLIENT_FAILURES)
    {
      try
      {
        if (socket == null)
        {
          socket = new DatagramSocket();
        }
        SocketHelper.sendBinaryMessage(request.toBinaryMessage(), socket);

        // try to receive response for 2 seconds
        BinaryMessageObject binaryResponse = SocketHelper.readBinaryMessage(null, socket, 2000);

        if (binaryResponse != null)
        {
          responseMessage = binaryResponse.toHTTPMessageObject();
          responseMessage.setDestinationAddress(binaryResponse.getSourceAddress());
          responseMessage.setSourceAddress(request.getDestinationAddress());
        }
        success = true;
      } catch (Exception e)
      {
        numberOfRetries++;
      }
    }
    tryClose();
    activeClients--;

    if (debug)
    {
      System.out.println("HTTPClient response header:" + responseMessage.getHeader());
      System.out.println("HTTPClient response body:" + StringHelper.byteArrayToUTF8String(responseMessage.getBody()));
    }
  }

  /**
   * Sends a request which is not answered by the server. This method can be used for time-critical data
   */
  public void sendRequest(HTTPMessageObject request)
  {
    if (request == null)
    {
      logger.error("no message to be sent");
      return;
    }
    try
    {
      if (socket == null)
      {
        socket = new DatagramSocket();
      }
      SocketHelper.sendBinaryMessage(request.toBinaryMessage(), socket);
    } catch (Exception e)
    {
    } finally
    {
      tryClose();
    }
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
   * Closes the client socket.
   */
  private void tryClose()
  {
    if (oneTimeSocket)
    {
      try
      {
        if (socket != null)
        {
          socket.close();
        }
        socket = null;
      } catch (Exception io)
      {
      }
    }
  }

  /** Terminates the client */
  public void terminate()
  {
    terminateClient = true;
    tryClose();
  }
}

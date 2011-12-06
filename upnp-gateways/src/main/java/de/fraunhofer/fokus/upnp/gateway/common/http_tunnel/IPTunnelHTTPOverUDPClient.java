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

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocketFactory;

/**
 * This class represents a HTTPOverUDPClient for tunneled connections.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class IPTunnelHTTPOverUDPClient implements IHTTPOverUDPClient
{
  /** Associated IPTunnel factory */
  private IPTunnelSocketFactory  ipTunnelSocketFactory;

  /** Number of currently active clients */
  private static int             activeClients;

  /** Socket used for the connection */
  private IPTunnelDatagramSocket socket          = null;

  /** Flag to use the socket just once */
  private boolean                oneTimeSocket;

  /** Response header from this client */
  private HTTPMessageObject      responseMessage;

  private boolean                terminateClient = false;

  /** Creates a new IPTunnelHTTPClient */
  public IPTunnelHTTPOverUDPClient(IPTunnelSocketFactory ipTunnelSocketFactory)
  {
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    oneTimeSocket = true;
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
          socket = ipTunnelSocketFactory.createIPTunnelDatagramSocket();
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
    close();
    activeClients--;
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
  private void close()
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
    close();
  }

}

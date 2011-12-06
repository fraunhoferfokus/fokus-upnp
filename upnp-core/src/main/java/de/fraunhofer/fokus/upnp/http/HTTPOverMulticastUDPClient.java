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

import java.net.MulticastSocket;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class represents a HTTP client that uses UDP as base protocol.
 * 
 * @author Alexander Koenig
 * 
 */
public class HTTPOverMulticastUDPClient
{
  /** HTTPclient logger */
  private static Logger   logger = Logger.getLogger("upnp.http");

  /** Socket used for the connection */
  private MulticastSocket socket = null;

  /** Sync object */
  private Object          lock   = new Object();

  /**
   * Creates a new instance of HTTPOverMulticastUDPClient.
   * 
   */
  public HTTPOverMulticastUDPClient()
  {
    try
    {
      socket = new MulticastSocket();
    } catch (Exception e)
    {
      socket = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPClient#sendRequestAndWaitForResponse(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void sendRequest(HTTPMessageObject request)
  {
    if (request == null)
    {
      logger.error("No message to be sent");
      return;
    }
    if (socket == null)
    {
      logger.error("Invalid socket");
      return;
    }
    SocketHelper.sendBinaryMessage(request.toBinaryMessage(), socket);
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

  /** Terminates the client */
  public void terminate()
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

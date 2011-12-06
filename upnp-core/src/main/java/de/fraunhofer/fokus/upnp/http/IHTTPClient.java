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

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This is the interface for a HTTPClient for the UPnP stack.
 * 
 * @author Alexander Koenig
 * 
 */
public interface IHTTPClient
{

  /**
   * This method sends a request to a HTTP server and waits for the response.
   * 
   * @param request
   *          Request to be send
   */
  public void sendRequestAndWaitForResponse(HTTPMessageObject request);

  /**
   * This method sends a request to a HTTP server and returns after the header has been received.
   * All further received bytes can then be read with readConsecutiveResponseBody().
   * 
   * @param request
   *          Request to be send
   */
  public void sendRequestAndReturnImmediately(HTTPMessageObject request);

  /**
   * This method is used after a call to sendRequestAndReturnImmediately(). It returns either a full
   * buffer or all data read within timeout. If no more data is available, the socket is closed.
   * 
   * @return The number of bytes read or -1 if the end of the stream has been reached
   */
  public int readConsecutiveResponseBody(byte[] buffer, long timeout) throws Exception;

  /**
   * Retrieves the response message.
   * 
   * @return The response message received by the client
   */
  public HTTPMessageObject getResponse();

}

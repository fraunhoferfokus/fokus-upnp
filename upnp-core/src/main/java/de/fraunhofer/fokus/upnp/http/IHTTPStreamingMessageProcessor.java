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

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This interface must be implemented by classes that process HTTP messages and return the response
 * in several chunks.
 * 
 * @author tje, Alexander Koenig
 * @version 1.0
 */
public interface IHTTPStreamingMessageProcessor
{

  /**
   * This method receives and processes one HTTP request. Further response data is read with
   * readConsecutiveResponseBody().
   * 
   * @param serverAddress
   *          Socket address that received the packet
   * @param message
   *          The request
   * 
   * @return The beginning of the response of the processed message
   */
  public HTTPMessageObject processMessage(InetSocketAddress serverAddress, HTTPMessageObject request);

  /**
   * This method returns outstanding bytes for a request
   * 
   * @param buffer
   *          Buffer for the bytes
   * @param timeout
   *          Maximal wait time
   * 
   * @return The number of bytes returned. If -1 is returned, all response data was delivered.
   */
  public int readConsecutiveResponseBody(byte[] buffer, long timeout) throws Exception;

}

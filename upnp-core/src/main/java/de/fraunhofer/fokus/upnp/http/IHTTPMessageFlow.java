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

import java.net.URL;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class models the sending and receiving of one message via HTTP.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public interface IHTTPMessageFlow
{

  /**
   * Create a new request that should be send to a server.
   * 
   * @param messageOptions
   *          Message options
   * @param targetURL
   *          The target URL for the message
   * 
   * @return The created message
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL targetURL);

  /**
   * Modify a created request.
   * 
   * @param messageOptions
   *          Message options
   * @param targetURL
   *          The target URL for the message
   * @param request
   *          The request
   */
  public void modifyRequest(Hashtable messageOptions, URL targetURL, HTTPMessageObject request);

  /**
   * Creates the parser for the response message.
   * 
   * @return The created HTTPParser
   */
  public HTTPParser createResponseParser(Hashtable messageOptions, URL targetURL);

  /**
   * Process the response for a sent request.
   * 
   * @param messageOptions
   *          Message options
   * @param targetURL
   *          The target URL for the message
   * @param response
   *          The response message object
   * @param responseParser
   *          The parser for the response message object
   * 
   * @return An implementation-dependent object
   */
  public Object processResponse(Hashtable messageOptions,
    URL targetURL,
    HTTPMessageObject response,
    HTTPParser responseParser);

}

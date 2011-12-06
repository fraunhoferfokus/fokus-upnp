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

import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class can be used to create GET messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class HTTPMessageBuilder extends HTTPHeaderBuilder
{
  /**
   * HTTP logger
   */
  protected static Logger logger = Logger.getLogger("upnp.http");

  /**
   * Builds a GET message.
   * 
   * @param path
   *          Path for the request
   * @param host
   *          Host for the request
   * @param port
   *          Port for the request
   * @param preferredLanguage
   *          Preferred language
   * 
   * @return GET-Message
   * 
   */
  public static String createGETRequest(String path, String host, int port, String preferredLanguage)
  {
    return createGETRequestBuffer(path, host, port, preferredLanguage).toString();
  }

  /**
   * Builds a GET message.
   * 
   * @param path
   *          Path for the request
   * @param host
   *          Host for the request
   * @param port
   *          Port for the request
   * @param preferredLanguage
   *          Preferred language
   * 
   * @return GET-Message
   * 
   */
  public static StringBuffer createGETRequestBuffer(String path, String host, int port, String preferredLanguage)
  {
    StringBuffer message = new StringBuffer(256);

    if (port == -1)
    { // no port in url
      port = CommonConstants.HTTP_DEFAULT_PORT;
    }

    message.append(buildGET(path));
    message.append(buildHost(host + ':' + port));

    if (preferredLanguage != null && preferredLanguage.length() > 0)
    {
      message.append(buildAcceptLanguage(preferredLanguage));
    }

    // logger.debug("Create GET request [\n" + message.toString() + "]");

    return message;
  }

  /**
   * Builds a GET message.
   * 
   * @param path
   *          Path for the request
   * @param host
   *          Host for the request
   * @param port
   *          Port for the request
   * @param preferredLanguage
   *          Preferred language
   * 
   * @return GET-Message
   * 
   */
  public static String createHEADRequest(String path, String host, int port, String preferredLanguage)
  {
    StringBuffer message = new StringBuffer(256);

    if (port == -1)
    {
      // no port in url
      port = CommonConstants.HTTP_DEFAULT_PORT;
    }

    message.append(buildHEAD(path));
    message.append(buildHost(host + ':' + port));

    if (preferredLanguage != null && preferredLanguage.length() > 0)
    {
      message.append(buildAcceptLanguage(preferredLanguage));
    }

    // logger.debug("Create HEAD request [\n" + message.toString() + "]");

    return message.toString();
  }

  /**
   * Builds a response header for a GET request.
   * 
   * @param languageUsed
   *          Language used in messaging
   * @param bytesInBody
   *          lenght of following XML-Body
   * @param contentType
   *          what MIME-Type the Body is
   * @param Date
   *          Date when message was build
   * 
   * @return response for the GET request
   * 
   */
  public static String createGETorHEADResponse(boolean http_1_0,
    String languageUsed,
    String bytesInBody,
    String contentType,
    String Date,
    Vector optionalHeaderLines)
  {
    StringBuffer response = new StringBuffer(256);

    if (http_1_0)
    {
      response.append(buildHTTPOK_1_0());
    } else
    {
      response.append(buildHTTPOK());
    }
    response.append(buildContentLanguage(languageUsed));
    response.append(buildContentLength(bytesInBody));
    response.append(buildContentType(contentType));
    response.append(buildHeader(CommonConstants.ACCEPT_RANGES, "bytes"));
    response.append(buildDate(Date));
    for (int i = 0; optionalHeaderLines != null && i < optionalHeaderLines.size(); i++)
    {
      response.append(optionalHeaderLines.elementAt(i).toString());
    }

    // logger.debug("Create GET or HEAD response [\n" + response.toString() + "]");

    return response.toString();
  }

  /**
   * Builds a response header for a partial GET request.
   * 
   * @param languageUsed
   *          Language used in messaging
   * @param bytesInBody
   *          length of following XML-Body
   * @param contentType
   *          what MIME-Type the Body is
   * @param Date
   *          Date when message was build
   * @param contentRange
   *          Content-Range header value for partial GET response
   * 
   * @return response for the partial GET request
   * 
   */
  public static String createPartialGETResponse(String languageUsed,
    String bytesInBody,
    String contentType,
    String Date,
    String contentRange,
    Vector optionalHeaderLines)
  {
    StringBuffer response = new StringBuffer(256);

    response.append(CommonConstants.HTTP_1_1_206 + CommonConstants.NEW_LINE);
    response.append(buildContentLanguage(languageUsed));
    response.append(buildContentLength(bytesInBody));
    response.append(buildContentType(contentType));
    response.append(buildHeader(CommonConstants.ACCEPT_RANGES, "bytes"));
    response.append(buildHeader(CommonConstants.CONTENT_RANGE, contentRange));
    response.append(buildDate(Date));
    for (int i = 0; optionalHeaderLines != null && i < optionalHeaderLines.size(); i++)
    {
      response.append(optionalHeaderLines.elementAt(i).toString());
    }

    // logger.debug("Create partial GET response [\n" + response.toString() + "]");

    return response.toString();
  }

}

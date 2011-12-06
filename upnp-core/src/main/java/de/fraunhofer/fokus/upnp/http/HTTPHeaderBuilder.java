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

import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class provides methods for HTTP header line creation.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class HTTPHeaderBuilder
{

  /**
   * Builds a header line for a HTTP message.
   * 
   * @param tag
   *          Tag name
   * @param value
   *          Tag value
   * 
   * @return A new header line (tag: value\r\n) as string
   */
  public static String buildHeader(String tag, String value)
  {
    return tag + CommonConstants.BLANK + value + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for host header
   * @return host header as string
   */
  public static String buildHost(String input)
  {
    return CommonConstants.HOST + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for CacheControl header
   * @return CacheControl header as string
   */
  public static String buildCacheControl(String input)
  {
    return HTTPConstant.CACHE_CONTROL + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for location header
   * @return location header as string
   */
  public static String buildLocation(String input)
  {
    return CommonConstants.LOCATION + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for server header
   * @return server header as string
   */
  public static String buildServer(String input)
  {
    return HTTPConstant.SERVER + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for MAN header
   * @return MAN header as string
   */
  public static String buildMAN(String input)
  {
    return HTTPConstant.MAN + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for MX header
   * @return MX header as string
   */
  public static String buildMX(String input)
  {
    return HTTPConstant.MX + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @return the HTTPOK header
   */
  public static String buildHTTPOK()
  {
    return HTTPConstant.HTTP_OK_NL;
  }

  /**
   * @return the HTTPOK header
   */
  public static String buildHTTPOK_1_0()
  {
    return HTTPConstant.HTTP_OK_1_0_NL;
  }

  /**
   * @return the HTTPError412 header
   */
  public static String buildHTTPError412()
  {
    return HTTPConstant.HTTP_ERROR_412 + CommonConstants.NEW_LINE;
  }

  /**
   * @return the HTTP 404 Not Found header
   */
  public static String buildHTTPNotFound()
  {
    return HTTPConstant.HTTP_ERROR_404 + CommonConstants.NEW_LINE;
  }

  /**
   * @return the HTTPError5xx header
   */
  public static String buildHTTPError5xx()
  {
    return HTTPConstant.HTTP_ERROR_503 + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          paramter for MX header
   * @return MX header as string
   */
  public static String buildDate(String input)
  {
    return HTTPConstant.DATE + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @return EXT header as string
   */
  public static String buildEXT()
  {
    return HTTPConstant.EXT + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          paramter for GET header
   * @return GET header as string
   */
  public static String buildGET(String input)
  {
    return CommonConstants.GET + CommonConstants.BLANK + input + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * Builds a HEAD header
   * 
   * @param path
   *          Path for HEAD request
   * @return GET header as string
   */
  public static String buildHEAD(String path)
  {
    return CommonConstants.HEAD + CommonConstants.BLANK + path + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for AcceptLanguage header
   * @return AcceptLanguage header as string
   */
  public static String buildAcceptLanguage(String input)
  {
    return HTTPConstant.ACCEPT_LANGUAGE + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for ContentLanguage header
   * @return ContentLanguage header as string
   */
  public static String buildContentLanguage(String input)
  {
    return HTTPConstant.CONTENT_LANGUAGE + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for ContentLength header
   * @return ContentLength header as string
   */
  public static String buildContentLength(String input)
  {
    return CommonConstants.CONTENT_LENGTH + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for ContentType header
   * @return ContentType header as string
   */
  public static String buildContentType(String input)
  {
    return HTTPConstant.CONTENT_TYPE + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for POST header
   * @return POST header as string
   */
  public static String buildPOST(String input)
  {
    return HTTPConstant.POST + CommonConstants.BLANK + input + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for MPOST header
   * @return MPOST header as string
   */
  public static String buildMPOST(String input)
  {
    return HTTPConstant.M_POST + CommonConstants.BLANK + input + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for timeout header
   * @return timeout header as string
   */
  public static String buildTimeout(String input)
  {
    return HTTPConstant.TIMEOUT + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }
}

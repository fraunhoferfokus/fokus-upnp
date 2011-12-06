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

/**
 * This class provides constants for HTTP.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class HTTPConstant
{

  public static final String ACCEPT_LANGUAGE              = "ACCEPT-LANGUAGE:";

  public static final String CACHE_CONTROL                = "CACHE-CONTROL:";

  public static final String CHARSET                      = "charset=\"utf-8\"";

  public static final String CHUNKED                      = "CHUNKED";

  public static final String CONNECTION_HEADER            = "CONNECTION:";

  public static final String CONTENT_LANGUAGE             = "CONTENT-LANGUAGE:";

  public static final String CONTENT_TYPE                 = "CONTENT-TYPE:";

  public static final String CONTENT_TYPE_TEXT_CSS        = "text/css";

  public static final String CONTENT_TYPE_TEXT_HTML       = "text/html";

  public static final String CONTENT_TYPE_TEXT_PLAIN      = "text/plain";

  public static final String CONTENT_TYPE_TEXT_XML        = "text/xml";

  public static final String CONTENT_TYPE_TEXT_XML_UTF8   = "text/xml; charset=\"utf-8\"";

  public static final String DATE                         = "DATE:";

  /** Default language used in the transfer messages */
  public static final String DEFAULT_LANGUAGE             = "en";

  public static final String DOCTYPE_4_01                 =
                                                            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";

  public static final String ETAG_HEADER                  = "ETAG:";

  public static final String EXT                          = "EXT:";

  public static final String HEADER_INCLUDE_JAVA_SCRIPT   =
                                                            "<script src=\"scripts.js\" type=\"text/javascript\"></script>";

  public static final String HEADER_INCLUDE_STYLESHEET    =
                                                            "<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">";

  public static final String HTTP_ERROR_400               = "HTTP/1.1 400 Bad Request\r\n";

  public static final String HTTP_ERROR_401               = "HTTP/1.1 401 Authorization Required\r\n";

  public static final String HTTP_ERROR_404               = "HTTP/1.1 404 Not Found\r\n";

  public static final String HTTP_ERROR_405               = "HTTP/1.1 405 Method Not Allowed\r\n";

  public static final String HTTP_ERROR_412               = "HTTP/1.1 412 Precondition Failed\r\n";

  public static final String HTTP_ERROR_415               = "HTTP/1.1 415 Unsupported Media Type\r\n";

  public static final String HTTP_ERROR_500               = "HTTP/1.1 500 Internal Server Error";                                         // can

  public static final String HTTP_ERROR_503               = "HTTP/1.1 503 Service Unavailable\r\n";                                       // =>Unable

  public static final String HTTP_OK_1_0                  = "HTTP/1.0 200 OK";

  public static final String HTTP_OK_1_0_NL               = "HTTP/1.0 200 OK\r\n";

  public static final String HTTP_OK_1_1                  = "HTTP/1.1 200 OK";

  public static final String HTTP_OK_NL                   = "HTTP/1.1 200 OK\r\n";

  /** Base port to use for HTTP servers on random ports */
  public static final int    HTTP_RANDOM_PORT_BASE        = 20000;

  public static final String M_POST                       = "M-POST";

  public static final String MAN                          = "MAN:";

  public static final String MESSAGE_END                  = "";

  public static final String META_CONTENT_STYLE_TYPE      =
                                                            "<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">";

  public static final String META_CONTENT_TYPE_8859       =
                                                            "<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">";

  public static final String META_CONTENT_TYPE_UTF_8      =
                                                            "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\">";

  public static final int    MS_FACTOR                    = 1000;

  public static final String MX                           = "MX:";

  public static final String POST                         = "POST";

  public static final String SERVER                       = "SERVER:";

  public static final String TIMEOUT                      = "TIMEOUT:";

  public static final String TRANSFER_ENCODING            = "TRANSFER-ENCODING:";

  public static final String X_ENCRYPTION_TAG             = "X_ENCRYPTION:";

  public static final String X_ETAG                       = "X_ETAG";

  public static final String X_KEY_ID                     = "X_KEY_ID:";

  public static final String X_NONCE                      = "X_NONCE:";

  public static final String X_ORIGINATOR                 = "X_ORIGINATOR:";

  public static final String X_PERSONALIZATION_KEY_ID     = "X_PERSONALIZATION_KEY_ID:";

  public static final String X_PERSONALIZATION_PUBLIC_KEY = "X_PERSONALIZATION_PUBLIC_KEY:";

  public static final String X_PERSONALIZATION_SEQUENCE   = "X_PERSONALIZATION_SEQUENCE:";

  public static final String X_PERSONALIZATION_SIGNATURE  = "X_PERSONALIZATION_SIGNATURE:";

  public static final String X_PUBLIC_KEY                 = "X_PUBLIC_KEY:";

  public static final String X_SEQUENCE                   = "X_SEQUENCE:";

  public static final String X_SIGNATURE                  = "X_SIGNATURE:";

  public static final String X_SYMMETRIC_KEY              = "X_SYMMETRIC_KEY:";

  public static final String X_SYMMETRIC_KEY_SIGNATURE    = "X_SYMMETRIC_KEY_SIGNATURE:";

  public static final String XML_8859                     = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>";

  public static final String XML_UTF_8                    = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

}

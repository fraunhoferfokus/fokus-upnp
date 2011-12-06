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
package de.fraunhofer.fokus.upnp.util;

/**
 * This class holds all constants that are also used by utility classes.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class CommonConstants
{

  // HTTP constants
  public static final String ACCEPT_RANGES         = "ACCEPT-RANGES:";

  public static final String BLANK                 = " ";

  public static final String CONTENT_LENGTH        = "CONTENT-LENGTH:";

  public static final String CONTENT_RANGE         = "CONTENT-RANGE:";

  public static final String GET                   = "GET";

  public static final String HEAD                  = "HEAD";

  public static final String HTTP_1_x              = "HTTP/1.";

  public static final String HTTP_1_1              = "HTTP/1.1";

  public static final String HTTP_1_0              = "HTTP/1.0";

  public static final String HTTP_1_1_206          = "HTTP/1.1 206 Partial Content";

  public static final String HTTP_OK               = "HTTP/1.1 200 OK";

  public static final String RANGE                 = "RANGE:";

  /** HTTP default port (80) */
  public static final int    HTTP_DEFAULT_PORT     = 80;

  /** Default size for HTTP message input buffers (16384) */
  public static final int    HTTP_BUFFER_READ_SIZE = 16384;

  public static final String HOST                  = "HOST:";

  /** HTTP new line (\r\n) */
  public static final String NEW_LINE              = "\r\n";

  // Discovery constants
  public static final String INITIAL_EVENT         = "X-INITIAL-EVENT";

  public static final String LOCATION              = "LOCATION:";

  public static final String M_SEARCH              = "M-SEARCH";

  public static final String NOTIFY                = "NOTIFY";

  public static final String USN                   = "USN:";

  // Eventing constants
  public static final String CALLBACK              = "CALLBACK:";

  public static final String SUBSCRIBE             = "SUBSCRIBE";

  public static final String UDP_CALLBACK          = "X_UDP_CALLBACK:";

  public static final String UNSUBSCRIBE           = "UNSUBSCRIBE";

  // UPnP AV Constants
  public final static String FILE_EXTENSIONS_AUDIO = "mp3;wma;ogg;wav";

  public final static String FILE_EXTENSIONS_VIDEO = "mpeg;mpg;avi;mp4;asf";

  public final static String FILE_EXTENSIONS_IMAGE = "jpg;jpeg;png;bmp;gif;ico";

  // Security constants */
  /** SHA-1 */
  public static final String SHA_1_JAVA            = "SHA-1";

  /** SHA1 */
  public static final String SHA_1_UPNP            = "SHA1";

  /** HmacSHA1 */
  public static final String HMAC_SHA_1_JAVA       = "HmacSHA1";

  /** SHA1withRSA */
  public static final String RSA_SHA_1_JAVA        = "SHA1withRSA";

  /** AES/CBC/NoPadding */
  public static final String AES_128_CBC_JAVA      = "AES/CBC/NoPadding";

  public static final String EXTENSION_JPG         = "jpg";

  public static final String EXTENSION_MP3         = "mp3";

  public static final String EXTENSION_MPG         = "mpg";

}

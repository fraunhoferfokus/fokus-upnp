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
package de.fraunhofer.fokus.upnp.core_security.helpers;

import java.net.URL;

import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.URLHelper;

/**
 * This class can be used to create GET messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class HTTPSecuredMessageBuilder extends HTTPMessageBuilder
{

  /**
   * Builds a signed GET message.
   * 
   * @param nonce
   *          Nonce for signature
   * @param publicKey
   *          XML description of control points public key
   * @param signature
   *          Base64 encoded signature over nonce, callback URLs and public key
   * 
   * @return GET message
   */
  public static String createPublicKeySignedGETRequest(URL descriptionURL,
    String nonce,
    String publicKey,
    String signature)
  {
    StringBuffer result =
      createGETRequestBuffer(URLHelper.getURLPath(descriptionURL),
        descriptionURL.getHost(),
        descriptionURL.getPort(),
        "");

    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_NONCE, nonce));
    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PUBLIC_KEY, publicKey));
    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_SIGNATURE, signature));

    // System.out.println("Signed GET request is [\n" + result.toString() + "]");

    return result.toString();
  }

  /**
   * Builds a signed GET message.
   * 
   * @param sequence
   *          Sequence number for this request
   * @param keyName
   *          Name for symmetric key
   * @param signature
   *          Base64 encoded signature over nonce, callback URLs and public key
   * 
   * @return GET message
   */
  public static String createSymmetricKeySignedGETRequest(URL descriptionURL,
    String sequence,
    String keyName,
    String signature)
  {
    StringBuffer result =
      createGETRequestBuffer(URLHelper.getURLPath(descriptionURL),
        descriptionURL.getHost(),
        descriptionURL.getPort(),
        "");

    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_SEQUENCE, sequence));
    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_KEY_ID, keyName));
    result.append(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_SIGNATURE, signature));

    return result.toString();
  }

}

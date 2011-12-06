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

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.soap.SOAPConstant;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SignatureHelper;

/**
 * This class is used to build secured SOAP messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SOAPSecuredMessageBuilder extends SOAPMessageBuilder
{

  /** SOAP logger */
  private static Logger logger = Logger.getLogger("upnp");

  /**
   * method builds the body of a RSA signed soap message if the content between the <Body>-Tags is
   * given
   * 
   * @param controlURL
   *          target URL for this action
   * @param lifetimeSequenceBase
   *          device specific information for replay prevention
   * @param signingKey
   *          private key of the caller for signing
   * @param publicKey
   *          public key of the caller for verification
   * @param action
   *          content between <Body>-Tags
   * @return body of signed SoapMessage
   */
  public static String buildRSASignedEnvelopeToBodyWrap(String controlUrl,
    String lifetimeSequenceBase,
    RSAPrivateKey signingKey,
    RSAPublicKey publicKey,
    String action)
  {
    StringBuffer body = new StringBuffer();
    body.append(SOAPConstant.ENVELOPE_START_TAG + CommonConstants.NEW_LINE);

    // build header for signing
    StringBuffer soapHeader = new StringBuffer();

    // build freshness block
    String freshnessBody = ElementBuilder.buildFreshnessBody(lifetimeSequenceBase, controlUrl);

    // calculate hash for freshness block
    String freshnessHashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(freshnessBody));

    String freshnessDescription = ElementBuilder.buildFreshnessXMLDescription(freshnessBody);

    // calculate hash for soap body
    // TO-DO: c14n canonicalization
    String canonalizedBody = action;

    String bodyHashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(canonalizedBody));

    String[] digestIDs = {
        "Freshness", "Body"
    };
    String[] digestValues = {
        freshnessHashBase64, bodyHashBase64
    };

    // calculate signature
    String signature = SignatureHelper.createRSASignature(digestIDs, digestValues, signingKey, publicKey);

    soapHeader.append(SOAPConstant.HEADER_START_TAG + CommonConstants.NEW_LINE);
    soapHeader.append(CommonSecurityConstant.SECURITY_INFO_TAG);
    soapHeader.append(freshnessDescription);
    soapHeader.append(signature);
    soapHeader.append(CommonSecurityConstant.SECURITY_INFO_END_TAG + CommonConstants.NEW_LINE);
    soapHeader.append(SOAPConstant.HEADER_END_TAG + CommonConstants.NEW_LINE);

    body.append(soapHeader.toString());
    body.append(SOAPConstant.BODY_SIGNED_START_TAG);
    body.append(action);
    body.append(SOAPConstant.BODY_END_TAG + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.ENVELOPE_END_TAG);
    // logger.debug(body.toString());

    return body.toString();
  }

  /**
   * method builds the body of a SHA1 HMAC signed soap message if the content between the
   * <Body>-Tags is given
   * 
   * @param controlURL
   *          target URL for this action
   * @param sequenceBase
   *          session specific information for replay prevention
   * @param sequenceNumber
   *          session specific information for replay prevention
   * @param signatureKey
   *          session key of the caller for signing
   * @param keyID
   *          session ID
   * @param action
   *          content between <Body>-Tags
   * @return body of signed SoapMessage
   */
  public static String buildSHA1HMACSignedEnvelopeToBodyWrap(String controlUrl,
    String sequenceBase,
    String sequenceNumber,
    Key signatureKey,
    int keyID,
    String action)
  {
    StringBuffer body = new StringBuffer();
    body.append(SOAPConstant.ENVELOPE_START_TAG + CommonConstants.NEW_LINE);

    // build header for signing
    StringBuffer soapHeader = new StringBuffer();

    // build session freshness block
    String freshnessBody = ElementBuilder.buildFreshnessBody(sequenceBase, sequenceNumber, controlUrl);

    // calculate hash for freshness block
    String freshnessHashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(freshnessBody));

    String freshnessDescription = ElementBuilder.buildFreshnessXMLDescription(freshnessBody);

    // calculate hash for soap body
    // TO-DO: c14n canonicalization
    String canonalizedBody = action;

    String bodyHashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(canonalizedBody));

    String[] digestIDs = {
        "Freshness", "Body"
    };
    String[] digestValues = {
        freshnessHashBase64, bodyHashBase64
    };

    // calculate SHA1 HMAC signature
    String signature = SignatureHelper.createSHA1HMACSignature(digestIDs, digestValues, signatureKey, keyID);

    soapHeader.append(SOAPConstant.HEADER_START_TAG + CommonConstants.NEW_LINE);
    soapHeader.append(CommonSecurityConstant.SECURITY_INFO_TAG);
    soapHeader.append(freshnessDescription);
    soapHeader.append(signature);
    soapHeader.append(CommonSecurityConstant.SECURITY_INFO_END_TAG + CommonConstants.NEW_LINE);
    soapHeader.append(SOAPConstant.HEADER_END_TAG + CommonConstants.NEW_LINE);

    body.append(soapHeader.toString());
    body.append(SOAPConstant.BODY_SIGNED_START_TAG);
    body.append(action);
    body.append(SOAPConstant.BODY_END_TAG + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.ENVELOPE_END_TAG);
    logger.debug(body.toString());

    return body.toString();
  }

}

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
package de.fraunhofer.fokus.upnp.core_security.control_point;

import java.net.URL;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPDeviceDescriptionRetrieval;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPointHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.helpers.HTTPSecuredMessageBuilder;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPMessageFlow;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class is responsible for retrieving device and service descriptions.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityAwareCPDeviceDescriptionRetrieval extends CPDeviceDescriptionRetrieval
{

  private SecurityAwareTemplateControlPoint securityAwareControlPoint;

  private static String                     SIGNATURE_TYPE_TAG    = "SignatureType";

  private static String                     SYMMETRIC_KEY_MESSAGE = "Symmetric";

  private static String                     PUBLIC_KEY_MESSAGE    = "Public";

  private static String                     UNSIGNED_MESSAGE      = "Unsigned";

  /** Symmetric keys used for security */
  private SymmetricKeyInfo                  securitySymmetricKeyInfo;

  /**
   * Creates a new instance of SecurityAwareCPDeviceDescriptionRetrieval.
   * 
   * @param controlPoint
   *          Associated control point
   * @param socketStructure
   *          Socket structure that received the discovery message
   * @param deviceDescriptionURL
   *          URL where to find device description
   * @param server
   *          server
   * @param maxage
   *          maxage
   * @param NLS
   *          network location signature
   * @param IPVersion
   *          Used IP version (4 or 6)
   */
  public SecurityAwareCPDeviceDescriptionRetrieval(SecurityAwareTemplateControlPoint securityAwareControlPoint,
    ControlPointHostAddressSocketStructure socketStructure,
    URL deviceDescriptionURL,
    String uuid,
    String server,
    int maxage,
    String NLS,
    int IPVersion)
  {
    super(securityAwareControlPoint.getBasicControlPoint(),
      socketStructure,
      deviceDescriptionURL,
      uuid,
      server,
      maxage,
      NLS,
      IPVersion);

    this.securityAwareControlPoint = securityAwareControlPoint;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPDeviceDescriptionRetrieval#getDescription(java.net.URL)
   */
  protected String getDescription(URL descriptionURL)
  {
    Hashtable messageOptions = new Hashtable();

    // check if symmetric key access is possible
    if (securitySymmetricKeyInfo != null)
    {
      // serialize access to sequence
      synchronized(securitySymmetricKeyInfo.getLock())
      {
        // try to sign with symmetric key
        messageOptions.put(SIGNATURE_TYPE_TAG, SYMMETRIC_KEY_MESSAGE);
        Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, descriptionURL, this);
        // return result if valid
        if (result instanceof String)
        {
          return (String)result;
        }
      }
    }
    // symmetric key failed, try normal request
    messageOptions.put(SIGNATURE_TYPE_TAG, UNSIGNED_MESSAGE);
    Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, descriptionURL, this);
    // return result if valid
    if (result instanceof String)
    {
      return (String)result;
    }

    // check for authentication failure
    if (result instanceof HTTPParser)
    {
      HTTPParser responseParser = (HTTPParser)result;
      if (responseParser.getResponseCode() == 401 && responseParser.hasField(HTTPConstant.X_NONCE))
      {
        // try public key signed request
        messageOptions.put(SIGNATURE_TYPE_TAG, PUBLIC_KEY_MESSAGE);
        messageOptions.put(HTTPConstant.X_NONCE, responseParser.getValue(HTTPConstant.X_NONCE));
        result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, descriptionURL, this);
        if (result instanceof String)
        {
          return (String)result;
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createRequest(java.lang.Object, java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL descriptionURL)
  {
    String signatureType = (String)messageOptions.get(SIGNATURE_TYPE_TAG);
    if (signatureType.equals(SYMMETRIC_KEY_MESSAGE) && securitySymmetricKeyInfo != null)
    {
      System.out.println("Create GET request signed with symmetric key");
      securitySymmetricKeyInfo.incSequence();

      // build content for signature
      String signatureContent = securitySymmetricKeyInfo.getKeyID();
      signatureContent += securitySymmetricKeyInfo.getSequence() + "";

      byte[] signature =
        DigestHelper.calculateSHA1HMACForString(securitySymmetricKeyInfo.getAESKey(), signatureContent);
      String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

      return new HTTPMessageObject(HTTPSecuredMessageBuilder.createSymmetricKeySignedGETRequest(descriptionURL,
        securitySymmetricKeyInfo.getSequence() + "",
        securitySymmetricKeyInfo.getKeyID(),
        signatureBase64), descriptionURL);
    }
    if (signatureType.equals(PUBLIC_KEY_MESSAGE))
    {
      System.out.println("Create GET request signed with public key");
      String publicKey = SecurityHelper.buildRSAPublicKeyXMLDescription(securityAwareControlPoint.getPublicKey());
      String nonce = (String)messageOptions.get(HTTPConstant.X_NONCE);

      // build content for signature
      String signatureContent = nonce;
      signatureContent += publicKey;

      byte[] signature =
        PublicKeyCryptographyHelper.calculateRSASignatureForString(securityAwareControlPoint.getPrivateKey(),
          signatureContent);

      String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

      return new HTTPMessageObject(HTTPSecuredMessageBuilder.createPublicKeySignedGETRequest(descriptionURL,
        nonce,
        publicKey,
        signatureBase64), descriptionURL);
    }

    if (signatureType.equals(UNSIGNED_MESSAGE))
    {
      return super.createRequest(messageOptions, descriptionURL);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPDeviceDescriptionRetrieval#processResponse(java.util.Hashtable,
   *      java.net.URL, de.fhg.fokus.magic.util.network.HTTPMessageObject,
   *      de.fhg.fokus.magic.http.HTTPParser)
   */
  public Object processResponse(Hashtable messageOptions,
    URL descriptionURL,
    HTTPMessageObject response,
    HTTPParser responseParser)
  {
    SymmetricKeyInfo keyInfo =
      SecuredMessageHelper.tryParseSymmetricKeyInfo((String)messageOptions.get(HTTPConstant.X_NONCE),
        securityAwareControlPoint.getPrivateKey(),
        responseParser);

    if (keyInfo != null)
    {
      securitySymmetricKeyInfo = keyInfo;
    }

    // check if the response is encrypted
    SecuredMessageHelper.tryDecryptMessageBody(response, responseParser, securitySymmetricKeyInfo);

    // build signature base
    String signatureContent = response.getBodyAsUTF8String();
    if (securitySymmetricKeyInfo != null)
    {
      signatureContent += securitySymmetricKeyInfo.getKeyID();
      signatureContent += securitySymmetricKeyInfo.getSequence();
    }
    // check if the response is signed
    if (!SecuredMessageHelper.tryVerifySignature(response, responseParser, securitySymmetricKeyInfo, signatureContent))
    {
      System.out.println("Received signed description with invalid signature");
      return null;
    }
    return super.processResponse(messageOptions, descriptionURL, response, responseParser);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPDeviceDescriptionRetrieval#newRootDevice(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  protected void newRootDevice(CPDevice device)
  {
    if (securitySymmetricKeyInfo != null)
    {
      securityAwareControlPoint.getSymmetricKeyInfoFromDeviceTable().put(device, securitySymmetricKeyInfo);
    }

  }

}

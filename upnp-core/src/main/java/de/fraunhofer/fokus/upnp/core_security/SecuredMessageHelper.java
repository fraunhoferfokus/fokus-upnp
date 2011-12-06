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
package de.fraunhofer.fokus.upnp.core_security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core_security.device.SecurityAwareControlPointObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.PublicKeysParser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class is responsible for retrieving device and service descriptions.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredMessageHelper
{

  /**
   * Try to parse an optional session key contained in the HTTP header.
   */
  public static SymmetricKeyInfo tryParseSymmetricKeyInfo(String nonce,
    RSAPrivateKey controlPointPrivateKey,
    HTTPParser responseParser)
  {
    // check response for symmetric key and sequence
    if (responseParser.hasField(HTTPConstant.X_SYMMETRIC_KEY) && responseParser.hasField(HTTPConstant.X_SEQUENCE) &&
      responseParser.hasField(HTTPConstant.X_SYMMETRIC_KEY_SIGNATURE) && responseParser.hasField(HTTPConstant.X_KEY_ID))
    {
      System.out.println("  Received response message with an encrypted symmetric key");
      String encryptedKeyBase64 = responseParser.getValue(HTTPConstant.X_SYMMETRIC_KEY);

      // check signature of received symmetric key
      String receivedSymmetricKeySignature = responseParser.getValue(HTTPConstant.X_SYMMETRIC_KEY_SIGNATURE);
      String receivedDevicePublicKey = responseParser.getValue(HTTPConstant.X_PUBLIC_KEY);

      // verify signature
      String signatureContent = nonce;
      signatureContent += encryptedKeyBase64;
      signatureContent += receivedDevicePublicKey;

      PublicKeysParser parser = new PublicKeysParser(receivedDevicePublicKey);
      RSAPublicKey devicePublicKey = parser.getPublicKey();

      boolean signatureValid =
        PublicKeyCryptographyHelper.verifyRSASignatureForString(devicePublicKey,
          signatureContent,
          receivedSymmetricKeySignature);

      if (signatureValid)
      {
        byte[] encryptedKey = Base64Helper.base64ToByteArray(encryptedKeyBase64);
        SymmetricKeyInfo symmetricKeyInfo =
          SymmetricCryptographyHelper.decryptRSAEncryptedAESKey(controlPointPrivateKey, encryptedKey);

        symmetricKeyInfo.setSequence(responseParser.getNumericValue(HTTPConstant.X_SEQUENCE));
        symmetricKeyInfo.setKeyID(responseParser.getValue(HTTPConstant.X_KEY_ID));

        return symmetricKeyInfo;
      } else
      {
        System.out.println("Discard received symmetric key due to an invalid signature");
      }
    }
    return null;
  }

  /**
   * Try to decrypt the body of a message.
   * 
   * @param message
   * @param messageParser
   * @param securitySymmetricKeyInfo
   */
  public static void tryDecryptMessageBody(HTTPMessageObject message,
    HTTPParser messageParser,
    SymmetricKeyInfo securitySymmetricKeyInfo)
  {
    // check if the response is encrypted
    if (messageParser.getBooleanValue(HTTPConstant.X_ENCRYPTION_TAG) && messageParser.hasField(HTTPConstant.X_KEY_ID) &&
      securitySymmetricKeyInfo != null &&
      securitySymmetricKeyInfo.getKeyID().equals(messageParser.getValue(HTTPConstant.X_KEY_ID)))
    {
      System.out.println("  Received encrypted message");
      byte[] encryptedBody = message.getBody();
      byte[] decryptedBody =
        SymmetricCryptographyHelper.decryptWithAES(securitySymmetricKeyInfo.getAESKey(),
          securitySymmetricKeyInfo.getIV(),
          encryptedBody);

      // System.out.println("Decrypted description is [\n" +
      // StringHelper.byteArrayToUTF8String(decryptedBody) + "]");

      // update header
      String header = message.getHeader();
      // remove encryption tag
      header = HTTPMessageHelper.removeHeaderLine(header, HTTPConstant.X_ENCRYPTION_TAG);
      // change length
      header = HTTPMessageHelper.replaceContentLength(header, decryptedBody.length);

      message.setHeader(header);
      message.setBody(decryptedBody);
    }
  }

  public static boolean tryVerifySignature(HTTPMessageObject message,
    HTTPParser messageParser,
    SymmetricKeyInfo securitySymmetricKeyInfo,
    String signatureContent)
  {
    // check if the response is signed
    if (messageParser.hasField(HTTPConstant.X_SIGNATURE) && messageParser.hasField(HTTPConstant.X_KEY_ID) &&
      securitySymmetricKeyInfo != null &&
      securitySymmetricKeyInfo.getKeyID().equals(messageParser.getValue(HTTPConstant.X_KEY_ID)))
    {
      System.out.println("  Received signed message");

      byte[] signature =
        DigestHelper.calculateSHA1HMACForString(securitySymmetricKeyInfo.getAESKey(), signatureContent);

      String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

      if (!signatureBase64.equals(HTTPMessageHelper.getHeaderLine(message.getHeader(), HTTPConstant.X_SIGNATURE)))
      {
        System.out.println("Received signed message with invalid signature");
        return false;
      }
    }
    return true;
  }

  /**
   * Adds an encrypted symmetric key to a response message if the request was signed with a public
   * key and a symmetric key is known.
   * 
   * @param requestParser
   * @param device
   * @param response
   */
  public static void tryAddSymmetricKeyToResponseMessage(HTTPParser requestParser,
    Device device,
    HTTPMessageObject response)
  {
    // if this message was signed by a public key, add the
    // already created symmetric key to the response message
    if (response != null && device instanceof SecuredTemplateDevice &&
      requestParser.hasField(HTTPConstant.X_SIGNATURE) && requestParser.hasField(HTTPConstant.X_NONCE) &&
      requestParser.hasField(HTTPConstant.X_PUBLIC_KEY))
    {
      // System.out.println("Add symmetric encryption data to response message");
      SecuredTemplateDevice securedDevice = (SecuredTemplateDevice)device;

      RSAPublicKey publicKey = requestParser.getPublicKey(HTTPConstant.X_PUBLIC_KEY);
      SecurityAwareControlPointObject controlPointObject = null;
      if (publicKey != null)
      {
        controlPointObject = securedDevice.getControlPointObject(publicKey);
      }
      if (controlPointObject != null)
      {
        String symmetricKeyBase64 =
          SymmetricCryptographyHelper.encryptAESKeyWithRSA(controlPointObject.getSymmetricKey().getEncoded(),
            controlPointObject.getSymmetricIV(),
            publicKey);

        // the symmetric key must be signed with the private key of the device
        // to provide authenticity for the device
        // build content for signature
        String devicePublicKey = SecurityHelper.buildRSAPublicKeyXMLDescription(securedDevice.getPublicKey());

        String signatureContent = requestParser.getValue(HTTPConstant.X_NONCE);
        signatureContent += symmetricKeyBase64;
        signatureContent += devicePublicKey;

        byte[] signature =
          PublicKeyCryptographyHelper.calculateRSASignatureForString(securedDevice.getPrivateKey(), signatureContent);

        String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

        String header = response.getHeader();
        // add encrypted symmetric key
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SYMMETRIC_KEY, symmetricKeyBase64);
        // add sequence base
        header =
          HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SEQUENCE, controlPointObject.getSymmetricSequence() +
            "");
        // add key name
        header =
          HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_KEY_ID, controlPointObject.getSymmetricKeyName());
        // add device public key
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_PUBLIC_KEY, devicePublicKey);
        // add signature for symmetric key
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SYMMETRIC_KEY_SIGNATURE, signatureBase64);

        response.setHeader(header);
      }
    }
  }

  /**
   * Signs and/or encrypts a response message object.
   * 
   * 
   * @param responseMessage
   *          The response message
   * @param symmetricKeyInfo
   *          The key to use for encryption and signature
   * @param signatureContent
   *          Content that should be signed
   * @param encryptBody
   *          Flag to encrypt the body
   * 
   */
  public static void trySecureResponseMessage(HTTPMessageObject responseMessage,
    SymmetricKeyInfo symmetricKeyInfo,
    String signatureContent,
    boolean encryptBody)
  {
    // check for all needed security related data
    if (responseMessage != null && symmetricKeyInfo != null)
    {
      // check for encrypted responses
      if (encryptBody)
      {
        // System.out.println("Encrypt response body");
        byte[] encryptedBody =
          SymmetricCryptographyHelper.encryptWithAES(symmetricKeyInfo.getAESKey(),
            symmetricKeyInfo.getIV(),
            responseMessage.getBody());

        // update header
        String header = responseMessage.getHeader();
        // add encryption tag
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_ENCRYPTION_TAG, "true");
        // change length
        header = HTTPMessageHelper.replaceContentLength(header, encryptedBody.length);

        responseMessage.setHeader(header);
        responseMessage.setBody(encryptedBody);
      }
      // System.out.println("Sign response body");
      // we sign all responses with a symmetric key
      byte[] signature = DigestHelper.calculateSHA1HMACForString(symmetricKeyInfo.getAESKey(), signatureContent);

      String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

      // update header
      String header = responseMessage.getHeader();
      // add key name
      if (HTTPMessageHelper.getHeaderLine(header, HTTPConstant.X_KEY_ID) == null)
      {
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_KEY_ID, symmetricKeyInfo.getKeyID());
      }
      // add signature
      header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SIGNATURE, signatureBase64);

      responseMessage.setHeader(header);
    }
  }
}

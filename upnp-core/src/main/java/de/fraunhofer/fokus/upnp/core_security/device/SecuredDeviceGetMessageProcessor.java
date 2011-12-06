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
package de.fraunhofer.fokus.upnp.core_security.device;

import java.net.InetSocketAddress;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceGetMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;

/**
 * This class handles the processing and response of a secured GET or HEAD message to retrieve UPnP
 * XML descriptions or resources.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredDeviceGetMessageProcessor extends DeviceGetMessageProcessor
{

  /**
   * Retrieve the control point object associated with a request, either by a public or symmetric
   * key.
   * 
   * @param securedDevice
   * @param parser
   * @return
   */
  public static SecurityAwareControlPointObject getSecurityAwareControlPointObject(SecuredTemplateDevice securedDevice,
    HTTPParser parser)
  {
    if (securedDevice == null)
    {
      return null;
    }

    SecurityAwareControlPointObject result = null;
    // check symmetric key ID
    String receivedKeyName = parser.getValue(HTTPConstant.X_KEY_ID);
    if (receivedKeyName != null)
    {
      result = securedDevice.getControlPointObjectByKeyName(receivedKeyName);
    }
    if (result != null)
    {
      return result;
    }

    // check public key
    RSAPublicKey publicKey = parser.getPublicKey(HTTPConstant.X_PUBLIC_KEY);
    if (publicKey != null)
    {
      result = securedDevice.getControlPointObject(publicKey);
    }
    return result;
  }

  /**
   * Processes a GET or HEAD request.
   * 
   * @param requestParser
   *          Associated parser
   * @param device
   *          Device that received the request
   * 
   * @return Response message object (HTTP error or requested data)
   * 
   */
  public static HTTPMessageObject processMessage(HTTPParser requestParser, Device device)
  {
    InetSocketAddress serverAddress = requestParser.getHTTPMessageObject().getDestinationAddress();

    // check for needed signature
    HTTPMessageObject securityErrorMessage = checkForSignature(requestParser, device, serverAddress);
    if (securityErrorMessage != null)
    {
      logger.info("GET or HEAD is ignored due to missing or invalid signature");
      return securityErrorMessage;
    }

    // signature is valid, use normal handling
    HTTPMessageObject result = DeviceGetMessageProcessor.processMessage(requestParser, device);

    // if this message was signed by a public key, add the encrypted symmetric key to the response
    // message
    SecuredMessageHelper.tryAddSymmetricKeyToResponseMessage(requestParser, device, result);

    // retrieve the symmetric key associated with this control point
    SecuredTemplateDevice securedDevice = null;
    if (device instanceof SecuredTemplateDevice)
    {
      securedDevice = (SecuredTemplateDevice)device;
    }

    SecurityAwareControlPointObject controlPointObject =
      getSecurityAwareControlPointObject(securedDevice, requestParser);

    // check for all needed security related data
    if (result != null && securedDevice != null && controlPointObject != null)
    {
      // always use unencrypted body as base for signature
      String signatureContent = result.getBodyAsUTF8String();
      // we sign all responses with the symmetric key
      signatureContent += controlPointObject.getSymmetricKeyName();
      signatureContent += controlPointObject.getSymmetricSequence() + "";

      SecuredMessageHelper.trySecureResponseMessage(result,
        controlPointObject.getSymmetricKeyInfo(),
        signatureContent,
        securedDevice.needsEncryptedDescriptionResponses());

      return result;
    }
    return result;
  }

  /**
   * Checks GET or HEAD messages for signatures. If the signature is missing or invalid, an error
   * response message is generated.
   */
  private static HTTPMessageObject checkForSignature(HTTPParser requestParser,
    Device device,
    InetSocketAddress serverAddress)
  {
    // check for signature
    if (device instanceof SecuredTemplateDevice)
    {
      SecuredTemplateDevice securedDevice = (SecuredTemplateDevice)device;

      // use signed GET requests if the device needs signed description requests
      if (requestParser.isGETRequest() && securedDevice.needsSignedDescriptionRequests())
      {
        // System.out.println("Accept only signed description requests for " +
        // securedDevice.getFriendlyName());

        if (!requestParser.hasField(HTTPConstant.X_SIGNATURE))
        {
          // System.out.println("Missing signature in GET request, return nonce");
          // create and store nonce
          String nonce = securedDevice.createAndStoreNonce();

          String responseHeader = HTTPConstant.HTTP_ERROR_401;

          responseHeader += HTTPConstant.X_NONCE + CommonConstants.BLANK + nonce + CommonConstants.NEW_LINE;

          return new HTTPMessageObject(responseHeader, serverAddress);
        }
        // try to verify signature
        boolean validSignature = verifySignature(requestParser, securedDevice);

        if (!validSignature)
        {
          System.out.println("Invalid signature");
          return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
        }
        // System.out.println("Received secured GET or HEAD with valid signature");
      }
    }
    return null;
  }

  /** Verifies a received signature for a GET or HEAD message. */
  private static boolean verifySignature(HTTPParser requestParser, SecuredTemplateDevice securedDevice)
  {
    String receivedSignature = requestParser.getValue(HTTPConstant.X_SIGNATURE);
    String receivedNonce = requestParser.getValue(HTTPConstant.X_NONCE);
    String receivedPublicKey = requestParser.getValue(HTTPConstant.X_PUBLIC_KEY);
    String receivedSequence = requestParser.getValue(HTTPConstant.X_SEQUENCE);
    String receivedKeyName = requestParser.getValue(HTTPConstant.X_KEY_ID);

    // System.out.println("Verify signature for request [\n" +
    // requestParser.getHTTPMessageObject().getHeader() + "]");
    // check public key signature
    if (receivedSignature != null && receivedNonce != null && receivedPublicKey != null)
    {
      // check nonce for validness
      if (!securedDevice.checkAndRemoveNonce(receivedNonce))
      {
        logger.warn("Nonce was invalid");
        return false;
      }
      // signature was built over nonce and public key
      String signatureContent = receivedNonce;
      signatureContent += receivedPublicKey;

      RSAPublicKey publicKey = requestParser.getPublicKey(HTTPConstant.X_PUBLIC_KEY);

      if (publicKey == null)
      {
        logger.warn("Could not parse public key");
        return false;
      }
      boolean signatureValid =
        PublicKeyCryptographyHelper.verifyRSASignatureForString(publicKey, signatureContent, receivedSignature);

      // if the signature is valid, create a new symmetric key for further signing
      if (signatureValid)
      {
        synchronized(securedDevice.getKeyCreationLock())
        {
          // search storage object for the control point
          if (!securedDevice.getSecurityAwareControlPointObjectFromPublicKeyTable().containsKey(publicKey))
          {
            // System.out.println("Create new symmetric key for signing GET requests");
            // not found, create new storage object
            SecurityAwareControlPointObject controlPointObject =
              new SecurityAwareControlPointObject(securedDevice, publicKey);

            // add storage object for control point to device
            securedDevice.getSecurityAwareControlPointObjectFromPublicKeyTable().put(publicKey, controlPointObject);
          }
        }
      }
      return signatureValid;
    }
    // check symmetric signature
    if (receivedSignature != null && receivedSequence != null && receivedKeyName != null)
    {
      // System.out.println("Try to verify symmetric signature");

      // try to find control point object
      SecurityAwareControlPointObject controlPointObject =
        securedDevice.getControlPointObjectByKeyName(receivedKeyName);
      // unknown key name
      if (controlPointObject == null)
      {
        logger.warn("Unknown key name");
        return false;
      }

      // check sequence number
      try
      {
        long receivedSequenceNumber = Long.parseLong(receivedSequence);
        if (receivedSequenceNumber <= controlPointObject.getSymmetricSequence())
        {
          logger.warn("Invalid sequence number");
          return false;
        }
        // update stored sequence number
        controlPointObject.setSymmetricSequence(receivedSequenceNumber);
      } catch (Exception e)
      {
        return false;
      }

      // build signature content
      String signatureContent = receivedKeyName;
      signatureContent += receivedSequence + "";

      SecretKey aesKey = controlPointObject.getSymmetricKey();

      return DigestHelper.verifySHA1HMACForString(receivedSignature, signatureContent, aesKey);
    }
    return false;
  }

}

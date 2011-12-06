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
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.device.DeviceSubscribeMessageProcessor;
import de.fraunhofer.fokus.upnp.core.device.DeviceSubscribedControlPointHandler;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.PublicKeysParser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateService;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.gena.GENAParser;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;

/**
 * This class handles the processing of secured event re/un/subscription messages
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SecuredDeviceSubscribeMessageProcessor extends DeviceSubscribeMessageProcessor
{

  /**
   * Processes a subscription message, if the message is OK a unique uuid and a event key for the
   * subscriber is generated the subscribe response builder is called and a response message is
   * returned
   * 
   * @param requestParser
   *          Associated parser
   * @param device
   *          The device that received the subscription request
   * 
   * @return subscribe response message as a string or if service is not found an error message
   */
  public static HTTPMessageObject processSubscribe(HTTPParser requestParser, Device device)
  {
    InetSocketAddress serverAddress = requestParser.getHTTPMessageObject().getDestinationAddress();
    // retrieve URL from request
    URL parameterURL = requestParser.getRequestURL();
    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // get callback URLs
    Vector deliveryURLs = GENAParser.parseCallbackValue(requestParser.getValue(CommonConstants.CALLBACK));

    // get optional UDP callback URLs
    Vector udpDeliveryURLs = null;
    String udpCallbackHeader = requestParser.getValue(CommonConstants.UDP_CALLBACK);
    if (udpCallbackHeader != null)
    {
      udpDeliveryURLs = GENAParser.parseCallbackValue(udpCallbackHeader);
    }

    // check if service is present
    DeviceService service = checkEventSubscriptionURL(parameterURL, device);
    // check for errors
    if (deliveryURLs.size() == 0 || service == null)
    {
      logger.info("SUBSCRIBE is ignored due to an error!");
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // check for needed signature
    HTTPMessageObject securityErrorMessage = checkForSignature(requestParser, device, service, serverAddress);
    if (securityErrorMessage != null)
    {
      logger.info("SUBSCRIBE is ignored due to missing or invalid signature");
      return securityErrorMessage;
    }

    HTTPMessageObject result = null;

    // check if a subscriber with these delivery urls already exists
    DeviceSubscribedControlPointHandler knownSubscriber = service.getSubscriber(deliveryURLs);
    String sid = null;
    if (knownSubscriber == null)
    {
      // get new SID for subscriber
      sid = device.getNewSubscriptionUUID();

      // create new subscriber thread
      int timeout = service.addSubscriber(sid, deliveryURLs, udpDeliveryURLs, requestParser);

      // create response
      result =
        new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(), sid, timeout),
          serverAddress);
    } else
    {
      System.out.println("Received multiple subscription request from callback URL " + deliveryURLs.elementAt(0));

      sid = knownSubscriber.getSID();
      // create response as if this is the first subscription request
      result =
        new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(),
          knownSubscriber.getSID(),
          knownSubscriber.getSubscriptionTimeout()), serverAddress);
    }

    // retrieve secured device
    SecuredTemplateDevice securedDevice = null;
    if (device instanceof SecuredTemplateDevice)
    {
      securedDevice = (SecuredTemplateDevice)device;
    }

    // if this message was signed by a public key, add the encrypted symmetric key that
    // was generated automatically to the response message
    SecuredMessageHelper.tryAddSymmetricKeyToResponseMessage(requestParser, device, result);

    // retrieve the keys associated with the control point
    SecurityAwareControlPointObject controlPointObject =
      SecuredDeviceGetMessageProcessor.getSecurityAwareControlPointObject(securedDevice, requestParser);

    // associate SID with symmetric key to allow symmetric signatures for events
    if (controlPointObject != null)
    {
      controlPointObject.addSID(sid);
    }

    // sign the response subscribe message
    if (securedDevice != null && controlPointObject != null)
    {
      // System.out.println("Sign SUBSCRIBE response");
      String signatureContent = sid;
      // we sign all responses with the symmetric key
      signatureContent += controlPointObject.getSymmetricKeyName();
      // if we already received a sequence base, we use that sequence base
      if (requestParser.hasField(HTTPConstant.X_SEQUENCE))
      {
        System.out.println("Use sequence from SUBSCRIBE request");
        signatureContent += requestParser.getValue(HTTPConstant.X_SEQUENCE);
      } else
      {
        // no sequence received, we use the sequence base from the newly created symmetric key
        signatureContent += controlPointObject.getSymmetricSequence();
      }

      // System.out.println("Signature content is " + signatureContent);
      SecuredMessageHelper.trySecureResponseMessage(result,
        controlPointObject.getSymmetricKeyInfo(),
        signatureContent,
        false);
    }

    return result;
  }

  /**
   * Processes a resubscription message.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          The device that received the subscription request
   * 
   * @return HTTP OK if message was correct otherwise returns the corresponding HTTP error message
   */
  public static HTTPMessageObject processResubscribe(HTTPParser httpParser, Device device)
  {
    InetSocketAddress serverAddress = httpParser.getHTTPMessageObject().getDestinationAddress();
    // retrieve URL from request
    URL parameterURL = httpParser.getRequestURL();

    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // check if service is present
    DeviceService service = checkEventSubscriptionURL(parameterURL, device);
    if (service == null)
    {
      logger.info("RESUBSCRIBE is ignored due to an error!");
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // check for needed signature
    HTTPMessageObject securityErrorMessage = checkForSignature(httpParser, device, service, serverAddress);
    if (securityErrorMessage != null)
    {
      logger.info("RESUBSCRIBE is ignored due to missing or invalid signature");
      return securityErrorMessage;
    }

    String sid = httpParser.getValue(GENAConstant.SID);
    // check if uuid is present
    if (!isKnownSubscriber(service, sid))
    {
      logger.warn("invalid resubscribe message");
      logger.warn("reason: unknown SID = " + sid);

      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
    }

    // set new timeout for subscriber thread
    int timeout = service.renewSubscriber(sid, httpParser.getValue(HTTPConstant.TIMEOUT));

    // send reply to control point
    HTTPMessageObject result =
      new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(), sid, timeout),
        serverAddress);

    return result;
  }

  /**
   * Processes an unsubscribe message.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          The device that received the subscription request
   * 
   * @return an HTTP OK Header if unsubscribe was successfull or an HTTP Error Header if not
   */
  public static HTTPMessageObject processUnsubscribe(HTTPParser httpParser, Device device)
  {
    InetSocketAddress serverAddress = httpParser.getHTTPMessageObject().getDestinationAddress();
    // check if called eventing_URL is correct
    URL parameterURL = httpParser.getRequestURL();

    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    DeviceService service = checkEventSubscriptionURL(parameterURL, device);
    if (service == null)
    {
      logger.info("UNSUBSCRIBE is ignored due to an error!");
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // check for needed signature
    HTTPMessageObject securityErrorMessage = checkForSignature(httpParser, device, service, serverAddress);
    if (securityErrorMessage != null)
    {
      logger.info("UNSUBSCRIBE is ignored due to missing or invalid signature");
      return securityErrorMessage;
    }

    String sid = httpParser.getValue(GENAConstant.SID);
    // check if uuid is present
    if (!isKnownSubscriber(service, sid))
    {
      logger.warn("invalid unsubscribe message");
      logger.warn("reason: unknown SID = " + sid);

      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
    }

    // delete subscriber from subscriber hashtable
    service.removeSubscriber(sid);

    // reply to control point
    return new HTTPMessageObject(HTTPConstant.HTTP_OK_NL, serverAddress);
  }

  /**
   * Checks GENA messages for signatures. If the signature is missing or invalid, an error response
   * message is generated.
   */
  private static HTTPMessageObject checkForSignature(HTTPParser httpParser,
    Device device,
    DeviceService service,
    InetSocketAddress serverAddress)
  {
    // check for signature
    if (device instanceof SecuredTemplateDevice)
    {
      SecuredTemplateDevice securedDevice = (SecuredTemplateDevice)device;
      SecuredTemplateService securedService = null;
      if (service instanceof SecuredTemplateService)
      {
        securedService = (SecuredTemplateService)service;
      }

      // use signed subscriptions if either the complete device or the current service need
      // signed subscriptions
      if (securedDevice.needsSignedEventSubscriptions() || securedService != null &&
        securedService.needsSignedEventSubscriptions())
      {
        // System.out.println("Accept only signed event messages for " +
        // securedDevice.getFriendlyName() +
        // (securedService != null ? securedService.getServiceType() : ""));

        if (!httpParser.hasField(HTTPConstant.X_SIGNATURE))
        {
          // System.out.println("Missing signature in subscription message, return nonce");
          String nonce = securedDevice.createAndStoreNonce();

          String responseHeader = HTTPConstant.HTTP_ERROR_401;

          responseHeader += HTTPHeaderBuilder.buildHeader(HTTPConstant.X_NONCE, nonce);

          return new HTTPMessageObject(responseHeader, serverAddress);
        }
        // try to verify signature
        boolean validSignature = verifySignature(httpParser, securedDevice);

        if (!validSignature)
        {
          System.out.println("Invalid signature");
          return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
        }
        if (httpParser.isSecuredSUBSCRIBEMessage())
        {
          System.out.println("Received secured SUBSCRIBE with valid signature");
        }

        if (httpParser.isSecuredRESUBSCRIBEMessage())
        {
          System.out.println("Received secured RESUBSCRIBE with valid signature");
        }

        if (httpParser.isSecuredUNSUBSCRIBEMessage())
        {
          System.out.println("Received secured UNSUBSCRIBE with valid signature");
        }
      }
    }
    return null;
  }

  /** Verifies a received signature for a GENA message. */
  private static boolean verifySignature(HTTPParser requestParser, SecuredTemplateDevice securedDevice)
  {
    String receivedSignature = requestParser.getValue(HTTPConstant.X_SIGNATURE);
    String receivedNonce = requestParser.getValue(HTTPConstant.X_NONCE);
    String receivedPublicKey = requestParser.getValue(HTTPConstant.X_PUBLIC_KEY);
    String receivedSequence = requestParser.getValue(HTTPConstant.X_SEQUENCE);
    String receivedKeyName = requestParser.getValue(HTTPConstant.X_KEY_ID);

    if (receivedSignature != null && receivedNonce != null && receivedPublicKey != null)
    {
      // check nonce for validness
      if (!securedDevice.checkAndRemoveNonce(receivedNonce))
      {
        System.out.println("Nonce was invalid");
        return false;
      }
      String signatureContent = receivedNonce;
      // add callback URLs for SUBSCRIBE messages
      if (requestParser.isSUBSCRIBEMessage())
      {
        signatureContent += requestParser.getValue(CommonConstants.CALLBACK);
        if (requestParser.hasField(CommonConstants.UDP_CALLBACK))
        {
          signatureContent += requestParser.getValue(CommonConstants.UDP_CALLBACK);
        }
      }
      // add sid for RESUBSCRIBE and UNSUBSCRIBE messages
      if (requestParser.isRESUBSCRIBEMessage() || requestParser.isUNSUBSCRIBEMessage())
      {
        signatureContent += requestParser.getValue(GENAConstant.SID);
      }
      signatureContent += receivedPublicKey;

      // build public key from string
      PublicKeysParser parser = new PublicKeysParser(receivedPublicKey);
      RSAPublicKey publicKey = parser.getPublicKey();

      if (publicKey == null)
      {
        System.out.println("Could not parse public key");
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
            System.out.println("Create new symmetric key for signing events");
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
          System.out.println("Invalid sequence number " + receivedSequenceNumber);
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
      // add callback URLs for SUBSCRIBE messages
      if (requestParser.isSUBSCRIBEMessage())
      {
        signatureContent += requestParser.getValue(CommonConstants.CALLBACK);
        if (requestParser.hasField(CommonConstants.UDP_CALLBACK))
        {
          signatureContent += requestParser.getValue(CommonConstants.UDP_CALLBACK);
        }
      }
      // add sid for RESUBSCRIBE and UNSUBSCRIBE messages
      if (requestParser.isRESUBSCRIBEMessage() || requestParser.isUNSUBSCRIBEMessage())
      {
        signatureContent += requestParser.getValue(GENAConstant.SID);
      }

      SecretKey aesKey = controlPointObject.getSymmetricKey();

      // System.out.println("Signature content is " + signatureContent);
      // System.out.println("Used key is " +
      // Base64Helper.byteArrayToBase64(aesKey.getEncoded()));
      // System.out.println("Received signature is " + receivedSignature);

      return DigestHelper.verifySHA1HMACForString(receivedSignature, signatureContent, aesKey);
    }
    return false;
  }

}

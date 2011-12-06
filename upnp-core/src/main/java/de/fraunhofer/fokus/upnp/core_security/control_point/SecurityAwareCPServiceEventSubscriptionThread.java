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
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPServiceEventSubscriptionThread;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPMessageFlow;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This thread is started in the SecurityAwareCPService for each subscription.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SecurityAwareCPServiceEventSubscriptionThread extends CPServiceEventSubscriptionThread
{

  private static String                     SIGNATURE_TYPE_TAG    = "SignatureType";

  private static String                     SYMMETRIC_KEY_MESSAGE = "Symmetric";

  private static String                     PUBLIC_KEY_MESSAGE    = "Public";

  private static String                     UNSIGNED_MESSAGE      = "Unsigned";

  private SecurityAwareTemplateControlPoint securityAwareControlPoint;

  /**
   * Creates a new event subscription thread.
   * 
   * @param service
   *          Service that should be subscribed to
   * @param eventSubscriptionURL
   *          URL where to send the subscription
   * @param deliveryURLs
   *          URLs where notify messages are to be sent
   * @param timeoutWish
   *          timeout wish of the subscription
   * @param IPVersion
   *          Used IP version
   * @param controlPoint
   *          Associated control point
   * 
   */
  public SecurityAwareCPServiceEventSubscriptionThread(CPService service,
    URL eventSubscriptionURL,
    Vector deliveryURLs,
    String timeoutWish,
    int IPVersion,
    ControlPoint controlPoint,
    SecurityAwareTemplateControlPoint securityAwareControlPoint)
  {
    super(service, eventSubscriptionURL, deliveryURLs, null, timeoutWish, IPVersion, controlPoint);

    this.securityAwareControlPoint = securityAwareControlPoint;
  }

  /**
   * Creates a new event subscription thread.
   * 
   * @param service
   *          Service that should be subscribed to
   * @param eventSubscriptionURL
   *          URL where to send the subscription
   * @param deliveryURLs
   *          URLs where notify messages are to be sent
   * @param udpDeliveryURLs
   *          URLs where UDP notify messages are to be sent
   * @param timeoutWish
   *          timeout wish of the subscription
   * @param IPVersion
   *          Used IP version
   * @param controlPoint
   *          Associated control point
   * 
   */
  public SecurityAwareCPServiceEventSubscriptionThread(CPService service,
    URL eventSubscriptionURL,
    Vector deliveryURLs,
    Vector udpDeliveryURLs,
    String timeoutWish,
    int IPVersion,
    ControlPoint controlPoint,
    SecurityAwareTemplateControlPoint securityAwareControlPoint)
  {
    super(service, eventSubscriptionURL, deliveryURLs, udpDeliveryURLs, timeoutWish, IPVersion, controlPoint);
    this.securityAwareControlPoint = securityAwareControlPoint;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createRequest(java.util.Hashtable, java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL targetURL)
  {
    String messageType = (String)messageOptions.get(MESSAGE_TYPE_TAG);
    HTTPMessageObject result = super.createRequest(messageOptions, targetURL);
    // add signatures if needed
    if (result != null)
    {
      String signatureType = (String)messageOptions.get(SIGNATURE_TYPE_TAG);
      if (signatureType.equals(PUBLIC_KEY_MESSAGE))
      {
        String publicKey = SecurityHelper.buildRSAPublicKeyXMLDescription(securityAwareControlPoint.getPublicKey());

        String nonce = (String)messageOptions.get(HTTPConstant.X_NONCE);
        // build content for signature
        String signatureContent = nonce;
        // add callback URLs for SUBSCRIBE messages
        if (messageType.equals(SUBSCRIBE))
        {
          signatureContent += deliveryURLString;
          signatureContent += udpDeliveryURLString;
        }
        // add sid for RESUBSCRIBE and UNSUBSCRIBE messages
        if (messageType.equals(RESUBSCRIBE) || messageType.equals(UNSUBSCRIBE))
        {
          signatureContent += sid;
        }
        signatureContent += publicKey;

        byte[] signature =
          PublicKeyCryptographyHelper.calculateRSASignatureForString(securityAwareControlPoint.getPrivateKey(),
            signatureContent);

        String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

        // add signature to original message
        String header = result.getHeader();
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_NONCE, nonce);
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_PUBLIC_KEY, publicKey);
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SIGNATURE, signatureBase64);
        result.setHeader(header);
      }
      // add symmetric signature
      if (signatureType.equals(SYMMETRIC_KEY_MESSAGE))
      {
        SymmetricKeyInfo keyInfo = null;

        // retrieve symmetric key info in associated device security object
        SecurityAwareCPDeviceObject cpDeviceObject =
          securityAwareControlPoint.getSecurityAwareCPDeviceObject(service.getCPDevice().getUDN());

        keyInfo = cpDeviceObject.getSymmetricKeyInfo();
        keyInfo.incSequence();
        System.out.println("Create GENA request signed with symmetric key and sequence " + keyInfo.getSequence());

        // build content for signature
        String signatureContent = keyInfo.getKeyID();
        signatureContent += keyInfo.getSequence() + "";
        // add callback URLs for SUBSCRIBE messages
        if (messageType.equals(SUBSCRIBE))
        {
          signatureContent += deliveryURLString;
          signatureContent += udpDeliveryURLString;
        }
        // add sid for RESUBSCRIBE and UNSUBSCRIBE messages
        if (messageType.equals(RESUBSCRIBE) || messageType.equals(UNSUBSCRIBE))
        {
          signatureContent += sid;
        }

        byte[] signature = DigestHelper.calculateSHA1HMACForString(keyInfo.getAESKey(), signatureContent);
        String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

        // System.out.println("Signature content is " + signatureContent);
        // System.out.println("Used key is " +
        // Base64Helper.byteArrayToBase64(symmetricKeyInfo.getAESKey().getEncoded()));
        // System.out.println("Calculated signature is " + signatureBase64);

        // add signature to original message
        String header = result.getHeader();
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_KEY_ID, keyInfo.getKeyID());
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SEQUENCE, keyInfo.getSequence() + "");
        header = HTTPMessageHelper.addHeaderLine(header, HTTPConstant.X_SIGNATURE, signatureBase64);
        result.setHeader(header);
      }
      return result;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPServiceEventSubscriptionThread#sendGENAMessage(java.lang.String)
   */
  public void sendGENAMessage(String messageType)
  {
    Hashtable messageOptions = new Hashtable();
    messageOptions.put(MESSAGE_TYPE_TAG, messageType);

    // retrieve key info to serialize requests
    SymmetricKeyInfo keyInfo = null;

    // try to find symmetric key info in associated device security object
    SecurityAwareCPDeviceObject cpDeviceObject =
      securityAwareControlPoint.getSecurityAwareCPDeviceObject(service.getCPDevice().getUDN());

    System.out.println("Done");

    if (cpDeviceObject != null)
    {
      keyInfo = cpDeviceObject.getSymmetricKeyInfo();
    }
    if (keyInfo != null)
    {
      synchronized(keyInfo.getLock())
      {
        // try to sign with symmetric key
        messageOptions.put(SIGNATURE_TYPE_TAG, SYMMETRIC_KEY_MESSAGE);

        Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, publisherURL, this);
        // return result if valid
        if (result instanceof Boolean)
        {
          terminateThread = !((Boolean)result).booleanValue();
          return;
        }
      }
    }
    // symmetric key failed, try normal request
    messageOptions.put(SIGNATURE_TYPE_TAG, UNSIGNED_MESSAGE);
    Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, publisherURL, this);
    // return result if valid
    if (result instanceof Boolean)
    {
      terminateThread = !((Boolean)result).booleanValue();
      return;
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
        result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, publisherURL, this);
        if (result instanceof Boolean)
        {
          terminateThread = !((Boolean)result).booleanValue();
          return;
        }
      }
    }
    terminateThread = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPServiceEventSubscriptionThread#processResponse(java.util.Hashtable,
   *      java.net.URL, de.fhg.fokus.magic.util.network.HTTPMessageObject,
   *      de.fhg.fokus.magic.http.HTTPParser)
   */
  public Object processResponse(Hashtable messageOptions,
    URL targetURL,
    HTTPMessageObject response,
    HTTPParser responseParser)
  {
    SymmetricKeyInfo keyInfo =
      SecuredMessageHelper.tryParseSymmetricKeyInfo((String)messageOptions.get(HTTPConstant.X_NONCE),
        securityAwareControlPoint.getPrivateKey(),
        responseParser);

    SecurityAwareCPDeviceObject cpDeviceObject =
      securityAwareControlPoint.getSecurityAwareCPDeviceObject(service.getCPDevice().getUDN());
    // store received symmetric key in device
    if (keyInfo != null && cpDeviceObject != null)
    {
      System.out.println("  Associate symmetric key with " + service.getCPDevice().getFriendlyName());

      cpDeviceObject.setSymmetricKeyInfo(keyInfo);
    }
    // try to load existing symmetric key info
    if (keyInfo == null && cpDeviceObject != null)
    {
      keyInfo = cpDeviceObject.getSymmetricKeyInfo();
    }

    String signatureContent = responseParser.getValue(GENAConstant.SID);
    if (keyInfo != null)
    {
      signatureContent += keyInfo.getKeyID();
      signatureContent += keyInfo.getSequence();
    }
    // System.out.println("Signature content is " + signatureContent);
    // check if the response is signed
    if (!SecuredMessageHelper.tryVerifySignature(response, responseParser, keyInfo, signatureContent))
    {
      System.out.println("Received signed SUBSCRIBE response with invalid signature");
      return null;
    }

    return super.processResponse(messageOptions, targetURL, response, responseParser);
  }

}

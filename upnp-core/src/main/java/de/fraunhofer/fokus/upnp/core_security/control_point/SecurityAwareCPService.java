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

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPServiceEventSubscriptionThread;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core_security.helpers.SOAPSecuredMessageBuilder;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.soap.SOAPHeaderBuilder;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class provides a control point view on a remote secured UPnP service.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityAwareCPService extends CPService
{

  private SecurityAwareTemplateControlPoint securityAwareControlPoint;

  /**
   * Creates a SecurityAwareCPService object.
   * 
   * @param serviceType
   *          type of service
   * @param serviceId
   *          ID of the service
   * @param SCPDURL
   *          url for service description of this service
   * @param controlURL
   *          the control URL for this service
   * @param eventSubscriptionURL
   *          the subscribtion URL for this service
   * @param parent
   *          the device this service belongs to
   */
  public SecurityAwareCPService(CPService service, SecurityAwareTemplateControlPoint securityAwareControlPoint)
  {
    super(service.getServiceType(),
      service.getServiceId(),
      service.getSCPDURL(),
      service.getControlURL(),
      service.getEventSubURL(),
      service.getIPVersion(),
      service.getCPDevice());

    stateVariableTable = service.getCPStateVariableTable();
    // associate security aware service with each state variable
    for (int i = 0; stateVariableTable != null && i < stateVariableTable.length; i++)
    {
      ((CPStateVariable)stateVariableTable[i]).setCPService(this);
    }

    actionTable = service.getCPActionTable();
    // associate security aware service with each action
    for (int j = 0; actionTable != null && j < actionTable.length; j++)
    {
      ((CPAction)actionTable[j]).setCPService(this);
    }

    serviceDescription = service.getServiceDescription();
    eventSubscriptionThread = service.getEventSubscriptionThread();
    isSubscribed = service.isSubscribed();
    eventKey = service.getEventKey();
    this.securityAwareControlPoint = securityAwareControlPoint;
  }

  /**
   * Send a signed action request to the remote device
   * 
   * @param action
   *          action
   * @param lifetimeSequenceBase
   *          Unique string used against replay attacks
   * @param signingKey
   *          private RSA key of the ControlPoint
   * @param publicKey
   *          public RSA key of the ControlPoint
   * 
   * @return action with new value if the invokeAction can be performed, otherwise action with old
   *         value
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeRSASignedAction(CPAction action,
    String lifetimeSequenceBase,
    RSAPrivateKey signingKey,
    RSAPublicKey publicKey) throws InvokeActionException, ActionFailedException
  {
    logger.info("invoke signed " + action.getName() + " action");

    String bodyValue = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);
    String bodyWrap =
      SOAPSecuredMessageBuilder.buildRSASignedEnvelopeToBodyWrap(SOAPMessageBuilder.buildFreshnessURL(controlURL),
        lifetimeSequenceBase,
        signingKey,
        publicKey,
        bodyValue);

    byte[] body = StringHelper.utf8StringToByteArray(bodyWrap);
    sendPostAction(action, body, null, null, null, null, null);
  }

  /**
   * Send a signed action request to the remote device
   * 
   * @param action
   *          action
   * @param sequenceBase
   *          string used against replay attacks per session
   * @param sequenceNumber
   *          string used against replay attacks per action
   * @param signingKey
   *          Session SHA1 HMAC key
   * @param keyID
   *          Session ID
   * 
   * @return action with new value if the invokeAction can be performed, otherwise action with old
   *         value
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeSHA1HMACSignedAction(CPAction action,
    String sequenceBase,
    String sequenceNumber,
    Key signingKey,
    int keyID) throws InvokeActionException, ActionFailedException
  {
    logger.info("invoke session signed " + action.getName() + " action");

    String innerBody = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);
    String body =
      SOAPSecuredMessageBuilder.buildSHA1HMACSignedEnvelopeToBodyWrap(SOAPMessageBuilder.buildFreshnessURL(controlURL),
        sequenceBase,
        sequenceNumber,
        signingKey,
        keyID,
        innerBody);

    byte[] bodyByteArray = StringHelper.utf8StringToByteArray(body);
    sendPostAction(action, bodyByteArray, null, null, null, null, null);
  }

  /**
   * Send an encrypted action request to the remote device
   * 
   * @param action
   *          action
   * @param deviceSecurityService
   *          Service that received the encrypted action
   * @param decryptAndExecuteAction
   *          The wrapper action
   * @param session
   *          Session specific information
   * 
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeEncryptedSHA1HMACSignedAction(CPAction action,
    CPService deviceSecurityService,
    CPAction decryptAndExecuteAction,
    Session session) throws InvokeActionException, ActionFailedException
  {
    logger.info("invoke encrypted " + action.getName() + " action");
    // create action body
    String innerBody = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);

    String body =
      SOAPSecuredMessageBuilder.buildSHA1HMACSignedEnvelopeToBodyWrap(SOAPMessageBuilder.buildFreshnessURL(controlURL),
        session.getSequenceBase(),
        Integer.toString(session.getCPSequenceNumber()),
        session.getSigningToDeviceKey(),
        session.getDeviceKeyID(),
        innerBody);

    byte[] bodyByteArray = StringHelper.utf8StringToByteArray(body);

    String messageHeader =
      SOAPHeaderBuilder.buildActionRequestHeader(controlURL, serviceType, action, null, bodyByteArray.length, false);

    // SOAP message ready for sending
    byte[] actionMessage = HTTPMessageHelper.createHTTPMessage(messageHeader, bodyByteArray);

    try
    {
      Argument keyIDArg = decryptAndExecuteAction.getInArgument("DeviceKeyID");
      keyIDArg.setNumericValue(session.getDeviceKeyID());

      // add '0'
      byte[] encryptionSource = new byte[actionMessage.length + 1];
      System.arraycopy(actionMessage, 0, encryptionSource, 0, actionMessage.length);
      encryptionSource[encryptionSource.length - 1] = (byte)0;
      // encrypt generated soap message as argument to decryptAndExecute action
      byte[] iv = SymmetricCryptographyHelper.generateIV();
      byte[] encryptedMessage =
        SymmetricCryptographyHelper.encryptWithAES(session.getConfidentialityToDeviceKey(), iv, encryptionSource);

      Argument requestArg = decryptAndExecuteAction.getInArgument("Request");
      requestArg.setValue(Base64Helper.byteArrayToBase64(encryptedMessage));

      Argument inIVArg = decryptAndExecuteAction.getInArgument("InIV");
      inIVArg.setValue(Base64Helper.byteArrayToBase64(iv));

      // do not sign the wrapper message
      // send decryptAndExecute SOAPMessage

      // we send the encrypted message directly
      deviceSecurityService.getCPDevice().getControlPoint().invokeAction(decryptAndExecuteAction);
      CPAction result = decryptAndExecuteAction;

      // process out arguments
      Argument replyArg = result.getOutArgument("Reply");
      byte[] encryptedReply = replyArg.getBinBase64Value();
      Argument outIVArg = result.getOutArgument("OutIV");
      byte[] outIV = outIVArg.getBinBase64Value();

      // decrypt reply
      byte[] decryptedMessage =
        SymmetricCryptographyHelper.decryptWithAES(session.getConfidentialityFromDeviceKey(), outIV, encryptedReply);
      // kill last '0'
      byte[] response = new byte[decryptedMessage.length - 1];
      System.arraycopy(decryptedMessage, 0, response, 0, response.length);

      HTTPMessageObject responseMessage =
        new HTTPMessageObject(HTTPMessageHelper.getHeader(response),
          HTTPMessageHelper.getBody(response),
          IPHelper.toSocketAddress(controlURL),
          null);

      // response now holds the content of the wrapped soap request
      // process decrypted SOAP response, do not retry with MPOST (too complicated right now)
      processActionResponse(action, null, responseMessage, null, null, null, null);

      return;

    } catch (Exception ex)
    {
      throw new InvokeActionException(ex.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPService#startCPServiceEventSubscriptionThread(java.util.Vector,
   *      java.util.Vector)
   */
  public CPServiceEventSubscriptionThread startCPServiceEventSubscriptionThread(Vector callbackURLs,
    Vector udpCallbackURLs)
  {
    // create new thread that is responsible for regular resubscription
    CPServiceEventSubscriptionThread eventSubscriptionThread =
      new SecurityAwareCPServiceEventSubscriptionThread(this,
        eventSubscriptionURL,
        callbackURLs,
        udpCallbackURLs,
        UPnPDefaults.CP_SUBSCRIPTION_TIMEOUT + "",
        IPVersion,
        getCPDevice().getControlPoint(),
        securityAwareControlPoint);
    eventSubscriptionThread.start();

    return eventSubscriptionThread;
  }
}

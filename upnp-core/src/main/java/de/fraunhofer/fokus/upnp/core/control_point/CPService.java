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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.AbstractService;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionArgument;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionHandler;
import de.fraunhofer.fokus.upnp.core.xml.SOAPErrorHandler;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.HTTPServer;
import de.fraunhofer.fokus.upnp.soap.SOAPHeaderBuilder;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.RSAPublicKeyParser;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class provides a control point view on a remote UPnP service.
 * 
 * @author icu, Alexander Koenig, Sebastian Nauck
 * 
 */
public class CPService extends AbstractService implements ICPStateVariableListener
{
  /**
   * UPnP logger
   */
  public static Logger                       logger                       = Logger.getLogger("upnp.cp");

  /** The associated device */
  protected CPDevice                         cpDevice;

  /** Service description for this service */
  protected String                           serviceDescription;

  /** The event subscription thread */
  protected CPServiceEventSubscriptionThread eventSubscriptionThread;

  /** True if the associated control point has an event subscription for this service */
  protected boolean                          isSubscribed                 = false;

  /**
   * Event key which is checked against incoming notify messages if it is in correct sequence
   */
  protected long                             eventKey                     = 0;

  /**
   * Multicast event key which is checked against incoming notify messages if it is in correct sequence
   */
  protected long                             multicastEventKey            = -1;

  /** Multicast initial event is pending */
  protected boolean                          multicastInitialEventPending = false;

  /** Flag for multicast event errors */
  protected boolean                          multicastEventError          = false;

  /** Multicast initial event sync object */
  protected Object                           multicastInitialEventLock    = new Object();

  /** Multicast event socket address */
  protected InetSocketAddress                multicastEventSocketAddress  = null;

  /** URL for the service description as an URL */
  protected URL                              SCPDURL;

  /** URL for control purposes for a service */
  protected URL                              controlURL;

  /** URL for subscribe purposes for a service */
  protected URL                              eventSubscriptionURL;

  /** Client used to send action requests. */
  protected HTTPClient                       persistentClient;

  /**
   * Creates CPService object.
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
   * @param device
   *          the device this service belongs to
   */
  public CPService(String serviceType,
    String serviceId,
    URL SCPDURL,
    URL controlURL,
    URL eventSubscriptionURL,
    int IPVersion,
    CPDevice device)
  {
    super(serviceType, serviceId, IPVersion);
    setSCPDURL(SCPDURL);
    setControlURL(controlURL);
    setEventSubscriptionURL(eventSubscriptionURL);
    cpDevice = device;
    if (device == null)
    {
      logger.warn("Invoked with parent device null");
    }
  }

  /**
   * Sets service ip version
   * 
   * @param IPVersion
   *          ip version
   */
  public void setIPVersion(int IPVersion)
  {
    this.IPVersion = IPVersion;
  }

  /**
   * Sends an action request to a remote service.
   * 
   * @param action
   *          The action
   * @param optionalHeaderLines
   *          A vector containing additional headers for the SOAP request
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  protected void invokeAction(CPAction action, Vector optionalHeaderLines) throws InvokeActionException,
    ActionFailedException
  {
    logger.info("invoke" + action.getName() + " action");
    String innerBody = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);
    String body = SOAPMessageBuilder.buildEnvelope(innerBody);

    byte[] bodyByteArray = StringHelper.utf8StringToByteArray(body);
    sendPostAction(action, bodyByteArray, optionalHeaderLines, null, null, null, null);
  }

  /**
   * Sends a personalized action request to the remote device. The action can optionally be encrypted to ensure privacy.
   * 
   * @param encrypt
   * @param action
   * @param sequenceBase
   * @param keyID
   * @param aesKey
   * @param iv
   * @throws InvokeActionException
   * @throws ActionFailedException
   */
  protected void invokeSymmetricKeyPersonalizedAction(boolean encrypt,
    CPAction action,
    String sequenceBase,
    String keyID,
    SecretKey aesKey,
    byte[] iv) throws InvokeActionException, ActionFailedException
  {
    logger.info("invoke " + action.getName() + " action");

    String innerBody = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);
    String body = SOAPMessageBuilder.buildEnvelope(innerBody);
    String signatureContent = sequenceBase + keyID + body;

    // calculate signature
    byte[] signatureAsByteArray = DigestHelper.calculateSHA1HMACForString(aesKey, signatureContent);
    String signatureBase64 = Base64Helper.byteArrayToBase64(signatureAsByteArray);

    Vector optionalHeaderLines = new Vector();
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64));
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_KEY_ID, keyID));
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SEQUENCE, sequenceBase));

    // encrypt body if needed
    byte[] bodyByteArray = StringHelper.utf8StringToByteArray(body);
    if (encrypt == true)
    {
      bodyByteArray = SymmetricCryptographyHelper.encryptWithAES(aesKey, iv, bodyByteArray);
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_ENCRYPTION_TAG, "true"));
    }
    sendPostAction(action, bodyByteArray, optionalHeaderLines, aesKey, iv, sequenceBase, null);
  }

  /**
   * Sends a personalized action request to the remote device which is signed with a RSA key.
   * 
   * @param action
   * @param privateKey
   * @param publicKey
   * @param nonce
   * 
   * @throws InvokeActionException
   * @throws ActionFailedException
   */
  protected void invokePublicKeyPersonalizedAction(CPAction action,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    String nonce) throws InvokeActionException, ActionFailedException
  {
    logger.info("invoke " + action.getName() + " action");

    String publicKeyXMLDescription = SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey);

    String innerBody = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, action);
    String body = SOAPMessageBuilder.buildEnvelope(innerBody);

    // calculate signature
    String signatureContent = nonce + publicKeyXMLDescription + body;
    byte[] signature = PublicKeyCryptographyHelper.calculateRSASignatureForString(privateKey, signatureContent);
    String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

    Vector optionalHeaderLines = new Vector();
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64));
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY,
      publicKeyXMLDescription));
    optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_NONCE, nonce));

    byte[] bodyByteArray = StringHelper.utf8StringToByteArray(body);
    sendPostAction(action, bodyByteArray, optionalHeaderLines, null, null, null, nonce);
  }

  /**
   * Sends a POST action request to the remote device.
   * 
   * @param action
   *          action
   * @param requestBody
   *          the SOAP body
   * @param optionalHeaderLines
   *          A vector containing additional headers for the SOAP request
   * @param aesKey
   *          is the Secret AES-Key to encrypt the data and/or generate a signature
   * @param iv
   *          (initalisation Vector) is an array which is need to encrypt teh data
   * @param sequenceBase
   *          a random number which become increment by request to prevent replay attacks
   * @param nonce
   *          a random number for the request to prevent replay attacks
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  protected void sendPostAction(CPAction action,
    byte[] requestBody,
    Vector optionalHeaderLines,
    SecretKey aesKey,
    byte[] iv,
    String sequenceBase,
    String nonce) throws InvokeActionException, ActionFailedException
  {
    String requestHeader =
      SOAPHeaderBuilder.buildActionRequestHeader(controlURL,
        serviceType,
        action,
        optionalHeaderLines,
        requestBody.length,
        false);

    // System.out.println("Request header is \n" + requestHeader);
    // System.out.println("Request body is \n" + StringHelper.byteArrayToUTF8String(requestBody));

    HTTPMessageObject soapRequest = new HTTPMessageObject(requestHeader, requestBody, null);
    soapRequest.setDestinationAddress(IPHelper.toSocketAddress(controlURL));

    HTTPMessageObject response = sendActionToRemoteDevice(soapRequest);

    // System.out.println("Response header is \n" + response.getHeader());
    // System.out.println("Response body is \n" + response.getBodyAsUTF8String());

    processActionResponse(action, requestBody, response, aesKey, iv, sequenceBase, nonce);
  }

  /**
   * Sends a M-POST action request to the remote device.
   * 
   * @param action
   *          action
   * @param bodyWrap
   *          the SOAP body
   * @return action with new value if the invokeAction can be performed, otherwise action with old value
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  private void sendMPostAction(CPAction action, byte[] requestBody) throws InvokeActionException, ActionFailedException
  {
    // build MPOST header
    String requestHeader =
      SOAPHeaderBuilder.buildActionRequestHeader(controlURL, serviceType, action, null, requestBody.length, true);

    HTTPMessageObject soapRequest = new HTTPMessageObject(requestHeader, requestBody, null);
    soapRequest.setDestinationAddress(IPHelper.toSocketAddress(controlURL));

    HTTPMessageObject response = sendActionToRemoteDevice(soapRequest);
    // we set body to null to prevent stack overflow with endless MPosts
    processActionResponse(action, null, response, null, null, null, null);
  }

  /**
   * This method sends a SOAP message to the remote device.
   * 
   * @param message
   *          The message
   * 
   * @return the response message
   * 
   * @throws InvokeActionException
   *           error if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  private HTTPMessageObject sendActionToRemoteDevice(HTTPMessageObject message) throws InvokeActionException
  {
    // create new http client if needed
    if (persistentClient == null || !HTTPDefaults.PERSISTENT_CLIENT_CONNECTION)
    {
      persistentClient = new HTTPClient(HTTPDefaults.PERSISTENT_CLIENT_CONNECTION);
    }
    try
    {
      //      System.out.println(DateTimeHelper.formatCurrentDateForDebug() + ": Send action to remote device");
      persistentClient.sendRequestAndWaitForResponse(message);
    } catch (Exception e1)
    {
      throw new InvokeActionException(e1.getMessage());
    }
    return persistentClient.getResponse();
  }

  /**
   * Processes the response from a sent SOAP action message.
   * 
   * @param action
   *          the action
   * @param bodyWrap
   *          the body of the original request
   * @param response
   *          The response
   * @param aesKey
   *          is the Secret AES-Key to encrypt the data and/or generate a signature
   * @param iv
   *          (initalisation Vector) is an array which is need to decrypt the data.
   * @param storedSequenceBase
   *          to compare with the sequenceBase in the response to prevent replay attacks
   * @param storedNonce
   *          to compare with the sequenceBase in the response to prevent replay attacks
   * 
   * @return action with new values if the action was performed, otherwise action with old values
   * 
   * @throws InvokeActionException
   *           error if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  protected void processActionResponse(CPAction action,
    byte[] requestBody,
    HTTPMessageObject response,
    SecretKey aesKey,
    byte[] iv,
    String storedSequenceBase,
    String storedNonce) throws InvokeActionException, ActionFailedException
  {
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(response);
    String receivedNonce = null;
    boolean trustableResponse = true;

    // valid response
    if (httpParser.isHTTPResponse())
    {
      // HTTP OK response
      if (httpParser.isHTTPOKResponse())
      {
        String responseBody = "";
        // response is encrypted
        if (httpParser.getBooleanValue(HTTPConstant.X_ENCRYPTION_TAG))
        {
          byte[] encryptedBody = response.getBody();
          byte[] decryptedBody = SymmetricCryptographyHelper.decryptWithAES(aesKey, iv, encryptedBody);
          responseBody = StringHelper.byteArrayToUTF8String(decryptedBody);
        } else
        // response is not encrypted
        {
          responseBody = response.getBodyAsUTF8String();
        }
        // check for symmetric signature
        if (httpParser.hasField(HTTPConstant.X_PERSONALIZATION_SEQUENCE) &&
          httpParser.hasField(HTTPConstant.X_PERSONALIZATION_KEY_ID) &&
          httpParser.hasField(HTTPConstant.X_PERSONALIZATION_SIGNATURE))
        {
          // check sequence base
          String receivedSequenceBase = httpParser.getValue(HTTPConstant.X_PERSONALIZATION_SEQUENCE);
          trustableResponse &= receivedSequenceBase != null && receivedSequenceBase.equals(storedSequenceBase);

          String signatureContent = receivedSequenceBase;
          signatureContent += httpParser.getValue(HTTPConstant.X_PERSONALIZATION_KEY_ID);
          signatureContent += responseBody;

          boolean validSignature =
            DigestHelper.verifySHA1HMACForString(httpParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE),
              signatureContent,
              aesKey);

          trustableResponse &= validSignature;
        }
        // check for public key signature
        if (storedNonce != null)
        {
          String signature = httpParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE);
          receivedNonce = httpParser.getValue(HTTPConstant.X_NONCE);
          String publicKey = httpParser.getValue(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY);
          RSAPublicKeyParser parser = new RSAPublicKeyParser();
          try
          {
            parser.parse(publicKey);
          } catch (SAXException e)
          {
          }
          if (signature != null && parser.getPublicKey() != null && receivedNonce != null &&
            receivedNonce.equals(storedNonce))
          {
            // right now, we do not check the identity of the sending device

            // check public key signature
            String signatureContent = storedNonce + publicKey + responseBody;
            boolean validSignature =
              PublicKeyCryptographyHelper.verifyRSASignatureForString(parser.getPublicKey(),
                signatureContent,
                signature);
            trustableResponse = validSignature;
          } else
          // insufficient signature data
          {
            trustableResponse = false;
          }
        }
        if (!trustableResponse)
        {
          System.out.println("Received action response with invalid signature");
        }
        SOAPActionHandler soapActionHandler = new SOAPActionHandler();
        if (trustableResponse)
        {
          try
          {
            soapActionHandler.parse(responseBody);
          } catch (SAXException e1)
          {
            throw new InvokeActionException("Error parsing response");
          }
          if (soapActionHandler.isValid())
          {
            // valid action response message
            if (!soapActionHandler.getServiceType().equalsIgnoreCase(serviceType))
            {
              logger.warn("wrong response service type =" + soapActionHandler.getServiceType());
              throw new InvokeActionException("wrong response service type =" + soapActionHandler.getServiceType());
            }
            // get action using action name provided in response message
            CPAction tempAction = getCPAction(soapActionHandler.getActionName());

            if (tempAction == null)
            {
              logger.warn("no action found " + soapActionHandler.getServiceType());
              throw new InvokeActionException("no action found" + soapActionHandler.getServiceType());
            }
            if (tempAction != action)
            {
              logger.warn("wrong response " + tempAction.getName());
              throw new InvokeActionException("Response does not fit " + soapActionHandler.getActionName());
            }
            // get all out arguments from response message
            Vector receivedArgumentTable = soapActionHandler.getArgumentList();
            Argument[] localArgumentTable = action.getOutArgumentTable();
            int localArgumentTableSize = localArgumentTable == null ? 0 : localArgumentTable.length;
            // compare size of argument tables
            if (receivedArgumentTable.size() != localArgumentTableSize)
            {
              logger.warn("incorrect number of out arguments. " + "incoming = " + receivedArgumentTable.size() +
                " local = " + localArgumentTable.length);
              throw new InvokeActionException("wrong response argument size=" + receivedArgumentTable.size());
            }
            // go through all out arguments
            for (int i = 0; i < receivedArgumentTable.size(); i++)
            {
              SOAPActionArgument receivedArgument = (SOAPActionArgument)receivedArgumentTable.elementAt(i);
              // get out argument using argument name provided in response message
              Argument localArgument = action.getOutArgument(receivedArgument.getName());

              if (localArgument == null)
              {
                logger.warn("wrong response argument =" + receivedArgument.getName());
                throw new InvokeActionException("wrong response argument =" + receivedArgument.getName());
              }
              // set new value
              try
              {
                localArgument.setValueFromString(receivedArgument.getValue());
              } catch (Exception e)
              {
                logger.warn("value of out argument could not be set: " + e);
              }
            }
            return;
          } else
          {
            System.out.println("Invalid invoke action response: " + responseBody);
            throw new InvokeActionException("invalid invoke action response");
          }
        }
      }
      if (httpParser.getResponseCode() == 500)
      {
        SOAPErrorHandler soapErrorHandler = new SOAPErrorHandler();
        int code = -1;
        try
        {
          soapErrorHandler.parse(response.getBodyAsUTF8String());
          if (soapErrorHandler.isValid())
          {
            logger.warn("invoke action unsuccessful; result=500, text='" + httpParser.getResponseDescription() + "'" +
              " errorCode: " + soapErrorHandler.getErrorCode() + " errorDescription: " +
              soapErrorHandler.getErrorDescription());
            code = Integer.parseInt(soapErrorHandler.getErrorCode());
          }
        } catch (Exception e)
        {
        }
        // check for detailed exception
        if (code != -1)
        {
          throw new ActionFailedException(code, soapErrorHandler.getErrorDescription());
        }
        // throw normal exception
        throw new InvokeActionException("Invoke action unsuccessful: httpResult=500");
      }
      if (httpParser.getResponseCode() == 405)
      {
        logger.warn("Post message failure, try again with MPost");

        // Post message failure, try MPost if bodyWrap is valid
        if (requestBody != null)
        {
          sendMPostAction(action, requestBody);
          return;
        } else
        {
          // message is already MPost, throw error
          throw new InvokeActionException("Invoke action unsuccessful: httpResult=500");
        }
      }
      throw new InvokeActionException("Invalid response to " + action.getName());
    }
  }

  /** Sends an event subscription message to a remote service. */
  public void sendSubscription()
  {
    // Callback URL is generated from network
    logger.info("send subscription to " + serviceId);
    System.out.println("Send subscription to " + toString());

    try
    {
      // get socket structure that discovered the device
      ControlPointHostAddressSocketStructure discoverySocketStructure = getCPDevice().getCPSocketStructure();

      // normal event handling
      Vector callbackURLs = new Vector();
      // get eventing server for that structure
      HTTPServer eventCallbackServer = discoverySocketStructure.getEventCallbackServer();
      // add callback URL for the discovered interface
      URL callbackURL =
        new URL("http", eventCallbackServer.getAddress(), eventCallbackServer.getPort(), "/eventNotify/");
      callbackURLs.add(callbackURL);

      // optional UDP event handling
      Vector udpCallbackURLs = null;
      // get eventing server for that structure
      DatagramSocket eventCallbackUDPServer = discoverySocketStructure.getEventCallbackUDPServer();
      if (eventCallbackUDPServer != null)
      {
        udpCallbackURLs = new Vector();
        // add callback URL for the discovered interface
        URL udpCallbackURL =
          new URL("http", eventCallbackServer.getAddress(), eventCallbackUDPServer.getLocalPort(), "/eventNotify/");
        udpCallbackURLs.add(udpCallbackURL);
      }

      // create new thread that is responsible for regular resubscription
      eventSubscriptionThread = startCPServiceEventSubscriptionThread(callbackURLs, udpCallbackURLs);
    } catch (Exception ex)
    {
      logger.error("could not build Callback URL. reason: " + ex);
    }
  }

  /** Starts a thread to subscribe to the events from this service. */
  public CPServiceEventSubscriptionThread startCPServiceEventSubscriptionThread(Vector callbackURLs,
    Vector udpCallbackURLs)
  {
    // create new thread that is responsible for regular resubscription
    CPServiceEventSubscriptionThread eventSubscriptionThread =
      new CPServiceEventSubscriptionThread(this,
        eventSubscriptionURL,
        callbackURLs,
        udpCallbackURLs,
        UPnPDefaults.CP_SUBSCRIPTION_TIMEOUT + "",
        IPVersion,
        getCPDevice().getControlPoint());
    eventSubscriptionThread.start();

    return eventSubscriptionThread;
  }

  /**
   * Sends unsubscribe message to remote services.
   */
  public void sendUnsubscription()
  {
    eventSubscriptionThread.terminate(true);
  }

  /**
   * Sets the event keys
   * 
   * @param newEventKey
   *          the new event key
   */
  public void setEventKey(long newEventKey)
  {
    eventKey = newEventKey;
  }

  /**
   * Returns event key
   * 
   * @return event key
   */
  public long getEventKey()
  {
    return eventKey;
  }

  /**
   * Retrieves the multicastEventKey.
   * 
   * @return The multicastEventKey
   */
  public long getMulticastEventKey()
  {
    return multicastEventKey;
  }

  /**
   * Sets the multicastEventKey.
   * 
   * @param multicastEventKey
   *          The new value for multicastEventKey
   */
  public void setMulticastEventKey(long multicastEventKey)
  {
    this.multicastEventKey = multicastEventKey;
  }

  /**
   * Checks if this service is subscribed via multicast.
   * 
   * @return True if the control point receives multicast events for this service, false otherwise
   */
  public boolean isMulticastSubscribed()
  {
    // if the multicast event key is at least 1, we receive valid multicast event messages
    return multicastEventError == false && multicastEventKey > 0;
  }

  /**
   * Checks if this service supports events via multicast.
   * 
   * @return True if the control point can receive multicast events for this service, false otherwise
   */
  public boolean supportsMulticastEvents()
  {
    return multicastEventSocketAddress != null && multicastEventError == false;
  }

  public CPServiceEventSubscriptionThread getEventSubscriptionThread()
  {
    return eventSubscriptionThread;
  }

  /** Sets the subscription state */
  public void setSubscribed(boolean state)
  {
    if (isSubscribed != state)
    {
      isSubscribed = state;
      // forward event to device
      cpDevice.subscriptionStateChanged(this);
    }
  }

  /** Retrieves the subscription state */
  public boolean isSubscribed()
  {
    return isSubscribed;
  }

  /**
   * Returns the device this service belongs to.
   * 
   * @return the parent device
   */
  public CPDevice getCPDevice()
  {
    return cpDevice;
  }

  /**
   * Sets control point stateVariableTable. The array is not copied. Methods using this class shall provide an array of
   * CPStateVariables instead of StateVariables. The array is simply delegated to the superclass.
   * 
   * @param stateVariableTable
   *          all service's stateVariable or null
   */
  public void setCPStateVariableTable(CPStateVariable[] stateVariableTable)
  {
    this.stateVariableTable = stateVariableTable;
  }

  /**
   * Returns control point stateVariableTable. The method retrieves the table from the superclass. If the table is null
   * or already a CPStateVariable array, then this is directly returned. Otherwise a new array is created with all
   * elements. If any of these elements is not an instance of CPStateVariable, then the method only returns null.
   * 
   * @return all service's stateVariable or null
   */
  public CPStateVariable[] getCPStateVariableTable()
  {
    return (CPStateVariable[])stateVariableTable;
  }

  /**
   * Returns control point stateVariable specified by the stateVariableName. If there is no such variable or if it is
   * not a CPStateVariable, then the method returns null.
   * 
   * @param stateVariableName
   *          the name of the stateVariable
   * @return stateVariable specified by the stateVariableName or null
   */
  public CPStateVariable getCPStateVariable(String stateVariableName)
  {
    for (int i = 0; stateVariableTable != null && i < stateVariableTable.length; i++)
    {
      if (stateVariableName.equalsIgnoreCase(stateVariableTable[i].getName()))
      {
        return (CPStateVariable)stateVariableTable[i];
      }
    }
    return null;
  }

  /**
   * Tests if the control point stateVariable exists in Service
   * 
   * @param stateVariableName
   *          name of the statevariable
   * @return true, if statevariable exist otherwise false
   */
  public boolean hasCPStateVariable(String stateVariableName)
  {
    return getCPStateVariable(stateVariableName) != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    // handle up to device
    cpDevice.stateVariableChanged(stateVariable);
  }

  /**
   * Returns the action for the specified actionName.
   * 
   * @param actionName
   *          the name of Action
   * 
   * @return the Action specified by the actionName
   */
  public CPAction getCPAction(String actionName)
  {
    for (int i = 0; actionTable != null && i < actionTable.length; i++)
    {
      if (actionName.equalsIgnoreCase(actionTable[i].getName()))
      {
        return (CPAction)actionTable[i];
      }
    }
    return null;
  }

  /**
   * Returns actionList. The array is not copied.
   * 
   * @return all actions offered by service or null
   */
  public CPAction[] getCPActionTable()
  {
    return (CPAction[])actionTable;
  }

  /**
   * Sets actionList. The array is not copied.
   * 
   * @param actionTable
   *          all actions offered by service or null
   */
  public void setCPActionTable(CPAction[] actionTable)
  {
    this.actionTable = actionTable;
  }

  /**
   * Returns service description URL
   * 
   * @return service description URL
   */
  public URL getSCPDURL()
  {
    return SCPDURL;
  }

  /**
   * Returns control URL
   * 
   * @return control URL
   */
  public URL getControlURL()
  {
    return controlURL;
  }

  /**
   * Returns event subscription URL
   * 
   * @return event subscription URL
   */
  public URL getEventSubURL()
  {
    return eventSubscriptionURL;
  }

  /**
   * Sets serviceDescription URL
   * 
   * @param SCPDURL
   *          serviceDescription URL
   */
  public void setSCPDURL(URL SCPDURL)
  {
    this.SCPDURL = SCPDURL;
  }

  /**
   * Sets control URL
   * 
   * @param controlURL
   *          control URL
   */
  public void setControlURL(URL controlURL)
  {
    this.controlURL = controlURL;
  }

  /**
   * Sets event subcription URL
   * 
   * @param eventSubscriptionURL
   *          event subscription URL
   */
  public void setEventSubscriptionURL(URL eventSubscriptionURL)
  {
    this.eventSubscriptionURL = eventSubscriptionURL;
  }

  /**
   * Retrieves the serviceDescription.
   * 
   * @return The serviceDescription
   */
  public String getServiceDescription()
  {
    return serviceDescription;
  }

  /**
   * Sets the serviceDescription.
   * 
   * @param serviceDescription
   *          The new value for serviceDescription
   */
  public void setServiceDescription(String serviceDescription)
  {
    this.serviceDescription = serviceDescription;
  }

  /** Sends a multicast request for current state variable values. */
  public void sendInitialEventMessage()
  {
    synchronized(multicastInitialEventLock)
    {
      if (multicastInitialEventPending)
      {
        System.out.println("Initial event request is already pending");
        return;
      }
      multicastInitialEventPending = true;
      // create thread to request state variable values
      new CPMulticastInitialEventRequestThread(this);
    }
  }

  /** Event that the initial event message request returned. */
  public void initialEventMessageResponse(boolean success)
  {
    synchronized(multicastInitialEventLock)
    {
      multicastInitialEventPending = false;
      multicastEventError = !success;
    }
  }

  /**
   * Retrieves the multicastEventSocketAddress.
   * 
   * @return The multicastEventSocketAddress
   */
  public InetSocketAddress getMulticastEventSocketAddress()
  {
    return multicastEventSocketAddress;
  }

  /**
   * Sets the multicastEventSocketAddress.
   * 
   * @param multicastEventSocketAddress
   *          The new value for multicastEventSocketAddress
   */
  public void setMulticastEventSocketAddress(InetSocketAddress multicastEventSocketAddress)
  {
    this.multicastEventSocketAddress = multicastEventSocketAddress;
  }

  /** Returns a short string identifying this service and its associated device. */
  public String toFriendlyNameString()
  {
    return cpDevice.getFriendlyName() + "." + getShortenedServiceId();
  }

}

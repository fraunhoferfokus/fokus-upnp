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
package de.fraunhofer.fokus.upnp.core.device;

import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.personalization.SecuredPersonalizationService;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionArgument;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionHandler;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.soap.SOAPConstant;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.PersonalizedKeyObject;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class handles the processing and building of response message for an incoming control
 * message.
 * 
 * @author tje, Alexander Koenig, Sebastian Nauck
 * 
 */
public class DeviceControlMessageProcessor
{

  /**
   * UPnP logger
   */
  protected static Logger logger = Logger.getLogger("upnp.soap");

  /**
   * Process control message.
   * 
   * @param parser
   *          HTTP parser
   * @param device
   *          The device that received the request
   * 
   * @return an error message or a query response message
   * 
   */
  public static HTTPMessageObject processControlMessage(HTTPParser parser, Device device)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("processControlMessage");
    }

    HTTPMessageObject request = parser.getHTTPMessageObject();
    InetSocketAddress serverAddress = request.getDestinationAddress();

    String urlPath = parser.getHostPath();
    DeviceService service = device.getServiceByRelativeURL(urlPath, "/control/");

    if (service == null)
    {
      logger.warn("no target service URL found. Seek URL = " + parser.getRequestURL().toString());

      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
    }

    // remove "
    String soapActionValue = parser.getValue(SOAPConstant.SOAPACTION).replaceAll("\"", "");

    if (soapActionValue.startsWith("urn:") && soapActionValue.indexOf(":service:") != -1)
    {
      // process action
      return processActionMessage(parser, device, service);
    }

    logger.warn("unknown request " + soapActionValue + " " + request.getBodyAsString());

    return null;
  }

  /** Verifies and stores the arguments of an received action message */
  protected static Action processReceivedArguments(Action action, Vector receivedArgumentList)
  {
    // clone action to handle concurrent requests
    Action result = (Action)action.clone();

    Argument[] localArgumentTable = result.getInArgumentTable();
    int localArgumentTableSize = localArgumentTable == null ? 0 : localArgumentTable.length;
    // compare argument tables
    if (receivedArgumentList.size() != localArgumentTableSize)
    {
      logger.warn("incorrect number of in arguments. " + "incoming = " + receivedArgumentList.size() + " local = " +
        localArgumentTable.length);
      return null;
    }
    // check, if all action in-arguments are also found as request arguments
    // this also checks allowed value list and ranges
    for (int i = 0; i < localArgumentTableSize; i++)
    {
      SOAPActionArgument receivedArgument = (SOAPActionArgument)receivedArgumentList.elementAt(i);
      Argument localArgument = localArgumentTable[i];
      if (!receivedArgument.getName().equalsIgnoreCase(localArgument.getName()))
      {
        logger.warn("Argument names differ: " + localArgument.getName());
        return null;
      } else
      {
        // check and set value
        try
        {
          localArgument.setValueFromString(receivedArgument.getValue());
        } catch (Exception exc)
        {
          logger.error("invalid invoke action request");
          logger.error("reason: invalid new value for " + localArgumentTable[i].getName());
          logger.error("reason: " + exc.getMessage());
          return null;
        }
      }
    }
    return result;
  }

  /**
   * Process invoke action message
   * 
   * @param requestParser
   *          Parser for the SOAP header
   * @param device
   *          The device that received the request
   * @param service
   *          Service that processes the request
   * 
   * @return an error message or a query response message
   */
  protected static HTTPMessageObject processActionMessage(HTTPParser requestParser, Device device, DeviceService service)
  {
    InetSocketAddress serverAddress = requestParser.getHTTPMessageObject().getDestinationAddress();

    // assume no encryption
    String bodyAsUTF8String = requestParser.getHTTPMessageObject().getBodyAsUTF8String();
    PersonalizedKeyObject keyObject = null;

    boolean validSignature = false;
    boolean isEncryptedBody = requestParser.isEncryptedBody();
    boolean isSymmetricKeySigned = false;

    String receivedNonce = null;
    String storedNonce = null;

    // check for personalization signature
    if (requestParser.hasField(HTTPConstant.X_PERSONALIZATION_KEY_ID) ||
      requestParser.hasField(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY))
    {
      SecuredPersonalizationService securedPersonalizationService =
        (SecuredPersonalizationService)device.getDeviceServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);
      // someone sent a personalized message but the device has no securedPersonalizationService
      // this is an implementation error
      if (securedPersonalizationService == null)
      {
        return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_501, device);
      }
      try
      {
        keyObject = securedPersonalizationService.getPersonalizedKeyObjectForHTTPParser(requestParser);
      } catch (ActionFailedException e)
      {
        return buildInvokeError(serverAddress, e.getErrorCode() + "", e.getErrorDescription(), device);
      }
      // check for symmetric key
      if (requestParser.hasField(HTTPConstant.X_PERSONALIZATION_KEY_ID) &&
        requestParser.hasField(HTTPConstant.X_PERSONALIZATION_SIGNATURE) &&
        requestParser.hasField(HTTPConstant.X_PERSONALIZATION_SEQUENCE))
      {
        isSymmetricKeySigned = true;
        // check sequence base
        long storedSequenceBase = keyObject.getSequenceBase();
        long receivedSequenceBase = requestParser.getNumericValue(HTTPConstant.X_PERSONALIZATION_SEQUENCE);

        if (receivedSequenceBase == -1 || receivedSequenceBase <= storedSequenceBase)
        {
          return buildInvokeError(serverAddress, "714", "InvalidSequence", device);
        }

        // update sequence base to prevent replay
        keyObject.setSequenceBase(receivedSequenceBase);
        if (isEncryptedBody)
        {
          byte[] decryptedBody =
            SymmetricCryptographyHelper.decryptWithAES(keyObject.getAESKey(),
              keyObject.getIV(),
              requestParser.getHTTPMessageObject().getBody());
          bodyAsUTF8String = StringHelper.byteArrayToUTF8String(decryptedBody);
        }
        // we have the original body, now we can check the signature
        String signatureContent =
          receivedSequenceBase + requestParser.getValue(HTTPConstant.X_PERSONALIZATION_KEY_ID) + bodyAsUTF8String;

        validSignature =
          DigestHelper.verifySHA1HMACForString(requestParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE),
            signatureContent,
            keyObject.getAESKey());

        if (!validSignature)
        {
          return buildInvokeError(serverAddress, "711", "Signature Failure", device);
        }
      }
      // public key signature
      if (requestParser.hasField(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY) &&
        requestParser.hasField(HTTPConstant.X_PERSONALIZATION_SIGNATURE))
      {
        // compare nonce
        receivedNonce = requestParser.getValue(HTTPConstant.X_NONCE);
        if (receivedNonce == null)
        {
          return buildInvokeError(serverAddress, "702", "Invalid nonce", device);
        }
        // asymmetric signature
        PersistentRSAPublicKey publicKey = requestParser.getPublicKey(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY);
        storedNonce = keyObject.getNonce();
        if (storedNonce == null || !receivedNonce.equals(storedNonce))
        {
          return buildInvokeError(serverAddress, "702", "Invalid nonce", device);
        }
        // clear nonce to prevent replay
        keyObject.setNonce(null);
        String publicKeyXMLDescription = SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey);
        String signatureContent = receivedNonce + publicKeyXMLDescription + bodyAsUTF8String;
        validSignature =
          PublicKeyCryptographyHelper.verifyRSASignatureForString(publicKey,
            signatureContent,
            requestParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE));
        if (!validSignature)
        {
          return buildInvokeError(serverAddress, "711", "Signature Failure", device);
        }
      }
    }

    // used to cache action response messages
    String requestBodyHashBase64 = null;

    // retrieve action name from header
    // retrieve action name to use soap control cache
    String soapAction = null;
    // retrieve action name from header
    if (requestParser.getMethod().equals(HTTPConstant.POST))
    {
      soapAction = requestParser.getValue(SOAPConstant.SOAPACTION);
    }

    if (requestParser.getMethod().equals(HTTPConstant.M_POST))
    {
      soapAction = requestParser.getValue("01-" + SOAPConstant.SOAPACTION);
    }

    if (soapAction != null)
    {
      int i = soapAction.lastIndexOf("#");
      if (i != -1)
      {
        soapAction = soapAction.substring(i + 1);
      }
      if (soapAction.endsWith("\""))
      {
        soapAction = soapAction.substring(0, soapAction.length() - 1);
      }

      // System.out.println("Soap action name from header is " + soapAction);

      Action action = service.getAction(soapAction);
      // cacheable action found
      if (action != null && action.isCacheable())
      {
        // build hash from request
        requestBodyHashBase64 =
          Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(bodyAsUTF8String));

        // search hash in cache hash table
        if (service.getControlMessageCache().containsKey(requestBodyHashBase64))
        {

          byte[] responseBodyByteArray = (byte[])service.getControlMessageCache().get(requestBodyHashBase64);
          String responseBody = StringHelper.byteArrayToUTF8String(responseBodyByteArray);

          // System.out.println("Found request " + service.toString() + "." + action.getName() +
          // " in cache, return cached response");

          // we already answered a similar request, so we return the cached response
          return SOAPMessageBuilder.createActionResponseMessage(serverAddress,
            responseBody,
            responseBodyByteArray,
            device.getServer(),
            // used for public key signature
            storedNonce,
            device.getDevicePrivateKey(),
            device.getDevicePublicKey(),
            // used for symmetric key signature
            (isSymmetricKeySigned ? keyObject : null),
            isEncryptedBody);
        }
      }
    }

    // parse body
    SOAPActionHandler soapActionHandler = new SOAPActionHandler();
    try
    {
      soapActionHandler.parse(bodyAsUTF8String);
    } catch (SAXException e)
    {
      System.out.println("Could not parse SOAP request: " + e.getMessage());
      // error parsing request
      return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_401, device);
    }
    if (soapActionHandler.isValid())
    {
      // check serviceType
      if (!soapActionHandler.getServiceType().equals(service.getServiceType()))
      {
        logger.warn("incorrect service type = " + soapActionHandler.getServiceType());
        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
      }
      // check action name from body
      Action action = service.getAction(soapActionHandler.getActionName());
      if (action == null)
      {
        logger.warn("incorrect action name = " + soapActionHandler.getActionName());
        return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_401, device);
      }

      // process arguments
      action = processReceivedArguments(action, soapActionHandler.getArgumentList());
      if (action == null)
      {
        return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_402, device);
      }

      // link action with parser that received the request
      action.setHTTPParser(requestParser);

      // send action to device
      if (logger.isInfoEnabled())
      {
        logger.info("sending action to device......");
      }
      if (service.invokeLocalAction(action))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("action successfully performed by device");
        }

        String responseBody = buildActionResponseBody(service, action, device);
        byte[] responseBodyByteArray = StringHelper.utf8StringToByteArray(responseBody);

        // a non-null value indicates that the action response is cacheable
        if (requestBodyHashBase64 != null)
        {
          service.getControlMessageCache().put(requestBodyHashBase64, responseBodyByteArray);
          // System.out.println("Store " + service.toString() + "." + action.getName() +
          // " in soap cache: " + requestBodyHashBase64 + " -> " + responseMessage.getBody().length
          // + " bytes");
          // System.out.println("Number of cache entries is " +
          // service.getControlMessageCache().size());
        }
        return SOAPMessageBuilder.createActionResponseMessage(serverAddress,
          responseBody,
          responseBodyByteArray,
          device.getServer(),
          // used for public key signature
          storedNonce,
          device.getDevicePrivateKey(),
          device.getDevicePublicKey(),
          // used for symmetric key signature
          (isSymmetricKeySigned ? keyObject : null),
          isEncryptedBody);
      } else
      {
        logger.warn("action cannot be performed by device");
        if (action.processingError())
        {
          logger.warn("errorCode:" + action.getErrorCode() + " errorDesc:" + action.getErrorDescription());

          return buildInvokeError(serverAddress,
            Integer.toString(action.getErrorCode()),
            action.getErrorDescription(),
            device);
        } else
        {
          return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_501, device);
        }
      }
    } else
    {
      return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_501, device);
    }
  }

  /**
   * This methods builds an error message for SOAP messages.
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param errorCode
   *          code which specifies the caused error
   * @param device
   *          Device that received the
   *          requestToolkit.getDefaultToolkit().getScreenSize().getHeight();
   * 
   * @return error message
   */
  protected static HTTPMessageObject buildInvokeError(InetSocketAddress serverAddress, String errorCode, Device device)
  {
    errorCode = SOAPMessageBuilder.buildErrorTag(errorCode);

    return SOAPMessageBuilder.createActionResponseErrorMessage(serverAddress,
      UPnPConstant.UPNP_ERROR,
      errorCode,
      device.getServer());
  }

  /**
   * This methods builds an error message for SOAP messages.
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param errorCode
   *          code which specifies the caused error
   * @param errorDescription
   *          short description of the caused error
   * @param device
   *          Device that received the request
   * 
   * @return error message
   */
  protected static HTTPMessageObject buildInvokeError(InetSocketAddress serverAddress,
    String errorCode,
    String errorDescription,
    Device device)
  {
    errorCode = SOAPMessageBuilder.buildErrorTag(errorCode, errorDescription);

    return SOAPMessageBuilder.createActionResponseErrorMessage(serverAddress,
      UPnPConstant.UPNP_ERROR,
      errorCode,
      device.getServer());
  }

  /**
   * This methods builds an action response message.
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param service
   *          The service that processed the request
   * @param action
   *          The action that handled the request
   * @param device
   *          The device that received the request
   * 
   * @return action's response message
   * 
   */
  protected static String buildActionResponseBody(DeviceService service, Action action, Device device)
  {
    // convert out arguments to string array
    String[] argName = new String[0];
    String[] argValue = new String[0];

    // build arrays with out argument names and valueStrings
    if (action.getOutArgumentTable() != null)
    {
      Argument[] outArguments = action.getOutArgumentTable();
      argName = new String[outArguments.length];
      argValue = new String[outArguments.length];
      for (int i = 0; i < outArguments.length; i++)
      {
        argName[i] = outArguments[i].getName();
        argValue[i] = outArguments[i].getValueAsString();
      }
    }
    return SOAPMessageBuilder.buildActionResponseBody(service.getServiceType(), action.getName(), argName, argValue);
  }

}

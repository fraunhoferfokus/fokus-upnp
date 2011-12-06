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
import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceControlMessageProcessor;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionArgument;
import de.fraunhofer.fokus.upnp.core.xml.SOAPActionHandler;
import de.fraunhofer.fokus.upnp.core_security.deviceSecurity.DeviceSecurityService;
import de.fraunhofer.fokus.upnp.core_security.helpers.ActionSecurityInfo;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.SOAPSignatureParser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateService;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.soap.SOAPConstant;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This class handles the processing and building of response message for incoming control messages.
 * This class also understands secured control messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SecuredDeviceControlMessageProcessor extends DeviceControlMessageProcessor
{

  /**
   * Process control message.
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param parser
   *          HTTP parser
   * @param request
   *          The SOAP request
   * @param device
   *          The device that received the request
   * 
   * @return an error message or a query response message
   * 
   */
  public static HTTPMessageObject processMessage(HTTPParser parser, Device device)
  {
    logger.info("processMessage");

    HTTPMessageObject request = parser.getHTTPMessageObject();
    InetSocketAddress serverAddress = request.getDestinationAddress();

    DeviceService service = device.getServiceByRelativeURL(parser.getHostPath(), UPnPConstant.SUFFIX_CONTROL);

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

  /**
   * Process invoke action message
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param service
   *          Service that processes the request
   * @param request
   *          The SOAP request
   * @param device
   *          The device that received the request
   * 
   * @return an error message or a query response message
   */
  protected static HTTPMessageObject processActionMessage(HTTPParser parser, Device device, DeviceService service)
  {
    InetSocketAddress serverAddress = parser.getHTTPMessageObject().getDestinationAddress();

    // body is UTF-8 encoded
    String body = parser.getHTTPMessageObject().getBodyAsUTF8String();

    // parse body
    SOAPActionHandler soapActionHandler = new SOAPActionHandler();
    try
    {
      soapActionHandler.parse(body);
    } catch (SAXException e)
    {
      System.out.println("Could not parse SOAP request: " + e.getMessage());
      // error parsing request
      return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_401, device);
    }
    ActionSecurityInfo securityInfo = null;
    SecuredTemplateService securedService = null;

    if (soapActionHandler.isValid())
    {
      // check if service is security aware
      if (service instanceof SecuredTemplateService)
      {
        securedService = (SecuredTemplateService)service;
        DeviceSecurityService deviceSecurityService = securedService.getSecuredDevice().getDeviceSecurityService();

        SOAPSignatureParser signatureParser = new SOAPSignatureParser(body);
        // check for signature
        if (signatureParser.containsSignature())
        {
          // TO-DO: verify controlURL

          // check public key signature
          if (signatureParser.isPublicKeySignature())
          {
            if (signatureParser.isValidPublicKeySignature())
            {
              if (signatureParser.getLifetimeSequenceBase().equals(deviceSecurityService.getLifetimeSequenceBase()))
              {
                securityInfo =
                  new ActionSecurityInfo(signatureParser.getPublicKey(), signatureParser.getLifetimeSequenceBase());
              } else
              {
                return buildInvokeError(serverAddress, "714", "Invalid Sequence", device);
              }
            } else
            {
              return buildInvokeError(serverAddress, "711", "Signature Failure", device);
            }
          }
          // check session key signature
          if (signatureParser.isSymmetricSignature())
          {
            boolean signatureFailure = true;
            // retrieve session
            String keyName = signatureParser.getKeyName();
            int deviceKeyID = Integer.parseInt(keyName);
            // try to get session
            Session session = securedService.getSecuredDevice().getSessionFromDeviceKeyID(deviceKeyID);
            if (session != null)
            {
              // verify session signature
              if (DigestHelper.verifySHA1HMACForString(signatureParser.getSignatureBase64(),
                signatureParser.getSignatureSource(),
                session.getSigningToDeviceKey()))
              {
                // verify freshness information
                if (signatureParser.getSequenceBase().equals(session.getSequenceBase()) &&
                  Integer.parseInt(signatureParser.getSequenceNumber()) > session.getCPSequenceNumber())
                {
                  // update sequence number for session
                  session.setCPSequenceNumber(Integer.parseInt(signatureParser.getSequenceNumber()));
                  securityInfo = new ActionSecurityInfo(deviceKeyID);
                  signatureFailure = false;
                } else
                {
                  return buildInvokeError(serverAddress, "714", "Invalid Sequence", device);
                }
              }
            }
            if (session == null)
            {
              return buildInvokeError(serverAddress, "781", "No Such Session", device);
            }
            if (signatureFailure)
            {
              return buildInvokeError(serverAddress, "711", "Signature Failure", device);
            }
          }
        }
      }
      // check serviceType
      if (!soapActionHandler.getServiceType().equals(service.getServiceType()))
      {
        logger.warn("incorrect service type = " + soapActionHandler.getServiceType());
        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
      }
      // check action
      Action action = service.getAction(soapActionHandler.getActionName());
      if (action == null)
      {
        logger.warn("incorrect action name = " + soapActionHandler.getActionName());
        return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_401, device);
      }
      // clone action to allow the independent handling of concurrent requests
      action = (Action)action.clone();

      // set address of server that received the request
      action.setHTTPParser(parser);

      Vector receivedArgumentTable = soapActionHandler.getArgumentList();
      Argument[] localArgumentTable = action.getInArgumentTable();
      int localArgumentTableSize = localArgumentTable == null ? 0 : localArgumentTable.length;
      // compare argument tables
      if (receivedArgumentTable.size() != localArgumentTableSize)
      {
        logger.warn("incorrect number of in arguments. " + "incoming = " + receivedArgumentTable.size() + " local = " +
          localArgumentTable.length);
        return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_402, device);
      }
      // check, if all action in-arguments are also found as request arguments
      // this also checks allowed value list and ranges
      for (int i = 0; i < localArgumentTableSize; i++)
      {
        SOAPActionArgument receivedArgument = (SOAPActionArgument)receivedArgumentTable.elementAt(i);
        Argument localArgument = localArgumentTable[i];

        if (!receivedArgument.getName().equals(localArgument.getName()))
        {
          logger.warn("Argument names differ: " + localArgument.getName());

          return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_402, device);
        } else
        {
          // check value
          try
          {
            localArgument.setValueFromString(receivedArgument.getValue());
          } catch (Exception exc)
          {
            logger.error("invalid invoke action request");
            logger.error("reason: invalid new value for " + localArgumentTable[i].getName());
            logger.error("reason: " + exc.getMessage());

            return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_402, device);
          }
        }
      }
      // send action to device
      logger.info("sending action to device......");

      // differentiate between security aware and security unaware services
      if (securedService != null)
      {
        // action listener always belongs to the service itself
        if (securedService.invokeSecuredLocalAction(securedService.getServiceId(), action, securityInfo))
        {
          logger.info("secured action successfully performed by device");

          String responseBody = buildActionResponseBody(service, action, device);
          byte[] responseBodyByteArray = StringHelper.utf8StringToByteArray(responseBody);

          return SOAPMessageBuilder.createActionResponseMessage(serverAddress,
            responseBody,
            responseBodyByteArray,
            device.getServer(),
            null,
            null,
            null,
            null,
            false);
        } else
        {
          logger.warn("secured action cannot be performed by device");
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
        if (service.invokeLocalAction(action))
        {
          logger.info("action successfully performed by device");

          String responseBody = buildActionResponseBody(service, action, device);
          byte[] responseBodyByteArray = StringHelper.utf8StringToByteArray(responseBody);

          return SOAPMessageBuilder.createActionResponseMessage(serverAddress,
            responseBody,
            responseBodyByteArray,
            device.getServer(),
            null,
            null,
            null,
            null,
            false);
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
      }
    } else
    {
      return buildInvokeError(serverAddress, UPnPConstant.SOAP_ERROR_501, device);
    }
  }

}

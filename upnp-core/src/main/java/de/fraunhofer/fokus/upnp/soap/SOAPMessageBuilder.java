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
package de.fraunhofer.fokus.upnp.soap;

import java.net.InetSocketAddress;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractAction;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersonalizedKeyObject;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class is used to build different SOAP messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SOAPMessageBuilder extends SOAPHeaderBuilder
{

  private static final String U_BEGIN_TAG                 = "<u:";

  private static final String U_END_TAG                   = "</u:";

  private static final String BEGIN_TAG                   = "<";

  private static final String CLOSE_TAG                   = ">";

  private static final String END_TAG                     = "</";

  private static final String ERROR_CODE_TAG_BEGIN        = "<errorCode>";

  private static final String ERROR_CODE_TAG_END          = "</errorCode>";

  private static final String ERROR_DESCRIPTION_TAG_BEGIN = "<errorDescription>";

  private static final String ERROR_DESCRIPTION_TAG_END   = "</errorDescription>";

  /**
   * Builds the action response body for an action.
   * 
   * @param serviceType
   *          Associated service type
   * @param actionName
   * @param argumentName
   *          Array with out argument names
   * @param argumentValue
   *          Array with out argument values
   * 
   * @return ActionResponseBody as UTF-8 string
   */
  public static String buildActionResponseBody(String serviceType,
    String actionName,
    String[] argumentName,
    String[] argumentValue)
  {
    StringBuffer innerResponseBody = new StringBuffer();

    innerResponseBody.append(U_BEGIN_TAG + actionName + "Response " + UPnPConstant.XMLNS_SERVICE + serviceType + "\"" +
      CLOSE_TAG + CommonConstants.NEW_LINE);

    for (int i = 0; i < argumentName.length; ++i)
    {
      innerResponseBody.append(XMLHelper.createTag(argumentName[i], StringHelper.escapeXMLIfNecessary(argumentValue[i])));
    }
    innerResponseBody.append(U_END_TAG + actionName + "Response" + CLOSE_TAG);

    return buildEnvelope(innerResponseBody.toString());
  }

  /**
   * Creates an ActionResponseMessage for a known body.
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param responseBodyByteArray
   *          Body of the response message
   * @param serverString
   *          OS/version a UPnP-Constant and the product version
   * @param aesKey
   *          a Secret AES-Key to encrypt the response if this is required
   * @param iv
   *          (Initialisation Vector) an array which is need when the response should become encrypt
   * @param privateKey
   *          to generate a signature for the response if this is required.So the Controlpoint can
   *          check integrity and if the data were manipulate during transmission
   * @param publicKey
   *          for the Header of the response. So the Controlpoint can search the information it is
   *          need to check the integrity and if the data were manipulate during transmission
   * 
   * @return The action response message
   */
  public static HTTPMessageObject createActionResponseMessage(InetSocketAddress serverAddress,
    String responseBody,
    byte[] responseBodyByteArray,
    String serverString,
    // used for public key signature
    String receivedNonce,
    RSAPrivateKey devicePrivateKey,
    RSAPublicKey devicePublicKey,
    // used for symmetric key signature
    PersonalizedKeyObject keyObject,
    boolean encryptBody)
  {
    Vector optionalHeaderLines = new Vector();
    String responseHeader = null;

    // symmetric signature
    if (keyObject != null)
    {
      String signatureContent = keyObject.getSequenceBase() + keyObject.getKeyID() + responseBody;

      // calculate signature
      byte[] signature = DigestHelper.calculateSHA1HMACForString(keyObject.getAESKey(), signatureContent);
      String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64));
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_KEY_ID, keyObject.getKeyID()));
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SEQUENCE,
        keyObject.getSequenceBase() + ""));

      if (encryptBody)
      {
        responseBodyByteArray =
          SymmetricCryptographyHelper.encryptWithAES(keyObject.getAESKey(), keyObject.getIV(), responseBodyByteArray);
        // build header
        optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_ENCRYPTION_TAG, "true"));
      }
    }
    // asymmetric signature
    if (devicePublicKey != null && devicePrivateKey != null && receivedNonce != null)
    {
      String publicKeyXMLDescription = SecurityHelper.buildRSAPublicKeyXMLDescription(devicePublicKey);
      String signatureContent = receivedNonce + publicKeyXMLDescription + responseBody;

      byte[] signatureAsByteArray =
        PublicKeyCryptographyHelper.calculateRSASignatureForString(devicePrivateKey, signatureContent);
      String signatureBase64 = Base64Helper.byteArrayToBase64(signatureAsByteArray);

      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_NONCE, receivedNonce));
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY,
        publicKeyXMLDescription));
      optionalHeaderLines.add(HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64));
    }
    responseHeader =
      SOAPHeaderBuilder.buildActionResponseHeader(HTTPConstant.HTTP_OK_1_1,
        responseBodyByteArray.length,
        DateTimeHelper.getRFC1123Date(),
        serverString,
        optionalHeaderLines);

    // build message
    return new HTTPMessageObject(responseHeader, responseBodyByteArray, serverAddress);
  }

  /**
   * method creates a ActionResponseErrorMessage for the Device
   * 
   * @param serverAddress
   *          Address of server that received the request
   * @param faultString
   *          parameter which describes the ocurred error
   * @param upnpError
   *          the corresponding upnp error
   * @param date
   *          date when response was created
   * @param serverString
   *          OS/version a UPnP-Constant and the product version
   * @return ActionResponseErrorMessage
   */
  public static HTTPMessageObject createActionResponseErrorMessage(InetSocketAddress serverAddress,
    String faultString,
    String upnpError,
    String serverString)
  {
    // get body of message (first build fault wrap then body wrap)
    String bodyString = buildEnvelope(buildActionResponseErrorInnerBody(faultString, upnpError));

    // encode with UTF-8
    byte[] responseBody = StringHelper.utf8StringToByteArray(bodyString);

    int bytesInBody = responseBody.length;

    // get header
    String responseHeader =
      SOAPHeaderBuilder.buildActionResponseHeader(HTTPConstant.HTTP_ERROR_500,
        bytesInBody,
        DateTimeHelper.getRFC1123Date(),
        serverString,
        null);

    // build message
    return new HTTPMessageObject(responseHeader, responseBody, serverAddress);
  }

  /**
   * Creates SOAPBody message for UPnPInvokeAction message
   * 
   * @param serviceType
   *          service type
   * @param action
   *          The action
   * @return SOAPBody message
   */
  public static String buildActionRequestInnerBody(String serviceType, AbstractAction action)
  {
    StringBuffer bodyValue = new StringBuffer();
    bodyValue.append(U_BEGIN_TAG);
    bodyValue.append(action.getName());
    bodyValue.append(" ");
    bodyValue.append(UPnPConstant.XMLNS_BEGIN_TAG);
    bodyValue.append(serviceType);
    bodyValue.append(UPnPConstant.XMLNS_END_TAG);
    bodyValue.append(CLOSE_TAG);

    Argument[] inArgs = action.getInArgumentTable();
    if (inArgs != null)
    {
      for (int i = 0; i < inArgs.length; i++)
      {
        bodyValue.append(XMLHelper.createTag(inArgs[i].getName(),
          StringHelper.escapeXMLIfNecessary(inArgs[i].getValueAsString())));
      }
    }

    bodyValue.append(U_END_TAG);
    bodyValue.append(action.getName());
    bodyValue.append(CLOSE_TAG);
    // logger.debug(bodyValue.toString());

    // System.out.println("Request body: " + bodyValue);

    return bodyValue.toString();
  }

  /**
   * method builds the body of a soap message if the content between the <Body>-Tags is given
   * 
   * @param innerBody
   *          content between <Body>-Tags
   * @return body of SoapMessage
   */
  public static String buildEnvelope(String innerBody)
  {
    StringBuffer body = new StringBuffer();

    body.append(XMLConstant.XML_VERSION + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.ENVELOPE_START_TAG + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.BODY_START_TAG + CommonConstants.NEW_LINE);
    body.append(innerBody + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.BODY_END_TAG + CommonConstants.NEW_LINE);
    body.append(SOAPConstant.ENVELOPE_END_TAG);

    // logger.debug(body.toString());

    return body.toString();
  }

  /**
   * method builds the body-content <FAULT>-Tag wrap
   * 
   * @param faultString
   *          parameter which describes the ocurred error
   * @param upnpError
   *          the corresponding upnp error
   * @return FaultTagWarp
   */
  public static String buildActionResponseErrorInnerBody(String faultString, String upnpError)
  {
    StringBuffer fault = new StringBuffer();
    fault.append(SOAPConstant.FAULT_START_TAG + CommonConstants.NEW_LINE);
    fault.append(SOAPConstant.FAULTCODE_START_TAG + SOAPConstant.S_CLIENT + SOAPConstant.FAULTCODE_END_TAG +
      CommonConstants.NEW_LINE);
    fault.append(SOAPConstant.FAULTSTRING_START_TAG + faultString + SOAPConstant.FAULTSTRING_END_TAG +
      CommonConstants.NEW_LINE);
    fault.append(SOAPConstant.DETAIL_START_TAG + CommonConstants.NEW_LINE);
    fault.append(upnpError + CommonConstants.NEW_LINE);
    fault.append(SOAPConstant.DETAIL_END_TAG + CommonConstants.NEW_LINE);
    fault.append(SOAPConstant.FAULT_END_TAG);
    // logger.debug(fault.toString());

    return fault.toString();
  }

  /**
   * Creates an error response message for UPnPInvoke message
   * 
   * @param errorCode
   *          UPnP control error code
   * @return UPnPInvoke error response message
   */
  public static String buildErrorTag(String errorCode)
  {
    String errorDescription;

    if (errorCode.equals(UPnPConstant.SOAP_ERROR_401))
    {
      errorDescription = UPnPConstant.SOAP_ERROR_401_DESCRIPTION;
    } else if (errorCode.equals(UPnPConstant.SOAP_ERROR_402))
    {
      errorDescription = UPnPConstant.SOAP_ERROR_402_DESCRIPTION;
    } else if (errorCode.equals(UPnPConstant.SOAP_ERROR_404))
    {
      errorDescription = UPnPConstant.SOAP_ERROR_404_DESCRIPTION;
    } else if (errorCode.equals(UPnPConstant.SOAP_ERROR_501))
    {
      errorDescription = UPnPConstant.SOAP_ERROR_501_DESCRIPTION;
    } else if (errorCode.equals(UPnPConstant.SOAP_ERROR_600))
    {
      errorDescription = UPnPConstant.SOAP_ERROR_600_DESCRIPTION;
    } else
    {
      errorDescription = "no available error description";
    }

    return SOAPMessageBuilder.buildErrorTag(errorCode, errorDescription);
  }

  /**
   * Creates an error response message for UPnPInvoke message
   * 
   * @param errorCode
   *          UPnP control error code
   * @param errorDescription
   *          short description of the caused error
   * @return UPnPInvoke error response message
   */
  public static String buildErrorTag(String errorCode, String errorDescription)
  {
    StringBuffer error = new StringBuffer();

    error.append(BEGIN_TAG + UPnPConstant.UPNP_ERROR + " " + UPnPConstant.XMLNS_ERROR + CLOSE_TAG +
      CommonConstants.NEW_LINE);
    error.append(ERROR_CODE_TAG_BEGIN + errorCode + ERROR_CODE_TAG_END + CommonConstants.NEW_LINE);
    error.append(ERROR_DESCRIPTION_TAG_BEGIN + errorDescription + ERROR_DESCRIPTION_TAG_END + CommonConstants.NEW_LINE);
    error.append(END_TAG + UPnPConstant.UPNP_ERROR + CLOSE_TAG);

    // logger.debug(error.toString());

    return error.toString();
  }

  /**
   * Creates a string representation of a URL usable for signed actions
   */
  public static String buildFreshnessURL(URL controlURL)
  {
    int port = controlURL.getPort();
    if (port == -1)
    {
      port = CommonConstants.HTTP_DEFAULT_PORT;
    }

    String result = controlURL.getHost() + ":" + port + URLHelper.getURLPath(controlURL);

    return result;
  }

}

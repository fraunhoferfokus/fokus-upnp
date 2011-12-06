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
package de.fraunhofer.fokus.upnp.http;

import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.soap.SOAPConstant;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.RSAPublicKeyParser;

/**
 * This class is used to parse header lines in HTTP messages.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class HTTPParser
{
  protected static Logger   logger         = Logger.getLogger("upnp.http");

  /** Message object associated with this parser */
  private HTTPMessageObject httpMessageObject;

  /** Method of request (GET, POST etc.) */
  protected String          method;

  private String            hostPath;

  private String            methodValue;

  private String            hostIP;

  private int               hostPort;

  private int               responseCode;

  private String            responseDescription;

  private URL               requestURL;

  /** A hashtable containing all header tags, including the final colon */
  private Hashtable         fieldTable     = new Hashtable();

  /** The first line has the format of a HTTP request */
  private boolean           httpMethodLine = false;

  /** The first line has the format of a HTTP response */
  private boolean           httpResponse   = false;

  /** HTTP version */
  private String            httpVersion    = "1.1";

  /** Parses a HTTP message header */
  public void parse(HTTPMessageObject message)
  {
    this.httpMessageObject = message;
    // error handling is done in other parse method
    parse(message != null ? message.getHeader() : null);
  }

  /** Parses a HTTP message header */
  public void parse(String messageHeader)
  {
    method = null;
    hostIP = null;
    hostPath = null;
    methodValue = null;
    hostPort = -1;
    responseCode = -1;
    responseDescription = null;
    fieldTable.clear();
    httpMethodLine = false;
    httpResponse = false;
    requestURL = null;

    if (messageHeader == null || messageHeader.length() == 0)
    {
      return;
    }

    StringTokenizer tokenizer = new StringTokenizer(messageHeader, CommonConstants.NEW_LINE);

    if (tokenizer.hasMoreTokens())
    {
      String methodLine = tokenizer.nextToken();
      // check for response line
      if (methodLine.toUpperCase().startsWith(CommonConstants.HTTP_1_x))
      {
        // retrieve number and description
        int codeIndex = methodLine.indexOf(" ");
        if (codeIndex != -1 && codeIndex < methodLine.length() - 1)
        {
          String codeString = methodLine.substring(codeIndex + 1).trim();
          int descriptionIndex = codeString.indexOf(" ");
          if (descriptionIndex != -1 && descriptionIndex < codeString.length() - 1)
          {
            try
            {
              responseCode = Integer.parseInt(codeString.substring(0, descriptionIndex));
              responseDescription = codeString.substring(descriptionIndex + 1).trim();
              httpResponse = true;
            } catch (Exception e)
            {
            }
          }
        }
      } else
      {
        // handle request
        int pathIndex = methodLine.indexOf(" ");
        if (pathIndex != -1 && pathIndex < methodLine.length() - 1)
        {
          // extract method
          method = methodLine.substring(0, pathIndex);
          // extract path
          String pathString = methodLine.substring(pathIndex + 1).trim();
          int httpIndex = pathString.indexOf(" ");
          if (httpIndex != -1 && httpIndex < pathString.length() - 1)
          {
            hostPath = pathString.substring(0, httpIndex);
            methodValue = hostPath;
            httpMethodLine = true;
          }
          // extract version
          int index = methodLine.lastIndexOf("/");
          if (index != -1 && index < methodLine.length() - 1)
          {
            httpVersion = methodLine.substring(index + 1);
          }
        }
      }
      // go through all header lines and store values
      while (tokenizer.hasMoreTokens())
      {
        String line = tokenizer.nextToken();
        int colonIndex = line.indexOf(":");
        if (colonIndex != -1)
        {
          // assume empty tag (e.g., EXT:)
          // store tag with colon
          String tag = line.toUpperCase();
          // store value
          String value = "";
          // check for non-empty tags
          if (colonIndex < line.length() - 1)
          {
            tag = line.substring(0, colonIndex + 1).toUpperCase();
            value = line.substring(colonIndex + 1).trim();
          }

          // System.out.println("Store in table: " + tag + "->" + value);
          fieldTable.put(tag, value);

          // handle special tags
          if (tag.equals(CommonConstants.HOST))
          {
            int portIndex = value.indexOf(":");
            if (portIndex != -1 && portIndex < value.length() - 1)
            {
              hostIP = value.substring(0, portIndex);
              try
              {
                hostPort = Integer.parseInt(value.substring(portIndex + 1).trim());
              } catch (Exception e)
              {
              }
            } else
            {
              hostIP = value;
              hostPort = CommonConstants.HTTP_DEFAULT_PORT;
            }
          }
        }
      }
    }
  }

  /**
   * Retrieves the hostIP.
   * 
   * @return The hostIP
   */
  public String getHostIP()
  {
    return hostIP;
  }

  /**
   * Retrieves the host value.
   * 
   * @return The host value (host and port)
   */
  public String getHost()
  {
    return hostIP + ":" + hostPort;
  }

  /**
   * Retrieves the hostPath.
   * 
   * @return The hostPath
   */
  public String getHostPath()
  {
    return hostPath;
  }

  /**
   * Sets the hostPath.
   * 
   * @param hostPath
   *          The new value for hostPath
   */
  public void setHostPath(String hostPath)
  {
    this.hostPath = hostPath;
  }

  /**
   * Retrieves the hostPort.
   * 
   * @return The hostPort
   */
  public int getHostPort()
  {
    return hostPort;
  }

  /**
   * Retrieves the method.
   * 
   * @return The method
   */
  public String getMethod()
  {
    return method;
  }

  /**
   * Retrieves the responseCode.
   * 
   * @return The responseCode
   */
  public int getResponseCode()
  {
    return responseCode;
  }

  /**
   * Retrieves the responseDescription.
   * 
   * @return The responseDescription
   */
  public String getResponseDescription()
  {
    return responseDescription;
  }

  /** Retrieves the URL of the request */
  public URL getRequestURL()
  {
    if (requestURL != null)
    {
      return requestURL;
    }

    if (httpMethodLine)
    {
      try
      {
        requestURL = new URL("HTTP", hostIP, hostPort, hostPath);
      } catch (Exception e)
      {
      }
    }
    return requestURL;
  }

  /**
   * Checks if this is a valid HTTP request.
   * 
   * @return True if this is a request, false otherwise
   */
  public boolean isHTTPRequest()
  {
    return httpMethodLine && hostIP != null && hostPort != -1;
  }

  /**
   * Checks if this is a valid HTTP message.
   * 
   * @return True if this is a HTTP message with a method line and a host, false otherwise
   */
  public boolean isHTTPMessage()
  {
    return httpMethodLine && hostIP != null && hostPort != -1;
  }

  /**
   * Checks if this is a valid GET request.
   * 
   * @return True if this is a GET request, false otherwise
   */
  public boolean isGETRequest()
  {
    return isHTTPRequest() && method.equalsIgnoreCase("GET") && getRequestURL() != null;
  }

  /**
   * Checks if this is a valid HEAD request.
   * 
   * @return True if this is a HEAD request, false otherwise
   */
  public boolean isHEADRequest()
  {
    return isHTTPRequest() && method.equalsIgnoreCase("HEAD") && getRequestURL() != null;
  }

  /**
   * Checks if this is a HTTP 1.0 request.
   * 
   * @return True if this is a 1.0 request, false otherwise
   */
  public boolean isHTTP_1_0_Request()
  {
    return isHTTPRequest() && httpVersion.equals("1.0");
  }

  /**
   * Checks if this is an invalid SOAP request.
   * 
   * @return True if this is an invalid SOAP request, false otherwise
   */
  public boolean isInvalidSOAPRequest()
  {
    // return true if the method is POST or M-POST but something else is missing
    return !isSOAPRequest() && method != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("M-POST"));
  }

  /**
   * Checks if this is a valid POST or M-POST request.
   * 
   * @return True if this is a SOAP request, false otherwise
   */
  public boolean isSOAPRequest()
  {
    // may be extended to check utf-8 and other namespace numberings for M-POST
    return isHTTPRequest() &&
      hasField(CommonConstants.CONTENT_LENGTH) &&
      hasField(HTTPConstant.CONTENT_TYPE) &&
      getValue(HTTPConstant.CONTENT_TYPE).startsWith(HTTPConstant.CONTENT_TYPE_TEXT_XML) &&
      (method.equalsIgnoreCase("POST") && hasField(SOAPConstant.SOAPACTION) || method.equalsIgnoreCase("M-POST") &&
        hasField(HTTPConstant.MAN) && hasField("01-" + SOAPConstant.SOAPACTION)) && getRequestURL() != null;
  }

  /**
   * Checks if this is a valid SUBSCRIBE response message.
   * 
   * @return True if this is a SUBSCRIBE response message, false otherwise
   */
  public boolean isSUBSCRIBEResponseMessage()
  {
    return isHTTPOKResponse() && hasField(HTTPConstant.SERVER) && hasField(GENAConstant.SID) &&
      hasField(HTTPConstant.TIMEOUT);
  }

  /**
   * Checks if this is a valid SUBSCRIBE message.
   * 
   * @return True if this is a SUBSCRIBE message, false otherwise
   */
  public boolean isSUBSCRIBEMessage()
  {
    return isHTTPMessage() && method.equalsIgnoreCase(CommonConstants.SUBSCRIBE) &&
      hasField(CommonConstants.CALLBACK) && hasField(GENAConstant.NT) && !hasField(GENAConstant.SID) &&
      getValue(GENAConstant.NT).equalsIgnoreCase(GENAConstant.UPNP_EVENT);
  }

  /**
   * Checks if this is a valid secured SUBSCRIBE message.
   * 
   * @return True if this is a secured SUBSCRIBE message, false otherwise
   */
  public boolean isSecuredSUBSCRIBEMessage()
  {
    return isHTTPMessage() &&
      method.equalsIgnoreCase(CommonConstants.SUBSCRIBE) &&
      hasField(CommonConstants.CALLBACK) &&
      hasField(GENAConstant.NT) &&
      hasField(HTTPConstant.X_SIGNATURE) &&
      (hasField(HTTPConstant.X_NONCE) && hasField(HTTPConstant.X_PUBLIC_KEY) || hasField(HTTPConstant.X_KEY_ID) &&
        hasField(HTTPConstant.X_SEQUENCE)) && !hasField(GENAConstant.SID) &&
      getValue(GENAConstant.NT).equalsIgnoreCase(GENAConstant.UPNP_EVENT);
  }

  /**
   * Checks if this is a valid RESUBSCRIBE message.
   * 
   * @return True if this is a RESUBSCRIBE message, false otherwise
   */
  public boolean isRESUBSCRIBEMessage()
  {
    return isHTTPMessage() && method.equalsIgnoreCase(CommonConstants.SUBSCRIBE) && hasField(GENAConstant.SID) &&
      !hasField(CommonConstants.CALLBACK) && !hasField(GENAConstant.NT);
  }

  /**
   * Checks if this is a valid secured RESUBSCRIBE message.
   * 
   * @return True if this is a secured RESUBSCRIBE message, false otherwise
   */
  public boolean isSecuredRESUBSCRIBEMessage()
  {
    return isHTTPMessage() &&
      method.equalsIgnoreCase(CommonConstants.SUBSCRIBE) &&
      hasField(GENAConstant.SID) &&
      hasField(HTTPConstant.X_SIGNATURE) &&
      (hasField(HTTPConstant.X_NONCE) && hasField(HTTPConstant.X_PUBLIC_KEY) || hasField(HTTPConstant.X_KEY_ID) &&
        hasField(HTTPConstant.X_SEQUENCE)) && !hasField(CommonConstants.CALLBACK) && !hasField(GENAConstant.NT);
  }

  /**
   * Checks if this is a valid UNSUBSCRIBE message.
   * 
   * @return True if this is a UNSUBSCRIBE message, false otherwise
   */
  public boolean isUNSUBSCRIBEMessage()
  {
    return isHTTPMessage() && method.equalsIgnoreCase(CommonConstants.UNSUBSCRIBE) && !hasField(HTTPConstant.TIMEOUT) &&
      hasField(GENAConstant.SID);
  }

  /**
   * Checks if this is a valid secured UNSUBSCRIBE message.
   * 
   * @return True if this is a secured UNSUBSCRIBE message, false otherwise
   */
  public boolean isSecuredUNSUBSCRIBEMessage()
  {
    return isHTTPMessage() &&
      method.equalsIgnoreCase(CommonConstants.UNSUBSCRIBE) &&
      !hasField(HTTPConstant.TIMEOUT) &&
      hasField(GENAConstant.SID) &&
      hasField(HTTPConstant.X_SIGNATURE) &&
      (hasField(HTTPConstant.X_NONCE) && hasField(HTTPConstant.X_PUBLIC_KEY) || hasField(HTTPConstant.X_KEY_ID) &&
        hasField(HTTPConstant.X_SEQUENCE)) && !hasField(CommonConstants.CALLBACK) && !hasField(GENAConstant.NT);
  }

  /**
   * Checks if this is a valid event NOTIFY message.
   * 
   * @return True if this is an event NOTIFY message, false otherwise
   */
  public boolean isEventNOTIFYMessage()
  {
    return isHTTPMessage() && method.equalsIgnoreCase(CommonConstants.NOTIFY) && hasField(HTTPConstant.CONTENT_TYPE) &&
      hasField(CommonConstants.CONTENT_LENGTH) && hasField(GENAConstant.NT) &&
      getValue(GENAConstant.NT).equalsIgnoreCase(GENAConstant.UPNP_EVENT) && hasField(GENAConstant.NTS) &&
      getValue(GENAConstant.NTS).equalsIgnoreCase(GENAConstant.UPNP_PROPCHANGE) && hasField(GENAConstant.SID) &&
      hasField(GENAConstant.SEQ) && isNumericValue(GENAConstant.SEQ);
  }

  /**
   * Checks if this is a valid multicast event NOTIFY message.
   * 
   * @return True if this is a multicast event NOTIFY message, false otherwise
   */
  public boolean isMulticastEventNOTIFYMessage()
  {
    return isHTTPMessage() && method.equalsIgnoreCase(CommonConstants.NOTIFY) && hasField(HTTPConstant.CONTENT_TYPE) &&
      hasField(CommonConstants.CONTENT_LENGTH) && hasField(GENAConstant.NT) &&
      getValue(GENAConstant.NT).equalsIgnoreCase(GENAConstant.UPNP_EVENT) && hasField(GENAConstant.NTS) &&
      getValue(GENAConstant.NTS).equalsIgnoreCase(GENAConstant.UPNP_PROPCHANGE) && hasField(GENAConstant.SEQ) &&
      isNumericValue(GENAConstant.SEQ);
  }

  /**
   * Checks if this is a valid NOTIFY alive message.
   * 
   * @return True if this is a NOTIFY alive message, false otherwise
   */
  public boolean isNOTIFYAliveMessage()
  {
    return isHTTPMessage() && hasField(HTTPConstant.CACHE_CONTROL) && hasField(CommonConstants.LOCATION) &&
      hasField(GENAConstant.NT) && hasField(GENAConstant.NTS) &&
      getValue(GENAConstant.NTS).equalsIgnoreCase(SSDPConstant.SSDP_ALIVE) && hasField(HTTPConstant.SERVER) &&
      hasField(CommonConstants.USN) && getMethodValue().equals("*");
  }

  /**
   * Checks if this is a valid NOTIFY byebye message.
   * 
   * @return True if this is a NOTIFY byebye message, false otherwise
   */
  public boolean isNOTIFYByeByeMessage()
  {
    return isHTTPMessage() && hasField(GENAConstant.NT) && hasField(GENAConstant.NTS) &&
      getValue(GENAConstant.NTS).equalsIgnoreCase(SSDPConstant.SSDP_BYEBYE) && hasField(CommonConstants.USN) &&
      getMethodValue().equals("*");
  }

  /**
   * Checks if this is a valid M-SEARCH message.
   * 
   * @return True if this is a M-SEARCH message, false otherwise
   */
  public boolean isMSEARCHMessage()
  {
    return isHTTPMessage() && hasField(HTTPConstant.MAN) && hasField(HTTPConstant.MX) &&
      isNumericValue(HTTPConstant.MX) && hasField(SSDPConstant.ST);
  }

  /**
   * Checks if this is a valid M-SEARCH response message.
   * 
   * @return True if this is a M-SEARCH response message, false otherwise
   */
  public boolean isMSEARCHResponseMessage()
  {
    return isHTTPOKResponse() && hasField(HTTPConstant.CACHE_CONTROL) && hasField(HTTPConstant.EXT) &&
      hasField(CommonConstants.LOCATION) && hasField(HTTPConstant.SERVER) && hasField(SSDPConstant.ST) &&
      hasField(CommonConstants.USN);
  }

  /**
   * Checks if this is a valid HTTP response
   * 
   * @return True if this is a response, false otherwise
   */
  public boolean isHTTPResponse()
  {
    return httpResponse;
  }

  /**
   * Checks if this is a HTTP OK response.
   * 
   * @return True if this is a valid response, false otherwise
   */
  public boolean isHTTPOKResponse()
  {
    return isHTTPResponse() && responseCode == 200;
  }

  /**
   * Checks if this is a HTTP error response.
   * 
   * @return True if this is an error response, false otherwise
   */
  public boolean isHTTPErrorResponse()
  {
    return isHTTPResponse() && responseCode != 200;
  }

  /** Checks the existence of a certain tag. */
  public boolean hasField(String tag)
  {
    return fieldTable.containsKey(tag.toUpperCase());
  }

  /** Retrieves the value for a certain tag. */
  public String getValue(String tag)
  {
    if (!fieldTable.containsKey(tag))
    {
      return null;
    }

    return fieldTable.get(tag.toUpperCase()).toString();
  }

  /** Retrieves the numeric value for a certain tag. */
  public long getNumericValue(String tag)
  {
    if (!fieldTable.containsKey(tag))
    {
      return -1;
    }

    try
    {
      return Long.parseLong(fieldTable.get(tag.toUpperCase()).toString());
    } catch (Exception e)
    {

    }
    return -1;
  }

  /** Checks if the value for a certain tag is a numeric value. */
  public boolean isNumericValue(String tag)
  {
    if (!fieldTable.containsKey(tag))
    {
      return false;
    }

    // try to convert to number
    try
    {
      Long.parseLong(fieldTable.get(tag.toUpperCase()).toString());
      return true;
    } catch (Exception e)
    {
    }
    return false;
  }

  /**
   * Retrieves the boolean value for a certain tag. Values are regarded to be true for the
   * following, case-insensitive values: true, yes, 1
   */
  public boolean getBooleanValue(String tag)
  {
    if (!fieldTable.containsKey(tag))
    {
      return false;
    }

    String value = fieldTable.get(tag.toUpperCase()).toString().trim();

    return StringHelper.stringToBoolean(value);
  }

  /**
   * Parses a field with multiple values (e.g., text/xml; charset="utf-8").
   * 
   * @param fieldValue
   *          The field value
   * 
   * @return All found values separated by ";"
   */
  public Vector getFieldParts(String fieldValue)
  {
    Vector result = new Vector();
    StringTokenizer tokenizer = new StringTokenizer(";");
    while (tokenizer.hasMoreTokens())
    {
      result.add(tokenizer.nextToken().trim());
    }
    return result;
  }

  /** Checks if the header contains a Connection: Close header. */
  public boolean hasConnectionCloseTag()
  {
    return fieldTable.containsKey(HTTPConstant.CONNECTION_HEADER) &&
      ((String)fieldTable.get(HTTPConstant.CONNECTION_HEADER)).equalsIgnoreCase("close");
  }

  /**
   * Retrieves the methodValue.
   * 
   * @return The methodValue
   */
  public String getMethodValue()
  {
    return methodValue;
  }

  /**
   * Retrieves the associated HTTP messageObject.
   * 
   * @return The messageObject
   */
  public HTTPMessageObject getHTTPMessageObject()
  {
    return httpMessageObject;
  }

  /**
   * Searches the key contained in the SOAP header
   * 
   * @param keyTag
   *          Tag to search for (e.g., HTTPConstant.PUBLIC_KEY)
   * 
   * @return The public key or null
   * 
   */
  public PersistentRSAPublicKey getPublicKey(String keyTag)
  {
    if (hasField(keyTag))
    {
      RSAPublicKeyParser parser = new RSAPublicKeyParser();
      try
      {
        parser.parse(getValue(keyTag));
      } catch (SAXException e)
      {
      }
      return parser.getPublicKey();
    }
    return null;
  }

  /**
   * Checks if the body of this message is encrypted
   * 
   * @param httpParser
   *          SoapHeaderParser object
   * @param device
   *          Device that received the request
   * 
   * @return true if the body is encrypted false otherwise
   * 
   */
  public boolean isEncryptedBody()
  {
    return getBooleanValue(HTTPConstant.X_ENCRYPTION_TAG);
  }
}

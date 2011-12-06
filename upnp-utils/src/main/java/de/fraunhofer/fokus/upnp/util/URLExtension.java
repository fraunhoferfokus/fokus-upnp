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
package de.fraunhofer.fokus.upnp.util;

import java.net.URL;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;

/** This class provides static methods for URL extension in device descriptions. */
public class URLExtension
{

  /**
   * Extends a URL for forwarding.
   * 
   * @param urlString
   *          The original URL
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   */
  public static String extendURL(String urlString,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    try
    {
      URL url = new URL(urlString);

      String host = url.getHost();
      int oldPort = url.getPort();
      if (oldPort == -1)
      {
        oldPort = CommonConstants.HTTP_DEFAULT_PORT;
      }

      String newFile = "/" + outgoingInterfaceAddress + "/" + host + "/" + oldPort + url.getFile();

      return new URL(url.getProtocol(), incomingInterfaceAddress, incomingInterfacePort, newFile).toExternalForm();
    } catch (Exception ex)
    {
    }
    return urlString;
  }

  /**
   * Extends a relative URL for forwarding.
   * 
   * @param urlString
   *          The original URL
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param baseURL
   *          Base URL for relative URLs
   */
  public static String extendRelativeURL(String urlString,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    String baseURL)
  {
    // baseURL goes into new path
    String newFile = "/" + outgoingInterfaceAddress + "/" + baseURL.replace(':', '/');

    // add original path
    if (urlString.startsWith("/"))
    {
      newFile += urlString;
    } else
    {
      newFile += "/" + urlString;
    }

    try
    {
      return new URL("http", incomingInterfaceAddress, incomingInterfacePort, newFile).toExternalForm();
    } catch (Exception ex)
    {
    }
    return urlString;
  }

  /**
   * Extends the URL in a tag.
   * 
   * @param message
   *          The original message
   * @param upperCaseMessage
   *          The original message in upper case
   * @param tag
   *          The tag in upper case
   * @param startIndex
   *          The index that should be searched from
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param baseURL
   *          Base URL for relative URLs
   * @return The changed message
   */
  public static String extendURLInTag(String message,
    String upperCaseMessage,
    String tag,
    int startIndex,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    String baseURL)
  {
    int tagStart = upperCaseMessage.indexOf("<" + tag, startIndex);
    int tagEnd = upperCaseMessage.indexOf(">", tagStart);
    int endTagStart = upperCaseMessage.indexOf("</" + tag + ">", tagEnd);

    if (tagStart != -1 && tagEnd != -1 && endTagStart != -1 && endTagStart > tagEnd + 1)
    {
      String value = message.substring(tagEnd + 1, endTagStart);
      if (URLHelper.isRelativeURL(value))
      {
        return message.substring(0, tagEnd + 1) +
          URLExtension.extendRelativeURL(value,
            outgoingInterfaceAddress,
            incomingInterfaceAddress,
            incomingInterfacePort,
            baseURL) + message.substring(endTagStart);
      } else
      {
        return message.substring(0, tagEnd + 1) +
          extendURL(value, outgoingInterfaceAddress, incomingInterfaceAddress, incomingInterfacePort) +
          message.substring(endTagStart);
      }
    }
    return message;
  }

  /**
   * Extends address and port in a LOCATION header with a new address and port
   * 
   * @param message
   *          The original message
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   */
  public static String extendLocation(String message,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    String currentLocation = HTTPMessageHelper.getHeaderLine(message, CommonConstants.LOCATION);
    if (currentLocation != null)
    {
      // replace location in message
      return HTTPMessageHelper.replaceHeaderLine(message, CommonConstants.LOCATION, extendURL(currentLocation,
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort));
    }
    // if no location is found, do nothing
    return message;
  }

  /**
   * Extends the CALLBACK header in a SUBSCRIBE message.
   * 
   * @param header
   *          The original header
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param udpCallback
   *          Flag to extend the UDP callback
   * 
   * @return The new header
   * 
   */
  private static String extendCallback(String header,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    boolean udpCallback)
  {
    String headerTag = udpCallback ? CommonConstants.UDP_CALLBACK : CommonConstants.CALLBACK;
    String value = HTTPMessageHelper.getHeaderLine(header, headerTag);
    if (value != null)
    {
      try
      {
        String newValue = "";
        // replace all callback URLs
        StringTokenizer urlTokenizer = new StringTokenizer(value, ">");
        while (urlTokenizer.hasMoreTokens())
        {
          try
          {
            String url = urlTokenizer.nextToken();
            // remove < and leading spaces
            url = url.substring(url.indexOf("<") + 1).trim();

            newValue +=
              "<" + extendURL(url, outgoingInterfaceAddress, incomingInterfaceAddress, incomingInterfacePort) + ">";
          } catch (Exception ex)
          {
            System.out.println("ERROR in tokenizing:" + ex.getMessage());
            return header;
          }
        }
        return HTTPMessageHelper.replaceHeaderLine(header, headerTag, newValue);
      } catch (Exception ex)
      {
      }
    }
    return header;
  }

  /**
   * Extends the CALLBACK header in a SUBSCRIBE message.
   * 
   * @param header
   *          The original header
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * 
   * @return The new header
   * 
   */
  public static String extendCallback(String header,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    return extendCallback(header, outgoingInterfaceAddress, incomingInterfaceAddress, incomingInterfacePort, false);
  }

  /**
   * Extends the UDP_CALLBACK header in a SUBSCRIBE message.
   * 
   * @param header
   *          The original header
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * 
   * @return The new header
   * 
   */
  public static String extendUDPCallback(String header,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    return extendCallback(header, outgoingInterfaceAddress, incomingInterfaceAddress, incomingInterfacePort, true);
  }

  /**
   * Extends all occurrences of a specific tag in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param tag
   *          The tag
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param baseURL
   *          Base URL for relative URLs
   * @return The new description
   */
  public static String extendURLInAllTags(String xmlDescription,
    String tag,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    String baseURL)
  {
    String upperDescription = xmlDescription.toUpperCase();
    int startIndex = upperDescription.indexOf("<" + tag);
    while (startIndex != -1)
    {
      // System.out.println("Try to replace "+tag+" at position "+startIndex);
      String newXMLDescription =
        extendURLInTag(xmlDescription,
          upperDescription,
          tag,
          startIndex,
          outgoingInterfaceAddress,
          incomingInterfaceAddress,
          incomingInterfacePort,
          baseURL);

      if (newXMLDescription != null)
      {
        xmlDescription = newXMLDescription;
        upperDescription = xmlDescription.toUpperCase();
        startIndex = upperDescription.indexOf("<" + tag, startIndex + tag.length());
      } else
      {
        // exit loop
        startIndex = -1;
      }
    }
    return xmlDescription;
  }

  /**
   * Extends host and port of all urls in a SOAP request message that are equal to a certain
   * address. This can be used to access media server items etc.
   * 
   * @param xmlDescription
   *          The original description
   * @param originalSocketAddress
   *          The socket address that should be replaced
   * @param outgoingInterfaceAddress
   *          The interface that should forward the resource request
   * @param incomingInterfaceAddress
   *          The interface that should receive the resource request
   * @param incomingInterfacePort
   *          The port that should receive the resource request
   * @return The new response message
   */
  public static String extendSoapRequestURLs(String xmlDescription,
    String originalSocketAddress,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    int foundIndex = 0;
    while (foundIndex < xmlDescription.length())
    {
      foundIndex = xmlDescription.indexOf(originalSocketAddress, foundIndex);
      if (foundIndex != -1)
      {
        int foundEndIndex = foundIndex + originalSocketAddress.length();
        // skip first '/'
        foundEndIndex++;
        // try to extract forwarder module ID for this URL
        String forwarderModuleID = xmlDescription.substring(foundEndIndex, xmlDescription.indexOf("/", foundEndIndex));

        System.out.println("Forwarder module ID is: " + forwarderModuleID);
        // compare forwarder module ID to outgoing interface address
        if (outgoingInterfaceAddress.equals(forwarderModuleID))
        {
          System.out.println("Resource is found in the same subnet as the request destination");

          // try to get original address and port
          foundEndIndex += forwarderModuleID.length();
          // skip next '/'
          foundEndIndex++;
          String originalAddress = xmlDescription.substring(foundEndIndex, xmlDescription.indexOf("/", foundEndIndex));
          foundEndIndex += originalAddress.length();
          // skip next '/'
          foundEndIndex++;
          String originalPort = xmlDescription.substring(foundEndIndex, xmlDescription.indexOf("/", foundEndIndex));
          foundEndIndex += originalPort.length();

          // change URL back to normal (remove forwarding)
          xmlDescription =
            xmlDescription.substring(0, foundIndex) + originalAddress + ":" + originalPort +
              xmlDescription.substring(foundEndIndex);
          // update start index
          foundIndex += originalAddress.length();
        } else
        {
          // replace original forwarder address with new forwarder address
          xmlDescription =
            xmlDescription.substring(0, foundIndex) + incomingInterfaceAddress + ":" + incomingInterfacePort +
              xmlDescription.substring(foundEndIndex);
          // update start index
          foundIndex += incomingInterfaceAddress.length();
        }
      } else
      // nothing found, end while loop
      {
        foundIndex = xmlDescription.length();
      }
    }
    return xmlDescription;
  }

  /**
   * Extends host and port of all urls in a SOAP response message that are equal to a certain
   * address. This can be used to access media server items etc.
   * 
   * @param xmlDescription
   *          The original description
   * @param originalSocketAddress
   *          The socket address that should be replaced
   * @param outgoingInterfaceAddress
   *          The interface that should forward the resource request
   * @param incomingInterfaceAddress
   *          The interface that should receive the resource request
   * @param incomingInterfacePort
   *          The port that should receive the resource request
   * @return The new response message
   */
  public static String extendSoapResponseURLs(String xmlDescription,
    String originalSocketAddress,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort)
  {
    int startIndex = 0;
    String oldHost = originalSocketAddress.substring(0, originalSocketAddress.indexOf(":"));
    String oldPort = originalSocketAddress.substring(originalSocketAddress.indexOf(":") + 1);
    while (startIndex < xmlDescription.length())
    {
      int foundIndex = xmlDescription.indexOf(originalSocketAddress, startIndex);
      if (foundIndex != -1)
      {
        String newURL =
          incomingInterfaceAddress + ":" + incomingInterfacePort + "/" + outgoingInterfaceAddress + "/" + oldHost +
            "/" + oldPort;
        // replace old with new URL
        xmlDescription =
          xmlDescription.substring(0, foundIndex) + newURL +
            xmlDescription.substring(foundIndex + originalSocketAddress.length());
        // update start index
        startIndex += newURL.length();

      } else
      // nothing found, end while loop
      {
        startIndex = xmlDescription.length();
      }
    }
    return xmlDescription;
  }

  /**
   * Extends all urls in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param baseURL
   *          Base URL for relative URLs
   * @return The new description
   */
  public static String extendDeviceDescriptionURLs(String xmlDescription,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    String baseURL)
  {
    String upperXMLDescription = xmlDescription.toUpperCase();
    int baseURLIndex = upperXMLDescription.indexOf("<" + XMLConstant.URLBASE_TAG.toUpperCase());
    // end index of <URLBase>
    int baseURLEndIndex = upperXMLDescription.indexOf(">", baseURLIndex);
    int baseURLEndTagIndex = upperXMLDescription.indexOf("</" + XMLConstant.URLBASE_TAG.toUpperCase());
    // base URL found
    if (baseURLIndex != -1 && baseURLEndIndex != -1 && baseURLEndTagIndex != -1)
    {
      // extract value
      String value = xmlDescription.substring(baseURLEndIndex + 1, baseURLEndTagIndex);

      // System.out.println("Found BASE URL:" + value);
      try
      {
        URL tempURL = new URL(value);
        baseURL = tempURL.getHost() + "/" + tempURL.getPort() + tempURL.getPath();
      } catch (Exception e)
      {
      }
      if (baseURL.endsWith("/"))
      {
        baseURL = baseURL.substring(0, baseURL.length() - 1);
      }
      // System.out.println("Set BASE URL:" + baseURL);

      // remove base URL tag because it is no longer needed
      xmlDescription =
        xmlDescription.substring(0, baseURLIndex) +
          xmlDescription.substring(baseURLEndTagIndex + ("</" + XMLConstant.URLBASE_TAG + ">").length());
    }
    // remove all occurences of MulticastEventAddress because this mechanism is not available over
    // gateways
    if (xmlDescription.indexOf(XMLConstant.MULTICAST_EVENT_ADDRESS_TAG) != -1)
    {
      System.out.println("Remove MulticastEvents from description");
      String pattern =
        "<" + XMLConstant.MULTICAST_EVENT_ADDRESS_TAG + ".*</" + XMLConstant.MULTICAST_EVENT_ADDRESS_TAG + ">";
      xmlDescription = xmlDescription.replaceAll(pattern, "");
      xmlDescription = xmlDescription.replaceAll("\r\n\r\n", "\r\n");
    }

    xmlDescription =
      extendURLInAllTags(xmlDescription,
        XMLConstant.SCPDURL_TAG.toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    xmlDescription =
      extendURLInAllTags(xmlDescription,
        XMLConstant.CONTROLURL_TAG.toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    xmlDescription =
      extendURLInAllTags(xmlDescription,
        XMLConstant.EVENTSUBURL_TAG.toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    // icon urls
    xmlDescription =
      extendURLInAllTags(xmlDescription,
        "url".toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    // presentation url
    xmlDescription =
      extendURLInAllTags(xmlDescription,
        XMLConstant.PRESENTATIONURL_TAG.toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    return xmlDescription;
  }

  /**
   * Extends all event URLs in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param outgoingInterfaceAddress
   *          The interface that should forward the message
   * @param incomingInterfaceAddress
   *          The interface that should receive the message
   * @param incomingInterfacePort
   *          The port that should receive the message
   * @param baseURL
   *          Base URL for relative URLs
   * 
   * @return The new description
   */
  public static String extendDeviceDescriptionEventURLs(String xmlDescription,
    String outgoingInterfaceAddress,
    String incomingInterfaceAddress,
    int incomingInterfacePort,
    String baseURL)
  {
    xmlDescription =
      extendURLInAllTags(xmlDescription,
        XMLConstant.EVENTSUBURL_TAG.toUpperCase(),
        outgoingInterfaceAddress,
        incomingInterfaceAddress,
        incomingInterfacePort,
        baseURL);

    return xmlDescription;
  }

  /**
   * Splits an encoded path created by a UPnP gateway into its elements
   * 
   * @param encodedPath
   *          The path with the encoded values
   * @return The decoded elements. [0] Outgoing interface address (String) [1] Target address
   *         (String) [2] Target port (Integer) [3] Original path (String)
   * 
   */
  public static Object[] decodeGatewayURLPath(String encodedPath)
  {
    Object[] result = new Object[4];
    try
    {
      // remove leading '/'
      if (encodedPath.startsWith("/"))
      {
        encodedPath = encodedPath.substring(1);
      }

      StringTokenizer tokenizer = new StringTokenizer(encodedPath, "/");
      result[0] = tokenizer.nextToken();
      result[1] = tokenizer.nextToken();
      result[2] = new Integer(tokenizer.nextToken());
      result[3] = "/";

      // try to find third delimiter
      int found = 0;
      int index = 0;
      while (index < encodedPath.length() && found < 3)
      {
        if (encodedPath.charAt(index) == '/')
        {
          found++;
        }

        index++;
      }
      // use the remaining part of the path as original path
      if (index < encodedPath.length() && found == 3)
      {
        result[3] = "/" + encodedPath.substring(index);
      }
      return result;
    } catch (Exception ex)
    {
    }
    return null;
  }

}

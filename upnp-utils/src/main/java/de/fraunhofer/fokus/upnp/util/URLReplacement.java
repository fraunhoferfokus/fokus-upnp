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

/** This class provides static methods for URL replacement in device descriptions. */
public class URLReplacement
{

  /**
   * Replaces the host in a URL.
   * 
   * @param urlString
   *          The original URL
   * @param newHost
   *          The new host
   * 
   * @return The new URL
   */
  public static String replaceURLHost(String urlString, String newHost)
  {
    try
    {
      URL url = new URL(urlString);

      int oldPort = url.getPort();
      if (oldPort == -1)
      {
        oldPort = CommonConstants.HTTP_DEFAULT_PORT;
      }

      return new URL(url.getProtocol(), newHost, oldPort, url.getFile()).toExternalForm();
    } catch (Exception ex)
    {
    }
    return urlString;
  }

  /**
   * Replace all occurrences of a specific tag in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param tag
   *          The tag
   * @param newHost
   *          The new host
   * 
   * @return The new description
   */
  public static String replaceURLInAllTags(String xmlDescription, String tag, String newHost)
  {
    String upperDescription = xmlDescription.toUpperCase();
    int startIndex = upperDescription.indexOf("<" + tag);
    while (startIndex != -1)
    {
      // System.out.println("Try to replace "+tag+" at position "+startIndex);
      String newXMLDescription = replaceURLInTag(xmlDescription, upperDescription, tag, startIndex, newHost);

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
   * Replaces the host of an URL in a tag.
   * 
   * @param message
   *          The original message
   * @param upperCaseMessage
   *          The original message in upper case
   * @param tag
   *          The tag in upper case
   * @param startIndex
   *          The index that should be searched from
   * @param newHost
   *          The new host
   * 
   * @return The changed message
   */
  public static String replaceURLInTag(String message,
    String upperCaseMessage,
    String tag,
    int startIndex,
    String newHost)
  {
    int tagStart = upperCaseMessage.indexOf("<" + tag, startIndex);
    int tagEnd = upperCaseMessage.indexOf(">", tagStart);
    int endTagStart = upperCaseMessage.indexOf("</" + tag + ">", tagEnd);

    if (tagStart != -1 && tagEnd != -1 && endTagStart != -1 && endTagStart > tagEnd + 1)
    {
      String value = message.substring(tagEnd + 1, endTagStart);
      if (URLHelper.isRelativeURL(value))
      {
        // @TODO: check method for relative URLs
        return message.substring(0, tagEnd + 1) + replaceURLHost(value, newHost) + message.substring(endTagStart);
      } else
      {
        return message.substring(0, tagEnd + 1) + replaceURLHost(value, newHost) + message.substring(endTagStart);
      }
    }
    return message;
  }

  /**
   * Replaces the host for event URLs in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param newHost
   *          The new host
   * 
   * @return The new description
   */
  public static String replaceDeviceDescriptionEventURLs(String xmlDescription, String newHost)
  {
    xmlDescription = replaceURLInAllTags(xmlDescription, XMLConstant.EVENTSUBURL_TAG.toUpperCase(), newHost);

    return xmlDescription;
  }

  /**
   * Replaces the host for all URLs except event URLs in a device description.
   * 
   * @param xmlDescription
   *          The original description
   * @param newHost
   *          The new host
   * 
   * @return The new description
   */
  public static String replaceDeviceDescriptionExceptEventURLs(String xmlDescription, String newHost)
  {
    String upperXMLDescription = xmlDescription.toUpperCase();
    int baseURLIndex = upperXMLDescription.indexOf("<" + XMLConstant.URLBASE_TAG.toUpperCase());
    int baseURLEndIndex = upperXMLDescription.indexOf(">" + XMLConstant.URLBASE_TAG.toUpperCase(), baseURLIndex);
    int baseURLEndTagIndex = upperXMLDescription.indexOf("</" + XMLConstant.URLBASE_TAG.toUpperCase());
    // base URL found
    if (baseURLIndex != -1 && baseURLEndIndex != -1 && baseURLEndTagIndex != -1)
    {
      // remove base URL tag because it is no longer needed
      xmlDescription =
        xmlDescription.substring(0, baseURLIndex) +
          xmlDescription.substring(baseURLEndTagIndex + ("</" + XMLConstant.URLBASE_TAG + ">").length());
    }

    xmlDescription = replaceURLInAllTags(xmlDescription, XMLConstant.SCPDURL_TAG.toUpperCase(), newHost);

    xmlDescription = replaceURLInAllTags(xmlDescription, XMLConstant.CONTROLURL_TAG.toUpperCase(), newHost);

    // icon urls
    xmlDescription = replaceURLInAllTags(xmlDescription, "URL", newHost);

    // presentation url
    xmlDescription = replaceURLInAllTags(xmlDescription, XMLConstant.PRESENTATIONURL_TAG.toUpperCase(), newHost);

    return xmlDescription;
  }

}

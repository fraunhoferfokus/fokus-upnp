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

/** This class provides static methods for URL handling */
public class URLHelper
{

  final static String VALIDURICHARS                =
                                                   // "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY0123456789/:@_-.%&,;?=";
                                                     // removed &%
                                                     "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY0123456789/:@_-.,;?=";

  /** Copied from RFC 2396 */
  final static String NOT_RESERVED_VALID_URI_CHARS =
                                                     "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY0123456789-_.!~*'()";

  /**
   * Converts an escaped URL back to its original value
   * 
   * @param url
   *          The escaped URL
   * @return the converted URL
   */
  public static String escapedURLToString(String url)
  {
    int i = 0;
    String result = "";

    try
    {
      while (i < url.length())
      {
        char currentChar = url.charAt(i);
        if (currentChar != '%')
        {
          result += currentChar;
          i++;
        } else
        {
          try
          {
            int value = Integer.parseInt(url.substring(i + 1, i + 3), 16);
            result += new Character((char)value);
            i += 3;
          } catch (Exception e)
          {
            result += currentChar;
            i++;
          }
        }
      }
      return StringHelper.decodeUTF8String(result);
    } catch (Exception ex)
    {
    }
    return null;
  }

  /** Builds the path for an URL from path and an optional query */
  public static String getURLPath(URL url)
  {
    return url.getPath() + (url.getQuery() == null ? "" : "?" + url.getQuery());
  }

  /**
   * This methods replaces invalid or reserved characters in an URL. This should be called when creating URLs from file
   * names etc.
   * 
   * @param url
   *          URL to convert
   * @return the converted and escaped URL
   */
  public static String escapeURL(String url)
  {
    if (url.length() == 0)
    {
      return "";
    }

    StringBuffer urlBuffer = new StringBuffer(url.length() * 2);

    try
    {
      byte[] urlData = StringHelper.utf8StringToByteArray(url);

      for (int i = 0; i < urlData.length; i++)
      {
        int ch = urlData[i] & 0xFF;
        // check for valid characters
        if (NOT_RESERVED_VALID_URI_CHARS.indexOf(ch) >= 0)
        {
          urlBuffer.append((char)ch);
        } else
        {
          // invalid character, escape
          urlBuffer.append('%');
          // force two digits
          if (ch < 0x10)
          {
            urlBuffer.append('0');
          }

          urlBuffer.append(Integer.toHexString(ch));
        }
      }
      return urlBuffer.toString();
    } catch (Exception ex)
    {
      System.err.println("Internal error escaping URI:");
      ex.printStackTrace();
    }
    return null;
  }

  /** Checks if a URL is relative */
  public static boolean isRelativeURL(String urlString)
  {
    return urlString.indexOf(":") == -1;
  }

  /**
   * Creates a path containing normal slashes as separators and ending with a separator.
   */
  public static String toValidURLPath(String directory)
  {
    String result = directory != null ? directory : "";
    result = result.replace('\\', '/');
    // end with separator
    if (result.length() != 0 && !result.endsWith("/"))
    {
      result += "/";
    }
    return result;
  }

  /** Parses the arguments found in an URL query */
  public static KeyValueVector parseURLQuery(String query)
  {
    KeyValueVector result = new KeyValueVector();
    if (query == null || query.length() < 3)
    {
      return result;
    }
    if (query.startsWith("?"))
    {
      query = query.substring(1);
    }
    StringTokenizer tokenizer = new StringTokenizer(query, "&");
    while (tokenizer.hasMoreTokens())
    {
      String currentToken = tokenizer.nextToken().trim();
      StringTokenizer tokenTokenizer = new StringTokenizer(currentToken, "=");
      if (tokenTokenizer.countTokens() == 2)
      {
        String key = tokenTokenizer.nextToken();
        String value = tokenTokenizer.nextToken();
        result.add(key, value);
      }
    }
    return result;
  }

}

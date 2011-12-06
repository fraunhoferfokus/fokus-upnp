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
package de.fraunhofer.fokus.upnp.util.network;

import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLReplacement;

/**
 * This class provides helper methods for HTTP message handling.
 * 
 * @author Alexander Koenig
 */
public class HTTPMessageHelper
{

  /**
   * Replaces the path in a request line
   * 
   * @param header
   *          The original header
   * @param newPath
   *          The path for replacement
   * @return The new header
   */
  public static String replacePathInRequestLine(String header, String newPath)
  {
    try
    {
      int firstLineEndIndex = header.indexOf(CommonConstants.NEW_LINE);
      String input = header.substring(0, firstLineEndIndex);
      String upperInput = input.toUpperCase();

      int methodEndIndex = upperInput.indexOf(" ");
      int httpIndex = upperInput.indexOf(CommonConstants.HTTP_1_x);

      if (methodEndIndex != -1 && httpIndex != -1 && methodEndIndex < httpIndex)
      {
        // build new request line
        return input.substring(0, methodEndIndex) + " " + newPath + CommonConstants.BLANK + input.substring(httpIndex) +
          header.substring(firstLineEndIndex);
      }
    } catch (Exception ex)
    {
    }
    return header;
  }

  /**
   * Replaces the HOST value in a header.
   * 
   * @param header
   *          The original header
   * @param newHost
   *          The new HOST value (without "HOST: ")
   * 
   * @return The modified header
   */
  public static String replaceHost(String header, String newHost)
  {
    return replaceHeaderLine(header, CommonConstants.HOST, newHost);
  }

  /**
   * Adds a port to the HOST line in a header if its missing
   * 
   * @param header
   *          The original header
   * @param port
   *          The port that should be added
   * @return The new header
   */
  public static String tryAddPortToHost(String header, int port)
  {
    try
    {
      String result = "";
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // search HOST header
        if (upperToken.startsWith(CommonConstants.HOST))
        {
          // extract value
          String value = token.substring(CommonConstants.HOST.length()).trim();

          // no port found
          if (value.lastIndexOf(":") == -1)
          {
            // add port
            result += CommonConstants.HOST + " " + value + ":" + port + CommonConstants.NEW_LINE;
          } else
          {
            // no change
            result += token + CommonConstants.NEW_LINE;
          }
        } else
        {
          // copy other tokens
          result += token + CommonConstants.NEW_LINE;
        }
      }
      return result;
    } catch (Exception ex)
    {
    }
    return header;
  }

  /**
   * Adds a new header line to a message
   * 
   * @param header
   *          The original header
   * @param tag
   *          The tag of the new header line
   * @param value
   *          The value of the new header line
   * @return The new header
   */
  public static String addHeaderLine(String header, String tag, String value)
  {
    int newLineIndex = header.lastIndexOf(CommonConstants.NEW_LINE);

    if (newLineIndex != -1)
    {
      return header.substring(0, newLineIndex) + CommonConstants.NEW_LINE + tag + CommonConstants.BLANK + value +
        CommonConstants.NEW_LINE;
    } else
    {
      return header;
    }
  }

  /**
   * Removes a header line
   * 
   * @param header
   *          The original header
   * @param headerToken
   *          The tag of the header line that should be removed
   * @return The new header
   */
  public static String removeHeaderLine(String header, String headerToken)
  {
    try
    {
      String result = "";
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        if (!upperToken.startsWith(headerToken))
        {
          // copy all other tokens
          result += token + CommonConstants.NEW_LINE;
        }
      }

      return result;
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Replaces the value in a specific header line.
   * 
   * @param header
   *          The original header
   * @param headerToken
   *          The tag which value should be replaced (e.g., HOST:)
   * @param newValue
   *          The new value for this header tag (e.g., 102.168.1.1)
   * 
   * @return The modified header
   */
  public static String replaceHeaderLine(String header, String headerToken, String newValue)
  {
    try
    {
      String upperCaseHeaderToken = headerToken.toUpperCase();
      String result = "";
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // search header
        if (upperToken.startsWith(upperCaseHeaderToken))
        {
          // replace value
          result += token.substring(0, upperCaseHeaderToken.length()) + " " + newValue + CommonConstants.NEW_LINE;
        } else
        {
          // copy other tokens
          result += token + CommonConstants.NEW_LINE;
        }
      }
      return result;
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Replaces the value in a specific header line.
   * 
   * @param header
   *          The original header
   * @param headerToken
   *          The tag which value should be replaced (e.g., HOST:)
   * @param newValue
   *          The new value for this header tag (e.g., 102.168.1.1)
   * 
   * @return The modified header
   */
  public static String replaceContentLength(String header, int newLength)
  {
    return replaceHeaderLine(header, CommonConstants.CONTENT_LENGTH, newLength + "");
  }

  /**
   * Builds a header line for a HTTP message.
   * 
   * @param tag
   *          Tag name
   * @param value
   *          Tag value
   * 
   * @return A new header line (tag: value\r\n) as string
   */
  public static String buildHeaderLine(String tag, String value)
  {
    return tag + CommonConstants.BLANK + value + CommonConstants.NEW_LINE;
  }

  /**
   * Retrieves the value of a header line.
   * 
   * @param header
   *          The original header
   * @param headerToken
   *          The tag for the searched line (e.g., LOCATION)
   * @return The value of this header or null
   */
  public static String getHeaderLine(String header, String headerToken)
  {
    try
    {
      String upperCaseHeaderToken = headerToken.toUpperCase();
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // search header
        if (upperToken.startsWith(upperCaseHeaderToken))
        {
          // return value
          return token.substring(upperCaseHeaderToken.length()).trim();
        }
      }
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Checks if a message is an initial GENA subscribe message
   * 
   * @param header
   *          The original header
   * @return True if the message is an initial GENA subscribe message, false otherwise
   */
  public static boolean isInitialSubscribe(String header)
  {
    if (!header.startsWith(CommonConstants.SUBSCRIBE))
    {
      return false;
    }

    try
    {
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // search CALLBACK header
        if (upperToken.startsWith(CommonConstants.CALLBACK))
        {
          return true;
        }
      }
    } catch (Exception ex)
    {
    }
    return false;
  }

  /**
   * Checks if a message contains specific header lines
   * 
   * @param header
   *          The original header
   * @param requested
   *          A vector with searched header lines
   * 
   * @return True if the message contains all headers from requested, false otherwise
   */
  public static boolean hasAllHeaderLines(String header, Vector requested)
  {
    return getHeaderLineCount(header, requested) == requested.size();
  }

  /**
   * Returns the number of found header tokens
   * 
   * @param header
   *          The original header
   * @param requested
   *          A vector with searched headers
   * 
   * @return True if the message contains all headers from requested, false otherwise
   */
  public static int getHeaderLineCount(String header, Vector requested)
  {
    int found = 0;
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens() && requested.size() > 0)
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        for (int i = 0; i < requested.size(); i++)
        {
          String currentRequest = ((String)requested.elementAt(i)).toUpperCase();
          if (upperToken.startsWith(currentRequest))
          {
            found++;
          }
        }
      }
    } catch (Exception ex)
    {
    }
    return found;
  }

  /**
   * Checks if this request will probably not have a body.
   * 
   * @param requestHeader
   *          The upper case request header
   * 
   * @return True if the request will probably be header only (e.g., GET or HEAD requests)
   */
  public static boolean isHeaderOnlyRequestMessage(String requestHeader)
  {
    return requestHeader.startsWith(CommonConstants.GET) || requestHeader.startsWith(CommonConstants.HEAD) ||
      requestHeader.startsWith(CommonConstants.SUBSCRIBE) || requestHeader.startsWith(CommonConstants.UNSUBSCRIBE);
  }

  /**
   * Checks if the response to this request will probably not have a body.
   * 
   * @param requestHeader
   *          The upper case request header
   * 
   * @return True if the response to this request will probably be header only (e.g., HEAD requests)
   */
  public static boolean isHeaderOnlyResponseMessage(String requestHeader)
  {
    return requestHeader.startsWith(CommonConstants.HEAD) || requestHeader.startsWith(CommonConstants.SUBSCRIBE) ||
      requestHeader.startsWith(CommonConstants.UNSUBSCRIBE) || requestHeader.startsWith(CommonConstants.NOTIFY);
  }

  /**
   * Replaces the host in a CALLBACK header for a SUBSCRIBE message.
   * 
   * @param header
   *          The original header
   * @param newHost
   *          The new callback host
   * 
   * @return The new header
   */
  public static String replaceCallbackHost(String header, String newHost)
  {
    try
    {
      String result = "";
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // search CALLBACK header
        if (upperToken.startsWith(CommonConstants.CALLBACK))
        {
          String newValue = "";
          // extract value
          String value = token.substring(CommonConstants.CALLBACK.length()).trim();
          // replace all callback URLs
          StringTokenizer urlTokenizer = new StringTokenizer(value, ">");
          while (urlTokenizer.hasMoreTokens())
          {
            try
            {
              String url = urlTokenizer.nextToken();
              // remove < and leading spaces
              url = url.substring(url.indexOf("<") + 1).trim();

              newValue += "<" + URLReplacement.replaceURLHost(url, newHost) + ">";
            } catch (Exception ex)
            {
              System.out.println("ERROR in tokenizing:" + ex.getMessage());
            }
          }
          result += CommonConstants.CALLBACK + newValue + CommonConstants.NEW_LINE;
        } else
        {
          // copy other tokens
          result += token + CommonConstants.NEW_LINE;
        }
      }
      return result;
    } catch (Exception ex)
    {
    }
    return header;
  }

  /**
   * Retrieves a simple description for a HTTP message
   * 
   * @param header
   *          The original header
   * @return A message description
   */
  public static String getHeaderDescription(String header)
  {
    if (header == null || header.length() == 0)
    {
      return "[<Empty header>]";
    }

    String result = "";
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(header, CommonConstants.NEW_LINE);
      String request = tokenizer.nextToken();
      result = request;
      request = request.toUpperCase();
      while (tokenizer.hasMoreTokens())
      {
        String token = tokenizer.nextToken();
        String upperToken = token.toUpperCase();

        // show HOST except for SSDP messages
        if (!request.startsWith(CommonConstants.M_SEARCH) && upperToken.startsWith(CommonConstants.HOST))
        {
          result += " - " + token;
        }
        // show LOCATION for SSDP messages
        if ((request.startsWith(CommonConstants.NOTIFY) || request.startsWith(CommonConstants.M_SEARCH) || request.startsWith(CommonConstants.HTTP_1_1)) &&
          upperToken.startsWith(CommonConstants.LOCATION))
        {
          result += " - " + token;
        }

        // search header
        if (header.startsWith(CommonConstants.SUBSCRIBE) && upperToken.startsWith(CommonConstants.CALLBACK))
        {
          result += " - " + token;
        }
        if (header.startsWith(CommonConstants.SUBSCRIBE) && upperToken.startsWith(CommonConstants.UDP_CALLBACK))
        {
          result += " - " + token;
        }

      }
    } catch (Exception ex)
    {
    }
    return "[" + result + "]";
  }

  /**
   * Builds the resulting byte array for a header and a body
   * 
   * @param header
   *          Header WITHOUT blank line
   * @param body
   *          Encoded body
   */
  public static byte[] createHTTPMessage(String header, byte[] body)
  {
    if (body == null)
    {
      return HTTPMessageHelper.createHTTPMessage(header);
    }

    if (!header.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
    {
      header += CommonConstants.NEW_LINE;
    }

    // Header will not be encoded
    byte[] headerBytes = StringHelper.stringToByteArray(header);
    byte[] result = new byte[headerBytes.length + body.length];

    System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
    System.arraycopy(body, 0, result, headerBytes.length, body.length);

    return result;
  }

  /**
   * Builds the resulting byte array for a header only message
   * 
   * @param header
   *          Header WITHOUT final blank line
   */
  public static byte[] createHTTPMessage(String header)
  {
    if (header == null)
    {
      return new byte[0];
    }

    // Header will not be encoded
    if (!header.endsWith(CommonConstants.NEW_LINE + CommonConstants.NEW_LINE))
    {
      header += CommonConstants.NEW_LINE;
    }

    return StringHelper.stringToByteArray(header);
  }

  /**
   * Builds a HTTP error message.
   * 
   * 
   * @param error
   *          An HTTP error (e.g., HTTP/1.1 404 Not Found)
   * 
   * @return The error message as byte array
   */
  public static byte[] createHTTPErrorMessage(String error)
  {
    return StringHelper.stringToByteArray(error + CommonConstants.NEW_LINE + CommonConstants.NEW_LINE);
  }

  /** Builds a HTTP-OK message */
  public static byte[] createHTTPOKMessage()
  {
    return StringHelper.stringToByteArray(CommonConstants.HTTP_OK + CommonConstants.NEW_LINE + CommonConstants.NEW_LINE);
  }

  /**
   * Builds a message for a partial GET.
   * 
   * @param requestHeader
   *          Header with partial request
   * @param responseHeader
   *          Header WITHOUT blank line
   * @param responseBody
   *          Encoded body
   */
  public static byte[] createPartialGETResponseMessage(String requestHeader, String responseHeader, byte[] responseBody)
  {
    long[] range = getRange(requestHeader);
    if (range != null)
    {
      if (range[1] == -1)
      {
        range[1] = responseBody.length;
      }

      byte[] partialBody = new byte[(int)(range[1] - range[0])];
      System.arraycopy(responseBody, (int)range[0], partialBody, 0, partialBody.length);

      // replace HTTP 200 OK response with HTTP 206
      int firstLineIndex = responseHeader.indexOf(CommonConstants.NEW_LINE);
      responseHeader = CommonConstants.HTTP_1_1_206 + responseHeader.substring(firstLineIndex);

      // add content range header
      responseHeader =
        addHeaderLine(responseHeader, CommonConstants.CONTENT_RANGE, range[0] + "-" + (range[1] - 1) + "/" +
          responseBody.length);

      // replace content length
      responseHeader = replaceContentLength(responseHeader, partialBody.length);

      // return changed message
      return createHTTPMessage(responseHeader, partialBody);
    }
    // invalid range, return whole document
    return createHTTPMessage(responseHeader, responseBody);
  }

  /**
   * Parses a RANGE header.
   * 
   * @param rangeValue
   *          Tag value (e.g., bytes=0-100)
   * 
   * @return Start ([0], inclusive) and end([1], exclusive) index found in the RANGE value or null
   */
  public static long[] getRange(String rangeValue)
  {
    // no range header found
    if (rangeValue == null)
    {
      return null;
    }
    long[] result = new long[2];

    try
    {
      // parse range header
      int equalSignIndex = rangeValue.indexOf("=");
      if (equalSignIndex != -1)
      {
        // parse header value
        String name = rangeValue.substring(0, equalSignIndex).trim();
        String value = rangeValue.substring(equalSignIndex + 1).trim();

        // parse byte range
        int minusIndex = value.indexOf("-");
        if (name.equalsIgnoreCase("bytes") && minusIndex != -1)
        {
          result[0] = 0;
          result[1] = -1;
          if (minusIndex > 0)
          {
            result[0] = Integer.parseInt(value.substring(0, minusIndex).trim());
          }
          if (minusIndex < value.length() - 1)
          {
            result[1] = Integer.parseInt(value.substring(minusIndex + 1).trim());
          }
          return result;
        }
      }
    } catch (Exception e)
    {
    }
    // invalid range
    return null;
  }

  /** Retrieves the body of a HTTP message byte array */
  public static byte[] getBody(byte[] data)
  {
    if (data == null)
    {
      return null;
    }

    int index = HTTPMessageHelper.getHeaderEndIndex(data);
    if (index != -1)
    {
      // body is remaining message
      byte[] body = new byte[data.length - index - 4];
      System.arraycopy(data, index + 4, body, 0, body.length);
      return body;
    }
    return null;
  }

  /** Retrieves the header of a HTTP message byte array */
  public static String getHeader(byte[] data, int headerEndIndex)
  {
    if (data == null)
    {
      return null;
    }

    byte[] header = new byte[headerEndIndex + 2];
    System.arraycopy(data, 0, header, 0, header.length);
    return StringHelper.byteArrayToString(header);
  }

  /** Retrieves the body of a HTTP message byte array */
  public static byte[] getBody(byte[] data, int headerEndIndex)
  {
    if (data == null)
    {
      return null;
    }

    // body is remaining message
    byte[] body = new byte[data.length - (headerEndIndex + 4)];
    System.arraycopy(data, headerEndIndex + 4, body, 0, body.length);
    return body;
  }

  /** Retrieves the header of a HTTP message byte array */
  public static String getHeader(byte[] data)
  {
    if (data == null)
    {
      return null;
    }

    int index = HTTPMessageHelper.getHeaderEndIndex(data);
    if (index != -1)
    {
      byte[] header = new byte[index + 2];
      System.arraycopy(data, 0, header, 0, header.length);
      return StringHelper.byteArrayToString(header);
    }
    return null;
  }

  /** Checks for the end of the header in a byte array that is a HTTP message */
  public static int getHeaderEndIndex(byte[] data)
  {
    if (data == null)
    {
      return -1;
    }

    for (int i = 0; i < data.length - 3; i++)
    {
      if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n')
      {
        return i;
      }
    }
    return -1;
  }

}

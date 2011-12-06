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

import java.text.DecimalFormat;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class provides static methods for string and byte array handling.
 * 
 * @author Alexander Koenig
 */
public class StringHelper
{

  final static String VALID_DIRECTORY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY0123456789-_.()";

  /** Converts a byte array to a string */
  public static String byteArrayToString(byte[] data)
  {
    if (data == null)
    {
      return null;
    }

    char[] result = new char[data.length];
    for (int i = 0; i < data.length; i++)
    {
      result[i] = (char)(data[i] & 0xFF);
    }

    return new String(result);
  }

  /** Converts a byte array to a string */
  public static String byteArrayToString(byte[] data, int offset, int length)
  {
    if (data == null || data.length < offset + length)
    {
      return null;
    }

    char[] result = new char[length];
    for (int i = 0; i < length; i++)
    {
      result[i] = (char)(data[offset + i] & 0xFF);
    }

    return new String(result);
  }

  /** Converts a string to a byte array */
  public static byte[] stringToByteArray(String dataString)
  {
    if (dataString == null)
    {
      return null;
    }

    byte[] result = new byte[dataString.length()];
    for (int i = 0; i < dataString.length(); i++)
    {
      result[i] = (byte)dataString.charAt(i);
    }

    return result;
  }

  /**
   * Converts a float to a string.
   * 
   * @param value
   * @param format
   *          The format, e.g. #.00
   * @return
   */
  public static String floatToString(float value, String format)
  {
    return doubleToString(value, format);
  }

  /**
   * Converts a double to a string.
   * 
   * @param value
   * @param format
   *          The format, e.g. #.00
   * @return
   */
  public static String doubleToString(double value, String format)
  {
    return new DecimalFormat(format).format(value);
  }

  /**
   * Converts a string to a boolean value.
   * 
   * @param value
   *          The value
   * 
   * @return True if value is equal to "yes", "true" or "1" (case-insensitive)
   */
  public static boolean stringToBoolean(String value)
  {
    return value != null && value.length() > 0 &&
      (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1"));
  }

  /**
   * Converts a string to an int value. If the conversion fails, the default value is returned.
   */
  public static int stringToIntegerDef(String dataString, int defaultValue)
  {
    try
    {
      return Integer.parseInt(dataString);
    } catch (Exception e)
    {
    }
    return defaultValue;
  }

  /** Converts a value to a hex string with a fixed number of digits. */
  public static String intToHexString(int value, int digits)
  {
    String result = Integer.toHexString(value);
    for (int i = result.length(); i < digits; i++)
    {
      result = "0" + result;
    }
    // trim result if needed
    if (result.length() > digits)
    {
      return result.substring(result.length() - digits);
    }
    return result;
  }

  /**
   * Converts a string to a float value. If the conversion fails, the default value is returned.
   */
  public static float stringToFloatDef(String dataString, float defaultValue)
  {
    try
    {
      return Float.parseFloat(dataString);
    } catch (Exception e)
    {
    }
    return defaultValue;
  }

  /** Creates an UTF-8 string from a byte array. */
  public static String byteArrayToUTF8String(byte[] data)
  {
    if (data == null)
    {
      return null;
    }
    if (data.length == 0)
    {
      return "";
    }

    byte[] source = data;
    // remove final '\0'
    if (data[data.length - 1] == (byte)0)
    {
      source = new byte[data.length - 1];
      System.arraycopy(data, 0, source, 0, source.length);
    }
    try
    {
      return new String(source, "utf-8");
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Creates a string from an UTF-8 encoded string.
   * 
   * @param dataString
   *          A string that is UTF-8 encoded but is treated as 8859-1
   * 
   * @return
   */
  public static String decodeUTF8String(String dataString)
  {
    if (dataString == null)
    {
      return null;
    }
    if (dataString.length() == 0)
    {
      return "";
    }

    byte[] utf8ByteArray = stringToByteArray(dataString);

    return byteArrayToUTF8String(utf8ByteArray);
  }

  /**
   * Creates a ISO-8859-1 version of an UTF-8 encoded string.
   * 
   * @param utf8String
   * 
   * @return
   */
  public static String encodeUTF8String(String utf8String)
  {
    if (utf8String == null)
    {
      return null;
    }
    if (utf8String.length() == 0)
    {
      return "";
    }

    byte[] utf8ByteArray = utf8StringToByteArray(utf8String);

    return byteArrayToString(utf8ByteArray);
  }

  /** Creates a byte array for an UTF-8 encoded string. */
  public static byte[] utf8StringToByteArray(String dataString)
  {
    if (dataString == null)
    {
      return null;
    }

    try
    {
      return dataString.getBytes("utf-8");
    } catch (Exception ex)
    {
    }
    return null;
  }

  /** Escapes invalid characters like >,< to &gt;,&lt; in XML */
  public static String xmlToEscapedString(String xmlString)
  {
    String result = xmlString;

    result = result.replaceAll("&", "&amp;");
    result = result.replaceAll("<", "&lt;");
    result = result.replaceAll(">", "&gt;");
    result = result.replaceAll("\"", "&quot;");

    return result;
  }

  /**
   * Checks a string for chars that must be escaped to send them in a XML message like >,< to &gt;,&lt;. If no such
   * chars are found, the string is not changed.
   */
  public static String escapeXMLIfNecessary(String textString)
  {
    // search invalid values
    boolean found = false;
    int i = 0;
    while (!found && i < textString.length())
    {
      found = textString.charAt(i) == '<' || textString.charAt(i) == '>' || textString.charAt(i) == '"';
      i++;
    }
    if (found)
    {
      System.out.println("NEED TO ESCAPE argument value: " + textString);
      return xmlToEscapedString(textString);
    }
    // now we check for singular &
    if (textString.indexOf("&") != -1 && textString.indexOf("&amp;") == -1 && textString.indexOf("&lt;") == -1 &&
      textString.indexOf("&quot;") == -1 && textString.indexOf("&gt;") == -1)
    {
      System.out.println("NEED TO ESCAPE argument value: " + textString);
      return xmlToEscapedString(textString);
    }
    return textString;
  }

  /** Builds a XML string from an escaped string. */
  public static String escapedStringToXML(String escapedString)
  {
    String result = escapedString;

    result = result.replaceAll("&lt;", "<");
    result = result.replaceAll("&gt;", ">");
    result = result.replaceAll("&quot;", "\"");
    // this must be last
    result = result.replaceAll("&amp;", "&");
    return result;
  }

  /**
   * This methods replaces invalid or reserved characters in a directory name. This should be called when creating
   * directory names from arbitrary strings.
   * 
   * @param directoryName
   *          File name to convert
   * 
   * @return A file name suitable for all operating systems that support % in filenames
   */
  public static String escapeDirectoryName(String directoryName)
  {
    if (directoryName.length() == 0)
    {
      return "";
    }

    StringBuffer nameBuffer = new StringBuffer(directoryName.length() * 2);

    try
    {
      byte[] nameData = StringHelper.stringToByteArray(directoryName);

      for (int i = 0; i < nameData.length; i++)
      {
        int ch = nameData[i] & 0xFF;
        // check for valid characters
        if (VALID_DIRECTORY_CHARS.indexOf(ch) >= 0)
        {
          nameBuffer.append((char)ch);
        } else
        {
          // invalid character, escape
          nameBuffer.append('%');
          // force two digits
          if (ch < 0x10)
          {
            nameBuffer.append('0');
          }

          nameBuffer.append(Integer.toHexString(ch));
        }
      }
      return nameBuffer.toString();
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Converts an escaped directory name back to its original value
   * 
   * @param directoryName
   *          The escaped URL
   * @return The original name
   */
  public static String escapedDirectoryNameToString(String directoryName)
  {
    int i = 0;
    String result = "";

    try
    {
      while (i < directoryName.length())
      {
        char currentChar = directoryName.charAt(i);
        if (currentChar != '%')
        {
          result += currentChar;
          i++;
        } else
        {
          int value = Integer.parseInt(directoryName.substring(i + 1, i + 3), 16);
          result += new Character((char)value);

          i += 3;
        }
      }
      return result;
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Converts a byte array to Base32 encoding. data.length mod 5 must be 0.
   */
  public static String byteArrayToBase32(byte[] data)
  {
    String result = "";
    if (data.length % 5 != 0)
    {
      return result;
    }

    byte[] bits = new byte[data.length * 8];
    // convert to bits
    for (int i = 0; i < data.length; i++)
    {
      bits[i * 8] = (byte)((data[i] & 0x80) >> 7);
      bits[i * 8 + 1] = (byte)((data[i] & 0x40) >> 6);
      bits[i * 8 + 2] = (byte)((data[i] & 0x20) >> 5);
      bits[i * 8 + 3] = (byte)((data[i] & 0x10) >> 4);
      bits[i * 8 + 4] = (byte)((data[i] & 0x08) >> 3);
      bits[i * 8 + 5] = (byte)((data[i] & 0x04) >> 2);
      bits[i * 8 + 6] = (byte)((data[i] & 0x02) >> 1);
      bits[i * 8 + 7] = (byte)((data[i] & 0x01) >> 0);
    }
    // extract 5 bit values and convert to string
    for (int i = 0; i < data.length / 5 * 8; i++)
    {
      if (i > 0 && i % 4 == 0)
      {
        result += '-';
      }
      byte value =
        (byte)(bits[i * 5 + 0] << 4 | bits[i * 5 + 1] << 3 | bits[i * 5 + 2] << 2 | bits[i * 5 + 3] << 1 | bits[i * 5 + 4] << 0);

      if (value >= 0 && value < 26)
      {
        result = result + (char)(value + 'A');
      }

      if (value >= 26 && value < 30)
      {
        result = result + (char)(value - 26 + '2');
      }

      if (value == 30)
      {
        result = result + '7';
      }

      if (value == 31)
      {
        result = result + '9';
      }
    }
    return result;
  }

  /** Converts a base32 encoded string to a byte array */
  public static byte[] base32ToByteArray(String data)
  {
    if ((data.length() + 1) % 10 != 0)
    {
      return null;
    }

    byte[] bits = new byte[(data.length() + 1) / 10 * 40];

    int bitIndex = 0;
    for (int i = 0; i < data.length(); i++)
    {
      char value = data.charAt(i);
      // ignore '-'
      if (value != '-')
      {
        byte byteValue = 0;
        if (value == '9')
        {
          byteValue = 31;
        }
        if (value == '7')
        {
          byteValue = 30;
        }
        if (value >= '2' && value <= '5')
        {
          byteValue = (byte)(value - '2' + 26);
        }
        if (value >= 'A' && value <= 'Z')
        {
          byteValue = (byte)(value - 'A');
        }
        // copy to bitArray
        bits[bitIndex] = (byte)((byteValue & 0x10) >> 4);
        bits[bitIndex + 1] = (byte)((byteValue & 0x08) >> 3);
        bits[bitIndex + 2] = (byte)((byteValue & 0x04) >> 2);
        bits[bitIndex + 3] = (byte)((byteValue & 0x02) >> 1);
        bits[bitIndex + 4] = (byte)((byteValue & 0x01) >> 0);
        bitIndex += 5;
      }
    }
    // build byte array
    byte[] result = new byte[bits.length / 8];

    for (int i = 0; i < result.length; i++)
    {
      byte byteValue =
        (byte)(bits[i * 8 + 0] << 7 | bits[i * 8 + 1] << 6 | bits[i * 8 + 2] << 5 | bits[i * 8 + 3] << 4 |
          bits[i * 8 + 4] << 3 | bits[i * 8 + 5] << 2 | bits[i * 8 + 6] << 1 | bits[i * 8 + 7] << 0);

      result[i] = byteValue;
    }
    return result;
  }

  /**
   * Converts a byte array into an hex string. Octets are divided by divider.
   */
  public static String byteArrayToHexString(byte[] data, String divider)
  {
    String result = "";
    if (data == null)
    {
      return result;
    }
    for (int i = 0; i < data.length; i++)
    {
      result += i > 0 ? divider : "";
      int value = data[i] & 0xFF;
      result += intToHexString(value, 2);
    }
    return result.toUpperCase();
  }

  /**
   * Converts a byte array into an hex string.
   */
  public static String byteArrayToBinHex(byte[] data)
  {
    return byteArrayToHexString(data, "");
  }

  /**
   * Converts a byte array into an hex string. Octets are divided by colons.
   */
  public static String byteArrayToMACString(byte[] data)
  {
    return byteArrayToHexString(data, ":");
  }

  /**
   * Converts a byte array into an hex string. Octets are divided by space.
   */
  public static String byteArrayToHexDebugString(byte[] data)
  {
    return byteArrayToHexString(data, " ");
  }

  /**
   * Converts a byte array into an ascii string. Invalid bytes are not shown.
   */
  public static String byteArrayToAsciiDebugString(byte[] data)
  {
    String result = "";
    if (data == null)
    {
      return result;
    }
    for (int i = 0; i < data.length; i++)
    {
      if (data[i] >= 32 && data[i] < 128)
      {
        result += (char)(data[i] & 0xFF);
      }
    }
    return result;
  }

  /**
   * Converts an hex string to a byte array. '-','_',' ',':' are removed prior to the conversion. data.length mod 2 must
   * be 0
   */
  public static byte[] binHexToByteArray(String data)
  {
    if (data == null)
    {
      return null;
    }

    // remove hyphens
    data = data.replaceAll("-", "");
    data = data.replaceAll("_", "");
    data = data.replaceAll(" ", "");
    data = data.replaceAll(":", "");

    if (data.length() % 2 != 0)
    {
      return null;
    }

    try
    {
      byte[] result = new byte[data.length() / 2];

      for (int i = 0; i < data.length() / 2; i++)
      {
        String valueString = data.substring(i * 2, (i + 1) * 2);
        result[i] = (byte)Integer.parseInt(valueString, 16);
      }
      return result;
    } catch (Exception e)
    {
    }
    return null;
  }

  /** This methods converts umlauts to the german equivalent */
  public static String convertUmlauts(String text)
  {

    /*
     * ä 195 164 0xC3 0xA4 0303 0244 ö 195 182 0xC3 0xB6 0303 0266 ü 195 188 0xC3 0xBC 0303 0274 Ä
     * 195 132 0xC3 0x84 0303 0204 Ö 195 150 0xC3 0x96 0303 0226 Ü 195 156 0xC3 0x9C 0303 0234 ß 195
     * 159 0xC3 0x9F 0303 0237
     */
    String result = text;
    boolean processed = false;
    int index = result.indexOf((char)195);
    while (index != -1)
    {
      processed = false;
      if (result.length() > index + 1)
      {
        if (result.charAt(index + 1) == (char)164)
        {
          processed = true;
          result = result.substring(0, index) + 'ä' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)182)
        {
          processed = true;
          result = result.substring(0, index) + 'ö' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)188)
        {
          processed = true;
          result = result.substring(0, index) + 'ü' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)132)
        {
          processed = true;
          result = result.substring(0, index) + 'Ä' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)150)
        {
          processed = true;
          result = result.substring(0, index) + 'Ö' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)156)
        {
          processed = true;
          result = result.substring(0, index) + 'Ü' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
        if (result.charAt(index + 1) == (char)159)
        {
          processed = true;
          result = result.substring(0, index) + 'ß' + (index + 2 < result.length() ? result.substring(index + 2) : "");
        }
      }
      // remove unrecognized characters
      if (!processed)
      {
        result = result.substring(0, index) + (index < result.length() - 2 ? result.substring(index + 2) : "");
      }

      index = result.indexOf((char)195);
    }
    return result;
  }

  /**
   * Returns the short name (e.g., StringHelper) for the complete class name (e.g.,
   * de.fraunhofer.fokus.upnp.util.StringHelper).
   * 
   * 
   * @param fullClassName
   * 
   * @return
   */
  public static String getShortClassName(String fullClassName)
  {
    String className = fullClassName;
    int index = className.lastIndexOf(".");
    if (index != -1)
    {
      return className.substring(index + 1);
    }

    return className;
  }

  /** Returns a string that can be used as graphical divider in console outputs. */
  public static String getDivider()
  {
    return "////////////////////////////////////////////////////////////";
  }

  /**
   * Prints some debug text to the console. text1 is prefixed by indentation, text2 is printed as given.
   * 
   * @param indentation
   * @param printDivider
   * @param text1
   * @param text2
   */
  public static void printDebugText(String indentation, boolean printDivider, String text1, String text2)
  {
    if (indentation == null)
    {
      indentation = "";
    }
    if (printDivider)
    {
      Portable.println(indentation + getDivider());
    }
    if (text1 != null)
    {
      Portable.println(indentation + text1);
    }
    if (text2 != null)
    {
      Portable.println(text2);
    }
  }

  /**
   * Converts a CSV(comma-separated value) list into a vector, containing all values as strings.
   * 
   * @param csvList
   *          The list
   */
  public static Vector csvStringToVector(String csvList)
  {
    Vector result = new Vector();
    StringTokenizer stringTokenizer = new StringTokenizer(csvList, ",");
    while (stringTokenizer.hasMoreTokens())
    {
      result.add(stringTokenizer.nextToken().trim());
    }
    return result;
  }

  /** Returns a CSV list from the elements of a List */
  public static String listToCSVString(List list)
  {
    if (list == null)
    {
      return "";
    }

    String result = "";
    for (int i = 0; i < list.size(); i++)
    {
      result += (i > 0 ? "," : "") + list.get(i).toString();
    }

    return result;
  }

}

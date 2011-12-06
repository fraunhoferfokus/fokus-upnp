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

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/**
 * 
 * @author Alexander Koenig
 */
public class Base64Helper
{

  /** Converts a base64 encoded string to a byte array */
  public static byte[] base64ToByteArray(String data)
  {
    if (data == null)
    {
      return null;
    }
    // length of string must be divisible by 4
    if (data.length() % 4 != 0)
    {
      return null;
    }

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    int offset = 0;
    while (offset < data.length())
    {
      byte[] fragment = base64ToBytes(data.substring(offset, offset + 4));
      result.write(fragment, 0, fragment.length);
      offset += 4;
    }
    return result.toByteArray();
  }

  /** Converts a BigInteger, e.g. a RSA modulus, to a Base64 encoded string */
  public static String bigIntegerToBase64(BigInteger value)
  {
    byte[] data = value.toByteArray();
    String result = "";
    // add leading zero if necessary
    if ((data[0] & 0x80) != 0)
    {
      result = "0";
    }

    for (int i = 0; i < data.length; i++)
    {
      result += (char)data[i];
    }

    return stringToBase64(result);
  }

  /** Converts a byte array to a Base64 encoded string */
  public static String byteArrayToBase64(byte[] data)
  {
    String result = "";
    byte[] dataFragment = new byte[3];
    // ceil(data.length/3)
    for (int i = 0; i < (data.length + 2) / 3; i++)
    {
      int fragmentLength = Math.min(3, data.length - i * 3);
      // copy current data to fragment
      for (int j = 0; j < fragmentLength; j++)
      {
        dataFragment[j] = data[i * 3 + j];
      }
      result += bytesToBase64(dataFragment, fragmentLength);
    }
    return result;
  }

  /** Converts a string to a Base64 encoded string */
  public static String stringToBase64(String data)
  {
    String result = "";
    byte[] dataFragment = new byte[3];
    // ceil(data.length/3)
    for (int i = 0; i < (data.length() + 2) / 3; i++)
    {
      int fragmentLength = Math.min(3, data.length() - i * 3);
      // copy current data to fragment
      for (int j = 0; j < fragmentLength; j++)
      {
        dataFragment[j] = (byte)data.charAt(i * 3 + j);
      }
      result += bytesToBase64(dataFragment, fragmentLength);
    }
    return result;
  }

  /** Converts a maximum of three bytes to 4 Base64 characters */
  private static String bytesToBase64(byte[] bytes, int length)
  {
    String result = "";
    if (length == 3)
    {
      result += bitsToBase64((byte)((bytes[0] & 0xFC) >> 2));
      result += bitsToBase64((byte)((bytes[0] & 0x03) << 4 | (bytes[1] & 0xF0) >> 4));
      result += bitsToBase64((byte)((bytes[1] & 0x0F) << 2 | (bytes[2] & 0xC0) >> 6));
      result += bitsToBase64((byte)(bytes[2] & 0x3F));
    }
    if (length == 2)
    {
      result += bitsToBase64((byte)((bytes[0] & 0xFC) >> 2));
      result += bitsToBase64((byte)((bytes[0] & 0x03) << 4 | (bytes[1] & 0xF0) >> 4));
      result += bitsToBase64((byte)((bytes[1] & 0x0F) << 2));
      result += '=';
    }
    if (length == 1)
    {
      result += bitsToBase64((byte)((bytes[0] & 0xFC) >> 2));
      result += bitsToBase64((byte)((bytes[0] & 0x03) << 4));
      result += '=';
      result += '=';
    }
    return result;
  }

  /** Converts a base64 encoded string of four chars to the corresponding bytes */
  private static byte[] base64ToBytes(String value)
  {
    // catch wrong size
    if (value.length() != 4)
    {
      return null;
    }

    // process padding
    int paddingOffset = value.indexOf("=");
    // no padding
    if (paddingOffset == -1)
    {
      byte[] result = new byte[3];
      result[0] = (byte)(base64ToBits(value.charAt(0)) << 2 | base64ToBits(value.charAt(1)) >> 4);
      result[1] = (byte)(base64ToBits(value.charAt(1)) << 4 | base64ToBits(value.charAt(2)) >> 2);
      result[2] = (byte)(base64ToBits(value.charAt(2)) << 6 | base64ToBits(value.charAt(3)));
      return result;
    }
    // 1 padded byte
    if (paddingOffset == 3)
    {
      byte[] result = new byte[2];
      result[0] = (byte)(base64ToBits(value.charAt(0)) << 2 | base64ToBits(value.charAt(1)) >> 4);
      result[1] = (byte)(base64ToBits(value.charAt(1)) << 4 | base64ToBits(value.charAt(2)) >> 2);
      return result;
    }
    // 2 padded bytes
    if (paddingOffset == 2)
    {
      byte[] result = new byte[1];
      result[0] = (byte)(base64ToBits(value.charAt(0)) << 2 | base64ToBits(value.charAt(1)) >> 4);
      return result;
    }
    // error
    return null;
  }

  /** Converts a 6-bit value to the corresponding Base64 char */
  private static char bitsToBase64(byte data)
  {
    if (data >= 0 && data < 26)
    {
      return (char)(data + 'A');
    }
    if (data >= 26 && data < 52)
    {
      return (char)(data - 26 + 'a');
    }
    if (data >= 52 && data < 62)
    {
      return (char)(data - 52 + '0');
    }
    if (data == 62)
    {
      return '+';
    }
    if (data == 63)
    {
      return '/';
    }
    return (char)0;
  }

  /** Converts a Base64 char to to corresponding 6-bit value */
  private static byte base64ToBits(char data)
  {
    if (data >= 'A' && data <= 'Z')
    {
      return (byte)(data - 'A');
    }
    if (data >= 'a' && data <= 'z')
    {
      return (byte)(data - 'a' + 26);
    }
    if (data >= '0' && data <= '9')
    {
      return (byte)(data - '0' + 52);
    }
    if (data == '+')
    {
      return (byte)62;
    }
    return (byte)63;
  }

}

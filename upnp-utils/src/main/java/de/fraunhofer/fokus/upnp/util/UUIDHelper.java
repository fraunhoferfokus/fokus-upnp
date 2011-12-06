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

import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This class provides helper methods for UUID generation and conversion.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UUIDHelper
{

  public static String FOKUS_NAMESPACE_UUID = "6ba7b827-9dad-11d1-80b4-00c0f4d430c8";

  /** Converts a uuid into a byte array. */
  public static byte[] uuidToByteArray(String uuid)
  {
    // remove prefix
    if (uuid != null && uuid.startsWith("uuid:"))
    {
      uuid = uuid.substring("uuid:".length());
    }

    byte[] result = new byte[16];

    if (uuid == null || uuid.length() != 36 || uuid.charAt(8) != '-' || uuid.charAt(13) != '-' ||
      uuid.charAt(18) != '-' || uuid.charAt(23) != '-')
    {
      return null;
    }

    uuid = uuid.replaceAll("-", "");
    try
    {
      for (int i = 0; i < 16; i++)
      {
        result[i] = (byte)Integer.parseInt(uuid.substring(i * 2, i * 2 + 2), 16);
      }
    } catch (Exception e)
    {
      System.out.println("Error converting UUID");
    }
    return result;
  }

  /** Converts a byte array into a name-based UUID. */
  public static String byteArrayToNameBasedUUID(byte[] data)
  {
    if (data == null || data.length != 16)
    {
      return null;
    }

    // insert type (SHA-1 name based)
    data[6] &= 0x0F;
    data[6] |= 0x50;

    // insert variant
    data[8] &= 0x3F;
    data[8] |= 0x80;

    String result = "";
    for (int i = 0; i < data.length; i++)
    {
      String number = Integer.toHexString(data[i] & 0xFF);
      if (number.length() == 1)
      {
        result += "0" + number;
      } else
      {
        result += number;
      }

      if (i == 3 || i == 5 || i == 7 || i == 9)
      {
        result += "-";
      }
    }
    return result;
  }

  /** Generates a UUID for a name. */
  public static String getUUIDFromName(String name)
  {
    if (name == null || name.length() == 0)
    {
      return null;
    }

    // build hash base by concatenating name space ID and name
    byte[] nameSpaceID = uuidToByteArray(FOKUS_NAMESPACE_UUID);
    byte[] nameData = StringHelper.stringToByteArray(name);
    byte[] hashBase = new byte[nameSpaceID.length + nameData.length];
    System.arraycopy(nameSpaceID, 0, hashBase, 0, nameSpaceID.length);
    System.arraycopy(nameData, 0, hashBase, nameSpaceID.length, nameData.length);

    byte[] hash = DigestHelper.calculateSHAHashForByteArray(hashBase);
    if (hash == null)
    {
      return null;
    }

    byte[] shortedHash = new byte[16];
    System.arraycopy(hash, 0, shortedHash, 0, 16);
    return byteArrayToNameBasedUUID(shortedHash);
  }

}

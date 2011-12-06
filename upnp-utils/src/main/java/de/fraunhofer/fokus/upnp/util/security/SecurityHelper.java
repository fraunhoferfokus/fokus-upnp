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
package de.fraunhofer.fokus.upnp.util.security;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;

/**
 * This class provides some helpful methods for security implementations.
 * 
 * @author Alexander Koenig
 */
public class SecurityHelper
{

  /** Creates a statistically unique BigInteger as String */
  public static String createSequenceBaseString()
  {
    return new BigInteger(128, new SecureRandom()).toString();
  }

  /** Creates a statistically unique long value */
  public static long createLongSequenceBase()
  {
    // leave at least 2^16 bits before wrap around
    BigInteger temp = new BigInteger(48, new SecureRandom());
    return new Long(temp.toString()).longValue();
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

    return Base64Helper.stringToBase64(result);
  }

  /** For debugging */
  public static String byteArrayToDebugString(byte[] data)
  {
    String result = "";
    for (int i = 0; i < data.length; i++)
    {
      result += i == 0 ? "" : ",";
      result += Integer.toString(data[i]);
    }
    return result;
  }

  /** Creates a XML description of a RSA public key that is suitable for UPnP security */
  public static String buildRSAPublicKeyXMLDescription(RSAPublicKey key)
  {
    String modulusBase64 = Base64Helper.bigIntegerToBase64(key.getModulus());
    String exponentBase64 = Base64Helper.bigIntegerToBase64(key.getPublicExponent());
    String data = "<RSAKeyValue><Modulus>";
    data += modulusBase64;
    data += "</Modulus><Exponent>";
    data += exponentBase64;
    data += "</Exponent></RSAKeyValue>";
    return data;
  }

  /** Creates a hash XML description */
  public static String buildSHABase64HashXMLDescription(String base64Hash)
  {
    return "<hash><algorithm>SHA1</algorithm><value>" + base64Hash + "</value></hash>";
  }
}

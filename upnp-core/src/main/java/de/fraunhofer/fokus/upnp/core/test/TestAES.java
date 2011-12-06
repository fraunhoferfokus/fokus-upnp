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
package de.fraunhofer.fokus.upnp.core.test;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class TestAES
{

  public static void main(String[] args)
  {
    byte[] password = "Hallo".getBytes();

    byte[] keyByteArray = new byte[16];
    for (int i = 0; i < keyByteArray.length; i++)
    {
      keyByteArray[i] = password[i % password.length];
    }
    SecretKey key = SymmetricCryptographyHelper.buildAESKey(keyByteArray);
    byte[] iv = new byte[16];
    for (int i = 0; i < keyByteArray.length; i++)
    {
      iv[i] = keyByteArray[i];
    }
    byte[] data = StringHelper.utf8StringToByteArray("Ich werde verschlüsselt");
    byte[] encrypted = SymmetricCryptographyHelper.encryptWithAES(key, iv, data);
    System.out.println("Encrypted byte array is " + Base64Helper.byteArrayToBase64(encrypted));

    byte[] decrypted = SymmetricCryptographyHelper.decryptWithAES(key, iv, encrypted);

    String decryptedString = StringHelper.byteArrayToUTF8String(decrypted);
    System.out.println("Decrypted string is " + decryptedString);

  }

}

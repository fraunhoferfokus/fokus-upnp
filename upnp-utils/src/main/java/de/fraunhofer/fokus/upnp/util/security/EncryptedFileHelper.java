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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class support the persistent storage of encrypted data in files.
 * 
 * @author Sebastian Nauck, Alexander Koenig
 */

public class EncryptedFileHelper
{

  /** Generates an AES key from a string */
  public static SecretKey getAESKeyFromString(String password)
  {
    byte[] keyBase = new byte[16];
    byte[] keyArray = password.getBytes();
    for (int i = 0; i < keyBase.length; i++)
    {
      keyBase[i] = keyArray[i % keyArray.length];
    }
    return SymmetricCryptographyHelper.buildAESKey(keyBase);
  }

  /** Generates an IV from a string */
  public static byte[] getIVFromString(String password)
  {
    byte[] result = new byte[16];
    byte[] keyArray = password.getBytes();
    for (int i = 0; i < result.length; i++)
    {
      result[i] = keyArray[i % keyArray.length];
    }
    return result;
  }

  /** Encrypts a string and stores it in a file. */
  public static void encryptAndSaveStringToFile(String text, SecretKey key, byte[] iv, File file)
  {
    byte[] textData = StringHelper.utf8StringToByteArray(text);

    byte[] encryptedData = SymmetricCryptographyHelper.encryptWithAES(key, iv, textData);

    saveByteArrayToFile(encryptedData, file);
  }

  /** Saves a byte array to a file. The byte array is base64 encoded prior to writing. */
  public static void saveByteArrayToFile(byte[] data, File file)
  {
    try
    {
      FileOutputStream outputStream = new FileOutputStream(file);

      outputStream.write(StringHelper.stringToByteArray(Base64Helper.byteArrayToBase64(data)));
      outputStream.close();
    } catch (Exception e)
    {
      System.out.println("Error saving string to file: " + e.getMessage());
    }
  }

  /**
   * Loads a string from a filereturn a byte Array which contains the pop3 Accountdata of an
   * assigned person. The method gets the byte Array out of an file
   */
  public static byte[] loadByteArrayFromFile(File file)
  {
    try
    {
      BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

      String stringAsBase64 = input.readLine();
      input.close();
      return Base64Helper.base64ToByteArray(stringAsBase64);
    } catch (Exception ex)
    {
    }
    return null;
  }

  /** Loads and decrypts a string from a file. */
  public static String loadAndDecryptStringFromFile(SecretKey key, byte[] iv, File file)
  {
    byte[] encryptedData = loadByteArrayFromFile(file);
    if (encryptedData != null)
    {
      byte[] decryptedAESArray = SymmetricCryptographyHelper.decryptWithAES(key, iv, encryptedData);

      return StringHelper.byteArrayToUTF8String(decryptedAESArray);
    }
    return null;
  }

}

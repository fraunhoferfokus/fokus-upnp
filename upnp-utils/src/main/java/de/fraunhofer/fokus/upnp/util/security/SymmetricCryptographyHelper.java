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

import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * 
 * @author Alexander Koenig
 */
public class SymmetricCryptographyHelper
{

  /** Generates a random 128 bit AES key */
  public static SecretKey generateAESKey()
  {
    try
    {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128);

      // Generate the secret key specs.
      SecretKey skey = kgen.generateKey();

      // System.out.println("Generated AES key is
      // "+SecurityHelper.byteArrayToBase64(skey.getEncoded()));

      return skey;
    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Builds a AES secret key from a byte array */
  public static SecretKey buildAESKey(byte[] encodedKey)
  {
    try
    {
      SecretKeySpec keySpec = new SecretKeySpec(encodedKey, "AES");
      return keySpec;

    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Generates a random 128 bit initialization vector */
  public static byte[] generateIV()
  {
    byte[] result = new byte[16];
    new SecureRandom().nextBytes(result);

    return result;
  }

  /** Encrypts a byte array with AES-128/CBC/ */
  public static byte[] encryptWithAES(SecretKey key, byte[] iv, byte[] data)
  {
    // apply padding
    int paddedBytes = 16 - data.length % 16;
    byte[] paddedData = new byte[data.length + paddedBytes];
    System.arraycopy(data, 0, paddedData, 0, data.length);
    paddedData[paddedData.length - 1] = (byte)paddedBytes;

    // init cipher
    SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    try
    {
      Cipher cipher = Cipher.getInstance(CommonConstants.AES_128_CBC_JAVA);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

      return cipher.doFinal(paddedData);
    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Decrypts a byte array with AES-128/CBC/ */
  public static byte[] decryptWithAES(SecretKey key, byte[] iv, byte[] data)
  {
    // init cipher
    SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    try
    {
      Cipher cipher = Cipher.getInstance(CommonConstants.AES_128_CBC_JAVA);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

      byte[] decrypted = cipher.doFinal(data);

      // System.out.println("Decrypted AES result is " + StringHelper.byteArrayToString(decrypted));

      // Remove padding
      int paddingSize = decrypted[decrypted.length - 1] & 0xFF;
      if (paddingSize > 16 || paddingSize < 1)
      {
        System.out.println("Unexpected padding size: " + paddingSize);
        return null;
      }

      byte[] result = new byte[decrypted.length - paddingSize];
      System.arraycopy(decrypted, 0, result, 0, result.length);

      return result;
    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Decrypts an AES key, that was encrypted with a public RSA key. */
  public static SymmetricKeyInfo decryptRSAEncryptedAESKey(RSAPrivateKey privateKey, byte[] data)
  {
    byte[] aesKeyData = null;
    byte[] iv = null;
    try
    {
      // System.out.println("Data before decryption is " + Base64Helper.byteArrayToBase64(data));

      // decrypt Key and IV
      byte[] decryptedKeyData = PublicKeyCryptographyHelper.decryptWithRSA(privateKey, data);

      // System.out.println("Data after decryption is " +
      // Base64Helper.byteArrayToBase64(decryptedKeyData));

      // 128 bit key
      iv = new byte[16];
      aesKeyData = new byte[decryptedKeyData.length - iv.length];
      // IV is at the most significant bytes
      System.arraycopy(decryptedKeyData, 0, iv, 0, iv.length);
      // key is at the remaining bytes
      System.arraycopy(decryptedKeyData, iv.length, aesKeyData, 0, aesKeyData.length);

      // System.out.println("Received symmetric key is " +
      // Base64Helper.byteArrayToBase64(aesKeyData));
      // System.out.println("Received iv is "+SecurityHelper.byteArrayToBase64(iv));

    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    if (aesKeyData != null)
    {
      SecretKey aesKey = SymmetricCryptographyHelper.buildAESKey(aesKeyData);
      return new SymmetricKeyInfo(aesKey, iv);
    }
    return null;
  }

  /**
   * Encrypts an AES key with a RSA public key.
   * 
   * 
   * @param aesKey
   * @param iv
   * @param publicKey
   * 
   * @return The encrypted AES key, encoded in Base64
   */
  public static String encryptAESKeyWithRSA(byte[] aesKey, byte[] iv, RSAPublicKey publicKey)
  {
    // build enciphered key
    byte[] keyData = new byte[iv.length + aesKey.length];
    System.arraycopy(iv, 0, keyData, 0, iv.length);
    System.arraycopy(aesKey, 0, keyData, iv.length, aesKey.length);

    // System.out.println("Used iv is "+SecurityHelper.byteArrayToBase64(iv));

    // System.out.println("Data before encryption is " + Base64Helper.byteArrayToBase64(keyData));

    // encipher with public key of target device
    try
    {
      byte[] encipheredKeyData = PublicKeyCryptographyHelper.encryptWithRSA(publicKey, keyData);

      // System.out.println("Data after encryption is " +
      // Base64Helper.byteArrayToBase64(encipheredKeyData));

      return Base64Helper.byteArrayToBase64(encipheredKeyData);
    } catch (Exception e)
    {

    }
    return null;
  }
}

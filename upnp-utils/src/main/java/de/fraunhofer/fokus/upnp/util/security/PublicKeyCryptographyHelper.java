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
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * 
 * @author Alexander Koenig
 */
public class PublicKeyCryptographyHelper
{

  /** Generates a new RSA key pair */
  public static KeyPair generateRSAKeyPair()
  {
    try
    {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(1024);
      return generator.generateKeyPair();
    } catch (Exception ex)
    {
      System.out.println("ERROR: " + ex.getMessage());
    }
    return null;
  }

  /**
   * Calculates the RSA signature for a string. This is done by building the SHA-1 hash over the
   * string and signing that hash value with a private key.
   */
  public static byte[] calculateRSASignatureForString(RSAPrivateKey signatureKey, String dataString)
  {
    byte[] data = StringHelper.stringToByteArray(dataString);

    // System.out.println("Calculate RSA signature for string with length "+data.length);

    try
    {
      Signature signature = Signature.getInstance(CommonConstants.RSA_SHA_1_JAVA);
      if (signature != null)
      {
        signature.initSign(signatureKey);
        signature.update(data);
        return signature.sign();
      }
    } catch (NoSuchAlgorithmException nsae)
    {
      System.out.println("Error: " + nsae.getMessage());
    } catch (InvalidKeyException ike)
    {
      System.out.println("Error: " + ike.getMessage());
    } catch (SignatureException se)
    {
      System.out.println("Error: " + se.getMessage());
    } catch (Exception ex)
    {
      System.out.println("Error: " + ex.getMessage());
    }
    return null;
  }

  /**
   * Verifies the RSA signature for a string.
   * 
   * @param publicKey
   * @param dataString
   * @param signatureBase64
   * @return
   */
  public static boolean verifyRSASignatureForString(RSAPublicKey publicKey, String dataString, String signatureBase64)
  {
    return verifyRSASignatureForString(publicKey, dataString, Base64Helper.base64ToByteArray(signatureBase64));
  }

  /**
   * Verifies the RSA signature for a string.
   * 
   * @param publicKey
   * @param dataString
   * @param signatureData
   * @return
   */
  public static boolean verifyRSASignatureForString(RSAPublicKey publicKey, String dataString, byte[] signatureData)
  {
    byte[] data = StringHelper.stringToByteArray(dataString);

    // System.out.println("Verify RSA signature for string with length "+data.length);
    try
    {
      Signature signature = Signature.getInstance(CommonConstants.RSA_SHA_1_JAVA);
      if (signature != null)
      {
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureData);
      }
    } catch (NoSuchAlgorithmException nsae)
    {
      System.out.println("Error: " + nsae.getMessage());
    } catch (InvalidKeyException ike)
    {
      System.out.println("Error: " + ike.getMessage());
    } catch (SignatureException se)
    {
      System.out.println("Error: " + se.getMessage());
    } catch (Exception ex)
    {
      System.out.println("Error: " + ex.getMessage());
    }
    return false;
  }

  /** Encrypts a byte array with an asymmetric key */
  public static byte[] encryptWithRSA(RSAPublicKey key, byte[] data) throws Exception
  {
    if (data.length > 117)
    {
      throw new Exception("Data too long (greater than 117 bytes)");
    }
    // apply PKCS#1 padding 1024 bits/8
    byte[] paddedMessage = new byte[128];
    paddedMessage[0] = (byte)0;
    paddedMessage[1] = (byte)2; // encryption
    SecureRandom random = new SecureRandom();
    for (int i = 2; i < 128 - data.length - 1; i++)
    {
      // random byte [1..255]
      byte randomByte = (byte)(1 + random.nextInt(255));
      paddedMessage[i] = randomByte;
    }
    // zero byte
    paddedMessage[128 - data.length - 1] = (byte)0;
    // copy data
    System.arraycopy(data, 0, paddedMessage, 128 - data.length, data.length);

    // encrypt
    BigInteger message = new BigInteger(paddedMessage);
    BigInteger encrypted = message.modPow(key.getPublicExponent(), key.getModulus());

    return encrypted.toByteArray();
  }

  /** Decrypts a byte array with an asymmetric key */
  public static byte[] decryptWithRSA(RSAPrivateKey key, byte[] data) throws Exception
  {
    // System.out.println("Decrypt with RSA: Data size is "+data.length);
    if (data.length > 129)
    {
      throw new Exception("Invalid data size (greater than 129)");
    }

    BigInteger encrypted = new BigInteger(data);
    BigInteger decrypted = encrypted.modPow(key.getPrivateExponent(), key.getModulus());

    byte[] decryptedMessage = decrypted.toByteArray();

    // find payload
    int i = 3;
    while (i < decryptedMessage.length - 1 && decryptedMessage[i] != 0)
    {
      i++;
    }

    if (i < decryptedMessage.length - 1)
    {
      byte[] result = new byte[decryptedMessage.length - 1 - i];
      System.arraycopy(decryptedMessage, i + 1, result, 0, result.length);
      return result;
    }

    // an error occured, produce random payload
    SecureRandom random = new SecureRandom();
    byte[] result = new byte[10 + random.nextInt(50)];
    for (i = 0; i < result.length; i++)
    {
      // random byte [1..255]
      byte randomByte = (byte)(1 + random.nextInt(255));
      result[i] = randomByte;
    }
    return result;
  }

}

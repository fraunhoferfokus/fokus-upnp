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

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class provides helper methods for hash generation.
 * 
 * @author Alexander Koenig
 */
public class DigestHelper
{

  /** Generates a secret key for SHA1 HMAC */
  public static SecretKey generateSHA1HMACKey()
  {
    try
    {
      KeyGenerator kgen = KeyGenerator.getInstance("HmacSHA1");
      return kgen.generateKey();

    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Builds a SHA1 HMAC secret key from a byte array */
  public static SecretKey buildSHA1HMACKey(byte[] encodedKey)
  {
    try
    {
      SecretKeySpec keySpec = new SecretKeySpec(encodedKey, "HmacSHA1");
      return keySpec;

    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /** Calculates a SHA1-HMAC (Message authentication code) */
  public static byte[] calculateSHA1HMACForString(Key key, String dataString)
  {
    byte[] data = StringHelper.stringToByteArray(dataString);

    try
    {
      Mac hmac = Mac.getInstance(CommonConstants.HMAC_SHA_1_JAVA);
      hmac.init(key);
      return hmac.doFinal(data);
    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
    return null;
  }

  /**
   * Verifies a SHA1-HMAC signature.
   * 
   * @param signatureBase64
   *          The received signature
   * @param signatureSource
   *          The string that was signed
   * @param signatureKey
   *          The key used for signing
   * 
   * @return True if the signature could be verified, false otherwise.
   */
  public static boolean verifySHA1HMACForString(String signatureBase64, String signatureSource, Key signatureKey)
  {
    byte[] calculatedSignature = calculateSHA1HMACForString(signatureKey, signatureSource);
    String calculatedSignatureBase64 = Base64Helper.byteArrayToBase64(calculatedSignature);
    return calculatedSignatureBase64.equals(signatureBase64);
  }

  /** Calculates the hash value for an input byte array */
  public static byte[] calculateHash(String hashAlgorithm, byte[] data)
  {
    try
    {
      MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
      if (digest != null)
      {
        return digest.digest(data);
      }
    } catch (NoSuchAlgorithmException nsae)
    {
    }
    return null;
  }

  /** Calculates the hash value for a string */
  public static byte[] calculateSHAHashForString(String dataString)
  {
    byte[] data = StringHelper.stringToByteArray(dataString);
    return calculateHash(CommonConstants.SHA_1_JAVA, data);
  }

  /** Calculates the security ID for a string */
  public static String calculateSecurityIDForString(String dataString)
  {
    byte[] data = StringHelper.stringToByteArray(dataString);
    return hashToSecurityID(calculateHash(CommonConstants.SHA_1_JAVA, data));
  }

  /** Calculates the SHA-1 hash value for a byte array */
  public static byte[] calculateSHAHashForByteArray(byte[] data)
  {
    return calculateHash(CommonConstants.SHA_1_JAVA, data);
  }

  /** Calculates the hash of a RSA public key */
  public static byte[] calculateSHAHashForRSAPublicKey(RSAPublicKey key)
  {
    return calculateSHAHashForString(SecurityHelper.buildRSAPublicKeyXMLDescription(key));
  }

  /** Calculates a base64 encoded hash of a RSA public key */
  public static String calculateBase64SHAHashForRSAPublicKey(RSAPublicKey key)
  {
    return Base64Helper.byteArrayToBase64(calculateSHAHashForRSAPublicKey(key));
  }

  /**
   * Converts a SHA1 hash-value (160 bits = 20 bytes) to a user-readable string (the SecurityID)
   * 
   * @param hash
   *          The hash value with the most significant byte at hash[0]
   */
  public static String hashToSecurityID(byte[] hash)
  {
    return StringHelper.byteArrayToBase32(hash);
  }

  /** Converts a RSA public key into a securityID. */
  public static String rsaPublicKeyToSecurityID(RSAPublicKey publicKey)
  {
    return hashToSecurityID(calculateSHAHashForRSAPublicKey(publicKey));
  }
}

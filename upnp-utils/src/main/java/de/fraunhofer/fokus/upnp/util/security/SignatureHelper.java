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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;

/**
 * This class builds signature headers for SOAP messages.
 * 
 * @author Alexander Koenig
 */
public class SignatureHelper
{

  /** Calculates a RSA signature for a single digest */
  public static String createRSASignature(String digestID,
    String digestValue,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey)
  {
    String[] digestIDs = {
      digestID
    };
    String[] digestValues = {
      digestValue
    };
    return createRSASignature(digestIDs, digestValues, privateKey, publicKey);
  }

  /** Calculates a RSA signature for multiple digests */
  public static String createRSASignature(String[] digestID,
    String[] digestValue,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey)
  {
    if (digestID.length != digestValue.length)
    {
      System.out.println("Length of digestID and digestValue differs");
      return null;
    }

    String result = "";
    result += CommonSecurityConstant.SIGNATURE_TAG;
    result += CommonSecurityConstant.SIGNED_INFO_TAG;

    // build string that will be signed
    String signSource = createSignatureBody(digestID, digestValue);
    byte[] signature = PublicKeyCryptographyHelper.calculateRSASignatureForString(privateKey, signSource);
    String signatureBase64 = Base64Helper.byteArrayToBase64(signature);
    // System.out.println("Calculated signature: "+signatureBase64);

    result += signSource;
    result += CommonSecurityConstant.SIGNED_INFO_END_TAG;
    result += CommonSecurityConstant.SIGNATURE_VALUE_TAG;
    result += signatureBase64;
    result += CommonSecurityConstant.SIGNATURE_VALUE_END_TAG;

    result += CommonSecurityConstant.KEY_INFO_TAG;
    result += CommonSecurityConstant.KEY_VALUE_TAG;
    result += SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey);
    result += CommonSecurityConstant.KEY_VALUE_END_TAG;
    result += CommonSecurityConstant.KEY_INFO_END_TAG;
    result += CommonSecurityConstant.SIGNATURE_END_TAG;

    return result;
  }

  /** Calculates a SHA1 HMAC signature for multiple digests */
  public static String createSHA1HMACSignature(String[] digestID, String[] digestValue, Key signatureKey, int keyInfo)
  {
    if (digestID.length != digestValue.length)
    {
      System.out.println("Length of digestID and digestValue differs");
      return null;
    }

    String result = "";
    result += CommonSecurityConstant.SIGNATURE_TAG;
    result += CommonSecurityConstant.SIGNED_INFO_TAG;

    // build string that will be signed
    String signSource = createSignatureBody(digestID, digestValue);
    // calculate signature
    byte[] signature = DigestHelper.calculateSHA1HMACForString(signatureKey, signSource);
    String signatureBase64 = Base64Helper.byteArrayToBase64(signature);
    // System.out.println("Calculated signature: "+signatureBase64);

    result += signSource;
    result += CommonSecurityConstant.SIGNED_INFO_END_TAG;
    result += CommonSecurityConstant.SIGNATURE_VALUE_TAG;
    result += signatureBase64;
    result += CommonSecurityConstant.SIGNATURE_VALUE_END_TAG;

    result += CommonSecurityConstant.KEY_INFO_TAG;
    result += CommonSecurityConstant.KEY_NAME_TAG;
    result += keyInfo;
    result += CommonSecurityConstant.KEY_NAME_END_TAG;
    result += CommonSecurityConstant.KEY_INFO_END_TAG;
    result += CommonSecurityConstant.SIGNATURE_END_TAG;

    return result;
  }

  /** Calculates the signature body for a single digest */
  public static String createSignatureBody(String digestID, String digestValue)
  {
    String[] digestIDs = {
      digestID
    };
    String[] digestValues = {
      digestValue
    };
    return createSignatureBody(digestIDs, digestValues);
  }

  /** Calculates the signature body for multiple digests */
  public static String createSignatureBody(String[] digestID, String[] digestValue)
  {
    if (digestID.length != digestValue.length)
    {
      System.out.println("Length of digestID and digestValue differs");
      return null;
    }

    // build string that will be signed
    String signSource = "";
    signSource += CommonSecurityConstant.CANONICALIZATION_METHOD_TAG;
    signSource += CommonSecurityConstant.SIGNATURE_METHOD_TAG;
    for (int i = 0; i < digestID.length; i++)
    {
      signSource += "<Reference URI=\"#" + digestID[i] + "\">";
      signSource += CommonSecurityConstant.DIGEST_METHOD_TAG;
      signSource += CommonSecurityConstant.DIGEST_VALUE_TAG;
      signSource += digestValue[i];
      signSource += CommonSecurityConstant.DIGEST_VALUE_END_TAG;
      signSource += CommonSecurityConstant.REFERENCE_END_TAG;
    }
    return signSource;
  }

}

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
package de.fraunhofer.fokus.upnp.core_security.helpers;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This class represents an object that is identified by its public key hash. This can be a UPnP
 * control point, a device, an owner or a defined group.
 * 
 * @author Alexander Koenig
 */
public class SecurityAwareObject
{

  private String hashAlgorithm;

  private byte[] publicKeyHash;

  private String publicKeyHashBase64;

  private String securityID;

  /**
   * Creates a new instance of a security-aware object
   * 
   * @param algorithm
   *          The algorithm used for hashing the key (UPnP syntax)
   * @param keyHash
   *          The hash of the public key
   * 
   */
  public SecurityAwareObject(String algorithm, byte[] keyHash)
  {
    hashAlgorithm = algorithm;
    publicKeyHash = new byte[keyHash.length];
    publicKeyHashBase64 = Base64Helper.byteArrayToBase64(keyHash);
    System.arraycopy(keyHash, 0, publicKeyHash, 0, keyHash.length);
    securityID = DigestHelper.hashToSecurityID(publicKeyHash);
  }

  /**
   * Creates a new instance of a security-aware object
   * 
   * @param algorithm
   *          The algorithm used for hashing the key (UPnP syntax)
   * @param keyHashBase64
   *          The base64 encoded string of the hash of the public key
   * 
   */
  public SecurityAwareObject(String algorithm, String keyHashBase64)
  {
    hashAlgorithm = algorithm;
    publicKeyHashBase64 = keyHashBase64;
    publicKeyHash = Base64Helper.base64ToByteArray(keyHashBase64);
    securityID = DigestHelper.hashToSecurityID(publicKeyHash);
  }

  /** Checks if object has the same key as this object */
  public boolean equals(SecurityAwareObject object)
  {
    return securityID.equals(object.getSecurityID());
  }

  /** Checks if this object has the same key */
  public boolean equals(byte[] keyHash)
  {
    return DigestHelper.hashToSecurityID(keyHash).equals(DigestHelper.hashToSecurityID(publicKeyHash));
  }

  /** Retrieves the hash of the public key for this object */
  public byte[] getPublicKeyHash()
  {
    return publicKeyHash;
  }

  /** Retrieves the hash algorithm used for this object */
  public String getHashAlgorithm()
  {
    return hashAlgorithm;
  }

  /** Retrieves the hash of the public key for this object in Base64 encoding */
  public String getPublicKeyHashBase64()
  {
    return publicKeyHashBase64;
  }

  /** Retrieves the hash of the public key for this object as securityID */
  public String getSecurityID()
  {
    return securityID;
  }

  /** Checks if this object is a device */
  public boolean isDevice()
  {
    return false;
  }

  /** Checks if this object is a control point */
  public boolean isControlPoint()
  {
    return true;
  }

  /** Checks if this object is a named group */
  public boolean isGroup()
  {
    return false;
  }

  /** Retrieves an XML description of this object */
  public String toString()
  {
    return "<hash><algorithm>" + hashAlgorithm + "</algorithm><value>" + publicKeyHashBase64 + "</value></hash>";
  }

  /** Retrieves an XML description of this object */
  public String toXMLDescription()
  {
    return "<hash><algorithm>" + hashAlgorithm + "</algorithm><value>" + publicKeyHashBase64 + "</value></hash>";
  }

}

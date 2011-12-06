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
import java.security.interfaces.RSAPublicKey;

/**
 * This class is used to manage RSA public keys.
 * 
 * @author Alexander Koenig
 */
public class PersistentRSAPublicKey implements RSAPublicKey
{
  private static final long serialVersionUID = 1L;

  private BigInteger        publicExponent   = null;

  private BigInteger        modulus          = null;

  /** Creates an instance with known values */
  public PersistentRSAPublicKey(BigInteger publicExponent, BigInteger modulus)
  {
    this.publicExponent = publicExponent;
    this.modulus = modulus;
  }

  /* Key interface */
  public String getAlgorithm()
  {
    return "RSA";
  }

  public byte[] getEncoded()
  {
    return null;
  }

  public String getFormat()
  {
    return null;
  }

  public BigInteger getModulus()
  {
    return modulus;
  }

  public BigInteger getPublicExponent()
  {
    return publicExponent;
  }

  public boolean isValid()
  {
    return publicExponent != null && modulus != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object key)
  {
    if (key instanceof PersistentRSAPublicKey)
    {
      return publicExponent.equals(((PersistentRSAPublicKey)key).getPublicExponent()) &&
        modulus.equals(((PersistentRSAPublicKey)key).getModulus());
    }
    if (key instanceof RSAPublicKey)
    {
      return publicExponent.equals(((RSAPublicKey)key).getPublicExponent()) &&
        modulus.equals(((RSAPublicKey)key).getModulus());
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return modulus.hashCode();
  }

  /** Returns an XML description of this public key, suitable for UPnP security. */
  public String toXMLDescription()
  {
    return SecurityHelper.buildRSAPublicKeyXMLDescription(this);
  }

}

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

import javax.crypto.SecretKey;

/** This class stores key data for one control point. */
public class PersonalizedKeyObject
{

  private String                 name;

  private PersistentRSAPublicKey persistentRSAPublicKey;

  private SymmetricKeyInfo       symmetricKeyInfo;

  private String                 nonce;

  private String                 password;

  /**
   * Creates a new instance of PersonalizedKeyObject.
   * 
   * @param persistentRSAPublicKey
   * @param name
   */
  public PersonalizedKeyObject(PersistentRSAPublicKey persistentRSAPublicKey, String name)
  {
    this.persistentRSAPublicKey = persistentRSAPublicKey;
    this.name = name;
  }

  /**
   * Retrieves the iv.
   * 
   * @return The iv
   */
  public byte[] getIV()
  {
    if (symmetricKeyInfo == null)
    {
      return null;
    }

    return symmetricKeyInfo.getIV();
  }

  /**
   * Retrieves the password.
   * 
   * @return The password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Sets the password.
   * 
   * @param password
   *          The new value for password
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * Retrieves the aesKey.
   * 
   * @return The aesKey
   */
  public SecretKey getAESKey()
  {
    if (symmetricKeyInfo == null)
    {
      return null;
    }

    return symmetricKeyInfo.getAESKey();
  }

  /**
   * Sets the aesKey. symmetricKeyInfo
   * 
   * @param aesKey
   *          The new value for aesKey
   */
  public void setSymmetricKey(SecretKey aesKey, byte[] iv)
  {
    symmetricKeyInfo = new SymmetricKeyInfo(aesKey, iv);
  }

  /**
   * Retrieves the keyID.
   * 
   * @return The keyID
   */
  public String getKeyID()
  {
    if (symmetricKeyInfo == null)
    {
      return null;
    }

    return symmetricKeyInfo.getKeyID();
  }

  /**
   * Sets the keyID.
   * 
   * @param keyID
   *          The new value for keyID
   */
  public void setKeyID(String keyId)
  {
    if (symmetricKeyInfo == null)
    {
      return;
    }

    symmetricKeyInfo.setKeyID(keyId);
  }

  /**
   * Retrieves the name.
   * 
   * @return The name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          The new value for name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Retrieves the persistenRsaPublicKey.
   * 
   * @return The persistenRsaPublicKey
   */
  public PersistentRSAPublicKey getPersistentRSAPublicKey()
  {
    return persistentRSAPublicKey;
  }

  /**
   * Sets the persistenRsaPublicKey.
   * 
   * @param persistenRsaPublicKey
   *          The new value for persistenRsaPublicKey
   */
  public void setPersistentRSAPublicKey(PersistentRSAPublicKey persistenRsaPublicKey)
  {
    this.persistentRSAPublicKey = persistenRsaPublicKey;
  }

  /**
   * Retrieves the nonce.
   * 
   * @return The nonce
   */
  public String getNonce()
  {
    return nonce;
  }

  /**
   * Sets the nonce.
   * 
   * @param nonce
   *          The new value for nonce
   */
  public void setNonce(String nonce)
  {
    this.nonce = nonce;
  }

  /**
   * Retrieves the sequenceBase.
   * 
   * @return The sequenceBase
   */
  public long getSequenceBase()
  {
    if (symmetricKeyInfo == null)
    {
      return 0;
    }

    return symmetricKeyInfo.getSequence();
  }

  /**
   * Sets the sequenceBase.
   * 
   * @param sequenceBase
   *          The new value for sequenceBase
   */
  public void setSequenceBase(long sequenceBase)
  {
    if (symmetricKeyInfo == null)
    {
      return;
    }

    symmetricKeyInfo.setSequence(sequenceBase);
  }

  /** Returns an XML description of the name and public RSA key. */
  public String toRSAPublicKeyXMLDescription()
  {
    if (persistentRSAPublicKey == null || name == null)
    {
      return null;
    }

    return "<KeyValue><KeyOwner>" + name + "</KeyOwner>" + persistentRSAPublicKey.toXMLDescription() + "</KeyValue>";
  }
}

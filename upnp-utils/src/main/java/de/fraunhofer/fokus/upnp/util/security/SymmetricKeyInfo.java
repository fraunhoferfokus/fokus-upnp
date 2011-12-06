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

/**
 * This class contains an AES key and an initialization vector.
 * 
 * @author Alexander Koenig
 * 
 */
public class SymmetricKeyInfo
{

  private SecretKey aesKey;

  private byte[]    iv;

  private long      sequence;

  private String    keyID;

  private Object    lock = new Object();

  /**
   * Creates a new instance of SymmetricKeyInfo.
   * 
   * @param aesKey
   * @param iv
   */
  public SymmetricKeyInfo(SecretKey aesKey, byte[] iv)
  {
    this.aesKey = aesKey;
    this.iv = iv;
    this.sequence = SecurityHelper.createLongSequenceBase();
    this.keyID = null;
  }

  /**
   * Creates a new instance of SymmetricKeyInfo.
   * 
   * @param aesKey
   * @param iv
   * @param sequence
   * @param keyID
   */
  public SymmetricKeyInfo(String keyID)
  {
    this.aesKey = SymmetricCryptographyHelper.generateAESKey();
    this.iv = SymmetricCryptographyHelper.generateIV();
    this.sequence = SecurityHelper.createLongSequenceBase();
    this.keyID = keyID;
  }

  /** Retrieves a sync object for simultaneous access. */
  public Object getLock()
  {
    return lock;
  }

  /**
   * Retrieves the aesKey.
   * 
   * @return The aesKey
   */
  public SecretKey getAESKey()
  {
    return aesKey;
  }

  /**
   * Retrieves the iv.
   * 
   * @return The iv
   */
  public byte[] getIV()
  {
    return iv;
  }

  /**
   * Retrieves the keyName.
   * 
   * @return The keyName
   */
  public String getKeyID()
  {
    return keyID;
  }

  /**
   * Sets the keyName.
   * 
   * @param keyID
   *          The new value for keyName
   */
  public void setKeyID(String keyID)
  {
    this.keyID = keyID;
  }

  /**
   * Retrieves the sequence.
   * 
   * @return The sequence
   */
  public long getSequence()
  {
    return sequence;
  }

  /**
   * Increases the sequence.
   * 
   */
  public void incSequence()
  {
    sequence++;
  }

  /**
   * Sets the sequence.
   * 
   * @param sequence
   *          The new value for sequence
   */
  public void setSequence(long sequence)
  {
    this.sequence = sequence;
  }

}

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
package de.fraunhofer.fokus.upnp.core_security.device;

import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class is used to encapsulate security related information for one remote control point.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SecurityAwareControlPointObject extends SecurityAwareObject
{

  /** Public key of the control point */
  private RSAPublicKey     rsaPublicKey;

  /** Name for the control point object which must be unique across the device lifetime */
  private String           symmetricKeyName;

  /** Symmetric key info */
  private SymmetricKeyInfo symmetricKeyInfo;

  /** List with subscription IDs where events should be signed with this symmetric key */
  private Vector           sidList;

  /**
   * Creates a new instance of SecurityAwareControlPointObject.
   * 
   * @param securedDevice
   *          The device that handles the remote control point
   * @param controlPointKey
   *          The public key of the remote control point
   * 
   */
  public SecurityAwareControlPointObject(SecuredTemplateDevice securedDevice, RSAPublicKey controlPointKey)
  {
    super(CommonConstants.SHA_1_UPNP, DigestHelper.calculateSHAHashForRSAPublicKey(controlPointKey));

    this.rsaPublicKey = controlPointKey;

    this.symmetricKeyName = securedDevice.createKeyName();
    symmetricKeyInfo = new SymmetricKeyInfo(symmetricKeyName);

    sidList = new Vector();
  }

  /**
   * Retrieves the symmetricSecurityKey.
   * 
   * @return The symmetricSecurityKey
   */
  public SecretKey getSymmetricKey()
  {
    return symmetricKeyInfo.getAESKey();
  }

  /**
   * Retrieves the iv.
   * 
   * @return The iv
   */
  public byte[] getSymmetricIV()
  {
    return symmetricKeyInfo.getIV();
  }

  /**
   * Retrieves the rsaPublicKey.
   * 
   * @return The rsaPublicKey
   */
  public RSAPublicKey getRSAPublicKey()
  {
    return rsaPublicKey;
  }

  /**
   * Retrieves the sequence.
   * 
   * @return The sequence
   */
  public long getSymmetricSequence()
  {
    return symmetricKeyInfo.getSequence();
  }

  /**
   * Sets the sequence.
   * 
   * @param sequence
   *          The new value for sequence
   */
  public void setSymmetricSequence(long sequence)
  {
    symmetricKeyInfo.setSequence(sequence);
  }

  /**
   * Retrieves the keyName.
   * 
   * @return The keyName
   */
  public String getSymmetricKeyName()
  {
    return symmetricKeyName;
  }

  /**
   * Retrieves the symmetricKeyInfo.
   * 
   * @return The symmetricKeyInfo
   */
  public SymmetricKeyInfo getSymmetricKeyInfo()
  {
    return symmetricKeyInfo;
  }

  /**
   * Sets the symmetricKeyInfo.
   * 
   * @param symmetricKeyInfo
   *          The new value for symmetricKeyInfo
   */
  public void setSymmetricKeyInfo(SymmetricKeyInfo symmetricKeyInfo)
  {
    this.symmetricKeyInfo = symmetricKeyInfo;
  }

  /**
   * Checks if the SID is known by the secured device.
   * 
   * @return True if sid is valid, false otherwise
   */
  public boolean isKnownSID(String sid)
  {
    return sidList.contains(sid);
  }

  /**
   * Sets the sid.
   * 
   * @param sid
   *          The new value for sid
   */
  public void addSID(String sid)
  {
    if (!sidList.contains(sid))
    {
      sidList.add(sid);
    }
  }

}

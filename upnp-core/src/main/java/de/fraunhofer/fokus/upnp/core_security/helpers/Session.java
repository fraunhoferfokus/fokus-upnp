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

import java.security.Key;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.SessionKeysParser;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class holds all data that is used for a session by either a control point or a device.
 * 
 * @author Alexander Koenig
 */
public class Session
{

  private SecretKey                   confidentialityToDeviceKey   = null; // used for encrypted
                                                                            // soap messages

  private SecretKey                   confidentialityFromDeviceKey = null; // used for encrypted
                                                                            // soap replies

  private Key                         signingToDeviceKey           = null; // used for signing soap
                                                                            // messages

  private Key                         signingFromDeviceKey         = null; // used for signing soap
                                                                            // replies

  private SecurityAwareCPDeviceObject associatedDeviceObject       = null;

  private int                         cpKeyID;

  private int                         deviceKeyID;

  private String                      sequenceBase;

  private int                         cpSequenceNumber;

  private int                         deviceSequenceNumber;

  // stored for revokeOwnership to verify caller
  private RSAPublicKey                controlPointPublicKey;

  /** Creates a new instance of Session */
  public Session(int cpKeyID, SecurityAwareCPDeviceObject device)
  {
    this.cpKeyID = cpKeyID;
    this.associatedDeviceObject = device;
    controlPointPublicKey = null;

    // create confidentiality keys
    confidentialityToDeviceKey = SymmetricCryptographyHelper.generateAESKey();
    confidentialityFromDeviceKey = SymmetricCryptographyHelper.generateAESKey();
    // create signing keys
    signingToDeviceKey = DigestHelper.generateSHA1HMACKey();
    signingFromDeviceKey = DigestHelper.generateSHA1HMACKey();

    sequenceBase = SecurityHelper.createSequenceBaseString();
    ;
    cpSequenceNumber = 1;
    deviceSequenceNumber = 1;
  }

  /** Creates a new instance of Session */
  public Session(String sessionKeys, int cpKeyID, RSAPublicKey controlPointPublicKey)
  {
    SessionKeysParser parser = new SessionKeysParser(sessionKeys);
    confidentialityToDeviceKey = parser.getConfidentialityToDeviceKey();
    confidentialityFromDeviceKey = parser.getConfidentialityFromDeviceKey();
    signingToDeviceKey = parser.getSigningToDeviceKey();
    signingFromDeviceKey = parser.getSigningFromDeviceKey();
    this.cpKeyID = cpKeyID;
    this.controlPointPublicKey = controlPointPublicKey;
    sequenceBase = SecurityHelper.createSequenceBaseString();
    cpSequenceNumber = 1;
    deviceSequenceNumber = 1;
  }

  public boolean isValid()
  {
    return confidentialityToDeviceKey != null && confidentialityFromDeviceKey != null && signingToDeviceKey != null &&
      signingFromDeviceKey != null;
  }

  public SecretKey getConfidentialityToDeviceKey()
  {
    return confidentialityToDeviceKey;
  }

  public SecretKey getConfidentialityFromDeviceKey()
  {
    return confidentialityFromDeviceKey;
  }

  public Key getSigningToDeviceKey()
  {
    return signingToDeviceKey;
  }

  public Key getSigningFromDeviceKey()
  {
    return signingFromDeviceKey;
  }

  public RSAPublicKey getControlPointPublicKey()
  {
    return controlPointPublicKey;
  }

  public SecurityAwareCPDeviceObject getAssociatedDeviceObject()
  {
    return associatedDeviceObject;
  }

  public int getCpKeyID()
  {
    return cpKeyID;
  }

  public void setCpKeyID(int keyID)
  {
    cpKeyID = keyID;
  }

  public int getDeviceKeyID()
  {
    return deviceKeyID;
  }

  public void setDeviceKeyID(int keyID)
  {
    deviceKeyID = keyID;
  }

  public String getSequenceBase()
  {
    return sequenceBase;
  }

  public void setSequenceBase(String sequenceBase)
  {
    this.sequenceBase = sequenceBase;
  }

  public int getCPSequenceNumber()
  {
    return cpSequenceNumber;
  }

  public void incCPSequenceNumber()
  {
    cpSequenceNumber++;
  }

  public void setCPSequenceNumber(int number)
  {
    cpSequenceNumber = number;
  }

  public int getDeviceSequenceNumber()
  {
    return deviceSequenceNumber;
  }

  public void incDeviceSequenceNumber()
  {
    deviceSequenceNumber++;
  }

  /** Retrieves the XML description of this session key object */
  public String toXMLDescription()
  {
    return "<SessionKeys>" + "<Confidentiality>" + "<Algorithm>" + CommonSecurityConstant.AES_128_CBC_UPNP +
      "</Algorithm>" + "<KeyToDevice>" + Base64Helper.byteArrayToBase64(confidentialityToDeviceKey.getEncoded()) +
      "</KeyToDevice>" + "<KeyFromDevice>" + Base64Helper.byteArrayToBase64(confidentialityFromDeviceKey.getEncoded()) +
      "</KeyFromDevice>" + "</Confidentiality>" + "<Signing>" + "<Algorithm>" + CommonSecurityConstant.HMAC_SHA_1_UPNP +
      "</Algorithm>" + "<KeyToDevice>" + Base64Helper.byteArrayToBase64(signingToDeviceKey.getEncoded()) +
      "</KeyToDevice>" + "<KeyFromDevice>" + Base64Helper.byteArrayToBase64(signingFromDeviceKey.getEncoded()) +
      "</KeyFromDevice>" + "</Signing>" + "</SessionKeys>";
  }
}

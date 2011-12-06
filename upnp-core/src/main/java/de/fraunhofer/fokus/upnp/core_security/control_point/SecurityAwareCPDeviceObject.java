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
package de.fraunhofer.fokus.upnp.core_security.control_point;

import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.ACLParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.AlgorithmsAndProtocolsParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.OwnersParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.PermissionsParser;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class extends the security aware object for security aware remote devices that have
 * additional properties.
 * 
 * @author Alexander Koenig
 */
public class SecurityAwareCPDeviceObject extends SecurityAwareObject
{

  /** Associated remote device */
  private CPDevice         device                  = null;

  /** Protocols understood by the remote device */
  private Vector           protocols               = new Vector();

  /** Hash algorithms understood by the remote device */
  private Vector           hashAlgorithms          = new Vector();

  private Vector           encryptionAlgorithms    = new Vector();

  private Vector           signingAlgorithms       = new Vector();

  private Vector           owners                  = new Vector();

  private boolean          ownersRead              = false;

  private boolean          ownersAccessDenied      = false;

  private Vector           permissions             = new Vector();

  private boolean          permissionsRead         = false;

  private boolean          permissionsAccessDenied = false;

  private Permission       anyPermission;

  private Vector           acl                     = new Vector();

  private boolean          aclAccessDenied         = false;

  private String           aclVersion              = "";

  private RSAPublicKey     confidentialityKey;

  /** Used key to sign description and event messages */
  private SymmetricKeyInfo symmetricKeyInfo;

  /**
   * Creates a new instance of a security-aware device
   * 
   * @param algorithm
   *          The algorithm used for hashing the key
   * @param keyHash
   *          The hash of the public key
   * 
   */
  public SecurityAwareCPDeviceObject(String algorithm, RSAPublicKey confidentialityKey, CPDevice device)
  {
    super(algorithm, DigestHelper.calculateSHAHashForRSAPublicKey(confidentialityKey));
    this.device = device;
    this.confidentialityKey = confidentialityKey;
    anyPermission = new Permission("Any rights", "<any/>", "", "This permission allows access to all device actions");
  }

  /** Retrieves the associated device */
  public CPDevice getCPDevice()
  {
    return device;
  }

  public boolean isDevice()
  {
    return true;
  }

  public boolean isControlPoint()
  {
    return false;
  }

  public CPService getDeviceSecurityService()
  {
    if (device != null)
    {
      return device.getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
    }
    return null;
  }

  /** Removes all information gathered about the device */
  public void removeDeviceInformation()
  {
    protocols.clear();
    hashAlgorithms.clear();
    encryptionAlgorithms.clear();
    signingAlgorithms.clear();
    owners.clear();
    ownersRead = false;
    permissions.clear();
    permissionsRead = false;
    aclVersion = "";
    acl.clear();
  }

  /** Retrieves all supported protocols */
  public Vector getProtocols()
  {
    return protocols;
  }

  /** Retrieves all supported hash algorithms */
  public Vector getHashAlgorithms()
  {
    return hashAlgorithms;
  }

  /** Retrieves all supported encryption algorithms */
  public Vector getEncryptionAlgorithms()
  {
    return encryptionAlgorithms;
  }

  /** Retrieves all supported signing algorithms */
  public Vector getSigningAlgorithms()
  {
    return signingAlgorithms;
  }

  /** Retrieves all owners */
  public Vector getOwners()
  {
    return owners;
  }

  /** Checks if the owners were already read */
  public boolean hasValidOwners()
  {
    return ownersRead;
  }

  /** Checks if the owners cannot be read */
  public boolean hasOwnersAccessDenied()
  {
    return ownersAccessDenied;
  }

  /** Set if the owners cannot be read */
  public void setOwnersAccessDenied()
  {
    ownersAccessDenied = true;
    ownersRead = false;
  }

  /** Retrieves all defined permissions */
  public Vector getPermissions()
  {
    return permissions;
  }

  /** Retrieves the any permission */
  public Permission getAnyPermission()
  {
    return anyPermission;
  }

  /** Checks if the permissions were already read */
  public boolean hasValidPermissions()
  {
    return permissionsRead;
  }

  /** Checks if the permissions cannot be read */
  public boolean hasPermissionsAccessDenied()
  {
    return permissionsAccessDenied;
  }

  /** Set if the permissions cannot be read */
  public void setPermissionsAccessDenied()
  {
    permissionsAccessDenied = true;
    permissionsRead = false;
  }

  /**
   * Retrieves a specific permission
   * 
   * @param aclEntry
   *          A device specific ACL entry, e.g. <mfgr:play/>
   */
  public Permission getPermission(String aclEntry)
  {
    for (int i = 0; i < permissions.size(); i++)
    {
      if (((Permission)permissions.elementAt(i)).getACLEntry().equals(aclEntry))
      {
        return (Permission)permissions.elementAt(i);
      }
    }
    if (aclEntry.equals(anyPermission.getACLEntry()))
    {
      return anyPermission;
    }

    return null;
  }

  /** Retrieves the ACL */
  public Vector getACL()
  {
    return acl;
  }

  /** Retrieves the last read ACL version */
  public String getACLVersion()
  {
    return aclVersion;
  }

  /** Checks if the ACL is valid */
  public boolean hasValidACL()
  {
    return aclVersion.length() > 0;
  }

  /** Checks if the ACL cannot be read */
  public boolean hasACLAccessDenied()
  {
    return aclAccessDenied;
  }

  /** Set if the ACL cannot be read */
  public void setACLAccessDenied()
  {
    aclAccessDenied = true;
    aclVersion = "";
  }

  /** Retrieves the confidentiality key of the associated device */
  public RSAPublicKey getConfidentialityKey()
  {
    return confidentialityKey;
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
   * Retrieves the symmetricSecurityKeyName.
   * 
   * @return The symmetricSecurityKeyName
   */
  public String getSymmetricKeyName()
  {
    if (symmetricKeyInfo != null)
    {
      return symmetricKeyInfo.getKeyID();
    }

    return null;
  }

  /** Sets the list of all supported protocols */
  public void setSupported(AlgorithmsAndProtocolsParser parser)
  {
    protocols = parser.getProtocols();
    hashAlgorithms = parser.getHashAlgorithms();
    encryptionAlgorithms = parser.getEncryptionAlgorithms();
    signingAlgorithms = parser.getSigningAlgorithms();
  }

  /** Adds a owner to the owner list */
  public void addOwner(SecurityAwareObject newOwner)
  {
    if (getOwnerIndex(newOwner) == -1)
    {
      owners.add(newOwner);
    }
  }

  /** Removes a owner from the owner list */
  public void removeOwner(SecurityAwareObject owner)
  {
    int index = getOwnerIndex(owner);
    if (index != -1)
    {
      owners.remove(index);
    }
  }

  /** Retrieves the index of a specific owner */
  private int getOwnerIndex(SecurityAwareObject owner)
  {
    for (int i = 0; i < owners.size(); i++)
    {
      if (((SecurityAwareObject)owners.elementAt(i)).getSecurityID().equals(owner.getSecurityID()))
      {
        return i;
      }
    }
    return -1;
  }

  /** Sets the list of all owners */
  public void setOwners(OwnersParser parser)
  {
    owners = parser.getOwners();
    ownersRead = true;
    ownersAccessDenied = false;
  }

  /** Sets the list of all permissions */
  public void setPermissions(PermissionsParser parser)
  {
    permissions = parser.getPermissions();
    permissionsRead = true;
    permissionsAccessDenied = false;
  }

  /** Sets the list of all ACL entries */
  public void setACL(ACLParser parser)
  {
    acl = parser.getACLList();
    aclAccessDenied = false;
  }

  /** Sets the last read ACL version */
  public void setACLVersion(String version)
  {
    aclVersion = version;
  }

}

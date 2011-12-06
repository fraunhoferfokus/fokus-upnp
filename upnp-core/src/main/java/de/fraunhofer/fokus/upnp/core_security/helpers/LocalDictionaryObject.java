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

import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;

/**
 * This class is used by the SecurityConsole to name control points and devices on a per user basis.
 * 
 * @author Alexander Koenig
 */
public class LocalDictionaryObject
{

  private String              userDefinedName;

  /** Associated object holding the public key of the device or control point */
  private SecurityAwareObject securityAwareObject;

  /** Flag that the device or control point is currently available */
  private boolean             online;

  /**
   * Creates a new instance of a local dictionary object
   * 
   * @param name
   *          The user defined name
   * @param object
   *          The associated security aware object
   * 
   */
  public LocalDictionaryObject(String name, SecurityAwareObject object)
  {
    userDefinedName = name.replace(':', '.');
    securityAwareObject = object;
    online = false;
  }

  public boolean equals(LocalDictionaryObject object)
  {
    return securityAwareObject.getSecurityID().equals(object.getSecurityAwareObject().getSecurityID());
  }

  /** Retrieves a reference to the security aware object associated with this dictionary entry */
  public SecurityAwareObject getSecurityAwareObject()
  {
    return securityAwareObject;
  }

  /** Sets a new associated security aware object */
  public void setSecurityAwareObject(SecurityAwareObject object)
  {
    securityAwareObject = object;
  }

  /** Retrieves a reference to the associated CP device object if known */
  public SecurityAwareCPDeviceObject getSecurityAwareCPDeviceObject()
  {
    if (securityAwareObject instanceof SecurityAwareCPDeviceObject)
    {
      return (SecurityAwareCPDeviceObject)securityAwareObject;
    }

    return null;
  }

  public String getUserDefinedName()
  {
    return userDefinedName;
  }

  public void setUserDefinedName(String name)
  {
    // update the user defined name for the device or control point
    userDefinedName = name.replace(':', '.');
  }

  public boolean isOnline()
  {
    return online;
  }

  public void setOnline(boolean state)
  {
    online = state;
    // remove all online data from device
    if (!state && getSecurityAwareCPDeviceObject() != null)
    {
      getSecurityAwareCPDeviceObject().removeDeviceInformation();
    }
  }

}

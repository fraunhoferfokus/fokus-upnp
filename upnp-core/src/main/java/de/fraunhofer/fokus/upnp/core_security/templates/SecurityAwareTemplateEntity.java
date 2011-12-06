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
package de.fraunhofer.fokus.upnp.core_security.templates;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;

/**
 * This class can contain a security aware UPnP control point and/or several secured and unsecured
 * UPnP devices. Entities may be executable. SecurityAwareTemplateEntity should be used as template
 * for all derived security aware UPnP entities.
 * 
 * @author Alexander Koenig
 */
public class SecurityAwareTemplateEntity extends TemplateEntity
{
  /** Creates a new instance of SecurityAwareTemplateEntity with a standard startup configuration. */
  public SecurityAwareTemplateEntity()
  {
    this(null);
  }

  /** Creates a new instance of SecurityAwareTemplateEntity with a predefined startup configuration. */
  public SecurityAwareTemplateEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
  }

  /** Event for discovered security aware devices */
  public void newSecurityAwareCPDevice(SecurityAwareCPDeviceObject device)
  {
  }

  /** Event for removal of a security aware devices */
  public void securityAwareCPDeviceGone(SecurityAwareCPDeviceObject device)
  {
  }

  /** Retrieves the associated security aware control point */
  public SecurityAwareTemplateControlPoint getSecurityAwareControlPoint()
  {
    if (getTemplateControlPoint() instanceof SecurityAwareTemplateControlPoint)
    {
      return (SecurityAwareTemplateControlPoint)getTemplateControlPoint();
    } else
    {
      return null;
    }
  }

  /** Retrieves the associated security aware device */
  public SecuredTemplateDevice getSecuredDevice()
  {
    if (getTemplateDevice() instanceof SecuredTemplateDevice)
    {
      return (SecuredTemplateDevice)getTemplateDevice();
    } else
    {
      return null;
    }
  }

}

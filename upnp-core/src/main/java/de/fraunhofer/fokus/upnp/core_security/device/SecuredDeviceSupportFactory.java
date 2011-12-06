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

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceNotifyAdvertiser;
import de.fraunhofer.fokus.upnp.core.device.DeviceSupportFactory;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;

/**
 * This class is used to instantiate the different support classes needed by the device.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredDeviceSupportFactory extends DeviceSupportFactory
{

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceSupportFactory#getInstanceOfDeviceNotifyAdvertiser(de.fhg.fokus.magic.upnp.device.Device)
   */
  public DeviceNotifyAdvertiser getInstanceOfDeviceNotifyAdvertiser(Device device)
  {
    if (device instanceof SecuredTemplateDevice)
    {
      return new SecuredDeviceNotifyAdvertiser((SecuredTemplateDevice)device);
    }
    System.out.println("ERROR: Device is not secured");
    return new DeviceNotifyAdvertiser(device);
  }

}

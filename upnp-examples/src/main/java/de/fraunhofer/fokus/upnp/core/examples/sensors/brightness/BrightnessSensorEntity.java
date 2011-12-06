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
package de.fraunhofer.fokus.upnp.core.examples.sensors.brightness;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.lsf.core.templates.BinaryToUPnPTemplateDevice;
import de.fraunhofer.fokus.lsf.core.templates.BinaryToUPnPTemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class encapsulates brightness sensor UPnP devices.
 * 
 * @author Alexander Koenig
 */
public class BrightnessSensorEntity extends BinaryToUPnPTemplateEntity
{

  /** Creates a new instance of BrightnessSensorEntity */
  public BrightnessSensorEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration, BinaryUPnPConstants.ServiceTypeBrightnessSensor);
  }

  public static void main(String[] args)
  {
    new BrightnessSensorEntity(null);
  }

  public BinaryToUPnPTemplateDevice createNewDevice(BinaryCPDevice newDevice, BinaryCPService service)
  {
    System.out.println("Create new UPnP device for " + newDevice.getName());
    return new BrightnessSensorDevice(this, startupConfiguration, newDevice);
  }

}

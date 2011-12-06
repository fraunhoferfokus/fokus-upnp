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
package de.fraunhofer.fokus.upnp.core.examples.clock;

import java.util.Calendar;

import de.fraunhofer.fokus.lsf.core.device.BinaryDevice;

/**
 * This class encapsulates a device providing a binary clock service.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryClockDevice extends BinaryDevice
{

  /**
   * Creates a new instance of BinaryClockDevice.
   * 
   * @param deviceID
   * @param deviceType
   * @param expectedLifeTime
   * @param name
   * @param application
   * @param manufacturer
   */
  public BinaryClockDevice(long deviceID,
    int deviceType,
    int expectedLifeTime,
    String name,
    String application,
    String manufacturer)
  {
    super(deviceID, deviceType, expectedLifeTime, name, application, manufacturer);
    // set description date to current date 
    Calendar calender = Calendar.getInstance();
    long deviceDescriptionDate =
      ((long)calender.get(Calendar.YEAR) << 24) + (calender.get(Calendar.MONTH) + 1 << 16) +
        (calender.get(Calendar.DAY_OF_MONTH) << 8);
    setDeviceDescriptionDate(deviceDescriptionDate);
    // add clock service
    addDeviceService(new BinaryClockService(this));
  }

}

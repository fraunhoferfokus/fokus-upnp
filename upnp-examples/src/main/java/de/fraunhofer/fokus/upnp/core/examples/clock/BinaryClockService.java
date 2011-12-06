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

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.device.BinaryDeviceService;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class encapsulates a binary UPnP clock service.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryClockService extends BinaryDeviceService implements IEventListener
{

  private long lastEvent = 0;

  /**
   * Creates a new instance of BinaryClockService.
   * 
   */
  public BinaryClockService(BinaryClockDevice binaryClockDevice)
  {
    // use type also as ID
    super(binaryClockDevice,
      BinaryUPnPConstants.ServiceTypeClock,
      BinaryUPnPConstants.ServiceTypeClock,
      "ClockService",
      "",
      BinaryUPnPConstants.VarTypeByteArray,
      true);
    binaryClockDevice.registerEventListener(this);
    value.fromByteArray(getCurrentServiceValue());
  }

  /** Returns the current time as byte array. */
  private byte[] getCurrentServiceValue()
  {
    byte[] result = new byte[13];
    // we use 4 bytes for the seconds 
    long timeSeconds = System.currentTimeMillis() / 1000;
    System.arraycopy(ByteArrayHelper.uint32ToByteArray(timeSeconds), 0, result, 0, 4);

    Calendar calendar = DateTimeHelper.dateToCalendar(DateTimeHelper.getDate());
    System.arraycopy(ByteArrayHelper.uint16ToByteArray(calendar.get(Calendar.YEAR)), 0, result, 4, 2);
    result[6] = (byte)(calendar.get(Calendar.MONTH) + 1);
    result[7] = (byte)calendar.get(Calendar.DAY_OF_MONTH);
    result[8] = (byte)calendar.get(Calendar.HOUR_OF_DAY);
    result[9] = (byte)calendar.get(Calendar.MINUTE);
    result[10] = (byte)calendar.get(Calendar.SECOND);
    System.arraycopy(ByteArrayHelper.uint16ToByteArray(calendar.get(Calendar.MILLISECOND)), 0, result, 11, 2);
    return result;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    byte[] newServiceValue = getCurrentServiceValue();
    // update service value
    value.fromByteArray(newServiceValue);
    // trigger one event per minute
    if (System.currentTimeMillis() - lastEvent > 2000 && newServiceValue[10] == 0)
    {
      lastEvent = System.currentTimeMillis();
      binaryDevice.addEvent(this);
    }
  }

}

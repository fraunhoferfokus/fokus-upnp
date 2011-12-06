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
package de.fraunhofer.fokus.lsf.core.event;

import java.util.EventListener;

import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;

/**
 * The listener interface for new device and device gone events. A class that wishes to be informed about device events
 * should implement this interface.
 * 
 * @author Alexander Koenig
 * 
 */
public interface IBinaryCPDeviceEventListener extends EventListener
{

  /**
   * Event that a new binary UPnP device was found.
   * 
   * @param newDevice
   *          The discovered UPnP device
   */
  public void newDevice(BinaryCPDevice newDevice);

  /**
   * Event that meta data of a binary UPnP device has changed.
   * 
   * @param changedDevice
   *          The changed UPnP device
   * @param eventCode
   *          Type of change
   */
  public void changedDevice(BinaryCPDevice changedDevice, int eventCode);

  /**
   * Event that a binary UPnP device has been removed from the network.
   * 
   * @param goneDevice
   *          The device that has been removed
   */
  public void deviceGone(BinaryCPDevice goneDevice);
}

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
package de.fraunhofer.fokus.lsf.core.control_point;

/**
 * This class provides constants for remote device events.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryCPConstants
{

  /** Event that a new announcement for the device has been received */
  public static final int EVENT_CODE_EXPIRATION_TIME_CHANGE = 1;

  /** Event that the device address has changed */
  public static final int EVENT_CODE_DEVICE_ADDRESS_CHANGE  = 2;

  /** Event that the path to the device has changed */
  public static final int EVENT_CODE_PATH_CHANGE            = 4;

  /** Event that the name, the application or the description date has changed */
  public static final int EVENT_CODE_META_DATA_CHANGE       = 8;

  /** Event that provided services of a device have changed */
  public static final int EVENT_CODE_SERVICE_CHANGE         = 16;

  /** Event that the management state of a service has changed */
  public static final int EVENT_CODE_SERVICE_STATE_CHANGE   = 32;

  /** Event that the ports for a device have changed */
  public static final int EVENT_CODE_PORT_CHANGE            = 64;

  public static final int EVENT_CODE_ALL                    = 0xFFFF;

}

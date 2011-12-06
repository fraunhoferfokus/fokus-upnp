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
package de.fraunhofer.fokus.upnp.core_security.securityConsole;

import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;

/**
 * 
 * @author Alexander Koenig
 */
public interface ISecurityConsoleEvents
{

  /**
   * Event for security aware device discovery. Signals the detection of a device that implements
   * the DeviceSecurityService
   */
  public void newSecurityAwareCPDevice(LocalDictionaryObject device);

  /**
   * Event if the status of a security aware device changes
   */
  public void securityAwareCPDeviceStatusChange(LocalDictionaryObject device);

  /**
   * Event for security aware control point discovery. Signals the detection of a control point that
   * presented itself via PresentKey() in a SecurityConsole
   */
  public void newSecurityAwareControlPoint(LocalDictionaryObject controlPoint);

  /**
   * Event if the status of a security aware control point changes
   */
  public void securityAwareControlPointStatusChange(LocalDictionaryObject controlPoint);

  /**
   * Event that the name of a local dictionary object (device or control point) was changed
   */
  public void localDictionaryNameChange(LocalDictionaryObject localDictionaryObject);

}

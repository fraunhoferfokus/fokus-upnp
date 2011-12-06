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
package de.fraunhofer.fokus.upnp.core_security;

import java.util.EventListener;

import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core_security.helpers.ActionSecurityInfo;

/**
 * The listener interface for secured action events. The class responsible for invoking an secured
 * action in the device should implement this interface.
 * 
 * @author Alexander Koenig
 * 
 */
public interface InvokeSecuredActionListener extends EventListener
{
  /**
   * Invoke when an invoke action request is received from a control point.
   * 
   * @param serviceID
   *          service unique identification
   * @param action
   *          action to be invoked
   * @param securityInfo
   *          Additional info needed for control point verification
   * 
   * @return true if invokeAction successful, otherwise false
   */
  public boolean invokeSecuredLocalAction(String serviceID, Action action, ActionSecurityInfo securityInfo);
}

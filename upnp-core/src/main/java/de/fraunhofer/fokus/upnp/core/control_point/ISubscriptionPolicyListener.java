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
package de.fraunhofer.fokus.upnp.core.control_point;

/**
 * This interface is used to handle the subscription policy for different devices and services. It
 * can be used by control points to automatically subscribe to certain services.
 * 
 * @author Alexander Koenig
 * 
 */
public interface ISubscriptionPolicyListener
{

  /**
   * Checks the subscription mode for a certain service.
   * 
   * @param service
   *          The service which subscription policy should be checked
   * 
   * @return UPnPConstant.SUBSCRIPTION_MODE_MANUAL to force a manual subscription or
   *         UPnPConstant.SUBSCRIPTION_MODE_AUTOMATIC to subscribe automatically to the service
   */
  public int getSubscriptionPolicy(CPService service);

}

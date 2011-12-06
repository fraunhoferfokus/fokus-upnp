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
package de.fraunhofer.fokus.upnp.core_security.control_point;

import de.fraunhofer.fokus.upnp.core.control_point.CPEventMessageProcessor;
import de.fraunhofer.fokus.upnp.core.control_point.CPMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core.control_point.CPSSDPMessageProcessor;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;

/**
 * This class is used to instantiate the different message processors used by security aware control
 * points.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityAwareCPMessageProcessorFactory extends CPMessageProcessorFactory
{

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPMessageProcessorFactory#getInstanceOfCPEventMessageProcessor(de.fhg.fokus.magic.upnp.control_point.ControlPoint)
   */
  public CPEventMessageProcessor getInstanceOfCPEventMessageProcessor(ControlPoint controlPoint)
  {
    // System.out.println("Create instance of SecurityAwareCPEventMessageProcessor");
    return new SecurityAwareCPEventMessageProcessor(controlPoint);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPMessageProcessorFactory#getInstanceOfCPSSDPMessageProcessor(de.fhg.fokus.magic.upnp.control_point.ControlPoint)
   */
  public CPSSDPMessageProcessor getInstanceOfCPSSDPMessageProcessor(ControlPoint controlPoint)
  {
    // System.out.println("Create instance of SecurityAwareCPSSDPMessageProcessor");
    return new SecurityAwareCPSSDPMessageProcessor(controlPoint);
  }

}

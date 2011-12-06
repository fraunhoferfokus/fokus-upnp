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

import de.fraunhofer.fokus.upnp.core.AbstractAction;

/**
 * This class represent a remote view on a UPnP action.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class CPAction extends AbstractAction
{

  /** Associated service for remote actions */
  private CPService cpService;

  /**
   * Creates an action object with only action name.
   * 
   * @param name
   *          action name
   */
  public CPAction(String name, CPService cpService)
  {
    super(name);
    this.cpService = cpService;
  }

  /**
   * Retrieves the cpService.
   * 
   * @return The cpService
   */
  public CPService getCPService()
  {
    return cpService;
  }

  /**
   * Sets the cpService.
   * 
   * @param cpService
   *          The new value for cpService
   */
  public void setCPService(CPService cpService)
  {
    this.cpService = cpService;
  }

}

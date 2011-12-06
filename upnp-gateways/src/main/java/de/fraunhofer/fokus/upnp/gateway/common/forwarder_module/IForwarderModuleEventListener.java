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
package de.fraunhofer.fokus.upnp.gateway.common.forwarder_module;

/**
 * This interface can be used to get informed about changes in forwarder modules.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public interface IForwarderModuleEventListener
{

  /**
   * Event that the forwarder module is connected.
   * 
   * @param module
   *          The connected module
   */
  public void newForwarderModule(IForwarderModule module);

  /**
   * Event that the forwarder module has been removed.
   * 
   * @param module
   *          The removed module
   */
  public void removedForwarderModule(IForwarderModule module);

  /**
   * Event that the forwarder module is disconnected.
   * 
   * @param module
   *          The disconnected module
   */
  public void disconnectedForwarderModule(IForwarderModule module);

  /**
   * Event that a forwarder module has been reconnected.
   * 
   * @param module
   *          The reconnected module
   */
  public void reconnectedForwarderModule(IForwarderModule module);

}

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
package de.fraunhofer.fokus.upnp.core_av.control_point;

import java.util.EventListener;

/**
 * This interface can be used to get informed about changes in a media server device.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public interface ICPMediaServerStateVariableListener extends EventListener
{

  /**
   * Event that the system update state variable has changed.
   * 
   * @param server
   *          The associated server
   */
  public void systemUpdateOccurred(MediaServerCPDevice server);

  /**
   * Event that a container update state variable has changed.
   * 
   * @param server
   *          The associated server
   * @param containerUpdateID
   *          The new container ID
   */
  public void containerUpdateOccurred(MediaServerCPDevice server, String containerUpdateID);

  /**
   * Event that a container was enumerated further.
   * 
   * @param server
   *          The associated server
   * @param containerID
   *          The ID of the enumerated container
   */
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID);

  /**
   * Event that a container enumeration is finished.
   * 
   * @param server
   *          The associated server
   * @param containerID
   *          The ID of the enumerated container
   */
  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID);
}

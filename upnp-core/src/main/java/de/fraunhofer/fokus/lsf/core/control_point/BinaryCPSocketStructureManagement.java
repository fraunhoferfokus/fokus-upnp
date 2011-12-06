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

import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.network.SocketStructureManagement;

/**
 * This class manages socket structures for physical interfaces in LSF.
 * 
 * @author Alexander Koenig
 */
public class BinaryCPSocketStructureManagement extends SocketStructureManagement
{
  private String multicastAddressString;

  private int    discoveryMulticastPort;

  private int    eventMulticastPort;

  /**
   * Creates a new instance of BinaryCPSocketStructureManagement.
   * 
   * @param preferredHostAddressList
   * @param ignoredHostAddressList
   */
  public BinaryCPSocketStructureManagement(Vector preferredHostAddressList, Vector ignoredHostAddressList)
  {
    this(preferredHostAddressList,
      ignoredHostAddressList,
      BinaryUPnPConstants.BinaryUPnPMulticastAddress,
      BinaryUPnPConstants.DiscoveryMulticastPort,
      BinaryUPnPConstants.EventMulticastPort);
  }

  /**
   * Creates a new instance of BinaryCPSocketStructureManagement using custom socket addresses.
   * 
   * @param preferredHostAddressList
   * @param ignoredHostAddressList
   */
  public BinaryCPSocketStructureManagement(Vector preferredHostAddressList,
    Vector ignoredHostAddressList,
    String multicastAddressString,
    int discoveryPort,
    int eventPort)
  {
    super(preferredHostAddressList, ignoredHostAddressList);
    this.multicastAddressString = multicastAddressString;
    this.discoveryMulticastPort = discoveryPort;
    this.eventMulticastPort = eventPort;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.SocketStructureManagement#createHostAddressSocketStructure(java.net.InetAddress)
   */
  protected AbstractHostAddressSocketStructure createHostAddressSocketStructure(InetAddress hostAddress)
  {
    BinaryCPHostAddressSocketStructure hostAddressSocketStructure =
      new BinaryCPHostAddressSocketStructure(hostAddress,
        multicastAddressString,
        discoveryMulticastPort,
        eventMulticastPort);
    if (hostAddressSocketStructure.isValid())
    {
      hostAddressSocketStructure.printUsedPorts();
      return hostAddressSocketStructure;
    }
    return null;
  }

}

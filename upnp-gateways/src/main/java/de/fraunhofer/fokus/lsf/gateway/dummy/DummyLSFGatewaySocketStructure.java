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
package de.fraunhofer.fokus.lsf.gateway.dummy;

import java.net.InetAddress;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.tunnel.common.dummy.DummyDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.dummy.DummyMulticastSocket;

/**
 * This class encapsulates all sockets for a dummy LSF forwarder module.
 * 
 * @author Alexander Koenig
 */
public class DummyLSFGatewaySocketStructure extends AbstractHostAddressSocketStructure
{

  /** Socket for announce and search */
  protected DummyMulticastSocket discoverySocket;

  /** Socket for unicast messages */
  protected DummyDatagramSocket  unicastSocket;

  /** Socket for events */
  protected DummyMulticastSocket eventSocket;

  /**
   * Creates a new instance of DummyLSFGatewaySocketStructure.
   * 
   * @param interfaceAddress
   */
  public DummyLSFGatewaySocketStructure(InetAddress interfaceAddress)
  {
    super(interfaceAddress);
    try
    {
      discoverySocket = new DummyMulticastSocket(interfaceAddress, BinaryUPnPConstants.DiscoveryMulticastPort);
      discoverySocket.setSoTimeout(10);

      unicastSocket = new DummyMulticastSocket(interfaceAddress, 0);
      unicastSocket.setSoTimeout(10);

      eventSocket = new DummyMulticastSocket(interfaceAddress, BinaryUPnPConstants.EventMulticastPort);
      eventSocket.setSoTimeout(10);
    } catch (Exception e)
    {
      valid = false;
      Portable.println("Could not start sockets on " + interfaceAddress.getHostAddress() + ": " + e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
    Portable.println("\r\n////////////////////////////////////////////////////////////");
    Portable.println("Dummy socket structure on host address    " + hostAddress.getHostAddress());
    Portable.println("");
    Portable.println("Discovery socket running on port          " + discoverySocket.getLocalPort());
    Portable.println("Unicast socket running on port            " + unicastSocket.getLocalPort());
    Portable.println("Eventing socket running on port           " + eventSocket.getLocalPort());
    Portable.println("////////////////////////////////////////////////////////////\r\n");
  }

  /**
   * Retrieves the value of discoverySocket.
   * 
   * @return The value of discoverySocket
   */
  public DummyMulticastSocket getDiscoverySocket()
  {
    return discoverySocket;
  }

  /**
   * Retrieves the value of unicastSocket.
   * 
   * @return The value of unicastSocket
   */
  public DummyDatagramSocket getUnicastSocket()
  {
    return unicastSocket;
  }

  /**
   * @return the eventSocket
   */
  public DummyMulticastSocket getEventSocket()
  {
    return eventSocket;
  }

  /** Terminates the socket structure */
  public void terminate()
  {
    try
    {
      discoverySocket.close();
      unicastSocket.close();
      eventSocket.close();
    } catch (Exception e)
    {
    }
  }

}

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
package de.fraunhofer.fokus.lsf.gateway.common.tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelMulticastSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocketFactory;

/**
 * This class is used to bundle all sockets that are needed for a LSF tunnel gateway.
 * 
 * @author Alexander Koenig
 */
public class TunnelLSFSocketStructure extends AbstractHostAddressSocketStructure
{
  public static Logger              logger = Logger.getLogger("lsf");

  /** Socket for announce and search */
  protected IPTunnelMulticastSocket discoverySocket;

  /** Socket for unicast messages */
  protected IPTunnelDatagramSocket  unicastSocket;

  /** Socket for events */
  protected IPTunnelMulticastSocket eventSocket;

  protected String                  multicastAddress;

  /**
   * Creates a new instance of TunnelLSFSocketStructure.
   * 
   * @param ipTunnelSocketFactory
   * @param address
   *          Address used in the tunnel
   * @param multicastAddress
   * @param discoveryMulticastPort
   * @param eventingMulticastPort
   */
  public TunnelLSFSocketStructure(IPTunnelSocketFactory ipTunnelSocketFactory,
    InetAddress address,
    String multicastAddress,
    int discoveryMulticastPort,
    int eventingMulticastPort)
  {
    super(address);
    this.multicastAddress = multicastAddress;
    try
    {
      discoverySocket = ipTunnelSocketFactory.createIPTunnelMulticastSocket(discoveryMulticastPort);
      discoverySocket.setSoTimeout(10);
      discoverySocket.joinGroup(InetAddress.getByName(multicastAddress));

      unicastSocket = ipTunnelSocketFactory.createIPTunnelDatagramSocket(0);
      unicastSocket.setSoTimeout(10);

      eventSocket = ipTunnelSocketFactory.createIPTunnelMulticastSocket(eventingMulticastPort);
      eventSocket.setSoTimeout(10);
      eventSocket.joinGroup(InetAddress.getByName(multicastAddress));
    } catch (Exception e)
    {
      valid = false;
      Portable.println("Could not start sockets on " + address.getHostAddress() + ": " + e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
    Portable.println("////////////////////////////////////////////////////////////");
    Portable.println("Socket structure for tunnel host address  " + hostAddress.getHostAddress());
    Portable.println("");
    Portable.println("Multicast address                         " + multicastAddress);
    Portable.println("Discovery socket running on port          " + discoverySocket.getLocalPort());
    Portable.println("Unicast socket running on port            " + unicastSocket.getLocalPort());
    Portable.println("Eventing socket running on port           " + eventSocket.getLocalPort());
    Portable.println("////////////////////////////////////////////////////////////\r\n");
  }

  public InetSocketAddress getDiscoveryMulticastSocketAddress()
  {
    return new InetSocketAddress(multicastAddress, discoverySocket.getLocalPort());
  }

  public InetSocketAddress getEventingMulticastSocketAddress()
  {
    return new InetSocketAddress(multicastAddress, eventSocket.getLocalPort());
  }

  /**
   * Retrieves the value of discoverySocket.
   * 
   * @return The value of discoverySocket
   */
  public IPTunnelMulticastSocket getDiscoverySocket()
  {
    return discoverySocket;
  }

  /**
   * Retrieves the value of unicastSocket.
   * 
   * @return The value of unicastSocket
   */
  public IPTunnelDatagramSocket getUnicastSocket()
  {
    return unicastSocket;
  }

  /**
   * @return the eventSocket
   */
  public IPTunnelMulticastSocket getEventSocket()
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

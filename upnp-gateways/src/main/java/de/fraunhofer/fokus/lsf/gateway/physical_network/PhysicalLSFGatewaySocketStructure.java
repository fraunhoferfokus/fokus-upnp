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
package de.fraunhofer.fokus.lsf.gateway.physical_network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class encapsulates all sockets for a LSF forwarder module which is bound to a physical network interface.
 * 
 * @author Alexander Koenig
 */
public class PhysicalLSFGatewaySocketStructure extends AbstractHostAddressSocketStructure
{

  /** Socket for announce and search */
  protected MulticastSocket discoverySocket;

  /** Socket for unicast messages */
  protected DatagramSocket  unicastSocket;

  /** Socket for events */
  protected MulticastSocket eventSocket;

  protected String          multicastAddress;

  /**
   * Creates a new instance of PhysicalLSFGatewaySocketStructure.
   * 
   * @param interfaceAddress
   * @param multicastAddress
   * @param discoveryMulticastPort
   * @param eventingMulticastPort
   */
  public PhysicalLSFGatewaySocketStructure(InetAddress interfaceAddress,
    String multicastAddress,
    int discoveryMulticastPort,
    int eventingMulticastPort)
  {
    super(interfaceAddress);
    this.multicastAddress = multicastAddress;
    try
    {
      discoverySocket = new MulticastSocket(discoveryMulticastPort);
      discoverySocket.setInterface(interfaceAddress);
      discoverySocket.setSoTimeout(10);
      discoverySocket.setTimeToLive(BinaryUPnPConstants.TTL);
      discoverySocket.joinGroup(InetAddress.getByName(multicastAddress));

      unicastSocket = new MulticastSocket(0);
      ((MulticastSocket)unicastSocket).setInterface(interfaceAddress);
      unicastSocket.setSoTimeout(10);
      ((MulticastSocket)unicastSocket).setTimeToLive(BinaryUPnPConstants.TTL);

      eventSocket = new MulticastSocket(eventingMulticastPort);
      eventSocket.setInterface(interfaceAddress);
      eventSocket.setSoTimeout(10);
      eventSocket.setTimeToLive(BinaryUPnPConstants.TTL);
      eventSocket.joinGroup(InetAddress.getByName(multicastAddress));
    } catch (Exception e)
    {
      valid = false;
      Portable.println("Could not start sockets on " + interfaceAddress.getHostAddress() + ": " + e.getMessage());
    }
    // check for JamVM
    try
    {
      InetAddress.getLocalHost();
    } catch (Exception e)
    {
      //      Portable.println("Localhost not available, probably JamVM. Increase socket timeout");
      SocketHelper.JAM_VM = true;
      //      SocketHelper.DEFAULT_SOCKET_TIMEOUT = 1000;
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
    Portable.println("////////////////////////////////////////////////////////////");
    Portable.println("Socket structure for host address         " + hostAddress.getHostAddress());
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
  public MulticastSocket getDiscoverySocket()
  {
    return discoverySocket;
  }

  /**
   * Retrieves the value of unicastSocket.
   * 
   * @return The value of unicastSocket
   */
  public DatagramSocket getUnicastSocket()
  {
    return unicastSocket;
  }

  /**
   * @return the eventSocket
   */
  public MulticastSocket getEventSocket()
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

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
package de.fraunhofer.fokus.lsf.core;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class encapsulates all sockets for one network interface which are needed by binary devices and control points.
 * 
 * @author Alexander Koenig
 */
public class BinaryHostAddressSocketStructure extends AbstractHostAddressSocketStructure
{

  /** Multicast address */
  protected InetAddress     multicastAddress;

  /** Socket for announce and search */
  protected MulticastSocket discoverySocket;

  /** Socket for description */
  protected DatagramSocket  descriptionSocket;

  /** Socket for events */
  protected MulticastSocket eventSocket;

  /** Socket for control */
  protected DatagramSocket  controlSocket;

  protected MulticastSocket debugSocket;

  /**
   * Creates a new instance of BinaryHostAddressSocketStructure.
   * 
   * @param interfaceAddress
   */
  public BinaryHostAddressSocketStructure(InetAddress interfaceAddress)
  {
    this(interfaceAddress,
      BinaryUPnPConstants.BinaryUPnPMulticastAddress,
      BinaryUPnPConstants.DiscoveryMulticastPort,
      BinaryUPnPConstants.EventMulticastPort);
  }

  /**
   * Creates a new instance of BinaryHostAddressSocketStructure.
   * 
   * @param interfaceAddress
   * @param multicastAddressString
   * @param discoveryPort
   * @param eventPort
   */
  public BinaryHostAddressSocketStructure(InetAddress interfaceAddress,
    String multicastAddressString,
    int discoveryPort,
    int eventPort)
  {
    super(interfaceAddress);
    try
    {
      this.multicastAddress = InetAddress.getByName(multicastAddressString);
      discoverySocket = new MulticastSocket(discoveryPort);
      discoverySocket.setInterface(interfaceAddress);
      discoverySocket.setSoTimeout(10);
      discoverySocket.setTimeToLive(BinaryUPnPConstants.TTL);
      discoverySocket.joinGroup(multicastAddress);

      descriptionSocket = new DatagramSocket(IPHelper.toSocketAddress(interfaceAddress.getHostAddress() + ":0"));
      descriptionSocket.setSoTimeout(10);

      controlSocket = new DatagramSocket(IPHelper.toSocketAddress(interfaceAddress.getHostAddress() + ":0"));
      controlSocket.setSoTimeout(10);

      eventSocket = new MulticastSocket(eventPort);
      eventSocket.setInterface(interfaceAddress);
      eventSocket.setSoTimeout(10);
      eventSocket.setTimeToLive(BinaryUPnPConstants.TTL);
      eventSocket.joinGroup(multicastAddress);

      debugSocket = new MulticastSocket(2400);
      debugSocket.setInterface(interfaceAddress);
      debugSocket.setSoTimeout(10);
      debugSocket.setTimeToLive(BinaryUPnPConstants.TTL);
      debugSocket.joinGroup(multicastAddress);
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
    Portable.println("Socket structure for host address         " + hostAddress.getHostAddress());
    Portable.println("");
    Portable.println("Multicast address                         " + multicastAddress.getHostAddress());
    Portable.println("Discovery socket running on port          " + discoverySocket.getLocalPort());
    Portable.println("Description socket running on port        " + descriptionSocket.getLocalPort());
    Portable.println("Control unicast socket running on port    " + controlSocket.getLocalPort());
    Portable.println("Eventing socket running on port           " + eventSocket.getLocalPort());
    Portable.println("////////////////////////////////////////////////////////////\r\n");
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
   * Retrieves the value of descriptionSocket.
   * 
   * @return The value of descriptionSocket
   */
  public DatagramSocket getDescriptionSocket()
  {
    return descriptionSocket;
  }

  /**
   * @return Returns the controlSocket.
   */
  public DatagramSocket getControlSocket()
  {
    return controlSocket;
  }

  /**
   * @return the eventSocket
   */
  public MulticastSocket getEventSocket()
  {
    return eventSocket;
  }

  /**
   * Retrieves the value of debugSocket.
   * 
   * @return The value of debugSocket
   */
  public MulticastSocket getDebugSocket()
  {
    return debugSocket;
  }

  /**
   * Retrieves the value of multicastAddress.
   * 
   * @return The value of multicastAddress
   */
  public InetAddress getMulticastAddress()
  {
    return multicastAddress;
  }

  /** Terminates the socket structure */
  public void terminate()
  {
    try
    {
      discoverySocket.close();
      descriptionSocket.close();
      controlSocket.close();
      eventSocket.close();
      debugSocket.close();
    } catch (Exception e)
    {
    }

  }

}

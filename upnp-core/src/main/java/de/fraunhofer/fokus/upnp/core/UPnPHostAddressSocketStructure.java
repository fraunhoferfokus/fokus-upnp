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
package de.fraunhofer.fokus.upnp.core;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;

/**
 * This class is used to bundle all sockets that are needed for one external interface for both UPnP devices and control
 * points.
 * 
 * Currently, this includes: A multicast socket for device discovery and a unicast socket for device discovery
 * 
 * @author Alexander Koenig
 * 
 */
public class UPnPHostAddressSocketStructure extends AbstractHostAddressSocketStructure
{

  public static Logger        logger = Logger.getLogger("upnp");

  /** Associated network interface */
  protected NetworkInterface  networkInterface;

  /** Socket used for multicast SSDP messages */
  protected MulticastSocket   ssdpMulticastSocket;

  /** Socket used for unicast SSDP messages */
  protected MulticastSocket   ssdpUnicastSocket;

  /** Flag that fixed ports should be used */
  protected boolean           fixedPorts;

  /** Port used for SSDP unicast */
  protected int               ssdpUnicastPort;

  /** Inet address for multicast */
  protected InetSocketAddress ssdpMulticastSocketAddress;

  /**
   * Creates a new instance of UPnPHostAddressSocketStructure with fixed ports.
   * 
   * @param networkInterface
   *          The associated network interface
   * @param address
   *          The address this structure should be bound to
   * @param ssdpMulticastSocketAddress
   *          The address used for SSDP messages
   * @param ssdpUnicastPort
   *          The port used for SSDP unicast
   * @param IPVersion
   *          The IP version
   */
  public UPnPHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress address,
    InetSocketAddress ssdpMulticastSocketAddress,
    int ssdpUnicastPort,
    int IPVersion)
  {
    super(address);
    this.networkInterface = networkInterface;
    this.hostAddress = address;
    this.ssdpUnicastPort = ssdpUnicastPort;
    this.ssdpMulticastSocketAddress = ssdpMulticastSocketAddress;
    try
    {
      // create SSDP multicast socket
      ssdpMulticastSocket = new MulticastSocket(ssdpMulticastSocketAddress.getPort());
      ssdpMulticastSocket.setInterface(address);
      if (IPVersion == SSDPConstant.IP6)
      { // IP version 6
        ssdpMulticastSocket.joinGroup(InetAddress.getByName(SSDPConstant.SSDPMulticastAddressV6));
      }
      if (IPVersion == SSDPConstant.IP4)
      { // IP version 4
        ssdpMulticastSocket.joinGroup(ssdpMulticastSocketAddress.getAddress());
      }
      // set timeout to simplify message processing for multiple sockets
      ssdpMulticastSocket.setSoTimeout(10);
      ssdpMulticastSocket.setTimeToLive(SSDPConstant.TTL);

      // must be a multicast socket to allow forwarding of multicast messages to other network segments
      ssdpUnicastSocket = new MulticastSocket(new InetSocketAddress(address, ssdpUnicastPort));
      ssdpUnicastSocket.setSoTimeout(10);
      ssdpUnicastSocket.setTimeToLive(SSDPConstant.TTL);
      ssdpUnicastPort = ssdpUnicastSocket.getLocalPort();
    } catch (Exception e)
    {
      System.out.println("Cannot start UPnPHostAddressSocketStructure for address " + address.getHostAddress() + ": " +
        e.getMessage());
      logger.fatal("cannot start UPnPHostAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      valid = false;
    }
  }

  /**
   * Creates a new instance of UPnPHostAddressSocketStructure with random ports.
   * 
   * @param networkInterface
   *          The associated network interface
   * @param address
   *          The address this structure should be bound to
   * @param ssdpMulticastAddress
   *          The address used for SSDP messages
   * @param IPVersion
   *          The IP version
   */
  public UPnPHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress address,
    InetSocketAddress ssdpMulticastSocketAddress,
    int IPVersion)
  {
    this(networkInterface, address, ssdpMulticastSocketAddress, 0, IPVersion);
  }

  /** Retrieves the socket that receives M-SEARCH and NOTIFY messages. */
  public DatagramSocket getSSDPMulticastSocket()
  {
    return ssdpMulticastSocket;
  }

  /** Retrieves the socket that sends M-SEARCH messages and receives M-SEARCH response messages. */
  public DatagramSocket getSSDPUnicastSocket()
  {
    return ssdpUnicastSocket;
  }

  /** Retrieves the network interface associated with this socket structure */
  public NetworkInterface getNetworkInterface()
  {
    return networkInterface;
  }

  /**
   * Retrieves the ssdpMulticastAddress.
   * 
   * @return The ssdpMulticastAddress.
   */
  public InetSocketAddress getSSDPMulticastSocketAddress()
  {
    return ssdpMulticastSocketAddress;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.InterfaceSocketStructure#terminate()
   */
  public void terminate()
  {
    try
    {
      ssdpMulticastSocket.close();
      ssdpMulticastSocket = null;
    } catch (Exception ex)
    {
    }
    try
    {
      ssdpUnicastSocket.close();
      ssdpUnicastSocket = null;
    } catch (Exception ex)
    {
    }
  }

}

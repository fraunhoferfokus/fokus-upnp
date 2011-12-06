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
package de.fraunhofer.fokus.upnp.gateway.network_interfaces;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.NetworkInterfaceManager;

/**
 * This class is used to generate IForwarderModules for all found network interfaces.
 * 
 * @author Alexander Koenig
 */
public class InetAddressManagement implements INetworkInterfaceChangeListener
{

  /** Reference to message forwarder */
  private MessageForwarder                messageForwarder;

  /** Manager for recognizing network change events */
  private NetworkInterfaceManager         networkInterfaceManager;

  /** Table with all created forwarder modules */
  private Hashtable                       forwarderModuleFromHostAddressTable = new Hashtable();

  /** SSDP socket address */
  private InetSocketAddress               ssdpMulticastSocketAddress;

  /** Optional listener for network interface events */
  private INetworkInterfaceChangeListener networkInterfaceChangeListener;

  /**
   * Creates a new instance of InetAddressManagement and adds all local network interfaces to the
   * message forwarder.
   * 
   * @param messageForwarder
   */
  public InetAddressManagement(MessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
    this.ssdpMulticastSocketAddress = messageForwarder.getStartupConfiguration().getSSDPMulticastSocketAddress();
    initSocketStructures();
    networkInterfaceManager = new NetworkInterfaceManager();
    networkInterfaceManager.addListener(this);
    // allow regular calling by event thread
    messageForwarder.getEventThread().register(networkInterfaceManager);
  }

  /** Initializes sockets and servers for all network interfaces. */
  private void initSocketStructures()
  {
    // start independent sockets and servers for all external network addresses
    Vector networkInterfaces = IPHelper.getSocketStructureNetworkInterfaces();
    for (int i = 0; i < networkInterfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
      createSocketStructuresForNetworkInterface(currentInterface);
    }
  }

  /** Creates all socket structures for one network interface */
  private void createSocketStructuresForNetworkInterface(NetworkInterface networkInterface)
  {
    Vector localHostAddresses = IPHelper.getIPv4InetAddresses(networkInterface);
    for (int j = 0; j < localHostAddresses.size(); j++)
    {
      InetAddress currentAddress = (InetAddress)localHostAddresses.elementAt(j);
      if (!currentAddress.getHostAddress().equals("127.0.0.1"))
      {
        createSocketStructuresForInetAddress(networkInterface, currentAddress);
      }
    }
  }

  /** Creates a socket structures for one inet address */
  private void createSocketStructuresForInetAddress(NetworkInterface networkInterface, InetAddress address)
  {
    // System.out.println();
    // System.out.println("Create InetAddressGatewaySocketStructure for local host address " +
    // address.getHostAddress());

    InetAddressGatewaySocketStructure socketStructure;
    int gatewayServerPort =
      messageForwarder.getGatewayStartupConfiguration("InetAddressManagement").getHTTPServerPort();
    int gatewayUDPServerPort =
      messageForwarder.getGatewayStartupConfiguration("InetAddressManagement").getHTTPOverUDPServerPort();

    socketStructure =
      new InetAddressGatewaySocketStructure(messageForwarder.getGatewayMessageManager(),
        networkInterface,
        address,
        ssdpMulticastSocketAddress,
        gatewayServerPort,
        gatewayUDPServerPort);

    // create new forwarder module for each inet address
    InetAddressForwarderModule networkInterfaceModule =
      new InetAddressForwarderModule(messageForwarder, ssdpMulticastSocketAddress, socketStructure);

    System.out.println("    " + networkInterfaceModule.getModuleID() + ": " + "Started HTTP gateway server on port " +
      socketStructure.getHTTPServerSocketAddress().getPort());

    // store in local hash table
    forwarderModuleFromHostAddressTable.put(address, networkInterfaceModule);

    // register in message forwarder
    messageForwarder.addForwarderModule(networkInterfaceModule);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#newInetAddress(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    // System.out.println("Add forwarder modules for " + inetAddress);
    createSocketStructuresForInetAddress(networkInterface, inetAddress);

    if (networkInterfaceChangeListener != null)
    {
      networkInterfaceChangeListener.newInetAddress(networkInterface, inetAddress);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#inetAddressGone(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    // System.out.println("Removed forwarder module for " + inetAddress);
    // remove and terminate all associated network interfaces
    Enumeration forwarderModules = forwarderModuleFromHostAddressTable.elements();
    while (forwarderModules.hasMoreElements())
    {
      InetAddressForwarderModule currentModule = (InetAddressForwarderModule)forwarderModules.nextElement();

      if (currentModule.getSocketStructure().getNetworkInterface().getName().equals(networkInterface.getName()) &&
        currentModule.getSocketStructure().getHostAddress().equals(inetAddress))
      {
        currentModule.terminate();
        // remove from message forwarder
        messageForwarder.removeForwarderModule(currentModule);
        // remove from hashtable
        forwarderModuleFromHostAddressTable.remove(currentModule.getSocketStructure().getHostAddress());
      }
    }
    if (networkInterfaceChangeListener != null)
    {
      networkInterfaceChangeListener.inetAddressGone(networkInterface, inetAddress);
    }
  }

  /**
   * Retrieves the networkInterfaceChangeListener.
   * 
   * @return The networkInterfaceChangeListener
   */
  public INetworkInterfaceChangeListener getNetworkInterfaceChangeListener()
  {
    return networkInterfaceChangeListener;
  }

  /**
   * Sets the networkInterfaceChangeListener.
   * 
   * @param networkInterfaceChangeListener
   *          The new value for networkInterfaceChangeListener
   */
  public void setNetworkInterfaceChangeListener(INetworkInterfaceChangeListener networkInterfaceChangeListener)
  {
    this.networkInterfaceChangeListener = networkInterfaceChangeListener;
  }

  /** Terminates the management for multiple interfaces. */
  public void terminate()
  {
    messageForwarder.getEventThread().unregister(networkInterfaceManager);

    // terminate all forwarder modules
    Enumeration forwarderModules =
      CollectionHelper.getPersistentElementsEnumeration(forwarderModuleFromHostAddressTable);
    while (forwarderModules.hasMoreElements())
    {
      InetAddressForwarderModule currentModule = (InetAddressForwarderModule)forwarderModules.nextElement();

      currentModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(currentModule);
    }
  }

}

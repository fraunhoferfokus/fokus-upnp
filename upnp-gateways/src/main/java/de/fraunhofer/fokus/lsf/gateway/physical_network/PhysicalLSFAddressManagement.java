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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.startup.LSFChildStartupConfiguration;
import de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.NetworkInterfaceManager;

/**
 * This class is used to generate LSFForwarderModules for all found network interfaces.
 * 
 * @author Alexander Koenig
 */
public class PhysicalLSFAddressManagement extends AbstractLSFAddressManagement implements
  INetworkInterfaceChangeListener
{

  /** Manager for recognizing network interface change events */
  private NetworkInterfaceManager           networkInterfaceManager;

  /** Optional listener for network interface events */
  protected INetworkInterfaceChangeListener networkInterfaceChangeListener;

  /** Table with all created forwarder modules */
  private Hashtable                         forwarderModuleFromHostAddressTable = new Hashtable();

  private LSFChildStartupConfiguration      gatewayStartupConfiguration         = null;

  /**
   * Creates a new instance of PhysicalLSFAddressManagement and adds all local network interfaces to the message
   * forwarder.
   * 
   * @param messageForwarder
   */
  public PhysicalLSFAddressManagement(LSFMessageForwarder messageForwarder)
  {
    super(messageForwarder);

    // check if network properties should be updated for this gateway entity
    gatewayStartupConfiguration =
      messageForwarder.getGatewayStartupConfiguration(StringHelper.getShortClassName(getClass().getName()));
    if (gatewayStartupConfiguration != null && gatewayStartupConfiguration.hasCustomNetworkProperties())
    {
      multicastAddress = gatewayStartupConfiguration.getMulticastAddress();
      discoveryPort = gatewayStartupConfiguration.getDiscoveryMulticastPort();
      eventPort = gatewayStartupConfiguration.getEventMulticastPort();
    }
    initSocketStructures();
    networkInterfaceManager = new NetworkInterfaceManager();
    networkInterfaceManager.addListener(this);
    // allow regular calling by event thread
    messageForwarder.getEventThread().register(networkInterfaceManager);
  }

  /** Initializes all socket structures. */
  protected void initSocketStructures()
  {
    // check if preferred socket structures have been given
    if (gatewayStartupConfiguration != null && gatewayStartupConfiguration.getPreferredIPAddressesList().size() > 0)
    {
      Portable.println("Use list with preferred IP addresses");
      for (int j = 0; j < gatewayStartupConfiguration.getPreferredIPAddressesList().size(); j++)
      {
        try
        {
          InetAddress currentAddress =
            InetAddress.getByName((String)gatewayStartupConfiguration.getPreferredIPAddressesList().elementAt(j));
          if (!currentAddress.getHostAddress().equals("127.0.0.1"))
          {
            createSocketStructuresForInetAddress(currentAddress);
          }
        } catch (Exception e)
        {
        }
      }
    } else
    {
      // start independent sockets and servers for all external network addresses
      Vector networkInterfaces = IPHelper.getSocketStructureNetworkInterfaces();
      for (int i = 0; i < networkInterfaces.size(); i++)
      {
        NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
        createSocketStructuresForNetworkInterface(currentInterface);
      }
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
        createSocketStructuresForInetAddress(currentAddress);
      }
    }
  }

  /** Creates a socket structures for one inet address */
  private void createSocketStructuresForInetAddress(InetAddress address)
  {
    // check if address is in ignored list
    if (gatewayStartupConfiguration != null &&
      gatewayStartupConfiguration.getIgnoredIPAddressesList().contains(address.getHostAddress()))
    {
      Portable.println("Ignore " + address.getHostAddress() + " cause it is in ignore list");
      return;
    }
    // create socket structure
    PhysicalLSFGatewaySocketStructure socketStructure =
      new PhysicalLSFGatewaySocketStructure(address, multicastAddress, discoveryPort, eventPort);

    // create forwarder module for socket structure
    PhysicalLSFForwarderModule forwarderModule = new PhysicalLSFForwarderModule(messageForwarder, socketStructure);

    Portable.println("ModuleID:" + forwarderModule.getModuleID() + " on physical network interface");
    socketStructure.printUsedPorts();

    // store in local hash table
    forwarderModuleFromHostAddressTable.put(address, forwarderModule);

    // register in message forwarder
    messageForwarder.addForwarderModule(forwarderModule);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener#newInetAddress(java.net.NetworkInterface, java.net.InetAddress)
   */
  public void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    // System.out.println("Add forwarder modules for " + inetAddress);
    createSocketStructuresForInetAddress(inetAddress);

    if (networkInterfaceChangeListener != null)
    {
      networkInterfaceChangeListener.newInetAddress(networkInterface, inetAddress);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener#inetAddressGone(java.net.NetworkInterface, java.net.InetAddress)
   */
  public void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    // System.out.println("Removed forwarder module for " + inetAddress);
    // remove and terminate all associated network interfaces
    Enumeration forwarderModules = forwarderModuleFromHostAddressTable.elements();
    while (forwarderModules.hasMoreElements())
    {
      PhysicalLSFForwarderModule currentModule = (PhysicalLSFForwarderModule)forwarderModules.nextElement();

      if (currentModule.getSocketStructure().getHostAddress().equals(inetAddress))
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

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#printDebugStats()
   */
  public void printDebugStats()
  {
    Enumeration forwarderModules = forwarderModuleFromHostAddressTable.elements();
    while (forwarderModules.hasMoreElements())
    {
      PhysicalLSFForwarderModule currentModule = (PhysicalLSFForwarderModule)forwarderModules.nextElement();

      Portable.println("  " + currentModule.getModuleID() + ": Running on " +
        currentModule.getSocketStructure().getHostAddress().getHostAddress());
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
      PhysicalLSFForwarderModule currentModule = (PhysicalLSFForwarderModule)forwarderModules.nextElement();

      currentModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(currentModule);
    }
  }

}

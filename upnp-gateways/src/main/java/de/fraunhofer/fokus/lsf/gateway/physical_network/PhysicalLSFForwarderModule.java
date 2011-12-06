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
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class can be used to forward LSF messages over a certain physical network interface.
 * 
 * @author Alexander Koenig
 * 
 */
public class PhysicalLSFForwarderModule extends AbstractLSFForwarderModule
{

  /** Reference to associated socket structure */
  private PhysicalLSFGatewaySocketStructure socketStructure;

  /**
   * Creates a new instance of PhysicalLSFForwarderModule.
   * 
   * @param messageForwarder
   * @param gatewaySocketStructure
   */
  public PhysicalLSFForwarderModule(LSFMessageForwarder messageForwarder,
    PhysicalLSFGatewaySocketStructure gatewaySocketStructure)
  {
    super(messageForwarder);
    this.socketStructure = gatewaySocketStructure;

    synchronized(messageForwarder.getLock())
    {
      this.moduleID = (byte)messageForwarder.getForwarderModuleNextID();
      messageForwarder.setForwarderModuleNextID(messageForwarder.getForwarderModuleNextID() + 1);
    }
    moduleHostAddress = gatewaySocketStructure.getHostAddress();
    this.discoveryGroupSocketAddress = socketStructure.getDiscoveryMulticastSocketAddress();
    this.eventGroupSocketAddress = socketStructure.getEventingMulticastSocketAddress();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getDiscoverySocket()
   */
  public IDatagramSocket getDiscoverySocket()
  {
    return new DatagramSocketWrapper(socketStructure.getDiscoverySocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocket()
   */
  public IDatagramSocket getUnicastSocket()
  {
    return new DatagramSocketWrapper(socketStructure.getUnicastSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getEventSocket()
   */
  public IDatagramSocket getEventSocket()
  {
    return new DatagramSocketWrapper(socketStructure.getEventSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocketAddress()
   */
  public InetSocketAddress getUnicastSocketAddress()
  {
    return new InetSocketAddress(socketStructure.getHostAddress(), socketStructure.getUnicastSocket().getLocalPort());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isHostAddressForwarderModule()
   */
  public boolean isLocalHostAddressForwarderModule()
  {
    try
    {
      return IPHelper.getLocalHostAddress().equals(socketStructure.getHostAddress());
    } catch (Exception e)
    {
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isNetworkInterfaceForwarderModule()
   */
  public boolean isNetworkInterfaceForwarderModule()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isSubnetForwarderModule()
   */
  public boolean isSubnetForwarderModule(InetAddress address)
  {
    return IPHelper.isCommonSubnet(address, socketStructure.getHostAddress());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule#announceDeviceToForwarderModule(de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice)
   */
  public boolean canForwardAnnouncementMessage(AbstractBinaryDevice device)
  {
    InetSocketAddress deviceDescriptionSocketAddress = device.getDeviceDescriptionSocketAddress();

    // check if device is accessed over the gateway port of this forwarder module
    // this indicates that the device is not located in the local network
    if (deviceDescriptionSocketAddress.equals(getUnicastSocketAddress()))
    {
      System.out.println("Device " + device.toString() + " is announced in " + toString() +
        " because it is located in another network");

      return true;
    }

    // if this forwarder module and the device description socket address use the same subnet
    // (e.g., in a parallel LAN/WLAN network), the device will announce itself
    if (IPHelper.isCommonSubnet(deviceDescriptionSocketAddress.getAddress(), socketStructure.getHostAddress()))
    {
      System.out.println("Device " + device.toString() + " is not announced in " + toString() + " due to common subnet");
      return false;
    }
    // if the device runs on the local host address, it will probably announce itself on all network
    // interfaces
    if (IPHelper.isLocalHostAddressString(deviceDescriptionSocketAddress.getAddress().getHostAddress()))
    {
      System.out.println("Device " + device.toString() + " is not announced in " + toString() +
        " because it runs on the same host as the forwarder");
      return false;
    }

    System.out.println("Device " + device.toString() + " is announced in " + toString());
    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastPort()
   */
  public int getUnicastPort()
  {
    return socketStructure.getUnicastSocket().getLocalPort();
  }

  /**
   * Retrieves the value of socketStructure.
   * 
   * @return The value of socketStructure
   */
  public PhysicalLSFGatewaySocketStructure getSocketStructure()
  {
    return socketStructure;
  }

  public String toString()
  {
    return "Physical:" + super.toString();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getPhyType()
   */
  public byte getPhyType()
  {
    return BinaryUPnPConstants.PhyType802_3;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    super.terminate();
    socketStructure.terminate();
  }

}

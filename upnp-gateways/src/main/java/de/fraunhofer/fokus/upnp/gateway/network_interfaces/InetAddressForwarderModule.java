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

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPMSearchHelper;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.AbstractForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class can be used to forward messages over an inet address bound to a certain network
 * interface.
 * 
 * @author Alexander Koenig
 * 
 */
public class InetAddressForwarderModule extends AbstractForwarderModule
{

  /** Reference to associated socket structure */
  private InetAddressGatewaySocketStructure gatewaySocketStructure;

  /**
   * @param messageForwarder
   * @param gatewaySocketStructure
   */
  public InetAddressForwarderModule(MessageForwarder messageForwarder,
    InetSocketAddress ssdpMulticastSocketAddress,
    InetAddressGatewaySocketStructure gatewaySocketStructure)
  {
    super(messageForwarder);
    this.gatewaySocketStructure = gatewaySocketStructure;
    this.moduleID =
      "Interface_" + gatewaySocketStructure.getHostAddress().getHostAddress() + "_" +
        gatewaySocketStructure.getSSDPMulticastSocket().getLocalPort();
    this.ssdpMulticastSocketAddress = ssdpMulticastSocketAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedMSearchMessage()
   */
  public void receivedMSearchMessage(HTTPMessageObject message)
  {
    // M-SEARCH messages received by local network interfaces can be forwarded by other
    // forwarder modules
    messageForwarder.getSSDPManagement().forwardMSearchMessage(moduleID, message, message.getSourceAddress());

    // process M-SEARCH message internally
    super.receivedMSearchMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardMSearchResponseMessage(
   *      java.lang.String, de.fhg.fokus.magic.http.HTTPMessageObject, java.net.InetSocketAddress)
   */
  public void forwardMSearchResponseMessage(HTTPMessageObject message,
    String forwarderModuleID,
    InetSocketAddress replyAddress)
  {
    // change M-SEARCH response message to allow proper forwarding of resulting GET requests
    String modifiedResponseMessage =
      SSDPMSearchHelper.extendMSearchResponseMessage(message.getHeader(),
        forwarderModuleID,
        gatewaySocketStructure.getHTTPServerSocketAddress());

    // send response to requesting control point
    SSDPMSearchHelper.sendMSearchResponse(modifiedResponseMessage, getSSDPSocket(), replyAddress);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getSSDPSocket()
   */
  public IDatagramSocket getSSDPSocket()
  {
    return new DatagramSocketWrapper(gatewaySocketStructure.getSSDPMulticastSocket());
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
      return IPHelper.getLocalHostAddress().equals(gatewaySocketStructure.getHostAddress());
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
    // assume subnet mask 255.255.255.0
    byte[] hostAddressData = gatewaySocketStructure.getHostAddress().getAddress();
    byte[] addressData = address.getAddress();

    return hostAddressData[0] == addressData[0] && hostAddressData[1] == addressData[1] &&
      hostAddressData[2] == addressData[2];
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isVisibleDevice()
   */
  public boolean announceDeviceToForwarderModule(AbstractDevice device)
  {
    InetSocketAddress deviceDescriptionSocketAddress = device.getDeviceDescriptionSocketAddress();

    // check if device is accessed over the gateway port of this forwarder module
    // this indicates that the device is not located in the local network
    if (deviceDescriptionSocketAddress.equals(getHTTPServerSocketAddress()))
    {
      System.out.println("Device " + device.toDiscoveryString() + " is announced in " + toString() +
        " because it is located in another network");

      return true;
    }

    // if this forwarder module and the device description socket address use the same subnet
    // (e.g., in a parallel LAN/WLAN network), the device will announce itself
    if (IPHelper.isCommonSubnet(deviceDescriptionSocketAddress.getAddress(), getHTTPServerSocketAddress().getAddress()))
    {
      System.out.println("Device " + device.toDiscoveryString() + " is not announced in " + toString() +
        " due to common subnet");
      return false;
    }
    // if the device runs on the local host address, it will probably announce itself on all network
    // interfaces
    if (IPHelper.isLocalHostAddressString(deviceDescriptionSocketAddress.getAddress().getHostAddress()))
    {
      System.out.println("Device " + device.toDiscoveryString() + " is not announced in " + toString() +
        " because it runs on the same host as the forwarder");
      return false;
    }

    System.out.println("Device " + device.toDiscoveryString() + " is announced in " + toString());
    return true;
  }

  /**
   * Retrieves the socket structure associated with this forwarder module.
   * 
   * @return The associated socket structure
   */
  public InetAddressGatewaySocketStructure getSocketStructure()
  {
    return gatewaySocketStructure;
  }

  /**
   * Retrieves the network interface for this forwarder module.
   * 
   * @return The associated network interface
   */
  public NetworkInterface getNetworkInterface()
  {
    return gatewaySocketStructure.getNetworkInterface();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPServerSocketAddress()
   */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return gatewaySocketStructure.getHTTPServerSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPServerSocketAddress()
   */
  public InetSocketAddress getHTTPOverUDPServerSocketAddress()
  {
    return gatewaySocketStructure.getHTTPOverUDPServerSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPSocket()
   */
  public IDatagramSocket getHTTPOverUDPSocket()
  {
    return gatewaySocketStructure.getHTTPOverUDPSocket();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPClient()
   */
  public IHTTPClient getHTTPClient()
  {
    return new HTTPClient();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPClient()
   */
  public IHTTPOverUDPClient getHTTPOverUDPClient()
  {
    return new HTTPOverUDPClient();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    super.terminate();
    gatewaySocketStructure.terminate();
  }

}

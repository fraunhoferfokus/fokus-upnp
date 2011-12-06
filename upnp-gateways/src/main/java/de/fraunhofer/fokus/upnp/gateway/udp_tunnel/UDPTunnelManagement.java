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
package de.fraunhofer.fokus.upnp.gateway.udp_tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.gateway.common.TemplateTunnelManagement;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.UDPTunnelNetworkInterface;

/**
 * This class is used to tunnel UPnP over a NOW connection.
 * 
 * @author Alexander Koenig
 */
public class UDPTunnelManagement extends TemplateTunnelManagement
{

  public static int                UDP_TUNNEL_PORT          = 2001;

  /** Forwarder module for UDP tunnel */
  private UDPTunnelForwarderModule udpTunnelForwarderModule = null;

  /**
   * Creates a new instance of UDPTunnelManagement.java
   * 
   * @param messageForwarder
   * @param peerSocketAddress
   * @param ipTunnelInetAddress
   */
  public UDPTunnelManagement(MessageForwarder messageForwarder,
    InetSocketAddress peerSocketAddress,
    InetAddress ipTunnelInetAddress)
  {
    super(messageForwarder, ipTunnelInetAddress);

    // create virtual network interface
    UDPTunnelNetworkInterface udpTunnelNetworkInterface =
      new UDPTunnelNetworkInterface(UDP_TUNNEL_PORT, peerSocketAddress);

    udpTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);

    // set optional network interface parameters
    udpTunnelNetworkInterface.setMaximumSegmentSize(1400);
    udpTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
    udpTunnelNetworkInterface.setPacketGapTime(2);

    // create UPnP socket structure for UDP tunnel
    System.out.println("Create UDPTunnelSocketStructure for address " + ipTunnelInetAddress.getHostAddress());

    UPnPTunnelSocketStructure ipTunnelSocketStructure =
      new UPnPTunnelSocketStructure(udpTunnelNetworkInterface.getIPTunnelSocketFactory(),
        messageForwarder.getGatewayMessageManager(),
        ipTunnelInetAddress);

    // create forwarder module
    udpTunnelForwarderModule =
      new UDPTunnelForwarderModule(messageForwarder,
        udpTunnelNetworkInterface,
        ipTunnelSocketStructure,
        StringHelper.byteArrayToBase32(id));

    // add forwarder module to central message forwarder
    messageForwarder.addForwarderModule(udpTunnelForwarderModule);

    // initialize tunnel
    udpTunnelForwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(id);
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.newForwarderModule(udpTunnelForwarderModule);
    }
  }

  /**
   * Retrieves the value of udpTunnelForwarderModule.
   * 
   * @return The value of udpTunnelForwarderModule
   */
  public UDPTunnelForwarderModule getUDPTunnelForwarderModule()
  {
    return udpTunnelForwarderModule;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.TemplateTunnelManagement#terminate()
   */
  public void terminate()
  {
    if (udpTunnelForwarderModule != null)
    {
      // terminate forwarder module
      udpTunnelForwarderModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(udpTunnelForwarderModule);
      // forward event
      if (forwarderModuleEventListener != null)
      {
        forwarderModuleEventListener.removedForwarderModule(udpTunnelForwarderModule);
      }
      udpTunnelForwarderModule = null;
    }
    super.terminate();

    System.out.println("  Terminated NOWTunnelManagement.");
  }

}

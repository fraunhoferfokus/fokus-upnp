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
package de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.IPTunnelHTTPConstants;
import de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.IPTunnelHTTPServer;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.GatewayHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelMulticastSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocketFactory;

/**
 * This class is used to bundle all sockets that are needed for an UPnP tunnel gateway.
 * 
 * @author Alexander Koenig
 */
public class UPnPTunnelSocketStructure extends AbstractHostAddressSocketStructure
{
  public static Logger            logger                         = Logger.getLogger("upnp");

  /** Associated server address */
  private InetSocketAddress       httpServerSocketAddress;

  /** Server that receives HTTP messages from the tunnel */
  private IPTunnelHTTPServer      httpFromTunnelServer;

  /** Socket that receives NOTIFY and M-SEARCH messages over the tunnel */
  private IPTunnelMulticastSocket ssdpMulticastSocket;

  /** Socket that receives UDP events */
  private IPTunnelDatagramSocket  httpOverUDPSocket;

  /** Server address of HTTP over UDP socket */
  private InetSocketAddress       httpOverUDPServerSocketAddress = null;

  /**
   * Creates a new instance of UPnPTunnelSocketStructure.
   * 
   * @param ipTunnelSocketFactory
   * @param gatewayHTTPMessageProcessor
   * @param address
   */
  public UPnPTunnelSocketStructure(IPTunnelSocketFactory ipTunnelSocketFactory,
    GatewayHTTPMessageProcessor gatewayHTTPMessageProcessor,
    InetAddress address)
  {
    super(address);
    try
    {
      // Server from tunnel should have a fixed port
      httpFromTunnelServer =
        new IPTunnelHTTPServer(ipTunnelSocketFactory,
          IPTunnelHTTPConstants.CLIENT_HTTP_SERVER_PORT,
          gatewayHTTPMessageProcessor);
      httpServerSocketAddress = new InetSocketAddress(address, httpFromTunnelServer.getPort());

      ssdpMulticastSocket = ipTunnelSocketFactory.createIPTunnelMulticastSocket(SSDPConstant.SSDPMulticastPort);
      ssdpMulticastSocket.setSoTimeout(20);
      ssdpMulticastSocket.joinGroup(SSDPConstant.getSSDPMulticastAddress());

      System.out.println("  Started HTTP IP tunnel server on port " + httpFromTunnelServer.getPort());

      httpOverUDPSocket =
        ipTunnelSocketFactory.createIPTunnelDatagramSocket(IPTunnelHTTPConstants.CLIENT_HTTP_OVER_UDP_SERVER_PORT);
      httpOverUDPSocket.setSoTimeout(20);
      httpOverUDPServerSocketAddress = new InetSocketAddress(address, httpOverUDPSocket.getLocalPort());

    } catch (Exception e)
    {
      System.out.println("Cannot start TCPTunnelSocketStructure for address " + address.getHostAddress());
      logger.fatal("cannot start TCPTunnelSocketStructure");
      logger.fatal("reason: " + e.getMessage());
    }
  }

  /**
   * Retrieves the device HTTP server that listens on the interface associated with this socket structure
   */
  public IPTunnelHTTPServer getHTTPServer()
  {
    return httpFromTunnelServer;
  }

  /** Retrieves the socket that listens for multicast messages */
  public IPTunnelDatagramSocket getSSDPFromIPTunnelSocket()
  {
    return ssdpMulticastSocket;
  }

  /** Retrieves the socket address of the tunnel HTTP server */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return httpServerSocketAddress;
  }

  /**
   * Retrieves the httpOverUDPSocket.
   * 
   * @return The httpOverUDPSocket
   */
  public IPTunnelDatagramSocket getHTTPOverUDPSocket()
  {
    return httpOverUDPSocket;
  }

  /** Retrieves the socket address of the HTTP over UDP server */
  public InetSocketAddress getHTTPOverUDPServerSocketAddress()
  {
    return httpOverUDPServerSocketAddress;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure#terminate()
   */
  public void terminate()
  {
    httpFromTunnelServer.terminate();
    ssdpMulticastSocket.close();
    httpOverUDPSocket.close();
  }

}

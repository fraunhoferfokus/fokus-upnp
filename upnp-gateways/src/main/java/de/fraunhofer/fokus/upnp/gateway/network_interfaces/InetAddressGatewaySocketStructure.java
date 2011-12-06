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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import de.fraunhofer.fokus.upnp.core.UPnPHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.GatewayHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPServer;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is used to bundle all sockets that are needed for a gateway on each network interface.
 * 
 * @author Alexander Koenig
 */
public class InetAddressGatewaySocketStructure extends UPnPHostAddressSocketStructure
{

  /** Associated server address */
  private InetSocketAddress httpServerSocketAddress        = null;

  /** Server that receives UPnP messages for forwarding */
  private HTTPServer        httpServer;

  /** Socket that receives UDP events */
  private DatagramSocket    httpOverUDPSocket;

  /** Wrapper for socket that receives UDP events */
  private IDatagramSocket   httpOverUDPSocketWrapper;

  /** Server address of HTTP over UDP socket */
  private InetSocketAddress httpOverUDPServerSocketAddress = null;

  /**
   * Creates a new instance of InetAddressGatewaySocketStructure.
   * 
   * @param gatewayHTTPMessageProcessor
   * @param networkInterface
   * @param address
   * @param ssdpMulticastSocketAddress
   * @param gatewayHTTPPort
   * @param gatewayHTTPOverUDPPort
   */
  public InetAddressGatewaySocketStructure(GatewayHTTPMessageProcessor gatewayHTTPMessageProcessor,
    NetworkInterface networkInterface,
    InetAddress address,
    InetSocketAddress ssdpMulticastSocketAddress,
    int gatewayHTTPPort,
    int gatewayHTTPOverUDPPort)
  {
    super(networkInterface, address, ssdpMulticastSocketAddress, gatewayHTTPMessageProcessor.getIPVersion());

    boolean fixedHTTPPort = true;
    if (gatewayHTTPPort == -1)
    {
      fixedHTTPPort = false;
      gatewayHTTPPort = HTTPConstant.HTTP_RANDOM_PORT_BASE;
    }
    boolean fixedHTTPOverUDPPort = true;
    if (gatewayHTTPOverUDPPort == -1)
    {
      fixedHTTPOverUDPPort = false;
      gatewayHTTPOverUDPPort = HTTPConstant.HTTP_RANDOM_PORT_BASE;
    }
    try
    {
      // start server for message forwarding
      httpServer =
        new HTTPServer(gatewayHTTPPort,
          fixedHTTPPort,
          hostAddress,
          gatewayHTTPMessageProcessor,
          gatewayHTTPMessageProcessor.getIPVersion());

      httpServerSocketAddress = new InetSocketAddress(address, httpServer.getPort());

      // start UDP server
      httpOverUDPSocket = SocketHelper.createSocket(address, gatewayHTTPOverUDPPort, fixedHTTPOverUDPPort);
      httpOverUDPServerSocketAddress = new InetSocketAddress(address, httpOverUDPSocket.getLocalPort());
      httpOverUDPSocketWrapper = new DatagramSocketWrapper(httpOverUDPSocket);
    } catch (Exception e)
    {
      System.out.println("Cannot start InetAddressGatewaySocketStructure for address " + address.getHostAddress());
      logger.fatal("cannot start InetAddressGatewaySocketStructure");
      logger.fatal("reason: " + e.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Retrieves the device HTTP server that listens on the interface associated with this socket
   * structure
   */
  public HTTPServer getHTTPServer()
  {
    return httpServer;
  }

  /** Retrieves the socket address of the device HTTP server */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return httpServerSocketAddress;
  }

  /**
   * Retrieves the httpOverUDPSocket.
   * 
   * @return The httpOverUDPSocket
   */
  public IDatagramSocket getHTTPOverUDPSocket()
  {
    return httpOverUDPSocketWrapper;
  }

  /** Retrieves the socket address of the HTTP over UDP server */
  public InetSocketAddress getHTTPOverUDPServerSocketAddress()
  {
    return httpOverUDPServerSocketAddress;
  }

  /** Terminates the socket structure */
  public void terminate()
  {
    httpServer.terminate();
    httpOverUDPSocket.close();
    super.terminate();
  }

}

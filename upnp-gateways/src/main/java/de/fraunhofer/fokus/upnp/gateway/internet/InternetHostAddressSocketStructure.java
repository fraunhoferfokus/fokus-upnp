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
package de.fraunhofer.fokus.upnp.gateway.internet;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.GatewayHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.http.HTTPServer;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class is used to bundle all sockets that are needed for an Internet gateway.
 * 
 * @author Alexander Koenig
 */
public class InternetHostAddressSocketStructure extends AbstractHostAddressSocketStructure
{
  public static Logger      logger = Logger.getLogger("upnp");

  /** Associated server address */
  private InetSocketAddress httpServerSocketAddress;

  /** Server that receives UPnP messages for forwarding from the Internet */
  private HTTPServer        httpFromInternetServer;

  /** Socket that receives external NOTIFY and M-SEARCH messages from all devices */
  private DatagramSocket    ssdpDeviceSocket;

  /** Socket that sends M-SEARCH messages for all devices */
  private DatagramSocket    ssdpDeviceMSearchSendSocket;

  /**
   * Creates a new instance of InternetHostAddressSocketStructure
   * 
   * @param gatewayHTTPMessageProcessor
   *          Processor to forward HTTP requests
   * @param subscribeForwardingMessageProcessor
   *          Processor to forward subscribe requests
   * @param globalIPAddress
   *          Global IP address that is accessible from the Internet
   * @param ssdpDeviceReceptionPort
   *          Port to receive NOTIFY and M-SEARCH from the Internet
   * @param ssdpDeviceMSearchPort
   *          Port to send M-SEARCH to the Internet
   * @param gatewayServerPort
   *          Port to receive HTTP from the Internet
   * 
   */
  public InternetHostAddressSocketStructure(GatewayHTTPMessageProcessor gatewayHTTPMessageProcessor,
    InetAddress globalIPAddress,
    int ssdpDeviceReceptionPort,
    int ssdpDeviceMSearchPort,
    int gatewayServerPort)
  {
    super(globalIPAddress);

    int ipVersion = gatewayHTTPMessageProcessor.getIPVersion();

    try
    {
      ssdpDeviceSocket = new DatagramSocket(ssdpDeviceReceptionPort);
      ssdpDeviceSocket.setSoTimeout(20);

      ssdpDeviceMSearchSendSocket = new DatagramSocket(ssdpDeviceMSearchPort);
      ssdpDeviceMSearchSendSocket.setSoTimeout(20);

      // Server from tunnel must have a fixed port to allow port forwarding over routers
      httpFromInternetServer = new HTTPServer(gatewayServerPort, true, gatewayHTTPMessageProcessor, ipVersion);

      System.out.println("  Started HTTP Internet server for device requests on port " +
        httpFromInternetServer.getPort());
      httpServerSocketAddress = new InetSocketAddress(globalIPAddress, httpFromInternetServer.getPort());
    } catch (Exception e)
    {
      System.out.println("Cannot start InternetHostAddressSocketStructure for address " +
        globalIPAddress.getHostAddress());
      logger.fatal("cannot start InternetHostAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Retrieves the device HTTP server that listens on the interface associated with this socket structure
   */
  public HTTPServer getHTTPFromInternetServer()
  {
    return httpFromInternetServer;
  }

  /** Retrieves the socket address of the device HTTP server */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return httpServerSocketAddress;
  }

  /** Retrieves the socket that listens for SSDP messages */
  public DatagramSocket getSSDPDeviceSocket()
  {
    return ssdpDeviceSocket;
  }

  /**
   * Retrieves the ssdpDeviceMSearchSendSocket.
   * 
   * @return The ssdpDeviceMSearchSendSocket.
   */
  public IDatagramSocket getSSDPDeviceMSearchSendSocket()
  {
    return new DatagramSocketWrapper(ssdpDeviceMSearchSendSocket);
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
    httpFromInternetServer.terminate();
    try
    {
      ssdpDeviceSocket.close();
      ssdpDeviceMSearchSendSocket.close();
    } catch (Exception e)
    {
    }
  }

}

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
package de.fraunhofer.fokus.upnp.core.device;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

import de.fraunhofer.fokus.upnp.core.UPnPHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPServer;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to bundle all sockets that are needed for one external device interface.
 * 
 * This class adds: A HTTP server for GENA, SOAP and GET messages
 * 
 * @author Alexander Koenig
 */
public class DeviceHostAddressSocketStructure extends UPnPHostAddressSocketStructure
{

  /** Associated server address */
  private InetSocketAddress httpServerAddress;

  /** Server that receives UPnP messages */
  private HTTPServer        httpServer;

  /** Optional socket used for multicast event messages */
  protected MulticastSocket multicastEventSocket;

  private InetSocketAddress multicastEventSocketAddress = null;

  /**
   * Creates a new instance of DeviceInetAddressSocketStructure with fixed ports.
   * 
   * @param device
   *          The associated device
   * @param networkInterface
   *          The associated network interface
   * @param ssdpUnicastPort
   *          The port used for SSDP unicast
   * @param httpServerPort
   *          The port used for HTTP
   * @param address
   *          The address this structure should be bound to
   */
  public DeviceHostAddressSocketStructure(Device device,
    NetworkInterface networkInterface,
    int ssdpUnicastPort,
    int httpServerPort,
    InetAddress address) throws SocketException
  {
    this(device, networkInterface, ssdpUnicastPort, httpServerPort, null, address);
  }

  /**
   * Creates a new instance of DeviceInetAddressSocketStructure
   * 
   * @param device
   *          The associated device
   * @param networkInterface
   *          The associated network interface
   * @param ssdpUnicastPort
   *          The port used for SSDP unicast
   * @param httpServerPort
   *          The port used for HTTP
   * @param multicastEventAddress
   *          The address used for event multicasting
   * @param address
   *          The address this structure should be bound to
   */
  public DeviceHostAddressSocketStructure(Device device,
    NetworkInterface networkInterface,
    int ssdpUnicastPort,
    int httpServerPort,
    InetSocketAddress multicastEventAddress,
    InetAddress address)
  {
    super(networkInterface, address, device.getSSDPMulticastSocketAddress(), ssdpUnicastPort, device.getIPVersion());

    if (!valid)
    {
      return;
    }

    try
    {
      // start server for message processing
      httpServer =
        new HTTPServer(httpServerPort, true, hostAddress, device.getHTTPMessageProcessor(), device.getIPVersion());
      httpServer.setServerName(device.toString());

      TemplateDevice.printMessage(device.toString() + ":   Started HTTP device server on port " + httpServer.getPort());
      TemplateDevice.printMessage(device.toString() + ":   Started SSDP response socket on port " +
        getSSDPUnicastSocket().getLocalPort());
      httpServerAddress = new InetSocketAddress(address, httpServer.getPort());
    } catch (Exception e)
    {
      System.out.println("Cannot start DeviceInetAddressSocketStructure for address " + address.getHostAddress() +
        ": " + e.getMessage());
      logger.fatal("cannot start DeviceInetAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      valid = false;
      return;
    }
    if (multicastEventAddress != null)
    {
      this.multicastEventSocketAddress = multicastEventAddress;
      try
      {
        multicastEventSocket = new MulticastSocket(multicastEventAddress.getPort());
        multicastEventSocket.setInterface(address);
        // IP version 4
        if (device.getIPVersion() == SSDPConstant.IP4)
        {
          multicastEventSocket.joinGroup(multicastEventAddress.getAddress());
        }
        // set timeout to simplify message processing for multiple sockets
        multicastEventSocket.setSoTimeout(10);
        multicastEventSocket.setTimeToLive(SSDPConstant.TTL);

        TemplateDevice.printMessage(device.toString() + ":   Started multicast event socket on address " +
          IPHelper.toString(multicastEventAddress));
      } catch (Exception e)
      {
        System.out.println("Cannot start DeviceInetAddressSocketStructure for address " + address.getHostAddress() +
          ": " + e.getMessage());
        logger.fatal("cannot start DeviceInetAddressSocketStructure");
        logger.fatal("reason: " + e.getMessage());
        valid = false;
      }
    }
  }

  /**
   * Creates a new instance of DeviceInetAddressSocketStructure with random ports.
   * 
   * @param device
   *          The associated device
   * @param networkInterface
   *          The associated network interface
   * @param address
   *          The address this structure should be bound to
   * 
   */
  public DeviceHostAddressSocketStructure(Device device, NetworkInterface networkInterface, InetAddress address)
  {
    super(networkInterface, address, device.getSSDPMulticastSocketAddress(), device.getIPVersion());

    if (!valid)
    {
      return;
    }

    try
    {
      // start server for message processing
      httpServer =
        new HTTPServer(HTTPConstant.HTTP_RANDOM_PORT_BASE,
          false,
          hostAddress,
          device.getHTTPMessageProcessor(),
          device.getIPVersion());
      httpServer.setServerName(device.toString());

      TemplateDevice.printMessage(device.toString() + ":   Started HTTP device server on port " + httpServer.getPort());
      httpServerAddress = new InetSocketAddress(address, httpServer.getPort());

    } catch (Exception e)
    {
      System.out.println("Cannot start DeviceInetAddressSocketStructure for address " + address.getHostAddress());
      logger.fatal("cannot start DeviceInetAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      valid = false;
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
  public InetSocketAddress getHTTPServerAddress()
  {
    return httpServerAddress;
  }

  /**
   * Retrieves the multicastEventSocket.
   * 
   * @return The multicastEventSocket
   */
  public MulticastSocket getMulticastEventSocket()
  {
    return multicastEventSocket;
  }

  /**
   * Retrieves the multicastEventSocketAddress.
   * 
   * @return The multicastEventSocketAddress
   */
  public InetSocketAddress getMulticastEventSocketAddress()
  {
    return multicastEventSocketAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.HostAddressSocketStructure#terminate()
   */
  public void terminate()
  {
    if (httpServer != null)
    {
      httpServer.terminate();
      httpServer = null;
    }
    if (multicastEventSocket != null)
    {
      try
      {
        multicastEventSocket.close();
      } catch (Exception e)
      {
      }
      multicastEventSocket = null;
    }
    super.terminate();
  }

}

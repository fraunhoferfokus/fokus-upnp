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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import de.fraunhofer.fokus.upnp.core.UPnPHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPServer;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;

/**
 * This class is used to bundle all sockets that are needed for one external control point interface.
 * 
 * This adds: A HTTP server for GENA event messages A HTTP over UDP server for UDP GENA event messages A HTTP over
 * multicast server for multicast GENA event messages
 * 
 * @author Alexander Koenig
 * 
 */
public class ControlPointHostAddressSocketStructure extends UPnPHostAddressSocketStructure
{

  private static Object     lock = new Object();

  /** Server address and port */
  private InetSocketAddress httpServerAddress;

  private HTTPServer        eventCallbackServer;

  private DatagramSocket    eventCallbackUDPServer;

  private InetSocketAddress callbackUDPServerSocketAddress;

  private MulticastSocket   multicastEventUDPServer;

  private InetSocketAddress multicastUDPServerSocketAddress;

  /** Creates a new instance of ControlPointHostAddressSocketStructure with random ports. */
  public ControlPointHostAddressSocketStructure(ControlPoint controlPoint,
    NetworkInterface networkInterface,
    InetAddress address,
    boolean useEventCallbackUDPServer)
  {
    super(networkInterface, address, controlPoint.getSSDPMulticastSocketAddress(), controlPoint.getIPVersion());

    if (!valid)
    {
      return;
    }
    try
    {
      // start server for NOTIFY message processing
      eventCallbackServer =
        new HTTPServer(HTTPConstant.HTTP_RANDOM_PORT_BASE,
          false,
          hostAddress,
          controlPoint.getCPEventMessageProcessor(),
          controlPoint.getIPVersion());

      httpServerAddress = new InetSocketAddress(address, eventCallbackServer.getPort());

      // start optional UDP event server
      if (useEventCallbackUDPServer)
      {
        callbackUDPServerSocketAddress = new InetSocketAddress(address, 0);
        eventCallbackUDPServer = new DatagramSocket(callbackUDPServerSocketAddress);
      }
    } catch (Exception e)
    {
      System.out.println("Cannot start ControlPointHostAddressSocketStructure for address " + address.getHostAddress());
      logger.fatal("cannot start ControlPointHostAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      valid = false;
      return;
    }
    printUsedPorts(controlPoint);
  }

  /**
   * Creates a new instance of ControlPointHostAddressSocketStructure with fixed ports and two optional proprietary
   * extension:
   * 
   * Eventing over UDP Eventing over multicast UDP
   * 
   */
  public ControlPointHostAddressSocketStructure(ControlPoint controlPoint,
    NetworkInterface networkInterface,
    InetAddress address,
    int eventCallbackServerPort,
    int eventCallbackUDPServerPort,
    int multicastEventServerPort,
    int ssdpUnicastPort)
  {
    super(networkInterface,
      address,
      controlPoint.getSSDPMulticastSocketAddress(),
      ssdpUnicastPort,
      controlPoint.getIPVersion());

    if (!valid)
    {
      return;
    }
    try
    {
      // start server for NOTIFY message processing
      eventCallbackServer =
        new HTTPServer(eventCallbackServerPort,
          true,
          hostAddress,
          controlPoint.getCPEventMessageProcessor(),
          controlPoint.getIPVersion());

      // start optional UDP event server
      if (eventCallbackUDPServerPort != -1)
      {
        callbackUDPServerSocketAddress = new InetSocketAddress(address, eventCallbackUDPServerPort);
        eventCallbackUDPServer = new DatagramSocket(callbackUDPServerSocketAddress);
      }
      // start optional multicast event server
      if (multicastEventServerPort != -1)
      {
        multicastUDPServerSocketAddress = new InetSocketAddress(address, multicastEventServerPort);
        multicastEventUDPServer = new MulticastSocket(multicastEventServerPort);
        multicastEventUDPServer.setInterface(address);
        multicastEventUDPServer.setTimeToLive(SSDPConstant.TTL);
      }
      httpServerAddress = new InetSocketAddress(address, eventCallbackServer.getPort());
    } catch (Exception e)
    {
      System.out.println("Cannot start ControlPointHostAddressSocketStructure for address " + address.getHostAddress());
      logger.fatal("cannot start ControlPointHostAddressSocketStructure");
      logger.fatal("reason: " + e.getMessage());
      valid = false;
      return;
    }
    printUsedPorts(controlPoint);
  }

  /** Prints a message with the used ports to the console. */
  private void printUsedPorts(ControlPoint controlPoint)
  {
    synchronized(lock)
    {
      TemplateControlPoint.printMessage(controlPoint.toString() + ":   Started HTTP event callback server on port " +
        eventCallbackServer.getPort());

      // start optional UDP event server
      if (eventCallbackUDPServer != null)
      {
        TemplateControlPoint.printMessage(controlPoint.toString() + ":   Started HTTPU event callback server on port " +
          eventCallbackUDPServer.getLocalPort());
      }
      // start optional multicast event server
      if (multicastEventUDPServer != null)
      {
        TemplateControlPoint.printMessage(controlPoint.toString() +
          ":   Started HTTPU event multicast server on port " + multicastEventUDPServer.getLocalPort());
      }
      TemplateControlPoint.printMessage(controlPoint.toString() + ":   Started SSDP search socket on port " +
        getSSDPUnicastSocket().getLocalPort());
    }
  }

  /**
   * Retrieves the event callback server that listens on the interface associated with this socket structure
   */
  public HTTPServer getEventCallbackServer()
  {
    return eventCallbackServer;
  }

  /**
   * Retrieves the UDP event callback server that listens on the interface associated with this socket structure
   */
  public DatagramSocket getEventCallbackUDPServer()
  {
    return eventCallbackUDPServer;
  }

  /**
   * Retrieves the multicastEventUDPServer.
   * 
   * @return The multicastEventUDPServer
   */
  public MulticastSocket getMulticastEventUDPServer()
  {
    return multicastEventUDPServer;
  }

  /** Retrieves the socket address of the event callback HTTP server */
  public InetSocketAddress getHTTPServerAddress()
  {
    return httpServerAddress;
  }

  /**
   * Retrieves the callbackUDPServerSocketAddress.
   * 
   * @return The callbackUDPServerSocketAddress
   */
  public InetSocketAddress getUDPServerSocketAddress()
  {
    return callbackUDPServerSocketAddress;
  }

  /**
   * Retrieves the multicastUDPServerSocketAddress.
   * 
   * @return The multicastUDPServerSocketAddress
   */
  public InetSocketAddress getMulticastUDPServerSocketAddress()
  {
    return multicastUDPServerSocketAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.HostAddressSocketStructure#terminate()
   */
  public void terminate()
  {
    if (eventCallbackServer != null)
    {
      eventCallbackServer.terminate();
      eventCallbackServer = null;
    }
    if (eventCallbackUDPServer != null)
    {
      try
      {
        eventCallbackUDPServer.close();
      } catch (Exception e)
      {
      }
      eventCallbackUDPServer = null;
    }
    if (multicastEventUDPServer != null)
    {
      try
      {
        multicastEventUDPServer.close();
      } catch (Exception e)
      {
      }
      multicastEventUDPServer = null;
    }
    super.terminate();
  }

}

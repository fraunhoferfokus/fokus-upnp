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
package de.fraunhofer.fokus.upnp.gateway.common.forwarder_module;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/** This interface must be implemented by all classes that forward UPnP messages */
public interface IForwarderModule
{

  /**
   * Retrieves the ID for this module
   * 
   * @return The ID for this module.
   * 
   */
  public String getModuleID();

  /**
   * Checks if this forwarder module is bound to the local host address.
   * 
   * @return True if this module is bound to the local host address, false otherwise
   * 
   */
  public boolean isLocalHostAddressForwarderModule();

  /**
   * Checks if this forwarder module is bound to a network interface.
   * 
   * @return True if this module is bound to a network interface, false otherwise
   * 
   */
  public boolean isNetworkInterfaceForwarderModule();

  /**
   * Checks if this forwarder module is bound to a specific subnet.
   * 
   * @param address
   *          The address that should be checked
   * 
   * @return True if this module is bound to the subnet, false otherwise
   * 
   */
  public boolean isSubnetForwarderModule(InetAddress address);

  /**
   * Checks if the HTTP server address belongs to this module.
   * 
   * @param serverAddress
   *          The address
   * @return True if the address belongs to this module, false otherwise
   * 
   */
  public boolean isHTTPServerAddress(InetSocketAddress serverAddress);

  /**
   * Checks if a device should be visible over this forwarder module. This can be used to filter
   * devices for this forwarder module.
   * 
   * @param device
   *          The device
   * 
   * @return True if the device should be visible over this module, false otherwise
   * 
   */
  public boolean announceDeviceToForwarderModule(AbstractDevice device);

  /**
   * Checks if a device should be visible over this forwarder module. This can be used to filter
   * devices for this forwarder module.
   * 
   * @param uuid
   *          The uuid of the device
   * 
   * @return True if the device should be visible over this module, false otherwise
   * 
   */
  public boolean announceDeviceToForwarderModule(String uuid);

  /**
   * Checks if a device that is connected over this forwarder module should be visible for the
   * central message forwarder.
   * 
   * @param uuid
   *          The uuid of the device
   * 
   * @return True if the device should be visible for the message forwarder, false otherwise
   * 
   */
  public boolean announceDeviceToMessageForwarder(String uuid);

  /**
   * A NOTIFY message was received by the module.
   * 
   * @param message
   *          The received message
   * 
   */
  public void receivedNotifyMessage(HTTPMessageObject message);

  /**
   * Forwards a NOTIFY message over this forwarder module. The message is changed to allow the
   * reception of consecutive GET messages over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the NOTIFY message
   * @param message
   *          The received message
   */
  public void forwardNotifyMessage(String sourceModuleID, HTTPMessageObject message);

  /**
   * A M-SEARCH message was received by the module.
   * 
   * @param message
   *          The received M-SEARCH message
   */
  public void receivedMSearchMessage(HTTPMessageObject message);

  /**
   * Forwards a M-SEARCH message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the M-SEARCH message
   * @param message
   *          The received message
   * @param replyAddress
   *          The address that should receive the responses
   */
  public void forwardMSearchMessage(String sourceModuleID, HTTPMessageObject message, InetSocketAddress replyAddress);

  /**
   * Forwards a M-SEARCH response message over this forwarder module.
   * 
   * @param message
   *          The response message
   * @param forwarderModuleID
   *          The ID of the module that received the M-SEARCH response
   * @param replyAddress
   *          The address that should receive the responses
   */
  public void forwardMSearchResponseMessage(HTTPMessageObject message,
    String forwarderModuleID,
    InetSocketAddress replyAddress);

  /**
   * A HTTP request was received by the module.
   * 
   * @param message
   *          The received message
   * 
   * @return The response message
   * 
   */
  public HTTPMessageObject receivedHTTPMessage(HTTPMessageObject message);

  /**
   * Forwards a HTTP request over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   * 
   * @return The response message
   */
  public HTTPMessageObject forwardHTTPRequest(String sourceModuleID, HTTPMessageObject message);

  /**
   * A UDP HTTP message was received by the module.
   * 
   * @param message
   *          The received message
   * 
   * @return The response message
   * 
   */
  public HTTPMessageObject receivedHTTPOverUDPMessage(HTTPMessageObject message);

  /**
   * Forwards a UDP HTTP message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   * 
   * @return The response message
   */
  public HTTPMessageObject forwardHTTPOverUDPMessage(String sourceModuleID, HTTPMessageObject message);

  /**
   * Sends a vector with message strings over this forwarder. The messages are not changed before
   * sending.
   * 
   * @param messages
   *          A vector with strings containing SSDP messages.
   * 
   */
  public void sendMessagesToSSDPMulticast(Vector messages);

  /**
   * Retrieves the SSDP socket that is used to receive NOTIFY and M-SEARCH messages.
   * 
   * @return The socket used for SSDP messages
   */
  public IDatagramSocket getSSDPSocket();

  /**
   * Retrieves the socket address of the HTTP server.
   * 
   * @return The socket address of the HTTP server of the forwarder module
   */
  public InetSocketAddress getHTTPServerSocketAddress();

  /**
   * Retrieves the address or name of the HTTP server.
   * 
   * @return The address of the HTTP server of the forwarder module
   */
  public String getHTTPServerAddress();

  /**
   * Retrieves the port of the HTTP server.
   * 
   * @return The port of the HTTP server of the forwarder module
   */
  public int getHTTPServerPort();

  /**
   * Retrieves the socket address of the UDP HTTP server.
   * 
   * @return The socket address of the UDP HTTP server of the forwarder module or null
   */
  public InetSocketAddress getHTTPOverUDPServerSocketAddress();

  /**
   * Retrieves the port of the UDP HTTP server.
   * 
   * @return The port of the UDP HTTP server of the forwarder module or -1
   */
  public int getHTTPOverUDPServerPort();

  /**
   * Retrieves the socket that is used to receive HTTP over UDP messages.
   * 
   * @return The socket used for HTTP over UDP messages
   */
  public IDatagramSocket getHTTPOverUDPSocket();

  /**
   * Retrieves the multicast socket address
   * 
   * @return The socket address for multicast of the forwarder module
   */
  public InetSocketAddress getSSDPMulticastSocketAddress();

  /**
   * Retrieves a HTTP client that can be used to forward a HTTP message over this forwarder module.
   * 
   * @return A HTTP client for the forwarder module
   */
  public IHTTPClient getHTTPClient();

  /**
   * Retrieves a HTTP client that can be used to forward a UDP HTTP message over this forwarder
   * module.
   * 
   * @return A UDP HTTP client for the forwarder module
   */
  public IHTTPOverUDPClient getHTTPOverUDPClient();

  /**
   * Retrieves the creation time of the module.
   * 
   * @return The creation time
   */
  public long getCreationTime();

  /** Terminates the forwarder module */
  public void terminate();

}

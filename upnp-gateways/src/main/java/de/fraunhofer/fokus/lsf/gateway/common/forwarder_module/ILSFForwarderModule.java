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
package de.fraunhofer.fokus.lsf.gateway.common.forwarder_module;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/** This interface must be implemented by all classes that forward LSF messages */
public interface ILSFForwarderModule
{

  /**
   * Retrieves the ID for this module
   * 
   * @return The ID for this module.
   * 
   */
  public byte getModuleID();

  /**
   * Retrieves the host address for this module
   * 
   * @return The host address for this module.
   * 
   */
  public InetAddress getModuleHostAddress();

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
   * An announcement message was received by the module.
   * 
   * @param message
   *          The received message
   * 
   */
  public void receivedAnnouncementMessage(BinaryMessageObject message);

  /**
   * Checks if a device that is connected over this forwarder module should be visible for the central message
   * forwarder.
   * 
   * @param uuid
   *          The uuid of the device
   * 
   * @return True if the device should be visible for the message forwarder, false otherwise
   * 
   */
  public boolean canProcessAnnouncementMessage(long deviceID);

  /**
   * Checks if a device should be visible over this forwarder module. This can be used to filter devices for this
   * forwarder module.
   * 
   * @param deviceID
   *          The ID of the device
   * 
   * @return True if the device should be visible over this module, false otherwise
   * 
   */
  public boolean canForwardAnnouncementMessage(long deviceID);

  /**
   * Forwards an announcement message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   */
  public void forwardAnnouncementMessage(byte sourceModuleID, BinaryMessageObject message);

  /**
   * Sends a vector with message strings over this forwarder. The messages are not changed before sending.
   * 
   * @param messages
   *          A vector with strings containing messages.
   * 
   */
  public void sendAnnouncementMessagesToMulticast(Vector messages);

  /**
   * A search message was received by the module.
   * 
   * @param message
   *          The received message
   */
  public void receivedSearchMessage(BinaryMessageObject message);

  /**
   * Checks if this forwarder module forwards search messages from another forwarder module.
   * 
   * 
   * @return True if the search should be forwarded over this module, false otherwise
   * 
   */
  public boolean canForwardSearchMessage(byte sourceModuleID);

  /**
   * Forwards a search message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   */
  public void forwardSearchMessage(byte sourceModuleID, BinaryMessageObject message);

  /**
   * Forwards a search response message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The ID of the module that received the search response
   * @param message
   *          The response message
   * @param responseAddress
   *          The address that should receive the responses
   */
  public void forwardSearchResponseMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress responseAddress);

  /**
   * Forwards a device request message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   * @param accessAddress
   * 
   */
  public void forwardDeviceRequestMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress accessAddress);

  /**
   * Forwards a device response message over this forwarder module.
   * 
   * @param sourceModuleID
   *          The module that received the message
   * @param message
   *          The received message
   * @param responseAddress
   * 
   */
  public void forwardDeviceResponseMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress responseAddress);

  /**
   * An event message was received by the module.
   * 
   * @param message
   *          The received message
   * 
   */
  public void receivedEventMessage(BinaryMessageObject message);

  /**
   * Checks if events from a specific device should be visible over this forwarder module. This can be used to filter
   * devices for this forwarder module.
   * 
   * @param deviceID
   *          The ID of the device
   * 
   * @return True if the device events should be visible over this module, false otherwise
   * 
   */
  public boolean canForwardEventMessage(long deviceID);

  /**
   * Forwards an event message over this forwarder module.
   * 
   * @param message
   *          The received message
   */
  public void forwardEventMessage(BinaryMessageObject message);

  /**
   * Retrieves the socket that is used for discovery.
   * 
   * @return The socket used for discovery
   */
  public IDatagramSocket getDiscoverySocket();

  /**
   * Retrieves the socket that is used for eventing.
   * 
   * @return The socket used for eventing
   */
  public IDatagramSocket getEventSocket();

  /**
   * Retrieves the socket that is used for search responses, description and control.
   * 
   * @return The socket used for unicast messages
   */
  public IDatagramSocket getUnicastSocket();

  /**
   * Retrieves the group socket address that receives discovery messages
   * 
   * @return The discovery socket address of the forwarder module
   */
  public InetSocketAddress getDiscoveryGroupSocketAddress();

  /**
   * Retrieves the socket address that receives unicast messages
   * 
   * @return The socket address for unicast of the forwarder module
   */
  public InetSocketAddress getUnicastSocketAddress();

  /**
   * Retrieves the group address that receives event messages
   * 
   * @return The event socket address of the forwarder module
   */
  public InetSocketAddress getEventGroupSocketAddress();

  /**
   * Retrieves the creation time of the module.
   * 
   * @return The creation time
   */
  public long getCreationTime();

  /** Retrieves the phy type of this forwarder module */
  public byte getPhyType();

  /** Terminates the forwarder module */
  public void terminate();

}

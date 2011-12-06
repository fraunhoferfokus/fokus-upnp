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
package de.fraunhofer.fokus.upnp.util.tunnel.common.ip;

import java.net.InetAddress;

/**
 * This interface is implemented by classes that provide a virtual IPv4 network interface.
 * 
 * @author Alexander Koenig
 */
public interface IIPTunnelNetworkInterface
{

  /**
   * This method sends a packet to the IP tunnel
   * 
   * @param packetType
   *          Type of packet (TCP, UDP, Multicast)
   * @param ipPacket
   *          IP packet that should be send
   */
  public void sendIPPacketToTunnel(byte packetType, IPPacket ipPacket);

  /** This method sends a config message with an ID. */
  public void sendIDToTunnel(byte[] id);

  /** Sets the inet address used by the IP tunnel. */
  public void setIPTunnelInetAddress(InetAddress ipTunnelInetAddress);

  /**
   * Checks if this IP tunnel has a valid tunnel connection.
   * 
   * @return True if the underlying tunnel is connected, false otherwise
   */
  public boolean isConnected();

  /** Retrieves the inet address used by the IP tunnel */
  public InetAddress getIPTunnelInetAddress();

  /** Retrieves the maximum segment size for this network interface. */
  public int getMaximumSegmentSize();

  /** Sets the maximum segment size for this network interface. */
  public void setMaximumSegmentSize(int maximumSegmentSize);

  public boolean acceptOnlySinglePacketsPerSocket();

  public void setAcceptOnlySinglePacketsPerSocket(boolean acceptOnlySinglePacketsPerSocket);

  /**
   * Sets the packetGapTime.
   * 
   * @param packetGapTime
   *          The new value for packetGapTime
   */
  public void setPacketGapTime(long packetGapTime);

  /** Retrieves the factory for socket creation. */
  public IPTunnelSocketFactory getIPTunnelSocketFactory();

  /**
   * Retrieves the connectionTime of the outer connection.
   * 
   * @return The connectionTime
   */
  public long getConnectionTime();

  /**
   * Retrieves the disconnection time of the outer connection
   * 
   * 
   * @return The disconnectionTime
   */
  public long getDisconnectionTime();

  /**
   * Sets the ipTunnelEventListener
   * 
   * @param ipTunnelEventListener
   *          The ipTunnelEventListener to set.
   */
  public void setIPTunnelEventListener(IIPTunnelEventListener ipTunnelEventListener);

  /** Terminates the network interface */
  public void terminate();

}

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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class represents a DatagramSocket that receives packets over an IP tunnel.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelDatagramSocket implements IDatagramSocket
{
  /** Associated management */
  protected IPTunnelSocketFactory ipTunnelSocketFactory;

  /** Virtual port used by this socket */
  private int                     virtualPort;

  /** List of packets that must be read */
  protected Vector                receivedPacketList = new Vector();

  protected Object                lock               = new Object();

  protected int                   socketTimeout      = 0;

  protected boolean               closeSocket        = false;

  /**
   * Creates a new instance of IPTunnelDatagramSocket and binds it to the specified port and the local IP address of the
   * network interface.
   * 
   * @param ipTunnelSocketFactory
   *          Management for tunnel socket creation
   * @param port
   *          The port that should be used by the socket
   * 
   */
  protected IPTunnelDatagramSocket(IPTunnelSocketFactory ipTunnelSocketFactory, int port) throws IOException
  {
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    this.virtualPort = port;
  }

  /** Closes the socket */
  public void close()
  {
    closeSocket = true;
    ipTunnelSocketFactory.removeIPTunnelDatagramSocket(this);
  }

  /** Sends a packet over the socket */
  public void send(DatagramPacket p)
  {
    if (p.getLength() > ipTunnelSocketFactory.getIPTunnelNetworkInterface().getMaximumSegmentSize())
    {
      Portable.println("Payload too big (" + p.getLength() + " > " +
        ipTunnelSocketFactory.getIPTunnelNetworkInterface().getMaximumSegmentSize() + ")");
      return;
    }
    // create UDP packet
    byte[] data = new byte[p.getLength()];
    System.arraycopy(p.getData(), p.getOffset(), data, 0, data.length);

    UDPPacket udpPacket = new UDPPacket(virtualPort, p.getPort(), data);

    // calculate checksum
    udpPacket.calculateChecksum(getLocalAddress(), p.getAddress());

    // create corresponding ip packet
    IPPacket ipPacket =
      new IPPacket(IPTunnelConstants.PROTOCOL_TYPE_UDP, getLocalAddress(), p.getAddress(), udpPacket.toByteArray());

    // send IP packet
    ipTunnelSocketFactory.getIPTunnelNetworkInterface().sendIPPacketToTunnel(IPTunnelConstants.PACKET_TYPE_UDP,
      ipPacket);
  }

  /** Sends a packet over the socket with a fake source address */
  public void send(DatagramPacket p, InetSocketAddress sourceAddress)
  {
    if (sourceAddress == null)
    {
      send(p);
      return;
    }

    // create UDP packet
    byte[] data = new byte[p.getLength()];
    System.arraycopy(p.getData(), p.getOffset(), data, 0, data.length);

    // create UDP packet
    UDPPacket udpPacket = new UDPPacket(sourceAddress.getPort(), p.getPort(), data);

    // calculate checksum
    udpPacket.calculateChecksum(sourceAddress.getAddress(), p.getAddress());

    // create corresponding ip packet
    IPPacket ipPacket =
      new IPPacket(IPTunnelConstants.PROTOCOL_TYPE_UDP,
        sourceAddress.getAddress(),
        p.getAddress(),
        udpPacket.toByteArray());

    // send IP packet
    ipTunnelSocketFactory.getIPTunnelNetworkInterface().sendIPPacketToTunnel(IPTunnelConstants.PACKET_TYPE_UDP,
      ipPacket);
  }

  /** Waits for received packets */
  public void receive(DatagramPacket p) throws SocketException, SocketTimeoutException
  {
    long time = System.currentTimeMillis();
    // wait for a packet
    while (!closeSocket && receivedPacketList.size() == 0 &&
      (socketTimeout == 0 || System.currentTimeMillis() - time < socketTimeout))
    {
      try
      {
        Thread.sleep(Math.max(1, Math.min(50, getSoTimeout())));
      } catch (Exception ex)
      {
      }
    }
    if (closeSocket)
    {
      throw new SocketException("Socket closed");
    }
    if (receivedPacketList.size() == 0)
    {
      throw new SocketTimeoutException("Operation timed out");
    }
    UDPPacket udpPacket;
    synchronized(lock)
    {
      udpPacket = (UDPPacket)receivedPacketList.remove(0);
    }
    // copy content to datagram packet
    p.setAddress(udpPacket.getIPPacket().getSourceAddress());
    p.setPort(udpPacket.getSourcePort());
    // check buffer size
    int length = Math.min(udpPacket.getUpperLayerData().length, p.getOffset() + p.getData().length);
    System.arraycopy(udpPacket.getUpperLayerData(), 0, p.getData(), p.getOffset(), length);
    p.setLength(length);
  }

  /** Adds a received tunnel UDP packet to the input queue of this socket */
  protected void processReceivedPacket(UDPPacket udpPacket)
  {
    synchronized(lock)
    {
      // System.out.println(" Add packet to receivedPacketList");
      receivedPacketList.add(udpPacket);
    }
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /** Retrieves the local IP address of this socket (e.g., 192.168.x.y) */
  public InetAddress getLocalAddress()
  {
    return ipTunnelSocketFactory.getIPTunnelNetworkInterface().getIPTunnelInetAddress();
  }

  /** Retrieves the local port of this socket */
  public int getLocalPort()
  {
    return virtualPort;
  }

  /** Retrieves the local IP address and port of this socket */
  public SocketAddress getLocalSocketAddress()
  {
    return new InetSocketAddress(getLocalAddress(), virtualPort);
  }

  /** Retrieves the SO_TIMEOUT, in milliseconds. */
  public int getSoTimeout()
  {
    return socketTimeout;
  }

  /** Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds. */
  public void setSoTimeout(int timeout)
  {
    socketTimeout = Math.max(0, timeout);
  }

}

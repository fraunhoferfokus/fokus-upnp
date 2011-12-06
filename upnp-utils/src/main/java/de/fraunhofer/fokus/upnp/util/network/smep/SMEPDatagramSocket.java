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
package de.fraunhofer.fokus.upnp.util.network.smep;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.LSFHelper;

/**
 * This class represents a DatagramSocket that receives and sends SMEP packets.
 * 
 * @author Alexander Koenig
 */
public class SMEPDatagramSocket implements IDatagramSocket
{
  /** Associated management */
  protected SMEPSocketFactory smepSocketFactory;

  /** Virtual port used by this socket */
  private int                 virtualPort;

  /** List of packets that must be read */
  protected Vector            receivedPacketList = new Vector();

  protected Object            lock               = new Object();

  protected int               socketTimeout      = 0;

  protected boolean           closeSocket        = false;

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
  protected SMEPDatagramSocket(SMEPSocketFactory smepSocketFactory, int port) throws IOException
  {
    this.smepSocketFactory = smepSocketFactory;
    this.virtualPort = port;
  }

  /** Closes the socket */
  public void close()
  {
    closeSocket = true;
    smepSocketFactory.removeSMEPDatagramSocket(this);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.IDatagramSocket#send(java.net.DatagramPacket)
   */
  public void send(DatagramPacket p)
  {
    // create SMEP packet
    byte[] data = new byte[p.getLength()];
    System.arraycopy(p.getData(), p.getOffset(), data, 0, data.length);

    SMEPPacket smepPacket = new SMEPPacket(virtualPort, p.getPort(), data);

    // source address is set in network layer if needed

    // set destination address
    byte[] destinationAddress = new byte[2];
    // copy last two bytes of packet destination address to SMEP packet
    Portable.arraycopy(p.getAddress().getAddress(), 2, destinationAddress, 0, 2);
    smepPacket.setDestinationAddress(destinationAddress);

    // send packet over HAL
    smepSocketFactory.getSMEPPacketSender().sendSMEPPacket(smepPacket);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.IDatagramSocket#send(java.net.DatagramPacket, java.net.InetSocketAddress)
   */
  public void send(DatagramPacket packet, InetSocketAddress sourceAddress) throws IOException
  {
    // not implemented
    return;
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
    SMEPPacket smepPacket;
    synchronized(lock)
    {
      smepPacket = (SMEPPacket)receivedPacketList.remove(0);
    }
    // copy content to datagram packet
    p.setAddress(LSFHelper.lsfAddressToInetAddress(smepPacket.getSourceAddress()));
    p.setPort(smepPacket.getSourcePort());
    // check buffer size
    int length = Math.min(smepPacket.getUpperLayerData().length, p.getOffset() + p.getData().length);
    System.arraycopy(smepPacket.getUpperLayerData(), 0, p.getData(), p.getOffset(), length);
    p.setLength(length);
  }

  /** Adds a received SMEP packet to the input queue of this socket */
  protected void processReceivedPacket(SMEPPacket smepPacket)
  {
    synchronized(lock)
    {
      //      System.out.println(" Add packet to receivedPacketList");
      receivedPacketList.add(smepPacket);
    }
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /** Retrieves the local address of this socket (e.g., 192.168.x.y) */
  public InetAddress getLocalAddress()
  {
    if (smepSocketFactory.getNetworkStatus() != null)
    {
      return LSFHelper.lsfAddressToInetAddress(smepSocketFactory.getNetworkStatus().getLocalAddress());
    }
    return null;
  }

  /** Retrieves the local port of this socket */
  public int getLocalPort()
  {
    return virtualPort;
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

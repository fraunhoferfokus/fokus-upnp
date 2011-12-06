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
package de.fraunhofer.fokus.upnp.util.network;

import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class manages received UDP packets to prevent multiple forwarding.
 * 
 * @author Alexander Koenig
 */
public class UDPPacketManager implements IEventListener
{

  private static final long PACKET_TIMEOUT    = 20000;

  // list of received unicast packets
  private Vector            unicastPacketList = new Vector();

  /** Timeout for removal */
  private long              packetTimeout;

  /**
   * Creates a new instance of UDPPacketManager with a timeout of 20 seconds.
   * 
   * @param name
   *          A name for the thread
   */
  public UDPPacketManager(String name)
  {
    this(name, PACKET_TIMEOUT);
  }

  /**
   * Creates a new instance of UDPPacketManager with a specific timeout.
   * 
   * @param name
   *          A name for the thread
   * @param timeout
   *          A timeout for packets
   */
  public UDPPacketManager(String name, long timeout)
  {
    this.packetTimeout = timeout;
  }

  /**
   * Adds a packet to this manager.
   * 
   * @param content
   *          The packet content
   * @param sourceAddress
   *          The source address
   * @param sourcePort
   *          The source port
   */
  public void addPacket(byte[] content, InetAddress sourceAddress, int sourcePort)
  {
    if (!isKnownPacket(content, sourceAddress, sourcePort))
    {
      PacketEntity packetEntity = new PacketEntity(content, sourceAddress, sourcePort);
      unicastPacketList.add(packetEntity);
    }
  }

  /**
   * Adds a packet to this manager.
   * 
   * @param packet
   *          The packet
   */
  public void addPacket(BinaryMessageObject packet)
  {
    if (!isKnownPacket(packet.getBody(), packet.getSourceAddress().getAddress(), packet.getSourceAddress().getPort()))
    {
      PacketEntity packetEntity =
        new PacketEntity(packet.getBody(), packet.getSourceAddress().getAddress(), packet.getSourceAddress().getPort());
      unicastPacketList.add(packetEntity);
    }
  }

  /**
   * Adds a packet to this manager.
   * 
   * @param content
   *          The packet content
   */
  public void addPacket(String content)
  {
    byte[] contentData = StringHelper.stringToByteArray(content);
    if (!isKnownPacket(contentData, null, -1))
    {
      PacketEntity packetEntity = new PacketEntity(contentData, null, -1);
      unicastPacketList.add(packetEntity);
    }
  }

  /** Checks if a packet with this data is already known */
  public boolean isKnownPacket(byte[] content, InetAddress source, int port)
  {
    boolean result = false;
    for (int i = 0; !result && i < unicastPacketList.size(); i++)
    {
      PacketEntity entity = (PacketEntity)unicastPacketList.elementAt(i);
      result = result || entity.equals(content, source, port);
    }
    return result;
  }

  /** Checks if a packet with this data is already known */
  public boolean isKnownPacket(BinaryMessageObject packet)
  {
    return isKnownPacket(packet.getBody(), packet.getSourceAddress().getAddress(), packet.getSourceAddress().getPort());
  }

  public long getPacketTimeout()
  {
    return packetTimeout;
  }

  public void setPacketTimeout(long timeout)
  {
    packetTimeout = Math.max(1, timeout);
  }

  /** Remove packets after the defined timeout */
  private void removeDeprecatedPackets()
  {
    int i = 0;
    while (i < unicastPacketList.size())
    {
      PacketEntity entity = (PacketEntity)unicastPacketList.elementAt(i);
      if (System.currentTimeMillis() - entity.timestamp > packetTimeout)
      {
        unicastPacketList.remove(i);
        i--;
      }
      i++;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    removeDeprecatedPackets();
  }

  /** Private class that holds one received UDP packet with a timestamp */
  private class PacketEntity
  {
    public byte[]      content;

    public long        timestamp;

    public InetAddress sourceAddress;

    public int         sourcePort;

    public PacketEntity(byte[] content, InetAddress source, int port)
    {
      this.content = content;
      sourceAddress = source;
      sourcePort = port;
      timestamp = System.currentTimeMillis();
    }

    public boolean equals(byte[] content, InetAddress source, int port)
    {
      // quick check
      boolean equal =
        (sourceAddress == null || sourceAddress.equals(source)) && (sourcePort == -1 || sourcePort == port) &&
          content != null && this.content != null && content.length == this.content.length;

      // if everything is equal so far, check content bytewise
      if (equal)
      {
        for (int i = 0; i < content.length; i++)
        {
          equal &= content[i] == this.content[i];
        }
      }

      return equal;
    }

  }

}

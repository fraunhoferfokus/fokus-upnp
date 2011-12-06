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
 * This class encapsulates one UDP packet.
 * 
 * @author Alexander Koenig
 */
public class UDPPacket
{
  private IPPacket ipPacket;

  private int      sourcePort;

  private int      destinationPort;

  private int      length;

  private int      checksum;

  private byte[]   upperLayerData;

  private boolean  checksumValid = false;

  /**
   * Creates a new instance of UDPPacket
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param upperLayerData
   *          The upper layer data
   */
  public UDPPacket(int sourcePort, int destinationPort, byte[] upperLayerData)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;
    length = 8 + upperLayerData.length;
    this.upperLayerData = upperLayerData;
    checksum = 0;
  }

  /**
   * Creates a new instance of UDPPacket from the corresponding byte array
   * 
   * @param packetData
   *          The byte array that forms the UDP packet
   */
  public UDPPacket(IPPacket ipPacket, byte[] packetData)
  {
    this.ipPacket = ipPacket;

    sourcePort = ((packetData[0] & 0xFF) << 8) + (packetData[1] & 0xFF);
    destinationPort = ((packetData[2] & 0xFF) << 8) + (packetData[3] & 0xFF);
    length = ((packetData[4] & 0xFF) << 8) + (packetData[5] & 0xFF);
    checksum = ((packetData[6] & 0xFF) << 8) + (packetData[7] & 0xFF);

    upperLayerData = new byte[packetData.length - 8];
    System.arraycopy(packetData, 8, upperLayerData, 0, upperLayerData.length);

    checksumValid = calculateChecksum(ipPacket.getSourceAddress(), ipPacket.getDestinationAddress(), packetData) == 0;
  }

  /** Retrieves the byte array representation of the UDP packet */
  public byte[] toByteArray()
  {
    byte[] result = new byte[8 + upperLayerData.length];

    result[0] = (byte)((sourcePort & 0xFF00) >> 8);
    result[1] = (byte)(sourcePort & 0xFF);
    result[2] = (byte)((destinationPort & 0xFF00) >> 8);
    result[3] = (byte)(destinationPort & 0xFF);
    result[4] = (byte)((length & 0xFF00) >> 8);
    result[5] = (byte)(length & 0xFF);
    result[6] = (byte)((checksum & 0xFF00) >> 8);
    result[7] = (byte)(checksum & 0xFF);

    System.arraycopy(upperLayerData, 0, result, 8, upperLayerData.length);

    return result;
  }

  /** Calculates the checksum for this packet */
  public void calculateChecksum(InetAddress sourceAddress, InetAddress destinationAddress)
  {
    int result = 0;

    // start with pseudo header
    byte[] addr = sourceAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    addr = destinationAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    // UDP Protocol identifier
    result += IPTunnelConstants.PROTOCOL_TYPE_UDP;

    // packet length
    result += getSize();
    // end of pseudoheader

    // UDP header
    result += sourcePort;

    result += destinationPort;

    result += getSize();

    // add data
    for (int i = 0; i < upperLayerData.length / 2; i++)
    {
      result += ((upperLayerData[i * 2] & 0xFF) << 8) + (upperLayerData[i * 2 + 1] & 0xFF);
    }
    // add last odd byte
    if (upperLayerData.length % 2 == 1)
    {
      result += (upperLayerData[upperLayerData.length - 1] & 0xFF) << 8;
    }

    // treat overflow
    while (result > 0xFFFF)
    {
      result = (result & 0xFFFF) + ((result & 0xFFFF0000) >> 16);
    }

    // build one's complement
    checksum = (result ^ 0xFFFF) & 0xFFFF;

    if (checksum == 0)
    {
      checksum = 0xFFFF;
    }

    checksumValid = true;
  }

  /** Calculates the checksum over the byte array representation of this packet (should always be 0) */
  public int calculateChecksum(InetAddress sourceAddress, InetAddress destinationAddress, byte[] data)
  {
    int result = 0;

    // start with pseudo header
    byte[] addr = sourceAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    addr = destinationAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    // UDP Protocol identifier
    result += IPTunnelConstants.PROTOCOL_TYPE_UDP;

    // UDP TCP packet size
    result += data.length;
    // end of pseudoheader

    for (int i = 0; i < data.length / 2; i++)
    {
      result += ((data[i * 2] & 0xFF) << 8) + (data[i * 2 + 1] & 0xFF);
    }
    // add last odd byte if existing
    if (data.length % 2 == 1)
    {
      result += (data[data.length - 1] & 0xFF) << 8;
    }

    // treat overflow
    while (result > 0xFFFF)
    {
      result = (result & 0xFFFF) + ((result & 0xFFFF0000) >> 16);
    }

    // build one's complement
    return (result ^ 0xFFFF) & 0xFFFF;
  }

  /** Retrieves the checksum for this packet */
  public int getChecksum()
  {
    return checksum;
  }

  /** Retrieves the upper layer data encapsulated in this packet */
  public byte[] getUpperLayerData()
  {
    return upperLayerData;
  }

  /** Retrieves the source port */
  public int getSourcePort()
  {
    return sourcePort;
  }

  /** Retrieves the destination port */
  public int getDestinationPort()
  {
    return destinationPort;
  }

  /** Retrieves the packet size as byte array */
  public int getSize()
  {
    return 8 + upperLayerData.length;
  }

  /** Retrieves the associated lower layer packet */
  public IPPacket getIPPacket()
  {
    return ipPacket;
  }

  /** Sets the associated lower layer packet */
  public void setIPPacket(IPPacket ipPacket)
  {
    this.ipPacket = ipPacket;
  }

  /** Retrieves if the checksum is valid for this packet */
  public boolean isValidChecksum()
  {
    return checksumValid;
  }

  /** Retrieves if a checksum was calculated by the sender of this packet */
  public boolean hasChecksum()
  {
    return checksum != 0;
  }

}

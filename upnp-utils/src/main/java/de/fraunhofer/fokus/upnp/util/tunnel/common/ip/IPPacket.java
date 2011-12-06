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
 * This class is used to wrap one IP packet.
 * 
 * @author Alexander Koenig
 */
public class IPPacket
{
  private int         version;

  private int         IHL;

  private int         typeOfService;

  // private int precedence;
  // private boolean delayFlag;
  // private boolean throughputFlag;
  // private boolean reliabilityFlag;

  private int         totalLength;

  private int         identification;

  private boolean     dontFragmentFlag;

  private boolean     moreFragmentsFlag;

  private int         fragmentOffset;

  private int         timeToLive;

  private int         protocol;

  private int         headerChecksum;

  private InetAddress sourceAddress;

  private InetAddress destinationAddress;

  private byte[]      options;

  private byte[]      upperLayerData;

  private boolean     checksumValid;

  /**
   * Creates a new instance of IPPacket
   * 
   * @param protocol
   *          The upper layer protocol
   * @param sourceAddress
   *          The source address for the packet
   * @param destinationAddress
   *          The destination address for the packet
   * @param upperLayerData
   *          The upper layer data
   */
  public IPPacket(int protocol, InetAddress sourceAddress, InetAddress destinationAddress, byte[] upperLayerData)
  {
    version = 4;
    IHL = 5;
    typeOfService = 0;
    totalLength = 20 + upperLayerData.length;
    // will be set when the packet is sent
    this.identification = 0;
    dontFragmentFlag = true;
    moreFragmentsFlag = false;
    fragmentOffset = 0;
    timeToLive = 16;
    this.protocol = protocol;
    this.sourceAddress = sourceAddress;
    this.destinationAddress = destinationAddress;
    options = null;
    this.upperLayerData = upperLayerData;
    headerChecksum = 0;
  }

  /**
   * Creates a new instance of IPPacket from a byte array
   * 
   * @param packetData
   *          The byte array that forms the IP packet
   */
  public IPPacket(byte[] packetData)
  {
    // rip byte array apart
    version = (packetData[0] & 0xF0) >> 4;
    IHL = packetData[0] & 0xF;
    typeOfService = packetData[1];
    // precedence = ((int)packetData[1] & 0xE0) >> 5;
    // delayFlag = ((int)packetData[1] & 0x10) != 0;
    // throughputFlag = ((int)packetData[1] & 0x8) != 0;
    // delayFlag = ((int)packetData[1] & 0x4) != 0;
    totalLength = packetData[2] * 256 + packetData[3];
    identification = packetData[4] * 256 + packetData[5];
    dontFragmentFlag = (packetData[6] & 0x40) != 0;
    moreFragmentsFlag = (packetData[6] & 0x20) != 0;
    fragmentOffset = (packetData[6] & 0x1F) * 256 + packetData[7];
    timeToLive = packetData[8];
    protocol = packetData[9];
    headerChecksum = packetData[10] * 256 + packetData[11];
    byte[] addr = new byte[4];
    try
    {
      System.arraycopy(packetData, 12, addr, 0, 4);
      sourceAddress = InetAddress.getByAddress(addr);
      System.arraycopy(packetData, 16, addr, 0, 4);
      destinationAddress = InetAddress.getByAddress(addr);
    } catch (Exception ex)
    {
    }
    // copy options
    if (IHL > 5)
    {
      options = new byte[IHL * 4 - 20];
      System.arraycopy(packetData, 20, options, 0, options.length);
    } else
    {
      options = null;
    }
    // check checksum

    byte[] header = new byte[IHL * 4];
    System.arraycopy(packetData, 0, header, 0, IHL * 4);

    checksumValid = calculateChecksum(header) == 0;

    // copy data
    upperLayerData = new byte[packetData.length - IHL * 4];
    System.arraycopy(packetData, IHL * 4, upperLayerData, 0, upperLayerData.length);
  }

  /** Creates the byte array containing the IP packet */
  public byte[] toByteArray()
  {
    byte[] result = new byte[20 + upperLayerData.length];
    result[0] = (byte)(0x40 + IHL);
    result[1] = (byte)typeOfService;
    result[2] = (byte)((totalLength & 0xFF00) >> 8);
    result[3] = (byte)(totalLength & 0xFF);

    result[4] = (byte)((identification & 0xFF00) >> 8);
    result[5] = (byte)identification;
    result[6] =
      (byte)((dontFragmentFlag ? 0x40 : 0) + (moreFragmentsFlag ? 0x20 : 0) + ((fragmentOffset & 0x1F00) >> 8));
    result[7] = (byte)(fragmentOffset & 0xFF);

    result[8] = (byte)timeToLive;
    result[9] = (byte)protocol;
    result[10] = (byte)((headerChecksum & 0xFF00) >> 8);
    result[11] = (byte)(headerChecksum & 0xFF);

    System.arraycopy(sourceAddress.getAddress(), 0, result, 12, 4);

    System.arraycopy(destinationAddress.getAddress(), 0, result, 16, 4);

    System.arraycopy(upperLayerData, 0, result, 20, upperLayerData.length);

    return result;
  }

  /** Calculates the checksum for the IP packet. */
  public void calculateChecksum()
  {
    int result = 0;
    result += (version << 12) + (IHL << 8) + typeOfService;
    result += totalLength;

    result += identification;
    result += dontFragmentFlag ? 0x4000 : 0;
    result += moreFragmentsFlag ? 0x2000 : 0;
    result += fragmentOffset;

    result += (timeToLive << 8) + protocol;

    byte[] addr = sourceAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    addr = destinationAddress.getAddress();
    result += ((addr[0] & 0xFF) << 8) + (addr[1] & 0xFF);
    result += ((addr[2] & 0xFF) << 8) + (addr[3] & 0xFF);

    if (options != null)
    {
      // options are always 32 bit aligned
      for (int i = 0; i < options.length / 2; i++)
      {
        result += (options[i * 2] << 8) + options[i * 2 + 1];
        // catch overflow
        if (result > 0xFFFF)
        {
          result = (result & 0xFFFF) + 1;
        }
      }
    }

    // treat overflow
    while (result > 0xFFFF)
    {
      result = (result & 0xFFFF) + ((result & 0xFFFF0000) >> 16);
    }

    // System.out.println("Result after one's complement is "+Integer.toHexString((result ^ 0xFFFF)
    // & 0xFFFF));
    // build one's complement
    headerChecksum = (result ^ 0xFFFF) & 0xFFFF;
    checksumValid = true;
  }

  /**
   * Calculates the checksum over the byte array representation of the packet header (should always
   * be 0)
   */
  public int calculateChecksum(byte[] header)
  {
    int result = 0;
    for (int i = 0; i < header.length / 2; i++)
    {
      result += ((header[i * 2] & 0xFF) << 8) + (header[i * 2 + 1] & 0xFF);
    }

    // treat overflow
    while (result > 0xFFFF)
    {
      result = (result & 0xFFFF) + ((result & 0xFFFF0000) >> 16);
    }

    // build one's complement
    return (result ^ 0xFFFF) & 0xFFFF;
  }

  public InetAddress getSourceAddress()
  {
    return sourceAddress;
  }

  public void setSourceAddress(InetAddress sourceAddress)
  {
    this.sourceAddress = sourceAddress;
  }

  public InetAddress getDestinationAddress()
  {
    return destinationAddress;
  }

  public int getTimeToLive()
  {
    return timeToLive;
  }

  public void setTimeToLive(int ttl)
  {
    timeToLive = ttl;
  }

  /** Retrieves the upper layer protocol encapsulated in this packet */
  public int getProtocol()
  {
    return protocol;
  }

  /** Retrieves the header checksum for this packet */
  public int getHeaderChecksum()
  {
    return headerChecksum;
  }

  /** Retrieves if the checksum is valid for this packet */
  public boolean isValidChecksum()
  {
    return checksumValid;
  }

  /** Retrieves the header length */
  public int getHeaderLength()
  {
    return IHL;
  }

  /** Retrieves the upper layer data encapsulated in this packet */
  public byte[] getUpperLayerData()
  {
    return upperLayerData;
  }

  /** Retrieves the identification */
  public int getIdentification()
  {
    return identification;
  }

  /** Sets the identification */
  public void setIdentification(int identification)
  {
    this.identification = identification % 65536;
  }

}

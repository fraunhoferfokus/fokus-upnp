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
 * This class represents a TCP packet.
 * 
 * @author Alexander Koenig
 */
public class TCPPacket
{
  /** Reference to outer IP packet */
  private IPPacket ipPacket;

  private int      sourcePort;

  private int      destinationPort;

  private long     sequenceNumber        = -1;

  private long     acknowledgementNumber = -1;

  private int      headerLength;

  private boolean  urgentFlag            = false;

  private boolean  acknowledgeFlag       = false;

  private boolean  pushFlag              = false;

  private boolean  resetFlag             = false;

  private boolean  synchronizationFlag   = false;

  private boolean  finishFlag            = false;

  private int      windowSize            = 0;

  private int      checksum              = 0;

  private int      urgentPointer         = 0;

  private byte[]   upperLayerData        = null;

  private byte[]   byteRepresentation    = null;

  private int      maximumPayloadSize    = 0;

  private long     lastSendTime          = 0;

  private long     creationTime          = System.currentTimeMillis();

  private int      sendCount             = 0;

  private int      calculatedChecksum    = 0;

  private boolean  checksumValid         = false;

  /**
   * Creates a new instance of TCPPacket
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param sequenceNumber
   *          Sequence number for the first byte of the upper layer data
   * @param acknowledgementNumber
   *          Expected sequence number for next received packet
   * @param windowSize
   *          Number of bytes that can be received without receiver buffer overflow
   */
  public TCPPacket(int sourcePort,
    int destinationPort,
    long sequenceNumber,
    long acknowledgementNumber,
    int windowSize,
    byte[] upperLayerData)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;

    this.sequenceNumber = sequenceNumber;
    this.acknowledgementNumber = acknowledgementNumber;

    headerLength = 5;
    acknowledgeFlag = true;
    // set push flag for packets with upper layer data
    pushFlag = upperLayerData != null;

    this.windowSize = windowSize;

    // upper layer data
    this.upperLayerData = upperLayerData;
  }

  /**
   * Creates an initial SYN response packet
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param sequenceNumber
   *          Sequence number for the SYN flag
   * @param acknowledgementNumber
   *          Expected sequence number for next received packet
   * @param windowSize
   *          Number of bytes that can be received without receiver buffer overflow
   * @param maximumPayloadSize
   *          Maximum payload size usable for this socket
   */
  public TCPPacket(int sourcePort,
    int destinationPort,
    long sequenceNumber,
    long acknowledgementNumber,
    int windowSize,
    int maximumPayloadSize)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;

    this.sequenceNumber = sequenceNumber;
    this.acknowledgementNumber = acknowledgementNumber;

    headerLength = 5 + (maximumPayloadSize > 0 ? 1 : 0);
    acknowledgeFlag = true;
    synchronizationFlag = true;
    this.windowSize = windowSize;

    this.maximumPayloadSize = maximumPayloadSize;
  }

  /**
   * Creates an initial SYN request packet
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param sequenceNumber
   *          Sequence number for the SYN flag
   * @param windowSize
   *          Number of bytes that can be received without receiver buffer overflow
   * @param maximumPayloadSize
   *          Maximum sequence size usable for this socket
   */
  public TCPPacket(int sourcePort, int destinationPort, long sequenceNumber, int windowSize, int maximumPayloadSize)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;

    this.sequenceNumber = sequenceNumber;
    this.acknowledgementNumber = 0;

    headerLength = 5 + (maximumPayloadSize > 0 ? 1 : 0);
    synchronizationFlag = true;
    this.windowSize = windowSize;

    this.maximumPayloadSize = maximumPayloadSize;
  }

  /**
   * Creates a FIN packet
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param sequenceNumber
   *          Sequence number for the SYN flag
   * @param acknowledgementNumber
   *          Expected sequence number for next received packet
   * @param windowSize
   *          Number of bytes that can be received without receiver buffer overflow
   */
  public TCPPacket(int sourcePort, int destinationPort, long sequenceNumber, long acknowledgementNumber, int windowSize)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;

    this.sequenceNumber = sequenceNumber;
    this.acknowledgementNumber = acknowledgementNumber;

    headerLength = 5;
    acknowledgeFlag = true;
    finishFlag = true;
    this.windowSize = windowSize;
  }

  /**
   * Creates a RST packet
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param sequenceNumber
   *          Sequence number for the SYN flag
   */
  public TCPPacket(int sourcePort, int destinationPort, long sequenceNumber)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;

    this.sequenceNumber = sequenceNumber;
    this.acknowledgementNumber = 0;

    headerLength = 5;
    resetFlag = true;
  }

  /**
   * Creates a new instance of TCPPacket from the corresponding byte array
   * 
   * @param packetData
   *          The byte array that forms the TCP packet
   */
  public TCPPacket(IPPacket ipPacket, byte[] packetData)
  {
    this.ipPacket = ipPacket;

    byteRepresentation = packetData;

    sourcePort = ((packetData[0] & 0xFF) << 8) + (packetData[1] & 0xFF);
    destinationPort = ((packetData[2] & 0xFF) << 8) + (packetData[3] & 0xFF);

    sequenceNumber =
      (((long)packetData[4] & 0xFF) << 24) + (((long)packetData[5] & 0xFF) << 16) +
        (((long)packetData[6] & 0xFF) << 8) + ((long)packetData[7] & 0xFF);

    acknowledgementNumber =
      (((long)packetData[8] & 0xFF) << 24) + (((long)packetData[9] & 0xFF) << 16) +
        (((long)packetData[10] & 0xFF) << 8) + ((long)packetData[11] & 0xFF);

    headerLength = (packetData[12] & 0xF0) >> 4;
    urgentFlag = (packetData[13] & 0x20) != 0;
    acknowledgeFlag = (packetData[13] & 0x10) != 0;
    pushFlag = (packetData[13] & 0x8) != 0;
    resetFlag = (packetData[13] & 0x4) != 0;
    synchronizationFlag = (packetData[13] & 0x2) != 0;
    finishFlag = (packetData[13] & 0x1) != 0;

    windowSize = ((packetData[14] & 0xFF) << 8) + (packetData[15] & 0xFF);
    checksum = ((packetData[16] & 0xFF) << 8) + (packetData[17] & 0xFF);
    urgentPointer = ((packetData[18] & 0xFF) << 8) + (packetData[19] & 0xFF);

    // parse options
    if (headerLength > 5)
    {
      // create options byte array
      byte[] options = new byte[(headerLength - 5) * 4];
      System.arraycopy(packetData, 20, options, 0, options.length);

      // extract options
      int offset = 0;
      while (offset < options.length)
      {
        int optionType = options[offset] & 0xFF;
        // end of options
        if (optionType == 0)
        {
          continue;
        }
        // nop is handled automatically

        // maximum payload size
        if (optionType == 2)
        {
          maximumPayloadSize = ((options[offset + 2] & 0xFF) << 8) + (options[offset + 3] & 0xFF);
          offset += 3;
        }

        // other options, unrecognized
        if (optionType > 2)
        {
          // try to find size of option
          if (offset + 1 < options.length)
          {
            int optionSize = options[offset + 1] & 0xFF;
            // discard option
            offset += optionSize - 1;
          }
        }

        offset++;
      }
    }

    if (packetData.length - headerLength * 4 > 0)
    {
      upperLayerData = new byte[packetData.length - headerLength * 4];
      System.arraycopy(packetData, headerLength * 4, upperLayerData, 0, upperLayerData.length);
    } else
    {
      upperLayerData = null;
    }

    checksumValid = calculateChecksum(ipPacket.getSourceAddress(), ipPacket.getDestinationAddress(), packetData) == 0;
  }

  /** Retrieves the byte array representation of the TCP packet */
  public byte[] toByteArray()
  {
    byte[] result = new byte[getSize()];

    result[0] = (byte)((sourcePort & 0xFF00) >> 8);
    result[1] = (byte)(sourcePort & 0xFF);
    result[2] = (byte)((destinationPort & 0xFF00) >> 8);
    result[3] = (byte)(destinationPort & 0xFF);

    result[4] = (byte)((sequenceNumber & 0xFF000000) >> 24);
    result[5] = (byte)((sequenceNumber & 0xFF0000) >> 16);
    result[6] = (byte)((sequenceNumber & 0xFF00) >> 8);
    result[7] = (byte)(sequenceNumber & 0xFF);

    result[8] = (byte)((acknowledgementNumber & 0xFF000000) >> 24);
    result[9] = (byte)((acknowledgementNumber & 0xFF0000) >> 16);
    result[10] = (byte)((acknowledgementNumber & 0xFF00) >> 8);
    result[11] = (byte)(acknowledgementNumber & 0xFF);

    result[12] = (byte)(headerLength << 4);
    result[13] = (byte)0;
    result[13] |= urgentFlag ? 0x20 : 0;
    result[13] |= acknowledgeFlag ? 0x10 : 0;
    result[13] |= pushFlag ? 0x8 : 0;
    result[13] |= resetFlag ? 0x4 : 0;
    result[13] |= synchronizationFlag ? 0x2 : 0;
    result[13] |= finishFlag ? 0x1 : 0;

    result[14] = (byte)((windowSize & 0xFF00) >> 8);
    result[15] = (byte)(windowSize & 0xFF);

    result[16] = (byte)((checksum & 0xFF00) >> 8);
    result[17] = (byte)(checksum & 0xFF);

    result[18] = (byte)((urgentPointer & 0xFF00) >> 8);
    result[19] = (byte)(urgentPointer & 0xFF);

    // add options
    if (maximumPayloadSize != 0)
    {
      result[20] = (byte)0x2;
      result[21] = (byte)0x4;
      result[22] = (byte)((maximumPayloadSize & 0xFF00) >> 8);
      result[23] = (byte)(maximumPayloadSize & 0xFF);
    }

    if (upperLayerData != null)
    {
      System.arraycopy(upperLayerData, 0, result, headerLength * 4, upperLayerData.length);
    }

    return result;
  }

  public String flagsToString()
  {
    String result = "";
    result += synchronizationFlag ? "SYN " : "";
    result += finishFlag ? "FIN " : "";
    result += pushFlag ? "PSH " : "";
    result += resetFlag ? "RST " : "";
    result += acknowledgeFlag ? "ACK " : "";

    return result;
  }

  public void toDebugOut()
  {
    System.out.println("TCPPacket:");
    System.out.println("SEQ  : " + sequenceNumber);
    System.out.println("ACK  : " + acknowledgementNumber);
    System.out.print("Flags: ");
    System.out.print(synchronizationFlag ? "SYN " : "");
    System.out.print(finishFlag ? "FIN " : "");
    System.out.print(pushFlag ? "PSH " : "");
    System.out.print(resetFlag ? "RST " : "");
    System.out.print(acknowledgeFlag ? "ACK " : "");
    System.out.println();
  }

  /** Checks whether two received TCP packets are equal */
  public boolean equals(Object packet)
  {
    return packet instanceof TCPPacket && checksum == ((TCPPacket)packet).getChecksum() &&
      sequenceNumber == ((TCPPacket)packet).getSequenceNumber();
  }

  /** Returns an identifier string that uniquely identifies this packet. */
  public String getHashIdentifier()
  {
    return checksum + "" + sequenceNumber + "" + sourcePort;
  }

  /** Retrieves a hash code for this packet */
  public int hashCode()
  {
    return checksum;
  }

  /** Calculates the checksum for this packet. */
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

    // TCP Protocol identifier
    result += IPTunnelConstants.PROTOCOL_TYPE_TCP;

    // TCP packet size
    result += getSize();
    // end of pseudoheader

    // TCP header
    result += sourcePort;

    result += destinationPort;

    result += (sequenceNumber & 0xFFFF0000) >> 16;
    result += sequenceNumber & 0xFFFF;

    result += (acknowledgementNumber & 0xFFFF0000) >> 16;
    result += acknowledgementNumber & 0xFFFF;

    result += headerLength << 12;

    result += urgentFlag ? 0x20 : 0;
    result += acknowledgeFlag ? 0x10 : 0;
    result += pushFlag ? 0x8 : 0;
    result += resetFlag ? 0x4 : 0;
    result += synchronizationFlag ? 0x2 : 0;
    result += finishFlag ? 0x1 : 0;

    result += windowSize;

    result += urgentPointer;

    // add option
    if (maximumPayloadSize != 0)
    {
      result += 0x0204;
      result += maximumPayloadSize;
    }

    // add data
    if (upperLayerData != null)
    {
      for (int i = 0; i < upperLayerData.length / 2; i++)
      {
        result += ((upperLayerData[i * 2] & 0xFF) << 8) + (upperLayerData[i * 2 + 1] & 0xFF);
      }
      // add last odd byte
      if (upperLayerData.length % 2 == 1)
      {
        result += (upperLayerData[upperLayerData.length - 1] & 0xFF) << 8;
      }
    }

    // treat overflow
    while (result > 0xFFFF)
    {
      result = (result & 0xFFFF) + ((result & 0xFFFF0000) >> 16);
    }

    // build one's complement
    checksum = (result ^ 0xFFFF) & 0xFFFF;
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

    // TCP Protocol identifier
    result += IPTunnelConstants.PROTOCOL_TYPE_TCP;

    // TCP packet size
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

    calculatedChecksum = (result ^ 0xFFFF) & 0xFFFF;

    // build one's complement
    return (result ^ 0xFFFF) & 0xFFFF;
  }

  /** Retrieves the checksum for this packet */
  public int getChecksum()
  {
    return checksum;
  }

  /** Retrieves the checksum calculated for a received packet */
  public int getCalculatedChecksum()
  {
    return calculatedChecksum;
  }

  /**
   * Retrieves the byte array representation for a received packet. For created packets, this will
   * return null
   */
  public byte[] getByteRepresentation()
  {
    return byteRepresentation;
  }

  public boolean isConnectionRequest()
  {
    return hasSYNFlag() && !hasACKFlag();
  }

  public boolean isConnectionAccepted()
  {
    return hasSYNFlag() && hasACKFlag();
  }

  public boolean hasURGFlag()
  {
    return urgentFlag;
  }

  public boolean hasACKFlag()
  {
    return acknowledgeFlag;
  }

  public boolean hasPSHFlag()
  {
    return pushFlag;
  }

  public boolean hasRSTFlag()
  {
    return resetFlag;
  }

  public boolean hasSYNFlag()
  {
    return synchronizationFlag;
  }

  public boolean hasFINFlag()
  {
    return finishFlag;
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

  /** Retrieves the window size */
  public int getWindowSize()
  {
    return windowSize;
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

  /** Retrieves the packet size as byte array */
  public int getSize()
  {
    return 20 + (maximumPayloadSize != 0 ? 4 : 0) + (upperLayerData != null ? upperLayerData.length : 0);
  }

  /** Retrieves the sequence number increment for this packet */
  public int getSequenceNumberSpace()
  {
    return (hasSYNFlag() ? 1 : 0) + (hasFINFlag() ? 1 : 0) + (upperLayerData != null ? upperLayerData.length : 0);
  }

  /** Retrieves the maximum sequence size */
  public int getMaximumPayloadSize()
  {
    return maximumPayloadSize;
  }

  /** Retrieves the creation time */
  public long getCreationTime()
  {
    return creationTime;
  }

  /** Retrieves the last send time */
  public long getLastSendTime()
  {
    return lastSendTime;
  }

  /** Updates the last send time */
  public void setLastSendTime()
  {
    this.lastSendTime = System.currentTimeMillis();
  }

  /** Retrieves the number of send attempts */
  public long getSendCount()
  {
    return sendCount;
  }

  /** Updates the number of send attempts */
  public void incrementSendCount()
  {
    sendCount++;
  }

  /** Retrieves the sequence number */
  public long getSequenceNumber()
  {
    return sequenceNumber;
  }

  /** Retrieves the acknowledgement number */
  public long getAcknowledgementNumber()
  {
    return acknowledgementNumber;
  }

  /** Sets the acknowledgement number */
  public void setAcknowledgementNumber(long number)
  {
    acknowledgementNumber = number & 0xFFFFFFFF;
    acknowledgeFlag = true;
  }

  /** Retrieves if the checksum is valid for this packet */
  public boolean isValidChecksum()
  {
    return checksumValid;
  }

}

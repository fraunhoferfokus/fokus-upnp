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

import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.network.LSFHelper;

/**
 * This class encapsulates one packet for the SMEP (Simple Exchange Message Protocol, used in the Lightweight Sensor
 * Framework).
 * 
 * @author Alexander Koenig
 */
public class SMEPPacket
{
  /** Packet source address. Length is hardware dependent */
  private byte[]    sourceAddress;

  /** Packet destination address. Length is hardware dependent */
  private byte[]    destinationAddress;

  private int       sourcePort;

  private int       destinationPort;

  private byte[]    upperLayerData;

  private boolean   valid      = false;

  private Hashtable properties = new Hashtable();

  /**
   * Creates a new instance of SMEPPacket
   * 
   * @param sourcePort
   *          Source port for this packet
   * @param destinationPort
   *          Destination port for this packet
   * @param upperLayerData
   *          The upper layer data
   */
  public SMEPPacket(int sourcePort, int destinationPort, byte[] upperLayerData)
  {
    this.sourcePort = sourcePort;
    this.destinationPort = destinationPort;
    this.upperLayerData = upperLayerData;
    this.valid = true;
  }

  /**
   * Creates a new instance of SMEPPacket from the corresponding byte array
   * 
   * @param packetData
   *          The byte array that forms the UDP packet
   */
  public SMEPPacket(byte[] packetData)
  {
    if (packetData != null && packetData.length > 2)
    {
      destinationPort = packetData[0] & 0xFF;
      sourcePort = packetData[1] & 0xFF;

      upperLayerData = new byte[packetData.length - 2];
      System.arraycopy(packetData, 2, upperLayerData, 0, upperLayerData.length);
      this.valid = true;
    }
  }

  /**
   * Retrieves the byte array representation of the SMEP packet. This includes source and destination port, but not the
   * addresses.
   */
  public byte[] toByteArray()
  {
    byte[] result = new byte[2 + upperLayerData.length];

    result[0] = (byte)(destinationPort & 0xFF);
    result[1] = (byte)(sourcePort & 0xFF);

    System.arraycopy(upperLayerData, 0, result, 2, upperLayerData.length);

    return result;
  }

  public String toString()
  {
    return "From:" + LSFHelper.lsfAddressToInetAddress(sourceAddress).getHostAddress() + ":" + sourcePort + " To:" +
      LSFHelper.lsfAddressToInetAddress(destinationAddress).getHostAddress() + ":" + destinationPort + " Payload:" +
      upperLayerData.length + " bytes";
  }

  /** Retrieves the upper layer data encapsulated in this packet */
  public byte[] getUpperLayerData()
  {
    return upperLayerData;
  }

  /**
   * Retrieves the value of sourceAddress.
   * 
   * @return The value of sourceAddress
   */
  public byte[] getSourceAddress()
  {
    return sourceAddress;
  }

  /**
   * Retrieves the value of sourceAddress as int.
   */
  public int getSourceAddressAsInt()
  {
    return ByteArrayHelper.byteArrayToUInt16(sourceAddress, 0);
  }

  /**
   * Sets the new value for sourceAddress.
   * 
   * @param sourceAddress
   *          The new value for sourceAddress
   */
  public void setSourceAddress(byte[] sourceAddress)
  {
    this.sourceAddress = sourceAddress;
  }

  /**
   * Retrieves the value of destinationAddress.
   * 
   * @return The value of destinationAddress
   */
  public byte[] getDestinationAddress()
  {
    return destinationAddress;
  }

  /**
   * Retrieves the value of destinationAddress as int.
   */
  public int getDestinationAddressAsInt()
  {
    return ByteArrayHelper.byteArrayToUInt16(destinationAddress, 0);
  }

  /**
   * Sets the new value for destinationAddress.
   * 
   * @param destinationAddress
   *          The new value for destinationAddress
   */
  public void setDestinationAddress(byte[] destinationAddress)
  {
    this.destinationAddress = destinationAddress;
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
    return 2 + upperLayerData.length;
  }

  /**
   * Retrieves the value of properties.
   * 
   * @return The value of properties
   */
  public Hashtable getProperties()
  {
    return properties;
  }

  /**
   * Retrieves the value of valid.
   * 
   * @return The value of valid
   */
  public boolean isValid()
  {
    return valid;
  }
}

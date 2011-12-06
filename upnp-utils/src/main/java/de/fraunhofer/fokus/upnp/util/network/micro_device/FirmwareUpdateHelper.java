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
package de.fraunhofer.fokus.upnp.util.network.micro_device;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class provides constants and helper methods for sensor devices that can be updated using UDP packets.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class FirmwareUpdateHelper
{

  /**
   * Sends a pause command to the device.
   * 
   * 
   * @param socket
   * @param deviceAddress
   */
  public static void sendPauseCommand(DatagramSocket socket, InetAddress deviceAddress)
  {
    try
    {
      byte[] packetData = {
          ConfigurationConstants.PACKET_TYPE_FIRMWARE, ConfigurationConstants.FIRMWARE_TYPE_PAUSE
      };
      // send command
      DatagramPacket packet = new DatagramPacket(packetData, packetData.length, deviceAddress, ConfigurationConstants.FIRMWARE_CLIENT_PORT);
      socket.send(packet);
    } catch (Exception ex)
    {
    }
  }

  /**
   * Sends a sector read request to the device.
   * 
   * 
   * @param socket
   * @param deviceAddress
   * @param sectorAddress
   */
  public static void sendReadSector(DatagramSocket socket, InetAddress deviceAddress, int sectorAddress)
  {
    try
    {
      byte[] packetData =
        {
            ConfigurationConstants.PACKET_TYPE_FIRMWARE, ConfigurationConstants.FIRMWARE_TYPE_READ_SECTOR,
            (byte)((sectorAddress & 0xFF0000) >> 16), (byte)((sectorAddress & 0x00FF00) >> 8),
            (byte)((sectorAddress & 0x0000FF) >> 0)
        };
      // send command
      DatagramPacket packet = new DatagramPacket(packetData, packetData.length, deviceAddress, ConfigurationConstants.FIRMWARE_CLIENT_PORT);
      socket.send(packet);
    } catch (Exception ex)
    {
    }
  }

  /**
   * Sends a sector write command to the device.
   * 
   * 
   * @param socket
   * @param deviceAddress
   * @param sectorAddress
   * @param sectorData
   */
  public static void sendWriteSector(DatagramSocket socket,
    InetAddress deviceAddress,
    int sectorAddress,
    byte[] sectorData)
  {
    try
    {
      byte[] packetData = new byte[517];
      packetData[0] = ConfigurationConstants.PACKET_TYPE_FIRMWARE;
      packetData[1] = ConfigurationConstants.FIRMWARE_TYPE_WRITE_SECTOR;
      packetData[2] = (byte)((sectorAddress & 0xFF0000) >> 16);
      packetData[3] = (byte)((sectorAddress & 0x00FF00) >> 8);
      packetData[4] = (byte)(sectorAddress & 0x0000FF);

      System.arraycopy(sectorData, 0, packetData, 5, sectorData.length);

      // send command
      DatagramPacket packet = new DatagramPacket(packetData, packetData.length, deviceAddress, ConfigurationConstants.FIRMWARE_CLIENT_PORT);
      socket.send(packet);
    } catch (Exception ex)
    {
    }
  }

}

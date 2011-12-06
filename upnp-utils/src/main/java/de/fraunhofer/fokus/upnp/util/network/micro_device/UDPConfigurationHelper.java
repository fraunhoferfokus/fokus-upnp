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
 * This class provides constants and helper methods for devices that can be configured using UDP packets.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UDPConfigurationHelper
{

  public static final int UDPClientConfigPort = 1504;

  /**
   * Sends a configuration request to a destination address. All sensor devices are expected to answer this request with
   * their individual configuration.
   */
  public static void sendConfigurationRequest(DatagramSocket socket, InetAddress destinationAddress)
  {
    try
    {
      byte[] packetData = {
          ConfigurationConstants.PACKET_TYPE_CONFIG, ConfigurationConstants.CONFIG_TYPE_REQUEST_CLIENT_CONFIG
      };
      // send config request
      DatagramPacket packet =
        new DatagramPacket(packetData, packetData.length, destinationAddress, UDPClientConfigPort);
      socket.send(packet);
    } catch (Exception ex)
    {
    }
  }

  /**
   * Sends a reset command to the device.
   * 
   * 
   * @param socket
   * @param deviceAddress
   */
  public static void sendResetCommand(DatagramSocket socket, InetAddress deviceAddress, byte[] macAddress)
  {
    try
    {
      byte[] packetData = new byte[7];
      packetData[0] = ConfigurationConstants.PACKET_TYPE_RESET;
      for (int i = 0; i < macAddress.length; i++)
      {
        packetData[1 + i] = macAddress[i];
      }
      // send command
      DatagramPacket packet = new DatagramPacket(packetData, packetData.length, deviceAddress, UDPClientConfigPort);
      socket.send(packet);
    } catch (Exception ex)
    {
    }
  }

}

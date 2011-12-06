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

/**
 * This class contains constants that are needed for UDP configuration packets. This is used both by IP tunnels and
 * sensor devices.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class ConfigurationConstants
{
  public static final String FIRMWARE_EVENT_MULTICAST_ADDRESS      = "239.255.255.201";

  public static final int    FIRMWARE_CLIENT_PORT                 = 1505;

  public static final byte   PACKET_TYPE_TCP                       = 1;

  public static final byte   PACKET_TYPE_UDP                       = 2;

  public static final byte   PACKET_TYPE_CONFIG                    = 3;

  public static final byte   PACKET_TYPE_FIRMWARE                  = 4;

  public static final byte   PACKET_TYPE_RESET                     = 5;

  public static final byte   CONFIG_TYPE_CONFIG_TO_SERVER          = 1;

  public static final byte   CONFIG_TYPE_PING                      = 2;

  public static final byte   CONFIG_TYPE_CONFIG_RECEIVED_BY_SERVER = 3;

  public static final byte   CONFIG_TYPE_REQUEST_CLIENT_CONFIG     = 4;

  public static final byte   CONFIG_TYPE_CLIENT_CONFIG             = 5;

  public static final byte   CONFIG_TYPE_SET_CLIENT_CONFIG         = 6;

  public static final byte   CONFIG_TYPE_CLIENT_ID                 = 20;

  public static final byte   FIRMWARE_TYPE_READ_SECTOR             = 1;

  public static final byte   FIRMWARE_TYPE_READ_SECTOR_RESPONSE    = 2;

  public static final byte   FIRMWARE_TYPE_WRITE_SECTOR            = 6;

  public static final byte   FIRMWARE_TYPE_WRITE_SECTOR_RESPONSE   = 7;

  public static final byte   FIRMWARE_TYPE_PAUSE                   = 10;

  public static final byte   FIRMWARE_TYPE_PAUSE_OK                = 11;

  public static final byte   FIRMWARE_TYPE_BOOTLOADER_READY        = 12;

  public static final int    OPTION_TYPE_TARGET_MAC                = 1;

  public static final int    OPTION_TYPE_LOCAL_DHCP                = 10;

  public static final int    OPTION_TYPE_LOCAL_IP                  = 11;

  public static final int    OPTION_TYPE_LOCAL_NETMASK             = 12;

  public static final int    OPTION_TYPE_LOCAL_GATEWAY             = 13;

  public static final int    OPTION_TYPE_LOCAL_MAC                 = 14;

  public static final int    OPTION_TYPE_LOCAL_NAME                = 15;

  public static final int    OPTION_TYPE_HARDWARE_ID               = 16;

  public static final int    OPTION_TYPE_SOFTWARE_ID               = 17;

  public static final int    OPTION_TYPE_FIRMWARE_DATE             = 18;

  public static final int    OPTION_TYPE_FIRMWARE_NAME             = 19;

  /** Dongle constants */
  public static final int    OPTION_TYPE_SERVER_IP                 = 20;

  public static final int    OPTION_TYPE_SERVER_TCP_PORT           = 21;

  public static final int    OPTION_TYPE_SERVER_UDP_PORT           = 22;

  /** Sensor app constants */
  public static final int    OPTION_TYPE_SENSOR_NAME               = 30;

  public static final int    OPTION_TYPE_SENSOR_APPLICATION        = 31;

  public static final int    OPTION_TYPE_SENSOR_ID                 = 32;

  public static final int    HARDWARE_ID_DEMO_BOARD                = 1;

  public static final int    HARDWARE_ID_SENSOR_NODE               = 2;

  public static final int    SOFTWARE_ID_COMMON_SENSOR             = 1;

  public static final int    SOFTWARE_ID_DONGLE                    = 2;

}

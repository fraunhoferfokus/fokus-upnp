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

/**
 * This class holds constants for IP tunnel connections.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelConstants
{
  /**
   * IPTunnel header size
   * 
   * 0..9 Magic number (0xFF) 10..13 Size 14 Packet type
   * 
   */
  public static final int  IP_TUNNEL_HEADER_SIZE                 = 15;

  public static final byte CONFIG_TYPE_CONFIG_TO_SERVER          = 1;

  public static final byte CONFIG_TYPE_PING                      = 2;

  public static final byte CONFIG_TYPE_CONFIG_RECEIVED_BY_SERVER = 3;

  public static final byte CONFIG_TYPE_CLIENT_CONFIG             = 5;

  public static final byte CONFIG_TYPE_CLIENT_ID                 = 20;

  public static final byte PACKET_TYPE_TCP                       = 1;

  public static final byte PACKET_TYPE_UDP                       = 2;

  public static final byte PACKET_TYPE_CONFIG                    = 3;

  /** Protocol type for TCP in IP packets (0x06) */
  public static final int  PROTOCOL_TYPE_TCP                     = 0x06;

  /** Protocol type for UDP in IP packets (0x11) */
  public static final int  PROTOCOL_TYPE_UDP                     = 0x11;

  /** Timeout for clients in the reconnection table */
  public static final long RECONNECTION_TIMEOUT                  = 300000;

  /** Time to wait before sending acknowledgements */
  public static final long TCP_ACKNOWLEDGEMENT_WAIT_TIME         = 500;

  /** Maximum default payload size for TCP packets (536 bytes) */
  public static final int  TCP_DEFAULT_PAYLOAD_SIZE              = 536;

  /** Maximum default segment size for TCP packets (556 bytes) */
  public static final int  TCP_DEFAULT_SEGMENT_SIZE              = 556;

  /** Initial congestion window (4096 bytes) */
  public static final int  TCP_INITIAL_CONGESTION_WINDOW         = 4096;

  /** Time to wait before the socket is closed due to an unacknowledged packet (2 minutes) */
  public static final long TCP_PACKET_ERROR_TIME                 = 120000;

  /** Time to wait before probing the send window (2 minutes) */
  public static final long TCP_PERSISTENCE_WAIT_TIME             = 120000;

  /** Time interval for ping packets */
  public static final long TCP_PING_TIME                         = 10000;

  /** Timeout for termination of the tunnel socket */
  public static final long TERMINATION_TIMEOUT                   = 600000;

  /** Delay after each packet */
  public static final long TUNNEL_PACKET_DELAY                   = 100;

  /** Maximum size for UDP packets */
  public static final int  UDP_BUFFER_SIZE                       = 1500;
}

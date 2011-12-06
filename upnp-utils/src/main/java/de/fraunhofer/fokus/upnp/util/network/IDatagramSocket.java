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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

/**
 * This is an interface for sending packets over a datagram socket. May be used to simplify access
 * to socket classes that do not extend DatagramSocket.
 * 
 * @author Alexander Koenig
 * 
 */
public interface IDatagramSocket
{

  /**
   * Sends a data packet over the socket.
   * 
   * @param packet
   *          The packet that should be send
   */
  public void send(DatagramPacket packet) throws IOException;

  /**
   * Sets a socket timeout for packet reception.
   * 
   * @param timeout
   *          The requested timeout in ms
   */
  public void setSoTimeout(int timeout) throws IOException;

  /**
   * Retrieves the socket timeout for packet reception.
   * 
   * @return The timeout in ms
   */
  public int getSoTimeout() throws IOException;

  /**
   * Sends a packet over the socket with a fake source address
   * 
   * @param sourceAddress
   *          The source address that should be used
   * @param packet
   *          The packet that should be send
   * 
   */
  public void send(DatagramPacket packet, InetSocketAddress sourceAddress) throws IOException;

  /**
   * Tries to receive a packet.
   * 
   * @param p
   *          Holds the received packet
   * 
   */
  public void receive(DatagramPacket p) throws IOException;

}

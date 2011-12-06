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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * This class wraps a Java datagram socket as IDatagramSocket.
 * 
 * @author Alexander Koenig
 * 
 */
public class DatagramSocketWrapper implements IDatagramSocket
{

  private DatagramSocket socket;

  /**
   * Creates a new socket wrapper.
   * 
   * @param socket
   *          The socket that should be wrapped.
   */
  public DatagramSocketWrapper(DatagramSocket socket)
  {
    this.socket = socket;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IDatagramSocket#send(java.net.DatagramPacket)
   */
  public void send(DatagramPacket packet) throws IOException
  {
    socket.send(packet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IDatagramSocket#setSoTimeout(int)
   */
  public void setSoTimeout(int timeout) throws IOException
  {
    socket.setSoTimeout(timeout);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.network.IDatagramSocket#getSoTimeout()
   */
  public int getSoTimeout() throws IOException
  {
    return socket.getSoTimeout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IDatagramSocket#receive(java.net.DatagramPacket)
   */
  public void receive(DatagramPacket packet) throws IOException
  {
    socket.receive(packet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IDatagramSocket#send(java.net.DatagramPacket,
   *      java.net.InetSocketAddress)
   */
  public void send(DatagramPacket packet, InetSocketAddress sourceAddress) throws IOException
  {
    throw new IOException("Not supported");
  }

}

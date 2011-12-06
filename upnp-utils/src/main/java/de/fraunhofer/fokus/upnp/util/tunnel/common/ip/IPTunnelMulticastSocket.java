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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author Alexander Koenig
 */
public class IPTunnelMulticastSocket extends IPTunnelDatagramSocket
{
  /** Multicast addresses joined by this socket */
  private Hashtable joinedMulticastAddressTable = new Hashtable();

  /**
   * Creates a new instance of IPTunnelMulticastSocket and binds it to the specified port and the
   * local IP address of the network interface
   * 
   */
  protected IPTunnelMulticastSocket(IPTunnelSocketFactory ipTunnelSocketFactory, int port) throws IOException
  {
    super(ipTunnelSocketFactory, port);
  }

  public void joinGroup(InetAddress mcastaddr)
  {
    if (!joinedMulticastAddressTable.contains(mcastaddr))
    {
      joinedMulticastAddressTable.put(mcastaddr, mcastaddr);
    }
  }

  public void leaveGroup(InetAddress mcastaddr)
  {
    joinedMulticastAddressTable.remove(mcastaddr);
  }

  /** Closes the socket */
  public void close()
  {
    closeSocket = true;
    ipTunnelSocketFactory.removeIPTunnelMulticastSocket(this);
  }

  /** Checks if this socket has joined a certain multicast address */
  public boolean isJoinedMulticastAddress(InetAddress address)
  {
    return joinedMulticastAddressTable.containsKey(address);
  }

  /** Retrieves a list of all multicast addresses this socket has joined */
  protected Enumeration getJoinedMulticastAddresses()
  {
    return joinedMulticastAddressTable.elements();
  }

}

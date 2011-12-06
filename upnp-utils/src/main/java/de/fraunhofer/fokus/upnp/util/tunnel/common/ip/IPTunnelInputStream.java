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
import java.io.InputStream;

/**
 * This is the input stream provided by IP tunnel sockets.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelInputStream extends InputStream
{
  /** Associated tunnel socket */
  private IPTunnelSocket tunnelSocket;

  /** Creates a new instance of IPTunnelOutputStream */
  public IPTunnelInputStream(IPTunnelSocket tunnelSocket)
  {
    this.tunnelSocket = tunnelSocket;
  }

  public void close() throws IOException
  {
  }

  public int available() throws IOException
  {
    return tunnelSocket.getAvailableBytes();
  }

  public int read() throws IOException
  {
    return tunnelSocket.readByteFromTunnel();
  }

  public int read(byte[] data) throws IOException
  {
    return tunnelSocket.readBytesFromTunnel(data, 0, data.length);
  }

  public int read(byte[] data, int offset, int length) throws IOException
  {
    return tunnelSocket.readBytesFromTunnel(data, offset, length);
  }

}

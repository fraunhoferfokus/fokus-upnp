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
import java.io.OutputStream;

/**
 * This is the output stream provided by IP tunnel sockets.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelOutputStream extends OutputStream
{
  /** Associated tunnel socket */
  private IPTunnelSocket tunnelSocket;

  /** Creates a new instance of IPTunnelOutputStream */
  public IPTunnelOutputStream(IPTunnelSocket tunnelSocket)
  {
    this.tunnelSocket = tunnelSocket;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException
  {
    tunnelSocket.sendBytesFromOutputStream(new byte[] {
      (byte)b
    }, 0, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#write(byte[])
   */
  public void write(byte[] b) throws IOException
  {
    tunnelSocket.sendBytesFromOutputStream(b, 0, b.length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int off, int len) throws IOException
  {
    tunnelSocket.sendBytesFromOutputStream(b, off, len);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException
  {
    tunnelSocket.sendCurrentSegment();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException
  {
  }

}

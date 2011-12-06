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

/**
 * This class holds one buffer to receive datagram packets. Saves the need to allocate the buffer on
 * the stack which is really inefficient for many fast requests.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SocketBufferEntry
{

  private boolean used   = false;

  private byte[]  buffer = new byte[SocketHelper.BUFFER_SIZE];

  /**
   * Retrieves the used.
   * 
   * @return The used
   */
  public boolean isUsed()
  {
    return used;
  }

  /**
   * Sets the used.
   * 
   * @param used
   *          The new value for used
   */
  public void setUsed(boolean used)
  {
    this.used = used;
  }

  /**
   * Retrieves the buffer.
   * 
   * @return The buffer
   */
  public byte[] getBuffer()
  {
    return buffer;
  }

}

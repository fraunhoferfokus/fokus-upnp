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
package de.fraunhofer.fokus.upnp.gateway.common;

import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used as template for classes that create specific UPnP tunnels.
 * 
 * @author Alexander Koenig
 */
public class TemplateTunnelManagement
{

  private static int                      START_ID = 0;

  /** Reference to message forwarder */
  protected MessageForwarder              messageForwarder;

  /** Unique ID for this tunnel */
  protected byte[]                        id;

  /** Optional listener for client events */
  protected IForwarderModuleEventListener forwarderModuleEventListener;

  /** Virtual inet address used in the tunnel */
  protected InetAddress                   ipTunnelInetAddress;

  /**
   * Creates a new instance of TemplateTunnelManagement.
   * 
   * @param messageForwarder
   * @param ipTunnelInetAddress
   */
  public TemplateTunnelManagement(MessageForwarder messageForwarder, InetAddress ipTunnelInetAddress)
  {
    this.messageForwarder = messageForwarder;
    this.ipTunnelInetAddress = ipTunnelInetAddress;

    // must be 5 bytes to allow transformation into base32
    id = new byte[5];
    // use IP address for ID
    System.arraycopy(IPHelper.getLocalHostAddress().getAddress(), 0, id, 0, 4);
    // make unique using counter
    id[4] = (byte)START_ID;
    START_ID = (START_ID + 1) % 256;
  }

  /**
   * Sets the clientEventsListener.
   * 
   * @param clientEventsListener
   *          The new value for clientEventsListener
   */
  public void setForwarderModuleEventListener(IForwarderModuleEventListener eventListener)
  {
    this.forwarderModuleEventListener = eventListener;
  }

  /** Terminates the forwarder module for a specific tunnel. */
  public void terminate()
  {
  }

}

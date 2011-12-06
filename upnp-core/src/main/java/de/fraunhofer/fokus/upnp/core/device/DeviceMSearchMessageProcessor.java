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
package de.fraunhofer.fokus.upnp.core.device;

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.core.MSearchMessageProcessor;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageProcessorResult;

/**
 * This class processes M-SEARCH messages and creates the appropriate responses.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class DeviceMSearchMessageProcessor extends MSearchMessageProcessor
{
  private Device device;

  /**
   * Creates a new instance of DeviceMSearchMessageProcessor.
   * 
   * @param device
   */
  public DeviceMSearchMessageProcessor(Device device)
  {
    this.device = device;
  }

  /**
   * Creates all response messages for a device for a received M-SEARCH message.
   * 
   * @param httpServerAddress
   *          Address of the server that should receive resulting GET requests
   * @param message
   *          The received message
   * 
   * @return All generated response messages as a vector of message strings
   */
  public MSearchMessageProcessorResult processMessage(InetSocketAddress httpServerAddress, String message)
  {
    if (device != null)
    {
      return processMessage(device, httpServerAddress, message, device.getIPVersion());
    }
    return null;
  }

}

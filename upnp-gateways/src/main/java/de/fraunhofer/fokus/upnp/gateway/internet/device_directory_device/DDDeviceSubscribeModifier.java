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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.http.IHTTPMessageModifier;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to modify SUBSCRIBE messages to globally reachable Internet addresses to
 * change the host in the callback URL to the global host name.
 * 
 * It implements the IHTTPMessageModifier interface.
 * 
 * @author Alexander Koenig
 * 
 */
public class DDDeviceSubscribeModifier implements IHTTPMessageModifier
{

  private String globalHostName;

  /**
   * Creates a new instance of DDDeviceSubscribeModifier.
   * 
   * @param globalHostName
   *          Globally reachable host name for the device.
   */
  public DDDeviceSubscribeModifier(String globalHostName)
  {
    this.globalHostName = globalHostName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageModifier#modifyHTTPRequest(de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public void modifyHTTPRequest(HTTPMessageObject request)
  {
    // check for GENA subscribe message, because callback URL must be redirected
    if (HTTPMessageHelper.isInitialSubscribe(request.getHeader()))
    {
      InetAddress destinationAddress = request.getDestinationAddress().getAddress();
      if (!IPHelper.isLocalAddress(destinationAddress))
      {
        System.out.println("Received SUBSCRIBE message to global address:\n[" + request.getHeader() + "]");
        // change callback host to global host name
        request.setHeader(HTTPMessageHelper.replaceCallbackHost(request.getHeader(), globalHostName));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageModifier#modifyHTTPResponse(de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public void modifyHTTPResponse(HTTPMessageObject response)
  {
    // nothing to be done for responses
  }

}

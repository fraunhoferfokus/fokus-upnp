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
package de.fraunhofer.fokus.upnp.util;

import java.net.InetSocketAddress;
import java.net.URL;

import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class provides helper methods for SSDP messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class SSDPHelper
{

  public static final String NETWORK_INTERFACE_FORWARDER_MODULE_NAME = "Interface";

  /**
   * Checks if the locationURL indicates that the associated discovery message was forwarded by this
   * host and rereceived.
   * 
   * @param locationURL
   *          The location URL
   * 
   * @return True if the message was issued by this host and is a gateway message, false otherwise
   */
  public static boolean isLoopedBackGatewayMessage(String locationURL)
  {
    try
    {
      URL url = new URL(locationURL);

      InetSocketAddress destinationAddress = IPHelper.toSocketAddress(url);

      // decode path
      Object[] pathElements = URLExtension.decodeGatewayURLPath(url.getPath());
      if (pathElements != null)
      {
        String outgoingModuleID = (String)pathElements[0];

        if (IPHelper.isLocalHostAddressString(IPHelper.toString(destinationAddress)) &&
          outgoingModuleID.startsWith(NETWORK_INTERFACE_FORWARDER_MODULE_NAME))
        {
          return true;
        }
      }
    } catch (Exception e)
    {
    }
    return false;
  }

  /**
   * Retrieves the UUID found in a NOTIFY message.
   * 
   * @param messageHeader
   *          The NOTIFY message
   * 
   * @return The UUID found in the USN header or an empty string
   */
  public static String getUUIDFromNotifyMessage(String messageHeader)
  {
    // extract device UUID to check if this message should be forwarded through the tunnel
    String usn = HTTPMessageHelper.getHeaderLine(messageHeader, CommonConstants.USN);
    if (usn != null)
    {
      int doubleColonIndex = usn.indexOf("::");
      // extract UUID from USN header
      if (doubleColonIndex != -1)
      {
        return usn.substring(0, doubleColonIndex);
      } else
      {
        return usn;
      }
    }
    return "";
  }

}

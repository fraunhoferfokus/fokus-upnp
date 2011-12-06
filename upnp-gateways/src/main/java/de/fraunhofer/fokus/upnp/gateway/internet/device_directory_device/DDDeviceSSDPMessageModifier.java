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

import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.ssdp.ISSDPMessageModifier;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to modify external SSDP requests to the DeviceDirectoryDevice.
 * 
 * @author Alexander Koenig
 * 
 */
public class DDDeviceSSDPMessageModifier implements ISSDPMessageModifier
{

  /** Globally reachable host name (e.g., fokus.dyndns.org) */
  // private String globalHostName;
  /** Globally reachable server socket address (e.g., fokus.dyndns.org:1903) */
  private String globalHostSocketAddress;

  /**
   * Creates a new instance of DDDeviceSSDPMessageModifier.
   * 
   * @param deviceDirectoryDevice
   *          The associated device
   */
  public DDDeviceSSDPMessageModifier(DeviceDirectoryDevice deviceDirectoryDevice)
  {
    InternetManagement internetManagement = deviceDirectoryDevice.getDeviceDirectoryEntity().getInternetManagement();
    // globalHostName = internetManagement.getGlobalIPAddress().getHostName();

    globalHostSocketAddress =
      internetManagement.getGlobalIPAddress().getHostAddress() + ":" +
        deviceDirectoryDevice.getDeviceStartupConfiguration().getHTTPServerPort();

    // System.out.println("Created SSDP message modifier. Global host name is " + globalHostName +
    // ", global socket address is " + globalHostSocketAddress);
  }

  /**
   * This method replaces the host found in the location header with the global IP address of this
   * DDDevice.
   * 
   * @param response
   *          The response
   */
  private void replaceLocationURL(BinaryMessageObject response)
  {
    // binary messages are not encoded
    String responseString = StringHelper.byteArrayToString(response.getBody());
    String locationValue = HTTPMessageHelper.getHeaderLine(responseString, CommonConstants.LOCATION);

    String address = response.getSourceAddress().getAddress().getHostAddress();
    int addressIndex = locationValue.indexOf(address);
    if (addressIndex != -1)
    {
      locationValue =
        locationValue.substring(0, addressIndex) + globalHostSocketAddress +
          locationValue.substring(addressIndex + address.length() + 1);

      responseString = HTTPMessageHelper.replaceHeaderLine(responseString, CommonConstants.LOCATION, locationValue);

      response.setBody(StringHelper.stringToByteArray(responseString));

      System.out.println("New header is [\n" + responseString + "]");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.ssdp.ISSDPMessageModifier#modifyMSEARCHRequest(de.fhg.fokus.magic.util.network.BinaryMessageObject)
   */
  public void modifyMSEARCHRequest(BinaryMessageObject request)
  {
    // nothing to do for requests
  }

  public void modifyMSEARCHResponse(BinaryMessageObject response)
  {
    System.out.println("Try to modify M-SEARCH response");
    // replace URLs only for external requests
    if (!IPHelper.isLocalAddress(response.getDestinationAddress().getAddress()))
    {
      replaceLocationURL(response);
    } else
    {
      System.out.println("M-SEARCH response is local");
    }
  }

}

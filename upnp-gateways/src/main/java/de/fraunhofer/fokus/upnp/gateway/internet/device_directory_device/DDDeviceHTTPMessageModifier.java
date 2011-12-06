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

import de.fraunhofer.fokus.upnp.http.IHTTPMessageModifier;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLReplacement;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to modify external HTTP requests to the DeviceDirectoryDevice.
 * 
 * @author Alexander Koenig
 * 
 */
public class DDDeviceHTTPMessageModifier implements IHTTPMessageModifier
{

  /** Globally reachable host name (e.g., fokus.dyndns.org) */
  private String globalHostName;

  /** Globally reachable server socket address (e.g., fokus.dyndns.org:1903) */
  // private String globalHostSocketAddress;
  /**
   * Creates a new instance of DeviceDirectoryHTTPMessageModifier.
   * 
   * @param deviceDirectoryDevice
   *          The associated device
   */
  public DDDeviceHTTPMessageModifier(DeviceDirectoryDevice deviceDirectoryDevice)
  {
    globalHostName =
      deviceDirectoryDevice.getDeviceDirectoryEntity().getInternetManagement().getGlobalIPAddress().getHostName();

    // globalHostSocketAddress =
    // deviceDirectoryDevice.getDeviceDirectoryEntity().getInternetManagement().
    // getGlobalIPAddress().getHostAddress() + ":" +
    // InternetManagementConstants.HTTP_DEVICE_DIRECTORY_DEVICE_REQUEST_PORT;

    // System.out.println("Created HTTP message modifier. Global host name is " + globalHostName +
    // ", global socket address is " + globalHostSocketAddress);
  }

  /**
   * This method tries to replace subscription URLs.
   * 
   * @param request
   *          The received request
   */
  private void replaceLocalCallbackURL(HTTPMessageObject request)
  {
    // this is the fast way, but it will only work if the
    // port of the control point is forwarded over the router
    if (HTTPMessageHelper.isInitialSubscribe(request.getHeader()) &&
      !IPHelper.isLocalAddress(request.getSourceAddress().getAddress()))
    {
      System.out.println("Replace callback URL with global IP address");

      request.setHeader(HTTPMessageHelper.replaceCallbackHost(request.getHeader(),
        IPHelper.toString(request.getSourceAddress())));

      System.out.println("New header is [\n" + request.getHeader() + "]");
    }
  }

  /**
   * This method replaces the local IP address found in device descriptions URLs with the global IP
   * address of this DDDevice.
   * 
   * @param response
   *          The device description response
   */
  private void replaceURLsInDeviceDescription(HTTPMessageObject response)
  {
    String responseBodyString = StringHelper.byteArrayToUTF8String(response.getBody());
    // check for XML and device description
    if (responseBodyString.startsWith(XMLConstant.XML_START) &&
      responseBodyString.indexOf(XMLConstant.XMLNS_DEVICE) != -1)
    {
      // replace normal URLs with global host name
      String changedDeviceDescription =
        URLReplacement.replaceDeviceDescriptionExceptEventURLs(responseBodyString, globalHostName);

      // replace event URL with global host name
      changedDeviceDescription =
        URLReplacement.replaceDeviceDescriptionEventURLs(changedDeviceDescription, globalHostName);

      byte[] modifiedBody = StringHelper.utf8StringToByteArray(changedDeviceDescription);
      response.setBody(modifiedBody);
      // change content length in response header
      response.setHeader(HTTPMessageHelper.replaceContentLength(response.getHeader(), modifiedBody.length));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageModifier#modifyHTTPRequest(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void modifyHTTPRequest(HTTPMessageObject request)
  {
    // replace callback URL in received SUBSCRIBE messages
    replaceLocalCallbackURL(request);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageModifier#modifyHTTPResponse(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void modifyHTTPResponse(HTTPMessageObject response)
  {
    // replace URLs only for external requests
    if (!IPHelper.isLocalAddress(response.getDestinationAddress().getAddress()))
    {
      replaceURLsInDeviceDescription(response);
    }
  }

}

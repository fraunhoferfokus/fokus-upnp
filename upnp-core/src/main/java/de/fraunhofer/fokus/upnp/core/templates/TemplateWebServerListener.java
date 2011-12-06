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
package de.fraunhofer.fokus.upnp.core.templates;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.HTMLMessageBuilder;
import de.fraunhofer.fokus.upnp.core.event.IDeviceWebServerListener;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.KeyValueVector;

/**
 * This class handles device specific requests to the device web server.
 * 
 * @author Alexander Koenig
 * 
 */
public class TemplateWebServerListener implements IDeviceWebServerListener
{

  protected KeyValueVector linkList = new KeyValueVector();

  public TemplateWebServerListener()
  {
    linkList.add(HTMLMessageBuilder.CONTENT, HTMLMessageBuilder.CONTENT_PAGE);
    linkList.add(HTMLMessageBuilder.INFO, HTMLMessageBuilder.INFO_PAGE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.IDeviceWebServerListener#processRequest(java.lang.String,
   *      de.fhg.fokus.magic.upnp.device.Device)
   */
  public WebServerListenerResponse processRequest(HTTPParser parser, Device device)
  {
    String path = parser.getHostPath();
    if (path == null || path.length() < 2 || path.startsWith("/" + HTMLMessageBuilder.CONTENT_PAGE))
    {
      // return content page
      return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML,
        HTMLMessageBuilder.buildContentPage(device, linkList));
    }
    // if (path.startsWith("/styles.css"))
    // {
    // return new WebServerListenerResponse(
    // HTTPConstant.CONTENT_TYPE_TEXT_CSS,
    // HTMLMessageBuilder.buildBasicStyleSheet());
    // }
    if (path.startsWith("/" + HTMLMessageBuilder.INFO_PAGE))
    {
      return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML,
        HTMLMessageBuilder.buildInfoPage(device, parser.getHostIP() + ":" + parser.getHostPort(), linkList));
    }
    // if (path.startsWith("/config.htm"))
    // {
    // return new WebServerListenerResponse(
    // HTTPConstant.CONTENT_TYPE_TEXT_HTML,
    // HTMLMessageBuilder.buildConfigPage(device, linkList));
    // }

    return null;
  }

}

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
package de.fraunhofer.fokus.upnp.core_av.server;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.HTMLMessageBuilder;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.core.templates.TemplateWebServerListener;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class handles web requests for media servers.
 * 
 * @author Alexander Koenig
 * 
 */
public class MediaServerWebServerListener extends TemplateWebServerListener
{
  public static final String MARGIN = " style=\"margin: 2px;\" ";

  private MediaServerDevice  mediaServerDevice;

  /**
   * Creates a new instance of MediaServerWebServerListener.
   * 
   * @param mediaServerDevice
   */
  public MediaServerWebServerListener(MediaServerDevice mediaServerDevice)
  {
    this.mediaServerDevice = mediaServerDevice;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateWebServerListener#processRequest(java.lang.String,
   *      de.fhg.fokus.magic.upnp.device.Device, java.net.InetSocketAddress)
   */
  public WebServerListenerResponse processRequest(HTTPParser parser, Device device)
  {
    String path = parser.getHostPath();
    // parse all requests
    if (mediaServerDevice != null && mediaServerDevice.getContentProvider() != null)
    {
      String id = null;
      if (path == null || path.length() < 2 || path.startsWith("/" + HTMLMessageBuilder.CONTENT_PAGE))
      {
        id = "0";
      }
      if (path != null && path.startsWith("/content?id="))
      {
        id = path.substring("/content?id=".length());
      }
      // invalid request
      if (id == null)
      {
        return super.processRequest(parser, device);
      }

      String result =
        HTMLMessageBuilder.buildHeader(device, HTMLMessageBuilder.CONTENT) +
          HTMLMessageBuilder.buildIncompleteEmptyBody(device, HTMLMessageBuilder.CONTENT, linkList);

      // show root content
      BrowseResponse browseResponse = mediaServerDevice.getContentProvider().browseDirectChildren(id);

      // build path to current directory
      Vector pathList = new Vector();
      BrowseResponse metadataBrowseResponse = mediaServerDevice.getContentProvider().browseMetadata(id);
      // current directory is not linked
      DIDLObject currentObject = metadataBrowseResponse.getFirstResult();
      // add current directory
      if (currentObject.getParentContainer() != null)
      {
        pathList.add(0, currentObject.getTitle());
      }
      // browse all parent directories
      while (currentObject.getParentContainer() != null)
      {
        currentObject = currentObject.getParentContainer();
        pathList.add(0, HTMLMessageBuilder.buildLink(currentObject.getParentContainer() == null ? "Wurzelverzeichnis"
          : currentObject.getTitle(), "/content?id=" + currentObject.getID()));
      }
      result += "<div class=\"content_path\">" + CommonConstants.NEW_LINE;
      for (int i = 0; i < pathList.size(); i++)
      {
        result +=
          "<span " + MARGIN + ">" + pathList.elementAt(i) + "</span>" + (i < pathList.size() - 1 ? " > " : "") +
            CommonConstants.NEW_LINE;
      }
      result += "</div>";

      // build content table
      result += "<div class=\"content_auto_table\">" + CommonConstants.NEW_LINE;
      result += "<table>" + CommonConstants.NEW_LINE;
      // add all found media items
      DIDLObject[] didlObjects = browseResponse.getResult();
      for (int i = 0; i < didlObjects.length; i++)
      {
        currentObject = didlObjects[i];

        String link = "";
        if (currentObject instanceof DIDLContainer)
        {
          link = HTMLMessageBuilder.buildLink(currentObject.getTitle(), "/content?id=" + currentObject.getID());
        }
        if (currentObject instanceof DIDLItem)
        {
          link = HTMLMessageBuilder.buildLink(currentObject.getTitle(), "/" + currentObject.getFirstResourceURL());
        }
        result += "<tr><td>" + CommonConstants.NEW_LINE;
        result += "<span " + MARGIN + ">" + link + "</span>" + CommonConstants.NEW_LINE;
        result += "</td></tr>";
      }
      result += "</table>" + CommonConstants.NEW_LINE;
      result += "</div></body></html>";
      return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML, result);
    }
    return super.processRequest(parser, device);
  }

}

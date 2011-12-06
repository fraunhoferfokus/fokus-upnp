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
package de.fraunhofer.fokus.upnp.core.examples.stress;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.HTMLMessageBuilder;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.templates.TemplateWebServerListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class encapsulates a clock device.
 * 
 * @author Alexander Koenig
 */
public class ClockDevice extends TemplateDevice
{

  private ClockService clockService;

  /**
   * Creates a new instance of ClockDevice.
   * 
   * @param anEntity
   * @param startupConfiguration
   */
  public ClockDevice(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateDevice#initDeviceContent()
   */
  public void initDeviceContent()
  {
    super.initDeviceContent();
    setIconList(new DeviceIcon[] {
      new DeviceIcon("image/gif", 32, 32, 8, "clock_icon.gif")
    });

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // services
    //    TranslationService translationService = new TranslationService(this);
    //    addTemplateService(translationService);

    //    AttributeService descriptionService = new AttributeService(this);
    //    addTemplateService(descriptionService);

    clockService = new ClockService(this);
    addTemplateService(clockService);

    //    UsageService usageService = new UsageService(this);
    //    addTemplateService(usageService);

    setWebServerListener(new WebServerListener());
  }

  private class WebServerListener extends TemplateWebServerListener
  {

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.event.IDeviceWebServerListener#processRequest(java.lang.String,
     *      de.fhg.fokus.magic.upnp.device.Device)
     */
    public WebServerListenerResponse processRequest(HTTPParser parser, Device device)
    {
      String path = parser.getHostPath();
      // overwrite handling for info
      if ((path == null || path.length() < 2 || path.startsWith("/" + HTMLMessageBuilder.CONTENT_PAGE)) &&
        clockService != null)
      {
        HTMLMessageBuilder.resetRowNumber();

        Vector resourceURLs = new Vector();
        resourceURLs.add("http://" + IPHelper.toString(device.getDeviceDescriptionSocketAddress()) +
          device.getRelativeServiceURL(clockService, UPnPConstant.SUFFIX_REST + "getTime"));
        resourceURLs.add("http://" + IPHelper.toString(device.getDeviceDescriptionSocketAddress()) +
          device.getRelativeServiceURL(clockService, UPnPConstant.SUFFIX_REST + "getSeconds"));

        // associate generic request with clock resources
        String bodyTag = HTMLMessageBuilder.buildSetResourceURLBodyTag(resourceURLs);

        String result =
          HTMLMessageBuilder.buildJavascriptHeader(device, HTMLMessageBuilder.CONTENT, "clock_scripts.js");

        result += HTMLMessageBuilder.buildIncompleteEmptyBody(device, bodyTag, HTMLMessageBuilder.CONTENT, linkList);
        result += "<div class=\"content\">" + CommonConstants.NEW_LINE;

        result += HTMLMessageBuilder.buildRelativeProperty("Aktuelle Zeit:", "Time", DateTimeHelper.getUPnPDate());
        result +=
          HTMLMessageBuilder.buildRelativeProperty("Zeit seit Tagesanbruch:", "Seconds", clockService.getCurrentTime() +
            " Sekunden");
        result += "</div></body></html>";
        return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML, result);
      }
      return super.processRequest(parser, device);
    }
  }

}

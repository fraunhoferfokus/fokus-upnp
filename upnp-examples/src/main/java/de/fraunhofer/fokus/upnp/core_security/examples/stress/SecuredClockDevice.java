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
package de.fraunhofer.fokus.upnp.core_security.examples.stress;

import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.HTMLMessageBuilder;
import de.fraunhofer.fokus.upnp.core.device.common.AttributeService;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.device.common.UsageService;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.core.templates.TemplateWebServerListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateEntity;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;

/**
 * This class encapsulates a secured clock device.
 * 
 * @author Alexander Koenig
 */
public class SecuredClockDevice extends SecuredTemplateDevice
{

  private SecuredClockService clockService;

  private UsageService        usageService;

  /** Creates a new instance of TemplateDevice */
  public SecuredClockDevice(SecurityAwareTemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#initDeviceContent()
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
    TranslationService translationService = new TranslationService(this);
    addTemplateService(translationService);

    AttributeService descriptionService = new AttributeService(this);
    addTemplateService(descriptionService);

    clockService = new SecuredClockService(this);
    addTemplateService(clockService);

    usageService = new UsageService(this);
    usageService.addUsageFromFile(getWorkingDirectory() + "usage_de.txt", "de");
    usageService.addUPnPDocFromFile(getWorkingDirectory() + "upnp_doc_de.txt", "de");
    addTemplateService(usageService);

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

        String result =
          HTMLMessageBuilder.buildRefreshHeader(device, HTMLMessageBuilder.CONTENT, 10, HTMLMessageBuilder.CONTENT_PAGE) +
            HTMLMessageBuilder.buildIncompleteEmptyBody(device, HTMLMessageBuilder.CONTENT, linkList);
        result += "<div class=\"content\">" + CommonConstants.NEW_LINE;

        result += HTMLMessageBuilder.buildRelativeProperty("Aktuelle Zeit:", DateTimeHelper.getUPnPDate());
        result +=
          HTMLMessageBuilder.buildRelativeProperty("Zeit seit Tagesanbruch:", clockService.getCurrentTime() + " Sekunden");
        result += "</div></body></html>";
        return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML, result);
      }
      return super.processRequest(parser, device);
    }
  }

}

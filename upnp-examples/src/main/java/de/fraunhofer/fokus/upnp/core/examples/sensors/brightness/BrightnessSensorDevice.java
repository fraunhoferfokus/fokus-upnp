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
package de.fraunhofer.fokus.upnp.core.examples.sensors.brightness;

import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;
import de.fraunhofer.fokus.lsf.core.templates.BinaryToUPnPTemplateDevice;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.HTMLMessageBuilder;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.event.WebServerListenerResponse;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.templates.TemplateWebServerListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class encapsulates a brightness sensor device.
 * 
 * @author Alexander Koenig
 */
public class BrightnessSensorDevice extends BinaryToUPnPTemplateDevice
{

  private BrightnessSensor brightnessSensor;

  /** Creates a new instance of BrightnessSensorDevice */
  public BrightnessSensorDevice(TemplateEntity anEntity,
    UPnPStartupConfiguration startupConfiguration,
    BinaryCPDevice binaryCPDevice)
  {
    super(anEntity, startupConfiguration, binaryCPDevice);
    runDelayed();
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
      new DeviceIcon("image/gif", 32, 32, 8, "brightness_icon.gif")
    });

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    brightnessSensor =
      new BrightnessSensor(this, binaryCPDevice.getCPServiceByType(BinaryUPnPConstants.ServiceTypeBrightnessSensor));
    addTemplateService(brightnessSensor);

    TranslationService translationService = new TranslationService(this);
    addTemplateService(translationService);

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
        brightnessSensor != null)
      {
        HTMLMessageBuilder.resetRowNumber();
        Vector resourceURLs = new Vector();
        resourceURLs.add("http://" + IPHelper.toString(device.getDeviceDescriptionSocketAddress()) +
          device.getRelativeServiceURL(brightnessSensor, UPnPConstant.SUFFIX_REST + "getCurrentBrightness"));

        // associate generic request with brightness resource
        String bodyTag = HTMLMessageBuilder.buildSetResourceURLBodyTag(resourceURLs);

        String result =
          HTMLMessageBuilder.buildJavascriptHeader(device, HTMLMessageBuilder.CONTENT, "brightness_scripts.js");

        result += HTMLMessageBuilder.buildIncompleteEmptyBody(device, bodyTag, HTMLMessageBuilder.CONTENT, linkList);
        result += "<div class=\"content\">" + CommonConstants.NEW_LINE;

        // build brightness property with unique ID
        result +=
          HTMLMessageBuilder.buildRelativeProperty("Aktuelle Helligkeit:",
            "CurrentBrightness",
            (int)brightnessSensor.getBinaryCPService().getNumericValue() + "%");

        result += "</div></body></html>";
        return new WebServerListenerResponse(HTTPConstant.CONTENT_TYPE_TEXT_HTML, result);
      }
      return super.processRequest(parser, device);
    }
  }

}

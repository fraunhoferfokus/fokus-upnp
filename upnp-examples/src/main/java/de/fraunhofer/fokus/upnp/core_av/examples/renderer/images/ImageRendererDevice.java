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
package de.fraunhofer.fokus.upnp.core_av.examples.renderer.images;

import java.net.MalformedURLException;
import java.net.URL;

import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.AVTransport;
import de.fraunhofer.fokus.upnp.core_av.ConnectionManager;

/**
 * This class holds a MediaRenderer device that can render images.
 * 
 * @author Alexander Koenig
 */
public class ImageRendererDevice extends TemplateDevice
{

  private ImageRendererAVTransport      avTransport;

  private ImageRendererRenderingControl renderingControl;

  private ConnectionManager             connectionManager;

  private TranslationService            translationService;

  private IImageRendererEvents          eventListener;

  public ImageRendererDevice(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);
  }

  public void setupDeviceVariables()
  {
    super.setupDeviceVariables();
    eventListener = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#initDeviceContent()
   */
  public void initDeviceContent()
  {
    super.initDeviceContent();
    DeviceIcon image_icon = new DeviceIcon("image/gif", 32, 32, 8, "image_icon.gif");

    setIconList(new DeviceIcon[] {
      image_icon
    });

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Description //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    String manURL = "http://www.fraunhofer.de";
    String modelDesc = "SmartEnvironments ImageRendererDevice 1.0";

    setModelDescription(modelDesc);
    try
    {
      setManufacturerURL(new URL(manURL));
    } catch (MalformedURLException mue)
    {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    avTransport = new ImageRendererAVTransport(this);
    addTemplateService(avTransport);
    renderingControl = new ImageRendererRenderingControl(this);
    addTemplateService(renderingControl);
    connectionManager =
      new ConnectionManager(this,
        deviceStartupConfiguration.getProperty("SourceProtocolInfo"),
        deviceStartupConfiguration.getProperty("SinkProtocolInfo"));
    connectionManager.resetConnectionState();
    addTemplateService(connectionManager);
    translationService = new TranslationService(this);
    translationService.addTranslationsFromFile(getWorkingDirectory() + "../translations.txt");
    addTemplateService(translationService);
  }

  public void setEventListener(IImageRendererEvents listener)
  {
    eventListener = listener;
  }

  public IImageRendererEvents getEventListener()
  {
    return eventListener;
  }

  public ConnectionManager getConnectionManager()
  {
    return connectionManager;
  }

  public AVTransport getAVTransport()
  {
    return avTransport;
  }

}

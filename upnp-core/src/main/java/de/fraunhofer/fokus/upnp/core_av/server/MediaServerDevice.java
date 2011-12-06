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

import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.ConnectionManager;
import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class encapsulates a UPnP media server.
 * 
 * @author Alexander Koenig
 * 
 */
public class MediaServerDevice extends TemplateDevice
{

  private ContentDirectory            contentDirectoryService;

  private ConnectionManager           connectionManagerService;

  private IMediaServerContentProvider contentProvider;

  private IMediaServerContentModifier contentModifier;

  private IMediaServerContentHelper   contentHelper;

  /**
   * Creates a new instance of MediaServerDevice.
   * 
   * @param anEntity
   * @param startupConfiguration
   */
  public MediaServerDevice(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
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
      new DeviceIcon("image/gif", 32, 32, 8, "server_icon.gif")
    });

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // services
    contentDirectoryService = new ContentDirectory(this);
    contentDirectoryService.setContentProvider(contentProvider);
    contentDirectoryService.setContentModifier(contentModifier);
    contentDirectoryService.setContentHelper(contentHelper);
    contentDirectoryService.runDelayed();

    addTemplateService(contentDirectoryService);
    connectionManagerService =
      new ConnectionManager(this,
        deviceStartupConfiguration.getProperty("SourceProtocolInfo"),
        deviceStartupConfiguration.getProperty("SinkProtocolInfo"));
    connectionManagerService.resetConnectionState();
    addTemplateService(connectionManagerService);

    TranslationService translationService = new TranslationService(this);
    translationService.addTranslationsFromFile(FileHelper.getResourceDirectoryName() +
      "core_av/examples/server/translations.txt");
    addTemplateService(translationService);

    setWebServerListener(new MediaServerWebServerListener(this));
  }

  public ContentDirectory getContentDirectory()
  {
    return contentDirectoryService;
  }

  /**
   * Sets the content provider. Must be called prior to initDeviceContent.
   * 
   * @param provider
   */
  public void setContentProvider(IMediaServerContentProvider provider)
  {
    this.contentProvider = provider;
  }

  /**
   * Sets the content modifier. Must be called prior to initDeviceContent.
   * 
   * @param modifier
   */
  public void setContentModifier(IMediaServerContentModifier modifier)
  {
    this.contentModifier = modifier;
  }

  /**
   * Sets the content helper. Must be called prior to initDeviceContent.
   * 
   * @param helper
   */
  public void setContentHelper(IMediaServerContentHelper helper)
  {
    this.contentHelper = helper;
  }

  /**
   * Retrieves the contentHelper.
   * 
   * @return The contentHelper.
   */
  public IMediaServerContentHelper getContentHelper()
  {
    return contentHelper;
  }

  /**
   * Retrieves the contentModifier.
   * 
   * @return The contentModifier.
   */
  public IMediaServerContentModifier getContentModifier()
  {
    return contentModifier;
  }

  /**
   * Retrieves the contentProvider.
   * 
   * @return The contentProvider.
   */
  public IMediaServerContentProvider getContentProvider()
  {
    return contentProvider;
  }

}

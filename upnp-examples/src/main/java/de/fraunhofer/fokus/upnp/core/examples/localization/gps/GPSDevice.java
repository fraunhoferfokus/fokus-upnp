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
package de.fraunhofer.fokus.upnp.core.examples.localization.gps;

import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.examples.localization.IGPSProvider;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class encapulates a device with a GPS service.
 * 
 * @author Alexander Koenig
 */
public class GPSDevice extends TemplateDevice
{

  private IGPSProvider gpsProvider;

  private GPSService   gpsService;

  public GPSDevice(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration, IGPSProvider gpsProvider)
  {
    super(anEntity, startupConfiguration);

    this.gpsProvider = gpsProvider;

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
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // services
    gpsService = new GPSService(this, gpsProvider);
    addTemplateService(gpsService);

    TranslationService translationService = new TranslationService(this);
    addTemplateService(translationService);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#forceRunDelayed()
   */
  public boolean forceRunDelayed()
  {
    return true;
  }

  /**
   * Retrieves the localizationService.
   * 
   * @return The localizationService
   */
  public GPSService getGPSService()
  {
    return gpsService;
  }

}

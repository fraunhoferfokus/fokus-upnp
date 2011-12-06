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
package de.fraunhofer.fokus.upnp.core.examples.lights;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.device.common.AttributeService;
import de.fraunhofer.fokus.upnp.core.device.common.TranslationService;
import de.fraunhofer.fokus.upnp.core.device.common.UsageService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class encapsulates a dimmable light.
 * 
 * @author Alexander Koenig
 */
public class DimmableLightDevice extends TemplateDevice
{
  /** Interface to the actual worker */
  private IDimming     dimmingImplementer;

  /** Interface to the actual worker */
  private ISwitchPower switchPowerImplementer;

  /** Switch service */
  private SwitchPower  switchPowerService;

  private Dimming      dimmingService;

  /**
   * Creates a new instance of DimmableLightDevice.
   * 
   * @param anEntity
   * @param startupConfiguration
   * @param dimming
   * @param switchPower
   */
  public DimmableLightDevice(TemplateEntity anEntity,
    UPnPStartupConfiguration startupConfiguration,
    IDimming dimming,
    ISwitchPower switchPower)
  {
    super(anEntity, startupConfiguration);

    this.dimmingImplementer = dimming;
    this.switchPowerImplementer = switchPower;

    runDelayed();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateDevice#forceRunDelayed()
   */
  public boolean forceRunDelayed()
  {
    return true;
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
      new DeviceIcon("image/gif", 32, 32, 8, "lights_icon.gif")
    });

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Services //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // services
    switchPowerService = new SwitchPower(this, switchPowerImplementer, DeviceConstant.SWITCH_POWER_SERVICE_ID);
    addTemplateService(switchPowerService);

    dimmingService = new Dimming(this, dimmingImplementer, DeviceConstant.DIMMING_SERVICE_ID);
    addTemplateService(dimmingService);

    TranslationService translationService = new TranslationService(this);
    addTemplateService(translationService);

    AttributeService descriptionService = new AttributeService(this);
    addTemplateService(descriptionService);

    UsageService usageService = new UsageService(this);
    usageService.addUsageFromFile(getWorkingDirectory() + "usage_de.txt", "de");
    usageService.addUPnPDocFromFile(getWorkingDirectory() + "upnp_doc_de.txt", "de");
    addTemplateService(usageService);

  }

  /**
   * Retrieves the value of switchPowerService.
   * 
   * @return The value of switchPowerService
   */
  public SwitchPower getSwitchPowerService()
  {
    return switchPowerService;
  }

  /**
   * Retrieves the value of dimmingService.
   * 
   * @return The value of dimmingService
   */
  public Dimming getDimmingService()
  {
    return dimmingService;
  }

}

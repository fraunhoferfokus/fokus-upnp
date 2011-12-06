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
package de.fraunhofer.fokus.upnp.core.examples.agents;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class starts an autonomous control point that changes the light level according to the
 * temperature received from a temperature sensor.
 * 
 * @author Alexander Koenig
 */
public class LightControlEntity extends TemplateEntity
{

  private String   sensorName   = "VirtualTemperature";

  private String   lightName    = "Light";

  private CPDevice sensorDevice = null;

  private CPDevice lightDevice  = null;

  public LightControlEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    // Name des Lichtsensors aus der Konfiguration lesen
    lightName = getStartupConfiguration().getProperty("LightName", lightName);
    System.out.println("Suche Licht mit Namen: " + lightName);

    sensorName = getStartupConfiguration().getProperty("SensorName", sensorName);
    System.out.println("Suche Sensor mit Namen: " + sensorName);

    // Kontrollpunkt starten
    setTemplateControlPoint(new TemplateControlPoint(this, getStartupConfiguration()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#newDevice(de.fraunhofer.fokus.upnp.core.control_point.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    super.newDevice(newDevice);
    // Sensor gefunden?
    if (newDevice.getFriendlyName().equals(sensorName))
    {
      System.out.println("Sensor gefunden");
      sensorDevice = newDevice;
      // Dienst suchen und Events abonnieren
      CPService temperatureService = sensorDevice.getCPServiceByType(SensorConstants.TEMPERATURE_SENSOR_SERVICE_TYPE);
      if (temperatureService != null)
      {
        temperatureService.sendSubscription();
      }
    }
    // Licht gefunden?
    if (newDevice.getFriendlyName().equals(lightName))
    {
      System.out.println("Licht gefunden");
      lightDevice = newDevice;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#deviceGone(de.fraunhofer.fokus.upnp.core.control_point.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    super.deviceGone(goneDevice);
    if (goneDevice.getFriendlyName().equals(sensorName))
    {
      System.out.println("Sensor beendet");
      sensorDevice = null;
    }
    if (goneDevice.getFriendlyName().equals(lightName))
    {
      System.out.println("Licht beendet");
      lightDevice = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#stateVariableChanged(de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    super.stateVariableChanged(stateVariable);
    if (stateVariable.getName().equals("CurrentTemperature") &&
      stateVariable.getCPService().getCPDevice() == sensorDevice && lightDevice != null)
    {
      System.out.println("Passe Licht an");
      try
      {
        int currentTemperature = (int)stateVariable.getNumericValue() / 100;

        currentTemperature = Math.max(-30, Math.min(70, currentTemperature));

        // dimmen
        CPService dimmingService = lightDevice.getCPServiceByType(DeviceConstant.DIMMING_SERVICE_TYPE);
        // Fehler im Intel-Licht abfangen (falscher Servicetyp)
        if (dimmingService == null)
        {
          dimmingService = lightDevice.getCPServiceByType("urn:schemas-upnp-org:service:DimmingService:1");
        }
        if (dimmingService != null)
        {
          CPAction action = dimmingService.getCPAction("SetLoadLevelTarget");
          action.getArgument("NewLoadLevelTarget").setNumericValue(currentTemperature + 30);
          getTemplateControlPoint().invokeAction(action);
        }
        // und anschalten
        CPService switchService = lightDevice.getCPServiceByType(DeviceConstant.SWITCH_POWER_SERVICE_TYPE);
        if (switchService != null)
        {
          CPAction action = switchService.getCPAction("SetTarget");
          action.getArgument("NewTargetValue").setBooleanValue(true);
          getTemplateControlPoint().invokeAction(action);
        }
      } catch (Exception e)
      {
        // Ausnahmen verarbeiten
      }
    }
  }

  public static void main(String[] args)
  {
    new LightControlEntity(args.length > 0 ? new UPnPStartupConfiguration(args[0]) : null);
  }

}

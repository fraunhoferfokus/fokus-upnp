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
package de.fraunhofer.fokus.upnp.core.examples.sensors.internet_weather;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

public class WeatherService extends TemplateService
{
  private StateVariable    currentAirPressure;

  private StateVariable    currentAirHumidity;

  private StateVariable    currentSunrise;

  private StateVariable    currentSunset;

  private StateVariable    currentTemperature;

  private StateVariable    currentCondition;

  private Action           getAirPressure;

  private Action           getAirHumidity;

  private Action           getSunrise;

  private Action           getSunset;

  private Action           getTemperature;

  private Action           getCurrentCondition;

  private IWeatherProvider weatherProvider;

  public WeatherService(TemplateDevice device, IWeatherProvider weatherProvider)
  {
    super(device, SensorConstants.WEATHER_SERVICE_TYPE, SensorConstants.WEATHER_SERVICE_ID, false);

    this.weatherProvider = weatherProvider;

    runDelayed();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // String variable
    currentAirPressure = new StateVariable("AirPressure", weatherProvider.getAirPressure(), true);
    currentAirHumidity = new StateVariable("AirHumidity", weatherProvider.getAirHumidity(), true);
    currentSunrise = new StateVariable("Sunrise", weatherProvider.getSunrise(), true);
    currentSunset = new StateVariable("Sunset", weatherProvider.getSunset(), true);
    currentTemperature = new StateVariable("Temperature", weatherProvider.getTemperature(), true);
    currentCondition = new StateVariable("CurrentCondition", weatherProvider.getCondition(), true);

    StateVariable[] stateVariableList = {
        currentAirPressure, currentAirHumidity, currentSunrise, currentSunset, currentTemperature, currentCondition
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    getAirPressure = new Action("GetAirPressure");
    getAirPressure.setArgumentTable(new Argument[] {
      new Argument("AirPressure", UPnPConstant.DIRECTION_OUT, currentAirPressure)
    });

    getAirHumidity = new Action("GetAirHumidity");
    getAirHumidity.setArgumentTable(new Argument[] {
      new Argument("AirHumidity", UPnPConstant.DIRECTION_OUT, currentAirHumidity)
    });

    getSunrise = new Action("GetSunrise");
    getSunrise.setArgumentTable(new Argument[] {
      new Argument("Sunrise", UPnPConstant.DIRECTION_OUT, currentSunrise)
    });

    getSunset = new Action("GetSunset");
    getSunset.setArgumentTable(new Argument[] {
      new Argument("Sunset", UPnPConstant.DIRECTION_OUT, currentSunset)
    });

    getTemperature = new Action("GetTemperature");
    getTemperature.setArgumentTable(new Argument[] {
      new Argument("Temperature", UPnPConstant.DIRECTION_OUT, currentTemperature)
    });

    getCurrentCondition = new Action("GetCurrentCondition");
    getCurrentCondition.setArgumentTable(new Argument[] {
      new Argument("Condition", UPnPConstant.DIRECTION_OUT, currentCondition)
    });

    Action[] actionList = {
        getAirPressure, getAirHumidity, getSunrise, getSunset, getTemperature, getCurrentCondition
    };
    setActionTable(actionList);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // must be equal to the action name but start with a lower case character
  public void getAirPressure(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentAirPressure);
  }

  public void getAirHumidity(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentAirHumidity);
  }

  public void getSunrise(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentSunrise);
  }

  public void getSunset(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentSunset);
  }

  public void getTemperature(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentTemperature);
  }

  public void getCurrentCondition(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentCondition);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Updates the current condition */
  public void conditionChanged(String condition)
  {
    currentCondition.trySetValue(condition);
  }

  /** Updates the current air pressure */
  public void airPressureChanged(String airPressure)
  {
    currentAirPressure.trySetValue(airPressure);
  }

  /** Updates the current air humidity */
  public void airHumidityChanged(String airHumidity)
  {
    currentAirHumidity.trySetValue(airHumidity);
  }

  /** Updates the current sunrise */
  public void sunriseTimeChanged(String sunrise)
  {
    currentSunrise.trySetValue(sunrise);
  }

  /** Updates the current sunset */
  public void sunsetTimeChanged(String sunset)
  {
    currentSunset.trySetValue(sunset);
  }

  /** Updates the current temperature */
  public void temperatureChanged(int temperature)
  {
    try
    {
      currentTemperature.setNumericValue(temperature);
    } catch (Exception ex)
    {
    }
  }

}

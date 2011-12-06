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
package de.fraunhofer.fokus.upnp.core.examples.sensors.temperature;

import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a UPnP compliant temperature sensor.
 * 
 * @author Alexander Koenig
 */
public class TemperatureSensor extends TemplateService
{

  private StateVariable        application;

  private StateVariable        currentTemperature;

  private StateVariable        name;

  private Action               getApplication;

  private Action               getCurrentTemperature;

  private Action               getName;

  private ITemperatureProvider temperatureProvider;

  private IBinaryUPnPDevice    binaryUPnPDevice;

  /** Creates a new instance of TemperatureSensor */
  public TemperatureSensor(TemplateDevice device,
    ITemperatureProvider temperatureProvider,
    IBinaryUPnPDevice binaryUPnPDevice)
  {
    super(device, SensorConstants.TEMPERATURE_SENSOR_SERVICE_TYPE, SensorConstants.TEMPERATURE_SENSOR_SERVICE_ID, false);

    this.temperatureProvider = temperatureProvider;
    this.binaryUPnPDevice = binaryUPnPDevice;

    runDelayed();
  }

  /** Initializes service specific properties */
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
    application = new StateVariable("Application", binaryUPnPDevice.getApplication(), true);
    currentTemperature = new StateVariable("CurrentTemperature", "i4", temperatureProvider.getTemperature(), true);
    name = new StateVariable("Name", binaryUPnPDevice.getName(), true);

    StateVariable[] stateVariableList = {
        application, currentTemperature, name
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getApplication = new Action("GetApplication");
    getApplication.setArgumentTable(new Argument[] {
      new Argument("CurrentApplication", UPnPConstant.DIRECTION_OUT, application)
    });
    getCurrentTemperature = new Action("GetCurrentTemperature");
    getCurrentTemperature.setArgumentTable(new Argument[] {
      new Argument("CurrentTemp", UPnPConstant.DIRECTION_OUT, currentTemperature)
    });
    getName = new Action("GetName");
    getName.setArgumentTable(new Argument[] {
      new Argument("CurrentName", UPnPConstant.DIRECTION_OUT, name)
    });

    Action[] actionList = {
        getApplication, getCurrentTemperature, getName
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
  public void getApplication(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, application);
  }

  public void getCurrentTemperature(Argument[] args) throws ActionFailedException
  {
    // long startTime = HighResTimerHelper.getTimeStamp();
    handleStateVariableRequest(args, currentTemperature);
    // long endTime = HighResTimerHelper.getTimeStamp();
    // System.out.println("Time to handle internal action: " +
    // HighResTimerHelper.getMicroseconds(startTime, endTime));
  }

  public void getName(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, name);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
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

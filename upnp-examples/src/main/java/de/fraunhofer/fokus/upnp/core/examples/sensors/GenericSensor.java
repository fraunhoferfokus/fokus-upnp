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
package de.fraunhofer.fokus.upnp.core.examples.sensors;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a generic sensor.
 * 
 * @author Alexander Koenig
 */
public class GenericSensor extends TemplateService
{

  private StateVariable   stringValue;

  private StateVariable   numericValue;

  private StateVariable   booleanValue;

  private StateVariable   serviceType;

  private StateVariable   application;

  private StateVariable   unit;

  private StateVariable   name;

  private Action          getBooleanValue;

  private Action          getStringValue;

  private Action          getNumericValue;

  private Action          getType;

  private Action          getApplication;

  private Action          getUnit;

  private Action          getName;

  private BinaryCPService binaryCPService;

  /** Creates a new instance of TemperatureSensor */
  public GenericSensor(TemplateDevice device, BinaryCPService binaryCPService)
  {
    super(device, SensorConstants.GENERIC_SENSOR_SERVICE_TYPE, SensorConstants.GENERIC_SENSOR_SERVICE_ID, false);

    this.binaryCPService = binaryCPService;

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
    booleanValue = new StateVariable("BooleanValue", binaryCPService.getBooleanValue(), true);
    numericValue = new StateVariable("NumericValue", binaryCPService.getNumericValue(), true);
    stringValue = new StateVariable("StringValue", binaryCPService.getStringValue(), true);
    serviceType =
      new StateVariable("Type", BinaryUPnPConstants.serviceTypeToString(binaryCPService.getServiceType()), true);
    application = new StateVariable("Application", binaryCPService.getBinaryCPDevice().getApplication(), true);
    unit = new StateVariable("Unit", binaryCPService.getUnit(), true);
    name = new StateVariable("Name", binaryCPService.getBinaryCPDevice().getName(), true);

    StateVariable[] stateVariableList = {
        booleanValue, numericValue, stringValue, serviceType, application, unit, name
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getBooleanValue = new Action("GetBooleanValue");
    getBooleanValue.setArgumentTable(new Argument[] {
      new Argument("CurrentValue", UPnPConstant.DIRECTION_OUT, booleanValue)
    });
    getNumericValue = new Action("GetNumericValue");
    getNumericValue.setArgumentTable(new Argument[] {
      new Argument("CurrentValue", UPnPConstant.DIRECTION_OUT, numericValue)
    });
    getStringValue = new Action("GetStringValue");
    getStringValue.setArgumentTable(new Argument[] {
      new Argument("CurrentValue", UPnPConstant.DIRECTION_OUT, stringValue)
    });
    getType = new Action("GetType");
    getType.setArgumentTable(new Argument[] {
      new Argument("SensorType", UPnPConstant.DIRECTION_OUT, serviceType)
    });
    getApplication = new Action("GetApplication");
    getApplication.setArgumentTable(new Argument[] {
      new Argument("CurrentApplication", UPnPConstant.DIRECTION_OUT, application)
    });
    getUnit = new Action("GetUnit");
    getUnit.setArgumentTable(new Argument[] {
      new Argument("SensorUnit", UPnPConstant.DIRECTION_OUT, unit)
    });
    getName = new Action("GetName");
    getName.setArgumentTable(new Argument[] {
      new Argument("CurrentName", UPnPConstant.DIRECTION_OUT, name)
    });

    Action[] actionList = {
        getBooleanValue, getNumericValue, getStringValue, getType, getApplication, getUnit, getName
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
  public void getBooleanValue(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(booleanValue.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getNumericValue(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(numericValue.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getStringValue(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(stringValue.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getType(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, serviceType);
  }

  public void getApplication(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, application);
  }

  public void getUnit(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, unit);
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
  /** Updates the current value */
  public void numericValueChanged(long value)
  {
    try
    {
      numericValue.setNumericValue(value);
    } catch (Exception ex)
    {
    }
  }

}

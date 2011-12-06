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

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class implements the standardized SwitchPower service.
 * 
 * @author Alexander Koenig
 */
public class SwitchPower extends TemplateService
{

  private ISwitchPower    switchPowerInterface;

  protected StateVariable target;

  protected StateVariable status;

  private Action          setTarget;

  private Action          getTarget;

  private Action          getStatus;

  /**
   * Creates a new instance of SwitchPower with the standard device path and a specific serviceID
   */
  public SwitchPower(TemplateDevice device, ISwitchPower switchPowerInterface, String serviceID)
  {
    super(device, DeviceConstant.SWITCH_POWER_SERVICE_TYPE, serviceID, false);
    this.switchPowerInterface = switchPowerInterface;
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
    boolean currentState = switchPowerInterface.getStatus(getServiceId());

    target = new StateVariable("Target", currentState, false);
    status = new StateVariable("Status", currentState, true);

    StateVariable[] stateVariableList = {
        target, status
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    setTarget = new Action("SetTarget");
    setTarget.setArgumentTable(new Argument[] {
      new Argument("NewTargetValue", UPnPConstant.DIRECTION_IN, target)
    });

    getTarget = new Action("GetTarget");
    getTarget.setArgumentTable(new Argument[] {
      new Argument("RetTargetValue", UPnPConstant.DIRECTION_OUT, target)
    });

    getStatus = new Action("GetStatus");
    getStatus.setArgumentTable(new Argument[] {
      new Argument("ResultStatus", UPnPConstant.DIRECTION_OUT, status)
    });

    Action[] actionList = {
        setTarget, getTarget, getStatus
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
  public void setTarget(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    boolean argTarget = false;
    try
    {
      argTarget = args[0].getBooleanValue();
      target.setBooleanValue(argTarget);
      // send value to light
      switchPowerInterface.setTarget(getServiceId(), argTarget);

    } catch (Exception ex)
    {
      throw new ActionFailedException(801, "Invalid args");
    }
  }

  public void getTarget(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setBooleanValue(target.getBooleanValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getStatus(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setBooleanValue(status.getBooleanValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Updates the current status of the light */
  public void statusChanged(boolean value)
  {
    try
    {
      status.setBooleanValue(value);
    } catch (Exception ex)
    {
    }
  }

}

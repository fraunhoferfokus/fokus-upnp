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

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapulates a UPnP compatible Dimming service.
 * 
 * @author Alexander Koenig
 */
public class Dimming extends TemplateService
{

  private IDimming        dimmingPowerInterface;

  protected StateVariable loadLevelTarget;

  protected StateVariable loadLevelStatus;

  private Action          setLoadLevelTarget;

  private Action          getLoadLevelTarget;

  private Action          getLoadLevelStatus;

  /**
   * Creates a new instance of Dimming with the standard device path and a specific serviceID
   */
  public Dimming(TemplateDevice device, IDimming dimmingPowerInterface, String serviceID)
  {
    super(device, DeviceConstant.DIMMING_SERVICE_TYPE, serviceID, false);
    this.dimmingPowerInterface = dimmingPowerInterface;
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
    // target and status are equal at startup
    int status = dimmingPowerInterface.getLoadLevelStatus(getServiceId());
    loadLevelTarget = new StateVariable("LoadLevelTarget", "ui1", status, false);
    loadLevelTarget.setAllowedValueRange(new AllowedValueRange(0, 100, 1));
    loadLevelStatus = new StateVariable("LoadLevelStatus", "ui1", status, true);
    loadLevelStatus.setAllowedValueRange(new AllowedValueRange(0, 100, 1));

    StateVariable[] stateVariableList = {
        loadLevelTarget, loadLevelStatus
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    setLoadLevelTarget = new Action("SetLoadLevelTarget");
    setLoadLevelTarget.setArgumentTable(new Argument[] {
      new Argument("NewLoadLevelTarget", UPnPConstant.DIRECTION_IN, loadLevelTarget)
    });

    getLoadLevelTarget = new Action("GetLoadLevelTarget");
    getLoadLevelTarget.setArgumentTable(new Argument[] {
      new Argument("retLoadLevelTarget", UPnPConstant.DIRECTION_OUT, loadLevelTarget)
    });

    getLoadLevelStatus = new Action("GetLoadLevelStatus");
    getLoadLevelStatus.setArgumentTable(new Argument[] {
      new Argument("retLoadLevelStatus", UPnPConstant.DIRECTION_OUT, loadLevelStatus)
    });

    Action[] actionList = {
        setLoadLevelTarget, getLoadLevelTarget, getLoadLevelStatus
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
  public void setLoadLevelTarget(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    long argTarget = 0;
    try
    {
      argTarget = args[0].getNumericValue();
      loadLevelTarget.setNumericValue(argTarget);
      // send value to light
      dimmingPowerInterface.setLoadLevelTarget(getServiceId(), (int)argTarget);

    } catch (Exception ex)
    {
      throw new ActionFailedException(801, "Invalid args");
    }
  }

  public void getLoadLevelTarget(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setNumericValue(loadLevelTarget.getNumericValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getLoadLevelStatus(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setNumericValue(loadLevelStatus.getNumericValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Interface to Inet //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Updates the current status of the light */
  public void loadLevelStatusChanged(int value)
  {
    try
    {
      loadLevelStatus.setNumericValue(value);
    } catch (Exception ex)
    {
    }
  }

}

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
package de.fraunhofer.fokus.upnp.core.examples.sensors.brightness;

import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.lsf.core.templates.BinaryToUPnPTemplateService;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a brightness sensor.
 * 
 * @author Alexander Koenig
 */
public class BrightnessSensor extends BinaryToUPnPTemplateService
{

  private StateVariable currentBrightness;

  private Action        getCurrentBrightness;

  /** Creates a new instance of BrightnessSensor */
  public BrightnessSensor(TemplateDevice device, BinaryCPService binaryCPService)
  {
    super(device,
      SensorConstants.BRIGHTNESS_SENSOR_SERVICE_TYPE,
      SensorConstants.BRIGHTNESS_SENSOR_SERVICE_ID,
      binaryCPService);
    runDelayed();
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    initBinaryUPnPServiceContent();
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // String variable
    currentBrightness = new StateVariable("CurrentBrightness", "i4", (int)binaryCPService.getNumericValue(), true);
    currentBrightness.setMinDelta(3);
    currentBrightness.setAllowedValueRange(0, 100, 1);

    StateVariable[] stateVariableList = {
        application, currentBrightness, name
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getCurrentBrightness = new Action("GetCurrentBrightness");
    getCurrentBrightness.setArgumentTable(new Argument[] {
      new Argument("CurrentBrightness", UPnPConstant.DIRECTION_OUT, currentBrightness)
    });

    Action[] actionList = {
        getApplication, getCurrentBrightness, getName
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
  public void getCurrentBrightness(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, currentBrightness);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.templates.BinaryToUPnPTemplateService#binaryUPnPServiceValueChanged()
   */
  public void binaryUPnPServiceValueChanged()
  {
    try
    {
      currentBrightness.setNumericValue(binaryCPService.getNumericValue());
    } catch (Exception ex)
    {
    }
  }

}

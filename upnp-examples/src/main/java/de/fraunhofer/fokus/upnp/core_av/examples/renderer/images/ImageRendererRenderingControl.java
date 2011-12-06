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
package de.fraunhofer.fokus.upnp.core_av.examples.renderer.images;

import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.renderer.RenderingControl;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class implements the renderingControl service specific for an image renderer
 * 
 * @author Alexander Koenig
 * 
 */
public class ImageRendererRenderingControl extends RenderingControl
{

  public ImageRendererRenderingControl(TemplateDevice device)
  {
    super(device);
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    super.initServiceContent();
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // this effectively prevents variables from showing up
    StateVariable[] stateVariableList = {
        lastChange, presetNameList, A_ARG_TYPE_Channel, A_ARG_TYPE_InstanceID, A_ARG_TYPE_PresetName
    };

    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // this effectively prevents actions from showing up
    Action[] actionList = {
        listPresets, selectPreset
    };
    setActionTable(actionList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.DeviceService#getInitialStateVariableValue(de.fhg.fokus.magic.upnp.StateVariable)
   */
  public Object getInitialStateVariableValue(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    if (stateVariable.getName().equals(UPnPAVConstant.LAST_CHANGE))
    {
      // build message with all state variables
      String initialValue = UPnPAVConstant.EVENT_START;
      initialValue += "<" + UPnPAVConstant.ARG_INSTANCE_ID + " " + UPnPAVConstant.ATTR_VAL + "=\"0\">";

      initialValue += "</" + UPnPAVConstant.ARG_INSTANCE_ID + ">";
      initialValue += UPnPAVConstant.EVENT_END;

      return StringHelper.xmlToEscapedString(initialValue);
    }
    return super.getInitialStateVariableValue(stateVariable, publicKey);
  }

}

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
package de.fraunhofer.fokus.upnp.core.examples.vehicle.active_safety;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class implements a ActiveSafetyService that can be used to retrieve active safety messages.
 * 
 * @author Alexander Koenig
 */
public class ActiveSafetyService extends TemplateService
{

  private StateVariable messageCount;

  private StateVariable A_ARG_TYPE_int;

  private StateVariable A_ARG_TYPE_string;

  private Action        getActiveSafetyMessage;

  private Action        getActiveSafetyMessageCount;

  /** Creates a new instance of ActiveSafetyService */
  public ActiveSafetyService(TemplateDevice device)
  {
    super(device, ActiveSafetyConstant.ACTIVE_SAFETY_SERVICE_TYPE, ActiveSafetyConstant.ACTIVE_SAFETY_SERVICE_ID);
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
    messageCount = new StateVariable("MessageCount", 1, true);
    A_ARG_TYPE_int = new StateVariable("A_ARG_TYPE_int", 0, false);
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);

    StateVariable[] stateVariableList = {
        messageCount, A_ARG_TYPE_int, A_ARG_TYPE_string
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getActiveSafetyMessage = new Action("GetActiveSafetyMessage");
    getActiveSafetyMessage.setArgumentTable(new Argument[] {
        new Argument("Index", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("Content", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getActiveSafetyMessageCount = new Action("GetActiveSafetyMessageCount");
    getActiveSafetyMessageCount.setArgumentTable(new Argument[] {
      new Argument("Count", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_int)
    });

    Action[] actionList = {
        getActiveSafetyMessage, getActiveSafetyMessageCount
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
  public void getActiveSafetyMessage(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    long index = 0;
    try
    {
      index = args[0].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (index != 0)
    {
      throw new ActionFailedException(801, "Invalid index");
    }
    try
    {
      String content = "";
      if (getTemplateDevice().getFriendlyName().indexOf("Accident") != -1)
      {
        content = "Unfall auf links abknickender Straße! Langsam fahren und nicht überholen";
      } else
      {
        content = "Achtung enge Kurve! Empfohlene Geschwindigkeit 30 km/h. 234 Unfälle im letzten Jahr";
      }
      args[1].setValue(content);
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getActiveSafetyMessageCount(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setNumericValue(1);
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }
}

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
package de.fraunhofer.fokus.upnp.core.device.personalization;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class implements a PersonalizationService that is not secured in a cryptographic way. It be
 * used to personalize devices and services.
 * 
 * @author Alexander Koenig
 */
public class SimplePersonalizationService extends TemplateService
{

  private StateVariable                  currentUser;

  private StateVariable                  userList;

  private Action                         getUserList;

  private Action                         setUser;

  private Vector                         userVector;

  private String                         userListString;

  private ISimplePersonalizationListener userEventListener;

  /**
   * Creates a new instance of SimplePersonalizationService.
   * 
   * @param device
   */
  public SimplePersonalizationService(TemplateDevice device, ISimplePersonalizationListener userEventListener)
  {
    super(device,
      DeviceConstant.SIMPLE_PERSONALIZATION_SERVICE_TYPE,
      DeviceConstant.SIMPLE_PERSONALIZATION_SERVICE_ID,
      false);

    this.userEventListener = userEventListener;

    runDelayed();
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    userVector = new Vector();
    userVector.add(UPnPConstant.USER_UNKNOWN);
    userVector.add("Alexander König");
    userVector.add("Sebastian Nauck");

    String[] allowedUsers = new String[userVector.size()];
    userListString = "";
    for (int i = 0; i < userVector.size(); i++)
    {
      userListString += (i == 0 ? "" : ",") + (String)userVector.elementAt(i);
      allowedUsers[i] = (String)userVector.elementAt(i);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // String variable
    currentUser = new StateVariable("CurrentUser", UPnPConstant.USER_UNKNOWN, true);
    currentUser.setAllowedValueList(allowedUsers);
    userList = new StateVariable("UserList", userListString, true);

    StateVariable[] stateVariableList = {
        currentUser, userList
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getUserList = new Action("GetUserList");
    getUserList.setArgumentTable(new Argument[] {
      new Argument("Users", UPnPConstant.DIRECTION_OUT, userList)
    });
    setUser = new Action("SetUser");
    setUser.setArgumentTable(new Argument[] {
      new Argument("User", UPnPConstant.DIRECTION_IN, currentUser)
    });

    Action[] actionList = {
        getUserList, setUser
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
  public void getUserList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(userListString);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void setUser(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      currentUser.setValue(args[0].getStringValue());

      // trigger event
      if (userEventListener != null)
      {
        userEventListener.userChanged(currentUser.getStringValue());
      }

    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

}

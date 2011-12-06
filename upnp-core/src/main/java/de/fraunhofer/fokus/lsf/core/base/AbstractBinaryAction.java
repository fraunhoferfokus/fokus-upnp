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
package de.fraunhofer.fokus.lsf.core.base;

import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This is the base class for binary actions.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public abstract class AbstractBinaryAction
{

  /** Action name */
  protected String actionName;

  /** Local action ID */
  protected int    actionID;

  /** Argument list for action */
  protected Vector argumentList;

  /**
   * Creates a new instance of BinaryAction.
   * 
   * @param actionName
   * @param actionID
   * @param argumentList
   */
  public AbstractBinaryAction(String actionName, int actionID, Vector argumentList)
  {
    this.actionName = actionName;
    this.actionID = actionID;
    this.argumentList = argumentList;
  }

  /**
   * Checks whether this action is equal to another action. Returns true if all action properties as well as all
   * arguments are equal.
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof AbstractBinaryAction)
    {
      AbstractBinaryAction compareAction = (AbstractBinaryAction)obj;
      // compare action data
      if (actionID != compareAction.getActionID() || !actionName.equals(compareAction.getActionName()))
      {
        return false;
      }
      Portable.println("Compare arguments for " + toString());

      // compare arguments 
      return hasEqualArguments(compareAction);
    }
    return super.equals(obj);
  }

  /** Checks whether two actions provide the same arguments. */
  public boolean hasEqualArguments(AbstractBinaryAction compareAction)
  {
    // compare all arguments, find associated arguments by ID
    for (int i = 0; i < argumentList.size(); i++)
    {
      BinaryArgument currentArgument = getArgument(i);
      BinaryArgument compareArgument = compareAction.getArgumentByID(currentArgument.getArgumentID());
      if (compareArgument == null)
      {
        return false;
      }
      if (!currentArgument.equals(compareArgument))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Retrieves the actionName.
   * 
   * @return The actionName
   */
  public String getActionName()
  {
    return actionName;
  }

  /**
   * Retrieves the actionID.
   * 
   * @return The actionID
   */
  public int getActionID()
  {
    return actionID;
  }

  /**
   * Retrieves the argumentList.
   * 
   * @return The argumentList
   */
  public Vector getArgumentList()
  {
    return argumentList;
  }

  /**
   * Sets the argumentList.
   * 
   * @param argumentList
   *          The new value for argumentList
   */
  public void setArgumentList(Vector argumentList)
  {
    this.argumentList = argumentList;
  }

  /** Retrieves a specific argument */
  public BinaryArgument getArgumentByName(String name)
  {
    for (int i = 0; argumentList != null && i < argumentList.size(); i++)
    {
      if (((BinaryArgument)argumentList.elementAt(i)).getArgumentName().equals(name))
      {
        return (BinaryArgument)argumentList.elementAt(i);
      }
    }
    return null;
  }

  /** Retrieves a specific argument */
  public BinaryArgument getArgumentByID(int id)
  {
    for (int i = 0; argumentList != null && i < argumentList.size(); i++)
    {
      if (((BinaryArgument)argumentList.elementAt(i)).getArgumentID() == id)
      {
        return (BinaryArgument)argumentList.elementAt(i);
      }
    }
    return null;
  }

  /** Retrieves an argument by its index. */
  public BinaryArgument getArgument(int index)
  {
    if (index >= 0 && index < argumentList.size())
    {
      return (BinaryArgument)argumentList.elementAt(index);
    }

    return null;
  }

  /** Checks if this action has input arguments */
  public boolean hasInputArguments()
  {
    for (int i = 0; argumentList != null && i < argumentList.size(); i++)
    {
      if (((BinaryArgument)argumentList.elementAt(i)).isInArgument())
      {
        return true;
      }
    }
    return false;
  }

  /** Checks if this action has output arguments */
  public boolean hasOutputArguments()
  {
    for (int i = 0; argumentList != null && i < argumentList.size(); i++)
    {
      if (!((BinaryArgument)argumentList.elementAt(i)).isInArgument())
      {
        return true;
      }
    }
    return false;
  }

  /** Returns a descriptive string for this action and its arguments. */
  public String toDebugString()
  {
    String result = "";
    result += "ActionID:0x" + Long.toHexString(actionID) + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ActionName:" + actionName;
    // show arguments
    if (getArgumentList().size() > 0)
    {
      for (int i = 0; i < getArgumentList().size(); i++)
      {
        BinaryArgument currentArgument = getArgument(i);
        result += "\r\n      ArgumentDescription[";
        result += "ArgumentName:" + currentArgument.getArgumentName() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
        result += "ArgumentID:0x" + Long.toHexString(currentArgument.getArgumentID());
      }
    }
    return result;
  }

  /** Returns the action name. */
  public String toString()
  {
    return actionName;
  }

}

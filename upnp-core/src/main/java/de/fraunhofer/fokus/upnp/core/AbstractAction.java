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
package de.fraunhofer.fokus.upnp.core;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.Action;

/**
 * This class represent an UPnP action.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class AbstractAction
{

  /**
   * name of the action
   */
  protected String     name;

  /**
   * Argument array of all arguments for action
   */
  protected Argument[] argumentTable;

  /**
   * action produced an error
   */
  private boolean      error            = false;

  /**
   * errorCode
   */
  private int          errorCode        = 0;

  /**
   * errorDescription
   */
  private String       errorDescription = "";

  /**
   * Creates an action object with only action name.
   * 
   * @param name
   *          action name
   */
  public AbstractAction(String name)
  {
    this.name = name;
    argumentTable = null;
  }

  /**
   * Creates an action object with action's name and action's arguments.
   * 
   * @param name
   *          action's name
   * @param argument
   *          action's arguments
   */
  public AbstractAction(String name, Argument[] argument)
  {
    this.name = name;
    this.argumentTable = argument;
  }

  /** Clone this action */
  public Object clone()
  {
    if (argumentTable == null || argumentTable.length == 0)
    {
      return new Action(name);
    } else
    {
      // clone argument list (this does not clone the related state variables)
      Argument[] clonedArgumentList = new Argument[argumentTable.length];
      for (int i = 0; i < clonedArgumentList.length; i++)
      {
        clonedArgumentList[i] = (Argument)argumentTable[i].clone();
      }
      return new Action(name, clonedArgumentList);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }

  /**
   * Returns an action's in argument that is specified by the argument's name.
   * 
   * @param argName
   *          argument's name
   * @return an action's in argument if the action has this argument, otherwise null
   */
  public Argument getInArgument(String argName)
  {
    if (argumentTable == null)
    {
      return null;
    }

    for (int i = 0; i < argumentTable.length; i++)
    {
      if (argumentTable[i].getName().equalsIgnoreCase(argName) &&
        argumentTable[i].getDirection().equalsIgnoreCase("in"))
      {
        return argumentTable[i];
      }
    }

    return null;
  }

  /**
   * Returns an action's out argument that is specified by the argument's name.
   * 
   * @param argName
   *          argument's name
   * @return an action's out argument if the action has this argument, otherwise null
   */
  public Argument getOutArgument(String argName)
  {
    if (argumentTable == null)
    {
      return null;
    }

    for (int i = 0; i < argumentTable.length; i++)
    {
      if (argumentTable[i].getName().equalsIgnoreCase(argName) &&
        argumentTable[i].getDirection().equalsIgnoreCase("out"))
      {
        return argumentTable[i];
      }
    }

    return null;
  }

  /**
   * Returns one or more action's argument that is specified by the argument's name.
   * 
   * @param argName
   *          argument's name
   * @return one or more argument if the action has this argument, otherwise null
   */
  public Argument getArgument(String argName)
  {

    if (argumentTable != null)
    {
      for (int i = 0; i < argumentTable.length; i++)
      {
        if (argumentTable[i].getName().equalsIgnoreCase(argName))
        {
          return argumentTable[i];
        }
      }
    }

    return null;
  }

  /**
   * Returns all action's in argument.
   * 
   * @return in arguments, if action has no in argument, then returns null
   */
  public Argument[] getInArgumentTable()
  {
    Vector v = new Vector();

    if (argumentTable != null)
    {
      for (int i = 0; i < argumentTable.length; i++)
      {
        if (argumentTable[i].getDirection().equalsIgnoreCase("in"))
        {
          v.add(argumentTable[i]);
        }
      }
    }

    if (v.size() == 0)
    {
      return null;
    }

    return toArgumentTable(v);
  }

  /**
   * Returns all action's out argument
   * 
   * @return out arguments, if action has no out arguments, then returns null
   */
  public Argument[] getOutArgumentTable()
  {
    Vector v = new Vector();

    if (argumentTable != null)
    {
      for (int i = 0; i < argumentTable.length; i++)
      {
        if (argumentTable[i].getDirection().equalsIgnoreCase("out"))
        {
          v.add(argumentTable[i]);
        }
      }
    }

    if (v.size() == 0)
    {
      return null;
    }

    return toArgumentTable(v);
  }

  /**
   * Converts an vector of arguments to an Argument[]
   * 
   * @param arguments
   *          in Vector
   * @return arguments in array
   */
  private Argument[] toArgumentTable(Vector v)
  {
    Argument[] args = new Argument[v.size()];

    for (int i = 0; i < v.size(); i++)
    {
      args[i] = (Argument)v.elementAt(i);
    }

    return args;
  }

  /**
   * Checks if the action defines the argument
   * 
   * @param arg
   *          argument
   * @return true if argument exists, false otherwise
   */
  public boolean checkArgument(Argument arg)
  {
    if (argumentTable == null)
    {
      return false;
    }

    for (int i = 0; i < argumentTable.length; i++)
    {
      if (argumentTable[i].equals(arg))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns action's name
   * 
   * @return action's name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns action's arguments
   * 
   * @return arguments or null
   */
  public Argument[] getArgumentTable()
  {
    return argumentTable;
  }

  /**
   * Set a new arguments.
   * 
   * @param argumentList
   *          arguments
   */
  public void setArgumentTable(Argument[] argumentList)
  {
    this.argumentTable = argumentList;
  }

  /**
   * Sets the error message for an action
   * 
   * @param errorCode
   *          UPnP-error code
   * @param errorDescription
   *          UPnP-error description
   * 
   */
  public void setError(int errorCode, String errorDescription)
  {
    this.errorCode = errorCode;
    this.errorDescription = errorDescription;
    error = true;
  }

  /**
   * Clears the last action error
   */
  public void clearError()
  {
    this.errorCode = 0;
    this.errorDescription = "";
    error = false;
  }

  /**
   * Checks for errors
   * 
   * @return True if an error occured, false otherwise. *
   */
  public boolean processingError()
  {
    return error;
  }

  /**
   * Gets the code for an error.
   * 
   * @return The UPnP-error code or 0
   */
  public int getErrorCode()
  {
    return errorCode;
  }

  /**
   * Gets the description for an error.
   * 
   * @return The UPnP-error description or ""
   */
  public String getErrorDescription()
  {
    return errorDescription;
  }

}

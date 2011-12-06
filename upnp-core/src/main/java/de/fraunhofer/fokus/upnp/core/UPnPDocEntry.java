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

import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.XMLHelper;

/**
 * This class contains the documentation for one UPnP action or state variable for a certain service
 * type. It is used by the UsageService.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UPnPDocEntry
{
  /** Associated service type */
  private String    serviceType;

  /** Associated state variable */
  private String    stateVariableName                = null;

  /** Associated action */
  private String    actionName                       = null;

  /** Action description */
  private String    description;

  /** Table with argument descriptions */
  private Hashtable descriptionFromArgumentNameTable = new Hashtable();

  /**
   * Creates a new instance of UPnPDocEntry.
   * 
   * @param serviceType
   *          The associated service type
   */
  public UPnPDocEntry(String serviceType)
  {
    this.serviceType = serviceType;
  }

  /**
   * Retrieves the serviceType.
   * 
   * @return The serviceType
   */
  public String getServiceType()
  {
    return serviceType;
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
   * Retrieves the description.
   * 
   * @return The description
   */
  public String getDescription()
  {
    return description;
  }

  /** Retrieves the description for one argument. */
  public String getArgumentDescription(String argumentName)
  {
    return (String)descriptionFromArgumentNameTable.get(argumentName);
  }

  /**
   * Adds an argument description to this doc entry.
   * 
   * 
   * @param argumentName
   * @param argumentDescription
   */
  public void addArgumentDescription(String argumentName, String argumentDescription)
  {
    descriptionFromArgumentNameTable.put(argumentName, argumentDescription);
  }

  /**
   * Retrieves the stateVariableName.
   * 
   * @return The stateVariableName
   */
  public String getStateVariableName()
  {
    return stateVariableName;
  }

  /**
   * Sets the stateVariableName.
   * 
   * @param stateVariableName
   *          The new value for stateVariableName
   */
  public void setStateVariableName(String stateVariableName)
  {
    this.stateVariableName = stateVariableName;
    this.actionName = null;
  }

  /**
   * Sets the actionName.
   * 
   * @param actionName
   *          The new value for actionName
   */
  public void setActionName(String actionName)
  {
    this.actionName = actionName;
    this.stateVariableName = null;
  }

  /**
   * Sets the description.
   * 
   * @param description
   *          The new value for description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /** Checks if the entry describes an action */
  public boolean isAction()
  {
    return actionName != null;
  }

  /** Checks if the entry describes a state variable */
  public boolean isStateVariable()
  {
    return stateVariableName != null;
  }

  /** Returns the UPnP doc entry as XML fragment. */
  public String toXMLDescription()
  {
    if (actionName != null)
    {
      String result =
        XMLHelper.createStartTag("action") + CommonConstants.NEW_LINE + XMLHelper.createTag("name", actionName) +
          XMLHelper.createTag("description", description) + XMLHelper.createStartTag("argumentList") +
          CommonConstants.NEW_LINE;

      Enumeration arguments = descriptionFromArgumentNameTable.keys();
      while (arguments.hasMoreElements())
      {
        String currentArgumentName = (String)arguments.nextElement();
        result += XMLHelper.createStartTag("argument") + CommonConstants.NEW_LINE;
        result += XMLHelper.createTag("name", currentArgumentName);
        result += XMLHelper.createTag("description", getArgumentDescription(currentArgumentName));
        result += XMLHelper.createEndTag("argument") + CommonConstants.NEW_LINE;
      }
      result += XMLHelper.createEndTag("argumentList") + CommonConstants.NEW_LINE;
      result += XMLHelper.createEndTag("action") + CommonConstants.NEW_LINE;

      return result;
    }
    if (stateVariableName != null)
    {
      String result =
        XMLHelper.createStartTag("stateVariable") + CommonConstants.NEW_LINE +
          XMLHelper.createTag("name", stateVariableName) + XMLHelper.createTag("description", description);
      result += XMLHelper.createEndTag("stateVariable") + CommonConstants.NEW_LINE;

      return result;
    }
    return "";
  }

}

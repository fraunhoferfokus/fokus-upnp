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
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a binary UPnP service.
 * 
 * @author Alexander Koenig
 * 
 */
public abstract class AbstractBinaryService
{

  /** Type of service */
  protected int         serviceType;

  /** Unique ID */
  protected int         serviceID;

  /** Optional service name */
  protected String      serviceName = "";

  /** Physical unit of variable */
  protected String      valueUnit   = "";

  /** Current service value */
  protected BinaryValue value;

  /** Optional action list */
  protected Vector      actionList;

  /** Active state */
  protected boolean     active      = true;

  /** Evented state */
  protected boolean     evented     = true;

  /** Maximum event rate */
  protected int         eventRate   = 0;

  /**
   * Creates a new instance of AbstractBinaryService.
   * 
   * @param serviceType
   * @param serviceID
   * @param valueUnit
   * @param valueType
   */
  public AbstractBinaryService(int serviceType, int serviceID, String valueUnit, int valueType)
  {
    this.serviceType = serviceType;
    this.serviceID = serviceID;
    this.serviceName = null;
    this.valueUnit = valueUnit;
    value = new BinaryValue(valueType);
  }

  /**
   * Checks whether this service is equal to another service. Returns true if all service properties as well as all
   * actions are equal.
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof AbstractBinaryService)
    {
      AbstractBinaryService compareService = (AbstractBinaryService)obj;
      // compare service data
      if (serviceID != compareService.getServiceID() || !serviceName.equals(compareService.getServiceName()) ||
        serviceType != compareService.getServiceType() || !valueUnit.equals(compareService.getValueUnit()))
      {
        Portable.println("Service metadata has changed");
        return false;
      }
      // compare actions
      return hasEqualActions(compareService);
    }
    return super.equals(obj);
  }

  /** Checks whether two services provide the same actions. */
  public boolean hasEqualActions(AbstractBinaryService compareService)
  {
    // compare all actions, find associated actions by ID
    for (int i = 0; i < actionList.size(); i++)
    {
      AbstractBinaryAction currentAction = getAction(i);
      AbstractBinaryAction compareAction = compareService.getActionByID(currentAction.getActionID());
      if (compareAction == null)
      {
        return false;
      }
      if (!currentAction.equals(compareAction))
      {
        return false;
      }
    }
    return true;
  }

  /** Checks if this service is boolean */
  public boolean isBooleanService()
  {
    return value.isBooleanValue();
  }

  /** Checks if this service has a byte array value */
  public boolean isByteArrayService()
  {
    return value.isByteArrayValue();
  }

  /** Checks if this service is numeric */
  public boolean isNumericService()
  {
    return value.isNumericValue();
  }

  /** Checks if this service has a String value */
  public boolean isStringService()
  {
    return value.isTextValue();
  }

  /** Checks if this service provides a value */
  public boolean hasServiceValue()
  {
    return value.getValueType() != BinaryUPnPConstants.VarTypeNotUsed;
  }

  /**
   * Retrieves the serviceID.
   * 
   * @return The serviceID.
   */
  public int getServiceID()
  {
    return serviceID;
  }

  /** Returns the service type. */
  public int getServiceType()
  {
    return serviceType;
  }

  /** Returns the service type as string. */
  public String getServiceTypeString()
  {
    return BinaryUPnPConstants.serviceTypeToString(serviceType);
  }

  /**
   * Retrieves the serviceName.
   * 
   * @return The serviceName
   */
  public String getServiceName()
  {
    return serviceName;
  }

  /**
   * Sets the serviceName.
   * 
   * @param serviceName
   *          The new value for serviceName
   */
  public void setServiceName(String serviceName)
  {
    this.serviceName = serviceName;
  }

  /**
   * Retrieves the valueType.
   * 
   * @return The valueType.
   */
  public int getValueType()
  {
    return value.getValueType();
  }

  /**
   * Retrieves the valueUnit.
   * 
   * @return The valueUnit.
   */
  public String getValueUnit()
  {
    return valueUnit;
  }

  /**
   * Tries to update the locally stored value for this service.
   * 
   * @param value
   *          The byte array representation of the new value
   * 
   * @return True if the value could be set, false otherwise
   */
  public boolean setValueFromByteArray(byte[] value)
  {
    return this.value.fromByteArray(value);
  }

  /** Retrieves the boolean value. */
  public boolean getBooleanValue()
  {
    if (isBooleanService())
    {
      return value.getBooleanValue();
    }

    return false;
  }

  /** Retrieves the numeric value. */
  public long getNumericValue()
  {
    if (isNumericService())
    {
      return value.getNumericValue();
    }

    return 0;
  }

  /** Sets a new numeric value. */
  public void setNumericValue(long newValue) throws ActionFailedException
  {
    if (isNumericService() && newValue != value.getNumericValue())
    {
      value.setNumericValue(newValue);
    }
  }

  /** Retrieves the string value. */
  public String getStringValue()
  {
    if (isStringService())
    {
      return value.getTextValue();
    }

    return null;
  }

  /** Returns the current service value as string */
  public String getValueAsString()
  {
    if (isStringService())
    {
      return value.getTextValue();
    }
    if (isNumericService())
    {
      return value.getNumericValue() + "";
    }
    if (isBooleanService())
    {
      return value.getBooleanValue() ? "True" : "False";
    }
    if (isByteArrayService())
    {
      return StringHelper.byteArrayToMACString(value.toByteArray());
    }
    return "";
  }

  /**
   * Retrieves the actionList.
   * 
   * @return The actionList
   */
  public Vector getActionList()
  {
    return actionList;
  }

  /**
   * Sets the actionList.
   * 
   * @param actionList
   *          The new value for actionList
   */
  public void setActionList(Vector actionList)
  {
    this.actionList = actionList;
  }

  /**
   * Retrieves the value of value.
   * 
   * @return The value of value
   */
  public BinaryValue getValue()
  {
    return value;
  }

  /** Retrieves an action by its index. */
  public AbstractBinaryAction getAction(int index)
  {
    if (index >= 0 && index < actionList.size())
    {
      return (AbstractBinaryAction)actionList.elementAt(index);
    }

    return null;
  }

  /** Retrieves an action by its ID. */
  public AbstractBinaryAction getActionByID(int id)
  {
    for (int i = 0; i < actionList.size(); i++)
    {
      AbstractBinaryAction currentAction = (AbstractBinaryAction)actionList.elementAt(i);
      if (currentAction.getActionID() == id)
      {
        return currentAction;
      }
    }
    return null;
  }

  /** Retrieves an action by its name. */
  public AbstractBinaryAction getAction(String name)
  {
    for (int i = 0; i < actionList.size(); i++)
    {
      AbstractBinaryAction currentAction = (AbstractBinaryAction)actionList.elementAt(i);
      if (currentAction.getActionName().equals(name))
      {
        return currentAction;
      }
    }
    return null;
  }

  /**
   * Retrieves the value of active.
   * 
   * @return The value of active
   */
  public boolean isActive()
  {
    return active;
  }

  /**
   * Sets the new value for active.
   * 
   * @param active
   *          The new value for active
   */
  public void setActive(boolean active)
  {
    this.active = active;
  }

  /**
   * Retrieves the value of evented.
   * 
   * @return The value of evented
   */
  public boolean isEvented()
  {
    return evented;
  }

  /**
   * Sets the new value for evented.
   * 
   * @param evented
   *          The new value for evented
   */
  public void setEvented(boolean evented)
  {
    this.evented = evented;
  }

  /**
   * Retrieves the value of eventRate.
   * 
   * @return The value of eventRate
   */
  public int getEventRate()
  {
    return eventRate;
  }

  /**
   * Sets the new value for eventRate.
   * 
   * @param eventRate
   *          The new value for eventRate
   */
  public void setEventRate(int eventRate)
  {
    this.eventRate = eventRate;
  }

  /** Returns a descriptive string for this service, including value and actions. */
  public String toDebugString()
  {
    String result = "";
    result +=
      "ServiceType:" + BinaryUPnPConstants.serviceTypeToString(serviceType) + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ServiceID:0x" + Long.toHexString(serviceID) + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ServiceValue:";
    if (isBooleanService())
    {
      result += getBooleanValue() ? "True" : "False";
    }
    if (isNumericService())
    {
      result += getNumericValue();
    }
    result += BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ServiceName:" + serviceName;
    // show optional actions
    if (getActionList().size() > 0)
    {
      for (int i = 0; i < getActionList().size(); i++)
      {
        result += "\r\n    ActionDescription[";
        AbstractBinaryAction currentAction = getAction(i);
        result += currentAction.toDebugString();
      }
    }
    return result;
  }

  /** Returns the service type as string. */
  public String toString()
  {
    String result = BinaryUPnPConstants.serviceTypeToString(serviceType);
    if (result.length() == 0)
    {
      result = serviceName;
    }
    return result;
  }

}

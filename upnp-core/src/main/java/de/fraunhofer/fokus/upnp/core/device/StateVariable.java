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
package de.fraunhofer.fokus.upnp.core.device;

import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import de.fraunhofer.fokus.upnp.core.AbstractStateVariable;
import de.fraunhofer.fokus.upnp.core.event.IDeviceStateVariableListener;
import de.fraunhofer.fokus.upnp.core.exceptions.StateVariableException;
import de.fraunhofer.fokus.upnp.core_av.renderer.LastChangeCollector;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class represents the structure of state variable element in the UPnP service description
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class StateVariable extends AbstractStateVariable
{

  /** Listener for state variable changes */
  protected IDeviceStateVariableListener stateVariableChangeListener = null;

  /** Optional last change collector */
  private LastChangeCollector            lastChangeCollector         = null;

  /** Optional instance ID for last change collector */
  private int                            instanceID                  = 0;

  /** Time of last change */
  private long                           lastChange;

  /** Flag for moderated state variables */
  private boolean                        moderated                   = false;

  /** Moderation time for state variable */
  private long                           moderationInterval          = 0;

  /** Minimal delta to trigger a change */
  private double                         minDelta                    = 0;

  /** Value for last event */
  private double                         lastTriggeredValue          = 0;

  /**
   * Creates a new StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, String datatype, Object value, boolean isEvented)
  {
    super(name, datatype, value, isEvented);
  }

  /**
   * Creates a new StateVariable object with a value represented as string
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable as string representation
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, String datatype, String value, boolean isEvented)
  {
    super(name, datatype, value, isEvented);
  }

  /**
   * Creates a numeric state variable
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, String datatype, int value, boolean isEvented)
  {
    this(name, datatype, new Long(value), isEvented);
  }

  /**
   * Creates a "i4" state variable
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, int value, boolean isEvented)
  {
    this(name, "i4", new Long(value), isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, double value, boolean isEvented)
  {
    this(name, "float", new Double(value), isEvented);
  }

  /**
   * Creates a float state variable
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, String datatype, double value, boolean isEvented)
  {
    this(name, datatype, new Double(value), isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, boolean value, boolean isEvented)
  {
    this(name, "boolean", new Boolean(value), isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, char value, boolean isEvented)
  {
    this(name, "char", new Character(value), isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, Character value, boolean isEvented)
  {
    this(name, "char", value, isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, String value, boolean isEvented)
  {
    this(name, "string", value, isEvented);
  }

  /**
   * Creates StateVariable object
   * 
   * @param name
   *          name of state variable
   * @param value
   *          value of state variable
   * @param isEvented
   *          if variable is a send variable or not
   */
  public StateVariable(String name, Date value, boolean isEvented)
  {
    this(name, "dateTime", value, isEvented);
  }

  /**
   * Sets state variable value. If the value has changed and if the variable is evented, then
   * registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setValue(Object newValue) throws StateVariableException, ClassCastException
  {
    if (!dataType.isValid(newValue))
    {
      throw new ClassCastException("Invalid value for " + dataType.getJavaDataType() + "-typed statevariable: " +
        newValue);
    }
    // trigger event if either the value is new or we have a personalized
    // entry with another value
    if (!value.equals(newValue) || personalizedValueTable.size() > 0)
    {
      // new unpersonalized value overrides all personalized values
      personalizedValueTable.clear();

      value = newValue;
      triggerEvents(null);
    }
  }

  /**
   * Tries to set the state variable value. Same as setValue() but errors are discarded silently (in
   * this case, the value is not changed).
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void trySetValue(Object newValue)
  {
    try
    {
      if (!dataType.isValid(newValue))
      {
        throw new ClassCastException("Invalid value for " + dataType.getJavaDataType() + "-typed statevariable: " +
          newValue);
      }
      // trigger event if either the value is new or we have a personalized
      // entry with another value
      if (!value.equals(newValue) || personalizedValueTable.size() > 0)
      {
        // new unpersonalized value overrides all personalized values
        personalizedValueTable.clear();

        value = newValue;
        triggerEvents(null);
      }
    } catch (Exception e)
    {
    }
  }

  /**
   * Sets a personalized state variable value. If the value has changed and if the variable is
   * evented, then registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setPersonalizedValue(Object newValue, RSAPublicKey publicKey) throws StateVariableException,
    ClassCastException
  {
    if (!dataType.isValid(newValue))
    {
      throw new ClassCastException("Invalid value for " + dataType.getJavaDataType() + "-typed statevariable: " +
        newValue);
    }
    if (publicKey == null)
    {
      throw new StateVariableException("Public key for personalized state variable is null");
    }
    // check if personalized value is equal to common value
    boolean isEqual = value != null && value.equals(newValue);
    // if a personalized value already exists, also compare to this value
    if (personalizedValueTable.containsKey(publicKey))
    {
      isEqual &= personalizedValueTable.get(publicKey).equals(newValue);
    }

    if (!isEqual)
    {
      // store or update personalized value in hashtable
      personalizedValueTable.put(publicKey, newValue);
      triggerEvents(publicKey);
    }
  }

  /**
   * Sets state variable value. If the value has changed and if the variable is evented, then
   * registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setNumericValue(long newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Long(newValue));
  }

  /**
   * Sets state variable value. If the value has changed and if the variable is evented, then
   * registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setDateValue(Date newValue) throws StateVariableException, ClassCastException
  {
    setValue(newValue);
  }

  /**
   * Sets state variable value. If the value has changed and if the variable is evented, then
   * registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setDoubleValue(double newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Double(newValue));
  }

  /**
   * Sets state variable value. If the value has changed and if the variable is evented, then
   * registered listeners are notified.
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setBooleanValue(boolean newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Boolean(newValue));
  }

  /**
   * This method triggers all events for the changed variable.
   * 
   * @param publicKey
   *          Optional public key for personalized state variables or null
   */
  protected void triggerEvents(RSAPublicKey publicKey)
  {
    if (isEvented)
    {
      // check for minDelta state variables
      if (Math.abs(minDelta) > 0.01)
      {
        try
        {
          if (dataType.isDoubleValue() && Math.abs(getDoubleValue() - lastTriggeredValue) >= minDelta)
          {
            lastTriggeredValue = getDoubleValue();
            notifyStateVariableChangeListener(publicKey);
          }
          if (dataType.isNumericValue() && Math.abs(getNumericValue() - lastTriggeredValue) >= minDelta)
          {
            lastTriggeredValue = getNumericValue();
            notifyStateVariableChangeListener(publicKey);
          }
        } catch (Exception e)
        {
        }
      } else
      {
        notifyStateVariableChangeListener(publicKey);
      }
    }
    // last change collector is independent from eventing,
    // because non-evented state variables are also added to the
    // last change state variable
    if (lastChangeCollector != null)
    {
      lastChangeCollector.addChange(instanceID, this);
    }
  }

  /**
   * This method checks if a state variable change can be send immediately.
   * 
   * @return True if the event can be send immediately, false otherwise.
   */
  public boolean canSendEvent()
  {
    if (!moderated)
    {
      return true;
    }

    // check if moderation time is over
    if (System.currentTimeMillis() - lastChange > moderationInterval)
    {
      lastChange = System.currentTimeMillis();
      return true;
    }
    return false;
  }

  /**
   * Sets state variable default value
   * 
   * @param newValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   * @throws IllegalArgumentException
   *           if argument is not specified
   */
  public void setDefaultValue(Object newDefaultValue)
  {
    if (!dataType.isValid(newDefaultValue))
    {
      return;
      // throw new ClassCastException("Invalid value for " + dataType.getDataType() + "-typed
      // statevariable: " + newDefaultValue);
    }
    if (!defaultValue.equals(newDefaultValue))
    {
      defaultValue = newDefaultValue;
    }
  }

  /** Returns state variable name and value as XML fragment */
  public String toXMLAttributeDescription()
  {
    return "<" + getName() + " val=\"" + StringHelper.xmlToEscapedString(getValueAsString()) + "\"/>";
  }

  /** Returns state variable name and value as XML fragment */
  public String toXMLAttributeDescription(String additionalAttributes)
  {
    return "<" + getName() + " " + additionalAttributes + " val=\"" +
      StringHelper.xmlToEscapedString(getValueAsString()) + "\"/>";
  }

  /**
   * Retrieves the moderated.
   * 
   * @return The moderated.
   */
  public boolean isModerated()
  {
    return moderated;
  }

  /**
   * Retrieves the lastChangeCollector.
   * 
   * @return The lastChangeCollector.
   */
  public LastChangeCollector getLastChangeCollector()
  {
    return lastChangeCollector;
  }

  /**
   * Sets the lastChangeCollector.
   * 
   * @param lastChangeCollector
   *          The lastChangeCollector to set.
   */
  public void setLastChangeCollector(LastChangeCollector lastChangeCollector)
  {
    this.lastChangeCollector = lastChangeCollector;
  }

  /**
   * Retrieves the instanceID.
   * 
   * @return The instanceID.
   */
  public int getInstanceID()
  {
    return instanceID;
  }

  /**
   * Sets the instanceID.
   * 
   * @param instanceID
   *          The instanceID to set.
   */
  public void setInstanceID(int instanceID)
  {
    this.instanceID = instanceID;
  }

  /**
   * Sets the moderation for a state variable.
   * 
   * @param moderated
   *          Flag to enable/disable moderation
   * @param moderationInterval
   *          Time for moderation in ms
   * 
   */
  public void setModeration(boolean moderated, long moderationInterval)
  {
    this.moderated = moderated;
    this.moderationInterval = moderationInterval;
    // trigger outstanding event if moderation is disabled
    if (!moderated)
    {
      notifyStateVariableChangeListener(null);
    }
  }

  /**
   * Retrieves the minDelta.
   * 
   * @return The minDelta
   */
  public double getMinDelta()
  {
    return minDelta;
  }

  /**
   * Sets the minDelta.
   * 
   * @param minDelta
   *          The new value for minDelta
   */
  public void setMinDelta(double minDelta)
  {
    this.minDelta = minDelta;
  }

  /**
   * Sets a listener for change events for this state variable.
   * 
   * @param enl
   *          state variable value changed listener
   */
  public void setStateVariableChangeListener(IDeviceStateVariableListener listener)
  {
    stateVariableChangeListener = listener;
  }

  /**
   * Notifies the registered listener about a value change.
   * 
   * @param publicKey
   *          Optional public key for personalized state variables or null
   */
  protected void notifyStateVariableChangeListener(RSAPublicKey publicKey)
  {
    if (stateVariableChangeListener != null)
    {
      stateVariableChangeListener.stateVariableChanged(this, publicKey);
    }
  }

}

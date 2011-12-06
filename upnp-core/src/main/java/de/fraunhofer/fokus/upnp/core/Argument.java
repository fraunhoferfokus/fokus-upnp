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

import java.util.Date;

import de.fraunhofer.fokus.upnp.core.exceptions.StateVariableException;
import de.fraunhofer.fokus.upnp.util.Base64Helper;

/**
 * This class represent the structure of an argument element in the UPnP service description.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class Argument
{

  /**
   * name of argument
   */
  private String                name;

  /**
   * direction of argument e.g. in or out argument
   */
  private String                direction;

  /**
   * state variable which is linked to the argument
   */

  private AbstractStateVariable relatedStateVariable;

  /**
   * Value of this argument
   */
  private Object                value;

  /**
   * Creates argument with name, direction and name of related state variable.
   * 
   * @param name
   *          argument's name
   * @param direction
   *          argument's direction
   * @param relatedStateVariable
   *          name of the related state variable
   */
  public Argument(String name, String direction, AbstractStateVariable relatedStateVariable)
  {
    this.name = name;
    this.direction = direction;
    this.relatedStateVariable = relatedStateVariable;
    value = null;
  }

  /** Clones an argument without cloning the value */
  public Object clone()
  {
    Argument result = new Argument(name, direction, relatedStateVariable);

    return result;
  }

  /**
   * Returns argument's name.
   * 
   * @return argument's name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns argument's direction.
   * 
   * @return argument's direction
   */
  public String getDirection()
  {
    return direction;
  }

  /**
   * Returns name of related state variable.
   * 
   * @return name of related state variable
   */
  public AbstractStateVariable getRelatedStateVariable()
  {
    return relatedStateVariable;
  }

  /**
   * Sets the value for this argument
   * 
   * @param newValue
   *          The new value
   * @throws Exception
   *           if the value is not compatible with the state variable datatype
   */
  public void setValue(Object newValue) throws StateVariableException, ClassCastException
  {
    // check new value
    if (!relatedStateVariable.getDataType().isValid(newValue))
    {
      throw new StateVariableException("newValue is not instance of " +
        relatedStateVariable.getDataType().getJavaDataType() + " or not in range or allowed value list");
    }
    value = newValue;
  }

  /**
   * Sets the value for numeric arguments
   * 
   * @param newValue
   *          new value
   * @throws Exception
   *           if the value cannot be set
   */
  public void setNumericValue(long newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Long(newValue));
  }

  /**
   * Sets the value for double arguments
   * 
   * @param newValue
   *          new value
   * @throws Exception
   *           if the value cannot be set
   */
  public void setDoubleValue(double newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Double(newValue));
  }

  /**
   * Sets the value for date arguments
   * 
   * @param newValue
   *          new value
   * @throws Exception
   *           if the value cannot be set
   */
  public void setDateValue(Date newValue) throws StateVariableException, ClassCastException
  {
    setValue(newValue);
  }

  /**
   * Sets the value for boolean arguments
   * 
   * @param newValue
   *          new value
   * @throws Exception
   *           if the value cannot be set
   */
  public void setBooleanValue(boolean newValue) throws StateVariableException, ClassCastException
  {
    setValue(new Boolean(newValue));
  }

  /**
   * Sets the value for bin.base64 arguments
   * 
   * @param newValue
   *          new value
   * @throws Exception
   *           if the value cannot be set
   */
  public void setBinBase64Value(byte[] newValue) throws StateVariableException, ClassCastException
  {
    setValue(Base64Helper.byteArrayToBase64(newValue));
  }

  /**
   * Sets the value for this argument from the string representation of a value
   * 
   * @param newStringValue
   *          new value as string
   * @throws Exception
   *           if the value cannot be set
   */
  public void setValueFromString(String newStringValue) throws Exception
  {
    // check new value
    if (!relatedStateVariable.getDataType().isValidStringRepresentation(newStringValue))
    {
      throw new StateVariableException("newValue is not instance of " +
        relatedStateVariable.getDataType().getJavaDataType() + " or not in range or allowed value list");
    }
    value = relatedStateVariable.getDataType().getValueFromString(newStringValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return getValueAsString();
  }

  /**
   * Returns the current value of the argument
   * 
   * @return current value
   */
  public Object getValue()
  {
    return value;
  }

  /**
   * Returns the current value for a string argument.
   * 
   * @return The current string value
   */
  public String getStringValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isStringValue())
    {
      return (String)value;
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for a base64 argument.
   * 
   * @return The current base64 value
   */
  public byte[] getBinBase64Value() throws Exception
  {
    if (relatedStateVariable.getDataType().isBinBase64Value())
    {
      return Base64Helper.base64ToByteArray((String)value);
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for an URI argument.
   * 
   * @return The current URI value
   */
  public String getURIValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isURIValue())
    {
      return (String)value;
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for a date argument.
   * 
   * @return The current date value
   */
  public Date getDateValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isDateTimeValue())
    {
      return (Date)value;
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for a boolean argument.
   * 
   * @return The current boolean value
   */
  public boolean getBooleanValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isBooleanValue())
    {
      return ((Boolean)value).booleanValue();
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for a numeric argument.
   * 
   * @return The current numeric value
   */
  public long getNumericValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isNumericValue())
    {
      return ((Long)value).longValue();
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value for a double or float argument.
   * 
   * @return The current double or float value
   */
  public double getDoubleValue() throws Exception
  {
    if (relatedStateVariable.getDataType().isDoubleValue())
    {
      return ((Double)value).doubleValue();
    }

    throw new ClassCastException("Invalid type for " + relatedStateVariable.getDataType().getJavaDataType() +
      "-typed statevariable: ");
  }

  /**
   * Returns the current value of the argument as string
   * 
   * @return current value
   */
  public String getValueAsString()
  {
    return getRelatedStateVariable().getDataType().getValueAsString(value);
  }

}

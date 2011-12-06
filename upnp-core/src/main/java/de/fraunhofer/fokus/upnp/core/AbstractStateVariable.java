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

import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.exceptions.StateVariableException;
import de.fraunhofer.fokus.upnp.util.Base64Helper;

/**
 * This is the abstract base class for local and remote state variables.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class AbstractStateVariable
{

  /** Flag for evented state variables */
  protected boolean   isEvented;

  /** Name of the state variable */
  protected String    name;

  /** Datatype for state variable */
  protected DataType  dataType;

  /** Default value of the state variable */
  protected Object    defaultValue;

  /** Current value of the state variable */
  protected Object    value;

  /** Current personalized value of the state variable */
  protected Hashtable personalizedValueTable = new Hashtable();

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
  public AbstractStateVariable(String name, String datatype, Object value, boolean isEvented)
  {
    this.name = name;
    dataType = new DataType(datatype);

    if (value != null && dataType.isValid(value))
    {
      this.value = value;
    } else
    {
      this.value = dataType.getDefaultValue();
    }

    this.defaultValue = dataType.getDefaultValue();
    this.isEvented = isEvented;
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
  public AbstractStateVariable(String name, String datatype, String value, boolean isEvented)
  {
    this.name = name;
    dataType = new DataType(datatype);

    if (value != null && dataType.isValidStringRepresentation(value))
    {
      this.value = dataType.getValueFromString(value);
    } else
    {
      this.value = dataType.getDefaultValue();
    }

    defaultValue = dataType.getDefaultValue();
    this.isEvented = isEvented;
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

  /**
   * Returns the data type value
   * 
   * @return state variable data type value
   */
  public DataType getDataType()
  {
    return dataType;
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public Object getValue()
  {
    return value;
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public Object getPersonalizedValue(RSAPublicKey publicKey)
  {
    // try to return personalized value
    if (publicKey != null && personalizedValueTable.containsKey(publicKey))
    {
      return personalizedValueTable.get(publicKey);
    }
    return value;
  }

  /**
   * Returns the value as string
   * 
   * @return state variable value as string
   */
  public String getValueAsString()
  {
    return getDataType().getValueAsString(value);
  }

  /**
   * Returns the state variable value as string
   * 
   * @return state variable value
   */
  public String getPersonalizedValueAsString(RSAPublicKey publicKey)
  {
    return getDataType().getValueAsString(getPersonalizedValue(publicKey));
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
   * Returns state variable value
   * 
   * @return state variable value
   */
  public long getNumericValue() throws Exception
  {
    if (dataType.isNumericValue())
    {
      return ((Long)value).longValue();
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public double getDoubleValue() throws Exception
  {
    if (dataType.isDoubleValue())
    {
      return ((Double)value).doubleValue();
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns the value of a date time state variable
   * 
   * @return Date represented by this state variable
   */
  public Date getDateValue() throws Exception
  {
    if (dataType.isDateTimeValue())
    {
      return (Date)value;
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public boolean getBooleanValue() throws Exception
  {
    if (dataType.isBooleanValue())
    {
      return ((Boolean)value).booleanValue();
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public String getStringValue() throws Exception
  {
    if (dataType.isStringValue())
    {
      return (String)value;
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public byte[] getBinBase64Value() throws Exception
  {
    if (dataType.isBinBase64Value())
    {
      return Base64Helper.base64ToByteArray((String)value);
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable value
   * 
   * @return state variable value
   */
  public String getURIValue() throws Exception
  {
    if (dataType.isURIValue())
    {
      return (String)value;
    }

    throw new ClassCastException("Invalid type for " + dataType.getDataType() + "-typed statevariable: ");
  }

  /**
   * Returns state variable name
   * 
   * @return state variable name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns UPnP state variable datatype
   * 
   * @return datatype of the state variable
   */
  public String getUPnPDataType()
  {
    return dataType.getDataType();
  }

  /**
   * Returns default value
   * 
   * @return default value of the state variable
   */
  public Object getDefaultValue()
  {
    return defaultValue;
  }

  /**
   * Returns default value as string
   * 
   * @return default value of the state variable
   */
  public String getDefaultValueAsString()
  {
    return dataType.getValueAsString(defaultValue);
  }

  /**
   * Returns state variable send event flag
   * 
   * @return state variable send event flag
   */
  public boolean isEvented()
  {
    return isEvented;
  }

  /**
   * Sets allowed value range
   * 
   * @param allowedValueRange
   *          allowed value range
   */
  public void setAllowedValueRange(AllowedValueRange allowedValueRange)
  {
    dataType.setAllowedValueRange(allowedValueRange);
  }

  /**
   * Sets allowed value range.
   * 
   * @param min
   * @param max
   * @param step
   */
  public void setAllowedValueRange(int min, int max, int step)
  {
    AllowedValueRange valueRange = new AllowedValueRange(min, max);
    valueRange.setStep(step);
    setAllowedValueRange(valueRange);
  }

  /**
   * Sets allowed value list. The array is not copied.
   * 
   * @param allowedValueList
   *          allowed value list
   */
  public void setAllowedValueList(String[] allowedValueList)
  {
    dataType.setAllowedValueList(allowedValueList);
  }

  /** Sets an allowed value list from a comma-separated string list. */
  public void setAllowedValueList(String valueList)
  {
    StringTokenizer tokenizer = new StringTokenizer(valueList, ",");
    Vector valueVector = new Vector();
    while (tokenizer.hasMoreTokens())
    {
      valueVector.add(tokenizer.nextToken().trim());
    }
    String[] allowedValues = new String[valueVector.size()];
    for (int i = 0; i < valueVector.size(); i++)
    {
      allowedValues[i] = (String)valueVector.elementAt(i);
    }
    setAllowedValueList(allowedValues);
  }

  /**
   * Returns allowed value range
   * 
   * @return the allowed value range
   */
  public AllowedValueRange getAllowedValueRange()
  {
    return dataType.getAllowedValueRange();
  }

  /**
   * Returns allowed value list. The array is not copied.
   * 
   * @return allowed value list
   */
  public String[] getAllowedValueList()
  {
    return dataType.getAllowedValueList();
  }

}

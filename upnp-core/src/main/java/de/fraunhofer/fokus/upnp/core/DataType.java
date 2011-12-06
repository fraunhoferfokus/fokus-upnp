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
import java.util.GregorianCalendar;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class represents a specific UPnP data type with optional constraints (e.g. an allowed value
 * list)
 * 
 * @author Alexander Koenig
 * 
 */
public class DataType
{

  /** UPnP data type */
  private String            datatype;

  /** The range of values allowed for this type */
  private AllowedValueRange allowedValueRange;

  /** A string array of allowed strings */
  private String[]          allowedValueList;

  /**
   * Creates a DataType object
   * 
   * @param datatype
   *          type of value
   */
  public DataType(String datatype)
  {
    this.datatype = datatype;

    allowedValueRange = null;
    allowedValueList = null;
  }

  /** Checks if the numeric value can be assigned to this object */
  public boolean isValidLongValue(long value)
  {
    if (!isNumericValue())
    {
      return false;
    }

    // check for allowed value range
    if (allowedValueRange != null)
    {
      if (!allowedValueRange.isInRange(new Long(value)))
      {
        return false;
      }
    }

    // check for default value ranges of data types
    if (datatype.equalsIgnoreCase("ui1"))
    {
      return value >= 0 && value <= 255;
    }

    if (datatype.equalsIgnoreCase("i1"))
    {
      return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
    }

    if (datatype.equalsIgnoreCase("ui2"))
    {
      return value >= 0 && value <= 65535;
    }

    if (datatype.equalsIgnoreCase("i2"))
    {
      return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    if (datatype.equalsIgnoreCase("ui4"))
    {
      return value >= 0 && value <= 4294967296l;
    }

    if (datatype.equalsIgnoreCase("i4") || datatype.equalsIgnoreCase("int"))
    {
      return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
    }

    return false;
  }

  /** Checks if the double value can be assigned to this object */
  public boolean isValidDoubleValue(double value)
  {
    if (!isDoubleValue())
    {
      return false;
    }

    // check for allowed value range
    if (allowedValueRange != null)
    {
      if (!allowedValueRange.isInRange(new Double(value)))
      {
        return false;
      }
    }
    return true;
  }

  /** Checks if the string value can be assigned to this object */
  public boolean isValidStringValue(String value)
  {
    if (!isStringValue())
    {
      return false;
    }

    if (allowedValueList == null)
    {
      return true;
    }

    for (int i = 0; i < allowedValueList.length; i++)
    {
      if (allowedValueList[i].equals(value))
      {
        return true;
      }
    }

    return false;
  }

  /** Checks if the bin.base64 is valid */
  public boolean isValidBinBase64Value(String value)
  {
    if (!isBinBase64Value())
    {
      return false;
    }

    return Base64Helper.base64ToByteArray(value) != null;
  }

  /** Checks if the bin.hex is valid */
  public boolean isValidBinHexValue(String value)
  {
    if (!isBinHexValue())
    {
      return false;
    }

    return StringHelper.binHexToByteArray(value) != null;
  }

  /** Checks if the uuid is valid */
  public boolean isValidUUIDValue(String value)
  {
    if (!isUUIDValue())
    {
      return false;
    }

    return StringHelper.binHexToByteArray(value) != null;
  }

  /** Checks if value has the correct type for an assignment to this object */
  public boolean isValidStringRepresentation(String value)
  {
    try
    {
      if (isNumericValue())
      {
        long longValue = new Long(value).longValue();
        return isValidLongValue(longValue);
      }
      if (isDoubleValue())
      {
        double doubleValue = new Double(value).doubleValue();
        return isValidDoubleValue(doubleValue);
      }
      if (isCharValue())
      {
        return value != null;
      }
      if (isStringValue())
      {
        return isValidStringValue(value);
      }
      if (isBinBase64Value())
      {
        return isValidBinBase64Value(value);
      }
      if (isBinHexValue())
      {
        return isValidBinHexValue(value);
      }
      if (isUUIDValue())
      {
        return isValidUUIDValue(value);
      }
      if (isDateTimeValue())
      {
        return DateTimeHelper.getDateFromUPnP(value) != null;
      }
      if (isBooleanValue())
      {
        if (value.equals("1") || value.equals("0"))
        {
          return true;
        }

        return new Boolean(value) != null;
      }
      if (isURIValue())
      {
        return true;
      }
    } catch (Exception ex)
    {
    }
    // unknown or wrong class type
    return false;
  }

  /** Checks if value has the correct type for an assignment to this object */
  public boolean isValid(Object value)
  {
    if (isNumericValue() && value instanceof Long)
    {
      return isValidLongValue(((Long)value).longValue());
    }
    if (isDoubleValue() && value instanceof Double)
    {
      return isValidDoubleValue(((Double)value).doubleValue());
    }
    if (isCharValue())
    {
      return value instanceof Character;
    }
    if (isStringValue() && value instanceof String)
    {
      return isValidStringValue((String)value);
    }
    if (isBinBase64Value() && value instanceof String)
    {
      return isValidBinBase64Value((String)value);
    }
    if ((isBinHexValue() || isUUIDValue()) && value instanceof String)
    {
      return value instanceof String;
    }
    if (isDateTimeValue())
    {
      return value instanceof Date;
    }
    if (isBooleanValue())
    {
      return value instanceof Boolean;
    }
    if (isURIValue())
    {
      return value instanceof String;
    }
    // unknown or wrong class type
    return false;
  }

  /**
   * Returns a value as string
   * 
   * @return A value as string
   */
  public String getValueAsString(Object value)
  {
    if (value == null)
    {
      return "";
    }

    if (isNumericValue())
    {
      return ((Long)value).toString();
    }
    if (isDoubleValue())
    {
      return ((Double)value).toString();
    }
    if (isCharValue())
    {
      return ((Character)value).toString();
    }
    if (isStringValue() || isBinBase64Value() || isBinHexValue() || isUUIDValue() || isURIValue())
    {
      return (String)value;
    }
    if (isDateTimeValue())
    {
      return DateTimeHelper.formatDateForUPnP((Date)value);
    }
    if (isBooleanValue())
    {
      return ((Boolean)value).toString();
    }

    return "";
  }

  /**
   * Returns a value from its string representation
   * 
   * @return A value as parsed from a string
   */
  public Object getValueFromString(String value)
  {
    // do range checks etc.
    if (!isValidStringRepresentation(value))
    {
      return null;
    }

    try
    {
      if (isNumericValue())
      {
        return new Long(value);
      }
      if (isDoubleValue())
      {
        return new Double(value);
      }
      if (isCharValue())
      {
        return new Character(value.charAt(0));
      }
      if (isStringValue() || isBinBase64Value() || isBinHexValue() || isUUIDValue() || isURIValue())
      {
        return value;
      }
      if (isDateTimeValue())
      {
        return DateTimeHelper.getDateFromUPnP(value);
      }
      if (isBooleanValue())
      {
        return new Boolean(StringHelper.stringToBoolean(value));
      }
    } catch (Exception ex)
    {
    }
    return null;
  }

  /**
   * Returns the UPnP datatype
   * 
   * @return datatype of the value
   */
  public String getDataType()
  {
    return datatype;
  }

  /**
   * Returns the datatype as String
   * 
   * @return datatype of the value
   */
  public String getJavaDataType()
  {
    if (isNumericValue())
    {
      return "Long";
    }
    if (isDoubleValue())
    {
      return "Double";
    }
    if (isCharValue())
    {
      return "Character";
    }
    if (isStringValue())
    {
      return "String";
    }
    if (isDateTimeValue())
    {
      return "Date";
    }
    if (isBooleanValue())
    {
      return "Boolean";
    }
    if (isBinBase64Value() || isBinHexValue())
    {
      return "String";
    }
    if (isURIValue())
    {
      return "String";
    }
    if (isUUIDValue())
    {
      return "String";
    }

    return "Unknown type";
  }

  /**
   * Returns default value
   * 
   * @return default value of the data type
   */
  public Object getDefaultValue()
  {
    if (isNumericValue())
    {
      return new Long(0);
    }
    if (isDoubleValue())
    {
      return new Double(0.0);
    }
    if (isCharValue())
    {
      return new Character('\0');
    }
    if (isStringValue() || isUUIDValue())
    {
      return "";
    }
    if (isDateTimeValue())
    {
      return new GregorianCalendar(1980, 0, 1, 0, 0).getTime();
    }
    if (isBooleanValue())
    {
      return new Boolean(false);
    }
    if (isBinBase64Value())
    {
      return "A===";
    }
    if (isBinHexValue())
    {
      return "00";
    }
    if (isURIValue())
    {
      return "";
    }
    return "";
  }

  /**
   * Sets allowed value range
   * 
   * @param allowedValueRange
   *          allowed value range
   */
  public void setAllowedValueRange(AllowedValueRange allowedValueRange)
  {
    this.allowedValueRange = allowedValueRange;
  }

  /**
   * Sets allowed value list. The array is not copied.
   * 
   * @param allowedValueList
   *          allowed value list
   */
  public void setAllowedValueList(String[] allowedValueList)
  {
    this.allowedValueList = allowedValueList;
  }

  /**
   * Returns allowed value range
   * 
   * @return the allowed value range
   */
  public AllowedValueRange getAllowedValueRange()
  {
    return allowedValueRange;
  }

  /**
   * Returns the minimal allowed value
   * 
   * @return the minimal allowed value
   */
  public long getMinimalNumericValue()
  {
    if (!isNumericValue() || datatype.equalsIgnoreCase("ui1") || datatype.equalsIgnoreCase("ui2") ||
      datatype.equalsIgnoreCase("ui4"))
    {
      return 0;
    }
    if (allowedValueRange != null)
    {
      return Math.round(allowedValueRange.getMin());
    }

    if (datatype.equalsIgnoreCase("i1"))
    {
      return Byte.MIN_VALUE;
    }

    if (datatype.equalsIgnoreCase("i2"))
    {
      return Short.MIN_VALUE;
    }

    if (datatype.equalsIgnoreCase("i4") || datatype.equalsIgnoreCase("int"))
    {
      return Integer.MIN_VALUE;
    }

    return 0;
  }

  /**
   * Returns the maximal allowed value
   * 
   * @return the maximal allowed value
   */
  public long getMaximalNumericValue()
  {
    if (!isNumericValue())
    {
      return 0;
    }
    if (allowedValueRange != null)
    {
      return Math.round(allowedValueRange.getMax());
    }

    if (datatype.equalsIgnoreCase("i1"))
    {
      return Byte.MAX_VALUE;
    }

    if (datatype.equalsIgnoreCase("ui1"))
    {
      return 255;
    }

    if (datatype.equalsIgnoreCase("i2"))
    {
      return Short.MAX_VALUE;
    }

    if (datatype.equalsIgnoreCase("ui2"))
    {
      return 65535;
    }

    if (datatype.equalsIgnoreCase("i4") || datatype.equalsIgnoreCase("int"))
    {
      return Integer.MAX_VALUE;
    }

    if (datatype.equalsIgnoreCase("ui4"))
    {
      return 4294967296l;
    }

    return 0;
  }

  /**
   * Returns allowed value list. The array is not copied.
   * 
   * @return allowed value list
   */
  public String[] getAllowedValueList()
  {
    return allowedValueList;
  }

  /**
   * Tests if value datatype is string.
   * 
   * @return true if this value is from datatype string, false otherwise
   */
  public boolean isStringValue()
  {
    return this.datatype.equalsIgnoreCase("string");
  }

  /**
   * Tests if value datatype is char
   * 
   * @return true if this value is from datatype char false otherwise
   */
  public boolean isCharValue()
  {
    return this.datatype.equalsIgnoreCase("char");
  }

  /**
   * Tests if value datatype is boolean
   * 
   * @return true if this value is from datatype boolean, false otherwise
   */
  public boolean isBooleanValue()
  {
    return this.datatype.equalsIgnoreCase("boolean");
  }

  /**
   * Tests if value datatype is bin.base64
   * 
   * @return true if this value is of datatype bin.base64, false otherwise
   */
  public boolean isBinBase64Value()
  {
    return this.datatype.equalsIgnoreCase("bin.base64");
  }

  /**
   * Tests if value datatype is bin.hex
   * 
   * @return true if this value is of datatype bin.hex, false otherwise
   */
  public boolean isBinHexValue()
  {
    return this.datatype.equalsIgnoreCase("bin.hex");
  }

  /**
   * Tests if value datatype is URI
   * 
   * @return true if this value is of datatype uri, false otherwise
   */
  public boolean isURIValue()
  {
    return this.datatype.equalsIgnoreCase("uri");
  }

  /**
   * Tests if value datatype is uuid
   * 
   * @return true if this value is of datatype uuid, false otherwise
   */
  public boolean isUUIDValue()
  {
    return this.datatype.equalsIgnoreCase("uuid");
  }

  /**
   * @return true if this value is a numeric type, false otherwise
   */
  public boolean isNumericValue()
  {
    return datatype.equalsIgnoreCase("ui1") || datatype.equalsIgnoreCase("ui2") || datatype.equalsIgnoreCase("ui4") ||
      datatype.equalsIgnoreCase("i1") || datatype.equalsIgnoreCase("i2") || datatype.equalsIgnoreCase("i4") ||
      datatype.equalsIgnoreCase("int");
  }

  /**
   * @return true if this value is a floating type, false otherwise
   */
  public boolean isDoubleValue()
  {
    return datatype.equalsIgnoreCase("float") || datatype.equalsIgnoreCase("r4") || datatype.equalsIgnoreCase("r8") ||
      datatype.equalsIgnoreCase("number") || datatype.equalsIgnoreCase("fixed.14.4");
  }

  /**
   * @return true if this value is a date time type, false otherwise
   */
  public boolean isDateTimeValue()
  {
    return datatype.equalsIgnoreCase("date") || datatype.equalsIgnoreCase("dateTime") ||
      datatype.equalsIgnoreCase("dateTime.tz") || datatype.equalsIgnoreCase("time") ||
      datatype.equalsIgnoreCase("time.tz");
  }

}

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

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class encapsulates a value for a service or an action argument.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryValue
{

  private int       valueType;

  /** Current value for numeric services */
  protected long    numericValue;

  /** Current value for String or URL services */
  protected String  textValue;

  /** Current value for byte array services */
  protected byte[]  byteArrayValue       = new byte[0];

  /** Current value for boolean services */
  protected boolean booleanValue;

  protected byte[]  compositeDescription = null;

  /**
   * Creates a new instance of BinaryValue.
   * 
   * @param argumentType
   */
  public BinaryValue(int argumentType)
  {
    this.valueType = argumentType;
  }

  /**
   * Retrieves the argumentType.
   * 
   * @return The argumentType
   */
  public int getValueType()
  {
    return valueType;
  }

  /** Checks if this service is boolean */
  public boolean isBooleanValue()
  {
    return valueType == BinaryUPnPConstants.VarTypeBoolean;
  }

  /** Checks if this service is numeric */
  public boolean isNumericValue()
  {
    return valueType == BinaryUPnPConstants.VarTypeINT8 || valueType == BinaryUPnPConstants.VarTypeUINT8 ||
      valueType == BinaryUPnPConstants.VarTypeINT16 || valueType == BinaryUPnPConstants.VarTypeUINT16 ||
      valueType == BinaryUPnPConstants.VarTypeINT32 || valueType == BinaryUPnPConstants.VarTypeUINT32 ||
      valueType == BinaryUPnPConstants.VarTypeINT64 || valueType == BinaryUPnPConstants.VarTypeUINT64;
  }

  /** Checks if this service has a string or URL value */
  public boolean isTextValue()
  {
    return valueType == BinaryUPnPConstants.VarTypeString || valueType == BinaryUPnPConstants.VarTypeURL;
  }

  /** Checks if this service has a byte array value */
  public boolean isByteArrayValue()
  {
    return valueType == BinaryUPnPConstants.VarTypeByteArray;
  }

  /** Checks if this service has a composite value */
  public boolean isCompositeValue()
  {
    return valueType == BinaryUPnPConstants.VarTypeComposite;
  }

  /**
   * 
   * @return
   */
  public boolean getBooleanValue()
  {
    if (isBooleanValue())
    {
      return booleanValue;
    }

    return false;
  }

  /**
   * 
   * @param newValue
   */
  public void setBooleanValue(boolean newValue)
  {
    if (isBooleanValue() && newValue != booleanValue)
    {
      booleanValue = newValue;
    }
  }

  /** Sets the value from a received byte array */
  public boolean fromByteArray(byte[] data)
  {
    if (data == null || data.length == 0)
    {
      return false;
    }
    try
    {
      if (isBooleanValue())
      {
        booleanValue = data[0] != 0;
        return true;
      }
      if (isNumericValue())
      {
        if (valueType == BinaryUPnPConstants.VarTypeUINT8)
        {
          numericValue = data[0] & 0xFF;
          return true;
        }
        if (valueType == BinaryUPnPConstants.VarTypeINT8)
        {
          numericValue = data[0];
          return true;
        }
        if (valueType == BinaryUPnPConstants.VarTypeUINT16)
        {
          numericValue = ByteArrayHelper.byteArrayToUInt16(data, 0);
          return true;
        }
        if (valueType == BinaryUPnPConstants.VarTypeINT16)
        {
          numericValue = ByteArrayHelper.byteArrayToInt16(data, 0);
          return true;
        }
        if (valueType == BinaryUPnPConstants.VarTypeUINT32)
        {
          numericValue = ByteArrayHelper.byteArrayToUInt32(data, 0);
          return true;
        }
        if (valueType == BinaryUPnPConstants.VarTypeINT32)
        {
          numericValue = ByteArrayHelper.byteArrayToInt32(data, 0);
          return true;
        }
      }
      if (isTextValue())
      {
        int length = data[0] & 0xFF;
        byte[] resultData = new byte[length];
        Portable.arraycopy(data, 1, resultData, 0, length);

        textValue = StringHelper.byteArrayToString(resultData);
        return true;
      }
      if (isByteArrayValue() || isCompositeValue())
      {
        byteArrayValue = new byte[data.length];
        Portable.arraycopy(data, 0, byteArrayValue, 0, data.length);
        return true;
      }
    } catch (Exception e)
    {
    }
    return false;
  }

  /** Sets the value from a received string */
  public boolean fromString(String data)
  {
    if (data == null || data.length() == 0)
    {
      return false;
    }
    try
    {
      if (isBooleanValue())
      {
        booleanValue = StringHelper.stringToBoolean(data);
        return true;
      }
      if (isNumericValue())
      {
        try
        {
          long value = new Long(data).longValue();
          if (valueType == BinaryUPnPConstants.VarTypeUINT8)
          {
            if (value >= 0 && value <= 0xFF)
            {
              numericValue = value;
              return true;
            }
          }
          if (valueType == BinaryUPnPConstants.VarTypeINT8)
          {
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
            {
              numericValue = value;
              return true;
            }
          }
          if (valueType == BinaryUPnPConstants.VarTypeUINT16)
          {
            if (value >= 0 && value <= 0xFFFF)
            {
              numericValue = value;
              return true;
            }
          }
          if (valueType == BinaryUPnPConstants.VarTypeINT16)
          {
            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            {
              numericValue = value;
              return true;
            }
          }
          if (valueType == BinaryUPnPConstants.VarTypeUINT32)
          {
            if (value >= 0 && value <= Long.parseLong("FFFFFFFF", 16))
            {
              numericValue = value;
              return true;
            }
          }
          if (valueType == BinaryUPnPConstants.VarTypeINT32)
          {
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
            {
              numericValue = value;
              return true;
            }
          }
        } catch (Exception e)
        {
          Portable.println("Value not set");
          return false;
        }
      }
      if (isTextValue())
      {
        textValue = data;
        return true;
      }
      if (isByteArrayValue() || isCompositeValue())
      {
        byteArrayValue = StringHelper.binHexToByteArray(data);
        if (byteArrayValue != null)
        {
          return true;
        } else
        {
          byteArrayValue = new byte[0];
        }
      }
    } catch (Exception e)
    {
    }
    Portable.println("Value not set");
    return false;
  }

  /**
   * 
   * @return
   */
  public long getNumericValue()
  {
    if (isNumericValue())
    {
      return numericValue;
    }

    return 0;
  }

  /**
   * 
   * @param newValue
   */
  public void setNumericValue(long newValue)
  {
    if (isNumericValue() && newValue != numericValue)
    {
      numericValue = newValue;
    }
  }

  /**
   * 
   * @return
   */
  public String getTextValue()
  {
    if (isTextValue())
    {
      return textValue;
    }

    return null;
  }

  /**
   * 
   * @param newValue
   */
  public void setTextValue(String newValue)
  {
    if (isTextValue() && !newValue.equals(textValue))
    {
      textValue = newValue;
    }
  }

  /**
   * Retrieves the value of compositeDescription.
   * 
   * @return The value of compositeDescription
   */
  public byte[] getCompositeDescription()
  {
    return compositeDescription;
  }

  /**
   * Sets the new value for compositeDescription.
   * 
   * @param compositeDescription
   *          The new value for compositeDescription
   */
  public void setCompositeDescription(byte[] compositeDescription)
  {
    this.compositeDescription = compositeDescription;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String result = "";
    if (isTextValue())
    {
      result += textValue;
    }
    if (isNumericValue())
    {
      result += numericValue;
    }
    if (isBooleanValue())
    {
      result += booleanValue ? "True" : "False";
    }
    if (isByteArrayValue() || isCompositeValue())
    {
      result += StringHelper.byteArrayToHexDebugString(byteArrayValue);
    }
    return result;
  }

  /** Returns the byte array representation for this value. */
  public byte[] toByteArray()
  {
    byte[] valueData = null;

    if (valueType == BinaryUPnPConstants.VarTypeByteArray)
    {
      valueData = byteArrayValue;
    }
    if (valueType == BinaryUPnPConstants.VarTypeString || valueType == BinaryUPnPConstants.VarTypeURL)
    {
      valueData = StringHelper.stringToByteArray(textValue);
    }
    if (isBooleanValue())
    {
      valueData = new byte[] {
        (byte)(getBooleanValue() ? 1 : 0)
      };
    }
    if (isNumericValue())
    {
      if (valueType == BinaryUPnPConstants.VarTypeINT8 || valueType == BinaryUPnPConstants.VarTypeUINT8)
      {
        valueData = new byte[] {
          (byte)(numericValue & 0xFF)
        };
      }
      if (valueType == BinaryUPnPConstants.VarTypeINT16 || valueType == BinaryUPnPConstants.VarTypeUINT16)
      {
        valueData = ByteArrayHelper.uint16ToByteArray((int)numericValue);
      }
      if (valueType == BinaryUPnPConstants.VarTypeINT32 || valueType == BinaryUPnPConstants.VarTypeUINT32)
      {
        valueData = ByteArrayHelper.uint32ToByteArray(numericValue);
      }
    }
    return valueData;
  }
}

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

import java.io.ByteArrayOutputStream;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates arguments for binary UPnP actions.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryArgument
{

  private boolean     inArgument;

  private String      argumentName;

  private int         argumentID;

  /** Argument value */
  private BinaryValue argumentValue;

  /**
   * Creates a new instance of BinaryCPArgument.
   * 
   * @param argumentName
   * @param argumentID
   * @param argumentType
   * @param inArgument
   */
  public BinaryArgument(String argumentName, int argumentID, int argumentType, boolean inArgument)
  {
    this.argumentName = argumentName;
    this.argumentID = argumentID;
    argumentValue = new BinaryValue(argumentType);
    this.inArgument = inArgument;
  }

  public boolean equals(Object obj)
  {
    if (obj instanceof BinaryArgument)
    {
      BinaryArgument compareArgument = (BinaryArgument)obj;

      Portable.println("Compare " + toDebugString() + " to " + compareArgument.toDebugString());

      // compare argument data
      return argumentID == compareArgument.getArgumentID() && argumentName.equals(compareArgument.getArgumentName()) &&
        inArgument == compareArgument.isInArgument() && getArgumentType() == compareArgument.getArgumentType();
    }
    return super.equals(obj);
  }

  /** Returns the argument name and value */
  public String toString()
  {
    return argumentName + ":" + argumentValue.toString();
  }

  /** Returns a descriptive string for this argument and its value. */
  public String toDebugString()
  {
    String result = "ID:" + Long.toHexString(argumentID) + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "Name:" + argumentName + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "In:" + inArgument + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "Type:" + getArgumentType();

    return result;
  }

  /**
   * Retrieves the argumentID.
   * 
   * @return The argumentID
   */
  public int getArgumentID()
  {
    return argumentID;
  }

  /**
   * Retrieves the argumentName.
   * 
   * @return The argumentName
   */
  public String getArgumentName()
  {
    return argumentName;
  }

  /**
   * Retrieves the argumentType.
   * 
   * @return The argumentType
   */
  public int getArgumentType()
  {
    return argumentValue.getValueType();
  }

  /**
   * Retrieves the inArgument.
   * 
   * @return The inArgument
   */
  public boolean isInArgument()
  {
    return inArgument;
  }

  /** Checks if this service is boolean */
  public boolean isBooleanArgument()
  {
    return argumentValue.isBooleanValue();
  }

  /** Checks if this service is numeric */
  public boolean isNumericArgument()
  {
    return argumentValue.isNumericValue();
  }

  /** Checks if this service has a string or URL value */
  public boolean isTextArgument()
  {
    return argumentValue.isTextValue();
  }

  /** Checks if this service has a byte array value */
  public boolean isByteArrayArgument()
  {
    return argumentValue.isByteArrayValue();
  }

  /**
   * 
   * @return
   */
  public boolean getBooleanValue()
  {
    if (isBooleanArgument())
    {
      return argumentValue.getBooleanValue();
    }
    return false;
  }

  /**
   * 
   * @param newValue
   * @throws ActionFailedException
   */
  public void setBooleanValue(boolean newValue)
  {
    if (isBooleanArgument() && newValue != argumentValue.getBooleanValue())
    {
      argumentValue.setBooleanValue(newValue);
    }
  }

  /** Tries to set the value from a received byte array */
  public boolean trySetValueFromByteArray(byte[] data)
  {
    return argumentValue.fromByteArray(data);
  }

  /**
   * 
   * @return
   */
  public long getNumericValue()
  {
    if (isNumericArgument())
    {
      return argumentValue.getNumericValue();
    }
    return 0;
  }

  /**
   * 
   * @param newValue
   * @throws ActionFailedException
   */
  public void setNumericValue(long newValue)
  {
    if (isNumericArgument() && newValue != argumentValue.getNumericValue())
    {
      argumentValue.setNumericValue(newValue);
    }
  }

  /**
   * 
   * @return
   */
  public String getTextValue()
  {
    if (isTextArgument())
    {
      return argumentValue.getTextValue();
    }
    return null;
  }

  /**
   * 
   * @param newValue
   * @throws ActionFailedException
   */
  public void setTextValue(String newValue)
  {
    if (isTextArgument() && !newValue.equals(argumentValue.getTextValue()))
    {
      argumentValue.setTextValue(newValue);
    }
  }

  /** Returns the byte array for this argument suitable for an action. */
  public byte[] toByteArrayForAction()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] valueData = argumentValue.toByteArray();

      // add prefix
      byte[] prefix =
        new byte[] {
            BinaryUPnPConstants.UnitTypeArgumentContainer, (byte)(5 + valueData.length),
            BinaryUPnPConstants.UnitTypeArgumentID, 1, (byte)(argumentID & 0xFF),
            BinaryUPnPConstants.UnitTypeArgumentValue, (byte)valueData.length,
        };
      byteArrayOutputStream.write(prefix);
      byteArrayOutputStream.write(valueData);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

  /** Returns the byte array for this argument suitable for the device description. */
  public byte[] toByteArrayForDescription()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      byte[] argumentNameData = StringHelper.stringToByteArray(argumentName);

      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeArgumentDescriptionContainer);
      byteArrayOutputStream.write(11 + argumentNameData.length);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeArgumentID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(argumentID);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeArgumentName);
      byteArrayOutputStream.write(argumentNameData.length);
      byteArrayOutputStream.write(argumentNameData);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeArgumentDirection);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(isInArgument() ? BinaryUPnPConstants.ArgumentDirectionIn
        : BinaryUPnPConstants.ArgumentDirectionOut);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeValueType);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(argumentValue.getValueType());

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }
}

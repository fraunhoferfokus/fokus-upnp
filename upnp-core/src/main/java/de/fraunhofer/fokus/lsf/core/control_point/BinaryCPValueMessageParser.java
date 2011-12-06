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
package de.fraunhofer.fokus.lsf.core.control_point;

import java.util.Hashtable;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;

/**
 * This class is used to parse value messages (control response and event messages).
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryCPValueMessageParser
{
  /** Parsed device ID */
  private long      deviceID              = -1;

  /** Parsed service ID */
  private int       serviceID             = -1;

  /** Parsed action ID */
  private int       actionID              = -1;

  /** Parsed result */
  private byte      result                = -1;

  /** Parsed service values */
  private Hashtable serviceValueTable     = new Hashtable();

  /** Parsed out arguments table */
  private Hashtable outArgumentValueTable = new Hashtable();

  /** Flag for event */
  private boolean   eventMessage          = false;

  /** Flag for device request */
  private boolean   deviceMessage         = false;

  /**
   * Parses a received event or control response message.
   * 
   * @param message
   *          The message
   * 
   * @return True if the message was parsed successfully, false otherwise
   */
  public void parse(BinaryMessageObject message) throws ActionFailedException
  {
    byte[] messageData = message.getBody();
    int offset = 0;
    int loops = 0;
    int currentArgumentID = -1;
    int currentServiceID = -1;

    Portable.println(BinaryUPnPConstants.toDebugString(messageData));

    while (offset < messageData.length && loops < BinaryCPMessageParser.MAX_LOOP_COUNT)
    {
      byte unitType = messageData[offset++];
      if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
      {
        break;
      }
      if (unitType == BinaryUPnPConstants.UnitTypePadding)
      {
        continue;
      }
      int length = BinaryUPnPConstants.decodeUnitLength(messageData[offset++] & 0xFF);

      // handle packet units
      // skip containers
      if (unitType == BinaryUPnPConstants.UnitTypeActionContainer ||
        unitType == BinaryUPnPConstants.UnitTypeArgumentContainer ||
        unitType == BinaryUPnPConstants.UnitTypeServiceContainer)
      {
        length = 0;
      }
      // handle common tupels
      if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
      {
        deviceID =
          ((messageData[offset] & 0xFF) << 24) + ((messageData[offset + 1] & 0xFF) << 16) +
            ((messageData[offset + 2] & 0xFF) << 8) + (messageData[offset + 3] & 0xFF);
      }
      if (unitType == BinaryUPnPConstants.UnitTypeServiceID)
      {
        serviceID = messageData[offset] & 0xFF;
        currentServiceID = messageData[offset] & 0xFF;
      }
      // handle event tupels
      if (unitType == BinaryUPnPConstants.UnitTypeEvent)
      {
        eventMessage = true;
      }
      if (unitType == BinaryUPnPConstants.UnitTypeServiceValue)
      {
        // right now, we don't know the type of the value, so we store it as byte array
        byte[] serviceValue = new byte[length];
        Portable.arraycopy(messageData, offset, serviceValue, 0, serviceValue.length);
        serviceValueTable.put(new Integer(currentServiceID), serviceValue);
      }
      // handle result tupels
      if (unitType == BinaryUPnPConstants.UnitTypeSetDeviceNameResult ||
        unitType == BinaryUPnPConstants.UnitTypeSetDeviceApplicationResult)
      {
        deviceMessage = true;
      }
      // handle result tupels
      if (unitType == BinaryUPnPConstants.UnitTypeActionResult ||
        unitType == BinaryUPnPConstants.UnitTypeServiceValueResult ||
        unitType == BinaryUPnPConstants.UnitTypeSetDeviceNameResult ||
        unitType == BinaryUPnPConstants.UnitTypeSetDeviceApplicationResult)
      {
        result = messageData[offset];
      }
      if (unitType == BinaryUPnPConstants.UnitTypeActionID)
      {
        actionID = messageData[offset] & 0xFF;
      }
      if (unitType == BinaryUPnPConstants.UnitTypeArgumentID)
      {
        currentArgumentID = messageData[offset] & 0xFF;
      }
      if (unitType == BinaryUPnPConstants.UnitTypeArgumentValue)
      {
        // right now, we don't know the type of the value, so we store it as byte array
        byte[] argumentValue = new byte[length];
        Portable.arraycopy(messageData, offset, argumentValue, 0, argumentValue.length);
        outArgumentValueTable.put(new Integer(currentArgumentID), argumentValue);
      }
      offset += length;
      loops++;
    }
    // missing device data
    if (deviceID == -1)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoDevice);
    }
    if (!deviceMessage && serviceID == -1)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoService);
    }
    // received a result
    if (result != -1)
    {
      return;
    }
    if (eventMessage && serviceValueTable.size() > 0)
    {
      return;
    }
    if (eventMessage)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidMessage);
    } else
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidResponseMessage);
    }
  }

  /**
   * Retrieves the deviceID.
   * 
   * @return The deviceID
   */
  public long getDeviceID()
  {
    return deviceID;
  }

  /**
   * Retrieves the serviceID.
   * 
   * @return The serviceID
   */
  public int getServiceID()
  {
    return serviceID;
  }

  /**
   * Checks if the message is a ok response
   * 
   * @return True if this is a ok response, false otherwise
   */
  public boolean isOkValueResponse()
  {
    return result == BinaryUPnPConstants.ResultTypeOk;
  }

  /**
   * Checks if the message is an action ok response
   * 
   * @return True if this is an action ok response, false otherwise
   */
  public boolean isOkActionResponse()
  {
    return result == BinaryUPnPConstants.ResultTypeOk && actionID != -1;
  }

  /**
   * Retrieves the value of serviceValueTable.
   * 
   * @return The value of serviceValueTable
   */
  public Hashtable getServiceValueTable()
  {
    return serviceValueTable;
  }

  /**
   * Retrieves the value of outArgumentValueTable.
   * 
   * @return The value of outArgumentValueTable
   */
  public Hashtable getOutArgumentValueTable()
  {
    return outArgumentValueTable;
  }

  /**
   * Retrieves the value of result.
   * 
   * @return The value of result
   */
  public byte getResult()
  {
    return result;
  }

}

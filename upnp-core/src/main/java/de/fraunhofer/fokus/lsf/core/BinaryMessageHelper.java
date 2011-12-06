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
package de.fraunhofer.fokus.lsf.core;

import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class is used to parse device messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryMessageHelper
{

  public static final int MAX_LOOP_COUNT = 100;

  /** Searches the device ID in a message. */
  public static long getDeviceIDFromMessage(byte[] messageData)
  {
    try
    {
      int offset = 0;
      int loops = 0;

      while (offset < messageData.length && loops < MAX_LOOP_COUNT)
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
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
        {
          return ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        offset += length;
        loops++;
      }
    } catch (Exception e)
    {
    }
    return 0;
  }

  /**
   * Parses a message for forwarding to find access and response entities
   * 
   * 
   * @param messageData
   *          The message
   * @param tupelList
   *          Contains all parsed tupels which are neither access nor response entities
   * @param accessEntityList
   *          List with parsed access entities
   * @param responseEntityList
   *          List with parsed response entities
   */
  public static void parseMessageForForwarding(byte[] messageData,
    Vector tupelList,
    Vector accessEntityList,
    Vector responseEntityList)
  {
    try
    {
      int offset = 0;
      int loops = 0;

      byte[] accessForwarderAddress = null;
      long accessID = 0;
      int accessForwarderID = 0xFF;
      byte[] accessDescriptionPort = null;
      byte[] accessControlPort = null;
      byte[] accessEventPort = null;
      int accessForwarderPhyType = 0xFF;

      byte[] responseForwarderAddress = null;
      long responseID = 0;
      int responseForwarderID = 0xFF;
      byte[] responseForwarderPort = null;

      boolean handled = false;

      while (offset < messageData.length && loops < MAX_LOOP_COUNT)
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
        handled = false;
        // handle packet units

        if (accessEntityList != null)
        {
          // marks a new access entity
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderAddress)
          {
            accessForwarderAddress = new byte[length];
            Portable.arraycopy(messageData, offset, accessForwarderAddress, 0, length);

            accessID = 0;
            accessForwarderID = 0xFF;
            accessDescriptionPort = null;
            accessControlPort = null;
            accessEventPort = null;
            accessForwarderPhyType = 0xFF;
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeAccessID)
          {
            accessID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderDescriptionPort)
          {
            accessDescriptionPort = new byte[length];
            Portable.arraycopy(messageData, offset, accessDescriptionPort, 0, length);
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderControlPort)
          {
            accessControlPort = new byte[length];
            Portable.arraycopy(messageData, offset, accessControlPort, 0, length);
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderEventPort)
          {
            accessEventPort = new byte[length];
            Portable.arraycopy(messageData, offset, accessEventPort, 0, length);
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderPhyType)
          {
            accessForwarderPhyType = messageData[offset] & 0xFF;
            handled = true;
          }
          // marks the end of one access entity
          if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderID)
          {
            accessForwarderID = messageData[offset] & 0xFF;
            if (accessForwarderAddress != null)
            {
              GatewayData gatewayData = new GatewayData(accessForwarderAddress, accessForwarderID);
              if (accessID != 0)
              {
                gatewayData.setID(accessID);
              }
              if (accessDescriptionPort != null)
              {
                gatewayData.setDescriptionPort(accessDescriptionPort);
              }
              if (accessControlPort != null)
              {
                gatewayData.setControlPort(accessControlPort);
              }
              if (accessEventPort != null)
              {
                gatewayData.setEventPort(accessEventPort);
              }
              if (accessForwarderPhyType != 0xFF)
              {
                gatewayData.setForwarderPhyType(accessForwarderPhyType);
              }
              accessEntityList.add(gatewayData);
            }
            handled = true;
          }
        }
        if (responseEntityList != null)
        {
          // marks a new response entity
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderAddress)
          {
            responseForwarderAddress = new byte[length];
            Portable.arraycopy(messageData, offset, responseForwarderAddress, 0, length);

            responseID = 0;
            responseForwarderID = 0xFF;
            responseForwarderPort = null;
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseID)
          {
            responseID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
            handled = true;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderPort)
          {
            responseForwarderPort = new byte[length];
            Portable.arraycopy(messageData, offset, responseForwarderPort, 0, length);
            handled = true;
          }
          // marks the end of one response entity
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderID)
          {
            responseForwarderID = messageData[offset] & 0xFF;
            if (responseForwarderAddress != null)
            {
              GatewayData gatewayData = new GatewayData(responseForwarderAddress, responseForwarderID);
              if (responseID != 0)
              {
                gatewayData.setID(responseID);
              }
              if (responseForwarderPort != null)
              {
                gatewayData.setResponsePort(responseForwarderPort);
              }
              responseEntityList.add(gatewayData);
            }
            handled = true;
          }
        }
        if (!handled)
        {
          // copy the complete tupel, including unitType and unitLength
          byte[] completeTupelData = new byte[length + 2];
          completeTupelData[0] = unitType;
          completeTupelData[1] = (byte)BinaryUPnPConstants.encodeUnitLength(length);
          Portable.arraycopy(messageData, offset, completeTupelData, 2, length);

          MessageTupel messageTupel = new MessageTupel(completeTupelData);
          tupelList.add(messageTupel);
        }
        offset += length;
        loops++;
      }
    } catch (Exception e)
    {
    }
  }

  /**
   * Builds a message from normal message tupels and gateway entities.
   * 
   * 
   * @param tupelList
   * @param accessEntityList
   * @param responseEntityList
   * @return
   */
  public static byte[] toByteArray(Vector tupelList, Vector accessEntityList, Vector responseEntityList)
  {
    int length = 0;
    for (int i = 0; i < tupelList.size(); i++)
    {
      length += ((MessageTupel)tupelList.elementAt(i)).getTupelLength();
    }
    if (responseEntityList != null)
    {
      // response entities first
      for (int i = 0; i < responseEntityList.size(); i++)
      {
        length += ((GatewayData)responseEntityList.elementAt(i)).toByteArrayForResponse().length;
      }
    }
    if (accessEntityList != null)
    {
      for (int i = 0; i < accessEntityList.size(); i++)
      {
        length += ((GatewayData)accessEntityList.elementAt(i)).toByteArrayForAccess().length;
      }
    }
    byte[] result = new byte[length + 1];
    int offset = 0;
    for (int i = 0; i < tupelList.size(); i++)
    {
      length = ((MessageTupel)tupelList.elementAt(i)).getTupelLength();
      Portable.arraycopy(((MessageTupel)tupelList.elementAt(i)).getTupelContent(), 0, result, offset, length);
      offset += length;
    }
    // response entities first
    if (responseEntityList != null)
    {
      for (int i = 0; i < responseEntityList.size(); i++)
      {
        byte[] data = ((GatewayData)responseEntityList.elementAt(i)).toByteArrayForResponse();
        Portable.arraycopy(data, 0, result, offset, data.length);
        offset += data.length;
      }
    }
    if (accessEntityList != null)
    {
      for (int i = 0; i < accessEntityList.size(); i++)
      {
        byte[] data = ((GatewayData)accessEntityList.elementAt(i)).toByteArrayForAccess();
        Portable.arraycopy(data, 0, result, offset, data.length);
        offset += data.length;
      }
    }
    result[result.length - 1] = BinaryUPnPConstants.UnitTypeEndOfPacket;
    return result;
  }
}

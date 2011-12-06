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
package de.fraunhofer.fokus.lsf.core.device;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;

/**
 * This class contains the parse result for one received message.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryDeviceMessageParser
{

  public static final int MAX_LOOP_COUNT = 100;

  /**
   * Processes a received device search message.
   * 
   * @param message
   *          The device message
   * 
   * @return True if the device should answer the search, false otherwise
   */
  public static BinaryDeviceMessageParserResult processMessage(BinaryMessageObject message)
  {
    try
    {
      BinaryDeviceMessageParserResult result = new BinaryDeviceMessageParserResult();

      byte[] messageData = message.getBody();
      int offset = 0;
      int loops = 0;

      byte[] entityResponseAddress = null;
      byte[] entityResponsePort = null;
      int entityResponseForwarderID = -1;
      long entityResponseID = -1;

      while (offset < messageData.length && loops < MAX_LOOP_COUNT)
      {
        byte unitType = messageData[offset++];
        if (unitType != BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          int length = messageData[offset++] & 0xFF;
          // handle packet units
          if (unitType == BinaryUPnPConstants.UnitTypeSearchDevice ||
            unitType == BinaryUPnPConstants.UnitTypeDeviceAnnouncement ||
            unitType == BinaryUPnPConstants.UnitTypeDeviceRemoval ||
            unitType == BinaryUPnPConstants.UnitTypeGetDeviceDescription ||
            unitType == BinaryUPnPConstants.UnitTypeDeviceDescription ||
            unitType == BinaryUPnPConstants.UnitTypeGetServiceDescription ||
            unitType == BinaryUPnPConstants.UnitTypeServiceDescription ||
            unitType == BinaryUPnPConstants.UnitTypeGetServiceValue ||
            unitType == BinaryUPnPConstants.UnitTypeSetServiceValue ||
            unitType == BinaryUPnPConstants.UnitTypeInvokeAction ||
            unitType == BinaryUPnPConstants.UnitTypeSetDeviceName ||
            unitType == BinaryUPnPConstants.UnitTypeSetDeviceNameResult ||
            unitType == BinaryUPnPConstants.UnitTypeSetDeviceApplication ||
            unitType == BinaryUPnPConstants.UnitTypeSetDeviceApplicationResult)
          {
            result.setMessageType(unitType);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
          {
            result.setDeviceID(ByteArrayHelper.byteArrayToUInt32(messageData, offset));
          }
          if (unitType == BinaryUPnPConstants.UnitTypeDeviceType)
          {
            result.setDeviceType(messageData[offset] & 0xFF);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeServiceType)
          {
            result.setServiceType(messageData[offset] & 0xFF);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeServiceID)
          {
            result.setServiceID(messageData[offset] & 0xFF);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderAddress)
          {
            entityResponseAddress = new byte[length];
            Portable.arraycopy(messageData, offset, entityResponseAddress, 0, length);
            entityResponsePort = null;
            entityResponseID = -1;
            entityResponseForwarderID = -1;
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderPort)
          {
            entityResponsePort = new byte[length];
            Portable.arraycopy(messageData, offset, entityResponsePort, 0, length);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseID)
          {
            entityResponseID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
          }
          if (unitType == BinaryUPnPConstants.UnitTypeResponseForwarderID)
          {
            entityResponseForwarderID = messageData[offset] & 0xFF;
            // check if a complete gateway description has been found
            if (entityResponseForwarderID != -1 && entityResponseAddress != null)
            {
              GatewayData gatewayData = null;
              gatewayData = new GatewayData(entityResponseAddress, entityResponseForwarderID);
              // add optional properties
              if (entityResponsePort != null)
              {
                gatewayData.setResponsePort(entityResponsePort);
              }
              if (entityResponseID != 0)
              {
                gatewayData.setID(entityResponseID);
              }
              result.getResponseEntityList().add(gatewayData);
            }
            entityResponseAddress = null;
          }
          offset += length;
          loops++;
        } else
        {
          offset = messageData.length;
        }
      }
      return result;
    } catch (Exception e)
    {
    }
    return null;
  }

}

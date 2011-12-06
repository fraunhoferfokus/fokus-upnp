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

import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.BinaryArgument;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;

/**
 * This class is used to parse device messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryCPMessageParser
{

  public static final int MAX_LOOP_COUNT = 100;

  /**
   * Processes a ping reply message.
   * 
   * @param message
   *          The message
   * 
   * @return True if this was a ping reply, false otherwise
   */
  public static boolean processPingReply(BinaryMessageObject message)
  {
    try
    {
      byte[] messageData = message.getBody();

      return messageData != null && messageData.length == 3 &&
        messageData[0] == BinaryUPnPConstants.UnitTypePingReply && messageData[1] == 0 &&
        messageData[2] == BinaryUPnPConstants.UnitTypeEndOfPacket;
    } catch (Exception e)
    {
    }
    return false;
  }

  /**
   * Processes a received device discovery message.
   * 
   * @param message
   *          The device message
   * 
   * @return The parsed device info or null
   */
  public static BinaryCPDeviceInfo processDeviceAnnouncement(BinaryMessageObject message)
  {
    try
    {
      byte[] messageData = message.getBody();
      int offset = 0;
      int loops = 0;
      // address used to access the device
      InetAddress accessAddress = message.getSourceAddress().getAddress();
      long deviceID = -1;
      int deviceType = -1;
      long deviceDescriptionDate = -1;
      int descriptionPort = BinaryUPnPConstants.DescriptionPort;
      int controlPort = BinaryUPnPConstants.ControlPort;
      int eventPort = BinaryUPnPConstants.EventMulticastPort;
      byte[] entityAccessAddress = null;
      byte[] entityAccessForwarderDescriptionPort = null;
      byte[] entityAccessForwarderControlPort = null;
      int entityAccessForwarderPhyType = -1;
      int entityAccessForwarderID = -1;
      long entityAccessID = -1;
      Vector accessEntityList = new Vector();

      //      Portable.println("\r\nFrom " + IPHelper.toString(message.getSourceAddress()) + "\r\n  " +
      //        BinaryUPnPConstants.toDebugString(messageData));

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
          if (unitType != BinaryUPnPConstants.UnitTypeDeviceAnnouncement)
          {
            return null;
          }
        }
        // handle packet units
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceDescriptionDate)
        {
          deviceDescriptionDate = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
        {
          deviceID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceType)
        {
          deviceType = messageData[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceDescriptionPort)
        {
          descriptionPort = ByteArrayHelper.byteArrayToUInt16(messageData, offset);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceControlPort)
        {
          controlPort = ByteArrayHelper.byteArrayToUInt16(messageData, offset);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceEventPort)
        {
          eventPort = ByteArrayHelper.byteArrayToUInt16(messageData, offset);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderAddress)
        {
          entityAccessAddress = new byte[length];
          Portable.arraycopy(messageData, offset, entityAccessAddress, 0, length);
          entityAccessForwarderDescriptionPort = null;
          entityAccessForwarderID = -1;
          entityAccessForwarderPhyType = 0;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderPhyType)
        {
          entityAccessForwarderPhyType = messageData[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderDescriptionPort)
        {
          entityAccessForwarderDescriptionPort = new byte[length];
          Portable.arraycopy(messageData, offset, entityAccessForwarderDescriptionPort, 0, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderControlPort)
        {
          entityAccessForwarderControlPort = new byte[length];
          Portable.arraycopy(messageData, offset, entityAccessForwarderControlPort, 0, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessID)
        {
          entityAccessID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeAccessForwarderID)
        {
          entityAccessForwarderID = messageData[offset] & 0xFF;
          // check if a complete gateway description has been found
          if (entityAccessForwarderID != -1 && entityAccessAddress != null)
          {
            GatewayData gatewayData = null;
            gatewayData = new GatewayData(entityAccessAddress, entityAccessForwarderID);
            // add optional properties
            if (entityAccessForwarderDescriptionPort != null)
            {
              gatewayData.setDescriptionPort(entityAccessForwarderDescriptionPort);
            }
            if (entityAccessForwarderControlPort != null)
            {
              gatewayData.setControlPort(entityAccessForwarderControlPort);
            }
            if (entityAccessForwarderPhyType != 0)
            {
              gatewayData.setForwarderPhyType(entityAccessForwarderPhyType);
            }
            if (entityAccessID != 0)
            {
              gatewayData.setID(entityAccessID);
            }
            accessEntityList.add(gatewayData);
          }
          entityAccessAddress = null;
        }
        offset += length;
        loops++;
      }
      // if gathered information is sufficient, create device
      if (deviceDescriptionDate != -1 && deviceID != -1)
      {
        BinaryCPDeviceInfo deviceInfo = new BinaryCPDeviceInfo(accessAddress, deviceDescriptionDate, deviceID);
        deviceInfo.setDescriptionPort(descriptionPort);
        deviceInfo.setControlPort(controlPort);
        deviceInfo.setEventPort(eventPort);
        if (deviceType != -1)
        {
          deviceInfo.setDeviceType(deviceType);
        }

        // determine device address
        if (accessEntityList.size() == 0)
        {
          // no intermediate gateways, device address is equal to access address
          deviceInfo.setDeviceAddress(accessAddress.getAddress());
        } else
        {
          // device address is part of first access entity
          deviceInfo.setAccessEntityList(accessEntityList);
          GatewayData gatewayData = deviceInfo.getAccessEntityData(0);
          deviceInfo.setDeviceAddress(gatewayData.getForwarderAddress());
        }
        return deviceInfo;
      }
    } catch (Exception e)
    {
    }
    return null;
  }

  /**
   * Processes a received device removal message.
   * 
   * @param message
   *          The device message
   * 
   * @return The parsed device ID or -1
   */
  public static long processDeviceRemoval(BinaryMessageObject message)
  {
    try
    {
      byte[] messageData = message.getBody();
      int offset = 0;
      int loops = 0;
      long deviceID = -1;

      //      Portable.println("\r\nFrom " + IPHelper.toString(message.getSourceAddress()) + "\r\n  " +
      //        BinaryUPnPConstants.toDebugString(messageData));

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
          if (unitType != BinaryUPnPConstants.UnitTypeDeviceRemoval)
          {
            return -1;
          }
        }
        // handle packet units
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
        {
          deviceID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        offset += length;
        loops++;
      }
      return deviceID;
    } catch (Exception e)
    {
    }
    return -1;
  }

  /**
   * Processes a received device message.
   * 
   * @param deviceInfo
   * @param message
   *          The device message
   * 
   * @return The parsed device or null
   */
  public static BinaryCPDevice processDeviceDescription(BinaryCPDeviceInfo deviceInfo, BinaryMessageObject message)
  {
    try
    {
      byte[] messageData = message.getBody();
      int offset = 0;
      int loops = 0;
      long deviceDescriptionDate = -1;
      int lifeTime = 0;
      long deviceID = -1;
      int deviceType = 0;
      Vector serviceList = new Vector();
      String name = "";
      String application = "";
      String manufacturer = "";
      boolean externalServiceDescriptions = false;

      //      Portable.println(BinaryUPnPConstants.toDebugString(messageData));

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
        // handle packet units
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceDescriptionDate)
        {
          deviceDescriptionDate = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceExpectedLifeTime)
        {
          lifeTime = (messageData[offset] & 0xFF) * 256 + (messageData[offset + 1] & 0xFF);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
        {
          deviceID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceType)
        {
          deviceType = messageData[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceName)
        {
          name = StringHelper.byteArrayToString(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceApplication)
        {
          application = StringHelper.byteArrayToString(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceManufacturer)
        {
          manufacturer = StringHelper.byteArrayToString(messageData, offset, length);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceExternalDescriptions)
        {
          externalServiceDescriptions = true;
        }
        // process service descriptions in extra method
        if (unitType == BinaryUPnPConstants.UnitTypeServiceDescriptionContainer)
        {
          //            Portable.println("Parse service description with size " + length);
          byte[] serviceDescription = new byte[length];
          Portable.arraycopy(messageData, offset, serviceDescription, 0, length);

          // Portable.println("ServiceDescription:");
          // for (int i = 0; i < serviceDescription.length; i++)
          // {
          // System.out.print(serviceDescription[i] + " ");
          // }
          // Portable.println("");
          BinaryCPService service = processServiceDescription(serviceDescription);
          if (service != null)
          {
            serviceList.add(service);
          }
        }
        offset += length;
        loops++;
      }
      // if gathered information is sufficient, create device
      if (lifeTime != 0 && deviceID != -1 && deviceInfo.getDeviceID() == deviceID)
      {
        BinaryCPDevice device = new BinaryCPDevice(deviceInfo, lifeTime);
        // update device description date from description (overrides discovery info)  
        device.setDeviceDescriptionDate(deviceDescriptionDate);
        device.setDeviceType(deviceType);
        device.setDescriptionPort(deviceInfo.getDescriptionPort());
        device.setControlPort(deviceInfo.getControlPort());
        device.setEventPort(deviceInfo.getEventPort());
        device.setApplication(application);
        device.setName(name);
        device.setManufacturer(manufacturer);
        device.setDescriptionMessage(messageData);
        device.setExternalServiceDescriptions(externalServiceDescriptions);
        // associate services with device
        for (int i = 0; i < serviceList.size(); i++)
        {
          ((BinaryCPService)serviceList.elementAt(i)).setBinaryCPDevice(device);
        }
        device.setServiceList(serviceList);

        return device;
      }
    } catch (Exception e)
    {
    }
    return null;
  }

  /**
   * Processes an external service description message.
   * 
   * @param message
   *          The message
   * 
   * @return The parsed service or null
   */
  public static BinaryCPService processExternalServiceDescription(BinaryMessageObject message, BinaryCPDevice device)
  {
    try
    {
      byte[] messageData = message.getBody();
      int offset = 0;
      int loops = 0;
      long deviceID = -1;

      //      Portable.println(BinaryUPnPConstants.toDebugString(messageData));

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
        // handle packet units
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceID)
        {
          deviceID = ByteArrayHelper.byteArrayToInt64(messageData, offset, length);
          if (deviceID != device.getDeviceID())
          {
            return null;
          }
        }
        // process service descriptions in extra method
        if (unitType == BinaryUPnPConstants.UnitTypeServiceDescriptionContainer)
        {
          //          Portable.println("Parse service description with size " + length);
          byte[] serviceDescription = new byte[length];
          Portable.arraycopy(messageData, offset, serviceDescription, 0, length);

          BinaryCPService service = processServiceDescription(serviceDescription);
          return service;
        }
        offset += length;
        loops++;
      }
      return null;
    } catch (Exception e)
    {
    }
    return null;
  }

  /**
   * Processes a service description.
   * 
   * @param serviceDescription
   *          The service description
   */
  public static BinaryCPService processServiceDescription(byte[] serviceDescription)
  {
    try
    {
      int offset = 0;
      int loops = 0;

      int serviceType = -1;
      int serviceID = -1;
      String serviceName = null;
      // may be empty for complex services
      int valueType = 0;
      String valueUnit = "";
      Vector actionList = new Vector();

      while (offset < serviceDescription.length && loops < MAX_LOOP_COUNT)
      {
        byte unitType = serviceDescription[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          continue;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(serviceDescription[offset++] & 0xFF);
        if (unitType == BinaryUPnPConstants.UnitTypeServiceType)
        {
          serviceType = serviceDescription[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeServiceID)
        {
          serviceID = serviceDescription[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeValueType)
        {
          valueType = serviceDescription[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeValueUnit && length > 0)
        {
          byte[] unitData = new byte[length];
          Portable.arraycopy(serviceDescription, offset, unitData, 0, length);
          valueUnit = URLHelper.escapedURLToString(StringHelper.byteArrayToString(unitData));
        }
        if (unitType == BinaryUPnPConstants.UnitTypeServiceName)
        {
          byte[] nameData = new byte[length];
          Portable.arraycopy(serviceDescription, offset, nameData, 0, length);
          serviceName = StringHelper.byteArrayToString(nameData);
        }
        // process action descriptions in extra method
        if (unitType == BinaryUPnPConstants.UnitTypeActionDescriptionContainer)
        {
          //          Portable.println("Parse action description with size " + length);

          byte[] actionDescription = new byte[length];
          Portable.arraycopy(serviceDescription, offset, actionDescription, 0, length);

          BinaryCPAction action = processActionDescription(actionDescription);
          if (action != null)
          {
            actionList.add(action);
          }
        }
        offset += length;
        loops++;
      }
      if (serviceType != -1 && serviceID != -1)
      {
        BinaryCPService service = null;

        // return appropriate subclasses for certain sensors
        if (serviceType == BinaryUPnPConstants.ServiceTypeTemperaturSensor)
        {
          service = new TemperatureSensorBinaryCPService(serviceType, serviceID, valueUnit, valueType);
        }
        if (service == null)
        {
          service = new BinaryCPService(serviceType, serviceID, valueUnit, valueType);
        }
        // set optional service name
        if (serviceName != null)
        {
          service.setServiceName(serviceName);
        }
        // associate actions with service
        for (int i = 0; i < actionList.size(); i++)
        {
          ((BinaryCPAction)actionList.elementAt(i)).setBinaryCPService(service);
        }
        service.setActionList(actionList);

        return service;
      }
    } catch (Exception e)
    {
      Portable.println("Error during parsing: " + e.getMessage());
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Processes an action description.
   * 
   * @param actionDescription
   *          The action description
   */
  public static BinaryCPAction processActionDescription(byte[] actionDescription)
  {
    try
    {
      int offset = 0;
      int loops = 0;

      int actionID = -1;
      String actionName = null;
      Vector argumentList = new Vector();

      while (offset < actionDescription.length && loops < MAX_LOOP_COUNT)
      {
        byte unitType = actionDescription[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          continue;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(actionDescription[offset++] & 0xFF);
        if (unitType == BinaryUPnPConstants.UnitTypeActionID)
        {
          actionID = actionDescription[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeActionName)
        {
          byte[] nameData = new byte[length];
          Portable.arraycopy(actionDescription, offset, nameData, 0, length);
          actionName = StringHelper.byteArrayToString(nameData);
        }
        // process argument descriptions in extra method
        if (unitType == BinaryUPnPConstants.UnitTypeArgumentDescriptionContainer)
        {
          //          Portable.println("Parse argument description with size " + length);

          byte[] argumentDescription = new byte[length];
          Portable.arraycopy(actionDescription, offset, argumentDescription, 0, length);

          BinaryArgument argument = processArgumentDescription(argumentDescription);
          if (argument != null)
          {
            argumentList.add(argument);
          }
        }
        // process packed argument descriptions in extra method
        if (unitType == BinaryUPnPConstants.UnitTypeArgumentPackedDescriptionContainer)
        {
          //          Portable.println("Parse packed argument description with size " + length);

          byte[] argumentDescription = new byte[length];
          Portable.arraycopy(actionDescription, offset, argumentDescription, 0, length);

          BinaryArgument argument = processPackedArgumentDescription(argumentDescription);
          if (argument != null)
          {
            argumentList.add(argument);
          }
        }
        offset += length;
        loops++;
      }
      if (actionID != -1 && actionName != null)
      {
        BinaryCPAction action = new BinaryCPAction(actionName, actionID, argumentList);
        action.setArgumentList(argumentList);

        return action;
      }
    } catch (Exception e)
    {
    }

    return null;
  }

  /**
   * Processes an argument description.
   * 
   * @param argumentDescription
   *          The argument description
   */
  public static BinaryArgument processArgumentDescription(byte[] argumentDescription)
  {
    // Portable.println("Argument description:");
    // for (int i = 0; i < argumentDescription.length; i++)
    // {
    // System.out.print(argumentDescription[i] + " ");
    // }
    // Portable.println("");
    try
    {
      int offset = 0;
      int loops = 0;

      int argumentID = -1;
      String argumentName = null;
      boolean inArgument = true;
      int argumentType = -1;

      while (offset < argumentDescription.length && loops < MAX_LOOP_COUNT)
      {
        byte unitType = argumentDescription[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          continue;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(argumentDescription[offset++] & 0xFF);
        if (unitType == BinaryUPnPConstants.UnitTypeArgumentID)
        {
          argumentID = argumentDescription[offset] & 0xFF;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeArgumentDirection)
        {
          inArgument = (argumentDescription[offset] & 0xFF) == BinaryUPnPConstants.ArgumentDirectionIn;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeArgumentName)
        {
          byte[] nameData = new byte[length];
          Portable.arraycopy(argumentDescription, offset, nameData, 0, length);
          argumentName = StringHelper.byteArrayToString(nameData);
        }
        if (unitType == BinaryUPnPConstants.UnitTypeValueType)
        {
          argumentType = argumentDescription[offset] & 0xFF;
        }
        offset += length;
        loops++;
      }
      if (argumentID != -1 && argumentName != null && argumentType != -1)
      {
        BinaryArgument argument = new BinaryArgument(argumentName, argumentID, argumentType, inArgument);

        return argument;
      }
    } catch (Exception e)
    {
    }

    return null;
  }

  /**
   * Processes a packed argument description.
   * 
   * @param argumentDescription
   *          The argument description
   */
  public static BinaryArgument processPackedArgumentDescription(byte[] argumentDescription)
  {
    try
    {
      int nameLength = argumentDescription.length - 3;

      int argumentID = argumentDescription[0] & 0xFF;
      boolean inArgument = (argumentDescription[1] & 0xFF) == BinaryUPnPConstants.ArgumentDirectionIn;
      int argumentType = argumentDescription[2] & 0xFF;
      String argumentName = StringHelper.byteArrayToString(argumentDescription, 3, nameLength);

      return new BinaryArgument(argumentName, argumentID, argumentType, inArgument);
    } catch (Exception e)
    {
    }

    return null;
  }
}

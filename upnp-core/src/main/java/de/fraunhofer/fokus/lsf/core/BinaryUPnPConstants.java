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

import java.net.InetAddress;
import java.util.Calendar;

import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryService;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

public class BinaryUPnPConstants
{
  public static final String DEBUG_STRING_DIVIDER                       = ", ";

  public static String       BinaryUPnPMulticastAddress                 = "239.255.255.200";

  public static String       XBeeBroadcastAddress                       = "0.0.255.255";

  public static boolean      USE_ACTIVE_PINGS                           = false;

  public static long         PING_INTERVAL                              = 10000;

  public static int          INVOCATION_RETRIES                         = 2;

  /** Time to live for multicast packets */
  public static int          TTL                                        = 10;

  public static int          DiscoveryMulticastPort                     = 2000;

  public static int          DescriptionPort                            = 2100;

  public static int          ControlPort                                = 2200;

  public static int          EventMulticastPort                         = 2300;

  public static int          SMEPDiscoveryPort                          = 20;

  public static int          SMEPDescriptionPort                        = 21;

  public static int          SMEPControlPort                            = 22;

  public static int          SMEPEventPort                              = 23;

  /** Port where binary UPnP device wait for config packets (1504) */
  public static final int    UDPClientConfigPort                        = 1504;

  public static long         ServiceTypeAll                             = 0xFF;

  public static long         ServiceIDUnknown                           = 0;

  public static long         DeviceTypeAll                              = 0xFF;

  public static long         DeviceIDAll                                = 0xFFFFFFFF;

  /* Unit types */

  public static final byte   UnitTypeEndOfPacket                        = 0;

  public static final byte   UnitTypePadding                            = 1;

  public static final byte   UnitTypeSDLVersion                         = 2;

  public static final byte   UnitTypeSearchDevice                       = 10;

  public static final byte   UnitTypeDeviceAnnouncement                 = 11;

  public static final byte   UnitTypeDeviceRemoval                      = 12;

  public static final byte   UnitTypeGetDeviceDescription               = 13;

  public static final byte   UnitTypeDeviceDescription                  = 14;

  public static final byte   UnitTypeGetServiceValue                    = 15;

  public static final byte   UnitTypeSetServiceValue                    = 16;

  public static final byte   UnitTypeInvokeAction                       = 17;

  public static final byte   UnitTypeEvent                              = 18;

  public static final byte   UnitTypeGetServiceDescription              = 19;

  public static final byte   UnitTypeServiceDescription                 = 20;

  public static final byte   UnitTypeValueName                          = 33;

  public static final byte   UnitTypeMinAllowedValue                    = 34;

  public static final byte   UnitTypeMaxAllowedValue                    = 35;

  public static final byte   UnitTypeAllowedTextValue                   = 36;

  public static final byte   UnitTypeValueUnit                          = 37;

  public static final byte   UnitTypeValueType                          = 38;

  public static final byte   UnitTypeDeviceDescriptionDate              = 41;

  public static final byte   UnitTypeDeviceID                           = 42;

  public static final byte   UnitTypeDeviceExpectedLifeTime             = 43;

  public static final byte   UnitTypeDeviceDescriptionPort              = 44;

  public static final byte   UnitTypeDeviceControlPort                  = 45;

  public static final byte   UnitTypeDeviceEventPort                    = 46;

  public static final byte   UnitTypeDeviceExternalDescriptions         = 47;

  public static final byte   UnitTypeDeviceType                         = 48;

  public static final byte   UnitTypeDeviceManufacturer                 = 49;

  public static final byte   UnitTypeAccessID                           = 52;

  public static final byte   UnitTypeAccessForwarderAddress             = 53;

  public static final byte   UnitTypeAccessForwarderDescriptionPort     = 54;

  public static final byte   UnitTypeAccessForwarderControlPort         = 55;

  public static final byte   UnitTypeAccessForwarderEventPort           = 56;

  public static final byte   UnitTypeAccessForwarderID                  = 57;

  public static final byte   UnitTypeAccessForwarderPhyType             = 58;

  public static final byte   UnitTypeResponseID                         = 62;

  public static final byte   UnitTypeResponseForwarderAddress           = 63;

  public static final byte   UnitTypeResponseForwarderPort              = 64;

  public static final byte   UnitTypeResponseForwarderID                = 67;

  public static final byte   UnitTypeResponseForwarderPhyType           = 68;

  public static final byte   UnitTypeServiceDescriptionContainer        = 70;

  public static final byte   UnitTypeServiceContainer                   = 71;

  public static final byte   UnitTypeServiceID                          = 72;

  public static final byte   UnitTypeServiceName                        = 73;

  public static final byte   UnitTypeServiceValue                       = 75;

  public static final byte   UnitTypeServiceValueResult                 = 77;

  public static final byte   UnitTypeServiceType                        = 78;

  public static final byte   UnitTypeServiceValueReadOnly               = 79;

  public static final byte   UnitTypeActionDescriptionContainer         = 80;

  public static final byte   UnitTypeActionContainer                    = 81;

  public static final byte   UnitTypeActionID                           = 82;

  public static final byte   UnitTypeActionName                         = 83;

  public static final byte   UnitTypeActionResult                       = 87;

  public static final byte   UnitTypeArgumentDescriptionContainer       = 90;

  public static final byte   UnitTypeArgumentContainer                  = 91;

  public static final byte   UnitTypeArgumentID                         = 92;

  public static final byte   UnitTypeArgumentName                       = 93;

  public static final byte   UnitTypeArgumentValue                      = 95;

  public static final byte   UnitTypeArgumentDirection                  = 96;

  public static final byte   UnitTypeArgumentPackedDescriptionContainer = 99;

  public static final byte   UnitTypeDeviceName                         = 105;

  public static final byte   UnitTypeSetDeviceName                      = 106;

  public static final byte   UnitTypeSetDeviceNameResult                = 107;

  public static final byte   UnitTypeDeviceApplication                  = 115;

  public static final byte   UnitTypeSetDeviceApplication               = 116;

  public static final byte   UnitTypeSetDeviceApplicationResult         = 117;

  public static final byte   UnitTypePing                               = (byte)200;

  public static final byte   UnitTypePingReply                          = (byte)201;

  /* Service constants */

  public static final byte   ServiceTypeTemperaturSensor                = 1;

  public static final byte   ServiceTypeBrightnessSensor                = 2;

  public static final byte   ServiceTypeButton                          = 3;

  public static final byte   ServiceTypeMultiButton                     = 4;

  public static final byte   ServiceTypeFanSpeed                        = 5;

  public static final byte   ServiceTypePotentiometer                   = 6;

  public static final byte   ServiceTypeDisplay                         = 7;

  public static final byte   ServiceTypeIRRemoteControl                 = 8;

  public static final byte   ServiceTypeRadioStatus                     = 9;

  public static final byte   ServiceTypeRadioNeighborhood               = 10;

  public static final byte   ServiceTypeGPS                             = 11;

  public static final byte   ServiceTypeServiceManagement               = 12;

  public static final byte   ServiceTypeClock                           = 13;

  public static final byte   ServiceTypeAccumulatedEnergy               = 14;

  public static final byte   ServiceTypeVoltage                         = 15;

  public static final byte   ServiceTypeCurrent                         = 16;

  /* Device types */

  public static final byte   DeviceTypeCustom                           = 0;

  public static final byte   DeviceTypeClock                            = 13;

  public static final byte   DeviceTypeEnergyMeasurement                = 14;

  /* Argument directions */

  public static final byte   ArgumentDirectionIn                        = 0;

  public static final byte   ArgumentDirectionOut                       = 1;

  /* Var types */

  public static final byte   VarTypeNotUsed                             = 0;

  public static final byte   VarTypeUINT8                               = 1;

  public static final byte   VarTypeINT8                                = 2;

  public static final byte   VarTypeUINT16                              = 3;

  public static final byte   VarTypeINT16                               = 4;

  public static final byte   VarTypeUINT32                              = 5;

  public static final byte   VarTypeINT32                               = 6;

  public static final byte   VarTypeUINT64                              = 7;

  public static final byte   VarTypeINT64                               = 8;

  public static final byte   VarTypeString                              = 20;

  public static final byte   VarTypeURL                                 = 21;

  public static final byte   VarTypeByteArray                           = 22;

  public static final byte   VarTypeBoolean                             = 30;

  public static final byte   VarTypeComposite                           = 40;

  /* Phy types */

  public static final byte   PhyType802_3                               = 1;

  public static final byte   PhyType802_11                              = 2;

  public static final byte   PhyType802_15_4                            = 10;

  public static final byte   PhyTypeBluetooth                           = 20;

  public static final byte   PhyType868                                 = 30;

  public static final byte   PhyTypeTunnel                              = 40;

  /* ResultTypes */

  public static final byte   ResultTypeOk                               = 1;

  /** An unknown error occurred */
  public static final byte   ResultTypeUnknownError                     = 2;

  /** The device did not answer the request */
  public static final byte   ResultTypeNoResponseMessage                = 3;

  /** The response message was not well-formed */
  public static final byte   ResultTypeInvalidResponseMessage           = 4;

  /** The device could not parse the message successfully */
  public static final byte   ResultTypeInvalidMessage                   = 5;

  /** The device could not handle the message */
  public static final byte   ResultTypeInternalError                    = 6;

  /** The request is not supported */
  public static final byte   ResultTypeInvalidRequest                   = 7;

  public static final byte   ResultTypeNoDevice                         = 20;

  /** The message does not contain a service ID */
  public static final byte   ResultTypeNoService                        = 30;

  /** The service does not provide a service value */
  public static final byte   ResultTypeNoServiceValue                   = 31;

  public static final byte   ResultTypeInvalidServiceValue              = 32;

  /** The message contains a non-existent service ID */
  public static final byte   ResultTypeInvalidServiceID                 = 33;

  /** The service does not support set service value */
  public static final byte   ResultTypeSetServiceValueNotSupported      = 34;

  /** The service is currently disabled */
  public static final byte   ResultTypeServiceInactive                  = 35;

  public static final byte   ResultTypeNoAction                         = 40;

  public static final byte   ResultTypeInvalidActionID                  = 43;

  public static final byte   ResultTypeNoArgument                       = 50;

  public static final byte   ResultTypeInvalidArgumentValue             = 52;

  public static final byte   ResultTypeInvalidArgumentID                = 53;

  /** Link quality IDs */
  public static final byte   LinkQualityIDOffsetSent                    = 0;

  public static final byte   LinkQualityIDOffsetSentTotalSize           = 1;

  public static final byte   LinkQualityIDOffsetSentAverageSize         = 2;

  public static final byte   LinkQualityIDOffsetReceivedValid           = 5;

  public static final byte   LinkQualityIDOffsetReceivedInvalid         = 6;

  public static final byte   LinkQualityIDOffsetReceivedTotalSize       = 7;

  public static final byte   LinkQualityIDOffsetReceivedAverageSize     = 8;

  public static final byte   LinkQualityIDTotal                         = 20;

  public static final byte   LinkQualityIDInterval                      = 30;

  public static final byte   LinkQualityIDMinute                        = 40;

  public static final byte   LinkQualityIDHour                          = 50;

  /**
   * Retrieves the serviceType as string.
   * 
   * @return The serviceType.
   */
  public static String serviceTypeToString(int serviceType)
  {
    switch (serviceType)
    {
      case ServiceTypeTemperaturSensor:
        return "TemperatureSensor";
      case ServiceTypeBrightnessSensor:
        return "BrightnessSensor";
      case ServiceTypeButton:
        return "Button";
      case ServiceTypeMultiButton:
        return "MultiButton";
      case ServiceTypeFanSpeed:
        return "FanSpeed";
      case ServiceTypePotentiometer:
        return "Potentiometer";
      case ServiceTypeDisplay:
        return "Display";
      case ServiceTypeIRRemoteControl:
        return "IR";
      case ServiceTypeRadioStatus:
        return "RadioStatus";
      case ServiceTypeRadioNeighborhood:
        return "RadioNeighborhood";
      case ServiceTypeGPS:
        return "GPS";
      case ServiceTypeServiceManagement:
        return "ServiceManagement";
      case ServiceTypeClock:
        return "Clock";
    }
    return "";
  }

  /**
   * Retrieves the serviceValue as string.
   * 
   * @return The service value.
   */
  public static String serviceValueToString(AbstractBinaryService service)
  {
    if (service.getServiceType() == BinaryUPnPConstants.ServiceTypeTemperaturSensor)
    {
      return service.getValue().getNumericValue() / 100 + "";
    }
    if (service.getServiceType() == BinaryUPnPConstants.ServiceTypeClock)
    {
      byte[] serviceValue = service.getValue().toByteArray();

      if (serviceValue.length != 13)
      {
        return "";
      }
      long seconds = ByteArrayHelper.byteArrayToUInt32(serviceValue, 0);
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(ByteArrayHelper.byteArrayToUInt16(serviceValue, 4),
      // month is zero-based
        (serviceValue[6] & 0xFF) - 1,
        serviceValue[7] & 0xFF,
        serviceValue[8] & 0xFF,
        serviceValue[9] & 0xFF,
        serviceValue[10] & 0xFF);
      calendar.set(Calendar.MILLISECOND, ByteArrayHelper.byteArrayToUInt16(serviceValue, 11));

      return seconds + " seconds, " + DateTimeHelper.formatDateForCompleteInfo(calendar.getTime());
    }

    return service.getValueAsString();
  }

  /**
   * Retrieves the deviceType as string.
   */
  public static String deviceTypeToString(int deviceType)
  {
    switch (deviceType)
    {
      case DeviceTypeClock:
        return "Clock";
      case DeviceTypeCustom:
        return "Custom";
    }
    return "Unknown (" + deviceType + ")";
  }

  /** Returns the var type as string. */
  public static String varTypeToString(int varType)
  {
    switch (varType)
    {
      case VarTypeUINT8:
        return "UInt8";
      case VarTypeINT8:
        return "Int8";
      case VarTypeUINT16:
        return "UInt16";
      case VarTypeINT16:
        return "Int16";
      case VarTypeUINT32:
        return "UInt32";
      case VarTypeINT32:
        return "Int32";
      case VarTypeBoolean:
        return "Boolean";
      case VarTypeByteArray:
        return "ByteArray";
      case VarTypeString:
        return "String";
    }
    return "Not used";
  }

  /** Returns the result type as string. */
  public static String resultTypeToString(int resultType)
  {
    switch (resultType)
    {
      case ResultTypeOk:
        return "Ok";
      case ResultTypeUnknownError:
        return "UnknownError";
      case ResultTypeNoResponseMessage:
        return "NoResponseMessage";
      case ResultTypeInvalidResponseMessage:
        return "InvalidResponseMessage";
      case ResultTypeInvalidMessage:
        return "InvalidMessage";
      case ResultTypeInternalError:
        return "InternalError";
      case ResultTypeInvalidRequest:
        return "InvalidRequest";
      case ResultTypeNoDevice:
        return "NoDevice";
      case ResultTypeNoService:
        return "NoService";
      case ResultTypeNoServiceValue:
        return "NoServiceValue";
      case ResultTypeInvalidServiceValue:
        return "InvalidServiceValue";
      case ResultTypeInvalidServiceID:
        return "InvalidServiceID";
      case ResultTypeSetServiceValueNotSupported:
        return "SetServiceValueNotSupported";
      case ResultTypeNoAction:
        return "NoAction";
      case ResultTypeInvalidActionID:
        return "InvalidActionID";
      case ResultTypeNoArgument:
        return "NoArgument";
      case ResultTypeInvalidArgumentValue:
        return "InvalidArgumentValue";
      case ResultTypeInvalidArgumentID:
        return "InvalidArgumentID";
    }
    return "Unknown result";
  }

  /** Creates a new action failed exception. */
  public static ActionFailedException createActionFailedException(byte exceptionType)
  {
    return new ActionFailedException(exceptionType, resultTypeToString(exceptionType));
  }

  /** Returns the description date as string. */
  public static String descriptionDateToString(long date)
  {
    return ((date & 0xFF00) >> 8) + "." + ((date & 0xFF0000) >> 16) + "." +
      ((date & Long.parseLong("FFFF000000", 16)) >> 24) + (char)(97 + (date & 0xFF));
  }

  /** Returns the device address as string. */
  public static String deviceAddressToString(byte[] address)
  {
    if (address == null || address.length == 0)
    {
      return "";
    }

    return IPHelper.toString(address);
  }

  /** Returns the unit type as string. */
  public static String unitTypeToString(int unitType)
  {
    switch (unitType)
    {
      // Common
      case UnitTypeEndOfPacket:
        return "EndOfPacket";
      case UnitTypePadding:
        return "Padding";
      case UnitTypeSDLVersion:
        return "SDLVersion";
        // Container
      case UnitTypeServiceDescriptionContainer:
        return "ServiceDescription";
      case UnitTypeActionDescriptionContainer:
        return "ActionDescription";
      case UnitTypeActionContainer:
        return "Action";
      case UnitTypeArgumentDescriptionContainer:
        return "ArgumentDescription";
      case UnitTypeArgumentPackedDescriptionContainer:
        return "ArgumentPackedDescription";
      case UnitTypeArgumentContainer:
        return "Argument";
        // Message types
      case UnitTypeInvokeAction:
        return "InvokeAction";
      case UnitTypeEvent:
        return "Event";
      case UnitTypeSearchDevice:
        return "SearchDevice";
      case UnitTypeDeviceAnnouncement:
        return "DeviceAnnouncement";
      case UnitTypeDeviceRemoval:
        return "DeviceRemoval";
      case UnitTypeGetDeviceDescription:
        return "GetDeviceDescription";
      case UnitTypeDeviceDescription:
        return "DeviceDescription";
      case UnitTypeGetServiceValue:
        return "GetServiceValue";
      case UnitTypeSetServiceValue:
        return "SetServiceValue";
      case UnitTypeServiceValueResult:
        return "ServiceValueResult";
      case UnitTypeGetServiceDescription:
        return "GetServiceDescription";
      case UnitTypeServiceDescription:
        return "ServiceDescription";
        // Device
      case UnitTypeDeviceDescriptionDate:
        return "DeviceDescriptionDate";
      case UnitTypeDeviceID:
        return "DeviceID";
      case UnitTypeDeviceExpectedLifeTime:
        return "DeviceExpectedLifeTime";
      case UnitTypeDeviceDescriptionPort:
        return "DeviceDescriptionPort";
      case UnitTypeDeviceControlPort:
        return "DeviceControlPort";
      case UnitTypeDeviceEventPort:
        return "DeviceEventPort";
      case UnitTypeDeviceExternalDescriptions:
        return "UnitTypeDeviceExternalDescriptions";
      case UnitTypeDeviceName:
        return "DeviceName";
      case UnitTypeSetDeviceName:
        return "SetDeviceName";
      case UnitTypeSetDeviceNameResult:
        return "SetDeviceNameResult";
      case UnitTypeDeviceApplication:
        return "DeviceApplication";
      case UnitTypeSetDeviceApplication:
        return "SetDeviceApplication";
      case UnitTypeSetDeviceApplicationResult:
        return "SetDeviceApplicationResult";
      case UnitTypeDeviceType:
        return "DeviceType";
      case UnitTypeDeviceManufacturer:
        return "DeviceManufacturer";
      case UnitTypeServiceID:
        return "ServiceID";
      case UnitTypeServiceType:
        return "ServiceType";
      case UnitTypeServiceName:
        return "ServiceName";
      case UnitTypeValueType:
        return "ValueType";
      case UnitTypeValueUnit:
        return "ValueUnit";
      case UnitTypeServiceValue:
        return "ServiceValue";
      case UnitTypeActionID:
        return "ActionID";
      case UnitTypeActionName:
        return "ActionName";
      case UnitTypeActionResult:
        return "ActionResult";
      case UnitTypeArgumentID:
        return "ArgumentID";
      case UnitTypeArgumentDirection:
        return "ArgumentDirection";
      case UnitTypeArgumentName:
        return "ArgumentName";
      case UnitTypeArgumentValue:
        return "ArgumentValue";
        // Access
      case UnitTypeAccessForwarderAddress:
        return "AccessForwarderAddress";
      case UnitTypeAccessForwarderID:
        return "AccessForwarderID";
      case UnitTypeAccessForwarderPhyType:
        return "AccessForwarderPhyType";
      case UnitTypeAccessID:
        return "AccessID";
      case UnitTypeAccessForwarderDescriptionPort:
        return "AccessForwarderDescriptionPort";
      case UnitTypeAccessForwarderControlPort:
        return "AccessForwarderControlPort";
      case UnitTypeAccessForwarderEventPort:
        return "AccessForwarderEventPort";
        // Response
      case UnitTypeResponseForwarderAddress:
        return "ResponseForwarderAddress";
      case UnitTypeResponseForwarderID:
        return "ResponseForwarderID";
      case UnitTypeResponseForwarderPhyType:
        return "ResponseForwarderPhyType";
      case UnitTypeResponseID:
        return "ResponseID";
      case UnitTypeResponseForwarderPort:
        return "ResponseForwarderPort";

      default:
        return "Unknown(" + unitType + ")";
    }
  }

  /**
   * Retrieves the phyType as string.
   * 
   * @return The phyType.
   */
  public static String phyTypeToString(int phyType)
  {
    switch (phyType)
    {
      case PhyType802_11:
        return "802.11";
      case PhyType802_15_4:
        return "802.15.4";
      case PhyType802_3:
        return "802.3";
      case PhyTypeBluetooth:
        return "Bluetooth";
      case PhyType868:
        return "868 MHz";
      case PhyTypeTunnel:
        return "Tunnel";
    }
    return "Unknown (" + phyType + ")";
  }

  /** Encodes the length of a unit */
  public static int encodeUnitLength(int length)
  {
    // invalid length
    if (length > 1016)
    {
      return 0;
    }
    // no change
    if (length < 128)
    {
      return length;
    }
    return 128 + (length + 7) / 8;
  }

  /** Decodes the length of a unit */
  public static int decodeUnitLength(int encodedLength)
  {
    // no change
    if (encodedLength < 128)
    {
      return encodedLength;
    }
    return (encodedLength & 0x7F) * 8;
  }

  /** Returns a debug string for a byte array. */
  public static String toDebugString(byte[] data)
  {
    String result = "";
    try
    {
      int offset = 0;
      int loops = 0;
      while (offset < data.length && loops < 100)
      {
        byte unitType = data[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          result += "\r\n" + unitTypeToString(unitType);
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          result += "\r\n" + unitTypeToString(unitType) + DEBUG_STRING_DIVIDER;
          continue;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(data[offset++] & 0xFF);

        switch (unitType)
        {
          case UnitTypeServiceDescriptionContainer:
            byte[] innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n  ServiceDescription:" + innerDescription.length + "[" + toDebugString(innerDescription);
            break;
          case UnitTypeServiceContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n  Service:" + innerDescription.length + "[" + toDebugString(innerDescription);
            break;
          case UnitTypeActionDescriptionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n    ActionDescription[" + toDebugString(innerDescription);
            break;
          case UnitTypeActionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n    Action[" + toDebugString(innerDescription);
            break;
          case UnitTypeArgumentDescriptionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n      ArgumentDescription[" + toDebugString(innerDescription);
            break;
          case UnitTypeArgumentContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result += "\r\n      Argument[" + toDebugString(innerDescription);
            break;
          default:
            String tupelPayloadString = "";
            if (length > 0)
            {
              byte[] tupelPayload = new byte[length];
              Portable.arraycopy(data, offset, tupelPayload, 0, length);
              String unitTypeName = unitTypeToString(unitType);
              boolean nameValue =
                unitTypeName.endsWith("Name") || unitTypeName.endsWith("Application") ||
                  unitTypeName.endsWith("Manufacturer") || unitTypeName.endsWith("Unit");
              // new line for each access or response tupel
              if (unitTypeName.endsWith("Address"))
              {
                result += "  \r\n";
              }

              tupelPayloadString = "(";
              // show name for string values
              if (nameValue)
              {
                tupelPayloadString += StringHelper.byteArrayToAsciiDebugString(tupelPayload);
              } else
              {
                // show hex value for multi-byte values 
                if (length > 1 || (tupelPayload[0] & 0xFF) > 9)
                {
                  // show hex representation
                  tupelPayloadString += "0x" + StringHelper.byteArrayToHexString(tupelPayload, "-") + ",";
                }
                if (length <= 8)
                {
                  tupelPayloadString += ByteArrayHelper.byteArrayToInt64(tupelPayload, 0, length);
                }
                // show in IP address format
                if (unitTypeName.endsWith("Address") && length == 4)
                {
                  tupelPayloadString += ", " + InetAddress.getByAddress(tupelPayload).getHostAddress();
                }
              }
              tupelPayloadString += ")";
            }
            result += unitTypeToString(unitType) + ":" + length + tupelPayloadString + DEBUG_STRING_DIVIDER;
        }
        offset += length;
        loops++;
      }
    } catch (Exception e)
    {
    }
    return result;
  }

  /** Returns a debug string for a byte array. */
  public static String toForwarderDebugString(String indentation, byte[] data, InetAddress sourceAddress)
  {
    if (indentation == null)
    {
      indentation = "";
    }
    String result = indentation;
    byte[] deviceID = null;
    try
    {
      int offset = 0;
      int loops = 0;
      boolean firstAccess = true;
      boolean firstResponse = true;
      while (offset < data.length && loops < 100)
      {
        byte unitType = data[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          continue;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeSearchDevice ||
          unitType == BinaryUPnPConstants.UnitTypeGetDeviceDescription ||
          unitType == BinaryUPnPConstants.UnitTypeDeviceDescription ||
          unitType == BinaryUPnPConstants.UnitTypeGetServiceDescription ||
          unitType == BinaryUPnPConstants.UnitTypeServiceDescription ||
          unitType == BinaryUPnPConstants.UnitTypeInvokeAction ||
          unitType == BinaryUPnPConstants.UnitTypeGetServiceValue ||
          unitType == BinaryUPnPConstants.UnitTypeServiceValueResult ||
          unitType == BinaryUPnPConstants.UnitTypeSetServiceValue ||
          unitType == BinaryUPnPConstants.UnitTypeSetDeviceName ||
          unitType == BinaryUPnPConstants.UnitTypeSetDeviceApplication)
        {
          result += "  " + unitTypeToString(unitType) + "\r\n" + indentation;
        }
        if (unitType == BinaryUPnPConstants.UnitTypeDeviceAnnouncement)
        {
          result += "  DeviceAnnouncement or SearchResponse\r\n" + indentation;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(data[offset++] & 0xFF);
        if (length > 0)
        {
          byte[] tupelPayload = new byte[length];
          Portable.arraycopy(data, offset, tupelPayload, 0, length);
          if (unitType == UnitTypeDeviceID)
          {
            deviceID = new byte[length];
            Portable.arraycopy(tupelPayload, 0, deviceID, 0, length);
          }
          if (unitType == UnitTypeAccessForwarderID)
          {
            result += "]<---[" + ByteArrayHelper.byteArrayToInt64(tupelPayload, 0, length) + ":";
          }
          if (unitType == UnitTypeAccessForwarderAddress)
          {
            if (firstAccess)
            {
              firstAccess = false;
              result +=
                (firstResponse == false ? "\r\n" + indentation : "") + "  [DeviceID:" +
                  ByteArrayHelper.byteArrayToInt64(deviceID, 0, deviceID.length) + "-----";
            }
            result +=
              length == 4 ? InetAddress.getByAddress(tupelPayload).getHostAddress()
                : ByteArrayHelper.byteArrayToInt64(tupelPayload, 0, length) + "";
          }
          if (unitType == UnitTypeResponseForwarderID)
          {
            result += "]<---[" + ByteArrayHelper.byteArrayToInt64(tupelPayload, 0, length) + ":";
          }
          if (unitType == UnitTypeResponseForwarderAddress)
          {
            if (firstResponse)
            {
              firstResponse = false;
              result += "  [ControlPoint-----";
            }
            result +=
              length == 4 ? InetAddress.getByAddress(tupelPayload).getHostAddress()
                : ByteArrayHelper.byteArrayToInt64(tupelPayload, 0, length) + "";
          }
        }
        offset += length;
        loops++;
      }
    } catch (Exception e)
    {
    }
    return result;
  }

  /** Returns a debug string for a byte array. */
  public static String toXMLDescription(byte[] data)
  {
    return toXMLDescription(data, "  ");
  }

  /** Returns the XML description for a given binary description. */
  public static String toXMLDescription(byte[] data, String indentation)
  {
    String result = "";
    boolean wasDeviceDescription = false;
    boolean wasServiceDescription = false;
    try
    {
      int offset = 0;
      int loops = 0;
      while (offset < data.length && loops < 100)
      {
        byte unitType = data[offset++];
        if (unitType == BinaryUPnPConstants.UnitTypeEndOfPacket)
        {
          break;
        }
        if (unitType == BinaryUPnPConstants.UnitTypePadding)
        {
          continue;
        }
        int length = BinaryUPnPConstants.decodeUnitLength(data[offset++] & 0xFF);

        result += indentation;
        switch (unitType)
        {
          case UnitTypeDeviceDescription:
            wasDeviceDescription = true;
            result = "<DeviceDescription>\r\n";
            break;
          case UnitTypeServiceDescription:
            wasServiceDescription = true;
            result = "<ServiceDescription>\r\n";
            break;
          case UnitTypeServiceDescriptionContainer:
            byte[] innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result +=
              "<ServiceDescriptionContainer>\r\n" + toXMLDescription(innerDescription, indentation + "  ") +
                indentation + "</ServiceDescriptionContainer>\r\n";
            break;
          case UnitTypeActionDescriptionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result +=
              "<ActionDescriptionContainer>\r\n" + toXMLDescription(innerDescription, indentation + "  ") +
                indentation + "</ActionDescriptionContainer>\r\n";
            break;
          case UnitTypeActionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result +=
              "<ActionContainer>\r\n" + toXMLDescription(innerDescription, indentation + "  ") + indentation +
                "</ActionContainer>\r\n";
            break;
          case UnitTypeArgumentDescriptionContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result +=
              "<ArgumentDescriptionContainer>\r\n" + toXMLDescription(innerDescription, indentation + "  ") +
                indentation + "</ArgumentDescriptionContainer>\r\n";
            break;
          case UnitTypeArgumentContainer:
            innerDescription = new byte[length];
            Portable.arraycopy(data, offset, innerDescription, 0, length);
            result +=
              "<ArgumentContainer>\r\n" + toXMLDescription(innerDescription, indentation + "  ") + indentation +
                "</ArgumentContainer>\r\n";
            break;
          case UnitTypeValueType:
            result +=
              "<" + unitTypeToString(unitType) + ">" + varTypeToString(data[offset] & 0xFF) + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          case UnitTypeServiceType:
            result +=
              "<" + unitTypeToString(unitType) + ">" + serviceTypeToString(data[offset] & 0xFF) + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          case UnitTypeDeviceType:
            result +=
              "<" + unitTypeToString(unitType) + ">" + deviceTypeToString(data[offset] & 0xFF) + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          case UnitTypeAccessForwarderPhyType:
            result +=
              "<" + unitTypeToString(unitType) + ">" + phyTypeToString(data[offset] & 0xFF) + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          case UnitTypeArgumentDirection:
            result +=
              "<" + unitTypeToString(unitType) + ">" + (data[offset] == 0 ? "In" : "Out") + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          case UnitTypeSDLVersion:
            result +=
              "<" + unitTypeToString(unitType) + ">" + (data[offset] & 0xFF) + "." + (data[offset + 1] & 0xFF) + "</" +
                unitTypeToString(unitType) + ">\r\n";
            break;
          default:
            String tupelDataString = "";
            if (length > 0)
            {
              byte[] tupelData = new byte[length];
              Portable.arraycopy(data, offset, tupelData, 0, length);
              String unitTypeName = unitTypeToString(unitType);
              boolean nameValue =
                unitTypeName.endsWith("Name") || unitTypeName.endsWith("Application") ||
                  unitTypeName.endsWith("Manufacturer") || unitTypeName.endsWith("Unit");

              boolean numericValue =
                unitTypeName.equalsIgnoreCase(unitTypeToString(UnitTypeDeviceExpectedLifeTime)) ||
                  unitTypeName.endsWith("ID");

              tupelDataString = "";
              // show name for string values
              if (nameValue)
              {
                tupelDataString += StringHelper.byteArrayToAsciiDebugString(tupelData);
              }
              // show numeric value
              if (numericValue)
              {
                tupelDataString += ByteArrayHelper.byteArrayToInt64(tupelData, 0, length);
              }
              if (!nameValue && !numericValue)
              {
                tupelDataString += "0x" + StringHelper.byteArrayToBinHex(tupelData);
              }
            }
            result +=
              "<" + unitTypeToString(unitType) + ">" + tupelDataString + "</" + unitTypeToString(unitType) + ">\r\n";
        }
        offset += length;
        loops++;
      }
    } catch (Exception e)
    {
    }
    if (wasDeviceDescription)
    {
      result += "</DeviceDescription>";
    }
    if (wasServiceDescription)
    {
      result += "</ServiceDescription>";
    }

    return result;
  }
}

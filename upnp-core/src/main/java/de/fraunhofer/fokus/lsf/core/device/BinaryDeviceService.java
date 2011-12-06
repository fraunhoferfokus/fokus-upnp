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

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryService;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class encapsulates a local binary UPnP device.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryDeviceService extends AbstractBinaryService
{

  /** Associated device */
  protected BinaryDevice binaryDevice;

  /** Flag that the service value is read only */
  protected boolean      readOnlyServiceValue;

  /**
   * Creates a new instance of BinaryDeviceService.
   * 
   * @param serviceType
   * @param serviceID
   * @param valueUnit
   * @param valueType
   */
  public BinaryDeviceService(BinaryDevice binaryDevice,
    int serviceType,
    int serviceID,
    String serviceName,
    String valueUnit,
    int valueType,
    boolean readOnlyServiceValue)
  {
    super(serviceType, serviceID, valueUnit, valueType);
    this.serviceName = serviceName;
    this.binaryDevice = binaryDevice;
    this.readOnlyServiceValue = readOnlyServiceValue;
    actionList = new Vector();
  }

  /**
   * Retrieves the value of binaryDevice.
   * 
   * @return The value of binaryDevice
   */
  public BinaryDevice getBinaryDevice()
  {
    return binaryDevice;
  }

  /**
   * Sets the new value for binaryDevice.
   * 
   * @param binaryDevice
   *          The new value for binaryDevice
   */
  public void setBinaryDevice(BinaryDevice binaryDevice)
  {
    this.binaryDevice = binaryDevice;
  }

  /** Retrieves an action by its index. */
  public BinaryDeviceAction getDeviceAction(int index)
  {
    return (BinaryDeviceAction)getAction(index);
  }

  /** Retrieves an action by its ID. */
  public BinaryDeviceAction getDeviceActionByID(int id)
  {
    return (BinaryDeviceAction)getActionByID(id);
  }

  /** Retrieves an action by its name. */
  public BinaryDeviceAction getDeviceAction(String name)
  {
    return (BinaryDeviceAction)getAction(name);
  }

  /** Processes a get request for the current service value. */
  public byte[] getValueInvoked()
  {
    if (!hasServiceValue())
    {
      return binaryDevice.toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
        BinaryUPnPConstants.ResultTypeNoServiceValue,
        serviceID);
    }
    return toByteArrayForGetValueResult();
  }

  /** Processes a set request for the current service value. */
  public byte[] setValueInvoked(byte[] newValue)
  {
    if (!hasServiceValue())
    {
      return binaryDevice.toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
        BinaryUPnPConstants.ResultTypeNoServiceValue,
        serviceID);
    }
    if (readOnlyServiceValue)
    {
      return binaryDevice.toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
        BinaryUPnPConstants.ResultTypeSetServiceValueNotSupported,
        serviceID);
    }
    if (value.fromByteArray(newValue))
    {
      return binaryDevice.toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
        BinaryUPnPConstants.ResultTypeOk,
        serviceID);
    }
    return binaryDevice.toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
      BinaryUPnPConstants.ResultTypeInvalidServiceValue,
      serviceID);
  }

  /** Returns the byte array description for this service. */
  public byte[] toByteArrayForGetDescriptionResponse()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // calculate actions size
      for (int i = 0; i < actionList.size(); i++)
      {
        BinaryDeviceAction currentAction = getDeviceAction(i);
        // add byte array description for action
        byteArrayOutputStream.write(currentAction.toByteArrayForDescription());
      }
      byte[] actionDescriptions = byteArrayOutputStream.toByteArray();
      byte[] serviceNameData = StringHelper.stringToByteArray(serviceName);

      byte[] serviceValueDescriptionData = new byte[0];
      if (hasServiceValue())
      {
        byte[] valueUnitData = StringHelper.stringToByteArray(valueUnit);
        // assume simple service value
        byte[] serviceTypeData = new byte[] {
          (byte)getValueType()
        };
        // replace with composite description if needed
        if (value.isCompositeValue())
        {
          serviceTypeData = value.getCompositeDescription();
        }
        serviceValueDescriptionData = new byte[4 + serviceTypeData.length + valueUnitData.length];
        serviceValueDescriptionData[0] = BinaryUPnPConstants.UnitTypeValueType;
        serviceValueDescriptionData[1] = (byte)serviceTypeData.length;
        Portable.arraycopy(serviceTypeData, 0, serviceValueDescriptionData, 2, serviceTypeData.length);
        serviceValueDescriptionData[serviceTypeData.length + 2] = BinaryUPnPConstants.UnitTypeValueUnit;
        serviceValueDescriptionData[serviceTypeData.length + 3] = (byte)valueUnitData.length;
        if (valueUnitData.length > 0)
        {
          Portable.arraycopy(valueUnitData,
            0,
            serviceValueDescriptionData,
            serviceTypeData.length + 4,
            valueUnitData.length);
        }
      }

      byteArrayOutputStream.reset();

      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceDescriptionContainer);
      // ServiceID, Type, Name
      byteArrayOutputStream.write(8 + serviceNameData.length + serviceValueDescriptionData.length +
        actionDescriptions.length);
      // add ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(serviceID);
      // add type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceType);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(serviceType);
      // add optional service value
      if (serviceValueDescriptionData.length > 0)
      {
        byteArrayOutputStream.write(serviceValueDescriptionData);
      }
      // add name
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceName);
      byteArrayOutputStream.write(serviceNameData.length);
      byteArrayOutputStream.write(serviceNameData);
      // add all actions
      byteArrayOutputStream.write(actionDescriptions);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

  /** Builds the message for a get value response message. */
  public byte[] toByteArrayForGetValueResult()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceValueResult);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(BinaryUPnPConstants.ResultTypeOk);
      // add device ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(binaryDevice.getDeviceID()));
      // add service ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(serviceID);
      // add value
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceValue);
      byte[] valueData = value.toByteArray();
      byteArrayOutputStream.write(valueData.length);
      byteArrayOutputStream.write(valueData);
      // add end of packet
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

}

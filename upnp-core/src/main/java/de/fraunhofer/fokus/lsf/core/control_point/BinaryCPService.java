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

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.IBinaryUPnPService;
import de.fraunhofer.fokus.lsf.core.IGenericBinaryUPnPActor;
import de.fraunhofer.fokus.lsf.core.IGenericBinaryUPnPSensorService;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryService;
import de.fraunhofer.fokus.lsf.core.base.BinaryValue;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a remote view on a binary UPnP service.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryCPService extends AbstractBinaryService implements
  IBinaryUPnPService,
  IGenericBinaryUPnPSensorService,
  IGenericBinaryUPnPActor
{
  /** Associated device */
  private BinaryCPDevice binaryCPDevice;

  /** Update ID for last service state */
  private int            managementServiceUpdateID = -1;

  /** Flag to request the management state */
  private boolean        requestManagementState    = false;

  /**
   * Creates a new instance of BinaryCPService.
   * 
   * @param serviceType
   *          The service type
   * @param serviceID
   *          The ID of the service
   * @param varUnit
   *          The unit of the value
   * @param valueType
   *          The type of the value
   */
  public BinaryCPService(int serviceType, int serviceID, String valueUnit, int valueType)
  {
    super(serviceType, serviceID, valueUnit, valueType);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.base.BinaryService#setNumericValue(long)
   */
  public void setNumericValue(long newValue) throws ActionFailedException
  {
    if (isNumericService() && newValue != value.getNumericValue())
    {
      BinaryValue tempValue = new BinaryValue(value.getValueType());
      tempValue.setNumericValue(newValue);
      // send to service. this will also update the local value in case of success
      invokeSetValue(tempValue.toByteArray());
    }
  }

  /** Request the current service value from the binary UPnP service. */
  public void invokeGetValue() throws ActionFailedException
  {
    if (!hasServiceValue())
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoServiceValue);
    }
    BinaryCPValueMessageParser messageParser = binaryCPDevice.getBinaryControlPoint().invokeGetServiceValue(this);
    byte[] valueData = (byte[])messageParser.getServiceValueTable().get(new Integer(serviceID));
    if (valueData == null)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidServiceID);
    }
    if (value.fromByteArray(valueData))
    {
      return;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidServiceValue);
  }

  /** Sends a new service value to the binary UPnP service. */
  void invokeSetValue(byte[] newValue) throws ActionFailedException
  {
    if (!hasServiceValue())
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoServiceValue);
    }
    BinaryCPValueMessageParser messageParser =
      binaryCPDevice.getBinaryControlPoint().invokeSetServiceValue(this, newValue);

    if (messageParser.isOkValueResponse())
    {
      // update locally
      value.fromByteArray(newValue);
      return;
    }
    throw BinaryUPnPConstants.createActionFailedException(messageParser.getResult());
  }

  /** Sends a new service value to the binary UPnP service. */
  public void invokeSetValue(String data) throws ActionFailedException
  {
    BinaryValue tempValue = new BinaryValue(value.getValueType());
    // string has correct format
    if (tempValue.fromString(data))
    {
      // send to service. this will also update the local value in case of success
      invokeSetValue(tempValue.toByteArray());
      return;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidServiceValue);
  }

  /** Request both service and evented state from the binary UPnP service. */
  public void invokeGetManagementState() throws ActionFailedException
  {
    active = binaryCPDevice.getBinaryControlPoint().invokeIsActive(this);
    evented = binaryCPDevice.getBinaryControlPoint().invokeIsEvented(this);
    eventRate = binaryCPDevice.getBinaryControlPoint().invokeGetEventRate(this);
    // store management ID associated with this information
    managementServiceUpdateID = (int)binaryCPDevice.getManagementService().getValue().getNumericValue();
  }

  /** Sets the current service state for the binary UPnP service. */
  public void invokeSetActive(boolean state) throws ActionFailedException
  {
    // it must not be possible to disable the management service
    if (getServiceType() == BinaryUPnPConstants.ServiceTypeServiceManagement)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInvalidRequest);
    }
    binaryCPDevice.getBinaryControlPoint().invokeSetActive(this, state);
  }

  /** Sets the current event state for the binary UPnP service. */
  public void invokeSetEvented(boolean state) throws ActionFailedException
  {
    binaryCPDevice.getBinaryControlPoint().invokeSetEvented(this, state);
  }

  /** Sets the current event rate for the binary UPnP service. */
  public void invokeSetEventRate(int rate) throws ActionFailedException
  {
    binaryCPDevice.getBinaryControlPoint().invokeSetEventRate(this, rate);
  }

  /**
   * Retrieves the value of managementServiceUpdateID.
   * 
   * @return The value of managementServiceUpdateID
   */
  public int getManagementServiceUpdateID()
  {
    return managementServiceUpdateID;
  }

  /** Checks whether the service metadata has already been read */
  public boolean hasValidManagementMetadata()
  {
    return managementServiceUpdateID != -1;
  }

  /** Checks whether the service can be managed */
  public boolean isManageable()
  {
    return getBinaryCPDevice().getManagementService() != null;
  }

  /**
   * Sets the new value for managementServiceUpdateID.
   * 
   * @param managementServiceUpdateID
   *          The new value for managementServiceUpdateID
   */
  public void setManagementServiceUpdateID(int managementServiceUpdateID)
  {
    this.managementServiceUpdateID = managementServiceUpdateID;
  }

  /**
   * @return Returns the binaryCPDevice.
   */
  public BinaryCPDevice getBinaryCPDevice()
  {
    return binaryCPDevice;
  }

  /**
   * @param binaryCPDevice
   *          The binaryCPDevice to set.
   */
  public void setBinaryCPDevice(BinaryCPDevice binaryCPDevice)
  {
    this.binaryCPDevice = binaryCPDevice;
  }

  /**
   * Retrieves the value of requestManagementState.
   * 
   * @return The value of requestManagementState
   */
  public boolean isRequestManagementState()
  {
    return requestManagementState;
  }

  /**
   * Sets the new value for requestManagementState.
   * 
   * @param requestManagementState
   *          The new value for requestManagementState
   */
  public void setRequestManagementState(boolean requestManagementState)
  {
    this.requestManagementState = requestManagementState;
  }

  /** Retrieves an action by its index. */
  public BinaryCPAction getCPAction(int index)
  {
    return (BinaryCPAction)getAction(index);
  }

  /** Retrieves an action by its ID. */
  public BinaryCPAction getCPActionByID(int id)
  {
    return (BinaryCPAction)getActionByID(id);
  }

  /** Retrieves an action by its name. */
  public BinaryCPAction getCPAction(String name)
  {
    return (BinaryCPAction)getAction(name);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IGenericBinaryUPnPSensorService#getUnit()
   */
  public String getUnit()
  {
    return getValueUnit();
  }

  /** Returns device and service type as string. */
  public String toUniqueString()
  {
    return binaryCPDevice.toString() + "." + toString();
  }

}

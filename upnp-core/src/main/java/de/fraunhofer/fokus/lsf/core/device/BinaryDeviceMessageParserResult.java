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

import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;

/**
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class BinaryDeviceMessageParserResult
{

  private Vector responseEntityList = new Vector();

  private long   deviceID           = BinaryUPnPConstants.DeviceIDAll;

  private int    deviceType         = (int)BinaryUPnPConstants.DeviceTypeAll;

  private int    serviceType        = (int)BinaryUPnPConstants.ServiceTypeAll;

  private int    serviceID          = (int)BinaryUPnPConstants.ServiceIDUnknown;

  private int    messageType        = 0;

  /**
   * Creates a new instance of BinaryDeviceMessageParserResult.
   * 
   */
  public BinaryDeviceMessageParserResult()
  {
  }

  /**
   * Retrieves the value of responseEntityList.
   * 
   * @return The value of responseEntityList
   */
  public Vector getResponseEntityList()
  {
    return responseEntityList;
  }

  /**
   * Sets the new value for responseEntityList.
   * 
   * @param responseEntityList
   *          The new value for responseEntityList
   */
  public void setResponseEntityList(Vector responseEntityList)
  {
    this.responseEntityList = responseEntityList;
  }

  /**
   * Retrieves the value of deviceID.
   * 
   * @return The value of deviceID
   */
  public long getDeviceID()
  {
    return deviceID;
  }

  /**
   * Sets the new value for deviceID.
   * 
   * @param deviceID
   *          The new value for deviceID
   */
  public void setDeviceID(long deviceID)
  {
    this.deviceID = deviceID;
  }

  /**
   * Retrieves the value of deviceType.
   * 
   * @return The value of deviceType
   */
  public int getDeviceType()
  {
    return deviceType;
  }

  /**
   * Sets the new value for deviceType.
   * 
   * @param deviceType
   *          The new value for deviceType
   */
  public void setDeviceType(int deviceType)
  {
    this.deviceType = deviceType;
  }

  /**
   * Retrieves the value of serviceID.
   * 
   * @return The value of serviceID
   */
  public int getServiceID()
  {
    return serviceID;
  }

  /**
   * Sets the new value for serviceID.
   * 
   * @param serviceID
   *          The new value for serviceID
   */
  public void setServiceID(int serviceID)
  {
    this.serviceID = serviceID;
  }

  /**
   * Retrieves the value of serviceType.
   * 
   * @return The value of serviceType
   */
  public int getServiceType()
  {
    return serviceType;
  }

  /**
   * Sets the new value for serviceType.
   * 
   * @param serviceType
   *          The new value for serviceType
   */
  public void setServiceType(int serviceType)
  {
    this.serviceType = serviceType;
  }

  /**
   * Retrieves the value of messageType.
   * 
   * @return The value of messageType
   */
  public int getMessageType()
  {
    return messageType;
  }

  /**
   * Sets the new value for messageType.
   * 
   * @param messageType
   *          The new value for messageType
   */
  public void setMessageType(int messageType)
  {
    this.messageType = messageType;
  }

  /** Checks whether this message is a matching search device message. */
  public boolean isSearchMessage(BinaryDevice binaryDevice)
  {
    return messageType == BinaryUPnPConstants.UnitTypeSearchDevice &&
      (deviceID == BinaryUPnPConstants.DeviceIDAll || deviceID == binaryDevice.getDeviceID()) &&
      (deviceType == BinaryUPnPConstants.DeviceTypeAll || deviceType == binaryDevice.getDeviceType()) &&
      (serviceType == BinaryUPnPConstants.ServiceTypeAll || binaryDevice.getServiceByType(serviceType) != null);
  }

  /** Checks whether this message is a matching get description message. */
  public boolean isGetDescriptionMessage(BinaryDevice binaryDevice)
  {
    return messageType == BinaryUPnPConstants.UnitTypeGetDeviceDescription && deviceID == binaryDevice.getDeviceID();
  }

  /** Checks whether this message is an incomplete get service value message. */
  public boolean isIncompleteGetServiceValueMessage(BinaryDevice binaryDevice)
  {
    return messageType == BinaryUPnPConstants.UnitTypeGetServiceValue && deviceID == binaryDevice.getDeviceID() &&
      serviceID == BinaryUPnPConstants.ServiceIDUnknown;
  }

  /** Checks whether this message is a matching get service value message. */
  public boolean isGetServiceValueMessage(BinaryDevice binaryDevice)
  {
    return messageType == BinaryUPnPConstants.UnitTypeGetServiceValue && deviceID == binaryDevice.getDeviceID() &&
      serviceID != BinaryUPnPConstants.ServiceIDUnknown;
  }

}

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

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class encapsulates a remote view on a binary UPnP device.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryCPDevice extends AbstractBinaryDevice implements IBinaryUPnPDevice
{
  /** Reference to outer control point */
  private BinaryControlPoint binaryControlPoint;

  /** Basic discovery info */
  private BinaryCPDeviceInfo deviceInfo;

  /** Flag that service descriptions are external */
  private boolean            externalServiceDescriptions          = false;

  /** Device description message */
  private byte[]             descriptionMessage;

  /** Optional service description messages. */
  private Hashtable          serviceDescriptionMessageFromIDTable = new Hashtable();

  /** Array with last ping times */
  private long[]             lastResponseTimes                    = new long[10];

  private int                lastResponseTimesCount               = 0;

  /**
   * Creates a new instance of BinaryCPDevice.
   * 
   * @param deviceInfo
   * @param expectedLifeTime
   */
  public BinaryCPDevice(BinaryCPDeviceInfo deviceInfo, int expectedLifeTime)
  {
    this.deviceInfo = deviceInfo;
    this.binaryControlPoint = deviceInfo.getBinaryControlPoint();
    this.expectedLifeTime = expectedLifeTime;
    this.eventPort = BinaryUPnPConstants.EventMulticastPort;
    this.controlPort = BinaryUPnPConstants.ControlPort;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice#getDeviceDescriptionSocketAddress()
   */
  public InetSocketAddress getDeviceDescriptionSocketAddress()
  {
    return new InetSocketAddress(getAccessAddress(), getDescriptionPort());
  }

  /**
   * Retrieves the lastDiscoveryTime.
   * 
   * @return The lastDiscoveryTime.
   */
  public long getLastDiscoveryTime()
  {
    return deviceInfo.getLastDiscoveryTime();
  }

  /**
   * Sets the lastDiscoveryTime.
   */
  public void updateDiscoveryTime()
  {
    deviceInfo.updateDiscoveryTime();
  }

  /**
   * Updates an existing device with newly parsed data.
   * 
   * 
   * @param changedDevice
   * @return The event code that describes changed properties
   * 
   */
  public int updateDevice(BinaryCPDevice changedDevice)
  {
    int result = 0;

    if (getExpirationTime() != changedDevice.getExpirationTime())
    {
      result |= BinaryCPConstants.EVENT_CODE_EXPIRATION_TIME_CHANGE;
    }
    if (!ByteArrayHelper.isEqual(getDeviceAddress(), changedDevice.getDeviceAddress()))
    {
      result |= BinaryCPConstants.EVENT_CODE_DEVICE_ADDRESS_CHANGE;
    }
    if (!getDeviceInfo().hasEqualPath(changedDevice.getDeviceInfo()))
    {
      result |= BinaryCPConstants.EVENT_CODE_PATH_CHANGE;
    }
    if (!getName().equals(changedDevice.getName()) || !getApplication().equals(changedDevice.getApplication()) ||
      !getManufacturer().equals(changedDevice.getManufacturer()) || deviceType != changedDevice.getDeviceType() ||
      getDeviceDescriptionDate() != changedDevice.getDeviceDescriptionDate())
    {
      application = changedDevice.getApplication();
      name = changedDevice.getName();
      deviceType = changedDevice.getDeviceType();
      manufacturer = changedDevice.getManufacturer();
      // description date is updated through device info exchange
      result |= BinaryCPConstants.EVENT_CODE_META_DATA_CHANGE;
    }
    if (!hasEqualServices(changedDevice))
    {
      serviceList = changedDevice.getServiceList();
      // associate each new service with existing device
      for (int i = 0; i < serviceList.size(); i++)
      {
        ((BinaryCPService)serviceList.elementAt(i)).setBinaryCPDevice(this);
      }
      result |= BinaryCPConstants.EVENT_CODE_SERVICE_CHANGE;
    }
    // update existing device
    deviceInfo = changedDevice.getDeviceInfo();
    descriptionMessage = changedDevice.getDescriptionMessage();
    expectedLifeTime = changedDevice.getExpectedLifeTime();

    return result;
  }

  /** Retrieves the number of seconds when this device expires. */
  public long getExpirationTime()
  {
    return (getExpectedLifeTime() * 60000 - (Portable.currentTimeMillis() - getLastDiscoveryTime())) / 1000;
  }

  /**
   * Checks if the device should be removed from the control point.
   * 
   * @return True if the device is deprecated, false otherwise
   */
  public boolean isDeprecated()
  {
    // for active pings, timeout is shorter than the expected lifetime
    if (BinaryUPnPConstants.USE_ACTIVE_PINGS)
    {
      return Portable.currentTimeMillis() - deviceInfo.getLastDiscoveryTime() > 2 * BinaryUPnPConstants.PING_INTERVAL;
    }
    return Portable.currentTimeMillis() - deviceInfo.getLastDiscoveryTime() > expectedLifeTime * 60000;
  }

  /** Adds access entities to a control message */
  public void addAccessEntities(ByteArrayOutputStream outputStream)
  {
    deviceInfo.addAccessEntities(outputStream);
  }

  /**
   * Retrieves the accessAddress.
   * 
   * @return The accessAddress.
   */
  public InetAddress getAccessAddress()
  {
    return deviceInfo.getAccessAddress();
  }

  /**
   * Sets the name in the device.
   * 
   * @param newName
   *          The name to set.
   */
  public boolean invokeSetName(String newName) throws ActionFailedException
  {
    if (!newName.equals(this.name))
    {
      BinaryCPValueMessageParser messageParser = getBinaryControlPoint().invokeSetDeviceName(this, newName);
      if (messageParser.isOkValueResponse())
      {
        Portable.println("Name set successfully");
        this.name = newName;
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the application in the device.
   * 
   * @param newApplication
   *          The application to set.
   */
  public boolean invokeSetApplication(String newApplication) throws ActionFailedException
  {
    if (!newApplication.equals(this.application))
    {
      BinaryCPValueMessageParser messageParser =
        getBinaryControlPoint().invokeSetDeviceApplication(this, newApplication);
      if (messageParser.isOkValueResponse())
      {
        Portable.println("Application set successfully");
        this.application = newApplication;
        return true;
      }
    }
    return false;
  }

  /**
   * @return Returns the controlPoint.
   */
  public BinaryControlPoint getBinaryControlPoint()
  {
    return binaryControlPoint;
  }

  /**
   * Sets the binaryControlPoint.
   * 
   * @param binaryControlPoint
   *          The binaryControlPoint to set.
   */
  public void setBinaryControlPoint(BinaryControlPoint binaryControlPoint)
  {
    this.binaryControlPoint = binaryControlPoint;
  }

  /**
   * Retrieves the value of deviceAddress.
   * 
   * @return The value of deviceAddress
   */
  public byte[] getDeviceAddress()
  {
    return deviceInfo.getDeviceAddress();
  }

  /**
   * Retrieves the value of gatewayList.
   * 
   * @return The value of gatewayList
   */
  public Vector getAccessEntityList()
  {
    return deviceInfo.getAccessEntityList();
  }

  /**
   * Retrieves a specific gateway data entry.
   * 
   * @param index
   * @return
   */
  public GatewayData getAccessEntityData(int index)
  {
    return deviceInfo.getAccessEntityData(index);
  }

  /** Retrieves the associated socket structure */
  public BinaryCPHostAddressSocketStructure getAssociatedSocketStructure()
  {
    return (BinaryCPHostAddressSocketStructure)binaryControlPoint.getBinaryCPMessageManagement()
      .getSocketStructureManagement()
      .getSocketStructure(deviceInfo.getAssociatedHostAddress());
  }

  /**
   * Retrieves the value of descriptionMessage.
   * 
   * @return The value of descriptionMessage
   */
  public byte[] getDescriptionMessage()
  {
    return descriptionMessage;
  }

  /**
   * Sets the new value for descriptionMessage.
   * 
   * @param descriptionMessage
   *          The new value for descriptionMessage
   */
  public void setDescriptionMessage(byte[] descriptionMessage)
  {
    this.descriptionMessage = descriptionMessage;
  }

  /**
   * Retrieves the value of serviceDescriptionMessageFromIDTable.
   * 
   * @return The value of serviceDescriptionMessageFromIDTable
   */
  public Hashtable getServiceDescriptionMessageFromIDTable()
  {
    return serviceDescriptionMessageFromIDTable;
  }

  /**
   * Retrieves the value of deviceInfo.
   * 
   * @return The value of deviceInfo
   */
  public BinaryCPDeviceInfo getDeviceInfo()
  {
    return deviceInfo;
  }

  /**
   * Sets the new value for deviceInfo.
   * 
   * @param deviceInfo
   *          The new value for deviceInfo
   */
  public void setDeviceInfo(BinaryCPDeviceInfo deviceInfo)
  {
    this.deviceInfo = deviceInfo;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.control_point.BinaryDevice#getDeviceID()
   */
  public long getDeviceID()
  {
    return deviceInfo.getDeviceID();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.control_point.BinaryDevice#getDeviceDescriptionDate()
   */
  public long getDeviceDescriptionDate()
  {
    return deviceInfo.getDeviceDescriptionDate();
  }

  /**
   * 
   * @param deviceDescriptionDate
   */
  public void setDeviceDescriptionDate(long deviceDescriptionDate)
  {
    deviceInfo.setDeviceDescriptionDate(deviceDescriptionDate);
  }

  /** Retrieves the number of services. */
  public int getCPServiceCount()
  {
    return getServiceCount();
  }

  /** Retrieves a service by its index. */
  public BinaryCPService getCPService(int index)
  {
    return (BinaryCPService)getService(index);
  }

  /** Retrieves a service by its ID. */
  public BinaryCPService getCPServiceByID(int serviceID)
  {
    return (BinaryCPService)getServiceByID(serviceID);
  }

  /**
   * Retrieves a service by its type. If more than one service fits, the first service is returned.
   * 
   */
  public BinaryCPService getCPServiceByType(int serviceType)
  {
    return (BinaryCPService)getServiceByType(serviceType);
  }

  /**
   * Replaces a service by its ID.
   * 
   */
  public void replaceCPService(BinaryCPService service)
  {
    for (int i = 0; i < serviceList.size(); i++)
    {
      BinaryCPService currentService = getCPService(i);
      if (currentService.getServiceID() == service.getServiceID())
      {
        // associate service with device
        service.setBinaryCPDevice(this);
        serviceList.remove(i);
        serviceList.insertElementAt(service, i);
        return;
      }
    }
  }

  /** Retrieves the management service if found */
  public BinaryCPService getManagementService()
  {
    return (BinaryCPService)getServiceByType(BinaryUPnPConstants.ServiceTypeServiceManagement);
  }

  /** Adds a new response time to the local store. */
  public void addResponseTime(long time)
  {
    if (lastResponseTimesCount < lastResponseTimes.length)
    {
      lastResponseTimesCount++;
    }
    for (int i = lastResponseTimes.length - 1; i > 0; i--)
    {
      lastResponseTimes[i] = lastResponseTimes[i - 1];
    }
    lastResponseTimes[0] = time;
  }

  /** Returns the average response time of the last sent messages. */
  public long getAverageResponseTimes()
  {
    if (lastResponseTimesCount == 0)
    {
      return 0;
    }
    long result = 0;
    for (int i = 0; i < lastResponseTimesCount; i++)
    {
      result += lastResponseTimes[i];
    }
    return result / lastResponseTimesCount;
  }

  /** Returns the number of measurements for the average response time. */
  public long getAverageResponseTimeMeasurements()
  {
    return lastResponseTimesCount;
  }

  /**
   * Retrieves the value of externalServiceDescriptions.
   * 
   * @return The value of externalServiceDescriptions
   */
  public boolean hasExternalServiceDescriptions()
  {
    return externalServiceDescriptions;
  }

  /**
   * Sets the new value for externalServiceDescriptions.
   * 
   * @param externalServiceDescriptions
   *          The new value for externalServiceDescriptions
   */
  public void setExternalServiceDescriptions(boolean externalServiceDescriptions)
  {
    this.externalServiceDescriptions = externalServiceDescriptions;
  }

}

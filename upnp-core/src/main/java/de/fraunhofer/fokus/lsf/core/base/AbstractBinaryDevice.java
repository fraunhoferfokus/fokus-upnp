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

import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class encapsulates a binary UPnP device.
 * 
 * @author Alexander Koenig
 * 
 */
public abstract class AbstractBinaryDevice
{

  /** Expected life time for device in minutes */
  protected long   expectedLifeTime;

  /** Device type */
  protected int    deviceType;

  /** Used description port */
  protected int    descriptionPort;

  /** Used control port */
  protected int    controlPort;

  /** Used event port */
  protected int    eventPort;

  /** List of offered services */
  protected Vector serviceList = new Vector();

  /** Name */
  protected String name;

  /** Intended application */
  protected String application;

  /** Manufacturer */
  protected String manufacturer;

  /**
   * Creates a new instance of AbstractBinaryDevice.
   * 
   */
  public AbstractBinaryDevice()
  {
    super();
  }

  /** Compares this device to another binary CP device. */
  public boolean equals(Object obj)
  {
    if (obj instanceof AbstractBinaryDevice)
    {
      return getDeviceID() == ((AbstractBinaryDevice)obj).getDeviceID();
    }
    return super.equals(obj);
  }

  /** Checks whether two devices provide the same services. */
  public boolean hasEqualServices(AbstractBinaryDevice compareDevice)
  {
    // compare all services, find associated services by ID
    for (int i = 0; i < serviceList.size(); i++)
    {
      AbstractBinaryService currentService = getService(i);
      AbstractBinaryService compareService = compareDevice.getServiceByID(currentService.getServiceID());
      if (compareService == null)
      {
        Portable.println("Service " + currentService.toString() + " no longer available");
        return false;
      }
      if (!currentService.equals(compareService))
      {
        Portable.println("Service " + currentService.toString() + " has changed");
        return false;
      }
    }
    return true;
  }

  /**
   * Retrieves the expectedLifeTime in minutes.
   * 
   * @return The expected life time in minutes
   */
  public long getExpectedLifeTime()
  {
    return expectedLifeTime;
  }

  /**
   * Sets the expectedLifeTime in minutes
   * 
   * @param expectedLifeTime
   *          The expected life time in minutes
   */
  public void setExpectedLifeTime(long expectedLifeTime)
  {
    this.expectedLifeTime = expectedLifeTime;
  }

  /**
   * Retrieves the serviceList.
   * 
   * @return The serviceList.
   */
  public Vector getServiceList()
  {
    return serviceList;
  }

  /** Retrieves the number of services. */
  public int getServiceCount()
  {
    return serviceList.size();
  }

  /** Retrieves a service by its index. */
  public AbstractBinaryService getService(int index)
  {
    if (index >= 0 && index < serviceList.size())
    {
      return (AbstractBinaryService)serviceList.elementAt(index);
    }

    return null;
  }

  /** Retrieves a service by its ID. */
  public AbstractBinaryService getServiceByID(int serviceID)
  {
    for (int j = 0; j < serviceList.size(); j++)
    {
      AbstractBinaryService currentService = (AbstractBinaryService)serviceList.elementAt(j);
      if (currentService.getServiceID() == serviceID)
      {
        return currentService;
      }
    }
    return null;
  }

  /**
   * Retrieves a service by its type. If more than one service fits, the first service is returned.
   * 
   */
  public AbstractBinaryService getServiceByType(int serviceType)
  {
    for (int j = 0; j < serviceList.size(); j++)
    {
      AbstractBinaryService currentService = (AbstractBinaryService)serviceList.elementAt(j);
      if (currentService.getServiceType() == serviceType)
      {
        return currentService;
      }
    }
    return null;
  }

  /**
   * Sets the serviceList
   * 
   * @param serviceList
   *          The serviceList to set.
   */
  public void setServiceList(Vector serviceList)
  {
    this.serviceList = serviceList;
  }

  /**
   * Retrieves the value of descriptionPort.
   * 
   * @return The value of descriptionPort
   */
  public int getDescriptionPort()
  {
    return descriptionPort;
  }

  /**
   * Sets the new value for descriptionPort.
   * 
   * @param descriptionPort
   *          The new value for descriptionPort
   */
  public void setDescriptionPort(int descriptionPort)
  {
    this.descriptionPort = descriptionPort;
  }

  /**
   * Retrieves the controlPort.
   * 
   * @return The controlPort.
   */
  public int getControlPort()
  {
    return controlPort;
  }

  /**
   * Sets the controlPort.
   * 
   * @param controlPort
   *          The controlPort to set.
   */
  public void setControlPort(int controlPort)
  {
    this.controlPort = controlPort;
  }

  /**
   * Retrieves the eventPort.
   * 
   * @return The eventPort.
   */
  public int getEventPort()
  {
    return eventPort;
  }

  /**
   * Sets the eventPort.
   * 
   * @param eventPort
   *          The eventPort to set.
   */
  public void setEventPort(int eventPort)
  {
    this.eventPort = eventPort;
  }

  public String getApplication()
  {
    return application;
  }

  /**
   * Sets the application.
   * 
   * @param application
   *          The application to set.
   */
  public void setApplication(String application)
  {
    this.application = application;
  }

  public String getName()
  {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Retrieves the value of manufacturer.
   * 
   * @return The value of manufacturer
   */
  public String getManufacturer()
  {
    return manufacturer;
  }

  /**
   * Sets the new value for manufacturer.
   * 
   * @param manufacturer
   *          The new value for manufacturer
   */
  public void setManufacturer(String manufacturer)
  {
    this.manufacturer = manufacturer;
  }

  /**
   * Retrieves the deviceID.
   * 
   * @return The deviceID.
   */
  public abstract long getDeviceID();

  /**
   * Retrieves the value of deviceDescriptionDate.
   * 
   * @return The value of deviceDescriptionDate
   */
  public abstract long getDeviceDescriptionDate();

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
   * Retrieves the address which can be used to access the device description.
   * 
   * @return The socket address to retrieve the device description.
   */
  public abstract InetSocketAddress getDeviceDescriptionSocketAddress();

  /** Returns a descriptive string for this device and its services. */
  public String toDebugString()
  {
    String result = "";
    result += "ExpectedLifetime:" + getExpectedLifeTime() + " minutes" + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "DeviceType:" + getDeviceType() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "DeviceID:" + getDeviceID() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "DeviceName:" + getName() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "DeviceApplication:" + getApplication() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "DeviceManufacturer:" + getManufacturer() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    for (int i = 0; i < getServiceCount(); i++)
    {
      result += "\r\n  ServiceDescription[";
      AbstractBinaryService currentService = getService(i);
      result += currentService.toDebugString();
    }
    return result;
  }

  /** Returns the name and application of this device. */
  public String toString()
  {
    return getName() + " (Application:" + getApplication() + ")";
  }

}

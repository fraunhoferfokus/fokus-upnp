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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;

/**
 * This class stores device information gained during the discovery phase.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class CPDeviceDiscoveryInfo
{

  /**
   * UPnP logger
   */
  private static Logger              logger                       = Logger.getLogger("upnp.ssdp.cp");

  /** Time till advertisements ends */
  private int                        maxage;

  /** Location URL of root device */
  private URL                        location;

  /** server information (OS version / product version) */
  private String                     server;

  /** List with alternative IP addresses */
  private Vector                     alternativeIPAddressList     = new Vector();

  /** List with previous or alternative IP addresses. May contain stale values */
  private Vector                     publishedIPAddressList       = new Vector();

  /** List with friendly names. Added here for convenience, but not part of the actual device info */
  private Vector                     friendlyNameList             = new Vector();

  /** Reference to root entry */
  private CPRootDeviceDiscoveryEntry rootDeviceDiscoveryEntry;

  /** Temporary device entry */
  private Vector                     tempDeviceDiscoveryEntryList = new Vector();

  /** Time at which the device was marked as invalid */
  private long                       invalidationTime             = 0;

  /** Flag that the associated device is invalid */
  private boolean                    invalidDeviceFlag            = false;

  /** Time of first discovery */
  private long                       firstDiscoveryTime           = 0;

  /** Time of last discovery */
  private long                       lastDiscoveryTime            = 0;

  /** Vector with time between discovery messages */
  private Vector                     discoveryIntervalList        = new Vector();

  /** Number of device removals during the control points lifetime */
  private int                        deviceRemovalCount           = 0;

  /** Number of device time outs during the control points lifetime */
  private int                        deviceTimedOutCount          = 0;

  /** Number of device discoveries during the control points lifetime */
  private int                        deviceDetectionCount         = 0;

  /**
   * network location signature
   */
  private String                     NLS;

  /**
   * Creates a new instance of CPDeviceDiscoveryInfo.
   * 
   * @param cacheControlValue
   *          maxage value
   * @param locationURL
   *          device description URL
   * @param serverValue
   *          server value
   * @param ntValue
   *          search target
   * @param usnValue
   *          unique service name
   * @param nlsValue
   *          network location signature
   * 
   */
  public CPDeviceDiscoveryInfo(String cacheControlValue,
    URL locationURL,
    String serverValue,
    String ntValue,
    String usnValue,
    String nlsValue)
  {
    // process cacheControl Value and location Value
    maxage = processCacheControl(cacheControlValue);
    location = locationURL;
    server = serverValue;
    NLS = nlsValue;
    firstDiscoveryTime = System.currentTimeMillis();
    lastDiscoveryTime = firstDiscoveryTime;

    processDiscoveryInfo(ntValue, usnValue);
  }

  /**
   * Creates a new instance of CPDeviceDiscoveryInfo with basic discovery infos.
   * 
   * @param locationURL
   *          device description URL
   * 
   */
  public CPDeviceDiscoveryInfo(URL locationURL)
  {
    maxage = UPnPDefaults.DEVICE_MAX_AGE;
    location = locationURL;
    server = "Unknown";
    NLS = "";
    firstDiscoveryTime = System.currentTimeMillis();
    lastDiscoveryTime = firstDiscoveryTime;
  }

  /**
   * Processes the cache control value
   * 
   * @param cacheControlValue
   *          max age value
   */
  private static int processCacheControl(String cacheControlValue)
  {
    // proof maxage value
    int tmpMaxage = UPnPDefaults.DEVICE_MAX_AGE;

    int equalIndex = cacheControlValue.indexOf("=");
    if (equalIndex != -1 && equalIndex < cacheControlValue.length() - 1)
    {
      String value = cacheControlValue.substring(equalIndex + 1).trim();
      tmpMaxage = Integer.parseInt(value);
    }
    // System.out.println("Parsed value from " + cacheControlValue + " is " + tmpMaxage);

    // check for valid times (Siemens router mixed up seconds and minutes
    if (tmpMaxage == 30)
    {
      tmpMaxage = UPnPDefaults.DEVICE_MAX_AGE;
    }

    // force minimum max-age of 10 minutes
    if (tmpMaxage < UPnPDefaults.CP_DEVICE_MIN_ACCEPTED_MAX_AGE)
    {
      tmpMaxage = UPnPDefaults.CP_DEVICE_MIN_ACCEPTED_MAX_AGE;
    }

    return tmpMaxage;
  }

  /**
   * Return device maximum lease time.
   * 
   * @return maximum lease time
   */
  public int getMaxage()
  {
    return maxage;
  }

  /**
   * Returns device description URL
   * 
   * @return URL device description
   */
  public URL getLocation()
  {
    return location;
  }

  /**
   * Returns device server (OS version and product version).
   * 
   * @return server device server (OS version and product version)
   */
  public String getServer()
  {
    return server;
  }

  /**
   * Sets the server.
   * 
   * @param server
   *          The new value for server
   */
  public void setServer(String server)
  {
    this.server = server;
  }

  /**
   * Returns device UUID
   * 
   * @return device UUID
   */
  public String getRootDeviceUUID()
  {
    if (rootDeviceDiscoveryEntry != null)
    {
      return rootDeviceDiscoveryEntry.uuid;
    }

    return null;
  }

  /**
   * Tests if a device UUID is already known
   * 
   * @param uuid
   *          The uuid
   * 
   * @return The index of the device with this UUID or -1
   */
  public int getDeviceDiscoveryEntryIndex(Vector deviceDiscoveryEntryList, String uuid)
  {
    for (int i = 0; i < deviceDiscoveryEntryList.size(); i++)
    {
      CPDeviceDiscoveryEntry currentEntry = (CPDeviceDiscoveryEntry)deviceDiscoveryEntryList.elementAt(i);
      if (currentEntry.uuid.equals(uuid))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Tests if device has the service type
   * 
   * @param serviceType
   *          service type
   * 
   * @return true if device has the service type, otherwise false
   */
  public boolean containsServiceType(String serviceType)
  {
    if (rootDeviceDiscoveryEntry != null)
    {
      if (rootDeviceDiscoveryEntry.serviceTypeList.contains(serviceType))
      {
        return true;
      }

      // check embedded devices
      for (int i = 0; i < rootDeviceDiscoveryEntry.deviceList.size(); i++)
      {
        CPDeviceDiscoveryEntry currentEntry = (CPDeviceDiscoveryEntry)rootDeviceDiscoveryEntry.deviceList.elementAt(i);

        if (currentEntry.serviceTypeList.contains(serviceType))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns all embedded device
   * 
   * @return embedded device
   */
  public Vector getEmbeddedDevices()
  {
    if (rootDeviceDiscoveryEntry != null)
    {
      return rootDeviceDiscoveryEntry.deviceList;
    }

    return null;
  }

  /**
   * Returns network location signature
   * 
   * @return network location signature
   */
  public String getNLS()
  {
    return NLS;
  }

  /**
   * Processes NT and USN of one NOTIFY or M-SEARCH response message.
   * 
   * @param ntValue
   *          search target
   * @param usnValue
   *          unique service name
   */
  public void processDiscoveryInfo(String ntValue, String usnValue)
  {
    logger.debug("Process SSDP info. NT: " + ntValue + " USN: " + usnValue);
    // check for root device
    boolean isRootDevice = ntValue.equals(UPnPConstant.UPNP_ROOTDEVICE);

    // assume single UUID
    String uuid = usnValue;
    String typeString = "";

    int doubleColonIndex = usnValue.indexOf("::");
    if (doubleColonIndex != -1 && doubleColonIndex < usnValue.length() - 2)
    {
      uuid = usnValue.substring(0, doubleColonIndex);
      typeString = usnValue.substring(doubleColonIndex + 2);
    }
    // System.out.println("Message: UUID: " + uuid + ", Type: " + typeString);

    // create root device
    if (isRootDevice && rootDeviceDiscoveryEntry == null)
    {
      processRootInfo(uuid);
      return;
    }

    // check for device type info
    if (typeString.indexOf(":device:") != -1)
    {
      processDeviceTypeInfo(uuid, typeString);
      return;
    }

    // check for service type info
    if (typeString.indexOf(":service:") != -1)
    {
      processServiceTypeInfo(uuid, typeString);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String result = "";
    // check if root device type
    if (rootDeviceDiscoveryEntry != null)
    {
      result += "Root: " + rootDeviceDiscoveryEntry.uuid + " (" + rootDeviceDiscoveryEntry.deviceType + ")\n";
      for (int i = 0; i < rootDeviceDiscoveryEntry.serviceTypeList.size(); i++)
      {
        result += "    : " + rootDeviceDiscoveryEntry.serviceTypeList.elementAt(i) + "\n";
      }
    }
    return result;
  }

  /**
   * Processes the upnp:rootdevice information.
   * 
   * @param uuid
   *          Root device UUID
   */
  private void processRootInfo(String uuid)
  {
    logger.debug("Process root info: " + uuid);
    rootDeviceDiscoveryEntry = new CPRootDeviceDiscoveryEntry(uuid);
    // search temporary device list for same UUID
    for (int i = 0; i < tempDeviceDiscoveryEntryList.size(); i++)
    {
      CPDeviceDiscoveryEntry currentEntry = (CPDeviceDiscoveryEntry)tempDeviceDiscoveryEntryList.elementAt(i);
      // entry is equal to root device entry, move service vector
      if (currentEntry.uuid.equals(uuid))
      {
        rootDeviceDiscoveryEntry.deviceType = currentEntry.deviceType;
        rootDeviceDiscoveryEntry.serviceTypeList = currentEntry.serviceTypeList;
      } else
      {
        // all other devices must be embedded devices
        System.out.println("Add embedded device with uuid " + currentEntry.uuid + " to root device with uuid " + uuid);
        rootDeviceDiscoveryEntry.deviceList.add(currentEntry);
      }
    }
    // clear temporary list
    tempDeviceDiscoveryEntryList.clear();
  }

  /**
   * Processes device type info.
   * 
   * @param ntValue
   *          search target
   * @param usnValue
   *          unique service name
   */
  private void processDeviceTypeInfo(String uuid, String deviceType)
  {
    logger.debug("Process device type info: " + uuid + ":" + deviceType);
    // check if root device type
    if (rootDeviceDiscoveryEntry != null && rootDeviceDiscoveryEntry.uuid.equals(uuid))
    {
      if (rootDeviceDiscoveryEntry.deviceType == null)
      {
        logger.debug("Set device type for root device: " + deviceType);
        rootDeviceDiscoveryEntry.deviceType = deviceType;
      }
      return;
    }

    // retrieve appropriate device list for storage
    Vector deviceList;
    if (rootDeviceDiscoveryEntry != null)
    {
      deviceList = rootDeviceDiscoveryEntry.deviceList;
    } else
    {
      deviceList = tempDeviceDiscoveryEntryList;
    }

    // retrieve appropriate entry or create new
    CPDeviceDiscoveryEntry entry;
    // check if device entry is already known
    int deviceIndex = getDeviceDiscoveryEntryIndex(deviceList, uuid);
    if (deviceIndex == -1)
    {
      // System.out.println("Create new device discovery entry for uuid " + uuid);
      entry = new CPDeviceDiscoveryEntry(uuid);
      deviceList.add(entry);
    } else
    {
      logger.debug("Root/Non-root device entry with uuid " + uuid + " already exists");
      entry = (CPDeviceDiscoveryEntry)deviceList.get(deviceIndex);
    }
    entry.deviceType = deviceType;
  }

  /**
   * Processes service type info.
   * 
   * @param ntValue
   *          search target
   * @param usnValue
   *          unique service name
   */
  private void processServiceTypeInfo(String uuid, String serviceType)
  {
    logger.debug("Process service type info: " + uuid + ":" + serviceType);
    // check if service of root device
    if (rootDeviceDiscoveryEntry != null && rootDeviceDiscoveryEntry.uuid.equals(uuid))
    {
      if (!rootDeviceDiscoveryEntry.serviceTypeList.contains(serviceType))
      {
        logger.debug("Add service " + serviceType + " to root device with uuid " + uuid);
        rootDeviceDiscoveryEntry.serviceTypeList.add(serviceType);
      }
      return;
    }

    // retrieve appropriate device list for storage
    Vector deviceList;
    if (rootDeviceDiscoveryEntry != null)
    {
      deviceList = rootDeviceDiscoveryEntry.deviceList;
    } else
    {
      deviceList = tempDeviceDiscoveryEntryList;
    }

    // retrieve appropriate entry or create new
    CPDeviceDiscoveryEntry entry;
    int deviceIndex = getDeviceDiscoveryEntryIndex(deviceList, uuid);
    // check if device entry is already known
    if (deviceIndex == -1)
    {
      System.out.println("Create new device discovery entry for uuid " + uuid);
      entry = new CPDeviceDiscoveryEntry(uuid);
      deviceList.add(entry);
    } else
    {
      logger.debug("Root/Non-root device entry with uuid " + uuid + " already exists");
      entry = (CPDeviceDiscoveryEntry)deviceList.get(deviceIndex);
    }
    // add service type to device
    if (!entry.serviceTypeList.contains(serviceType))
    {
      logger.debug("Add service " + serviceType + " to root/Non-root device entry with uuid " + uuid);
      entry.serviceTypeList.add(serviceType);

    }
  }

  /** This entry stores infos about an embedded device. */
  private class CPDeviceDiscoveryEntry
  {

    /** UUID of the embedded device */
    public String uuid;

    /** Device type of the embedded device */
    public String deviceType;

    /** List of services announced by the embedded device */
    public Vector serviceTypeList = new Vector();

    /**
     * Creates a new instance of CPDeviceDiscoveryEntry
     * 
     * @param uuid
     */
    public CPDeviceDiscoveryEntry(String uuid)
    {
      this.uuid = uuid;
    }

    public boolean equals(String uuid)
    {
      return this.uuid.equals(uuid);
    }
  }

  /** This entry stores infos about one root device. */
  private class CPRootDeviceDiscoveryEntry extends CPDeviceDiscoveryEntry
  {
    public Vector deviceList = new Vector();

    public CPRootDeviceDiscoveryEntry(String uuid)
    {
      super(uuid);
    }
  }

  /**
   * Retrieves the invalidationTime.
   * 
   * @return The invalidationTime
   */
  protected long getInvalidationTime()
  {
    return invalidationTime;
  }

  /**
   * Retrieves the invalidDevice.
   * 
   * @return The invalidDevice
   */
  protected boolean isInvalidDevice()
  {
    return invalidDeviceFlag;
  }

  /**
   * Sets the invalidDevice.
   * 
   * @param invalidDevice
   *          The new value for invalidDevice
   */
  protected void setInvalidDevice(boolean invalidDevice)
  {
    this.invalidDeviceFlag = invalidDevice;
    invalidationTime = System.currentTimeMillis();
  }

  /**
   * @return the alternativeIPAddressList
   */
  public Vector getAlternativeIPAddressList()
  {
    return alternativeIPAddressList;
  }

  /**
   * @return The publishedIPAddressList
   */
  public Vector getPublishedIPAddressList()
  {
    return publishedIPAddressList;
  }

  /**
   * 
   * @return The friendlyNameList
   */
  public Vector getFriendlyNameList()
  {
    return friendlyNameList;
  }

  /** Updates the last discovery time */
  public void updateLastDiscoveryTime()
  {
    // do not process resend packets
    if (System.currentTimeMillis() - lastDiscoveryTime > 30000)
    {
      discoveryIntervalList.add(new Long(System.currentTimeMillis() - lastDiscoveryTime));
      incDeviceDetectionCount();
    }
    lastDiscoveryTime = System.currentTimeMillis();
  }

  /** Retrieves the average time between received discovery messages in seconds. */
  public int getAverageDiscoveryTime()
  {
    if (discoveryIntervalList.size() < 2)
    {
      return 0;
    }
    long sum = 0;
    // ignore first sample because it will not contain the correct interval
    // but the time between control point startup and first notify
    for (int i = 1; i < discoveryIntervalList.size(); i++)
    {
      sum += ((Long)discoveryIntervalList.elementAt(i)).longValue();
    }
    return (int)sum / ((discoveryIntervalList.size() - 1) * 1000);
  }

  /** Increments the number of device removals */
  public void incDeviceRemovalCount()
  {
    deviceRemovalCount++;
  }

  /** Increments the number of device time outs */
  public void incDeviceTimedOutCount()
  {
    deviceTimedOutCount++;
  }

  /** Increments the number of device detections */
  public void incDeviceDetectionCount()
  {
    deviceDetectionCount++;
  }

  public int getDeviceRemovalCount()
  {
    return deviceRemovalCount;
  }

  public int getDeviceDetectionCount()
  {
    return deviceDetectionCount;
  }

  /**
   * Retrieves the deviceTimedOutCount.
   * 
   * @return The deviceTimedOutCount
   */
  public int getDeviceTimedOutCount()
  {
    return deviceTimedOutCount;
  }

  /**
   * Retrieves the firstDiscoveryTime.
   * 
   * @return The firstDiscoveryTime
   */
  public long getFirstDiscoveryTime()
  {
    return firstDiscoveryTime;
  }

  /**
   * Retrieves the lastDiscoveryTime.
   * 
   * @return The lastDiscoveryTime
   */
  public long getLastDiscoveryTime()
  {
    return lastDiscoveryTime;
  }

}

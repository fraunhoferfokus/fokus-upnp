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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * A CPDevice represents a remote UPnP device.
 * 
 * @author zis, Alexander Koenig
 * 
 */
public class CPDevice extends AbstractDevice implements ICPStateVariableListener
{

  /** Device logger */
  public static Logger                           logger                    = Logger.getLogger("upnp.cp");

  /** Control point that discovered the device */
  private ControlPoint                           associatedControlPoint;

  /** Device description URL for this device */
  private URL                                    deviceDescriptionURL;

  /** Device description for this device */
  private String                                 deviceDescription;

  /** Socket structure that discovered this device */
  private ControlPointHostAddressSocketStructure discoverySocketStructure;

  /** Address of device description server for this device */
  private InetSocketAddress                      deviceDescriptionSocketAddress;

  /** Optional listeners for state variable change events */
  private Vector                                 stateVariableListenerList = new Vector();

  /** Optional information found in an attribute service */
  private Hashtable                              attributeHashtable        = null;

  /** Optional information found in a translation service */
  private Hashtable                              translationTable          = null;

  /** Optional information found in a usage service */
  private Hashtable                              upnpDocFromLanguageTable  = new Hashtable();

  /** Flag that this device was read from the cache */
  private boolean                                cachedDevice;

  /** Flag that the device can be cached */
  private boolean                                cacheable;

  /** Expected end of lifetime */
  private long                                   expectedLifetime;

  /** Flag that a request to this device is pending */
  private boolean                                pendingHeadRequest        = false;

  /** Symmetric key used to personalize actions on that device. */
  private SymmetricKeyInfo                       personalizationSymmetricKeyInfo;

  /** Sync object to serialize personalized requests */
  private Object                                 personalizationSyncLock;

  /** Optional baseURL (no longer recommended) */
  // private URL baseURL;
  /**
   * Creates a control point device. The parameters are delegated to the constructor of the
   * superclass.
   * 
   * @param deviceType
   *          type of the device
   * @param friendlyName
   *          short description of the device for the end user
   * @param manufacturer
   *          manufacturer of the device
   * @param modelName
   *          the model name of the device
   * @param UDN
   *          unique device name
   * @param rootDevice
   *          flag for root device, false for embedded a device
   * @param IPVersion
   *          IP stack version
   * @param control
   *          point The associated control point
   * @param socketStructure
   *          The socket structure that discovered the device
   */
  public CPDevice(String deviceType,
    String friendlyName,
    String manufacturer,
    String modelName,
    String UDN,
    boolean rootDevice,
    int IPVersion,
    ControlPoint controlPoint,
    ControlPointHostAddressSocketStructure socketStructure)
  {
    super(deviceType, friendlyName, manufacturer, modelName, UDN, rootDevice, IPVersion);
    associatedControlPoint = controlPoint;
    this.discoverySocketStructure = socketStructure;
    pendingHeadRequest = false;
    personalizationSyncLock = new Object();
  }

  /** Checks if this is a CPDevice. */
  public boolean isCPDevice()
  {
    return true;
  }

  /**
   * Sets the list with all services found on the remote device.
   * 
   * @param serviceTable
   *          The device services
   */
  public void setCPServiceTable(CPService[] serviceTable)
  {
    this.serviceTable = serviceTable;
  }

  /**
   * Returns a list with all services found on the remote device.
   * 
   * @return The device services
   */
  public CPService[] getCPServiceTable()
  {
    return (CPService[])serviceTable;
  }

  /**
   * Returns the control point service that has the specified service id.
   * 
   * @param serviceID
   *          service id
   * @return service or null if there is no such service or if it is not a CPService
   */
  public CPService getCPServiceByID(String serviceID)
  {
    CPService result = null;

    CPService list[] = getCPServiceTable();
    if (list != null)
    {
      for (int i = 0; i < list.length; i++)
      {
        if (list[i].getServiceId().equalsIgnoreCase(serviceID))
        {
          result = list[i];
          break;
        }
      }
    }

    return result;
  }

  /**
   * Returns the control point service that has the specified service id.
   * 
   * @param shortenedServiceID
   *          shortened service id
   * @return service or null if there is no such service or if it is not a CPService
   */
  public CPService getCPServiceByShortenedID(String shortenedServiceID)
  {
    CPService result = null;

    CPService list[] = getCPServiceTable();
    if (list != null)
    {
      for (int i = 0; i < list.length; i++)
      {
        if (list[i].getShortenedServiceId().equalsIgnoreCase(shortenedServiceID))
        {
          result = list[i];
          break;
        }
      }
    }

    return result;
  }

  /**
   * Return the control point service that has the specified service type.
   * 
   * @param serviceType
   *          service type
   * @return service or null if there is either none, or if it is not a CPService
   */
  public CPService getCPServiceByType(String serviceType)
  {
    CPService result = null;
    CPService list[] = getCPServiceTable();
    if (list != null)
    {
      for (int i = 0; i < list.length; i++)
      {
        if (list[i].getServiceType().equalsIgnoreCase(serviceType))
        {
          result = list[i];
          break;
        }
      }
    }
    return result;
  }

  /**
   * Sets embedded control point devices. The array is not copied. Methods using this class shall
   * provide an array of CPDevices instead of Devices. The array is simply delegated to the
   * superclass.
   * 
   * @param deviceTable
   *          embedded devices or null
   */
  protected void setDeviceTable(CPDevice[] deviceTable)
  {
    this.deviceTable = deviceTable;
  }

  /**
   * Returns embedded control point devices. The method retrieves the list from the superclass. If
   * the list is null or already a CPDevice array, then this is directly returned. Otherwise a new
   * array is created with all elements. If any of these elements is not an instance of CPDevice,
   * then the method only returns null.
   * 
   * @return embedded devices or null
   */
  public CPDevice[] getCPDeviceTable()
  {
    CPDevice result[];

    AbstractDevice list[] = getAbstractDeviceTable();
    if (list == null || list instanceof CPDevice[])
    {
      result = (CPDevice[])list;
    } else
    {
      int elements = list.length;
      result = new CPDevice[elements];
      if (elements > 0)
      {
        try
        {
          System.arraycopy(list, 0, result, 0, elements);
        } catch (ClassCastException cce)
        {
          logger.error("CPDeviceList contains normal Devices", cce);
          result = null;
        }
      }
    }

    return result;
  }

  /** Retrieves the control point */
  public ControlPoint getControlPoint()
  {
    return associatedControlPoint;
  }

  /**
   * Sets device description's URL
   * 
   * @param deviceDescriptionURL
   *          device description's URL
   */
  protected void setDeviceDescriptionURL(URL deviceDescriptionURL)
  {
    this.deviceDescriptionURL = deviceDescriptionURL;
    try
    {
      deviceDescriptionSocketAddress =
        new InetSocketAddress(InetAddress.getByName(deviceDescriptionURL.getHost()), deviceDescriptionURL.getPort());
    } catch (Exception ex)
    {
    }
  }

  /** Retrieves the device description URL for this device. */
  public URL getDeviceDescriptionURL()
  {
    return deviceDescriptionURL;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#getDeviceDescriptionURL(java.net.InetSocketAddress)
   */
  public String getDeviceDescriptionURL(InetSocketAddress serverAddress)
  {
    return deviceDescriptionURL.toExternalForm();
  }

  /**
   * Retrieves the socket structure that received the NOTIFY or HTTP OK messages for this device
   * 
   * @return device socket structure
   */
  public ControlPointHostAddressSocketStructure getCPSocketStructure()
  {
    return discoverySocketStructure;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#getDeviceDescriptionSocketAddress()
   */
  public InetSocketAddress getDeviceDescriptionSocketAddress()
  {
    return deviceDescriptionSocketAddress;
  }

  /** Retrieves the IP address of the device. */
  public InetAddress getDeviceAddress()
  {
    return deviceDescriptionSocketAddress.getAddress();
  }

  /**
   * Retrieves the deviceDescription.
   * 
   * @return The deviceDescription
   */
  public String getDeviceDescription()
  {
    return deviceDescription;
  }

  /**
   * Sets the deviceDescription.
   * 
   * @param deviceDescription
   *          The new value for deviceDescription
   */
  protected void setDeviceDescription(String deviceDescription)
  {
    this.deviceDescription = deviceDescription;
  }

  /**
   * Retrieves the network interface address that received the NOTIFY or HTTP OK messages for this
   * device
   * 
   * @return The network interface that discovered the device
   */
  public InetAddress getCPNetworkInterface()
  {
    return discoverySocketStructure.getHostAddress();
  }

  // public void setURLBase(URL baseURL)
  // {
  // this.baseURL = baseURL;
  // }

  /**
   * Retrieves the descriptionHashtable.
   * 
   * @return The descriptionHashtable
   */
  public Hashtable getAttributeHashtable()
  {
    return attributeHashtable;
  }

  /**
   * Sets the descriptionHashtable.
   * 
   * @param descriptionHashtable
   *          The new value for descriptionHashtable
   */
  protected void setAttributeHashtable(Hashtable descriptionHashtable)
  {
    this.attributeHashtable = descriptionHashtable;
  }

  /**
   * Retrieves the translationTable.
   * 
   * @return The translationTable
   */
  public Hashtable getTranslationTable()
  {
    return translationTable;
  }

  /**
   * Sets the translationTable.
   * 
   * @param translationTable
   *          The new value for translationTable
   */
  public void setTranslationTable(Hashtable translationTable)
  {
    this.translationTable = translationTable;
  }

  /**
   * Retrieves the upnpDocFromLanguageTable.
   * 
   * @return The upnpDocFromLanguageTable
   */
  public Hashtable getUPnPDocFromLanguageTable()
  {
    return upnpDocFromLanguageTable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    // handle up to control point
    associatedControlPoint.stateVariableChanged(stateVariable);

    // handle to optional listeners
    for (int i = 0; i < stateVariableListenerList.size(); i++)
    {
      ((ICPStateVariableListener)stateVariableListenerList.elementAt(i)).stateVariableChanged(stateVariable);
    }
  }

  /** Event that the subscription state of a service has changed */
  public void subscriptionStateChanged(CPService service)
  {
    // handle up to control point
    associatedControlPoint.subscriptionStateChanged(service);
  }

  /**
   * Adds a listener for state variable changes.
   * 
   * @param eventListener
   *          The event listener
   */
  public void addStateVariableListener(ICPStateVariableListener eventListener)
  {
    if (stateVariableListenerList.indexOf(eventListener) == -1)
    {
      stateVariableListenerList.add(eventListener);
    }
  }

  /** Retrieves a XML description of all basic device properties. */
  public String toPropertyDescription()
  {
    StringBuffer result = new StringBuffer(256);

    result.append(XMLHelper.createTag(XMLConstant.DEVICETYPE_TAG, getDeviceType()));
    result.append(XMLHelper.createTag(XMLConstant.FRIENDLYNAME_TAG, getFriendlyName()));
    result.append(XMLHelper.createTag(XMLConstant.MANUFACTURER_TAG, getManufacturer()));
    if (getManufacturerURL() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.MANUFACTURER_URL_TAG, getManufacturerURL().toString()));
    }
    if (getModelDescription() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.MODEL_DESCRIPTION_TAG, getModelDescription()));
    }
    result.append(XMLHelper.createTag(XMLConstant.MODEL_NAME_TAG, getModelName()));
    if (getModelNumber() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.MODEL_NUMBER_TAG, getModelNumber()));
    }
    if (getModelURL() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.MODEL_URL_TAG, getModelURL().toString()));
    }
    if (getSerialNumber() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.SERIALNUMBER_TAG, getSerialNumber()));
    }
    result.append(XMLHelper.createTag(XMLConstant.UDN_TAG, getUDN()));
    if (getUPC() != null)
    {
      result.append(XMLHelper.createTag(XMLConstant.UPC_TAG, getUPC()));
    }
    // add properties from description service
    if (getAttributeHashtable() != null)
    {
      Enumeration keys = getAttributeHashtable().keys();
      while (keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        result.append(XMLHelper.createTag(key, (String)getAttributeHashtable().get(key)));
      }
    }
    return result.toString();
  }

  /** Checks if this device has a certain property value. */
  public boolean hasPropertyValue(String property, String value)
  {
    if (property == null || property.length() == 0 || value == null)
    {
      return false;
    }

    if (property.equalsIgnoreCase(XMLConstant.DEVICETYPE_TAG))
    {
      return getDeviceType().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.FRIENDLYNAME_TAG))
    {
      return getFriendlyName().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MANUFACTURER_TAG))
    {
      return getManufacturer().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MANUFACTURER_URL_TAG))
    {
      return getManufacturerURL() != null && getManufacturerURL().toString().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_DESCRIPTION_TAG))
    {
      return getModelDescription() != null && getModelDescription().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_NAME_TAG))
    {
      return getModelName().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_NUMBER_TAG))
    {
      return getModelNumber() != null && getModelNumber().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_URL_TAG))
    {
      return getModelURL() != null && getModelURL().toString().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.SERIALNUMBER_TAG))
    {
      return getSerialNumber() != null && getSerialNumber().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.UDN_TAG))
    {
      return getUDN().equalsIgnoreCase(value);
    }

    if (property.equalsIgnoreCase(XMLConstant.UPC_TAG))
    {
      return getUPC() != null && getUPC().equalsIgnoreCase(value);
    }

    // add properties from description service
    if (getAttributeHashtable() != null)
    {
      Enumeration keys = getAttributeHashtable().keys();
      while (keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        if (key.equalsIgnoreCase(property))
        {
          return value.equalsIgnoreCase((String)getAttributeHashtable().get(key));
        }
      }
    }
    return false;
  }

  /**
   * Checks if this device has a fuzzy property value. This is done by checking
   * property.toLowerCase.indexOf(value.toLowerCase()) != -1.
   */
  public boolean hasFuzzyPropertyValue(String property, String value)
  {
    if (property == null || property.length() == 0 || value == null)
    {
      return false;
    }

    value = value.toLowerCase();

    if (property.equalsIgnoreCase(XMLConstant.DEVICETYPE_TAG))
    {
      return getDeviceType().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.FRIENDLYNAME_TAG))
    {
      return getFriendlyName().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MANUFACTURER_TAG))
    {
      return getManufacturer().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MANUFACTURER_URL_TAG))
    {
      return getManufacturerURL() != null && getManufacturerURL().toString().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_DESCRIPTION_TAG))
    {
      return getModelDescription() != null && getModelDescription().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_NAME_TAG))
    {
      return getModelName().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_NUMBER_TAG))
    {
      return getModelNumber() != null && getModelNumber().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.MODEL_URL_TAG))
    {
      return getModelURL() != null && getModelURL().toString().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.SERIALNUMBER_TAG))
    {
      return getSerialNumber() != null && getSerialNumber().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.UDN_TAG))
    {
      return getUDN().toLowerCase().indexOf(value) != -1;
    }

    if (property.equalsIgnoreCase(XMLConstant.UPC_TAG))
    {
      return getUPC() != null && getUPC().toLowerCase().indexOf(value) != -1;
    }

    // add properties from description service
    if (getAttributeHashtable() != null)
    {
      Enumeration keys = getAttributeHashtable().keys();
      while (keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        if (key.equalsIgnoreCase(property))
        {
          return ((String)getAttributeHashtable().get(key)).toLowerCase().indexOf(value) != -1;
        }
      }
    }
    return false;
  }

  /**
   * Retrieves the cachedDevice.
   * 
   * @return The cachedDevice
   */
  public boolean isCachedDevice()
  {
    return cachedDevice;
  }

  /**
   * Sets the cachedDevice.
   * 
   * @param cachedDevice
   *          The new value for cachedDevice
   */
  protected void setCachedDevice(boolean cachedDevice)
  {
    this.cachedDevice = cachedDevice;
  }

  /**
   * Sets the URL of the manufacture web page
   * 
   * @param manufacturerURL
   *          manufacturer's URL
   */
  protected void setManufacturerURL(URL manufacturerURL)
  {
    this.manufacturerURL = manufacturerURL;
  }

  /**
   * Sets model description of the device (long description)
   * 
   * @param modelDescription
   *          model description
   */
  protected void setModelDescription(String modelDescription)
  {
    this.modelDescription = modelDescription;
  }

  /**
   * Sets model name of the device (long description)
   * 
   * @param modelName
   *          model name
   */
  protected void setModelName(String modelName)
  {
    this.modelName = modelName;
  }

  /**
   * Sets model number of the device
   * 
   * @param modelNumber
   *          model number
   */
  protected void setModelNumber(String modelNumber)
  {
    this.modelNumber = modelNumber;
  }

  /**
   * Sets URL of the device model
   * 
   * @param modelURL
   *          url of the device model
   */
  protected void setModelURL(URL modelURL)
  {
    this.modelURL = modelURL;
  }

  /**
   * Sets device serial number
   * 
   * @param serialNumber
   *          device serial number
   */
  protected void setSerialNumber(String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  /**
   * Sets UPC (universal product code) of the device
   * 
   * @param UPC
   *          universal product code
   */
  protected void setUPC(String UPC)
  {
    this.UPC = UPC;
  }

  /**
   * Sets the max lease time of the device
   * 
   * @param maxage
   *          max lease time
   */
  protected void setMaxage(int maxage)
  {
    this.maxage = maxage;
    expectedLifetime = System.currentTimeMillis() + maxage * 1000;
  }

  /**
   * Sets Network Location Signature
   * 
   * @param NLS
   *          Network Location Signature
   */
  protected void setNLS(String NLS)
  {
    this.NLS = NLS;
  }

  /**
   * Sets the server info (OS / product version)
   * 
   * @param server
   *          server info
   */
  protected void setServer(String server)
  {
    this.server = server;
  }

  /**
   * Sets device icons
   * 
   * @param iconList
   *          device icons
   */
  protected void setIconList(DeviceIcon[] iconList)
  {
    this.iconTable = iconList;
  }

  /**
   * Sets the ssdpMulticastSocketAddress.
   * 
   * @param ssdpMulticastSocketAddress
   *          The ssdpMulticastSocketAddress to set.
   */
  protected void setSSDPMulticastSocketAddress(InetSocketAddress ssdpMulticastSocketAddress)
  {
    this.ssdpMulticastSocketAddress = ssdpMulticastSocketAddress;
  }

  /**
   * Sets the url for the presentation page for the device. (control page for the device)
   * 
   * @param presentationURL
   *          the URL of the presentation page
   */
  protected void setPresentationURL(String presentationURL)
  {
    this.presentationURL = presentationURL;
  }

  /**
   * Sets the rootDevice flag
   * 
   * @param rootDevice
   *          root device flag
   */
  protected void setRootDevice(boolean rootDevice)
  {
    this.rootDevice = rootDevice;
  }

  /**
   * Retrieves the expectedLifetime.
   * 
   * @return The expectedLifetime
   */
  public long getExpectedLifetime()
  {
    return expectedLifetime;
  }

  /**
   * Sets the expectedLifetime.
   * 
   * @param expectedLifetime
   *          The new value for expectedLifetime
   */
  public void setExpectedLifetime(long expectedLifetime)
  {
    this.expectedLifetime = expectedLifetime;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.AbstractDevice#isCacheable()
   */
  public boolean isCacheable()
  {
    return cacheable;
  }

  /**
   * Sets the cacheable flag.
   * 
   * @param cacheable
   */
  public void setCacheable(boolean cacheable)
  {
    this.cacheable = cacheable;
  }

  /**
   * Retrieves the pendingDeviceRequest flag.
   * 
   * @return The pendingDeviceRequest
   */
  public boolean isPendingHeadRequest()
  {
    return pendingHeadRequest;
  }

  /**
   * Sets the pendingDeviceRequest flag.
   * 
   * @param pendingDeviceRequest
   *          The new value for pendingDeviceRequest
   */
  public void setPendingHeadRequest(boolean pendingDeviceRequest)
  {
    this.pendingHeadRequest = pendingDeviceRequest;
  }

  /**
   * Retrieves the symmetricKeyInfo.
   * 
   * @return The symmetricKeyInfo
   */
  public SymmetricKeyInfo getPersonalizationSymmetricKeyInfo()
  {
    return personalizationSymmetricKeyInfo;
  }

  /**
   * Sets the symmetricKeyInfo.
   * 
   * @param symmetricKeyInfo
   *          The new value for symmetricKeyInfo
   */
  public void setPersonalizationSymmetricKeyInfo(SymmetricKeyInfo symmetricKeyInfo)
  {
    this.personalizationSymmetricKeyInfo = symmetricKeyInfo;
  }

  /**
   * Retrieves the personalizationSyncLock.
   * 
   * @return The personalizationSyncLock
   */
  public Object getPersonalizationSyncLock()
  {
    return personalizationSyncLock;
  }

  /** Checks if this device supports personalization. */
  public boolean supportsPersonalization()
  {
    return getCPServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE) != null;
  }

  /**
   * Sets the descriptionHashBase64.
   * 
   * @param descriptionHashBase64
   *          The new value for descriptionHashBase64
   */
  public void setDescriptionHashBase64(String descriptionHashBase64)
  {
    this.descriptionHashBase64 = descriptionHashBase64;
  }

}

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
package de.fraunhofer.fokus.upnp.core.device;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.AbstractService;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.event.IDeviceWebServerListener;
import de.fraunhofer.fokus.upnp.core.exceptions.DescriptionNotCreatedException;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.UUIDHelper;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAKeyPair;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;

/**
 * This class represent a UPnP device.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class Device extends AbstractDevice
{

  /** Device logger */
  public static Logger                  logger                                = Logger.getLogger("upnp.device");

  // Table of socket structures for all external IP addresses
  private Hashtable                     socketStructureFromHostAddressTable   = new Hashtable();

  // Table of device descriptions for all external IP addresses
  private Hashtable                     deviceDescriptionFromHostAddressTable = new Hashtable();

  /** Message processor factory for the device */
  private DeviceMessageProcessorFactory messageProcessorFactory;

  // Parser for SOAP, GENA, GET, HEAD etc.
  protected DeviceHTTPMessageProcessor  httpMessageProcessor;

  /** Directory for resource files etc. */
  private String                        workingDirectory                      = "";

  /** String used as base for the UDN. */
  private String                        udnBase                               = "";

  /** Vector with different directories for resources accessible via the internal web server */
  private Vector                        webServerDirectoryList                = new Vector();

  /** Vector with different directories for resources accessible via the internal web server */
  private Vector                        classLoaderResourceDirectoryList      = new Vector();

  /** Optional listener for web server requests */
  private IDeviceWebServerListener      webServerListener;

  /** Flag to use fixed ports */
  protected boolean                     useFixedPorts                         = false;

  protected int                         ssdpUnicastPort;

  protected int                         httpServerPort;

  /** Flag to send multicast events */
  protected boolean                     useMulticastEvents                    = false;

  /** Number of services that use multicast events */
  protected int                         multicastEventThreadCount             = 0;

  /** Used multicast address (usually 239.255.255.250:1900) */
  protected InetSocketAddress           ssdpMulticastSocketAddress;

  /** Optional event multicast address */
  protected InetSocketAddress           multicastEventAddress;

  /** Optional multicast event request handler */
  protected DeviceMulticastEventHandler multicastEventHandler;

  /** The startup configuration for the outer entity */
  protected UPnPStartupConfiguration    startupConfiguration;

  /** The startup configuration for this device */
  protected DeviceStartupConfiguration  deviceStartupConfiguration;

  /**
   * This counter is increased by one each time a new subscriber requests a subscribtion. This counter is used by the
   * getNewSubscriptionUUID method to create a unique identifier (uuid) for a subscriber.
   */
  private long                          subscriptionCounter                   = 0;

  /** RSA keys for personalization */
  protected KeyPair                     personalizationKeyPair;

  /** Thread to handle management tasks */
  protected EventThread                 deviceEventThread;

  /**
   * Creates a device but does not start servers etc.
   * 
   * @param deviceType
   *          type of the device
   * @param friendlyName
   *          short description of the device for the end user
   * @param manufacturer
   *          manufactures name
   * @param modelName
   *          model name of the device
   * @param UDN
   *          unique device name
   */
  public Device(UPnPStartupConfiguration startupConfiguration)
  {
    super();

    // store reference to entity startup configuration
    this.startupConfiguration = startupConfiguration;
    setWorkingDirectory(startupConfiguration.getWorkingDirectory());
    setSSDPMulticastSocketAddress(startupConfiguration.getSSDPMulticastSocketAddress());

    deviceStartupConfiguration = getDeviceStartupConfiguration();
    // search for device configuration
    if (deviceStartupConfiguration == null)
    {
      System.out.println("Missing device configuration for " + getDeviceClassName());
      return;
    }
    if (!deviceStartupConfiguration.isValidDeviceConfiguration())
    {
      System.out.println("Incomplete device configuration for " + getDeviceClassName());
      return;
    }
    System.out.println("  Create device " + deviceStartupConfiguration.getFriendlyName() + "...");
    this.deviceType = deviceStartupConfiguration.getDeviceType();
    this.friendlyName = deviceStartupConfiguration.getFriendlyName();
    this.manufacturer = deviceStartupConfiguration.getManufacturer();
    this.modelName = deviceStartupConfiguration.getModelName();
    // always add host address to UDN to make devices different on different machines
    udnBase = deviceStartupConfiguration.getUDN() + "_" + IPHelper.getLocalHostAddressString();

    // load all directories that can contain resources for the internal web server
    // always add common resources
    addWebServerDirectory(FileHelper.getResourceDirectoryName() + "web_server_common");
    addClassloaderResourceDirectory("web_server_common");

    Vector webServerDirectoryList = deviceStartupConfiguration.getWebServerDirectoryList();
    for (int i = 0; i < webServerDirectoryList.size(); i++)
    {
      String currentDirectory = (String)webServerDirectoryList.elementAt(i);
      addWebServerDirectory(currentDirectory);
      addClassloaderResourceDirectory(currentDirectory);
    }
    this.rootDevice = true;
    maxage = UPnPDefaults.DEVICE_MAX_AGE;
    server = UPnPConstant.SERVER;
    NLS = UPnPConstant.NLS_Value;
    deviceEventThread = new EventThread(toString());

    this.useMulticastEvents = deviceStartupConfiguration.getBooleanProperty("UseMulticastEvents");
    // store socket address used for multicast events
    if (useMulticastEvents && deviceStartupConfiguration.getProperty("MulticastEventSocketAddress") != null)
    {
      multicastEventAddress =
        IPHelper.toSocketAddress(deviceStartupConfiguration.getProperty("MulticastEventSocketAddress"));
    }
    if (useMulticastEvents && multicastEventAddress == null)
    {
      System.out.println("Disable multicast eventing due to missing event socket address");
      useMulticastEvents = false;
    }
    if (useMulticastEvents)
    {
      multicastEventHandler = new DeviceMulticastEventHandler(this);
    }
    // set fixed ports if necessary
    if (deviceStartupConfiguration.useFixedPorts())
    {
      useFixedPorts = true;
      this.ssdpUnicastPort = deviceStartupConfiguration.getSSDPUnicastPort();
      this.httpServerPort = deviceStartupConfiguration.getHTTPServerPort();
    }
    // override working directory of entity if necessary
    if (deviceStartupConfiguration.getWorkingDirectory().length() > 0)
    {
      setWorkingDirectory(deviceStartupConfiguration.getWorkingDirectory());
    }
    messageProcessorFactory = getInstanceOfDeviceMessageProcessorFactory();
  }

  /** First step: This method can be used to setup variables prior to device initialization */
  public void setupDeviceVariables()
  {
    // update IPVersion if not set
    if (IPVersion == 0)
    {
      // get IP version
      String sIPVersion = System.getProperty("PreferIPVersion", UPnPConstant.IPv4_VERSION);

      if (sIPVersion.equals(UPnPConstant.IPv4_VERSION))
      {
        IPVersion = UPnPConstant.IP4;
      } else if (sIPVersion.equals(UPnPConstant.IPv6_VERSION))
      {
        IPVersion = UPnPConstant.IP6;
      } else
      {
        logger.error("unknown IP version" + sIPVersion);
        return;
      }
    }
    // Parser for SOAP, GENA, GET etc.
    httpMessageProcessor = messageProcessorFactory.getInstanceOfHTTPMessageProcessor(this);

    // set SSDPMulticast address
    if (ssdpMulticastSocketAddress == null)
    {
      ssdpMulticastSocketAddress = SSDPConstant.getSSDPMulticastSocketAddress();
    }
  }

  /**
   * Second step: Initialize device specific properties. This method is called after setupDeviceVariables().
   */
  public void initDeviceContent()
  {

  }

  /** Third step: This method is executed after device initialization */
  public void runDevice()
  {
    if (rootDevice)
    {
      // start sockets and server
      initHostAddressSocketStructures();

      // build valid UUID from UDN
      this.UDN = getUUID();
      TemplateDevice.printMessage(friendlyName + ": " + UDN);

      // check if multicast events are supported
      if (multicastEventHandler != null)
      {
        // create multicast delivery URL for all services
        for (int i = 0; i < getDeviceServiceTable().length; i++)
        {
          getDeviceServiceTable()[i].trySetMulticastEventDeliveryURL();
        }
      }

      // create the device and service descriptions to build a unique hash
      try
      {
        StringBuffer hashContent = new StringBuffer(16384);

        hashContent.append(toXMLDescription("localhost"));
        addServiceDescriptions(hashContent);

        descriptionHashBase64 =
          Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(hashContent.toString()));

        // System.out.println("Description hash is " + descriptionHashBase64);

      } catch (Exception e)
      {
        System.out.println("Error creating device or service description: " + e.getMessage());
      }
      deviceEventThread.start();
    }
  }

  /** Retrieves the UUID to use for this device. May be overridden in descendant classes */
  protected String getUUID()
  {
    return "uuid:" + UUIDHelper.getUUIDFromName(udnBase);
  }

  /**
   * Retrieves the message processor factory for the device. This method may be overwritten by descendant classes to use
   * other message processors.
   */
  protected DeviceMessageProcessorFactory getInstanceOfDeviceMessageProcessorFactory()
  {
    return new DeviceMessageProcessorFactory();
  }

  /**
   * Retrieves the support processor factory for the device. This method may be overwritten by descendant classes to use
   * other message processors.
   */
  protected DeviceSupportFactory getInstanceOfDeviceSupportFactory()
  {
    return new DeviceSupportFactory();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.AbstractDevice#isInternalDevice()
   */
  public boolean isInternalDevice()
  {
    return true;
  }

  /** Returns a list with embedded devices. */
  public Device[] getEmbeddedDeviceTable()
  {
    return (Device[])deviceTable;
  }

  /** Retrieves the appropriate startup configuration for this device */
  public DeviceStartupConfiguration getDeviceStartupConfiguration()
  {
    if (startupConfiguration == null)
    {
      return null;
    }

    DeviceStartupConfiguration deviceStartupConfiguration =
      (DeviceStartupConfiguration)startupConfiguration.getDeviceStartupConfiguration(getDeviceClassName());
    if (deviceStartupConfiguration != null)
    {
      return deviceStartupConfiguration;
    }

    deviceStartupConfiguration = (DeviceStartupConfiguration)startupConfiguration.getSingleDeviceStartupConfiguration();
    if (deviceStartupConfiguration != null)
    {
      return deviceStartupConfiguration;
    }

    return null;
  }

  /** Adds a service to this device */
  public void addDeviceService(DeviceService service)
  {
    // initialize array if needed
    if (serviceTable == null)
    {
      serviceTable = new AbstractService[0];
    }

    // extend array
    DeviceService[] result = new DeviceService[serviceTable.length + 1];
    // copy existing services
    for (int i = 0; i < serviceTable.length; i++)
    {
      result[i] = getDeviceServiceTable()[i];
    }
    // add new service
    result[serviceTable.length] = service;
    // update service list
    serviceTable = result;
  }

  /**
   * Sets device services.
   * 
   * @param serviceList
   *          device services
   */
  public void setDeviceServiceTable(DeviceService[] serviceList)
  {
    this.serviceTable = serviceList;
  }

  /**
   * Returns device services
   * 
   * @return device services
   */
  public DeviceService[] getDeviceServiceTable()
  {
    return (DeviceService[])serviceTable;
  }

  /**
   * Return service that specified by the service id.
   * 
   * @param serviceID
   *          service id
   * @return service, if device has no this service than return null
   */
  public DeviceService getDeviceServiceByID(String serviceID)
  {
    if (serviceTable != null)
    {
      for (int i = 0; i < serviceTable.length; i++)
      {
        if (serviceTable[i].getServiceId().equalsIgnoreCase(serviceID))
        {
          return (DeviceService)serviceTable[i];
        }
      }
    }

    return null;
  }

  /**
   * Return service that specified by the shortened service id.
   * 
   * @param shortenedServiceID
   *          shortened service id
   * 
   * @return service with this shortened service ID or null
   */
  public DeviceService getDeviceServiceByShortenedID(String shortenedServiceID)
  {
    if (serviceTable != null)
    {
      for (int i = 0; i < serviceTable.length; i++)
      {
        if (serviceTable[i].getShortenedServiceId().equalsIgnoreCase(shortenedServiceID))
        {
          return (DeviceService)serviceTable[i];
        }
      }
    }

    return null;
  }

  /**
   * Return service that specified by the service type.
   * 
   * @param serviceType
   *          service type
   * @return service, if device has no this service than return null
   */
  public DeviceService getDeviceServiceByType(String serviceType)
  {
    if (serviceTable != null)
    {
      for (int i = 0; i < serviceTable.length; i++)
      {
        if (serviceTable[i].getServiceType().equalsIgnoreCase(serviceType))
        {
          return (DeviceService)serviceTable[i];
        }
      }
    }

    return null;
  }

  /**
   * Sets embedded devices
   * 
   * @param deviceList
   *          embedded devices
   */
  public void setEmbeddedDeviceList(Device[] deviceList)
  {
    this.deviceTable = deviceList;
  }

  /**
   * Sets the url for the presentation page for the device. (control page for the device)
   * 
   * @param presentationURL
   *          the URL of the presentation page
   */
  public void setPresentationURL(String presentationURL)
  {
    this.presentationURL = presentationURL;
  }

  /**
   * Returns presentation page URL
   * 
   * @return the url of the presentation page
   */
  public String getPresentationURL()
  {
    return presentationURL;
  }

  /**
   * Sets the rootDevice flag
   * 
   * @param rootDevice
   *          root device flag
   */
  public void setRootDevice(boolean rootDevice)
  {
    this.rootDevice = rootDevice;
  }

  /**
   * Return root device's flag
   * 
   * @return root device's flag
   */
  public boolean getRootDevice()
  {
    return rootDevice;
  }

  /**
   * Sets the max lease time of the device
   * 
   * @param maxage
   *          max lease time
   */
  public void setMaxage(int maxage)
  {
    this.maxage = maxage;
  }

  /**
   * Returns max lease time
   * 
   * @return max lease time
   */
  public int getMaxage()
  {
    return maxage;
  }

  /**
   * Sets the server info (OS / product version)
   * 
   * @param server
   *          server info
   */
  public void setServer(String server)
  {
    this.server = server;
  }

  /**
   * Sets device friendly name
   * 
   * @param name
   *          device friendly name
   */
  public void setFriendlyName(String name)
  {
    friendlyName = name;
  }

  /**
   * Sets the URL of the manufacture web page
   * 
   * @param manufacturerURL
   *          manufacturer's URL
   */
  public void setManufacturerURL(URL manufacturerURL)
  {
    this.manufacturerURL = manufacturerURL;
  }

  /**
   * Sets model description of the device (long description)
   * 
   * @param modelDescription
   *          model description
   */
  public void setModelDescription(String modelDescription)
  {
    this.modelDescription = modelDescription;
  }

  /**
   * Sets model name of the device (long description)
   * 
   * @param modelName
   *          model name
   */
  public void setModelName(String modelName)
  {
    this.modelName = modelName;
  }

  /**
   * Sets model number of the device
   * 
   * @param modelNumber
   *          model number
   */
  public void setModelNumber(String modelNumber)
  {
    this.modelNumber = modelNumber;
  }

  /**
   * Sets URL of the device model
   * 
   * @param modelURL
   *          url of the device model
   */
  public void setModelURL(URL modelURL)
  {
    this.modelURL = modelURL;
  }

  /**
   * Sets device serial number
   * 
   * @param serialNumber
   *          device serial number
   */
  public void setSerialNumber(String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  /** Sets a unique device name */
  public void setUDN(String UDN)
  {
    this.UDN = UDN;
  }

  /**
   * Sets UPC (universal product code) of the device
   * 
   * @param UPC
   *          universal product code
   */
  public void setUPC(String UPC)
  {
    this.UPC = UPC;
  }

  /**
   * Sets device icons
   * 
   * @param iconList
   *          device icons
   */
  public void setIconList(DeviceIcon[] iconList)
  {
    this.iconTable = iconList;
  }

  /**
   * Returns server info (OS / product version)
   * 
   * @return server info
   */
  public String getServer()
  {
    return server;
  }

  /** Retrieves the absolute device description URL for a certain socket address. */
  public String getDeviceDescriptionURL(String serverAddress)
  {
    return "http://" + serverAddress + getRelativeDeviceDescriptionURL();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.AbstractDevice#getDeviceDescriptionURL(java.net.InetSocketAddress)
   */
  public String getDeviceDescriptionURL(InetSocketAddress serverAddress)
  {
    return "http://" + IPHelper.toString(serverAddress) + getRelativeDeviceDescriptionURL();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.AbstractDevice#getDeviceDescriptionURL()
   */
  public URL getDeviceDescriptionURL()
  {
    try
    {
      return new URL(getDeviceDescriptionURL(getDeviceDescriptionSocketAddress()));
    } catch (Exception e)
    {
    }
    return null;
  }

  /**
   * Searches a service by its relative service URL (/ShortServiceType/ShortServiceID/)
   * 
   * @param relativeURL
   *          The relative URL to check
   * @param urlSuffix
   *          An optional URL suffix to check, e.g., /Control
   * 
   * @return The service that can process the request or null
   * 
   */
  public DeviceService getServiceByRelativeURL(String relativeURL, String urlSuffix)
  {
    return getServiceByRelativeURL(relativeURL, this, urlSuffix);
  }

  /**
   * Searches a service in a specific device by its relative service URL (/ShortServiceType/ShortServiceID/)
   * 
   * @param relativeURL
   *          The relative URL to check
   * @param device
   *          The device and its embedded devices to check
   * @param urlSuffix
   *          An optional URL suffix to check, e.g., /Control
   * 
   * @return The service that can process the request or null
   * 
   */
  private DeviceService getServiceByRelativeURL(String relativeURL, Device device, String urlSuffix)
  {
    DeviceService[] serviceList = device.getDeviceServiceTable();
    if (serviceList != null)
    {
      for (int i = 0; i < serviceList.length; i++)
      {
        if (relativeURL.startsWith(getRelativeServiceURL(serviceList[i], urlSuffix)))
        {
          logger.info("found target service = " + serviceList[i].getServiceId());
          return serviceList[i];
        }
      }
    }
    if (getEmbeddedDeviceTable() != null)
    {
      Device[] embeddedDeviceList = device.getEmbeddedDeviceTable();
      for (int i = 0; i < embeddedDeviceList.length; i++)
      {
        DeviceService service = getServiceByRelativeURL(relativeURL, embeddedDeviceList[i], urlSuffix);
        if (service != null)
        {
          return service;
        }
      }
    }
    return null;
  }

  /**
   * Retrieves the relative URL for a device service.
   * 
   * @param service
   *          The requested service
   * @param suffix
   *          The suffix for the URL, e.g., "/control/"
   * 
   */
  public String getRelativeServiceURL(DeviceService service, String suffix)
  {
    String result = null;
    try
    {
      int serviceTypeIndex = service.getServiceType().indexOf("service");
      String shortServiceType = service.getServiceType().substring(serviceTypeIndex + "service:".length());
      shortServiceType = shortServiceType.replaceAll(":", "_");
      shortServiceType = shortServiceType.replaceAll(" ", "_");

      int serviceIDIndex = service.getServiceId().indexOf("serviceId");
      String shortServiceID = service.getServiceId().substring(serviceIDIndex + "serviceId:".length());
      shortServiceID = shortServiceID.replaceAll(":", "_");
      shortServiceID = shortServiceID.replaceAll(" ", "_");
      result = "/" + shortServiceType + "/" + shortServiceID + suffix;
    } catch (Exception e)
    {
      logger.fatal("cannot create service URL.");
      logger.fatal("reason: " + e.getMessage());
    }
    return result;
  }

  /**
   * Returns relative device description's URL
   * 
   * @return relative device description's URL
   */
  public String getRelativeDeviceDescriptionURL()
  {
    return "/description.xml";
  }

  /**
   * Returns Network Location Signature
   * 
   * @return Network Location Signature
   */
  public String getNLS()
  {
    return NLS;
  }

  /**
   * Sets Network Location Signature
   * 
   * @param NLS
   *          Network Location Signature
   */
  public void setNLS(String NLS)
  {
    this.NLS = NLS;
  }

  /**
   * Creates a device description in XML format.
   * 
   * @param serverAddress
   *          InetAddress of the calling server
   * 
   * @return the device description
   * 
   * @throws DescriptionNotCreatedException
   *           if creating the device description is not possible
   */
  public String toXMLDescription(String serverAddress) throws DescriptionNotCreatedException
  {
    // check if an device description is already known
    if (deviceDescriptionFromHostAddressTable.containsKey(serverAddress))
    {
      return (String)deviceDescriptionFromHostAddressTable.get(serverAddress);
    }

    StringBuffer deviceDescription = new StringBuffer();

    deviceDescription.append(XMLConstant.XML_VERSION + CommonConstants.NEW_LINE);

    deviceDescription.append(XMLHelper.createStartTag(XMLConstant.ROOT_TAG + " " + XMLConstant.XMLNS_DEVICE) +
      CommonConstants.NEW_LINE);
    deviceDescription.append(XMLHelper.createStartTag(XMLConstant.SPECVERSION_TAG) + CommonConstants.NEW_LINE);
    deviceDescription.append(XMLHelper.createTag(XMLConstant.MAJOR_TAG, "1"));
    deviceDescription.append(XMLHelper.createTag(XMLConstant.MINOR_TAG, "0"));
    deviceDescription.append(XMLHelper.createEndTag(XMLConstant.SPECVERSION_TAG) + CommonConstants.NEW_LINE);

    // call building for deviceinfo
    deviceDescription.append(toDeviceXMLDescription(serverAddress, this));
    deviceDescription.append(XMLHelper.createEndTag(XMLConstant.ROOT_TAG));

    // add to hashtable
    deviceDescriptionFromHostAddressTable.put(serverAddress, deviceDescription.toString());

    return deviceDescription.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.AbstractDevice#getServiceDescriptionHashBase64(java.lang.String)
   */
  public String getServiceDescriptionHashBase64(DeviceService service)
  {
    // return existing value
    if (serviceDescriptionHashFromServiceTypeTable.containsKey(service.getServiceType()))
    {
      return (String)serviceDescriptionHashFromServiceTypeTable.get(service.getServiceType());
    }

    // hash is not yet known, calculate
    try
    {
      String serviceDescription = service.toXMLDescription();
      String hashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(serviceDescription));

      serviceDescriptionHashFromServiceTypeTable.put(service.getServiceType(), hashBase64);

      return hashBase64;
    } catch (DescriptionNotCreatedException e)
    {
    }
    return null;
  }

  /**
   * Creates a string containing all service descriptions and adds a hash for each service description to an internal
   * hashtable.
   */
  private void addServiceDescriptions(StringBuffer buffer) throws DescriptionNotCreatedException
  {
    DeviceService[] services = getDeviceServiceTable();

    for (int i = 0; services != null && i < services.length; i++)
    {
      String serviceDescription = services[i].toXMLDescription();
      String hashBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(serviceDescription));

      serviceDescriptionHashFromServiceTypeTable.put(services[i].getServiceType(), hashBase64);

      buffer.append(serviceDescription);
    }
    // add service descriptions from embedded devices
    Device[] embeddedDevices = getEmbeddedDeviceTable();
    for (int i = 0; embeddedDevices != null && i < embeddedDevices.length; i++)
    {
      embeddedDevices[i].addServiceDescriptions(buffer);
    }
  }

  /** Builds the XML description for the root device or an embedded device */
  private StringBuffer toDeviceXMLDescription(String serverAddress, Device device)
  {
    StringBuffer deviceDescription = new StringBuffer();

    deviceDescription.append(XMLHelper.createStartTag(XMLConstant.DEVICE_TAG) + CommonConstants.NEW_LINE);
    deviceDescription.append(XMLHelper.createTag(XMLConstant.DEVICETYPE_TAG, device.getDeviceType()));
    deviceDescription.append(XMLHelper.createTag(XMLConstant.FRIENDLYNAME_TAG, device.getFriendlyName()));
    deviceDescription.append(XMLHelper.createTag(XMLConstant.MANUFACTURER_TAG, device.getManufacturer()));

    if (device.getManufacturerURL() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.MANUFACTURER_URL_TAG, device.getManufacturerURL()
        .toString()));
    }
    if (device.getModelDescription() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.MODEL_DESCRIPTION_TAG, device.getModelDescription()));
    }
    deviceDescription.append(XMLHelper.createTag(XMLConstant.MODEL_NAME_TAG, device.getModelName()));
    if (device.getModelNumber() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.MODEL_NUMBER_TAG, device.getModelNumber()));
    }
    if (device.getModelURL() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.MODEL_URL_TAG, device.getModelURL().toString()));
    }
    if (device.getSerialNumber() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.SERIALNUMBER_TAG, device.getSerialNumber()));
    }
    deviceDescription.append(XMLHelper.createTag(XMLConstant.UDN_TAG, device.getUDN()));
    if (device.getUPC() != null)
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.UPC_TAG, device.getUPC()));
    }

    // icons
    if (device.getIconTable() != null)
    {
      deviceDescription.append(XMLHelper.createStartTag(XMLConstant.ICONLIST_TAG) + CommonConstants.NEW_LINE);
      deviceDescription.append(toIconXMLDescription(serverAddress, device.getIconTable()));
      deviceDescription.append(XMLHelper.createEndTag(XMLConstant.ICONLIST_TAG) + CommonConstants.NEW_LINE);
    }

    // optional cacheable tag
    if (!device.isCacheable())
    {
      deviceDescription.append(XMLHelper.createTag(XMLConstant.CACHEABLE_TAG, "false"));
    }

    // services
    if (device.getAbstractServiceTable() != null)
    {
      deviceDescription.append(XMLHelper.createStartTag(XMLConstant.SERVICELIST_TAG) + CommonConstants.NEW_LINE);
      deviceDescription.append(toServiceListXMLDescription(serverAddress, device.getDeviceServiceTable()));
      deviceDescription.append(XMLHelper.createEndTag(XMLConstant.SERVICELIST_TAG) + CommonConstants.NEW_LINE);
    }

    // embedded devices
    if (device.getEmbeddedDeviceTable() != null)
    {
      Device[] embDev = device.getEmbeddedDeviceTable();
      deviceDescription.append(XMLHelper.createStartTag(XMLConstant.DEVICELIST_TAG) + CommonConstants.NEW_LINE);

      for (int i = 0; i < embDev.length; i++)
      {
        deviceDescription.append(toDeviceXMLDescription(serverAddress, embDev[i]));
      }

      deviceDescription.append(XMLHelper.createEndTag(XMLConstant.DEVICELIST_TAG) + CommonConstants.NEW_LINE);
    }
    if (device.getPresentationURL() != null)
    {
      String url = device.getPresentationURL();

      // extend local URLs
      if (!UPnPDefaults.DEVICE_USE_RELATIVE_URLS && !url.startsWith("http"))
      {
        url = "http://" + serverAddress + (url.startsWith("/") ? "" : "/") + url;
      }
      deviceDescription.append(XMLHelper.createTag(XMLConstant.PRESENTATIONURL_TAG, url));
    }

    deviceDescription.append(XMLHelper.createEndTag(XMLConstant.DEVICE_TAG) + CommonConstants.NEW_LINE);

    return deviceDescription;
  }

  /** Builds the XML description for services */
  private StringBuffer toServiceListXMLDescription(String serverAddress, DeviceService[] services)
  {
    StringBuffer description = new StringBuffer();

    for (int i = 0; i < services.length; i++)
    {
      description.append(XMLHelper.createStartTag(XMLConstant.SERVICE_TAG) + CommonConstants.NEW_LINE);

      description.append(XMLHelper.createTag(XMLConstant.SERVICETYPE_TAG, services[i].getServiceType()));
      description.append(XMLHelper.createTag(XMLConstant.SERVICEID_TAG, services[i].getServiceId()));
      if (UPnPDefaults.DEVICE_USE_RELATIVE_URLS)
      {
        description.append(XMLHelper.createTag(XMLConstant.SCPDURL_TAG, services[i].getRelativeSCPDURL()));
        description.append(XMLHelper.createTag(XMLConstant.CONTROLURL_TAG, services[i].getRelativeControlURL()));
        description.append(XMLHelper.createTag(XMLConstant.EVENTSUBURL_TAG,
          services[i].getRelativeEventSubscriptionURL()));
      } else
      {
        description.append(XMLHelper.createTag(XMLConstant.SCPDURL_TAG, services[i].getSCPDURL(serverAddress)));
        description.append(XMLHelper.createTag(XMLConstant.CONTROLURL_TAG, services[i].getControlURL(serverAddress)));
        description.append(XMLHelper.createTag(XMLConstant.EVENTSUBURL_TAG,
          services[i].getEventSubscriptionURL(serverAddress)));
      }
      // add multicast event address if possible
      if (services[i].useMulticastEvents())
      {
        description.append(XMLHelper.createTag(XMLConstant.MULTICAST_EVENT_ADDRESS_TAG,
          IPHelper.toString(services[i].getDevice().getMulticastEventSocketAddress())));
      }
      // add service description hash if requested
      if (UPnPDefaults.DEVICE_ADD_SERVICE_DESCRIPTION_HASH)
      {
        String serviceHash = getServiceDescriptionHashBase64(services[i]);

        if (serviceHash != null)
        {
          description.append(XMLHelper.createTag(HTTPConstant.X_ETAG, serviceHash));
        }
      }
      description.append(XMLHelper.createEndTag(XMLConstant.SERVICE_TAG) + CommonConstants.NEW_LINE);
    }

    return description;
  }

  /** Builds the XML description for icons */
  private StringBuffer toIconXMLDescription(String serverAddress, DeviceIcon[] deviceIconList)
  {
    StringBuffer iconDescription = new StringBuffer();

    try
    {
      for (int i = 0; i < deviceIconList.length; i++)
      {
        iconDescription.append(XMLHelper.createStartTag(XMLConstant.ICON_TAG) + CommonConstants.NEW_LINE);
        iconDescription.append(XMLHelper.createTag(XMLConstant.MIMETYPE_TAG, deviceIconList[i].getMimetype()));
        iconDescription.append(XMLHelper.createTag(XMLConstant.WIDTH_TAG, deviceIconList[i].getWidth() + ""));
        iconDescription.append(XMLHelper.createTag(XMLConstant.HEIGHT_TAG, deviceIconList[i].getHeight() + ""));
        iconDescription.append(XMLHelper.createTag(XMLConstant.DEPTH_TAG, deviceIconList[i].getDepth() + ""));

        // extend local URLs
        String iconURL = deviceIconList[i].getURL();
        if (!UPnPDefaults.DEVICE_USE_RELATIVE_URLS && !iconURL.startsWith("http"))
        {
          iconURL = "http://" + serverAddress + (iconURL.startsWith("/") ? "" : "/") + iconURL;
        }
        if (UPnPDefaults.DEVICE_USE_RELATIVE_URLS && !iconURL.startsWith("/"))
        {
          iconURL = "/" + iconURL;
        }
        iconDescription.append(XMLHelper.createTag(XMLConstant.URL_TAG, iconURL));
        iconDescription.append(XMLHelper.createEndTag(XMLConstant.ICON_TAG) + CommonConstants.NEW_LINE);
      }
    } catch (Exception ex)
    {
    }

    return iconDescription;
  }

  public int getIPVersion()
  {
    return IPVersion;
  }

  /**
   * Retrieves the working directory for device specific files.
   * 
   * @return The working directory, ending with a separator
   */
  public String getWorkingDirectory()
  {
    return workingDirectory;
  }

  /** Sets the working directory for the device */
  public void setWorkingDirectory(String directory)
  {
    workingDirectory = FileHelper.tryFindWorkingDirectory(this.getClass().getName(), directory);
  }

  /**
   * Retrieves the directories for files that can be accessed via HTTP GET
   * 
   * @return The webServerResourcePath.
   */
  public Vector getWebServerDirectoryList()
  {
    return webServerDirectoryList;
  }

  /**
   * Adds a new web server directory.
   * 
   * @param directory
   *          An absolute or relative directory
   */
  public void addWebServerDirectory(String directory)
  {
    String webServerDirectory = FileHelper.toValidDirectoryName(directory);

    // check if directory exists
    if (!new File(webServerDirectory).exists())
    {
      webServerDirectory = FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + webServerDirectory);
    }
    if (new File(webServerDirectory).exists())
    {
      TemplateDevice.printMessage("Change web server directory " + webServerDirectory +
        " to be relative to resource directory");

      if (!webServerDirectoryList.contains(webServerDirectory))
      {
        webServerDirectoryList.add(webServerDirectory);
      }
      return;
    }
    TemplateDevice.printMessage("Ignore non-existing web server directory: " + webServerDirectory);
  }

  /**
   * Retrieves the directories for class loader resources that can be accessed via HTTP GET
   * 
   * @return The classloaderResourcePath.
   */
  public Vector getClassloaderResourceDirectoryList()
  {
    return classLoaderResourceDirectoryList;
  }

  /**
   * Adds a new classloader directory.
   * 
   * @param directory
   *          An absolute directory
   */
  public void addClassloaderResourceDirectory(String directory)
  {
    String classloaderPath = ResourceHelper.getDefaultResourceDirectoryName() + URLHelper.toValidURLPath(directory);
    if (!classLoaderResourceDirectoryList.contains(classloaderPath))
    {
      TemplateDevice.printMessage("Add classloader resource directory: " + classloaderPath);
      classLoaderResourceDirectoryList.add(classloaderPath);
    }
  }

  /** Retrieves the message httpMessageProcessor associated with this device */
  public DeviceHTTPMessageProcessor getHTTPMessageProcessor()
  {
    return httpMessageProcessor;
  }

  /** Retrieves the number of external host address socket structures */
  public int getSocketStructureCount()
  {
    return socketStructureFromHostAddressTable.size();
  }

  /** Retrieves a vector of all host address socket structure */
  public Vector getSocketStructures()
  {
    Vector result = new Vector();
    Enumeration elements = socketStructureFromHostAddressTable.elements();
    while (elements.hasMoreElements())
    {
      result.add(elements.nextElement());
    }

    return result;
  }

  /**
   * Returns the address and the port where the device description for this device can be found.
   * 
   * @return device description's socket address
   */
  public InetSocketAddress getDeviceDescriptionSocketAddress()
  {
    try
    {
      Object result = socketStructureFromHostAddressTable.get(IPHelper.getLocalHostAddress());
      if (result != null)
      {
        return ((DeviceHostAddressSocketStructure)result).getHTTPServerAddress();
      }

    } catch (Exception e)
    {
    }
    return null;
  }

  /** Retrieves a specific host address socket structure */
  public DeviceHostAddressSocketStructure getSocketStructure(InetAddress hostAddress)
  {
    Object result = socketStructureFromHostAddressTable.get(hostAddress);
    if (result != null)
    {
      return (DeviceHostAddressSocketStructure)result;
    }

    return null;
  }

  /** Closes all socket structures */
  public void closeSocketStructures()
  {
    Vector socketStructures = getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      DeviceHostAddressSocketStructure socketStructure =
        (DeviceHostAddressSocketStructure)socketStructures.elementAt(i);

      socketStructure.terminate();
    }
  }

  /**
   * Initializes sockets and servers for the device
   */
  private void initHostAddressSocketStructures()
  {
    System.out.println("    " + deviceStartupConfiguration.getFriendlyName() + ": Use multicast socket address: " +
      IPHelper.toString(startupConfiguration.getSSDPMulticastSocketAddress()));

    // start independent sockets and servers for all external network addresses
    Vector networkInterfaces = IPHelper.getSocketStructureNetworkInterfaces();
    // System.out.println("Found " + networkInterfaces.size() + " network interface(s)");
    for (int i = 0; i < networkInterfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
      Vector localHostAddresses = IPHelper.getIPv4InetAddresses(currentInterface);

      // System.out.println("Found " + localHostAddresses.size() + " IP address(es) for interface "
      // +
      // currentInterface.getName() + "(" + currentInterface.getDisplayName() + ")");

      for (int j = 0; j < localHostAddresses.size(); j++)
      {
        InetAddress currentAddress = (InetAddress)localHostAddresses.elementAt(j);
        tryAddHostAddressSocketStructure(currentInterface, currentAddress);
      }
    }
  }

  /**
   * Tries to add a new network interface to this device.
   * 
   * @param networkInterface
   *          The associated network interface
   * @param hostAddress
   *          The new host address
   * 
   * @return The created socket structure or null
   */
  protected DeviceHostAddressSocketStructure tryAddHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress hostAddress)
  {
    if (hostAddress instanceof Inet6Address)
    {
      System.out.println(toString() + ": Host address " + hostAddress.getHostName() + " is IPv6");
      return null;
    }
    if (socketStructureFromHostAddressTable.containsKey(hostAddress))
    {
      System.out.println(friendlyName + ": Host address " + hostAddress.getHostName() + " is already in use");
      return null;
    }
    // ignore loopback address
    if (hostAddress.getHostAddress().equals("127.0.0.1"))
    {
      // System.out.println(toString() + ": Host address is loopback address");
      return null;
    }
    // get list of ignored IP addresses
    Vector ignoredIPAddresses = startupConfiguration.getIgnoredIPAddressesList();
    if (ignoredIPAddresses.contains(hostAddress.getHostAddress()))
    {
      System.out.println(toString() + ": Host address " + hostAddress.getHostName() + " is in ignore list");
      return null;
    }

    TemplateDevice.printMessage(friendlyName + ": Create DeviceHostAddressSocketStructure for local host address " +
      hostAddress.getHostAddress());

    // create the socket structure
    DeviceHostAddressSocketStructure hostAddressSocketStructure =
      createDeviceHostAddressSocketStructure(networkInterface, hostAddress);

    // add to hashtable if valid
    if (hostAddressSocketStructure != null)
    {
      socketStructureFromHostAddressTable.put(hostAddress, hostAddressSocketStructure);
    }

    return hostAddressSocketStructure;
  }

  /** Creates a new host address socket structure for a certain host address. */
  protected DeviceHostAddressSocketStructure createDeviceHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress hostAddress)
  {
    DeviceHostAddressSocketStructure hostAddressSocketStructure = null;
    // choose whether to use fixed ports
    if (useFixedPorts)
    {
      hostAddressSocketStructure =
        new DeviceHostAddressSocketStructure(this,
          networkInterface,
          ssdpUnicastPort,
          httpServerPort,
          multicastEventAddress,
          hostAddress);
    }
    // if fixed ports are not possible, use random ports
    if (hostAddressSocketStructure == null || !hostAddressSocketStructure.isValid())
    {
      // check if fixed ports has been requested
      boolean useBackupSockets = hostAddressSocketStructure != null;
      if (useBackupSockets)
      {
        hostAddressSocketStructure.terminate();
        TemplateDevice.printMessage(friendlyName + ": Fixed ports (" + httpServerPort + "," + ssdpUnicastPort +
          ") already in use, try to use random ports");
      }
      hostAddressSocketStructure = new DeviceHostAddressSocketStructure(this, networkInterface, hostAddress);
      // change UDN base if backup sockets are used
      if (useBackupSockets && hostAddressSocketStructure.isValid())
      {
        udnBase += hostAddressSocketStructure.getHTTPServerAddress().getPort();
      }
    }
    if (hostAddressSocketStructure.isValid())
    {
      return hostAddressSocketStructure;
    }

    return null;
  }

  /** Removes a network interface from this device */
  protected void removeHostAddressSocketStructure(InetAddress hostAddress)
  {
    Object structure = socketStructureFromHostAddressTable.remove(hostAddress);
    if (structure != null)
    {
      DeviceHostAddressSocketStructure hostAddressSocketStructure = (DeviceHostAddressSocketStructure)structure;

      hostAddressSocketStructure.terminate();
    }
  }

  /**
   * Returns new subscriber UUID
   * 
   * @return new subscriber UUID
   */
  public String getNewSubscriptionUUID()
  {
    String subscriptionUUID = getUDN() + ":" + subscriptionCounter;
    subscriptionCounter++;

    return "uuid:" + UUIDHelper.getUUIDFromName(subscriptionUUID);
  }

  /**
   * Retrieves the ssdpMulticastAddress.
   * 
   * @return The ssdpMulticastAddress.
   */
  public InetSocketAddress getSSDPMulticastSocketAddress()
  {
    return ssdpMulticastSocketAddress;
  }

  /**
   * Retrieves the ssdpMulticastSocketAddress.
   * 
   * @return The ssdpMulticastSocketAddress.
   */
  public String getSSDPMulticastSocketAddressString()
  {
    return IPHelper.toString(ssdpMulticastSocketAddress);
  }

  /**
   * Sets the ssdpMulticastSocketAddress.
   * 
   * @param ssdpMulticastSocketAddress
   *          The ssdpMulticastSocketAddress to set.
   */
  public void setSSDPMulticastSocketAddress(InetSocketAddress ssdpMulticastSocketAddress)
  {
    this.ssdpMulticastSocketAddress = ssdpMulticastSocketAddress;
  }

  /**
   * Retrieves the webServerListener.
   * 
   * @return The webServerListener
   */
  public IDeviceWebServerListener getWebServerListener()
  {
    return webServerListener;
  }

  /**
   * Sets the webServerListener.
   * 
   * @param webServerListener
   *          The new value for webServerListener
   */
  public void setWebServerListener(IDeviceWebServerListener webServerListener)
  {
    this.webServerListener = webServerListener;
  }

  /**
   * Retrieves the useMulticastEvents flag.
   * 
   * @return The useMulticastEvents flag
   */
  public boolean useMulticastEvents()
  {
    return useMulticastEvents;
  }

  /**
   * Retrieves the multicastEventAddress.
   * 
   * @return The multicastEventAddress
   */
  public InetSocketAddress getMulticastEventSocketAddress()
  {
    return multicastEventAddress;
  }

  /**
   * Sets the useMulticastEvents.
   * 
   * @param useMulticastEvents
   *          The new value for useMulticastEvents
   */
  public void setUseMulticastEvents(boolean useMulticastEvents)
  {
    this.useMulticastEvents = useMulticastEvents;
  }

  /**
   * Increments and retrieves the multicastEventThreadCount.
   * 
   * @return The old multicastEventThreadCount
   */
  public synchronized int incMulticastEventThreadCount()
  {
    multicastEventThreadCount++;
    return multicastEventThreadCount - 1;
  }

  public void loadPersonalizationKeyPair()
  {
    String path = FileHelper.getHostBasedFileName(startupConfiguration.getWorkingDirectory() + "deviceKeys");
    // update personalization key
    personalizationKeyPair = PersistentRSAKeyPair.getPersistentKeyPair(path);
  }

  /** Returns the public key of this control point */
  public RSAPublicKey getDevicePublicKey()
  {
    if (personalizationKeyPair == null)
    {
      return null;
    }

    return (RSAPublicKey)personalizationKeyPair.getPublic();
  }

  /** Returns the private key of this control point */
  public RSAPrivateKey getDevicePrivateKey()
  {
    if (personalizationKeyPair == null)
    {
      return null;
    }

    return (RSAPrivateKey)personalizationKeyPair.getPrivate();
  }

  /**
   * Retrieves the deviceEventThread.
   * 
   * @return The deviceEventThread
   */
  public EventThread getDeviceEventThread()
  {
    return deviceEventThread;
  }

  /** Terminates the device */
  public void terminate()
  {
    deviceEventThread.terminate();
    if (multicastEventHandler != null)
    {
      multicastEventHandler.terminate();
    }
    closeSocketStructures();
  }

}

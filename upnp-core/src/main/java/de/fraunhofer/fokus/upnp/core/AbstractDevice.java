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
package de.fraunhofer.fokus.upnp.core;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This is the abstract base class for Device and CPDevice.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public abstract class AbstractDevice
{

  protected int               IPVersion;

  protected int               maxage;

  protected String            server;

  protected String            deviceType;                                                  // required

  protected String            friendlyName;                                                // required

  protected String            manufacturer;                                                // required

  protected URL               manufacturerURL;                                             // optional

  protected String            modelDescription;                                            // recommended

  protected String            modelName;                                                   // required

  protected String            modelNumber;                                                 // recommended

  protected URL               modelURL;                                                    // optional

  protected String            serialNumber;                                                // recommended

  protected String            UDN;                                                         // required

  protected String            UPC;                                                         // optional

  protected DeviceIcon[]      iconTable;                                                   // recommended

  /** Embedded services */
  protected AbstractService[] serviceTable;                                                // required

  /** Embedded devices */
  protected AbstractDevice[]  deviceTable;                                                 // required

  protected String            presentationURL;                                             // recommended

  protected boolean           rootDevice;

  protected String            NLS;

  /** Used multicast address (usually 239.255.255.250:1900) */
  protected InetSocketAddress ssdpMulticastSocketAddress;

  /** Device hash over the device and service descriptions */
  protected String            descriptionHashBase64                      = null;

  /** Hash table containing a content hash for all known service descriptions */
  protected Hashtable         serviceDescriptionHashFromServiceTypeTable = new Hashtable();

  /** Constructor for derived devices. */
  public AbstractDevice()
  {
  }

  /**
   * Constructor for derived CPDevices.
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
   * @param rootDevice
   *          flag for root device
   * @param IPVersion
   *          ip stack version
   */
  public AbstractDevice(String deviceType,
    String friendlyName,
    String manufacturer,
    String modelName,
    String UDN,
    boolean rootDevice,
    int IPVersion)
  {
    this.deviceType = deviceType;
    this.friendlyName = friendlyName;
    this.manufacturer = manufacturer;
    this.modelName = modelName;
    this.UDN = UDN;
    this.rootDevice = rootDevice;

    maxage = UPnPDefaults.DEVICE_MAX_AGE;
    server = UPnPConstant.SERVER;
    NLS = UPnPConstant.NLS_Value;
    this.IPVersion = IPVersion;
    this.ssdpMulticastSocketAddress = SSDPConstant.getSSDPMulticastSocketAddress();
  }

  /** Returns the device description URL for a certain network interface. */
  public abstract String getDeviceDescriptionURL(InetSocketAddress serverAddress);

  /** Returns the preferred socket address for this device. */
  public abstract InetSocketAddress getDeviceDescriptionSocketAddress();

  /** Retrieves the simple class name for this device. */
  public String getDeviceClassName()
  {
    String className = getClass().getName();
    int index = className.lastIndexOf(".");
    if (index != -1)
    {
      return className.substring(index + 1);
    }

    return className;
  }

  /** Checks if this is a CPDevice. */
  public boolean isCPDevice()
  {
    return false;
  }

  /** Checks if this is an internal device. */
  public boolean isInternalDevice()
  {
    return false;
  }

  /** Checks if the device description can be cached. */
  public boolean isCacheable()
  {
    return true;
  }

  /**
   * Returns device type
   * 
   * @return device type
   */
  public String getDeviceType()
  {
    return deviceType;
  }

  /**
   * Returns device type version
   * 
   * @return device type version
   */
  public int getDeviceTypeVersion()
  {
    try
    {
      int result = Integer.parseInt(deviceType.substring(deviceType.lastIndexOf(":") + 1));

      return result;
    } catch (Exception ex)
    {
    }
    return 1;
  }

  /**
   * Returns device friendly name
   * 
   * @return device friendly name
   */
  public String getFriendlyName()
  {
    return friendlyName;
  }

  /**
   * Returns manufacturer name
   * 
   * @return manufacture name
   */
  public String getManufacturer()
  {
    return manufacturer;
  }

  /**
   * Returns manufacturer's URL
   * 
   * @return the URL of the manufacture web page
   */
  public URL getManufacturerURL()
  {
    return manufacturerURL;
  }

  /**
   * Returns model description
   * 
   * @return model description of the device (long description)
   */
  public String getModelDescription()
  {
    return modelDescription;
  }

  /**
   * Returns model name
   * 
   * @return model name
   */
  public String getModelName()
  {
    return modelName;
  }

  /**
   * Returns model number
   * 
   * @return the model number of the device
   */
  public String getModelNumber()
  {
    return modelNumber;
  }

  /**
   * Returns device model's URL
   * 
   * @return device model'S URL
   */
  public URL getModelURL()
  {
    return modelURL;
  }

  /**
   * Returns device serial number
   * 
   * @return device serial number
   */
  public String getSerialNumber()
  {
    return serialNumber;
  }

  /**
   * Returns unique device name
   * 
   * @return unique device name
   */
  public String getUDN()
  {
    return UDN;
  }

  /**
   * Returns unique product code
   * 
   * @return universal product code
   */
  public String getUPC()
  {
    return UPC;
  }

  /**
   * Returns device icons
   * 
   * @return device icons
   */
  public DeviceIcon[] getIconTable()
  {
    return iconTable;
  }

  /**
   * Returns a list with all services belonging to this device.
   * 
   * @return device services
   */
  public AbstractService[] getAbstractServiceTable()
  {
    return serviceTable;
  }

  /**
   * Returns embedded devices
   * 
   * @return embedded devices
   */
  public AbstractDevice[] getAbstractDeviceTable()
  {
    return deviceTable;
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
   * Return root device's flag
   * 
   * @return root device's flag
   */
  public boolean getRootDevice()
  {
    return rootDevice;
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
   * Returns server info (OS / product version)
   * 
   * @return server info
   */
  public String getServer()
  {
    return server;
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

  public int getIPVersion()
  {
    return IPVersion;
  }

  /**
   * Returns friendly name
   * 
   * @return friendly name
   */
  public String toString()
  {
    return friendlyName;
  }

  /**
   * Returns friendly name plus discovery address
   * 
   * @return A string like <Device name> at <device address>
   */
  public String toDiscoveryString()
  {
    return friendlyName + " at " + IPHelper.toString(getDeviceDescriptionSocketAddress());
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
   * Retrieves the descriptionHashBase64.
   * 
   * @return The descriptionHashBase64
   */
  public String getDescriptionHashBase64()
  {
    return descriptionHashBase64;
  }

  /**
   * Retrieves the descriptionHashBase64.
   * 
   * @return The descriptionHashBase64
   */
  public String getServiceDescriptionHashBase64(String serviceType)
  {
    return (String)serviceDescriptionHashFromServiceTypeTable.get(serviceType);
  }

  /** Terminates the device. */
  public void terminate()
  {
  }

}

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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateEntity;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;

/**
 * This class encapsulates the device and the control point for handling interconnected UPnP
 * networks.
 * 
 * @author Alexander Koenig
 */
public class DeviceDirectoryEntity extends SecurityAwareTemplateEntity
{
  /** Reference to outer InternetManagement */
  private InternetManagement internetManagement;

  /** List of remote device directory devices */
  private Vector             deviceDirectoryDeviceList = new Vector();

  /**
   * Creates a new instance of DeviceDirectoryEntity.
   * 
   * @param internetManagement
   *          Reference to outer management
   * @param startupConfiguration
   *          Startup configuration for the entity
   */
  public DeviceDirectoryEntity(InternetManagement internetManagement, UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    this.internetManagement = internetManagement;
    // add event listener for new devices
    internetManagement.getMessageForwarder().addCPDeviceEventListener(this);

    // add a secured device to this entity
    setTemplateDevice(new DeviceDirectoryDevice(this, startupConfiguration));

    // start autoconnecting peers
    getDeviceDirectoryDevice().autoConnectDevices();
  }

  /** Event that a new deviceDirectoryDevice has been discovered */
  public void newDeviceDirectoryDevice(DeviceDirectoryCPDevice device)
  {
    getDeviceDirectoryDevice().newDeviceDirectoryDevice(device);
  }

  /** Event that a deviceDirectoryDevice has been removed from the network */
  public void deviceDirectoryDeviceGone(CPDevice device)
  {
    getDeviceDirectoryDevice().deviceDirectoryDeviceGone(device);
  }

  /** Retrieves the associated DeviceDirectoryDevice */
  public DeviceDirectoryDevice getDeviceDirectoryDevice()
  {
    return (DeviceDirectoryDevice)getTemplateDevice();
  }

  /**
   * Retrieves the deviceDirectoryDeviceList.
   * 
   * @return The deviceDirectoryDeviceList
   */
  public Vector getDeviceDirectoryDeviceList()
  {
    return deviceDirectoryDeviceList;
  }

  /** Retrieves the associated internetManagement */
  public InternetManagement getInternetManagement()
  {
    return internetManagement;
  }

  /**
   * Retrieves the control point of the message forwarder.
   * 
   * @return The control point of the message forwarder
   */
  public TemplateControlPoint getMessageForwarderControlPoint()
  {
    return getInternetManagement().getMessageForwarderControlPoint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    super.newDevice(newDevice);

    // System.out.println("DeviceDirectoryEntity: Found new device: " + newDevice.getFriendlyName()
    // + " ["
    // + newDevice.getUDN() + "]");

    if (newDevice.getDeviceType().equals(InternetManagementConstants.DEVICE_DIRECTORY_DEVICE_TYPE))
    {
      if (!isKnownDeviceDirectoryDevice(newDevice.getUDN()))
      {
        DeviceDirectoryCPDevice device = new DeviceDirectoryCPDevice(getTemplateControlPoint(), newDevice);
        deviceDirectoryDeviceList.add(device);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    System.out.println("Device gone: " + goneDevice.getFriendlyName() + " [" + goneDevice.getUDN() + "]");

    // remove from local list
    if (goneDevice.getDeviceType().startsWith(InternetManagementConstants.DEVICE_DIRECTORY_DEVICE_TYPE))
    {
      int index = getDeviceDirectoryDeviceIndex(goneDevice.getUDN());
      if (index != -1)
      {
        deviceDirectoryDeviceList.remove(index);
      }
    }

    super.deviceGone(goneDevice);
  }

  /**
   * Returns the index for a DeviceDirectory device
   */
  protected int getDeviceDirectoryDeviceIndex(String udn)
  {
    for (int i = 0; i < deviceDirectoryDeviceList.size(); i++)
    {
      if (((DeviceDirectoryCPDevice)deviceDirectoryDeviceList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  protected boolean isKnownDeviceDirectoryDevice(String udn)
  {
    for (int i = 0; i < deviceDirectoryDeviceList.size(); i++)
    {
      if (((DeviceDirectoryCPDevice)deviceDirectoryDeviceList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a device with this IP address is already in the list
   */
  public boolean isKnownDeviceDirectoryDevice(InetAddress ipAddress)
  {
    for (int i = 0; i < deviceDirectoryDeviceList.size(); i++)
    {
      DeviceDirectoryCPDevice currentDevice = (DeviceDirectoryCPDevice)deviceDirectoryDeviceList.elementAt(i);
      try
      {
        InetAddress descriptionAddress =
          InetAddress.getByName(currentDevice.getCPDevice().getDeviceDescriptionURL().getHost());

        if (descriptionAddress.equals(ipAddress))
        {
          return true;
        }
      } catch (Exception ex)
      {
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    // remove event listener for new devices
    internetManagement.getMessageForwarder().removeCPDeviceEventListener(this);

    super.terminate();
  }

}

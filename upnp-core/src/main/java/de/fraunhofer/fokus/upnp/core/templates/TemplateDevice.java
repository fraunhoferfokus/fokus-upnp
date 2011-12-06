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
package de.fraunhofer.fokus.upnp.core.templates;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.device.DeviceMSearchServer;
import de.fraunhofer.fokus.upnp.core.device.DeviceNotifyAdvertiser;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.MemoryMonitor;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.network.NetworkInterfaceManager;

/**
 * This class should be used as base class for all UPnP devices.
 * 
 * @author Alexander Koenig
 */
public class TemplateDevice extends Device implements INetworkInterfaceChangeListener
{
  /** Device advertising by sending NOTIFY messages */
  private DeviceNotifyAdvertiser  deviceNotifyAdvertiser = null;

  /** Device advertising by answering M-SEARCH messages */
  private DeviceMSearchServer     deviceMSearchServer    = null;

  /** Triggers events for network interface changes */
  private NetworkInterfaceManager networkInterfaceManager;

  /** Associated entity */
  private TemplateEntity          entity                 = null;

  /** Memory monitor */
  private MemoryMonitor           memoryMonitor          = null;

  /**
   * Creates a template device with a startup configuration.
   * 
   * @param anEntity
   *          Associated entity
   * @param startupConfiguration
   *          A class containing all startup information
   * 
   */
  public TemplateDevice(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    entity = anEntity;
    // check if device can be started immediately
    if (!forceRunDelayed() && !deviceStartupConfiguration.runDelayed())
    {
      setupDeviceVariables();
      initDeviceContent();
      runDevice();
    }
  }

  /** Prints a message */
  public static void printMessage(String text)
  {
    System.out.println("    " + text);
  }

  /** Proceeds with the creation for delayed devices */
  public void runDelayed()
  {
    setupDeviceVariables();
    initDeviceContent();
    runDevice();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#setupDeviceVariables()
   */
  public void setupDeviceVariables()
  {
    super.setupDeviceVariables();

    networkInterfaceManager = new NetworkInterfaceManager();
    networkInterfaceManager.addListener(this);

    memoryMonitor = new MemoryMonitor();

    deviceEventThread.register(networkInterfaceManager);
    deviceEventThread.register(memoryMonitor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#initDeviceContent()
   */
  public void initDeviceContent()
  {
    super.initDeviceContent();
    // create basic listener for web server requests
    TemplateWebServerListener webServerListener = new TemplateWebServerListener();
    setWebServerListener(webServerListener);
    setPresentationURL("/");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#runDevice()
   */
  public void runDevice()
  {
    super.runDevice();

    // everything is set, start device advertising
    startAdvertisement();
  }

  /** This method can be overwritten by descendant classes to force a delayed start. */
  public boolean forceRunDelayed()
  {
    return false;
  }

  /** Adds a service to this device */
  public void addTemplateService(TemplateService service)
  {
    addDeviceService(service);
  }

  /** Retrieves the number of embedded services */
  public int getTemplateServiceCount()
  {
    if (getAbstractServiceTable() == null)
    {
      return 0;
    }

    return getAbstractServiceTable().length;
  }

  /** Retrieves a specific service */
  public TemplateService getTemplateService(int index)
  {
    if (index >= 0 && index < getTemplateServiceCount())
    {
      return (TemplateService)getAbstractServiceTable()[index];
    }

    return null;
  }

  /** Sets the outer entity */
  public void setTemplateEntity(TemplateEntity entity)
  {
    this.entity = entity;
  }

  /** Retrieves a reference to the outer entity */
  public TemplateEntity getTemplateEntity()
  {
    return entity;
  }

  /**
   * Starts the discovery phase of the device
   */
  private void startAdvertisement()
  {
    // client for sending NOTIFY messages
    deviceNotifyAdvertiser = getInstanceOfDeviceSupportFactory().getInstanceOfDeviceNotifyAdvertiser(this);

    // server for receiving M-SEARCH messages
    deviceMSearchServer =
      new DeviceMSearchServer(this,
        getInstanceOfDeviceMessageProcessorFactory().getInstanceOfDeviceMSearchMessageProcessor(this));
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Network interface management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#newInetAddress(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    DeviceHostAddressSocketStructure socketStructure = tryAddHostAddressSocketStructure(networkInterface, inetAddress);

    if (socketStructure != null)
    {
      // send NOTIFY messages via newly created socket structure
      deviceNotifyAdvertiser.sendNotifyMessagesToSocketStructure(SSDPConstant.SSDP_ALIVE, socketStructure);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#inetAddressGone(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    Vector socketStructures = getSocketStructures();
    // remove associated socket structures
    for (int i = 0; i < socketStructures.size(); i++)
    {
      DeviceHostAddressSocketStructure currentStructure =
        (DeviceHostAddressSocketStructure)socketStructures.elementAt(i);

      if (currentStructure.getNetworkInterface().getName().equals(networkInterface.getName()) &&
        currentStructure.getHostAddress().equals(inetAddress))
      {
        removeHostAddressSocketStructure(currentStructure.getHostAddress());
      }
    }
  }

  /**
   * Retrieves the deviceMSearchServer.
   * 
   * @return The deviceMSearchServer
   */
  public DeviceMSearchServer getDeviceMSearchServer()
  {
    return deviceMSearchServer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.Device#terminate()
   */
  public void terminate()
  {
    TemplateDevice.printMessage("Shutdown device " + toString() + " [" + getUDN() + "]...");

    deviceEventThread.unregister(memoryMonitor);
    deviceEventThread.unregister(networkInterfaceManager);
    // stop services
    for (int i = 0; i < getTemplateServiceCount(); i++)
    {
      ((TemplateService)getAbstractServiceTable()[i]).terminate();
    }

    // stop server for CP search requests
    if (deviceMSearchServer != null)
    {
      deviceMSearchServer.terminate();
    }
    // stop discovery and send NOTIFY byebye
    if (deviceNotifyAdvertiser != null)
    {
      deviceNotifyAdvertiser.terminate();
    }

    super.terminate();
  }
}

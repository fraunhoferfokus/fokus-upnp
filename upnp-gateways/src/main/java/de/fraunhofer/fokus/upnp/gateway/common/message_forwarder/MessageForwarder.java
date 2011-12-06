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
package de.fraunhofer.fokus.upnp.gateway.common.message_forwarder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.Log4JLogRefWrapper;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.GatewayStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.util.URLExtension;
import de.fraunhofer.fokus.upnp.util.logging.LogHelper;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;

/**
 * This class is the central module for message forwarding.
 * 
 * @author Alexander Koenig
 * 
 */
public class MessageForwarder implements ICPDeviceEventListener, INetworkInterfaceChangeListener
{

  /** Vector with all registered forwarders */
  private Vector                      forwarderModules          = new Vector();

  /** Control point for device handling */
  private TemplateControlPoint        templateControlPoint;

  /**
   * UDP Manager to prevent multiple packet forwarding and loops. This manager is triggered regularly by the control
   * point event thread.
   */
  private UDPPacketManager            udpPacketManager;

  /** Manager for SSDP message handling */
  private SSDPManagement              ssdpManagement;

  /** Processor for HTTP messages */
  private GatewayHTTPMessageProcessor gatewayHTTPMessageProcessor;

  /** Manager for HTTP message handling */
  private HTTPManagement              httpManagement;

  /** Reference to common startup configuration */
  private UPnPStartupConfiguration    startupConfiguration;

  /** Optional listener for device events */
  private Vector                      cpDeviceEventListenerList = new Vector();

  /** Starts a new UPnP message forwarder */
  public MessageForwarder(UPnPStartupConfiguration startupConfiguration)
  {
    this.startupConfiguration = startupConfiguration;
    if (startupConfiguration.getGatewayStartupInfoList().size() == 0)
    {
      System.out.println("Missing gateway startup infos. Exit application");
      return;
    }
    templateControlPoint = new TemplateControlPoint(null, startupConfiguration);
    templateControlPoint.setCPDeviceEventListener(this);
    templateControlPoint.setNetworkInterfaceChangeListener(this);

    // set logger for UPnP util classes
    LogHelper.setLogger(new Log4JLogRefWrapper(TemplateControlPoint.logger));

    ssdpManagement = new SSDPManagement(this);
    httpManagement = new HTTPManagement(this);
    udpPacketManager = new UDPPacketManager("MessageForwarder");

    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(ssdpManagement);
    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(httpManagement);
    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(udpPacketManager);

    gatewayHTTPMessageProcessor = new GatewayHTTPMessageProcessor(this);
  }

  /** Adds a module to this forwarder */
  public synchronized void addForwarderModule(IForwarderModule module)
  {
    if (forwarderModules.indexOf(module) == -1)
    {
      forwarderModules.add(module);
      // add to new list to generate initial NOTIFY messages for this module
      ssdpManagement.addNewForwarderModule(module);
    }
  }

  /** Removes a module from this forwarder */
  public synchronized void removeForwarderModule(IForwarderModule module)
  {
    // send NOTIFY byebye messages for all devices that were connected over this module
    ssdpManagement.sendFinalNotifyByeByeMessages(module);
    // remove after byebye message sending
    forwarderModules.remove(module);
  }

  /** Retrieves a module by its ID */
  public IForwarderModule getForwarderModuleByID(String moduleID)
  {
    for (int i = 0; i < forwarderModules.size(); i++)
    {
      IForwarderModule module = (IForwarderModule)forwarderModules.elementAt(i);
      if (module.getModuleID().equals(moduleID))
      {
        return module;
      }
    }
    return null;
  }

  /** Retrieves the module for a specific subnet */
  public IForwarderModule getForwarderModuleBySubnet(InetAddress address)
  {
    for (int i = 0; i < forwarderModules.size(); i++)
    {
      IForwarderModule module = (IForwarderModule)forwarderModules.elementAt(i);
      if (module.isSubnetForwarderModule(address))
      {
        return module;
      }
    }
    // if the subnet is not found, we need to find the local network interface forwarder module
    // that acts as gateway
    // currently, I have no good idea how to do this, so we just use the forwarder module for the
    // local host
    return getForwarderModuleForLocalHost();
  }

  /** Retrieves a module by its HTTP server address */
  public IForwarderModule getForwarderModuleByHTTPServerAddress(InetSocketAddress serverAddress)
  {
    for (int i = 0; i < forwarderModules.size(); i++)
    {
      IForwarderModule module = (IForwarderModule)forwarderModules.elementAt(i);
      if (module.isHTTPServerAddress(serverAddress))
      {
        return module;
      }
    }
    return null;
  }

  /** Retrieves the forwarder module that forwards HTTP requests for a specific device */
  public IForwarderModule getForwarderModuleByDevice(AbstractDevice device)
  {
    // System.out.println("DiscoveryAddress for " + device + " is " +
    // IPAddress.toString(device.getDeviceDescriptionSocketAddress()));
    // try to find module over device description URL
    if (device instanceof CPDevice)
    {
      try
      {
        URL deviceDescriptionURL = ((CPDevice)device).getDeviceDescriptionURL();
        // try to decode the path to find the forwarder module that allows
        // access to this device
        Object[] pathElements = URLExtension.decodeGatewayURLPath(deviceDescriptionURL.getPath());
        if (pathElements != null)
        {
          String outgoingModuleID = (String)pathElements[0];
          for (int i = 0; i < forwarderModules.size(); i++)
          {
            IForwarderModule currentForwarderModule = (IForwarderModule)forwarderModules.elementAt(i);
            if (currentForwarderModule.getModuleID().equals(outgoingModuleID))
            {
              return currentForwarderModule;
            }
          }
        }
      } catch (Exception e)
      {
      }
    }
    // if device is local and was discovered without forwarding,
    // the device description socket address contains
    // address and port of the devices internal HTTP server, so we could not
    // find a forwarder module

    // in this case, try to find a forwarder module that can forward
    // consecutive requests for this device
    return getForwarderModuleBySubnet(device.getDeviceDescriptionSocketAddress().getAddress());
  }

  /** Retrieves the module for the local host address */
  public IForwarderModule getForwarderModuleForLocalHost()
  {
    for (int i = 0; i < forwarderModules.size(); i++)
    {
      IForwarderModule module = (IForwarderModule)forwarderModules.elementAt(i);
      if (module.isLocalHostAddressForwarderModule())
      {
        return module;
      }
    }
    return null;
  }

  /** Retrieves an array with all forwarder modules */
  public synchronized IForwarderModule[] getForwarderModules()
  {
    IForwarderModule[] result = new IForwarderModule[forwarderModules.size()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = (IForwarderModule)forwarderModules.elementAt(i);
    }
    return result;
  }

  /** Retrieves the number of forwarder modules */
  public synchronized int getForwarderModuleCount()
  {
    return forwarderModules.size();
  }

  /**
   * Retrieves all devices for a certain forwarder module.
   * 
   * @param moduleID
   *          The module ID
   * 
   * @return A vector with all devices connected over that forwarder module
   */
  public Vector getDevicesForForwarderModule(String moduleID)
  {
    Vector result = new Vector();
    for (int i = 0; i < templateControlPoint.getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = templateControlPoint.getCPDevice(i);
      IForwarderModule forwarderModule = getForwarderModuleByDevice(currentDevice);
      if (forwarderModule != null && forwarderModule.getModuleID().equals(moduleID))
      {
        result.add(currentDevice);
      }
    }
    System.out.println("Number of devices for " + moduleID + " is " + result.size() + " out of " +
      templateControlPoint.getCPDeviceCount());
    return result;
  }

  /** Retrieves the UDP packet manager */
  public UDPPacketManager getUDPPacketManager()
  {
    return udpPacketManager;
  }

  /**
   * Retrieves the SSDP management.
   * 
   * @return The ssdpManagement.
   */
  public SSDPManagement getSSDPManagement()
  {
    return ssdpManagement;
  }

  /**
   * Retrieves the HTTP management
   * 
   * @return The HTTP management
   */
  public HTTPManagement getHTTPManagement()
  {
    return httpManagement;
  }

  /**
   * Retrieves the GatewayHTTPMessageProcessor.
   * 
   * @return Returns the gatewayHTTPMessageProcessor.
   */
  public GatewayHTTPMessageProcessor getGatewayMessageManager()
  {
    return gatewayHTTPMessageProcessor;
  }

  /**
   * Retrieves the templateControlPoint.
   * 
   * @return Returns the templateControlPoint.
   */
  public TemplateControlPoint getTemplateControlPoint()
  {
    return templateControlPoint;
  }

  /**
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public UPnPStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves a gateway startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public GatewayStartupConfiguration getGatewayStartupConfiguration(String className)
  {
    if (startupConfiguration.getSingleGatewayStartupConfiguration() != null)
    {
      return (GatewayStartupConfiguration)startupConfiguration.getSingleGatewayStartupConfiguration();
    }

    return (GatewayStartupConfiguration)startupConfiguration.getGatewayStartupConfiguration(className);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    // send NOTIFY events for the new device to all registered forwarder modules
    ssdpManagement.sendInitialNotifyMessagesForDevice(newDevice);
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((ICPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).newDevice(newDevice);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((ICPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).deviceGone(goneDevice);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceEvent(de.fhg.fokus.magic.upnp.control_point.CPDevice,
   *      int)
   */
  public void deviceEvent(CPDevice device, int eventCode, Object eventParameter)
  {
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((ICPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).deviceEvent(device, eventCode, eventParameter);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#newInetAddress(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#inetAddressGone(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    // due to thread handling, this event may be called prior to removeForwarderModule
    // so we also generate byebye messages in this method
    // get forwarder module that is soon gone
    IForwarderModule forwarderModule = getForwarderModuleBySubnet(inetAddress);
    // send NOTIFY byebye messages for all devices that were connected over this module
    if (forwarderModule != null)
    {
      ssdpManagement.sendFinalNotifyByeByeMessages(forwarderModule);
    }
  }

  /**
   * Adds a cpDeviceEventListener.
   * 
   * @param cpDeviceEventListener
   *          A new cpDeviceEventListener
   */
  public void addCPDeviceEventListener(ICPDeviceEventListener cpDeviceEventListener)
  {
    if (!cpDeviceEventListenerList.contains(cpDeviceEventListener))
    {
      cpDeviceEventListenerList.add(cpDeviceEventListener);
    }
  }

  /**
   * Removes a cpDeviceEventListener.
   * 
   * @param cpDeviceEventListener
   *          The removed cpDeviceEventListener
   */
  public void removeCPDeviceEventListener(ICPDeviceEventListener cpDeviceEventListener)
  {
    cpDeviceEventListenerList.remove(cpDeviceEventListener);
  }

  /**
   * Retrieves the messageForwarderEventThread.
   * 
   * @return The messageForwarderEventThread
   */
  public EventThread getEventThread()
  {
    return templateControlPoint.getBasicControlPoint().getControlPointEventThread();
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(ssdpManagement);
    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(httpManagement);
    templateControlPoint.getBasicControlPoint().getControlPointEventThread().register(udpPacketManager);

    // terminate forwarder modules
    for (int i = 0; i < forwarderModules.size(); i++)
    {
      ((IForwarderModule)forwarderModules.elementAt(i)).terminate();
    }

    templateControlPoint.terminate();
  }

}

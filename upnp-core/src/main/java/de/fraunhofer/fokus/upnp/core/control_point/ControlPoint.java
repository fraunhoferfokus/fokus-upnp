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

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageModifier;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageBuilder;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class represents a UPnP control point. All device and state variable change messages are handled in this class.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class ControlPoint implements ICPStateVariableListener, IEventListener
{
  /**
   * UPnP logger
   */
  private static Logger                    logger                              = Logger.getLogger("upnp.cp");

  /** Optional listener for device change events */
  private ICPDeviceEventListener           deviceEventListener                 = null;

  /** Optional listener for state variable change events */
  private ICPStateVariableListener         stateVariableListener               = null;

  /** IP version of control point */
  private int                              ipVersion                           = UPnPConstant.IP4;

  private int                              mxValue                             = UPnPDefaults.CP_MX_VALUE;

  /** Contains device description URL for key UUID */
  private Hashtable                        deviceDescriptionURLFromUUIDTable;

  /** Contains device discovery info for key deviceDescription URL */
  private Hashtable                        deviceDiscoveryInfoFromDescriptionURLTable;

  /** Contains CPDevice for key deviceDescription URL */
  private Hashtable                        cpDeviceFromDescriptionURLTable;

  /** Contains management threads for each subscription */
  private Hashtable                        eventSubscriptionThreadFromSIDTable;

  /** Contains long term discovery infos */
  private Hashtable                        deviceDiscoveryInfoFromUUIDTable;

  /** Contains description retrieval threads */
  private Vector                           descriptionRetrievalList;

  /** Processor for GENA NOTIFY packets */
  private CPEventMessageProcessor          cpEventMessageProcessor;

  /** Processor for multicast GENA NOTIFY packets */
  private CPMulticastEventMessageProcessor cpMulticastEventMessageProcessor;

  /** Processor for SSDP packets */
  private CPSSDPMessageProcessor           cpSSDPMessageProcessor;

  /** Table of socket structures for all external IP addresses */
  private Hashtable                        socketStructureFromHostAddressTable = new Hashtable();

  /** Server that parses received multicast NOTIFY messages */
  protected CPNotifyServer                 ssdpNotifyServer;

  /** Server that sends M-SEARCH packets and process the responses */
  protected CPMSearchClient                ssdpMSearchClient;

  /** Class that handles device lifetime */
  protected CPDeviceLifetimeHandler        deviceLifetimeHandler;

  /** Friendly name */
  private String                           name                                = "ControlPoint";

  /** Used multicast address (usually 239.255.255.250) */
  protected InetSocketAddress              ssdpMulticastSocketAddress;

  /** StartupConfiguration */
  private UPnPStartupConfiguration         startupConfiguration;

  /** StartupConfiguration for control point */
  private ControlPointStartupConfiguration controlPointStartupConfiguration;

  /** DeviceCache */
  private CPDeviceCache                    deviceCache;

  /** Flag to store only discovery info */
  private boolean                          discoveryOnly;

  /** Flag to disable reading the metadata */
  private boolean                          disableMetadataRetrieval;

  /** Flag to disable event processing */
  private boolean                          disableEventProcessing;

  /** Server that parses received UDP event NOTIFY messages. Proprietary extension */
  protected CPUDPEventNotifyServer         udpEventNotifyServer;

  /** Optional modifier for GENA subscribe messages. */
  protected IHTTPMessageModifier           subscribeModifier;

  /** Associated template control point or null */
  protected TemplateControlPoint           templateControlPoint;

  /** Event thread */
  protected EventThread                    controlPointEventThread;

  /** Sync object for device info */
  protected Object                         deviceInfoLock                      = new Object();

  /** Vector with ignored IP addresses for devices */
  protected Vector                         ignoredDeviceAddressList            = new Vector();

  /** Earliest time to send a pending search request */
  protected long                           searchRootDeviceTime                = 0;

  /**
   * Creates a new UPnP Control point.
   * 
   * @param startupConfiguration
   *          Startup configuration for the control point
   * @param cpMessageProcessorFactory
   *          The factory that creates needed message processors
   * @param deviceEventListener
   *          The listener for UPnP device events
   * @param stateVariableListener
   *          The listener for state variable events
   * 
   */
  public ControlPoint(UPnPStartupConfiguration startupConfiguration,
    CPMessageProcessorFactory cpMessageProcessorFactory,
    ICPDeviceEventListener deviceEventListener,
    ICPStateVariableListener stateVariableListener)
  {
    this.startupConfiguration = startupConfiguration;
    this.controlPointStartupConfiguration =
      (ControlPointStartupConfiguration)startupConfiguration.getSingleControlPointStartupConfiguration();
    if (controlPointStartupConfiguration == null)
    {
      System.out.println("Missing control point startup info. Exit application");
      return;
    }
    this.name = controlPointStartupConfiguration.getFriendlyName();

    this.deviceEventListener = deviceEventListener;
    this.stateVariableListener = stateVariableListener;
    this.discoveryOnly = controlPointStartupConfiguration.isSetDiscoveryOnly();
    if (!discoveryOnly && !controlPointStartupConfiguration.isSetDisableDeviceCache())
    {
      this.deviceCache = new CPDeviceCache(this, FileHelper.getResourceDirectoryName() + "device_cache");
    }
    this.disableMetadataRetrieval = controlPointStartupConfiguration.isSetDisableMetadataRetrieval();
    this.disableEventProcessing = controlPointStartupConfiguration.isSetDisableEventProcessing();

    // parse string with IP addresses that should be ignored
    String ignoredDeviceAddressesString = controlPointStartupConfiguration.getIgnoredDeviceAddresses();
    if (ignoredDeviceAddressesString != null)
    {
      StringTokenizer tokenizer = new StringTokenizer(ignoredDeviceAddressesString, ",;:");
      while (tokenizer.hasMoreTokens())
      {
        ignoredDeviceAddressList.add(tokenizer.nextToken().trim());
      }
    }
    String IPVersion = System.getProperty("PreferIPVersion", UPnPConstant.IPv4_VERSION);
    if (IPVersion.equals(UPnPConstant.IPv4_VERSION))
    {
      ipVersion = UPnPConstant.IP4;
    } else if (IPVersion.equals(UPnPConstant.IPv6_VERSION))
    {
      ipVersion = UPnPConstant.IP6;
    } else
    {
      logger.fatal("unknown IP version" + IPVersion);
      return;
    }
    controlPointEventThread = new EventThread(toString());

    deviceDiscoveryInfoFromDescriptionURLTable = new Hashtable();
    cpDeviceFromDescriptionURLTable = new Hashtable();
    deviceDescriptionURLFromUUIDTable = new Hashtable();
    eventSubscriptionThreadFromSIDTable = new Hashtable();
    deviceDiscoveryInfoFromUUIDTable = new Hashtable();
    descriptionRetrievalList = new Vector();
    deviceLifetimeHandler = new CPDeviceLifetimeHandler(this);

    // create message processors using factory
    cpEventMessageProcessor = cpMessageProcessorFactory.getInstanceOfCPEventMessageProcessor(this);
    cpSSDPMessageProcessor = cpMessageProcessorFactory.getInstanceOfCPSSDPMessageProcessor(this);

    // create multicast message processor if needed
    if (controlPointStartupConfiguration.getMulticastEventServerPort() != -1)
    {
      cpMulticastEventMessageProcessor = cpMessageProcessorFactory.getInstanceOfCPMulticastEventMessageProcessor(this);
    }

    this.ssdpMulticastSocketAddress = startupConfiguration.getSSDPMulticastSocketAddress();

    // start servers
    initHostAddressSocketStructures();
    ssdpNotifyServer = new CPNotifyServer(this, cpSSDPMessageProcessor);
    ssdpMSearchClient = new CPMSearchClient(this, cpSSDPMessageProcessor);
    // start proprietary UDP event server if requested
    if (controlPointStartupConfiguration.getEventCallbackUDPServerPort() != -1 ||
      controlPointStartupConfiguration.getMulticastEventServerPort() != -1)
    {
      udpEventNotifyServer =
        new CPUDPEventNotifyServer(this, cpEventMessageProcessor, cpMulticastEventMessageProcessor);
    }

    controlPointEventThread.register(this);
    controlPointEventThread.register(deviceLifetimeHandler);
    controlPointEventThread.register(ssdpMSearchClient);
    controlPointEventThread.register(ssdpNotifyServer);
    if (deviceCache != null)
    {
      controlPointEventThread.register(deviceCache);
    }
    if (udpEventNotifyServer != null)
    {
      controlPointEventThread.register(udpEventNotifyServer);
    }
  }

  /** Retrieves the name of this control point */
  public String toString()
  {
    return name;
  }

  /** Retrieves a string containing device statistics */
  public String toDeviceStatisticsString()
  {
    String result = "";

    Enumeration discoveryInfos = CollectionHelper.getPersistentElementsEnumeration(deviceDiscoveryInfoFromUUIDTable);
    while (discoveryInfos.hasMoreElements())
    {
      CPDeviceDiscoveryInfo discoveryInfo = (CPDeviceDiscoveryInfo)discoveryInfos.nextElement();
      result += discoveryInfo.getRootDeviceUUID() + "\n";
      for (int i = 0; i < discoveryInfo.getPublishedIPAddressList().size(); i++)
      {
        InetSocketAddress currentAddress = (InetSocketAddress)discoveryInfo.getPublishedIPAddressList().get(i);
        if (i == 0)
        {
          result += "  URLs:            " + IPHelper.toString(currentAddress) + "\n";
        } else
        {
          result += "                   " + IPHelper.toString(currentAddress) + "\n";
        }
      }
      for (int i = 0; i < discoveryInfo.getFriendlyNameList().size(); i++)
      {
        String name = (String)discoveryInfo.getFriendlyNameList().get(i);
        if (i == 0)
        {
          result += "  FriendlyName:    " + name + "\n";
        } else
        {
          result += "                   " + name + "\n";
        }
      }
      result +=
        "  Active:          " + (getCPDeviceByUDN(discoveryInfo.getRootDeviceUUID()) != null) + ", " +
          discoveryInfo.getDeviceDetectionCount() + " discoveries\n";
      result +=
        "  Removals:        " + discoveryInfo.getDeviceTimedOutCount() + " time outs," +
          discoveryInfo.getDeviceRemovalCount() + " removals\n";
      result +=
        "  Disc. interval:  " + discoveryInfo.getAverageDiscoveryTime() + " seconds, max age is " +
          discoveryInfo.getMaxage() + " seconds \n";
      result +=
        "  Discovery:       " + (System.currentTimeMillis() - discoveryInfo.getFirstDiscoveryTime()) / 1000 +
          " seconds ago (first), " + (System.currentTimeMillis() - discoveryInfo.getLastDiscoveryTime()) / 1000 +
          " seconds ago (last)\n";
      result += "\n";
    }

    return result;
  }

  /**
   * Retrieves the hashtable with all device discovery infos, indexed with the device description URL.
   */
  public Hashtable getDiscoveryInfoFromDescriptionURLTable()
  {
    return deviceDiscoveryInfoFromDescriptionURLTable;
  }

  /** Retrieves the hashtable with all root devices, indexed with their device description URL. */
  public Hashtable getCPDeviceFromDescriptionURLTable()
  {
    return cpDeviceFromDescriptionURLTable;
  }

  /** Retrieves the hashtable with all device description URLs, indexed by the root device UUID. */
  public Hashtable getDeviceDescriptionURLFromUUIDTable()
  {
    return deviceDescriptionURLFromUUIDTable;
  }

  /**
   * @return
   */
  public Hashtable getEventSubscriptionThreadFromSIDTable()
  {
    return eventSubscriptionThreadFromSIDTable;
  }

  public Hashtable getDeviceDiscoveryInfoFromUUIDTable()
  {
    return deviceDiscoveryInfoFromUUIDTable;
  }

  public void addDescriptionRetrievalThread(CPDeviceDescriptionRetrieval deviceDescriptionRetrieval)
  {
    synchronized(descriptionRetrievalList)
    {
      descriptionRetrievalList.add(deviceDescriptionRetrieval);
    }
  }

  public void removeDescriptionRetrievalThread(CPDeviceDescriptionRetrieval deviceDescriptionRetrieval)
  {
    synchronized(descriptionRetrievalList)
    {
      if (descriptionRetrievalList.contains(deviceDescriptionRetrieval))
      {
        if (deviceDescriptionRetrieval.isAlive())
        {
          CPDeviceDescriptionRetrieval.ACTIVE_THREADS--;
        } else
        {
          // try to remove thread that was not yet started
          System.out.println("ERROR: Remove thread that was not started");
        }
        descriptionRetrievalList.remove(deviceDescriptionRetrieval);
      }
    }
  }

  /** Starts description retrieval threads that are waiting */
  protected void startPendingDescriptionRetrievals()
  {
    if (CPDeviceDescriptionRetrieval.ACTIVE_THREADS >= UPnPDefaults.CP_DEVICE_RETRIEVAL_THREAD_COUNT)
    {
      return;
    }
    synchronized(descriptionRetrievalList)
    {
      for (int i = 0; i < descriptionRetrievalList.size() &&
        CPDeviceDescriptionRetrieval.ACTIVE_THREADS < UPnPDefaults.CP_DEVICE_RETRIEVAL_THREAD_COUNT; i++)
      {
        CPDeviceDescriptionRetrieval currentDescriptionRetrieval =
          (CPDeviceDescriptionRetrieval)descriptionRetrievalList.elementAt(i);
        if (!currentDescriptionRetrieval.isAlive())
        {
          // System.out.println("START " + currentDescriptionRetrieval.toString());

          CPDeviceDescriptionRetrieval.ACTIVE_THREADS++;
          currentDescriptionRetrieval.start();

          System.out.println("  Number of active or pending descriptions is " + descriptionRetrievalList.size());
        }
      }
    }
  }

  public int getIPVersion()
  {
    return ipVersion;
  }

  /**
   * Sends a M_SEARCH message to the UPnP multicast address and creates a thread that parses all responses to this
   * request
   */
  private void sendMessage(String message)
  {
    getSSDPMSearchClient().sendMessageToMulticast(message);
  }

  /**
   * Sends search all device message.
   */
  public void sendSearchAllDeviceMessage()
  {
    logger.info("search all device......");

    String message =
      MSearchMessageBuilder.createSearchAllMessage(mxValue, getSSDPMulticastSocketAddressString(), ipVersion);
    sendMessage(message);
  }

  /**
   * Sends search device unique identification message
   * 
   * @param UUID
   *          uuid of device (UDN)
   */
  public void sendSearchUUIDMessage(String UUID)
  {
    logger.info("search device uuid = " + UUID);

    String message =
      MSearchMessageBuilder.createSearchUUIDMessage(mxValue, getSSDPMulticastSocketAddressString(), UUID, ipVersion);
    sendMessage(message);
  }

  /**
   * Sends search root device message.
   */
  public void sendSearchRootDeviceMessage()
  {
    logger.info("search root device.......");

    String message =
      MSearchMessageBuilder.createSearchRootMessage(mxValue, getSSDPMulticastSocketAddressString(), ipVersion);
    sendMessage(message);
  }

  /**
   * Sends a search root device message after a random wait interval up to MX seconds.
   */
  public void sendDelayedSearchRootDeviceMessage()
  {
    searchRootDeviceTime = (long)(Portable.currentTimeMillis() + Math.random() * UPnPDefaults.CP_MX_VALUE * 1000);
  }

  /**
   * Creates a search device type packet.
   * 
   * @param deviceType
   *          device type
   */
  public String createSearchRootDeviceMessage()
  {
    logger.info("create search root device");

    return MSearchMessageBuilder.createSearchRootMessage(mxValue, getSSDPMulticastSocketAddressString(), ipVersion);
  }

  /**
   * Sends search device type message.
   * 
   * @param deviceType
   *          device type
   */
  public void sendSearchDeviceTypeMessage(String deviceType)
  {
    logger.info("search device type = " + deviceType);

    String message =
      MSearchMessageBuilder.createSearchDeviceTypeMessage(mxValue,
        getSSDPMulticastSocketAddressString(),
        deviceType,
        ipVersion);
    sendMessage(message);
  }

  /**
   * Creates a search device type packet.
   * 
   * @param deviceType
   *          device type
   */
  public String createSearchDeviceTypeMessage(String deviceType)
  {
    logger.info("create search device type = " + deviceType);

    return MSearchMessageBuilder.createSearchDeviceTypeMessage(mxValue,
      getSSDPMulticastSocketAddressString(),
      deviceType,
      ipVersion);
  }

  /**
   * Sends serch service type message
   * 
   * @param serviceType
   *          service type
   */
  public void sendSearchServiceTypeMessage(String serviceType)
  {
    logger.info("search service type = " + serviceType);

    String message =
      MSearchMessageBuilder.createSearchServiceTypeMessage(mxValue,
        getSSDPMulticastSocketAddressString(),
        serviceType,
        ipVersion);
    sendMessage(message);
  }

  /**
   * Sets the listener for device found and gone event
   * 
   * @param eventListener
   *          The event listener
   */
  public void setCPDeviceEventListener(ICPDeviceEventListener eventListener) throws Exception
  {
    if (eventListener != null && deviceEventListener != null)
    {
      throw new Exception("Would kick old listener");
    }

    this.deviceEventListener = eventListener;
  }

  /**
   * Sets a listener for state variable changes.
   * 
   * @param eventListener
   *          The event listener
   */
  public void setCPStateVariableListener(ICPStateVariableListener eventListener) throws Exception
  {
    if (eventListener != null && stateVariableListener != null)
    {
      throw new Exception("Would kick old listener");
    }

    this.stateVariableListener = eventListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (stateVariableListener != null)
    {
      stateVariableListener.stateVariableChanged(stateVariable);
    }
  }

  /** Event that the subscription state of a service has changed */
  public void subscriptionStateChanged(CPService service)
  {
    if (deviceEventListener != null)
    {
      deviceEventListener.deviceEvent(service.getCPDevice(), service.isSubscribed()
        ? UPnPConstant.DEVICE_EVENT_SUBSCRIPTION_START : UPnPConstant.DEVICE_EVENT_SUBSCRIPTION_END, service);
    }
  }

  /**
   * Removes a root device from this control point.
   * 
   * @param device
   *          The device that should be removed
   */
  public void removeRootDevice(CPDevice device, boolean timedOut)
  {
    try
    {
      synchronized(deviceInfoLock)
      {
        String uuid = device.getUDN();
        // remove discovery URL from UUID table
        URL deviceURL = (URL)deviceDescriptionURLFromUUIDTable.remove(uuid);
        if (deviceURL != null)
        {
          // remove discovery info
          deviceDiscoveryInfoFromDescriptionURLTable.remove(deviceURL);
          // remove device
          cpDeviceFromDescriptionURLTable.remove(deviceURL);
        }
        // add statistical data for this UUID
        CPDeviceDiscoveryInfo uuidDeviceDiscoveryInfo =
          (CPDeviceDiscoveryInfo)deviceDiscoveryInfoFromUUIDTable.get(device.getUDN());
        if (uuidDeviceDiscoveryInfo != null)
        {
          if (timedOut)
          {
            uuidDeviceDiscoveryInfo.incDeviceTimedOutCount();
          } else
          {
            uuidDeviceDiscoveryInfo.incDeviceRemovalCount();
          }
        }

        // end subscription threads
        for (int i = 0; i < device.getCPServiceTable().length; i++)
        {
          CPService service = device.getCPServiceTable()[i];
          if (service.isSubscribed())
          {
            // no unsubscription message needed, because the device is already gone
            service.getEventSubscriptionThread().terminate(false);
          }
        }
      }
      // generate event
      rootDeviceGone(device);
    } catch (Exception e)
    {
      e.printStackTrace();
      logger.error("removeDevice not possible:" + e);
    }
  }

  /**
   * Event that a new root device has been found.
   * 
   * @param device
   *          New device
   * @param cacheNewDevice
   *          Flag to cache device description
   * @param updateSSDPInfo
   *          Flag to update cached SSDP info
   * 
   */
  public void newRootDevice(CPDevice device, boolean cacheNewDevice, boolean updateSSDPInfo)
  {
    if (deviceCache != null)
    {
      if (cacheNewDevice)
      {
        deviceCache.storeCompleteDeviceDescription(device);
      }

      // update discovery info to allow deprecation of devices
      if (updateSSDPInfo)
      {
        deviceCache.updateSSDPInfo(device);
      }
    }
    // try to subscribe to multicast events
    if (cpMulticastEventMessageProcessor != null)
    {
      CPService[] services = device.getCPServiceTable();
      for (int i = 0; i < services.length; i++)
      {
        if (services[i].supportsMulticastEvents())
        {
          TemplateControlPoint.printMessage(toString() + ": Try to subscribe to multicast events for " +
            device.toString() + "." + services[i].toString());

          // join multicast address
          try
          {
            device.getCPSocketStructure()
              .getMulticastEventUDPServer()
              .joinGroup(services[i].getMulticastEventSocketAddress().getAddress());
          } catch (IOException e)
          {
          }
          // request current state variable values
          services[i].sendInitialEventMessage();
        }
      }
    }

    logger.info("New device: " + device.getDeviceType());
    TemplateControlPoint.printMessage(toString() + ": NEW DEVICE: " + device.getFriendlyName() + " [" +
      device.getDeviceDescriptionURL() + "]");

    if (deviceEventListener != null)
    {
      deviceEventListener.newDevice(device);
    }
  }

  /**
   * Event that a device is no longer available.
   * 
   * @param device
   *          Removed device
   */
  public void rootDeviceGone(CPDevice device)
  {
    // forward to event listener
    logger.info("Device gone: " + device.getDeviceType());
    if (deviceEventListener != null)
    {
      deviceEventListener.deviceGone(device);
    }
  }

  /** Subscribes to all services of a device. */
  public void subscribeToDeviceServices(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();
    if (services != null)
    {
      for (int i = 0; i < services.length; i++)
      {
        if (!services[i].isSubscribed() && !services[i].isMulticastSubscribed())
        {
          services[i].sendSubscription();
        }
      }
    }
  }

  /** Ends the subscription for all services of a device. */
  public void endSubscriptionToDeviceServices(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();
    if (services != null)
    {
      for (int i = 0; i < services.length; i++)
      {
        if (services[i].isSubscribed())
        {
          services[i].sendUnsubscription();
        }
      }
    }
  }

  /**
   * returns device found in the UPnP nets
   * 
   * @param UDN
   *          Unique device name
   * @return upnp device if found, otherwise null
   */
  public CPDevice getCPDeviceByUDN(String UDN)
  {
    Enumeration e = cpDeviceFromDescriptionURLTable.elements();

    while (e.hasMoreElements())
    {
      CPDevice dev = (CPDevice)e.nextElement();

      if (dev.getUDN().equalsIgnoreCase(UDN))
      {
        return dev;
      }

      CPDevice[] embDevs = dev.getCPDeviceTable();
      dev = getCPDeviceByUDN(UDN, embDevs);

      if (dev != null)
      {
        return dev;
      }
    }

    return null;
  }

  private CPDevice getCPDeviceByUDN(String UDN, CPDevice[] devs)
  {
    if (devs == null)
    {
      return null;
    }

    for (int i = 0; i < devs.length; i++)
    {
      if (devs[i].getUDN().equalsIgnoreCase(UDN))
      {
        return devs[i];
      }

      CPDevice[] embDevs = devs[i].getCPDeviceTable();
      CPDevice dev = getCPDeviceByUDN(UDN, embDevs);

      if (dev != null)
      {
        return dev;
      }
    }

    return null;
  }

  /**
   * Retrieves the message processor for NOTIFY event messages.
   * 
   * @return
   */
  public CPEventMessageProcessor getCPEventMessageProcessor()
  {
    return cpEventMessageProcessor;
  }

  /**
   * Retrieves the cpMulticastEventMessageProcessor.
   * 
   * @return The cpMulticastEventMessageProcessor
   */
  public CPMulticastEventMessageProcessor getCPMulticastEventMessageProcessor()
  {
    return cpMulticastEventMessageProcessor;
  }

  /**
   * Retrieves the message processor for SSDP messages.
   * 
   * @return
   */
  public CPSSDPMessageProcessor getCPSSDPMessageProcessor()
  {
    return cpSSDPMessageProcessor;
  }

  public CPMSearchClient getSSDPMSearchClient()
  {
    return ssdpMSearchClient;
  }

  /**
   * Sends an action request to a remote service.
   * 
   * @param action
   *          The action
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    action.getCPService().invokeAction(action, null);
  }

  /**
   * Sends an action request to a remote service.
   * 
   * @param action
   *          The action
   * @param optionalHeaderLines
   *          A vector containing additional headers for the SOAP request
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeAction(CPAction action, Vector optionalHeaderLines) throws InvokeActionException,
    ActionFailedException
  {
    action.getCPService().invokeAction(action, optionalHeaderLines);
  }

  /**
   * Sends a personalized action request to a remote service. The Signature of this invoke will create with an symmetric
   * operation
   * 
   * @param encrypt
   *          check if the data will become enrcrypt.
   * @param action
   *          the invoke action.
   * @param signature
   *          to check the integrity and if the data were manipulate.
   * @param sequenceBase
   * @param keyId
   *          to find the aesKey in the personalizedManger object, because the aesKey will not transmit to the invoke
   *          action.
   * @param aesKey
   *          to encrypt the data.
   * @param iv
   *          to check the signature.
   * 
   * @throws InvokeActionException
   *           if action could not be invoked
   * @throws ActionFailedException
   *           if action produced an error at the server
   */
  public void invokeSymmetricKeyPersonalizedAction(boolean encrypt,
    CPAction action,
    String sequenceBase,
    String keyId,
    SecretKey aesKey,
    byte[] iv) throws InvokeActionException, ActionFailedException
  {
    action.getCPService().invokeSymmetricKeyPersonalizedAction(encrypt, action, sequenceBase, keyId, aesKey, iv);
  }

  /**
   * Sends a personalized action request to a remote service. The Signature of this invoke will create with an
   * asymmetric operation
   * 
   * @param action
   *          the invoke action.
   * @param publicKey
   *          the public key to check the signature.
   * @param signature
   *          to check the integrity and if the data were manipulate during transmission.
   * @param nonce
   *          a random number for the request to prevent replay attacks.
   * @throws InvokeActionException
   * @throws ActionFailedException
   */
  public void invokePublicKeyPersonalizedAction(CPAction action,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    String nonce) throws InvokeActionException, ActionFailedException
  {
    action.getCPService().invokePublicKeyPersonalizedAction(action, privateKey, publicKey, nonce);
  }

  /** Retrieves the number of external host address socket structures */
  public int getSocketStructureCount()
  {
    return socketStructureFromHostAddressTable.size();
  }

  /** Retrieves a specific host address socket structure */
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

  /** Retrieves a specific host address socket structure */
  public ControlPointHostAddressSocketStructure getSocketStructure(InetAddress hostAddress)
  {
    Object result = socketStructureFromHostAddressTable.get(hostAddress);
    if (result != null)
    {
      return (ControlPointHostAddressSocketStructure)result;
    }

    return null;
  }

  /** Retrieves the preferred host address socket structure */
  public ControlPointHostAddressSocketStructure getPreferredSocketStructure()
  {
    Object result = socketStructureFromHostAddressTable.get(IPHelper.getLocalHostAddress());
    if (result != null)
    {
      return (ControlPointHostAddressSocketStructure)result;
    }

    return null;
  }

  /** Closes all socket structures */
  public void closeSocketStructures()
  {
    Vector socketStructures = getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure socketStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      socketStructure.terminate();
    }
  }

  /**
   * Initializes sockets and servers for the device
   */
  private void initHostAddressSocketStructures()
  {
    System.out.println("    " + name + ": Use multicast socket address: " +
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
   * Adds a new network interface to this control point
   * 
   * @param The
   *          new host address
   * @return The created socket structure
   */
  public ControlPointHostAddressSocketStructure tryAddHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress hostAddress)
  {
    if (hostAddress instanceof Inet6Address)
    {
      System.out.println(toString() + ": Host address " + hostAddress.getHostName() + " is IPv6");
      return null;
    }
    if (socketStructureFromHostAddressTable.containsKey(hostAddress))
    {
      System.out.println(toString() + ": Host address " + hostAddress.getHostName() + " is already in use");
      return null;
    }
    // get list of ignored IP addresses
    Vector ignoredIPAddresses = startupConfiguration.getIgnoredIPAddressesList();
    if (ignoredIPAddresses.contains(hostAddress.getHostAddress()))
    {
      System.out.println(toString() + ": Host address " + hostAddress.getHostName() + " is in ignore list");
      return null;
    }
    // ignore loopback address
    if (hostAddress.getHostAddress().equals("127.0.0.1"))
    {
      // System.out.println(toString() + ": Host address is loopback address");
      return null;
    }
    TemplateControlPoint.printMessage(toString() +
      ": Create ControlPointHostAddressSocketStructure for local host address " + hostAddress.getHostAddress());

    ControlPointHostAddressSocketStructure hostAddressSocketStructure =
      createControlPointHostAddressSocketStructure(networkInterface, hostAddress);

    // add to hashtable if valid
    if (hostAddressSocketStructure != null)
    {
      socketStructureFromHostAddressTable.put(hostAddress, hostAddressSocketStructure);
    }
    return hostAddressSocketStructure;
  }

  /** Creates a new host address socket structure for a certain host address. */
  protected ControlPointHostAddressSocketStructure createControlPointHostAddressSocketStructure(NetworkInterface networkInterface,
    InetAddress hostAddress)
  {
    ControlPointHostAddressSocketStructure hostAddressSocketStructure = null;
    // choose whether to use fixed ports
    if (controlPointStartupConfiguration.useFixedPorts())
    {
      hostAddressSocketStructure =
        new ControlPointHostAddressSocketStructure(this,
          networkInterface,
          hostAddress,
          controlPointStartupConfiguration.getEventCallbackServerPort(),
          controlPointStartupConfiguration.getEventCallbackUDPServerPort(),
          controlPointStartupConfiguration.getMulticastEventServerPort(),
          controlPointStartupConfiguration.getSSDPUnicastPort());
    }
    // if fixed ports are not possible, use random ports
    if (hostAddressSocketStructure == null || !hostAddressSocketStructure.isValid())
    {
      if (hostAddressSocketStructure != null)
      {
        // close already opened sockets
        hostAddressSocketStructure.terminate();

      }
      hostAddressSocketStructure =
        new ControlPointHostAddressSocketStructure(this,
          networkInterface,
          hostAddress,
          controlPointStartupConfiguration.getEventCallbackUDPServerPort() != -1);
    }
    if (hostAddressSocketStructure.isValid())
    {
      return hostAddressSocketStructure;
    }

    return null;
  }

  /** Removes a network interface from this device */
  public void removeHostAddressSocketStructure(InetAddress hostAddress)
  {
    Object structure = socketStructureFromHostAddressTable.remove(hostAddress);
    if (structure != null)
    {
      ControlPointHostAddressSocketStructure hostAddressSocketStructure =
        (ControlPointHostAddressSocketStructure)structure;

      hostAddressSocketStructure.terminate();

      boolean newSearch = false;
      // remove all devices that were connected over this socket structure
      Enumeration devices = CollectionHelper.getPersistentElementsEnumeration(cpDeviceFromDescriptionURLTable);

      while (devices.hasMoreElements())
      {
        CPDevice currentDevice = (CPDevice)devices.nextElement();
        InetAddress deviceAddress = currentDevice.getDeviceDescriptionSocketAddress().getAddress();
        if (IPHelper.isCommonSubnet(hostAddress, deviceAddress))
        {
          TemplateControlPoint.printMessage(toString() + ": Remove device due to removed host address: " +
            currentDevice.toDiscoveryString());
          removeRootDevice(currentDevice, false);
        }
        // check if device is running on the same host as the control point
        // in this case the device may be available over other local host addresses
        if (hostAddress.equals(deviceAddress))
        {
          newSearch = true;
        }
      }
      // start new M-SEARCH
      if (getSocketStructureCount() > 0 && newSearch)
      {
        sendSearchRootDeviceMessage();
      }
    }
  }

  /**
   * Retrieves the ssdpMulticastAddress.
   * 
   * @return The ssdpMulticastAddress.
   */
  public String getSSDPMulticastSocketAddressString()
  {
    return IPHelper.toString(ssdpMulticastSocketAddress);
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
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public UPnPStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves the controlPointStartupConfiguration.
   * 
   * @return The controlPointStartupConfiguration
   */
  public ControlPointStartupConfiguration getControlPointStartupConfiguration()
  {
    return controlPointStartupConfiguration;
  }

  /**
   * Retrieves the deviceCache.
   * 
   * @return The deviceCache
   */
  public CPDeviceCache getCPDeviceCache()
  {
    return deviceCache;
  }

  /**
   * Retrieves the discoveryOnly flag.
   * 
   * @return The discoveryOnly
   */
  public boolean isSetDiscoveryOnly()
  {
    return discoveryOnly;
  }

  /**
   * Retrieves the readMetadata.
   * 
   * @return The readMetadata
   */
  public boolean isSetDisableMetadataRetrieval()
  {
    return disableMetadataRetrieval;
  }

  /**
   * @return the disableEventProcessing
   */
  public boolean isSetDisableEventProcessing()
  {
    return disableEventProcessing;
  }

  /**
   * Sets the new value for disableEventProcessing.
   * 
   * @param disableEventProcessing
   *          The new value for disableEventProcessing
   */
  public void setDisableEventProcessing(boolean disableEventProcessing)
  {
    this.disableEventProcessing = disableEventProcessing;
  }

  /** Checks if a device with a certain IP address should be ignored */
  public boolean isIgnoredDeviceAddress(String hostAddress)
  {
    for (int i = 0; i < ignoredDeviceAddressList.size(); i++)
    {
      if (((String)ignoredDeviceAddressList.elementAt(i)).equalsIgnoreCase(hostAddress))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves the subscribeModifier.
   * 
   * @return The subscribeModifier
   */
  public IHTTPMessageModifier getSubscribeModifier()
  {
    return subscribeModifier;
  }

  /**
   * Sets the subscribeModifier.
   * 
   * @param subscribeModifier
   *          The new value for subscribeModifier
   */
  public void setSubscribeModifier(IHTTPMessageModifier subscribeModifier)
  {
    this.subscribeModifier = subscribeModifier;
  }

  /**
   * Retrieves the templateControlPoint.
   * 
   * @return The templateControlPoint
   */
  public TemplateControlPoint getTemplateControlPoint()
  {
    return templateControlPoint;
  }

  /**
   * Sets the templateControlPoint.
   * 
   * @param templateControlPoint
   *          The new value for templateControlPoint
   */
  public void setTemplateControlPoint(TemplateControlPoint templateControlPoint)
  {
    this.templateControlPoint = templateControlPoint;
  }

  /**
   * Retrieves the controlPointEventThread.
   * 
   * @return The controlPointEventThread
   */
  public EventThread getControlPointEventThread()
  {
    return controlPointEventThread;
  }

  /**
   * Retrieves the deviceInfoLock.
   * 
   * @return The deviceInfoLock
   */
  public Object getDeviceInfoLock()
  {
    return deviceInfoLock;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    startPendingDescriptionRetrievals();
    if (searchRootDeviceTime != 0 && Portable.currentTimeMillis() > searchRootDeviceTime)
    {
      Portable.println("Search root devices");
      searchRootDeviceTime = 0;

      String message =
        MSearchMessageBuilder.createSearchRootMessage(mxValue, getSSDPMulticastSocketAddressString(), ipVersion);
      sendMessage(message);
    }
  }

  /** Called when the control point is terminated */
  public void terminate()
  {
    controlPointEventThread.unregister(this);
    controlPointEventThread.unregister(deviceLifetimeHandler);
    controlPointEventThread.unregister(ssdpMSearchClient);
    controlPointEventThread.unregister(ssdpNotifyServer);
    if (deviceCache != null)
    {
      controlPointEventThread.unregister(deviceCache);
    }
    if (udpEventNotifyServer != null)
    {
      controlPointEventThread.unregister(udpEventNotifyServer);
    }

    controlPointEventThread.terminate();
    // terminate description retrieval threads
    // the original list is changed when each description thread terminates so we use a persistent
    // copy
    for (Enumeration e = CollectionHelper.getPersistentEntryEnumeration(descriptionRetrievalList); e.hasMoreElements();)
    {
      ((CPDeviceDescriptionRetrieval)e.nextElement()).terminate();
    }
    closeSocketStructures();
  }

}

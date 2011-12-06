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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPDeviceDescriptionRetrieval;
import de.fraunhofer.fokus.upnp.core.control_point.CPDeviceDiscoveryInfo;
import de.fraunhofer.fokus.upnp.core.control_point.CPMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPointHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.control_point.ISubscriptionPolicyListener;
import de.fraunhofer.fokus.upnp.core.control_point.MetadataRetrievalThread;
import de.fraunhofer.fokus.upnp.core.device.personalization.PersonalizationDefaults;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.MemoryMonitor;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.NetworkInterfaceManager;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAKeyPair;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class represents a template UPnP control point. This class adds new functionality compared to ControlPoint and
 * should be used as UPnP control point for all applications.
 * 
 * Other classes can register for the observation of special device classes like media servers.
 * 
 * @author Alexander Koenig, Sebastian Nauck
 * 
 */
public class TemplateControlPoint implements
  ICPDeviceEventListener,
  ICPStateVariableListener,
  INetworkInterfaceChangeListener,
  ISubscriptionPolicyListener
{
  /** Associated logger */
  public static Logger                       logger                            = Logger.getLogger("upnp.cp");

  /** Associated inner control point */
  protected ControlPoint                     basicControlPoint;

  /** Associated entity */
  private TemplateEntity                     entity;

  /** List with discovered devices */
  private Vector                             deviceList;

  /** Name for the control point */
  private String                             name;

  /** Flag to automatically subscribe to found devices (default=false) */
  private boolean                            automaticEventSubscription;

  /** Hashtable that holds list of special device classes */
  private Hashtable                          deviceListFromDeviceTypeHashtable;

  /** Triggers events for network interface changes */
  private NetworkInterfaceManager            networkInterfaceManager;

  /** Optional listener for new device/device gone events */
  private ICPDeviceEventListener             deviceEventListener               = null;

  /** OptionaL listener for network interface events */
  private INetworkInterfaceChangeListener    networkInterfaceChangeListener    = null;

  /** Optional listener for state variable events */
  private ICPStateVariableListener           stateVariableListener             = null;

  /** Reference to startup info */
  protected UPnPStartupConfiguration         startupConfiguration;

  /** Reference to control point startup configuration */
  protected ControlPointStartupConfiguration controlPointStartupConfiguration;

  /** Vector containing pending metadata retrieval threads */
  protected Vector                           metadataRetrievalThreadList;

  private Object                             metadataRetrievalThreadLock;

  /** Memory monitor */
  protected MemoryMonitor                    memoryMonitor;

  /** Optional subscription policy listener */
  protected ISubscriptionPolicyListener      subscriptionPolicyListener;

  /** Object to synchronize symmetric key requests */
  private Object                             sessionKeyRequestLock;

  /** Flag to personalize invoked actions if possible */
  protected boolean                          personalizationActive             = true;

  /** RSA keys for personalization */
  protected KeyPair                          personalizationKeyPair;

  /** Method used for signatures (symmetric/asymmetric) */
  protected String                           personalizationSignatureAlgorithm =
                                                                                 PersonalizationDefaults.PERSONALIZATION_SIGNATURE_ALGORITHM;

  /** Flag to encrypt personalized actions. */
  protected boolean                          personalizationEncrypted          =
                                                                                 PersonalizationDefaults.PERSONALIZATION_ENCRYPTED;

  /** Current user for the control point. */
  protected String                           personalizationUser;

  /**
   * Creates a new template control point.
   * 
   * @param anEntity
   *          Associated template entity.
   * @param startupConfiguration
   *          Startup config for the control point
   * 
   */
  public TemplateControlPoint(TemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    this.entity = anEntity;
    this.startupConfiguration = startupConfiguration;
    this.controlPointStartupConfiguration =
      (ControlPointStartupConfiguration)startupConfiguration.getSingleControlPointStartupConfiguration();
    if (controlPointStartupConfiguration == null)
    {
      System.out.println("Missing control point startup info. Exit application");
      return;
    }
    this.name = controlPointStartupConfiguration.getFriendlyName();
    this.automaticEventSubscription = controlPointStartupConfiguration.automaticEventSubscription();

    if (controlPointStartupConfiguration.isSetDiscoveryOnly() != UPnPDefaults.CP_DISCOVERY_ONLY)
    {
      System.out.println("Set discovery only to " + controlPointStartupConfiguration.isSetDiscoveryOnly());
    }
    if (controlPointStartupConfiguration.isSetDisableDeviceCache() != UPnPDefaults.CP_DISABLE_DEVICE_CACHE)
    {
      System.out.println("Set disable device cache to " + controlPointStartupConfiguration.isSetDisableDeviceCache());
    }
    if (controlPointStartupConfiguration.isSetDisableMetadataRetrieval() != UPnPDefaults.CP_DISABLE_METADATA_RETRIEVAL)
    {
      System.out.println("Set disable metadata retrieval to " +
        controlPointStartupConfiguration.isSetDisableMetadataRetrieval());
    }
    if (controlPointStartupConfiguration.isSetDisableEventProcessing() != UPnPDefaults.CP_DISABLE_EVENT_PROCESSING)
    {
      System.out.println("Set disable event processing to " +
        controlPointStartupConfiguration.isSetDisableEventProcessing());
    }
    if (controlPointStartupConfiguration.getMulticastEventServerPort() != -1)
    {
      System.out.println("Activate multicast events for port " +
        controlPointStartupConfiguration.getMulticastEventServerPort());
    }

    deviceList = new Vector();
    deviceListFromDeviceTypeHashtable = new Hashtable();

    metadataRetrievalThreadList = new Vector();
    metadataRetrievalThreadLock = new Object();

    sessionKeyRequestLock = new Object();

    networkInterfaceManager = new NetworkInterfaceManager();
    networkInterfaceManager.addListener(this);

    // load user name and RSA key pair
    initPersonalization(startupConfiguration);

    if (!forceRunDelayed() && !controlPointStartupConfiguration.runDelayed())
    {
      runBasicControlPoint();
    }
  }

  /**
   * This method is called immediately after the control point has been created. It can be overridden by descendant
   * classes to enable additional functionality.
   */
  public void basicControlPointCreated()
  {
  }

  /** Starts the UPnP control point if the template control point was not started immediately */
  public void runBasicControlPoint()
  {
    if (startupConfiguration == null)
    {
      System.out.println("Missing startup info");
      return;
    } else
    {
      // register template control point for device and state variable events
      basicControlPoint = new ControlPoint(startupConfiguration, getInstanceOfCPMessageProcessorFactory(), this, this);
      // associate with this control point
      basicControlPoint.setTemplateControlPoint(this);
      basicControlPointCreated();

      basicControlPoint.getControlPointEventThread().start();

      // start memory monitor
      memoryMonitor = new MemoryMonitor();

      basicControlPoint.getControlPointEventThread().register(memoryMonitor);
      basicControlPoint.getControlPointEventThread().register(networkInterfaceManager);

      // use cache only for standard multicast address and
      // if not disabled
      if (!controlPointStartupConfiguration.isSetDiscoveryOnly() &&
        startupConfiguration.getSSDPMulticastPort() == SSDPConstant.SSDPMulticastPort &&
        !controlPointStartupConfiguration.isSetDisableDeviceCache())
      {
        // try to read devices from cache
        basicControlPoint.getCPDeviceCache().readDeviceCache();

        // try to discover remote devices if device description URLs are known
        discoverRemoteDevices();
      }

      printMessage(DateTimeHelper.formatCurrentDateForDebug() + ":Send search message...");
      // send search message
      basicControlPoint.sendSearchRootDeviceMessage();
    }
  }

  /** Loads the private/public key of the current user from a file or creates a new key. */
  public void initPersonalization(UPnPStartupConfiguration startupConfiguration)
  {
    setPersonalizationUser(loadUserName(startupConfiguration, controlPointStartupConfiguration));
  }

  /** Discover devices that are not announced via multicast. */
  public void discoverRemoteDevices()
  {
    // try to find files in URL cache directory
    File directoryFile = new File(FileHelper.getResourceDirectoryName() + "device_urls");
    if (directoryFile.exists())
    {
      File[] files = directoryFile.listFiles();
      for (int i = 0; i < files.length; i++)
      {
        if (files[i].isFile())
        {
          String deviceURLString = "http://" + URLHelper.escapedURLToString(files[i].getName());
          addRemoteDevice(deviceURLString);
        }
      }
    } else
    {
      printMessage("URLCache directory not found");
    }
  }

  /** Discover devices that are not announced via multicast. */
  public void addRemoteDevice(String deviceURLString)
  {
    try
    {
      URL deviceURL = new URL(deviceURLString);

      // check if a device with this URL is already known
      boolean knownDevice = false;
      for (int j = 0; !knownDevice && j < getCPDeviceCount(); j++)
      {
        knownDevice = getCPDevice(j).getDeviceDescriptionURL().equals(deviceURL);
        if (knownDevice)
        {
          printMessage("Remote device with URL " + deviceURL + " is already known");
        }
      }
      // check if URL is found in discovery info
      if (!knownDevice)
      {
        knownDevice = basicControlPoint.getDiscoveryInfoFromDescriptionURLTable().contains(deviceURL);
        if (knownDevice)
        {
          printMessage("Device description URL found in discovery info");
        }
      }
      // check device cache
      if (!knownDevice && basicControlPoint.getCPDeviceCache() != null)
      {
        knownDevice = basicControlPoint.getCPDeviceCache().isKnownDeviceDescriptionURL(deviceURL);
        if (knownDevice)
        {
          printMessage("Device description URL found in device cache");
        }
      }
      if (!knownDevice)
      {
        printMessage("Remote device with new device description URL " + deviceURL);

        // create basic discovery info
        CPDeviceDiscoveryInfo deviceDiscoveryInfo = new CPDeviceDiscoveryInfo(deviceURL);

        // add discovery info to hashtable
        if (!basicControlPoint.getDiscoveryInfoFromDescriptionURLTable().containsKey(deviceURL))
        {
          basicControlPoint.getDiscoveryInfoFromDescriptionURLTable().put(deviceURL, deviceDiscoveryInfo);
          TemplateControlPoint.printMessage(basicControlPoint.toString() + ": Add basic DEVICE INFO for URL " +
            deviceURL);

          // try to request complete device description
          new CPDeviceDescriptionRetrieval(basicControlPoint, deviceDiscoveryInfo);
        } else
        {
          TemplateControlPoint.printMessage(basicControlPoint.toString() + ": Device discovery info for URL " +
            deviceURL + " is already known");

        }
      }
    } catch (Exception e)
    {
      System.out.println("Error adding remote device with URL " + deviceURLString + ": " + e.getMessage());
    }
  }

  /** Event that additional meta data has been read from the device */
  public void metadataRetrievalFinished(MetadataRetrievalThread retrievalThread)
  {
    synchronized(metadataRetrievalThreadLock)
    {
      metadataRetrievalThreadList.remove(retrievalThread);
    }
  }

  /** This method can be overwritten by descendant classes to force a delayed start. */
  public boolean forceRunDelayed()
  {
    return false;
  }

  /** Retrieves the associated template entity */
  public TemplateEntity getTemplateEntity()
  {
    return entity;
  }

  /** Retrieves the internal UPnP control point */
  public ControlPoint getBasicControlPoint()
  {
    return basicControlPoint;
  }

  /** Returns the used IP version */
  public int getIPVersion()
  {
    return basicControlPoint.getIPVersion();
  }

  /** Sets the event listener for UPnP devices */
  public void setCPDeviceEventListener(ICPDeviceEventListener listener)
  {
    this.deviceEventListener = listener;
  }

  /**
   * Retrieves the message processor factory for the control point. This method may be overwritten by descendant classes
   * to use other message processors.
   */
  protected CPMessageProcessorFactory getInstanceOfCPMessageProcessorFactory()
  {
    return new CPMessageProcessorFactory();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // UPnP events //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void userChanged(String userName)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    // add to list of known devices
    if (!isKnownCPDevice(newDevice.getUDN()) && !isEntityDevice(newDevice.getUDN()))
    {
      deviceList.addElement(newDevice);

      // add to observed lists
      Enumeration keys = deviceListFromDeviceTypeHashtable.keys();
      while (keys.hasMoreElements())
      {
        String currentDeviceType = (String)keys.nextElement();
        if (newDevice.getDeviceType().startsWith(currentDeviceType))
        {
          ((Vector)deviceListFromDeviceTypeHashtable.get(currentDeviceType)).add(newDevice);
        }
      }

      // try to subscribe to all services for event notification
      // do not subscribe if we have a local IP address and the remote device address is global
      if (!(IPHelper.isLocalAddress(IPHelper.getLocalHostAddress()) && !IPHelper.isLocalAddress(newDevice.getDeviceDescriptionSocketAddress()
        .getAddress())))
      {
        startAutomaticEventSubscriptions(newDevice);
      }

      // try to read cached attribute service information
      if (basicControlPoint.getCPDeviceCache() != null)
      {
        basicControlPoint.getCPDeviceCache().readCachedAttributeServiceInformation(newDevice);
      }
      // read from device itself if not already cached
      if (newDevice.getAttributeHashtable() == null && !basicControlPoint.isSetDisableMetadataRetrieval())
      {
        synchronized(metadataRetrievalThreadLock)
        {
          MetadataRetrievalThread metadataRetrievalThread =
          // try to read new information from description service
            new MetadataRetrievalThread(this, newDevice);

          metadataRetrievalThreadList.add(metadataRetrievalThread);
        }
      }
      // signal new device to entity
      if (entity != null)
      {
        entity.newDevice(newDevice);
      }

      // signal new device to listener
      if (deviceEventListener != null)
      {
        deviceEventListener.newDevice(newDevice);
      }
    }

    // recursively create events for embedded devices
    if (newDevice.getCPDeviceTable().length > 0)
    {
      CPDevice[] embeddedDevices = newDevice.getCPDeviceTable();
      for (int i = 0; i < embeddedDevices.length; i++)
      {
        if (embeddedDevices[i] != null)
        {
          newDevice(embeddedDevices[i]);
        }
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
    // DEBUG ONLY
    /*
     * printMessage("Device gone="+goneDevice.getFriendlyName() + " UDN="+goneDevice.getUDN() + "
     * Devicetype="+goneDevice.getDeviceType());
     */
    // recursively remove embedded devices
    if (goneDevice.getCPDeviceTable().length > 0)
    {
      CPDevice[] embeddedDevices = goneDevice.getCPDeviceTable();
      for (int i = 0; i < embeddedDevices.length; i++)
      {
        if (embeddedDevices[i] != null)
        {
          deviceGone(embeddedDevices[i]);
        }
      }
    }
    // try to remove from device list
    int index = getCPDeviceIndexByUDN(goneDevice.getUDN());
    if (index != -1)
    {
      deviceList.removeElementAt(index);

      // remove from observed lists
      Enumeration keys = deviceListFromDeviceTypeHashtable.keys();
      while (keys.hasMoreElements())
      {
        String currentDeviceType = (String)keys.nextElement();
        if (goneDevice.getDeviceType().startsWith(currentDeviceType))
        {
          ((Vector)deviceListFromDeviceTypeHashtable.get(currentDeviceType)).remove(goneDevice);
        }
      }

      // signal to entity
      if (entity != null)
      {
        entity.deviceGone(goneDevice);
      }

      if (deviceEventListener != null)
      {
        deviceEventListener.deviceGone(goneDevice);
      }
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
    // signal to entity
    if (entity != null)
    {
      entity.deviceEvent(device, eventCode, eventParameter);
    }

    // signal event to listener
    if (deviceEventListener != null)
    {
      deviceEventListener.deviceEvent(device, eventCode, eventParameter);
    }
  }

  /**
   * Subscribes to all services of a device if the subscription policy for the service is set to automatic.
   */
  public void startAutomaticEventSubscriptions(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();
    for (int i = 0; services != null && i < services.length; i++)
    {
      // subscribe if either global subscription is on or the service subscription
      // policy is automatic
      if (!services[i].isSubscribed() && !services[i].isMulticastSubscribed() &&
        (automaticEventSubscription || getSubscriptionPolicy(services[i]) == UPnPConstant.SUBSCRIPTION_MODE_AUTOMATIC))
      {
        services[i].sendSubscription();
      }
    }
  }

  /** Subscribes to all services of a device regardless of the subscription policy. */
  public void startManualEventSubscriptions(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();
    for (int i = 0; services != null && i < services.length; i++)
    {
      if (!services[i].isSubscribed() && !services[i].isMulticastSubscribed())
      {
        services[i].sendSubscription();
      }
    }
  }

  /**
   * Ends the subscription for all services of a device if the subscription policy for the service is set to manual.
   */
  public void endManualEventSubscriptions(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();
    for (int i = 0; services != null && i < services.length; i++)
    {
      // end subscription if subscription is manual
      if (services[i].isSubscribed() && !automaticEventSubscription &&
        getSubscriptionPolicy(services[i]) == UPnPConstant.SUBSCRIPTION_MODE_MANUAL)
      {
        services[i].sendUnsubscription();
      }
    }
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
    // signal to entity
    if (entity != null)
    {
      entity.stateVariableChanged(stateVariable);
    }

    if (stateVariableListener != null)
    {
      stateVariableListener.stateVariableChanged(stateVariable);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action invocation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Invokes an action on the remote device. */
  public void invokeAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    // check if the control point uses personalization
    if (personalizationActive)
    {
      invokePersonalizedAction(personalizationSignatureAlgorithm, personalizationEncrypted, action);
    } else
    {
      basicControlPoint.invokeAction(action);
    }
  }

  /** Invokes an action on the remote device. */
  public void invokeUnpersonalizedAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    basicControlPoint.invokeAction(action);
  }

  /** Invokes a personalized action on the remote device. */
  public void invokePersonalizedAction(String keyType, boolean encrypt, CPAction action) throws InvokeActionException,
    ActionFailedException
  {
    CPService cpService = action.getCPService();
    CPDevice cpDevice = cpService.getCPDevice();
    CPService securedPersonalizationService =
      cpDevice.getCPServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);

    // personalization not possible
    if (securedPersonalizationService == null)
    {
      basicControlPoint.invokeAction(action);
      return;
    }
    // asymmetric personalization
    if (keyType.equals(PersonalizationDefaults.SIGNATURE_ASYMMETRIC))
    {
      invokePublicKeyPersonalizedAction(action);
      return;
    }
    // symmetric personalization
    invokeSymmetricKeyPersonalizedAction(encrypt, action);
  }

  /** Invokes a public key personalized action on the remote device. */
  public void invokePublicKeyPersonalizedAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    CPService cpService = action.getCPService();
    CPDevice cpDevice = cpService.getCPDevice();
    CPService securedPersonalizationService =
      cpDevice.getCPServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);
    RSAPublicKey publicKey = getPersonalizationPublicKey();
    RSAPrivateKey privateKey = getPersonalizationPrivateKey();

    // personalization not possible
    if (securedPersonalizationService == null)
    {
      basicControlPoint.invokeAction(action);
      return;
    }
    // asymmetric personalization
    System.out.println("Invoke RSA personalized action " + cpDevice.getFriendlyName() + "." +
      cpService.getShortenedServiceId() + "." + action.getName());
    // synchronize actions to prevent race conditions with nonces
    synchronized(cpDevice.getPersonalizationSyncLock())
    {
      String nonce = null;
      try
      {
        nonce = invokeGetNonce(securedPersonalizationService, publicKey);
      } catch (Exception e)
      {
        System.out.println("Error retrieving nonce for public key signed action: " + e.getMessage());
      }
      if (nonce == null)
      {
        throw new InvokeActionException("Error retrieving nonce for public key signed action");
      }
      basicControlPoint.invokePublicKeyPersonalizedAction(action, privateKey, publicKey, nonce);
    }
  }

  /** Invokes a symmetric key personalized action on the remote device. */
  public void invokeSymmetricKeyPersonalizedAction(boolean encrypt, CPAction action) throws InvokeActionException,
    ActionFailedException
  {
    CPService cpService = action.getCPService();
    CPDevice cpDevice = cpService.getCPDevice();
    CPService securedPersonalizationService =
      cpDevice.getCPServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);
    SymmetricKeyInfo symmetricKeyInfo = null;
    RSAPublicKey publicKey = getPersonalizationPublicKey();
    RSAPrivateKey privateKey = getPersonalizationPrivateKey();

    // personalization not possible
    if (securedPersonalizationService == null)
    {
      basicControlPoint.invokeAction(action);
      return;
    }
    // System.out.println("Invoke AES personalized action " + cpDevice.getFriendlyName() +
    // "." + cpService.getShortenedServiceId() + "." + action.getName());

    // prevent parallel requests to a session key
    synchronized(sessionKeyRequestLock)
    {
      symmetricKeyInfo = cpDevice.getPersonalizationSymmetricKeyInfo();

      // check if a new symmetric key must be exchanged
      if (symmetricKeyInfo == null)
      {
        // printMessage("Request AES key");

        CPAction getSessionKeyAction = securedPersonalizationService.getCPAction("GetSessionKey");
        try
        {
          String nonce = invokeGetNonce(securedPersonalizationService, publicKey);

          basicControlPoint.invokePublicKeyPersonalizedAction(getSessionKeyAction, privateKey, publicKey, nonce);

          byte[] aesKey = getSessionKeyAction.getOutArgument("SessionKey").getBinBase64Value();
          symmetricKeyInfo = SymmetricCryptographyHelper.decryptRSAEncryptedAESKey(privateKey, aesKey);

          // store received symmetric key in CPDevice
          cpDevice.setPersonalizationSymmetricKeyInfo(symmetricKeyInfo);

          // store additional key properties
          String keyID = getSessionKeyAction.getOutArgument("KeyID").getStringValue();
          symmetricKeyInfo.setKeyID(keyID);

          String sequenceBaseString = getSessionKeyAction.getOutArgument("SequenceBase").getStringValue();
          printMessage("AES key received");
          long sequenceBase = Long.parseLong(sequenceBaseString);
          symmetricKeyInfo.setSequence(sequenceBase);
        } catch (Exception e)
        {
          System.out.println("Error requesting session key: " + e.getMessage());
        }
      }
    }
    if (symmetricKeyInfo == null)
    {
      System.out.println("Could not request a symmetric key from " + cpDevice.getFriendlyName() +
        ".SecuredPersonalizationService");
      throw new InvokeActionException("symmetricKeyInfo is null");
      // error
    } else
    {
      // synchronize action requests to keep order for sequence
      synchronized(cpDevice.getPersonalizationSyncLock())
      {
        SecretKey secretKey = symmetricKeyInfo.getAESKey();
        // increment the current sequenceBase
        symmetricKeyInfo.incSequence();

        String sequenceBase = symmetricKeyInfo.getSequence() + "";
        String keyId = symmetricKeyInfo.getKeyID();

        basicControlPoint.invokeSymmetricKeyPersonalizedAction(encrypt,
          action,
          sequenceBase,
          keyId,
          secretKey,
          symmetricKeyInfo.getIV());
      }
    }
  }

  /**
   * Requests a nonce from a remote personalized device.
   * 
   * @throws Exception
   */
  public String invokeGetNonce(CPService securedPersonalizationService, RSAPublicKey publicKey) throws Exception
  {
    CPAction getNonceAction = securedPersonalizationService.getCPAction("GetNonce");
    if (getNonceAction != null)
    {
      Vector optionalHeaderLines = new Vector();
      // add public key of control point to header
      optionalHeaderLines.add(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY + CommonConstants.BLANK +
        SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey) + CommonConstants.NEW_LINE);

      basicControlPoint.invokeAction(getNonceAction, optionalHeaderLines);

      // System.out.println("Received nonce: " +
      // getNonceAction.getOutArgument("Nonce").getStringValue());
      return getNonceAction.getOutArgument("Nonce").getStringValue();
    }
    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if the device belongs to the same entity as the control point
   */
  protected boolean isEntityDevice(String udn)
  {
    // check if device belongs to the entity of this control point
    return getTemplateEntity() != null && getTemplateEntity().isEntityDevice(udn);
  }

  /**
   * Checks if a device with this udn is already in the list
   */
  public boolean isKnownCPDevice(String udn)
  {
    for (int i = 0; i < deviceList.size(); i++)
    {
      if (((CPDevice)deviceList.elementAt(i)).getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a device in the internal list
   */
  public CPDevice getCPDeviceByUDN(String udn)
  {
    for (int i = 0; i < deviceList.size(); i++)
    {
      if (((CPDevice)deviceList.elementAt(i)).getUDN().equals(udn))
      {
        return (CPDevice)deviceList.elementAt(i);
      }
    }
    return null;
  }

  /**
   * Returns the index for a device in the internal list
   */
  public int getCPDeviceIndexByUDN(String udn)
  {
    for (int i = 0; i < deviceList.size(); i++)
    {
      if (((CPDevice)deviceList.elementAt(i)).getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the device at the index
   */
  public CPDevice getCPDevice(int index)
  {
    if (index >= 0 && index < deviceList.size())
    {
      return (CPDevice)deviceList.elementAt(index);
    }
    return null;
  }

  /**
   * Returns the number of known devices
   */
  public int getCPDeviceCount()
  {
    return deviceList.size();
  }

  /** Returns a CPService by its shortened service ID. */
  public CPService getCPServiceByShortenedServiceID(String udn, String shortenedServiceID)
  {
    CPDevice device = getCPDeviceByUDN(udn);
    if (device != null)
    {
      return device.getCPServiceByShortenedID(shortenedServiceID);
    }

    return null;
  }

  /** Returns a CPAction by its name. */
  public CPAction getCPActionByName(String udn, String shortenedServiceID, String name)
  {
    CPService service = getCPServiceByShortenedServiceID(udn, shortenedServiceID);
    if (service != null)
    {
      return service.getCPAction(name);
    }

    return null;
  }

  /** Returns a CPStateVariable by its name. */
  public CPStateVariable getCPStateVariableByName(String udn, String shortenedServiceID, String name)
  {
    CPService service = getCPServiceByShortenedServiceID(udn, shortenedServiceID);
    if (service != null)
    {
      return service.getCPStateVariable(name);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.ISubscriptionPolicyListener#getSubscriptionPolicy(de.fhg.fokus.magic.upnp.control_point.CPService)
   */
  public int getSubscriptionPolicy(CPService service)
  {
    // try to handle with optional listener
    if (subscriptionPolicyListener != null)
    {
      return subscriptionPolicyListener.getSubscriptionPolicy(service);
    }

    // do not actively subscribe to any service
    return UPnPConstant.SUBSCRIPTION_MODE_MANUAL;
  }

  /**
   * Adds a new device class for observation.
   * 
   * @param deviceType
   *          Device type that should be observed
   */
  public void addSpecialDeviceClass(String deviceType)
  {
    // check if class is new
    if (!deviceListFromDeviceTypeHashtable.containsKey(deviceType))
    {
      Vector specialDeviceList = new Vector();
      // check known devices for the requested device type
      for (int i = 0; i < deviceList.size(); i++)
      {
        if (getCPDevice(i).getDeviceType().startsWith(deviceType))
        {
          specialDeviceList.add(getCPDevice(i));
        }
      }
      deviceListFromDeviceTypeHashtable.put(deviceType, specialDeviceList);
    }
  }

  /**
   * Removes a device class from observation.
   * 
   * @param deviceType
   *          Device type that should be observed no longer
   */
  public void removeSpecialDeviceClass(String deviceType)
  {
    // check if class is new
    if (deviceListFromDeviceTypeHashtable.containsKey(deviceType))
    {
      Vector specialDeviceList = (Vector)deviceListFromDeviceTypeHashtable.remove(deviceType);
      specialDeviceList.clear();
    }
  }

  /**
   * Retrieves a list of observed devices. This method is more efficient for previously registered device types.
   * 
   * @param deviceType
   *          Device type that should be retrieved
   * 
   * @return A list with all known devices of the requested type
   */
  public Vector getSpecialDeviceList(String deviceType)
  {
    // check if class is new
    if (deviceListFromDeviceTypeHashtable.containsKey(deviceType))
    {
      return (Vector)deviceListFromDeviceTypeHashtable.get(deviceType);
    }
    // class is not registered, go the long way
    Vector result = new Vector();
    // check known devices for the requested device type
    for (int i = 0; i < deviceList.size(); i++)
    {
      if (getCPDevice(i).getDeviceType().startsWith(deviceType))
      {
        result.add(getCPDevice(i));
      }
    }
    return result;
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
    if (networkInterfaceChangeListener != null)
    {
      networkInterfaceChangeListener.newInetAddress(networkInterface, inetAddress);
    }

    System.out.println("New inet address found: " + inetAddress.getHostAddress());
    ControlPointHostAddressSocketStructure socketStructure =
      basicControlPoint.tryAddHostAddressSocketStructure(networkInterface, inetAddress);

    if (socketStructure != null)
    {
      // send M-SEARCH messages via newly created socket structure
      String message = basicControlPoint.createSearchRootDeviceMessage();

      basicControlPoint.getSSDPMSearchClient().sendMessageToSocketStructureMulticast(message, socketStructure);
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
    if (networkInterfaceChangeListener != null)
    {
      networkInterfaceChangeListener.inetAddressGone(networkInterface, inetAddress);
    }

    Vector socketStructures = basicControlPoint.getSocketStructures();
    // remove associated socket structures
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure currentStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      if (currentStructure.getNetworkInterface().getName().equals(networkInterface.getName()) &&
        currentStructure.getHostAddress().equals(inetAddress))
      {
        basicControlPoint.removeHostAddressSocketStructure(currentStructure.getHostAddress());
      }
    }
  }

  /** Prints a message */
  public static void printMessage(String text)
  {
    System.out.println("    " + text);
  }

  /** Loads the name of the current user from a file. */
  public static String loadUserName(UPnPStartupConfiguration startupConfiguration,
    ControlPointStartupConfiguration controlPointStartupConfiguration)
  {
    String workingDirectory = startupConfiguration.getWorkingDirectory();
    String currentUserFileName =
      workingDirectory +
        FileHelper.getHostBasedFileName(controlPointStartupConfiguration.getProperty("CurrentUserFile", "current_user"));

    File currentUserFile = new File(currentUserFileName);
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(currentUserFile), "utf-8"));

      // user is in first line of file
      return reader.readLine();
    } catch (FileNotFoundException e)
    {
    } catch (IOException e)
    {
    }
    return null;
  }

  public String toString()
  {
    return name;
  }

  /**
   * Retrieves the networkInterfaceChangeListener.
   * 
   * @return The networkInterfaceChangeListener
   */
  public INetworkInterfaceChangeListener getNetworkInterfaceChangeListener()
  {
    return networkInterfaceChangeListener;
  }

  /**
   * Sets the networkInterfaceChangeListener.
   * 
   * @param networkInterfaceChangeListener
   *          The new value for networkInterfaceChangeListener
   */
  public void setNetworkInterfaceChangeListener(INetworkInterfaceChangeListener networkInterfaceChangeListener)
  {
    this.networkInterfaceChangeListener = networkInterfaceChangeListener;
  }

  /**
   * Sets the subscriptionPolicyListener.
   * 
   * @param subscriptionPolicyListener
   *          The new value for subscriptionPolicyListener
   */
  public void setSubscriptionPolicyListener(ISubscriptionPolicyListener subscriptionPolicyListener)
  {
    this.subscriptionPolicyListener = subscriptionPolicyListener;
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
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration
   */
  public UPnPStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves the user.
   * 
   * @return The user
   */
  public String getPersonalizationUser()
  {
    return personalizationUser;
  }

  /** Loads the private/public key of the current user from a file or creates a new key. */
  public void setPersonalizationUser(String userName)
  {
    this.personalizationUser = userName;

    // default name
    if (personalizationUser == null || personalizationUser.length() == 0)
    {
      personalizationUser = UPnPConstant.USER_UNKNOWN;
    }

    // use always a new key pair for the anonymous user
    if (personalizationUser.equals(UPnPConstant.USER_UNKNOWN))
    {
      // update personalization key
      personalizationKeyPair = PersistentRSAKeyPair.getTemporaryKeyPair();
    } else
    {
      // update personalization key
      personalizationKeyPair =
        PersistentRSAKeyPair.getPersistentKeyPair(startupConfiguration.getWorkingDirectory() + personalizationUser +
          "_keys.txt");
    }
    // remove symmetric keys for last user
    for (int i = 0; i < getCPDeviceCount(); i++)
    {
      CPDevice device = getCPDevice(i);
      // remove symmetric key info from each remote device
      if (device.getPersonalizationSymmetricKeyInfo() != null)
      {
        device.setPersonalizationSymmetricKeyInfo(null);
      }
    }
    System.out.println();
    printMessage(toString() + ": User is " + personalizationUser + " with personalized securityID " +
      DigestHelper.rsaPublicKeyToSecurityID(getPersonalizationPublicKey()));
  }

  /** Tries to personalize devices with a SimplePersonalizationService. */
  public void tryPersonalizeDevice(CPDevice device)
  {
    CPService simplePersonalizationService =
      device.getCPServiceByType(DeviceConstant.SIMPLE_PERSONALIZATION_SERVICE_TYPE);
    if (simplePersonalizationService != null)
    {
      try
      {
        CPAction setUser = simplePersonalizationService.getCPAction("SetUser");
        setUser.getInArgument("User").setValue(personalizationUser);

        invokeAction(setUser);
      } catch (Exception e)
      {
        System.out.println("Error updating profile in device " + device.getFriendlyName() + ":" + e.getMessage());
      }
    }
  }

  /** Returns the public key of this control point */
  public RSAPublicKey getPersonalizationPublicKey()
  {
    return (RSAPublicKey)personalizationKeyPair.getPublic();
  }

  /** Returns the private key of this control point */
  public RSAPrivateKey getPersonalizationPrivateKey()
  {
    return (RSAPrivateKey)personalizationKeyPair.getPrivate();
  }

  /** Triggers a search for root devices. */
  public void searchRootDevices()
  {
    basicControlPoint.sendDelayedSearchRootDeviceMessage();
  }

  /** Called when the control point is terminated */
  public void terminate()
  {
    basicControlPoint.getControlPointEventThread().unregister(networkInterfaceManager);
    basicControlPoint.getControlPointEventThread().unregister(memoryMonitor);
    // end subscription for all devices
    for (int i = 0; i < deviceList.size(); i++)
    {
      CPDevice currentDevice = (CPDevice)deviceList.elementAt(i);
      // end subscription for all services in the device
      CPService[] services = currentDevice.getCPServiceTable();
      if (services != null)
      {
        for (int j = 0; j < services.length; j++)
        {
          if (services[j].isSubscribed())
          {
            services[j].sendUnsubscription();
          }
        }
      }
    }
    basicControlPoint.terminate();
  }
}

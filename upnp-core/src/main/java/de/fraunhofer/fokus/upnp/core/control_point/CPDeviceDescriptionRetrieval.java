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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.ActionDescriptionHandler;
import de.fraunhofer.fokus.upnp.core.xml.AllowedValueRangeHandler;
import de.fraunhofer.fokus.upnp.core.xml.ArgumentHandler;
import de.fraunhofer.fokus.upnp.core.xml.DeviceDescriptionHandler;
import de.fraunhofer.fokus.upnp.core.xml.DeviceDescriptionServiceHandler;
import de.fraunhofer.fokus.upnp.core.xml.IconHandler;
import de.fraunhofer.fokus.upnp.core.xml.RootDeviceDescriptionHandler;
import de.fraunhofer.fokus.upnp.core.xml.ServiceDescriptionHandler;
import de.fraunhofer.fokus.upnp.core.xml.StateVariableHandler;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessageFlow;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageFlow;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is responsible for retrieving device and service descriptions.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class CPDeviceDescriptionRetrieval extends Thread implements IHTTPMessageFlow
{

  public static int                              ACTIVE_THREADS  = 0;

  /** UPnP logger */
  protected static Logger                        logger          = Logger.getLogger("upnp.desc");

  /** URL to the device description */
  private URL                                    deviceDescriptionURL;

  private String                                 server;

  private int                                    maxage;

  private String                                 NLS;

  private String                                 descriptionHashBase64;

  private String                                 uuid;

  private int                                    IPVersion;

  private ControlPointHostAddressSocketStructure discoverySocketStructure;

  protected ControlPoint                         controlPoint;

  protected HTTPClient                           httpClient;

  /** Path to cached descriptions */
  private String                                 deviceCacheDirectory;

  private CPDeviceDiscoveryInfo                  cpDeviceDiscoveryInfo;

  private boolean                                terminateThread = false;

  /**
   * Creates a new instance of CPDeviceDescriptionRetrieval.
   * 
   * @param controlPoint
   *          Associated control point
   * @param socketStructure
   *          Socket structure that received the discovery message
   * @param deviceDescriptionURL
   *          URL where to find device description
   * @param server
   *          server
   * @param maxage
   *          maxage
   * @param NLS
   *          network location signature
   * @param IPVersion
   *          Used IP version (4 or 6)
   */
  public CPDeviceDescriptionRetrieval(ControlPoint controlPoint,
    ControlPointHostAddressSocketStructure socketStructure,
    URL deviceDescriptionURL,
    String uuid,
    String server,
    int maxage,
    String NLS,
    int IPVersion)
  {
    this.setName("CPDeviceDescriptionRetrieval [" + deviceDescriptionURL.toString() + "]");
    this.controlPoint = controlPoint;
    this.discoverySocketStructure = socketStructure;
    this.deviceDescriptionURL = deviceDescriptionURL;
    this.uuid = uuid;
    this.server = server;
    this.maxage = maxage;
    this.NLS = NLS;
    this.IPVersion = IPVersion;

    // add to description retrieval list
    controlPoint.addDescriptionRetrievalThread(this);
  }

  /**
   * Creates a new instance of CPDeviceDescriptionRetrieval for cached device descriptions.
   * 
   * @param controlPoint
   *          Associated control point
   * @param deviceCacheDirectory
   *          Path to cached descriptions
   * @param cpDeviceDiscoveryInfo
   *          Discovery info
   */
  public CPDeviceDescriptionRetrieval(ControlPoint controlPoint,
    String deviceCacheDirectory,
    CPDeviceDiscoveryInfo cpDeviceDiscoveryInfo,
    String descriptionHashBase64)
  {
    super("CPDeviceDescriptionRetrieval [" + deviceCacheDirectory + "]");
    this.controlPoint = controlPoint;
    this.deviceCacheDirectory = deviceCacheDirectory;
    this.deviceDescriptionURL = cpDeviceDiscoveryInfo.getLocation();
    this.uuid = cpDeviceDiscoveryInfo.getRootDeviceUUID();
    this.server = cpDeviceDiscoveryInfo.getServer();
    this.maxage = cpDeviceDiscoveryInfo.getMaxage();
    this.NLS = cpDeviceDiscoveryInfo.getNLS();
    this.IPVersion = 4;
    this.descriptionHashBase64 = descriptionHashBase64;

    // use preferred socket structure for cached devices
    // this should be changed to a more sophisticated solution
    this.discoverySocketStructure = controlPoint.getPreferredSocketStructure();

    // add to description retrieval list
    controlPoint.addDescriptionRetrievalThread(this);
  }

  /**
   * Creates a new instance of CPDeviceDescriptionRetrieval for remote device descriptions.
   * 
   * @param controlPoint
   *          Associated control point
   * @param cpDeviceDiscoveryInfo
   *          Discovery info
   */
  public CPDeviceDescriptionRetrieval(ControlPoint controlPoint, CPDeviceDiscoveryInfo cpDeviceDiscoveryInfo)
  {
    super("CPDeviceDescriptionRetrieval [" + cpDeviceDiscoveryInfo.getLocation() + "]");
    this.controlPoint = controlPoint;
    this.cpDeviceDiscoveryInfo = cpDeviceDiscoveryInfo;

    this.deviceDescriptionURL = cpDeviceDiscoveryInfo.getLocation();
    this.maxage = cpDeviceDiscoveryInfo.getMaxage();
    this.server = cpDeviceDiscoveryInfo.getServer();
    this.NLS = cpDeviceDiscoveryInfo.getNLS();
    this.IPVersion = 4;
    // use preferred socket structure for remote devices
    this.discoverySocketStructure = controlPoint.getPreferredSocketStructure();

    // add to description retrieval list
    controlPoint.addDescriptionRetrievalThread(this);
  }

  /**
   * Creates a CPDevice from the parsed device description.
   * 
   * @param deviceHandler
   *          Parser for device description
   * @param urlBase
   *          device URLbase
   * 
   * @return The new CPDevice
   */
  private CPDevice createCPDevice(DeviceDescriptionHandler deviceHandler, String urlBase)
  {
    CPDevice device =
      new CPDevice(deviceHandler.getDeviceType(),
        deviceHandler.getFriendlyName(),
        deviceHandler.getManufacturer(),
        deviceHandler.getModelName(),
        deviceHandler.getUDN(),
        deviceHandler.isRootDevice(),
        IPVersion,
        controlPoint,
        discoverySocketStructure);

    // try to update root discovery info for remote devices that were not discovered via multicast
    if (deviceHandler.isRootDevice() && cpDeviceDiscoveryInfo != null &&
      cpDeviceDiscoveryInfo.getRootDeviceUUID() == null)
    {
      synchronized(controlPoint.getDeviceInfoLock())
      {
        String uuid = deviceHandler.getUDN();

        // add root device info to discovery info
        cpDeviceDiscoveryInfo.processDiscoveryInfo(UPnPConstant.UPNP_ROOTDEVICE, uuid + "::" +
          UPnPConstant.UPNP_ROOTDEVICE);

        // add URL for UUID to hashtable
        if (!controlPoint.getDeviceDescriptionURLFromUUIDTable().containsKey(uuid))
        {
          controlPoint.getDeviceDescriptionURLFromUUIDTable().put(uuid, deviceDescriptionURL);
        } else
        {
          TemplateControlPoint.printMessage(controlPoint.toString() + ": Failed to add URL " + deviceDescriptionURL +
            " for UUID " + uuid);
        }
      }
    }
    try
    {
      // if (urlBase != null)
      // {
      // device.setURLBase(new URL(urlBase));
      // }
      device.setManufacturerURL(trySetURL(deviceHandler.getManufacturerURL()));
      device.setModelDescription(deviceHandler.getModelDescription());
      device.setModelNumber(deviceHandler.getModelNumber());
      device.setModelURL(trySetURL(deviceHandler.getModelURL()));
      device.setSerialNumber(deviceHandler.getSerialNumber());
      device.setUPC(deviceHandler.getUPC());
      device.setPresentationURL(trySetAbsoluteURLString(deviceHandler.getPresentationURL(), urlBase));
      device.setCacheable(deviceHandler.isCacheable());

      if (deviceHandler.getIconHandlerList() != null)
      {
        device.setIconList(createIcons(deviceHandler.getIconHandlerList(), urlBase));
      }

      if (deviceHandler.getEmbeddedDeviceHandlerList() != null)
      {
        device.setDeviceTable(createEmbeddedCPDevices(deviceHandler.getEmbeddedDeviceHandlerList(), urlBase));
      }

      if (deviceHandler.getServiceHandlerList() != null)
      {
        CPService[] services = createCPServices(deviceHandler.getServiceHandlerList(), urlBase, device);

        if (services != null)
        {
          device.setCPServiceTable(services);
        } else
        {
          System.err.println("No Services found for Device: " + device.toString());
          return null;
        }
      } else
      {
        logger.warn("no serviceList found for URL = " + deviceDescriptionURL);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      logger.warn(e.getMessage());
    }

    return device;
  }

  /**
   * Creates a list of embedded devices.
   * 
   * @param deviceList
   *          Vector containing the parsers for each embedded device
   * @param urlBase
   *          device URLbase
   * 
   * @return Array with embedded devices or null
   */
  private CPDevice[] createEmbeddedCPDevices(Vector deviceList, String urlBase)
  {
    CPDevice[] embDevs = new CPDevice[deviceList.size()];

    for (int i = 0; i < deviceList.size(); i++)
    {
      DeviceDescriptionHandler embDevice = (DeviceDescriptionHandler)deviceList.elementAt(i);
      embDevs[i] = createCPDevice(embDevice, urlBase);

      if (embDevs[i] == null)
      {
        System.err.println("embedded device is null!");
      } else
      {
        embDevs[i].setDeviceDescriptionURL(deviceDescriptionURL);
        embDevs[i].setMaxage(maxage);
        embDevs[i].setServer(server);
        embDevs[i].setRootDevice(false);
      }
    }
    return embDevs;
  }

  /**
   * Creates a list of device icons.
   * 
   * @param iconHandlerList
   *          Parser for icon description
   * @param urlBase
   *          base URL
   * 
   * @return Array with device icons or null.
   */
  private DeviceIcon[] createIcons(Vector iconHandlerList, String urlBase)
  {
    DeviceIcon[] deviceIcons = new DeviceIcon[iconHandlerList.size()];

    for (int i = 0; i < iconHandlerList.size(); i++)
    {
      IconHandler iconHandler = (IconHandler)iconHandlerList.get(i);
      // force absolute URLs
      String iconURL = trySetAbsoluteURLString(iconHandler.getUrl(), urlBase);

      deviceIcons[i] =
        new DeviceIcon(iconHandler.getMimetype(),
          Integer.parseInt(iconHandler.getWidth()),
          Integer.parseInt(iconHandler.getHeight()),
          Integer.parseInt(iconHandler.getDepth()),
          iconURL);
    }

    return deviceIcons;
  }

  /**
   * Creates device services.
   * 
   * @param serviceHandlerList
   *          services
   * @param urlBase
   *          device URLbase
   * @return device services
   */
  private CPService[] createCPServices(Vector serviceHandlerList, String urlBase, CPDevice device)
  {
    CPService[] services = new CPService[serviceHandlerList.size()];

    // go through all services found in the device description
    for (int i = 0; i < serviceHandlerList.size(); i++)
    {
      DeviceDescriptionServiceHandler deviceDescriptionServiceHandler =
        (DeviceDescriptionServiceHandler)serviceHandlerList.elementAt(i);

      try
      {
        URL serviceDescriptionURL = trySetAbsoluteURL(deviceDescriptionServiceHandler.getSCPDURL(), urlBase);

        String serviceDescription = getServiceDescription(serviceDescriptionURL);

        ServiceDescriptionHandler serviceDescriptionHandler = new ServiceDescriptionHandler();
        serviceDescriptionHandler.parse(serviceDescription);

        services[i] =
          createCPService(serviceDescriptionHandler,
            deviceDescriptionServiceHandler,
            urlBase,
            serviceDescription,
            device);
      } catch (Exception e)
      {
        logger.warn("error during service description retrieval from " + deviceDescriptionServiceHandler.getSCPDURL());
        logger.warn("reason: " + e.getMessage());
        System.out.println("Error during service description retrieval from " +
          deviceDescriptionServiceHandler.getSCPDURL() + ":" + e.getMessage());
        return null;
      }
    }

    return services;
  }

  /**
   * Creates a service representation.
   * 
   * @param serviceDescriptionHandler
   *          Handler that has parsed the service description
   * @param deviceDescriptionServiceHandler
   *          Handler that has parsed the service part of the device description
   * @param urlBase
   *          device URLbase
   * @param serviceDescription
   *          Received service description
   * @param device
   *          Associated CPDevice
   * 
   * @return service object
   */
  private CPService createCPService(ServiceDescriptionHandler serviceDescriptionHandler,
    DeviceDescriptionServiceHandler deviceDescriptionServiceHandler,
    String urlBase,
    String serviceDescription,
    CPDevice device)
  {
    URL scpdURL = trySetAbsoluteURL(deviceDescriptionServiceHandler.getSCPDURL(), urlBase);
    URL controlURL = trySetAbsoluteURL(deviceDescriptionServiceHandler.getControlURL(), urlBase);
    URL eventSubscriptionURL = trySetAbsoluteURL(deviceDescriptionServiceHandler.getEventSubURL(), urlBase);

    if (scpdURL == null || controlURL == null || eventSubscriptionURL == null)
    {
      logger.warn("Found invalid URL in service description");
      return null;
    }
    try
    {
      CPService service =
        new CPService(deviceDescriptionServiceHandler.getServiceType(),
          deviceDescriptionServiceHandler.getServiceID(),
          scpdURL,
          controlURL,
          eventSubscriptionURL,
          IPVersion,
          device);

      // store state variable table
      service.setCPStateVariableTable(createCPStateVariables(serviceDescriptionHandler.getStateVariableHandlerList(),
        service));
      // store action table if present
      if (serviceDescriptionHandler.getActionHandlerList() != null)
      {
        service.setCPActionTable(createCPActions(serviceDescriptionHandler.getActionHandlerList(), service));
      }
      // store service description
      service.setServiceDescription(serviceDescription);

      // check for proprietary multicast event address
      if (deviceDescriptionServiceHandler.getMulticastEventAddress() != null)
      {
        try
        {
          InetSocketAddress multicastEventAddress =
            IPHelper.toSocketAddress(deviceDescriptionServiceHandler.getMulticastEventAddress());

          if (multicastEventAddress != null)
          {
            service.setMulticastEventSocketAddress(multicastEventAddress);
          }
        } catch (Exception e)
        {
          System.out.println("Error parsing multicast event address: " + e.getMessage());
        }
      }

      return service;
    } catch (Exception e)
    {
      logger.warn("cannot create service.");
      logger.warn("reason: " + e.getMessage());

      return null;
    }
  }

  /**
   * Creates the state variable for a remote service.
   * 
   * @param stateVariableHandlers
   *          List with state variable parsers
   * @param service
   *          Associated service
   * 
   * @return state variable objects
   */
  private CPStateVariable[] createCPStateVariables(Vector stateVariableHandlers, CPService service)
  {
    try
    {
      CPStateVariable[] stateVariables = new CPStateVariable[stateVariableHandlers.size()];

      for (int i = 0; i < stateVariableHandlers.size(); i++)
      {
        StateVariableHandler stateVariableHandler = (StateVariableHandler)stateVariableHandlers.elementAt(i);

        if (stateVariableHandler.getDefaultValue() != null)
        {
          stateVariables[i] =
            new CPStateVariable(stateVariableHandler.getName(),
              stateVariableHandler.getDataType(),
              stateVariableHandler.getDefaultValue(),
              stateVariableHandler.getSendEvents(),
              service);
        } else
        {
          logger.debug("Creating StateVariable without default Value" + stateVariableHandler.getName());
          stateVariables[i] =
            new CPStateVariable(stateVariableHandler.getName(),
              stateVariableHandler.getDataType(),
              stateVariableHandler.getSendEvents(),
              service);
        }

        if (!stateVariableHandler.getAllowedValueList().isEmpty())
        {
          String[] s = createAllowedValueList(stateVariableHandler.getAllowedValueList());
          stateVariables[i].setAllowedValueList(s);
        } else if (stateVariableHandler.getAllowedValueRangeHandler() != null)
        {
          AllowedValueRangeHandler allValueRange = stateVariableHandler.getAllowedValueRangeHandler();
          String minimum = allValueRange.getMinimum();
          String maximum = allValueRange.getMaximum();
          String step = allValueRange.getStep();
          AllowedValueRange servAllValue =
            new AllowedValueRange(minimum.equalsIgnoreCase("MIN") ? -Float.MIN_VALUE : Float.valueOf(minimum)
              .floatValue(),
              maximum.equalsIgnoreCase("MAX") ? Float.MAX_VALUE : Float.valueOf(maximum).floatValue(),
              step != null ? Float.valueOf(step).floatValue() : 0);
          stateVariables[i].setAllowedValueRange(servAllValue);
        }
      }

      return stateVariables;
    } catch (Exception e)
    {
      e.printStackTrace();
      logger.warn("cannot create state variable.");
      logger.warn("reason: " + e.getMessage());
    }

    return null;
  }

  /**
   * Creates a list of allowed value
   * 
   * @param allValueList
   *          special derived instance from JAXB-class for a AllowedValueListType
   * @return a list of allowed value
   */
  private String[] createAllowedValueList(Vector allValueList)
  {
    String[] servAllValue = new String[allValueList.size()];

    for (int j = 0; j < allValueList.size(); j++)
    {
      servAllValue[j] = (String)allValueList.elementAt(j);

      // logger.info(servAllValue[j]);
    }

    return servAllValue;
  }

  /**
   * Creates the actions for a remote service.
   * 
   * @param actionHandlerList
   *          List with action parsers
   * @param service
   *          Associated service
   * 
   * @return service's actions
   */
  private CPAction[] createCPActions(Vector actionHandlerList, CPService service)
  {
    CPAction[] actions = new CPAction[actionHandlerList.size()];

    for (int i = 0; i < actionHandlerList.size(); i++)
    {
      ActionDescriptionHandler action = (ActionDescriptionHandler)actionHandlerList.get(i);
      actions[i] = new CPAction(action.getName(), service);
      // actions[i].setCPService(service);

      // create arguments for each action
      if (!action.getArgumentHandlerList().isEmpty())
      {
        Vector argumentList = action.getArgumentHandlerList();
        Argument[] servArgument = new Argument[argumentList.size()];

        for (int j = 0; j < argumentList.size(); j++)
        {
          ArgumentHandler argument = (ArgumentHandler)argumentList.get(j);

          servArgument[j] =
            new Argument(argument.getName(),
              argument.getDirection(),
              service.getCPStateVariable(argument.getRelatedStateVariableName()));
        }

        actions[i].setArgumentTable(servArgument);
      }
    }

    return actions;
  }

  /** Removes this thread from the list of pending retrievals. */
  private void removeFromDescriptionRetrievalList()
  {
    controlPoint.removeDescriptionRetrievalThread(this);
  }

  /** Creates an absolute URL with the device description URL. */
  private String createAbsoluteURLFromDeviceDescriptionURL(String urlPath)
  {
    if (!urlPath.startsWith("/"))
    {
      urlPath = "/" + urlPath;
    }
    return "http://" + deviceDescriptionURL.getHost() + ":" + deviceDescriptionURL.getPort() + urlPath;
  }

  /** Adds a base URL to a relative URL. */
  private String addURLBase(String path, String URLBase)
  {
    if (URLBase.endsWith("/"))
    {
      URLBase = URLBase.substring(0, URLBase.length() - 1);
    }

    if (!path.startsWith("/"))
    {
      path = "/" + path;
    }

    return URLBase + path;
  }

  /** Tries to convert a string to an URL. */
  private URL trySetURL(String url)
  {
    if (url == null)
    {
      return null;
    }
    try
    {
      return new URL(url);
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Tries to create an absolute URL. */
  private URL trySetAbsoluteURL(String url, String baseURL)
  {
    if (url == null)
    {
      return null;
    }

    if (!url.startsWith("http"))
    {
      if (baseURL != null)
      {
        url = addURLBase(url, baseURL);
      } else
      {
        url = createAbsoluteURLFromDeviceDescriptionURL(url);
      }
    }
    try
    {
      return new URL(url);
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Tries to create an absolute URL. */
  private String trySetAbsoluteURLString(String url, String baseURL)
  {
    if (url == null)
    {
      return null;
    }

    if (!url.toLowerCase().startsWith("http"))
    {
      if (baseURL != null)
      {
        url = addURLBase(url, baseURL);
      } else
      {
        url = createAbsoluteURLFromDeviceDescriptionURL(url);
      }
    }
    return url;
  }

  /** Retrieves a cached description. */
  private String getCachedDescription(URL descriptionURL)
  {
    if (deviceCacheDirectory != null)
    {
      String escapedDescriptionFileName = StringHelper.escapeDirectoryName(descriptionURL.getPath());
      try
      {
        FileInputStream descriptionFileInputStream =
          new FileInputStream(deviceCacheDirectory + escapedDescriptionFileName);

        // should be enough for most descriptions
        ByteArrayOutputStream descriptionOutputStream = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int length = descriptionFileInputStream.read(buffer);
        while (length != -1)
        {
          descriptionOutputStream.write(buffer, 0, length);
          length = descriptionFileInputStream.read(buffer);
        }
        // all descriptions are UTF-8 encoded
        return StringHelper.byteArrayToUTF8String(descriptionOutputStream.toByteArray());
      } catch (Exception e)
      {
      }
    }
    return null;
  }

  /** Retrieves the device description for this device. */
  private String getDeviceDescription()
  {
    if (deviceCacheDirectory != null)
    {
      return getCachedDescription(deviceDescriptionURL);
    } else
    {
      logger.info("start retrieving device description from " + deviceDescriptionURL + " .....");
      TemplateControlPoint.printMessage(controlPoint.toString() + ": Retrieve DEVICE DESCRIPTION from " +
        deviceDescriptionURL);

      String deviceDescription = getDescription(deviceDescriptionURL);

      if (deviceDescription != null)
      {
        TemplateControlPoint.printMessage(controlPoint.toString() + ": Success  DEVICE DESCRIPTION from " +
          deviceDescriptionURL + " (" + deviceDescription.length() + " decoded bytes)");
      }

      return deviceDescription;
    }
  }

  /** Retrieves a service description for this device. */
  private String getServiceDescription(URL serviceDescriptionURL)
  {
    boolean canUseCache = deviceCacheDirectory != null;
    // if size must be checked, set verify to false
    boolean verifiedServiceDescription = !CPDeviceCache.SERVICE_DESCRIPTION_SIZE_CHECKING;
    if (canUseCache && !verifiedServiceDescription)
    {
      // check size of service description
      String absoluteDescriptionFileName =
        deviceCacheDirectory + StringHelper.escapeDirectoryName(serviceDescriptionURL.getPath());
      verifiedServiceDescription =
        CPDeviceCache.getCachedDescriptionState(absoluteDescriptionFileName, serviceDescriptionURL, null) == CPDeviceCache.CACHED_DESCRIPTION_VALID;
    }
    if (canUseCache && verifiedServiceDescription)
    {
      return getCachedDescription(serviceDescriptionURL);
    } else
    {
      // System.out.println(" Retrieve SERVICE DESCRIPTION from " + serviceDescriptionURL);
      return getDescription(serviceDescriptionURL);
    }
  }

  /**
   * Retrieves a description via HTTP. May be overriden by descendant classes to implement
   * additional funtionality.
   */
  protected String getDescription(URL descriptionURL)
  {
    Object result = HTTPMessageFlow.sendMessageAndProcessResponse(null, descriptionURL, this);
    if (!(result instanceof String))
    {
      return null;
    }
    return (String)result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createRequest(java.lang.Object, java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageType, URL descriptionURL)
  {
    // System.out.println("Create request to " + descriptionURL.toExternalForm());
    HTTPMessageObject descriptionRequest =
      new HTTPMessageObject(HTTPMessageBuilder.createGETRequest(URLHelper.getURLPath(descriptionURL),
        descriptionURL.getHost(),
        descriptionURL.getPort(),
        ""), descriptionURL);

    return descriptionRequest;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#modifyRequest(java.util.Hashtable, java.net.URL,
   *      de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public void modifyRequest(Hashtable messageOptions, URL descriptionURL, HTTPMessageObject request)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createResponseParser(java.util.Hashtable,
   *      java.net.URL)
   */
  public HTTPParser createResponseParser(Hashtable messageOptions, URL targetURL)
  {
    return new HTTPParser();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#processResponse(java.util.Hashtable,
   *      java.net.URL, de.fhg.fokus.magic.util.network.HTTPMessageObject,
   *      de.fhg.fokus.magic.http.HTTPParser)
   */
  public Object processResponse(Hashtable messageOptions,
    URL descriptionURL,
    HTTPMessageObject response,
    HTTPParser responseParser)
  {
    // descriptions are UTF-8 encoded
    String responseBody = response.getBodyAsUTF8String();
    if (responseBody == null || responseBody.length() == 0)
    {
      System.out.println("Response body is empty for request to " + descriptionURL.toExternalForm());
      return null;
    }
    if (response.getBody().length != responseBody.length())
    {
      // System.out.println("CAUTION: Description contains UTF-8 characters");
    }
    // removing trailing \r\n etc.
    if (responseBody.lastIndexOf(">") < responseBody.length() - 1)
    {
      // System.out.println("Remove trailing chars from " + descriptionURL.toExternalForm());
      responseBody = responseBody.substring(0, responseBody.lastIndexOf(">") + 1);
    }
    // store description hash if found in response header
    if (responseParser.hasField(HTTPConstant.ETAG_HEADER))
    {
      descriptionHashBase64 = responseParser.getValue(HTTPConstant.ETAG_HEADER).replaceAll("\"", "");
    }
    return responseBody;
  }

  /** Event that a new root device is available. */
  protected void newRootDevice(CPDevice device)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#toString()
   */
  public String toString()
  {
    return "CPDeviceDescriptionRetrieval (" + deviceDescriptionURL + ")";
  }

  /**
   * Retrieves device description and service description
   */
  public void run()
  {
    // exit immediately if terminated from the outside before the actual retrieval has started
    // this is done to prevent unstarted threads which are not collected by the
    // garbage collector
    if (terminateThread)
    {
      removeFromDescriptionRetrievalList();
      return;
    }
    try
    {
      String deviceDescription = getDeviceDescription();
      if (deviceDescription == null)
      {
        throw new IllegalArgumentException("Could not retrieve device description.");
      }

      // start parsing the device description
      RootDeviceDescriptionHandler root = new RootDeviceDescriptionHandler();
      try
      {
        root.parse(deviceDescription);
      } catch (Exception e)
      {
        throw new IllegalArgumentException("Response body is not valid XML.");
      }

      CPDevice device = createCPDevice(root.getRootDevice(), root.getURLBase());

      if (device != null)
      {
        device.setDeviceDescriptionURL(deviceDescriptionURL);
        device.setDeviceDescription(deviceDescription);
        device.setMaxage(maxage);
        device.setServer(server);
        device.setNLS(NLS);
        device.setDescriptionHashBase64(descriptionHashBase64);
        // store device in control point hashtable
        controlPoint.getCPDeviceFromDescriptionURLTable().put(deviceDescriptionURL, device);

        // store friendly name in discovery info
        CPDeviceDiscoveryInfo discoveryInfo =
          (CPDeviceDiscoveryInfo)controlPoint.getDeviceDiscoveryInfoFromUUIDTable().get(device.getUDN());
        if (discoveryInfo != null && !discoveryInfo.getFriendlyNameList().contains(device.getFriendlyName()))
        {
          discoveryInfo.getFriendlyNameList().add(device.getFriendlyName());
        }

        // do not re-cache devices that were read from the cache
        boolean cacheDeviceDescription = deviceCacheDirectory == null;
        boolean updateSSDPInfo = true;
        // do not cache devices with cacheable property set to false
        if (!device.isCacheable())
        {
          cacheDeviceDescription = false;
          updateSSDPInfo = false;
        }

        // store flag for caching in device
        device.setCachedDevice(!cacheDeviceDescription);

        // call method to allow handling in descendant classes
        newRootDevice(device);

        // announce event in associated control point
        controlPoint.newRootDevice(device, cacheDeviceDescription, updateSSDPInfo);
      }
    } catch (Exception e)
    {
      logger.warn("error during device description retrieval from " + deviceDescriptionURL);
      logger.warn("reason: " + e.getMessage());

      System.out.println("Could not request device description from " + deviceDescriptionURL);

      synchronized(controlPoint.getDeviceInfoLock())
      {
        if (uuid != null)
        {
          controlPoint.getDeviceDescriptionURLFromUUIDTable().remove(uuid);
        }

        if (deviceDescriptionURL != null)
        {
          CPDeviceDiscoveryInfo deviceDiscoveryInfo =
            (CPDeviceDiscoveryInfo)controlPoint.getDiscoveryInfoFromDescriptionURLTable().get(deviceDescriptionURL);

          if (deviceDiscoveryInfo != null)
          {
            System.out.println("Mark device with URL " + deviceDescriptionURL + " as invalid");
            deviceDiscoveryInfo.setInvalidDevice(true);
          }
        }
      }
      removeFromDescriptionRetrievalList();
      return;
    }
    logger.info("successfully retrieved description from " + deviceDescriptionURL);
    removeFromDescriptionRetrievalList();
  }

  /** Terminates the description retrieval */
  public void terminate()
  {
    // start and exit immediately if needed
    if (!this.isAlive())
    {
      System.out.println("Exit pending device description thread immediately");
      terminateThread = true;
      start();
    }
    if (httpClient != null)
    {
      httpClient.terminate();
    }
    removeFromDescriptionRetrievalList();
  }

}

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
package de.fraunhofer.fokus.upnp.core.xml;

import java.io.File;
import java.net.InetSocketAddress;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.startup.StartupConfiguration;

/**
 * This class includes all parameters that are needed by an UPnP device, control point or gateway to get started.
 * 
 * @author Alexander Koenig
 * 
 */
public class UPnPStartupConfiguration extends StartupConfiguration
{

  /** Tag for flag to automatically subscribe to services (optional, default = false) */
  public static final String AUTOMATIC_EVENT_SUBSCRIPTION   = "AutomaticEventSubscription";

  /** Tag for device type */
  public static final String DEVICE_TYPE                    = "DeviceType";

  /** Tag for flag to disable the device cache (optional, default = false) */
  public static final String DISABLE_DEVICE_CACHE           = "DisableDeviceCache";

  /** Tag for flag to disable the event processing (optional, default = false) */
  public static final String DISABLE_EVENT_PROCESSING       = "DisableEventProcessing";

  /** Tag for flag to disable the metadata retrieval (optional, default = false) */
  public static final String DISABLE_METADATA_RETRIEVAL     = "DisableMetadataRetrieval";

  /** Tag for flag to stick to UPnP discovery (optional, default = false) */
  public static final String DISCOVERY_ONLY                 = "DiscoveryOnly";

  /** Tag for event HTTP port used by control point (optional, default = random) */
  public static final String EVENT_CALLBACK_SERVER_PORT     = "EventCallbackServerPort";

  /** Tag for event UDP port used by control point (optional, default = inactive) */
  public static final String EVENT_CALLBACK_UDP_SERVER_PORT = "EventCallbackUDPServerPort";

  /** Tag for HTTP port used by gateway (optional, default = random) */
  public static final String GATEWAY_SERVER_PORT            = "GatewayServerPort";

  /** Tag for HTTP over UDP port used by gateway (optional, default = random) */
  public static final String GATEWAY_UDP_SERVER_PORT        = "GatewayUDPServerPort";

  /** Tag for HTTP port used by device (optional, default = random) */
  public static final String HTTP_SERVER_PORT               = "HTTPServerPort";

  /** Tag for ignored device IP addresses */
  public static final String IGNORE_DEVICE_IP_ADDRESSES     = "IgnoreDeviceIPAddresses";

  /** Tag for manufacturer */
  public static final String MANUFACTURER                   = "Manufacturer";

  /** Tag for model name */
  public static final String MODEL_NAME                     = "ModelName";

  /** Tag for event multicast port used by devices (optional, default = inactive) */
  public static final String MULTICAST_EVENT_SERVER_PORT    = "MulticastEventServerPort";

  /**
   * 
   */
  private static final long  serialVersionUID               = 1L;

  /** Tag for used multicast address (optional, default = 239.255.255.250) */
  public static final String SSDP_MULTICAST_ADDRESS         = "SSDPMulticastAddress";

  /** Tag for used multicast port (optional, default = 1900) */
  public static final String SSDP_MULTICAST_PORT            = "SSDPMulticastPort";

  /** Tag for SSDP port used for M-SEARCH response messages (optional, default = random) */
  public static final String SSDP_UNICAST_PORT              = "SSDPUnicastPort";

  /** Tag for device UUID */
  public static final String UDN_TAG                        = "UDN";

  /** Tag for path to resources (optional) */
  public static final String WEB_SERVER_DIRECTORY           = "WebServerDirectory";

  protected String           ssdpMulticastAddress;

  protected int              ssdpMulticastPort;

  /**
   * Creates a new instance of UPnPStartupConfiguration using the class loader.
   * 
   * @param classInstance
   *          The class loader of that instance is used
   * @param resourceName
   *          The relative resource name that contains the startup info
   * 
   */
  public UPnPStartupConfiguration(Class classInstance, String resourceName)
  {
    super(classInstance, resourceName);
  }

  /**
   * Creates a new instance of UPnPStartupConfiguration.
   * 
   * @param fileName
   *          The name of the file that contains the startup info
   * 
   */
  public UPnPStartupConfiguration(File file)
  {
    super(file);
  }

  /**
   * Creates a new instance of UPnPStartupConfiguration.java
   * 
   * @param fileName
   */
  public UPnPStartupConfiguration(String fileName)
  {
    super(fileName);
  }

  /**
   * Creates a new instance of UPnPStartupConfiguration.
   * 
   * 
   */
  public UPnPStartupConfiguration(UPnPStartupConfiguration parentHandler)
  {
    super(parentHandler);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.startup.AbstractStartupConfiguration#setupVariables()
   */
  public void setupVariables()
  {
    ssdpMulticastAddress = SSDPConstant.SSDPMulticastAddress;
    ssdpMulticastPort = SSDPConstant.SSDPMulticastPort;
  }

  /**
   * Retrieves the ssdpMulticastAddress.
   * 
   * @return The ssdpMulticastAddress.
   */
  public String getSSDPMulticastAddress()
  {
    return ssdpMulticastAddress;
  }

  /**
   * Retrieves the ssdpMulticastPort.
   * 
   * @return The ssdpMulticastPort.
   */
  public int getSSDPMulticastPort()
  {
    return ssdpMulticastPort;
  }

  /**
   * Retrieves the ssdpMulticastSocketAddress.
   * 
   * @return The ssdpMulticastSocketAddress.
   */
  public InetSocketAddress getSSDPMulticastSocketAddress()
  {
    try
    {
      return new InetSocketAddress(ssdpMulticastAddress, ssdpMulticastPort);
    } catch (Exception e)
    {
    }
    return SSDPConstant.getSSDPMulticastSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    super.processContentElement(content);

    content = content.trim();
    if (getTagCount() == 2)
    {
      if (getCurrentTag().equals(UPnPStartupConfiguration.SSDP_MULTICAST_PORT))
      {
        ssdpMulticastPort = StringHelper.stringToIntegerDef(content, ssdpMulticastPort);
      }
      if (getCurrentTag().equals(UPnPStartupConfiguration.SSDP_MULTICAST_ADDRESS))
      {
        ssdpMulticastAddress = content;
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equals("Device"))
    {
      DeviceStartupConfiguration deviceStartupConfiguration = new DeviceStartupConfiguration(this);
      redirectSAXEvents(deviceStartupConfiguration, uri, name, qName, atts);
    }
    if (getCurrentTag().equals("ControlPoint"))
    {
      ControlPointStartupConfiguration controlPointStartupConfiguration = new ControlPointStartupConfiguration(this);
      redirectSAXEvents(controlPointStartupConfiguration, uri, name, qName, atts);
    }
    if (getCurrentTag().equals("Gateway"))
    {
      GatewayStartupConfiguration gatewayStartupConfiguration = new GatewayStartupConfiguration(this);
      redirectSAXEvents(gatewayStartupConfiguration, uri, name, qName, atts);
    }
  }

  /**
   * Sets the ssdpMulticastAddress.
   * 
   * @param ssdpMulticastAddress
   *          The ssdpMulticastAddress to set.
   */
  public void setSSDPMulticastAddress(String ssdpMulticastAddress)
  {
    this.ssdpMulticastAddress = ssdpMulticastAddress;
  }

  /**
   * Sets the ssdpMulticastPort.
   * 
   * @param ssdpMulticastPort
   *          The ssdpMulticastPort to set.
   */
  public void setSSDPMulticastPort(int ssdpMulticastPort)
  {
    this.ssdpMulticastPort = ssdpMulticastPort;
  }

}

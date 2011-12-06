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

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.startup.ChildStartupConfiguration;

/**
 * This class includes all parameters that are needed by an UPnP device to get started.
 * 
 * @author Alexander Koenig
 * 
 */
public class ControlPointStartupConfiguration extends ChildStartupConfiguration
{
  /**
   * 
   */
  private static final long serialVersionUID           = 1L;

  private boolean           useFixedPorts              = false;

  private int               ssdpUnicastPort            = -1;

  private int               eventCallbackServerPort    = -1;

  private int               eventCallbackUDPServerPort = -1;

  private int               multicastEventServerPort   = -1;

  private boolean           disableDeviceCache         = UPnPDefaults.CP_DISABLE_DEVICE_CACHE;

  private boolean           discoveryOnly              = UPnPDefaults.CP_DISCOVERY_ONLY;

  private boolean           disableMetadataRetrieval   = UPnPDefaults.CP_DISABLE_METADATA_RETRIEVAL;

  private boolean           disableEventProcessing     = UPnPDefaults.CP_DISABLE_EVENT_PROCESSING;

  private boolean           runDelayed                 = false;

  private boolean           automaticEventSubscription = false;

  private String            ignoredDeviceAddresses     = "";

  /**
   * Creates a new instance of ControlPointStartupConfiguration.
   * 
   * @param parentHandler
   *          The parent startup configuration
   * 
   */
  public ControlPointStartupConfiguration(UPnPStartupConfiguration parentHandler)
  {
    super(parentHandler);
    this.parentHandler = parentHandler;
  }

  /**
   * Retrieves the eventCallbackServerPort.
   * 
   * @return The eventCallbackServerPort.
   */
  public int getEventCallbackServerPort()
  {
    return eventCallbackServerPort;
  }

  /**
   * Retrieves the eventCallbackUDPServerPort.
   * 
   * @return The eventCallbackUDPServerPort.
   */
  public int getEventCallbackUDPServerPort()
  {
    return eventCallbackUDPServerPort;
  }

  /**
   * Sets the new value for eventCallbackUDPServerPort.
   * 
   * @param eventCallbackUDPServerPort
   *          The new value for eventCallbackUDPServerPort
   */
  public void setEventCallbackUDPServerPort(int eventCallbackUDPServerPort)
  {
    this.eventCallbackUDPServerPort = eventCallbackUDPServerPort;
  }

  /**
   * Retrieves the multicastEventServerPort.
   * 
   * @return The multicastEventServerPort
   */
  public int getMulticastEventServerPort()
  {
    return multicastEventServerPort;
  }

  /**
   * Sets the eventCallbackServerPort.
   * 
   * @param eventCallbackServerPort
   *          The eventCallbackServerPort to set.
   */
  public void setEventCallbackServerPort(int httpServerPort)
  {
    this.eventCallbackServerPort = httpServerPort;
  }

  /**
   * Retrieves the ssdpUnicastPort.
   * 
   * @return The ssdpUnicastPort.
   */
  public int getSSDPUnicastPort()
  {
    return ssdpUnicastPort;
  }

  /**
   * Sets the ssdpUnicastPort.
   * 
   * @param ssdpUnicastPort
   *          The ssdpUnicastPort to set.
   */
  public void setSSDPUnicastPort(int ssdpUnicastPort)
  {
    this.ssdpUnicastPort = ssdpUnicastPort;
  }

  /**
   * Retrieves the useFixedPorts.
   * 
   * @return The useFixedPorts.
   */
  public boolean useFixedPorts()
  {
    return useFixedPorts;
  }

  /**
   * Sets the useFixedPorts.
   * 
   * @param useFixedPorts
   *          The useFixedPorts to set.
   */
  public void setUseFixedPorts(boolean useFixedPorts)
  {
    this.useFixedPorts = useFixedPorts;
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
      if (getCurrentTag().equals(UPnPStartupConfiguration.EVENT_CALLBACK_SERVER_PORT))
      {
        eventCallbackServerPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.EVENT_CALLBACK_UDP_SERVER_PORT))
      {
        eventCallbackUDPServerPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.MULTICAST_EVENT_SERVER_PORT))
      {
        multicastEventServerPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.SSDP_UNICAST_PORT))
      {
        ssdpUnicastPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.DISABLE_DEVICE_CACHE))
      {
        disableDeviceCache = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.DISABLE_METADATA_RETRIEVAL))
      {
        disableMetadataRetrieval = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.DISABLE_EVENT_PROCESSING))
      {
        disableEventProcessing = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.DISCOVERY_ONLY))
      {
        discoveryOnly = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.AUTOMATIC_EVENT_SUBSCRIPTION))
      {
        automaticEventSubscription = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.IGNORE_DEVICE_IP_ADDRESSES))
      {
        ignoredDeviceAddresses = content;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    super.processEndElement(uri, localName, qName);
    // set fixed ports state
    if (getTagCount() == 1)
    {
      useFixedPorts = ssdpUnicastPort != -1 && eventCallbackServerPort != -1;
      // store the parsed description
      parentHandler.addControlPointStartupInfo(this);
      parentHandler.handleSAXEvents();
    }
  }

  /**
   * Checks if this description is valid for devices.
   * 
   * @return The valid.
   */
  public boolean isValidControlPointConfiguration()
  {
    return friendlyName.length() > 0;
  }

  /**
   * Retrieves the disableDeviceCache flag.
   * 
   * @return The disableDeviceCache
   */
  public boolean isSetDisableDeviceCache()
  {
    return disableDeviceCache;
  }

  /**
   * Retrieves the disableMetadataRetrieval flag.
   * 
   * @return The disableMetadataRetrieval
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
   * Retrieves the discoveryOnly flag.
   * 
   * @return The discoveryOnly
   */
  public boolean isSetDiscoveryOnly()
  {
    return discoveryOnly;
  }

  /**
   * Retrieves the runDelayed.
   * 
   * @return The runDelayed.
   */
  public boolean runDelayed()
  {
    return runDelayed;
  }

  /**
   * Sets the runDelayed
   * 
   * @param runDelayed
   *          The runDelayed to set.
   */
  public void setRunDelayed(boolean runDelayed)
  {
    this.runDelayed = runDelayed;
  }

  /**
   * Retrieves the automaticEventSubscription.
   * 
   * @return The automaticEventSubscription
   */
  public boolean automaticEventSubscription()
  {
    return automaticEventSubscription;
  }

  /**
   * Sets the automaticEventSubscription.
   * 
   * @param automaticEventSubscription
   *          The new value for automaticEventSubscription
   */
  public void setAutomaticEventSubscription(boolean automaticEventSubscription)
  {
    this.automaticEventSubscription = automaticEventSubscription;
  }

  /**
   * Retrieves the value of ignoredDeviceAddresses.
   * 
   * @return The value of ignoredDeviceAddresses
   */
  public String getIgnoredDeviceAddresses()
  {
    return ignoredDeviceAddresses;
  }

}

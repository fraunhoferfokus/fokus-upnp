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

import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.startup.ChildStartupConfiguration;

/**
 * This class includes all parameters that are needed by an UPnP device to get started.
 * 
 * @author Alexander Koenig
 * 
 */
public class DeviceStartupConfiguration extends ChildStartupConfiguration
{
  /**
   * 
   */
  private static final long serialVersionUID       = 1L;

  private Vector            webServerDirectoryList = new Vector();

  private String            deviceType             = "";

  private String            manufacturer           = "Fraunhofer FOKUS";

  private String            modelName              = "Device";

  private String            UDN                    = "";

  private boolean           useFixedPorts          = false;

  private int               ssdpUnicastPort        = -1;

  private int               httpServerPort         = -1;

  private boolean           runDelayed             = false;

  /**
   * Creates a new instance of DeviceStartupConfiguration.
   * 
   * @param parentHandler
   *          The parent startup configuration
   * 
   */
  public DeviceStartupConfiguration(UPnPStartupConfiguration parentHandler)
  {
    super(parentHandler);
    this.parentHandler = parentHandler;
  }

  /**
   * Retrieves the deviceType.
   * 
   * @return The deviceType.
   */
  public String getDeviceType()
  {
    return deviceType;
  }

  /**
   * Sets the deviceType.
   * 
   * @param deviceType
   *          The deviceType to set.
   */
  public void setDeviceType(String deviceType)
  {
    this.deviceType = deviceType;
  }

  /**
   * Retrieves the httpServerPort.
   * 
   * @return The httpServerPort.
   */
  public int getHTTPServerPort()
  {
    return httpServerPort;
  }

  /**
   * Sets the httpServerPort.
   * 
   * @param httpServerPort
   *          The httpServerPort to set.
   */
  public void setHTTPServerPort(int httpServerPort)
  {
    this.httpServerPort = httpServerPort;
  }

  /**
   * Retrieves the manufacturer.
   * 
   * @return The manufacturer.
   */
  public String getManufacturer()
  {
    return manufacturer;
  }

  /**
   * Sets the manufacturer.
   * 
   * @param manufacturer
   *          The manufacturer to set.
   */
  public void setManufacturer(String manufacturer)
  {
    this.manufacturer = manufacturer;
  }

  /**
   * Retrieves the modelName.
   * 
   * @return The modelName.
   */
  public String getModelName()
  {
    return modelName;
  }

  /**
   * Sets the modelName.
   * 
   * @param modelName
   *          The modelName to set.
   */
  public void setModelName(String modelName)
  {
    this.modelName = modelName;
  }

  /**
   * Sets the webServerDirectory.
   * 
   * @param webServerDirectory
   *          The webServerDirectory to set.
   */
  public void addWebServerDirectory(String webServerDirectory)
  {
    webServerDirectoryList.add(FileHelper.toValidDirectoryName(webServerDirectory));
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
   * Retrieves the uDN.
   * 
   * @return The uDN.
   */
  public String getUDN()
  {
    return UDN;
  }

  /**
   * Sets the uDN.
   * 
   * @param udn
   *          The uDN to set.
   */
  public void setUDN(String udn)
  {
    UDN = udn;
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
      if (getCurrentTag().equals(UPnPStartupConfiguration.DEVICE_TYPE))
      {
        deviceType = content;
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.MANUFACTURER))
      {
        manufacturer = content;
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.MODEL_NAME))
      {
        modelName = content;
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.UDN_TAG))
      {
        UDN = content;
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.WEB_SERVER_DIRECTORY))
      {
        addWebServerDirectory(content);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.HTTP_SERVER_PORT))
      {
        httpServerPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.SSDP_UNICAST_PORT))
      {
        ssdpUnicastPort = StringHelper.stringToIntegerDef(content, -1);
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
      useFixedPorts = ssdpUnicastPort != -1 && httpServerPort != -1;
      // store the parsed description
      parentHandler.addDeviceStartupInfo(this);
      parentHandler.handleSAXEvents();
    }
  }

  /**
   * Checks if this description is valid for devices.
   * 
   * @return The valid.
   */
  public boolean isValidDeviceConfiguration()
  {
    return UDN.length() > 0 && friendlyName.length() > 0 && deviceType.length() > 0;
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
   * Retrieves the webServerDirectoryList.
   * 
   * @return The webServerDirectoryList
   */
  public Vector getWebServerDirectoryList()
  {
    return webServerDirectoryList;
  }

}

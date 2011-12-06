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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.XMLConstant;

/**
 * This class parses the device part of a device description.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceDescriptionHandler extends SAXTemplateHandler
{

  private boolean                      isRootDevice              = false;

  /** Parent parser */
  private DeviceDescriptionHandler     parentDeviceHandler;

  /** Handler for root device */
  private RootDeviceDescriptionHandler rootDeviceHandler;

  // device information
  private String                       deviceType                = null;

  private String                       friendlyName              = null;

  private String                       manufacturer              = null;

  private String                       manufacturerURL           = null;

  private String                       modelDescription          = null;

  private String                       modelName                 = null;

  private String                       modelNumber               = null;

  private String                       modelURL                  = null;

  private String                       serialNumber              = null;

  private String                       UDN                       = null;

  private String                       UPC                       = null;

  private String                       presentationURL           = null;

  private boolean                      cacheable                 = true;

  private Vector                       iconHandlerList           = new Vector();

  private Vector                       serviceHandlerList        = new Vector();

  private Vector                       embeddedDeviceHandlerList = new Vector();

  /** Creates a new handler for an embedded device. */
  public DeviceDescriptionHandler(DeviceDescriptionHandler parentDeviceHandler)
  {
    super(parentDeviceHandler.getSAXParser());
    this.parentDeviceHandler = parentDeviceHandler;
    isRootDevice = false;
  }

  /** Creates a new handler for the root device. */
  public DeviceDescriptionHandler(RootDeviceDescriptionHandler rootHandler)
  {
    super(rootHandler.getSAXParser());
    this.rootDeviceHandler = rootHandler;
    isRootDevice = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    // start new parser classes for different segments of the description
    if (getCurrentTag().equals(XMLConstant.ICON_TAG))
    {
      IconHandler iconHandler = new IconHandler(this);
      redirectSAXEvents(iconHandler, uri, name, qName, atts);
    }
    if (getCurrentTag().equals(XMLConstant.SERVICE_TAG))
    {
      DeviceDescriptionServiceHandler serviceHandler = new DeviceDescriptionServiceHandler(this);
      redirectSAXEvents(serviceHandler, uri, name, qName, atts);
    }
    // handle embedded devices
    if (getTagCount() == 3 && getCurrentTag().equals(XMLConstant.DEVICE_TAG))
    {
      DeviceDescriptionHandler deviceHandler = new DeviceDescriptionHandler(this);
      redirectSAXEvents(deviceHandler, uri, name, qName, atts);
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
    if (getCurrentTag().equals(XMLConstant.DEVICE_TAG))
    {
      if (!isRootDevice)
      {
        parentDeviceHandler.addEmbeddedDevice(this);
        parentDeviceHandler.handleSAXEvents();
      }
      if (isRootDevice)
      {
        rootDeviceHandler.setRootDevice(this);
        rootDeviceHandler.handleSAXEvents();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    // checks for single line tags
    if (getCurrentTag().equals(XMLConstant.DEVICETYPE_TAG))
    {
      deviceType = content;
    }

    if (getCurrentTag().equals(XMLConstant.FRIENDLYNAME_TAG))
    {
      friendlyName = content;
    }

    if (getCurrentTag().equals(XMLConstant.MANUFACTURER_TAG))
    {
      manufacturer = content;
    }

    if (getCurrentTag().equals(XMLConstant.MANUFACTURER_URL_TAG))
    {
      manufacturerURL = content;
    }

    if (getCurrentTag().equals(XMLConstant.MODEL_DESCRIPTION_TAG))
    {
      modelDescription = content;
    }

    if (getCurrentTag().equals(XMLConstant.MODEL_NAME_TAG))
    {
      modelName = content;
    }

    if (getCurrentTag().equals(XMLConstant.MODEL_NUMBER_TAG))
    {
      modelNumber = content;
    }

    if (getCurrentTag().equals(XMLConstant.MODEL_URL_TAG))
    {
      modelURL = content;
    }

    if (getCurrentTag().equals(XMLConstant.SERIALNUMBER_TAG))
    {
      serialNumber = content;
    }

    if (getCurrentTag().equals(XMLConstant.UDN_TAG))
    {
      UDN = content;
    }

    if (getCurrentTag().equals(XMLConstant.UPC_TAG))
    {
      UPC = content;
    }

    if (getCurrentTag().equals(XMLConstant.PRESENTATIONURL_TAG))
    {
      presentationURL = content;
    }
    if (getCurrentTag().equals(XMLConstant.CACHEABLE_TAG))
    {
      cacheable = StringHelper.stringToBoolean(content);
    }
  }

  public boolean isRootDevice()
  {
    return isRootDevice;
  }

  // CHECK AND STORE METHODS
  public void addIconHandler(IconHandler iconHandler)
  {
    iconHandlerList.add(iconHandler);
  }

  public void addServiceHandler(DeviceDescriptionServiceHandler serviceHandler)
  {
    serviceHandlerList.add(serviceHandler);
  }

  public void addEmbeddedDevice(DeviceDescriptionHandler device)
  {
    embeddedDeviceHandlerList.add(device);
  }

  public String getDeviceType()
  {
    return deviceType;
  }

  public String getFriendlyName()
  {
    return friendlyName;
  }

  public String getManufacturer()
  {
    return manufacturer;
  }

  public String getManufacturerURL()
  {
    return manufacturerURL;
  }

  public String getModelDescription()
  {
    return modelDescription;
  }

  public String getModelName()
  {
    return modelName;
  }

  public String getModelNumber()
  {
    return modelNumber;
  }

  public String getModelURL()
  {
    return modelURL;
  }

  public String getSerialNumber()
  {
    return serialNumber;
  }

  public String getUDN()
  {
    return UDN;
  }

  public String getUPC()
  {
    return UPC;
  }

  public String getPresentationURL()
  {
    return presentationURL;
  }

  /**
   * Retrieves the cacheable flag.
   * 
   * @return The cacheable flag
   */
  public boolean isCacheable()
  {
    return cacheable;
  }

  /** Retrieves a vector with handlers for all device icons */
  public Vector getIconHandlerList()
  {
    return iconHandlerList;
  }

  /** Retrieves a vector with handlers for all device services */
  public Vector getServiceHandlerList()
  {
    return serviceHandlerList;
  }

  /** Retrieves a vector with handlers for all embedded devices */
  public Vector getEmbeddedDeviceHandlerList()
  {
    return embeddedDeviceHandlerList;
  }
}

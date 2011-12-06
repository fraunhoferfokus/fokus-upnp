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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.XMLConstant;

/**
 * This class parses the service part of device descriptions.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceDescriptionServiceHandler extends SAXTemplateHandler
{

  private DeviceDescriptionHandler deviceHandler;

  // initialise for checkService method
  private String                   serviceType           = null;

  private String                   serviceID             = null;

  private String                   SCPDURL               = null;

  private String                   controlURL            = null;

  private String                   eventSubURL           = null;

  private boolean                  eventSubURLfound      = false;

  private String                   multicastEventAddress = null;

  public DeviceDescriptionServiceHandler(DeviceDescriptionHandler deviceHandler)
  {
    super(deviceHandler.getSAXParser());
    this.deviceHandler = deviceHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String localName, String qName, Attributes attributes)
  {
    if (getCurrentTag().equals(XMLConstant.EVENTSUBURL_TAG))
    {
      eventSubURLfound = true;
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
    if (getCurrentTag().equals(XMLConstant.SERVICE_TAG))
    {
      tryAddService();
      deviceHandler.handleSAXEvents();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    String elementValue = content.trim();
    if (getCurrentTag().equals(XMLConstant.SERVICETYPE_TAG))
    {
      serviceType = elementValue;
    }

    if (getCurrentTag().equals(XMLConstant.SERVICEID_TAG))
    {
      serviceID = elementValue;
    }

    if (getCurrentTag().equals(XMLConstant.SCPDURL_TAG))
    {
      SCPDURL = elementValue;
    }

    if (getCurrentTag().equals(XMLConstant.CONTROLURL_TAG))
    {
      controlURL = elementValue;
    }

    if (getCurrentTag().equals(XMLConstant.EVENTSUBURL_TAG))
    {
      eventSubURL = elementValue;
    }
    if (getCurrentTag().equals(XMLConstant.MULTICAST_EVENT_ADDRESS_TAG))
    {
      multicastEventAddress = elementValue;
    }
  }

  /** Tries to add the service to the device */
  private void tryAddService() throws SAXException
  {
    if (serviceType != null && serviceID != null && SCPDURL != null && controlURL != null &&
      (eventSubURL != null || eventSubURL == null && eventSubURLfound))
    {
      // if a device has a service with no evented variables, the tag for the eventSubURL must be
      // present, but
      // no value ist needed. To avoid an error the value ist set from null to ""
      if (eventSubURL == null && eventSubURLfound)
      {
        eventSubURL = "";
      }
      deviceHandler.addServiceHandler(this);
    } else
    {
      System.out.println("SCPDURL=" + SCPDURL);
      System.out.println("eventSub=" + eventSubURL);
      System.out.println("eventSubURLfound=" + eventSubURLfound);
      System.out.println("controlURL=" + controlURL);
      throw new SAXException("Service URLS incomplete");
    }
  }

  public String getServiceType()
  {
    return serviceType;
  }

  public String getServiceID()
  {
    return serviceID;
  }

  public String getSCPDURL()
  {
    return SCPDURL;
  }

  public String getControlURL()
  {
    return controlURL;
  }

  public String getEventSubURL()
  {
    return eventSubURL;
  }

  /**
   * Retrieves the multicastEventAddress.
   * 
   * @return The multicastEventAddress
   */
  public String getMulticastEventAddress()
  {
    return multicastEventAddress;
  }
}

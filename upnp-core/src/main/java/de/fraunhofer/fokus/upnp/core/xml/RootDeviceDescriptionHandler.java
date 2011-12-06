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
 * This is the root parser for device descriptions.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class RootDeviceDescriptionHandler extends SAXTemplateHandler
{

  private boolean                  specVersionFound = false;

  private String                   URLBase;

  private DeviceDescriptionHandler rootDevice;

  private boolean                  major_exist      = false;

  private boolean                  minor_exist      = false;

  /**
   * Creates a new instance of RootDeviceHandler.
   * 
   */
  public RootDeviceDescriptionHandler()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equals(XMLConstant.SPECVERSION_TAG))
    {
      specVersionFound = true;
    }
    // redirect to sub handler
    if (getCurrentTag().equals(XMLConstant.DEVICE_TAG))
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
    if (getCurrentTag().equals(XMLConstant.SPECVERSION_TAG))
    {
      checkMajorMinor();
      specVersionFound = false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (specVersionFound)
    {
      checkSpecVersion(content);
    }

    if (getCurrentTag().equals(XMLConstant.URLBASE_TAG))
    {
      URLBase = content;
    }
  }

  // CHECK AND STORE METHODS
  private void checkSpecVersion(String elementValue) throws SAXException
  {
    if (getCurrentTag().equals(XMLConstant.MAJOR_TAG))
    {
      major_exist = true;

      if (!elementValue.equals("1"))
      {
        throw new SAXException("MAJOR not 1, elementValue " + elementValue);
      }
    } else if (getCurrentTag().equals(XMLConstant.MINOR_TAG))
    {
      minor_exist = true;

      if (!elementValue.equals("0") && !elementValue.equals("1"))
      {
        throw new SAXException("MINOR neither 0 nor 1, elementValue:" + elementValue + ":");
      }
    }
  }

  private void checkMajorMinor() throws SAXException
  {
    if (!(major_exist && minor_exist))
    {
      throw new SAXException("MAJOR AND MINOR TAG not found");
    }
  }

  public void setRootDevice(DeviceDescriptionHandler device)
  {
    rootDevice = device;
  }

  public String getURLBase()
  {
    return URLBase;
  }

  public DeviceDescriptionHandler getRootDevice()
  {
    return rootDevice;
  }
}

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

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.XMLConstant;

/**
 * This class parses icon descriptions.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class IconHandler extends SAXTemplateHandler
{

  private DeviceDescriptionHandler deviceHandler;

  // initialise for checkIcon method
  private String                   mimetype = null;

  private String                   width    = null;

  private String                   height   = null;

  private String                   depth    = null;

  private String                   url      = null;

  public IconHandler(DeviceDescriptionHandler deviceHandler)
  {
    super(deviceHandler.getSAXParser());
    this.deviceHandler = deviceHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getCurrentTag().equals(XMLConstant.ICON_TAG))
    {
      tryAddIcon();
      deviceHandler.handleSAXEvents();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getCurrentTag().equals(XMLConstant.MIMETYPE_TAG))
    {
      mimetype = content;
    }

    if (getCurrentTag().equals(XMLConstant.WIDTH_TAG))
    {
      width = content;
    }

    if (getCurrentTag().equals(XMLConstant.HEIGHT_TAG))
    {
      height = content;
    }

    if (getCurrentTag().equals(XMLConstant.DEPTH_TAG))
    {
      depth = content;
    }

    if (getCurrentTag().equals(XMLConstant.URL_TAG))
    {
      url = content;
    }
  }

  private void tryAddIcon() throws SAXException
  {
    if (mimetype != null && width != null && height != null && depth != null && url != null)
    {
      deviceHandler.addIconHandler(this);
    }
    // we simply ignore incomplete or wrong items
  }

  /**
   * Retrieves the depth.
   * 
   * @return The depth
   */
  public String getDepth()
  {
    return depth;
  }

  /**
   * Retrieves the height.
   * 
   * @return The height
   */
  public String getHeight()
  {
    return height;
  }

  /**
   * Retrieves the mimetype.
   * 
   * @return The mimetype
   */
  public String getMimetype()
  {
    return mimetype;
  }

  /**
   * Retrieves the url.
   * 
   * @return The url
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Retrieves the width.
   * 
   * @return The width
   */
  public String getWidth()
  {
    return width;
  }
}

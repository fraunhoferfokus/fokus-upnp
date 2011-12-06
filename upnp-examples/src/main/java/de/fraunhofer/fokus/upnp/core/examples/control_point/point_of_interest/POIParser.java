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
package de.fraunhofer.fokus.upnp.core.examples.control_point.point_of_interest;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class is used to parse POI XML messages.
 * 
 * @author Alexander Koenig
 * 
 */
public class POIParser extends SAXTemplateHandler
{
  private final String ITEM_TAG    = "Item";

  private Vector       messageList = new Vector();

  private String       genre;

  private String       title;

  private String       description;

  private String       infoURL;

  private String       imageURL;

  private Float        longitude;

  private Float        latitude;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2 && qName.equalsIgnoreCase(ITEM_TAG))
    {
      genre = null;
      title = null;
      description = null;
      infoURL = null;
      imageURL = null;
      longitude = null;
      latitude = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getTagCount() == 2 && qName.equalsIgnoreCase(ITEM_TAG) && genre != null && title != null && longitude != null &&
      latitude != null)
    {
      messageList.add(new POIMessageEntry(genre,
        title,
        description,
        infoURL,
        imageURL,
        longitude.floatValue(),
        latitude.floatValue()));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getTagCount() == 3)
    {
      if (getCurrentTag().equalsIgnoreCase("Genre"))
      {
        genre = content;
      }
      if (getCurrentTag().equalsIgnoreCase("Title"))
      {
        title = content;
      }
      if (getCurrentTag().equalsIgnoreCase("Description"))
      {
        description = content;
      }
      if (getCurrentTag().equalsIgnoreCase("InfoURL"))
      {
        infoURL = content;
      }
      if (getCurrentTag().equalsIgnoreCase("ImageURL"))
      {
        imageURL = content;
      }
      if (getCurrentTag().equalsIgnoreCase("Description"))
      {
        description = content;
      }
    }
    if (getTagCount() == 4 && getTag(2).equalsIgnoreCase("Position"))
    {
      try
      {
        if (getCurrentTag().equalsIgnoreCase("Latitude"))
        {
          latitude = new Float(content);
        }
        if (getCurrentTag().equalsIgnoreCase("Longitude"))
        {
          longitude = new Float(content);
        }
      } catch (Exception ex)
      {
      }
    }
  }

  /**
   * Retrieves the messageList.
   * 
   * @return The messageList
   */
  public Vector getMessageList()
  {
    return messageList;
  }

}

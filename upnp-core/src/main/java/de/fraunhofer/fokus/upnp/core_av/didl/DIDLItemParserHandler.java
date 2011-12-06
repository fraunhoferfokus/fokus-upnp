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
package de.fraunhofer.fokus.upnp.core_av.didl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class parses a DIDLItem.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DIDLItemParserHandler extends SAXTemplateHandler
{

  private SAXTemplateHandler       parentHandler;

  private IDIDLObjectParentHandler objectParentHandler;

  private DIDLItem                 itemObject;

  /**
   * Creates a new instance of DIDLItemParserHandler
   * 
   * @param parser
   *          The associated parser.
   */
  public DIDLItemParserHandler(SAXTemplateHandler parentHandler, IDIDLObjectParentHandler objectParentHandler)
  {
    super(parentHandler.getSAXParser());

    this.objectParentHandler = objectParentHandler;
    this.parentHandler = parentHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equals(DIDLConstants.ITEM))
    {
      boolean handled;
      itemObject = new DIDLItem();

      for (int i = 0; i < atts.getLength(); ++i)
      {
        handled = itemObject.handleAttribute(atts.getQName(i), atts.getValue(i));
        // add unhandled attributes
        if (!handled)
        {
          itemObject.addAttribute(atts.getQName(i), atts.getValue(i));
        }
      }
    }
    // parse resource in extra handler
    else if (getCurrentTag().equals(DIDLConstants.RES))
    {
      DIDLResourceParserHandler resourceHandler = new DIDLResourceParserHandler(this, itemObject);
      redirectSAXEvents(resourceHandler, uri, name, qName, atts);
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
    // item was parsed completely
    if (getCurrentTag().equals(DIDLConstants.ITEM))
    {
      // add new object to parent
      objectParentHandler.addDIDLItem(itemObject);

      parentHandler.handleSAXEvents();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    String elementValue = content.trim();
    if (elementValue.length() >= 0)
    {
      boolean handled = itemObject.handleTags(getCurrentTag(), elementValue);

      if (!handled && getCurrentTag().equals(DIDLConstants.UPNP_CLASS))
      {
        // as soon as the class is known, try to create an object appropriate for this class
        // this could be changed to dynamic class loading

        // class specific tags that were already read are automatically copied to the appropriate
        // tags (via fillClassSpecificData())
        if (elementValue.equals(DIDLConstants.UPNP_CLASS_AUDIO_ITEM))
        {
          itemObject = new DIDLAudioItem(itemObject);
        }
        if (elementValue.equals(DIDLConstants.UPNP_CLASS_IMAGE_ITEM))
        {
          itemObject = new DIDLImageItem(itemObject);
        }
        if (elementValue.equals(DIDLConstants.UPNP_CLASS_MUSIC_TRACK))
        {
          itemObject = new DIDLMusicTrack(itemObject);
        }
        if (elementValue.equals(DIDLConstants.UPNP_CLASS_PHOTO))
        {
          itemObject = new DIDLPhoto(itemObject);
        }

        // set class if no specific subclass was found
        itemObject.setObjectClass(elementValue);
        handled = true;
      }
      // unknown elements are packed into a generic property vector
      if (!handled)
      {
        itemObject.addProperty(getCurrentTag(), elementValue);
      }
    }
  }

}

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
public class DIDLContainerParserHandler extends SAXTemplateHandler
{

  private SAXTemplateHandler       parentHandler;

  private IDIDLObjectParentHandler objectParentHandler;

  private DIDLContainer            containerObject;

  private DIDLSearchClass          searchClassObject;

  private DIDLCreateClass          createClassObject;

  /**
   * Creates a new instance of DIDLItemParserHandler
   * 
   * @param parser
   *          The associated parser.
   */
  public DIDLContainerParserHandler(SAXTemplateHandler parentHandler, IDIDLObjectParentHandler objectParentHandler)
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
    // handle container data
    if (getCurrentTag().equals(DIDLConstants.CONTAINER))
    {
      boolean handled;
      containerObject = new DIDLContainer();

      for (int i = 0; i < atts.getLength(); ++i)
      {
        handled = containerObject.handleAttribute(atts.getQName(i), atts.getValue(i));
        // add unhandled attributes
        if (!handled)
        {
          containerObject.addAttribute(atts.getQName(i), atts.getValue(i));
        }
      }
    }
    // handle search class
    else if (getCurrentTag().equals(DIDLConstants.UPNP_SEARCHCLASS))
    {
      searchClassObject = new DIDLSearchClass();

      for (int i = 0; i < atts.getLength(); ++i)
      {
        if (atts.getQName(i).equals(DIDLConstants.ATTR_INCLUDE_DERIVED))
        {
          searchClassObject.setDerived(atts.getValue(i));
        }
        if (atts.getQName(i).equals(DIDLConstants.ATTR_NAME))
        {
          searchClassObject.setName(atts.getValue(i));
        }
      }
    }
    // handle create class
    else if (getCurrentTag().equals(DIDLConstants.UPNP_CREATECLASS))
    {
      createClassObject = new DIDLCreateClass();

      for (int i = 0; i < atts.getLength(); ++i)
      {
        if (atts.getQName(i).equals(DIDLConstants.ATTR_INCLUDE_DERIVED))
        {
          createClassObject.setDerived(atts.getValue(i));
        }
        if (atts.getQName(i).equals(DIDLConstants.ATTR_NAME))
        {
          createClassObject.setName(atts.getValue(i));
        }
      }
    }
    // handle resources in extra handler
    else if (getCurrentTag().equals(DIDLConstants.RES))
    {
      DIDLResourceParserHandler resourceHandler = new DIDLResourceParserHandler(this, containerObject);
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
    if (getCurrentTag().equals(DIDLConstants.CONTAINER))
    {
      // add new object to parent
      objectParentHandler.addDIDLContainer(containerObject);
      parentHandler.handleSAXEvents();
    }
    if (getCurrentTag().equals(DIDLConstants.UPNP_SEARCHCLASS))
    {
      containerObject.addSearchClass(searchClassObject);
    }
    if (getCurrentTag().equals(DIDLConstants.UPNP_CREATECLASS))
    {
      containerObject.addCreateClass(createClassObject);
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
      boolean handled = containerObject.handleTags(getCurrentTag(), elementValue);

      if (getCurrentTag().equals(DIDLConstants.UPNP_CLASS))
      {
        // set class if no specific subclass was found
        containerObject.setObjectClass(elementValue);
        handled = true;
      }
      if (getCurrentTag().equals(DIDLConstants.UPNP_SEARCHCLASS))
      {
        searchClassObject.setValue(elementValue);
        handled = true;
      }
      if (getCurrentTag().equals(DIDLConstants.UPNP_CREATECLASS))
      {
        createClassObject.setValue(elementValue);
        handled = true;
      }

      // unknown elements are packed into a generic property vector
      if (!handled)
      {
        containerObject.addProperty(getCurrentTag(), elementValue);
      }
    }
  }

}

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

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * Parses a given DIDLResult for inner DIDL containers and DIDL items.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DIDLParserHandler extends SAXTemplateHandler implements IDIDLObjectParentHandler
{

  public final static String DC_XMLNS             = "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"";

  public final static String UPNP_XMLNS           = "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"";

  public final static String UPNP_DIDL_LITE_XMLNS = "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"";

  public final static String DIDL_LITE            = "DIDL-Lite";

  private int                foundStartAttrib     = 0;

  private Vector             containerList        = new Vector();

  private Vector             itemList             = new Vector();

  private Vector             objectList           = new Vector();

  // for browse metadata
  private DIDLObject         firstObject          = null;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equals(DIDL_LITE))
    {
      for (int i = 0; i < atts.getLength(); i++)
      {
        if (DC_XMLNS.indexOf(atts.getQName(i) + "=\"" + atts.getValue(i)) != -1)
        {
          foundStartAttrib++;
        }
        if (UPNP_XMLNS.indexOf(atts.getQName(i) + "=\"" + atts.getValue(i)) != -1)
        {
          foundStartAttrib++;
        }
        if (UPNP_DIDL_LITE_XMLNS.indexOf(atts.getQName(i) + "=\"" + atts.getValue(i)) != -1)
        {
          foundStartAttrib++;
        }
      }
    } else if (getCurrentTag().equals(DIDLConstants.CONTAINER) && foundStartAttrib == 3)
    {
      DIDLContainerParserHandler containerParserHandler = new DIDLContainerParserHandler(this, this);
      redirectSAXEvents(containerParserHandler, uri, name, qName, atts);
    } else if (getCurrentTag().equals(DIDLConstants.ITEM) && foundStartAttrib == 3)
    {
      DIDLItemParserHandler itemParserHandler = new DIDLItemParserHandler(this, this);
      redirectSAXEvents(itemParserHandler, uri, name, qName, atts);
    }
  }

  /** Returns the list with all parsed containers */
  public Vector getContainerList()
  {
    return containerList;
  }

  /** Returns the list with all parsed items */
  public Vector getItemList()
  {
    return itemList;
  }

  /** Returns the list with all parsed objects (containers + items) */
  public Vector getObjectList()
  {
    return objectList;
  }

  /** Returns the first parsed object */
  public DIDLObject getFirstDIDLObject()
  {
    return firstObject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.IDIDLObjectParentHandler#addDIDLItem(de.fhg.fokus.magic.upnpav.didl.DIDLItem)
   */
  public void addDIDLItem(DIDLItem didlItem)
  {
    itemList.add(didlItem);
    objectList.add(didlItem);
    if (firstObject == null)
    {
      firstObject = didlItem;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.IDIDLObjectParentHandler#addDIDLContainer(de.fhg.fokus.magic.upnpav.didl.DIDLContainer)
   */
  public void addDIDLContainer(DIDLContainer didlContainer)
  {
    containerList.add(didlContainer);
    objectList.add(didlContainer);
    if (firstObject == null)
    {
      firstObject = didlContainer;
    }
  }

}

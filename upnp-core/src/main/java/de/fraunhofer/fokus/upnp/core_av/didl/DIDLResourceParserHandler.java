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
 * Parses a given DIDLResource.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DIDLResourceParserHandler extends SAXTemplateHandler
{

  private SAXTemplateHandler parentHandler;

  private DIDLObject         didlObject;

  private DIDLResource       didlResource;

  /**
   * Creates a new instance of DIDLResourceParserHandler
   * 
   * @param parentHandler
   *          The parent handler
   * @param didlObject
   *          The associated DIDLObject
   */
  public DIDLResourceParserHandler(SAXTemplateHandler parentHandler, DIDLObject didlObject)
  {
    this.parentHandler = parentHandler;
    this.didlObject = didlObject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equals(DIDLConstants.RES))
    {
      didlResource = new DIDLResource();

      for (int i = 0; i < atts.getLength(); ++i)
      {
        didlResource.handleAttribute(atts.getQName(i), atts.getValue(i));
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
    // resource was read completely
    if (getCurrentTag().equals(DIDLConstants.RES))
    {
      didlObject.addResource(didlResource);
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
    // read resource value
    if (getCurrentTag().equals(DIDLConstants.RES) && didlResource != null)
    {
      didlResource.setValue(content.trim());
    }
  }

}

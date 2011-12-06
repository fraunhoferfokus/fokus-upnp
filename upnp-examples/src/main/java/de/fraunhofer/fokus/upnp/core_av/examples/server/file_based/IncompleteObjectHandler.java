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
package de.fraunhofer.fokus.upnp.core_av.examples.server.file_based;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLConstants;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItemParserHandler;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.IDIDLObjectParentHandler;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class parses incomplete object descriptions.
 * 
 * @author Alexander Koenig
 * 
 */
public class IncompleteObjectHandler extends SAXTemplateHandler implements IDIDLObjectParentHandler
{

  private DIDLObject didlObject = new DIDLObject();

  private String     absName    = "";

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
      // create sub handler
      DIDLItemParserHandler itemParserHandler = new DIDLItemParserHandler(this, this);
      redirectSAXEvents(itemParserHandler, uri, name, qName, atts);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getCurrentTag().equals(DirectoryMediaServerEntity.ABS_NAME))
    {
      absName = content;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.IDIDLObjectParentHandler#addDIDLItem(de.fhg.fokus.magic.upnpav.didl.DIDLItem)
   */
  public void addDIDLItem(DIDLItem didlItem)
  {
    this.didlObject = didlItem;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.IDIDLObjectParentHandler#addDIDLContainer(de.fhg.fokus.magic.upnpav.didl.DIDLContainer)
   */
  public void addDIDLContainer(DIDLContainer didlContainer)
  {
    this.didlObject = didlContainer;
  }

  /**
   * Retrieves the absName.
   * 
   * @return The absName.
   */
  public String getAbsName()
  {
    return absName;
  }

  /**
   * Retrieves the didlObject.
   * 
   * @return The didlObject.
   */
  public DIDLObject getDIDLObject()
  {
    return didlObject;
  }
}

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
import de.fraunhofer.fokus.upnp.util.XMLConstant;

/**
 * This class parses action descriptions.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class ActionDescriptionHandler extends SAXTemplateHandler
{

  private String                    name;

  private Vector                    argumentHandlerList;

  private ServiceDescriptionHandler scpdHandler;

  public ActionDescriptionHandler(ServiceDescriptionHandler scpdHandler)
  {
    super(scpdHandler.getSAXParser());
    this.scpdHandler = scpdHandler;
    argumentHandlerList = new Vector();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.ARGUMENT))
    {
      ArgumentHandler argumentHandler = new ArgumentHandler(this);
      redirectSAXEvents(argumentHandler, uri, name, qName, atts);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.NAME))
    {
      name = content;
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
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.ACTION))
    {
      if (name != null)
      {
        scpdHandler.addActionHandler(this);
      } else
      {
        throw new SAXException("action description does not contain a name tag.\n name tag value = " + name);
      }
      scpdHandler.handleSAXEvents();
    }
  }

  public void addArgumentHandler(ArgumentHandler argh)
  {
    argumentHandlerList.add(argh);
  }

  public String getName()
  {
    return name;
  }

  public Vector getArgumentHandlerList()
  {
    return argumentHandlerList;
  }
}

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
 * @author icu, Alexander Koenig
 * 
 */
public class SpecVersionHandler extends SAXTemplateHandler
{

  private ServiceDescriptionHandler scpdHandler;

  private boolean                   major;

  private boolean                   minor;

  public SpecVersionHandler(ServiceDescriptionHandler scpdHandler)
  {
    super(scpdHandler.getSAXParser());
    this.scpdHandler = scpdHandler;
    minor = false;
    major = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.MAJOR))
    {
      if (!content.equalsIgnoreCase(XMLConstant.MAJOR_VERSION))
      {
        throw new SAXException("Invalid major tag: " + content);
      }
      major = true;

      return;
    }
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.MINOR))
    {
      if (!content.equalsIgnoreCase("0") && !content.equalsIgnoreCase("1"))
      {
        throw new SAXException("Invalid minor tag: " + content);
      }
      minor = true;

      return;
    }
  }

  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.SPEC_VERSION))
    {
      if (major && minor)
      {
        scpdHandler.setSpecVersion();
      }
      scpdHandler.handleSAXEvents();
    }
  }
}

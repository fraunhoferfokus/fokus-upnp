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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class is used to parse translation lists returned by the TranslationService.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class TranslationParser extends SAXTemplateHandler
{

  private Hashtable translationTable   = new Hashtable();

  private String    currentName        = null;

  private String    currentTranslation = null;

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String name2, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2)
    {
      currentName = null;
      currentTranslation = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getTagCount() == 3)
    {
      if (getCurrentTag().equalsIgnoreCase("name"))
      {
        currentName = content;
      }
      if (getCurrentTag().equalsIgnoreCase("translation"))
      {
        currentTranslation = content;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String name) throws SAXException
  {
    if (getTagCount() == 2 && currentName != null && currentName.length() > 0 && currentTranslation != null)
    {
      translationTable.put(currentName, currentTranslation);
    }
  }

  /**
   * Retrieves the hashtable.
   * 
   * @return The hashtable
   */
  public Hashtable getTranslationTable()
  {
    return translationTable;
  }
}

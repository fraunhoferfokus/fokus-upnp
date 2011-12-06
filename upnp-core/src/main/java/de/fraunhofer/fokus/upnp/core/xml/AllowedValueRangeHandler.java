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
 * This class parses an AllowedValueRange
 * 
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class AllowedValueRangeHandler extends SAXTemplateHandler
{

  private StateVariableHandler stateVariableHandler;

  private String               minimum;

  private String               maximum;

  private String               step;

  public AllowedValueRangeHandler(StateVariableHandler stateVariableHandler)
  {
    super(stateVariableHandler.getSAXParser());
    this.stateVariableHandler = stateVariableHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.MINIMUM))
    {
      minimum = content;
    }
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.MAXIMUM))
    {
      maximum = content;
    }
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.STEP))
    {
      step = content;
    }
  }

  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.ALLOWED_VALUE_RANGE))
    {
      if (minimum != null && maximum != null)
      {
        stateVariableHandler.setAllowedValueRangeHandler(this);
      } else
      {
        throw new SAXException("allowedValueRange tag does not contain all required argument tag.\n minimum tag value = " +
          minimum + " maximum = " + maximum);
      }
      stateVariableHandler.handleSAXEvents();
    }
  }

  public String getMinimum()
  {
    return minimum;
  }

  public String getMaximum()
  {
    return maximum;
  }

  public String getStep()
  {
    return step;
  }
}

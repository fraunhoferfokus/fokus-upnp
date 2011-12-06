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
 * This class parses state variables.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class StateVariableHandler extends SAXTemplateHandler
{

  private ServiceDescriptionHandler scpdHandler;

  private boolean                   sendEvents;

  private String                    name;

  private String                    dataType;

  private String                    defaultValue;

  private Vector                    allowedValueList;

  private AllowedValueRangeHandler  allowedValueRangeHandler;

  public StateVariableHandler(ServiceDescriptionHandler scpdHandler)
  {
    super(scpdHandler.getSAXParser());
    this.scpdHandler = scpdHandler;
    sendEvents = false;
    allowedValueList = new Vector();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.STATE_VARIABLE))
    {
      if (atts.getValue(XMLConstant.SEND_EVENTS) != null &&
        atts.getValue(XMLConstant.SEND_EVENTS).equalsIgnoreCase(XMLConstant.YES))
      {
        sendEvents = true;
      }
    }

    if (getCurrentTag().equalsIgnoreCase(XMLConstant.ALLOWED_VALUE_RANGE))
    {
      AllowedValueRangeHandler rangeHandler = new AllowedValueRangeHandler(this);
      redirectSAXEvents(rangeHandler, uri, name, qName, atts);
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
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.DATA_TYPE))
    {
      dataType = content;
    }
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.DEFAULT_VALUE))
    {
      defaultValue = content;
    }
    if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase(XMLConstant.ALLOWED_VALUE))
    {
      allowedValueList.add(content);
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
    if (getCurrentTag().equalsIgnoreCase(XMLConstant.STATE_VARIABLE))
    {
      if (name != null && dataType != null)
      {
        if (!allowedValueList.isEmpty() && allowedValueRangeHandler != null)
        {
          throw new SAXException("state variable has allowedValueList and allowedValueRange tags");
        }
        scpdHandler.addStateVariableHandler(this);
      }
      scpdHandler.handleSAXEvents();
    }
  }

  public boolean getSendEvents()
  {
    return sendEvents;
  }

  public String getName()
  {
    return name;
  }

  public String getDataType()
  {
    return dataType;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public Vector getAllowedValueList()
  {
    return allowedValueList;
  }

  public void setAllowedValueRangeHandler(AllowedValueRangeHandler rangeHandler)
  {
    this.allowedValueRangeHandler = rangeHandler;
  }

  public AllowedValueRangeHandler getAllowedValueRangeHandler()
  {
    return allowedValueRangeHandler;
  }
}

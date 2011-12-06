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

/**
 * This class parses a SOAP request.
 * 
 * @author Alexander Koenig
 * 
 */
public class SOAPActionHandler extends SAXTemplateHandler
{

  private String  actionName   = "";

  private String  serviceType  = "";

  private String  content      = "";

  private Vector  argumentList = new Vector();

  private boolean isBody       = false;

  public boolean isValid()
  {
    return actionName.length() > 0 && serviceType.length() > 0;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2 && getCurrentTag().indexOf(":") != -1)
    {
      String body = getCurrentTag().substring(getCurrentTag().indexOf(":") + 1);
      isBody = body.equalsIgnoreCase("body");
    }
    if (isBody && getTagCount() == 3 && getCurrentTag().indexOf(":") != -1)
    {
      try
      {
        actionName = getCurrentTag().substring(getCurrentTag().indexOf(":") + 1);
        // remove pending "Response"
        if (actionName.length() > 0)
        {
          int index = actionName.indexOf("Response");
          if (index != -1)
          {
            actionName = actionName.substring(0, index);
          }
        }
      } catch (Exception e)
      {
      }
      for (int i = 0; i < atts.getLength(); i++)
      {
        if (atts.getQName(i).startsWith("xmlns:"))
        {
          serviceType = atts.getValue(i);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getTagCount() == 4 && actionName.length() > 0 && isBody)
    {
      this.content = content;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getTagCount() == 4 && actionName.length() > 0 && isBody)
    {
      argumentList.add(new SOAPActionArgument(getCurrentTag(), content));
      content = "";
    }
    if (getTagCount() == 2)
    {
      isBody = false;
    }
  }

  public String getActionName()
  {
    return actionName;
  }

  public Vector getArgumentList()
  {
    return argumentList;
  }

  /**
   * Retrieves the serviceType.
   * 
   * @return The serviceType
   */
  public String getServiceType()
  {
    return serviceType;
  }
}

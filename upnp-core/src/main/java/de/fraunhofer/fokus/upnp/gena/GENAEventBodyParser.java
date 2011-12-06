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
package de.fraunhofer.fokus.upnp.gena;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This parser processes the body of GENA NOTIFY messages to update state variables.
 * 
 * @author tje, Alexander Koenig
 */

public class GENAEventBodyParser extends SAXTemplateHandler
{

  /** Parsed variable names */
  private Vector variableNames        = new Vector();

  /** Parsed variable values */
  private Vector variableValues       = new Vector();

  private String currentVariableName  = "";

  private String currentVariableValue = "";

  /**
   * Parses an event NOTIFY message.
   * 
   * @param notifyMessage
   *          The NOTIFY event message
   * 
   * @return The parser if processing was successfull, null otherwise
   * 
   * @throws GENAParseException
   *           Special exception, if gena message could not be parsed
   * 
   */
  public boolean parseMessageBody(HTTPMessageObject notifyMessage)
  {
    try
    {
      variableNames.clear();
      variableValues.clear();

      parse(notifyMessage.getBodyAsUTF8String());
      return true;
    } catch (Exception e)
    {
      System.out.println("Error parsing notify body: " + e.getMessage());
    }
    return false;
  }

  /**
   * Retrieves the variableNames.
   * 
   * @return The variableNames
   */
  public Vector getVariableNames()
  {
    return variableNames;
  }

  /**
   * Retrieves the variableValues.
   * 
   * @return The variableValues
   */
  public Vector getVariableValues()
  {
    return variableValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 3 && getTag(0).equals("e:propertyset") && getTag(1).equals("e:property"))
    {
      currentVariableName = qName;
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
    if (getTagCount() == 3 && getTag(0).equals("e:propertyset") && getTag(1).equals("e:property") &&
      currentVariableName.length() > 0)
    {
      variableNames.add(currentVariableName);
      variableValues.add(currentVariableValue);
      currentVariableName = "";
      currentVariableValue = "";
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getTagCount() == 3 && getTag(0).equals("e:propertyset") && getTag(1).equals("e:property"))
    {
      currentVariableValue = content.trim();
    }
  }

}

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
 * This class is used to parse a service description.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class ServiceDescriptionHandler extends SAXTemplateHandler
{

  private Vector  actionHandlerList;

  private Vector  stateVariableHandlerList;

  private boolean specVersion;

  /**
   * Creates a new instance of SCPDHandler.
   * 
   * @param saxParser
   */
  public ServiceDescriptionHandler()
  {
    super();
    actionHandlerList = new Vector();
    stateVariableHandlerList = new Vector();
    specVersion = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2 && getCurrentTag().equalsIgnoreCase(XMLConstant.SPEC_VERSION))
    {
      SpecVersionHandler versionHandler = new SpecVersionHandler(this);
      redirectSAXEvents(versionHandler, uri, name, qName, atts);
    }
    if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase(XMLConstant.ACTION))
    {
      ActionDescriptionHandler actionHandler = new ActionDescriptionHandler(this);
      redirectSAXEvents(actionHandler, uri, name, qName, atts);
    }
    if (getTagCount() == 3 && getCurrentTag().equalsIgnoreCase(XMLConstant.STATE_VARIABLE))
    {
      StateVariableHandler stateVariableHandler = new StateVariableHandler(this);
      redirectSAXEvents(stateVariableHandler, uri, name, qName, atts);
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
    if (qName.equalsIgnoreCase(XMLConstant.SCPD))
    {
      if (stateVariableHandlerList.isEmpty())
      {
        throw new SAXException("service description does not contain any state variable, which is required.");
      }
      if (!specVersion)
      {
        throw new SAXException("service description has no or invalid spec version, which is required.");
      }
    }
  }

  /** Adds the handler for one parsed remote action. */
  public void addActionHandler(ActionDescriptionHandler actionHandler)
  {
    actionHandlerList.add(actionHandler);
  }

  /** Retrieves the handler list for all parsed remote actions. */
  public Vector getActionHandlerList()
  {
    return actionHandlerList;
  }

  /** Adds the handler for one parsed remote state variable. */
  public void addStateVariableHandler(StateVariableHandler stateVariableHandler)
  {
    stateVariableHandlerList.add(stateVariableHandler);
  }

  /** Retrieves the handler list for all parsed remote state variables. */
  public Vector getStateVariableHandlerList()
  {
    return stateVariableHandlerList;
  }

  public void setSpecVersion()
  {
    specVersion = true;
  }
}

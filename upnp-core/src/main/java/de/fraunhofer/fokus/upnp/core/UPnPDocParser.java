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
package de.fraunhofer.fokus.upnp.core;

import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class is used to parse UPnPDoc messages.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class UPnPDocParser extends SAXTemplateHandler
{

  /** Doc entries for the current service type */
  private Vector       currentDocEntryList          = new Vector();

  private boolean      isAction                     = false;

  private boolean      isStateVariable              = false;

  private String       currentServiceType           = null;

  private String       currentArgumentName          = null;

  private String       currentArgumentDescription   = null;

  private UPnPDocEntry currentDocEntry              = null;

  /** Hashtable containing the UPnP doc entry list for one service type */
  private Hashtable    docEntryFromServiceTypeTable = new Hashtable();

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
      for (int i = 0; i < atts.getLength(); i++)
      {
        if (atts.getQName(i).equalsIgnoreCase("serviceType"))
        {
          currentServiceType = atts.getValue(i);
          currentDocEntryList = new Vector();
        }
      }
    }
    if (getTagCount() == 3 && currentServiceType != null)
    {
      isAction = getCurrentTag().equalsIgnoreCase("actionList");
      isStateVariable = getCurrentTag().equalsIgnoreCase("serviceStateTable");
    }
    if (getTagCount() == 4 && currentServiceType != null)
    {
      currentDocEntry = new UPnPDocEntry(currentServiceType);
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
    if (getTagCount() == 6 && isAction && currentDocEntry != null && currentArgumentName != null &&
      currentArgumentDescription != null)
    {
      currentDocEntry.addArgumentDescription(currentArgumentName, currentArgumentDescription);

      currentArgumentName = null;
      currentArgumentDescription = null;
    }
    if (getTagCount() == 4)
    {
      if (currentDocEntry != null && currentDocEntry.getActionName() != null && isAction)
      {
        // TemplateService.printMessage(" Add doc entry for action " +
        // currentDocEntry.getActionName());
        currentDocEntryList.add(currentDocEntry);
      }
      if (currentDocEntry != null && currentDocEntry.getStateVariableName() != null && isStateVariable)
      {
        // TemplateService.printMessage(" Add doc entry for state variable " +
        // currentDocEntry.getStateVariableName());
        currentDocEntryList.add(currentDocEntry);
      }
      currentDocEntry = null;
    }
    if (getTagCount() == 3)
    {
      isAction = false;
      isStateVariable = false;
    }
    if (getTagCount() == 2)
    {
      // store list with doc entries for one service type
      docEntryFromServiceTypeTable.put(currentServiceType, currentDocEntryList);
      currentServiceType = null;
      currentDocEntryList = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getTagCount() == 5 && currentDocEntry != null)
    {
      if (getCurrentTag().equalsIgnoreCase("name") && isAction)
      {
        currentDocEntry.setActionName(content.trim());
      }

      if (getCurrentTag().equalsIgnoreCase("name") && isStateVariable)
      {
        currentDocEntry.setStateVariableName(content.trim());
      }

      if (getCurrentTag().equalsIgnoreCase("description"))
      {
        currentDocEntry.setDescription(content.trim());
      }
    }
    if (getTagCount() == 7 && currentDocEntry != null)
    {
      if (getCurrentTag().equalsIgnoreCase("name"))
      {
        currentArgumentName = content.trim();
      }

      if (getCurrentTag().equalsIgnoreCase("description"))
      {
        currentArgumentDescription = content.trim();
      }
    }
  }

  /**
   * Retrieves the upnpDocEntryTable.
   * 
   * @return The upnpDocEntryTable
   */
  public Hashtable getDocEntryFormServiceTypeTable()
  {
    return docEntryFromServiceTypeTable;
  }

}

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
package de.fraunhofer.fokus.upnp.core_security.helpers.parser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * 
 * @author Alexander Koenig
 */
public class PermissionsParser
{

  private final String ACL_ENTRY_TAG           = "ACLEntry";

  private final String DEFINED_PERMISSIONS_TAG = "DefinedPermissions";

  private final String FULL_DESCRIPTION_TAG    = "FullDescription";

  private final String PERMISSIONS_TAG         = "Permission";

  private final String SHORT_DESCRIPTION_TAG   = "ShortDescription";

  private final String UI_NAME_TAG             = "UIName";

  private Vector       permissions             = new Vector();

  /** Creates a new instance of AlgorithmsAndProtocolsParser */
  public PermissionsParser(String parseText)
  {
    // parse message
    try
    {
      DefaultHandlerClass parserClass = new DefaultHandlerClass();
      parserClass.parse(parseText);
    } catch (Exception ex)
    {
    }
  }

  public Vector getPermissions()
  {
    return permissions;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {
    private String uiName           = "";

    private String aclEntry         = "";

    private String fullDescription  = "";

    private String shortDescription = "";

    /**
     * Template function for processing an start element
     */
    public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
    {
      if (getTagCount() == 4 && getTag(0).equals(DEFINED_PERMISSIONS_TAG) && getTag(1).equals(PERMISSIONS_TAG) &&
        getTag(2).equals(ACL_ENTRY_TAG))
      {
        aclEntry = "<" + getCurrentTag() + "/>";
      }
    }

    /**
     * Template function for processing an end element
     */
    public void processEndElement(String uri, String localName, String qName) throws SAXException
    {
      // end of one permission
      if (getTagCount() == 2 && uiName.length() != 0 && aclEntry.length() != 0 && shortDescription.length() != 0)
      {
        Permission permission = new Permission(uiName, aclEntry, fullDescription, shortDescription);
        permissions.add(permission);
        // clear data
        uiName = "";
        aclEntry = "";
        fullDescription = "";
        shortDescription = "";
      }
    }

    /**
     * Template function for processing content
     */
    public void processContentElement(String content)
    {
      if (getTagCount() == 3 && getTag(0).equals(DEFINED_PERMISSIONS_TAG) && getTag(1).equals(PERMISSIONS_TAG))
      {
        if (getTag(2).equals(UI_NAME_TAG))
        {
          uiName = content;
        }
        if (getTag(2).equals(FULL_DESCRIPTION_TAG))
        {
          fullDescription = content;
        }
        if (getTag(2).equals(SHORT_DESCRIPTION_TAG))
        {
          shortDescription = content;
        }
      }
    }
  }

}

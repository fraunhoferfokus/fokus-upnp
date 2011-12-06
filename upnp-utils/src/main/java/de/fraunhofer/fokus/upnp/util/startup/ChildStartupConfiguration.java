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
package de.fraunhofer.fokus.upnp.util.startup;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class can be used as base class for inner startup configurations.
 * 
 * @author Alexander Koenig
 * 
 */
public class ChildStartupConfiguration extends StartupConfiguration
{
  /**
   * 
   */
  private static final long       serialVersionUID = 1L;

  /** Handler for outer XML parsing */
  protected StartupConfiguration parentHandler;

  /** Friendly name for the child entity */
  protected String                friendlyName     = "";

  /** Optional attribute ID to identify the configuration */
  protected String                id               = "";

  /**
   * Creates a new instance of DeviceStartupConfiguration.
   * 
   * @param parentHandler
   *          The parent startup configuration
   * 
   */
  public ChildStartupConfiguration(StartupConfiguration parentHandler)
  {
    super(parentHandler);
    this.parentHandler = parentHandler;
  }

  /**
   * Retrieves the value of friendlyName.
   * 
   * @return The value of friendlyName
   */
  public String getFriendlyName()
  {
    return friendlyName;
  }

  /**
   * Sets the new value for friendlyName.
   * 
   * @param friendlyName
   *          The new value for friendlyName
   */
  public void setFriendlyName(String friendlyName)
  {
    this.friendlyName = friendlyName;
  }

  /**
   * Retrieves the value of id.
   * 
   * @return The value of id
   */
  public String getID()
  {
    return id;
  }

  /**
   * Sets the new value for id.
   * 
   * @param id
   *          The new value for id
   */
  public void setID(String id)
  {
    this.id = id;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (atts.getValue("id") != null &&
      (getCurrentTag().equals("Device") || getCurrentTag().equals("ControlPoint") || getCurrentTag().equals("Gateway")))
    {
      id = atts.getValue("id");
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.startup.StartupConfiguration#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    super.processContentElement(content);

    content = content.trim();
    if (getTagCount() == 2)
    {
      if (getCurrentTag().equals(FRIENDLY_NAME))
      {
        friendlyName = content;
      }
    }
  }

}

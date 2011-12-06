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
package de.fraunhofer.fokus.upnp.core_security.helpers;

/**
 * This class holds one predefined permission for a device.
 * 
 * @author Alexander Koenig
 */
public class Permission
{

  private String UIName;

  private String ACLEntry;

  private String fullDescriptionURL;

  private String shortDescription;

  /** Creates a new instance of Permission */
  public Permission(String UIName, String ACLEntry, String fullDescriptionURL, String shortDescription)
  {
    this.UIName = UIName;
    this.ACLEntry = ACLEntry;
    this.fullDescriptionURL = fullDescriptionURL;
    this.shortDescription = shortDescription;
  }

  /** Checks if permission grants the same rights as this permission */
  public boolean equals(Permission permission)
  {
    return ACLEntry.equals(permission.getACLEntry());
  }

  /** Returns an XML-Description of this permission */
  public String toXMLDescription()
  {
    return "<Permission>" + "<UIName>" + UIName + "</UIName>" + "<ACLEntry>" + ACLEntry + "</ACLEntry>" +
      "<FullDescriptionURL>" + fullDescriptionURL + "</FullDescriptionURL>" + "<ShortDescription>" + shortDescription +
      "</ShortDescription>" + "</Permission>";
  }

  /**
   * Retrieves the UIName of this permission.
   */
  public String getUIName()
  {
    return this.UIName;
  }

  /**
   * Sets the UIName of this permission.
   */
  public void setUIName(String name)
  {
    UIName = name;
  }

  /**
   * Retrieves the ACLEntry (e.g. <mfgr:read/>) of this permission.
   */
  public String getACLEntry()
  {
    return this.ACLEntry;
  }

  /**
   * Retrieves the URL to the full description of this permission.
   */
  public String getFullDescriptionURL()
  {
    return this.fullDescriptionURL;
  }

  /**
   * Retrieves the short description of this permission.
   */
  public String getShortDescription()
  {
    return this.shortDescription;
  }

  /**
   * Sets the short description of this permission.
   */
  public void setShortDescription(String description)
  {
    shortDescription = description;
  }

}

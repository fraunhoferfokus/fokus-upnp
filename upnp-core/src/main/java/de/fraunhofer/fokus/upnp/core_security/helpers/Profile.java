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
 * This class holds one predefined profile for a device.
 * 
 * @author Alexander Koenig
 */
public class Profile
{

  private String UIName;

  private String definition;

  private String fullDescriptionURL;

  private String shortDescription;

  /** Creates a new instance of Profile */
  public Profile(String UIName, String definition, String fullDescriptionURL, String shortDescription)
  {
    this.UIName = UIName;
    this.definition = definition;
    this.fullDescriptionURL = fullDescriptionURL;
    this.shortDescription = shortDescription;
  }

  /** Checks if profile grants the same permissions as this profile */
  public boolean equals(Profile profile)
  {
    return definition.equals(profile.getDefinition());
  }

  /** Returns an XML-Description of this profile */
  public String toXMLDescription()
  {
    return "<Profile>" + "<UIName>" + UIName + "</UIName>" + "<Definition>" + definition + "</Definition>" +
      "<FullDescriptionURL>" + fullDescriptionURL + "</FullDescriptionURL>" + "<ShortDescription>" + shortDescription +
      "</ShortDescription>" + "</Profile>";
  }

  /**
   * Getter for property UIName.
   * 
   * @return Value of property UIName.
   */
  public String getUIName()
  {
    return this.UIName;
  }

  public String getDefinition()
  {
    return this.definition;
  }

  /**
   * Getter for property fullDescriptionURL.
   * 
   * @return Value of property fullDescriptionURL.
   */
  public String getFullDescriptionURL()
  {
    return this.fullDescriptionURL;
  }

  /**
   * Getter for property shortDescription.
   * 
   * @return Value of property shortDescription.
   */
  public String getShortDescription()
  {
    return this.shortDescription;
  }

}

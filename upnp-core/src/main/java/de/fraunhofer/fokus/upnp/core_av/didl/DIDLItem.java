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
package de.fraunhofer.fokus.upnp.core_av.didl;

import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core_av.server.MediaServerConstant;

/**
 * @author tje, Alexander Koenig
 * 
 * Class describes the standard DIDL-Item Tag with its arguments and various inner DIDL-Class Tags
 */
public class DIDLItem extends DIDLObject
{

  /** Reference ID */
  private String refID;

  public DIDLItem(DIDLObject sourceObject)
  {
    super(sourceObject);
    setObjectClass(DIDLConstants.UPNP_CLASS_ITEM);
  }

  public DIDLItem()
  {
    setObjectClass(DIDLConstants.UPNP_CLASS_ITEM);
  }

  /** Creates a item in the root container */
  public DIDLItem(String title, String id)
  {
    setTitle(title);
    setID(id);
    setParentID("0");
    setObjectClass(DIDLConstants.UPNP_CLASS_ITEM);
    setRestricted("1");
    setWriteStatus(MediaServerConstant.WRITE_STATUS_NOT_WRITABLE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLItem(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLItem))
    {
      return false;
    }
    return super.equals(arg0);
  }

  /*
   * Checks if the item probably holds the same resource as arg0
   */
  public boolean equalItem(DIDLItem arg0)
  {
    // compare title
    boolean equalTitle = getTitle().equals(arg0.getTitle());
    if (!equalTitle)
    {
      return false;
    }

    // compare UPnP-class
    boolean equalClass = getObjectClass().equals(arg0.getObjectClass());
    if (!equalClass)
    {
      return false;
    }

    // compare metadata for musicTracks
    if (getObjectClass().indexOf(DIDLConstants.UPNP_CLASS_MUSIC_TRACK) != -1)
    {
      String artistA = getPropertyValue(DIDLConstants.UPNP_ARTIST);
      String albumA = getPropertyValue(DIDLConstants.UPNP_ALBUM);
      String artistB = arg0.getPropertyValue(DIDLConstants.UPNP_ARTIST);
      String albumB = arg0.getPropertyValue(DIDLConstants.UPNP_ALBUM);
      boolean equalMetaData =
        artistA != null && artistB != null && artistA.equals(artistB) && albumA != null && albumB != null &&
          albumA.equals(albumB);

      if (!equalMetaData)
      {
        return false;
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#attributeXMLDescription(boolean,
   *      java.util.Vector)
   */
  public String attributeXMLDescription(boolean showAll, Vector filterList)
  {
    String result = super.attributeXMLDescription(showAll, filterList);

    // add optional elements
    if (getRefID() != null && (showAll || isContained(filterList, DIDLConstants.ATTR_REF_ID)))
    {
      result += DIDLConstants.ATTR_REF_ID + "=\"" + getRefID() + "\" ";
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#toXMLDescription(java.lang.String,
   *      java.lang.String)
   */
  public String toXMLDescription(String filter, String absoluteServerPath)
  {
    StringTokenizer tokenizer = new StringTokenizer(filter, ",");
    Vector filterList = new Vector();
    while (tokenizer.hasMoreTokens())
    {
      filterList.add(tokenizer.nextToken().trim());
    }

    String result = "<item ";
    result += attributeXMLDescription(filter.equals("*"), filterList);
    result += ">";
    result += innerXMLDescription(filter.equals("*"), filterList, absoluteServerPath);
    result += "</item>";

    return result;
  }

  /**
   * Retrieves the refID.
   * 
   * @return The refID.
   */
  public String getRefID()
  {
    return refID;
  }

  /**
   * Sets the refID.
   * 
   * @param refID
   *          The refID to set.
   */
  public void setRefID(String refID)
  {
    this.refID = refID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#handleAttribute(java.lang.String,
   *      java.lang.String)
   */
  public boolean handleAttribute(String attribute, String value)
  {
    boolean handled = super.handleAttribute(attribute, value);

    if (attribute.equals(DIDLConstants.ATTR_REF_ID))
    {
      setRefID(value);
      handled = true;
    }

    return handled;
  }

}

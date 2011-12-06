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

import java.util.Vector;

/**
 * @author Alexander Koenig
 * 
 * This class describes an imageItem
 */
public class DIDLImageItem extends DIDLItem
{

  private String longDescription;

  private String storageMedium;

  private String description;

  private String rating;

  private String publisher;

  private String date;

  private String rights;

  public DIDLImageItem(DIDLObject sourceObject)
  {
    super(sourceObject);
    setObjectClass(DIDLConstants.UPNP_CLASS_IMAGE_ITEM);
  }

  /*
   * public DIDLImageItem() { super(); setObjectClass(DIDLConstants.UPNP_CLASS_IMAGE_ITEM); }
   */
  /** Creates a item in the root container */
  public DIDLImageItem(String title, String id)
  {
    super(title, id);
    setObjectClass(DIDLConstants.UPNP_CLASS_IMAGE_ITEM);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLImageItem(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLImageItem))
    {
      return false;
    }
    return super.equals(arg0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#innerXMLDescription(boolean, java.util.Vector,
   *      java.lang.String)
   */
  public String innerXMLDescription(boolean showAll, Vector filterList, String absoluteServerPath)
  {
    String result = super.innerXMLDescription(showAll, filterList, absoluteServerPath);

    // add optional elements
    if (showAll || isContained(filterList, DIDLConstants.DC_DESCRIPTION))
    {
      result += "<" + DIDLConstants.DC_DESCRIPTION + ">" + getDescription() + "</" + DIDLConstants.DC_DESCRIPTION + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.DC_PUBLISHER))
    {
      result += "<" + DIDLConstants.DC_PUBLISHER + ">" + getPublisher() + "</" + DIDLConstants.DC_PUBLISHER + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.DC_DATE))
    {
      result += "<" + DIDLConstants.DC_DATE + ">" + getDate() + "</" + DIDLConstants.DC_DATE + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.DC_RIGHTS))
    {
      result += "<" + DIDLConstants.DC_RIGHTS + ">" + getRights() + "</" + DIDLConstants.DC_RIGHTS + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.UPNP_LONG_DESCRIPTION))
    {
      result +=
        "<" + DIDLConstants.UPNP_LONG_DESCRIPTION + ">" + getLongDescription() + "</" +
          DIDLConstants.UPNP_LONG_DESCRIPTION + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.UPNP_RATING))
    {
      result += "<" + DIDLConstants.UPNP_RATING + ">" + getRating() + "</" + DIDLConstants.UPNP_RATING + ">";
    }
    if (showAll || isContained(filterList, DIDLConstants.UPNP_STORAGE_MEDIUM))
    {
      result +=
        "<" + DIDLConstants.UPNP_STORAGE_MEDIUM + ">" + getStorageMedium() + "</" + DIDLConstants.UPNP_STORAGE_MEDIUM +
          ">";
    }

    return result;
  }

  /**
   * Getter for property longDescription.
   * 
   * @return Value of property longDescription.
   */
  public String getLongDescription()
  {
    return this.longDescription;
  }

  /**
   * Setter for property longDescription.
   * 
   * @param longDescription
   *          New value of property longDescription.
   */
  public void setLongDescription(String longDescription)
  {
    this.longDescription = longDescription;
  }

  /**
   * Getter for property storageMedium.
   * 
   * @return Value of property storageMedium.
   */
  public String getStorageMedium()
  {
    return this.storageMedium;
  }

  /**
   * Setter for property storageMedium.
   * 
   * @param storageMedium
   *          New value of property storageMedium.
   */
  public void setStorageMedium(String storageMedium)
  {
    this.storageMedium = storageMedium;
  }

  /**
   * Getter for property description.
   * 
   * @return Value of property description.
   */
  public String getDescription()
  {
    return this.description;
  }

  /**
   * Setter for property description.
   * 
   * @param description
   *          New value of property description.
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Getter for property rating.
   * 
   * @return Value of property rating.
   */
  public String getRating()
  {
    return this.rating;
  }

  /**
   * Setter for property rating.
   * 
   * @param rating
   *          New value of property rating.
   */
  public void setRating(String rating)
  {
    this.rating = rating;
  }

  /**
   * Getter for property publisher.
   * 
   * @return Value of property publisher.
   */
  public String getPublisher()
  {
    return this.publisher;
  }

  /**
   * Setter for property publisher.
   * 
   * @param publisher
   *          New value of property publisher.
   */
  public void setPublisher(String publisher)
  {
    this.publisher = publisher;
  }

  /**
   * Getter for property date.
   * 
   * @return Value of property date.
   */
  public String getDate()
  {
    return this.date;
  }

  /**
   * Setter for property date.
   * 
   * @param date
   *          New value of property date.
   */
  public void setDate(String date)
  {
    this.date = date;
  }

  /**
   * Getter for property rights.
   * 
   * @return Value of property rights.
   */
  public String getRights()
  {
    return this.rights;
  }

  /**
   * Setter for property rights.
   * 
   * @param rights
   *          New value of property rights.
   */
  public void setRights(String rights)
  {
    this.rights = rights;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#fillClassSpecificData()
   */
  public void fillClassSpecificData()
  {
    super.fillClassSpecificData();
    int i = 0;
    while (i < propertyList.size())
    {
      boolean found = false;
      DIDLPropertyClass property = (DIDLPropertyClass)propertyList.elementAt(i);

      if (property.getIdentifier().equals(DIDLConstants.DC_DESCRIPTION))
      {
        description = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.DC_DATE))
      {
        date = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.DC_PUBLISHER))
      {
        publisher = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.DC_RIGHTS))
      {
        rights = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.UPNP_LONG_DESCRIPTION))
      {
        longDescription = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.UPNP_RATING))
      {
        rating = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.UPNP_STORAGE_MEDIUM))
      {
        storageMedium = property.getValue();
        found = true;
      }
      if (found)
      {
        propertyList.remove(i);
      } else
      {
        i++;
      }
    }
  }

}

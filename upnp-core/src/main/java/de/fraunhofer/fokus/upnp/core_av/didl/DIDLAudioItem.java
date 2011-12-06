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
 * This class describes an audioItem
 */
public class DIDLAudioItem extends DIDLItem
{

  private String longDescription;

  private String genre;

  private String description;

  private String publisher;

  private String language;

  private String relation;

  private String rights;

  public DIDLAudioItem(DIDLObject sourceObject)
  {
    super(sourceObject);
    setObjectClass(DIDLConstants.UPNP_CLASS_AUDIO_ITEM);
  }

  /** Creates a item in the root container */
  public DIDLAudioItem(String title, String id)
  {
    super(title, id);
    setObjectClass(DIDLConstants.UPNP_CLASS_AUDIO_ITEM);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLAudioItem(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLAudioItem))
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
    if (getDescription() != null && (showAll || isContained(filterList, DIDLConstants.DC_DESCRIPTION)))
    {
      result += "<" + DIDLConstants.DC_DESCRIPTION + ">" + getDescription() + "</" + DIDLConstants.DC_DESCRIPTION + ">";
    }
    if (getPublisher() != null && (showAll || isContained(filterList, DIDLConstants.DC_PUBLISHER)))
    {
      result += "<" + DIDLConstants.DC_PUBLISHER + ">" + getPublisher() + "</" + DIDLConstants.DC_PUBLISHER + ">";
    }
    if (getLanguage() != null && (showAll || isContained(filterList, DIDLConstants.DC_LANGUAGE)))
    {
      result += "<" + DIDLConstants.DC_LANGUAGE + ">" + getLanguage() + "</" + DIDLConstants.DC_LANGUAGE + ">";
    }
    if (getRelation() != null && (showAll || isContained(filterList, DIDLConstants.DC_RELATION)))
    {
      result += "<" + DIDLConstants.DC_RELATION + ">" + getRelation() + "</" + DIDLConstants.DC_RELATION + ">";
    }
    if (getRights() != null && (showAll || isContained(filterList, DIDLConstants.DC_RIGHTS)))
    {
      result += "<" + DIDLConstants.DC_RIGHTS + ">" + getRights() + "</" + DIDLConstants.DC_RIGHTS + ">";
    }
    if (getLongDescription() != null && (showAll || isContained(filterList, DIDLConstants.UPNP_LONG_DESCRIPTION)))
    {
      result +=
        "<" + DIDLConstants.UPNP_LONG_DESCRIPTION + ">" + getLongDescription() + "</" +
          DIDLConstants.UPNP_LONG_DESCRIPTION + ">";
    }
    if (getGenre() != null && (showAll || isContained(filterList, DIDLConstants.UPNP_GENRE)))
    {
      result += "<" + DIDLConstants.UPNP_GENRE + ">" + getGenre() + "</" + DIDLConstants.UPNP_GENRE + ">";
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

  /**
   * Getter for property genre.
   * 
   * @return Value of property genre.
   */
  public String getGenre()
  {
    return this.genre;
  }

  /**
   * Setter for property genre.
   * 
   * @param genre
   *          New value of property genre.
   */
  public void setGenre(String genre)
  {
    this.genre = genre;
  }

  /**
   * Getter for property language.
   * 
   * @return Value of property language.
   */
  public String getLanguage()
  {
    return this.language;
  }

  /**
   * Setter for property language.
   * 
   * @param language
   *          New value of property language.
   */
  public void setLanguage(String language)
  {
    this.language = language;
  }

  /**
   * Getter for property relation.
   * 
   * @return Value of property relation.
   */
  public String getRelation()
  {
    return this.relation;
  }

  /**
   * Setter for property relation.
   * 
   * @param relation
   *          New value of property relation.
   */
  public void setRelation(String relation)
  {
    this.relation = relation;
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
      if (property.getIdentifier().equals(DIDLConstants.DC_LANGUAGE))
      {
        language = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.DC_RELATION))
      {
        relation = property.getValue();
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
      if (property.getIdentifier().equals(DIDLConstants.UPNP_GENRE))
      {
        genre = property.getValue();
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

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

import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class describes the standard DIDL object.
 * 
 * @author Alexander Koenig
 * 
 */
public class DIDLObject
{

  private String        id;

  private String        parentID;

  private String        title;

  private String        creator;

  private Vector        resourceList    = new Vector();

  private String        objectClass;

  private String        restricted      = "true";

  private String        writeStatus;

  // non-standard properties
  protected Vector      propertyList    = new Vector();

  // non-standard attributes
  protected Vector      attributeList   = new Vector();

  // this property is used for tree building
  private DIDLContainer parentContainer = null;

  /** Creates a new object from an existing object */
  public DIDLObject(DIDLObject sourceObject)
  {
    super();
    setID(sourceObject.getID());
    setParentID(sourceObject.getParentID());
    setTitle(sourceObject.getTitle());
    setCreator(sourceObject.getCreator());
    setRestricted(sourceObject.getRestricted());
    setWriteStatus(sourceObject.getWriteStatus());
    setResourceList(sourceObject.getResources());
    setPropertyList(sourceObject.getPropertyList());
    fillClassSpecificData();
  }

  /** Creates an empty DIDL object */
  public DIDLObject()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLObject(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLObject))
    {
      return false;
    }
    DIDLObject didl = (DIDLObject)arg0;

    boolean equalID = didl.getID() == null && this.id == null || didl.getID() != null && didl.getID().equals(this.id);

    return equalID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return this.id.hashCode();
  }

  /**
   * Returns the title of this object
   * 
   */
  public String toString()
  {
    return title;
  }

  /**
   * Returns an XML description of this object
   * 
   * @param filter
   *          The properties that should be included in the description
   * @param absoluteServerPath
   *          The path that should be used for relative URLs (e.g. http://192.168.1.5:80/resources/)
   * 
   * @return The XML fragment for this object
   * 
   */
  public String toXMLDescription(String filter, String absoluteServerPath)
  {
    // invalid call
    return "";
  }

  /**
   * Returns the attribute part of the XML description of this object
   * 
   * @param showAll
   *          Flag to include all attributes in the description
   * @param filterList
   *          The properties that should be included in the description
   * 
   * @return The XML fragment of all requested attributes
   */
  public String attributeXMLDescription(boolean showAll, Vector filterList)
  {
    String result =
      "id=\"" + getID() + "\" " + "parentID=\"" + getParentID() + "\" " + "restricted=\"" + getRestricted() + "\" ";

    // add optional elements
    if ((showAll || isContained(filterList, DIDLConstants.UPNP_WRITESTATUS)) && getWriteStatus() != null)
    {
      result += "writeStatus=\"" + getWriteStatus() + "\" ";
    }
    return result;
  }

  /**
   * Returns the inner part of the XML description of this object
   * 
   * @param showAll
   *          Flag to include all attributes in the description
   * @param filterList
   *          The properties that should be included in the description
   * @param absoluteServerPath
   *          The path that should be used for relative URLs (e.g. http://192.168.1.5:80/resources/)
   * 
   * @return The XML fragment for this object
   * 
   */
  public String innerXMLDescription(boolean showAll, Vector filterList, String absoluteServerPath)
  {
    String result = "";
    // add xml fragments
    result += "<dc:title>" + StringHelper.xmlToEscapedString(getTitle()) + "</dc:title>";
    result += "<upnp:class>" + getObjectClass() + "</upnp:class>";

    // add optional elements
    if (getCreator() != null && (showAll || isContained(filterList, DIDLConstants.DC_CREATOR)))
    {
      result += "<" + DIDLConstants.DC_CREATOR + ">" + getCreator() + "</" + DIDLConstants.DC_CREATOR + ">";
    }

    // if (showAll || isContained(filterList,DIDLConstants.RES))
    {
      for (int i = 0; i < getResources().size(); i++)
      {
        result += ((DIDLResource)getResources().elementAt(i)).toXMLDescription(showAll, filterList, absoluteServerPath);
      }
    }

    return result;
  }

  /** Checks if a specific tag is contained in a vector */
  protected boolean isContained(Vector list, String tag)
  {
    for (int i = 0; i < list.size(); i++)
    {
      if (((String)list.elementAt(i)).equals(tag))
      {
        return true;
      }
    }
    return false;
  }

  /** Sets the ID for this object */
  public void setID(String id)
  {
    this.id = id;
  }

  public void setParentID(String id)
  {
    parentID = id;
  }

  public void setRestricted(String restricted)
  {
    this.restricted = restricted;
  }

  public void setWriteStatus(String status)
  {
    this.writeStatus = status;
  }

  public void setObjectClass(String newClass)
  {
    objectClass = newClass;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setCreator(String creator)
  {
    this.creator = creator;
  }

  public void addResource(DIDLResource resource)
  {
    if (resourceList == null)
    {
      resourceList = new Vector();
    }

    resourceList.add(resource);
  }

  /** Adds a non-standard property to this object */
  public void addProperty(String identifier, String value)
  {
    propertyList.add(new DIDLPropertyClass(identifier, value));
  }

  /** Adds a non-standard attribute to this object */
  public void addAttribute(String identifier, String value)
  {
    attributeList.add(new DIDLPropertyClass(identifier, value));
  }

  /** Sets the resource list for this object */
  public void setResourceList(Vector list)
  {
    resourceList = list;
  }

  /** Sets the property list for this object */
  public void setPropertyList(Vector list)
  {
    propertyList = list;
  }

  /** Retrieves the ID for this object */
  public String getID()
  {
    return id;
  }

  /** Retrieves the parent ID for this object */
  public String getParentID()
  {
    return parentID;
  }

  /** Retrieves the restricted attribute for this object */
  public String getRestricted()
  {
    return restricted;
  }

  /** Retrieves the restricted attribute for this object */
  public boolean isRestricted()
  {
    return StringHelper.stringToBoolean(restricted);
  }

  /** Retrieves the write status for this object */
  public String getWriteStatus()
  {
    return writeStatus;
  }

  /** Retrieves the object class for this object */
  public String getObjectClass()
  {
    return objectClass;
  }

  /** Retrieves the title for this object */
  public String getTitle()
  {
    return title;
  }

  /** Retrieves the creator for this object */
  public String getCreator()
  {
    return creator;
  }

  /** Retrieves the resources for this object */
  public Vector getResources()
  {
    return resourceList;
  }

  /** Retrieves the first resource for this object */
  public DIDLResource getFirstResource()
  {
    if (resourceList != null && resourceList.size() > 0)
    {
      return (DIDLResource)resourceList.elementAt(0);
    }

    return null;
  }

  /** Retrieves the URL of the first resource for this object */
  public String getFirstResourceURL()
  {
    DIDLResource firstResource = getFirstResource();
    if (firstResource != null)
    {
      return firstResource.getValue();
    }

    return null;
  }

  /** Retrieves the non-standard properties for this object */
  public Vector getPropertyList()
  {
    return propertyList;
  }

  /** Retrieves a string list with non-standard property identifiers */
  public Vector getIdentifierList()
  {
    Vector result = new Vector();
    for (int i = 0; i < propertyList.size(); i++)
    {
      result.add(((DIDLPropertyClass)propertyList.elementAt(i)).getIdentifier());
    }

    return result;
  }

  /** Retrieves a string list with non-standard property values */
  public String getPropertyValue(String identifier)
  {
    for (int i = 0; i < propertyList.size(); i++)
    {
      if (((DIDLPropertyClass)propertyList.elementAt(i)).getIdentifier().equals(identifier))
      {
        return ((DIDLPropertyClass)propertyList.elementAt(i)).getValue();
      }
    }
    return null;
  }

  /** Retrieves the non-standard properties for this object */
  public Vector getAttributeList()
  {
    return attributeList;
  }

  /** Retrieves a string list with non-standard attribute identifiers */
  public Vector getAttributeIdentifierList()
  {
    Vector result = new Vector();
    for (int i = 0; i < attributeList.size(); i++)
    {
      result.add(((DIDLPropertyClass)attributeList.elementAt(i)).getIdentifier());
    }

    return result;
  }

  /** Retrieves a string list with non-standard attribute values */
  public String getAttributeValue(String identifier)
  {
    for (int i = 0; i < attributeList.size(); i++)
    {
      if (((DIDLPropertyClass)attributeList.elementAt(i)).getIdentifier().equals(identifier))
      {
        return ((DIDLPropertyClass)attributeList.elementAt(i)).getValue();
      }
    }
    return null;
  }

  /** Sets the parent of this object */
  public void setParentContainer(DIDLContainer newParentContainer)
  {
    parentContainer = newParentContainer;
  }

  /** Retrieves the parent of this object */
  public DIDLContainer getParentContainer()
  {
    return parentContainer;
  }

  /** Retrieves all objects that are located in the same container as this object */
  public DIDLObject[] getContainerContent()
  {
    // root container, contains only the object itself
    if (getParentContainer() == null)
    {
      DIDLObject[] result = new DIDLObject[1];
      result[0] = this;
      return result;
    } else
    {
      return getParentContainer().getChildList();
    }
  }

  /**
   * This method is used to fill data that is only available in derived classes (e.g. date,
   * publisher)
   */
  public void fillClassSpecificData()
  {
  }

  /**
   * This method parses object attributes.
   * 
   * @param attribute
   *          The attribute
   * @param value
   *          The attribute value
   * 
   * @return True if the attribute was handled, false otherwise
   * 
   */
  public boolean handleAttribute(String attribute, String value)
  {
    boolean handled = false;
    if (attribute.equals(DIDLConstants.ATTR_ID))
    {
      setID(value);
      handled = true;
    }
    if (attribute.equals(DIDLConstants.ATTR_PARENT_ID))
    {
      setParentID(value);
      handled = true;
    }
    if (attribute.equals(DIDLConstants.ATTR_RESTRICTED))
    {
      setRestricted(value);
      handled = true;
    }

    return handled;
  }

  /**
   * This method parses object tags.
   * 
   * @param tag
   *          The tag
   * @param value
   *          The value
   * 
   * @return True if the tag was handled, false otherwise
   * 
   */
  public boolean handleTags(String tag, String value)
  {
    boolean handled = false;
    if (tag.equals(DIDLConstants.DC_TITLE))
    {
      setTitle(value);
      handled = true;
    } else if (tag.equals(DIDLConstants.DC_CREATOR))
    {
      setCreator(value);
      handled = true;
    } else if (tag.equals(DIDLConstants.UPNP_WRITESTATUS))
    {
      setWriteStatus(value);
      handled = true;
    }

    return handled;
  }

}

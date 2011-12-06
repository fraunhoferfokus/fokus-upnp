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

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core_av.server.MediaServerConstant;

/**
 * @author tje, Alexander Koenig
 * 
 * Class describes the standard DIDL-Container Tag with its arguments and inner Tags like DIDL-Item
 * and the various DIDL-CLass Tags
 */
public class DIDLContainer extends DIDLObject
{

  private String       searchable;

  private Vector       searchClasses                = new Vector();

  private Vector       createClasses                = new Vector();

  private DIDLObject[] childList                    = null;

  /** Hashtable containing information for management */
  private Hashtable    managementData               = new Hashtable();

  private long         expectedChildCount           = 0;

  private long         containerUpdateID            = 0;

  private boolean      hasBeenEnumerated            = false;

  /** SystemUpdateID when this container was enumerated */
  private long         systemUpdateIDForEnumeration = 0;

  /** Creates an empty container */
  public DIDLContainer()
  {
    this("", "0");
  }

  public DIDLContainer(DIDLContainer sourceObject)
  {
    super(sourceObject);
    setObjectClass(DIDLConstants.UPNP_CLASS_CONTAINER);
  }

  /** Creates a container */
  public DIDLContainer(String title, String id)
  {
    setObjectClass(DIDLConstants.UPNP_CLASS_CONTAINER);
    setID(id);
    setParentID("-1");
    setTitle(title);
    setRestricted("1");
    searchable = "0";
    setWriteStatus(MediaServerConstant.WRITE_STATUS_NOT_WRITABLE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLContainer(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLContainer))
    {
      return false;
    }
    return super.equals(arg0);
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
    if (showAll || isContained(filterList, DIDLConstants.ATTR_CHILD_COUNT))
    {
      result += DIDLConstants.ATTR_CHILD_COUNT + "=\"" + getExpectedChildCountString() + "\" ";
    }

    if (getSearchable() != null && (showAll || isContained(filterList, DIDLConstants.ATTR_SEARCHABLE)))
    {
      result += DIDLConstants.ATTR_SEARCHABLE + "=\"" + getSearchable() + "\" ";
    }
    return result;
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
    if (showAll || isContained(filterList, DIDLConstants.UPNP_CREATECLASS))
    {
      for (int i = 0; i < createClasses.size(); i++)
      {
        result += ((DIDLCreateClass)createClasses.elementAt(i)).toXMLDescription();
      }
    }
    if (showAll || isContained(filterList, DIDLConstants.UPNP_SEARCHCLASS))
    {
      for (int i = 0; i < searchClasses.size(); i++)
      {
        result += ((DIDLSearchClass)searchClasses.elementAt(i)).toXMLDescription();
      }
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

    String result = "<container ";
    result += attributeXMLDescription(filter.equals("*"), filterList);
    result += ">";
    result += innerXMLDescription(filter.equals("*"), filterList, absoluteServerPath);
    result += "</container>";

    return result;
  }

  /** Retrieves the title of this container */
  public String toString()
  {
    return getTitle();
  }

  /** Retrieves the last read containerUpdateID */
  public long getContainerUpdateID()
  {
    return containerUpdateID;
  }

  /** Sets the containerUpdateID */
  public void setContainerUpdateID(long id)
  {
    containerUpdateID = id;
  }

  /** Retrieve the management hashtable */
  public Hashtable getManagementHashtable()
  {
    return managementData;
  }

  /** Checks if the content of this container has been enumerated */
  public boolean hasBeenEnumerated()
  {
    return hasBeenEnumerated;
  }

  /** Sets the enumeration state of this container to true */
  public void setEnumerated()
  {
    hasBeenEnumerated = true;
  }

  /**
   * Updates the child list of this container
   * 
   * @param childList
   *          The new child list for this container
   */
  public void setChildList(DIDLObject[] childList)
  {
    DIDLObject[] oldChildList = this.childList;
    this.childList = childList;
    if (childList != null)
    {
      // automatically link to parent (for tree building in devices)
      for (int i = 0; i < childList.length; i++)
      {
        childList[i].setParentID(getID());
        childList[i].setParentContainer(this);
      }
      // link already enumerated childs of the old list with the new list
      if (oldChildList != null)
      {

        for (int i = 0; i < childList.length; i++)
        {
          if (childList[i] instanceof DIDLContainer)
          {
            DIDLContainer currentContainer = (DIDLContainer)childList[i];
            for (int j = 0; j < oldChildList.length; j++)
            {
              if (oldChildList[j] instanceof DIDLContainer)
              {
                DIDLContainer oldContainer = (DIDLContainer)oldChildList[j];
                if (oldContainer.getTitle().equals(currentContainer.getTitle()) &&
                  oldContainer.getCurrentChildCount() != 0)
                {
                  currentContainer.setChildList(oldContainer.getChildList());
                }
              }
            }
          }
        }
      }
    }
    this.expectedChildCount = childList == null ? 0 : childList.length;
  }

  /**
   * Adds some childs to the list of this container. This method also links all new childs to this
   * parent container.
   */
  public void addChilds(DIDLObject[] newChildList)
  {
    if (childList == null)
    {
      childList = new DIDLObject[0];
    }

    if (newChildList == null)
    {
      return;
    }

    DIDLObject[] tempList = new DIDLObject[childList.length + newChildList.length];
    // copy old entries
    for (int i = 0; i < childList.length; i++)
    {
      tempList[i] = childList[i];
    }

    // add new entries
    for (int i = 0; i < newChildList.length; i++)
    {
      newChildList[i].setParentID(getID());
      newChildList[i].setParentContainer(this);
      tempList[childList.length + i] = newChildList[i];
    }

    childList = tempList;
    this.expectedChildCount = childList == null ? 0 : childList.length;
  }

  /**
   * Adds one child to the list of this container. This method also links the new child to this
   * parent container.
   */
  public void addChild(DIDLObject newChild)
  {
    if (childList == null)
    {
      childList = new DIDLObject[0];
    }

    DIDLObject[] tempList = new DIDLObject[childList.length + 1];
    // copy old list
    for (int i = 0; i < childList.length; i++)
    {
      tempList[i] = childList[i];
    }

    newChild.setParentID(getID());
    newChild.setParentContainer(this);
    tempList[childList.length] = newChild;

    childList = tempList;
    this.expectedChildCount = childList == null ? 0 : childList.length;
  }

  public void setSearchable(String newSearchable)
  {
    searchable = newSearchable;
  }

  public void addSearchClass(DIDLSearchClass newSearchClass)
  {
    searchClasses.add(newSearchClass);
  }

  public void addCreateClass(DIDLCreateClass newCreateClass)
  {
    createClasses.add(newCreateClass);
  }

  /** Retrieves the number of known childs */
  public int getCurrentChildCount()
  {
    return childList != null ? childList.length : 0;
  }

  /** Sets the overall child count of this container */
  public void setExpectedChildCountString(String count)
  {
    try
    {
      expectedChildCount = Long.parseLong(count);
    } catch (Exception ex)
    {
    }
  }

  /** Sets the overall child count of this container */
  public void setExpectedChildCount(long count)
  {
    expectedChildCount = count;
  }

  /** Retrieves the number of expected childs */
  public long getExpectedChildCount()
  {
    return expectedChildCount;
  }

  /** Retrieves the number of expected childs */
  public String getExpectedChildCountString()
  {
    return Long.toString(expectedChildCount);
  }

  public String getSearchable()
  {
    return searchable;
  }

  public Vector getSearchClasses()
  {
    return searchClasses;
  }

  public Vector getCreateClasses()
  {
    return createClasses;
  }

  public DIDLObject[] getChildList()
  {
    if (childList == null)
    {
      return new DIDLObject[0];
    }

    return childList;
  }

  /** Retrieves an array with all child containers. */
  public DIDLContainer[] getChildContainerList()
  {
    int count = 0;
    if (childList != null)
    {
      for (int i = 0; i < childList.length; i++)
      {
        if (childList[i] instanceof DIDLContainer)
        {
          count++;
        }
      }
      DIDLContainer[] result = new DIDLContainer[count];
      count = 0;
      for (int i = 0; i < childList.length; i++)
      {
        if (childList[i] instanceof DIDLContainer)
        {
          result[count] = (DIDLContainer)childList[i];
          count++;
        }
      }
      return result;
    }
    return new DIDLContainer[0];
  }

  /** Retrieves an array with all child items. */
  public DIDLItem[] getChildItemList()
  {
    int count = 0;
    if (childList != null)
    {
      for (int i = 0; i < childList.length; i++)
      {
        if (childList[i] instanceof DIDLItem)
        {
          count++;
        }
      }
      DIDLItem[] result = new DIDLItem[count];
      count = 0;
      for (int i = 0; i < childList.length; i++)
      {
        if (childList[i] instanceof DIDLItem)
        {
          result[count] = (DIDLItem)childList[i];
          count++;
        }
      }
      return result;
    }
    return new DIDLItem[0];
  }

  /** Recursively searches a specific child */
  public DIDLObject getRecursiveChild(String objectID)
  {
    if (objectID.equals(getID()))
    {
      return this;
    }

    if (childList == null)
    {
      return null;
    }

    // search direct childs first
    for (int i = 0; i < childList.length; i++)
    {
      DIDLObject didlObject = childList[i];
      if (didlObject.getID().equals(objectID))
      {
        return didlObject;
      }
    }
    // not found, search in subcontainers
    for (int i = 0; i < childList.length; i++)
    {
      if (childList[i] instanceof DIDLContainer)
      {
        DIDLObject result = ((DIDLContainer)childList[i]).getRecursiveChild(objectID);
        if (result != null)
        {
          return result;
        }
      }
    }
    return null;
  }

  /** Retrieves a specific child of this container */
  public DIDLObject getChild(String objectID)
  {
    if (childList == null)
    {
      return null;
    }

    for (int i = 0; i < childList.length; i++)
    {
      DIDLObject didlObject = childList[i];
      if (didlObject.getID().equals(objectID))
      {
        return didlObject;
      }
    }
    return null;
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

    if (attribute.equals(DIDLConstants.ATTR_CHILD_COUNT))
    {
      setExpectedChildCountString(value);
      handled = true;
    }
    if (attribute.equals(DIDLConstants.ATTR_SEARCHABLE))
    {
      setSearchable(value);
      handled = true;
    }

    return handled;
  }

  /**
   * Retrieves the systemUpdateIDForEnumeration.
   * 
   * @return The systemUpdateIDForEnumeration.
   */
  public long getSystemUpdateID()
  {
    return systemUpdateIDForEnumeration;
  }

  /**
   * Sets the systemUpdateIDForEnumeration.
   * 
   * @param systemUpdateID
   *          The systemUpdateID to set.
   */
  public void setSystemUpdateID(long systemUpdateID)
  {
    this.systemUpdateIDForEnumeration = systemUpdateID;
  }

}

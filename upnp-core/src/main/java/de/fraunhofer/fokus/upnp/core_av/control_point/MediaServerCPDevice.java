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
package de.fraunhofer.fokus.upnp.core_av.control_point;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateCPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.server.BrowseResponse;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentModifier;
import de.fraunhofer.fokus.upnp.core_av.server.TransferStatus;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * A MediaServerCPDevice holds the content structure of a UPnP media server device. It can be seen
 * as a remote view on that media server.
 * 
 * @author tje, Alexander Koenig
 */
public class MediaServerCPDevice extends TemplateCPDevice implements Runnable, IMediaServerContentModifier
{

  private static Logger              logger                   = Logger.getLogger("upnp");

  /** Root container of the server */
  private DIDLContainer              contentRootObject;

  /** Currently selected object in the server */
  private DIDLObject                 currentObject;

  /** Vector with containers that are known but not yet linked to the root container */
  private Vector                     unconnectedContainerList = new Vector();

  /** Sync object for content updates */
  private Object                     contentLock              = new Object();

  /** Sync object for content creation */
  private Object                     creationLock             = new Object();

  /** Listener for server events */
  private Vector                     serverChangeListenerList = new Vector();

  private long                       systemUpdateID;

  /** Class used for browsing the remote server */
  private CPContentDirectoryBrowser  contentDirectoryBrowser;

  /** Class used for modifying the remote server */
  private CPContentDirectoryModifier contentDirectoryModifier;

  /** List of objectIDs that are waiting for enumeration */
  private Vector                     pendingContainerList     = new Vector();

  private boolean                    terminateThread          = false;

  private boolean                    terminated               = false;

  /**
   * Creates a new instance of MediaServerCPDevice.
   * 
   * @param controlPoint
   *          The control point for action invocations
   * @param serverDevice
   *          The remote media server device
   */
  public MediaServerCPDevice(TemplateControlPoint controlPoint, CPDevice serverDevice)
  {
    super(controlPoint, serverDevice);
    contentDirectoryBrowser = new CPContentDirectoryBrowser(this);
    try
    {
      // immediately enumerate root folder
      contentRootObject = (DIDLContainer)contentDirectoryBrowser.browseRootMetadata().getFirstResult();
      DIDLObject[] rootChildren = contentDirectoryBrowser.browseRootDirectChildren().getResult();
      // link parent with all childs
      contentRootObject.setChildList(rootChildren);
      contentRootObject.setContainerUpdateID(contentDirectoryBrowser.getSystemUpdateID());
      currentObject = contentRootObject;
    } catch (Exception ex)
    {
      logger.warn("error parsing root container:" + ex.getMessage());
    }

    contentDirectoryModifier = new CPContentDirectoryModifier(this);
    // get initial value for system update ID
    systemUpdateID = contentDirectoryBrowser.getSystemUpdateID();

    Thread thread = new Thread(this);
    thread.setName("MediaServerCPDevice");
    thread.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateCPDevice#toString()
   */
  public String toString()
  {
    return getCPDevice().getFriendlyName();
  }

  public void addServerChangeListener(ICPMediaServerStateVariableListener listener)
  {
    serverChangeListenerList.add(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateCPDevice#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    // check if state variable belongs to this device
    if (stateVariable.getName().equals("SystemUpdateID") && stateVariable.getCPService().getCPDevice() == getCPDevice())
    {
      try
      {
        System.out.println("MediaServerCPDevice: SystemUpdateID changed: " + stateVariable.getNumericValue());
        if (systemUpdateID != stateVariable.getNumericValue())
        {
          systemUpdateID = stateVariable.getNumericValue();
          // inform listeners
          for (int i = 0; i < serverChangeListenerList.size(); ++i)
          {
            ((ICPMediaServerStateVariableListener)serverChangeListenerList.get(i)).systemUpdateOccurred(this);
          }
        }
      } catch (Exception e)
      {
      }
    }
    if (stateVariable.getName().equals("ContainerUpdateIDs"))
    {
      // System.out.println("Var changed="+csv.getValue().toString()+":");

      /*
       * for (int i = 0; i < listenerList.size(); ++i) { ((UPnPAVServerChangeAdapter)
       * listenerList.get(i)).containerUpdateOccurred(csv.getValue() .toString()); }
       */
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content navigation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the root container of this media server */
  public DIDLContainer getRootContainer()
  {
    return contentRootObject;
  }

  /** Retrieves the currently selected object in this media server */
  public DIDLObject getCurrentObject()
  {
    return currentObject;
  }

  /** Retrieves the currently selected object as container */
  public DIDLContainer getCurrentContainer()
  {
    if (currentObject instanceof DIDLContainer)
    {
      return (DIDLContainer)currentObject;
    }

    return null;
  }

  /** Retrieves the currently selected object as item */
  public DIDLItem getCurrentItem()
  {
    if (currentObject instanceof DIDLItem)
    {
      return (DIDLItem)currentObject;
    }

    return null;
  }

  /** Retrieves the number of parent containers */
  public int getPathCountToCurrentObject()
  {
    if (currentObject == null)
    {
      return -1;
    }

    int count = 0;
    DIDLContainer parent = currentObject.getParentContainer();
    while (parent != null)
    {
      count++;
      parent = parent.getParentContainer();
    }
    return count;
  }

  /** Retrieves the path to the currently selected object */
  public String[] getPathToCurrentObject()
  {
    if (currentObject == null)
    {
      return null;
    }

    int count = 0;
    DIDLContainer parent = currentObject.getParentContainer();
    while (parent != null)
    {
      count++;
      parent = parent.getParentContainer();
    }

    String[] result = new String[count];
    count--;
    parent = currentObject.getParentContainer();
    while (parent != null)
    {
      result[count] = parent.getTitle();
      count--;
      parent = parent.getParentContainer();
    }
    return result;
  }

  /** Selects a new object in the media server */
  public void setCurrentObject(DIDLObject didlObject)
  {
    currentObject = didlObject;
  }

  /** Goes to the parent */
  public void toParent()
  {
    if (currentObject.getParentContainer() != null)
    {
      currentObject = currentObject.getParentContainer();
    }
  }

  /** Goes to a child */
  public boolean toChild(String objectID)
  {
    if (currentObject instanceof DIDLContainer)
    {
      DIDLObject child = ((DIDLContainer)currentObject).getChild(objectID);
      if (child != null)
      {
        currentObject = child;
        return true;
      }
    }
    return false;
  }

  /** Goes to another entry in the current container */
  public boolean toDIDLObject(String objectID)
  {
    DIDLContainer parent = currentObject.getParentContainer();
    if (parent != null)
    {
      DIDLObject child = parent.getChild(objectID);
      if (child != null)
      {
        currentObject = child;
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves a specific object from the whole known tree.
   * 
   * @param objectID
   *          ID of the requested object
   * 
   * @return The object with that ID or null
   */
  public DIDLObject getObject(String objectID)
  {
    return contentRootObject.getRecursiveChild(objectID);
  }

  /**
   * Retrieves a specific container from the whole known tree.
   * 
   * @param objectID
   *          ID of the requested container
   * 
   * @return The container with that ID or null
   */
  public DIDLContainer getContainer(String objectID)
  {
    DIDLObject didlObject = contentRootObject.getRecursiveChild(objectID);
    if (didlObject != null && didlObject instanceof DIDLContainer)
    {
      return (DIDLContainer)didlObject;
    }

    return null;
  }

  /** Enumerates the current container in another thread. The method returns immediately. */
  public void enumerateCurrentContainer()
  {
    DIDLContainer currentContainer = getCurrentContainer();
    if (currentContainer != null)
    {
      if (!isPendingContainer(currentContainer.getID()))
      {
        pendingContainerList.add(currentContainer.getID());
      }
    }
  }

  /** Enumerates a container in another thread. The method returns immediately. */
  public void enumerateContainer(String containerID)
  {
    if (!isPendingContainer(containerID))
    {
      pendingContainerList.add(containerID);
    }
  }

  /** Checks if there are containers waiting for enumeration */
  public boolean hasPendingContainers()
  {
    return pendingContainerList.size() > 0;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content creation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if the media server supports object creation.
   * 
   * @return True if objects can be created, false otherwise
   */
  public boolean supportsCreateObject()
  {
    CPService cpContentDirectoryService =
      getCPDevice().getCPServiceByType("urn:schemas-upnp-org:service:ContentDirectory:1");
    if (cpContentDirectoryService != null)
    {
      CPAction action = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_CREATE_OBJECT);

      return action != null;
    }

    return false;
  }

  /**
   * Checks if the media server supports object deletion.
   * 
   * @return True if objects can be deleted, false otherwise
   */
  public boolean supportsDestroyObject()
  {
    CPService cpContentDirectoryService =
      getCPDevice().getCPServiceByType("urn:schemas-upnp-org:service:ContentDirectory:1");
    if (cpContentDirectoryService != null)
    {
      CPAction action = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_DESTROY_OBJECT);

      return action != null;
    }

    return false;
  }

  /**
   * Checks if the media server supports resource imports.
   * 
   * @return True if resources can be imported, false otherwise
   */
  public boolean supportsImportResource()
  {
    CPService cpContentDirectoryService =
      getCPDevice().getCPServiceByType("urn:schemas-upnp-org:service:ContentDirectory:1");
    if (cpContentDirectoryService != null)
    {
      CPAction action = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_IMPORT_RESOURCE);

      return action != null;
    }

    return false;
  }

  /**
   * Checks if the media server can create a new object in a certain container.
   * 
   * @param containerID
   *          The ID of the requested container
   * 
   * @return True if an object can be created, false otherwise
   */
  public boolean canCreateObject(String containerID)
  {
    DIDLObject container = getObject(containerID);
    if (container != null)
    {
      return container.isRestricted();
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#createObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public DIDLObject createObject(DIDLObject didlObject, String containerID) throws ActionFailedException
  {
    // wait for all outstanding create requests
    synchronized(creationLock)
    {
      DIDLObject result = contentDirectoryModifier.createObject(didlObject, containerID);
      // add newly created object to container
      if (result != null)
      {
        DIDLContainer container = getContainer(containerID);
        if (container != null)
        {
          synchronized(contentLock)
          {
            addChildToContainer(container, result, container.getContainerUpdateID());
          }
        }
      }
      return result;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#destroyObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject)
   */
  public void destroyObject(DIDLObject didlObject) throws ActionFailedException
  {
    contentDirectoryModifier.destroyObject(didlObject);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#updateObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.util.Vector, java.util.Vector)
   */
  public void updateObject(DIDLObject didlObject, Vector currentTagValues, Vector newTagValues) throws ActionFailedException
  {
    contentDirectoryModifier.updateObject(didlObject, currentTagValues, newTagValues);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#getTransferProgress(long)
   */
  public TransferStatus getTransferProgress(long transferID) throws ActionFailedException
  {
    return contentDirectoryModifier.getTransferProgress(transferID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#importResource(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public long importResource(DIDLObject didlObject, String sourceURI) throws ActionFailedException
  {
    return contentDirectoryModifier.importResource(didlObject, sourceURI);
  }

  /**
   * Checks if the current container has a deprecated system update ID.
   * 
   */
  public boolean isDeprecatedCurrentContainer()
  {
    return getCurrentContainer() != null && getCurrentContainer().getSystemUpdateID() != systemUpdateID;
  }

  public void debugOut(DIDLContainer container, String indentation)
  {
    DIDLContainer[] childList = container.getChildContainerList();
    System.out.println(indentation + container.getTitle() + ", " + container.getChildItemList().length + " items");
    for (int i = 0; i < childList.length; i++)
    {
      debugOut(childList[i], indentation + "  ");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Adds one child object to the container */
  private void addChildToContainer(DIDLContainer container, DIDLObject addedChildren, long containerUpdateID)
  {
    container.addChild(addedChildren);
    container.setContainerUpdateID(containerUpdateID);
  }

  private boolean isPendingContainer(String containerID)
  {
    boolean result = false;
    for (int i = 0; i < pendingContainerList.size(); i++)
    {
      result |= ((String)pendingContainerList.elementAt(i)).equals(containerID);
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Thread //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void run()
  {
    while (!terminateThread)
    {
      // check for containers that wait for enumeration
      if (pendingContainerList.size() > 0)
      {
        String containerID = (String)pendingContainerList.elementAt(0);
        // flag that container is not linked to the root container
        boolean unconnectedContainer = false;

        // try to find container in known tree
        DIDLContainer container = getContainer(containerID);
        if (container == null)
        {
          // System.out.println("Try to enumerate unconnected container: " + containerID);
          // retrieve container metadata
          BrowseResponse browseResponse = contentDirectoryBrowser.browseMetadata(containerID);
          try
          {
            container = (DIDLContainer)browseResponse.getFirstResult();
            // add container to unconnected list
            unconnectedContainerList.add(container);
            // add parent container to list of requested containers
            if (!container.getParentID().equals("-1") && !isPendingContainer(container.getParentID()))
            {
              pendingContainerList.add(container.getParentID());
            }
            unconnectedContainer = true;
          } catch (Exception e)
          {
          }
        }
        // container is valid, enumerate
        if (container != null)
        {
          System.out.println("Enumerate " + container.getTitle() + "(" + containerID + ")...");

          // check if the container is currently empty
          boolean emptyContainer = container.getCurrentChildCount() == 0;
          // if (emptyContainer)
          // {
          // System.out.println(" Container content is unknown");
          // }
          DIDLObject[] completeResult = new DIDLObject[0];

          // browse 5 items a time
          int offset = 0;
          BrowseResponse browseResponse = contentDirectoryBrowser.browseDirectChildren(containerID, offset, 5, "*", "");
          DIDLObject[] result = browseResponse != null ? browseResponse.getResult() : null;

          while (browseResponse != null && result != null && result.length > 0)
          {
            // if container content is not yet known, update immediately
            if (emptyContainer && !unconnectedContainer)
            {
              synchronized(contentLock)
              {
                container.addChilds(result);
                container.setContainerUpdateID(browseResponse.getUpdateID());
              }
              // send event to listeners
              for (int i = 0; i < serverChangeListenerList.size(); i++)
              {
                ((ICPMediaServerStateVariableListener)serverChangeListenerList.get(i)).containerEnumerationUpdate(this,
                  containerID);
              }
            } else
            {
              // container already has content, add to complete result
              DIDLObject[] tempResult = new DIDLObject[completeResult.length + result.length];
              for (int i = 0; i < completeResult.length; i++)
              {
                tempResult[i] = completeResult[i];
              }
              for (int i = 0; i < result.length; i++)
              {
                tempResult[completeResult.length + i] = result[i];
              }
              completeResult = tempResult;
            }
            offset += result.length;
            // continue browsing
            browseResponse = contentDirectoryBrowser.browseDirectChildren(containerID, offset, 5, "*", "");
            result = browseResponse != null ? browseResponse.getResult() : null;
          }
          // update in one step if container already had content
          if (!emptyContainer || unconnectedContainer)
          {
            synchronized(contentLock)
            {
              container.setChildList(completeResult);
              container.setContainerUpdateID(browseResponse.getUpdateID());
            }
          }
          // try to link unconnected containers
          synchronized(contentLock)
          {
            DIDLContainer[] childContainerList = container.getChildContainerList();
            // check all new child containers if their content is already known
            for (int i = 0; i < childContainerList.length; i++)
            {
              Enumeration unconnectedContainers = unconnectedContainerList.elements();
              while (unconnectedContainers.hasMoreElements())
              {
                DIDLContainer currentContainer = (DIDLContainer)unconnectedContainers.nextElement();
                // try to find the current child container in the list of unconnected containers
                if (currentContainer.getID().equals(childContainerList[i].getID()))
                {
                  // System.out.println("Found container " + currentContainer.getTitle() + " in the
                  // unconnected list");
                  // copy reference to childs
                  childContainerList[i].setChildList(currentContainer.getChildList());
                  // remove from unconnected list
                  unconnectedContainerList.remove(currentContainer);
                  // trigger event for child container
                  for (int j = 0; j < serverChangeListenerList.size(); j++)
                  {
                    ((ICPMediaServerStateVariableListener)serverChangeListenerList.get(j)).containerEnumerationFinished(this,
                      currentContainer.getID());
                  }
                }
              }
            }
          }
          // set flag for enumeration
          container.setEnumerated();
          // store system update ID for the enumeration
          container.setSystemUpdateID(systemUpdateID);
        }
        pendingContainerList.remove(0);
        // Event for finish
        if (!unconnectedContainer)
        {
          for (int i = 0; i < serverChangeListenerList.size(); i++)
          {
            ((ICPMediaServerStateVariableListener)serverChangeListenerList.get(i)).containerEnumerationFinished(this,
              containerID);
          }
        }
        // System.out.println("done");
        // if (container != null)
        // debugOut(container, "");
      }
      try
      {
        Thread.sleep(50);
      } catch (Exception ex)
      {
      }
    }
    terminated = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateCPDevice#terminate()
   */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
    super.terminate();
  }

}

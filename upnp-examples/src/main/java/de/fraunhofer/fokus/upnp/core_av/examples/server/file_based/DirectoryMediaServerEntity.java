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
package de.fraunhofer.fokus.upnp.core_av.examples.server.file_based;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLImageItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLMusicTrack;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;
import de.fraunhofer.fokus.upnp.core_av.server.BrowseResponse;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentHelper;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentModifier;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentProvider;
import de.fraunhofer.fokus.upnp.core_av.server.MediaServerConstant;
import de.fraunhofer.fokus.upnp.core_av.server.MediaServerDevice;
import de.fraunhofer.fokus.upnp.core_av.server.TransferStatus;
import de.fraunhofer.fokus.upnp.soap.SOAPHeaderBuilder;
import de.fraunhofer.fokus.upnp.soap.SOAPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.FileExtensionHelper;
import de.fraunhofer.fokus.upnp.util.HighResTimerHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This media server provides all files found under a certain subdirectory. It does not use a database, only data found
 * in the files (mp3 tags etc.). This media server supports createObject.
 * 
 * @author Alexander Koenig
 */
public class DirectoryMediaServerEntity extends TemplateEntity implements
  IMediaServerContentProvider,
  IMediaServerContentModifier,
  IMediaServerContentHelper
{
  public static final String  ABS_NAME                         = "AbsName";

  private static final String PATH_ID                          = "Path_ID";

  private static final String RELATIVE_PATH_ID                 = "Relative_Path_ID";

  private static final String NEW_OBJECT_FILE_NAME             = "createObject";

  private static final String NEW_OBJECT_FILE_EXTENSION        = "xml";

  private static final String PATH_SEPARATOR                   = System.getProperty("file.separator");

  private Hashtable           didlObjectFromImportURIHashtable = new Hashtable();

  private int                 transferID                       = 1;

  /** Vector with all ongoing transfers */
  private Vector              transferList                     = new Vector();

  /** Vector with all completed transfers */
  private Vector              completedTransferList            = new Vector();

  /** Sync object for transfer list */
  private Object              transferLock                     = new Object();

  /** Sync object for media server content */
  private Object              contentLock                      = new Object();

  private DIDLContainer       rootContainer;

  private String              rootDirectory                    = "/home/mp3";

  private MediaServerDevice   mediaServerDevice;

  /** Flag to start the media server read-only */
  private boolean             readOnly                         = true;

  /** Creates a new instance of DirectoryMediaServerEntity */
  public DirectoryMediaServerEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    if (startupConfiguration == null)
    {
      startupConfiguration = getStartupConfiguration();
    }
    DeviceStartupConfiguration deviceStartupConfiguration =
      (DeviceStartupConfiguration)startupConfiguration.getSingleDeviceStartupConfiguration();

    // force runDelayed
    deviceStartupConfiguration.setRunDelayed(true);
    this.readOnly = deviceStartupConfiguration.getBooleanProperty("ReadOnly");
    this.rootDirectory = (String)deviceStartupConfiguration.getWebServerDirectoryList().elementAt(0);

    rootContainer = new DIDLContainer("Medienserver", "0");
    // store directory in management hashtable
    rootContainer.getManagementHashtable().put(PATH_ID, this.rootDirectory);
    rootContainer.getManagementHashtable().put(RELATIVE_PATH_ID, "");
    rootContainer.setWriteStatus(MediaServerConstant.WRITE_STATUS_WRITABLE);
    rootContainer.setRestricted(readOnly ? "1" : "0");
    // read root directory
    DIDLObject[] rootChildren = readContainerContentFromFileSystem(rootContainer);
    if (rootChildren != null)
    {
      System.out.println("Found " + rootChildren.length + " entries in root directory" + rootDirectory);
      System.out.println(readOnly ? "Media server is read-only" : "Media server is modifiable");
      rootContainer.setChildList(rootChildren);
    } else
    {
      System.out.println("Root directory " + rootDirectory + " is empty");
    }
    try
    {
      // start media server
      mediaServerDevice = new MediaServerDevice(this, startupConfiguration);
      mediaServerDevice.setContentProvider(this);
      if (!readOnly)
      {
        mediaServerDevice.setContentModifier(this);
      }
      mediaServerDevice.setContentHelper(this);
      mediaServerDevice.runDelayed();

      setTemplateDevice(mediaServerDevice);
    } catch (Exception e)
    {
      System.out.println("Can't create media server. Exit application." + e.getMessage());

      System.exit(0);
    }
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    DirectoryMediaServerEntity mediaServerEntity = null;
    if (args.length > 0)
    {
      UPnPStartupConfiguration startupConfiguration = new UPnPStartupConfiguration(args[0]);
      mediaServerEntity = new DirectoryMediaServerEntity(startupConfiguration);
    } else
    {
      mediaServerEntity = new DirectoryMediaServerEntity(null);
    }
    testProcessingTime(mediaServerEntity);
  }

  /** This method is used for performance evaluation, using SOAP caching or not */
  public static void testProcessingTime(DirectoryMediaServerEntity mediaServerEntity)
  {
    try
    {
      String serviceType = UPnPAVConstant.CONTENT_DIRECTORY_SERVICE_TYPE;

      MediaServerDevice mediaServerDevice = (MediaServerDevice)mediaServerEntity.getTemplateDevice();

      DeviceService contentDirectoryService = mediaServerDevice.getDeviceServiceByType(serviceType);
      Action browseAction = contentDirectoryService.getAction("Browse");
      browseAction.getArgument("ObjectID").setValue("0");
      browseAction.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
      browseAction.getArgument("Filter").setValue("*");
      browseAction.getArgument("StartingIndex").setValueFromString("0");
      browseAction.getArgument("RequestedCount").setValueFromString("10");

      String browseInnerBodyString = SOAPMessageBuilder.buildActionRequestInnerBody(serviceType, browseAction);
      String browseBodyString = SOAPMessageBuilder.buildEnvelope(browseInnerBodyString);

      byte[] browseBody = StringHelper.utf8StringToByteArray(browseBodyString);

      String browseHeader =
        SOAPHeaderBuilder.buildActionRequestHeader(new URL(contentDirectoryService.getControlURL("localhost")),
          serviceType,
          browseAction,
          null,
          browseBody.length,
          false);

      HTTPMessageObject browseRequest =
        new HTTPMessageObject(browseHeader, browseBody, null, IPHelper.toSocketAddress("localhost:80"));

      int runCount = 1000;
      System.out.println("Wait for system to settle down");
      ThreadHelper.sleep(10000);

      System.out.println("Start uncached benchmark");
      browseAction.setCacheable(false);
      long time = System.currentTimeMillis();

      // time for the actual processing in microseconds. Removed from overall time to
      // get net time for XML parsing
      HighResTimerHelper.PERFORMANCE_CORRECTION = 0;

      for (int i = 0; i < runCount; i++)
      {
        mediaServerDevice.getHTTPMessageProcessor().processMessage(browseRequest);
      }
      long grossInvocationTime = System.currentTimeMillis() - time;
      long netInvocationTime = grossInvocationTime - HighResTimerHelper.PERFORMANCE_CORRECTION / 1000;
      System.out.println("Gross time for " + runCount + " runs is " + grossInvocationTime + " ms");
      System.out.println("Net time for " + runCount + " runs is " + netInvocationTime + " ms");

      System.out.println("Start cached benchmark");
      browseAction.setCacheable(true);
      // process one time to create cache entry
      mediaServerDevice.getHTTPMessageProcessor().processMessage(browseRequest);

      time = System.currentTimeMillis();

      // time for the actual processing in microseconds. Removed from overall time to
      // get net time for XML parsing
      HighResTimerHelper.PERFORMANCE_CORRECTION = 0;

      for (int i = 0; i < runCount; i++)
      {
        mediaServerDevice.getHTTPMessageProcessor().processMessage(browseRequest);
      }
      grossInvocationTime = System.currentTimeMillis() - time;
      netInvocationTime = grossInvocationTime - HighResTimerHelper.PERFORMANCE_CORRECTION / 1000;
      System.out.println("Gross time for " + runCount + " runs is " + grossInvocationTime + " ms");
      System.out.println("Net time for " + runCount + " runs is " + netInvocationTime + " ms");

    } catch (Exception e)
    {
      System.out.println("Error: " + e.getMessage());
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content provider implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public long getSystemUpdateID()
  {
    return mediaServerDevice.getContentDirectory().getSystemUpdateID();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#browseMetadata(java.lang.String)
   */
  public BrowseResponse browseMetadata(String objectID)
  {
    DIDLObject didlObject = rootContainer.getRecursiveChild(objectID);
    if (didlObject != null)
    {
      if (didlObject.getParentContainer() != null)
      {
        return new BrowseResponse(didlObject, didlObject.getParentContainer().getContainerUpdateID());
      }

      return new BrowseResponse(didlObject, mediaServerDevice.getContentDirectory().getSystemUpdateID());
    }
    System.out.println("Could not find object with ID: " + objectID);
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#browseDirectChildren(java.lang.String)
   */
  public BrowseResponse browseDirectChildren(String objectID)
  {
    return browseDirectChildren(objectID, 0, 0, "*", "");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#browseDirectChildren(java.lang.String,
   *      int, int, java.lang.String, java.lang.String)
   */
  public BrowseResponse browseDirectChildren(String objectID,
    int startingIndex,
    int requestedCount,
    String filter,
    String sortCriteria)
  {
    DIDLObject didlObject = rootContainer.getRecursiveChild(objectID);
    if (didlObject instanceof DIDLContainer)
    {
      DIDLContainer currentContainer = (DIDLContainer)didlObject;
      DIDLObject[] childList = currentContainer.getChildList();
      // folder was not yet read or is deprecated
      if (childList == null ||
        // update folder for each new browse
        startingIndex == 0 ||
        currentContainer.getSystemUpdateID() != mediaServerDevice.getContentDirectory().getSystemUpdateID())
      {
        // prevent parallel content updates
        synchronized(contentLock)
        {
          // read children from file system
          childList = readContainerContentFromFileSystem(currentContainer);
          if (childList != null)
          {
            currentContainer.setChildList(childList);
            currentContainer.setSystemUpdateID(mediaServerDevice.getContentDirectory().getSystemUpdateID());
          }
        }
      }
      // check if data is now available
      if (childList != null)
      {
        // split result depending on request
        int resultCount = childList.length - startingIndex;
        if (requestedCount != 0)
        {
          resultCount = Math.min(startingIndex + requestedCount, childList.length) - startingIndex;
        }
        if (resultCount <= 0)
        {
          return new BrowseResponse(null, 0, childList.length, currentContainer.getContainerUpdateID());
        }

        DIDLObject[] result = new DIDLObject[resultCount];
        for (int i = 0; i < resultCount; i++)
        {
          result[i] = childList[i + startingIndex];
        }
        return new BrowseResponse(result, result.length, childList.length, currentContainer.getContainerUpdateID());
      }
    }
    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content helper implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentHelper#getDIDLItem(java.lang.String)
   */
  public DIDLObject getDIDLItemByImportURI(String importURI)
  {
    try
    {
      URL importURL = new URL(importURI);
      importURI = importURL.getPath();
      if (importURI.startsWith("/"))
      {
        importURI = importURI.substring(1);
      }
    } catch (Exception e)
    {
    }
    if (didlObjectFromImportURIHashtable.containsKey(importURI))
    {
      return (DIDLObject)didlObjectFromImportURIHashtable.get(importURI);
    }

    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content modifier implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#createObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public DIDLObject createObject(DIDLObject didlObject, String containerID) throws ActionFailedException
  {
    DIDLObject parentContainerObject = rootContainer.getRecursiveChild(containerID);
    if (parentContainerObject instanceof DIDLContainer)
    {
      return createObjectInFileSystem(didlObject, (DIDLContainer)parentContainerObject);
    }
    throw new ActionFailedException(710, "No such container");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#destroyObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject)
   */
  public void destroyObject(DIDLObject didlObject) throws ActionFailedException
  {
    if (didlObject.isRestricted())
    {
      throw new ActionFailedException(711, "Restricted object");
    }

    DIDLContainer container = didlObject.getParentContainer();
    if (container != null)
    {
      if (container.isRestricted())
      {
        throw new ActionFailedException(713, "Restricted parent object");
      }

      if (removeObjectFromFileSystem(didlObject, container))
      {
        return;
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#updateObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.util.Vector, java.util.Vector)
   */
  public void updateObject(DIDLObject didlObject, Vector currentTagValues, Vector newTagValues) throws ActionFailedException
  {
    // TODO: implement updateObject
    throw new ActionFailedException(711, "Restricted object");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#getTransferProgress(int)
   */
  public TransferStatus getTransferProgress(long transferID) throws ActionFailedException
  {
    for (int i = 0; i < transferList.size(); i++)
    {
      ImportResourceThread currentThread = (ImportResourceThread)transferList.elementAt(i);
      if (currentThread.getTransferID() == transferID)
      {
        return currentThread.getTransferStatus();
      }
    }
    for (int i = 0; i < completedTransferList.size(); i++)
    {
      ImportResourceThread currentThread = (ImportResourceThread)completedTransferList.elementAt(i);
      if (currentThread.getTransferID() == transferID)
      {
        return currentThread.getTransferStatus();
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#importResource(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public long importResource(DIDLObject didlObject, String sourceURI) throws ActionFailedException
  {
    try
    {
      URL url = new URL(sourceURI);
      int retries = 0;
      URLConnection urlConnection = url.openConnection();
      // open resource stream, try multiple times
      while (urlConnection.getContentLength() == -1 && retries < 3)
      {
        try
        {
          Thread.sleep(200);
        } catch (Exception e)
        {
        }
        urlConnection = url.openConnection();
        retries++;
      }

      transferID = (transferID + 1) % 10000;

      // create thread for importing
      ImportResourceThread importResourceThread = new ImportResourceThread(this, didlObject, urlConnection, transferID);

      synchronized(transferLock)
      {
        // store thread in list
        transferList.add(importResourceThread);

        // build new transferID list
        String transferIDs = buildTransferIDs();

        // update content directory
        mediaServerDevice.getContentDirectory().transferIDsChanged(transferIDs);
      }
      System.out.println("TransferID for " + sourceURI + " is " + transferID);

      return transferID;
    } catch (Exception e)
    {
    }

    throw new ActionFailedException(501, "Action failed");
  }

  /** Event that a transfer has been finished */
  public void transferFinished(ImportResourceThread importResourceThread)
  {
    // inform content directory about change
    mediaServerDevice.getContentDirectory().contentChanged();

    synchronized(transferLock)
    {
      transferList.remove(importResourceThread);
      completedTransferList.add(importResourceThread);

      // build new transferID list
      String transferIDs = buildTransferIDs();

      // update content directory
      mediaServerDevice.getContentDirectory().transferIDsChanged(transferIDs);
    }
  }

  /** Event that a transfer is deprecated */
  public void transferDeprecated(ImportResourceThread importResourceThread)
  {
    completedTransferList.remove(importResourceThread);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Adds a new resource to a DIDL object.
   * 
   * @param didlObject
   * @param resourceType
   * @param relativePathName
   * @param fileSize
   */
  private void addResource(DIDLObject didlObject, String resourceType, String relativePathName, long fileSize)
  {
    DIDLResource resource =
      new DIDLResource(resourceType, URLHelper.escapeURL((relativePathName.startsWith("/")
        ? relativePathName.substring(1) : relativePathName)));

    resource.setSize(fileSize + "");
    // remove leading "/"
    didlObject.addResource(resource);
  }

  /** Removes an incomplete item from the hash table */
  public void removeIncompleteItemFromHashtable(String importURI)
  {
    if (importURI.startsWith("/"))
    {
      importURI = importURI.substring(1);
    }
    Object result = didlObjectFromImportURIHashtable.remove(importURI);
    if (result == null)
    {
      System.out.println("Incomplete item not found");
    } else
    {
      // System.out.println("Incomplete item removed");
    }
  }

  /** Read a new subdirectory in the file system and build appropriate DIDL objects */
  private DIDLObject[] readContainerContentFromFileSystem(DIDLContainer container)
  {
    // retrieve directories from DIDL container
    String path = (String)container.getManagementHashtable().get(PATH_ID);
    String relativePath = (String)container.getManagementHashtable().get(RELATIVE_PATH_ID);

    File currentFile = new File(path);
    if (currentFile.exists() && currentFile.isDirectory())
    {
      File[] children = currentFile.listFiles();
      Vector result = new Vector();
      // check all files in the current directory
      for (int i = 0; i < children.length; i++)
      {
        String name = children[i].getName();
        String dispName = StringHelper.convertUmlauts(children[i].getName());
        String absName = path + PATH_SEPARATOR + name;
        String relativePathName = (relativePath.length() > 0 ? relativePath + PATH_SEPARATOR : "") + name;

        // build id from absolute path
        String id = DigestHelper.hashToSecurityID(DigestHelper.calculateSHAHashForString(absName));

        if (children[i].isDirectory())
        {
          // create new container
          DIDLContainer childContainer = new DIDLContainer(dispName, id);
          // store directory in management hashtable
          childContainer.getManagementHashtable().put(PATH_ID, absName);
          childContainer.getManagementHashtable().put(RELATIVE_PATH_ID, relativePathName);
          childContainer.setRestricted(readOnly ? "1" : "0");

          result.add(childContainer);
        }
        // check file type
        if (children[i].isFile() && !children[i].isHidden())
        {
          // add audio files
          if (FileExtensionHelper.isAudioFile(name))
          {
            DIDLMusicTrack track = new DIDLMusicTrack(dispName, id);
            String protocolInfo = FileExtensionHelper.getProtocolInfoByFileExtension(name);

            // add resource
            addResource(track, protocolInfo, relativePathName, children[i].length());
            track.setRestricted(readOnly ? "1" : "0");
            // TO-DO: add meta-data

            result.add(track);
          }
          // add images
          if (FileExtensionHelper.isImageFile(name))
          {
            DIDLImageItem image = new DIDLImageItem(dispName, id);
            String protocolInfo = FileExtensionHelper.getProtocolInfoByFileExtension(name);

            // add resource
            addResource(image, protocolInfo, relativePathName, children[i].length());
            image.setRestricted(readOnly ? "1" : "0");
            // TO-DO: add meta-data

            result.add(image);
          }
          // add video files
          if (FileExtensionHelper.isVideoFile(name))
          {
            DIDLItem video = new DIDLItem(dispName, id);
            String protocolInfo = FileExtensionHelper.getProtocolInfoByFileExtension(name);

            // add resource
            addResource(video, protocolInfo, relativePathName, children[i].length());
            video.setRestricted(readOnly ? "1" : "0");
            // TO-DO: add meta-data

            result.add(video);
          }
          // add incomplete items
          if (name.startsWith(NEW_OBJECT_FILE_NAME) &&
            FileExtensionHelper.getFileExtension(name).equals(NEW_OBJECT_FILE_EXTENSION))
          {
            // retrieve content of file
            try
            {
              IncompleteObjectHandler createObjectHandler = new IncompleteObjectHandler();
              // parse file content
              createObjectHandler.parse(children[i]);

              // check if an incomplete object was found
              DIDLObject incompleteObject = createObjectHandler.getDIDLObject();
              // add result to list
              if (incompleteObject != null && createObjectHandler.getAbsName().length() != 0)
              {
                result.add(incompleteObject);
                // check if object is already in hash table
                String importURI = incompleteObject.getFirstResource().getImportURI();
                if (!didlObjectFromImportURIHashtable.containsKey(importURI))
                {
                  System.out.println("Add incomplete object to hash table with key " + importURI);
                  didlObjectFromImportURIHashtable.put(importURI, incompleteObject);
                }
              }
            } catch (Exception e)
            {
              e.printStackTrace();
            }
          }
        }
      }
      Collections.sort(result, new DIDLComparator(""));
      // copy references to array
      DIDLObject[] resultArray = new DIDLObject[result.size()];
      for (int i = 0; i < result.size(); i++)
      {
        resultArray[i] = (DIDLObject)result.elementAt(i);
      }
      return resultArray;
    }
    return null;
  }

  /**
   * Creates a new object in the file system.
   * 
   * @param requestedObject
   *          The new object
   * @param container
   *          The parent container
   * 
   * @return The created object or null
   */
  private DIDLObject createObjectInFileSystem(DIDLObject requestedObject, DIDLContainer container)
  {
    if (readOnly)
    {
      return null;
    }

    // retrieve directories from DIDL container
    String path = (String)container.getManagementHashtable().get(PATH_ID);
    String relativePath = (String)container.getManagementHashtable().get(RELATIVE_PATH_ID);

    boolean knownTitle = false;
    for (int i = 0; i < container.getCurrentChildCount(); i++)
    {
      knownTitle = knownTitle || container.getChildList()[i].getTitle().equals(requestedObject.getTitle());
    }
    if (knownTitle)
    {
      System.out.println("Title " + requestedObject.getTitle() + " is already in use");
      return null;
    }

    // check if a folder or a file should be created
    if (requestedObject instanceof DIDLContainer)
    {
      String absName = path + PATH_SEPARATOR + requestedObject.getTitle();
      String relativePathName = relativePath + PATH_SEPARATOR + requestedObject.getTitle();
      // check if folder name already exists
      if (new File(absName).exists())
      {
        System.out.println("Folder already exists");
        return null;
      }
      // build id from absolute folder name to ensure consistency
      String id = DigestHelper.hashToSecurityID(DigestHelper.calculateSHAHashForString(absName));
      requestedObject.setID(id);
      // try to create folder
      if (new File(absName).mkdir())
      {
        synchronized(contentLock)
        {
          // update management information
          DIDLContainer newChildContainer = (DIDLContainer)requestedObject;
          newChildContainer.getManagementHashtable().put(PATH_ID, absName);
          newChildContainer.getManagementHashtable().put(RELATIVE_PATH_ID, relativePathName);
          newChildContainer.setRestricted("0");

          // add to parent container
          container.addChild(requestedObject);
        }
        // inform content directory about change
        mediaServerDevice.getContentDirectory().contentChanged();

        return requestedObject;
      }
    } else
    {
      // create absolute name for final file
      String absName = path + PATH_SEPARATOR + requestedObject.getTitle();

      // check if file name already exists
      if (new File(absName).exists())
      {
        // try to find next unused file name
        int i = 1;
        while (new File(absName + i).exists())
        {
          i++;
        }

        absName = absName + i;
      }
      // absName now holds the name of the file after importResource()

      // build id from absolute file name to ensure consistency
      String id = DigestHelper.hashToSecurityID(DigestHelper.calculateSHAHashForString(absName));

      requestedObject.setID(id);

      // try to find next unused file name in root directory for temporary file
      int i = 1;
      while (new File(path + PATH_SEPARATOR + NEW_OBJECT_FILE_NAME + i + "." + NEW_OBJECT_FILE_EXTENSION).exists())
      {
        i++;
      }

      // found a new file name
      String temporaryFileName = path + PATH_SEPARATOR + NEW_OBJECT_FILE_NAME + i + "." + NEW_OBJECT_FILE_EXTENSION;

      // check if the object already has a resource
      if (requestedObject.getFirstResource() != null)
      {
        // check if the resource needs an importURI
        if (requestedObject.getFirstResource().getValue() == null)
        {
          // build the importURI from the temporary file name
          String importURI = URLHelper.escapeURL(temporaryFileName);

          requestedObject.getFirstResource().setImportURI(importURI);
        }
      } else
      {
        // create new resource with importURI
        DIDLResource resource = new DIDLResource("*:*:*:*", null);
        // build the importURI from the temporary file name
        String importURI = URLHelper.escapeURL(temporaryFileName);
        resource.setImportURI(importURI);

        requestedObject.addResource(resource);
      }
      try
      {
        FileWriter fileWriter = new FileWriter(temporaryFileName);

        // create an XML description that can be used to later create the file in the requested
        // directory
        fileWriter.write("<IncompleteItem>");
        fileWriter.write("<" + ABS_NAME + ">" + StringHelper.xmlToEscapedString(absName) + "</" + ABS_NAME + ">");
        fileWriter.write(requestedObject.toXMLDescription("*", ""));
        fileWriter.write("</IncompleteItem>");
        fileWriter.close();

        synchronized(contentLock)
        {
          // add object to hash table
          String importURI = requestedObject.getFirstResource().getImportURI();
          if (!didlObjectFromImportURIHashtable.containsKey(importURI))
          {
            // System.out.println("Add import URI " + importURI + " for new object " +
            // requestedObject.getTitle());
            didlObjectFromImportURIHashtable.put(importURI, requestedObject);
          }
          // add new item to container
          container.addChild(requestedObject);
        }
        // inform content directory about change
        mediaServerDevice.getContentDirectory().contentChanged();

        return requestedObject;
      } catch (Exception ex)
      {
      }
    }
    return null;
  }

  /**
   * Deletes a object from the file system.
   * 
   * @param requestedObject
   *          The object that should be deleted
   * @param container
   *          The parent container
   * 
   * @return True if the entry was deleted, false otherwise
   */
  private boolean removeObjectFromFileSystem(DIDLObject requestedObject, DIDLContainer container)
  {
    if (readOnly)
    {
      return false;
    }

    // retrieve directories
    String path = (String)container.getManagementHashtable().get(PATH_ID);
    String absName = path + PATH_SEPARATOR + requestedObject.getTitle();

    File deleteFile = new File(absName);
    if (deleteFile.exists())
    {
      if (deleteFile.isDirectory())
      {
        // recursively remove all subdirectories and files

      }
      if (deleteFile.isFile() && deleteFile.delete())
      {
        // inform content directory about change
        mediaServerDevice.getContentDirectory().contentChanged();

        return true;
      }
    }
    // file could be incomplete resource
    try
    {
      String importURI = requestedObject.getFirstResource().getImportURI();
      String importURIFileName = URLHelper.escapedURLToString(importURI);

      File importURIFile = new File(importURIFileName);

      // delete importURI object
      if (importURIFile.exists() && importURIFile.delete())
      {
        // delete from hashtable
        removeIncompleteItemFromHashtable(importURI);

        // inform content directory about change
        mediaServerDevice.getContentDirectory().contentChanged();

        return true;
      }
    } catch (Exception e)
    {
    }

    return false;
  }

  /** Builds the current list of transfer IDs */
  private String buildTransferIDs()
  {
    String result = "";
    for (int i = 0; i < transferList.size(); i++)
    {
      result += (i == 0 ? "" : ",") + ((ImportResourceThread)transferList.elementAt(i)).getTransferID();
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private classes //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  private class DIDLComparator implements Comparator
  {
    private String sortCriteria = "";

    public DIDLComparator(String sort)
    {
      sortCriteria = sort;
    }

    public int compare(Object a, Object b)
    {
      DIDLObject didlA = (DIDLObject)a;
      DIDLObject didlB = (DIDLObject)b;

      if (didlA instanceof DIDLContainer && !(didlB instanceof DIDLContainer))
      {
        return -1;
      }

      if (!(didlA instanceof DIDLContainer) && didlB instanceof DIDLContainer)
      {
        return 1;
      }

      if (sortCriteria.equals("-dc:title"))
      {
        return didlB.getTitle().compareTo(didlA.getTitle());
      } else
      {
        return didlA.getTitle().compareTo(didlB.getTitle());
      }
    }
  }
}

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
package de.fraunhofer.fokus.upnp.core_av.server;

import de.fraunhofer.fokus.upnp.configuration.UPnPAVDefaults;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLParser;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLParserHandler;
import de.fraunhofer.fokus.upnp.util.HighResTimerHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class provides the actions for a ContentDirectory service. The actual content is provided
 * and managed by a class that implements IMediaServerContentProvider.
 * 
 * @author Alexander Koenig
 * @version 1.0
 */

public class ContentDirectory extends TemplateService implements Runnable
{
  private final static String         SORT_CAPABILITIES = "dc:title";

  // state variables
  private StateVariable               A_ARG_TYPE_ObjectID;

  private StateVariable               A_ARG_TYPE_Result;

  private StateVariable               A_ARG_TYPE_BrowseFlag;

  private StateVariable               A_ARG_TYPE_Filter;

  private StateVariable               A_ARG_TYPE_SortCriteria;

  private StateVariable               A_ARG_TYPE_Index;

  private StateVariable               A_ARG_TYPE_Count;

  private StateVariable               A_ARG_TYPE_UpdateID;

  private StateVariable               A_ARG_TYPE_TransferID;

  private StateVariable               A_ARG_TYPE_TransferStatus;

  private StateVariable               A_ARG_TYPE_TransferLength;

  private StateVariable               A_ARG_TYPE_TransferTotal;

  private StateVariable               A_ARG_TYPE_TagValueList;

  private StateVariable               A_ARG_TYPE_URI;

  private StateVariable               searchCapabilities;

  private StateVariable               sortCapabilities;

  private StateVariable               systemUpdateID;

  private StateVariable               transferIDs;

  // required actions
  private Action                      getSearchCapabilities;

  private Action                      getSortCapabilities;

  private Action                      getSystemUpdateID;

  private Action                      browse;

  private Action                      createObject;

  private Action                      destroyObject;

  private Action                      updateObject;

  private Action                      importResource;

  private Action                      stopTransferProgress;

  private Action                      getTransferProgress;

  private Action                      deleteResource;

  private boolean                     terminateThread   = false;

  private boolean                     terminated        = false;

  private boolean                     contentChange;

  private IMediaServerContentProvider contentProvider;

  private IMediaServerContentModifier contentModifier;

  private IMediaServerContentHelper   contentHelper;

  /** Creates a new instance of ContentDirectory. */
  public ContentDirectory(TemplateDevice device)
  {
    super(device, UPnPAVConstant.CONTENT_DIRECTORY_SERVICE_TYPE, UPnPAVConstant.CONTENT_DIRECTORY_SERVICE_ID, false);
  }

  /** Sets the content provider for this ContentDirectory service */
  public void setContentProvider(IMediaServerContentProvider provider)
  {
    this.contentProvider = provider;
  }

  /** Sets the content modifier for this ContentDirectory service */
  public void setContentModifier(IMediaServerContentModifier modifier)
  {
    this.contentModifier = modifier;
  }

  /**
   * Sets the contentHelper.
   * 
   * @param contentHelper
   *          The contentHelper to set.
   */
  public void setContentHelper(IMediaServerContentHelper contentHelper)
  {
    this.contentHelper = contentHelper;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#setupServiceVariables()
   */
  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    contentChange = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // state Variables
    A_ARG_TYPE_ObjectID = new StateVariable("A_ARG_TYPE_ObjectID", "", false);
    A_ARG_TYPE_Result = new StateVariable("A_ARG_TYPE_Result", "", false);
    A_ARG_TYPE_BrowseFlag = new StateVariable("A_ARG_TYPE_BrowseFlag", "", false);
    A_ARG_TYPE_BrowseFlag.setAllowedValueList(new String[] {
        MediaServerConstant.BROWSE_METADATA, MediaServerConstant.BROWSE_DIRECT_CHILDREN
    });
    A_ARG_TYPE_Filter = new StateVariable("A_ARG_TYPE_Filter", "", false);
    A_ARG_TYPE_SortCriteria = new StateVariable("A_ARG_TYPE_SortCriteria", "", false);
    A_ARG_TYPE_Index = new StateVariable("A_ARG_TYPE_Index", "ui4", 0, false);
    A_ARG_TYPE_Count = new StateVariable("A_ARG_TYPE_Count", "ui4", 0, false);
    A_ARG_TYPE_UpdateID = new StateVariable("A_ARG_TYPE_UpdateID", "ui4", 0, false);
    A_ARG_TYPE_TransferID = new StateVariable("A_ARG_TYPE_TransferID", "ui4", 0, false);
    A_ARG_TYPE_TransferStatus = new StateVariable("A_ARG_TYPE_TransferStatus", "", false);
    A_ARG_TYPE_TransferLength = new StateVariable("A_ARG_TYPE_TransferLength", "", false);
    A_ARG_TYPE_TransferTotal = new StateVariable("A_ARG_TYPE_TransferTotal", "", false);
    A_ARG_TYPE_TagValueList = new StateVariable("A_ARG_TYPE_TagValueList", "", false);
    A_ARG_TYPE_URI = new StateVariable("A_ARG_TYPE_URI", "uri", "", false);

    transferIDs = new StateVariable("TransferIDs", "", true);
    searchCapabilities = new StateVariable("SearchCapabilities", "", false);
    sortCapabilities = new StateVariable("SortCapabilities", SORT_CAPABILITIES, false);
    systemUpdateID = new StateVariable("SystemUpdateID", "ui4", 0, true);

    // create state variable table for modifiable servers
    if (contentModifier != null)
    {
      setStateVariableTable(new StateVariable[] {
          A_ARG_TYPE_ObjectID, A_ARG_TYPE_Result, A_ARG_TYPE_BrowseFlag, A_ARG_TYPE_Filter, A_ARG_TYPE_SortCriteria,
          A_ARG_TYPE_Index, A_ARG_TYPE_Count, A_ARG_TYPE_UpdateID, A_ARG_TYPE_TransferID, A_ARG_TYPE_TransferStatus,
          A_ARG_TYPE_TransferLength, A_ARG_TYPE_TransferTotal, A_ARG_TYPE_TagValueList, A_ARG_TYPE_URI, transferIDs,
          searchCapabilities, sortCapabilities, systemUpdateID
      });
    } else
    {
      // create state variable table for static servers
      setStateVariableTable(new StateVariable[] {
          A_ARG_TYPE_ObjectID, A_ARG_TYPE_Result, A_ARG_TYPE_BrowseFlag, A_ARG_TYPE_Filter, A_ARG_TYPE_SortCriteria,
          A_ARG_TYPE_Index, A_ARG_TYPE_Count, A_ARG_TYPE_UpdateID, A_ARG_TYPE_TagValueList, searchCapabilities,
          sortCapabilities, systemUpdateID
      });
    }
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // Actions
    getSearchCapabilities = new Action("GetSearchCapabilities");
    getSearchCapabilities.setArgumentTable(new Argument[] {
      new Argument("SearchCaps", UPnPConstant.DIRECTION_OUT, searchCapabilities)
    });

    getSortCapabilities = new Action("GetSortCapabilities");
    getSortCapabilities.setArgumentTable(new Argument[] {
      new Argument("SortCaps", UPnPConstant.DIRECTION_OUT, sortCapabilities)
    });

    getSystemUpdateID = new Action("GetSystemUpdateID");
    getSystemUpdateID.setArgumentTable(new Argument[] {
      new Argument("Id", UPnPConstant.DIRECTION_OUT, systemUpdateID)
    });

    browse = new Action("Browse");
    browse.setArgumentTable(new Argument[] {
        new Argument("ObjectID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ObjectID),
        new Argument("BrowseFlag", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_BrowseFlag),
        new Argument("Filter", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Filter),
        new Argument("StartingIndex", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Index),
        new Argument("RequestedCount", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Count),
        new Argument("SortCriteria", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_SortCriteria),
        new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Result),
        new Argument("NumberReturned", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Count),
        new Argument("TotalMatches", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Count),
        new Argument("UpdateID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_UpdateID)
    });
    browse.setCacheable(UPnPAVDefaults.CACHE_BROWSE_ACTION);

    createObject = new Action(UPnPAVConstant.ACTION_CREATE_OBJECT);
    createObject.setArgumentTable(new Argument[] {
        new Argument("ContainerID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ObjectID),
        new Argument("Elements", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_Result),
        new Argument("ObjectID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ObjectID),
        new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Result)
    });

    destroyObject = new Action(UPnPAVConstant.ACTION_DESTROY_OBJECT);
    destroyObject.setArgumentTable(new Argument[] {
      new Argument("ObjectID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ObjectID)
    });

    updateObject = new Action("UpdateObject");
    updateObject.setArgumentTable(new Argument[] {
        new Argument("ObjectID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ObjectID),
        new Argument("CurrentTagValue", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_TagValueList),
        new Argument("NewTagValue", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_TagValueList)
    });

    importResource = new Action(UPnPAVConstant.ACTION_IMPORT_RESOURCE);
    importResource.setArgumentTable(new Argument[] {
        new Argument("SourceURI", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_URI),
        new Argument("DestinationURI", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_URI),
        new Argument("TransferID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_TransferID)
    });

    stopTransferProgress = new Action("StopTransferProgress");
    stopTransferProgress.setArgumentTable(new Argument[] {
      new Argument("TransferID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_TransferID)
    });

    getTransferProgress = new Action("GetTransferProgress");
    getTransferProgress.setArgumentTable(new Argument[] {
        new Argument("TransferID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_TransferID),
        new Argument("TransferStatus", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_TransferStatus),
        new Argument("TransferLength", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_TransferLength),
        new Argument("TransferTotal", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_TransferTotal)
    });

    deleteResource = new Action("DeleteResource");
    deleteResource.setArgumentTable(new Argument[] {
      new Argument("ResourceURI", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_URI)
    });

    if (contentModifier != null)
    {
      setActionTable(new Action[] {
          getSearchCapabilities, getSortCapabilities, getSystemUpdateID, browse, createObject, destroyObject,
          importResource, getTransferProgress
      });
    } else
    // do not show modify actions for static media servers
    {
      setActionTable(new Action[] {
          getSearchCapabilities, getSortCapabilities, getSystemUpdateID, browse
      });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#runService()
   */
  public void runService()
  {
    super.runService();
    Thread thread = new Thread(this);
    thread.setName("ContentDirectory");
    thread.start();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getSearchCapabilities(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(searchCapabilities.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

  }

  public void getSortCapabilities(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(sortCapabilities.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getSystemUpdateID(Argument[] args) throws ActionFailedException
  {
    try
    {
      args[0].setValue(systemUpdateID.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Browse for available objects */
  public void browse(Argument[] args) throws ActionFailedException
  {
    String objectID;
    String browseFlag;
    String filter;
    int startingIndex;
    int requestedCount;
    String sortCriteria;
    String result;
    try
    {
      objectID = args[0].getStringValue();
      browseFlag = args[1].getStringValue();
      filter = (String)args[2].getValue();
      startingIndex = (int)args[3].getNumericValue();
      requestedCount = (int)args[4].getNumericValue();
      sortCriteria = args[5].getStringValue().trim();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (contentProvider == null)
    {
      throw new ActionFailedException(720, "Cannot process the request");
    }
    if (!(sortCriteria.equals("") || sortCriteria.equals("+" + SORT_CAPABILITIES) || sortCriteria.equals("-" +
      SORT_CAPABILITIES)))
    {
      throw new ActionFailedException(709, "Unsupported or invalid sort criteria");
    }
    // build result header
    result =
      "<" + DIDLParserHandler.DIDL_LITE + " " + DIDLParserHandler.DC_XMLNS + " " + DIDLParserHandler.UPNP_XMLNS + " " +
        DIDLParserHandler.UPNP_DIDL_LITE_XMLNS + ">";

    String serverAddress = getServerAddressForCurrentAction();
    String absoluteServerPath = "";
    if (serverAddress != null)
    {
      absoluteServerPath = "http://" + serverAddress + "/";
    }
    BrowseResponse browseResponse = null;

    // metadata
    if (browseFlag.equals(MediaServerConstant.BROWSE_METADATA))
    {
      browseResponse = contentProvider.browseMetadata(objectID);
      if (browseResponse == null)
      {
        throw new ActionFailedException(701, "No such object");
      }

      result += browseResponse.getResult()[0].toXMLDescription(filter, absoluteServerPath);
    }
    // children
    if (browseFlag.equals(MediaServerConstant.BROWSE_DIRECT_CHILDREN))
    {
      long startTime = HighResTimerHelper.getTimeStamp();
      browseResponse = contentProvider.browseDirectChildren(objectID, startingIndex, requestedCount, "*", sortCriteria);
      long endTime = HighResTimerHelper.getTimeStamp();
      HighResTimerHelper.PERFORMANCE_CORRECTION += HighResTimerHelper.getMicroseconds(startTime, endTime);

      if (browseResponse == null)
      {
        throw new ActionFailedException(701, "No such object");
      }

      // build result
      for (int i = 0; i < browseResponse.getNumberReturned(); i++)
      {
        result += browseResponse.getResult()[i].toXMLDescription(filter, absoluteServerPath);
      }
    }

    // build result footer
    result += "</" + DIDLParserHandler.DIDL_LITE + ">";

    // System.out.println("Result of browse action is [\n" + result + "]");

    try
    {
      args[6].setValue(StringHelper.xmlToEscapedString(result));
      args[7].setNumericValue(browseResponse.getNumberReturned());
      args[8].setNumericValue(browseResponse.getTotalMatches());
      args[9].setNumericValue(browseResponse.getUpdateID());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Create a new object */
  public void createObject(Argument[] args) throws ActionFailedException
  {
    String containerID;
    String elements;
    String result;
    try
    {
      containerID = (String)args[0].getValue();
      elements = (String)args[1].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (contentModifier == null)
    {
      throw new ActionFailedException(720, "Cannot process the request");
    }
    // try to find appropriate container
    BrowseResponse browseResponse = contentProvider.browseMetadata(containerID);
    if (browseResponse == null)
    {
      throw new ActionFailedException(710, "No such container");
    }
    DIDLObject container = browseResponse.getFirstResult();
    // check container write status
    if (container.isRestricted())
    {
      throw new ActionFailedException(713, "Restricted parent object");
    }

    // "deescape" is done by SOAP parser, so elements is not changed

    DIDLParser parser = new DIDLParser(elements);

    DIDLObject createdObject = null;
    // check parser for valid content
    if (parser.getDIDLItem() != null)
    {
      createdObject = contentModifier.createObject(parser.getDIDLItem(), containerID);
    }
    if (parser.getDIDLContainer() != null)
    {
      createdObject = contentModifier.createObject(parser.getDIDLContainer(), containerID);
    }

    if (createdObject == null)
    {
      throw new ActionFailedException(712, "Bad metadata");
    }

    // build result header
    result =
      "<" + DIDLParserHandler.DIDL_LITE + " " + DIDLParserHandler.DC_XMLNS + " " + DIDLParserHandler.UPNP_XMLNS + " " +
        DIDLParserHandler.UPNP_DIDL_LITE_XMLNS + ">";

    String serverAddress = getServerAddressForCurrentAction();
    String absoluteServerPath = "";
    if (serverAddress != null)
    {
      absoluteServerPath = "http://" + serverAddress + "/";
    }
    result += createdObject.toXMLDescription("*", absoluteServerPath);

    // build result footer
    result += "</" + DIDLParserHandler.DIDL_LITE + ">";

    try
    {
      args[2].setValue(createdObject.getID());
      args[3].setValue(result);
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Deletes an object */
  public void destroyObject(Argument[] args) throws ActionFailedException
  {
    String objectID;
    try
    {
      objectID = (String)args[0].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (contentModifier == null)
    {
      throw new ActionFailedException(720, "Cannot process the request");
    }
    // try to find object
    BrowseResponse browseResponse = contentProvider.browseMetadata(objectID);
    if (browseResponse == null)
    {
      throw new ActionFailedException(701, "No such object");
    }
    DIDLObject didlObject = browseResponse.getFirstResult();
    // check object restrictions
    if (didlObject.isRestricted())
    {
      throw new ActionFailedException(711, "Restricted object");
    }
    contentModifier.destroyObject(didlObject);
  }

  /** Import a resource for an object */
  public void importResource(Argument[] args) throws ActionFailedException
  {
    String sourceURI;
    String destinationURI;
    long transferID;
    try
    {
      sourceURI = args[0].getURIValue();
      destinationURI = args[1].getURIValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (contentModifier == null || contentHelper == null)
    {
      throw new ActionFailedException(501, "Action failed");
    }
    // try to find item with this importURI
    DIDLObject item = contentHelper.getDIDLItemByImportURI(destinationURI);
    if (item == null)
    {
      throw new ActionFailedException(718, "No such destination resource");
    }
    // check container write status
    if (item.isRestricted())
    {
      throw new ActionFailedException(713, "Restricted parent object");
    }

    transferID = contentModifier.importResource(item, sourceURI);

    try
    {
      args[2].setNumericValue(transferID);
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Retrieves the transfer progress */
  public void getTransferProgress(Argument[] args) throws ActionFailedException
  {
    long transferID;
    try
    {
      transferID = args[0].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (contentModifier == null || contentHelper == null)
    {
      throw new ActionFailedException(501, "Action failed");
    }
    TransferStatus transferStatus = contentModifier.getTransferProgress(transferID);
    if (transferStatus == null)
    {
      throw new ActionFailedException(717, "No such file transfer");
    }
    try
    {
      args[1].setValue(transferStatus.getTransferStatus());
      args[2].setValue(transferStatus.getTransferLength() + "");
      args[3].setValue(transferStatus.getTransferTotal() + "");
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Service handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Content change event from content provider */
  public void contentChanged()
  {
    // only set variable, because state variable SystemUpdateID is moderated with 0.5 Hz
    contentChange = true;
  }

  /**
   * Retrieves the current systemUpdateID.
   * 
   * @return The current systemUpdateID
   */
  public long getSystemUpdateID()
  {
    try
    {
      return systemUpdateID.getNumericValue();
    } catch (Exception e)
    {
    }
    return 0;
  }

  /** TransferID update from content modifier */
  public void transferIDsChanged(String newTransferIDs)
  {
    try
    {
      transferIDs.setValue(newTransferIDs);
      // System.out.println("Set transfer IDs to " + newTransferIDs);
    } catch (Exception e)
    {
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Thread handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** This method handles System- and ContainerUpdate state variables */
  public void run()
  {
    while (!terminateThread)
    {
      // evented system update
      if (contentChange)
      {
        try
        {
          printMessage("Update system update ID");
          systemUpdateID.setNumericValue(systemUpdateID.getNumericValue() + 1);
          contentChange = false;
        } catch (Exception ex)
        {
          logger.error(ex.getMessage());
        }
      }
      try
      {
        Thread.sleep(2000);
      } catch (Exception ex)
      {
      }
    }
    terminated = true;
  }

  public void terminate()
  {
    super.terminate();

    printMessage("Shutdown thread in service " + toString() + "...");

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
  }

}

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

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLParser;
import de.fraunhofer.fokus.upnp.core_av.server.BrowseResponse;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentProvider;
import de.fraunhofer.fokus.upnp.core_av.server.MediaServerConstant;

/**
 * This class calls browse and search for the content directory and parses the results.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class CPContentDirectoryBrowser implements IMediaServerContentProvider
{

  private static Logger        logger              = Logger.getLogger("upnp");

  public static final String   ACTION_SEARCH       = "Search";

  public static final String   ARG_CONTAINER_ID    = "ContainerID";

  public static final String   ARG_SEARCH_CRITERIA = "SearchCriteria";

  public static final String   ARG_FILTER          = "Filter";

  public static final String   ARG_STARTING_INDEX  = "StartingIndex";

  public static final String   ARG_REQUESTED_COUNT = "RequestedCount";

  public static final String   ARG_SORT_CRITERIA   = "SortCriteria";

  private CPService            cpContentDirectoryService;

  private TemplateControlPoint controlPoint;

  /**
   * Creates a new instance of CPContentDirectoryBrowser.
   * 
   * @param serverDevice
   *          The associated media server device
   */
  public CPContentDirectoryBrowser(MediaServerCPDevice mediaServerCPDevice)
  {
    controlPoint = mediaServerCPDevice.getTemplateControlPoint();
    cpContentDirectoryService =
      mediaServerCPDevice.getCPDevice().getCPServiceByType(UPnPAVConstant.CONTENT_DIRECTORY_SERVICE_TYPE);
  }

  /**
   * @param browseParam
   *          the parameter which should be browsed
   * @return a xml structure for the browsed parameter which are expressed in a DIDL conatiner
   *         structure could also return NumberReturned and TotalMatches, but these information is
   *         currently not used
   */
  /*  
    public String browse(String[] browseParam) {
      Action action = cpContentDirectoryService.getAction("Browse");
      
      Argument[] arguments = action.getInArgument();
      
      try {
        if (arguments != null) {
          for (int j = 0; j < arguments.length; j++) {
            arguments[j].setNewValueAsString(browseParam[j]);
          }
        }
        
        action = cpContentDirectoryService.invokeAction(action);
      } catch (Exception e) {
        errorInAVCAction("Browse", arguments, e);
      }
      
      String actionResult = (String)action.getOutArgument("Result").getValue();
      
      logger.info("###Browse(");
      for (int i = 0; i < arguments.length; ++i) {
        Argument a = arguments[i];
        logger.info(a.getName()+"='"+ a.getRelatedStateVariable().getValue() + "'");
      }
      logger.info(") result = \"" + actionResult + "\"");
      return actionResult;
    }
    
  /*
    public String search(String containerID, String searchCriteria,
    	String filter, int startingIndex, int requestedCount,
    	String sortCriteria) 
  	{
      Action action = serviceContentDirectory.getAction(ACTION_SEARCH);
      
      if (action == null) {
        System.err.println("ServiceContentDirectory '' does not implement search-Action.");
        return null;
      }
      Argument[] arguments = action.getInArgument();
      
      if (arguments != null) {
        try {
          for (int counter = 0; counter < arguments.length; ++counter) {
            if (arguments[counter].getName().equals(ARG_CONTAINER_ID)) {
              arguments[counter].setNewValueAsString(containerID);
            } else if (arguments[counter].getName().equals(ARG_SEARCH_CRITERIA)) {
              arguments[counter].setNewValueAsString(searchCriteria);
            } else if (arguments[counter].getName().equals(ARG_FILTER)) {
              arguments[counter].setNewValueAsString(filter);
            } else if (arguments[counter].getName().equals(ARG_STARTING_INDEX)) {
              arguments[counter].setNewValueAsString(startingIndex +
              "");
            } else if (arguments[counter].getName().equals(ARG_REQUESTED_COUNT)) {
              arguments[counter].setNewValueAsString(requestedCount +
              "");
            } else if (arguments[counter].getName().equals(ARG_SORT_CRITERIA)) {
              arguments[counter].setNewValueAsString(sortCriteria);
            }
          }
          
          serviceContentDirectory.invokeAction(action);
        } catch (Exception e) {
          errorInAVCAction(ACTION_SEARCH, arguments, e);
        }
      }
      
      return serviceContentDirectory.getCPStateVariable("A_ARG_TYPE_Result")
      .getValue().toString();
    }
  */

  private void errorInAVCAction(String action, Argument[] arguments, Exception e)
  {
    logger.error("--------------------------------");
    logger.error("Device '" + cpContentDirectoryService.getCPDevice().getFriendlyName() + "': " + action);
    for (int i = 0; i < arguments.length; ++i)
    {
      Argument a = arguments[i];
      logger.error("arg[" + i + "](" + a.getName() + ") ='" + a.getValue() + "'");
    }
    logger.error(e.toString());
    logger.error("--------------------------------");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content provider implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /* (non-Javadoc)
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#getSystemUpdateID()
   */
  public long getSystemUpdateID()
  {
    CPAction action = cpContentDirectoryService.getCPAction("GetSystemUpdateID");
    if (action != null)
    {
      try
      {
        controlPoint.invokeAction(action);

        long result = action.getOutArgument("Id").getNumericValue();

        return result;
      } catch (Exception e)
      {
      }
    }
    return 0;
  }

  /** Returns all children of the root object */
  public BrowseResponse browseRootDirectChildren()
  {
    return browseDirectChildren("0", 0, 0, "*", "");
  }

  /** Returns the metadata for the root object */
  public BrowseResponse browseRootMetadata()
  {
    return browseMetadata("0");
  }

  /* (non-Javadoc)
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#browseDirectChildren(java.lang.String)
   */
  public BrowseResponse browseDirectChildren(String objectID)
  {
    return browseDirectChildren(objectID, 0, 0, "*", "");
  }

  /* (non-Javadoc)
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#browseDirectChildren(java.lang.String, int, int, java.lang.String, java.lang.String)
   */
  public BrowseResponse browseDirectChildren(String objectID,
    int startingIndex,
    int requestedCount,
    String filter,
    String sortCriteria)
  {
    CPAction action = cpContentDirectoryService.getCPAction("Browse");

    if (action != null)
    {
      try
      {
        action.getInArgument("ObjectID").setValue(objectID);
        action.getInArgument("BrowseFlag").setValue(MediaServerConstant.BROWSE_DIRECT_CHILDREN);
        action.getInArgument("Filter").setValue(filter);
        action.getInArgument("StartingIndex").setValue(new Long(startingIndex));
        action.getInArgument("RequestedCount").setValue(new Long(requestedCount));
        action.getInArgument("SortCriteria").setValue(sortCriteria);

        controlPoint.invokeAction(action);

        String actionResult = action.getOutArgument("Result").getStringValue();
        long numberReturned = action.getOutArgument("NumberReturned").getNumericValue();
        long totalMatches = action.getOutArgument("TotalMatches").getNumericValue();
        long containerUpdateID = action.getOutArgument("UpdateID").getNumericValue();

        DIDLParser parser = new DIDLParser(actionResult);
        return new BrowseResponse(parser.getDIDLObjects(), numberReturned, totalMatches, containerUpdateID);

      } catch (Exception e)
      {
        errorInAVCAction("Browse", action.getArgumentTable(), e);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#getDIDLObject(java.lang.String)
   */
  public BrowseResponse browseMetadata(String objectID)
  {
    CPAction action = cpContentDirectoryService.getCPAction("Browse");

    if (action != null)
    {
      try
      {
        action.getInArgument("ObjectID").setValue(objectID);
        action.getInArgument("BrowseFlag").setValue(MediaServerConstant.BROWSE_METADATA);
        action.getInArgument("Filter").setValue("*");
        action.getInArgument("StartingIndex").setValue(new Long(0));
        action.getInArgument("RequestedCount").setValue(new Long(0));
        action.getInArgument("SortCriteria").setValue("");

        controlPoint.invokeAction(action);

        String actionResult = (String)action.getOutArgument("Result").getValue();
        DIDLParser parser = new DIDLParser(actionResult);

        if (parser.getDIDLObject() != null)
        {
          return new BrowseResponse(parser.getDIDLObject(), action.getArgument("UpdateID").getNumericValue());
        }

      } catch (Exception e)
      {
        errorInAVCAction("Browse", action.getArgumentTable(), e);
      }
    }
    return null;
  }

}

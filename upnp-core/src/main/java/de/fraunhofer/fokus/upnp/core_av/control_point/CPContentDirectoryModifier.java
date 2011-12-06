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

import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLParser;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLParserHandler;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentModifier;
import de.fraunhofer.fokus.upnp.core_av.server.TransferStatus;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class can be used to modify external media servers.
 * 
 * @author Alexander Koenig
 * 
 */
public class CPContentDirectoryModifier implements IMediaServerContentModifier
{

  /** Associated logger */
  private static Logger        logger                = Logger.getLogger("upnp");

  public static final String   ARG_CONTAINER_ID      = "ContainerID";

  public static final String   ARG_CURRENT_TAG_VALUE = "CurrentTagValue";

  public static final String   ARG_DESTINATION_URI   = "DestinationURI";

  public static final String   ARG_ELEMENTS          = "Elements";

  public static final String   ARG_NEW_TAG_VALUE     = "NewTagValue";

  public static final String   ARG_OBJECT_ID         = "ObjectID";

  public static final String   ARG_RESULT            = "Result";

  public static final String   ARG_SOURCE_URI        = "SourceURI";

  public static final String   ARG_TRANSFER_ID       = "TransferID";

  public static final String   ARG_TRANSFER_STATUS   = "TransferStatus";

  public static final String   ARG_TRANSFER_LENGTH   = "TransferLength";

  public static final String   ARG_TRANSFER_TOTAL    = "TransferTotal";

  private CPService            cpContentDirectoryService;

  private TemplateControlPoint controlPoint;

  /*
   * Creates a new instance of CPContentDirectoryModifier.
   * 
   * @param serverDevice Associated media server device
   */
  public CPContentDirectoryModifier(MediaServerCPDevice mediaServerCPDevice)
  {
    this.controlPoint = mediaServerCPDevice.getTemplateControlPoint();

    cpContentDirectoryService =
      mediaServerCPDevice.getCPDevice().getCPServiceByType("urn:schemas-upnp-org:service:ContentDirectory:1");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#createObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public DIDLObject createObject(DIDLObject didlObject, String containerID) throws ActionFailedException
  {
    CPAction createObjectAction = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_CREATE_OBJECT);
    if (createObjectAction != null)
    {
      // create elements value for new object
      // build result header
      String elements =
        "<" + DIDLParserHandler.DIDL_LITE + " " + DIDLParserHandler.DC_XMLNS + " " + DIDLParserHandler.UPNP_XMLNS +
          " " + DIDLParserHandler.UPNP_DIDL_LITE_XMLNS + ">";

      // the new object either contains absolute or no URLs
      elements += didlObject.toXMLDescription("*", "");

      // build result footer
      elements += "</" + DIDLParserHandler.DIDL_LITE + ">";

      try
      {
        createObjectAction.getArgument(ARG_CONTAINER_ID).setValue(containerID);
        createObjectAction.getArgument(ARG_ELEMENTS).setValue(StringHelper.xmlToEscapedString(elements));

        controlPoint.invokeAction(createObjectAction);

        String result = createObjectAction.getArgument(ARG_RESULT).getStringValue();

        // try to create object from description
        DIDLParser parser = new DIDLParser(result);
        return parser.getDIDLObject();

      } catch (ActionFailedException afe)
      {
        logger.warn(afe.getMessage());
        throw afe;
      } catch (Exception e)
      {
        logger.warn(e.getMessage());
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#destroyObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject)
   */
  public void destroyObject(DIDLObject didlObject) throws ActionFailedException
  {
    CPAction action = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_DESTROY_OBJECT);
    if (action != null)
    {
      try
      {
        action.getArgument(ARG_OBJECT_ID).setValue(didlObject.getID());

        controlPoint.invokeAction(action);

        return;
      } catch (ActionFailedException afe)
      {
        logger.warn(afe.getMessage());
        throw afe;
      } catch (Exception e)
      {
        logger.warn(e.getMessage());
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#updateObject(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.util.Vector)
   */
  public void updateObject(DIDLObject didlObject, Vector currentTagValues, Vector newTagValues) throws ActionFailedException
  {
    if (currentTagValues.size() != newTagValues.size())
    {
      throw new ActionFailedException(706, "Parameter mismatch");
    }
    CPAction action = cpContentDirectoryService.getCPAction("UpdateObject");
    if (action != null)
    {
      try
      {
        String currentTagValue = "";
        String newTagValue = "";
        for (int i = 0; i < currentTagValues.size(); i++)
        {
          currentTagValue += i == 0 ? "" : ",";
          currentTagValue += currentTagValues.elementAt(i) == null ? "" : (String)currentTagValues.elementAt(i);

          newTagValue += i == 0 ? "" : ",";
          newTagValue += newTagValues.elementAt(i) == null ? "" : (String)newTagValues.elementAt(i);
        }
        action.getArgument(ARG_OBJECT_ID).setValue(didlObject.getID());
        action.getArgument(ARG_CURRENT_TAG_VALUE).setValue(currentTagValue);
        action.getArgument(ARG_NEW_TAG_VALUE).setValue(newTagValue);

        controlPoint.invokeAction(action);
      } catch (ActionFailedException afe)
      {
        logger.warn(afe.getMessage());
        throw afe;
      } catch (Exception e)
      {
        logger.warn(e.getMessage());
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#getTransferProgress(long)
   */
  public TransferStatus getTransferProgress(long transferID) throws ActionFailedException
  {
    CPAction action = cpContentDirectoryService.getCPAction("GetTransferProgress");
    if (action != null)
    {
      try
      {
        action.getArgument(ARG_TRANSFER_ID).setNumericValue(transferID);

        controlPoint.invokeAction(action);

        TransferStatus result =
          new TransferStatus(action.getArgument(ARG_TRANSFER_STATUS).getStringValue(),
            action.getArgument(ARG_TRANSFER_LENGTH).getNumericValue(),
            action.getArgument(ARG_TRANSFER_TOTAL).getNumericValue());

        return result;
      } catch (ActionFailedException afe)
      {
        logger.warn(afe.getMessage());
        throw afe;
      } catch (Exception e)
      {
        logger.warn(e.getMessage());
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentModifier#importResource(de.fhg.fokus.magic.upnpav.didl.DIDLObject,
   *      java.lang.String)
   */
  public long importResource(DIDLObject didlObject, String sourceURI) throws ActionFailedException
  {
    CPAction action = cpContentDirectoryService.getCPAction(UPnPAVConstant.ACTION_IMPORT_RESOURCE);
    if (action != null)
    {
      try
      {
        action.getArgument(ARG_SOURCE_URI).setValue(sourceURI);
        action.getArgument(ARG_DESTINATION_URI).setValue(didlObject.getFirstResource().getImportURI());

        controlPoint.invokeAction(action);

        long transferID = action.getArgument(ARG_TRANSFER_ID).getNumericValue();

        return transferID;
      } catch (ActionFailedException afe)
      {
        logger.warn(afe.getMessage());
        throw afe;
      } catch (Exception e)
      {
        logger.warn(e.getMessage());
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

}

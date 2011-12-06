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
package de.fraunhofer.fokus.upnp.core_av;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_av.renderer.RendererConstants;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class is the implementation of the ConnectionManager service for the UPnP MediaRenderer and
 * MediaServer. It supports only one active connection.
 * 
 * @author Ie Piu Cu, Thomas Jenschar, Andreas Zisowsky, Alexander Koenig
 */
public class ConnectionManager extends TemplateService
{

  private StateVariable sourceProtocolInfo;

  private StateVariable sinkProtocolInfo;

  private StateVariable currentConnectionIDs;

  private StateVariable A_ARG_TYPE_ConnectionStatus;

  private StateVariable A_ARG_TYPE_ConnectionManager;

  private StateVariable A_ARG_TYPE_Direction;

  private StateVariable A_ARG_TYPE_ProtocolInfo;

  private StateVariable A_ARG_TYPE_ConnectionID;

  private StateVariable A_ARG_TYPE_AVTransportID;

  private StateVariable A_ARG_TYPE_RcsID;

  private Action        getProtocolInfo;

  private Action        getCurrentConnectionIDs;

  private Action        getCurrentConnectionInfo;

  private String        connectionState;

  private String        connectionProtocol;

  private String        connectionDirection;

  private int           avTransportID;

  private int           renderingControlID;

  public ConnectionManager(TemplateDevice device, String sourceProtocolInfo, String sinkProtocolInfo)
  {
    super(device, UPnPAVConstant.CONNECTION_MANAGER_SERVICE_TYPE, UPnPAVConstant.CONNECTION_MANAGER_SERVICE_ID);
    try
    {
      this.sourceProtocolInfo.setValue(sourceProtocolInfo);
      this.sinkProtocolInfo.setValue(sinkProtocolInfo);
    } catch (Exception ex)
    {
      logger.error(ex.getMessage());
    }
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    // initiate variables suitable for a media renderer
    connectionState = RendererConstants.VALUE_UNKNOWN;
    connectionProtocol = "";
    connectionDirection = RendererConstants.VALUE_INPUT;
    avTransportID = 0;
    renderingControlID = 0;
  }

  /** Initializes service specific properties */
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
    sourceProtocolInfo = new StateVariable("SourceProtocolInfo", "", true);
    sinkProtocolInfo = new StateVariable("SinkProtocolInfo", "", true);
    currentConnectionIDs = new StateVariable("CurrentConnectionIDs", "0", true);
    connectionState = RendererConstants.VALUE_UNKNOWN;
    A_ARG_TYPE_ConnectionStatus = new StateVariable("A_ARG_TYPE_ConnectionStatus", connectionState, false);
    A_ARG_TYPE_ConnectionStatus.setAllowedValueList(new String[] {
        "OK", "ContentFormatMismatch", "InsufficientBandwidth", "UnreliableChannel", "Unknown"
    });
    A_ARG_TYPE_ConnectionManager = new StateVariable("A_ARG_TYPE_ConnectionManager", "", false);
    A_ARG_TYPE_Direction = new StateVariable("A_ARG_TYPE_Direction", connectionDirection, false);
    A_ARG_TYPE_Direction.setAllowedValueList(new String[] {
        RendererConstants.VALUE_OUTPUT, RendererConstants.VALUE_INPUT
    });
    A_ARG_TYPE_ProtocolInfo = new StateVariable("A_ARG_TYPE_ProtocolInfo", "", false);
    A_ARG_TYPE_ConnectionID = new StateVariable("A_ARG_TYPE_ConnectionID", 0, false);
    A_ARG_TYPE_AVTransportID = new StateVariable("A_ARG_TYPE_AVTransportID", 0, false);
    A_ARG_TYPE_RcsID = new StateVariable("A_ARG_TYPE_RcsID", 0, false);

    StateVariable[] stVars =
      {
          sourceProtocolInfo, sinkProtocolInfo, currentConnectionIDs, A_ARG_TYPE_ConnectionStatus,
          A_ARG_TYPE_ConnectionManager, A_ARG_TYPE_Direction, A_ARG_TYPE_ProtocolInfo, A_ARG_TYPE_ConnectionID,
          A_ARG_TYPE_AVTransportID, A_ARG_TYPE_RcsID
      };
    setStateVariableTable(stVars);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // Actions
    getProtocolInfo = new Action("GetProtocolInfo");
    getProtocolInfo.setArgumentTable(new Argument[] {
        new Argument("Source", UPnPConstant.DIRECTION_OUT, sourceProtocolInfo),
        new Argument("Sink", UPnPConstant.DIRECTION_OUT, sinkProtocolInfo)
    });

    getCurrentConnectionIDs = new Action("GetCurrentConnectionIDs");
    getCurrentConnectionIDs.setArgumentTable(new Argument[] {
      new Argument("ConnectionIDs", UPnPConstant.DIRECTION_OUT, currentConnectionIDs)
    });

    getCurrentConnectionInfo = new Action("GetCurrentConnectionInfo");
    getCurrentConnectionInfo.setArgumentTable(new Argument[] {
        new Argument("ConnectionID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_ConnectionID),
        new Argument("RcsID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_RcsID),
        new Argument("AVTransportID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_AVTransportID),
        new Argument("ProtocolInfo", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ProtocolInfo),
        new Argument("PeerConnectionManager", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionManager),
        new Argument("PeerConnectionID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionID),
        new Argument("Direction", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_Direction),
        new Argument("Status", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_ConnectionStatus)
    });

    Action[] acts = {
        getProtocolInfo, getCurrentConnectionIDs, getCurrentConnectionInfo
    };
    setActionTable(acts);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getProtocolInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    // format <protocol>:<network>:<contentFormat>:<additionalInfo>
    // value: http-get:*:MIME-type:*
    // rtsp-rtp-udp:*:name of rtp payload type:*
    // internal:IP address:*:*
    try
    {
      args[0].setValue(sourceProtocolInfo.getValue());
      args[1].setValue(sinkProtocolInfo.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getCurrentConnectionIDs(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }

    try
    {
      args[0].setValue(currentConnectionIDs.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getCurrentConnectionInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 8)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (currentConnectionIDs.getValue().equals(""))
    {
      throw new ActionFailedException(706, "Invalid connection reference");
    }

    try
    {
      int connectionID = (int)args[0].getNumericValue();
      if (connectionID == 0)
      {
        // RcsID
        args[1].setNumericValue(renderingControlID);
        // AVTransportID
        args[2].setNumericValue(avTransportID);
        args[3].setValue(connectionProtocol);
        // peer connection manager
        args[4].setValue("");
        // peer connection id
        args[5].setNumericValue(-1);
        args[6].setValue(connectionDirection);
        args[7].setValue(connectionState);
      } else
      {
        throw new ActionFailedException(RendererConstants.CM_ERROR_REFID_NO, RendererConstants.CM_ERROR_REFID);
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Methods for managing //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * public void setConnectionStatus(boolean isOK) { connectionState = (isOK ?
   * RendererConstants.VALUE_OK : RendererConstants.VALUE_UNKNOWN); }
   * 
   * public void setConnectionProtocol(String currentProtocol) { connectionProtocol =
   * currentProtocol; }
   * 
   * public void setConnectionDirection(String currentDirection) { connectionDirection =
   * currentDirection; }
   * 
   * public void setAVTransportConnectionDirection(String currentDirection) { connectionDirection =
   * currentDirection; }
   */
  public void resetConnectionState()
  {
    connectionState = RendererConstants.VALUE_UNKNOWN;
    connectionProtocol = "";
    connectionDirection = RendererConstants.VALUE_INPUT;
    try
    {
      currentConnectionIDs.setValue("");
    } catch (Exception ex)
    {
    }
    avTransportID = -1;
    renderingControlID = -1;
  }

  /**
   * Initializes the connection manager for a new connection. UseAVTransport and useRenderingControl
   * are used to determine whether the connection can be manipulated from external control points.
   */
  public void setConnectionStateForRenderer(String currentProtocol, boolean useAVTransport, boolean useRenderingControl)
  {
    connectionState = UPnPAVConstant.VALUE_OK;
    connectionProtocol = currentProtocol;
    connectionDirection = RendererConstants.VALUE_INPUT;
    try
    {
      currentConnectionIDs.setValue("0");
    } catch (Exception ex)
    {
    }
    avTransportID = useAVTransport ? 0 : -1;
    renderingControlID = useRenderingControl ? 0 : -1;
  }

}

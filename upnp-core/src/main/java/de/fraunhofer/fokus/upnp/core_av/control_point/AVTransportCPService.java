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

import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;

/**
 * This class represents a remote view on an AVTransport service. It can be used to retrieve state
 * variable values for different instance IDs.
 * 
 * @author Alexander Koenig
 * 
 */
public class AVTransportCPService implements ILastChangeEventListener
{

  private CPService                           cpService;

  private TemplateControlPoint                controlPoint;

  private Hashtable                           hashtableFromInstanceIDHashtable = new Hashtable();

  private ICPAVTransportStateVariableListener stateVariableListener;

  /**
   * Creates a new instance of AVTransportCPService.
   * 
   * @param mediaRendererCPDevice
   * @param stateVariableListener
   */
  public AVTransportCPService(MediaRendererCPDevice mediaRendererCPDevice,
    ICPAVTransportStateVariableListener stateVariableListener)
  {
    this.cpService = mediaRendererCPDevice.getCPDevice().getCPServiceByType(UPnPAVConstant.AV_TRANSPORT_SERVICE_TYPE);
    this.controlPoint = mediaRendererCPDevice.getTemplateControlPoint();
    this.stateVariableListener = stateVariableListener;
  }

  /**
   * Creates a new instance of AVTransportCPService.
   * 
   * @param cpService
   * @param stateVariableListener
   */
  public AVTransportCPService(CPService cpService, ICPAVTransportStateVariableListener stateVariableListener)
  {
    this.cpService = cpService;
    this.stateVariableListener = stateVariableListener;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // AVTransport //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getTransportSettings(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_TRANSPORT_SETTINGS);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  /** Sets a new URI for this renderer */
  public void setAVTransportURI(long instanceID, String mediaURL, String itemMetaData)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_SET_AV_TRANSPORT_URI);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);
      action.getInArgument(UPnPAVConstant.ARG_CURRENT_URI).setValue(mediaURL);
      action.getInArgument(UPnPAVConstant.ARG_CURRENT_URI_METADATA).setValueFromString(itemMetaData);

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public void play(long instanceID, String speed)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_PLAY);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);
      action.getInArgument(UPnPAVConstant.ARG_SPEED).setValueFromString(speed);

      controlPoint.invokeAction(action);
    } catch (Exception e1)
    {
      errorInAVTAction(action, e1);
    }
  }

  public void stopMedia(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_STOP);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  public void pauseMedia(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_PAUSE);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  public void getMediaInfo(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_MEDIA_INFO);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  public void getTransportInfo(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_TRANSPORT_INFO);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  public void getPositionInfo(long instanceID)
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_POS_INFO);
    try
    {
      action.getInArgument(UPnPAVConstant.ARG_INSTANCE_ID).setNumericValue(instanceID);

      controlPoint.invokeAction(action);
    } catch (Exception e)
    {
      errorInAVTAction(action, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ILastChangeEventListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPService,
   *      long, java.lang.String, java.lang.String)
   */
  public void stateVariableChanged(CPService cpService, long instanceID, String name, String value)
  {
    if (cpService == this.cpService)
    {
      Hashtable hashtable = getHashtable(instanceID);
      // hash table not found, create new one
      if (hashtable == null)
      {
        hashtable = new Hashtable();
        Long instanceIDObject = new Long(instanceID);
        hashtableFromInstanceIDHashtable.put(instanceIDObject, hashtable);
      }
      // hash table found or newly created
      if (hashtable != null)
      {
        // this effectively overwrites any previously stored value
        hashtable.put(name, value);

        // signal to listener
        if (stateVariableListener != null)
        {
          try
          {
            if (name.equals(UPnPAVConstant.TRANSPORT_STATE))
            {
              stateVariableListener.transportStateChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.TRANSPORT_STATUS))
            {
              stateVariableListener.transportStatusChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.PLAYBACK_STORAGE_MEDIUM))
            {
              stateVariableListener.playbackStorageMediumChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.POSSIBLE_PLAYBACK_STORAGE_MEDIA))
            {
              stateVariableListener.possiblePlaybackStorageMediaChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.CURRENT_PLAY_MODE))
            {
              stateVariableListener.currentPlayModeChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.SV_TRANSPORT_PLAY_SPEED))
            {
              stateVariableListener.transportPlaySpeedChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.NUMBER_OF_TRACKS))
            {
              stateVariableListener.numberOfTracksChanged(instanceID, new Long(value).longValue());
            }

            if (name.equals(UPnPAVConstant.CURRENT_TRACK))
            {
              stateVariableListener.currentTrackChanged(instanceID, new Long(value).longValue());
            }

            if (name.equals(UPnPAVConstant.CURRENT_TRACK_URI))
            {
              stateVariableListener.currentTrackURIChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.AV_TRANSPORT_URI))
            {
              stateVariableListener.avTransportURIChanged(instanceID, value);
            }

            if (name.equals(UPnPAVConstant.CURRENT_TRANSPORT_ACTIONS))
            {
              stateVariableListener.currentTransportActionsChanged(instanceID, value);
            }

          } catch (Exception e)
          {
          }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ILastChangeEventListener#channelStateVariableChanged(de.fhg.fokus.magic.upnp.CPService,
   *      long, java.lang.String, java.lang.String, java.lang.String)
   */
  public void channelStateVariableChanged(CPService cpService,
    long instanceID,
    String name,
    String channel,
    String value)
  {
    // this should never be called for AVTransport
  }

  /**
   * Retrieves the value for a specific state variable.
   * 
   * @param instanceID
   * @param name
   * @return
   */
  public String getStateVariableValue(long instanceID, String name)
  {
    Hashtable hashtable = getHashtable(instanceID);
    if (hashtable != null && hashtable.containsKey(name))
    {
      return (String)hashtable.get(name);
    }

    return null;
  }

  /**
   * Retrieves the state variable hash table for an instanceID.
   * 
   * @param instanceID
   * @return
   */
  private Hashtable getHashtable(long instanceID)
  {
    Long instanceIDObject = new Long(instanceID);
    if (hashtableFromInstanceIDHashtable.containsKey(instanceIDObject))
    {
      return (Hashtable)hashtableFromInstanceIDHashtable.get(instanceIDObject);
    }

    return null;
  }

  /**
   * Retrieves the cpService.
   * 
   * @return The cpService.
   */
  public CPService getCPService()
  {
    return cpService;
  }

  /**
   * Logs arguments for a failed action.
   * 
   * @param action
   * @param e
   *          The exception
   */
  private void errorInAVTAction(CPAction action, Exception e)
  {
    if (action != null)
    {
      Argument[] argument = action.getArgumentTable();

      CPService.logger.error("--------------------------------");
      CPService.logger.error("Device '" + cpService.getCPDevice().getFriendlyName() + "': " + action);
      for (int i = 0; i < argument.length; ++i)
      {
        Argument a = argument[i];
        CPService.logger.error("arg[" + i + "](" + a.getName() + ") ='" + a.getValue() + "'");
      }
    } else
    {
      CPService.logger.error("Action not available");
    }
    CPService.logger.error(e.toString());
    CPService.logger.error("--------------------------------");
  }

}

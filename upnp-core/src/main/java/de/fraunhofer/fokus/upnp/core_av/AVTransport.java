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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_av.renderer.LastChangeCollector;
import de.fraunhofer.fokus.upnp.core_av.renderer.RendererConstants;
import de.fraunhofer.fokus.upnp.core_av.renderer.RendererHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class is a generic implementation of the AVTransport service. It supports only one
 * instanceID.
 * 
 * @author Alexander Koenig
 */
public class AVTransport extends TemplateService
{

  public StateVariable          transportState;

  public StateVariable          transportStatus;

  public StateVariable          playbackStorageMedium;

  public StateVariable          recordStorageMedium;

  public StateVariable          possiblePlaybackStorageMedia;

  public StateVariable          possibleRecordStorageMedia;

  public StateVariable          currentPlayMode;

  public StateVariable          transportPlaySpeed;

  public StateVariable          recordMediumWriteStatus;

  public StateVariable          currentRecordQualityMode;

  public StateVariable          possibleRecordQualityModes;

  public StateVariable          numberOfTracks;

  public StateVariable          currentTrack;

  public StateVariable          currentTrackDuration;

  public StateVariable          currentMediaDuration;

  public StateVariable          currentTrackMetaData;

  public StateVariable          currentTrackURI;

  public StateVariable          avTransportURI;

  public StateVariable          avTransportURIMetaData;

  public StateVariable          nextAVTransportURI;

  public StateVariable          nextAVTransportURIMetaData;

  public StateVariable          relativeTimePosition;

  public StateVariable          absoluteTimePosition;

  public StateVariable          relativeCounterPosition;

  public StateVariable          absoluteCounterPosition;

  public StateVariable          currentTransportActions;

  protected StateVariable       lastChange;

  private StateVariable         A_ARG_TYPE_SeekMode;

  private StateVariable         A_ARG_TYPE_SeekTarget;

  private StateVariable         A_ARG_TYPE_InstanceID;

  protected Action              setAVTransportURI;

  protected Action              getMediaInfo;

  protected Action              getTransportInfo;

  protected Action              getPositionInfo;

  protected Action              getDeviceCapabilities;

  protected Action              getTransportSettings;

  protected Action              stop;

  protected Action              play;

  protected Action              pause;

  protected Action              seek;

  protected Action              next;

  protected Action              previous;

  protected Action              setPlayMode;

  protected Action              getCurrentTransportActions;

  protected LastChangeCollector lastChangeCollector;

  private long                  maxTrackCount;

  public AVTransport(TemplateDevice device, long maxTrackCount)
  {
    super(device, UPnPAVConstant.AV_TRANSPORT_SERVICE_TYPE, UPnPAVConstant.AV_TRANSPORT_SERVICE_ID, false);

    this.maxTrackCount = maxTrackCount;

    setupServiceVariables();
    initServiceContent();
    runService();
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
    lastChange = new StateVariable("LastChange", "", true);
    lastChangeCollector = new LastChangeCollector(lastChange, 200);
    getDevice().getDeviceEventThread().register(lastChangeCollector);

    transportState = new StateVariable(UPnPAVConstant.TRANSPORT_STATE, UPnPAVConstant.VALUE_NO_MEDIA_PRESENT, false);
    transportState.setAllowedValueList(new String[] {
        UPnPAVConstant.VALUE_STOPPED, UPnPAVConstant.VALUE_PLAYING, UPnPAVConstant.VALUE_TRANSITIONING,
        UPnPAVConstant.VALUE_PAUSED_PLAYBACK, UPnPAVConstant.VALUE_PAUSED_RECORDING, UPnPAVConstant.VALUE_RECORDING,
        UPnPAVConstant.VALUE_NO_MEDIA_PRESENT
    });
    transportState.setDefaultValue(UPnPAVConstant.VALUE_NO_MEDIA_PRESENT);
    transportState.setLastChangeCollector(lastChangeCollector);

    transportStatus = new StateVariable(UPnPAVConstant.TRANSPORT_STATUS, UPnPAVConstant.VALUE_OK, false);
    transportStatus.setAllowedValueList(new String[] {
        UPnPAVConstant.VALUE_OK, UPnPAVConstant.VALUE_ERROR_OCCURRED
    });
    transportStatus.setDefaultValue(UPnPAVConstant.VALUE_OK);
    transportStatus.setLastChangeCollector(lastChangeCollector);

    playbackStorageMedium = new StateVariable(UPnPAVConstant.PLAYBACK_STORAGE_MEDIUM, UPnPAVConstant.VALUE_NONE, false);
    playbackStorageMedium.setAllowedValueList(new String[] {
        UPnPAVConstant.VALUE_NONE, UPnPAVConstant.VALUE_NETWORK
    });
    playbackStorageMedium.setDefaultValue(UPnPAVConstant.VALUE_NONE);
    playbackStorageMedium.setLastChangeCollector(lastChangeCollector);

    recordStorageMedium = new StateVariable("RecordStorageMedium", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    recordStorageMedium.setAllowedValueList(new String[] {
      UPnPAVConstant.VALUE_NOT_IMPLEMENTED
    });
    recordStorageMedium.setDefaultValue(UPnPAVConstant.VALUE_NOT_IMPLEMENTED);
    recordStorageMedium.setLastChangeCollector(lastChangeCollector);

    possiblePlaybackStorageMedia =
      new StateVariable(UPnPAVConstant.POSSIBLE_PLAYBACK_STORAGE_MEDIA, UPnPAVConstant.VALUE_NONE, false);
    possiblePlaybackStorageMedia.setLastChangeCollector(lastChangeCollector);

    possibleRecordStorageMedia =
      new StateVariable("PossibleRecordStorageMedia", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    possibleRecordStorageMedia.setLastChangeCollector(lastChangeCollector);

    currentPlayMode = new StateVariable(UPnPAVConstant.CURRENT_PLAY_MODE, UPnPAVConstant.VALUE_PLAY_MODE_NORMAL, false);
    currentPlayMode.setAllowedValueList(new String[] {
      UPnPAVConstant.VALUE_PLAY_MODE_NORMAL
    });
    currentPlayMode.setDefaultValue(UPnPAVConstant.VALUE_PLAY_MODE_NORMAL);
    currentPlayMode.setLastChangeCollector(lastChangeCollector);

    transportPlaySpeed = new StateVariable(UPnPAVConstant.SV_TRANSPORT_PLAY_SPEED, "1", false);
    transportPlaySpeed.setAllowedValueList(new String[] {
      "1"
    });
    transportPlaySpeed.setDefaultValue("1");
    transportPlaySpeed.setLastChangeCollector(lastChangeCollector);

    recordMediumWriteStatus = new StateVariable("RecordMediumWriteStatus", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    recordMediumWriteStatus.setAllowedValueList(new String[] {
      UPnPAVConstant.VALUE_NOT_IMPLEMENTED
    });
    recordMediumWriteStatus.setDefaultValue(UPnPAVConstant.VALUE_NOT_IMPLEMENTED);
    recordMediumWriteStatus.setLastChangeCollector(lastChangeCollector);

    currentRecordQualityMode =
      new StateVariable("CurrentRecordQualityMode", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    currentRecordQualityMode.setAllowedValueList(new String[] {
      UPnPAVConstant.VALUE_NOT_IMPLEMENTED
    });
    currentRecordQualityMode.setDefaultValue(UPnPAVConstant.VALUE_NOT_IMPLEMENTED);
    currentRecordQualityMode.setLastChangeCollector(lastChangeCollector);

    possibleRecordQualityModes =
      new StateVariable("PossibleRecordQualityModes", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    possibleRecordQualityModes.setDefaultValue(UPnPAVConstant.VALUE_NOT_IMPLEMENTED);
    possibleRecordQualityModes.setLastChangeCollector(lastChangeCollector);

    numberOfTracks = new StateVariable(UPnPAVConstant.NUMBER_OF_TRACKS, "ui4", 0, false);
    AllowedValueRange avr = new AllowedValueRange(0, maxTrackCount);
    numberOfTracks.setAllowedValueRange(avr);
    numberOfTracks.setLastChangeCollector(lastChangeCollector);

    currentTrack = new StateVariable(UPnPAVConstant.CURRENT_TRACK, "ui4", 0, false);
    AllowedValueRange avr2 = new AllowedValueRange(0, maxTrackCount, 1);
    currentTrack.setAllowedValueRange(avr2);
    currentTrack.setLastChangeCollector(lastChangeCollector);

    currentTrackDuration = new StateVariable("CurrentTrackDuration", "00:00:00", false);
    currentTrackDuration.setLastChangeCollector(lastChangeCollector);

    currentMediaDuration = new StateVariable("CurrentMediaDuration", "NOT_IMPLEMENTED", false);
    currentMediaDuration.setLastChangeCollector(lastChangeCollector);

    currentTrackMetaData = new StateVariable("CurrentTrackMetaData", "NOT_IMPLEMENTED", false);
    currentTrackMetaData.setLastChangeCollector(lastChangeCollector);

    currentTrackURI = new StateVariable("CurrentTrackURI", "", false);
    currentTrackURI.setLastChangeCollector(lastChangeCollector);

    avTransportURI = new StateVariable("AVTransportURI", "", false);
    avTransportURI.setLastChangeCollector(lastChangeCollector);

    avTransportURIMetaData = new StateVariable("AVTransportURIMetaData", "NOT_IMPLEMENTED", false);
    avTransportURIMetaData.setLastChangeCollector(lastChangeCollector);

    nextAVTransportURI = new StateVariable("NextAVTransportURI", "NOT_IMPLEMENTED", false);
    nextAVTransportURIMetaData = new StateVariable("NextAVTransportURIMetaData", "NOT_IMPLEMENTED", false);

    relativeTimePosition = new StateVariable("RelativeTimePosition", "0:00:00", false);
    absoluteTimePosition = new StateVariable("AbsoluteTimePosition", UPnPAVConstant.VALUE_NOT_IMPLEMENTED, false);
    relativeCounterPosition = new StateVariable("RelativeCounterPosition", Integer.MAX_VALUE, false);
    absoluteCounterPosition = new StateVariable("AbsoluteCounterPosition", Integer.MAX_VALUE, false);

    currentTransportActions = new StateVariable("CurrentTransportActions", "", false);
    currentTransportActions.setLastChangeCollector(lastChangeCollector);

    A_ARG_TYPE_SeekMode = new StateVariable("A_ARG_TYPE_SeekMode", "TRACK NR", false);
    A_ARG_TYPE_SeekMode.setAllowedValueList(new String[] {
      "TRACK NR"
    });
    A_ARG_TYPE_SeekTarget = new StateVariable("A_ARG_TYPE_SeekTarget", "", false);
    A_ARG_TYPE_InstanceID = new StateVariable("A_ARG_TYPE_InstanceID", "ui4", 0, false);

    StateVariable[] stateVariableList =
      {
          transportState, transportStatus, playbackStorageMedium, recordStorageMedium, possiblePlaybackStorageMedia,
          possibleRecordStorageMedia, currentPlayMode, transportPlaySpeed, recordMediumWriteStatus,
          currentRecordQualityMode, possibleRecordQualityModes, numberOfTracks, currentTrack, currentTrackDuration,
          currentMediaDuration, currentTrackMetaData, currentTrackURI, avTransportURI, avTransportURIMetaData,
          nextAVTransportURI, nextAVTransportURIMetaData, relativeTimePosition, absoluteTimePosition,
          relativeCounterPosition, absoluteCounterPosition, currentTransportActions, lastChange, A_ARG_TYPE_SeekMode,
          A_ARG_TYPE_SeekTarget, A_ARG_TYPE_InstanceID
      };

    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    setAVTransportURI = new Action("SetAVTransportURI");
    setAVTransportURI.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentURI", UPnPConstant.DIRECTION_IN, avTransportURI),
        new Argument("CurrentURIMetaData", UPnPConstant.DIRECTION_IN, avTransportURIMetaData)
    });
    getMediaInfo = new Action("GetMediaInfo");
    getMediaInfo.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("NrTracks", UPnPConstant.DIRECTION_OUT, numberOfTracks),
        new Argument("MediaDuration", UPnPConstant.DIRECTION_OUT, currentMediaDuration),
        new Argument("CurrentURI", UPnPConstant.DIRECTION_OUT, avTransportURI),
        new Argument("CurrentURIMetaData", UPnPConstant.DIRECTION_OUT, avTransportURIMetaData),
        new Argument("NextURI", UPnPConstant.DIRECTION_OUT, nextAVTransportURI),
        new Argument("NextURIMetaData", UPnPConstant.DIRECTION_OUT, nextAVTransportURIMetaData),
        new Argument("PlayMedium", UPnPConstant.DIRECTION_OUT, playbackStorageMedium),
        new Argument("RecordMedium", UPnPConstant.DIRECTION_OUT, recordStorageMedium),
        new Argument("WriteStatus", UPnPConstant.DIRECTION_OUT, recordMediumWriteStatus)
    });
    getTransportInfo = new Action("GetTransportInfo");
    getTransportInfo.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("CurrentTransportState", UPnPConstant.DIRECTION_OUT, transportState),
        new Argument("CurrentTransportStatus", UPnPConstant.DIRECTION_OUT, transportStatus),
        new Argument("CurrentSpeed", UPnPConstant.DIRECTION_OUT, transportPlaySpeed)
    });
    getPositionInfo = new Action("GetPositionInfo");
    getPositionInfo.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Track", UPnPConstant.DIRECTION_OUT, currentTrack),
        new Argument("TrackDuration", UPnPConstant.DIRECTION_OUT, currentTrackDuration),
        new Argument("TrackMetaData", UPnPConstant.DIRECTION_OUT, currentTrackMetaData),
        new Argument("TrackURI", UPnPConstant.DIRECTION_OUT, currentTrackURI),
        new Argument("RelTime", UPnPConstant.DIRECTION_OUT, relativeTimePosition),
        new Argument("AbsTime", UPnPConstant.DIRECTION_OUT, absoluteTimePosition),
        new Argument("RelCount", UPnPConstant.DIRECTION_OUT, relativeCounterPosition),
        new Argument("AbsCount", UPnPConstant.DIRECTION_OUT, absoluteCounterPosition)
    });
    getDeviceCapabilities = new Action("GetDeviceCapabilities");
    getDeviceCapabilities.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("PlayMedia", UPnPConstant.DIRECTION_OUT, possiblePlaybackStorageMedia),
        new Argument("RecMedia", UPnPConstant.DIRECTION_OUT, possibleRecordStorageMedia),
        new Argument("RecQualityModes", UPnPConstant.DIRECTION_OUT, possibleRecordQualityModes)
    });
    getTransportSettings = new Action("GetTransportSettings");
    getTransportSettings.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("PlayMode", UPnPConstant.DIRECTION_OUT, currentPlayMode),
        new Argument("RecQualityMode", UPnPConstant.DIRECTION_OUT, currentRecordQualityMode)
    });
    stop = new Action("Stop");
    stop.setArgumentTable(new Argument[] {
      new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID)
    });
    play = new Action("Play");
    play.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Speed", UPnPConstant.DIRECTION_IN, transportPlaySpeed)
    });
    pause = new Action("Pause");
    pause.setArgumentTable(new Argument[] {
      new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID)
    });
    seek = new Action("Seek");
    seek.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Unit", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_SeekMode),
        new Argument("Target", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_SeekTarget)
    });
    next = new Action("Next");
    next.setArgumentTable(new Argument[] {
      new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID)
    });
    previous = new Action("Previous");
    previous.setArgumentTable(new Argument[] {
      new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID)
    });
    setPlayMode = new Action("SetPlayMode");
    setPlayMode.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("NewPlayMode", UPnPConstant.DIRECTION_IN, currentPlayMode)
    });
    getCurrentTransportActions = new Action("GetCurrentTransportActions");
    getCurrentTransportActions.setArgumentTable(new Argument[] {
        new Argument(UPnPAVConstant.ARG_INSTANCE_ID, UPnPConstant.DIRECTION_IN, A_ARG_TYPE_InstanceID),
        new Argument("Actions", UPnPConstant.DIRECTION_OUT, currentTransportActions)
    });

    Action[] actionList =
      {
          setAVTransportURI, getMediaInfo, getTransportInfo, getPositionInfo, getDeviceCapabilities,
          getTransportSettings, stop, play, pause, seek, next, previous, setPlayMode, getCurrentTransportActions
      };
    setActionTable(actionList);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void setAVTransportURI(Argument[] args) throws ActionFailedException
  {
    // System.out.println("SetAVTransportURI invoked");
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      avTransportURI.setValue(args[1].getValue());
      avTransportURIMetaData.setValue(args[2].getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getMediaInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 10)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(numberOfTracks.getValue());
      args[2].setValue(currentMediaDuration.getValue());
      args[3].setValue(avTransportURI.getValue());
      args[4].setValue(avTransportURIMetaData.getValue());
      args[5].setValue(nextAVTransportURI.getValue());
      args[6].setValue(nextAVTransportURIMetaData.getValue());
      args[7].setValue(playbackStorageMedium.getValue());
      args[8].setValue(recordStorageMedium.getValue());
      args[9].setValue(recordMediumWriteStatus.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getTransportInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(transportState.getValue());
      args[2].setValue(transportStatus.getValue());
      args[3].setValue(transportPlaySpeed.getValue());
    } catch (Exception ex)
    {
      System.out.println("ERROR:" + ex.getMessage());

      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getPositionInfo(Argument[] args) throws ActionFailedException
  {
    if (args.length != 9)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(currentTrack.getValue());
      args[2].setValue(currentTrackDuration.getValue());
      args[3].setValue(currentTrackMetaData.getValue());
      args[4].setValue(currentTrackURI.getValue());
      args[5].setValue(relativeTimePosition.getValue());
      args[6].setValue(absoluteTimePosition.getValue());
      args[7].setValue(relativeCounterPosition.getValue());
      args[8].setValue(absoluteCounterPosition.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getDeviceCapabilities(Argument[] args) throws ActionFailedException
  {
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(possiblePlaybackStorageMedia.getValue());
      args[2].setValue(possibleRecordStorageMedia.getValue());
      args[3].setValue(possibleRecordQualityModes.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getTransportSettings(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(currentPlayMode.getValue());
      args[2].setValue(currentRecordQualityMode.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void stop(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void play(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void pause(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void seek(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void next(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void previous(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void setPlayMode(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);
  }

  public void getCurrentTransportActions(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    RendererHelper.checkZeroInstanceID(args[0]);

    try
    {
      args[1].setValue(currentTransportActions.getValue());
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Public methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.DeviceService#getInitialStateVariableValue(de.fhg.fokus.magic.upnp.StateVariable)
   */
  public Object getInitialStateVariableValue(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    if (stateVariable.getName().equals(UPnPAVConstant.LAST_CHANGE))
    {
      // build message with all state variables
      String initialValue = UPnPAVConstant.EVENT_START;
      initialValue += "<" + UPnPAVConstant.ARG_INSTANCE_ID + " " + UPnPAVConstant.ATTR_VAL + "=\"0\">";

      // add all state variables
      initialValue += transportState.toXMLAttributeDescription();
      initialValue += transportStatus.toXMLAttributeDescription();
      initialValue += playbackStorageMedium.toXMLAttributeDescription();
      initialValue += recordStorageMedium.toXMLAttributeDescription();
      initialValue += possiblePlaybackStorageMedia.toXMLAttributeDescription();
      initialValue += possibleRecordStorageMedia.toXMLAttributeDescription();
      initialValue += currentPlayMode.toXMLAttributeDescription();
      initialValue += transportPlaySpeed.toXMLAttributeDescription();
      initialValue += recordMediumWriteStatus.toXMLAttributeDescription();
      initialValue += currentRecordQualityMode.toXMLAttributeDescription();
      initialValue += possibleRecordQualityModes.toXMLAttributeDescription();
      initialValue += numberOfTracks.toXMLAttributeDescription();
      initialValue += currentTrack.toXMLAttributeDescription();
      initialValue += currentTrackDuration.toXMLAttributeDescription();
      initialValue += currentMediaDuration.toXMLAttributeDescription();
      initialValue += currentTrackMetaData.toXMLAttributeDescription();
      initialValue += currentTrackURI.toXMLAttributeDescription();
      initialValue += avTransportURI.toXMLAttributeDescription();
      initialValue += avTransportURIMetaData.toXMLAttributeDescription();
      initialValue += nextAVTransportURI.toXMLAttributeDescription();
      initialValue += nextAVTransportURIMetaData.toXMLAttributeDescription();
      initialValue += currentTransportActions.toXMLAttributeDescription();

      initialValue += "</" + UPnPAVConstant.ARG_INSTANCE_ID + ">";
      initialValue += UPnPAVConstant.EVENT_END;

      // System.out.println("Initial value for last change is: " + initialValue);

      return StringHelper.xmlToEscapedString(initialValue);
    }
    return super.getInitialStateVariableValue(stateVariable, publicKey);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Opens a connection to the server issueing a HEAD request. If the request fails then an
   * ActionFailedException is thrown.
   * 
   * @param url
   *          the URL to test
   * @return the content type of the content referenced by the url
   * @throws ActionFailedException
   *           in case of a failure
   */
  public static String checkURL(URL url) throws ActionFailedException
  {
    String mimeType;

    try
    {
      mimeType = checkURL(url, "HEAD");
    } catch (IOException io)
    {
      try
      {
        mimeType = checkURL(url, "GET");
        System.out.println("Server does not support HEAD, used GET: " + io);
      } catch (IOException io2)
      {
        System.err.println("Invalid URI: " + url);
        io.printStackTrace();
        throw new ActionFailedException(RendererConstants.AV_ERROR_RESOURCE_NO, RendererConstants.AV_ERROR_RESOURCE);
      }
    }

    return mimeType;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Helper methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  protected static String checkURL(URL url, String method) throws IOException, ActionFailedException
  {
    HttpURLConnection conn = null;
    String mimeType;

    try
    {
      try
      {
        conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        conn.connect();

        int response = conn.getResponseCode();
        String msg = conn.getResponseMessage();

        if (response > 399)
        {
          System.err.println("Resource not found: " + url);
          System.err.println("Server response was: " + response + ' ' + msg);
          throw new ActionFailedException(RendererConstants.AV_ERROR_RESOURCE_NO, RendererConstants.AV_ERROR_RESOURCE);
        }

        mimeType = conn.getContentType();
      } finally
      {
        if (conn != null)
        {
          conn.disconnect();
        }
      }
    } catch (Exception ex)
    {
      throw new ActionFailedException(RendererConstants.AV_ERROR_RESOURCE_NO, RendererConstants.AV_ERROR_RESOURCE);
    }

    return mimeType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.DeviceService#terminate()
   */
  public void terminate()
  {
    getDevice().getDeviceEventThread().unregister(lastChangeCollector);
    super.terminate();
  }

}

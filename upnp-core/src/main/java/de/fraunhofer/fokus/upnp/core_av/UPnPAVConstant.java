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

/**
 * This class holds constants for UPnP AV devices and services.
 * 
 * @author Alexander Koenig
 */
public class UPnPAVConstant
{

  public final static String AV_TRANSPORT_SERVICE_TYPE                 = "urn:schemas-upnp-org:service:AVTransport:1";

  public final static String AV_TRANSPORT_SERVICE_ID                   = "urn:upnp-org:serviceId:AVTransport1.0";

  public static final String CONNECTION_MANAGER_SERVICE_TYPE           =
                                                                         "urn:schemas-upnp-org:service:ConnectionManager:1";

  public static final String CONNECTION_MANAGER_SERVICE_ID             = "urn:upnp-org:serviceId:ConnectionManager1.0";

  public static final String CONTENT_DIRECTORY_SERVICE_TYPE            =
                                                                         "urn:schemas-upnp-org:service:ContentDirectory:1";

  public static final String CONTENT_DIRECTORY_SERVICE_ID              = "urn:upnp-org:serviceId:ContentDirectory1.0";

  public static final String RENDERING_CONTROL_SERVICE_TYPE            =
                                                                         "urn:schemas-upnp-org:service:RenderingControl:1";

  public static final String RENDERING_CONTROL_SERVICE_ID              =
                                                                         "urn:schemas-upnp-org:serviceId:RenderingControl1.0";

  public final static String MEDIA_RENDERER_DEVICE_TYPE                = "urn:schemas-upnp-org:device:MediaRenderer:1";

  public final static String MEDIA_SERVER_DEVICE_TYPE                  = "urn:schemas-upnp-org:device:MediaServer:1";

  public final static String MEDIA_RENDERER_DEVICE_TYPE_START          = "urn:schemas-upnp-org:device:MediaRenderer:";

  public final static String MEDIA_SERVER_DEVICE_TYPE_START            = "urn:schemas-upnp-org:device:MediaServer:";

  public static final String AV_SYNCHRONIZATION_SERVICE_TYPE           =
                                                                         "urn:schemas-fokus-fraunhofer-de:service:AVSynchronizationService:1";

  public static final String AV_SYNCHRONIZATION_SERVICE_ID             =
                                                                         "urn:fokus-fraunhofer-de:serviceId:AVSynchronizationService1.0";

  public static final String AV_SYNCHRONIZATION_DEVICE_TYPE            =
                                                                         "urn:schemas-fokus-fraunhofer-de:device:AVSynchronization:1";

  // Action constants
  public final static String ACTION_CREATE_OBJECT                      = "CreateObject";

  public final static String ACTION_DESTROY_OBJECT                     = "DestroyObject";

  public final static String ACTION_GET_CONTRAST                       = "GetContrast";

  public final static String ACTION_GET_MEDIA_INFO                     = "GetMediaInfo";

  public final static String ACTION_GET_MUTE                           = "GetMute";

  public final static String ACTION_GET_POS_INFO                       = "GetPositionInfo";

  public final static String ACTION_GET_PROT_INFO                      = "GetProtocolInfo";

  public final static String ACTION_GET_TRANSPORT_INFO                 = "GetTransportInfo";

  public final static String ACTION_GET_TRANSPORT_SETTINGS             = "GetTransportSettings";

  public final static String ACTION_GET_VOLUME                         = "GetVolume";

  public final static String ACTION_IMPORT_RESOURCE                    = "ImportResource";

  public final static String ACTION_LIST_PRESETS                       = "ListPresets";

  public final static String ACTION_PAUSE                              = "Pause";

  public final static String ACTION_PLAY                               = "Play";

  public final static String ACTION_STOP                               = "Stop";

  public final static String ACTION_SELECT_PRESET                      = "SelectPreset";

  public final static String ACTION_SET_AV_TRANSPORT_URI               = "SetAVTransportURI";

  public final static String ACTION_SET_BRIGHTNESS                     = "SetBrightness";

  public final static String ACTION_GET_BRIGHTNESS                     = "GetBrightness";

  public final static String ACTION_SET_CONTRAST                       = "SetContrast";

  public final static String ACTION_SET_MUTE                           = "SetMute";

  public final static String ACTION_SET_VOLUME                         = "SetVolume";

  public final static String ACTION_SET_DESTINATION_CONTAINER          = "SetDestinationContainer";

  public final static String ACTION_SET_DESTINATION_SERVER             = "SetDestinationServer";

  public final static String ACTION_SET_SOURCE_CONTAINER               = "SetSourceContainer";

  public final static String ACTION_SET_SOURCE_SERVER                  = "SetSourceServer";

  public final static String ACTION_SET_SYNCHRONIZATION_CRITERIA       = "SetSynchronizationCriteria";

  public final static String ACTION_SET_SYNCHRONIZATION_SOURCE_MODE    = "SetSynchronizationSourceMode";

  public final static String ACTION_SET_SYNCHRONIZATION_HIERARCHY_MODE = "SetSynchronizationHierarchyMode";

  public final static String ACTION_SYNCHRONIZE                        = "Synchronize";

  // Argument constants
  public final static String ARG_DESTINATION_CONTAINER_ID              = "DestinationContainerID";

  public final static String ARG_DESTINATION_UDN                       = "DestinationUDN";

  public final static String ARG_SOURCE_CONTAINER_ID                   = "SourceContainerID";

  public final static String ARG_SOURCE_UDN                            = "SourceUDN";

  public final static String ARG_SYNC_CRITERIA                         = "SyncCriteria";

  public final static String ARG_SYNC_DIRECTION                        = "SyncDirection";

  public final static String ARG_SYNC_SOURCE_MODE                      = "SyncSourceMode";

  public final static String ARG_SYNC_HIERARCHY_MODE                   = "SyncHierarchyMode";

  public final static String ARG_CHANNEL                               = "Channel";

  public final static String ARG_CURRENT_PRESET_NAMELIST               = "CurrentPresetNameList";

  // public final static String ARG_CURRENT_SPEED = "CurrentSpeed";
  public final static String ARG_CURRENT_URI                           = "CurrentURI";

  public final static String ARG_CURRENT_URI_METADATA                  = "CurrentURIMetaData";

  public final static String ARG_DESIRED_BRIGHTNESS                    = "DesiredBrightness";

  public final static String ARG_DESIRED_CONTRAST                      = "DesiredContrast";

  public final static String ARG_DESIRED_VOLUME                        = "DesiredVolume";

  public final static String ARG_INSTANCE_ID                           = "InstanceID";

  public final static String ARG_MUTE                                  = "DesiredMute";

  public final static String ARG_PRESET_NAME                           = "PresetName";

  public final static String ARG_SINK                                  = "Sink";

  public final static String ARG_SPEED                                 = "Speed";

  // Value constants
  public static final String VALUE_COMPLETED                           = "COMPLETED";

  public final static String VALUE_CONTAINER                           = "CONTAINER";

  public final static String VALUE_CONTENT_ONLY                        = "CONTENT_ONLY";

  public static final String VALUE_ERROR                               = "ERROR";

  public final static String VALUE_ERROR_OCCURRED                      = "ERROR OCCURRED";

  public final static String VALUE_FLAT                                = "FLAT";

  public final static String VALUE_FOLDER_STRUCTURE                    = "FOLDER_STRUCTURE";

  public static final String VALUE_IN_PROGRESS                         = "IN_PROGRESS";

  public final static String VALUE_MASTER                              = "Master";

  public final static String VALUE_NETWORK                             = "NETWORK";

  public final static String VALUE_NONE                                = "NONE";

  public final static String VALUE_NO_MEDIA_PRESENT                    = "NO_MEDIA_PRESENT";

  public final static String VALUE_NOT_IMPLEMENTED                     = "NOT_IMPLEMENTED";

  public final static String VALUE_OK                                  = "OK";

  public final static String VALUE_PENDING                             = "Pending";

  public final static String VALUE_PLAY_MODE_NORMAL                    = "NORMAL";

  public final static String VALUE_PLAYING                             = "PLAYING";

  public final static String VALUE_PAUSED_PLAYBACK                     = "PAUSED_PLAYBACK";

  public final static String VALUE_PAUSED_RECORDING                    = "PAUSED_RECORDING";

  public final static String VALUE_RECORDING                           = "RECORDING";

  public final static String VALUE_STOPPED                             = "STOPPED";

  public final static String VALUE_SYNCHRONIZE_CONTAINER               = "SYNCHRONIZE_CONTAINER";

  public final static String VALUE_TRANSITIONING                       = "TRANSITIONING";

  // State variable constants
  public final static String AV_TRANSPORT_URI                          = "AVTransportURI";

  public final static String BRIGHTNESS                                = "Brightness";

  public final static String CONTRAST                                  = "Contrast";

  public final static String CURRENT_MEDIA_DURATION                    = "CurrentMediaDuration";

  public final static String CURRENT_PLAY_MODE                         = "CurrentPlayMode";

  public final static String SV_CURRENT_SYNCHRONIZED_ENTRY             = "CurrentSynchronizedEntry";

  public final static String CURRENT_TRACK                             = "CurrentTrack";

  public final static String CURRENT_TRACK_DURATION                    = "CurrentTrackDuration";

  public final static String CURRENT_TRACK_EMB_METADATA                = "CurrentTrackEmbeddedMetaData";

  public final static String CURRENT_TRACK_METADATA                    = "CurrentTrackMetaData";

  public final static String CURRENT_TRACK_URI                         = "CurrentTrackURI";

  public final static String CURRENT_TRANSPORT_ACTIONS                 = "CurrentTransportActions";

  public static final String LAST_CHANGE                               = "LastChange";

  public final static String MUTE                                      = "Mute";

  public final static String NUMBER_OF_TRACKS                          = "NumberOfTracks";

  public final static String PLAYBACK_STORAGE_MEDIUM                   = "PlaybackStorageMedium";

  public final static String POSSIBLE_PLAYBACK_STORAGE_MEDIA           = "PossiblePlaybackStorageMedia";

  public final static String SV_SYNCHRONIZATION_PROGRESS               = "SynchronizationProgress";

  public final static String SV_TRANSFER_IDS                           = "TransferIDs";

  public final static String SV_TRANSPORT_PLAY_SPEED                   = "TransportPlaySpeed";

  public final static String TRANSPORT_STATE                           = "TransportState";

  public final static String TRANSPORT_STATUS                          = "TransportStatus";

  public final static String VOLUME                                    = "Volume";

  // Misc constants
  public final static String ATTR_CHANNEL                              = "channel";

  public final static String ATTR_VAL                                  = "val";

  public final static String EVENT_END                                 = "</Event>";

  public final static String EVENT_START                               =
                                                                         "<Event xmlns = \"urn:schemas-upnp-org:metadata-1-0/AVT_RCS\">";
  // public final String SYNCHRONIZE_WITH_CRITERIA = "SynchronizeWithCriteria";

}

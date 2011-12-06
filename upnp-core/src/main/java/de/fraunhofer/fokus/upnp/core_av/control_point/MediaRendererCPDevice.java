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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateCPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;

/**
 * This class represents a remote view on a media renderer.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class MediaRendererCPDevice extends TemplateCPDevice implements
  ICPAVTransportStateVariableListener,
  ICPRenderingControlStateVariableListener
{
  private static Logger                            logger                                      =
                                                                                                 Logger.getLogger("upnp");

  private static final String                      PROT_HTTP_GET                               = "http-get";

  private static final String                      PROT_RTP                                    = "rtsp-rtp-udp";

  private ICPAVTransportStateVariableListener      avTransportStateVariableListener;

  private ICPRenderingControlStateVariableListener renderingControlStateVariableListener;

  /** Remote view on AVTransport */
  private AVTransportCPService                     avTransportCPService;

  /** Remote view on ConnectionManager */
  private ConnectionManagerCPService               connectionManagerCPService;

  /** Remote view on RenderingControl */
  private RenderingControlCPService                renderingControlCPService;

  private Hashtable                                getProtocolInfoEntryFromFileFormatHashtable = new Hashtable();

  private Hashtable                                rtpProtocolInfoEntryFromFileFormatHashtable = new Hashtable();

  public MediaRendererCPDevice(TemplateControlPoint controlPoint, CPDevice rendererDevice)
  {
    super(controlPoint, rendererDevice);

    avTransportCPService = new AVTransportCPService(this, this);

    connectionManagerCPService = new ConnectionManagerCPService(this);

    renderingControlCPService = new RenderingControlCPService(this, this);

    requestSupportedProtocols();
    // getPositionInfo();
    // getTransportInfo();
    listPresets();
  }

  public CPDevice getRendererDevice()
  {
    return getCPDevice();
  }

  public void setAVTransportStateVariableListener(ICPAVTransportStateVariableListener listener)
  {
    this.avTransportStateVariableListener = listener;
  }

  /**
   * Retrieves the renderingControlStateVariableListener.
   * 
   * @return The renderingControlStateVariableListener.
   */
  public ICPRenderingControlStateVariableListener getRenderingControlStateVariableListener()
  {
    return renderingControlStateVariableListener;
  }

  /**
   * Sets the renderingControlStateVariableListener.
   * 
   * @param renderingControlStateVariableListener
   *          The renderingControlStateVariableListener to set.
   */
  public void setRenderingControlStateVariableListener(ICPRenderingControlStateVariableListener renderingControlStateVariableListener)
  {
    this.renderingControlStateVariableListener = renderingControlStateVariableListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (stateVariable.getName().equals(UPnPAVConstant.LAST_CHANGE))
    {
      try
      {
        String lastChangeValue = stateVariable.getStringValue();
        // System.out.println("Received last change: " + lastChangeValue);
        if (stateVariable.getCPService() == avTransportCPService.getCPService())
        {
          LastChangeParser parser = new LastChangeParser(stateVariable.getCPService(), avTransportCPService);
          parser.parse(lastChangeValue);
        }
        if (stateVariable.getCPService() == renderingControlCPService.getCPService())
        {
          LastChangeParser parser = new LastChangeParser(stateVariable.getCPService(), renderingControlCPService);
          parser.parse(lastChangeValue);
        }
      } catch (Exception e)
      {
        logger.warn("An error occured while parsing lastChange: " + e.getMessage());
      }
    }
  }

  /*
   * private void notSupportedMsg(CPService service, String thingNotSupp) { logger.warn("Device '" +
   * service.getDevice().getFriendlyName() + "' does not support '" + thingNotSupp + "'."); }
   * 
   * private boolean checkBoolean(String elementValue) { String toCheck =
   * elementValue.toUpperCase();
   * 
   * if (toCheck.equals("TRUE") || toCheck.equals("1") || toCheck.equals("YES")) { return true; }
   * else { return false; } }
   */
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // AVTransport //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Sets a new URI for playback. */
  public void setAVTransportURI(String mediaURL, String metaData)
  {
    avTransportCPService.setAVTransportURI(0, mediaURL, metaData);
  }

  public void play()
  {
    avTransportCPService.play(0, "1");
  }

  public void stop()
  {
    avTransportCPService.stopMedia(0);
  }

  public void pause()
  {
    avTransportCPService.pauseMedia(0);
  }

  public void resume()
  {
    avTransportCPService.play(0, "1");
  }

  public void getPositionInfo()
  {
    avTransportCPService.getPositionInfo(0);
  }

  public void getTransportInfo()
  {
    avTransportCPService.getTransportInfo(0);
  }

  public void getTransportSettings()
  {
    avTransportCPService.getTransportSettings(0);
    /*
     * logger.info("Mode=" +
     * mediaRendererActionInvocation.getServiceAVTransport().getCPStateVariable("CurrentRecordQualityMode").
     * getValue().toString() + ":");
     */
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPAVTransportStateVariableListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportStateChanged(long,
   *      java.lang.String)
   */
  public void transportStateChanged(long instanceID, String newTransportState)
  {
    // System.out.println("Transport state changed to: " + newTransportState);
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.transportStateChanged(instanceID, newTransportState);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportStatusChanged(long,
   *      java.lang.String)
   */
  public void transportStatusChanged(long instanceID, String newTransportStatus)
  {
    // System.out.println("Transport status changed to: " + newTransportStatus);
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.transportStatusChanged(instanceID, newTransportStatus);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#playbackStorageMediumChanged(long,
   *      java.lang.String)
   */
  public void playbackStorageMediumChanged(long instanceID, String newPlaybackStorageMedium)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.playbackStorageMediumChanged(instanceID, newPlaybackStorageMedium);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#possiblePlaybackStorageMediaChanged(long,
   *      java.lang.String)
   */
  public void possiblePlaybackStorageMediaChanged(long instanceID, String newPossiblePlaybackStorageMedia)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.possiblePlaybackStorageMediaChanged(instanceID, newPossiblePlaybackStorageMedia);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentPlayModeChanged(long,
   *      java.lang.String)
   */
  public void currentPlayModeChanged(long instanceID, String newCurrentPlayMode)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentPlayModeChanged(instanceID, newCurrentPlayMode);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportPlaySpeedChanged(long,
   *      java.lang.String)
   */
  public void transportPlaySpeedChanged(long instanceID, String newTransportPlaySpeed)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.transportPlaySpeedChanged(instanceID, newTransportPlaySpeed);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#numberOfTracksChanged(long,
   *      long)
   */
  public void numberOfTracksChanged(long instanceID, long newNumberOfTracks)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.numberOfTracksChanged(instanceID, newNumberOfTracks);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackChanged(long,
   *      long)
   */
  public void currentTrackChanged(long instanceID, long newCurrentTrack)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentTrackChanged(instanceID, newCurrentTrack);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackDurationChanged(long,
   *      java.lang.String)
   */
  public void currentTrackDurationChanged(long instanceID, String newCurrentTrackDuration)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentTrackDurationChanged(instanceID, newCurrentTrackDuration);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentMediaDurationChanged(long,
   *      java.lang.String)
   */
  public void currentMediaDurationChanged(long instanceID, String newCurrentMediaDuration)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentMediaDurationChanged(instanceID, newCurrentMediaDuration);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackMetaDataChanged(long,
   *      java.lang.String)
   */
  public void currentTrackMetaDataChanged(long instanceID, String newCurrentTrackMetaData)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentTrackMetaDataChanged(instanceID, newCurrentTrackMetaData);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackURIChanged(long,
   *      java.lang.String)
   */
  public void currentTrackURIChanged(long instanceID, String newCurrentTrackURI)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentTrackURIChanged(instanceID, newCurrentTrackURI);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#avTransportURIChanged(long,
   *      java.lang.String)
   */
  public void avTransportURIChanged(long instanceID, String newURI)
  {
    // System.out.println("AVTransportURI changed to: " + newURI);
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.avTransportURIChanged(instanceID, newURI);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#avTransportURIMetaDataChanged(long,
   *      java.lang.String)
   */
  public void avTransportURIMetaDataChanged(long instanceID, String newURIMetaData)
  {
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.avTransportURIMetaDataChanged(instanceID, newURIMetaData);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTransportActionsChanged(long,
   *      java.lang.String)
   */
  public void currentTransportActionsChanged(long instanceID, String newCurrentTransportActions)
  {
    // System.out.println("CurrentTransportActionsChanged changed to: " +
    // newCurrentTransportActions);
    if (avTransportStateVariableListener != null)
    {
      avTransportStateVariableListener.currentTransportActionsChanged(instanceID, newCurrentTransportActions);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Rendering control //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String listPresets()
  {
    return renderingControlCPService.listPresets(0);
  }

  public void selectPreset(String presetName)
  {
    renderingControlCPService.selectPreset(0, presetName);
  }

  public short getCurrentBrightness()
  {
    return renderingControlCPService.getBrightness(0);
  }

  public short getBrightnessMax()
  {
    return renderingControlCPService.getMaxBrightness();
  }

  public void setBrightness(short desiredBrightness)
  {
    renderingControlCPService.setBrightness(0, desiredBrightness);
  }

  public short getCurrentContrast()
  {
    return renderingControlCPService.getContrast(0);
  }

  public short getContrastMax()
  {
    return renderingControlCPService.getMaxContrast();
  }

  public void setContrast(short desiredContrast)
  {
    renderingControlCPService.setContrast(0, desiredContrast);
  }

  public short getVolume(String channel)
  {
    return renderingControlCPService.getVolume(0, channel);
  }

  public short getVolMax()
  {
    return renderingControlCPService.getMaxVolume();
  }

  /** Sets the volume for a certain channel. */
  public void setVolume(String channel, short desiredVolume)
  {
    System.out.println("Invoke set volume");
    renderingControlCPService.setVolume(0, channel, desiredVolume);
  }

  /** Checks if this renderer is muted. */
  public boolean getMute(String channel)
  {
    return renderingControlCPService.getMute(0, channel);
  }

  /** Sets this renderer to mute. */
  public void setMute(String channel, boolean desiredMute)
  {
    renderingControlCPService.setMute(0, channel, desiredMute);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPRenderingControlStateVariableListener#brightnessChanged(long,
   *      short)
   */
  public void brightnessChanged(long instanceID, short brightness)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPRenderingControlStateVariableListener#volumeChanged(long,
   *      java.lang.String, short)
   */
  public void volumeChanged(long instanceID, String channel, short newVolume)
  {
    System.out.println("Volume changed to: " + newVolume);
    if (renderingControlStateVariableListener != null)
    {
      renderingControlStateVariableListener.volumeChanged(instanceID, channel, newVolume);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Protocol management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Request and store sink protocols */
  private void requestSupportedProtocols()
  {
    connectionManagerCPService.getProtocolInfo();
  }

  /** Parses the sink protocol info string for this media renderer */
  protected void storeSinkProtocolInfo(String sinkProtocolInfo)
  {
    // store all individual values
    StringTokenizer tokens = new StringTokenizer(sinkProtocolInfo, ",");

    while (tokens.hasMoreTokens())
    {
      String token = tokens.nextToken().trim();

      if (token.indexOf(PROT_HTTP_GET) != -1)
      {
        getProtocolInfoEntryFromFileFormatHashtable.put(getFileFormat(token), sinkProtocolInfo);
      } else if (token.indexOf(PROT_RTP) != -1)
      {
        rtpProtocolInfoEntryFromFileFormatHashtable.put(getFileFormat(token), sinkProtocolInfo);
      }
    }
  }

  /**
   * Extracts the file format from a UPnP protocol info string. For http-get:*:audio/mpeg:* this
   * would return audio/mpeg.
   * 
   * @param protocolInfoEntry
   *          One value of the sink protocol info
   */
  private String getFileFormat(String protocolInfoEntry)
  {
    protocolInfoEntry = protocolInfoEntry.substring(0, protocolInfoEntry.lastIndexOf(":"));
    protocolInfoEntry = protocolInfoEntry.substring(protocolInfoEntry.lastIndexOf(":") + 1);

    return protocolInfoEntry;
  }

  /** Checks if this renderer understands a certain resource */
  public boolean canRenderResource(DIDLResource resource)
  {
    if (resource == null)
    {
      return false;
    }

    String protocolInfo = resource.getProtocolInfo();

    if (protocolInfo.indexOf(PROT_HTTP_GET) != -1 &&
      getProtocolInfoEntryFromFileFormatHashtable.containsKey(getFileFormat(protocolInfo)))
    {
      return true;
    }
    if (protocolInfo.indexOf(PROT_RTP) != -1 &&
      rtpProtocolInfoEntryFromFileFormatHashtable.containsKey(getFileFormat(protocolInfo)))
    {
      return true;
    }
    return false;
  }

  /** Checks if this renderer can play in the current state */
  public boolean canPlay()
  {
    // first check current transport actions
    String currentTransportActions =
      avTransportCPService.getStateVariableValue(0, UPnPAVConstant.CURRENT_TRANSPORT_ACTIONS);

    if (currentTransportActions != null)
    {
      return currentTransportActions.indexOf("Play") != -1;
    }

    // current transport actions not available, use transportState
    String transportState = avTransportCPService.getStateVariableValue(0, UPnPAVConstant.TRANSPORT_STATE);

    if (transportState != null)
    {
      return transportState.equals(UPnPAVConstant.VALUE_STOPPED) ||
        transportState.equals(UPnPAVConstant.VALUE_PAUSED_PLAYBACK);
    }

    return false;
  }

  /** Checks if this renderer can play in the current state */
  public boolean canStop()
  {
    // first check current transport actions
    String currentTransportActions =
      avTransportCPService.getStateVariableValue(0, UPnPAVConstant.CURRENT_TRANSPORT_ACTIONS);

    if (currentTransportActions != null)
    {
      return currentTransportActions.indexOf("Stop") != -1;
    }

    // current transport actions not available, use transportState
    String transportState = avTransportCPService.getStateVariableValue(0, UPnPAVConstant.TRANSPORT_STATE);

    if (transportState != null)
    {
      return transportState.equals(UPnPAVConstant.VALUE_PLAYING) ||
        transportState.equals(UPnPAVConstant.VALUE_PAUSED_PLAYBACK) ||
        transportState.equals(UPnPAVConstant.VALUE_PAUSED_RECORDING) ||
        transportState.equals(UPnPAVConstant.VALUE_RECORDING);
    }

    return false;
  }

  /** Retrieves the number of supported protocols. */
  public int getSupportedProtocolsCount()
  {
    return getProtocolInfoEntryFromFileFormatHashtable.size() + rtpProtocolInfoEntryFromFileFormatHashtable.size();
  }

  /** Returns a list with all supported GET file formats. */
  public Vector getSupportedGetFileFormats()
  {
    Vector result = new Vector();
    Enumeration fileFormats = getProtocolInfoEntryFromFileFormatHashtable.keys();
    while (fileFormats.hasMoreElements())
    {
      result.add(fileFormats.nextElement());
    }
    return result;
  }

}

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

/**
 * This is a convenience for classes that need only some of the ICPMediaRenderLastChangeListener
 * methods. All methods in this class have empty bodies. Classes may extend this class overriding
 * only the required methods.
 * 
 * @author tje
 */
public class CPAVTransportStateVariableAdapter implements ICPAVTransportStateVariableListener
{

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportStateChanged(long,
   *      java.lang.String)
   */
  public void transportStateChanged(long instanceID, String newTransportState)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportStatusChanged(long,
   *      java.lang.String)
   */
  public void transportStatusChanged(long instanceID, String newTransportState)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#avTransportURIChanged(long,
   *      java.lang.String)
   */
  public void avTransportURIChanged(long instanceID, String newURI)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#playbackStorageMediumChanged(long,
   *      java.lang.String)
   */
  public void playbackStorageMediumChanged(long instanceID, String newPlaybackStorageMedium)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#possiblePlaybackStorageMediaChanged(long,
   *      java.lang.String)
   */
  public void possiblePlaybackStorageMediaChanged(long instanceID, String newPossiblePlaybackStorageMedia)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentPlayModeChanged(long,
   *      java.lang.String)
   */
  public void currentPlayModeChanged(long instanceID, String newCurrentPlayMode)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#transportPlaySpeedChanged(long,
   *      java.lang.String)
   */
  public void transportPlaySpeedChanged(long instanceID, String newTransportPlaySpeed)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#numberOfTracksChanged(long,
   *      long)
   */
  public void numberOfTracksChanged(long instanceID, long newNumberOfTracks)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackChanged(long,
   *      long)
   */
  public void currentTrackChanged(long instanceID, long newCurrentTrack)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackDurationChanged(long,
   *      java.lang.String)
   */
  public void currentTrackDurationChanged(long instanceID, String newCurrentTrackDuration)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentMediaDurationChanged(long,
   *      java.lang.String)
   */
  public void currentMediaDurationChanged(long instanceID, String newCurrentMediaDuration)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackMetaDataChanged(long,
   *      java.lang.String)
   */
  public void currentTrackMetaDataChanged(long instanceID, String newCurrentTrackMetaData)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTrackURIChanged(long,
   *      java.lang.String)
   */
  public void currentTrackURIChanged(long instanceID, String newCurrentTrackURI)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#avTransportURIMetaDataChanged(long,
   *      java.lang.String)
   */
  public void avTransportURIMetaDataChanged(long instanceID, String newURIMetaData)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPAVTransportStateVariableListener#currentTransportActionsChanged(long,
   *      java.lang.String)
   */
  public void currentTransportActionsChanged(long instanceID, String newCurrentTransportActions)
  {
  }

}

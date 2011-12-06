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
 * This interface can be used by classes to get informed about changes in an AVTransport.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public interface ICPAVTransportStateVariableListener
{

  public void transportStateChanged(long instanceID, String newTransportState);

  public void transportStatusChanged(long instanceID, String newTransportStatus);

  public void playbackStorageMediumChanged(long instanceID, String newPlaybackStorageMedium);

  public void possiblePlaybackStorageMediaChanged(long instanceID, String newPossiblePlaybackStorageMedia);

  public void currentPlayModeChanged(long instanceID, String newCurrentPlayMode);

  public void transportPlaySpeedChanged(long instanceID, String newTransportPlaySpeed);

  public void numberOfTracksChanged(long instanceID, long newNumberOfTracks);

  public void currentTrackChanged(long instanceID, long newCurrentTrack);

  public void currentTrackDurationChanged(long instanceID, String newCurrentTrackDuration);

  public void currentMediaDurationChanged(long instanceID, String newCurrentMediaDuration);

  public void currentTrackMetaDataChanged(long instanceID, String newCurrentTrackMetaData);

  public void currentTrackURIChanged(long instanceID, String newCurrentTrackURI);

  public void avTransportURIChanged(long instanceID, String newURI);

  public void avTransportURIMetaDataChanged(long instanceID, String newURIMetaData);

  public void currentTransportActionsChanged(long instanceID, String newCurrentTransportActions);

}

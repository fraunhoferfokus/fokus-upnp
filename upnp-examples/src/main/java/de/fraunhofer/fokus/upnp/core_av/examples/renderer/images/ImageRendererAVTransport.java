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
package de.fraunhofer.fokus.upnp.core_av.examples.renderer.images;

import java.net.URL;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core_av.AVTransport;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.renderer.RendererConstants;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class is the implementation of the AVTransport service for the UPnP ImageRenderer.
 * 
 * @author Alexander Koenig
 */
public class ImageRendererAVTransport extends AVTransport
{

  public ImageRendererAVTransport(ImageRendererDevice imageRenderer)
  {
    super(imageRenderer, 1);
  }

  /** Remove optional actions from AVTransport */
  public void initServiceContent()
  {
    super.initServiceContent();
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    Action[] actionList =
      {
          setAVTransportURI, getMediaInfo, getTransportInfo, getPositionInfo, getDeviceCapabilities,
          getTransportSettings, stop, play, getCurrentTransportActions
      };
    setActionTable(actionList);
  }

  public void setAVTransportURI(Argument[] args) throws ActionFailedException
  {
    // error handling
    super.setAVTransportURI(args);

    String uri = (String)args[1].getValue();
    try
    {
      URL url = new URL(uri);
      // update connection manager
      getImageRenderer().getConnectionManager().setConnectionStateForRenderer("http-get:*:" + checkURL(url) + ":*",
        true,
        true);

      // handle up to device
      if (getImageRenderer().getEventListener() != null)
      {
        getImageRenderer().getEventListener().urlChanged(url);
        // if transport state is playing, show immediately
        if (transportState.getStringValue().equals(UPnPAVConstant.VALUE_PLAYING))
        {
          getImageRenderer().getEventListener().play();
        }
      }
      // update state variables
      if (transportState.getStringValue().equals(UPnPAVConstant.VALUE_NO_MEDIA_PRESENT))
      {
        transportState.setValue(UPnPAVConstant.VALUE_STOPPED);
        playbackStorageMedium.setValue(UPnPAVConstant.VALUE_NETWORK);
        numberOfTracks.setNumericValue(1);
        currentTrack.setNumericValue(1);
        currentTrackDuration.setValue("00:00:00");
        currentMediaDuration.setValue("00:00:00");
        currentTransportActions.setValue("Play");
      }
      currentTrackMetaData.setValue(avTransportURIMetaData.getValue());
      currentTrackURI.setValue(avTransportURI.getValue());

    } catch (Exception ex)
    {
      // resource invalid
      getImageRenderer().getConnectionManager().resetConnectionState();

      throw new ActionFailedException(RendererConstants.AV_ERROR_RESOURCE_NO, RendererConstants.AV_ERROR_RESOURCE);
    }
  }

  public void play(Argument[] args) throws ActionFailedException
  {
    // error handling
    super.play(args);

    try
    {
      String speed = (String)args[1].getValue();
      if (!speed.equals("1"))
      {
        throw new ActionFailedException(717, "Play speed not available");
      }
      if (getImageRenderer().getEventListener() != null)
      {
        getImageRenderer().getEventListener().play();
      }

      transportState.setValue(UPnPAVConstant.VALUE_PLAYING);
      transportPlaySpeed.setValue("1");
      currentTransportActions.setValue("Stop");
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void stop(Argument[] args) throws ActionFailedException
  {
    // error handling
    super.stop(args);

    try
    {
      if (getImageRenderer().getEventListener() != null)
      {
        getImageRenderer().getEventListener().stop();
      }
      transportState.setValue(UPnPAVConstant.VALUE_STOPPED);
      currentTransportActions.setValue("Play");
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public ImageRendererDevice getImageRenderer()
  {
    return (ImageRendererDevice)getTemplateDevice();
  }

}

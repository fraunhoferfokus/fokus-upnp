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
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class is a special control point for AV devices. It holds internal lists of both discovered
 * servers and renderers.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class AVControlPoint extends TemplateControlPoint implements ICPMediaServerStateVariableListener
{

  private Vector mediaServerList   = new Vector();

  private Vector mediaRendererList = new Vector();

  public AVControlPoint(AVEntity entity, UPnPStartupConfiguration startupConfiguration)
  {
    super(entity, startupConfiguration);
  }

  public AVEntity getAVEntity()
  {
    return (AVEntity)getTemplateEntity();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPDeviceEventListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    super.newDevice(newDevice);

    if (newDevice.getDeviceType().startsWith("urn:schemas-upnp-org:device:MediaServer:"))
    {
      if (!isKnownMediaServer(newDevice.getUDN()))
      {
        MediaServerCPDevice mediaServer = new MediaServerCPDevice(this, newDevice);
        mediaServer.addServerChangeListener(this);
        mediaServerList.add(mediaServer);
        getAVEntity().newMediaServer(mediaServer);
      }
    } else if (newDevice.getDeviceType().startsWith("urn:schemas-upnp-org:device:MediaRenderer:"))
    {
      if (!isKnownMediaRenderer(newDevice.getUDN()))
      {
        MediaRendererCPDevice renderer = new MediaRendererCPDevice(this, newDevice);
        mediaRendererList.add(renderer);
        getAVEntity().newMediaRenderer(renderer);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    // remove from local list
    if (goneDevice.getDeviceType().startsWith("urn:schemas-upnp-org:device:MediaServer:"))
    {
      int index = getMediaServerIndex(goneDevice.getUDN());
      if (index != -1)
      {
        ((MediaServerCPDevice)mediaServerList.elementAt(index)).terminate();
        mediaServerList.remove(index);
        getAVEntity().mediaServerGone(goneDevice);
      }
    }
    if (goneDevice.getDeviceType().startsWith("urn:schemas-upnp-org:device:MediaRenderer:"))
    {
      int index = getMediaRendererIndex(goneDevice.getUDN());
      if (index != -1)
      {
        ((MediaRendererCPDevice)mediaRendererList.elementAt(index)).terminate();
        mediaRendererList.remove(index);
        getAVEntity().mediaRendererGone(goneDevice);
      }
    }
    super.deviceGone(goneDevice);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPMediaServerStateVariableListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationUpdate(java.lang.String)
   */
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID)
  {
    getAVEntity().containerEnumerationUpdate(server, containerID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationFinished(java.lang.String)
   */
  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID)
  {
    getAVEntity().containerEnumerationFinished(server, containerID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerUpdateOccurred(java.lang.String)
   */
  public void containerUpdateOccurred(MediaServerCPDevice server, String containerUpdateID)
  {
    getAVEntity().containerUpdateOccurred(server, containerUpdateID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#systemUpdateOccurred(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice)
   */
  public void systemUpdateOccurred(MediaServerCPDevice server)
  {
    getAVEntity().systemUpdateOccurred(server);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if a device with this udn is already in the list
   */
  protected boolean isKnownMediaServer(String udn)
  {
    for (int i = 0; i < mediaServerList.size(); i++)
    {
      if (((MediaServerCPDevice)mediaServerList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a device with this udn is already in the list
   */
  protected boolean isKnownMediaRenderer(String udn)
  {
    for (int i = 0; i < mediaRendererList.size(); i++)
    {
      if (((MediaRendererCPDevice)mediaRendererList.elementAt(i)).getRendererDevice().getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the index for a media server
   */
  public int getMediaServerIndex(String udn)
  {
    for (int i = 0; i < mediaServerList.size(); i++)
    {
      if (((MediaServerCPDevice)mediaServerList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index for a media renderer
   */
  public int getMediaRendererIndex(String udn)
  {
    for (int i = 0; i < mediaRendererList.size(); i++)
    {
      if (((MediaRendererCPDevice)mediaRendererList.elementAt(i)).getRendererDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the number of media servers
   */
  public int getMediaServerCount()
  {
    return mediaServerList.size();
  }

  /**
   * Returns the number of media renderers
   */
  public int getMediaRendererCount()
  {
    return mediaRendererList.size();
  }

  /**
   * Returns a media server
   */
  public MediaServerCPDevice getMediaServer(int index)
  {
    if (index >= 0 && index < mediaServerList.size())
    {
      return (MediaServerCPDevice)mediaServerList.elementAt(index);
    }

    return null;
  }

  /**
   * Returns a media renderer
   */
  public MediaRendererCPDevice getMediaRenderer(int index)
  {
    if (index >= 0 && index < mediaRendererList.size())
    {
      return (MediaRendererCPDevice)mediaRendererList.elementAt(index);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#terminate()
   */
  public void terminate()
  {
    for (Enumeration e = mediaServerList.elements(); e.hasMoreElements();)
    {
      ((MediaServerCPDevice)e.nextElement()).terminate();
    }
    for (Enumeration e = mediaRendererList.elements(); e.hasMoreElements();)
    {
      ((MediaRendererCPDevice)e.nextElement()).terminate();
    }
    super.terminate();
  }

}

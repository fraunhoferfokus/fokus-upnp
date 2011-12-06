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

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class starts a AVControlPoint and provides events to a GUI.
 * 
 * @author Alexander Koenig
 */
public class AVEntity extends TemplateEntity implements
  ICPMediaDeviceEventListener,
  ICPMediaServerStateVariableListener
{

  private ICPMediaDeviceEventListener         deviceChangeListener;

  private ICPMediaServerStateVariableListener serverChangeListener;

  /** Creates a new instance of TemplateEntity */
  public AVEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
  }

  /** Adds a control point to this entity */
  public void createAVControlPoint()
  {
    setTemplateControlPoint(new AVControlPoint(this, getStartupConfiguration()));
  }

  /** Retrieves the control point of this entity */
  public AVControlPoint getAVControlPoint()
  {
    return (AVControlPoint)getTemplateControlPoint();
  }

  /** Sets the listener for device change events */
  public void setMediaDeviceListener(ICPMediaDeviceEventListener listener)
  {
    deviceChangeListener = listener;
  }

  /** Sets the listener for media server events */
  public void setServerChangeListener(ICPMediaServerStateVariableListener listener)
  {
    serverChangeListener = listener;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPMediaDeviceEventListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaDeviceEventListener#mediaServerGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void mediaServerGone(CPDevice goneServer)
  {
    if (deviceChangeListener != null)
    {
      deviceChangeListener.mediaServerGone(goneServer);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaDeviceEventListener#newMediaServer(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice)
   */
  public void newMediaServer(MediaServerCPDevice newMediaServer)
  {
    if (deviceChangeListener != null)
    {
      deviceChangeListener.newMediaServer(newMediaServer);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaDeviceEventListener#newMediaRenderer(de.fhg.fokus.magic.upnpav.controlpoint.MediaRendererCPDevice)
   */
  public void newMediaRenderer(MediaRendererCPDevice newRenderer)
  {
    if (deviceChangeListener != null)
    {
      deviceChangeListener.newMediaRenderer(newRenderer);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaDeviceEventListener#mediaRendererGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void mediaRendererGone(CPDevice goneRenderer)
  {
    if (deviceChangeListener != null)
    {
      deviceChangeListener.mediaRendererGone(goneRenderer);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPMediaServerStateVariableListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID)
  {
    if (serverChangeListener != null)
    {
      serverChangeListener.containerEnumerationUpdate(server, containerID);
    }
  }

  public void containerUpdateOccurred(MediaServerCPDevice server, String containerUpdateID)
  {
    if (serverChangeListener != null)
    {
      serverChangeListener.containerUpdateOccurred(server, containerUpdateID);
    }
  }

  public void systemUpdateOccurred(MediaServerCPDevice server)
  {
    if (serverChangeListener != null)
    {
      serverChangeListener.systemUpdateOccurred(server);
    }
  }

  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID)
  {
    if (serverChangeListener != null)
    {
      serverChangeListener.containerEnumerationFinished(server, containerID);
    }
  }

}

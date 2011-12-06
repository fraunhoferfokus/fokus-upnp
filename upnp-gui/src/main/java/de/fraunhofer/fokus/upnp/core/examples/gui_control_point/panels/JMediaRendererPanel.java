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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.IDeviceGUIContextProvider;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.IDIDLResourceProvider;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ScrollableComponentPanel;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.MediaRendererPlugin;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;
import de.fraunhofer.fokus.upnp.util.FileExtensionHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;

/**
 * This panel is used to show media renders that can render certain resources.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class JMediaRendererPanel extends ScrollableComponentPanel implements IDIDLResourceProvider
{

  private static final long         serialVersionUID = 1L;

  private String                    mediaItemURL;

  private DIDLResource              mediaItemResource;

  private IDeviceGUIContextProvider deviceGUIContextProvider;

  public JMediaRendererPanel()
  {
    super((String)null, ButtonConstants.SMOOTH_GREEN_COLOR, 350, ButtonConstants.BUTTON_HEIGHT + 10, 3);

    mediaItemURL = null;
    mediaItemResource = null;
  }

  /** Makes the media renderer panel visible if it contains components, else invisible. */
  public void updateVisibility()
  {
    setVisible(componentPanel.getComponentCount() > 0);
  }

  /** Updates the media renderer panel */
  public void updateMediaRenderers()
  {
    clear();

    if (mediaItemURL != null && deviceGUIContextProvider != null)
    {
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(0, 0, 0, 0);

      // show panels for all found media renderer plugins that could render the current item
      for (int i = 0; i < deviceGUIContextProvider.getDeviceContextCount(); i++)
      {
        DeviceGUIContext currentContext = deviceGUIContextProvider.getDeviceContext(i);
        // check each device for media renderer
        if (currentContext.isValid() && currentContext.isVisibleDeviceGUIContext() &&
          currentContext.getDevicePlugin() != null && currentContext.getDevicePlugin() instanceof MediaRendererPlugin)
        {
          MediaRendererPlugin currentPlugin = (MediaRendererPlugin)currentContext.getDevicePlugin();
          // check for resource compatibility
          if (currentPlugin.getMediaRendererCPDevice() != null &&
            currentPlugin.getMediaRendererCPDevice().canRenderResource(mediaItemResource))
          {
            currentPlugin.setResourceProvider(this);
            gridBagConstraints.gridy++;
            addComponent(currentPlugin.getControlPanel(), gridBagConstraints);
          }
        }
      }
    }
    update();
    updateVisibility();
  }

  /**
   * Retrieves the mediaItemURL.
   * 
   * @return The mediaItemURL
   */
  public String getMediaItemURL()
  {
    return mediaItemURL;
  }

  /**
   * Sets the mediaItemURL.
   * 
   * @param mediaItemURL
   *          The new value for mediaItemURL
   */
  public void setMediaItemURL(String mediaItemURL)
  {
    this.mediaItemURL = mediaItemURL;
    if (mediaItemURL != null)
    {
      String protocolInfo = FileExtensionHelper.getProtocolInfoByFileExtension(mediaItemURL);

      // extension for security camera that uses an unusual URL format
      if (protocolInfo.equals("http-get:*:text/xml:*"))
      {
        protocolInfo = "http-get:*:image/jpeg:*";
      }
      mediaItemResource = new DIDLResource(protocolInfo, mediaItemURL);
    } else
    {
      mediaItemResource = null;
    }
    updateMediaRenderers();
  }

  public DIDLResource getSelectedResource()
  {
    return mediaItemResource;
  }

  /**
   * Retrieves the deviceGUIContextProvider.
   * 
   * @return The deviceGUIContextProvider
   */
  public IDeviceGUIContextProvider getDeviceGUIContextProvider()
  {
    return deviceGUIContextProvider;
  }

  /**
   * Sets the deviceGUIContextProvider.
   * 
   * @param deviceGUIContextProvider
   *          The new value for deviceGUIContextProvider
   */
  public void setDeviceGUIContextProvider(IDeviceGUIContextProvider deviceGUIContextProvider)
  {
    this.deviceGUIContextProvider = deviceGUIContextProvider;
    updateMediaRenderers();
  }

  /**
   * Sets the mediaItemResource.
   * 
   * @param mediaItemResource
   *          The new value for mediaItemResource
   */
  public void setMediaItemResource(DIDLResource mediaItemResource)
  {
    this.mediaItemResource = mediaItemResource;
    if (mediaItemResource != null)
    {
      mediaItemURL = mediaItemResource.getValue();
    } else
    {
      mediaItemURL = null;
    }
    updateMediaRenderers();
  }

}

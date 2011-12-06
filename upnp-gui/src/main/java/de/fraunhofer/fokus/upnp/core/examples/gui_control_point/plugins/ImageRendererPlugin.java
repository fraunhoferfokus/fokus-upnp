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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JFrame;

import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.examples.renderer.images.IImageRendererEvents;
import de.fraunhofer.fokus.upnp.core_av.examples.renderer.images.ImageRendererEntity;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.JImagePanel;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This plugin implements an ImageRendererDevice.
 * 
 * @author Alexander Koenig
 */
public class ImageRendererPlugin extends BaseDevicePlugin implements IImageRendererEvents
{

  private static final long   serialVersionUID = 1L;

  public static String        PLUGIN_TYPE      = "InternalImageRenderer";

  private JImagePanel         imagePanel;

  private SmoothButton        urlButton;

  private ImageRendererEntity imageRenderer;

  private Image               currentImage;

  private String              currentURL;

  public ImageRendererPlugin(JFrame frame, UPnPStartupConfiguration startupConfiguration)
  {
    setFrame(frame);
    initComponents();

    imagePanel = new JImagePanel();
    urlButton =
      new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    urlButton.setVisible(false);

    // create device
    imageRenderer = new ImageRendererEntity(startupConfiguration);
    // add listener to forward events to this panel
    imageRenderer.getImageRendererDevice().setEventListener(this);

    // associate device with this plugin
    setDevice(imageRenderer.getImageRendererDevice());

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    add(imagePanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    add(urlButton, gridBagConstraints);

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    imagePanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    urlButton.setBackground(ButtonConstants.BACKGROUND_COLOR);

    repaint();
    validateTree();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#getPluginType()
   */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.examples.renderer.IImageRendererEvents#urlChanged(java.net.URL)
   */
  public void urlChanged(URL imageURL)
  {
    // System.out.println("Try to load image: "+imageURL);
    if (imageURL != null)
    {
      MediaTracker mediaTracker = new MediaTracker(this);
      Image image = null;
      try
      {
        image = Toolkit.getDefaultToolkit().createImage(imageURL);

        mediaTracker.addImage(image, 0);
        mediaTracker.waitForAll();

        currentURL = imageURL.toString();
        currentImage = image;
      } catch (Exception ex)
      {
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.examples.renderer.IImageRendererEvents#play()
   */
  public void play()
  {
    urlButton.setVisible(true);
    urlButton.setText(currentURL);
    imagePanel.setImage(currentImage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.examples.renderer.IImageRendererEvents#stop()
   */
  public void stop()
  {
    urlButton.setVisible(false);
    urlButton.setText("");
    imagePanel.setImage(null);
  }

  public ImageRendererEntity getImageRendererEntity()
  {
    return imageRenderer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#terminate()
   */
  public void terminate()
  {
    imageRenderer.terminate();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {

    setLayout(new java.awt.GridBagLayout());

    setBackground(new java.awt.Color(204, 204, 255));
  }// GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables

}

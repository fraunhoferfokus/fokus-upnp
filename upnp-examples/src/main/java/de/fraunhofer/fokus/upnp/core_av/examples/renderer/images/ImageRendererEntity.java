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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JFrame;

import de.fraunhofer.fokus.upnp.core.event.IFrameCloseListener;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.JImagePanel;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This class creates a MediaRenderer device for images. It can either be embedded in another JFrame
 * or using its own JFrame (controlled by property "GUI").
 * 
 * 
 * @author Alexander Koenig
 */
public class ImageRendererEntity extends TemplateEntity
{
  private ImageRendererDevice imageRendererDevice;

  private ImageRendererGUI    imageRendererGUI;

  /** Optional listener for window close */
  private IFrameCloseListener frameCloseListener;

  /**
   * Creates a new instance of ImageRendererEntity.
   * 
   * @param startupConfiguration
   *          A predefined startup configuration
   */
  public ImageRendererEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    imageRendererDevice = new ImageRendererDevice(this, getStartupConfiguration());
    setTemplateDevice(imageRendererDevice);

    DeviceStartupConfiguration deviceStartupConfiguration = imageRendererDevice.getDeviceStartupConfiguration();
    // prevent start of GUI if flag exists and is false
    if (deviceStartupConfiguration.hasProperty("GUI") && !deviceStartupConfiguration.getBooleanProperty("GUI"))
    {
      return;
    }
    imageRendererGUI = new ImageRendererGUI(this);
    imageRendererGUI.setVisible(true);
  }

  public ImageRendererDevice getImageRendererDevice()
  {
    return imageRendererDevice;
  }

  /**
   * Retrieves the imageRendererGUI.
   * 
   * @return The imageRendererGUI
   */
  public ImageRendererGUI getImageRendererGUI()
  {
    return imageRendererGUI;
  }

  /**
   * Sets the frameCloseListener.
   * 
   * @param frameCloseListener
   *          The new value for frameCloseListener
   */
  public void setFrameCloseListener(IFrameCloseListener frameCloseListener)
  {
    this.frameCloseListener = frameCloseListener;
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new ImageRendererEntity(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    super.terminate();

    // terminate GUI
    if (imageRendererGUI != null)
    {
      imageRendererGUI.setVisible(false);
      imageRendererGUI.dispose();
      imageRendererGUI = null;
    }
  }

  private class ImageRendererGUI extends javax.swing.JFrame implements IImageRendererEvents
  {

    private static final long   serialVersionUID = 1L;

    private final JFrame        frame;

    private JImagePanel         imagePanel;

    private SmoothButton        urlButton;

    private ImageRendererEntity imageRenderer;

    private Image               currentImage;

    private String              currentURL;

    public ImageRendererGUI(ImageRendererEntity imageRendererEntity)
    {
      frame = this;
      getContentPane().setLayout(new GridBagLayout());
      getContentPane().setBackground(Color.black);

      setTitle("Bildbetrachter");
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      addWindowListener(new java.awt.event.WindowAdapter()
      {
        public void windowClosing(java.awt.event.WindowEvent evt)
        {
          if (frameCloseListener != null)
          {
            frameCloseListener.windowClosed(frame);
          }
          exitForm(evt);
        }
      });

      imagePanel = new JImagePanel();
      urlButton =
        new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
          12,
          "",
          null);
      urlButton.setVisible(false);

      // create device
      imageRenderer = imageRendererEntity;
      // add listener to forward events to this panel
      imageRenderer.getImageRendererDevice().setEventListener(this);

      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      getContentPane().add(imagePanel, gridBagConstraints);

      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      getContentPane().add(urlButton, gridBagConstraints);

      imagePanel.setBackground(Color.black);
      urlButton.setBackground(Color.black);

      repaint();
      validateTree();

      this.setSize(1024, 768);
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt)
    {
      // prevent double closing
      imageRendererGUI = null;

      // terminate device
      imageRenderer.terminate();
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

  }

}

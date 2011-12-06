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
package de.fraunhofer.fokus.upnp.util.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * This class is able to display a scaled image while maintaining the aspect ratio. Unused space is
 * painted in the background color.
 * 
 * @author Alexander König
 */
public class JImagePanel extends JPanel
{
  private static final long serialVersionUID        = 1L;

  /** Current image */
  protected Image           image                   = null;

  /** Scaled version of the current image */
  protected BufferedImage   scaledImage             = null;

  protected Dimension       lastScaledSize          = new Dimension(0, 0);

  private boolean           scaleImageAutomatically = true;

  /** Flag to draw the image as tiles over the complete panel */
  private boolean           tileImage               = false;

  private double            manualScale             = 1;

  private Vector            overlayList             = new Vector();

  public JImagePanel()
  {
    setOpaque(true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  public void paintComponent(Graphics g)
  {
    int panelHeight = this.getSize().height;
    int panelWidth = this.getSize().width;

    if (panelWidth <= 0 || panelHeight <= 0)
    {
      return;
    }

    // clear if empty
    g.setColor(getBackground());
    g.fillRect(0, 0, panelWidth, panelHeight);

    // draw image
    if (image != null)
    {
      // use manual scaling
      if (!scaleImageAutomatically)
      {
        if (tileImage)
        {
          int width = image.getWidth(null);
          int height = image.getHeight(null);

          int tilesX = (this.getWidth() + width - 1) / width;
          int tilesY = (this.getHeight() + height - 1) / height;

          for (int x = 0; x < tilesX; x++)
          {
            for (int y = 0; y < tilesY; y++)
            {
              g.drawImage(image, x * width, y * height, null);
            }
          }
        } else
        {
          if (manualScale == 1)
          {
            int imageLeft = (this.getSize().width - image.getWidth(null)) / 2;
            int imageTop = (this.getSize().height - image.getHeight(null)) / 2;

            // draw image to panel
            g.drawImage(image, imageLeft, imageTop, null);
          } else
          {
            int scaledImageWidth = (int)(image.getWidth(null) * manualScale);
            int scaledImageHeight = (int)(image.getHeight(null) * manualScale);

            int imageLeft = (this.getSize().width - scaledImageWidth) / 2;
            int imageTop = (this.getSize().height - scaledImageHeight) / 2;

            Dimension scaledImageSize = new Dimension(scaledImageWidth, scaledImageHeight);
            if (!scaledImageSize.equals(lastScaledSize) || scaledImage == null)
            {
              lastScaledSize = scaledImageSize;
              // create buffered target image
              scaledImage = new BufferedImage(scaledImageWidth, scaledImageHeight, BufferedImage.TYPE_INT_RGB);
              Graphics2D bufferedGraphics = scaledImage.createGraphics();

              bufferedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
              bufferedGraphics.drawImage(image,
                0,
                0,
                scaledImageWidth,
                scaledImageHeight,
                0,
                0,
                image.getWidth(null),
                image.getHeight(null),
                null);
            }
            // draw scaled image to panel
            g.drawImage(scaledImage, imageLeft, imageTop, null);
          }
        }
      } else
      {
        // autoscale
        float imageRatio = (float)image.getWidth(null) / (float)image.getHeight(null);
        float panelRatio = (float)panelWidth / (float)panelHeight;

        if (imageRatio < panelRatio)
        {
          panelWidth = Math.round(panelHeight * imageRatio);
        } else
        {
          panelHeight = Math.round(panelWidth / imageRatio);
        }
        int imageLeft = (this.getSize().width - panelWidth) / 2;
        int imageTop = (this.getSize().height - panelHeight) / 2;

        // we need a new scale
        if (!lastScaledSize.equals(this.getSize()) || scaledImage == null)
        {
          lastScaledSize = this.getSize();

          // create buffered target image
          scaledImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
          Graphics2D bufferedGraphics = scaledImage.createGraphics();

          bufferedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          bufferedGraphics.drawImage(image,
            0,
            0,
            panelWidth,
            panelHeight,
            0,
            0,
            image.getWidth(null),
            image.getHeight(null),
            null);
        }
        // draw scaled image to panel
        g.drawImage(scaledImage, imageLeft, imageTop, null);
      }
    }
    drawOverlays(g);
  }

  /**
   * Retrieves the image.
   * 
   * @return The image
   */
  public Image getImage()
  {
    return image;
  }

  /**
   * Set a new Image to display.
   * 
   * @param image
   *          The new Image or null to clear the image.
   */
  public void setImage(Image image)
  {
    this.image = image;
    this.scaledImage = null;
    revalidate();
    repaint();
  }

  /**
   * Retrieves the scaleImageAutomatically flag.
   * 
   * @return The scaleImageAutomatically flag
   */
  public boolean isScaleImageAutomatically()
  {
    return scaleImageAutomatically;
  }

  /**
   * Sets the scaleImageAutomatically flag.
   * 
   * @param scaleImage
   *          The new value for scaleImageAutomatically
   */
  public void setScaleImageAutomatically(boolean scaleImage)
  {
    if (scaleImageAutomatically != scaleImage)
    {
      this.scaleImageAutomatically = scaleImage;
      if (scaleImage)
      {
        tileImage = false;
      }

      repaint();
    }
  }

  /**
   * Retrieves the manualScale.
   * 
   * @return The manualScale
   */
  public double getManualScale()
  {
    return manualScale;
  }

  /**
   * Sets the manualScale.
   * 
   * @param manualScale
   *          The new value for manualScale
   */
  public void setManualScale(double manualScale)
  {
    if (this.manualScale != manualScale)
    {
      this.manualScale = manualScale;
      repaint();
    }
  }

  public void addOverlay(ImageOverlay overlay)
  {
    overlayList.add(overlay);
    repaint();
  }

  public void removeOverlay(ImageOverlay overlay)
  {
    overlayList.remove(overlay);
    repaint();
  }

  public void clearOverlays()
  {
    overlayList.clear();
    repaint();
  }

  public void drawOverlays(Graphics g)
  {
    for (int i = 0; i < overlayList.size(); i++)
    {
      ((ImageOverlay)overlayList.elementAt(i)).paint(g);
    }

  }

  /**
   * Retrieves the tileImage.
   * 
   * @return The tileImage
   */
  public boolean isTileImage()
  {
    return tileImage;
  }

  /**
   * Sets the tileImage.
   * 
   * @param tileImage
   *          The new value for tileImage
   */
  public void setTileImage(boolean tileImage)
  {
    if (this.tileImage != tileImage)
    {
      this.tileImage = tileImage;
      if (tileImage)
      {
        scaleImageAutomatically = false;
      }

      repaint();
    }
  }
}

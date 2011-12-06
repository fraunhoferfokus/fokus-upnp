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
package de.fraunhofer.fokus.upnp.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * This class provides helper methods for multimedia.
 * 
 * @author Alexander Koenig
 * 
 */
public class MediaHelper
{

  /** Resizes an image */
  public static Image resizeImage(Image image, int maxWidth, int maxHeight)
  {
    if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0)
    {
      return null;
    }
    // autoscale
    float imageRatio = (float)image.getWidth(null) / (float)image.getHeight(null);
    float panelRatio = (float)maxWidth / (float)maxHeight;

    if (imageRatio < panelRatio)
    {
      maxWidth = Math.round(maxHeight * imageRatio);
    } else
    {
      maxHeight = Math.round(maxWidth / imageRatio);
    }
    // create buffered target image
    BufferedImage bufferedImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D bufferedGraphics = bufferedImage.createGraphics();

    bufferedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    bufferedGraphics.drawImage(image,
      0,
      0,
      maxWidth,
      maxHeight,
      0,
      0,
      image.getWidth(null),
      image.getHeight(null),
      null);

    return bufferedImage;
  }

  /** Loads an image from a URL */
  public static Image loadImageFromURL(String imageURL, Component component)
  {
    if (imageURL == null)
    {
      return null;
    }
    // System.out.println("Try to load image: " + imageURL);
    Image result = null;

    MediaTracker mediaTracker = new MediaTracker(component);
    try
    {
      result = Toolkit.getDefaultToolkit().createImage(new URL(imageURL));
    } catch (Exception ex)
    {
      System.out.println("Error loading image: " + ex.getMessage());
      return null;
    }
    mediaTracker.addImage(result, 0);
    try
    {
      mediaTracker.waitForAll();
    } catch (InterruptedException ie)
    {
      System.out.println("Error loading image: " + ie.getMessage());
      return null;
    }
    // System.out.println("Image loaded.");
    return result;
  }

  /** Creates an image that is mirrored */
  public static Image createMirrorImage(Image image, Color backgroundColor, int mirrorSize)
  {
    if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0 || mirrorSize < 0)
    {
      return null;
    }

    BufferedImage result =
      new BufferedImage(image.getWidth(null), image.getHeight(null) + mirrorSize, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = result.createGraphics();
    graphics2D.drawImage(image, 0, 0, null);
    // draw mirrored image
    graphics2D.drawImage(image,
      0,
      image.getHeight(null) + 5,
      image.getWidth(null),
      image.getHeight(null) + mirrorSize,
      0,
      image.getHeight(null),
      image.getWidth(null),
      image.getHeight(null) - mirrorSize - 5,
      null);

    int backRed = backgroundColor.getRed();
    int backGreen = backgroundColor.getGreen();
    int backBlue = backgroundColor.getBlue();

    for (int y = 0; y < mirrorSize; y++)
    {
      double backRatio = 0.5 + y / ((mirrorSize - 1.0) * 2.0);
      // System.out.println("Backratio at " + y + " is " + backRatio);
      double ratio = 1 - backRatio;
      for (int x = 0; x < image.getWidth(null); x++)
      {
        int color = result.getRGB(x, y + image.getHeight(null));
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color >> 0 & 0xFF;

        int newColor =
          0xFF000000 + (int)Math.round(red * ratio + backRed * backRatio) * 65536 +
            (int)Math.round(green * ratio + backGreen * backRatio) * 256 +
            (int)Math.round(blue * ratio + backBlue * backRatio);

        result.setRGB(x, y + image.getHeight(null), newColor);
      }
    }

    return result;
  }

}

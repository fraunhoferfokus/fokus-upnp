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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

/**
 * @author tje
 * 
 */
public class ImageHandler
{
  private String  function;

  private Image   image;

  private Image   imageFocus;

  private Image   imageFocusActive;

  private Image   imageActive;

  private Image   imageDrawn;

  private int     imagePosX;

  private int     imagePosY;

  private int     imageWidth;

  private int     imageHeight;

  private int     animationWidth;

  private String  toolTipText = "";

  private boolean showToolTip = false;

  private boolean active      = false;

  public ImageHandler(String function,
    Image image,
    Image imageFocus,
    Image imageFocusActive,
    int imagePosX,
    int imagePosY,
    int imageWidth,
    int imageHeight,
    String toolTipText)
  {
    this.function = function;
    this.image = image;
    this.imageFocus = imageFocus;
    this.imageFocusActive = imageFocusActive;
    this.imagePosX = imagePosX;
    this.imagePosY = imagePosY;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.animationWidth = 0;
    this.toolTipText = toolTipText;
    imageDrawn = image;
  }

  public ImageHandler(String function,
    Image image,
    Image imageFocus,
    Image imageFocusActive,
    Image imageActive,
    int imagePosX,
    int imagePosY,
    int imageWidth,
    int imageHeight,
    String toolTipText)
  {
    this(function, image, imageFocus, imageFocusActive, imagePosX, imagePosY, imageWidth, imageHeight, toolTipText);
    this.imageActive = imageActive;
  }

  public void setAnimationWidth(int i)
  {
    animationWidth = i;
  }

  public Image getImage()
  {
    return image;
  }

  public Image getImageFocus()
  {
    return imageFocus;
  }

  public Image getImageFocusActive()
  {
    return imageFocusActive;
  }

  public Image getImageActive()
  {
    return imageActive;
  }

  public void setImagePosX(int i)
  {
    imagePosX = i;
  }

  public int getImagePosX()
  {
    return imagePosX;
  }

  public int getImagePosY()
  {
    return imagePosY;
  }

  public void setImageWidth(int w)
  {
    imageWidth = w;
  }

  public int getImageWidth()
  {
    return imageWidth;
  }

  public int getImageHeigth()
  {
    return imageHeight;
  }

  public String getFunction()
  {
    return function;
  }

  public void setActiveImage()
  {
    imageDrawn = imageFocusActive;
    active = true;
  }

  public void setActiveFocusImage()
  {
    imageDrawn = imageFocusActive;
  }

  public void setFocusImage()
  {
    imageDrawn = imageFocus;
  }

  public void setDefaultImage()
  {
    imageDrawn = image;
    active = false;
  }

  public void paint(Graphics g)
  {

    if (imageDrawn != null)
    {
      g.drawImage(imageDrawn,
        imagePosX,
        imagePosY,
        imageDrawn.getWidth(null) - animationWidth,
        imageDrawn.getHeight(null),
        null);
    }

    if (showToolTip && !toolTipText.equals(""))
    {
      drawToolTip(g);
    }
  }

  private void drawToolTip(Graphics gbi)
  {
    Font font = new Font("ARIAL", Font.PLAIN, 10);
    FontMetrics fm = gbi.getFontMetrics(font);
    gbi.setFont(font);

    int stringWidth = fm.stringWidth(toolTipText);
    int fontHeight = font.getSize();
    // Rectangle2D toolField = new Rectangle2D.Float(getImagePosX() + (getImageWidth()),
    // getImagePosY() + (getImageHeigth()), stringWidth + (fontHeight / 2), fontHeight
    // + (fontHeight / 2));

    gbi.setColor(Color.BLACK);
    gbi.drawRect(getImagePosX() + getImageWidth() - 1, getImagePosY() + getImageHeigth() - 1, stringWidth + fontHeight /
      2 + 1, fontHeight + fontHeight / 2 + 1);
    gbi.setColor(new Color(255, 255, 225));

    /*
     * gbi.fill(toolField); gbi.setColor(Color.BLACK); gbi.drawString(toolTipText, ((float)
     * toolField.getCenterX() - (stringWidth / 2)), ((float) toolField.getCenterY() + (fontHeight /
     * 2)-1));
     */
  }

  public boolean mouseClicked(int x, int y)
  {
    return mouseOver(x, y);
  }

  public boolean mouseOver(int x, int y)
  {
    if (x >= imagePosX && x <= imagePosX + imageWidth && y >= imagePosY && y <= imagePosY + imageHeight)
    {
      showToolTip = true;

      if (active)
      {
        imageDrawn = imageFocusActive;
      } else
      {
        imageDrawn = imageFocus;
      }
      return true;
    }

    if (active)
    {
      imageDrawn = imageActive;
    } else
    {
      imageDrawn = image;
    }

    showToolTip = false;

    return false;
  }
}

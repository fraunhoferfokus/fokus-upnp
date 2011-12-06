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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.border.AbstractBorder;

/**
 * This class implements a nice round border.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SmoothBorder extends AbstractBorder
{

  private static final long serialVersionUID = 1L;

  private Color             color;

  private Point             shadow           = new Point(3, 5);

  public SmoothBorder()
  {
    color = SmoothButton.darker(ButtonConstants.BACKGROUND_COLOR, 20);
  }

  public SmoothBorder(Color color)
  {
    this.color = color;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
   */
  public Insets getBorderInsets(Component c)
  {
    return new Insets(10, 10, 10 + shadow.x, 10 + shadow.y);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
  {
    SmoothButton.paintFrame(g, color, c.getBackground(), new Dimension(width, height), shadow, 6);
  }

  /**
   * Retrieves the color.
   * 
   * @return The color
   */
  public Color getColor()
  {
    return color;
  }

  /**
   * Sets the color.
   * 
   * @param color
   *          The new value for color
   */
  public void setColor(Color color)
  {
    this.color = color;
  }

  /**
   * Retrieves the shadow.
   * 
   * @return The shadow
   */
  public Point getShadow()
  {
    return shadow;
  }

  /**
   * Sets the shadow.
   * 
   * @param shadow
   *          The new value for shadow
   */
  public void setShadow(Point shadow)
  {
    this.shadow = shadow;
  }

}

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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

/**
 * This class is used to add overlays to a panel or image
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class ImageOverlay
{

  public static final int OVAL = 1;

  protected int           type;

  protected Point         position;

  protected Dimension     size;

  protected Color         color;

  public ImageOverlay(int type, Point position, Dimension size, Color color)
  {
    this.type = type;
    this.position = position;
    this.size = size;
    this.color = color;
  }

  public void paint(Graphics g)
  {
    if (type == OVAL)
    {
      Color lastColor = g.getColor();
      g.setColor(color);
      g.fillOval(position.x, position.y, size.width, size.height);
      g.setColor(lastColor);
    }

  }

  /**
   * Retrieves the position.
   * 
   * @return The position
   */
  public Point getPosition()
  {
    return position;
  }

  /**
   * Sets the position.
   * 
   * @param position
   *          The new value for position
   */
  public void setPosition(Point position)
  {
    this.position = position;
  }

}

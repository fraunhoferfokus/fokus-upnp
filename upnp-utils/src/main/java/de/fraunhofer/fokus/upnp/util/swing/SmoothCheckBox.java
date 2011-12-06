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
 * This class represents a button that can hold a boolean value.
 * 
 * @author Alexander Koenig
 */
public class SmoothCheckBox extends SmoothValueButton
{

  private static final long serialVersionUID = 1L;

  /** Creates a new instance of SmoothCheckBox */
  public SmoothCheckBox(Dimension size, boolean value, String id)
  {
    super(size, 12, "", value ? "true" : "false", id);
  }

  public boolean isChecked()
  {
    return value.equalsIgnoreCase("true");
  }

  public void setChecked(boolean newState)
  {
    value = newState ? "true" : "false";
    repaint();
  }

  public boolean getState()
  {
    return value.equalsIgnoreCase("true");
  }

  public void setState(boolean newState)
  {
    value = newState ? "true" : "false";
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.buttons.SmoothButton#paint(java.awt.Graphics)
   */
  public void paint(Graphics g)
  {
    super.paint(g);

    // background
    // g.setColor(getBackground());
    // g.fillRect(0, 0, getSize().width, getSize().height);

    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);
    Color color = getCurrentButtonColor();

    // paintRoundButton(bufferedImageGraphics, color, getBackground(), size, shadowOffset);

    Point emptySpaceCorner = new Point(4, 4);
    // add 3 to size to allow nice cross with width 3
    Dimension emptySpaceSize =
      new Dimension(innerSize.height - emptySpaceCorner.y * 2 + 3, innerSize.height - emptySpaceCorner.y * 2);

    paintButtonEmptySpace(bufferedImageGraphics, color, getBackground(), emptySpaceSize, emptySpaceCorner);
    if (isChecked())
    {
      Point corner = new Point(emptySpaceCorner.x + 3, emptySpaceCorner.y + 3);
      paintButtonCross(bufferedImageGraphics, color, emptySpaceSize.height - 6, corner);
    }
    g.drawImage(bufferedImage, 0, 0, null);

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.buttons.SmoothButton#mouseReleased(int)
   */
  protected void mouseReleased(int modifiers)
  {
    if (isSelectable())
    {
      setChecked(!isChecked());
    }
  }

}

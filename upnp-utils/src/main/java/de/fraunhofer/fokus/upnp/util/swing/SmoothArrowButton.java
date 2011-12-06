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

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * This class provides a nice-looking, smooth arrow button that is used for scrollbars.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SmoothArrowButton extends BasicArrowButton
{

  /** Color for arrow button */
  public static Color       BUTTON_COLOR        = ButtonConstants.BUTTON_COLOR;

  /** Color for pressed arrow button */
  public static Color       ACTIVE_BUTTON_COLOR = ButtonConstants.ACTIVE_BUTTON_COLOR;

  /**  */
  private static final long serialVersionUID    = 1L;

  /**
   * Creates a new instance of SmoothArrowButton.
   * 
   * @param direction
   * @param background
   */
  public SmoothArrowButton(int direction, Color background)
  {
    super(direction, background,
    // the following values are never used
      UIManager.getColor("controlShadow"),
      UIManager.getColor("controlDkShadow"),
      UIManager.getColor("controlLtHighlight"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicArrowButton#paint(java.awt.Graphics)
   */
  public void paint(Graphics g)
  {
    Color origColor;
    boolean isPressed, isEnabled;
    int w, h, size;

    w = getSize().width;
    h = getSize().height;
    origColor = g.getColor();
    isPressed = getModel().isPressed();
    isEnabled = isEnabled();

    Color color = BUTTON_COLOR;
    if (isPressed)
    {
      color = ACTIVE_BUTTON_COLOR;
    }

    // background
    g.setColor(getBackground());
    g.fillRect(0, 0, getSize().width, getSize().height);

    SmoothButton.paintRoundButton(g, color, getBackground(), new Dimension(w, h), new Point(0, 0));

    if (isPressed)
    {
      g.translate(1, 1);
    }

    // Draw the arrow
    size = Math.min((h - 4) / 3, (w - 4) / 3);
    size = Math.max(size, 2);
    paintTriangle(g, (w - size) / 2, (h - size) / 2, size, direction, isEnabled);

    // Reset the Graphics back to it's original settings
    if (isPressed)
    {
      g.translate(-1, -1);
    }
    g.setColor(origColor);
  }

}

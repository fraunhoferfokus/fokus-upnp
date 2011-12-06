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

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class SmoothScrollbar extends BasicScrollBarUI
{

  /** Background color for track */
  public static Color TRACK_COLOR         = Color.white;

  /** Color for arrow and thumb buttons */
  public static Color BUTTON_COLOR        = ButtonConstants.BUTTON_COLOR;

  /** Color for pressed arrow and thumb buttons */
  public static Color ACTIVE_BUTTON_COLOR = ButtonConstants.ACTIVE_BUTTON_COLOR;

  // Create our own scrollbar UI!
  public static ComponentUI createUI(JComponent c)
  {
    return new SmoothScrollbar();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicScrollBarUI#paintTrack(java.awt.Graphics,
   *      javax.swing.JComponent, java.awt.Rectangle)
   */
  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
  {
    g.setColor(TRACK_COLOR);
    g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

    if (trackHighlight == DECREASE_HIGHLIGHT)
    {
      paintDecreaseHighlight(g);
    } else if (trackHighlight == INCREASE_HIGHLIGHT)
    {
      paintIncreaseHighlight(g);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicScrollBarUI#paintThumb(java.awt.Graphics,
   *      javax.swing.JComponent, java.awt.Rectangle)
   */
  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
  {
    if (!c.isEnabled())
    {
      return;
    }

    g.translate(thumbBounds.x, thumbBounds.y);
    if (scrollbar.getOrientation() == Adjustable.VERTICAL)
    {
      SmoothButton.paintRoundButton(g,
        BUTTON_COLOR,
        TRACK_COLOR,
        new Dimension(thumbBounds.width, thumbBounds.height),
        new Point(0, 0));
    } else
    { // HORIZONTAL
      SmoothButton.paintRoundButton(g,
        BUTTON_COLOR,
        TRACK_COLOR,
        new Dimension(thumbBounds.width, thumbBounds.height),
        new Point(0, 0));
    }
    g.translate(-thumbBounds.x, -thumbBounds.y);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicScrollBarUI#createDecreaseButton(int)
   */
  protected JButton createDecreaseButton(int orientation)
  {
    SmoothArrowButton.BUTTON_COLOR = BUTTON_COLOR;
    SmoothArrowButton.ACTIVE_BUTTON_COLOR = ACTIVE_BUTTON_COLOR;

    JButton button = new SmoothArrowButton(orientation, TRACK_COLOR);
    return button;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicScrollBarUI#createIncreaseButton(int)
   */
  protected JButton createIncreaseButton(int orientation)
  {
    SmoothArrowButton.BUTTON_COLOR = BUTTON_COLOR;
    SmoothArrowButton.ACTIVE_BUTTON_COLOR = ACTIVE_BUTTON_COLOR;

    JButton button = new SmoothArrowButton(orientation, TRACK_COLOR);
    return button;
  }
}

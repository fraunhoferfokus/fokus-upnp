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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * 
 * @author Alexander Koenig
 * 
 */
public class JGUIControlBottomPanel extends JPanel
{

  private static final long serialVersionUID = 1L;

  private String            text;

  public JGUIControlBottomPanel()
  {
    setFont(new Font("Serif", 0, 12));
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paint(Graphics g)
  {
    super.paint(g);
    // clear if empty
    g.setColor(getBackground());
    g.fillRect(0, 0, this.getSize().width, this.getSize().height);

    int height = this.getSize().height;

    g.setColor(ButtonConstants.BACKGROUND_COLOR);
    g.fillRect(this.getSize().width - 2 * height, 0, 2 * height, height);

    g.setColor(getBackground());
    g.fillArc(this.getSize().width - 3 * height, 0, 2 * height, 2 * height, 0, 90);

    Color fontShadowColor = SmoothButton.brighter(getBackground(), 30);

    if (text != null)
    {
      SmoothButton.paintString(g, text, Color.black, fontShadowColor, this.getSize(), new Point(10,
        this.getSize().height / 2), false, false, true);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize()
  {
    Dimension result = super.getPreferredSize();
    result.height = Math.max(30, result.height);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#update(java.awt.Graphics)
   */
  public void update(Graphics g)
  {

  }

  /**
   * Retrieves the text.
   * 
   * @return The text
   */
  public String getText()
  {
    return text;
  }

  /**
   * Sets the text.
   * 
   * @param text
   *          The new value for text
   */
  public void setText(String text)
  {
    this.text = text;
    repaint();
  }

}

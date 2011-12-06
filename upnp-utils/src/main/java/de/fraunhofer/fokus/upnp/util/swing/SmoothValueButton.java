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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * 
 * @author Alexander Koenig
 */
public class SmoothValueButton extends SmoothButton
{

  private static final long serialVersionUID = 1L;

  protected String          value;

  /**
   * Creates a new instance of SmoothValueButton.
   * 
   * @param size
   *          The size of the button
   * @param fontSize
   *          The font size
   * @param name
   *          The name of the value
   * @param value
   *          The initial value
   * @param id
   *          The ID for actionPerformed
   */
  public SmoothValueButton(Dimension size, int fontSize, String name, String value, String id)
  {

    super(size, fontSize, name, id);
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.swing.SmoothButton#mousePressed(int)
   */
  protected void mousePressed(int modifiers)
  {
    if ((modifiers & ActionEvent.CTRL_MASK) != 0)
    {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.swing.SmoothButton#paintText(java.awt.Graphics,
   *      java.awt.Color, java.awt.Dimension)
   */
  public void paintText(Graphics g, Color color, Dimension innerSize)
  {
    boolean highlighted = mouseIn && id != null && !selected && selectable;
    Color fontShadowColor = highlighted ? ButtonConstants.FONT_HIGHLIGHT_COLOR : brighter(getCurrentButtonColor(), 30);

    Dimension nameSize = new Dimension(innerSize.width * 2 / 3, innerSize.height);

    paintString(bufferedImageGraphics,
      text,
      fontColor,
      fontShadowColor,
      nameSize,
      new Point(10, innerSize.height / 2),
      highlighted,
      false,
      true);

    // retrieve name width
    int nameWidth = getStringSize(bufferedImageGraphics, text);
    if (isShortenedString(bufferedImageGraphics, text, nameSize))
    {
      nameWidth = nameSize.width;
    }

    // get maximum size for value
    Dimension valueSize = new Dimension(innerSize.width - 15 - nameWidth, innerSize.height);

    paintString(bufferedImageGraphics, value, fontColor, fontShadowColor, valueSize, new Point(nameWidth + 15,
      innerSize.height / 2), highlighted, false, true);

    if (automaticTooltip)
    {
      if (isShortenedString(bufferedImageGraphics, value, valueSize))
      {
        setToolTipText(value);
      } else
      {
        setToolTipText("");
      }
    }
  }

}

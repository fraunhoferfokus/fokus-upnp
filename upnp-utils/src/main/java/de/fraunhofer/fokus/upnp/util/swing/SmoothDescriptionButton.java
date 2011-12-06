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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

/**
 * This button contains an additional description
 * 
 * @author Alexander Koenig
 */
public class SmoothDescriptionButton extends SmoothButton
{

  private static final long serialVersionUID = 1L;

  protected String          description;

  protected int             descriptionFontSize;

  protected Font            descriptionFont;

  /**
   * Creates a new instance of SmoothDescriptionButton.
   * 
   * @param size
   *          The size of the button
   * @param fontSize
   *          The font size
   * @param descriptionFontSize
   *          The font size for the description
   * @param name
   *          The name of the value
   * @param description
   *          The initial description
   * @param id
   *          The ID for actionPerformed
   */
  public SmoothDescriptionButton(Dimension size,
    int fontSize,
    int descriptionFontSize,
    String name,
    String description,
    String id)
  {
    super(size, fontSize, name, id);
    this.description = description;
    this.descriptionFontSize = descriptionFontSize;
    descriptionFont = new Font("Serif", 0, descriptionFontSize);
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
    repaint();
  }

  public void paintText(Graphics g, Color color, Dimension innerSize)
  {
    boolean highlighted = mouseIn && id != null && !selected && selectable;
    Color fontShadowColor = highlighted ? ButtonConstants.FONT_HIGHLIGHT_COLOR : brighter(getCurrentButtonColor(), 30);

    // divide area between text and description
    int textHeight = (int)Math.round(innerSize.height * fontSize * 1.0 / (fontSize + descriptionFontSize));
    int descriptionHeight = innerSize.height - textHeight;
    Color descriptionColor = brighter(fontColor, 40);

    Point textCenter = new Point(innerSize.width / 2, textHeight / 2);
    Point descCenter = new Point(innerSize.width / 2, textHeight + descriptionHeight / 2 - 3);
    // shift text if image is present
    if (iconImage != null)
    {
      innerSize.width = innerSize.width - 35;
      textCenter = new Point(35 + innerSize.width / 2, textHeight / 2);
      descCenter = new Point(35 + innerSize.width / 2, textHeight + descriptionHeight / 2 - 3);
    }

    if (centeredText)
    {
      bufferedImageGraphics.setFont(textFont);
      paintString(bufferedImageGraphics,
        text,
        fontColor,
        fontShadowColor,
        innerSize,
        textCenter,
        highlighted,
        true,
        true);
      bufferedImageGraphics.setFont(descriptionFont);
      paintString(bufferedImageGraphics,
        description,
        descriptionColor,
        fontShadowColor,
        innerSize,
        descCenter,
        highlighted,
        true,
        true);
    } else
    {
      int textStart = iconImage != null ? 35 : 10;
      bufferedImageGraphics.setFont(textFont);
      paintString(bufferedImageGraphics, text, fontColor, fontShadowColor, innerSize, new Point(textStart,
        textHeight / 2), highlighted, false, true);
      bufferedImageGraphics.setFont(descriptionFont);
      paintString(bufferedImageGraphics,
        description,
        descriptionColor,
        fontShadowColor,
        innerSize,
        new Point(textStart, textHeight + descriptionHeight / 2 - 3),
        highlighted,
        true,
        true);
    }

    // set tooltip if necessary
    if (automaticTooltip)
    {
      setToolTipText(isShortenedString(bufferedImageGraphics, text, innerSize) ? text : "");
    }
  }

}

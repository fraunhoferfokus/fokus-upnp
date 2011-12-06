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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class represents an area that can be filled with text.
 * 
 * @author Alexander Koenig
 */
public class SmoothArea extends SmoothButton
{
  private static final long serialVersionUID = 1L;

  protected Vector          content;

  /** Creates a new instance of SmoothArea */
  public SmoothArea(Dimension size, String id)
  {
    this(size, 18, id);
  }

  /** Creates a new instance of SmoothArea */
  public SmoothArea(Dimension size, int fontSize, String id)
  {
    super(size, fontSize, "", id);
    content = new Vector();
  }

  public Vector getContent()
  {
    return content;
  }

  public void clearContent()
  {
    content.clear();
    repaint();
  }

  public void addLine(String value)
  {
    content.add(value);
    repaint();
  }

  public void setContent(String value)
  {
    content.clear();
    StringTokenizer stringTokenizer = new StringTokenizer(value, CommonConstants.NEW_LINE);
    while (stringTokenizer.hasMoreTokens())
    {
      content.add(stringTokenizer.nextToken());
    }
    repaint();
  }

  /** Resizes the component so that the current text is completely shown */
  public void setSizeToFitContent()
  {
    FontMetrics fontMetrics = bufferedImageGraphics.getFontMetrics();
    int fontHeight = fontMetrics.getHeight();

    Dimension size = new Dimension(getPreferredSize().width, fontHeight + 5 + 10);
    if (content != null && content.size() != 0)
    {
      size.height = content.size() * (fontHeight + 5) + 10;
    }
    setPreferredSize(size);
    setSize(size);
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
    bufferedImageGraphics.setColor(getBackground());
    bufferedImageGraphics.fillRect(0, 0, getSize().width, getSize().height);

    Color color = selected ? activeButtonColor : buttonColor;

    bufferedImageGraphics.setColor(color);
    bufferedImageGraphics.fillRoundRect(0, 0, size.width - 1, size.height - 1, 10, 10);
    // top
    bufferedImageGraphics.setColor(brighter(color, 50));
    bufferedImageGraphics.drawLine(4, 1, size.width - 4 - 1, 1);
    bufferedImageGraphics.setColor(brighter(color, 30));
    bufferedImageGraphics.drawLine(2, 2, size.width - 2 - 1, 2);
    bufferedImageGraphics.setColor(brighter(color, 10));
    bufferedImageGraphics.drawLine(2, 3, size.width - 2 - 1, 3);
    // sides
    bufferedImageGraphics.setColor(brighter(color, 20));
    bufferedImageGraphics.drawLine(1, 5, 1, size.height - 6);
    bufferedImageGraphics.drawLine(size.width - 3, 5, size.width - 3, size.height - 6);
    // bottom
    bufferedImageGraphics.setColor(darker(color, 50));
    bufferedImageGraphics.drawLine(3, size.height - 2, size.width - 3 - 1, size.height - 2);
    bufferedImageGraphics.setColor(darker(color, 30));
    bufferedImageGraphics.drawLine(2, size.height - 3, size.width - 2 - 1, size.height - 3);
    bufferedImageGraphics.setColor(darker(color, 10));
    bufferedImageGraphics.drawLine(2, size.height - 4, size.width - 2 - 1, size.height - 4);

    if (content != null && content.size() != 0)
    {
      FontMetrics fontMetrics = bufferedImageGraphics.getFontMetrics();
      // int fontHeight =fontMetrics.getHeight()+fontMetrics.getAscent();
      int fontHeight = fontMetrics.getHeight();
      for (int i = 0; i < content.size(); i++)
      {
        bufferedImageGraphics.setColor(brighter(color, 30));
        bufferedImageGraphics.drawString((String)content.elementAt(i), 11, (i + 1) * fontHeight + 5);

        bufferedImageGraphics.setColor(fontColor);
        bufferedImageGraphics.drawString((String)content.elementAt(i), 10, (i + 1) * fontHeight + 4);
      }
    }
    g.drawImage(bufferedImage, 0, 0, null);
  }

}

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

/**
 * This class represents a button with a playback command.
 * 
 * @author Alexander Koenig
 */
public class SmoothCommandButton extends SmoothButton
{

  private static final long serialVersionUID = 1L;

  public static final int   COMMAND_PLAY     = 1;

  public static final int   COMMAND_STOP     = 2;

  public static final int   COMMAND_PREVIOUS = 3;

  public static final int   COMMAND_NEXT     = 4;

  public static final int   COMMAND_PAUSE    = 5;

  public static final int   COMMAND_SET_URI  = 6;

  private int               command;

  /** Creates a new instance of DeviceButton */
  public SmoothCommandButton(Dimension size, int fontSize, String id, int command)
  {
    super(size, fontSize, "", id);
    this.command = command;
    repaint();
  }

  public void setCommand(int command)
  {
    if (this.command != command)
    {
      this.command = command;
      repaint();
    }
  }

  public int getCommand()
  {
    return command;
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

    boolean highlighted = mouseIn && id != null && !selected && selectable;

    if (command == COMMAND_PREVIOUS)
    {
      int size = 5;

      bufferedImageGraphics.setColor(brighter(color, 30));
      int[] x = new int[] {
          innerSize.width / 2 + 1, innerSize.width / 2 - size + 1, innerSize.width / 2 + 1
      };
      int[] y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 + 1, innerSize.height / 2 + size + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
      x = new int[] {
          innerSize.width / 2 + size + 1, innerSize.width / 2 + 1, innerSize.width / 2 + size + 1
      };
      y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 + 1, innerSize.height / 2 + size + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      x = new int[] {
          innerSize.width / 2, innerSize.width / 2 - size, innerSize.width / 2
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2, innerSize.height / 2 + size
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
      x = new int[] {
          innerSize.width / 2 + size, innerSize.width / 2, innerSize.width / 2 + size
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2, innerSize.height / 2 + size
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
    }
    if (command == COMMAND_NEXT)
    {
      int size = 5;
      // draw shadow
      bufferedImageGraphics.setColor(brighter(color, 30));
      int[] x = new int[] {
          innerSize.width / 2 - size + 1, innerSize.width / 2 + 1, innerSize.width / 2 - size + 1
      };
      int[] y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 + 1, innerSize.height / 2 + size + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
      x = new int[] {
          innerSize.width / 2 + 1, innerSize.width / 2 + size + 1, innerSize.width / 2 + 1
      };
      y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 + 1, innerSize.height / 2 + size + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      x = new int[] {
          innerSize.width / 2 - size, innerSize.width / 2, innerSize.width / 2 - size
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2, innerSize.height / 2 + size
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
      x = new int[] {
          innerSize.width / 2, innerSize.width / 2 + size, innerSize.width / 2
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2, innerSize.height / 2 + size
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
    }
    if (command == COMMAND_PLAY)
    {
      int size = 5;
      // draw shadow
      bufferedImageGraphics.setColor(brighter(color, 30));
      int[] x = new int[] {
          innerSize.width / 2 - size + 1, innerSize.width / 2 + size + 1, innerSize.width / 2 - size + 1
      };
      int[] y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 + 1, innerSize.height / 2 + size + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      x = new int[] {
          innerSize.width / 2 - size, innerSize.width / 2 + size, innerSize.width / 2 - size
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2, innerSize.height / 2 + size
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
    }
    if (command == COMMAND_STOP)
    {
      int size = 5;
      // draw shadow
      bufferedImageGraphics.setColor(brighter(color, 30));
      bufferedImageGraphics.fillRect(innerSize.width / 2 - size + 1,
        innerSize.height / 2 - size + 1,
        2 * size,
        2 * size);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      bufferedImageGraphics.fillRect(innerSize.width / 2 - size, innerSize.height / 2 - size, 2 * size, 2 * size);
    }
    if (command == COMMAND_PAUSE)
    {
      int size = 5;

      int left = innerSize.width / 2 - size;
      int top = innerSize.height / 2 - size + 1;
      // draw shadow
      bufferedImageGraphics.setColor(brighter(color, 30));
      bufferedImageGraphics.fillRect(left, top, size, 2 * size);
      left = innerSize.width / 2 + 2;
      bufferedImageGraphics.fillRect(left, top, size, 2 * size);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      top -= 1;
      left = innerSize.width / 2 - size - 1;
      bufferedImageGraphics.fillRect(left, top, size, 2 * size);
      left = innerSize.width / 2 + 1;
      bufferedImageGraphics.fillRect(left, top, size, 2 * size);
    }
    if (command == COMMAND_SET_URI)
    {
      int size = 5;

      int left = innerSize.width / 2 - size + 1;
      int top = innerSize.height / 2 + 2;
      // draw shadow
      bufferedImageGraphics.setColor(brighter(color, 30));
      bufferedImageGraphics.fillRect(left, top, 2 * size, size);
      int[] x = new int[] {
          innerSize.width / 2 - size + 1, innerSize.width / 2 + size + 1, innerSize.width / 2 + 1
      };
      int[] y = new int[] {
          innerSize.height / 2 - size + 1, innerSize.height / 2 - size + 1, innerSize.height / 2 + 1
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);

      if (highlighted)
      {
        bufferedImageGraphics.setColor(activeButtonColor);
      } else
      {
        bufferedImageGraphics.setColor(Color.black);
      }

      left = innerSize.width / 2 - size;
      top = innerSize.height / 2 + 1;
      bufferedImageGraphics.fillRect(left, top, 2 * size, size);
      x = new int[] {
          innerSize.width / 2 - size, innerSize.width / 2 + size, innerSize.width / 2
      };
      y = new int[] {
          innerSize.height / 2 - size, innerSize.height / 2 - size, innerSize.height / 2
      };
      bufferedImageGraphics.fillPolygon(x, y, 3);
    }

    g.drawImage(bufferedImage, 0, 0, null);
  }

}

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
import java.awt.event.ActionEvent;

/**
 * This button represents an integer value that can be changed with a slider.
 * 
 * @author Alexander Koenig
 */
public class SmoothSliderValueButton extends SmoothValueButton
{

  private static final long serialVersionUID = 1L;

  private int               currentValue;

  private int               minValue;

  private int               maxValue;

  /** Start of x-coordinate of value */
  private int               valueStart;

  /** Size of slider */
  private int               sliderWidth;

  /** Start of x-coordinate of slider */
  private int               sliderStart;

  /** Size of knob */
  private int               knobWidth;

  private boolean           pressedInButton;

  /** Creates a new instance of SmoothSliderValueButton */
  public SmoothSliderValueButton(Dimension size,
    int valueStart,
    int sliderStart,
    int fontSize,
    String name,
    int value,
    String id)
  {
    super(size, fontSize, name, "", id);
    minValue = 0;
    maxValue = 100;
    currentValue = value;
    this.valueStart = valueStart;
    this.sliderStart = sliderStart;
    this.sliderWidth = size.width - shadowOffset.x - sliderStart - 7;
    this.knobWidth = size.height - shadowOffset.y - 15;

    setValue(Integer.toString(currentValue));
  }

  /** Retrieves the current value */
  public int getCurrentValue()
  {
    return currentValue;
  }

  /** Retrieves the current value */
  public String getValue()
  {
    return Integer.toString(currentValue);
  }

  /** Sets the current value */
  public void setCurrentValue(int value)
  {
    if (selectable && currentValue != value)
    {
      currentValue = Math.min(maxValue, Math.max(minValue, value));
      repaint();
    }
  }

  public int getMinValue()
  {
    return minValue;
  }

  public void setMinValue(int value)
  {
    minValue = value;
  }

  public int getMaxValue()
  {
    return maxValue;
  }

  public void setMaxValue(int value)
  {
    maxValue = value;
  }

  /** Retrieves the start coordinate of the slider */
  public int getSliderStart()
  {
    return sliderStart;
  }

  /**
   * Retrieves the width of the slider
   * 
   * @return
   */
  public int getSliderWidth()
  {
    return sliderWidth;
  }

  /** Updates the slider */
  protected void mouseMoved()
  {
    super.mouseMoved();
    if (mouseIn && pressed && selectable)
    {
      // set knob position and value
      int newValue = 0;

      // calculate relative position
      float relativePos = (float)(mousePos.x - sliderStart - knobWidth / 2 - 1) / (float)(sliderWidth - knobWidth - 2);
      relativePos = Math.max(0, Math.min(1, relativePos));

      newValue = Math.round(minValue + (maxValue - minValue) * relativePos);

      if (newValue != currentValue)
      {
        currentValue = newValue;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.buttons.SmoothButton#mousePressed()
   */
  protected void mousePressed(int modifiers)
  {
    pressedInButton = mouseIn;
    if (mouseIn && pressed && selectable)
    {
      // set knob position and value
      int newValue = 0;

      // calculate relative position
      float relativePos = (float)(mousePos.x - sliderStart - knobWidth / 2 - 1) / (float)(sliderWidth - knobWidth - 2);
      relativePos = Math.max(0, Math.min(1, relativePos));

      newValue = Math.round(minValue + (maxValue - minValue) * relativePos);

      if (newValue != currentValue)
      {
        currentValue = newValue;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.buttons.SmoothButton#mouseReleased()
   */
  protected void mouseReleased(int modifiers)
  {
    if (pressedInButton && selectable && !mouseIn)
    {
      // create action event
      if (actionListener != null)
      {
        actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, id, modifiers));
      }
    }
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

    Point shadowOffset = new Point(3, 5);
    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);
    Color color = selected ? activeButtonColor : buttonColor;

    if (!isSelectable())
    {
      color = ButtonConstants.DISABLED_BUTTON_COLOR;
    }

    // paintRoundButton(bufferedImageGraphics, color, getBackground(), size, shadowOffset);

    // draw slider field
    Point emptySpaceCorner = new Point(sliderStart, 4);
    Dimension emptySpaceSize =
      new Dimension(innerSize.width - emptySpaceCorner.x - 7, innerSize.height - emptySpaceCorner.y * 2);

    paintButtonEmptySpace(bufferedImageGraphics, color, getBackground(), emptySpaceSize, emptySpaceCorner);

    // calculate relative position of knob
    float relativePosition = (float)(currentValue - minValue) / (float)(maxValue - minValue);

    Dimension knobSize = new Dimension(emptySpaceSize.height - 7, emptySpaceSize.height - 7);

    int position = Math.round(relativePosition * (emptySpaceSize.width - knobSize.width - 6));

    Point knobCorner = new Point(emptySpaceCorner.x + position + 3, emptySpaceCorner.y + 3);

    paintButtonKnob(bufferedImageGraphics, color, knobSize, knobCorner);

    paintText(bufferedImageGraphics, color, innerSize);

    g.drawImage(bufferedImage, 0, 0, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.buttons.SmoothValueButton#paintText(java.awt.Graphics,
   *      java.awt.Color, java.awt.Dimension)
   */
  public void paintText(Graphics g, Color color, Dimension innerSize)
  {
    boolean highlighted = mouseIn && id != null && !selected && selectable;
    Color fontShadowColor = highlighted ? ButtonConstants.FONT_HIGHLIGHT_COLOR : brighter(getCurrentButtonColor(), 30);

    Dimension nameSize = new Dimension(valueStart - 15, innerSize.height);
    Point corner = new Point(10, innerSize.height / 2);

    paintString(bufferedImageGraphics, text, fontColor, fontShadowColor, nameSize, corner, highlighted, false, true);

    // get maximum size for value
    Dimension valueSize = new Dimension(sliderStart - valueStart - 5, innerSize.height);

    paintString(bufferedImageGraphics, currentValue + "", fontColor, fontShadowColor, valueSize, new Point(valueStart,
      innerSize.height / 2), highlighted, false, true);

    if (automaticTooltip)
    {
      if (isShortenedString(bufferedImageGraphics, value + "", valueSize))
      {
        setToolTipText(value + "");
      } else
      {
        setToolTipText("");
      }
    }
  }
}

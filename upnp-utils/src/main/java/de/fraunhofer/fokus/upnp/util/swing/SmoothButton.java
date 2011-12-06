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

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * This class represents a nice GUI button with rounded edges and shadowed text.
 * 
 * @author Alexander Koenig
 */
public class SmoothButton extends JComponent
{

  private static final long serialVersionUID = 1L;

  protected boolean         pressed;

  protected boolean         focused;

  protected boolean         mouseIn;

  // protected boolean keyAction;
  protected boolean         selected;

  protected boolean         selectable;

  protected boolean         automaticTooltip;

  protected boolean         centeredText;

  protected BufferedImage   bufferedImage;

  protected Graphics        bufferedImageGraphics;

  protected String          text;

  protected String          id;

  protected Color           fontColor;

  protected Color           fontHighlightColor;

  protected Color           buttonColor;

  protected Color           activeButtonColor;

  protected Color           disabledButtonColor;

  protected Dimension       size;

  protected int             fontSize;

  protected ActionListener  actionListener   = null;

  protected Point           mousePos;

  protected Point           shadowOffset     = new Point(3, 5);

  protected Font            textFont;

  protected Image           iconImage;

  /** Metadata associated with this button. */
  protected Object          metaData         = null;

  /**
   * Creates a new instance of SmoothButton.
   * 
   * @param size
   * @param text
   * @param id
   */
  public SmoothButton(Dimension size, String text, String id)
  {
    this(size, ButtonConstants.STANDARD_FONT_SIZE, text, id);
  }

  /**
   * Creates a new instance of SmoothButton.
   * 
   * @param size
   * @param fontSize
   * @param text
   * @param id
   */
  public SmoothButton(Dimension size, int fontSize, String text, String id)
  {
    pressed = false;
    focused = false;
    mouseIn = false;
    // keyAction = false;
    selected = false;
    selectable = true;
    automaticTooltip = true;
    centeredText = true;
    this.text = text;
    this.id = id;
    this.fontSize = fontSize;
    enableEvents(AWTEvent.FOCUS_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK/*
     * |
     * AWTEvent.KEY_EVENT_MASK
     */);
    // bufferedImage = new BufferedImage(getSize().width, getSize().height,
    // BufferedImage.TYPE_3BYTE_BGR);
    // bufferedImageGraphics = bufferedImage.getGraphics();
    textFont = new Font("Serif", 0, fontSize);
    fontColor = ButtonConstants.FONT_COLOR;
    fontHighlightColor = ButtonConstants.FONT_HIGHLIGHT_COLOR;
    setBackground(ButtonConstants.BACKGROUND_COLOR);
    buttonColor = ButtonConstants.BUTTON_COLOR;
    activeButtonColor = ButtonConstants.ACTIVE_BUTTON_COLOR;
    disabledButtonColor = ButtonConstants.DISABLED_BUTTON_COLOR;

    setOpaque(true);
    setPreferredSize(size);
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
   */
  public void setPreferredSize(Dimension size)
  {
    this.size = size;
    setMinimumSize(size);
    super.setPreferredSize(size);
  }

  public String getText()
  {
    return text;
  }

  public String getID()
  {
    return id;
  }

  public void setID(String id)
  {
    this.id = id;
  }

  public void setText(String text)
  {
    if (text == null)
    {
      text = "";
    }

    if (!this.text.equals(text))
    {
      this.text = text;
      repaint();
    }
  }

  public void setSelected(boolean state)
  {
    if (selected != state)
    {
      selected = state;
      repaint();
    }
  }

  public boolean isSelected()
  {
    return selected;
  }

  public void setCenteredText(boolean state)
  {
    if (centeredText != state)
    {
      centeredText = state;
      repaint();
    }
  }

  public boolean isCenteredText()
  {
    return centeredText;
  }

  /**
   * Updates the selectable state for a button
   * 
   * @param state
   */
  public void setSelectable(boolean state)
  {
    if (selectable != state)
    {
      selectable = state;
      repaint();
    }
  }

  /** Checks whether this button can be clicked */
  public boolean isSelectable()
  {
    return selectable;
  }

  /** Chooses a bold font for this button */
  public void setBoldFont()
  {
    textFont = new Font("Serif", Font.BOLD, fontSize);
  }

  /** Sets automatic tooltips. If set to true, the button name is automatically shown as tooltip. */
  public void setAutomaticTooltip(boolean state)
  {
    if (automaticTooltip != state)
    {
      automaticTooltip = state;
      repaint();
    }
  }

  /** Retrieves the button color depending on the current state. */
  public Color getCurrentButtonColor()
  {
    // check first for selection (may override selectable)
    if (selected)
    {
      return activeButtonColor;
    }

    if (!selectable)
    {
      return disabledButtonColor;
    }

    return buttonColor;
  }

  public void paintText(Graphics g, Color buttonColor, Dimension innerSize)
  {
    boolean highlighted = mouseIn && id != null && !selected && selectable;
    Color fontShadowColor = highlighted ? fontHighlightColor : brighter(getCurrentButtonColor(), 30);

    if (centeredText)
    {
      int imageWidth = getImageSize().width;
      paintString(g, text, fontColor, fontShadowColor, innerSize, new Point((innerSize.width + imageWidth) / 2,
        innerSize.height / 2), highlighted, true, true);
    } else
    {
      int textOffset = 10 + getImageSize().width;
      paintString(g,
        text,
        fontColor,
        fontShadowColor,
        innerSize,
        new Point(textOffset, innerSize.height / 2),
        highlighted,
        false,
        true);
    }

    // set tooltip if necessary
    if (automaticTooltip)
    {
      setToolTipText(isShortenedString(bufferedImageGraphics, text, innerSize) ? text : "");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paint(Graphics g)
  {
    super.paint(g);

    if (getSize().width == 0 || getSize().height == 0)
    {
      return;
    }

    // update buffered image
    if (bufferedImage == null || bufferedImage.getWidth() != getSize().width ||
      bufferedImage.getHeight() != getSize().height)
    {
      bufferedImage = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_3BYTE_BGR);
      bufferedImageGraphics = bufferedImage.getGraphics();
      bufferedImageGraphics.setFont(textFont);
    }

    // background
    bufferedImageGraphics.setColor(getBackground());
    bufferedImageGraphics.fillRect(0, 0, getSize().width, getSize().height);

    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);
    Color color = getCurrentButtonColor();

    paintRoundButton(bufferedImageGraphics, color, getBackground(), size, shadowOffset);
    paintText(bufferedImageGraphics, color, innerSize);

    g.drawImage(bufferedImage, 0, 0, null);
    if (iconImage != null)
    {
      Dimension imageSize = getImageSize();
      int width = imageSize.width;
      int height = imageSize.height;

      // center single images
      if (text == null || text.length() == 0)
      {
        g.drawImage(iconImage, (innerSize.width - width) / 2, (innerSize.height - height) / 2, width, height, null);
      } else
      {
        g.drawImage(iconImage, 5, (innerSize.height - height) / 2, width, height, null);
      }
    }
  }

  private Dimension getImageSize()
  {
    if (iconImage == null)
    {
      return new Dimension(0, 0);
    }

    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);

    int width = iconImage.getWidth(null);
    int height = iconImage.getHeight(null);

    if (height > innerSize.height)
    {
      width = Math.round(width * ((float)innerSize.height - 4) / height);
      height = innerSize.height - 4;
    }

    return new Dimension(width, height);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#update(java.awt.Graphics)
   */
  public void update(Graphics g)
  {
    // System.out.println("Called update");
  }

  /** Adds an action listener for this button. */
  public void addActionListener(ActionListener listener)
  {
    actionListener = AWTEventMulticaster.add(actionListener, listener);
  }

  /** Removes an action listener for this button. */
  public void removeActionListener(ActionListener listener)
  {
    actionListener = AWTEventMulticaster.remove(actionListener, listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Component#toString()
   */
  public String toString()
  {
    return text == null ? super.toString() : text;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Component#isFocusable()
   */
  public boolean isFocusable()
  {
    if (isEnabled())
    {
      return true;
    } else
    {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  public void setEnabled(boolean state)
  {
    super.setEnabled(state);
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Component#processMouseEvent(java.awt.event.MouseEvent)
   */
  public void processMouseEvent(MouseEvent e)
  {
    switch (e.getID())
    {
    case MouseEvent.MOUSE_ENTERED:
      mouseIn = true;
      mouseMoved();
      break;
    case MouseEvent.MOUSE_EXITED:
      mouseIn = false;
      mouseMoved();
      break;
    case MouseEvent.MOUSE_PRESSED:
      if (isEnabled())
      {
        pressed = true;
        mouseIn = true;
        requestFocus();

        mousePressed(e.getModifiers());
      }
      break;
    case MouseEvent.MOUSE_RELEASED:
      // forward event first to allow handling in descendent classes
      mouseReleased(e.getModifiers());
      if (pressed && mouseIn)
      {
        // create action event
        if (actionListener != null && selectable)
        {
          actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, id, e.getModifiers()));
        }
      }
      pressed = false;
    }
    repaint();
    super.processMouseEvent(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#processMouseMotionEvent(java.awt.event.MouseEvent)
   */
  public void processMouseMotionEvent(MouseEvent e)
  {
    mousePos = new Point(e.getX(), e.getY());
    mouseMoved();
    repaint();
    super.processMouseMotionEvent(e);
  }

  /** Template for mouse move handling */
  protected void mouseMoved()
  {
  }

  /** Template for mouse pressed handling */
  protected void mousePressed(int modifiers)
  {
    if ((modifiers & ActionEvent.CTRL_MASK) != 0)
    {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }
  }

  /** Template for mouse release handling */
  protected void mouseReleased(int modifiers)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Component#processFocusEvent(java.awt.event.FocusEvent)
   */
  public void processFocusEvent(FocusEvent e)
  {
    switch (e.getID())
    {
    case FocusEvent.FOCUS_GAINED:
      focused = true;
      break;
    case FocusEvent.FOCUS_LOST:
      focused = false;
      mouseIn = false;
    }
    repaint();
    super.processFocusEvent(e);
  }

  /** Sets the selected color for this button. */
  public void setActiveButtonColor(Color color)
  {
    activeButtonColor = color;
    repaint();
  }

  /** Sets the disabled color for this button. */
  public void setDisabledButtonColor(Color color)
  {
    disabledButtonColor = color;
    repaint();
  }

  /** Retrieves the normal color for this button. */
  public Color getButtonColor()
  {
    return buttonColor;
  }

  /** Sets the normal color for this button. */
  public void setButtonColor(Color color)
  {
    buttonColor = color;
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setBackground(java.awt.Color)
   */
  public void setBackground(Color color)
  {
    super.setBackground(color);
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setFont(java.awt.Font)
   */
  public void setFont(Font font)
  {
    super.setFont(font);
    textFont = font;
    repaint();
  }

  /** Sets the font color for the text */
  public void setFontColor(Color color)
  {
    fontColor = color;
    repaint();
  }

  /**
   * Retrieves the value of fontHighlightColor.
   * 
   * @return The value of fontHighlightColor
   */
  public Color getFontHighlightColor()
  {
    return fontHighlightColor;
  }

  /**
   * Sets the new value for fontHighlightColor.
   * 
   * @param fontHighlightColor
   *          The new value for fontHighlightColor
   */
  public void setFontHighlightColor(Color fontHighlightColor)
  {
    this.fontHighlightColor = fontHighlightColor;
  }

  /**
   * Retrieves the iconImage.
   * 
   * @return The iconImage
   */
  public Image getIconImage()
  {
    return iconImage;
  }

  /**
   * Sets the iconImage.
   * 
   * @param iconImage
   *          The new value for iconImage
   */
  public void setIconImage(Image iconImage)
  {
    this.iconImage = iconImage;
  }

  /**
   * Retrieves the metaData.
   * 
   * @return The metaData
   */
  public Object getMetaData()
  {
    return metaData;
  }

  /**
   * Sets the metaData.
   * 
   * @param metaData
   *          The new value for metaData
   */
  public void setMetaData(Object metaData)
  {
    this.metaData = metaData;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Static methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public static boolean isShortenedString(Graphics graphics, String name, Dimension maxSize)
  {
    if (name == null)
    {
      return false;
    }

    FontMetrics fontMetrics = graphics.getFontMetrics();
    // shorten name if necessary
    String drawName = name;
    if (fontMetrics.stringWidth(drawName) > maxSize.width - 10)
    {
      return true;
    }

    return false;
  }

  /** Retrieves the width for a string on a certain graphics object. */
  public static int getStringSize(Graphics graphics, String name)
  {
    FontMetrics fontMetrics = graphics.getFontMetrics();
    return fontMetrics.stringWidth(name);
  }

  /**
   * 
   * @param graphics
   * @param text
   * @param fontColor
   * @param fontShadowColor
   * @param innerSize
   * @param corner
   * @param highlighted
   * @param centerX
   * @param centerY
   */
  public static void paintString(Graphics graphics,
    String text,
    Color fontColor,
    Color fontShadowColor,
    Dimension innerSize,
    Point corner,
    boolean highlighted,
    boolean centerX,
    boolean centerY)
  {
    if (text == null)
    {
      return;
    }

    FontMetrics fontMetrics = graphics.getFontMetrics();
    // shorten if necessary
    String drawText = text;
    if (fontMetrics.stringWidth(drawText) > innerSize.width - 10)
    {
      while (drawText.length() > 0 && fontMetrics.stringWidth(drawText + "...") > innerSize.width - 10)
      {
        drawText = drawText.substring(0, drawText.length() - 1);
      }

      drawText += "...";
    }
    Point position = new Point(corner.x, corner.y);
    if (centerX)
    {
      position = new Point(position.x - fontMetrics.stringWidth(drawText) / 2, position.y);
    }
    if (centerY)
    {
      position = new Point(position.x, position.y - fontMetrics.getHeight() / 2 + fontMetrics.getAscent());
    }

    graphics.setColor(fontShadowColor);
    // draw glow frame if highlighted
    if (highlighted)
    {
      graphics.drawString(drawText, position.x + 1, position.y);
      graphics.drawString(drawText, position.x - 1, position.y);
      graphics.drawString(drawText, position.x, position.y + 1);
      graphics.drawString(drawText, position.x, position.y - 1);
    } else
    {
      graphics.drawString(drawText, position.x + 1, position.y + 1);
    }
    // draw name itself
    graphics.setColor(fontColor);
    graphics.drawString(drawText, position.x, position.y);
  }

  /**
   * Paints the background without shadow.
   * 
   * @param graphics
   * @param backgroundColor
   * @param size
   */
  public static void paintBackground(Graphics graphics, Color backgroundColor, Dimension size)
  {
    graphics.setColor(backgroundColor);
    graphics.fillRect(0, 0, size.width, size.height);
  }

  /**
   * Paints a rounded button.
   * 
   * @param graphics
   * @param buttonColor
   * @param backgroundColor
   * @param size
   * @param shadowOffset
   */
  public static void paintRoundButton(Graphics graphics,
    Color buttonColor,
    Color backgroundColor,
    Dimension size,
    Point shadowOffset)
  {
    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);

    // shadow
    graphics.setColor(darker(backgroundColor, 5));
    graphics.fillRect(shadowOffset.x, shadowOffset.y, innerSize.width - 1, innerSize.height - 1);

    graphics.setColor(darker(backgroundColor, 15));
    graphics.fillRoundRect(shadowOffset.x + 1, shadowOffset.y + 1, innerSize.width - 3, innerSize.height - 3, 3, 3);

    graphics.setColor(darker(backgroundColor, 40));
    graphics.fillRoundRect(shadowOffset.x + 2, shadowOffset.y + 2, innerSize.width - 5, innerSize.height - 5, 3, 3);

    // button
    graphics.setColor(buttonColor);
    graphics.fillRoundRect(0, 0, innerSize.width - 1, innerSize.height - 1, 5, 5);

    // fix java rounding errors round
    graphics.setColor(backgroundColor);
    graphics.drawLine(innerSize.width - 2, 1, innerSize.width - 2, 1);
    graphics.drawLine(1, innerSize.height - 2, 1, innerSize.height - 2);

    // top
    int scale = 10;

    graphics.setColor(brighter(buttonColor, 7 * scale));
    graphics.drawLine(3, 1, innerSize.width - 4 - 1, 1);
    graphics.setColor(brighter(buttonColor, 6 * scale));
    graphics.drawLine(2, 2, innerSize.width - 3 - 1, 2);
    graphics.setColor(brighter(buttonColor, 5 * scale));
    graphics.drawLine(1, 3, innerSize.width - 2 - 1, 3);
    for (int i = 0; i < 4; i++)
    {
      graphics.setColor(brighter(buttonColor, (4 - i) * scale));
      graphics.drawLine(1, i + 4, innerSize.width - 2 - 1, i + 4);
    }

    // sides
    graphics.setColor(brighter(buttonColor, 15));
    graphics.drawLine(1, 5, 1, innerSize.height - 5);
    graphics.drawLine(innerSize.width - 3, 5, innerSize.width - 3, innerSize.height - 5);
    // bottom
    graphics.setColor(darker(buttonColor, 50));
    graphics.drawLine(2, innerSize.height - 2, innerSize.width - 3 - 1, innerSize.height - 2);
    graphics.setColor(darker(buttonColor, 30));
    graphics.drawLine(1, innerSize.height - 3, innerSize.width - 2 - 1, innerSize.height - 3);
    graphics.setColor(darker(buttonColor, 10));
    graphics.drawLine(0, innerSize.height - 4, innerSize.width - 1 - 1, innerSize.height - 4);
  }

  /**
   * Paints the empty space for a checkbox
   * 
   * @param graphics
   * @param buttonColor
   * @param backgroundColor
   * @param size
   *          Width and height for the empty space
   * @param corner
   *          Upper left edge of the empty space
   */
  public static void paintButtonEmptySpace(Graphics graphics,
    Color buttonColor,
    Color backgroundColor,
    Dimension size,
    Point corner)
  {
    graphics.setColor(backgroundColor);

    // draw empty space
    graphics.fillRect(corner.x, corner.y, size.width, size.height);

    // top dark
    graphics.setColor(brighter(buttonColor, 20));
    graphics.drawLine(corner.x, corner.y, corner.x + size.width, corner.y);
    graphics.setColor(darker(buttonColor, 5));
    graphics.drawLine(corner.x + 1, corner.y + 1, corner.x + size.width - 1, corner.y + 1);

    // bottom bright
    graphics.setColor(brighter(buttonColor, 30));
    graphics.drawLine(corner.x + 1, corner.y + size.height - 2, corner.x + size.width - 1, corner.y + size.height - 2);
    graphics.setColor(brighter(buttonColor, 10));
    graphics.drawLine(corner.x, corner.y + size.height - 1, corner.x + size.width, corner.y + size.height - 1);

    // sides
    graphics.setColor(brighter(buttonColor, 10));
    graphics.drawLine(corner.x, corner.y + 1, corner.x, corner.y + size.height - 2);
    graphics.drawLine(corner.x + size.width, corner.y + 1, corner.x + size.width, corner.y + size.height - 2);

    graphics.setColor(brighter(buttonColor, 20));
    graphics.drawLine(corner.x + 1, corner.y + 2, corner.x + 1, corner.y + size.height - 3);
    graphics.drawLine(corner.x + size.width - 1, corner.y + 2, corner.x + size.width - 1, corner.y + size.height - 3);

    // upper corners
    graphics.setColor(brighter(buttonColor, 30));
    graphics.drawLine(corner.x, corner.y, corner.x, corner.y);
    graphics.drawLine(corner.x + size.width, corner.y, corner.x + size.width, corner.y);

  }

  /**
   * Paints a knob for a checkbox button.
   * 
   * @param graphics
   *          Graphics to draw onto
   * @param buttonColor
   *          Color for button
   * @param size
   *          Size of knob
   * @param corner
   *          Point where to start drawing
   * 
   */
  public static void paintButtonKnob(Graphics graphics, Color buttonColor, Dimension size, Point corner)
  {
    graphics.setColor(buttonColor);
    graphics.fillRect(corner.x, corner.y, size.width, size.height);

    // top
    for (int i = 0; i < 2; i++)
    {
      graphics.setColor(brighter(buttonColor, (2 - i) * 25));
      graphics.drawLine(corner.x, corner.y + i, corner.x + size.width, corner.y + i);
    }
    // bottom
    for (int i = 0; i < 1; i++)
    {
      graphics.setColor(darker(buttonColor, (1 - i) * 25));
      graphics.drawLine(corner.x, corner.y + size.height - i, corner.x + size.width, corner.y + size.height - i);
    }
    graphics.setColor(brighter(buttonColor, 15));
    graphics.drawLine(corner.x, corner.y + 1, corner.x, corner.y + size.height - 1);
    graphics.drawLine(corner.x + size.width, corner.y + 1, corner.x + size.width, corner.y + size.height - 1);
    // bottom corners
    graphics.setColor(buttonColor);
    graphics.drawLine(corner.x, corner.y + size.height, corner.x, corner.y + size.height);
    graphics.drawLine(corner.x + size.width, corner.y + size.height, corner.x + size.width, corner.y + size.height);
  }

  /**
   * Paints a cross for a checkbox button.
   * 
   * @param graphics
   *          Graphics to draw onto
   * @param buttonColor
   *          Color for button
   * @param size
   *          Size of knob
   * @param corner
   *          Point where to start drawing
   * 
   */
  public static void paintButtonCross(Graphics graphics, Color buttonColor, int height, Point corner)
  {
    for (int i = 0; i < height; i++)
    {
      // if (i < 2)
      // graphics.setColor(brighter(buttonColor, (2 - i) * 25));
      //      
      // if (i >= height -2)
      // graphics.setColor(darker(buttonColor, (height - i) * 25));
      graphics.setColor(darker(buttonColor, 25));
      graphics.drawLine(corner.x + i, corner.y + i, corner.x + i + 3, corner.y + i);
      graphics.drawLine(corner.x + height - 1 - i, corner.y + i, corner.x + height - i + 2, corner.y + i);

      // force darker frame
      if (i > 0 && i < height - 1)
      {
        graphics.setColor(buttonColor);
      }
      if (i == 1)
      {
        graphics.setColor(brighter(buttonColor, 25));
      }
      if (i == height - 2)
      {
        graphics.setColor(darker(buttonColor, 25));
      }
      graphics.drawLine(corner.x + i + 1, corner.y + i, corner.x + i + 2, corner.y + i);
      graphics.drawLine(corner.x + height - i, corner.y + i, corner.x + height - i + 1, corner.y + i);
    }
  }

  /** Paints a smooth frame. */
  public static void paintFrame(Graphics graphics,
    Color frameColor,
    Color backgroundColor,
    Dimension size,
    Point shadowOffset,
    int frameSize)
  {
    Dimension innerSize = new Dimension(size.width - shadowOffset.x, size.height - shadowOffset.y);

    // background
    graphics.setColor(backgroundColor);
    graphics.fillRect(0, 0, size.width, size.height);

    // shadow
    if (shadowOffset.x > 0 || shadowOffset.y > 0)
    {
      graphics.setColor(darker(backgroundColor, 5));
      graphics.fillRect(shadowOffset.x, shadowOffset.y, innerSize.width, innerSize.height);

      graphics.setColor(darker(backgroundColor, 15));
      graphics.fillRoundRect(shadowOffset.x + 1, shadowOffset.y + 1, innerSize.width - 2, innerSize.height - 2, 3, 3);

      graphics.setColor(darker(backgroundColor, 40));
      graphics.fillRoundRect(shadowOffset.x + 2, shadowOffset.y + 2, innerSize.width - 4, innerSize.height - 4, 3, 3);

      graphics.setColor(backgroundColor);
      graphics.fillRect(size.width - 3, size.height - 3, 3, 3);
    }

    // button
    graphics.setColor(frameColor);
    graphics.fillRoundRect(0, 0, innerSize.width, innerSize.height, 5, 5);

    // fix java rounding errors
    graphics.setColor(backgroundColor);
    graphics.drawLine(innerSize.width - 1, 1, innerSize.width - 1, 1);
    graphics.drawLine(1, innerSize.height - 1, 1, innerSize.height - 1);
    // rounding errors lying in the shadow
    graphics.setColor(darker(backgroundColor, 40));
    graphics.drawLine(innerSize.width - 2, innerSize.height - 1, innerSize.width - 1, innerSize.height - 1);
    graphics.drawLine(innerSize.width - 1, innerSize.height - 2, innerSize.width - 1, innerSize.height - 2);

    // top
    int scale = 10;

    graphics.setColor(brighter(frameColor, 6 * scale));
    graphics.drawLine(3, 1, innerSize.width - 4, 1);
    graphics.setColor(brighter(frameColor, 4 * scale));
    graphics.drawLine(2, 2, innerSize.width - 3, 2);
    graphics.setColor(brighter(frameColor, 2 * scale));
    graphics.drawLine(1, 3, innerSize.width - 2, 3);
    // bottom of upper frame
    graphics.setColor(darker(frameColor, 10));
    graphics.drawLine(8, 4, innerSize.width - 9, 4);
    graphics.setColor(darker(frameColor, 30));
    graphics.drawLine(7, 5, innerSize.width - 8, 5);

    // sides
    graphics.setColor(brighter(frameColor, 15));
    graphics.drawLine(1, 4, 1, innerSize.height - 4);
    graphics.drawLine(innerSize.width - 3, 4, innerSize.width - 2, innerSize.height - 4);

    graphics.setColor(darker(frameColor, 15));
    graphics.drawLine(5, 7, 5, innerSize.height - 7);
    graphics.drawLine(innerSize.width - 6, 7, innerSize.width - 6, innerSize.height - 7);

    // bottom
    graphics.setColor(darker(frameColor, 50));
    graphics.drawLine(2, innerSize.height - 1, innerSize.width - 3, innerSize.height - 1);
    graphics.setColor(darker(frameColor, 30));
    graphics.drawLine(1, innerSize.height - 2, innerSize.width - 2, innerSize.height - 2);
    graphics.setColor(darker(frameColor, 10));
    graphics.drawLine(0, innerSize.height - 3, innerSize.width - 1, innerSize.height - 3);
    // top of bottom frame
    graphics.setColor(brighter(frameColor, 40));
    graphics.drawLine(8, innerSize.height - 5, innerSize.width - 9, innerSize.height - 5);
    graphics.setColor(brighter(frameColor, 20));
    graphics.drawLine(8, innerSize.height - 4, innerSize.width - 9, innerSize.height - 4);

    graphics.drawLine(5, innerSize.height - 6, 6, innerSize.height - 6);
    graphics.drawLine(6, innerSize.height - 5, 7, innerSize.height - 5);
    graphics.drawLine(innerSize.width - 7, innerSize.height - 6, innerSize.width - 6, innerSize.height - 6);
    graphics.drawLine(innerSize.width - 8, innerSize.height - 5, innerSize.width - 7, innerSize.height - 5);

    // empty inside
    graphics.setColor(backgroundColor);
    graphics.fillRect(frameSize, frameSize, innerSize.width - 2 * frameSize, innerSize.height - 2 * frameSize);

    // round edges
    graphics.setColor(darker(frameColor, 30));
    graphics.drawLine(6, 6, 6, 6);
    graphics.drawLine(innerSize.width - 7, 6, innerSize.width - 7, 6);

  }

  /** Calculates a brighter color for a certain base color. */
  public static Color brighter(Color color, int steps)
  {
    return new Color(Math.min(255, color.getRed() + steps), Math.min(255, color.getGreen() + steps), Math.min(255,
      color.getBlue() + steps));
  }

  /** Calculates a darker color for a certain base color. */
  public static Color darker(Color color, int steps)
  {
    return new Color(Math.max(0, color.getRed() - steps), Math.max(0, color.getGreen() - steps), Math.max(0,
      color.getBlue() - steps));
  }

}

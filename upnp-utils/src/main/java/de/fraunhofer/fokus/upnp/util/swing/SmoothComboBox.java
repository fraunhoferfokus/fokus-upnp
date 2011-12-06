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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * This class provides a smooth combobox.
 * 
 * @author Alexander Koenig
 * 
 */
public class SmoothComboBox extends BasicComboBoxUI
{

  public static ComponentUI createUI(JComponent c)
  {
    return new SmoothComboBox();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicComboBoxUI#createRenderer()
   */
  protected ListCellRenderer createRenderer()
  {
    // created once for each new combobox
    return new SmoothListCellRenderer();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicComboBoxUI#paint(java.awt.Graphics, javax.swing.JComponent)
   */
  public void paint(Graphics g, JComponent c)
  {
    // draws the currently selected value
    try
    {
      JComboBox comboBox = (JComboBox)c;
      Rectangle bounds = rectangleForCurrentValue();

      SmoothButton.paintBackground(g, ButtonConstants.BACKGROUND_COLOR, new Dimension(bounds.width, bounds.height));

      SmoothButton.paintRoundButton(g,
        ButtonConstants.ACTIVE_BUTTON_COLOR,
        ButtonConstants.BACKGROUND_COLOR,
        new Dimension(bounds.width, bounds.height),
        new Point(0, 0));

      Font textFont = new Font("Serif", 0, 12);
      g.setFont(textFont);

      SmoothButton.paintString(g,
        comboBox.getSelectedItem().toString(),
        Color.BLACK,
        SmoothButton.brighter(ButtonConstants.ACTIVE_BUTTON_COLOR, 25),
        new Dimension(bounds.width, bounds.height),
        new Point(5, bounds.height / 2),
        hasFocus,
        false,
        true);

    } catch (Exception e)
    {
      System.out.println("Error parsing component to JComboBox");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicComboBoxUI#createArrowButton()
   */
  protected JButton createArrowButton()
  {
    JButton button = new SmoothArrowButton(SwingConstants.SOUTH, ButtonConstants.BACKGROUND_COLOR);
    return button;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicComboBoxUI#createPopup()
   */
  protected ComboPopup createPopup()
  {
    return new SmoothComboPopupRenderer(comboBox);
  }

  private class SmoothComboPopupRenderer extends BasicComboPopup
  {

    /**  */
    private static final long serialVersionUID = 8396025289845071117L;

    /**
     * Creates a new instance of SmoothComboBox.java
     * 
     * @param combo
     */
    public SmoothComboPopupRenderer(JComboBox combo)
    {
      super(combo);
      setBackground(ButtonConstants.BACKGROUND_COLOR);
      setBorderPainted(false);
    }

  }

  private class SmoothListCellRenderer extends JLabel implements ListCellRenderer
  {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
      SmoothButton.paintBackground(g, getBackground(), getSize());

      SmoothButton.paintRoundButton(g, getForeground(), getBackground(), getSize(), new Point(0, 0));

      Font textFont = new Font("Serif", 0, 12);
      g.setFont(textFont);

      SmoothButton.paintString(g,
        getText(),
        Color.BLACK,
        SmoothButton.brighter(getForeground(), 25),
        getSize(),
        new Point(5, getSize().height / 2),
        false,
        false,
        true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus)
    {
      setBackground(ButtonConstants.BACKGROUND_COLOR);
      setForeground(isSelected ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.BUTTON_COLOR);
      setText(value.toString());
      return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
      return new Dimension(Math.min(200, super.getPreferredSize().width), ButtonConstants.BUTTON_HEIGHT - 5);
    }
  }

}

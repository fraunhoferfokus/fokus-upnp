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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This panel can be used to display similar swing components with the same height. If a certain
 * number of components is reached, the size of the panel is automatically limited.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class ScrollableComponentPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  protected SmoothButton    titleButton;

  protected JPanel          componentPanel;

  protected JScrollPane     scrollPane;

  protected int             preferredComponentCount;

  protected int             preferredWidth;

  protected int             componentHeight;

  /**
   * Creates a new instance of ScrollableComponentPanel.
   * 
   * @param title
   * @param borderColor
   * @param panelWidth
   * @param componentHeight
   * @param preferredComponentCount
   */
  public ScrollableComponentPanel(String title,
    Color borderColor,
    int panelWidth,
    int componentHeight,
    int preferredComponentCount)
  {
    this.preferredComponentCount = preferredComponentCount;
    this.preferredWidth = panelWidth;
    this.componentHeight = componentHeight;

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    setBorder(new SmoothBorder(borderColor));
    setLayout(new GridBagLayout());

    if (title != null)
    {
      titleButton = new SmoothButton(new Dimension(panelWidth - 30, ButtonConstants.BUTTON_HEIGHT), title, null);
      titleButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
      titleButton.setButtonColor(borderColor);
    }

    componentPanel = new JPanel();
    componentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    componentPanel.setLayout(new GridBagLayout());

    scrollPane = new JScrollPane(componentPanel);
    scrollPane.setBorder(null);
    scrollPane.setMinimumSize(new Dimension(panelWidth, componentHeight * 2));
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // build basic layout
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
    if (titleButton != null)
    {
      add(titleButton, gridBagConstraints);
      gridBagConstraints.gridy = 1;
    }
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.weightx = 1.0;
    add(scrollPane, gridBagConstraints);
  }

  /**
   * Creates a new instance of ScrollableComponentPanel.
   * 
   * @param title
   * @param borderColor
   * @param panelWidth
   * @param componentHeight
   * @param preferredComponentCount
   */
  public ScrollableComponentPanel(SmoothButton titleButton,
    Color borderColor,
    int panelWidth,
    int componentHeight,
    int preferredComponentCount)
  {
    this.preferredComponentCount = preferredComponentCount;
    this.preferredWidth = panelWidth;
    this.componentHeight = componentHeight;

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    setBorder(new SmoothBorder(borderColor));
    setLayout(new GridBagLayout());

    this.titleButton = titleButton;

    componentPanel = new JPanel();
    componentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    componentPanel.setLayout(new GridBagLayout());

    scrollPane = new JScrollPane(componentPanel);
    scrollPane.setBorder(null);
    scrollPane.setMinimumSize(new Dimension(panelWidth, componentHeight * 2));
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // build basic layout
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
    if (titleButton != null)
    {
      add(titleButton, gridBagConstraints);
      gridBagConstraints.gridy = 1;
    }
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.weightx = 1.0;
    add(scrollPane, gridBagConstraints);
  }

  /**
   * Retrieves the panel holding the components.
   * 
   * @return The devicePanel
   */
  public JPanel getComponentPanel()
  {
    return componentPanel;
  }

  /** Removes all buttons from the panel. */
  public void clear()
  {
    componentPanel.removeAll();
    componentPanel.invalidate();
  }

  /** Repaints and resizes the panel after adding new buttons. */
  public void update()
  {
    setSizeToFitContent();
    repaint();
  }

  /** Adds a new component. */
  public void addComponent(JComponent component, GridBagConstraints gridBagConstraints)
  {
    componentPanel.add(component, gridBagConstraints);
  }

  /** Updates the size of the panel to fit the number of available buttons. */
  private void setSizeToFitContent()
  {
    scrollPane.setMinimumSize(new Dimension(preferredWidth, Math.min(componentPanel.getComponentCount() *
      componentHeight, componentHeight * 2)));
    scrollPane.setPreferredSize(new Dimension(preferredWidth, Math.min(componentPanel.getComponentCount() *
      componentHeight, componentHeight * preferredComponentCount)));

    componentPanel.setMinimumSize(new Dimension(preferredWidth, componentPanel.getComponentCount() * componentHeight));
    componentPanel.setPreferredSize(new Dimension(preferredWidth, componentPanel.getComponentCount() * componentHeight));

    revalidate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setBackground(java.awt.Color)
   */
  public void setBackground(Color color)
  {
    super.setBackground(color);
    if (componentPanel != null)
    {
      componentPanel.setBackground(color);
    }
    if (scrollPane != null)
    {
      scrollPane.setBackground(color);
    }
    if (titleButton != null)
    {
      titleButton.setBackground(color);
    }
  }

}

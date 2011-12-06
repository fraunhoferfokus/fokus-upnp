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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class ThemeDeviceOverviewPanel extends JPanel
{
  public static final int   PREFERRED_WIDTH  = GUIConstants.DESCRIPTION_BUTTON_WIDTH + 6;

  public static final int   PREFERRED_HEIGHT = GUIConstants.DESCRIPTION_BUTTON_HEIGHT + 2;

  private static final long serialVersionUID = 1L;

  /** Optional title button for panel */
  private SmoothButton      themeButton;

  /** Scrollable panel with devices */
  private JPanel            devicePanel;

  /** Scroll pane for devices */
  private JScrollPane       deviceScrollPane;

  /** Number of visible devices */
  private int               preferredDeviceCount;

  /**
   * Creates a new instance of ThemeDeviceOverviewPanel.java
   * 
   */
  public ThemeDeviceOverviewPanel(String theme, Color borderColor, int preferredDeviceCount)
  {
    this.preferredDeviceCount = preferredDeviceCount;
    setBackground(ButtonConstants.BACKGROUND_COLOR);
    setBorder(new SmoothBorder(borderColor));
    setLayout(new GridBagLayout());

    themeButton = new SmoothButton(new Dimension(250, ButtonConstants.BUTTON_HEIGHT), theme, null);
    themeButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    themeButton.setButtonColor(borderColor);

    devicePanel = new JPanel();
    devicePanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    devicePanel.setLayout(new GridBagLayout());

    deviceScrollPane = new JScrollPane(devicePanel);
    deviceScrollPane.setBorder(null);
    deviceScrollPane.setMinimumSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT * 2));
    deviceScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    deviceScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // build basic layout
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
    add(themeButton, gridBagConstraints);

    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.weightx = 1.0;
    add(deviceScrollPane, gridBagConstraints);
  }

  /**
   * Retrieves the devicePanel.
   * 
   * @return The devicePanel
   */
  public JPanel getDevicePanel()
  {
    return devicePanel;
  }

  /** Removes all device buttons from the device panel. */
  public void clear()
  {
    devicePanel.removeAll();
    devicePanel.invalidate();
    invalidate();
  }

  /** Repaints and resizes the panel after adding new devices. */
  public void update()
  {
    setSizeToFitContent();
    repaint();
  }

  /** Adds a new device button. */
  public void addDeviceButton(SmoothButton deviceButton, GridBagConstraints gridBagConstraints)
  {
    devicePanel.add(deviceButton, gridBagConstraints);
  }

  private void setSizeToFitContent()
  {
    invalidate();

    deviceScrollPane.setMinimumSize(new Dimension(PREFERRED_WIDTH, Math.min(devicePanel.getComponentCount() *
      PREFERRED_HEIGHT, PREFERRED_HEIGHT * 2)));
    deviceScrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, Math.min(devicePanel.getComponentCount() *
      PREFERRED_HEIGHT, PREFERRED_HEIGHT * preferredDeviceCount)));

    devicePanel.setMinimumSize(new Dimension(PREFERRED_WIDTH, devicePanel.getComponentCount() * PREFERRED_HEIGHT));
    devicePanel.setPreferredSize(new Dimension(PREFERRED_WIDTH, devicePanel.getComponentCount() * PREFERRED_HEIGHT));

    validateTree();
  }

  /**
   * Retrieves the number of device buttons.
   * 
   * @return The number of device buttons
   */
  public int getDeviceButtonCount()
  {
    return devicePanel.getComponentCount();
  }

  /**
   * Retrieves the preferredDeviceCount.
   * 
   * @return The preferredDeviceCount
   */
  public int getPreferredDeviceCount()
  {
    return preferredDeviceCount;
  }

  /**
   * Sets the preferredDeviceCount.
   * 
   * @param preferredDeviceCount
   *          The new value for preferredDeviceCount
   */
  public void setPreferredDeviceCount(int preferredDeviceCount)
  {
    if (this.preferredDeviceCount != preferredDeviceCount)
    {
      this.preferredDeviceCount = preferredDeviceCount;
      update();
    }
  }

  /** Retrieves the size of the header (panel + theme button) */
  public int getHeaderHeight()
  {
    return getHeight() - deviceScrollPane.getHeight();
  }

}

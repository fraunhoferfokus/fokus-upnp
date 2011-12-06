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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * This class provides static methods to instantiate swing panels.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class PanelHelper
{

  public static final Color TRANSPARENT_PANEL_COLOR = new Color(0, 0, 0, 0);

  public static final Color OPAQUE_COLOR            = new Color(230, 230, 230, 255);

  public static final Color LIGHT_PANEL_COLOR       = new Color(210, 210, 220, 50);

  public static final Color PANEL_COLOR             = new Color(240, 240, 240, 200);

  public static final Color BACKGROUND_COLOR        = new Color(200, 200, 210, 200);

  /** Creates a gridbag layout panel with a line border */
  public static JPanel initLineBorderPanel(Color color, Color lineColor)
  {
    JPanel result = new JPanel();
    result.setBackground(color);
    result.setBorder(new LineBorder(lineColor, 1));
    result.setLayout(new GridBagLayout());

    return result;
  }

  /** Creates a gridbag layout panel */
  public static JPanel initPanel(Color color)
  {
    JPanel result = new JPanel();
    result.setBackground(color);
    result.setLayout(new GridBagLayout());

    return result;
  }

  /** Initializes gridbag constraints that use as much space as available. */
  public static GridBagConstraints initScaleGridBagConstraints(int inset, int insetBottom)
  {
    GridBagConstraints result = new java.awt.GridBagConstraints();
    result.anchor = java.awt.GridBagConstraints.NORTHWEST;
    result.insets = new Insets(inset, inset, insetBottom, inset);
    result.weightx = 1.0;
    result.weighty = 1.0;
    result.fill = GridBagConstraints.BOTH;
    result.gridx = 0;
    result.gridy = 0;

    return result;
  }

  /** Initializes gridbag constraints that use as much space as needed. */
  public static GridBagConstraints initGridBagConstraints(int inset, int insetBottom)
  {
    GridBagConstraints result = new java.awt.GridBagConstraints();
    result.anchor = java.awt.GridBagConstraints.NORTHWEST;
    result.insets = new Insets(inset, inset, insetBottom, inset);
    result.weightx = 0.0;
    result.weighty = 0.0;
    result.fill = GridBagConstraints.NONE;
    result.gridx = 0;
    result.gridy = 0;

    return result;
  }

}

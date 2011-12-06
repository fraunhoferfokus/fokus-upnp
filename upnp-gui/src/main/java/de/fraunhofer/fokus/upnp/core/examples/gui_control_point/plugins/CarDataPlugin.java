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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for the car data service.
 * 
 * @author Alexander Koenig
 */
public class CarDataPlugin extends BaseCPServicePlugin
{
  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = DeviceConstant.CAR_DATA_SERVICE_TYPE;

  private SmoothValueButton fuelLevelButton;

  private SmoothValueButton fuelConsumptionButton;

  private SmoothValueButton rpmButton;

  private SmoothValueButton speedButton;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();

    fuelLevelButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Tankfüllstand:", "", null);
    fuelLevelButton.setSelectable(false);
    fuelLevelButton.setDisabledButtonColor(fuelLevelButton.getButtonColor());

    fuelConsumptionButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Verbrauch:", "", null);
    fuelConsumptionButton.setSelectable(false);
    fuelConsumptionButton.setDisabledButtonColor(fuelConsumptionButton.getButtonColor());

    rpmButton = new SmoothValueButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Drehzahl:", "", null);
    rpmButton.setSelectable(false);
    rpmButton.setDisabledButtonColor(rpmButton.getButtonColor());

    speedButton =
      new SmoothValueButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Geschwindigkeit:", "", null);
    speedButton.setSelectable(false);
    speedButton.setDisabledButtonColor(speedButton.getButtonColor());

    setLayout(new GridBagLayout());
    setBackground(ButtonConstants.BACKGROUND_COLOR);

    // initialize panel layout
    updateLayout();
  }

  /** Retrieves the type of the plugin */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  public void stateVariableChanged(CPStateVariable csv)
  {
    if (service.getCPStateVariable("Speed") == csv)
    {
      // set value
      try
      {
        speedButton.setValue(StringHelper.doubleToString(csv.getDoubleValue(), "0.0") + " km/h");
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("RPM") == csv)
    {
      // set value
      try
      {
        rpmButton.setValue(csv.getNumericValue() + " min-1");
      } catch (Exception ex)
      {
      }
    }
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    removeAll();

    JPanel contentPanel = new JPanel();
    contentPanel.setBackground(getBackground());
    contentPanel.setLayout(new GridBagLayout());

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    contentPanel.add(fuelLevelButton, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    contentPanel.add(fuelConsumptionButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    contentPanel.add(rpmButton, gridBagConstraints);

    gridBagConstraints.gridy = 2;
    contentPanel.add(speedButton, gridBagConstraints);

    buildCenteredLayout(contentPanel);

    repaint();
    validateTree();
  }

}

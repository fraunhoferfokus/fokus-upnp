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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationConstant;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for Weather services.
 * 
 * @author Alexander Koenig, Sebastian Nauck
 */

public class GPSServicePlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = LocalizationConstant.GPS_SERVICE_TYPE;

  private SmoothButton      validButton;

  private SmoothValueButton latitudeButton;

  private SmoothValueButton longitudeButton;

  private SmoothValueButton velocityButton;

  private SmoothValueButton directionButton;

  private JPanel            contentPanel;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    initComponents();

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    contentPanel = new JPanel();
    contentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    validButton = new SmoothButton(new Dimension(370, ButtonConstants.BUTTON_HEIGHT), 12, "Keine Daten", null);
    validButton.setSelectable(false);
    validButton.setDisabledButtonColor(validButton.getButtonColor());

    latitudeButton = new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Breite:", "", null);
    latitudeButton.setSelectable(false);
    latitudeButton.setDisabledButtonColor(latitudeButton.getButtonColor());

    longitudeButton = new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Länge:", "", null);
    longitudeButton.setSelectable(false);
    longitudeButton.setDisabledButtonColor(longitudeButton.getButtonColor());

    velocityButton =
      new SmoothValueButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Geschwindigkeit:", "", null);
    velocityButton.setSelectable(false);
    velocityButton.setDisabledButtonColor(velocityButton.getButtonColor());

    directionButton =
      new SmoothValueButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Richtung:", "", null);
    directionButton.setSelectable(false);
    directionButton.setDisabledButtonColor(directionButton.getButtonColor());

    latitudeButton.setFontColor(Color.gray);
    longitudeButton.setFontColor(Color.gray);
    velocityButton.setFontColor(Color.gray);
    directionButton.setFontColor(Color.gray);

    // initialize panel layout
    updateLayout();
  }

  /** This method starts the plugin as soon as all needed information is available */
  public void startPlugin()
  {
    super.startPlugin();

    try
    {
      latitudeButton.setValue(Double.toString(service.getCPStateVariable("Latitude").getDoubleValue()) + " °");
      longitudeButton.setValue(Double.toString(service.getCPStateVariable("Longitude").getDoubleValue()) + " °");
      velocityButton.setValue(Double.toString(service.getCPStateVariable("Speed").getDoubleValue()));
      directionButton.setValue(Double.toString(service.getCPStateVariable("Direction").getDoubleValue()));
    } catch (Exception ex)
    {
      System.out.println("An error occured:" + ex.getMessage());
    }
  }

  /** Retrieves the type of the plugin */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    setLayout(new java.awt.GridBagLayout());

    setBackground(new java.awt.Color(204, 204, 255));

  }// GEN-END:initComponents

  /** Updates a state variable if an appropriate event was received. */
  public void trySetDoubleValue(SmoothValueButton button, String name, CPStateVariable stateVariable)
  {
    if (service.getCPStateVariable(name) == stateVariable)
    {
      // set value
      try
      {
        button.setValue(Double.toString(stateVariable.getDoubleValue()));
      } catch (Exception ex)
      {
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPServicePlugin#stateVariableChanged(de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable csv)
  {
    if (service.getCPStateVariable("Active") == csv)
    {
      try
      {
        if (csv.getBooleanValue())
        {
          validButton.setText("Gültige Daten");
          latitudeButton.setFontColor(Color.black);
          longitudeButton.setFontColor(Color.black);
          velocityButton.setFontColor(Color.black);
          directionButton.setFontColor(Color.black);
        } else
        {
          validButton.setText("Keine Daten");
          latitudeButton.setFontColor(Color.gray);
          longitudeButton.setFontColor(Color.gray);
          velocityButton.setFontColor(Color.gray);
          directionButton.setFontColor(Color.gray);
        }
      } catch (Exception e)
      {
      }
    }
    trySetDoubleValue(latitudeButton, "Latitude", csv);
    trySetDoubleValue(longitudeButton, "Longitude", csv);
    trySetDoubleValue(velocityButton, "Speed", csv);
    trySetDoubleValue(directionButton, "Direction", csv);
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    removeAll();

    GridBagConstraints gridBagConstraints;
    contentPanel.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    contentPanel.add(validButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 1;
    contentPanel.add(latitudeButton, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    contentPanel.add(longitudeButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    contentPanel.add(velocityButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    contentPanel.add(directionButton, gridBagConstraints);

    buildCenteredLayout(contentPanel);

    repaint();
    validateTree();
  }
}

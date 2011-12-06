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
import java.awt.Insets;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for Weather services.
 * 
 * @author Alexander Koenig, Sebastian Nauck
 */

public class WeatherPlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = SensorConstants.WEATHER_SERVICE_TYPE;

  private SmoothValueButton airPressureButton;

  private SmoothValueButton airHumidityButton;

  private SmoothValueButton sunriseButton;

  private SmoothValueButton sunsetButton;

  private SmoothButton      temperatureButton;

  private SmoothValueButton conditionButton;

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

    temperatureButton = new SmoothButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Lese...", null);
    temperatureButton.setSelectable(false);
    temperatureButton.setDisabledButtonColor(temperatureButton.getButtonColor());

    airPressureButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Luftdruck:", "", null);
    airPressureButton.setSelectable(false);
    airPressureButton.setDisabledButtonColor(airPressureButton.getButtonColor());

    airHumidityButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Luftfeuchtigkeit:", "", null);
    airHumidityButton.setSelectable(false);
    airHumidityButton.setDisabledButtonColor(airHumidityButton.getButtonColor());

    sunriseButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Sonnenaufgang:", "", null);
    sunriseButton.setSelectable(false);
    sunriseButton.setDisabledButtonColor(sunriseButton.getButtonColor());

    sunsetButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Sonnenuntergang:", "", null);
    sunsetButton.setSelectable(false);
    sunsetButton.setDisabledButtonColor(sunsetButton.getButtonColor());

    conditionButton =
      new SmoothValueButton(new Dimension(370, ButtonConstants.BUTTON_HEIGHT), 12, "Wetterlage:", "", null);
    conditionButton.setSelectable(false);
    conditionButton.setDisabledButtonColor(conditionButton.getButtonColor());

    // initialize panel layout
    updateLayout();
  }

  /** This method starts the plugin as soon as all needed information is available */
  public void startPlugin()
  {
    super.startPlugin();

    try
    {
      airPressureButton.setValue(service.getCPStateVariable("AirPressure").getStringValue() + " hPa");
      airHumidityButton.setValue(service.getCPStateVariable("AirHumidity").getStringValue() + " %");
      sunriseButton.setValue(service.getCPStateVariable("Sunrise").getStringValue());
      sunsetButton.setValue(service.getCPStateVariable("Sunset").getStringValue());
      temperatureButton.setText(service.getCPStateVariable("Temperature").getNumericValue() + " °C");
      conditionButton.setValue(service.getCPStateVariable("CurrentCondition").getStringValue());
      // try to translate condition
      deviceGUIContext.getDeviceTranslations().setTranslationForButtonValue(conditionButton,
        conditionButton.getValue(),
        "",
        "");
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

  public void stateVariableChanged(CPStateVariable csv)
  {
    if (service.getCPStateVariable("AirPressure") == csv)
    {
      // set value
      try
      {
        airPressureButton.setValue(csv.getStringValue() + " hPa");
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("AirHumidity") == csv)
    {
      // set value
      try
      {
        airHumidityButton.setValue(csv.getStringValue() + " %");
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("Sunrise") == csv)
    {
      // set value
      try
      {
        sunriseButton.setValue(csv.getStringValue());
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("Sunset") == csv)
    {
      // set value
      try
      {
        sunsetButton.setValue(csv.getStringValue());
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("Temperature") == csv)
    {
      // set value
      try
      {
        temperatureButton.setText(csv.getValue() + " °C");
      } catch (Exception ex)
      {
      }
    }
    if (service.getCPStateVariable("CurrentCondition") == csv)
    {
      // set value
      try
      {
        conditionButton.setValue(csv.getStringValue());
        // try to translate condition
        deviceGUIContext.getDeviceTranslations().setTranslationForButtonValue(conditionButton,
          conditionButton.getValue(),
          "",
          "");
      } catch (Exception ex)
      {
      }
    }
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    removeAll();

    GridBagConstraints gridBagConstraints;
    contentPanel.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    contentPanel.add(temperatureButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 1;
    contentPanel.add(airPressureButton, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    contentPanel.add(airHumidityButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    contentPanel.add(sunriseButton, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    contentPanel.add(sunsetButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    contentPanel.add(conditionButton, gridBagConstraints);

    buildCenteredLayout(contentPanel);

    repaint();
    validateTree();
  }
}

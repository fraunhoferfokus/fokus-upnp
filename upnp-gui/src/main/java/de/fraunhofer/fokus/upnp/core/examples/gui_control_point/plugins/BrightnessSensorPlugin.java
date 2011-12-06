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

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for TemperatureSensor services.
 * 
 * @author Alexander Koenig
 */
public class BrightnessSensorPlugin extends BaseCPServicePlugin
{
  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = SensorConstants.BRIGHTNESS_SENSOR_SERVICE_TYPE;

  private SmoothValueButton applicationButton;

  private SmoothValueButton nameButton;

  private SmoothButton      brightnessButton;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    initComponents();

    applicationButton =
      new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Applikation:", "", null);
    applicationButton.setSelectable(false);
    applicationButton.setDisabledButtonColor(applicationButton.getButtonColor());

    nameButton = new SmoothValueButton(new Dimension(180, ButtonConstants.BUTTON_HEIGHT), 12, "Name:", "", null);
    nameButton.setSelectable(false);
    nameButton.setDisabledButtonColor(nameButton.getButtonColor());

    brightnessButton = new SmoothButton(new Dimension(370, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Lese...", null);
    brightnessButton.setSelectable(false);
    brightnessButton.setDisabledButtonColor(brightnessButton.getButtonColor());

    setBackground(ButtonConstants.BACKGROUND_COLOR);

    // initialize panel layout
    updateLayout();
  }

  /** This method starts the plugin as soon as all needed information is available */
  public void startPlugin()
  {
    super.startPlugin();

    CPAction action = service.getCPAction("GetApplication");
    if (action != null)
    {
      try
      {
        controlPoint.invokeAction(action);

        applicationButton.setValue(action.getArgument("CurrentApplication").getStringValue());
        deviceGUIContextProvider.getDeviceContext(service)
          .getDeviceTranslations()
          .setTranslationForButtonValue(applicationButton, applicationButton.getValue(), "", "");

      } catch (ActionFailedException afe)
      {
        System.out.println("An error occured:" + afe.getMessage());
      } catch (Exception ex)
      {
        System.out.println("An error occured:" + ex.getMessage());
      }
    }
    action = service.getCPAction("GetName");
    if (action != null)
    {
      try
      {
        controlPoint.invokeAction(action);

        nameButton.setValue(action.getArgument("CurrentName").getStringValue());
        deviceGUIContextProvider.getDeviceContext(service)
          .getDeviceTranslations()
          .setTranslationForButtonValue(nameButton, nameButton.getValue(), "", "");
      } catch (ActionFailedException afe)
      {
        nameButton.setVisible(false);
        System.out.println("An error occured:" + afe.getMessage());
      } catch (Exception ex)
      {
        nameButton.setVisible(false);
        System.out.println("An error occured:" + ex.getMessage());
      }
    } else
    {
      nameButton.setVisible(false);
    }
    action = service.getCPAction("GetCurrentBrightness");
    if (action != null)
    {
      try
      {
        controlPoint.invokeAction(action);

        brightnessButton.setText(action.getArgument("CurrentBrightness").getNumericValue() + " %");
      } catch (ActionFailedException afe)
      {
        nameButton.setVisible(false);
        System.out.println("An error occured:" + afe.getMessage());
      } catch (Exception ex)
      {
        nameButton.setVisible(false);
        System.out.println("An error occured:" + ex.getMessage());
      }
    } else
    {
      nameButton.setVisible(false);
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
    if (service.getCPStateVariable("CurrentBrightness") == csv)
    {
      // set value
      try
      {
        brightnessButton.setText(csv.getNumericValue() + " %");
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

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel leftFillPanel = new JPanel();
    leftFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(leftFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel rightFillPanel = new JPanel();
    rightFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(rightFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel topFillPanel = new JPanel();
    topFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(topFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel bottomFillPanel = new JPanel();
    bottomFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(bottomFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    add(applicationButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    add(nameButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    add(brightnessButton, gridBagConstraints);

    repaint();
    validateTree();
  }

}

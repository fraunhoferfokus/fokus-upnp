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
import java.util.StringTokenizer;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothArea;

/**
 * This plugin is used for Usage services.
 * 
 * @author Alexander Koenig
 */
public class UsagePlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = DeviceConstant.USAGE_SERVICE_TYPE;

  private SmoothArea        usageArea;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    initComponents();

    usageArea = new SmoothArea(new Dimension(500, 300), ButtonConstants.TEXT_FONT_SIZE, null);
    usageArea.setButtonColor(ButtonConstants.LIGHT_BUTTON_COLOR);

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    // initialize panel layout
    updateLayout();
  }

  /** Retrieves the type of the plugin */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#runPlugin()
   */
  public void startPlugin()
  {
    super.startPlugin();

    Thread thread = new Thread(this);
    thread.setName("UsagePlugin");
    thread.start();
  }

  /** Read the usage. */
  public void run()
  {
    CPAction action = service.getCPAction("GetUsage");
    if (action != null)
    {
      try
      {
        action.getInArgument("Language").setValue("de");
        controlPoint.invokeAction(action);

        String usage = action.getArgument("Usage").getStringValue();

        if (usage != null)
        {
          StringTokenizer tokenizer = new StringTokenizer(usage, "\n");
          while (tokenizer.hasMoreTokens())
          {
            usageArea.addLine(tokenizer.nextToken());
          }
          usageArea.setSizeToFitContent();
        }
      } catch (ActionFailedException afe)
      {
        System.out.println("An error occured:" + afe.getMessage());
      } catch (Exception ex)
      {
        System.out.println("An error occured:" + ex.getMessage());
      }
    }
    updateLayout();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   */
  private void initComponents()
  {

    setLayout(new java.awt.GridBagLayout());

    setBackground(new java.awt.Color(204, 204, 255));
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    removeAll();
    GridBagConstraints gridBagConstraints;

    // build panels to center information
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel leftFillPanel = new JPanel();
    leftFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(leftFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel rightFillPanel = new JPanel();
    rightFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(rightFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 1;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel topFillPanel = new JPanel();
    topFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(topFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 1;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel bottomFillPanel = new JPanel();
    bottomFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(bottomFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    add(usageArea, gridBagConstraints);

    repaint();
    validateTree();
  }

}

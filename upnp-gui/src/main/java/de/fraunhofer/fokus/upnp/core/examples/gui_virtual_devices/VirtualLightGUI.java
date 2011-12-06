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
package de.fraunhofer.fokus.upnp.core.examples.gui_virtual_devices;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.examples.lights.DimmableLightDevice;
import de.fraunhofer.fokus.upnp.core.examples.lights.IDimming;
import de.fraunhofer.fokus.upnp.core.examples.lights.ISwitchPower;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.swing.PanelHelper;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This GUI can be used as virtual dimmable light.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class VirtualLightGUI extends JFrame implements IDimming, ISwitchPower
{

  /**  */
  private static final long      serialVersionUID = 1L;

  private static final Color     BACKGROUND_COLOR = new Color(200, 200, 200);

  private static final Color     BORDER_COLOR     = new Color(220, 220, 220);

  private static final Dimension FORM_SIZE        = new Dimension(340, 100);

  private JPanel                 contentPanel;

  private SmoothBorder           border;

  private SmoothValueButton      switchButton;

  private UPnPStartupConfiguration   startupConfiguration;

  private DimmableLightDevice    dimmableLightDevice;

  private boolean                activated        = false;

  private int                    level            = 0;

  /**
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    new VirtualLightGUI(args).setVisible(true);
  }

  /**
   * Creates a new instance of VirtualTemperatureGUI.
   * 
   * @param args
   */
  public VirtualLightGUI(String args[])
  {
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // build layout
    contentPanel = new JPanel();

    setTitle("Virtuelles Licht");
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        terminateForm(evt);
      }
    });

    // init layout
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new GridBagLayout());

    border = new SmoothBorder(BORDER_COLOR);
    border.setShadow(new Point(0, 0));
    contentPanel.setBorder(border);

    contentPanel.setBackground(BACKGROUND_COLOR);

    switchButton = new SmoothValueButton(new Dimension(300, 40), 20, "Licht:", "Aus", null);
    switchButton.setBackground(BACKGROUND_COLOR);
    switchButton.setDisabledButtonColor(switchButton.getButtonColor());
    switchButton.setSelectable(false);

    getContentPane().add(contentPanel, BorderLayout.CENTER);

    GridBagConstraints constraints = PanelHelper.initGridBagConstraints(5, 5);
    contentPanel.add(switchButton, constraints);

    constraints = PanelHelper.initScaleGridBagConstraints(0, 0);
    constraints.gridy = 1;
    JPanel fillPanel = new JPanel();
    fillPanel.setBackground(BACKGROUND_COLOR);
    contentPanel.add(fillPanel, constraints);

    // load startup configuration
    String startupFileName =
      args.length > 0 ? args[0] : FileHelper.getStartupConfigurationName(StringHelper.getShortClassName(this.getClass()
        .getName()));
    startupConfiguration = new UPnPStartupConfiguration(startupFileName);
    startupConfiguration.setStartKeyboardThread(false);
    if (!startupConfiguration.isValid())
    {
      System.out.println("Invalid or missing startup configuration.");
      System.exit(1);
    }
    startupConfiguration.trySetValidWorkingDirectory(this.getClass().getName());
    dimmableLightDevice = new DimmableLightDevice(null, startupConfiguration, this, this);

    this.setSize(FORM_SIZE);
    this.setResizable(false);
    updateGUI();
    contentPanel.revalidate();
    contentPanel.repaint();
  }

  /** Terminates the GUI. */
  private void terminateForm(java.awt.event.WindowEvent evt)
  {
    dimmableLightDevice.terminate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.lights.IDimming#getLoadLevelStatus(java.lang.String)
   */
  public int getLoadLevelStatus(String upnpServiceID)
  {
    return level;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.lights.IDimming#setLoadLevelTarget(java.lang.String,
   *      int)
   */
  public void setLoadLevelTarget(String upnpServiceID, int value)
  {
    level = value;
    updateGUI();
    dimmableLightDevice.getDimmingService().loadLevelStatusChanged(level);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.lights.ISwitchPower#getStatus(java.lang.String)
   */
  public boolean getStatus(String upnpServiceID)
  {
    return activated;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.lights.ISwitchPower#setTarget(java.lang.String,
   *      boolean)
   */
  public void setTarget(String upnpServiceID, boolean state)
  {
    activated = state;
    updateGUI();
    dimmableLightDevice.getSwitchPowerService().statusChanged(activated);
  }

  private void updateGUI()
  {
    switchButton.setValue(activated ? level + "%" : "Aus");
    int colorComponent = (int)Math.round(0.2 + level * 2);
    switchButton.setFontColor(level < 60 ? new Color(200, 200, 200) : Color.black);
    switchButton.setDisabledButtonColor(new Color(colorComponent, colorComponent, colorComponent));
  }

}

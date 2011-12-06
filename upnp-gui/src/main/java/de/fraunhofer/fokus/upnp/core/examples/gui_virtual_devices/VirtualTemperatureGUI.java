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
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider;
import de.fraunhofer.fokus.upnp.core.examples.sensors.temperature.TemperatureSensorDevice;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.swing.PanelHelper;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This GUI can be used as virtual temperature sensor.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class VirtualTemperatureGUI extends JFrame implements ITemperatureProvider, IBinaryUPnPDevice, Runnable
{

  /**  */
  private static final long       serialVersionUID = 1L;

  private static final Color      BACKGROUND_COLOR = new Color(200, 200, 200);

  private static final Color      BORDER_COLOR     = new Color(220, 220, 220);

  private static final Dimension  FORM_SIZE        = new Dimension(340, 130);

  private JPanel                  contentPanel;

  private SmoothBorder            border;

  private SmoothValueButton       temperatureButton;

  private JSlider                 temperatureSlider;

  private UPnPStartupConfiguration    startupConfiguration;

  private TemperatureSensorDevice temperatureSensorDevice;

  private int                     lastTemperature  = 0;

  private boolean                 terminateThread  = false;

  private boolean                 terminated       = false;

  /**
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    new VirtualTemperatureGUI(args).setVisible(true);
  }

  /**
   * Creates a new instance of VirtualTemperatureGUI.
   * 
   * @param args
   */
  public VirtualTemperatureGUI(String args[])
  {
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // build layout
    contentPanel = new JPanel();

    setTitle("Virtueller Sensor");
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

    temperatureButton = new SmoothValueButton(new Dimension(300, 40), 20, "Temperatur:", "0 °C", null);
    temperatureButton.setBackground(BACKGROUND_COLOR);
    temperatureButton.setDisabledButtonColor(temperatureButton.getButtonColor());
    temperatureButton.setSelectable(false);

    temperatureSlider = new JSlider(SwingConstants.HORIZONTAL, -30, 70, 0);
    temperatureSlider.setBackground(BACKGROUND_COLOR);

    getContentPane().add(contentPanel, BorderLayout.CENTER);

    GridBagConstraints constraints = PanelHelper.initGridBagConstraints(5, 5);
    contentPanel.add(temperatureButton, constraints);

    constraints = PanelHelper.initScaleGridBagConstraints(0, 0);
    constraints.gridy = 1;
    constraints.weighty = 0.0;
    contentPanel.add(temperatureSlider, constraints);

    constraints = PanelHelper.initScaleGridBagConstraints(0, 0);
    constraints.gridy = 2;
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
    temperatureSensorDevice = new TemperatureSensorDevice(null, startupConfiguration, this, this);
    // must be started manually because TemperatureSensorDevice.forceRunDelayed() is set to true
    temperatureSensorDevice.runDelayed();

    // start temperature update thread
    new Thread(this).start();

    this.setSize(FORM_SIZE);
    this.setResizable(false);
    contentPanel.revalidate();
    contentPanel.repaint();
  }

  /** Terminates the GUI. */
  private void terminateForm(java.awt.event.WindowEvent evt)
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
    temperatureSensorDevice.terminate();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice#getApplication()
   */
  public String getApplication()
  {
    return "Room";
  }

  /* (non-Javadoc)
   * @see java.awt.Component#getName()
   */
  public String getName()
  {
    return "VirtualTemperature";
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider#getTemperature()
   */
  public int getTemperature()
  {
    return temperatureSlider.getValue() * 100;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    while (!terminateThread)
    {
      // check if temperature has changed
      if (lastTemperature != temperatureSlider.getValue())
      {
        lastTemperature = temperatureSlider.getValue();
        temperatureButton.setValue(lastTemperature + " °C");
        temperatureSensorDevice.temperatureChanged(lastTemperature * 100);
      }
      ThreadHelper.sleep(500);
    }
    terminated = true;
  }
}

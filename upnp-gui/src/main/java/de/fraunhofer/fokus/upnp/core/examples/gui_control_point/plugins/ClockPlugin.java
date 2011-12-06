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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This plugin is used for a clock service.
 * 
 * @author Alexander Koenig
 */
public class ClockPlugin extends BaseCPServicePlugin
{

  private static final long   serialVersionUID = 1L;

  private static final String DATE_TIME_GAP    = "     ";

  public static String        PLUGIN_TYPE      = DeviceConstant.CLOCK_SERVICE_TYPE;

  private SmoothButton        timeButton;

  private String              currentDate      = "";

  private int                 currentSeconds   = 0;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#getPluginType()
   */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    setLayout(new GridBagLayout());
    setBackground(ButtonConstants.BACKGROUND_COLOR);

    timeButton = new SmoothButton(new Dimension(300, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "", null);

    // initialize panel layout
    updateLayout();
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
    thread.setName("ClockPlugin");
    thread.start();
  }

  /** Get initial time */
  public void run()
  {
    CPAction action = service.getCPAction("GetTime");
    if (action != null)
    {
      try
      {
        controlPoint.invokeAction(action);

        String time = action.getArgument("Time").getStringValue();

        // parse time
        String[] split = time.split(" ");
        if (split.length > 1)
        {
          String[] dates = split[0].split("-");
          if (dates.length > 2)
          {
            currentDate = dates[2] + "." + dates[1] + "." + dates[0];
            timeButton.setText(currentDate + DATE_TIME_GAP + split[1]);
          }
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

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (stateVariable.getName().equals("Seconds") && stateVariable.getCPService() == service)
    {
      try
      {
        int newSeconds = (int)stateVariable.getNumericValue();
        // update date if seconds overflow
        if (newSeconds < currentSeconds)
        {
          new Thread(this).start();
        }
        currentSeconds = newSeconds;
        int hours = currentSeconds / 3600;
        int minutes = currentSeconds / 60 % 60;
        int seconds = currentSeconds % 60;

        timeButton.setText(currentDate + DATE_TIME_GAP + hours + ":" + extendInt(minutes) + ":" + extendInt(seconds));
      } catch (Exception e)
      {
      }
    }
  }

  /** Extend an int to two digits */
  private String extendInt(int value)
  {
    return value < 10 ? "0" + value : value + "";
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    removeAll();

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(timeButton, BorderLayout.CENTER);

    buildCenteredLayout(contentPanel);

    repaint();
    validateTree();
  }

}

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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationConstant;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This plugin is used for the localization service.
 * 
 * @author Alexander Koenig
 */
public class LocalizationPlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID = 1L;

  public static String      PLUGIN_TYPE      = LocalizationConstant.LOCALIZATION_SERVICE_TYPE;

  private SmoothButton      readButton;

  private SmoothButton      inactiveButton;

  private SmoothButton      userTitleButton;

  private SmoothButton      locationTitleButton;

  private Vector            userList;

  private JPanel            overviewFillPanel;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    setLayout(new GridBagLayout());

    readButton = new SmoothButton(new Dimension(300, ButtonConstants.BUTTON_HEIGHT), 12, "Lese Nachrichten...", null);
    readButton.setBackground(getBackground());
    readButton.setDisabledButtonColor(readButton.getButtonColor());
    readButton.setSelectable(false);

    inactiveButton =
      new SmoothButton(new Dimension(300, 2 * ButtonConstants.BUTTON_HEIGHT), 20, "Lokalisierung ist nicht aktiv", null);
    inactiveButton.setBackground(getBackground());
    inactiveButton.setDisabledButtonColor(ButtonConstants.RED_BUTTON_COLOR);
    inactiveButton.setSelectable(false);

    userTitleButton = new SmoothButton(new Dimension(200, ButtonConstants.BUTTON_HEIGHT), 12, "Benutzer", null);
    userTitleButton.setBackground(getBackground());
    userTitleButton.setButtonColor(ButtonConstants.LIGHT_BUTTON_COLOR);
    locationTitleButton = new SmoothButton(new Dimension(200, ButtonConstants.BUTTON_HEIGHT), 12, "Ort", null);
    locationTitleButton.setBackground(getBackground());
    locationTitleButton.setButtonColor(ButtonConstants.LIGHT_BUTTON_COLOR);

    overviewFillPanel = new JPanel();
    overviewFillPanel.setBackground(getBackground());

    userList = new Vector();

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
    thread.setName("LocalizationPlugin");
    thread.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#getPluginType()
   */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /** Collect initial locations */
  public void run()
  {
    CPStateVariable userListStateVariable = service.getCPStateVariable("UserList");
    if (userListStateVariable != null)
    {
      String userListString = null;
      try
      {
        userListString = userListStateVariable.getStringValue();
      } catch (Exception ex)
      {
      }
      StringTokenizer stringTokenizer = new StringTokenizer(userListString, ",");
      while (stringTokenizer.hasMoreTokens())
      {
        String currentUser = stringTokenizer.nextToken().trim();

        CPAction action = service.getCPAction("GetLocation");
        if (action != null)
        {
          try
          {
            action.getArgument("User").setValue(currentUser);

            controlPoint.invokeAction(action);

            String location = action.getArgument("Location").getStringValue();

            UserEntry userEntry = new UserEntry(currentUser, location);
            userList.add(userEntry);
          } catch (ActionFailedException afe)
          {
            System.out.println("An error occured:" + afe.getMessage());
          } catch (Exception ex)
          {
            System.out.println("An error occured:" + ex.getMessage());
          }
        }
      }
    }
    updateLayout();
    System.out.println(userList.size() + " users read.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BaseCPServicePlugin#stateVariableChanged(de.fhg.fokus.magic.upnp.control_point.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {

    if (service.getCPStateVariable("Active") == stateVariable)
    {
      updateLayout();
    }
    if (service.getCPStateVariable("CurrentEventID") == stateVariable)
    {
      CPAction action = service.getCPAction("GetEvent");
      if (action != null)
      {
        try
        {
          long eventID = stateVariable.getNumericValue();

          action.getArgument("EventID").setNumericValue(eventID);

          controlPoint.invokeAction(action);

          String user = action.getArgument("User").getStringValue();
          String location = action.getArgument("Location").getStringValue();

          for (int i = 0; i < userList.size(); i++)
          {
            UserEntry currentEntry = (UserEntry)userList.elementAt(i);
            if (currentEntry.user.equals(user))
            {
              currentEntry.location = location;
              currentEntry.locationButton.setText(location);
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
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Updates the layout of the plugin */
  private void updateLayout()
  {
    removeAll();
    invalidate();

    GridBagConstraints gridBagConstraints;
    boolean active = false;
    try
    {
      active = service.getCPStateVariable("Active").getBooleanValue();
    } catch (Exception e)
    {
    }
    if (!active)
    {
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      add(inactiveButton, gridBagConstraints);

      // FillPanel
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      add(overviewFillPanel, gridBagConstraints);
    } else if (userList.size() == 0)
    {
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      add(readButton, gridBagConstraints);

      // FillPanel
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      add(overviewFillPanel, gridBagConstraints);
    } else
    {
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      add(userTitleButton, gridBagConstraints);
      gridBagConstraints.gridx = 1;
      add(locationTitleButton, gridBagConstraints);

      int userCount = userList.size();
      for (int i = 0; i < userCount; i++)
      {
        UserEntry currentEntry = (UserEntry)userList.elementAt(i);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i + 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(currentEntry.userButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        add(currentEntry.locationButton, gridBagConstraints);
      }
      // FillPanel for overview
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = userCount + 1;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      add(overviewFillPanel, gridBagConstraints);
    }

    repaint();
    validateTree();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private classes //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** This class holds one message */
  private class UserEntry
  {

    public String       user;

    public String       location;

    public SmoothButton userButton;

    public SmoothButton locationButton;

    /**
     * Creates a new instance of UserEntry
     * 
     * @param user
     * @param location
     */
    public UserEntry(String user, String location)
    {
      this.user = user;
      this.location = location;

      userButton = new SmoothButton(new Dimension(200, 30), 12, user, null);
      userButton.setBackground(getBackground());

      locationButton = new SmoothButton(new Dimension(200, 30), 12, location, null);
      locationButton.setBackground(getBackground());
    }

  }

}

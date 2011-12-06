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
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for the vehicle team service.
 * 
 * @author Alexander Koenig
 */
public class VehicleTeamServicePlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID  = 1L;

  public static String      PLUGIN_TYPE       = DeviceConstant.VEHICLE_TEAM_SERVICE_TYPE;

  private JPanel            myNodePanel;

  private JPanel            myNodeFillPanel;

  private JPanel            myTeamPanel;

  private JPanel            myTeamFillPanel;

  private JPanel            otherTeamsPanel;

  private JPanel            otherTeamsFillPanel;

  private JPanel            fillPanel;

  private SmoothValueButton myAddressButton;

  private SmoothButton      myTeamButton;

  private SmoothButton      myTeamNameButton;

  private SmoothValueButton myTeamLeaderButton;

  private SmoothValueButton myTeamMembersButton;

  private SmoothButton      createTeamButton;

  private SmoothButton      deleteTeamButton;

  private SmoothButton      otherTeamsButton;

  private SmoothButton      requestTeamsButton;

  /** Joined team */
  private String            joinedTeamName    = null;

  /** Hashtable with known teams */
  private Hashtable         teamFromNameTable = new Hashtable();

  /** Vector with known team names */
  private Vector            teamNameList      = new Vector();

  /** List with team names that must be requested */
  private Vector            pendingTeamList   = new Vector();

  private boolean           terminateThread   = false;

  private boolean           terminated        = false;

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

    myAddressButton =
      new SmoothValueButton(new Dimension(200, ButtonConstants.BUTTON_HEIGHT), 12, "Eigene Adresse:", "", null);
    myAddressButton.setDisabledButtonColor(myAddressButton.getButtonColor());
    myAddressButton.setSelectable(false);

    myTeamButton = new SmoothButton(new Dimension(770, ButtonConstants.BUTTON_HEIGHT), 12, "Mein Team", null);
    myTeamButton.setBoldFont();
    myTeamButton.setDisabledButtonColor(myTeamButton.getButtonColor());
    myTeamButton.setSelectable(false);

    otherTeamsButton = new SmoothButton(new Dimension(930, ButtonConstants.BUTTON_HEIGHT), 12, "Andere Teams", null);
    otherTeamsButton.setBoldFont();
    otherTeamsButton.setDisabledButtonColor(myTeamButton.getButtonColor());
    otherTeamsButton.setSelectable(false);

    myTeamNameButton = new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    myTeamLeaderButton =
      new SmoothValueButton(new Dimension(200, ButtonConstants.BUTTON_HEIGHT), 12, "Anführer:", "", null);
    myTeamMembersButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Mitglieder:", "", null);
    createTeamButton =
      new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, "Team erstellen", "create");
    createTeamButton.addActionListener(this);
    deleteTeamButton =
      new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, "Team verlassen", "leave");
    deleteTeamButton.addActionListener(this);
    deleteTeamButton.setSelectable(false);

    requestTeamsButton =
      new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, "Nach Teams fragen", "requestOverview");
    requestTeamsButton.addActionListener(this);

    myNodePanel = new JPanel();
    myNodePanel.setBackground(getBackground());
    myNodePanel.setLayout(new GridBagLayout());
    myNodePanel.setBorder(new SmoothBorder(ButtonConstants.SMOOTH_ORANGE_COLOR));
    myNodeFillPanel = new JPanel();
    myNodeFillPanel.setBackground(getBackground());

    myTeamPanel = new JPanel();
    myTeamPanel.setBackground(getBackground());
    myTeamPanel.setLayout(new GridBagLayout());
    myTeamPanel.setBorder(new SmoothBorder(ButtonConstants.SMOOTH_BLUE_COLOR));
    myTeamFillPanel = new JPanel();
    myTeamFillPanel.setBackground(getBackground());

    otherTeamsPanel = new JPanel();
    otherTeamsPanel.setBackground(getBackground());
    otherTeamsPanel.setLayout(new GridBagLayout());
    otherTeamsPanel.setBorder(new SmoothBorder(ButtonConstants.BUTTON_COLOR));
    otherTeamsFillPanel = new JPanel();
    otherTeamsFillPanel.setBackground(getBackground());

    fillPanel = new JPanel();
    fillPanel.setBackground(getBackground());

    GridBagConstraints gridBagConstraints;
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    add(myNodePanel, gridBagConstraints);

    gridBagConstraints.gridy = 1;
    add(myTeamPanel, gridBagConstraints);

    gridBagConstraints.gridy = 2;
    add(otherTeamsPanel, gridBagConstraints);

    gridBagConstraints.gridy = 3;
    gridBagConstraints.weighty = 1.0;
    add(fillPanel, gridBagConstraints);

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
    thread.setName("VehicleTeamServicePlugin");
    thread.start();
  }

  /** Get initial time */
  public void run()
  {
    while (!terminateThread)
    {
      String pendingTeamName = null;
      synchronized(pendingTeamList)
      {
        if (pendingTeamList.size() > 0)
        {
          pendingTeamName = (String)pendingTeamList.remove(0);
        }
      }
      if (pendingTeamName != null)
      {
        System.out.println("Request members for team " + pendingTeamName);
        teamFromNameTable.remove(pendingTeamName);
        try
        {
          CPAction action = service.getCPAction("GetTeamMembers");
          action.getArgument("Name").setValue(pendingTeamName);

          controlPoint.invokeAction(action);

          String result = action.getArgument("Result").getStringValue();

          TeamParser teamParser = new TeamParser();
          teamParser.parse(result);

          Team team = teamParser.team;
          if (team != null)
          {
            teamFromNameTable.put(team.name, team);

            // update members of my team
            if (joinedTeamName != null && joinedTeamName.equals(team.name))
            {
              myTeamNameButton.setText(team.name);
              myTeamLeaderButton.setValue(team.leader);
              myTeamMembersButton.setValue(team.toMemberString());
            }
          }
        } catch (Exception e)
        {
          System.out.println("Error requesting team members: " + e.getMessage());
        }
        updateLayout();
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BasePlugin#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    super.actionPerformed(e);
    if (e.getActionCommand().equals(createTeamButton.getID()))
    {
      try
      {
        String teamName = DialogValueInvocation.getInvokedString(null, "Neuer Teamname", "");

        CPAction action = service.getCPAction("CreateTeam");
        action.getArgument("Name").setValue(teamName);

        controlPoint.invokeAction(action);

      } catch (Exception ex)
      {
      }
    }
    if (e.getActionCommand().equals(deleteTeamButton.getID()))
    {
      try
      {
        CPAction action = service.getCPAction("LeaveTeam");
        controlPoint.invokeAction(action);

      } catch (Exception ex)
      {
      }
    }
    if (e.getActionCommand().equals(requestTeamsButton.getID()))
    {
      try
      {
        CPAction action = service.getCPAction("RequestAvailableTeams");
        controlPoint.invokeAction(action);

      } catch (Exception ex)
      {
      }
    }
    if (e.getActionCommand().startsWith("join."))
    {
      try
      {
        CPAction action = service.getCPAction("JoinTeam");
        String teamName = e.getActionCommand().substring("join.".length());

        System.out.println("Try to join team " + teamName);

        action.getArgument("Name").setValue(teamName);

        controlPoint.invokeAction(action);
      } catch (Exception ex)
      {
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    // a state variable in our remote device has changed
    if (stateVariable.getCPService() == service)
    {
      System.out.println("State variable: " + stateVariable.getName() + " to " + stateVariable.getValueAsString());
      if (stateVariable.getName().equals("NodeAddress"))
      {
        try
        {
          myAddressButton.setValue(stateVariable.getStringValue());
        } catch (Exception e)
        {
        }
      }
      if (stateVariable.getName().equals("UpdateID") || stateVariable.getName().equals("AvailableTeams"))
      {
        System.out.println("UpdateID or AvailableTeams changed, trigger team member request");

        teamNameList.clear();
        try
        {
          String availableTeams = service.getCPStateVariable("AvailableTeams").getStringValue();

          StringTokenizer stringTokenizer = new StringTokenizer(availableTeams, ",");
          while (stringTokenizer.hasMoreTokens())
          {
            teamNameList.add(stringTokenizer.nextToken());
          }
        } catch (Exception e)
        {
        }
        // remove teams from hashtable if not found in available teams
        Enumeration teamNames = CollectionHelper.getPersistentKeysEnumeration(teamFromNameTable);
        while (teamNames.hasMoreElements())
        {
          String currentTeamName = (String)teamNames.nextElement();
          if (!teamNameList.contains(currentTeamName))
          {
            teamFromNameTable.remove(currentTeamName);
          }
        }
        // request team members for all available teams
        for (int i = 0; i < teamNameList.size(); i++)
        {
          String currentTeamName = (String)teamNameList.elementAt(i);
          synchronized(pendingTeamList)
          {
            if (!pendingTeamList.contains(currentTeamName))
            {
              pendingTeamList.add(currentTeamName);
            }
          }
        }
        updateLayout();
      }
      if (stateVariable.getName().equals("JoinedTeamName"))
      {
        System.out.println("Team name changed");
        try
        {
          if (stateVariable.getStringValue().length() > 0)
          {
            joinedTeamName = stateVariable.getStringValue();
            synchronized(pendingTeamList)
            {
              if (!pendingTeamList.contains(stateVariable.getStringValue()))
              {
                pendingTeamList.add(stateVariable.getStringValue());
              }
            }
          } else
          {
            joinedTeamName = null;
            myTeamLeaderButton.setValue("");
            myTeamMembersButton.setValue("");
          }
          myTeamNameButton.setText(joinedTeamName != null ? joinedTeamName : "");
          createTeamButton.setSelectable(joinedTeamName == null);
          deleteTeamButton.setSelectable(joinedTeamName != null);

        } catch (Exception e)
        {
          System.out.println("Error updating team name: " + e.getMessage());
          e.printStackTrace();
        }
        updateLayout();
      }
    }
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    myTeamPanel.removeAll();
    otherTeamsPanel.removeAll();

    GridBagConstraints gridBagConstraints;

    // my node panel
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    myNodePanel.add(myAddressButton, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    myNodePanel.add(myNodeFillPanel, gridBagConstraints);

    // my team panel
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    myTeamPanel.add(myTeamButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 1;
    myTeamPanel.add(myTeamNameButton, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    myTeamPanel.add(myTeamLeaderButton, gridBagConstraints);
    gridBagConstraints.gridx = 2;
    myTeamPanel.add(myTeamMembersButton, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    myTeamPanel.add(createTeamButton, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    myTeamPanel.add(deleteTeamButton, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    myTeamPanel.add(myTeamFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(2, 5, 2, 5);
    otherTeamsPanel.add(otherTeamsButton, gridBagConstraints);

    for (int i = 0; i < teamNameList.size(); i++)
    {
      String currentTeamName = (String)teamNameList.elementAt(i);
      if (joinedTeamName == null || !currentTeamName.equals(joinedTeamName))
      {
        // other teams panel
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i + 1;
        gridBagConstraints.gridwidth = 1;
        SmoothButton nameButton =
          new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, currentTeamName, null);

        otherTeamsPanel.add(nameButton, gridBagConstraints);

        Team associatedTeam = (Team)teamFromNameTable.get(currentTeamName);
        if (associatedTeam != null)
        {
          SmoothValueButton currentTeamLeaderButton =
            new SmoothValueButton(new Dimension(200, ButtonConstants.BUTTON_HEIGHT),
              12,
              "Anführer:",
              associatedTeam.leader,
              null);
          SmoothValueButton currentTeamMembersButton =
            new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT),
              12,
              "Mitglieder:",
              associatedTeam.toMemberString(),
              null);

          gridBagConstraints.gridx = 1;
          otherTeamsPanel.add(currentTeamLeaderButton, gridBagConstraints);
          gridBagConstraints.gridx = 2;
          otherTeamsPanel.add(currentTeamMembersButton, gridBagConstraints);
        }
        SmoothButton joinButton =
          new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT), 12, "Team beitreten", "join." +
            currentTeamName);
        joinButton.addActionListener(this);
        joinButton.setSelectable(joinedTeamName == null);
        gridBagConstraints.gridx = 3;
        otherTeamsPanel.add(joinButton, gridBagConstraints);
      }
    }
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = teamNameList.size() + 1;
    otherTeamsPanel.add(requestTeamsButton, gridBagConstraints);

    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    otherTeamsPanel.add(otherTeamsFillPanel, gridBagConstraints);

    myTeamPanel.repaint();
    otherTeamsPanel.repaint();
    validateTree();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BasePlugin#terminate()
   */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
    super.terminate();
  }

  /** This class represents a team */
  private class Team
  {

    public String name    = null;

    public Vector members = new Vector();

    public String leader  = null;

    public Team(String name)
    {
      this.name = name;
    }

    public String toMemberString()
    {
      String result = "";
      for (int i = 0; i < members.size(); i++)
      {
        result += (result.length() > 0 ? "," : "") + members.elementAt(i).toString();
      }
      return result;
    }

  }

  /** This class parses team messages */
  private class TeamParser extends SAXTemplateHandler
  {

    public Team team;

    /*
     * (non-Javadoc)
     * 
     * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processStartElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void processStartElement(String uri, String name, String name2, Attributes atts) throws SAXException
    {
      if (getTagCount() == 1)
      {
        String teamName = null;
        for (int i = 0; i < atts.getLength(); i++)
        {
          if (atts.getQName(i).equalsIgnoreCase("name"))
          {
            teamName = atts.getValue(i);
          }
        }
        if (teamName != null)
        {
          team = new Team(teamName);
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processContentElement(java.lang.String)
     */
    public void processContentElement(String content) throws SAXException
    {
      if (team != null && getTagCount() == 2)
      {
        if (getCurrentTag().equalsIgnoreCase("leader"))
        {
          team.leader = content;
        }
        if (getCurrentTag().equalsIgnoreCase("member"))
        {
          team.members.add(content);
        }
      }
    }

  }

}

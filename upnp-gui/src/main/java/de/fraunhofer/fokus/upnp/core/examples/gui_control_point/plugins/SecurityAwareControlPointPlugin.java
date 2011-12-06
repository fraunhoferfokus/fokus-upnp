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
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JFrame;

import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.SecurityGUIContext;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.ISecurityConsoleEvents;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This plugin is used for maintaining security aware control points.
 * 
 * @author Alexander Koenig
 */
public class SecurityAwareControlPointPlugin extends BaseCPDevicePlugin implements ISecurityConsoleEvents
{
  private static final long     serialVersionUID         = 1L;

  public static String          PLUGIN_TYPE              = "SecurityAwareControlPointManager";

  /** Security console for action invocation */
  private SecurityConsoleEntity securityConsole;

  /** Currently selected device */
  private LocalDictionaryObject currentDictionaryObject;

  private Vector                namedControlPointButtons = new Vector();

  private SmoothButton          currentNameButton;

  private SmoothButton          nameChangeButton;

  private SmoothButton          securityIDButton;

  /** Creates new form SecurityAwareControlPointPlugin */
  public SecurityAwareControlPointPlugin(JFrame frame, SecurityGUIContext securityContext)
  {
    setFrame(frame);
    setControlPoint(securityContext.getSecurityConsole().getSecurityAwareControlPoint());

    securityConsole = securityContext.getSecurityConsole();

    currentDictionaryObject = null;

    initComponents();
    setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityInfoPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityPropertyPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    buildNamedControlPointsButtonList();

    // create name change button
    nameChangeButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "",
        "userDefinedNameChange");
    nameChangeButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    nameChangeButton.addActionListener(this);

    // securityID button
    securityIDButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    securityIDButton.setSelectable(false);
    securityIDButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // initialize panel layout
    updateLayout();
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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    // System.out.println("SecurityAwareControlPointPlugin: Action performed with command
    // "+e.getActionCommand());
    // check if a new device was chosen
    boolean found = false;
    LocalDictionaryObject foundControlPoint = null;
    int i = 0;
    while (!found && i < securityConsole.getNamedControlPointCount())
    {
      foundControlPoint = securityConsole.getNamedControlPoint(i);
      found = foundControlPoint.getSecurityAwareObject().getSecurityID().equals(e.getActionCommand());
      if (!found)
      {
        i++;
      }
    }
    // new control point selected
    if (found && foundControlPoint != currentDictionaryObject)
    {
      setCurrentDictionaryObject(foundControlPoint);
    }
    // set user-defined control point name
    if (currentDictionaryObject != null && nameChangeButton.getID().equals(e.getActionCommand()))
    {
      String newName =
        DialogValueInvocation.getInvokedString(frame, "Name", currentDictionaryObject.getUserDefinedName());
      if (newName != null)
      {
        currentDictionaryObject.setUserDefinedName(newName);
        updateCurrentDictionaryObject();
        // Inform the security console about the changed name
        securityConsole.localDictionaryNameChange(currentDictionaryObject);
      }
    }
  }

  public void setCurrentDictionaryObject(LocalDictionaryObject dictionaryObject)
  {
    if (dictionaryObject != null)
    {
      currentDictionaryObject = dictionaryObject;
      // set current name button
      for (int i = 0; i < securityConsole.getNamedControlPointCount(); i++)
      {
        if (securityConsole.getNamedControlPoint(i) == currentDictionaryObject)
        {
          currentNameButton = getNameButton(i);
        }
      }
      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Security events //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#newSecurityAwareControlPoint(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void newSecurityAwareControlPoint(LocalDictionaryObject controlPoint)
  {
    SmoothButton nameButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        controlPoint.getUserDefinedName(),
        controlPoint.getSecurityAwareObject().getSecurityID());
    nameButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    namedControlPointButtons.add(nameButton);
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#newSecurityAwareDevice(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void newSecurityAwareCPDevice(LocalDictionaryObject device)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#securityAwareControlPointStatusChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void securityAwareControlPointStatusChange(LocalDictionaryObject controlPoint)
  {
    if (controlPoint == currentDictionaryObject)
    {
      updateCurrentDictionaryObject();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#securityAwareDeviceStatusChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void securityAwareCPDeviceStatusChange(LocalDictionaryObject device)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#localDictionaryNameChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void localDictionaryNameChange(LocalDictionaryObject localDictionaryObject)
  {
    buildNamedControlPointsButtonList();

    updateLayout();
  }

  /** Event that the security console user has changed. */
  public void securityConsoleUserChange()
  {
    // clear complete context
    currentDictionaryObject = null;
    currentNameButton = null;

    buildNamedControlPointsButtonList();

    updateLayout();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jSecurityContentPanel = new javax.swing.JPanel();
    jSecurityFillPanel = new javax.swing.JPanel();
    jSecurityDividerPanel = new javax.swing.JPanel();
    jSecurityPropertyPanel = new javax.swing.JPanel();
    jSecurityInfoPanel = new javax.swing.JPanel();

    setLayout(new java.awt.GridBagLayout());

    setBackground(new java.awt.Color(204, 204, 255));
    jSecurityContentPanel.setLayout(new java.awt.GridBagLayout());

    jSecurityContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSecurityContentPanel, gridBagConstraints);

    jSecurityFillPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jSecurityFillPanel, gridBagConstraints);

    jSecurityDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jSecurityDividerPanel.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jSecurityDividerPanel.setMinimumSize(new java.awt.Dimension(4, 10));
    jSecurityDividerPanel.setPreferredSize(new java.awt.Dimension(4, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    add(jSecurityDividerPanel, gridBagConstraints);

    jSecurityPropertyPanel.setLayout(new java.awt.GridBagLayout());

    jSecurityPropertyPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSecurityPropertyPanel, gridBagConstraints);

    jSecurityInfoPanel.setLayout(new java.awt.GridBagLayout());

    jSecurityInfoPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSecurityInfoPanel, gridBagConstraints);

  }// GEN-END:initComponents

  /** Updates all buttons to reflect the current object state */
  public void updateCurrentDictionaryObject()
  {
    if (currentDictionaryObject != null)
    {
      // update name in device overview
      currentNameButton.setText(currentDictionaryObject.getUserDefinedName());

      nameChangeButton.setText(currentDictionaryObject.getUserDefinedName());
      securityIDButton.setText(currentDictionaryObject.getSecurityAwareObject().getSecurityID());
    }
  }

  /** Redraws the layout of the plugin */
  public void updateLayout()
  {
    jSecurityContentPanel.removeAll();
    jSecurityPropertyPanel.removeAll();
    jSecurityInfoPanel.removeAll();
    jSecurityContentPanel.invalidate();
    jSecurityPropertyPanel.invalidate();
    jSecurityInfoPanel.invalidate();

    GridBagConstraints gridBagConstraints;

    // add all known security aware control points to content panel
    for (int i = 0; i < securityConsole.getNamedControlPointCount(); i++)
    {
      SmoothButton nameButton = getNameButton(i);

      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = i;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      nameButton.setSelected(nameButton == currentNameButton);
      jSecurityContentPanel.add(nameButton, gridBagConstraints);
    }

    // show selected control point if available
    if (currentDictionaryObject != null)
    {
      // show securityID, this is for debug only
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = java.awt.GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      jSecurityPropertyPanel.add(securityIDButton, gridBagConstraints);

      // show name button
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = java.awt.GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      jSecurityPropertyPanel.add(nameChangeButton, gridBagConstraints);

    }
    // hide plugin if there are no devices
    setVisible(securityConsole.getNamedControlPointCount() > 0);
    jSecurityContentPanel.repaint();
    jSecurityPropertyPanel.repaint();
    jSecurityInfoPanel.repaint();
    validateTree();
  }

  private SmoothButton getNameButton(int index)
  {
    if (index >= 0 && index < namedControlPointButtons.size())
    {
      return (SmoothButton)namedControlPointButtons.elementAt(index);
    }

    return null;
  }

  /** Creates a vector with buttons for all known security aware control points */
  private void buildNamedControlPointsButtonList()
  {
    namedControlPointButtons.clear();
    // create name buttons for all known control points
    for (int i = 0; i < securityConsole.getNamedControlPointCount(); i++)
    {
      LocalDictionaryObject dictionaryObject = securityConsole.getNamedControlPoint(i);
      SmoothButton nameButton =
        new SmoothButton(new Dimension(GUIConstants.OVERVIEW_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
          12,
          dictionaryObject.getUserDefinedName(),
          dictionaryObject.getSecurityAwareObject().getSecurityID());
      nameButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
      nameButton.addActionListener(this);

      namedControlPointButtons.add(nameButton);
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jSecurityContentPanel;

  private javax.swing.JPanel jSecurityDividerPanel;

  private javax.swing.JPanel jSecurityFillPanel;

  private javax.swing.JPanel jSecurityInfoPanel;

  private javax.swing.JPanel jSecurityPropertyPanel;
  // End of variables declaration//GEN-END:variables

}

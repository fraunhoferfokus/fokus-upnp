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
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JFrame;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControlConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ACLEntryButtons;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceTranslations;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.IDeviceTranslationManager;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.SecurityGUIContext;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.ACLParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.OwnersParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.PermissionsParser;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.ISecurityConsoleEvents;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothArea;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for maintaining secured devices, e.g. show its defined permissions or take
 * ownership.
 * 
 * @author Alexander Koenig
 */
public class SecuredDevicePlugin extends BaseCPDevicePlugin implements ISecurityConsoleEvents
{
  private static final long         serialVersionUID              = 1L;

  public static String              PLUGIN_TYPE                   = "SecuredDeviceManager";

  public static String              ACCESS_DENIED                 = "Zugriff verweigert";

  public static String              CHOOSE_CONTROLPOINT           = "Kontrollpunkt wählen...";

  public static String              CHOOSE_PERMISSION             = "Recht wählen...";

  public static String              CHOOSE_OWNER                  = "Besitzer wählen...";

  public static String              FREE_ENTRIES                  = "Freie Einträge:";

  public static String              LOADING                       = "Wird geladen...";

  public static String              NEW_ENTRY                     = "Neuer Eintrag";

  public static String              NEW_OWNER                     = "Neuer Besitzer";

  public static String              PERMISSION                    = "Recht";

  public static String              REMOVE                        = "Löschen";

  public static String              SAVE                          = "Speichern";

  /** Security console for action invocation */
  private SecurityConsoleEntity     securityConsole;

  /** Currently selected device */
  private LocalDictionaryObject     currentDictionaryObject;

  /** Currently selected info */
  private String                    currentInfoCommand            = "";

  private IDeviceTranslationManager deviceTranslationManager;

  private boolean                   terminateThread               = false;

  private boolean                   terminated                    = false;

  private boolean                   requestPermissions            = false;

  private boolean                   requestACL                    = false;

  private boolean                   requestOwners                 = false;

  // editing
  private boolean                   newACLEntrySubjectSelected    = false;

  private boolean                   newACLEntryAccessSelected     = false;

  private boolean                   changeACLEntrySubjectSelected = false;

  // private boolean changeACLEntryAccessSelected = false;
  private boolean                   newOwnerSelected              = false;

  // owner that should be added to the current dictionary object
  private SecurityAwareObject       pendingOwner                  = null;

  // owner that should be removed from the current dictionary object
  private SecurityAwareObject       removeOwner                   = null;

  // ACL entry that should be added to the current dictionary object
  private ACLEntry                  pendingACLEntry               = null;

  // ACL entry index that should be removed
  private int                       removeACLEntryIndex           = -1;

  private Vector                    namedDevicesButtons           = new Vector();

  private Vector                    namedControlPointsButtons     = new Vector();

  // currently selected device
  private SmoothButton              currentNameButton;

  private SmoothButton              nameChangeButton;

  private SmoothButton              securityIDButton;

  private SmoothButton              onlineButton;

  private SmoothButton              takeOwnerShipButton;

  private SmoothButton              showInfoButton;

  private SmoothButton              showOwnersButton;

  private SmoothButton              showPermissionsButton;

  private SmoothButton              showACLButton;

  private SmoothArea                infoArea;

  private SmoothValueButton         freeACLEntriesButton;

  private SmoothButton              newACLEntrySubjectButton;

  private SmoothButton              newACLEntryAccessButton;

  private SmoothButton              newACLEntrySaveButton;

  private SmoothButton              newOwnerButton;

  private SmoothButton              newOwnerSaveButton;

  private Permission                selectedPermission            = null;

  private LocalDictionaryObject     selectedControlPoint          = null;

  private SmoothButton              loadingButton;

  private SmoothButton              accessDeniedButton;

  private Vector                    ownerButtonList               = new Vector();

  private Vector                    ownerDeleteButtonList         = new Vector();

  private Vector                    permissionButtonList          = new Vector();

  private Vector                    aclButtonList                 = new Vector();

  /** Creates new form SecuredDevicePlugin */
  public SecuredDevicePlugin(JFrame frame,
    SecurityGUIContext securityContext,
    IDeviceTranslationManager deviceTranslationManager)
  {
    setFrame(frame);
    setControlPoint(securityContext.getSecurityConsole().getSecurityAwareControlPoint());

    securityConsole = securityContext.getSecurityConsole();
    this.deviceTranslationManager = deviceTranslationManager;

    currentDictionaryObject = null;

    initComponents();
    setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityInfoPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSecurityPropertyPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    buildNamedDevicesButtonList();
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
    securityIDButton.setDisabledButtonColor(securityIDButton.getButtonColor());

    // online button
    onlineButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Offline",
        null);
    onlineButton.setSelectable(false);
    onlineButton.setSelected(false);
    // onlineButton.setButtonColor(GUIConstants.DISABLED_BUTTON_COLOR);
    onlineButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // takeOwnership button
    takeOwnerShipButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "In Besitz nehmen",
        "takeOwnership");
    takeOwnerShipButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    takeOwnerShipButton.addActionListener(this);

    // show info button
    showInfoButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Informationen",
        "showInfos");
    showInfoButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    showInfoButton.addActionListener(this);

    // show owners button
    showOwnersButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Besitzer",
        "showOwners");
    showOwnersButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    showOwnersButton.addActionListener(this);

    // new owner button
    newOwnerButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        NEW_OWNER,
        "newOwner");
    newOwnerButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    newOwnerButton.addActionListener(this);

    newOwnerSaveButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        SAVE,
        "newOwnerSave");
    newOwnerSaveButton.setButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
    newOwnerSaveButton.setActiveButtonColor(Color.white);
    newOwnerSaveButton.addActionListener(this);

    // show permissions button
    showPermissionsButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Definierte Rechte",
        "showPermissions");
    showPermissionsButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    showPermissionsButton.addActionListener(this);

    // show ACL button
    showACLButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Zugewiesene Rechte",
        "showACL");
    showACLButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    showACLButton.addActionListener(this);

    // free ACL entries button
    freeACLEntriesButton =
      new SmoothValueButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        FREE_ENTRIES,
        "",
        "");
    freeACLEntriesButton.setSelectable(false);
    freeACLEntriesButton.setDisabledButtonColor(freeACLEntriesButton.getButtonColor());

    // new ACL entry buttons
    newACLEntrySubjectButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        NEW_ENTRY,
        "newACLEntrySubject");
    newACLEntrySubjectButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    newACLEntrySubjectButton.addActionListener(this);

    newACLEntryAccessButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        PERMISSION,
        "newACLEntryAccess");
    newACLEntryAccessButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    newACLEntryAccessButton.addActionListener(this);

    newACLEntrySaveButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        SAVE,
        "newACLEntrySave");
    newACLEntrySaveButton.setButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
    newACLEntrySaveButton.setActiveButtonColor(Color.white);
    newACLEntrySaveButton.addActionListener(this);

    loadingButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        LOADING,
        "");
    loadingButton.setSelectable(false);

    accessDeniedButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        ACCESS_DENIED,
        "");
    accessDeniedButton.setSelectable(false);

    // info area
    infoArea = new SmoothArea(new Dimension(500, 300), 12, null);
    infoArea.setSelectable(false);
    infoArea.setSelected(false);

    // initialize panel layout
    updateLayout();

    Thread thread = new Thread(this);
    thread.setName("SecuredDevicePlugin");
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

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (currentDictionaryObject != null && currentDictionaryObject.getSecurityAwareCPDeviceObject() != null &&
      stateVariable.getCPService() == service)
    {
      if (stateVariable.getName().equals("FreeACLSize"))
      {
        updateACLSize();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    System.out.println("SecuredDevicePlugin: Action performed with command " + e.getActionCommand());
    // check if a new device was chosen
    actionPerformedSelectDevice(e);
    actionPerformedSelectControlPoint(e);
    actionPerformedInfos(e);
    actionPerformedMisc(e);
    actionPerformedOwners(e);
    actionPerformedPermissions(e);
    actionPerformedACL(e);
  }

  /** This method is used for evaluating buttons events */
  public void actionPerformedSelectDevice(ActionEvent e)
  {
    boolean found = false;
    LocalDictionaryObject foundDevice = null;
    int i = 0;
    while (!found && i < securityConsole.getNamedDeviceCount())
    {
      foundDevice = securityConsole.getNamedDevice(i);
      found = foundDevice.getSecurityAwareObject().getSecurityID().equals(e.getActionCommand());
      if (!found)
      {
        i++;
      }
    }
    // new device selected
    if (found && foundDevice != currentDictionaryObject && pendingACLEntry == null)
    {
      setCurrentDictionaryObject(foundDevice);
    }
    // show misc info
    if (found && !currentInfoCommand.equals(foundDevice.getSecurityAwareObject().getSecurityID()))
    {
      currentInfoCommand = foundDevice.getSecurityAwareObject().getSecurityID();
      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  /** Checks for control point actions */
  public void actionPerformedSelectControlPoint(ActionEvent e)
  {
    if (newACLEntrySubjectSelected || newOwnerSelected)
    {
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
      // control point selected for ACL entry editing
      if (found && newACLEntrySubjectSelected)
      {
        // choose control point as new ACL subject
        selectedControlPoint = foundControlPoint;
        newACLEntrySubjectButton.setText(foundControlPoint.getUserDefinedName());
        newACLEntrySubjectSelected = false;
        updateCurrentDictionaryObject();
        updateLayout();
      }
      // control point selected for owner editing
      if (found && newOwnerSelected)
      {
        selectedControlPoint = foundControlPoint;
        newOwnerButton.setText(foundControlPoint.getUserDefinedName());
        newOwnerSelected = false;
        updateCurrentDictionaryObject();
        updateLayout();
      }
    }
  }

  /** Checks for miscellanous device actions */
  public void actionPerformedMisc(ActionEvent e)
  {
    // set user-defined device name
    if (currentDictionaryObject != null && nameChangeButton.getID().equals(e.getActionCommand()))
    {
      String newName =
        DialogValueInvocation.getInvokedString(frame, "Name", currentDictionaryObject.getUserDefinedName());
      if (newName != null)
      {
        currentDictionaryObject.setUserDefinedName(newName);
        // update buttons for display
        updateCurrentDictionaryObject();
        // inform the security console about the changed name
        securityConsole.localDictionaryNameChange(currentDictionaryObject);
      }
    }
    // take ownership
    if (currentDictionaryObject != null && takeOwnerShipButton.getID().equals(e.getActionCommand()))
    {
      try
      {
        // open dialog to invoke secret
        String secret = DialogValueInvocation.getInvokedString(frame, "SECRET eingeben", "");
        if (secret != null)
        {
          securityConsole.getSecurityConsoleControlPoint()
            .invokeTakeOwnership(currentDictionaryObject.getSecurityAwareCPDeviceObject(), secret);
        }
        updateCurrentDictionaryObject();
        // reread owners
        requestOwners = true;
      } catch (Exception ex)
      {
        System.out.println("ERROR: " + ex.getMessage());
      }
    }
  }

  /** Checks for device info actions */
  public void actionPerformedInfos(ActionEvent e)
  {
    // show infos
    if (currentDictionaryObject != null && showInfoButton.getID().equals(e.getActionCommand()) &&
      !showInfoButton.getID().equals(currentInfoCommand))
    {
      currentInfoCommand = showInfoButton.getID();
      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  /** Checks for device owner actions */
  public void actionPerformedOwners(ActionEvent e)
  {
    // show all owners
    if (currentDictionaryObject != null && showOwnersButton.getID().equals(e.getActionCommand()) &&
      !showOwnersButton.getID().equals(currentInfoCommand))
    {
      currentInfoCommand = showOwnersButton.getID();
      newOwnerSelected = false;
      newOwnerButton.setText(NEW_OWNER);
      selectedControlPoint = null;
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // check for remove owner
    for (int i = 0; i < ownerDeleteButtonList.size(); i++)
    {
      SmoothButton button = (SmoothButton)ownerDeleteButtonList.elementAt(i);
      if (removeOwner == null && button.getID().equals(e.getActionCommand()))
      {
        String keyHashBase64 = button.getID().substring(button.getID().indexOf("_") + 1);
        try
        {
          removeOwner = new SecurityAwareObject(CommonConstants.SHA_1_UPNP, keyHashBase64);
        } catch (Exception ex)
        {
        }
      }
    }
    // new owner selected
    if (currentDictionaryObject != null && newOwnerButton.getID().equals(e.getActionCommand()) && !newOwnerSelected)
    {
      newOwnerSelected = true;
      newOwnerButton.setText(CHOOSE_OWNER);
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // save owner selected
    if (currentDictionaryObject != null && newOwnerSaveButton.getID().equals(e.getActionCommand()) &&
      selectedControlPoint != null && pendingOwner == null)
    {
      pendingOwner =
        new SecurityAwareObject(CommonConstants.SHA_1_UPNP, selectedControlPoint.getSecurityAwareObject()
          .getPublicKeyHash());

      newOwnerSelected = false;
      selectedControlPoint = null;
      newOwnerButton.setText(NEW_OWNER);
      updateCurrentDictionaryObject();
      updateLayout();
    }

  }

  /** Checks for device permission actions */
  public void actionPerformedPermissions(ActionEvent e)
  {
    // show all permissions
    if (currentDictionaryObject != null && showPermissionsButton.getID().equals(e.getActionCommand()) &&
      !showPermissionsButton.getID().equals(currentInfoCommand))
    {
      currentInfoCommand = showPermissionsButton.getID();
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // select specific permission (for ACL editing)
    if (currentDictionaryObject != null && newACLEntryAccessSelected)
    {
      boolean found = false;
      int i = 0;
      Permission foundPermission = null;
      Vector permissions = currentDictionaryObject.getSecurityAwareCPDeviceObject().getPermissions();
      while (!found && i < permissions.size())
      {
        foundPermission = (Permission)permissions.elementAt(i);
        found = foundPermission.getACLEntry().equals(e.getActionCommand());
        if (!found)
        {
          i++;
        }
      }
      // check for any permission
      if (!found)
      {
        foundPermission = currentDictionaryObject.getSecurityAwareCPDeviceObject().getAnyPermission();
        found = foundPermission.getACLEntry().equals(e.getActionCommand());
      }
      // permission selected for ACL entry editing
      if (found)
      {
        // get permission
        selectedPermission = foundPermission;
        if (newACLEntryAccessSelected)
        {
          newACLEntryAccessButton.setText(foundPermission.getUIName());
          newACLEntryAccessSelected = false;
        }
        updateCurrentDictionaryObject();
        updateLayout();
      }
    }
  }

  /** Checks for device ACL actions */
  public void actionPerformedACL(ActionEvent e)
  {
    // show ACL overview
    if (currentDictionaryObject != null && showACLButton.getID().equals(e.getActionCommand()) &&
      !showACLButton.getID().equals(currentInfoCommand))
    {
      currentInfoCommand = showACLButton.getID();
      newACLEntrySubjectSelected = false;
      newACLEntrySubjectButton.setText(NEW_ENTRY);
      newACLEntryAccessSelected = false;
      newACLEntryAccessButton.setText(PERMISSION);
      selectedControlPoint = null;
      selectedPermission = null;

      buildACLButtonList();
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // check for existing ACL entries
    for (int i = 0; i < aclButtonList.size(); i++)
    {
      ACLEntryButtons entryButtons = (ACLEntryButtons)aclButtonList.elementAt(i);
      // check for removal
      if (removeACLEntryIndex == -1 && entryButtons.getRemoveButton().getID().equals(e.getActionCommand()))
      {
        try
        {
          String id = entryButtons.getRemoveButton().getID();
          String indexString = id.substring(0, id.indexOf("_"));
          removeACLEntryIndex = Integer.parseInt(indexString);
        } catch (Exception ex)
        {
          removeACLEntryIndex = -1;
        }
      }
      /*
       * // check for subject change if
       * (entryButtons.getSubjectButton().getID().equals(e.getActionCommand())) {
       * changeACLEntrySubjectSelected = true; changeACLEntryAccessSelected = false;
       * entryButtons.getSubjectButton().setText(CHOOSE_CONTROLPOINT);
       * updateCurrentDictionaryObject(); updateLayout(); }
       */
    }
    // new ACL entry subject selected
    if (currentDictionaryObject != null && newACLEntrySubjectButton.getID().equals(e.getActionCommand()) &&
      !newACLEntrySubjectSelected)
    {
      newACLEntrySubjectSelected = true;
      newACLEntryAccessSelected = false;
      newACLEntrySubjectButton.setText(CHOOSE_CONTROLPOINT);
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // new ACL entry access selected
    if (currentDictionaryObject != null && newACLEntryAccessButton.getID().equals(e.getActionCommand()) &&
      !newACLEntryAccessSelected)
    {
      newACLEntrySubjectSelected = false;
      newACLEntryAccessSelected = true;
      newACLEntryAccessButton.setText(CHOOSE_PERMISSION);
      updateCurrentDictionaryObject();
      updateLayout();
    }
    // save ACL entry selected
    if (currentDictionaryObject != null && newACLEntrySaveButton.getID().equals(e.getActionCommand()) &&
      selectedControlPoint != null && selectedPermission != null && pendingACLEntry == null)
    {
      pendingACLEntry =
        new ACLEntry(selectedControlPoint.getSecurityAwareObject(), false, selectedPermission.getACLEntry());

      newACLEntrySubjectSelected = false;
      newACLEntryAccessSelected = false;
      selectedControlPoint = null;
      selectedPermission = null;
      newACLEntrySubjectButton.setText(NEW_ENTRY);
      newACLEntryAccessButton.setText(PERMISSION);
      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  /** Selects a new dictionary object for displaying */
  private void setCurrentDictionaryObject(LocalDictionaryObject dictionaryObject)
  {
    if (dictionaryObject != null)
    {
      currentDictionaryObject = dictionaryObject;
      currentInfoCommand = currentDictionaryObject.getSecurityAwareObject().getSecurityID();
      // set current name button
      for (int i = 0; i < securityConsole.getNamedDeviceCount(); i++)
      {
        if (securityConsole.getNamedDevice(i) == currentDictionaryObject)
        {
          currentNameButton = getNameButton(i);
        }
      }
      // reset deviceSecurityService
      service = null;

      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  /** Thread for plugin specific algorithms */
  public void run()
  {
    while (!terminateThread)
    {
      // add an owner
      if (pendingOwner != null)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // grantOwnership
          CPAction action = service.getCPAction("GrantOwnership");
          if (action != null)
          {
            try
            {
              Argument hashAlgorithmArg = action.getInArgument("HashAlgorithm");
              hashAlgorithmArg.setValue(pendingOwner.getHashAlgorithm());
              Argument keyHashArg = action.getInArgument("KeyHash");
              keyHashArg.setValue(pendingOwner.getPublicKeyHashBase64());

              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              // no error, add new owner
              currentDictionaryObject.getSecurityAwareCPDeviceObject().addOwner(pendingOwner);
              buildOwnerButtonList();
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          pendingOwner = null;
        }
      }
      // remove an owner
      if (removeOwner != null)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // revokeOwnership
          CPAction action = service.getCPAction("RevokeOwnership");
          if (action != null)
          {
            try
            {
              Argument hashAlgorithmArg = action.getInArgument("HashAlgorithm");
              hashAlgorithmArg.setValue(removeOwner.getHashAlgorithm());
              Argument keyHashArg = action.getInArgument("KeyHash");
              keyHashArg.setValue(removeOwner.getPublicKeyHashBase64());

              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              // no error, remove owner
              currentDictionaryObject.getSecurityAwareCPDeviceObject().removeOwner(removeOwner);
              buildOwnerButtonList();
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          removeOwner = null;
        }
      }

      // add an ACL entry
      if (pendingACLEntry != null)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // add ACL entry
          CPAction action = service.getCPAction("AddACLEntry");
          if (action != null)
          {
            try
            {
              Argument entryArg = action.getInArgument("Entry");
              entryArg.setValue(StringHelper.xmlToEscapedString(pendingACLEntry.toXMLDescription()));

              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              // no error, reread acl
              requestACL = true;
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          pendingACLEntry = null;
        }
      }
      // remove an ACL entry
      if (removeACLEntryIndex != -1)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // remove ACL entry
          CPAction action = service.getCPAction("DeleteACLEntry");
          if (action != null)
          {
            try
            {
              Argument versionArg = action.getInArgument("TargetACLVersion");
              versionArg.setValue(currentDictionaryObject.getSecurityAwareCPDeviceObject().getACLVersion());
              Argument indexArg = action.getInArgument("Index");
              indexArg.setNumericValue(removeACLEntryIndex);

              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              // no error, update ACL and ACL version
              currentDictionaryObject.getSecurityAwareCPDeviceObject().getACL().remove(removeACLEntryIndex);
              Argument newVersionArg = action.getOutArgument("NewACLVersion");
              currentDictionaryObject.getSecurityAwareCPDeviceObject().setACLVersion((String)newVersionArg.getValue());
              buildACLButtonList();
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          removeACLEntryIndex = -1;
        }
      }

      if (requestPermissions)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // retrieve permissions
          CPAction action = service.getCPAction("GetDefinedPermissions");
          if (action != null)
          {
            try
            {
              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              Argument permissionsArg = action.getOutArgument("Permissions");

              String permissionsArgString = permissionsArg.getStringValue();
              PermissionsParser parser = new PermissionsParser(permissionsArgString);
              currentDictionaryObject.getSecurityAwareCPDeviceObject().setPermissions(parser);
            } catch (ActionFailedException afe)
            {
              if (afe.getErrorCode() == 701)
              {
                currentDictionaryObject.getSecurityAwareCPDeviceObject().setPermissionsAccessDenied();
              }

              System.out.println("ERROR: " + afe.getMessage());
              logger.warn(afe.getMessage());
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          // update GUI to reflect read permissions
          buildPermissionButtonList();
        }
        requestPermissions = false;
      }
      if (requestACL)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // System.out.println("Request permissions");
          // retrieve ACL
          CPAction action = service.getCPAction("ReadACL");
          if (action != null)
          {
            try
            {
              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);

              Argument versionArg = action.getOutArgument("Version");
              Argument aclArg = action.getOutArgument("ACL");

              String versionArgString = (String)versionArg.getValue();
              String aclArgString = aclArg.getStringValue();
              ACLParser parser = new ACLParser();
              parser.parse(aclArgString);
              currentDictionaryObject.getSecurityAwareCPDeviceObject().setACL(parser);
              currentDictionaryObject.getSecurityAwareCPDeviceObject().setACLVersion(versionArgString);
            } catch (ActionFailedException afe)
            {
              if (afe.getErrorCode() == 701)
              {
                currentDictionaryObject.getSecurityAwareCPDeviceObject().setACLAccessDenied();
              }

              System.out.println("ERROR: " + afe.getMessage());
              logger.warn(afe.getMessage());
            } catch (Exception ex)
            {
              System.out.println("ERROR: " + ex.getMessage());
              logger.warn(ex.getMessage());
            }
          }
          // update GUI to reflect read ACL
          buildACLButtonList();
        }
        requestACL = false;
      }

      if (requestOwners)
      {
        if (service != null && currentDictionaryObject != null)
        {
          // retrieve owner list
          CPAction action = service.getCPAction("ListOwners");
          if (action != null)
          {
            try
            {
              securityConsole.getSecurityConsoleControlPoint().invokeAction(action);
              Argument ownersArg = action.getOutArgument("Owners");
              String ownersArgString = ownersArg.getStringValue();
              OwnersParser parser = new OwnersParser(ownersArgString);
              currentDictionaryObject.getSecurityAwareCPDeviceObject().setOwners(parser);
            } catch (ActionFailedException afe)
            {
              if (afe.getErrorCode() == 701)
              {
                currentDictionaryObject.getSecurityAwareCPDeviceObject().setPermissionsAccessDenied();
              }

              System.out.println("ERROR: " + afe.getMessage());
              logger.warn(afe.getMessage());
            } catch (Exception ex)
            {
              logger.warn(ex.getMessage());
            }
          }
          // update GUI to reflect read permissions
          buildOwnerButtonList();
        }
        requestOwners = false;
      }
      try
      {
        Thread.sleep(50);
      } catch (Exception ex)
      {
      }
    }
    terminated = true;
  }

  /** Terminates the plugin */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
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
    nameButton.addActionListener(this);

    namedControlPointsButtons.add(nameButton);
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#newSecurityAwareDevice(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void newSecurityAwareCPDevice(LocalDictionaryObject device)
  {
    SmoothButton nameButton =
      new SmoothButton(new Dimension(GUIConstants.OVERVIEW_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        device.getUserDefinedName(),
        device.getSecurityAwareObject().getSecurityID());
    nameButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    namedDevicesButtons.add(nameButton);
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#securityAwareControlPointStatusChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void securityAwareControlPointStatusChange(LocalDictionaryObject controlPoint)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#securityAwareDeviceStatusChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void securityAwareCPDeviceStatusChange(LocalDictionaryObject device)
  {
    if (device == currentDictionaryObject)
    {
      // if device is offline, clear service
      if (!device.isOnline())
      {
        service = null;
        buildOwnerButtonList();
        buildPermissionButtonList();
        buildACLButtonList();
      }
      updateCurrentDictionaryObject();
      updateLayout();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.securityConsole.ISecurityConsoleEvents#localDictionaryNameChange(de.fhg.fokus.magic.upnp.security.helpers.LocalDictionaryObject)
   */
  public void localDictionaryNameChange(LocalDictionaryObject localDictionaryObject)
  {
    buildACLButtonList();
    buildNamedControlPointsButtonList();
    buildNamedDevicesButtonList();

    updateLayout();
  }

  /** Event that the security console user has changed. */
  public void securityConsoleUserChange()
  {
    // clear complete context
    currentDictionaryObject = null;
    currentNameButton = null;
    currentInfoCommand = "";
    pendingACLEntry = null;
    pendingOwner = null;

    buildACLButtonList();
    buildNamedControlPointsButtonList();
    buildNamedDevicesButtonList();

    updateLayout();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()
  {// GEN-BEGIN:initComponents
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
  private void updateCurrentDictionaryObject()
  {
    if (currentDictionaryObject != null)
    {
      // check for initialization
      if (service == null && currentDictionaryObject.isOnline() &&
        currentDictionaryObject.getSecurityAwareCPDeviceObject() != null)
      {
        service =
          currentDictionaryObject.getSecurityAwareCPDeviceObject()
            .getCPDevice()
            .getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);

        if (currentDictionaryObject.getSecurityAwareCPDeviceObject().hasValidOwners())
        {
          buildOwnerButtonList();
        }
        // request permissions if necessary
        if (!currentDictionaryObject.getSecurityAwareCPDeviceObject().hasValidPermissions())
        {
          requestPermissions = true;
        }
        // build list if permissions are known
        if (currentDictionaryObject.getSecurityAwareCPDeviceObject().getPermissions().size() != 0)
        {
          // create list of all permissions
          buildPermissionButtonList();
        }
        // request acl if necessary
        if (!currentDictionaryObject.getSecurityAwareCPDeviceObject().hasValidACL())
        {
          requestACL = true;
        }
      }
      // go to device overview if device is gone offline
      if (!currentDictionaryObject.isOnline() &&
        !currentInfoCommand.equals(currentDictionaryObject.getSecurityAwareObject().getSecurityID()))
      {
        currentInfoCommand = currentDictionaryObject.getSecurityAwareObject().getSecurityID();
      }

      // update name in device overview
      currentNameButton.setText(currentDictionaryObject.getUserDefinedName());

      nameChangeButton.setText(currentDictionaryObject.getUserDefinedName());
      securityIDButton.setText(currentDictionaryObject.getSecurityAwareObject().getSecurityID());
      onlineButton.setText(currentDictionaryObject.isOnline() ? "Online" : "Offline");
      onlineButton.setSelected(currentDictionaryObject.isOnline());
      takeOwnerShipButton.setSelectable(currentDictionaryObject.isOnline() &&
        currentDictionaryObject.getSecurityAwareCPDeviceObject().getOwners().size() == 0);

      infoArea.clearContent();
      if (currentDictionaryObject.isOnline() && currentInfoCommand.equals(showInfoButton.getID()))
      {
        String line = "Informationen: ";
        infoArea.addLine(line);
        // add protocols etc.
        SecurityAwareCPDeviceObject device = currentDictionaryObject.getSecurityAwareCPDeviceObject();
        if (device != null)
        {
          line = "Protokolle: ";
          for (int i = 0; i < device.getProtocols().size(); i++)
          {
            line += (i == 0 ? "" : ", ") + device.getProtocols().elementAt(i);
          }
          infoArea.addLine("  " + line);

          line = "Hash-Algorithmen: ";
          for (int i = 0; i < device.getHashAlgorithms().size(); i++)
          {
            line += (i == 0 ? "" : ", ") + device.getHashAlgorithms().elementAt(i);
          }
          infoArea.addLine("  " + line);

          line = "Verschlüsselung: ";
          for (int i = 0; i < device.getEncryptionAlgorithms().size(); i++)
          {
            line += (i == 0 ? "" : ", ") + device.getEncryptionAlgorithms().elementAt(i);
          }
          infoArea.addLine("  " + line);

          line = "Signatur: ";
          for (int i = 0; i < device.getSigningAlgorithms().size(); i++)
          {
            line += (i == 0 ? "" : ", ") + device.getSigningAlgorithms().elementAt(i);
          }
          infoArea.addLine("  " + line);

          infoArea.setSizeToFitContent();
        }
      }
    }
  }

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    jSecurityContentPanel.removeAll();
    jSecurityPropertyPanel.removeAll();
    jSecurityInfoPanel.removeAll();
    jSecurityContentPanel.invalidate();
    jSecurityPropertyPanel.invalidate();
    jSecurityInfoPanel.invalidate();

    updateLayoutDeviceOverview();
    updateLayoutMisc();
    updateLayoutInfos();
    updateLayoutOwners();
    updateLayoutPermissions();
    updateLayoutACL();

    // hide plugin if there are no devices
    setVisible(securityConsole.getNamedDeviceCount() > 0);

    jSecurityContentPanel.repaint();
    jSecurityPropertyPanel.repaint();
    jSecurityInfoPanel.repaint();
    validateTree();
  }

  /** Redraws the layout of the plugin */
  private void updateLayoutDeviceOverview()
  {
    GridBagConstraints gridBagConstraints;

    // add all known devices to content panel
    int row = 0;
    for (int i = 0; i < securityConsole.getNamedDeviceCount(); i++)
    {
      SmoothButton nameButton = getNameButton(i);

      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = row;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      nameButton.setSelected(currentDictionaryObject != null &&
        nameButton.getID().equals(currentDictionaryObject.getSecurityAwareObject().getSecurityID()));

      jSecurityContentPanel.add(nameButton, gridBagConstraints);
      row++;

      // add additional buttons after selected device
      if (currentNameButton != null && nameButton.getID().equals(currentNameButton.getID()) &&
        currentDictionaryObject != null && currentDictionaryObject.isOnline())
      {
        // infos
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
        showInfoButton.setSelected(currentInfoCommand.equals(showInfoButton.getID()));
        jSecurityContentPanel.add(showInfoButton, gridBagConstraints);
        row++;
        // owners
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
        showOwnersButton.setSelected(currentInfoCommand.equals(showOwnersButton.getID()));
        jSecurityContentPanel.add(showOwnersButton, gridBagConstraints);
        row++;
        // defined permissions
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
        showPermissionsButton.setSelected(currentInfoCommand.equals(showPermissionsButton.getID()));
        jSecurityContentPanel.add(showPermissionsButton, gridBagConstraints);
        row++;
        // ACL
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
        showACLButton.setSelected(currentInfoCommand.equals(showACLButton.getID()));
        jSecurityContentPanel.add(showACLButton, gridBagConstraints);
        row++;
      }
    }
  }

  /** Redraws the layout for misc infos */
  private void updateLayoutMisc()
  {
    // show selected device if available
    if (currentDictionaryObject != null &&
      currentInfoCommand.equals(currentDictionaryObject.getSecurityAwareObject().getSecurityID()))
    {
      GridBagConstraints gridBagConstraints;
      // show online button
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      jSecurityPropertyPanel.add(onlineButton, gridBagConstraints);

      // show take ownership button if device is online and unowned
      if (currentDictionaryObject.isOnline() &&
        currentDictionaryObject.getSecurityAwareCPDeviceObject().getOwners().size() == 0)
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        jSecurityPropertyPanel.add(takeOwnerShipButton, gridBagConstraints);
      }
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
  }

  /** Redraws the layout for infos */
  private void updateLayoutInfos()
  {
    if (currentDictionaryObject != null && currentInfoCommand.equals(showInfoButton.getID()))
    {
      GridBagConstraints gridBagConstraints;
      // show info area
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      jSecurityPropertyPanel.add(infoArea, gridBagConstraints);
    }
  }

  /** Redraws the layout for owner entries */
  private void updateLayoutOwners()
  {
    if (currentDictionaryObject != null && currentInfoCommand.equals(showOwnersButton.getID()))
    {
      GridBagConstraints gridBagConstraints;
      // show buttons for all owners
      for (int i = 0; i < ownerButtonList.size(); i++)
      {
        SmoothButton ownerButton = (SmoothButton)ownerButtonList.elementAt(i);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        jSecurityPropertyPanel.add(ownerButton, gridBagConstraints);

        // if not local security console, show remove button
        if (!ownerButton.getID().equals(securityConsole.getSecurityAwareControlPoint().getSecurityID()))
        {
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
          jSecurityPropertyPanel.add((SmoothButton)ownerDeleteButtonList.elementAt(i), gridBagConstraints);
        }
      }

      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      // keep gap to known owners
      gridBagConstraints.insets = new Insets(2 + 15, 5, 2, 5);
      newOwnerButton.setSelected(newOwnerSelected);

      jSecurityPropertyPanel.add(newOwnerButton, gridBagConstraints);

      // save is only visible if new owner is valid
      if (!(newOwnerButton.getText().equals(NEW_OWNER) || newOwnerButton.getText().equals(CHOOSE_OWNER)))
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
        newACLEntryAccessButton.setSelected(newACLEntryAccessSelected);

        jSecurityPropertyPanel.add(newOwnerSaveButton, gridBagConstraints);
      }
      // if new owner is selected, show available control points on right side
      if (newOwnerSelected)
      {
        for (int i = 0; i < namedControlPointsButtons.size(); i++)
        {
          SmoothButton button = (SmoothButton)namedControlPointsButtons.elementAt(i);
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          jSecurityInfoPanel.add(button, gridBagConstraints);
        }
      }
    }
  }

  /** Redraws the layout for permission entries */
  private void updateLayoutPermissions()
  {
    if (currentDictionaryObject != null && currentInfoCommand.equals(showPermissionsButton.getID()))
    {
      GridBagConstraints gridBagConstraints;
      // show access denied if permissions can not be retrieved
      if (currentDictionaryObject.getSecurityAwareCPDeviceObject().hasPermissionsAccessDenied())
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        jSecurityPropertyPanel.add(accessDeniedButton, gridBagConstraints);
      } else
      {
        // show load button if nothing is known at this moment
        if (!currentDictionaryObject.getSecurityAwareCPDeviceObject().hasValidPermissions())
        {
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          jSecurityPropertyPanel.add(loadingButton, gridBagConstraints);
        } else
        {
          // show buttons for all permissions
          for (int i = 0; i < permissionButtonList.size(); i++)
          {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(2, 5, 2, 5);
            ((SmoothButton)permissionButtonList.elementAt(i)).setSelectable(false);

            jSecurityPropertyPanel.add((SmoothButton)permissionButtonList.elementAt(i), gridBagConstraints);
          }
        }
      }
    }
  }

  /** Redraws the layout for ACL entries */
  private void updateLayoutACL()
  {
    if (currentDictionaryObject != null && currentInfoCommand.equals(showACLButton.getID()))
    {
      GridBagConstraints gridBagConstraints;
      if (currentDictionaryObject.getSecurityAwareCPDeviceObject().hasACLAccessDenied())
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        jSecurityPropertyPanel.add(accessDeniedButton, gridBagConstraints);
      } else
      {
        // show load button if nothing is known at this moment
        if (!currentDictionaryObject.getSecurityAwareCPDeviceObject().hasValidACL())
        {
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          jSecurityPropertyPanel.add(loadingButton, gridBagConstraints);
        } else
        {
          // show free entry value if acl is valid
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          jSecurityPropertyPanel.add(freeACLEntriesButton, gridBagConstraints);
          // show buttons for all ACL entries
          for (int i = 0; i < aclButtonList.size(); i++)
          {
            ACLEntryButtons entryButtons = (ACLEntryButtons)aclButtonList.elementAt(i);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(2 + (i == 0 ? 15 : 0), 5, 2, 5);
            jSecurityPropertyPanel.add(entryButtons.getSubjectButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
            jSecurityPropertyPanel.add(entryButtons.getPermissionButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);

            jSecurityPropertyPanel.add(entryButtons.getRemoveButton(), gridBagConstraints);
          }
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          // keep gap to defined ACLs
          gridBagConstraints.insets = new Insets(2 + 15, 5, 2, 5);
          newACLEntrySubjectButton.setSelected(newACLEntrySubjectSelected);

          jSecurityPropertyPanel.add(newACLEntrySubjectButton, gridBagConstraints);

          // access is only visible if control point is valid
          if (!(newACLEntrySubjectButton.getText().equals(NEW_ENTRY) || newACLEntrySubjectButton.getText()
            .equals(CHOOSE_CONTROLPOINT)))
          {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);
            newACLEntryAccessButton.setSelected(newACLEntryAccessSelected);

            jSecurityPropertyPanel.add(newACLEntryAccessButton, gridBagConstraints);

            // save is only visible if access is also valid
            if (!(newACLEntryAccessButton.getText().equals(PERMISSION) || newACLEntryAccessButton.getText()
              .equals(CHOOSE_PERMISSION)))
            {
              gridBagConstraints = new GridBagConstraints();
              gridBagConstraints.gridx = 0;
              gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
              gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
              gridBagConstraints.insets = new Insets(2, 5 + GUIConstants.BUTTON_INDENTATION, 2, 5);

              jSecurityPropertyPanel.add(newACLEntrySaveButton, gridBagConstraints);
            }
          }
        }
      }

      // if acl entry subject is selected, show available control points on right side
      if (newACLEntrySubjectSelected || changeACLEntrySubjectSelected)
      {
        for (int i = 0; i < namedControlPointsButtons.size(); i++)
        {
          SmoothButton button = (SmoothButton)namedControlPointsButtons.elementAt(i);
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          jSecurityInfoPanel.add(button, gridBagConstraints);
        }
      }
      // if new acl entry acess is selected, show available permissions on right side
      if (newACLEntryAccessSelected)
      {
        // show buttons for all permissions
        for (int i = 0; i < permissionButtonList.size(); i++)
        {
          SmoothButton button = (SmoothButton)permissionButtonList.elementAt(i);
          gridBagConstraints = new GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
          gridBagConstraints.insets = new Insets(2, 5, 2, 5);
          button.setSelectable(true);

          jSecurityInfoPanel.add(button, gridBagConstraints);
        }
      }
    }
  }

  private SmoothButton getNameButton(int index)
  {
    if (index >= 0 && index < namedDevicesButtons.size())
    {
      return (SmoothButton)namedDevicesButtons.elementAt(index);
    }

    return null;
  }

  /** Creates a vector with buttons for all known secured devices */
  private void buildNamedDevicesButtonList()
  {
    namedDevicesButtons.clear();
    // create name buttons for all known devices
    for (int i = 0; i < securityConsole.getNamedDeviceCount(); i++)
    {
      LocalDictionaryObject dictionaryObject = securityConsole.getNamedDevice(i);
      SmoothButton nameButton =
        new SmoothButton(new Dimension(GUIConstants.OVERVIEW_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
          12,
          dictionaryObject.getUserDefinedName(),
          dictionaryObject.getSecurityAwareObject().getSecurityID());
      nameButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
      nameButton.addActionListener(this);

      namedDevicesButtons.add(nameButton);
    }
  }

  /** Creates a vector with buttons for all known security aware control points */
  private void buildNamedControlPointsButtonList()
  {
    namedControlPointsButtons.clear();
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

      namedControlPointsButtons.add(nameButton);
    }
  }

  /** Creates a vector with buttons for all defined permissions */
  private void buildPermissionButtonList()
  {
    permissionButtonList.clear();
    if (currentDictionaryObject != null &&
      currentDictionaryObject.getSecurityAwareCPDeviceObject().getPermissions().size() > 0)
    {
      DeviceTranslations deviceTranslation =
        deviceTranslationManager.getDeviceTranslations(currentDictionaryObject.getSecurityAwareCPDeviceObject()
          .getCPDevice());

      // add all rights button
      Permission anyPermission = currentDictionaryObject.getSecurityAwareCPDeviceObject().getAnyPermission();

      // translate anyPermission
      anyPermission.setUIName(GUIControlConstants.ANY_RIGHTS);
      anyPermission.setShortDescription(GUIControlConstants.ANY_RIGHTS_SHORT_DESCRIPTION);

      SmoothButton button =
        new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
          12,
          anyPermission.getUIName(),
          anyPermission.getACLEntry());
      button.setAutomaticTooltip(false);
      button.setToolTipText(anyPermission.getShortDescription());
      button.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
      button.addActionListener(this);
      button.setSelectable(false);
      button.setDisabledButtonColor(button.getButtonColor());

      permissionButtonList.add(button);
      // add all read permissions
      for (int i = 0; i < currentDictionaryObject.getSecurityAwareCPDeviceObject().getPermissions().size(); i++)
      {
        Permission permission =
          (Permission)currentDictionaryObject.getSecurityAwareCPDeviceObject().getPermissions().elementAt(i);
        button =
          new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            permission.getUIName(),
            permission.getACLEntry());
        button.setAutomaticTooltip(false);
        button.setToolTipText(permission.getShortDescription());
        button.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        button.addActionListener(this);
        button.setSelectable(false);
        button.setDisabledButtonColor(button.getButtonColor());

        // try to translate permission
        deviceTranslation.setTranslationForButton(button, permission.getUIName());
        deviceTranslation.setTranslationForTooltip(button, permission.getShortDescription());

        permissionButtonList.add(button);
      }
    }
    if (currentInfoCommand.equals(showPermissionsButton.getID()))
    {
      updateLayout();
    }
  }

  /** Creates a vector with buttons for all owners */
  private void buildOwnerButtonList()
  {
    ownerButtonList.clear();
    ownerDeleteButtonList.clear();
    if (currentDictionaryObject != null &&
      currentDictionaryObject.getSecurityAwareCPDeviceObject().getOwners().size() > 0)
    {
      Vector owners = currentDictionaryObject.getSecurityAwareCPDeviceObject().getOwners();
      for (int i = 0; i < owners.size(); i++)
      {
        SecurityAwareObject owner = (SecurityAwareObject)owners.elementAt(i);

        String securityID = owner.getSecurityID();
        String name = securityID;

        // check if securityID is associated with this security console
        if (securityID.equals(securityConsole.getSecurityAwareControlPoint().getSecurityID()))
        {
          name = securityConsole.getUserDefinedName();
        }

        // check if securityID is known control point
        String userDefinedName = securityConsole.getUserDefinedNameForControlPoint(owner);
        if (userDefinedName != null)
        {
          name = userDefinedName;
        }

        SmoothButton button =
          new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            name,
            owner.getSecurityID());
        button.setSelectable(false);
        button.setDisabledButtonColor(button.getButtonColor());
        ownerButtonList.add(button);

        button =
          new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            REMOVE,
            "remove_" + owner.getPublicKeyHashBase64());
        button.setButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
        button.setActiveButtonColor(Color.white);
        button.addActionListener(this);
        ownerDeleteButtonList.add(button);
      }
    }
    if (currentInfoCommand.equals(showOwnersButton.getID()))
    {
      updateLayout();
    }
  }

  private void updateACLSize()
  {
    freeACLEntriesButton.setValue("");
    if (service != null)
    {
      CPStateVariable freeACLEntries = service.getCPStateVariable("FreeACLSize");
      if (freeACLEntries != null)
      {
        try
        {
          freeACLEntriesButton.setValue(Long.toString(freeACLEntries.getNumericValue()));
        } catch (Exception ex)
        {
        }
      }
    }
  }

  /** Creates a vector with buttons for all ACL entries */
  private void buildACLButtonList()
  {
    updateACLSize();

    aclButtonList.clear();
    if (currentDictionaryObject != null && currentDictionaryObject.getSecurityAwareCPDeviceObject() != null &&
      currentDictionaryObject.getSecurityAwareCPDeviceObject().getACL().size() > 0)
    {
      for (int i = 0; i < currentDictionaryObject.getSecurityAwareCPDeviceObject().getACL().size(); i++)
      {
        ACLEntry entry = (ACLEntry)currentDictionaryObject.getSecurityAwareCPDeviceObject().getACL().elementAt(i);
        ACLEntryButtons entryButtons =
          new ACLEntryButtons(securityConsole, deviceTranslationManager, currentDictionaryObject, entry, i);
        entryButtons.addActionListener(this);

        aclButtonList.add(entryButtons);
      }
    }
    if (currentInfoCommand.equals(showACLButton.getID()))
    {
      updateLayout();
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

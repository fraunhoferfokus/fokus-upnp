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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControlConstants;
import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This class holds all buttons for one ACL entry.
 * 
 * 
 * @author Alexander Koenig
 */
public class ACLEntryButtons
{

  // private ACLEntry entry;
  /** Button that holds the subject of the ACL entry */
  private SmoothButton subjectButton;

  /** Button that holds the permission of the ACL entry */
  private SmoothButton permissionButton;

  /** Button for entry removal */
  private SmoothButton removeButton;

  /** Creates a new instance of ACLEntryButtons */
  public ACLEntryButtons(SecurityConsoleEntity securityConsole,
    IDeviceTranslationManager deviceTranslationManager,
    LocalDictionaryObject localDictionaryObject,
    ACLEntry entry,
    int entryIndex)
  {
    // try to get user-defined name for ACL subject
    String userDefinedName = securityConsole.getUserDefinedNameForControlPoint(entry.getSubject());
    // if not known, show securityID
    if (userDefinedName == null)
    {
      userDefinedName = entry.getSubject().getSecurityID();
    }

    subjectButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        userDefinedName,
        Integer.toString(entryIndex) + "_" + entry.getSubject().getSecurityID());
    subjectButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    subjectButton.setSelectable(false);
    subjectButton.setDisabledButtonColor(subjectButton.getButtonColor());

    DeviceTranslations deviceTranslation =
      deviceTranslationManager.getDeviceTranslations(localDictionaryObject.getSecurityAwareCPDeviceObject()
        .getCPDevice());

    // try to get permission for the acl entry
    Permission aclEntryPermission = null;
    if (localDictionaryObject.getSecurityAwareCPDeviceObject() != null)
    {
      aclEntryPermission =
        localDictionaryObject.getSecurityAwareCPDeviceObject().getPermission(entry.getAccessPermission());
    }

    // show access permission
    String permissionUIName = entry.getAccessPermission();
    if (aclEntryPermission != null)
    {
      permissionUIName = aclEntryPermission.getUIName();
    }
    // create permission button
    permissionButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        permissionUIName,
        Integer.toString(entryIndex) + "_" + entry.getAccessPermission());
    permissionButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    permissionButton.setSelectable(false);
    permissionButton.setDisabledButtonColor(permissionButton.getButtonColor());

    // try to translate permission
    if (aclEntryPermission != null)
    {
      deviceTranslation.setTranslationForButton(permissionButton, aclEntryPermission.getUIName());
      deviceTranslation.setTranslationForTooltip(permissionButton, aclEntryPermission.getShortDescription());
    }
    // create remove button
    removeButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_INDENT_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        GUIControlConstants.REMOVE,
        Integer.toString(entryIndex) + "_" + "remove");
    removeButton.setButtonColor(GUIConstants.SAVE_ACTIVE_BUTTON_COLOR);
    removeButton.setActiveButtonColor(Color.white);
  }

  public void addActionListener(ActionListener listener)
  {
    subjectButton.addActionListener(listener);
    permissionButton.addActionListener(listener);
    removeButton.addActionListener(listener);
  }

  public SmoothButton getSubjectButton()
  {
    return subjectButton;
  }

  public SmoothButton getPermissionButton()
  {
    return permissionButton;
  }

  public SmoothButton getRemoveButton()
  {
    return removeButton;
  }

}

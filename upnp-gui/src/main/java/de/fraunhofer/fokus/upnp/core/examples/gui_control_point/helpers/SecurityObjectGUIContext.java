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

import java.awt.Dimension;
import java.awt.event.ActionListener;

import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothArea;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This class holds one local dictionary entry (device or control point) and its associated buttons.
 * 
 * 
 * @author Alexander Koenig
 */
public class SecurityObjectGUIContext
{

  private SecurityConsoleEntity securityConsole;

  private LocalDictionaryObject localDictionaryObject;

  private SmoothButton          nameButton;

  private SmoothButton          nameChangeButton;

  private SmoothButton          securityIDButton;

  private SmoothButton          onlineButton;

  private SmoothButton          takeOwnerShipButton;

  private SmoothButton          showInfosButton;

  private SmoothButton          showPermissionsButton;

  private SmoothArea            infoArea;

  /** Creates a new instance of DeviceGUIContext */
  public SecurityObjectGUIContext(SecurityConsoleEntity securityConsole, LocalDictionaryObject localDictionaryObject)
  {
    this.securityConsole = securityConsole;
    this.localDictionaryObject = localDictionaryObject;

    // create name button
    nameButton =
      new SmoothButton(new Dimension(GUIConstants.ACTION_BUTTON_WIDTH, 30),
        12,
        localDictionaryObject.getUserDefinedName(),
        localDictionaryObject.getSecurityAwareObject().getSecurityID());
    nameButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // create name button
    nameChangeButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30),
        12,
        localDictionaryObject.getUserDefinedName(),
        localDictionaryObject.getSecurityAwareObject().getSecurityID() + "_Change");
    nameChangeButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // securityID button
    securityIDButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30),
        12,
        localDictionaryObject.getSecurityAwareObject().getSecurityID(),
        null);
    securityIDButton.setSelectable(false);
    securityIDButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // online button
    onlineButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30), 12, localDictionaryObject.isOnline()
        ? "Online" : "Offline", null);
    onlineButton.setSelectable(false);
    onlineButton.setSelected(localDictionaryObject.isOnline());
    onlineButton.setButtonColor(ButtonConstants.DISABLED_BUTTON_COLOR);
    onlineButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // takeOwnership button
    takeOwnerShipButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30), 12, "In Besitz nehmen", "takeOwnership");
    takeOwnerShipButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // show infos button
    showInfosButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30),
        12,
        "Allgemeine Informationen",
        "showInfos");
    showInfosButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // show permissions button
    showPermissionsButton =
      new SmoothButton(new Dimension(GUIConstants.PROPERTY_BUTTON_WIDTH, 30),
        12,
        "Definierte Rechte",
        "showPermissions");
    showPermissionsButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    // info area
    infoArea = new SmoothArea(new Dimension(300, 300), 12, null);
    infoArea.setSelectable(false);
    infoArea.setSelected(false);
  }

  public void addActionListener(ActionListener listener)
  {
    nameButton.addActionListener(listener);
    nameChangeButton.addActionListener(listener);
    takeOwnerShipButton.addActionListener(listener);
    showInfosButton.addActionListener(listener);
    showPermissionsButton.addActionListener(listener);
  }

  public SmoothButton getNameButton()
  {
    return nameButton;
  }

  public SmoothButton getNameChangeButton()
  {
    return nameChangeButton;
  }

  public SmoothButton getSecurityIDButton()
  {
    return securityIDButton;
  }

  public SmoothButton getOnlineButton()
  {
    return onlineButton;
  }

  public SmoothButton getTakeOwnershipButton()
  {
    return takeOwnerShipButton;
  }

  public SmoothButton getShowInfosButton()
  {
    return showInfosButton;
  }

  public SmoothButton getShowPermissionsButton()
  {
    return showPermissionsButton;
  }

  public SmoothArea getInfoArea()
  {
    return infoArea;
  }

  /** Retrieves the security aware object associated with this context */
  public SecurityAwareObject getSecurityAwareObject()
  {
    return localDictionaryObject.getSecurityAwareObject();
  }

  /** Retrieves the security aware device associated with this context */
  public SecurityAwareCPDeviceObject getSecurityAwareCPDevice()
  {
    return localDictionaryObject.getSecurityAwareCPDeviceObject();
  }

  /** Retrieves the local dictionary object associated with this context */
  public LocalDictionaryObject getLocalDictionaryObject()
  {
    return localDictionaryObject;
  }

  public boolean equals(LocalDictionaryObject object)
  {
    return getSecurityAwareObject().equals(object.getSecurityAwareObject());
  }

  /** Updates all buttons to reflect the current object state */
  public void updateContext()
  {
    nameButton.setText(localDictionaryObject.getUserDefinedName());
    nameChangeButton.setText(localDictionaryObject.getUserDefinedName());
    onlineButton.setText(localDictionaryObject.isOnline() ? "Online" : "Offline");
    onlineButton.setSelected(localDictionaryObject.isOnline());
    takeOwnerShipButton.setSelectable(localDictionaryObject.isOnline() && getSecurityAwareCPDevice() != null &&
      getSecurityAwareCPDevice().getOwners().size() == 0);

    infoArea.clearContent();
    if (localDictionaryObject.isOnline())
    {
      // add protocols etc.
      SecurityAwareCPDeviceObject device = localDictionaryObject.getSecurityAwareCPDeviceObject();
      if (device != null)
      {
        String line = "Protokolle: ";
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

        line = "Verschl�sselung: ";
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

        line = "Besitzer: ";
        for (int i = 0; i < device.getOwners().size(); i++)
        {
          SecurityAwareObject owner = (SecurityAwareObject)device.getOwners().elementAt(i);
          String securityID = owner.getSecurityID();
          String name = securityID;
          // check if securityID is associated with a known DictionaryObject or this security
          // console
          if (securityID.equals(securityConsole.getSecurityAwareControlPoint().getSecurityID()))
          {
            name = securityConsole.getUserDefinedName();
          }

          line += (i == 0 ? "" : ", ") + name;
        }
        infoArea.addLine("  " + line);

      }
    }

  }

}

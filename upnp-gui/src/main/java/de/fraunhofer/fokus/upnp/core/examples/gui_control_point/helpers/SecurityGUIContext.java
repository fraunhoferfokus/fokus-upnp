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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControl;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This is the main class for device security. It contains the context objects for all known
 * security aware devices and control points.
 * 
 * @author Alexander Koenig
 */
public class SecurityGUIContext
{

  private GUIControl   guiControl;

  private SmoothButton securityButton;

  private SmoothButton securityOverviewButton;

  private SmoothButton securityIDButton;

  private SmoothButton securityConsoleNameButton;

  private SmoothButton securityAwareDevicesButton;

  private SmoothButton securityAwareControlPointsButton;

  // private ActionListener actionListener = null;

  /** Creates a new instance of DeviceGUIContext */
  public SecurityGUIContext(GUIControl control)
  {

    guiControl = control;
    // create name button
    securityButton =
      new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, 40),
        "Sicherheitseinstellungen",
        GUIControl.GO_TO_SECURITY);
    securityButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    securityButton.setSelectable(false);

    securityOverviewButton = new SmoothButton(new Dimension(50, 50), 12, "", GUIControl.GO_TO_SECURITY);
    securityOverviewButton.setAutomaticTooltip(false);
    securityOverviewButton.setToolTipText("Sicherheitseinstellungen");
    securityOverviewButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    // use working directory of GUIControl
    String workingDirectory =
      FileHelper.toValidDirectoryName(guiControl.getStartupConfiguration().getWorkingDirectory());

    Image securityImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "lock.gif");
    if (securityImage != null && securityImage.getWidth(null) != 0)
    {
      securityOverviewButton.setIconImage(securityImage);
    }

    // create ID button
    securityIDButton =
      new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        getSecurityConsole().getSecurityAwareControlPoint().getSecurityID(),
        null);
    securityIDButton.setSelectable(false);

    // create console name button
    securityConsoleNameButton =
      new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        getSecurityConsole().getUserDefinedName(),
        getSecurityConsole().getSecurityAwareControlPoint().getSecurityID());
    securityConsoleNameButton.setAutomaticTooltip(false);

    securityAwareControlPointsButton =
      new SmoothButton(new Dimension(GUIConstants.SERVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Kontrollpunkte",
        GUIControl.GO_TO_SECURITY_CONTROL_POINTS);

    securityAwareDevicesButton =
      new SmoothButton(new Dimension(GUIConstants.SERVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Geräte",
        GUIControl.GO_TO_SECURITY_DEVICES);
  }

  public void addActionListener(ActionListener listener)
  {
    // actionListener = listener;
    securityOverviewButton.addActionListener(listener);
    securityConsoleNameButton.addActionListener(listener);
    securityAwareControlPointsButton.addActionListener(listener);
    securityAwareDevicesButton.addActionListener(listener);
  }

  public SmoothButton getSecurityButton()
  {
    return securityButton;
  }

  public SmoothButton getSecurityOverviewButton()
  {
    return securityOverviewButton;
  }

  public SmoothButton getSecurityIDButton()
  {
    return securityIDButton;
  }

  public SmoothButton getSecurityConsoleNameButton()
  {
    return securityConsoleNameButton;
  }

  public SecurityConsoleEntity getSecurityConsole()
  {
    return guiControl.getSecurityConsole();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public SmoothButton getSecurityAwareDevicesButton()
  {
    return securityAwareDevicesButton;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Control point management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public SmoothButton getSecurityAwareControlPointsButton()
  {
    return securityAwareControlPointsButton;
  }

}

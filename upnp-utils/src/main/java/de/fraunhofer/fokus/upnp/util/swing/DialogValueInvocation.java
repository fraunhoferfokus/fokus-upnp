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
package de.fraunhofer.fokus.upnp.util.swing;

import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.JFrame;

/**
 * This class provides static methods to request user input in an extra dialog.
 * 
 * @author Alexander Koenig
 */
public class DialogValueInvocation
{

  /**
   * Requests a string value from the user.
   * 
   * @param parent
   *          The parent frame
   * @param title
   *          The title for the dialog
   * @param initialValue
   *          The initial value
   * 
   * @return The invoked value or null
   */
  public static String getInvokedString(JFrame parent, String title, String initialValue)
  {
    // open dialog to change value
    JPanelDialog panelDialog = new JPanelDialog(parent, true);
    panelDialog.setTitle(title);

    JStringEncapsulationPanel stringPanel = new JStringEncapsulationPanel(initialValue, panelDialog);
    panelDialog.addPanel(stringPanel, 5);
    // center dialog
    panelDialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - panelDialog.getSize().width) / 2,
      (Toolkit.getDefaultToolkit().getScreenSize().height - panelDialog.getSize().height) / 2);
    panelDialog.setVisible(true);
    if (panelDialog.isValidResult())
    {
      return stringPanel.getValue();
    }
    return null;
  }

  /**
   * Requests a value from a combobox from the user.
   * 
   * @param parent
   *          The parent frame
   * @param title
   *          The title for the dialog
   * @param list
   *          The list of possible values
   * 
   * @return The index of the invoked value or -1
   */
  public static int getInvokedItemIndex(JFrame parent, String title, Vector list)
  {
    // open dialog to change value
    JPanelDialog panelDialog = new JPanelDialog(parent, true);
    panelDialog.setTitle(title);

    JComboBoxEncapsulationPanel comboBoxEncapsulationPanel = new JComboBoxEncapsulationPanel(list);
    panelDialog.addPanel(comboBoxEncapsulationPanel, 5);
    // center dialog
    panelDialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - panelDialog.getSize().width) / 2,
      (Toolkit.getDefaultToolkit().getScreenSize().height - panelDialog.getSize().height) / 2);
    panelDialog.setVisible(true);
    if (panelDialog.isValidResult())
    {
      return comboBoxEncapsulationPanel.getIndex();
    }
    return -1;
  }

}

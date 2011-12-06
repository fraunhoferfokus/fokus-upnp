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

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * This panel allows the invocation of a combo box value.
 * 
 * @author Alexander Koenig
 * 
 */
public class JComboBoxEncapsulationPanel extends JPanel
{

  private static final long serialVersionUID = 1L;

  protected JComboBox       jComboBox;

  public JComboBoxEncapsulationPanel(Vector list)
  {
    setLayout(new BorderLayout());

    jComboBox = new JComboBox(list);
    add(jComboBox, BorderLayout.NORTH);
  }

  /**
   * Returns the value, which is shown by the component.
   * 
   * @return Value, shown by component.
   */
  public String getValue()
  {
    return jComboBox.getSelectedItem().toString();
  }

  /**
   * Returns the value, which is shown by the component.
   * 
   * @return Value, shown by component.
   */
  public int getIndex()
  {
    return jComboBox.getSelectedIndex();
  }

}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This panel allows the invocation of a string value.
 * 
 * @author Alexander Koenig
 * 
 */
public class JStringEncapsulationPanel extends JPanel
{

  private static final long         serialVersionUID = 1L;

  private JStringEncapsulationPanel parentPanel      = this;

  protected JTextField              jTextField;

  protected ActionListener          actionListener   = null;

  public JStringEncapsulationPanel(String initialValue)
  {
    this(initialValue, null);
  }

  public JStringEncapsulationPanel(String initialValue, ActionListener listener)
  {
    setLayout(new BorderLayout());
    this.actionListener = listener;

    jTextField = new JTextField(initialValue);
    add(jTextField, BorderLayout.NORTH);
    jTextField.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == 10 && actionListener != null)
        {
          actionListener.actionPerformed(new ActionEvent(parentPanel, ActionEvent.ACTION_PERFORMED, "Enter"));
        }
        if (e.getKeyCode() == 27 && actionListener != null)
        {
          actionListener.actionPerformed(new ActionEvent(parentPanel, ActionEvent.ACTION_PERFORMED, "Exit"));
        }
      }
    });
  }

  /**
   * Returns the value, which is shown by the component.
   * 
   * @return Value, shown by component.
   */
  public String getValue()
  {
    return jTextField.getText();
  }

}

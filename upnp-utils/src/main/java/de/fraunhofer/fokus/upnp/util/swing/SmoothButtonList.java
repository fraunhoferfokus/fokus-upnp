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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * 
 * @author Alexander Koenig
 */
public class SmoothButtonList implements ActionListener
{

  private Vector       buttonList       = null;

  private SmoothButton upButton;

  private SmoothButton downButton;

  private int          firstButtonIndex = 0;

  /** Creates a new instance of SmoothButtonList */
  public SmoothButtonList(JPanel sizePanel, JPanel contentPanel, Vector buttons)
  {
    upButton = new SmoothButton(new Dimension(50, 30), 12, "Nach oben", "up");
    upButton.addActionListener(this);
    downButton = new SmoothButton(new Dimension(50, 30), 12, "Nach unten", "down");
    downButton.addActionListener(this);
  }

  /** Sets the action listener for all buttons except up and down */
  public void addActionListener(ActionListener listener)
  {
    if (buttonList != null)
    {
      for (int i = 0; i < buttonList.size(); i++)
      {
        ((SmoothButton)buttonList.elementAt(i)).addActionListener(listener);
      }
    }
  }

  /** This method is used for evaluating buttons events */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(upButton.getID()) && firstButtonIndex > 0)
    {
      firstButtonIndex--;
    }
    if (e.getActionCommand().equals(upButton.getID()) && firstButtonIndex > 0)
    {
      firstButtonIndex--;
    }
  }

  public void updateLayout()
  {

  }

}

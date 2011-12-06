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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_control_point;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;

/**
 * @author tje, Alexander Koenig
 * 
 */
public class PanelContainer extends JPanel
{

  private static final long  serialVersionUID = 1L;

  private DefaultListModel   listModel        = new DefaultListModel();

  private JList              resultList       = new JList(listModel);

  private JLabel             headlineLabel    = new JLabel();

  private final AVGUIControl parent;

  public PanelContainer(AVGUIControl parent)
  {
    this.parent = parent;

    headlineLabel.setText("Container");
    resultList.addMouseListener(new ContainerSelectionListener());

    BoxLayout layoutFrame = new BoxLayout(this, BoxLayout.Y_AXIS);
    this.setLayout(layoutFrame);

    add(headlineLabel);
    add(new JScrollPane(resultList));
  }

  /** Updates the list with child containers */
  public void setContainerList(DIDLContainer[] container)
  {
    listModel.clear();
    // add element for going to parent
    if (parent.getCurrentServer() != null &&
      (container == null || container.length == 0 || !container[0].getParentID().equals("0")))
    {
      listModel.addElement("..");
    }
    if (container != null)
    {
      for (int i = 0; i < container.length; i++)
      {
        listModel.addElement(container[i]);
      }
    }
  }

  private class ContainerSelectionListener extends MouseAdapter
  {

    public void mousePressed(MouseEvent e)
    {
      if (resultList.getSelectedValue() != null)
      {
        if (resultList.getSelectedValue().equals(".."))
        {
          parent.toParent();
        } else
        {
          parent.toChild((DIDLContainer)resultList.getSelectedValue());
        }
      }
    }
  }
}

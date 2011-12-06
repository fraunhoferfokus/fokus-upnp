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

import de.fraunhofer.fokus.upnp.core_av.control_point.AVControlPoint;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaServerCPDevice;

/**
 * This panel shows all found media servers.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class PanelServer extends JPanel
{

  private static final long serialVersionUID = 1L;

  private DefaultListModel  listModel        = new DefaultListModel();

  private JList             resultList       = new JList(listModel);

  private JLabel            headlineLabel    = new JLabel();

  private AVGUIControl      parent;

  public PanelServer(AVGUIControl parent)
  {
    this.parent = parent;

    headlineLabel.setText("Server");
    resultList.addMouseListener(new ServerSelectionListener());

    BoxLayout layoutBPanel = new BoxLayout(this, BoxLayout.Y_AXIS);
    this.setLayout(layoutBPanel);
    add(headlineLabel);
    add(new JScrollPane(resultList));
  }

  /** Updates the list with all found items */
  public void updateServerList()
  {
    listModel.clear();
    AVControlPoint controlPoint = parent.getAVEntity().getAVControlPoint();

    for (int i = 0; i < controlPoint.getMediaServerCount(); i++)
    {
      listModel.addElement(controlPoint.getMediaServer(i));
    }
  }

  private class ServerSelectionListener extends MouseAdapter
  {
    public void mousePressed(MouseEvent e)
    {
      parent.setCurrentServer((MediaServerCPDevice)resultList.getSelectedValue());
    }
  }

}

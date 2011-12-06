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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;

/**
 * @author tje, Alexander Koenig
 * 
 */
public class PanelItem extends JPanel implements ActionListener
{

  private static final long   serialVersionUID   = 1L;

  private static final String NO_RENDERER        = "No Renderer found";

  private static final String PLAY_SELECTED      = "Play selected";

  private DefaultListModel    listModel          = new DefaultListModel();

  private JList               resultList         = new JList(listModel);

  private JLabel              headlineLabel      = new JLabel();

  private JLabel              urlLabel           = new JLabel(" ");

  private JComboBox           selectRenderer     = new JComboBox();

  private JButton             buttonPlaySelected = new JButton(PLAY_SELECTED);

  public PanelItem(AVGUIControl parent)
  {
    BoxLayout layoutFrame = new BoxLayout(this, BoxLayout.Y_AXIS);
    this.setLayout(layoutFrame);
    headlineLabel.setText("Items");
    add(headlineLabel);
    add(new JScrollPane(resultList));
    resultList.addMouseListener(new ItemSelectionListener());

    add(urlLabel);

    selectRenderer.addItem(NO_RENDERER);
    selectRenderer.addActionListener(this);
    add(selectRenderer);

    buttonPlaySelected.addActionListener(this);
    enableButton();
    add(buttonPlaySelected);
  }

  /** Updates the list with all found items */
  public void setItemList(DIDLItem[] items)
  {
    listModel.clear();
    enableButton();

    if (items != null)
    {
      for (int i = 0; i < items.length; ++i)
      {
        listModel.add(i, items[i]);
      }
    }
    urlLabel.setText(" ");
  }

  public void newRenderer(MediaRendererCPDevice newRenderer)
  {
    if (selectRenderer.getItemAt(0).toString().equals(NO_RENDERER))
    {
      selectRenderer.removeItemAt(0);
      selectRenderer.addItem(newRenderer);
    } else
    {
      selectRenderer.addItem(newRenderer);
    }
    enableButton();
  }

  public void rendererGone(CPDevice goneRenderer)
  {
    for (int i = 0; i < selectRenderer.getItemCount(); ++i)
    {
      if (((MediaRendererCPDevice)selectRenderer.getItemAt(i)).getRendererDevice().equals(goneRenderer))
      {
        selectRenderer.removeItemAt(i);
      }
    }
    if (selectRenderer.getItemCount() == 0)
    {
      selectRenderer.addItem(NO_RENDERER);
    }
    enableButton();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() instanceof JButton && !resultList.isSelectionEmpty())
    {

      String songURL = ((DIDLItem)resultList.getSelectedValue()).getFirstResourceURL();
      String buttonName = ((JButton)e.getSource()).getText();

      if (buttonName.equals(PLAY_SELECTED))
      {
        ((MediaRendererCPDevice)selectRenderer.getSelectedItem()).setAVTransportURI(songURL, "");
        ((MediaRendererCPDevice)selectRenderer.getSelectedItem()).play();
      }
    }
  }

  private void enableButton()
  {
    boolean enable =
      resultList.getSelectedValue() != null && !selectRenderer.getItemAt(0).toString().equals(NO_RENDERER);

    buttonPlaySelected.setEnabled(enable);
  }

  private class ItemSelectionListener extends MouseAdapter
  {

    public void mousePressed(MouseEvent e)
    {
      if (resultList.getSelectedValue() != null)
      {
        String songURL =
          ((DIDLResource)((DIDLItem)resultList.getSelectedValue()).getResources().elementAt(0)).getValue();
        urlLabel.setText(songURL);
      }
      enableButton();
    }
  }

}

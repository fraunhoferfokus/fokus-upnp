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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_renderer;

import javax.swing.JFrame;

import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;

/**
 * @author tje
 * 
 */
public class RendererControlInit extends JFrame
{

  private static final long  serialVersionUID = 1L;

  private RendererControlGUI gui;

  public RendererControlInit(MediaRendererCPDevice renderer)
  {
    super("GUI: " + renderer.getRendererDevice().getFriendlyName());
    setUndecorated(true);

    setSize(RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG).getWidth(null) +
      RepositoryCPGUI.getRendererImages(CPGUIConstants.VIDEOBACK).getWidth(null) * 3,
      RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG).getHeight(null));

    gui = new RendererControlGUI(this, renderer);
    this.setContentPane(gui);
    this.setVisible(true);
    this.toFront();
  }
}

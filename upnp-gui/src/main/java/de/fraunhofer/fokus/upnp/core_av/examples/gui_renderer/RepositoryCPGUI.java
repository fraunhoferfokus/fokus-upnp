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

import java.awt.Image;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;

/**
 * @author tje
 * 
 */
public class RepositoryCPGUI
{

  private static Hashtable rendererImages = new Hashtable();

  private static Hashtable rendererGUIs   = new Hashtable();

  // methods fo the gui of renderer control
  public static void putRendererImages(String imageName, Image toolImage)
  {
    rendererImages.put(imageName, toolImage);
  }

  public static Image getRendererImages(String imageName)
  {
    return (Image)rendererImages.get(imageName);
  }

  public static void addRendererGUI(CPDevice rendererDevice, RendererControlInit renderer)
  {
    rendererGUIs.put(rendererDevice, renderer);
  }

  public static void removeRendereGUI(CPDevice rendererDevice)
  {
    rendererGUIs.remove(rendererDevice);
  }

  public static RendererControlInit getRendereGUI(CPDevice rendererDevice)
  {
    return (RendererControlInit)rendererGUIs.get(rendererDevice);
  }

}

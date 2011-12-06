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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;

/**
 * This class is the base class for plugins that implement devices.
 * 
 * @author Alexander Koenig
 */
public class BaseDevicePlugin extends BasePlugin
{

  private static final long serialVersionUID = 1L;

  /** Device associated with this plugin */
  protected AbstractDevice  device;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#isInitialized()
   */
  public boolean canStartPlugin()
  {
    return frame != null && device != null;
  }

  /** Retrieves the device associated with this plugin */
  public AbstractDevice getDevice()
  {
    return device;
  }

  /** Sets the device associated with this plugin */
  public void setDevice(AbstractDevice device)
  {
    this.device = device;
    if (canStartPlugin() && !started)
    {
      started = true;
      startPlugin();
    }
  }

}

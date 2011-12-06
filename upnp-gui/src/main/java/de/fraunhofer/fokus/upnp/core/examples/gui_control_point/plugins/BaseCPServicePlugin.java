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

import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.IDeviceGUIContextProvider;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceGUIContext;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;

/**
 * This class is the base class for plugins that are specific for one service.
 * 
 * @author Alexander Koenig
 */
public class BaseCPServicePlugin extends BasePlugin implements ICPStateVariableListener
{

  private static final long                   serialVersionUID = 1L;

  /** Service associated with this plugin */
  protected CPService                         service;

  /** Control point for action invocation */
  protected SecurityAwareTemplateControlPoint controlPoint;

  /** Interface to other known device context objects */
  protected IDeviceGUIContextProvider         deviceGUIContextProvider;

  /** Associated device GUI */
  protected DeviceGUIContext                  deviceGUIContext;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#isInitialized()
   */
  public boolean canStartPlugin()
  {
    return frame != null && service != null && controlPoint != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
  }

  /** Checks if an action is waiting for processing */
  public boolean isActionPending()
  {
    return false;
  }

  /** Retrieves the control point associated with this plugin */
  public SecurityAwareTemplateControlPoint getControlPoint()
  {
    return controlPoint;
  }

  /** Sets the control point associated with this plugin */
  public void setControlPoint(SecurityAwareTemplateControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
    if (canStartPlugin() && !started)
    {
      started = true;
      startPlugin();
    }
  }

  /** Retrieves the service associated with this plugin */
  public CPService getCPService()
  {
    return service;
  }

  /** Sets the service associated with this plugin */
  public void setCPService(CPService service)
  {
    this.service = service;
    if (canStartPlugin() && !started)
    {
      started = true;
      startPlugin();
    }
  }

  /** Sets the device GUI context */
  public void setDeviceGUIContext(DeviceGUIContext deviceGUIContext)
  {
    this.deviceGUIContext = deviceGUIContext;
  }

  /** Sets the optional device GUI context provider */
  public void setDeviceGUIContextProvider(IDeviceGUIContextProvider provider)
  {
    this.deviceGUIContextProvider = provider;
  }

}

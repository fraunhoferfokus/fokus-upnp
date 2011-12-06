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
package de.fraunhofer.fokus.upnp.core_av.control_point;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;

/**
 * This class represents a remote view on an ConnectionManager service.
 * 
 * @author Alexander Koenig
 * 
 */
public class ConnectionManagerCPService
{
  private MediaRendererCPDevice mediaRendererCPDevice;

  private CPService             cpService;

  /**
   * Creates a new instance of ConnectionManagerCPService.
   * 
   * @param mediaRendererCPDevice
   * @param stateVariableListener
   */
  public ConnectionManagerCPService(MediaRendererCPDevice mediaRendererCPDevice)
  {
    this.mediaRendererCPDevice = mediaRendererCPDevice;
    this.cpService =
      mediaRendererCPDevice.getCPDevice().getCPServiceByType(UPnPAVConstant.CONNECTION_MANAGER_SERVICE_TYPE);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Connection manager //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the protocols this media renderer can understand. */
  public void getProtocolInfo()
  {
    CPAction action = cpService.getCPAction(UPnPAVConstant.ACTION_GET_PROT_INFO);

    try
    {
      mediaRendererCPDevice.getTemplateControlPoint().invokeAction(action);

      String sinkProtocols = (String)action.getOutArgument(UPnPAVConstant.ARG_SINK).getValue();

      mediaRendererCPDevice.storeSinkProtocolInfo(sinkProtocols);
    } catch (Exception ex)
    {
      errorInAVTAction(action, ex);
    }
  }

  /**
   * Logs arguments for a failed action.
   * 
   * @param action
   * @param e
   *          The exception
   */
  private void errorInAVTAction(CPAction action, Exception e)
  {
    if (action != null)
    {
      Argument[] argument = action.getArgumentTable();

      CPService.logger.error("--------------------------------");
      CPService.logger.error("Device '" + cpService.getCPDevice().getFriendlyName() + "': " + action);
      for (int i = 0; i < argument.length; ++i)
      {
        Argument a = argument[i];
        CPService.logger.error("arg[" + i + "](" + a.getName() + ") ='" + a.getValue() + "'");
      }
    } else
    {
      CPService.logger.error("Action not available");
    }
    CPService.logger.error(e.toString());
    CPService.logger.error("--------------------------------");
  }

}

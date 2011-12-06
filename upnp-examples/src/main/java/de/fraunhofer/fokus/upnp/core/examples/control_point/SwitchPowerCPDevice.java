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
package de.fraunhofer.fokus.upnp.core.examples.control_point;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.examples.lights.ISwitchPower;
import de.fraunhofer.fokus.upnp.core.templates.TemplateCPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;

/**
 * A SwitchPowerCPDevice can be used to simply control a UPnP device that implements the SwitchPower
 * device template.
 * 
 * @author Alexander Koenig
 */
public class SwitchPowerCPDevice extends TemplateCPDevice implements ISwitchPower
{

  private String    upnpServiceID;

  private CPService cpSwitchPowerService;

  public SwitchPowerCPDevice(TemplateControlPoint controlPoint, CPDevice serverDevice, String upnpServiceID)
  {
    super(controlPoint, serverDevice);
    this.upnpServiceID = upnpServiceID;
    cpSwitchPowerService = serverDevice.getCPServiceByID(upnpServiceID);
  }

  public String toString()
  {
    return getCPDevice().getFriendlyName();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public boolean getStatus(String upnpServiceID)
  {
    if (upnpServiceID.equals(this.upnpServiceID) && cpSwitchPowerService != null)
    {
      CPAction action = cpSwitchPowerService.getCPAction("GetStatus");

      if (action != null)
      {
        try
        {
          getTemplateControlPoint().invokeAction(action);

          boolean result = ((Boolean)action.getOutArgument("ResultStatus").getValue()).booleanValue();

          return result;

        } catch (Exception e)
        {
          System.out.println("Error: " + e.getMessage());
        }
      }
    }
    return false;
  }

  public void setTarget(String upnpServiceID, boolean state)
  {
    if (upnpServiceID.equals(this.upnpServiceID) && cpSwitchPowerService != null)
    {
      CPAction action = cpSwitchPowerService.getCPAction("SetTarget");

      if (action != null)
      {
        try
        {
          action.getInArgument("NewTargetValue").setValue(new Boolean(state));
          getTemplateControlPoint().invokeAction(action);
        } catch (Exception e)
        {
          System.out.println("Error: " + e.getMessage());
        }
      }
    }
  }

}

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
package de.fraunhofer.fokus.upnp.core_security.control_point;

import java.net.URL;

import de.fraunhofer.fokus.upnp.core.control_point.CPDeviceDescriptionRetrieval;
import de.fraunhofer.fokus.upnp.core.control_point.CPSSDPMessageProcessor;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPointHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;

/**
 * This class adds support for security aware devices that need signed GET requests.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityAwareCPSSDPMessageProcessor extends CPSSDPMessageProcessor
{

  private SecurityAwareTemplateControlPoint securityAwareTemplateControlPoint;

  /**
   * Creates a new instance of SecurityAwareCPSSDPMessageProcessor.
   * 
   * @param securityAwareTemplateControlPoint
   *          The associated control point
   */
  public SecurityAwareCPSSDPMessageProcessor(ControlPoint controlPoint)
  {
    super(controlPoint);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.CPSSDPMessageProcessor#startDeviceDescriptionRetrieval(de.fhg.fokus.magic.upnp.control_point.ControlPointHostAddressSocketStructure,
   *      java.net.URL, java.lang.String, java.lang.String, int, java.lang.String)
   */
  protected void startDeviceDescriptionRetrieval(ControlPointHostAddressSocketStructure discoverySocketStructure,
    URL descriptionURL,
    String rootDeviceUUID,
    String serverValue,
    int maxAge,
    String NLSValue)
  {
    if (securityAwareTemplateControlPoint != null)
    {
      // create and start device description retrieval thread
      new SecurityAwareCPDeviceDescriptionRetrieval(securityAwareTemplateControlPoint,
        discoverySocketStructure,
        descriptionURL,
        rootDeviceUUID,
        serverValue,
        maxAge,
        NLSValue,
        IPVersion);
    } else
    {
      System.out.println("SecurityAwareControlPoint is not yet known, " + "fall back to normal description retrieval");
      // create and start device description retrieval thread
      new CPDeviceDescriptionRetrieval(controlPoint,
        discoverySocketStructure,
        descriptionURL,
        rootDeviceUUID,
        serverValue,
        maxAge,
        NLSValue,
        IPVersion);
    }
  }

  /**
   * Sets the securityAwareTemplateControlPoint.
   * 
   * @param controlPoint
   *          The new value for controlPoint
   */
  public void setSecurityAwareTemplateControlPoint(SecurityAwareTemplateControlPoint controlPoint)
  {
    this.securityAwareTemplateControlPoint = controlPoint;
  }

}

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
package de.fraunhofer.fokus.upnp.core_security;

/**
 * 
 * @author Alexander Koenig
 */
public class SecurityConstant
{
  public static final String CONTROL_POINT_END_TAG         = "</CP>";

  public static final String CONTROL_POINT_TAG             = "<CP>";

  public static final String CONTROL_URL_END_TAG           = "</controlURL>";

  public static final String CONTROL_URL_TAG               = "<controlURL>";

  public static final String DEVICE_END_TAG                = "</Device>";

  public static final String DEVICE_SECURITY_SERVICE_ID    = "urn:upnp-org:serviceId:DeviceSecurity1.0";

  /** urn:schemas-upnp-org:service:DeviceSecurity:1 */
  public static final String DEVICE_SECURITY_SERVICE_TYPE  = "urn:schemas-upnp-org:service:DeviceSecurity:1";

  public static final String DEVICE_TAG                    = "<Device>";

  public static final int    MAX_SESSION_COUNT             = 20;

  public static final String SECURITY_CONSOLE_SERVICE_ID   = "urn:upnp-org:serviceId:SecurityConsole1.0";

  public static final String SECURITY_CONSOLE_SERVICE_TYPE = "urn:schemas-upnp-org:service:SecurityConsole:1";

}

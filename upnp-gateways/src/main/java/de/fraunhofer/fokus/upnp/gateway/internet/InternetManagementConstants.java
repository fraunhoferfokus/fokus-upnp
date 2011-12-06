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
package de.fraunhofer.fokus.upnp.gateway.internet;

/**
 * 
 * @author Alexander Koenig
 */
public class InternetManagementConstants
{

  public static final String DEVICE_DIRECTORY_DEVICE_TYPE               =
                                                                          "urn:schemas-fokus-fraunhofer-de:device:DeviceDirectoryDevice:1";

  public static final String DISCOVERY_SERVICE_TYPE                     =
                                                                          "urn:schemas-fokus-fraunhofer-de:service:DiscoveryService:1";

  public static final String DISCOVERY_SERVICE_ID                       =
                                                                          "urn:fokus-fraunhofer-de:serviceId:DiscoveryService1.0";

  public static final String CONNECTION_TYPE_TRANSPARENT                = "Transparent";

  public static final String CONNECTION_TYPE_EVENTED                    = "Evented";

  public static final String CONNECTION_TYPE_MANUAL                     = "Manual";

  public static final String CONNECTION_STATUS_CONNECTED                = "Connected";

  public static final String CONNECTION_STATUS_SEARCHED                 = "Searching";

  public static final String CONNECTION_STATUS_DISCONNECTED             = "Disconnected";

  public static final String DEVICE_LOCATION_LOCAL                      = "Local";

  public static final String DEVICE_LOCATION_GLOBAL                     = "Global";

  public static final String DEVICE_LOCATION_UNKNOWN                    = "Unknown";

  public static final String INTERNET_GATEWAY_MODULE_ID                 = "Internet_Gateway";

  public static final int    REMOTE_DEVICE_WAIT_TIMEOUT                 = 30000;

  /** Reception of M-SEARCH messages from other DeviceDirectoryDevices (1901) */
  public static final int    SSDP_DEVICE_DIRECTORY_DEVICE_M_SEARCH_PORT = 1901;

  /** Reception of HTTP requests from other DeviceDirectoryDevices (1902) */
  public static final int    HTTP_DEVICE_DIRECTORY_DEVICE_REQUEST_PORT  = 1902;

  /** Reception of M-SEARCH and NOTIFY messages for all devices (1904) */
  public static final int    SSDP_DEVICE_PORT                           = 1904;

  /** Sending of M-SEARCH messages for all devices (1905) */
  public static final int    SSDP_DEVICE_M_SEARCH_SEND_PORT             = 1905;

  /** Reception of external HTTP requests (1906) */
  public static final int    HTTP_DEVICE_REQUEST_PORT                   = 1906;

  /** Forwarding of SUBSCRIBE messages to DeviceDirectoryDevices (1907) */
  // public static final int HTTP_DEVICE_DIRECTORY_DEVICE_SUBSCRIBE_PORT = 1907;
  /** Sending of local M-SEARCH messages for DeviceDirectoryDevices (1910) */
  // public static final int SSDP_DEVICE_DIRECTORY_DEVICE_LOCAL_UNICAST_PORT = 1910;
}

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
package de.fraunhofer.fokus.upnp.core;

/**
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class UPnPConstant
{

  // Device event codes
  public static final int    DEVICE_EVENT_ATTRIBUTE_SERVICE_READ   = 1;

  public static final int    DEVICE_EVENT_SUBSCRIPTION_END         = 11;

  public static final int    DEVICE_EVENT_SUBSCRIPTION_START       = 10;

  public static final int    DEVICE_EVENT_TRANSLATION_SERVICE_READ = 2;

  public static final int    DEVICE_EVENT_USAGE_SERVICE_READ       = 3;

  public static final String DEVICE_TYPE                           = "urn:schemas-upnp-org:device:";

  public static final String DEVICE_UUID                           = "uuid:";

  // UPnP device handling
  public static final String DIRECTION_IN                          = "in";

  public static final String DIRECTION_OUT                         = "out";

  public static final String ERROR_CODE                            = "errorCode";

  public static final String ERROR_DESCRIPTION                     = "errorDescription";

  public static final int    IP4                                   = 4;

  public static final int    IP6                                   = 6;

  public final static String IPv4_VERSION                          = "4";

  public final static String IPv6_VERSION                          = "6";

  public static final String MAX_AGE_TAG                           = "max-age = ";

  public static String       MUS_VERSION                           = "Fokus UPnP Stack V1.0 started";

  public static final String NLS_Value                             = "";

  public static final String QUERY_STATE_VARIABLE                  = "QueryStateVariable";

  public static final String QUERY_STATE_VARIABLE_RESPONSE         = "QueryStateVariableResponse";

  public static final String SDDP_ALL                              = "ssdp:all";

  public static String       SERVER                                = "JAVA/1.4.2 UPnP/1.0 FokusUPnPStack/1.0";

  public static final String SERVICE_TYPE                          = "urn:schemas-upnp-org:service:";

  public static final String SOAP_ERROR_401                        = "401";

  public static final String SOAP_ERROR_401_DESCRIPTION            = "Invalid Action";

  public static final String SOAP_ERROR_402                        = "402";

  public static final String SOAP_ERROR_402_DESCRIPTION            = "Invalid Args";

  public static final String SOAP_ERROR_404                        = "404";

  public static final String SOAP_ERROR_404_DESCRIPTION            = "Invalid Var";

  public static final String SOAP_ERROR_501                        = "501";

  public static final String SOAP_ERROR_501_DESCRIPTION            = "Action Failed";

  public static final String SOAP_ERROR_600                        = "600";

  public static final String SOAP_ERROR_600_DESCRIPTION            = "Argument Value Invalid";

  public static final String SOAP_ERROR_601                        = "601";

  public static final String SOAP_ERROR_601_DESCRIPTION            = "Argument Value Out of Range";

  public static final String SOAP_ERROR_602                        = "602";

  public static final String SOAP_ERROR_602_DESCRIPTION            = "Optional Action Not Implemented";

  public static final String SOAP_ERROR_603                        = "603";

  public static final String SOAP_ERROR_603_DESCRIPTION            = "Out of Memory";

  public static final String SOAP_ERROR_604                        = "604";

  public static final String SOAP_ERROR_604_DESCRIPTION            = "Human Intervention Required";

  public static final String SOAP_ERROR_605                        = "605";

  public static final String SOAP_ERROR_605_DESCRIPTION            = "String Argument Too Long";

  public static final String SOAP_ERROR_702                        = "702";

  public static final int    SUBSCRIPTION_MODE_AUTOMATIC           = 2;

  // Subscription modes
  public static final int    SUBSCRIPTION_MODE_MANUAL              = 1;

  public static final String SUFFIX_CONTROL                        = "/control/";

  public static final String SUFFIX_EVENTING                       = "/eventSub/";

  public static final String SUFFIX_REST                           = "/rest/";

  public static final String SUFFIX_SCPD                           = "/description.xml";

  public static final String UPNP_ERROR                            = "UPnPError";

  public static final String UPNP_ROOTDEVICE                       = "upnp:rootdevice";

  public static final String UPNP_VERSION                          = " UPnP/1.0 ";

  public static final String USER_UNKNOWN                          = "Unknown";

  public static final String UUID                                  = "uuid:";

  public static final String XMLNS_BEGIN_TAG                       = "xmlns:u=\"";

  public static final String XMLNS_END_TAG                         = "\"";

  public static final String XMLNS_ERROR                           = "xmlns=\"urn:schemas-upnp-org:control-1-0\"";

  public static final String XMLNS_SERVICE                         = "xmlns:u=\"";
}

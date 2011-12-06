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
package de.fraunhofer.fokus.upnp.soap;

/**
 * This class holds various constants used for SOAP.
 * 
 * @author icu, Alexander Koenig
 */
public class SOAPConstant
{

  public static final String SOAPACTION            = "SOAPACTION:";

  public static final String ACTION                = "ACTION";

  public static final String ENVELOPE_START_TAG    =
                                                     "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                                                       + "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">";

  public static final String ENVELOPE_END_TAG      = "</s:Envelope>";

  public static final String BODY_START_TAG        = "<s:Body>";

  public static final String BODY_SIGNED_START_TAG =
                                                     "<s:Body xmlns:us=\"urn:schemas-upnp-org:service:DeviceSecurity:1\" us:Id=\"Body\">";

  public static final String BODY_END_TAG          = "</s:Body>";

  public static final String FAULT_START_TAG       = "<s:Fault>";

  public static final String FAULT_END_TAG         = "</s:Fault>";

  public static final String HEADER_START_TAG      = "<s:Header>";

  public static final String HEADER_END_TAG        = "</s:Header>";

  public static final String FAULTCODE_START_TAG   = "<faultcode>";

  public static final String FAULTCODE_END_TAG     = "</faultcode>";

  public static final String S_CLIENT              = "s:Client";

  public static final String FAULTSTRING_START_TAG = "<faultstring>";

  public static final String FAULTSTRING_END_TAG   = "</faultstring>";

  public static final String DETAIL_START_TAG      = "<detail>";

  public static final String DETAIL_END_TAG        = "</detail>";

  public static final String ENVELOPE_URL          = "\"http://schemas.xmlsoap.org/soap/envelope/\"";

  public static final String NS                    = "ns=";
}

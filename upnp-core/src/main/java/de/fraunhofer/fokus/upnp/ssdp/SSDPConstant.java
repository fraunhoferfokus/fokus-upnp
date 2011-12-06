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
package de.fraunhofer.fokus.upnp.ssdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class provides constants for SSDP.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class SSDPConstant
{

  public static final int          IP4                            = 4;

  public static final int          IP6                            = 6;

  public static final String       NLS                            = "01-NLS:";

  public static final String       OPT                            = "OPT:";

  public static final String       OPT_VALUE                      = "\"http://schemas.upnp.org/upnp/1/0/\"; ns=01";

  public static final String       SSDP_ALIVE                     = "ssdp:alive";

  public static final String       SSDP_ALL                       = "ssdp:all";

  public static final String       SSDP_BYEBYE                    = "ssdp:byebye";

  public static final String       SSDP_DISCOVER                  = "\"ssdp:discover\"";

  public static final int          SSDP_SERVER_PORT               = 5454;

  /**
   * default IP adress for multicasting in UPnP stack
   */
  public static final String       SSDPMulticastAddress           = "239.255.255.250";

  /**
   * multicast adress for IPv6 on the local link
   */

  // public static final String SSDPMulticastAddressV6 = "[FF02::c]";
  public static final String       SSDPMulticastAddressV6         = "[FF05::c]";

  private static InetAddress       SSDPMulticastInetAddress       = null;

  private static InetSocketAddress SSDPMulticastInetSocketAddress = null;

  /**
   * default PORT number for multicasting in UPnP stack
   */
  public static final int          SSDPMulticastPort              = 1900;

  public static final String       SSDPMulticastSocketAddress     = "239.255.255.250:1900";

  public static final String       ST                             = "ST:";

  public static final int          TTL                            = 15;

  public static InetAddress getSSDPMulticastAddress()
  {
    if (SSDPMulticastInetAddress == null)
    {
      try
      {
        SSDPMulticastInetAddress = InetAddress.getByName(SSDPMulticastAddress);
      } catch (Exception ex)
      {
      }
    }
    return SSDPMulticastInetAddress;
  }

  public static InetSocketAddress getSSDPMulticastSocketAddress()
  {
    if (SSDPMulticastInetSocketAddress == null)
    {
      try
      {
        SSDPMulticastInetSocketAddress = new InetSocketAddress(SSDPMulticastAddress, SSDPMulticastPort);
      } catch (Exception ex)
      {
      }
    }
    return SSDPMulticastInetSocketAddress;
  }

}

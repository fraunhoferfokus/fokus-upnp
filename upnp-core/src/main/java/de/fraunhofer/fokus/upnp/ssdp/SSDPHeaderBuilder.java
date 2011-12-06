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

import de.fraunhofer.fokus.upnp.gena.GENAHeaderBuilder;
import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class is used to build SSDP messages.
 * 
 * @author tje, Alexander Koenig
 */
public class SSDPHeaderBuilder extends GENAHeaderBuilder
{

  /**
   * @param input
   *          parameter for USN header
   * @return USN header as string
   */
  public static String buildUSN(String input)
  {
    return CommonConstants.USN + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for MSearch header
   * @return MSearch header as string
   */
  public static String buildMSearch(String input)
  {
    return CommonConstants.M_SEARCH + CommonConstants.BLANK + input + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * @param input
   *          parameter for ST header
   * @return ST header as string
   */
  public static String buildST(String input)
  {
    return SSDPConstant.ST + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  // **************** IPv6 extension
  public static String buildOPT()
  {
    return SSDPConstant.OPT + " " + SSDPConstant.OPT_VALUE + CommonConstants.NEW_LINE;
  }

  public static String buildNLS(String input)
  {
    return SSDPConstant.NLS + " " + input + CommonConstants.NEW_LINE;
  }
}

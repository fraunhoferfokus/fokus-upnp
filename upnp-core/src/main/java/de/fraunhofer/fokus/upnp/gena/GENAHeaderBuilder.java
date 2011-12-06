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
package de.fraunhofer.fokus.upnp.gena;

import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class supplies methods for GENAHeader building.
 * 
 * @author tje, Alexander Koenig
 */
public class GENAHeaderBuilder extends HTTPHeaderBuilder
{
  /**
   * Builds the notify header from input string.
   * 
   * @param input
   *          parameter for notify header
   * 
   * @return notify header as string
   */
  public static String buildNotify(String input)
  {
    return CommonConstants.NOTIFY + CommonConstants.BLANK + input + CommonConstants.BLANK + CommonConstants.HTTP_1_1 +
      CommonConstants.NEW_LINE;
  }

  /**
   * Builds the NT header from input string.
   * 
   * @param input
   *          parameter for NT header
   * 
   * @return NT header as string
   */
  public static String buildNT(String input)
  {
    return GENAConstant.NT + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the NTS header from input string.
   * 
   * @param input
   *          parameter for NTS header
   * 
   * @return NTS header as string
   */
  public static String buildNTS(String input)
  {
    return GENAConstant.NTS + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the subscribe header from input string.
   * 
   * @param input
   *          parameter for subscribe header
   * 
   * @return subscribe header as string
   */
  public static String buildSubscribe(String input)
  {
    return CommonConstants.SUBSCRIBE + CommonConstants.BLANK + input + CommonConstants.BLANK +
      CommonConstants.HTTP_1_1 + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the INITIAL_EVENT header from input string.
   * 
   * @param input
   *          parameter for subscribe header
   * 
   * @return subscribe header as string
   */
  public static String buildInitialEvent(String input)
  {
    return CommonConstants.INITIAL_EVENT + CommonConstants.BLANK + input + CommonConstants.BLANK +
      CommonConstants.HTTP_1_1 + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the unsubscribe header from input string.
   * 
   * @param input
   *          parameter for unsubscribe header
   * 
   * @return unsubscribe header as string
   */
  public static String buildUnsubscribe(String input)
  {
    return CommonConstants.UNSUBSCRIBE + CommonConstants.BLANK + input + CommonConstants.BLANK +
      CommonConstants.HTTP_1_1 + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the callback header from the delivery URL vector.
   * 
   * @param urls
   *          Vector which includes all delivery URLs
   * 
   * @return callback header
   */
  public static String buildCallback(Vector urls)
  {
    StringBuffer deliveryUrls = new StringBuffer();

    for (Enumeration e = urls.elements(); e.hasMoreElements();)
    {
      deliveryUrls.append("<" + e.nextElement().toString() + ">");
    }

    return CommonConstants.CALLBACK + CommonConstants.BLANK + deliveryUrls.toString() + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the callback header from the delivery URL vector.
   * 
   * @param urls
   *          Vector which includes all delivery URLs
   * 
   * @return UDP callback header
   */
  public static String buildUDPCallback(Vector urls)
  {
    StringBuffer deliveryUrls = new StringBuffer();

    for (Enumeration e = urls.elements(); e.hasMoreElements();)
    {
      deliveryUrls.append("<" + e.nextElement().toString() + ">");
    }

    return CommonConstants.UDP_CALLBACK + CommonConstants.BLANK + deliveryUrls.toString() + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the SID header from input string.
   * 
   * @param input
   *          parameter for SID header
   * 
   * @return SID header as string
   */
  public static String buildSID(String input)
  {
    return GENAConstant.SID + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }

  /**
   * Builds the SEQ header from input string.
   * 
   * @param input
   *          parameter for SEQ header
   * 
   * @return SEQ header as string
   */
  public static String buildSEQ(String input)
  {
    return GENAConstant.SEQ + CommonConstants.BLANK + input + CommonConstants.NEW_LINE;
  }
}

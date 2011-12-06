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

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class can be used to create SSDP messages.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class MSearchMessageBuilder extends SSDPHeaderBuilder
{
  /**
   * SSDP logger
   */
  static Logger logger = Logger.getLogger("upnp.ssdp");

  /**
   * methods creates the SSDP-MSEARCHRESPONSE message
   * 
   * @param cacheControlValue
   *          data of the CACHE-CONTROL headerline
   * @param dateValue
   *          data of the DATE headerline
   * @param locationValue
   *          data of the LOCATION headerline
   * @param serverValue
   *          data of the SERVER headerline
   * @param STValue
   *          data of the ST headerline
   * @param USNValue
   *          data of the USN headerline
   * @return complete MSearchResponse message as a string
   */
  public static String createMSearchResponseMessage(String cacheControlValue,
    String dateValue,
    String locationValue,
    String serverValue,
    String STValue,
    String USNValue)
  {
    StringBuffer respMsg = new StringBuffer();
    respMsg.append(buildHTTPOK());
    respMsg.append(buildCacheControl(cacheControlValue));
    respMsg.append(buildDate(dateValue));
    respMsg.append(buildEXT());
    respMsg.append(buildLocation(locationValue));
    respMsg.append(buildServer(serverValue));
    respMsg.append(buildST(STValue));
    respMsg.append(buildUSN(USNValue));
    respMsg.append(CommonConstants.NEW_LINE);

    // logger.debug("[\n" + respMsg.toString() + "]");

    return respMsg.toString();
  }

  public static String createMSearchResponseMessageV6(String cacheControlValue,
    String dateValue,
    String locationValue,
    String serverValue,
    String STValue,
    String USNValue,
    String NLSValue)
  {
    StringBuffer respMsg1 = new StringBuffer();
    respMsg1.append(createMSearchResponseMessage(cacheControlValue,
      dateValue,
      locationValue,
      serverValue,
      STValue,
      USNValue));

    StringBuffer respMsg = new StringBuffer();
    respMsg.append(respMsg1.substring(0, respMsg1.length() - CommonConstants.NEW_LINE.length() - 1));
    respMsg.append(buildOPT());
    respMsg.append(buildNLS(NLSValue));
    respMsg.append(CommonConstants.NEW_LINE);

    // logger.debug("[\n" + respMsg.toString() + "]");

    return respMsg.toString();
  }

  /**
   * methods creates the SSDP-MSEARCH message
   * 
   * @param mSearchValue
   *          data of the MSEARCH headerline
   * @param hostValue
   *          data of the HOST headerline
   * @param MANValue
   *          data of the MAN headerline
   * @param MXValue
   *          data of the MX headerline
   * @param STValue
   *          data of the ST headerline
   * @return complete MSearch message as a string
   */
  public static String createMSearchMessage(String mSearchValue,
    String hostValue,
    String MANValue,
    String MXValue,
    String STValue)
  {
    StringBuffer discoveryMsg = new StringBuffer();
    discoveryMsg.append(buildMSearch(mSearchValue));
    discoveryMsg.append(buildHost(hostValue));
    discoveryMsg.append(buildMAN(MANValue));
    discoveryMsg.append(buildMX(MXValue));
    discoveryMsg.append(buildST(STValue));
    discoveryMsg.append(CommonConstants.NEW_LINE);

    // logger.debug("[\n" + discoveryMsg.toString() + "]");

    return discoveryMsg.toString();
  }

  /**
   * Creates a message that search all devices
   * 
   * @param mx
   *          maximum wait, Device responses should be delayed a random duration between 0 and this
   *          many seconds.
   * @return search all message
   */
  public static String createSearchAllMessage(int mx, String ssdpMulticastSocketAddress, int IPVersion)
  {
    return createMSearchMessage("*",
      ssdpMulticastSocketAddress,
      SSDPConstant.SSDP_DISCOVER,
      String.valueOf(mx),
      SSDPConstant.SSDP_ALL);
  }

  /**
   * Creates a message that search only root devices
   * 
   * @param mx
   *          maximum wait, Device responses should be delayed a random duration between 0 and this
   *          many seconds.
   * @return search root message
   */
  public static String createSearchRootMessage(int mx, String ssdpMulticastSocketAddress, int IPVersion)
  {
    return createMSearchMessage("*",
      ssdpMulticastSocketAddress,
      SSDPConstant.SSDP_DISCOVER,
      String.valueOf(mx),
      UPnPConstant.UPNP_ROOTDEVICE);
  }

  /**
   * Creates a message that search a device using a device unique identification(UUID)
   * 
   * @param mx
   *          maximum wait, Device responses should be delayed a random duration between 0 and this
   *          many seconds.
   * @param UUID
   *          device unique identification(UUID)
   * @return search device UUID message
   */
  public static String createSearchUUIDMessage(int mx, String ssdpMulticastSocketAddress, String UUID, int IPVersion)
  {
    return createMSearchMessage("*", ssdpMulticastSocketAddress, SSDPConstant.SSDP_DISCOVER, String.valueOf(mx), UUID);
  }

  /**
   * Creates a message that search devices from this deviceType
   * 
   * @param mx
   *          maximum wait, Device responses should be delayed a random duration between 0 and this
   *          many seconds.
   * @param deviceType
   *          type of device
   * @return search device type message
   */
  public static String createSearchDeviceTypeMessage(int mx,
    String ssdpMulticastSocketAddress,
    String deviceType,
    int IPVersion)
  {
    return createMSearchMessage("*",
      ssdpMulticastSocketAddress,
      SSDPConstant.SSDP_DISCOVER,
      String.valueOf(mx),
      deviceType);
  }

  /**
   * Creates a message that search a service from this service type
   * 
   * @param mx
   *          maximum wait, Device responses should be delayed a random duration between 0 and this
   *          many seconds.
   * @param serviceType
   *          type of service
   * @return search service type message
   */
  public static String createSearchServiceTypeMessage(int mx,
    String ssdpMulticastSocketAddress,
    String serviceType,
    int IPVersion)
  {
    return createMSearchMessage("*",
      ssdpMulticastSocketAddress,
      SSDPConstant.SSDP_DISCOVER,
      String.valueOf(mx),
      serviceType);
  }
}

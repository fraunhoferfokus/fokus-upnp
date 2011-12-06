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

import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.AbstractService;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;

/**
 * This class provides all methods to create device discovery messages
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class NotifyMessageBuilder extends MSearchMessageBuilder
{

  /**
   * SSDP logger
   */
  static Logger logger = Logger.getLogger("upnp.ssdp");

  /**
   * Creates all response messages for a search message.
   * 
   * @param device
   *          device object
   * @return response messages
   */
  public static Vector createAllResponseMessages(AbstractDevice device, InetSocketAddress serverAddress, int IPVersion)
  {
    return createAllMessages(device, serverAddress, "", IPVersion);
  }

  /**
   * Creates all discovery message for a device and its embedded devices.
   * 
   * @param device
   *          device object
   * @param SSDPType
   *          type of ssdp message( ssdp:alive xor ssdp:byebye)
   * @return discovery messages
   */
  public static Vector createAllMessages(AbstractDevice device,
    InetSocketAddress serverAddress,
    String SSDPType,
    int IPVersion)
  {
    Vector messages = new Vector();
    messages.addElement(createUUIDMessage(device.getUDN(),
      SSDPType,
      device.getSSDPMulticastSocketAddressString(),
      IPVersion,
      device.getMaxage(),
      device.getDeviceDescriptionURL(serverAddress),
      device.getServer(),
      device.getNLS()));

    messages.addElement(createDeviceTypeMessage(device.getUDN(),
      device.getDeviceType(),
      SSDPType,
      device.getSSDPMulticastSocketAddressString(),
      IPVersion,
      device.getMaxage(),
      device.getDeviceDescriptionURL(serverAddress),
      device.getServer(),
      device.getNLS()));

    messages.addElement(createRootDeviceMessage(device.getUDN(),
      SSDPType,
      device.getSSDPMulticastSocketAddressString(),
      IPVersion,
      device.getMaxage(),
      device.getDeviceDescriptionURL(serverAddress),
      device.getServer(),
      device.getNLS()));

    createEmbDevicesMessage(serverAddress, device.getAbstractDeviceTable(), messages, device, SSDPType, IPVersion);

    AbstractService[] services = device.getAbstractServiceTable();

    if (services != null)
    {
      for (int l = 0; l < services.length; l++)
      {
        messages.addElement(createServiceTypeMessage(device.getUDN(),
          services[l].getServiceType(),
          SSDPType,
          device.getSSDPMulticastSocketAddressString(),
          IPVersion,
          device.getMaxage(),
          device.getDeviceDescriptionURL(serverAddress),
          device.getServer(),
          device.getNLS()));
      }
    }

    return messages;
  }

  /**
   * Creates root device discovery messages.
   * 
   * @param device
   *          device object
   * @param SSDPType
   *          type of ssdp message( ssdp:alive xor ssdp:byebye)
   * @return discovery messages
   */
  public static Vector createRootDeviceMessage(AbstractDevice device,
    InetSocketAddress serverAddress,
    String SSDPType,
    int IPVersion)
  {
    Vector messages = new Vector();
    messages.addElement(createRootDeviceMessage(device.getUDN(),
      SSDPType,
      device.getSSDPMulticastSocketAddressString(),
      IPVersion,
      device.getMaxage(),
      device.getDeviceDescriptionURL(serverAddress),
      device.getServer(),
      device.getNLS()));

    return messages;
  }

  private static void createEmbDevicesMessage(InetSocketAddress serverAddress,
    AbstractDevice[] embDevices,
    Vector messages,
    AbstractDevice device,
    String SSDPType,
    int IPVersion)
  {
    if (embDevices != null)
    {
      for (int i = 0; i < embDevices.length; i++)
      {
        messages.addElement(createUUIDMessage(embDevices[i].getUDN(),
          SSDPType,
          device.getSSDPMulticastSocketAddressString(),
          IPVersion,
          device.getMaxage(),
          device.getDeviceDescriptionURL(serverAddress),
          device.getServer(),
          device.getNLS()));
        messages.addElement(createDeviceTypeMessage(embDevices[i].getUDN(),
          embDevices[i].getDeviceType(),
          SSDPType,
          device.getSSDPMulticastSocketAddressString(),
          IPVersion,
          device.getMaxage(),
          device.getDeviceDescriptionURL(serverAddress),
          device.getServer(),
          device.getNLS()));

        AbstractService[] services = embDevices[i].getAbstractServiceTable();

        if (services != null)
        {
          for (int j = 0; j < services.length; j++)
          {
            messages.addElement(createServiceTypeMessage(embDevices[i].getUDN(),
              services[j].getServiceType(),
              SSDPType,
              device.getSSDPMulticastSocketAddressString(),
              IPVersion,
              device.getMaxage(),
              device.getDeviceDescriptionURL(serverAddress),
              device.getServer(),
              device.getNLS()));
          }
        }

        createEmbDevicesMessage(serverAddress,
          embDevices[i].getAbstractDeviceTable(),
          messages,
          device,
          SSDPType,
          IPVersion);
      }
    }
  }

  /**
   * Creates response message for search root device message
   * 
   * @param deviceUUID
   *          device unique identification
   * @return response message
   */
  public static String createRootDeviceResponseMessage(String deviceUUID,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    return createRootDeviceMessage(deviceUUID, "", "", IPVersion, maxage, ddURL, server, NLS);
  }

  /**
   * Creates discovery message for root device
   * 
   * @param deviceUUID
   *          device unique identification
   * @param SSDPType
   *          type of ssdp message( ssdp:alive xor ssdp:byebye)
   * @return root device discovery message
   */
  public static String createRootDeviceMessage(String deviceUUID,
    String SSDPType,
    String ssdpMulticastSocketAddress,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    if (IPVersion == UPnPConstant.IP6)
    {
      if (!SSDPType.equals(""))
      {
        return NotifyMessageBuilder.createNotifyMessageV6("*", ssdpMulticastSocketAddress, UPnPConstant.MAX_AGE_TAG +
          maxage, ddURL, UPnPConstant.UPNP_ROOTDEVICE, SSDPType, server, deviceUUID + "::" +
          UPnPConstant.UPNP_ROOTDEVICE, NLS);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessageV6(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          UPnPConstant.UPNP_ROOTDEVICE,
          deviceUUID + "::" + UPnPConstant.UPNP_ROOTDEVICE,
          NLS);
      }
    } else
    {
      if (!SSDPType.equals(""))
      {
        return NotifyMessageBuilder.createNotifyMessage("*", ssdpMulticastSocketAddress, UPnPConstant.MAX_AGE_TAG +
          maxage, ddURL, UPnPConstant.UPNP_ROOTDEVICE, SSDPType, server, deviceUUID + "::" +
          UPnPConstant.UPNP_ROOTDEVICE);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessage(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          UPnPConstant.UPNP_ROOTDEVICE,
          deviceUUID + "::" + UPnPConstant.UPNP_ROOTDEVICE);
      }
    }
  }

  /**
   * Creates response message for search device type message
   * 
   * @param deviceUUID
   *          device unique identification
   * @param deviceType
   *          device type
   * @return response message
   */
  public static String createDeviceTypeResponseMessage(String deviceUUID,
    String deviceType,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    return createDeviceTypeMessage(deviceUUID, deviceType, "", "", IPVersion, maxage, ddURL, server, NLS);
  }

  /**
   * Creates a SSDP device type message (NOTIFY or HTTP OK)
   * 
   * @param deviceUUID
   * @param deviceType
   * @param SSDPType
   * @param ssdpMulticastSocketAddress
   * @param IPVersion
   * @param maxage
   * @param ddURL
   * @param server
   * @param NLS
   * @return
   */
  public static String createDeviceTypeMessage(String deviceUUID,
    String deviceType,
    String SSDPType,
    String ssdpMulticastSocketAddress,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    if (IPVersion == UPnPConstant.IP6)
    {
      if (!SSDPType.equals(""))
      {
        return createNotifyMessageV6("*",
          ssdpMulticastSocketAddress,
          UPnPConstant.MAX_AGE_TAG + maxage,
          ddURL,
          deviceType,
          SSDPType,
          server,
          deviceUUID + "::" + deviceType,
          NLS);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessageV6(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          deviceType,
          deviceUUID + "::" + deviceType,
          NLS);
      }
    } else
    {
      if (!SSDPType.equals(""))
      {
        return createNotifyMessage("*",
          ssdpMulticastSocketAddress,
          UPnPConstant.MAX_AGE_TAG + maxage,
          ddURL,
          deviceType,
          SSDPType,
          server,
          deviceUUID + "::" + deviceType);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessage(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          deviceType,
          deviceUUID + "::" + deviceType);
      }
    }
  }

  /**
   * Create response message for search device unique identification message
   * 
   * @param deviceUUID
   *          device unique identification
   * @return response message
   */
  public static String createUUIDResponseMessage(String deviceUUID,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    return createUUIDMessage(deviceUUID, "", "", IPVersion, maxage, ddURL, server, NLS);
  }

  /**
   * Create discovery message for device unique identification
   * 
   * @param deviceUUID
   *          device unique identification
   * @param SSDPType
   *          type of ssdp message( ssdp:alive xor ssdp:byebye)
   * @return discovery message for device unique identification
   */
  public static String createUUIDMessage(String deviceUUID,
    String SSDPType,
    String ssdpMulticastSocketAddress,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    if (IPVersion == UPnPConstant.IP6)
    {
      if (!SSDPType.equals(""))
      {
        return NotifyMessageBuilder.createNotifyMessageV6("*", ssdpMulticastSocketAddress, UPnPConstant.MAX_AGE_TAG +
          maxage, ddURL, deviceUUID, SSDPType, server, deviceUUID, NLS);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessageV6(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          deviceUUID,
          deviceUUID,
          NLS);
      }
    } else
    {
      if (!SSDPType.equals(""))
      {
        return NotifyMessageBuilder.createNotifyMessage("*", ssdpMulticastSocketAddress, UPnPConstant.MAX_AGE_TAG +
          maxage, ddURL, deviceUUID, SSDPType, server, deviceUUID);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessage(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          deviceUUID,
          deviceUUID);
      }
    }
  }

  /**
   * Creates response message for search service type message
   * 
   * @param deviceUUID
   *          device unique identification
   * @param serviceType
   *          type of service
   * @return response message
   */
  public static String createServiceTypeResponseMessage(String deviceUUID,
    String serviceType,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    return createServiceTypeMessage(deviceUUID, serviceType, "", "", IPVersion, maxage, ddURL, server, NLS);
  }

  /**
   * Creates discovery message for service type
   * 
   * @param deviceUUID
   *          device unique identification
   * @param serviceType
   *          type of service
   * @param SSDPType
   *          type of ssdp message( ssdp:alive xor ssdp:byebye)
   * @return discovery message for service type
   */
  public static String createServiceTypeMessage(String deviceUUID,
    String serviceType,
    String SSDPType,
    String ssdpMulticastSocketAddress,
    int IPVersion,
    int maxage,
    String ddURL,
    String server,
    String NLS)
  {
    if (IPVersion == UPnPConstant.IP6)
    {
      if (!SSDPType.equals(""))
      {
        return createNotifyMessageV6("*",
          ssdpMulticastSocketAddress,
          UPnPConstant.MAX_AGE_TAG + maxage,
          ddURL,
          serviceType,
          SSDPType,
          server,
          deviceUUID + "::" + serviceType,
          NLS);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessageV6(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          serviceType,
          deviceUUID + "::" + serviceType,
          NLS);
      }
    } else
    {
      if (!SSDPType.equals(""))
      {
        return createNotifyMessage("*",
          ssdpMulticastSocketAddress,
          UPnPConstant.MAX_AGE_TAG + maxage,
          ddURL,
          serviceType,
          SSDPType,
          server,
          deviceUUID + "::" + serviceType);
      } else
      {
        return MSearchMessageBuilder.createMSearchResponseMessage(UPnPConstant.MAX_AGE_TAG + maxage,
          DateTimeHelper.getRFC1123Date(),
          ddURL,
          server,
          serviceType,
          deviceUUID + "::" + serviceType);
      }
    }
  }

  /**
   * methods creates the SSDP-Notify message
   * 
   * @param notifyValue
   *          data of the Notify headerline
   * @param hostValue
   *          data of the HOST headerline
   * @param cacheControlValue
   *          data of the CACHE-CONTROL headerline
   * @param locationValue
   *          data of the LOCATION headerline
   * @param NTValue
   *          data of the NT headerline
   * @param NTSValue
   *          data of the NTS headerline
   * @param serverValue
   *          data of the SERVER headerline
   * @param USNValue
   *          data of the USN headerline
   * @return complete SSDP-Notify message as a string
   */
  public static String createNotifyMessage(String notifyValue,
    String hostValue,
    String cacheControlValue,
    String locationValue,
    String NTValue,
    String NTSValue,
    String serverValue,
    String USNValue)
  {
    StringBuffer discoveryMsg = new StringBuffer();
    discoveryMsg.append(buildNotify(notifyValue));
    discoveryMsg.append(buildHost(hostValue));

    if (NTSValue.equals(SSDPConstant.SSDP_ALIVE))
    {
      discoveryMsg.append(buildCacheControl(cacheControlValue));
      discoveryMsg.append(buildLocation(locationValue));
    }

    discoveryMsg.append(buildNT(NTValue));

    if (NTSValue.equals(SSDPConstant.SSDP_ALIVE))
    {
      discoveryMsg.append(buildNTS(SSDPConstant.SSDP_ALIVE));
      discoveryMsg.append(buildServer(serverValue));
    } else
    {
      discoveryMsg.append(buildNTS(SSDPConstant.SSDP_BYEBYE));
    }

    discoveryMsg.append(buildUSN(USNValue));
    discoveryMsg.append(CommonConstants.NEW_LINE);

    // logger.debug("[\n" + discoveryMsg.toString() + "]");

    return discoveryMsg.toString();
  }

  public static String createNotifyMessageV6(String notifyValue,
    String hostValue,
    String cacheControlValue,
    String locationValue,
    String NTValue,
    String NTSValue,
    String serverValue,
    String USNValue,
    String NLSValue)
  {
    StringBuffer discoveryMsg1 = new StringBuffer();
    discoveryMsg1.append(createNotifyMessage(notifyValue,
      hostValue,
      cacheControlValue,
      locationValue,
      NTValue,
      NTSValue,
      serverValue,
      USNValue));

    StringBuffer discoveryMsg = new StringBuffer();
    discoveryMsg.append(discoveryMsg1.substring(0, discoveryMsg1.length() - CommonConstants.NEW_LINE.length() - 1));
    discoveryMsg.append(buildOPT());
    discoveryMsg.append(buildNLS(NLSValue));
    discoveryMsg.append(CommonConstants.NEW_LINE);

    // logger.debug("[\n" + discoveryMsg.toString() + "]");

    return discoveryMsg.toString();
  }
}

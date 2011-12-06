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

import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageProcessorResult;
import de.fraunhofer.fokus.upnp.ssdp.NotifyMessageBuilder;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class processes M-SEARCH messages and creates the appropriate responses.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class MSearchMessageProcessor
{
  /** UPnP logger */
  protected static Logger logger = Logger.getLogger("upnp.ssdp");

  public MSearchMessageProcessor()
  {
  }

  /**
   * Creates all response messages for a device for a received M-SEARCH message.
   * 
   * @param abstractDevice
   *          The device that should answer the request
   * @param httpServerAddress
   *          Address of the server that should receive resulting GET requests
   * @param message
   *          The received message
   * 
   * @return All generated response messages as a vector of message strings
   */
  public static MSearchMessageProcessorResult processMessage(AbstractDevice abstractDevice,
    InetSocketAddress httpServerAddress,
    String message,
    int IPVersion)
  {
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(message);
    int mxValue;
    if (httpParser.isMSEARCHMessage())
    {
      // message is search message
      if (httpParser.getMethodValue().equals("*"))
      {
        if (httpParser.getValue(HTTPConstant.MAN).equals(SSDPConstant.SSDP_DISCOVER))
        {
          String STValue = httpParser.getValue(SSDPConstant.ST);

          // store MX value
          mxValue = StringHelper.stringToIntegerDef(httpParser.getValue(HTTPConstant.MX), 0);

          // search all message
          if (STValue.equals(SSDPConstant.SSDP_ALL))
          {
            logger.info("Search all device request received");

            return new MSearchMessageProcessorResult(NotifyMessageBuilder.createAllResponseMessages(abstractDevice,
              httpServerAddress,
              IPVersion), mxValue);
          }
          // search root message
          else if (STValue.equals(UPnPConstant.UPNP_ROOTDEVICE))
          {
            logger.info("Search root device request received");

            Vector result = new Vector();
            result.addElement(NotifyMessageBuilder.createRootDeviceResponseMessage(abstractDevice.getUDN(),
              IPVersion,
              abstractDevice.getMaxage(),
              abstractDevice.getDeviceDescriptionURL(httpServerAddress),
              abstractDevice.getServer(),
              abstractDevice.getNLS()));

            return new MSearchMessageProcessorResult(result, mxValue);
          }
          // search device uuid message
          else if (STValue.startsWith(UPnPConstant.DEVICE_UUID))
          {
            logger.info("Search device uuid " + STValue + "request received");

            return new MSearchMessageProcessorResult(createDeviceUUIDResponse(abstractDevice,
              httpServerAddress,
              STValue,
              IPVersion), mxValue);
          }
          // search device type message
          else if (STValue.startsWith(UPnPConstant.DEVICE_TYPE) || STValue.indexOf("device") != -1)
          {
            logger.info("Search device type " + STValue + " request received");

            return new MSearchMessageProcessorResult(createDeviceResponse(abstractDevice,
              httpServerAddress,
              STValue,
              IPVersion), mxValue);
          }
          // search service type message
          else if (STValue.startsWith(UPnPConstant.SERVICE_TYPE) || STValue.indexOf("service") != -1)
          {
            logger.info("Search service type " + STValue + " request received");

            return new MSearchMessageProcessorResult(createServiceResponse(abstractDevice,
              httpServerAddress,
              STValue,
              IPVersion), mxValue);
          } else
          {
            logger.warn("Unknown search request = " + STValue);
          }
        } else
        { // check man value
          logger.warn("Invalid search request: MAN header != ssdp:discover (" + " MAN header = " +
            httpParser.getValue(HTTPConstant.MAN) + ")");

          // message is not search message, return HTTP 412 error
          Vector result = new Vector();
          result.add(HTTPHeaderBuilder.buildHTTPError412());

          return new MSearchMessageProcessorResult(result, 0);
        }

        // }else{//check host value
        // logger.warn("DISCOVERY MESSAGE: unknown host target = "
        // +mSearch.getHostValue());
        // }
      } else
      { // check MSEARCH value
        logger.warn("mSearch value != *, mSearch value = " + httpParser.getMethodValue());
      }
    }

    return null;
  }

  /**
   * Creates the response message for the search to a certain UUID.
   * 
   * @param deviceUUID
   *          device unique identification
   * @return response for search UUID message
   */
  public static Vector createDeviceUUIDResponse(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    String deviceUUID,
    int IPVersion)
  {
    Vector result = new Vector();

    if (deviceUUID.equals(abstractDevice.getUDN()))
    {
      logger.info(deviceUUID + " found.");

      result.addElement(NotifyMessageBuilder.createUUIDResponseMessage(abstractDevice.getUDN(),
        IPVersion,
        abstractDevice.getMaxage(),
        abstractDevice.getDeviceDescriptionURL(serverAddress),
        abstractDevice.getServer(),
        abstractDevice.getNLS()));
    } else
    {
      result =
        createDeviceUUIDResponseForEmbeddedDevice(abstractDevice,
          serverAddress,
          abstractDevice.getAbstractDeviceTable(),
          deviceUUID,
          IPVersion);
    }

    return result;
  }

  private static Vector createDeviceUUIDResponseForEmbeddedDevice(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    AbstractDevice[] embDevices,
    String deviceUUID,
    int IPVersion)
  {
    Vector result = new Vector();

    if (embDevices != null)
    {
      for (int j = 0; j < embDevices.length; j++)
      {
        if (deviceUUID.equals(embDevices[j].getUDN()))
        {
          logger.info(deviceUUID + " found.");
          result.addElement(NotifyMessageBuilder.createUUIDResponseMessage(deviceUUID,
            IPVersion,
            abstractDevice.getMaxage(),
            abstractDevice.getDeviceDescriptionURL(serverAddress),
            abstractDevice.getServer(),
            abstractDevice.getNLS()));
          break;
        } else
        {
          result =
            createDeviceUUIDResponseForEmbeddedDevice(abstractDevice,
              serverAddress,
              embDevices[j].getAbstractDeviceTable(),
              deviceUUID,
              IPVersion);

          if (result.size() != 0)
          {
            return result;
          }
        }
      }
    }

    return result;
  }

  /**
   * Creates response for search device type message
   * 
   * @param deviceType
   *          device type
   * @return response for search device type message
   */
  private static Vector createDeviceResponse(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    String deviceType,
    int IPVersion)
  {
    Vector result = new Vector();

    if (deviceType.equals(abstractDevice.getDeviceType()))
    {
      logger.info(deviceType + " found.");
      result.addElement(NotifyMessageBuilder.createDeviceTypeResponseMessage(abstractDevice.getUDN(),
        deviceType,
        IPVersion,
        abstractDevice.getMaxage(),
        abstractDevice.getDeviceDescriptionURL(serverAddress),
        abstractDevice.getServer(),
        abstractDevice.getNLS()));
    }

    createDeviceResponseForEmbeddedDevice(abstractDevice,
      serverAddress,
      abstractDevice.getAbstractDeviceTable(),
      deviceType,
      IPVersion,
      result);

    return result;
  }

  private static void createDeviceResponseForEmbeddedDevice(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    AbstractDevice[] embDevices,
    String deviceType,
    int IPVersion,
    Vector result)
  {
    if (embDevices != null)
    {
      for (int j = 0; j < embDevices.length; j++)
      {
        if (deviceType.equals(embDevices[j].getDeviceType()))
        {
          logger.info(deviceType + " found.");
          result.addElement(NotifyMessageBuilder.createDeviceTypeResponseMessage(embDevices[j].getUDN(),
            deviceType,
            IPVersion,
            abstractDevice.getMaxage(),
            abstractDevice.getDeviceDescriptionURL(serverAddress),
            abstractDevice.getServer(),
            abstractDevice.getNLS()));
        }
        // recursive search
        createDeviceResponseForEmbeddedDevice(abstractDevice,
          serverAddress,
          embDevices[j].getAbstractDeviceTable(),
          deviceType,
          IPVersion,
          result);
      }
    }
  }

  /**
   * Creates response for search service type message
   * 
   * @param serviceType
   *          service type
   * @return response for service type message
   */
  private static Vector createServiceResponse(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    String serviceType,
    int IPVersion)
  {

    Vector result = new Vector();
    AbstractService[] services = abstractDevice.getAbstractServiceTable();

    if (services != null)
    {
      for (int l = 0; l < services.length; l++)
      {
        if (serviceType.equals(services[l].getServiceType()))
        {
          logger.info(serviceType + " found");
          result.addElement(NotifyMessageBuilder.createServiceTypeResponseMessage(abstractDevice.getUDN(),
            serviceType,
            IPVersion,
            abstractDevice.getMaxage(),
            abstractDevice.getDeviceDescriptionURL(serverAddress),
            abstractDevice.getServer(),
            abstractDevice.getNLS()));
        }
      }
    }
    createServiceResponseForEmbeddedDevice(abstractDevice,
      serverAddress,
      abstractDevice.getAbstractDeviceTable(),
      serviceType,
      IPVersion,
      result);

    return result;
  }

  private static void createServiceResponseForEmbeddedDevice(AbstractDevice abstractDevice,
    InetSocketAddress serverAddress,
    AbstractDevice[] embDevices,
    String serviceType,
    int IPVersion,
    Vector result)
  {
    if (embDevices != null)
    {
      for (int j = 0; j < embDevices.length; j++)
      {
        AbstractService[] embDevServices = embDevices[j].getAbstractServiceTable();

        for (int k = 0; k < embDevServices.length; k++)
        {
          logger.info(serviceType + " found");

          if (serviceType.equals(embDevServices[k].getServiceType()))
          {
            result.addElement(NotifyMessageBuilder.createServiceTypeResponseMessage(embDevices[j].getUDN(),
              serviceType,
              IPVersion,
              embDevices[j].getMaxage(),
              embDevices[j].getDeviceDescriptionURL(serverAddress),
              embDevices[j].getServer(),
              embDevices[j].getNLS()));
          }
        }
        // recursive search
        createServiceResponseForEmbeddedDevice(abstractDevice,
          serverAddress,
          embDevices[j].getAbstractDeviceTable(),
          serviceType,
          IPVersion,
          result);
      }
    }
  }
}

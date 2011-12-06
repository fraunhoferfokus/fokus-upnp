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
package de.fraunhofer.fokus.upnp.core.device;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.gena.GENAParser;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class handles the processing of event re/un/subscription messages
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceSubscribeMessageProcessor
{

  /** UPnP logger */
  protected static Logger logger = Logger.getLogger("upnp.gena");

  /**
   * Processes a subscription message, if the message is OK a unique uuid and a event key for the
   * subscriber is generated the subscribe response builder is called and a response message is
   * returned
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          The device that received the subscription request
   * 
   * @return subscribe response message as a string or if service is not found an error message
   */
  public static HTTPMessageObject processSubscribe(HTTPParser httpParser, Device device)
  {
    InetSocketAddress serverAddress = httpParser.getHTTPMessageObject().getDestinationAddress();
    // retrieve URL from request
    URL parameterURL = httpParser.getRequestURL();

    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // get callback URLs
    Vector deliveryURLs = GENAParser.parseCallbackValue(httpParser.getValue(CommonConstants.CALLBACK));

    // get optional UDP callback URLs
    Vector udpDeliveryURLs = null;
    String udpCallbackHeader = httpParser.getValue(CommonConstants.UDP_CALLBACK);
    if (udpCallbackHeader != null)
    {
      udpDeliveryURLs = GENAParser.parseCallbackValue(udpCallbackHeader);
    }

    // check if service is present
    DeviceService service = checkEventSubscriptionURL(parameterURL, device);

    // check for errors
    if (deliveryURLs.size() == 0 || service == null)
    {
      logger.info("SUBSCRIBE is ignored due to an error!");
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    // check if a subscriber with these delivery urls already exists
    DeviceSubscribedControlPointHandler knownSubscriber = service.getSubscriber(deliveryURLs);
    if (knownSubscriber == null)
    {
      // get new SID for subscriber
      String sid = device.getNewSubscriptionUUID();
      //      TemplateDevice.printMessage("Accept new subscription for callback " + deliveryURLs.elementAt(0) + " with SID " +
      //        sid);

      // create new subscriber thread
      int timeout = service.addSubscriber(sid, deliveryURLs, udpDeliveryURLs, httpParser);

      // reply to subscribing control point
      return new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(), sid, timeout),
        serverAddress);
    } else
    {
      System.out.println("Received multiple subscription request from callback URL " + deliveryURLs.elementAt(0));
      // return as if this is the first subscription request
      return new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(),
        knownSubscriber.getSID(),
        knownSubscriber.getSubscriptionTimeout()), serverAddress);
    }
  }

  /**
   * Processes a resubscription message.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          The device that received the resubscription request
   * 
   * @return HTTP OK if message was correct otherwise returns the corresponding HTTP error message
   */
  public static HTTPMessageObject processResubscribe(HTTPParser httpParser, Device device)
  {
    InetSocketAddress serverAddress = httpParser.getHTTPMessageObject().getDestinationAddress();
    // retrieve URL from request
    URL parameterURL = httpParser.getRequestURL();

    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    String sid = httpParser.getValue(GENAConstant.SID);

    // check if service is present
    DeviceService service = checkEventSubscriptionURL(parameterURL, device);
    if (service != null)
    {
      // check if uuid is present
      if (!isKnownSubscriber(service, sid))
      {
        logger.warn("invalid resubscribe message");
        logger.warn("reason: unknown SID = " + sid);

        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
      }

      // set new timeout for subscriber thread
      int timeout = service.renewSubscriber(sid, httpParser.getValue(HTTPConstant.TIMEOUT));

      // send reply to control point
      return new HTTPMessageObject(GENAMessageBuilder.buildSubscribeResponseHeader(device.getServer(), sid, timeout),
        serverAddress);
    } else
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }
  }

  /**
   * Processes an unsubscribe message.
   * 
   * @param httpParser
   *          Associated parser
   * @param device
   *          The device that received the resubscription request
   * 
   * @return HTTP OK if message was correct otherwise returns the corresponding HTTP error message
   */
  public static HTTPMessageObject processUnsubscribe(HTTPParser httpParser, Device device)
  {
    InetSocketAddress serverAddress = httpParser.getHTTPMessageObject().getDestinationAddress();
    // check if called eventing_URL is correct
    URL parameterURL = httpParser.getRequestURL();

    if (parameterURL == null)
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
    }

    String sid = httpParser.getValue(GENAConstant.SID);

    // check if service is present
    DeviceService service = checkEventSubscriptionURL(parameterURL, device);

    if (service != null)
    {
      // check if uuid is present
      if (!isKnownSubscriber(service, sid))
      {
        logger.warn("invalid unsubscribe message");
        logger.warn("reason: unknown SID = " + sid);

        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
      }

      // delete subscriber from subscriber hashtable
      service.removeSubscriber(sid);

      // reply to control point
      return new HTTPMessageObject(HTTPConstant.HTTP_OK_NL, serverAddress);
    }
    return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_503, serverAddress);
  }

  /**
   * Tests if called eventing URL exist and return the corresponding service
   * 
   * @param parameterURL
   *          URL to be checked
   * @return the service for the eventing URL, if url was not found, null is returned
   */
  public static DeviceService checkEventSubscriptionURL(URL parameterURL, Device device)
  {
    DeviceService[] services = device.getDeviceServiceTable();
    if (services != null)
    {
      for (int i = 0; i < services.length; i++)
      {
        if (parameterURL.getPath().equals(services[i].getRelativeEventSubscriptionURL()))
        {
          return services[i];
        }
      }
    }

    if (device.getEmbeddedDeviceTable() != null)
    {
      Device[] embeddedDevices = device.getEmbeddedDeviceTable();
      for (int i = 0; i < embeddedDevices.length; i++)
      {
        DeviceService embeddedService = checkEventSubscriptionURL(parameterURL, embeddedDevices[i]);
        if (embeddedService != null)
        {
          return embeddedService;
        }
      }
    }

    logger.info("unknown subscription url = " + parameterURL.toString());

    return null;
  }

  /**
   * Tests if a certain sid is registered for a certain service.
   * 
   * @param service
   *          service to be checked
   * @param sid
   *          sid of subscriber to be cheched
   * @return true if sid is registered, false otherwise
   */
  protected static boolean isKnownSubscriber(DeviceService service, String sid)
  {
    return service.isKnownSubscriber(sid);
  }

  /**
   * Builds an URL-Object from the corresponding Hashtable-parameter
   * 
   * @param dHost
   *          ip and port number
   * @param path
   *          path part of the url
   * @return URL which is build out of the URL components, null if URL could not be build
   */
  public static URL buildParameterUrl(String[] dHost, String path, AbstractDevice dev)
  {
    try
    {
      // add a / to path if not present
      if (!path.endsWith("/"))
      {
        path += "/";
      }

      URL parameterURL = new URL("http", dHost[0], Integer.parseInt(dHost[1]), path);

      return parameterURL;
    } catch (Exception e)
    {
      logger.error("cannot build URL using host = " + dHost[0] + " port = " + dHost[1] + "path = " + path);
      logger.error("reason: " + e.getMessage());
    }

    return null;
  }

}

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

import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.device.StateVariableEventObject;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.XMLHelper;

/**
 * This class supplies methods for building GENA specific messages for the UPnP architecture like:
 * subscribing / resubscribing / unsubscribing / response / notify messages
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class GENAMessageBuilder
{
  /** GENA logger */
  protected static Logger logger = Logger.getLogger("upnp.gena");

  /**
   * This method builds a subscription message.
   * 
   * @param publisherPath
   *          path part of service URL
   * @param publisherHost
   *          host part of service URL
   * @param publisherPort
   *          port part of service URL
   * @param deliveryURLs
   *          URLs of the CP where the notify of the subscription is to be sent
   * @param timeoutwish
   *          how long the subscription should exist, could be null
   * 
   * @return subscription message as string
   * 
   */
  public static String buildSubscribe(String publisherPath,
    String publisherHost,
    int publisherPort,
    Vector deliveryURLs,
    String timeoutwish)
  {
    return buildSubscribe(publisherPath, publisherHost, publisherPort, deliveryURLs, null, timeoutwish);
  }

  /**
   * This method builds a subscription message with optional UDP callback.
   * 
   * @param publisherPath
   *          path part of service URL
   * @param publisherHost
   *          host part of service URL
   * @param publisherPort
   *          port part of service URL
   * @param deliveryURLs
   *          URLs of the CP where the notify of the subscription is to be sent
   * @param udpDeliveryURLs
   *          URLs of the CP where the notify of the subscription is to be sent
   * @param timeoutwish
   *          how long the subscription should exist, could be null
   * 
   * @return subscription message as string
   * 
   */
  public static String buildSubscribe(String publisherPath,
    String publisherHost,
    int publisherPort,
    Vector deliveryURLs,
    Vector udpDeliveryURLs,
    String timeoutwish)
  {
    StringBuffer response = new StringBuffer();
    response.append(GENAHeaderBuilder.buildSubscribe(publisherPath));
    response.append(HTTPHeaderBuilder.buildHost(publisherHost + ":" + publisherPort));
    // add callback URLs
    if (deliveryURLs != null && deliveryURLs.size() > 0)
    {
      response.append(GENAHeaderBuilder.buildCallback(deliveryURLs));
    }

    // add optional UDP callback
    if (udpDeliveryURLs != null && udpDeliveryURLs.size() > 0)
    {
      response.append(GENAHeaderBuilder.buildUDPCallback(udpDeliveryURLs));
    }

    response.append(GENAHeaderBuilder.buildNT(GENAConstant.UPNP_EVENT));

    if (timeoutwish != null)
    {
      if (!timeoutwish.startsWith(GENAConstant.SECOND))
      {
        response.append(HTTPHeaderBuilder.buildTimeout(GENAConstant.SECOND + timeoutwish));
      } else
      {
        response.append(HTTPHeaderBuilder.buildTimeout(timeoutwish));
      }
    }

    // logger.debug(response.toString());

    return response.toString();
  }

  /**
   * method builds a resubscription message
   * 
   * @param publisherPath
   *          path part of service URL
   * @param publisherHost
   *          host part of service URL
   * @param publisherPort
   *          port part of service URL
   * @param uuid
   *          uuid which the device has given the control point for identification
   * @param timeoutwish
   *          how long the subscription should exist, could be null
   * @return resubscription message as string
   */
  public static String buildResubscribe(String publisherPath,
    String publisherHost,
    int publisherPort,
    String uuid,
    String timeoutwish)
  {
    StringBuffer response = new StringBuffer();
    response.append(GENAHeaderBuilder.buildSubscribe(publisherPath));
    response.append(HTTPHeaderBuilder.buildHost(publisherHost + ":" + publisherPort));
    response.append(GENAHeaderBuilder.buildSID(uuid));

    if (timeoutwish != null)
    {
      if (!timeoutwish.startsWith(GENAConstant.SECOND))
      {
        response.append(HTTPHeaderBuilder.buildTimeout(GENAConstant.SECOND + timeoutwish));
      }
    }

    logger.debug(response.toString());

    return response.toString();
  }

  /**
   * method builds a unsubscription message
   * 
   * @param publisherPath
   *          path part of service URL
   * @param publisherHost
   *          host part of service URL
   * @param publisherPort
   *          port part of service URL
   * @param uuid
   *          uuid which the device has given the control point for identification
   * @return unsubscription message as string
   */
  public static String buildUnsubscribe(String publisherPath, String publisherHost, int publisherPort, String uuid)
  {
    StringBuffer response = new StringBuffer();
    response.append(GENAHeaderBuilder.buildUnsubscribe(publisherPath));
    response.append(HTTPHeaderBuilder.buildHost(publisherHost + ":" + publisherPort));
    response.append(GENAHeaderBuilder.buildSID(uuid));
    logger.debug(response.toString());

    return response.toString();
  }

  /**
   * This method builds a response message to a re/subscription message.
   * 
   * @param server
   *          OS and product version
   * @param sid
   *          Subscription ID for the control point
   * @param timeout
   *          Timeout of the subscription in seconds
   * 
   * @return subscription response message
   * 
   */
  public static String buildSubscribeResponseHeader(String server, String sid, int timeout)
  {
    StringBuffer response = new StringBuffer();
    response.append(HTTPConstant.HTTP_OK_NL);
    response.append(HTTPConstant.DATE + " " + DateTimeHelper.getRFC1123Date() + CommonConstants.NEW_LINE);
    response.append(HTTPHeaderBuilder.buildServer(server));
    response.append(HTTPHeaderBuilder.buildContentLength("0"));
    response.append(GENAHeaderBuilder.buildSID(sid));
    response.append(HTTPHeaderBuilder.buildTimeout(GENAConstant.SECOND + timeout));

    logger.debug(response.toString());

    return response.toString();
  }

  /**
   * This method builds the header of an event NOTIFY message.
   * 
   * @param deliveryPath
   *          path part of delivery URL
   * @param deliveryHost
   *          host part of delivery URL
   * @param deliveryPort
   *          port part of delivery URL
   * @param uuid
   *          unique uuid for each control point
   * @param eventKey
   *          how often this messages was send to coressponding CP
   * @param bytesInBody
   *          bytes in the body of the message
   * @return header as string
   */
  public static String buildNotify(String deliveryPath,
    String deliveryHost,
    int deliveryPort,
    String uuid,
    String eventKey,
    String bytesInBody)
  {
    StringBuffer message = new StringBuffer();

    message.append(GENAHeaderBuilder.buildNotify(deliveryPath));
    message.append(HTTPHeaderBuilder.buildHost(deliveryHost + ":" + deliveryPort));
    message.append(HTTPHeaderBuilder.buildContentType(HTTPConstant.CONTENT_TYPE_TEXT_XML_UTF8));
    message.append(HTTPHeaderBuilder.buildContentLength(bytesInBody));
    message.append(GENAHeaderBuilder.buildNT(GENAConstant.UPNP_EVENT));
    message.append(GENAHeaderBuilder.buildNTS(GENAConstant.UPNP_PROPCHANGE));
    if (uuid != null)
    {
      message.append(GENAHeaderBuilder.buildSID(uuid));
    }

    message.append(GENAHeaderBuilder.buildSEQ(eventKey));

    logger.debug(message.toString());

    return message.toString();
  }

  /**
   * This method builds the body of a notify message.
   * 
   * @param eventObjects
   *          List with StateVariableEventObjects
   * 
   * @return body of notify message as string
   */
  public static String buildNotifyBody(Vector eventObjects)
  {
    StringBuffer body = new StringBuffer();
    body.append(GENAConstant.PROPERTYSET_BEGIN_TAG + CommonConstants.NEW_LINE);

    for (int i = 0; i < eventObjects.size(); i++)
    {
      StateVariableEventObject currentObject = (StateVariableEventObject)eventObjects.elementAt(i);

      if (currentObject.getValueString() == null)
      {
        logger.warn("state variable = " + currentObject.getName() + " has no current value");
        logger.warn("not clear if it's valid or invalid?");
        logger.warn("hence the variable will not be written in the eventnotify message");

        continue;
      }

      body.append(GENAConstant.PROPERTY_BEGIN_TAG + CommonConstants.NEW_LINE);
      body.append(XMLHelper.createTag(currentObject.getName(), currentObject.getValueString()));
      body.append(GENAConstant.PROPERTY_END_TAG + CommonConstants.NEW_LINE);
    }

    body.append(GENAConstant.PROPERTYSET_END_TAG);
    logger.debug(body.toString());

    return body.toString();
  }

  /**
   * Builds an INITIAL_EVENT message.
   * 
   * @param path
   *          Path for the request
   * @param host
   *          Host for the request
   * @param port
   *          Port for the request
   * 
   * @return INITIAL_EVENT Message
   * 
   */
  public static String buildInitialEventRequest(String path, String host, int port)
  {
    StringBuffer message = new StringBuffer(256);

    if (port == -1)
    { // no port in url
      port = CommonConstants.HTTP_DEFAULT_PORT;
    }

    message.append(GENAHeaderBuilder.buildInitialEvent(path));
    message.append(HTTPHeaderBuilder.buildHost(host + ':' + port));

    return message.toString();
  }

}

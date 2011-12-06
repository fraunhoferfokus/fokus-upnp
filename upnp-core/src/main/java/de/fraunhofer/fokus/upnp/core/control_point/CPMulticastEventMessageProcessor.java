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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.net.URL;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class processes incoming multicast GENA notify event messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class CPMulticastEventMessageProcessor extends CPEventMessageProcessor
{

  /**
   * Creates a new instance of CPMulticastEventMessageProcessor.
   * 
   * @param controlPoint
   *          The associated control point
   */
  public CPMulticastEventMessageProcessor(ControlPoint controlPoint)
  {
    super(controlPoint);
  }

  /**
   * Returns the service for an URL that contains the serviceID as URL path
   * 
   * @param parameterURL
   *          URL to be checked
   * 
   * @return the service for the URL or null
   */
  public static CPService getCPServiceByURLPath(URL parameterURL, ControlPoint controlPoint)
  {
    // tokenize path
    String deviceUDN = null;
    String shortenedServiceID = null;

    StringTokenizer pathTokenizer = new StringTokenizer(parameterURL.getPath(), "/");
    try
    {
      deviceUDN = URLHelper.escapedURLToString(pathTokenizer.nextToken());
      shortenedServiceID = URLHelper.escapedURLToString(pathTokenizer.nextToken());
    } catch (Exception e)
    {
      return null;
    }

    CPDevice device = controlPoint.getCPDeviceByUDN(deviceUDN);
    if (device == null)
    {
      return null;
    }
    return getCPServiceByServiceID(shortenedServiceID, device);
  }

  /**
   * Returns the service for an URL that contains the serviceID as URL path
   * 
   * @param parameterURL
   *          URL to be checked
   * 
   * @return the service for the URL or null
   */
  public static CPService getCPServiceByServiceID(String shortenedServiceID, CPDevice device)
  {
    CPService result = device.getCPServiceByShortenedID(shortenedServiceID);
    if (result != null)
    {
      return result;
    }
    // check embedded device services
    if (device.getCPDeviceTable() != null)
    {
      CPDevice[] embeddedDevices = device.getCPDeviceTable();
      for (int i = 0; i < embeddedDevices.length; i++)
      {
        CPService embeddedService = getCPServiceByServiceID(shortenedServiceID, embeddedDevices[i]);
        if (embeddedService != null)
        {
          return embeddedService;
        }
      }
    }
    return null;
  }

  /**
   * Processes a multicast event NOTIFY message.
   * 
   * @param request
   *          The NOTIFY message
   * 
   * @return always null
   * 
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    // System.out.println("Received multicast event message from " +
    // IPAddress.toString(request.getSourceAddress()));
    HTTPParser notifyParser = new HTTPParser();
    notifyParser.parse(request);

    if (notifyParser.isMulticastEventNOTIFYMessage())
    {
      synchronized(lock)
      {
        CPService service = getCPServiceByURLPath(notifyParser.getRequestURL(), controlPoint);
        if (service == null)
        {
          System.out.println("No service found for multicast event message received from " +
            IPHelper.toString(request.getSourceAddress()));
          return null;
        }
        // everything was fine
        if (tryUpdateStateVariables(request, notifyParser, service))
        {
          return null;
        } else
        {
          System.out.println("Received multicast event but no INITIAL_EVENT message");

          // request INITIAL_EVENT again
          service.sendInitialEventMessage();
        }
      }
    }
    return null;
  }

  /**
   * Processes a INITIAL_EVENT response message.
   * 
   * @param request
   *          The NOTIFY message
   * 
   */
  public void processInitialEventResponseMessage(HTTPMessageObject request)
  {
    HTTPParser notifyParser = new HTTPParser();
    notifyParser.parse(request);

    if (notifyParser.isMulticastEventNOTIFYMessage())
    {
      synchronized(lock)
      {
        CPService service = getCPServiceByURLPath(notifyParser.getRequestURL(), controlPoint);
        if (service == null)
        {
          logger.info("No service found for event message with URL " + notifyParser.getRequestURL() +
            " received from " + IPHelper.toString(request.getSourceAddress()));
          return;
        }
        TemplateControlPoint.printMessage(controlPoint.toString() + ": Received INITIAL EVENT via multicast from " +
          service.getCPDevice().toString() + "." + service.toString());

        service.setMulticastEventKey(notifyParser.getNumericValue(GENAConstant.SEQ));
        updateStateVariables(request, service);

        // forward event to device
        service.getCPDevice().subscriptionStateChanged(service);
      }
    }
    return;
  }

  /**
   * Verifies the event key. If the event key is wrong, a new INITIAL_EVENT message is send.
   * Otherwise, the values for all state variables found in the message are updated.
   * 
   * @param notifyParser
   *          GENANotifyParser which supplies the needed data
   * @param service
   *          service
   * @return true if event key is OK, otherwise false
   */
  protected boolean tryUpdateStateVariables(HTTPMessageObject notifyMessage, HTTPParser notifyParser, CPService service)
  {
    long seq = notifyParser.getNumericValue(GENAConstant.SEQ);

    // check if new eventkey is an increment of 1 to the old event key
    // if not, a new initial event request is send
    if (seq == service.getMulticastEventKey() + 1)
    {
      service.setMulticastEventKey(seq);
      updateStateVariables(notifyMessage, service);

      return true;
    }
    // due to race conditions, we ignore messages with the same sequence number
    if (seq == service.getMulticastEventKey())
    {
      logger.info("Received multicast event message with the current sequence number");

      return true;
    }
    return false;
  }

}

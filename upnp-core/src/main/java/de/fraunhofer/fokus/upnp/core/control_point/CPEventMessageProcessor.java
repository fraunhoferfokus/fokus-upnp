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

import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAEventBodyParser;
import de.fraunhofer.fokus.upnp.gena.GENAParseException;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class processes incoming GENA notify event messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class CPEventMessageProcessor implements IHTTPMessageProcessor
{

  /**
   * UPnP logger
   */
  protected static Logger logger = Logger.getLogger("upnp.gena");

  protected ControlPoint  controlPoint;

  protected Object        lock;

  /**
   * Creates a new instance of CPEventMessageProcessor.
   * 
   * @param controlPoint
   *          The associated control point
   */
  public CPEventMessageProcessor(ControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
    lock = new Object();
  }

  /**
   * Processes an event NOTIFY message.
   * 
   * @param request
   *          The NOTIFY message
   * 
   * @return HTTP OK or HTTP error messages
   * 
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    if (controlPoint.isSetDisableEventProcessing())
    {
      return new HTTPMessageObject(HTTPConstant.HTTP_OK_NL, request.getDestinationAddress());
    }
    // System.out.println("Received event message from " +
    // IPAddress.toString(request.getSourceAddress()));
    HTTPParser notifyParser = new HTTPParser();
    notifyParser.parse(request);
    InetSocketAddress serverAddress = request.getDestinationAddress();

    try
    {
      if (notifyParser.isEventNOTIFYMessage())
      {
        return processEventNotifyMessage(request, notifyParser);
      } else
      {
        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
      }
    } catch (GENAParseException ge)
    {
      return new HTTPMessageObject(ge.getMessage(), serverAddress);
    }
  }

  /**
   * Processes the content of the event NOTIFY message.
   * 
   * @param request
   *          The NOTIFY message
   * @param notifyParser
   *          The parser for the message
   * 
   * @return HTTP OK or HTTP error messages
   * 
   */
  protected HTTPMessageObject processEventNotifyMessage(HTTPMessageObject request, HTTPParser notifyParser) throws GENAParseException
  {
    synchronized(lock)
    {
      String sid = notifyParser.getValue(GENAConstant.SID);
      CPService service = getServiceBySID(sid);
      // SID is invalid
      if (service == null)
      {
        System.out.println("No service found for event message with SID " + sid + " received from " +
          IPHelper.toString(request.getSourceAddress()));
        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, request.getDestinationAddress());
      }
      // everything was fine
      if (tryUpdateStateVariables(request, notifyParser, service))
      {
        return new HTTPMessageObject(HTTPConstant.HTTP_OK_NL, request.getDestinationAddress());
      } else
      {
        System.out.println("a notify message must be passed unnoticed");
        logger.warn("a notify message must be passed unnoticed");
        throw new GENAParseException("a notify message must be passed unnoticed");
      }
    }
  }

  /**
   * Returns the service for a SID
   * 
   * @param sid
   *          The SID for the event
   * @return The associated service or null
   */
  protected CPService getServiceBySID(String sid)
  {
    // System.out.println("Size of event subscription thread table is " +
    // controlPoint.getEventSubscriptionThreadFromSIDTable().size());
    // check if sid exists in subscriber hashtable
    // try 3 times because the subscribe response could be gained after notify for some stacks
    for (int i = 1; i <= 3; i++)
    {
      if (controlPoint.getEventSubscriptionThreadFromSIDTable().containsKey(sid))
      {
        CPServiceEventSubscriptionThread thread =
          (CPServiceEventSubscriptionThread)controlPoint.getEventSubscriptionThreadFromSIDTable().get(sid);

        return thread.getCPService();
      } else
      {
        try
        {
          Thread.sleep(UPnPDefaults.CP_SUBSCRIBER_TEST_SLEEP_TIME);
        } catch (InterruptedException e)
        {
          logger.error("ThreadSleep not possible:" + e);
        }
      }

      if (i == 3)
      {
        logger.warn("unknown event notify message.");
        logger.warn("reason: SID " + sid + " does not exist in subscriber hashtable");
      }
    }
    return null;
  }

  /**
   * Verifies the event key. If the event key is wrong, an unsubscribe and resubscribe for this
   * service is initiated. Otherwise, the values for all state variables found in the message are
   * updated.
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

    // check if first notify message
    if (seq == 0)
    {
      TemplateControlPoint.printMessage(controlPoint.toString() + ": Received INITIAL EVENT from " +
        service.getCPDevice().toString() + "." + service.toString() + " (SID:" +
        notifyParser.getValue(GENAConstant.SID) + ")");

      GENAEventBodyParser bodyParser = new GENAEventBodyParser();
      bodyParser.parseMessageBody(notifyMessage);

      Vector variableNames = bodyParser.getVariableNames();
      Vector variableValues = bodyParser.getVariableValues();

      for (int i = 0; i < variableNames.size(); i++)
      {
        TemplateControlPoint.printMessage(controlPoint.toString() + ":   " + (String)variableNames.elementAt(i) +
          " -> " + (String)variableValues.elementAt(i));
      }

      service.setEventKey(0);
      updateStateVariables(notifyMessage, service);

      return true;
    } else
    {
      // check if new eventkey is an increment of 1 to the old event key
      // if not, subscription must be cancelled and an new subscription must be send
      if (seq == service.getEventKey() + 1)
      {
        service.setEventKey(seq);
        updateStateVariables(notifyMessage, service);

        return true;
      } else
      {
        System.out.println("Received later notify with seq " + seq);
        logger.warn("unknown event notify message.");
        logger.warn("reason: event notify SEQ number is unsycnchronized. new SEQ = " + seq + "old SEQ = " +
          service.getEventKey());

        service.sendUnsubscription();
        ThreadHelper.sleep(300);
        service.sendSubscription();
      }
    }
    return false;
  }

  /**
   * Sets the new state variable values from the notify message body for the particular service
   * 
   * @param notifyMessage
   *          Received message
   * @param service
   *          the particular service to be used
   */
  protected void updateStateVariables(HTTPMessageObject notifyMessage, CPService service)
  {
    if (service == null)
    {
      logger.warn("notify message error: service id not found");

      return;
    }
    GENAEventBodyParser bodyParser = new GENAEventBodyParser();
    bodyParser.parseMessageBody(notifyMessage);

    Vector variableNames = bodyParser.getVariableNames();
    Vector variableValues = bodyParser.getVariableValues();

    for (int i = 0; i < variableNames.size(); i++)
    {
      String variableName = (String)variableNames.elementAt(i);
      String variableValue = (String)variableValues.elementAt(i);
      CPStateVariable stateVariable = service.getCPStateVariable(variableName);

      if (stateVariable != null)
      {
        try
        {
          // System.out.println("Set CP state variable " + variableName + " to value: " +
          // variableValue);
          stateVariable.setValueFromString(variableValue);
        } catch (Exception e)
        {
          logger.error("cannot set state variable value:" + e);
        }
      } else
      {
        logger.warn("notify event error: variable " + variableName + " not found in service " + service.getServiceId());
      }
    }
  }
}

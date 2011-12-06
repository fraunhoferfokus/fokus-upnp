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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessageFlow;
import de.fraunhofer.fokus.upnp.http.HTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageFlow;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.URLHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class is used to send GENA event NOTIFY messages to a subscriber or to a multicast address.
 * 
 * @author Alexander Koenig
 */
public class DeviceSendEventNotifyThread extends Thread implements IHTTPMessageFlow
{

  /** GENA logger */
  private static Logger                         logger              = Logger.getLogger("upnp.gena");

  protected static String                       TCP                 = "TCP";

  protected static String                       UDP                 = "UDP";

  protected static String                       UDP_MULTICAST       = "UDP_MULTICAST";

  /** Reference to associated subscriber thread */
  protected DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread;

  protected int                                 retries;

  protected int                                 IPVersion;

  protected int                                 eventErrors         = 0;

  /** Errors for using UDP eventing */
  protected int                                 udpEventErrors      = 0;

  protected long                                currentEventKey;

  protected boolean                             terminateThread     = false;

  protected boolean                             terminated          = false;

  /** Vector holding collected state variable events */
  protected Vector                              eventObjects        = new Vector();

  /** Vector holding state variable events that are currently sent */
  protected Vector                              pendingEventObjects = new Vector();

  protected Object                              eventLock           = new Object();

  /** Reference to current client that sends event NOTIFY messages */
  // protected HTTPClient currentClient;
  /** Reference to current client that sends event NOTIFY messages */
  protected HTTPOverUDPClient                   currentUDPClient;

  /** Reference to current client that sends event NOTIFY messages */
  // protected HTTPOverMulticastUDPClient multicastUDPClient;
  /** Optional multicast IP address for events */
  private InetSocketAddress                     multicastDeliveryAddress;

  /** Optional multicast URL for events */
  private URL                                   multicastDeliveryURL;

  /** Associated service */
  private DeviceService                         service;

  /** Creates a new instance of DeviceSendEventNotifyThread */
  public DeviceSendEventNotifyThread(DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread)
  {
    super("DeviceSendEventNotifyThread [" + deviceSubscribedControlPointThread.getDeviceService().toString() + " for " +
      ((URL)deviceSubscribedControlPointThread.getDeliveryURLs().elementAt(0)).toString() + "]");

    this.deviceSubscribedControlPointThread = deviceSubscribedControlPointThread;
    IPVersion = deviceSubscribedControlPointThread.getDeviceService().getIPVersion();

    start();
  }

  /** Creates a new instance of DeviceSendEventNotifyThread for multicast event delivery */
  public DeviceSendEventNotifyThread(DeviceService deviceService, InetSocketAddress deliveryAddress)
  {
    super("DeviceSendEventNotifyThread [" + deviceService.toString() + " for " + IPHelper.toString(deliveryAddress) +
      "]");

    this.multicastDeliveryAddress = deliveryAddress;
    this.service = deviceService;
    // URL can be created as soon as the device UDN is known (after TemplateDevice.runDevice())
    multicastDeliveryURL = null;
    currentEventKey = 1;
    IPVersion = deviceService.getIPVersion();

    start();
  }

  /** Sends the initial event message */
  public void sendInitialEvent(Vector eventObjects)
  {
    synchronized(eventLock)
    {
      retries = UPnPDefaults.DEVICE_INITIAL_NOTIFY_RETRIES;
      // set initial event key
      currentEventKey = deviceSubscribedControlPointThread.incrementEventKey();

      this.eventObjects.addAll(eventObjects);
    }
  }

  /** Adds new events targeted to this subscriber to the event queue. */
  public void addEventObjects(Vector eventObjects)
  {
    synchronized(eventLock)
    {
      // check if there are outstanding events
      if (this.eventObjects.size() != 0)
      {
        if (deviceSubscribedControlPointThread != null)
        {
          System.out.println(deviceSubscribedControlPointThread.getSID() + ": Add new events to " +
            this.eventObjects.size() + " pending messages");
        }
      }
      retries = UPnPDefaults.DEVICE_EVENT_NOTIFY_RETRIES;
      // add all events that are either targeted to all subscribers
      // or have the same public key as this subscriber
      for (int i = 0; i < eventObjects.size(); i++)
      {
        StateVariableEventObject currentEventObject = (StateVariableEventObject)eventObjects.elementAt(i);
        if (currentEventObject.getPublicKey() == null || deviceSubscribedControlPointThread != null &&
          currentEventObject.getPublicKey().equals(deviceSubscribedControlPointThread.getSubscriberPublicKey()))
        {
          this.eventObjects.add(currentEventObject);
        }
      }
      if (this.eventObjects.size() > 0 && deviceSubscribedControlPointThread != null)
      {
        deviceSubscribedControlPointThread.getDeviceService().incEventTriggerCount();
      }
    }
  }

  /** Retrieves the number of event messages that could not be sent */
  public int getSendEventErrors()
  {
    return eventErrors;
  }

  /** Sends an outstanding event message */
  protected void trySendEventObjects()
  {
    // check if there is work to do
    if (eventObjects.size() > 0)
    {
      //      long sendStart = HighResTimerHelper.getTimeStamp();

      // build message body because it does not change for the different delivery mechanisms
      byte[] messageBody = null;
      synchronized(eventLock)
      {
        messageBody = StringHelper.utf8StringToByteArray(GENAMessageBuilder.buildNotifyBody(eventObjects));

        // copy to pending event vectors
        // this is done to allow a later retry for event sending
        pendingEventObjects.addAll(eventObjects);
        // clear event vectors
        eventObjects.clear();
      }
      Hashtable messageOptions = new Hashtable();
      messageOptions.put(HTTPMessageFlow.MESSAGE_OPTION_KEY_BODY, messageBody);
      // add SID for individual subscribers
      if (deviceSubscribedControlPointThread != null)
      {
        messageOptions.put(GENAConstant.SID, deviceSubscribedControlPointThread.getSID());
      }

      // this thread is a multicast event handler
      if (multicastDeliveryAddress != null)
      {
        // check if all data is set
        if (multicastDeliveryURL != null)
        {
          messageOptions.put(HTTPMessageFlow.MESSAGE_OPTION_KEY_MESSAGE_TYPE, UDP_MULTICAST);
          sendEventsOverMulticastUDP(messageOptions);
        } else
        {
          System.out.println("Try to send multicast event packet, but delivery URL is still invalid");
        }
        return;
      }
      // standard subscriber event handling
      // send small messages over UDP if possible
      if (deviceSubscribedControlPointThread.getUDPDeliveryURLs() != null &&
        deviceSubscribedControlPointThread.getUDPDeliveryURLs().size() > 0 && udpEventErrors < 5 &&
        messageBody.length < 1000)
      {
        messageOptions.put(HTTPMessageFlow.MESSAGE_OPTION_KEY_MESSAGE_TYPE, UDP);

        // add UDP client
        if (currentUDPClient == null)
        {
          System.out.println("    DeviceSendEventNotifyThread: Create new UDP client");
          currentUDPClient = new HTTPOverUDPClient(false);
        }
        messageOptions.put(HTTPMessageFlow.MESSAGE_OPTION_KEY_UDP_CLIENT, currentUDPClient);
        sendEventsOverUDP(messageOptions);
      } else
      {
        messageOptions.put(HTTPMessageFlow.MESSAGE_OPTION_KEY_MESSAGE_TYPE, TCP);
        sendEventsOverTCP(messageOptions);
      }
      //      long time = HighResTimerHelper.getTimeStamp();
      //      long duration = HighResTimerHelper.getMicroseconds(sendStart, time) / 1000;
      //      System.out.print(duration + ",");
      // performance evaluation
      deviceSubscribedControlPointThread.getDeviceService().incEventHandleCount();
    }
  }

  /** Handles the case that delivery to all known URLs fails. */
  protected void handleSendError()
  {
    eventErrors++;
    // copy pending variables back to variables due to error
    synchronized(eventLock)
    {
      if (UPnPDefaults.DEVICE_FAULT_TOLERANT_EVENT_COLLECTION)
      {
        // add at start to keep event order
        eventObjects.addAll(0, pendingEventObjects);
        // clear pending variables after copying back
        pendingEventObjects.clear();
        if (!terminateThread)
        {
          System.out.println("Event send error. " + eventObjects.size() + " outstanding events. Error count is " +
            eventErrors);
        }
      } else
      {
        System.out.println("Event send error. Force resubscribe.");
        // update event key for next message, this forces a resubscribe by the control point
        currentEventKey = deviceSubscribedControlPointThread.incrementEventKey();
      }
    }
  }

  /** Sends events with a HTTP client. This is the standardized UPnP way for event handling. */
  protected void sendEventsOverTCP(Hashtable messageOptions)
  {
    Vector deliveryURLs = deviceSubscribedControlPointThread.getDeliveryURLs();

    // try to deliver message
    for (int i = 0; !terminateThread && i < retries; i++)
    {
      // go through all deliveryUrls until one hits
      for (Enumeration ee = deliveryURLs.elements(); !terminateThread && ee.hasMoreElements();)
      {
        URL deliveryURL = (URL)ee.nextElement();

        if (IPVersion == UPnPConstant.IP4 && deliveryURL.getHost().indexOf("[") != -1)
        {
          continue;
        }
        if (IPVersion == UPnPConstant.IP6 && deliveryURL.getHost().indexOf("[") == -1)
        {
          continue;
        }
        Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, deliveryURL, this);
        if (result != null && result instanceof Boolean)
        {
          return;
        }
      }
    }
    handleSendError();
  }

  /** Tries to send events over UDP to speed up event handling. */
  protected void sendEventsOverUDP(Hashtable messageOptions)
  {
    Vector deliveryURLs = deviceSubscribedControlPointThread.getUDPDeliveryURLs();

    // try to deliver message
    for (int i = 0; !terminateThread && i < retries; i++)
    {
      // go through all deliveryUrls until one hits
      for (Enumeration ee = deliveryURLs.elements(); !terminateThread && ee.hasMoreElements();)
      {
        URL deliveryURL = (URL)ee.nextElement();

        if (IPVersion == UPnPConstant.IP4 && deliveryURL.getHost().indexOf("[") != -1)
        {
          continue;
        }
        if (IPVersion == UPnPConstant.IP6 && deliveryURL.getHost().indexOf("[") == -1)
        {
          continue;
        }
        Object result = HTTPMessageFlow.sendMessageOverUDPAndProcessResponse(messageOptions, deliveryURL, this);
        if (result != null && result instanceof Boolean)
        {
          return;
        }
      }
    }
    System.out.println("Could not send event message over UDP to " + deliveryURLs.elementAt(0));

    udpEventErrors++;
    handleSendError();
  }

  /** Tries to send events over multicast UDP to speed up event handling. */
  protected void sendEventsOverMulticastUDP(Hashtable messageOptions)
  {
    HTTPMessageFlow.sendMessageOverMulticastUDP(messageOptions, multicastDeliveryURL, this);

    /*
     * if (notifyMessage.toByteArray().length < 1400) {
     * multicastUDPClient.sendRequest(notifyMessage); } else { logger.error("Message is too large
     * for multicast delivery."); }
     */
    synchronized(eventLock)
    {
      // always clear pending variables
      pendingEventObjects.clear();
      // update event key for next message
      currentEventKey++;
    }
  }

  /**
   * Retrieves the multicastDeliveryURL.
   * 
   * @return The multicastDeliveryURL
   */
  public URL getMulticastEventDeliveryURL()
  {
    return multicastDeliveryURL;
  }

  /**
   * Sets the multicastDeliveryURL.
   * 
   */
  public void setMulticastEventDeliveryURL()
  {
    if (multicastDeliveryAddress != null)
    {
      try
      {
        multicastDeliveryURL =
          new URL("http://" + IPHelper.toString(multicastDeliveryAddress) + "/" +
            URLHelper.escapeURL(service.getDevice().getUDN()) + "/" +
            URLHelper.escapeURL(service.getShortenedServiceId()));
      } catch (Exception e)
      {
        multicastDeliveryURL = null;
        logger.error("Invalid delivery address for multicast eventing: " + e.getMessage());
      }
    }
  }

  /**
   * Retrieves the currentEventKey.
   * 
   * @return The currentEventKey
   */
  public long getCurrentEventKey()
  {
    return currentEventKey;
  }

  public void run()
  {
    while (!terminateThread)
    {
      trySendEventObjects();
      ThreadHelper.sleep(10);
    }
    terminated = true;
    // TemplateService.printMessage(deviceSubscribedControlPointThread.getDeviceService().toString()
    // +
    // ": Send event thread for " +
    // ((URL)deviceSubscribedControlPointThread.getDeliveryURLs().elementAt(0)).toString() + " was
    // shut down");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createRequest(java.util.Hashtable, java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL targetURL)
  {
    byte[] body = (byte[])messageOptions.get(HTTPMessageFlow.MESSAGE_OPTION_KEY_BODY);
    String sid = (String)messageOptions.get(GENAConstant.SID);

    String header =
      GENAMessageBuilder.buildNotify(targetURL.getFile(),
        targetURL.getHost(),
        targetURL.getPort(),
        sid,
        currentEventKey + "",
        body.length + "");

    HTTPMessageObject result = new HTTPMessageObject(header, body, null, IPHelper.toSocketAddress(targetURL));

    String messageType = (String)messageOptions.get(HTTPMessageFlow.MESSAGE_OPTION_KEY_MESSAGE_TYPE);
    // 
    if (messageType.equals(UDP_MULTICAST) && result.toByteArray().length > 1400)
    {
      logger.error("Message is too large for multicast delivery.");
      return null;
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#modifyRequest(java.util.Hashtable, java.net.URL,
   *      de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public void modifyRequest(Hashtable messageOptions, URL targetURL, HTTPMessageObject request)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createResponseParser(java.util.Hashtable,
   *      java.net.URL)
   */
  public HTTPParser createResponseParser(Hashtable messageOptions, URL targetURL)
  {
    return new HTTPParser();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#processResponse(java.util.Hashtable,
   *      java.net.URL, de.fhg.fokus.magic.util.network.HTTPMessageObject,
   *      de.fhg.fokus.magic.http.HTTPParser)
   */
  public Object processResponse(Hashtable messageOptions,
    URL targetURL,
    HTTPMessageObject response,
    HTTPParser responseParser)
  {
    // check if response is HTTP OK => message received
    if (responseParser.isHTTPOKResponse())
    {
      synchronized(eventLock)
      {
        // System.out.println("Event message was sent successfully, " +
        // " already pending event size is " + variableNames.size());
        // clear pending variables because they have been sent successfully
        pendingEventObjects.clear();
        // update event key for next message
        currentEventKey = deviceSubscribedControlPointThread.incrementEventKey();
      }
      return new Boolean(true);
    }
    return null;
  }

  /** Terminates the event thread */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
  }

}

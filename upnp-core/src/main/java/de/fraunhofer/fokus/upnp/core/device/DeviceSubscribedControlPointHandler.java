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

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class stores data of a subscriber and controls the eventing key for the subscriber. It is
 * also used to create the thread that sends event messages to the subscriber.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceSubscribedControlPointHandler implements IEventListener
{

  private String                      sid;

  private Vector                      deliveryURLs;

  /** Optional delivery URLs for UDP */
  private Vector                      udpDeliveryURLs;

  private RSAPublicKey                subscriberPublicKey = null;

  private long                        eventKey;

  private DeviceService               deviceService;

  private Object                      lock                = new Object();

  private long                        subscriptionStartTime;

  private int                         subscriptionTimeout = UPnPDefaults.DEVICE_SUBSCRIPTION_TIMEOUT;

  private DeviceSendEventNotifyThread sendEventNotifyThread;

  private boolean                     initialEventSent    = false;

  /**
   * Creates a new DeviceSubscribedControlPointThread object.
   * 
   * @param service
   *          Associated device service
   * @param sid
   *          subscription uuid of subscriber
   * @param deliveryURLs
   *          urls of the subscriber
   * @param udpDeliveryURLs
   *          UDP urls of the subscriber
   * @param timeout
   *          requested timeout of subscription
   */
  public DeviceSubscribedControlPointHandler(DeviceService service,
    String sid,
    Vector deliveryURLs,
    Vector udpDeliveryURLs,
    String timeout)
  {
    this.sid = sid;
    this.eventKey = 0;
    this.deliveryURLs = deliveryURLs;
    this.udpDeliveryURLs = udpDeliveryURLs;
    this.deviceService = service;

    if (udpDeliveryURLs != null && udpDeliveryURLs.size() > 0)
    {
      TemplateService.printMessage(service.toString() + ": Create new subscriber handler for UDP callback " +
        ((URL)udpDeliveryURLs.elementAt(0)).toString() + " with sid " + sid + "...");
    } else
    {
      TemplateService.printMessage(service.toString() + ": Create new subscriber handler for TCP callback " +
        ((URL)deliveryURLs.elementAt(0)).toString() + " with sid " + sid + "...");
    }

    setSubscriptionTimeout(timeout);
    // create thread to deliver event messages
    sendEventNotifyThread = service.createDeviceSendEventNotifyThread(this);

    deviceService.getDevice().getDeviceEventThread().register(this);
  }

  /**
   * Returns subscriber's delivery URLs
   * 
   * @return subscriber's delivery URLs
   */
  public Vector getDeliveryURLs()
  {
    return deliveryURLs;
  }

  /**
   * Returns subscriber's UDP delivery URLs
   * 
   * @return subscriber's UDP delivery URLs
   */
  public Vector getUDPDeliveryURLs()
  {
    return udpDeliveryURLs;
  }

  /** Returns the subscription identifier for this subscriber */
  public String getSID()
  {
    return sid;
  }

  /** Retrieves the associated service. */
  public DeviceService getDeviceService()
  {
    return deviceService;
  }

  /** Retrieves the timeout for the subscriber. */
  public int getSubscriptionTimeout()
  {
    return subscriptionTimeout;
  }

  /**
   * Sets a new subscription timeout
   * 
   * @param timeout
   *          Requested timeout
   */
  public void setSubscriptionTimeout(String timeout)
  {
    // update start time for subscription
    subscriptionStartTime = System.currentTimeMillis();
    try
    {
      // remove text before the value
      int hyphenIndex = timeout.lastIndexOf("-");
      if (hyphenIndex != -1 && hyphenIndex < timeout.length() - 1)
      {
        timeout = timeout.substring(hyphenIndex + 1).trim();
      }
      // we limit infinite subscriptions
      if (timeout.equalsIgnoreCase(GENAConstant.INFINITE))
      {
        subscriptionTimeout = UPnPDefaults.DEVICE_SUBSCRIPTION_TIMEOUT;
      } else
      {
        subscriptionTimeout = Integer.parseInt(timeout); // in ms
      }
    } catch (NumberFormatException e1)
    {
    }
    // we limit subscriptions to our timeout limit
    subscriptionTimeout = Math.min(UPnPDefaults.DEVICE_SUBSCRIPTION_TIMEOUT, subscriptionTimeout);
  }

  /**
   * Increments the subscriber event key.
   * 
   * @return the last event key for the subscriber
   */
  public long incrementEventKey()
  {
    synchronized(lock)
    {
      eventKey++;

      return eventKey - 1;
    }
  }

  /**
   * Sends the first event message to the subscriber with the names and current status of all state
   * variables of this service.
   */
  private void sendInitialEventMessage()
  {
    // get all values and names of variables of this service
    Vector eventObjects = new Vector();

    deviceService.fillInitialEventObjectList(eventObjects, subscriberPublicKey);

    // TemplateService.printMessage(deviceService + ": Send initial event message with " +
    // variableNames.size() + " values to subscriber");
    sendEventNotifyThread.sendInitialEvent(eventObjects);
    initialEventSent = true;
  }

  /** Adds pending event messages to the associated send event thread. */
  public void addEventObjects(Vector eventObjects)
  {
    // prevent sending of events before initial event was sent
    if (initialEventSent)
    {
      //      System.out.println(deviceService + ": Send event message with " + eventObjects.size() + " values to subscriber");

      // forward to event thread
      sendEventNotifyThread.addEventObjects(eventObjects);
    }
  }

  /**
   * Retrieves the subscriberPublicKey.
   * 
   * @return The subscriberPublicKey
   */
  public RSAPublicKey getSubscriberPublicKey()
  {
    return subscriberPublicKey;
  }

  /**
   * Sets the subscriberPublicKey.
   * 
   * @param subscriberPublicKey
   *          The new value for subscriberPublicKey
   */
  public void setSubscriberPublicKey(RSAPublicKey subscriberPublicKey)
  {
    this.subscriberPublicKey = subscriberPublicKey;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (!initialEventSent &&
      System.currentTimeMillis() - subscriptionStartTime > UPnPDefaults.DEVICE_INITIAL_NOTIFY_DELAY)
    {
      sendInitialEventMessage();
    }
    if (System.currentTimeMillis() - subscriptionStartTime > subscriptionTimeout * 1000)
    {
      TemplateDevice.printMessage("DeviceSubscribedControlPointThread: Missing resubscription, " +
        "cancel subscription for sid " + sid + " with callback URL " + deliveryURLs.elementAt(0));
      terminate();
    }
    // remove subscriber if there are too many errors
    if (sendEventNotifyThread.getSendEventErrors() > 5)
    {
      TemplateDevice.printMessage("DeviceSubscribedControlPointThread: Number of send errors is too high, " +
        "cancel subscription for sid " + sid + " with callback URL " + deliveryURLs.elementAt(0));
      terminate();
    }
  }

  /** Terminates the subscriber thread */
  public void terminate()
  {
    deviceService.getDevice().getDeviceEventThread().unregister(this);
    // terminate associated thread for event sending
    sendEventNotifyThread.terminate();
    // this method also calls terminate so we need to set terminated before to prevent deadlocks
    deviceService.removeSubscriber(sid);
  }

}

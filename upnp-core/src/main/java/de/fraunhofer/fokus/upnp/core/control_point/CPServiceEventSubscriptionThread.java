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
import java.security.interfaces.RSAPublicKey;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAMessageBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.http.HTTPMessageFlow;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageFlow;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This thread is started in the CPService for each successful subscription. The thread is put into
 * a hashtable in the control point for further reference. This thread automatically resubscribes
 * (if timeout is not set to infinite) to the corresponing service if a certain configurable timeout
 * is reached
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class CPServiceEventSubscriptionThread extends Thread implements IHTTPMessageFlow
{

  protected static String MESSAGE_TYPE_TAG = "MessageType";

  protected static String SUBSCRIBE        = "Subscribe";

  protected static String RESUBSCRIBE      = "Resubscribe";

  protected static String UNSUBSCRIBE      = "Unsubscribe";

  protected static Logger logger           = Logger.getLogger("upnp.gena");

  /** URL to send subscription messages to. */
  protected URL           publisherURL;

  /** File part to the publisher URl */
  protected String        publisherPath;                                   // required

  /** HOST part (IP) to the publisher URl */
  protected String        publisherHost;                                   // required

  /** PORT part to the publisher URl */
  protected int           publisherPort;                                   // required

  /** URLs which should be used to receive notify messages */
  protected Vector        deliveryURLs;                                    // required

  protected String        deliveryURLString;

  /** URLs which should be used to receive notify messages */
  protected Vector        udpDeliveryURLs;                                 // optional

  protected String        udpDeliveryURLString;

  /** Requested timeout for subscription */
  protected String        timeoutwish;                                     // recommended

  /** Associated service */
  protected CPService     service;

  /** Associated SID */
  protected String        sid;

  /** Received timeout */
  protected String        receivedTimeout;

  protected ControlPoint  controlPoint;

  protected boolean       terminateThread  = false;

  protected boolean       terminated       = false;

  protected boolean       sendUnsubscription;

  /**
   * Creates a new event subscription thread.
   * 
   * @param service
   *          Service that should be subscribed to
   * @param eventSubscriptionURL
   *          URL where to send the subscription
   * @param deliveryURLs
   *          URLs where notify messages are to be sent
   * @param timeoutWish
   *          timeout wish of the subscription
   * @param IPVersion
   *          Used IP version
   * @param controlPoint
   *          Associated control point
   * 
   */
  public CPServiceEventSubscriptionThread(CPService service,
    URL eventSubscriptionURL,
    Vector deliveryURLs,
    String timeoutWish,
    int IPVersion,
    ControlPoint controlPoint)
  {
    this(service, eventSubscriptionURL, deliveryURLs, null, timeoutWish, IPVersion, controlPoint);
  }

  /**
   * Creates a new event subscription thread.
   * 
   * @param service
   *          Service that should be subscribed to
   * @param eventSubscriptionURL
   *          URL where to send the subscription
   * @param deliveryURLs
   *          URLs where notify messages are to be sent
   * @param udpDeliveryURLs
   *          URLs where UDP notify messages are to be sent
   * @param timeoutWish
   *          timeout wish of the subscription
   * @param IPVersion
   *          Used IP version
   * @param controlPoint
   *          Associated control point
   * 
   */
  public CPServiceEventSubscriptionThread(CPService service,
    URL eventSubscriptionURL,
    Vector deliveryURLs,
    Vector udpDeliveryURLs,
    String timeoutWish,
    int IPVersion,
    ControlPoint controlPoint)
  {
    super("CPServiceEventSubscriptionThread [" + controlPoint.toString() + "]");

    // TemplateService.printMessage(controlPoint.toString() + ": Start event subscription thread" +
    // " for service " + service.getCPDevice() + "." + service +
    // ", requested interval is " + timeoutwish + " seconds");

    this.service = service;
    this.publisherURL = eventSubscriptionURL;
    this.publisherPath = eventSubscriptionURL.getFile();
    this.publisherHost = eventSubscriptionURL.getHost();
    this.publisherPort = eventSubscriptionURL.getPort();
    this.controlPoint = controlPoint;

    if (publisherPort == -1)
    {
      publisherPort = CommonConstants.HTTP_DEFAULT_PORT;
    }

    // initialize callback URLs
    this.deliveryURLs = deliveryURLs;
    deliveryURLString = "";
    for (int i = 0; i < deliveryURLs.size(); i++)
    {
      deliveryURLString += "<" + deliveryURLs.elementAt(i) + ">";
    }

    // initialize UDP callback URLs
    this.udpDeliveryURLs = udpDeliveryURLs;
    udpDeliveryURLString = "";
    if (udpDeliveryURLs != null)
    {
      for (int i = 0; i < udpDeliveryURLs.size(); i++)
      {
        udpDeliveryURLString += "<" + udpDeliveryURLs.elementAt(i) + ">";
      }
    }

    this.timeoutwish = timeoutWish;
  }

  /**
   * Returns the associated service.
   * 
   * @return The associated service
   */
  public CPService getCPService()
  {
    return service;
  }

  /**
   * Builds subscribe message
   * 
   * @return subscribe message
   */
  protected String buildSubscribeMessage()
  {
    CPDevice cpDevice = service.getCPDevice();
    CPService securedPersonalizationService =
      cpDevice.getCPServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);
    TemplateControlPoint templateControlPoint = cpDevice.getControlPoint().getTemplateControlPoint();

    String subscribeMessage =
      GENAMessageBuilder.buildSubscribe(publisherPath,
        publisherHost,
        publisherPort,
        deliveryURLs,
        udpDeliveryURLs,
        timeoutwish);

    // personalization not possible
    if (securedPersonalizationService == null || templateControlPoint == null)
    {
      return subscribeMessage;
    }

    // System.out.println("Try to send personalized SUBSCRIBE");
    // check for existing symmetric key
    SymmetricKeyInfo symmetricKeyInfo = cpDevice.getPersonalizationSymmetricKeyInfo();
    if (symmetricKeyInfo != null)
    {
      synchronized(symmetricKeyInfo.getLock())
      {
        // System.out.println("Create personalized GENA request signed with symmetric key");
        symmetricKeyInfo.incSequence();

        // build content for signature
        String signatureContent = symmetricKeyInfo.getKeyID();
        signatureContent += symmetricKeyInfo.getSequence() + "";
        // add callback URLs for SUBSCRIBE messages
        signatureContent += deliveryURLString;
        signatureContent += udpDeliveryURLString;

        byte[] signature = DigestHelper.calculateSHA1HMACForString(symmetricKeyInfo.getAESKey(), signatureContent);
        String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

        // add signature to original message
        subscribeMessage +=
          HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_KEY_ID, symmetricKeyInfo.getKeyID());
        subscribeMessage +=
          HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SEQUENCE, symmetricKeyInfo.getSequence() + "");
        subscribeMessage += HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64);
      }
      return subscribeMessage;
    }

    // asymmetric personalization
    // System.out.println("Create personalized GENA request signed with private key");

    RSAPublicKey publicKey = templateControlPoint.getPersonalizationPublicKey();
    // request nonce
    String nonce;
    try
    {
      nonce = templateControlPoint.invokeGetNonce(securedPersonalizationService, publicKey);
    } catch (Exception e)
    {
      System.out.println("Could not request nonce for personalized SUBSCRIBE: " + e.getMessage());
      return subscribeMessage;
    }
    String publicKeyXMLDescription = SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey);

    // build content for signature
    String signatureContent = "";
    signatureContent += publicKeyXMLDescription;
    signatureContent += nonce;
    // add callback URLs for SUBSCRIBE messages
    signatureContent += deliveryURLString;
    signatureContent += udpDeliveryURLString;

    byte[] signature =
      PublicKeyCryptographyHelper.calculateRSASignatureForString(templateControlPoint.getPersonalizationPrivateKey(),
        signatureContent);

    String signatureBase64 = Base64Helper.byteArrayToBase64(signature);

    // add signature to original message
    subscribeMessage += HTTPHeaderBuilder.buildHeader(HTTPConstant.X_NONCE, nonce);
    subscribeMessage +=
      HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY, publicKeyXMLDescription);
    subscribeMessage += HTTPHeaderBuilder.buildHeader(HTTPConstant.X_PERSONALIZATION_SIGNATURE, signatureBase64);

    return subscribeMessage;
  }

  /**
   * build the resubscribe message
   * 
   * @return the resubscribe message as a string
   */
  protected String buildResubscribeMessage()
  {
    return GENAMessageBuilder.buildResubscribe(publisherPath, publisherHost, publisherPort, sid, timeoutwish);
  }

  /**
   * build the unsubscribe message
   * 
   * @return the unsubscribe message as a string
   */
  protected String buildUnsubscribeMessage()
  {
    return GENAMessageBuilder.buildUnsubscribe(publisherPath, publisherHost, publisherPort, sid);
  }

  /** Calculates the sleep time to the next resubscription in ms */
  protected long getResubscriptionSleepInterval(String subscribeReplyTimeout)
  {
    String original = subscribeReplyTimeout;
    int result = UPnPDefaults.CP_SUBSCRIPTION_TIMEOUT;

    // remove text before the value
    int hyphenIndex = subscribeReplyTimeout.lastIndexOf("-");
    if (hyphenIndex != -1 && hyphenIndex < subscribeReplyTimeout.length() - 1)
    {
      subscribeReplyTimeout = subscribeReplyTimeout.substring(hyphenIndex + 1).trim();
    }

    // process only non infinite values
    if (!subscribeReplyTimeout.equalsIgnoreCase(GENAConstant.INFINITE))
    {
      try
      {
        result = Integer.parseInt(subscribeReplyTimeout);
      } catch (Exception e)
      {
      }
    }
    if (result < 0)
    {
      System.out.println("Received negative timeout: " + original + " from " + service.getCPDevice().getFriendlyName() +
        "." + service.toString());

      // use 5 minutes
      return 300;
    }

    // sleep 80% of the received resubscription time
    return Math.round(result * 0.8 * 1000);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#createRequest(java.util.Hashtable, java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL targetURL)
  {
    String messageType = (String)messageOptions.get(MESSAGE_TYPE_TAG);
    if (messageType.equals(SUBSCRIBE))
    {
      // initial subscription
      return new HTTPMessageObject(buildSubscribeMessage(), publisherURL);
    }
    if (messageType.equals(RESUBSCRIBE))
    {
      // initial subscription
      return new HTTPMessageObject(buildResubscribeMessage(), publisherURL);
    }
    if (messageType.equals(UNSUBSCRIBE))
    {
      // initial subscription
      return new HTTPMessageObject(buildUnsubscribeMessage(), publisherURL);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.http.IHTTPMessageFlow#modifyRequest(java.util.Hashtable, java.net.URL,
   *      de.fhg.fokus.magic.util.network.HTTPMessageObject)
   */
  public void modifyRequest(Hashtable messageOptions, URL targetURL, HTTPMessageObject request)
  {
    // modify message if necessary
    if (controlPoint.getSubscribeModifier() != null)
    {
      controlPoint.getSubscribeModifier().modifyHTTPRequest(request);
    }
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
    String messageType = (String)messageOptions.get(MESSAGE_TYPE_TAG);
    if (messageType.equals(SUBSCRIBE) || messageType.equals(RESUBSCRIBE))
    {
      if (responseParser.isSUBSCRIBEResponseMessage())
      {
        String sid = responseParser.getValue(GENAConstant.SID);
        receivedTimeout = responseParser.getValue(HTTPConstant.TIMEOUT);

        // System.out.println("CPServiceEventSubscriptionThread: Received SUBSCRIBE response with
        // SID " + sid);

        // check for first subscription
        if (!controlPoint.getEventSubscriptionThreadFromSIDTable().containsKey(sid))
        {
          logger.info("Put " + sid + " in subscriber table.");
          controlPoint.getEventSubscriptionThreadFromSIDTable().put(sid, this);
          // store received SID and timeout
          this.sid = sid;
          return new Boolean(true);
        }
        // resubscription
        return new Boolean(sid.equals(this.sid));
      } else
      {
        System.out.println("Received invalid response to SUBSCRIBE or RESUBSCRIBE message for " +
          service.getCPDevice().getFriendlyName() + ":" + service.getServiceType() + "\n " +
          responseParser.getHTTPMessageObject().getHeader() + "\n");
      }
    }
    if (messageType.equals(UNSUBSCRIBE))
    {
      if (responseParser.isHTTPOKResponse())
      {
        return new Boolean(true);
      } else
      {
        System.out.println("Received invalid response to UNSUBSCRIBE message for " +
          service.getCPDevice().getFriendlyName() + ":" + service.getServiceType() + "\n " +
          responseParser.getHTTPMessageObject().getHeader() + "\n");
      }
    }
    return new Boolean(false);
  }

  /** Sends a subscription, resubscription or unsubscription request to a device. */
  public void sendGENAMessage(String messageType)
  {
    Hashtable messageOptions = new Hashtable();
    messageOptions.put(MESSAGE_TYPE_TAG, messageType);

    Object result = HTTPMessageFlow.sendMessageAndProcessResponse(messageOptions, publisherURL, this);
    terminateThread = !(result instanceof Boolean && ((Boolean)result).booleanValue());
  }

  /** This method is responsible for handling one subscription. */
  public void run()
  {
    // send subscription request
    if (service.getCPDevice().supportsPersonalization())
    {
      synchronized(service.getCPDevice().getPersonalizationSyncLock())
      {
        sendGENAMessage(SUBSCRIBE);
      }
    } else
    {
      sendGENAMessage(SUBSCRIBE);
    }
    while (!terminateThread)
    {
      // signal subscription to CPService
      service.setSubscribed(true);

      long sleepTime = getResubscriptionSleepInterval(receivedTimeout);

      // System.out.println(getName() + ": Try to sleep for " + sleepTime/60000 + " minutes");

      try
      {
        Thread.sleep(sleepTime);

        // send resubscription
        sendGENAMessage(RESUBSCRIBE);

        // update sleep time if possible
        if (!terminateThread)
        {
          sleepTime = getResubscriptionSleepInterval(receivedTimeout);
        }

      } catch (InterruptedException e)
      {
        terminateThread = true;
      }
    }

    // end event subscription
    if (sendUnsubscription && service.isSubscribed())
    {
      // System.out.println("Send unsubscribe for " + service.getDevice().getFriendlyName()+"." +
      // service.toString());
      logger.info("send unsubscribe");

      sendGENAMessage(UNSUBSCRIBE);
    }
    // remove thread
    if (sid != null)
    {
      // remove thread from hashtable
      controlPoint.getEventSubscriptionThreadFromSIDTable().remove(sid);
    }
    service.setSubscribed(false);
    terminated = true;
    // TemplateControlPoint.printMessage(controlPoint.toString() + ":" +
    // "Terminated subscription to " + service.getCPDevice().getFriendlyName()+"." +
    // service.toString() + " (SID:" + sid + ")");
  }

  /**
   * Terminates the event subscription thread.
   * 
   * 
   * @param sendUnsubscription
   *          True to send an UNSUBSCRIBE, false otherwise
   */
  public void terminate(boolean sendUnsubscription)
  {
    this.sendUnsubscription = sendUnsubscription;
    terminateThread = true;
    this.interrupt();
    while (!terminated)
    {
      ThreadHelper.sleep(10);
    }
    System.out.println("  " + service.getCPDevice().getControlPoint().toString() +
      ": Shutdown event subscription thread for " + service.getCPDevice().getFriendlyName() + "." + service.toString() +
      "...");
  }

}

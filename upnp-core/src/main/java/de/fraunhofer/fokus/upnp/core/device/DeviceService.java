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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.core.AbstractService;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.personalization.SecuredPersonalizationService;
import de.fraunhofer.fokus.upnp.core.event.IDeviceInvokedActionListener;
import de.fraunhofer.fokus.upnp.core.event.IDeviceStateVariableListener;
import de.fraunhofer.fokus.upnp.core.exceptions.DescriptionNotCreatedException;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.HighResTimerHelper;
import de.fraunhofer.fokus.upnp.util.XMLConstant;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.PersonalizedKeyObject;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;

/**
 * This class provides methods that are needed by a device service but not by a CPService.
 * 
 * @author icu, Alexander Koenig
 */
public class DeviceService extends AbstractService implements IDeviceStateVariableListener
{

  /**
   * UPnP logger
   */
  public static Logger                 logger                       = Logger.getLogger("upnp.device");

  public static int                    EVENT_HANDLED_COUNT          = 0;

  public static int                    EVENT_TRIGGERED_COUNT        = 0;

  public static long                   EVENT_HANDLED_START          = 0;

  /** Hashtable containing threads for all subscribed control points */
  private Hashtable                    subscriberThreadFromSIDTable = new Hashtable();

  /** Collector for service events. */
  private DeviceServiceEventCollector  eventCollector;

  /** Optional send thread for multicast events */
  private DeviceSendEventNotifyThread  multicastSendEventNotifyThread;

  /** Action listener */
  private IDeviceInvokedActionListener actionListener               = null;

  /** Associated device */
  private Device                       device;

  /** SOAP control cache */
  private Hashtable                    controlMessageCache          = new Hashtable();

  /**
   * Creates DeviceService object
   * 
   * @param serviceType
   *          type of the service
   * @param serviceId
   *          the ID of this service
   */
  public DeviceService(Device device, String serviceType, String serviceId, int IPVersion)
  {
    super(serviceType, serviceId, IPVersion);
    System.out.println("    Create service " + toString() + "...");
    this.device = device;
    registerStateVariableListener();
    eventCollector = new DeviceServiceEventCollector(this);
    // check if this service should start a multicast event thread
    if (device.getMulticastEventSocketAddress() != null && canUseMulticastEvents())
    {
      TemplateService.printMessage("Start multicast event notify thread to address " +
        IPHelper.toString(device.getMulticastEventSocketAddress()));

      multicastSendEventNotifyThread = new DeviceSendEventNotifyThread(this, device.getMulticastEventSocketAddress());
    }
  }

  /** This method is used for performance evaluation. */
  public void incEventHandleCount()
  {
    synchronized(this)
    {
      EVENT_HANDLED_COUNT++;
      if (EVENT_HANDLED_COUNT == subscriberThreadFromSIDTable.size())
      {
        long time = HighResTimerHelper.getTimeStamp();
        long duration = HighResTimerHelper.getMicroseconds(EVENT_HANDLED_START, time) / 1000;

        System.out.println("\r\nEvent handling took " + duration);
      }
    }
  }

  /** This method is used for performance evaluation. */
  public void incEventTriggerCount()
  {
    EVENT_TRIGGERED_COUNT++;
  }

  /**
   * Returns the stateVariable specified by the stateVariableName
   * 
   * @param stateVariableName
   *          the name of the stateVariable
   * @return stateVariable specified by the stateVariableName or null
   */
  public StateVariable getStateVariable(String stateVariableName)
  {
    for (int i = 0; stateVariableTable != null && i < stateVariableTable.length; i++)
    {
      if (stateVariableName.equalsIgnoreCase(stateVariableTable[i].getName()))
      {
        return (StateVariable)stateVariableTable[i];
      }
    }
    return null;
  }

  /**
   * Returns stateVariableTable
   * 
   * @return all service's stateVariable or null
   */
  public StateVariable[] getStateVariableTable()
  {
    return (StateVariable[])stateVariableTable;
  }

  /**
   * Sets the stateVariableTable. The array is not copied.
   * 
   * @param stateVariableTable
   *          all service's stateVariable or null
   */
  public void setStateVariableTable(StateVariable[] stateVariableTable)
  {
    for (int i = 0; stateVariableTable != null && i < stateVariableTable.length; i++)
    {
      if (stateVariableTable[i] == null)
      {
        System.out.println("STATE VARIABLE IN TABLE IS NULL");
      }
    }
    this.stateVariableTable = stateVariableTable;
    registerStateVariableListener();
  }

  /**
   * Sets this device service as listener for all state variable changes.
   */
  private void registerStateVariableListener()
  {
    for (int i = 0; stateVariableTable != null && i < stateVariableTable.length; i++)
    {
      if (stateVariableTable[i].isEvented())
      {
        ((StateVariable)stateVariableTable[i]).setStateVariableChangeListener(this);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.IDeviceStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.StateVariable)
   */
  public void stateVariableChanged(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    // check if state variable change can be handled directly
    if (stateVariable.canSendEvent())
    {
      eventCollector.addVariable(stateVariable, publicKey);
    } else
    {
      // add to event collector for later processing
      eventCollector.addModeratedVariable(stateVariable, publicKey);
    }
  }

  /** Allows an adaption of the thread collection time based on the current number of subscribers */
  public int getThreadCollectionTime()
  {
    if (UPnPDefaults.DEVICE_ADAPTIVE_EVENT_COLLECTION_INTERVAL)
    {
      // increase time every five subscribers
      return Math.max(300, Math.min(5000, 300 * subscriberThreadFromSIDTable.size() / 5));
    } else
    {
      return 300;
    }
  }

  /**
   * Retrieves the initial value for a certain state variable. May be overridden by descendant
   * classes, e.g., for LastChange initialization.
   * 
   * @param stateVariable
   *          The state variable
   * @param publicKey
   *          Optional public key for personalized events or null
   * 
   * @return The initial value for this state variable
   */
  public Object getInitialStateVariableValue(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    if (publicKey == null)
    {
      return stateVariable.getValue();
    } else
    {
      return stateVariable.getPersonalizedValue(publicKey);
    }
  }

  /**
   * Fills a vector with the initial state variable names and values.
   * 
   * @param eventObjects
   *          List with StateVariableEventObjects
   * @param publicKey
   *          Optional public key for personalized events or null
   * 
   */
  public void fillInitialEventObjectList(Vector eventObjects, RSAPublicKey publicKey)
  {
    // get all values and names of variables of this service
    if (stateVariableTable != null)
    {
      for (int i = 0; i < stateVariableTable.length; i++)
      {
        if (stateVariableTable[i].isEvented())
        {
          Object initialValue = getInitialStateVariableValue((StateVariable)stateVariableTable[i], publicKey);
          eventObjects.add(new StateVariableEventObject(stateVariableTable[i].getName(),
            initialValue.toString(),
            publicKey));
        }
      }
    }
  }

  /**
   * Distributes pending event messages to all subscribed control points.
   * 
   * @param eventObjects
   *          List with StateVariableEventObjects
   * 
   */
  public void distributeEventObjects(Vector eventObjects)
  {
    System.out.println(DateTimeHelper.formatCurrentDateForDebug() + " Distribute event for " + getServiceType() + " (" +
      EVENT_HANDLED_COUNT + "/" + EVENT_TRIGGERED_COUNT + "/" + subscriberThreadFromSIDTable.size() + ")");
    EVENT_HANDLED_COUNT = 0;
    EVENT_TRIGGERED_COUNT = 0;
    EVENT_HANDLED_START = HighResTimerHelper.getTimeStamp();
    // read all subscribers from table
    for (Enumeration e = subscriberThreadFromSIDTable.elements(); e.hasMoreElements();)
    {
      DeviceSubscribedControlPointHandler subscriber = (DeviceSubscribedControlPointHandler)e.nextElement();
      subscriber.addEventObjects(eventObjects);
    }
    if (multicastSendEventNotifyThread != null)
    {
      multicastSendEventNotifyThread.addEventObjects(eventObjects);
    }
  }

  /**
   * Creates a new subscriber for this service.
   * 
   * @param sid
   *          New subscriber uuid
   * @param deliveryUrls
   *          delivery Callback urls of the control point
   * @param udpDeliveryURLs
   *          UDP delivery callback urls of the control point
   * @param timeout
   *          Requested timeout for the subscription
   * 
   * @return Timeout for subscription as created by the device
   */
  public int addSubscriber(String sid, Vector deliveryUrls, Vector udpDeliveryURLs, HTTPParser subscribeParser)
  {
    String timeout = subscribeParser.getValue(HTTPConstant.TIMEOUT);

    // create thread for new subscriber
    DeviceSubscribedControlPointHandler newSubscriber =
      new DeviceSubscribedControlPointHandler(this, sid, deliveryUrls, udpDeliveryURLs, timeout);

    // handle personalized subscriptions
    boolean validSignature = false;
    SecuredPersonalizationService securedPersonalizationService =
      (SecuredPersonalizationService)device.getDeviceServiceByType(DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE);

    boolean error = securedPersonalizationService == null;
    if (!error)
    {
      // check symmetric signature
      if (subscribeParser.hasField(HTTPConstant.X_PERSONALIZATION_KEY_ID) &&
        subscribeParser.hasField(HTTPConstant.X_PERSONALIZATION_SIGNATURE) &&
        subscribeParser.hasField(HTTPConstant.X_PERSONALIZATION_SEQUENCE))
      {
        // System.out.println("Received symmetric key signed SUBSCRIBE");
        String keyID = subscribeParser.getValue(HTTPConstant.X_PERSONALIZATION_KEY_ID);
        PersonalizedKeyObject keyObject = securedPersonalizationService.getPersonalizedKeyObjectFromKeyID(keyID);

        error |= keyObject == null;
        long receivedSequenceBase = -1;
        if (!error)
        {
          // check sequence base
          long storedSequenceBase = keyObject.getSequenceBase();
          receivedSequenceBase = subscribeParser.getNumericValue(HTTPConstant.X_PERSONALIZATION_SEQUENCE);

          error |= receivedSequenceBase == -1 || receivedSequenceBase <= storedSequenceBase;
        }
        if (!error)
        {
          keyObject.setSequenceBase(receivedSequenceBase);
          SecretKey aesKey = keyObject.getAESKey();

          String signatureContent =
            keyID + receivedSequenceBase + subscribeParser.getValue(CommonConstants.CALLBACK) +
              subscribeParser.getValue(CommonConstants.UDP_CALLBACK);

          validSignature =
            DigestHelper.verifySHA1HMACForString(subscribeParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE),
              signatureContent,
              aesKey);

          error |= !validSignature;
        }
        if (!error)
        {
          // System.out.println("Found valid signature for symmetric key signed SUBSCRIBE message");
          newSubscriber.setSubscriberPublicKey(keyObject.getPersistentRSAPublicKey());
        } else
        {
          System.out.println("Found invalid, symmetric key signed SUBSCRIBE message");
        }
      }
      // public key signature
      if (subscribeParser.hasField(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY) &&
        subscribeParser.hasField(HTTPConstant.X_PERSONALIZATION_SIGNATURE) &&
        subscribeParser.hasField(HTTPConstant.X_NONCE))
      {
        // System.out.println("Received private key signed SUBSCRIBE");
        PersonalizedKeyObject keyObject = null;
        PersistentRSAPublicKey publicKey = null;
        // compare nonce
        String receivedNonce = subscribeParser.getValue(HTTPConstant.X_NONCE);
        error |= receivedNonce == null;

        if (!error)
        {
          // asymmetric signature
          publicKey = subscribeParser.getPublicKey(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY);
          keyObject = securedPersonalizationService.getPersonalizedKeyObjectFromRSAKey(publicKey);

          error |= keyObject == null;
        }
        if (!error)
        {
          String storedNonce = keyObject.getNonce();
          error |= storedNonce == null || !receivedNonce.equals(storedNonce);
        }
        if (!error)
        {
          // clear nonce to prevent replay
          keyObject.setNonce(null);
          String publicKeyXMLDescription = SecurityHelper.buildRSAPublicKeyXMLDescription(publicKey);
          String signatureContent =
            publicKeyXMLDescription + receivedNonce + subscribeParser.getValue(CommonConstants.CALLBACK) +
              subscribeParser.getValue(CommonConstants.UDP_CALLBACK);

          validSignature =
            PublicKeyCryptographyHelper.verifyRSASignatureForString(publicKey,
              signatureContent,
              subscribeParser.getValue(HTTPConstant.X_PERSONALIZATION_SIGNATURE));
          error |= !validSignature;
          if (!validSignature)
          {
            System.out.println("Signature is invalid");
          }
        }
        if (!error)
        {
          // System.out.println("Found valid signature for private key signed SUBSCRIBE message");
          newSubscriber.setSubscriberPublicKey(keyObject.getPersistentRSAPublicKey());
        } else
        {
          System.out.println("Found invalid, private key signed SUBSCRIBE message");
        }
      }
    }

    // add new subscriber thread
    subscriberThreadFromSIDTable.put(sid, newSubscriber);

    return newSubscriber.getSubscriptionTimeout();
  }

  /**
   * Creates a new thread to deliver event messages. This method can be overwritten by descendant
   * services.
   */
  protected DeviceSendEventNotifyThread createDeviceSendEventNotifyThread(DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread)
  {
    return new DeviceSendEventNotifyThread(deviceSubscribedControlPointThread);
  }

  /**
   * Checks if a potential subscriber is already known
   * 
   * @param deliveryUrls
   *          delivery Urls of the control point
   */
  public DeviceSubscribedControlPointHandler getSubscriber(Vector deliveryUrls)
  {
    Enumeration e = subscriberThreadFromSIDTable.elements();
    while (e.hasMoreElements())
    {
      DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread =
        (DeviceSubscribedControlPointHandler)e.nextElement();
      // get old delivery URLs
      Vector knownURLs = deviceSubscribedControlPointThread.getDeliveryURLs();

      for (int i = 0; i < knownURLs.size(); i++)
      {
        URL tmpUrl = (URL)knownURLs.get(i);
        // compare to all new delivery URLs
        for (int j = 0; j < deliveryUrls.size(); j++)
        {
          if (tmpUrl.equals(deliveryUrls.get(j)))
          {
            return deviceSubscribedControlPointThread;
          }
        }
      }
    }
    return null;
  }

  /**
   * Renews a subscriber timeout.
   * 
   * @param sid
   *          subscriber uuid
   * @param timeout
   *          new timeout
   * 
   * @return Accepted timeout
   */
  public int renewSubscriber(String sid, String timeout)
  {
    DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread =
      (DeviceSubscribedControlPointHandler)subscriberThreadFromSIDTable.get(sid);
    // update timeout for subscription
    deviceSubscribedControlPointThread.setSubscriptionTimeout(timeout);

    return deviceSubscribedControlPointThread.getSubscriptionTimeout();
  }

  /**
   * Removes subscriber from subscriber hashtable
   * 
   * @param uuid
   *          uuid of subscriber
   */
  public void removeSubscriber(String sid)
  {
    DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread =
      (DeviceSubscribedControlPointHandler)subscriberThreadFromSIDTable.remove(sid);

    // end thread if found
    if (deviceSubscribedControlPointThread != null)
    {
      deviceSubscribedControlPointThread.terminate();
    }
  }

  /**
   * Returns subscriber information specified by subscriber uuid.
   * 
   * @param sid
   *          uuid of subscriber
   * @return subscriber information
   */
  public DeviceSubscribedControlPointHandler getSubscriber(String sid)
  {
    return (DeviceSubscribedControlPointHandler)subscriberThreadFromSIDTable.get(sid);
  }

  /**
   * Tests if a specific subscriber is registered in subscriber hashtable
   * 
   * @param sid
   *          uuid of subscriber
   * @return true if subscriber is registered false otherwise
   */
  public boolean isKnownSubscriber(String sid)
  {
    return subscriberThreadFromSIDTable.containsKey(sid);
  }

  /**
   * Sets actionList. For each Action, a DeviceAction instance is created.
   * 
   * @param actionTable
   *          all actions offered by service or null
   */
  public void setActionTable(Action[] actionTable)
  {
    this.actionTable = actionTable;
  }

  /**
   * Returns the action for the specified actionName.
   * 
   * @param actionName
   *          the name of Action
   * 
   * @return the Action specified by the actionName
   */
  public Action getAction(String actionName)
  {
    Action action = null;

    if (actionTable != null)
    {
      for (int i = 0; i < actionTable.length; i++)
      {
        if (actionName.equalsIgnoreCase(actionTable[i].getName()))
        {
          action = (Action)actionTable[i];

          break;
        }
      }
    }
    return action;
  }

  /**
   * Sets an action listener for this service
   * 
   * @param iaListener
   *          action listener
   */
  public void setActionListener(IDeviceInvokedActionListener iaListener)
  {
    actionListener = iaListener;
  }

  /**
   * Sends action to the action listener.
   * 
   * @param action
   *          action
   * @return true if action can be performed by the action listener, false otherwise
   */
  public boolean invokeLocalAction(Action action)
  {
    // reset error
    action.clearError();

    return actionListener.invokeLocalAction(serviceId, action);
  }

  /** Retrieves the absolute SCPD URL for a network interface. */
  public String getSCPDURL(String serverAddress)
  {
    return "http://" + serverAddress + getRelativeSCPDURL();
  }

  /** Retrieves the relative SCPD URL for this service. */
  public String getRelativeSCPDURL()
  {
    return device.getRelativeServiceURL(this, UPnPConstant.SUFFIX_SCPD);
  }

  /** Retrieves the absolute URL where to send event subscriptions to this service. */
  public String getEventSubscriptionURL(String serverAddress)
  {
    return "http://" + serverAddress + getRelativeEventSubscriptionURL();
  }

  /** Retrieves the relative URL where to send event subscriptions to this service. */
  public String getRelativeEventSubscriptionURL()
  {
    return device.getRelativeServiceURL(this, UPnPConstant.SUFFIX_EVENTING);
  }

  /**
   * Retrieve absolute control url
   */
  public String getControlURL(String serverAddress)
  {
    return "http://" + serverAddress + getRelativeControlURL();
  }

  /**
   * Retrieve relative control url
   */
  public String getRelativeControlURL()
  {
    return device.getRelativeServiceURL(this, UPnPConstant.SUFFIX_CONTROL);
  }

  /**
   * Creates a serviceDescription in XML format.
   * 
   * @return serviceDescription in XML as a string
   * @throws DescriptionNotCreatedException
   *           error if description could not be created
   */
  public String toXMLDescription() throws DescriptionNotCreatedException
  {
    // service description is independent from calling server
    if (serviceDescription != null)
    {
      return serviceDescription;
    }

    StringBuffer serviceDescriptionBuffer = new StringBuffer();

    serviceDescriptionBuffer.append(XMLConstant.XML_VERSION + CommonConstants.NEW_LINE);

    serviceDescriptionBuffer.append(XMLHelper.createStartTag(XMLConstant.SCPD + " " + XMLConstant.XMLNS_SERVICE) +
      CommonConstants.NEW_LINE);
    serviceDescriptionBuffer.append(XMLHelper.createStartTag(XMLConstant.SPECVERSION_TAG) + CommonConstants.NEW_LINE);
    serviceDescriptionBuffer.append(XMLHelper.createTag(XMLConstant.MAJOR_TAG, "1"));
    serviceDescriptionBuffer.append(XMLHelper.createTag(XMLConstant.MINOR_TAG, "0"));
    serviceDescriptionBuffer.append(XMLHelper.createEndTag(XMLConstant.SPECVERSION_TAG) + CommonConstants.NEW_LINE);

    // Action
    if (actionTable != null)
    {
      serviceDescriptionBuffer.append(XMLHelper.createStartTag(XMLConstant.ACTION_LIST) + CommonConstants.NEW_LINE);
      serviceDescriptionBuffer.append(toActionXMLDescription());
      serviceDescriptionBuffer.append(XMLHelper.createEndTag(XMLConstant.ACTION_LIST) + CommonConstants.NEW_LINE);
    }

    // StateVariable
    serviceDescriptionBuffer.append(XMLHelper.createStartTag(XMLConstant.SERVICE_STATE_TABLE) +
      CommonConstants.NEW_LINE);
    serviceDescriptionBuffer.append(toStateVariableXMLDescription());
    serviceDescriptionBuffer.append(XMLHelper.createEndTag(XMLConstant.SERVICE_STATE_TABLE) + CommonConstants.NEW_LINE);
    serviceDescriptionBuffer.append(XMLHelper.createEndTag(XMLConstant.SCPD));

    serviceDescription = serviceDescriptionBuffer.toString();

    return serviceDescription;
  }

  /** Creates the XML description for all state variables */
  private StringBuffer toStateVariableXMLDescription()
  {
    String isSend;
    StringBuffer stateVariableDescription = new StringBuffer();

    for (int i = 0; i < stateVariableTable.length; i++)
    {
      isSend = stateVariableTable[i].isEvented() ? "\"yes\"" : "\"no\"";

      stateVariableDescription.append(XMLHelper.createStartTag(XMLConstant.STATE_VARIABLE + " sendEvents=" + isSend) +
        CommonConstants.NEW_LINE);
      stateVariableDescription.append(XMLHelper.createTag(XMLConstant.NAME, stateVariableTable[i].getName()));
      stateVariableDescription.append(XMLHelper.createTag(XMLConstant.DATA_TYPE,
        stateVariableTable[i].getUPnPDataType()));

      if (stateVariableTable[i].getDefaultValue() != null)
      {
        String defaultValue = stateVariableTable[i].getDefaultValueAsString();

        defaultValue = defaultValue.replaceAll("&", "&amp;");
        defaultValue = defaultValue.replaceAll("<", "&lt;");
        defaultValue = defaultValue.replaceAll(">", "&gt;");
        defaultValue = defaultValue.replaceAll("\"", "&quot;");

        stateVariableDescription.append(XMLHelper.createTag(XMLConstant.DEFAULT_VALUE, defaultValue));
      }

      if (stateVariableTable[i].getAllowedValueList() != null)
      {
        stateVariableDescription.append(XMLHelper.createStartTag(XMLConstant.ALLOWED_VALUE_LIST) +
          CommonConstants.NEW_LINE);

        String[] allowedValues = stateVariableTable[i].getAllowedValueList();
        for (int j = 0; j < allowedValues.length; j++)
        {
          stateVariableDescription.append(XMLHelper.createStartTag(XMLConstant.ALLOWED_VALUE));
          stateVariableDescription.append(allowedValues[j]);
          stateVariableDescription.append(XMLHelper.createEndTag(XMLConstant.ALLOWED_VALUE) + CommonConstants.NEW_LINE);
        }
        stateVariableDescription.append(XMLHelper.createEndTag(XMLConstant.ALLOWED_VALUE_LIST) +
          CommonConstants.NEW_LINE);
      } else if (stateVariableTable[i].getAllowedValueRange() != null)
      {
        stateVariableDescription.append(XMLHelper.createStartTag(XMLConstant.ALLOWED_VALUE_RANGE) +
          CommonConstants.NEW_LINE);

        stateVariableDescription.append(XMLHelper.createTag(XMLConstant.MINIMUM,
          stateVariableTable[i].getAllowedValueRange().getMin() + ""));
        stateVariableDescription.append(XMLHelper.createTag(XMLConstant.MAXIMUM,
          stateVariableTable[i].getAllowedValueRange().getMax() + ""));
        if (stateVariableTable[i].getAllowedValueRange().getStep() != 0)
        {
          stateVariableDescription.append(XMLHelper.createTag(XMLConstant.STEP,
            stateVariableTable[i].getAllowedValueRange().getStep() + ""));
        }
        stateVariableDescription.append(XMLHelper.createEndTag(XMLConstant.ALLOWED_VALUE_RANGE) +
          CommonConstants.NEW_LINE);
      }
      stateVariableDescription.append(XMLHelper.createEndTag(XMLConstant.STATE_VARIABLE) + CommonConstants.NEW_LINE);
    }

    return stateVariableDescription;
  }

  /** Creates the XML description for all actions */
  private StringBuffer toActionXMLDescription()
  {
    StringBuffer actionDescription = new StringBuffer();

    for (int i = 0; i < actionTable.length; i++)
    {
      actionDescription.append(XMLHelper.createStartTag(XMLConstant.ACTION) + CommonConstants.NEW_LINE);
      actionDescription.append(XMLHelper.createTag(XMLConstant.NAME, actionTable[i].getName()));

      Argument[] argumentList = actionTable[i].getArgumentTable();
      if (argumentList != null && argumentList.length > 0)
      {
        actionDescription.append(XMLHelper.createStartTag(XMLConstant.ARGUMENT_LIST) + CommonConstants.NEW_LINE);
        actionDescription.append(toArgumentXMLDescription(argumentList));
        actionDescription.append(XMLHelper.createEndTag(XMLConstant.ARGUMENT_LIST) + CommonConstants.NEW_LINE);
      }

      actionDescription.append(XMLHelper.createEndTag(XMLConstant.ACTION) + CommonConstants.NEW_LINE);
    }

    return actionDescription;
  }

  /** Creates the XML description for one argument */
  private StringBuffer toArgumentXMLDescription(Argument[] argumentList)
  {
    StringBuffer argumentDescription = new StringBuffer();

    for (int i = 0; i < argumentList.length; i++)
    {
      argumentDescription.append(XMLHelper.createStartTag(XMLConstant.ARGUMENT) + CommonConstants.NEW_LINE);
      argumentDescription.append(XMLHelper.createTag(XMLConstant.NAME, argumentList[i].getName()));
      argumentDescription.append(XMLHelper.createTag(XMLConstant.DIRECTION, argumentList[i].getDirection()));
      // TODO: check usage of retval
      // serviceDescription.append("<retval />" + HTTPConstant.NEW_LINE);
      argumentDescription.append(XMLHelper.createTag(XMLConstant.RELATED_STATE_VARIABLE,
        argumentList[i].getRelatedStateVariable().getName()));
      argumentDescription.append(XMLHelper.createEndTag(XMLConstant.ARGUMENT) + CommonConstants.NEW_LINE);
    }

    return argumentDescription;
  }

  /**
   * Retrieves the device.
   * 
   * @return The device
   */
  public Device getDevice()
  {
    return device;
  }

  /**
   * Checks if this service should be allowed to use multicast events. This method can be overriden
   * by descendant classes.
   * 
   * @return True if the service can use multicast eventing, false otherwise
   */
  public boolean canUseMulticastEvents()
  {
    return false;
  }

  /**
   * Checks if this service currently uses multicast events.
   * 
   * @return True if the service uses multicast eventing, false otherwise
   */
  public boolean useMulticastEvents()
  {
    return multicastSendEventNotifyThread != null;
  }

  /**
   * Tries to update the multicastDeliveryURL as soon as the device UDN is known.
   * 
   * @return The multicastDeliveryURL
   */
  public void trySetMulticastEventDeliveryURL()
  {
    if (device.getUDN() != null && multicastSendEventNotifyThread != null)
    {
      multicastSendEventNotifyThread.setMulticastEventDeliveryURL();
    }
  }

  /**
   * Retrieves the multicastDeliveryURL.
   * 
   * @return The multicastDeliveryURL
   */
  public URL getMulticastEventDeliveryURL()
  {
    if (multicastSendEventNotifyThread != null)
    {
      return multicastSendEventNotifyThread.getMulticastEventDeliveryURL();
    }

    return null;
  }

  /**
   * Retrieves the current event key for multicast events.
   * 
   * @return The event key for multicast events
   */
  public long getMulticastEventKey()
  {
    return multicastSendEventNotifyThread.getCurrentEventKey();
  }

  /**
   * Retrieves the controlMessageCache.
   * 
   * @return The controlMessageCache
   */
  public Hashtable getControlMessageCache()
  {
    return controlMessageCache;
  }

  /** Terminates the device service */
  public void terminate()
  {
    TemplateService.printMessage("Shutdown service " + toString() + "...");
    // terminate the event collector thread
    eventCollector.terminate();

    // terminate all threads for event handling
    // clone table because of changes to the original table via removeSubscriber()
    Enumeration subscriberTable = CollectionHelper.getPersistentElementsEnumeration(subscriberThreadFromSIDTable);
    while (subscriberTable.hasMoreElements())
    {
      DeviceSubscribedControlPointHandler subscriber =
        (DeviceSubscribedControlPointHandler)subscriberTable.nextElement();
      subscriber.terminate();
    }
    // terminate multicast send event notify thread
    if (multicastSendEventNotifyThread != null)
    {
      multicastSendEventNotifyThread.terminate();
    }

  }

}

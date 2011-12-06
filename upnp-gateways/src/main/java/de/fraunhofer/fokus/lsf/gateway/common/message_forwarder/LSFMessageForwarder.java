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
package de.fraunhofer.fokus.lsf.gateway.common.message_forwarder;

import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryMessageHelper;
import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.MessageTupel;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener;
import de.fraunhofer.fokus.lsf.core.startup.LSFChildStartupConfiguration;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is the central module for LSF message forwarding.
 * 
 * @author Alexander Koenig
 * 
 */
public class LSFMessageForwarder implements IBinaryCPDeviceEventListener, IEventListener
{

  /** Vector with all registered forwarders */
  private Vector                  forwarderModuleList       = new Vector();

  /** Reference to common startup configuration */
  private LSFStartupConfiguration startupConfiguration;

  /** Optional listener for device events */
  private Vector                  cpDeviceEventListenerList = new Vector();

  /** Thread to trigger regular actions */
  private EventThread             eventThread;

  /** Random ID for this gateway */
  private long                    id                        = (int)(Portable.random() * Integer.MAX_VALUE);

  /** Numbering scheme for forwarder modules */
  private int                     forwarderModuleNextID     = 1;

  /** Sync object */
  private Object                  lock                      = new Object();

  /**
   * Creates a new instance of LSFMessageForwarder.
   * 
   * @param startupConfiguration
   */
  public LSFMessageForwarder(LSFStartupConfiguration startupConfiguration)
  {
    this.startupConfiguration = startupConfiguration;
    if (startupConfiguration.getGatewayStartupInfoList().size() == 0)
    {
      System.out.println("Missing gateway startup infos. Exit application");
      return;
    }
    eventThread = new EventThread("LSFMessageForwarder");
    eventThread.register(this);
    eventThread.start();
  }

  /** Adds a module to this forwarder */
  public synchronized void addForwarderModule(ILSFForwarderModule module)
  {
    if (forwarderModuleList.indexOf(module) == -1)
    {
      forwarderModuleList.add(module);
      // add to new list to generate initial NOTIFY messages for this module
      //      ssdpManagement.addNewForwarderModule(module);
    }
  }

  /** Removes a module from this forwarder */
  public synchronized void removeForwarderModule(ILSFForwarderModule module)
  {
    // send NOTIFY byebye messages for all devices that were connected over this module
    //    ssdpManagement.sendFinalNotifyByeByeMessages(module);
    // remove after byebye message sending
    forwarderModuleList.remove(module);
  }

  /** Retrieves a module by its ID */
  public ILSFForwarderModule getForwarderModuleByID(byte moduleID)
  {
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ILSFForwarderModule module = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
      if (module.getModuleID() == moduleID)
      {
        return module;
      }
    }
    return null;
  }

  /** Retrieves an array with all forwarder modules */
  public synchronized ILSFForwarderModule[] getForwarderModuleArray()
  {
    ILSFForwarderModule[] result = new ILSFForwarderModule[forwarderModuleList.size()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
    }
    return result;
  }

  /** Retrieves the number of forwarder modules */
  public synchronized int getForwarderModuleCount()
  {
    return forwarderModuleList.size();
  }

  /**
   * Retrieves all devices for a certain forwarder module.
   * 
   * @param moduleID
   *          The module ID
   * 
   * @return A vector with all devices connected over that forwarder module
   */
  public Vector getDevicesForForwarderModule(String moduleID)
  {
    Vector result = new Vector();

    //    for (int i = 0; i < templateControlPoint.getCPDeviceCount(); i++)
    //    {
    //      CPDevice currentDevice = templateControlPoint.getCPDevice(i);
    //      IForwarderModule forwarderModule = getForwarderModuleByDevice(currentDevice);
    //      if (forwarderModule != null && forwarderModule.getModuleID().equals(moduleID))
    //      {
    //        result.add(currentDevice);
    //      }
    //    }
    //    System.out.println("Number of devices for " + moduleID + " is " + result.size() + " out of " +
    //      templateControlPoint.getCPDeviceCount());
    return result;
  }

  /**
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public LSFStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves a gateway startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public LSFChildStartupConfiguration getGatewayStartupConfiguration(String className)
  {
    if (startupConfiguration.getSingleGatewayStartupConfiguration() != null)
    {
      return (LSFChildStartupConfiguration)startupConfiguration.getSingleGatewayStartupConfiguration();
    }
    return (LSFChildStartupConfiguration)startupConfiguration.getGatewayStartupConfiguration(className);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#newDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void newDevice(BinaryCPDevice newDevice)
  {
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((IBinaryCPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).newDevice(newDevice);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#changedDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice, int)
   */
  public void changedDevice(BinaryCPDevice changedDevice, int eventCode)
  {
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((IBinaryCPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).changedDevice(changedDevice, eventCode);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#deviceGone(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void deviceGone(BinaryCPDevice goneDevice)
  {
    for (int i = 0; i < cpDeviceEventListenerList.size(); i++)
    {
      ((IBinaryCPDeviceEventListener)cpDeviceEventListenerList.elementAt(i)).deviceGone(goneDevice);
    }
  }

  /**
   * Adds a cpDeviceEventListener.
   * 
   * @param cpDeviceEventListener
   *          A new cpDeviceEventListener
   */
  public void addCPDeviceEventListener(IBinaryCPDeviceEventListener cpDeviceEventListener)
  {
    if (!cpDeviceEventListenerList.contains(cpDeviceEventListener))
    {
      cpDeviceEventListenerList.add(cpDeviceEventListener);
    }
  }

  /**
   * Removes a cpDeviceEventListener.
   * 
   * @param cpDeviceEventListener
   *          The removed cpDeviceEventListener
   */
  public void removeCPDeviceEventListener(IBinaryCPDeviceEventListener cpDeviceEventListener)
  {
    cpDeviceEventListenerList.remove(cpDeviceEventListener);
  }

  /** Processes a received announcement message. */
  public void processAnnouncementMessage(ILSFForwarderModule sourceForwarderModule, BinaryMessageObject message)
  {
    //    Portable.println("Process announcement");

    // parse complete message to retrieve all access entities
    Vector tupelList = new Vector();
    Vector accessEntityList = new Vector();
    Vector responseEntityList = new Vector();
    long deviceID = BinaryMessageHelper.getDeviceIDFromMessage(message.getBody());
    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, accessEntityList, responseEntityList);

    //    Portable.println("Message for " + deviceID + " contains " + tupelList.size() + " normal tupels, " +
    //      accessEntityList.size() + " access entities and " + responseEntityList.size() + " response entities");

    // check for hop count
    if (accessEntityList.size() > 3)
    {
      return;
    }
    // check for loops 
    for (int i = 0; i < accessEntityList.size(); i++)
    {
      GatewayData currentGatewayData = (GatewayData)accessEntityList.elementAt(i);
      if (currentGatewayData.getID() == id)
      {
        return;
      }
    }
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ILSFForwarderModule currentForwarderModule = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
      if (currentForwarderModule != sourceForwarderModule &&
        currentForwarderModule.canForwardAnnouncementMessage(deviceID))
      {
        //        Portable.println("Forward announcement");
        // clone multicast message
        currentForwarderModule.forwardAnnouncementMessage(sourceForwarderModule.getModuleID(),
          (BinaryMessageObject)message.clone());
      }
    }
  }

  /** Processes a search received via a forwarder module */
  public void processSearchMessage(ILSFForwarderModule sourceForwarderModule, BinaryMessageObject message)
  {
    //    Portable.println("Process search");

    // parse complete message to retrieve all access entities
    Vector tupelList = new Vector();
    Vector responseEntityList = new Vector();

    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, null, responseEntityList);

    //    Portable.println("Message contains " + tupelList.size() + " normal tupels and " + responseEntityList.size() +
    //      " response entities");

    // check for hop count
    if (responseEntityList.size() > 3)
    {
      return;
    }
    // check for loops 
    for (int i = 0; i < responseEntityList.size(); i++)
    {
      GatewayData currentGatewayData = (GatewayData)responseEntityList.elementAt(i);
      if (currentGatewayData.getID() == id)
      {
        Portable.println("Search message loop detected");
        return;
      }
    }
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ILSFForwarderModule currentForwarderModule = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
      if (currentForwarderModule != sourceForwarderModule &&
        currentForwarderModule.canForwardSearchMessage(sourceForwarderModule.getModuleID()))
      {
        //        Portable.println("Forward search");
        // clone multicast message
        currentForwarderModule.forwardSearchMessage(sourceForwarderModule.getModuleID(),
          (BinaryMessageObject)message.clone());
      }
    }
  }

  /** Processes a unicast message received via a forwarder module */
  public void processUnicastMessage(ILSFForwarderModule sourceForwarderModule, BinaryMessageObject message)
  {
    //    Portable.println(sourceForwarderModule.toString() + ":Received unicast:");
    StringHelper.printDebugText("", true, sourceForwarderModule.toString() + ":Received unicast from " +
      IPHelper.toString(message.getSourceAddress()) + ":", BinaryUPnPConstants.toForwarderDebugString("",
      message.getBody(),
      message.getSourceAddress().getAddress()));

    // parse complete message to retrieve all access entities
    Vector tupelList = new Vector();
    Vector accessEntityList = new Vector();
    Vector responseEntityList = new Vector();

    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, accessEntityList, responseEntityList);

    // determine type
    byte messageType = message.getBody()[0];

    // handle search response
    if (messageType == BinaryUPnPConstants.UnitTypeDeviceAnnouncement)
    {
      if (responseEntityList.size() == 0)
      {
        Portable.println("Received packet can not be routed");
        return;
      }
      GatewayData lastResponseEntity = (GatewayData)responseEntityList.lastElement();
      if (lastResponseEntity.getResponsePortAsInt() == -1)
      {
        Portable.println("Received packet can not be routed due to missing response port");
        return;
      }
      ILSFForwarderModule responseForwarderModule = getForwarderModuleByID((byte)lastResponseEntity.getForwarderID());
      if (responseForwarderModule != null)
      {
        responseForwarderModule.forwardSearchResponseMessage(sourceForwarderModule.getModuleID(),
          message,
          lastResponseEntity.getResponseSocketAddress());
      }
    }
    // handle device requests
    if (messageType == BinaryUPnPConstants.UnitTypeGetDeviceDescription ||
      messageType == BinaryUPnPConstants.UnitTypeGetServiceDescription ||
      messageType == BinaryUPnPConstants.UnitTypeGetServiceValue ||
      messageType == BinaryUPnPConstants.UnitTypeSetServiceValue ||
      messageType == BinaryUPnPConstants.UnitTypeInvokeAction ||
      messageType == BinaryUPnPConstants.UnitTypeSetDeviceName ||
      messageType == BinaryUPnPConstants.UnitTypeSetDeviceApplication)
    {
      if (accessEntityList.size() == 0)
      {
        Portable.println("Received packet can not be routed");
        return;
      }
      GatewayData lastAccessEntity = (GatewayData)accessEntityList.lastElement();
      // determine destination socket address
      int destinationPort = 0;
      if (messageType == BinaryUPnPConstants.UnitTypeGetDeviceDescription ||
        messageType == BinaryUPnPConstants.UnitTypeGetServiceDescription)
      {
        destinationPort = lastAccessEntity.getDescriptionPortAsInt();
      } else
      {
        destinationPort = lastAccessEntity.getControlPortAsInt();
      }
      ILSFForwarderModule accessForwarderModule = getForwarderModuleByID((byte)lastAccessEntity.getForwarderID());
      if (accessForwarderModule != null)
      {
        accessForwarderModule.forwardDeviceRequestMessage(sourceForwarderModule.getModuleID(),
          message,
          new InetSocketAddress(lastAccessEntity.getForwarderInetAddress(), destinationPort));
      }
    }
    // handle device responses
    if (messageType == BinaryUPnPConstants.UnitTypeDeviceDescription ||
      messageType == BinaryUPnPConstants.UnitTypeServiceDescription ||
      messageType == BinaryUPnPConstants.UnitTypeServiceValueResult ||
      messageType == BinaryUPnPConstants.UnitTypeActionResult ||
      messageType == BinaryUPnPConstants.UnitTypeSetDeviceNameResult ||
      messageType == BinaryUPnPConstants.UnitTypeSetDeviceApplicationResult)
    {
      if (responseEntityList.size() == 0)
      {
        Portable.println("Received packet can not be routed");
        return;
      }
      GatewayData lastResponseEntity = (GatewayData)responseEntityList.lastElement();
      if (lastResponseEntity.getResponsePortAsInt() == -1)
      {
        Portable.println("Received packet can not be routed due to missing response port");
        return;
      }
      ILSFForwarderModule responseForwarderModule = getForwarderModuleByID((byte)lastResponseEntity.getForwarderID());
      if (responseForwarderModule != null)
      {
        responseForwarderModule.forwardDeviceResponseMessage(sourceForwarderModule.getModuleID(),
          message,
          lastResponseEntity.getResponseSocketAddress());
      }
    }
  }

  /** Processes an event received via a forwarder module */
  public void processEventMessage(ILSFForwarderModule sourceForwarderModule, BinaryMessageObject message)
  {
    //    Portable.println("Process event");

    // parse complete message to find accessID tupels
    Vector tupelList = new Vector();
    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, null, null);
    long deviceID = BinaryMessageHelper.getDeviceIDFromMessage(message.getBody());

    int hopCount = 0;
    // check for loops 
    for (int i = 0; i < tupelList.size(); i++)
    {
      MessageTupel currentMessageTupel = (MessageTupel)tupelList.elementAt(i);
      if (currentMessageTupel.getTupelType() == BinaryUPnPConstants.UnitTypeAccessID)
      {
        long accessID =
          ByteArrayHelper.byteArrayToInt64(currentMessageTupel.getPayload(), 0, currentMessageTupel.getPayloadLength());
        if (accessID == id)
        {
          Portable.println("Event message loop detected");
          return;
        }
        hopCount++;
      }
    }
    // check for hop count
    if (hopCount > 3)
    {
      return;
    }
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ILSFForwarderModule currentForwarderModule = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
      if (currentForwarderModule != sourceForwarderModule && currentForwarderModule.canForwardEventMessage(deviceID))
      {
        //        Portable.println("Forward event");
        // clone multicast message
        currentForwarderModule.forwardEventMessage((BinaryMessageObject)message.clone());
      }
    }
  }

  /**
   * Retrieves the value of eventThread.
   * 
   * @return The value of eventThread
   */
  public EventThread getEventThread()
  {
    return eventThread;
  }

  /**
   * Retrieves the value of forwarderModuleNextID.
   * 
   * @return The value of forwarderModuleNextID
   */
  public int getForwarderModuleNextID()
  {
    return forwarderModuleNextID;
  }

  /**
   * Sets the new value for forwarderModuleNextID.
   * 
   * @param forwarderModuleNextID
   *          The new value for forwarderModuleNextID
   */
  public void setForwarderModuleNextID(int forwarderModuleNextID)
  {
    this.forwarderModuleNextID = forwarderModuleNextID;
  }

  /**
   * Retrieves the value of lock.
   * 
   * @return The value of lock
   */
  public Object getLock()
  {
    return lock;
  }

  /**
   * Retrieves the value of id.
   * 
   * @return The value of id
   */
  public long getID()
  {
    return id;
  }

  public void triggerEvents()
  {
    // read messages from all modules
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ILSFForwarderModule currentForwarderModule = (ILSFForwarderModule)forwarderModuleList.elementAt(i);
      BinaryMessageObject message = null;
      // handle discovery messages
      do
      {
        message =
          SocketHelper.readBinaryMessage(null,
            currentForwarderModule.getDiscoverySocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);
        if (message != null && message.getBody() != null && message.getBody().length > 0)
        {
          byte messageType = message.getBody()[0];
          if (messageType == BinaryUPnPConstants.UnitTypeDeviceAnnouncement)
          {
            // start handling in receiving module
            currentForwarderModule.receivedAnnouncementMessage(message);
          }
          if (messageType == BinaryUPnPConstants.UnitTypeSearchDevice)
          {
            // start handling in receiving module
            currentForwarderModule.receivedSearchMessage(message);
          }
        }
      } while (message != null);

      // handle unicast messages
      do
      {
        message =
          SocketHelper.readBinaryMessage(null,
            currentForwarderModule.getUnicastSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);
        if (message != null && message.getBody() != null && message.getBody().length > 0)
        {
          processUnicastMessage(currentForwarderModule, message);
        }
      } while (message != null);

      // handle event messages
      do
      {
        message =
          SocketHelper.readBinaryMessage(null,
            currentForwarderModule.getEventSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);
        if (message != null && message.getBody() != null && message.getBody().length > 0)
        {
          byte messageType = message.getBody()[0];

          if (messageType == BinaryUPnPConstants.UnitTypeEvent)
          {
            // start handling in receiving module
            currentForwarderModule.receivedEventMessage(message);
          }
        }
      } while (message != null);
    }
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    eventThread.terminate();
    // terminate forwarder modules
    for (int i = 0; i < forwarderModuleList.size(); i++)
    {
      ((ILSFForwarderModule)forwarderModuleList.elementAt(i)).terminate();
    }
  }

}

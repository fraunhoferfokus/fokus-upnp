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
package de.fraunhofer.fokus.lsf.core.device;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.CriticalSection;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class encapsulates a local binary UPnP device.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryDevice extends AbstractBinaryDevice implements IBinaryUPnPDevice, Runnable, IEventListener
{

  /** Local device ID */
  private long                          deviceID;

  /** Last device description date */
  private long                          deviceDescriptionDate;

  /** Time of last device announcement */
  private long                          lastAnnouncement        = 0;

  /** Device description message */
  private byte[]                        descriptionMessage      = null;

  /** Device message management */
  private BinaryDeviceMessageManagement binaryDeviceMessageManagement;

  /** List with pending services */
  protected Vector                      pendingEventServiceList = new Vector();

  /** Pending services lock */
  protected Object                      pendingEventServiceLock = new Object();

  /** List with registered event listeners */
  protected Vector                      eventListenerList       = new Vector();

  private boolean                       terminateThread         = false;

  private boolean                       terminated              = false;

  /**
   * Creates a new instance of BinaryDevice.
   * 
   * @param deviceID
   * @param deviceType
   * @param expectedLifeTime
   * @param name
   * @param application
   * @param manufacturer
   */
  public BinaryDevice(long deviceID,
    int deviceType,
    int expectedLifeTime,
    String name,
    String application,
    String manufacturer)
  {
    this.deviceID = deviceID;
    this.deviceType = deviceType;
    this.expectedLifeTime = expectedLifeTime;
    this.name = name;
    this.application = application;
    this.manufacturer = manufacturer;

    binaryDeviceMessageManagement = new BinaryDeviceMessageManagement(this);

    // these may be different for different host addresses
    // this.descriptionPort = messageManagement.getDescriptionSocket().getLocalPort();
    // this.controlPort = messageManagement.getControlSocket().getLocalPort();
    this.eventPort = BinaryUPnPConstants.EventMulticastPort;

    // register myself as event listener which can be used in derived classes
    registerEventListener(this);

    new Thread(this, "BinaryDeviceEventThread").start();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice#getDeviceDescriptionSocketAddress()
   */
  public InetSocketAddress getDeviceDescriptionSocketAddress()
  {
    return new InetSocketAddress(IPHelper.getLocalHostAddress(), getDescriptionPort());
  }

  /** Adds a local service to this device. */
  public void addDeviceService(BinaryDeviceService deviceService)
  {
    CollectionHelper.tryAdd(serviceList, deviceService);
  }

  /** Adds a service with a pending event. */
  public void addEvent(BinaryDeviceService eventService)
  {
    CriticalSection.enter(pendingEventServiceLock);
    CollectionHelper.tryAdd(pendingEventServiceList, eventService);
    CriticalSection.exit(pendingEventServiceLock);
  }

  /** Adds an event listener. */
  public void registerEventListener(IEventListener eventListener)
  {
    CollectionHelper.tryAdd(eventListenerList, eventListener);
  }

  /** Removes an event listener. */
  public void unregisterEventListener(IEventListener eventListener)
  {
    eventListenerList.remove(eventListener);
  }

  /**
   * Processes a received set name command.
   * 
   * @param newName
   *          The name to set.
   */
  public void processSetName(String newName) throws ActionFailedException
  {
  }

  /**
   * Processes a received set application command.
   * 
   * @param newApplication
   *          The application to set.
   */
  public void processSetApplication(String newApplication) throws ActionFailedException
  {

  }

  /**
   * Retrieves the value of descriptionMessage.
   * 
   * @return The value of descriptionMessage
   */
  public byte[] getDescriptionMessage()
  {
    return descriptionMessage;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.control_point.BinaryDevice#getDeviceID()
   */
  public long getDeviceID()
  {
    return deviceID;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.control_point.BinaryDevice#getDeviceDescriptionDate()
   */
  public long getDeviceDescriptionDate()
  {
    return deviceDescriptionDate;
  }

  /**
   * Sets the new value for deviceDescriptionDate.
   * 
   * @param deviceDescriptionDate
   *          The new value for deviceDescriptionDate
   */
  public void setDeviceDescriptionDate(long deviceDescriptionDate)
  {
    this.deviceDescriptionDate = deviceDescriptionDate;
  }

  /** Retrieves the number of services. */
  public int getDeviceServiceCount()
  {
    return getServiceCount();
  }

  /** Retrieves a service by its index. */
  public BinaryDeviceService getDeviceService(int index)
  {
    return (BinaryDeviceService)getService(index);
  }

  /** Retrieves a service by its ID. */
  public BinaryDeviceService getDeviceServiceByID(int serviceID)
  {
    return (BinaryDeviceService)getServiceByID(serviceID);
  }

  /**
   * Retrieves a service by its type. If more than one service fits, the first service is returned.
   * 
   */
  public BinaryDeviceService getDeviceServiceByType(int serviceType)
  {
    return (BinaryDeviceService)getServiceByType(serviceType);
  }

  /**
   * Retrieves the value of lastAnnouncement.
   * 
   * @return The value of lastAnnouncement
   */
  public long getLastAnnouncement()
  {
    return lastAnnouncement;
  }

  /**
   * Sets the new value for lastAnnouncement.
   * 
   * @param lastAnnouncement
   *          The new value for lastAnnouncement
   */
  public void setLastAnnouncement(long lastAnnouncement)
  {
    this.lastAnnouncement = lastAnnouncement;
  }

  /** Parses and processes a received discovery message. */
  public void processDiscoveryMessage(BinaryMessageObject message,
    BinaryDeviceHostAddressSocketStructure socketStructure)
  {
    BinaryDeviceMessageParserResult parseResult = BinaryDeviceMessageParser.processMessage(message);
    if (parseResult != null && parseResult.isSearchMessage(this))
    {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      try
      {
        // add announcement without end of packet
        byte[] announcementMessageData = toByteArrayForAnnouncement(socketStructure);
        addResponseEntities(outputStream, announcementMessageData, parseResult.getResponseEntityList());
        announcementMessageData = outputStream.toByteArray();

        Portable.println("Answer search from " + message.getSourceAddress() + " with " +
          BinaryUPnPConstants.toDebugString(announcementMessageData));

        BinaryMessageObject responseMessage =
          new BinaryMessageObject(announcementMessageData, (InetSocketAddress)socketStructure.getDiscoverySocket()
            .getLocalSocketAddress(), message.getSourceAddress());
        SocketHelper.sendBinaryMessage(responseMessage, socketStructure.getDiscoverySocket());

      } catch (Exception e)
      {
      }
    }
  }

  /** Parses and processes a received description message. */
  public void processDescriptionMessage(BinaryMessageObject message,
    BinaryDeviceHostAddressSocketStructure socketStructure)
  {
    BinaryDeviceMessageParserResult parseResult = BinaryDeviceMessageParser.processMessage(message);
    if (parseResult != null && parseResult.isGetDescriptionMessage(this))
    {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      try
      {
        byte[] descriptionMessageData = toByteArrayForGetDescriptionResponse();
        addResponseEntities(outputStream, descriptionMessageData, parseResult.getResponseEntityList());
        descriptionMessageData = outputStream.toByteArray();

        Portable.println("Answer get description from " + IPHelper.toString(message.getSourceAddress()) + " with " +
          BinaryUPnPConstants.toDebugString(outputStream.toByteArray()));

        BinaryMessageObject responseMessage =
          new BinaryMessageObject(descriptionMessageData, (InetSocketAddress)socketStructure.getDescriptionSocket()
            .getLocalSocketAddress(), message.getSourceAddress());
        SocketHelper.sendBinaryMessage(responseMessage, socketStructure.getDescriptionSocket());
      } catch (Exception e)
      {
        Portable.println("Error " + e.getMessage());
        e.printStackTrace();
      }

    }
  }

  /** Parses and processes a received control message. */
  public void processControlMessage(BinaryMessageObject message, BinaryDeviceHostAddressSocketStructure socketStructure)
  {
    BinaryDeviceMessageParserResult parseResult = BinaryDeviceMessageParser.processMessage(message);
    try
    {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] responseMessageData = null;

      if (parseResult != null && parseResult.isGetServiceValueMessage(this))
      {
        Portable.println("Answer get service value from " + message.getSourceAddress());
        BinaryDeviceService deviceService = getDeviceServiceByID(parseResult.getServiceID());
        if (deviceService == null)
        {
          responseMessageData =
            toByteArrayForValueResult(BinaryUPnPConstants.UnitTypeServiceValueResult,
              BinaryUPnPConstants.ResultTypeInvalidServiceID,
              parseResult.getServiceID());
        } else
        {
          responseMessageData = deviceService.toByteArrayForGetValueResult();
        }
        Portable.println("Generated control response message is\r\n" +
          BinaryUPnPConstants.toDebugString(responseMessageData));
      }
      if (responseMessageData != null)
      {
        addResponseEntities(outputStream, responseMessageData, parseResult.getResponseEntityList());

        Portable.println("Generated control response message with response entities is\r\n" +
          BinaryUPnPConstants.toDebugString(outputStream.toByteArray()));

        BinaryMessageObject responseMessage =
          new BinaryMessageObject(outputStream.toByteArray(), (InetSocketAddress)socketStructure.getControlSocket()
            .getLocalSocketAddress(), message.getSourceAddress());
        SocketHelper.sendBinaryMessage(responseMessage, socketStructure.getControlSocket());
      }
    } catch (Exception e)
    {
      Portable.println("Error " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Adds optional response entities to a generated message.
   * 
   * 
   * @param outputStream
   *          Destination stream
   * @param coreMessageData
   *          Original message
   * @param responseEntityList
   *          Optional response entities
   */
  private void addResponseEntities(ByteArrayOutputStream outputStream, byte[] coreMessageData, Vector responseEntityList)
  {
    try
    {
      if (responseEntityList.size() > 0)
      {
        // discard original end of packet tupel
        outputStream.write(coreMessageData, 0, coreMessageData.length - 1);
        for (int i = 0; i < responseEntityList.size(); i++)
        {
          outputStream.write(((GatewayData)responseEntityList.elementAt(i)).toByteArrayForResponse());
        }
        outputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);
      } else
      {
        outputStream.write(coreMessageData);
      }
    } catch (Exception e)
    {
    }
  }

  /** Returns the byte array announcement for this device. */
  public byte[] toByteArrayForAnnouncement(BinaryDeviceHostAddressSocketStructure socketStructure)
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add packet type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceAnnouncement);
      byteArrayOutputStream.write(0);
      // add version
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeSDLVersion);
      byteArrayOutputStream.write(2);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(0);
      // add date
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceDescriptionDate);
      byteArrayOutputStream.write(5);
      byteArrayOutputStream.write(ByteArrayHelper.int64ToByteArray(deviceDescriptionDate, 5));
      // add ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(deviceID));
      // add type if not custom
      if (deviceType != BinaryUPnPConstants.DeviceTypeCustom)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceType);
        byteArrayOutputStream.write(1);
        byteArrayOutputStream.write(deviceType);
      }
      // add description port if not standard
      if (socketStructure.getDescriptionSocket().getLocalPort() != BinaryUPnPConstants.DescriptionPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceDescriptionPort);
        byteArrayOutputStream.write(2);
        byteArrayOutputStream.write(ByteArrayHelper.uint16ToByteArray(socketStructure.getDescriptionSocket()
          .getLocalPort()));
      }
      // add control port if not standard
      if (socketStructure.getControlSocket().getLocalPort() != BinaryUPnPConstants.ControlPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceControlPort);
        byteArrayOutputStream.write(2);
        byteArrayOutputStream.write(ByteArrayHelper.uint16ToByteArray(socketStructure.getControlSocket().getLocalPort()));
      }
      // add end of packet
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

  /** Returns the byte array removal message for this device. */
  public byte[] toByteArrayForRemoval()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add packet type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceRemoval);
      byteArrayOutputStream.write(0);
      // add version
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeSDLVersion);
      byteArrayOutputStream.write(2);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(0);
      // add ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(deviceID));
      // add end of packet
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {

    }
    return null;
  }

  /** Returns the byte array description for this device. */
  public byte[] toByteArrayForGetDescriptionResponse()
  {
    if (descriptionMessage != null)
    {
      return descriptionMessage;
    }
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // calculate services size
      for (int i = 0; i < serviceList.size(); i++)
      {
        BinaryDeviceService currentService = getDeviceService(i);
        // add byte array description for service
        byteArrayOutputStream.write(currentService.toByteArrayForGetDescriptionResponse());
      }
      byte[] serviceDescriptions = byteArrayOutputStream.toByteArray();
      byteArrayOutputStream.reset();

      // add packet type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceDescription);
      byteArrayOutputStream.write(0);
      // add date
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceDescriptionDate);
      byteArrayOutputStream.write(5);
      byteArrayOutputStream.write(ByteArrayHelper.int64ToByteArray(deviceDescriptionDate, 5));
      // add lifetime
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceExpectedLifeTime);
      byteArrayOutputStream.write(2);
      byteArrayOutputStream.write(ByteArrayHelper.uint16ToByteArray((int)expectedLifeTime));
      // add type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceType);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(deviceType);
      // add ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(deviceID));
      // add name
      byte[] nameData = StringHelper.stringToByteArray(name);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceName);
      byteArrayOutputStream.write(nameData.length);
      byteArrayOutputStream.write(nameData);
      // add application
      byte[] applicationData = StringHelper.stringToByteArray(application);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceApplication);
      byteArrayOutputStream.write(applicationData.length);
      byteArrayOutputStream.write(applicationData);
      // add manufacturer
      byte[] manufacturerData = StringHelper.stringToByteArray(manufacturer);
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceManufacturer);
      byteArrayOutputStream.write(manufacturerData.length);
      byteArrayOutputStream.write(manufacturerData);
      // add control port if changed
      if (controlPort != BinaryUPnPConstants.ControlPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceControlPort);
        byteArrayOutputStream.write(2);
        byteArrayOutputStream.write(ByteArrayHelper.uint16ToByteArray(controlPort));
      }
      // add all services
      byteArrayOutputStream.write(serviceDescriptions);
      // add end of packet
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      descriptionMessage = byteArrayOutputStream.toByteArray();
      return descriptionMessage;
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /** Builds the response message for a control request */
  public byte[] toByteArrayForValueResult(int messageType, int result, int serviceID)
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add type
      byteArrayOutputStream.write(messageType);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(result);
      // add device ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(deviceID));
      // add service ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(serviceID);
      // add end of packet
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Builds the message for events. */
  public byte[] toByteArrayForEventMessage()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEvent);
      byteArrayOutputStream.write(0);
      // add device ID
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceID);
      byteArrayOutputStream.write(4);
      byteArrayOutputStream.write(ByteArrayHelper.uint32ToByteArray(deviceID));
      // single event
      if (pendingEventServiceList.size() == 1)
      {
        BinaryDeviceService deviceService = (BinaryDeviceService)pendingEventServiceList.elementAt(0);
        // add service ID
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceID);
        byteArrayOutputStream.write(1);
        byteArrayOutputStream.write(deviceService.getServiceID());
        // add value
        byte[] value = deviceService.getValue().toByteArray();
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceValue);
        byteArrayOutputStream.write(value.length);
        byteArrayOutputStream.write(value);
      } else
      {
        for (int i = 0; i < pendingEventServiceList.size(); i++)
        {
          BinaryDeviceService deviceService = (BinaryDeviceService)pendingEventServiceList.elementAt(i);
          byte[] value = deviceService.getValue().toByteArray();
          int containerSize = 5 + value.length;
          // add container
          byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceContainer);
          byteArrayOutputStream.write(containerSize);
          // add service ID
          byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceID);
          byteArrayOutputStream.write(1);
          byteArrayOutputStream.write(deviceService.getServiceID());
          // add value
          byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeServiceValue);
          byteArrayOutputStream.write(value.length);
          byteArrayOutputStream.write(value);
        }
      }
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice#triggerEvents()
   */
  public void triggerEvents()
  {
    byte[] eventMessageData = null;
    CriticalSection.enter(pendingEventServiceLock);
    if (pendingEventServiceList.size() > 0)
    {
      eventMessageData = toByteArrayForEventMessage();
      pendingEventServiceList.clear();
    }
    CriticalSection.exit(pendingEventServiceLock);
    // trigger pending events
    if (eventMessageData != null)
    {
      for (int i = 0; i < binaryDeviceMessageManagement.getSocketStructureManagement().getSocketStructureCount(); i++)
      {
        BinaryDeviceHostAddressSocketStructure currentSocketStructure =
          (BinaryDeviceHostAddressSocketStructure)binaryDeviceMessageManagement.getSocketStructureManagement()
            .getSocketStructure(i);

        BinaryMessageObject eventMessage =
          new BinaryMessageObject(eventMessageData, (InetSocketAddress)currentSocketStructure.getEventSocket()
            .getLocalSocketAddress(), IPHelper.toSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress + ":" +
            BinaryUPnPConstants.EventMulticastPort));
        SocketHelper.sendBinaryMessage(eventMessage, currentSocketStructure.getEventSocket());
      }
    }
    // announce device
    if (Portable.currentTimeMillis() - lastAnnouncement > getExpectedLifeTime() * 60000 / 15)
    {
      lastAnnouncement = Portable.currentTimeMillis();
      for (int i = 0; i < binaryDeviceMessageManagement.getSocketStructureManagement().getSocketStructureCount(); i++)
      {
        BinaryDeviceHostAddressSocketStructure currentSocketStructure =
          (BinaryDeviceHostAddressSocketStructure)binaryDeviceMessageManagement.getSocketStructureManagement()
            .getSocketStructure(i);

        byte[] announcementMessageData = toByteArrayForAnnouncement(currentSocketStructure);

        Portable.println("Announce device with " + BinaryUPnPConstants.toDebugString(announcementMessageData));

        BinaryMessageObject message =
          new BinaryMessageObject(announcementMessageData,
            (InetSocketAddress)currentSocketStructure.getDiscoverySocket().getLocalSocketAddress(),
            IPHelper.toSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress + ":" +
              BinaryUPnPConstants.DiscoveryMulticastPort));

        SocketHelper.sendBinaryMessage(message, currentSocketStructure.getDiscoverySocket());
      }
    }
  }

  public void run()
  {
    while (!terminateThread)
    {
      for (int i = 0; i < eventListenerList.size(); i++)
      {
        try
        {
          ((IEventListener)eventListenerList.elementAt(i)).triggerEvents();
        } catch (Exception e)
        {
        }
      }
      ThreadHelper.sleep(50);
    }
    // send device removal message
    for (int i = 0; i < binaryDeviceMessageManagement.getSocketStructureManagement().getSocketStructureCount(); i++)
    {
      BinaryDeviceHostAddressSocketStructure currentSocketStructure =
        (BinaryDeviceHostAddressSocketStructure)binaryDeviceMessageManagement.getSocketStructureManagement()
          .getSocketStructure(i);

      byte[] removalMessageData = toByteArrayForRemoval();

      Portable.println("Send device removal with " + BinaryUPnPConstants.toDebugString(removalMessageData));

      BinaryMessageObject message =
        new BinaryMessageObject(removalMessageData, (InetSocketAddress)currentSocketStructure.getDiscoverySocket()
          .getLocalSocketAddress(), IPHelper.toSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress + ":" +
          BinaryUPnPConstants.DiscoveryMulticastPort));

      SocketHelper.sendBinaryMessage(message, currentSocketStructure.getDiscoverySocket());
    }

    terminated = true;
  }

  /** Terminates the device. */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(30);
    }
    binaryDeviceMessageManagement.terminate();
  }

}

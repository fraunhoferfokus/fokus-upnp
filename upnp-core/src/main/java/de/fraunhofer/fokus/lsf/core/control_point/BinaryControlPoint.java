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
package de.fraunhofer.fokus.lsf.core.control_point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPServiceValueListener;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.lsf.core.templates.BinaryTemplateEntity;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.CriticalSection;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.KeyValueVector;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is used to enumerate binary UPnP devices.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryControlPoint extends Thread implements IBinaryCPDeviceEventListener, IBinaryCPServiceValueListener
{
  /** Associated entity */
  private BinaryTemplateEntity          binaryTemplateEntity;

  /** Message processing */
  private BinaryCPMessageManagement     binaryCPMessageManagement;

  /** List of discovered devices info objects */
  private Vector                        deviceInfoList                = new Vector();

  /** List of discovered devices */
  private Vector                        deviceList                    = new Vector();

  /** List of devices that have pending management changes */
  private Vector                        deviceManagementChangedList   = new Vector();

  /** List of devices that have pending service value requests */
  private Vector                        initialServiceValueDeviceList = new Vector();

  /** Sync object for device list */
  private Object                        listLock                      = new Object();

  /** Time of last ping */
  private long                          nextPingTime;

  /** Optional event listener */
  private IBinaryCPDeviceEventListener  deviceEventListener;

  /** Optional event listener */
  private IBinaryCPServiceValueListener serviceValueListener;

  /** Directory used for cached descriptions */
  private File                          descriptionCacheDirectory     = null;

  /** Resource directory */
  private String                        resourceDirectory             = null;

  /** Flag to terminate the control point */
  private boolean                       terminateThread               = false;

  /** Flag that the entity thread has been terminated */
  private boolean                       terminated                    = false;

  /**
   * Creates a new instance of BinaryControlPoint.
   * 
   * @param binaryTemplateEntity
   */
  public BinaryControlPoint(BinaryTemplateEntity binaryTemplateEntity)
  {
    super("BinaryControlPoint");
    this.binaryTemplateEntity = binaryTemplateEntity;

    LSFStartupConfiguration startupConfiguration = binaryTemplateEntity.getStartupConfiguration();
    init(startupConfiguration);
  }

  /**
   * Creates a new instance of BinaryControlPoint.
   * 
   * @param startupConfiguration
   */
  public BinaryControlPoint(LSFStartupConfiguration startupConfiguration)
  {
    super("BinaryControlPoint");
    this.binaryTemplateEntity = null;

    init(startupConfiguration);
  }

  private void init(LSFStartupConfiguration startupConfiguration)
  {
    binaryCPMessageManagement = new BinaryCPMessageManagement(this, startupConfiguration);

    nextPingTime = Portable.currentTimeMillis();

    Portable.println("  Start binary control point...");
    if (BinaryUPnPConstants.USE_ACTIVE_PINGS)
    {
      Portable.println("    Use active pings with interval " + BinaryUPnPConstants.PING_INTERVAL + " ms");
    }

    resourceDirectory = FileHelper.getResourceDirectoryNameFromPackage(this.getClass().getName());
    Portable.println("Resource directory is " + resourceDirectory);
    File directoryFile = new File(resourceDirectory);
    if (!directoryFile.exists())
    {
      directoryFile.mkdirs();
    }
    descriptionCacheDirectory = new File(resourceDirectory + "DescriptionCache");
    if (!descriptionCacheDirectory.exists())
    {
      descriptionCacheDirectory.mkdir();
    }

    start();
  }

  /** Sends a search message */
  public void sendSearchAllMessage()
  {
    Portable.println("  Send binary UPnP search all...");

    binaryCPMessageManagement.sendSearchAllMessage(new byte[] {
        BinaryUPnPConstants.UnitTypeSearchDevice, 0,

        BinaryUPnPConstants.UnitTypeSDLVersion, 2, 1, 0,

        BinaryUPnPConstants.UnitTypeEndOfPacket
    });
  }

  /**
   * Processes a received discovery message (announcement, removal or ping).
   * 
   * @param message
   *          The message
   */
  public void processDiscoveryMessage(BinaryMessageObject message,
    BinaryCPHostAddressSocketStructure associatedSocketStructure)
  {
    // check for ping reply
    if (BinaryCPMessageParser.processPingReply(message))
    {
      Portable.println("    Process ping reply from " + IPHelper.toString(message.getSourceAddress()));
      // search device for the message
      CriticalSection.enter(listLock);
      for (int i = 0; i < deviceList.size(); i++)
      {
        BinaryCPDevice currentDevice = (BinaryCPDevice)deviceList.elementAt(i);
        if (currentDevice.getAccessAddress().equals(message.getSourceAddress().getAddress()))
        {
          currentDevice.updateDiscoveryTime();
        }
      }
      CriticalSection.exit(listLock);
      return;
    }

    // handle announcement
    BinaryCPDeviceInfo deviceInfo = BinaryCPMessageParser.processDeviceAnnouncement(message);
    if (deviceInfo != null)
    {
      deviceInfo.setAssociatedSocketStructure(associatedSocketStructure);

      // check if this is a completely new device
      if (getCPDeviceByID(deviceInfo.getDeviceID()) == null && getCPDeviceInfoByID(deviceInfo.getDeviceID()) == null)
      {
        // associate info with this control point
        deviceInfo.setBinaryControlPoint(this);

        Portable.println("BinaryControlPoint: Found new device info " + deviceInfo.getDeviceID() + " at " +
          deviceInfo.getAccessAddress().getHostAddress());

        CriticalSection.enter(listLock);
        deviceInfoList.add(deviceInfo);
        CriticalSection.exit(listLock);
      }
      // check if the device is already known
      BinaryCPDeviceInfo knownDeviceInfo = getCPDeviceInfoByID(deviceInfo.getDeviceID());
      if (knownDeviceInfo != null)
      {
        // update port information for pending device info
        knownDeviceInfo.updateDeviceInfo(deviceInfo);
      }

      // check if the device is already known
      BinaryCPDevice knownDevice = getCPDeviceByID(deviceInfo.getDeviceID());
      if (knownDevice != null)
      {
        // update life time
        knownDevice.updateDiscoveryTime();

        // check revision
        if (knownDevice.getDeviceDescriptionDate() != deviceInfo.getDeviceDescriptionDate())
        {
          Portable.println("BinaryControlPoint: Description date changed for " + knownDevice);
          // rerequest device description
          CriticalSection.enter(listLock);
          // prevent duplicates
          if (getCPDeviceInfoByID(deviceInfo.getDeviceID()) == null)
          {
            Portable.println("BinaryControlPoint: Trigger new description request");
            deviceInfoList.add(deviceInfo);
          }
          CriticalSection.exit(listLock);
        }
        // check IP address
        if (!ByteArrayHelper.isEqual(knownDevice.getDeviceAddress(), deviceInfo.getDeviceAddress()))
        {
          knownDevice.setDeviceInfo(deviceInfo);
          Portable.println("BinaryControlPoint: Device address changed for " + knownDevice);
          changedDevice(knownDevice, BinaryCPConstants.EVENT_CODE_DEVICE_ADDRESS_CHANGE);
        }
        // check path
        if (!knownDevice.getAccessAddress().equals(deviceInfo.getAccessAddress()) ||
          !knownDevice.getDeviceInfo().hasEqualPath(deviceInfo))
        {
          knownDevice.setDeviceInfo(deviceInfo);
          Portable.println("BinaryControlPoint: Path changed for " + knownDevice);
          changedDevice(knownDevice, BinaryCPConstants.EVENT_CODE_PATH_CHANGE);
        }
        // check ports
        int eventCode = knownDevice.getDeviceInfo().updateDeviceInfo(deviceInfo);
        if (eventCode != 0)
        {
          Portable.println("BinaryControlPoint: Ports changed for " + knownDevice);
          // update port data for the device itself
          knownDevice.setDescriptionPort(deviceInfo.getDescriptionPort());
          knownDevice.setControlPort(deviceInfo.getControlPort());
          knownDevice.setEventPort(deviceInfo.getEventPort());
          changedDevice(knownDevice, eventCode);
        }
      }
    }

    // handle removal
    long deviceID = BinaryCPMessageParser.processDeviceRemoval(message);
    if (deviceID != -1)
    {
      // check if the device is known
      BinaryCPDevice removedDevice = getCPDeviceByID(deviceID);
      if (removedDevice != null)
      {
        Portable.println("BinaryControlPoint: Device removed with ID " + deviceID);
        CriticalSection.enter(listLock);
        deviceList.remove(removedDevice);
        CriticalSection.exit(listLock);
        deviceGone(removedDevice);
      }
    }
  }

  /**
   * Processes a received event message.
   * 
   * @param message
   *          The message
   */
  public void processEventMessage(BinaryMessageObject message)
  {
    try
    {
      if (message != null)
      {
        BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
        messageParser.parse(message);
        long deviceID = messageParser.getDeviceID();
        // search device for the event
        BinaryCPDevice eventDevice = null;
        CriticalSection.enter(listLock);
        for (int i = 0; eventDevice == null && i < deviceList.size(); i++)
        {
          BinaryCPDevice currentDevice = (BinaryCPDevice)deviceList.elementAt(i);
          if (currentDevice.getDeviceID() == deviceID)
          {
            eventDevice = currentDevice;
          }
        }
        CriticalSection.exit(listLock);
        if (eventDevice != null)
        {
          KeyValueVector valueList = CollectionHelper.getPersistentElementList(messageParser.getServiceValueTable());
          for (int i = 0; i < valueList.size(); i++)
          {
            Integer currentServiceIDKey = (Integer)valueList.getKey(i);
            byte[] newValue = (byte[])valueList.getValue(i);
            int currentServiceID = currentServiceIDKey.intValue();
            BinaryCPService service = eventDevice.getCPServiceByID(currentServiceID);
            // update value in service and inform entity
            if (service != null && service.setValueFromByteArray(newValue))
            {
              valueChanged(service);
            }
          }
        }
      }
    } catch (Exception e)
    {
      Portable.println("Error during eventing: " + e.getMessage());
    }
  }

  /**
   * Retrieves the number of known devices.
   * 
   */
  public int getCPDeviceCount()
  {
    return deviceList.size();
  }

  /**
   * Retrieves a certain device.
   * 
   * @param index
   *          The index in the device list
   * 
   * @return The device with that index or null
   */
  public BinaryCPDevice getCPDevice(int index)
  {
    BinaryCPDevice result = null;
    CriticalSection.enter(listLock);
    if (index >= 0 && index < deviceList.size())
    {
      result = (BinaryCPDevice)deviceList.elementAt(index);
    }
    CriticalSection.exit(listLock);

    return result;
  }

  /**
   * Retrieves a certain device.
   * 
   * @param deviceID
   *          The device ID
   * 
   * @return The device with that ID or null
   */
  public BinaryCPDevice getCPDeviceByID(long deviceID)
  {
    BinaryCPDevice result = null;
    CriticalSection.enter(listLock);
    for (int i = 0; result == null && i < deviceList.size(); i++)
    {
      BinaryCPDevice currentDevice = (BinaryCPDevice)deviceList.elementAt(i);
      if (currentDevice.getDeviceID() == deviceID)
      {
        result = currentDevice;
      }
    }
    CriticalSection.exit(listLock);

    return result;
  }

  /**
   * Retrieves a certain device info.
   * 
   * @param deviceID
   *          The device ID
   * 
   * @return The device info with that ID or null
   */
  public BinaryCPDeviceInfo getCPDeviceInfoByID(long deviceID)
  {
    BinaryCPDeviceInfo result = null;
    CriticalSection.enter(listLock);
    for (int i = 0; i < deviceInfoList.size(); i++)
    {
      BinaryCPDeviceInfo currentDeviceInfo = (BinaryCPDeviceInfo)deviceInfoList.elementAt(i);
      if (currentDeviceInfo.getDeviceID() == deviceID)
      {
        result = currentDeviceInfo;
      }
    }
    CriticalSection.exit(listLock);

    return result;
  }

  /**
   * @return Returns the messageManagement.
   */
  public BinaryCPMessageManagement getBinaryCPMessageManagement()
  {
    return binaryCPMessageManagement;
  }

  /**
   * Retrieves the binaryTemplateEntity.
   * 
   * @return The binaryTemplateEntity.
   */
  public BinaryTemplateEntity getBinaryTemplateEntity()
  {
    return binaryTemplateEntity;
  }

  /** Request the device description from a local proxy. */
  public BinaryCPDevice getDescriptionFromProxy(BinaryCPDeviceInfo deviceInfo)
  {
    if (deviceInfo.getDeviceType() == BinaryUPnPConstants.DeviceTypeEnergyMeasurement)
    {
      BinaryCPDevice result = new BinaryCPDevice(deviceInfo, 30);
      // update device description date from description (overrides discovery info)  
      result.setDeviceDescriptionDate(deviceInfo.getDeviceDescriptionDate());
      result.setDeviceType(deviceInfo.getDeviceType());
      result.setDescriptionPort(deviceInfo.getDescriptionPort());
      result.setControlPort(deviceInfo.getControlPort());
      result.setEventPort(deviceInfo.getEventPort());
      result.setApplication("4032");
      result.setName("EnergyMeasurement");
      result.setManufacturer("FhG Fokus");
      result.setExternalServiceDescriptions(false);
      Vector serviceList = new Vector();
      // create energy service
      BinaryCPService service =
        new BinaryCPService(BinaryUPnPConstants.ServiceTypeAccumulatedEnergy,
          0,
          "",
          BinaryUPnPConstants.VarTypeComposite);
      service.setBinaryCPDevice(result);
      serviceList.add(service);
      // create voltage service
      service = new BinaryCPService(BinaryUPnPConstants.ServiceTypeVoltage, 0, "V", BinaryUPnPConstants.VarTypeUINT16);
      service.setBinaryCPDevice(result);
      serviceList.add(service);
      // create current service
      service = new BinaryCPService(BinaryUPnPConstants.ServiceTypeCurrent, 0, "mA", BinaryUPnPConstants.VarTypeUINT16);
      service.setBinaryCPDevice(result);
      serviceList.add(service);

      // set final service list
      result.setServiceList(serviceList);

      result.setDescriptionMessage(new byte[] {});

      return result;
    }
    return null;
  }

  /** Request the device description from a device. */
  public BinaryCPDevice invokeGetDescription(BinaryCPDeviceInfo deviceInfo)
  {
    long deviceID = deviceInfo.getDeviceID();
    byte[] prefix =
      new byte[] {
          BinaryUPnPConstants.UnitTypeGetDeviceDescription, 0,

          BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF),
          (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF)
      };

    BinaryCPHostAddressSocketStructure socketStructure =
      (BinaryCPHostAddressSocketStructure)deviceInfo.getAssociatedSocketStructure();
    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefix,
        deviceInfo,
        socketStructure.getDescriptionSocket(),
        deviceInfo.getDescriptionPort(),
        null);

    if (response != null)
    {
      BinaryCPDevice result = BinaryCPMessageParser.processDeviceDescription(deviceInfo, response);
      // check if we need to retrieve external service descriptions
      if (result != null)
      {
        if (result.hasExternalServiceDescriptions())
        {
          for (int i = 0; i < result.getServiceCount(); i++)
          {
            BinaryCPService currentService = result.getCPService(i);
            prefix =
              new byte[] {
                  BinaryUPnPConstants.UnitTypeGetServiceDescription, 0,

                  BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF),
                  (byte)(deviceID >> 16 & 0xFF), (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF),

                  BinaryUPnPConstants.UnitTypeServiceID, 1, (byte)currentService.getServiceID()
              };
            response =
              sendRequestAndWaitForResponse(prefix,
                deviceInfo,
                socketStructure.getDescriptionSocket(),
                deviceInfo.getDescriptionPort(),
                result);

            if (response != null)
            {
              BinaryCPService completeService =
                BinaryCPMessageParser.processExternalServiceDescription(response, result);
              if (completeService != null)
              {
                result.replaceCPService(completeService);
              }
              // store service description message
              result.getServiceDescriptionMessageFromIDTable().put(new Integer(currentService.getServiceID()),
                response.getBody());
            } else
            {
              // this will trigger a later request for the whole device and all service descriptions
              Portable.println("Could not request service description " + currentService.getServiceTypeString());
              return null;
            }
          }
        }
      }
      return result;
    }
    return null;
  }

  /** Request the current service value from the binary UPnP service. */
  public BinaryCPValueMessageParser invokeGetServiceValue(BinaryCPService binaryCPService) throws ActionFailedException
  {
    long deviceID = binaryCPService.getBinaryCPDevice().getDeviceID();
    byte[] prefix =
      new byte[] {
          BinaryUPnPConstants.UnitTypeGetServiceValue, 0,

          BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF),
          (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF),

          BinaryUPnPConstants.UnitTypeServiceID, 1, (byte)(binaryCPService.getServiceID() & 0xFF)
      };

    BinaryCPHostAddressSocketStructure socketStructure =
      binaryCPService.getBinaryCPDevice().getAssociatedSocketStructure();
    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefix,
        binaryCPService.getBinaryCPDevice().getDeviceInfo(),
        socketStructure.getControlSocket(),
        binaryCPService.getBinaryCPDevice().getControlPort(),
        binaryCPService.getBinaryCPDevice());
    if (response != null)
    {
      // Portable.println("Received response for get value");
      BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
      messageParser.parse(response);
      return messageParser;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
  }

  /** Sends a command to a binary UPnP service. */
  public BinaryCPValueMessageParser invokeSetServiceValue(BinaryCPService binaryCPService, byte[] newValue) throws ActionFailedException
  {
    long deviceID = binaryCPService.getBinaryCPDevice().getDeviceID();
    byte[] prefix =
      new byte[] {
          BinaryUPnPConstants.UnitTypeSetServiceValue, 0,

          BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF),
          (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF),

          BinaryUPnPConstants.UnitTypeServiceID, 1, (byte)(binaryCPService.getServiceID() & 0xFF)
      };
    ByteArrayOutputStream prefixOutputStream = new ByteArrayOutputStream();
    try
    {
      prefixOutputStream.write(prefix);
      prefixOutputStream.write(BinaryUPnPConstants.UnitTypeServiceValue);
      prefixOutputStream.write(newValue.length);
      prefixOutputStream.write(newValue);
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeUnknownError);
    }
    BinaryCPHostAddressSocketStructure socketStructure =
      binaryCPService.getBinaryCPDevice().getAssociatedSocketStructure();
    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefixOutputStream.toByteArray(),
        binaryCPService.getBinaryCPDevice().getDeviceInfo(),
        socketStructure.getControlSocket(),
        binaryCPService.getBinaryCPDevice().getControlPort(),
        binaryCPService.getBinaryCPDevice());
    if (response != null)
    {
      BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
      messageParser.parse(response);
      return messageParser;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
  }

  /** Sends a new name to the binary UPnP device. */
  public BinaryCPValueMessageParser invokeSetDeviceName(BinaryCPDevice binaryCPDevice, String newName) throws ActionFailedException
  {
    long deviceID = binaryCPDevice.getDeviceID();
    byte[] prefix =
      new byte[] {
          BinaryUPnPConstants.UnitTypeSetDeviceName, 0,

          BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF),
          (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF)
      };
    byte[] nameData = StringHelper.stringToByteArray(newName);
    ByteArrayOutputStream prefixOutputStream = new ByteArrayOutputStream();
    try
    {
      prefixOutputStream.write(prefix);
      prefixOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceName);
      prefixOutputStream.write(nameData.length);
      prefixOutputStream.write(nameData);
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeUnknownError);
    }
    BinaryCPHostAddressSocketStructure socketStructure = binaryCPDevice.getAssociatedSocketStructure();
    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefixOutputStream.toByteArray(),
        binaryCPDevice.getDeviceInfo(),
        socketStructure.getControlSocket(),
        binaryCPDevice.getControlPort(),
        binaryCPDevice);
    if (response != null)
    {
      BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
      messageParser.parse(response);
      return messageParser;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
  }

  /** Sends a new application to the binary UPnP device. */
  public BinaryCPValueMessageParser invokeSetDeviceApplication(BinaryCPDevice binaryCPDevice, String newApplication) throws ActionFailedException
  {
    long deviceID = binaryCPDevice.getDeviceID();
    byte[] prefix =
      new byte[] {
          BinaryUPnPConstants.UnitTypeSetDeviceApplication, 0,

          BinaryUPnPConstants.UnitTypeDeviceID, 4, (byte)(deviceID >> 24 & 0xFF), (byte)(deviceID >> 16 & 0xFF),
          (byte)(deviceID >> 8 & 0xFF), (byte)(deviceID >> 0 & 0xFF)
      };
    byte[] applicationData = StringHelper.stringToByteArray(newApplication);
    ByteArrayOutputStream prefixOutputStream = new ByteArrayOutputStream();
    try
    {
      prefixOutputStream.write(prefix);
      prefixOutputStream.write(BinaryUPnPConstants.UnitTypeDeviceApplication);
      prefixOutputStream.write(applicationData.length);
      prefixOutputStream.write(applicationData);
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeUnknownError);
    }
    BinaryCPHostAddressSocketStructure socketStructure = binaryCPDevice.getAssociatedSocketStructure();
    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefixOutputStream.toByteArray(),
        binaryCPDevice.getDeviceInfo(),
        socketStructure.getControlSocket(),
        binaryCPDevice.getControlPort(),
        binaryCPDevice);
    if (response != null)
    {
      BinaryCPValueMessageParser messageParser = new BinaryCPValueMessageParser();
      messageParser.parse(response);
      return messageParser;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
  }

  /** Request the current state from the binary UPnP service. */
  public boolean invokeIsActive(BinaryCPService binaryCPService) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("GServiceState");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.invokeAction();
      return action.getArgumentByID(1).getBooleanValue();
    } catch (Exception e)
    {
      Portable.println("Error reading GServiceState: " + e.getMessage());
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Request the current evented state from the binary UPnP service. */
  public boolean invokeIsEvented(BinaryCPService binaryCPService) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("GEventState");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.invokeAction();
      return action.getArgumentByID(1).getBooleanValue();
    } catch (Exception e)
    {
      Portable.println("Error reading GEventState: " + e.getMessage());
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Request the current event rate from the binary UPnP service. */
  public int invokeGetEventRate(BinaryCPService binaryCPService) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("GEventRate");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.invokeAction();
      return (int)action.getArgumentByID(1).getNumericValue();
    } catch (Exception e)
    {
      Portable.println("Error reading GEventRate: " + e.getMessage());
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Sets the current service state for a binary UPnP service. */
  public void invokeSetActive(BinaryCPService binaryCPService, boolean state) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("SServiceState");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.getArgumentByID(1).setBooleanValue(state);
      action.invokeAction();
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Sets the current event state for a binary UPnP service. */
  public void invokeSetEvented(BinaryCPService binaryCPService, boolean state) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("SEventState");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.getArgumentByID(1).setBooleanValue(state);
      action.invokeAction();
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Sets the current event state for a binary UPnP service. */
  public void invokeSetEventRate(BinaryCPService binaryCPService, int seconds) throws ActionFailedException
  {
    try
    {
      // search management service
      BinaryCPService managementService = binaryCPService.getBinaryCPDevice().getManagementService();
      // search action
      BinaryCPAction action = managementService.getCPAction("SEventRate");
      action.getArgumentByID(0).setNumericValue(binaryCPService.getServiceID());
      action.getArgumentByID(1).setNumericValue(seconds);
      action.invokeAction();
    } catch (Exception e)
    {
      throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeInternalError);
    }
  }

  /** Invokes an action. */
  public BinaryMessageObject invokeAction(BinaryCPAction action, byte[] prefix) throws ActionFailedException
  {
    BinaryCPHostAddressSocketStructure socketStructure =
      action.getBinaryCPService().getBinaryCPDevice().getAssociatedSocketStructure();

    BinaryMessageObject response =
      sendRequestAndWaitForResponse(prefix,
        action.getBinaryCPService().getBinaryCPDevice().getDeviceInfo(),
        socketStructure.getControlSocket(),
        action.getBinaryCPService().getBinaryCPDevice().getControlPort(),
        action.getBinaryCPService().getBinaryCPDevice());

    if (response != null)
    {
      return response;
    }
    throw BinaryUPnPConstants.createActionFailedException(BinaryUPnPConstants.ResultTypeNoResponseMessage);
  }

  /**
   * Sends a request to the device and tries to receive an answer.
   * 
   * 
   * @param prefix
   *          The current message
   * @param deviceInfo
   *          Object containing forwarder info
   * @param socket
   *          Send socket
   * @param destinationPort
   *          Destination port
   * @return
   */
  private BinaryMessageObject sendRequestAndWaitForResponse(byte[] prefix,
    BinaryCPDeviceInfo deviceInfo,
    DatagramSocket socket,
    int destinationPort,
    BinaryCPDevice device)
  {
    try
    {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      outputStream.write(prefix);

      // add path to device to allow proper routing
      deviceInfo.addAccessEntities(outputStream);

      outputStream.write(BinaryUPnPConstants.UnitTypeEndOfPacket);
      byte[] packetData = outputStream.toByteArray();

      StringHelper.printDebugText("", true, "To " + deviceInfo.getAccessAddress().getHostAddress() + ":" +
        destinationPort + ":", BinaryUPnPConstants.toForwarderDebugString("", packetData, null));

      DatagramPacket packet =
        new DatagramPacket(packetData, packetData.length, deviceInfo.getAccessAddress(), destinationPort);

      int invocationCount = BinaryUPnPConstants.INVOCATION_RETRIES + 1;
      do
      {
        long startTime = Portable.currentTimeMillis();

        socket.send(packet);

        // read response
        BinaryMessageObject responseMessage =
          SocketHelper.readBinaryMessage(null, socket, deviceInfo.getResponseWaitTime());
        long endTime = Portable.currentTimeMillis();

        // a message was received
        if (responseMessage != null)
        {
          Portable.println("\r\n  Response received after " + (endTime - startTime) + " ms (" +
            responseMessage.getBody().length + " bytes)");
          if (device != null)
          {
            device.addResponseTime(endTime - startTime);
          }
          return responseMessage;
        }
        invocationCount--;
        if (invocationCount > 0)
        {
          Portable.println("Resend request");
        }
      } while (invocationCount > 0);
    } catch (Exception e)
    {
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#changedDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice, int)
   */
  public void changedDevice(BinaryCPDevice changedDevice, int eventCode)
  {
    storeDescriptionsInCache(changedDevice);

    // trigger new service value request if services have changed
    if ((eventCode & BinaryCPConstants.EVENT_CODE_SERVICE_CHANGE) > 0)
    {
      CriticalSection.enter(listLock);
      CollectionHelper.tryAdd(initialServiceValueDeviceList, changedDevice);
      CriticalSection.exit(listLock);
    }
    if (binaryTemplateEntity != null)
    {
      binaryTemplateEntity.changedDevice(changedDevice, eventCode);
    }
    if (deviceEventListener != null)
    {
      deviceEventListener.changedDevice(changedDevice, eventCode);
    }

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#deviceGone(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void deviceGone(BinaryCPDevice goneDevice)
  {
    if (binaryTemplateEntity != null)
    {
      binaryTemplateEntity.deviceGone(goneDevice);
    }
    if (deviceEventListener != null)
    {
      deviceEventListener.deviceGone(goneDevice);
    }

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#newDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void newDevice(BinaryCPDevice newDevice)
  {
    CriticalSection.enter(listLock);
    initialServiceValueDeviceList.add(newDevice);
    CriticalSection.exit(listLock);

    storeDescriptionsInCache(newDevice);

    if (binaryTemplateEntity != null)
    {
      binaryTemplateEntity.newDevice(newDevice);
    }
    if (deviceEventListener != null)
    {
      deviceEventListener.newDevice(newDevice);
    }
  }

  private void storeDescriptionsInCache(BinaryCPDevice device)
  {
    // store descriptions in cache
    File deviceDirectoryFile = new File(descriptionCacheDirectory.getAbsolutePath() + "/" + device.getDeviceID());
    if (!deviceDirectoryFile.exists())
    {
      deviceDirectoryFile.mkdir();
    }
    if (deviceDirectoryFile.exists())
    {
      Vector deviceDescriptionDate = new Vector();
      deviceDescriptionDate.add(BinaryUPnPConstants.descriptionDateToString(device.getDeviceDescriptionDate()));
      ResourceHelper.storeStringListToFile(deviceDirectoryFile.getAbsolutePath() + "/deviceDescriptionDate.txt",
        deviceDescriptionDate);

      try
      {
        FileOutputStream deviceDescriptionOutputStream =
          new FileOutputStream(deviceDirectoryFile.getAbsolutePath() + "/deviceDescription.bin");
        deviceDescriptionOutputStream.write(device.getDescriptionMessage());
        deviceDescriptionOutputStream.close();

        // store service descriptions        
        Enumeration serviceDescriptionIDs =
          CollectionHelper.getPersistentKeysEnumeration(device.getServiceDescriptionMessageFromIDTable());
        while (serviceDescriptionIDs.hasMoreElements())
        {
          Integer currentServiceIDObject = (Integer)serviceDescriptionIDs.nextElement();
          byte[] currentServiceDescription =
            (byte[])device.getServiceDescriptionMessageFromIDTable().get(currentServiceIDObject);

          FileOutputStream serviceDescriptionOutputStream =
            new FileOutputStream(deviceDirectoryFile.getAbsolutePath() + "/serviceDescription" +
              currentServiceIDObject.intValue() + ".bin");
          serviceDescriptionOutputStream.write(currentServiceDescription);
          serviceDescriptionOutputStream.close();
        }

      } catch (Exception e)
      {
      }
    }

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPServiceValueListener#valueChanged(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService)
   */
  public void valueChanged(BinaryCPService binaryCPService)
  {
    // check for change in management service
    if (binaryCPService.getServiceType() == BinaryUPnPConstants.ServiceTypeServiceManagement)
    {
      CriticalSection.enter(listLock);
      boolean requestState = false;
      // request state of all services
      for (int i = 0; i < binaryCPService.getBinaryCPDevice().getCPServiceCount(); i++)
      {
        BinaryCPService currentService = binaryCPService.getBinaryCPDevice().getCPService(i);

        if (currentService.getManagementServiceUpdateID() > -1)
        {
          System.out.println("Trigger read management data:" + binaryCPService.getBinaryCPDevice().toString() + "." +
            currentService.toString());
          requestState = true;

          currentService.setRequestManagementState(true);
        }
      }
      // at least one service state should be requested
      if (requestState)
      {
        CollectionHelper.tryAdd(deviceManagementChangedList, binaryCPService.getBinaryCPDevice());
      }
      CriticalSection.exit(listLock);
    }
    if (binaryTemplateEntity != null)
    {
      binaryTemplateEntity.valueChanged(binaryCPService);
    }
    if (serviceValueListener != null)
    {
      serviceValueListener.valueChanged(binaryCPService);
    }
  }

  /** Requests the management state for a service in another thread. */
  public void triggerReadManagement(BinaryCPService binaryCPService)
  {
    CriticalSection.enter(listLock);
    System.out.println("Store to read management data:" + binaryCPService.getBinaryCPDevice().toString() + "." +
      binaryCPService.toString());

    binaryCPService.setRequestManagementState(true);
    // remove from list if needed
    deviceManagementChangedList.remove(binaryCPService.getBinaryCPDevice());
    // insert at the beginning to read immediately
    CollectionHelper.tryInsert(deviceManagementChangedList, binaryCPService.getBinaryCPDevice());
    CriticalSection.exit(listLock);
  }

  /** Pings known devices if this feature is enabled. */
  private void tryPingDevices()
  {
    // send active pings to all known devices
    if (BinaryUPnPConstants.USE_ACTIVE_PINGS)
    {
      if (Portable.currentTimeMillis() > nextPingTime)
      {
        CriticalSection.enter(listLock);
        for (int i = 0; i < deviceList.size(); i++)
        {
          BinaryCPDevice currentDevice = (BinaryCPDevice)deviceList.elementAt(i);
          getBinaryCPMessageManagement().sendPingMessage(currentDevice.getAccessAddress());
        }
        CriticalSection.exit(listLock);
        nextPingTime =
          Portable.currentTimeMillis() + (long)((0.8 + Math.random() * 0.2) * BinaryUPnPConstants.PING_INTERVAL);
      }
    }
  }

  /** Requests the initial service values from newly found devices. */
  private void tryRequestInitialServiceValues()
  {
    BinaryCPDevice pendingDevice = null;
    CriticalSection.enter(listLock);
    if (initialServiceValueDeviceList.size() > 0)
    {
      pendingDevice = (BinaryCPDevice)initialServiceValueDeviceList.remove(0);
    }
    CriticalSection.exit(listLock);

    // request initial values from all found services
    if (pendingDevice != null)
    {
      for (int i = 0; i < pendingDevice.getCPServiceCount(); i++)
      {
        BinaryCPService currentService = pendingDevice.getCPService(i);
        if (currentService.hasServiceValue())
        {
          //          System.out.println("Try to read service value for " + pendingDevice.toString() + "." +
          //            currentService.toString());
          try
          {
            // request from remote service
            currentService.invokeGetValue();
            // trigger initial event
            valueChanged(currentService);
          } catch (ActionFailedException e)
          {
            System.out.println("Could not read service value for " + pendingDevice.toString() + "." +
              currentService.toString() + ":" + e.getMessage());
          }
        }
      }
    }
  }

  /** Tries to request the description from the currently pending device. */
  private void tryRequestDeviceDescription()
  {
    // retrieve description from newly discovered or changed devices
    BinaryCPDeviceInfo pendingDeviceInfo = null;
    CriticalSection.enter(listLock);
    if (deviceInfoList.size() > 0)
    {
      pendingDeviceInfo = (BinaryCPDeviceInfo)deviceInfoList.elementAt(0);
    }
    CriticalSection.exit(listLock);

    if (pendingDeviceInfo != null)
    {
      boolean shiftDeviceInfo = false;
      // leave some time between retries
      if (Portable.currentTimeMillis() - pendingDeviceInfo.getLastDescriptionRequest() > 10000)
      {
        // check local proxy 
        BinaryCPDevice requestedDevice = getDescriptionFromProxy(pendingDeviceInfo);

        // proxy did not return a result
        if (requestedDevice == null)
        {
          Portable.println("BinaryControlPoint: Try to request device description for " +
            pendingDeviceInfo.getDeviceID() + " via " + pendingDeviceInfo.getAccessAddress().getHostAddress() + ":" +
            pendingDeviceInfo.getDescriptionPort());
          pendingDeviceInfo.setLastDescriptionRequest(Portable.currentTimeMillis());
          requestedDevice = invokeGetDescription(pendingDeviceInfo);
        }
        // device description was parsed successfully
        if (requestedDevice != null)
        {
          // Portable.println("  Success");

          BinaryCPDevice existingDevice = getCPDeviceByID(requestedDevice.getDeviceID());
          // check if this is a new device or an update
          if (existingDevice == null)
          {
            Portable.println("BinaryControlPoint: Found new device " + requestedDevice.getName() + " at " +
              requestedDevice.getAccessAddress().getHostAddress());

            // show complete device description
            //            Portable.println(requestedDevice.toDebugString());

            // add new device to internal list
            CriticalSection.enter(listLock);
            deviceList.add(requestedDevice);
            CriticalSection.exit(listLock);
            // trigger events
            newDevice(requestedDevice);
          } else
          {
            Portable.println("BinaryControlPoint: Updated existing device " + requestedDevice.getName() + " at " +
              requestedDevice.getAccessAddress().getHostAddress());
            // retrieve change code
            int eventCode = existingDevice.updateDevice(requestedDevice);
            // trigger events
            changedDevice(existingDevice, eventCode);
          }
          // request was successful, remove from device info list
          CriticalSection.enter(listLock);
          deviceInfoList.remove(0);
          CriticalSection.exit(listLock);
        } else
        {
          // device did not respond, shift to end of list
          shiftDeviceInfo = true;
        }
      } else
      {
        // device must wait, shift to end of list
        shiftDeviceInfo = true;
      }
      if (shiftDeviceInfo)
      {
        CriticalSection.enter(listLock);
        // move from first position to last
        deviceInfoList.add(deviceInfoList.remove(0));
        CriticalSection.exit(listLock);
      }
    }
  }

  /** Reads the management state of all services for a device */
  private void tryUpdateServiceManagementMetadata()
  {
    // retrieve description from newly discovered or changed devices
    BinaryCPDevice managedDevice = null;
    CriticalSection.enter(listLock);
    if (deviceManagementChangedList.size() > 0)
    {
      managedDevice = (BinaryCPDevice)deviceManagementChangedList.remove(0);
    }
    CriticalSection.exit(listLock);

    if (managedDevice != null)
    {
      for (int i = 0; i < managedDevice.getCPServiceCount(); i++)
      {
        BinaryCPService currentService = managedDevice.getCPService(i);
        // check for initial request
        boolean initialRequest = currentService.getManagementServiceUpdateID() == -1;

        if (currentService.isRequestManagementState())
        {
          System.out.println("Read management data:" + currentService.getBinaryCPDevice().toString() + "." +
            currentService.toString());
          boolean active = currentService.isActive();
          boolean evented = currentService.isEvented();
          int eventRate = currentService.getEventRate();
          try
          {
            currentService.invokeGetManagementState();
            currentService.setRequestManagementState(false);
          } catch (ActionFailedException e)
          {
            Portable.println("Error reading management state of " + currentService.toString() + ": " + e.getMessage());
          }
          if (initialRequest || active != currentService.isActive() || evented != currentService.isEvented() ||
            eventRate != currentService.getEventRate())
          {
            changedDevice(managedDevice, BinaryCPConstants.EVENT_CODE_SERVICE_STATE_CHANGE);
          }
        }
      }
    }
  }

  /** Removes deprecated devices from the internal list. */
  private void tryRemoveDeprecatedDevices()
  {
    // remove deprecated devices
    int i = 0;
    CriticalSection.enter(listLock);
    while (i < deviceList.size())
    {

      BinaryCPDevice currentDevice = (BinaryCPDevice)deviceList.elementAt(i);
      if (currentDevice.isDeprecated())
      {
        Portable.println("Remove deprecated device " + currentDevice.getName());
        deviceList.remove(i);
        // free section during event
        CriticalSection.exit(listLock);
        deviceGone(currentDevice);
        CriticalSection.enter(listLock);
      } else
      {
        i++;
      }
    }
    CriticalSection.exit(listLock);

  }

  /**
   * Retrieves the value of deviceEventListener.
   * 
   * @return The value of deviceEventListener
   */
  public IBinaryCPDeviceEventListener getDeviceEventListener()
  {
    return deviceEventListener;
  }

  /**
   * Sets the new value for deviceEventListener.
   * 
   * @param deviceEventListener
   *          The new value for deviceEventListener
   */
  public void setDeviceEventListener(IBinaryCPDeviceEventListener deviceEventListener)
  {
    this.deviceEventListener = deviceEventListener;
  }

  /**
   * Retrieves the value of serviceValueListener.
   * 
   * @return The value of serviceValueListener
   */
  public IBinaryCPServiceValueListener getServiceValueListener()
  {
    return serviceValueListener;
  }

  /**
   * Sets the new value for serviceValueListener.
   * 
   * @param serviceValueListener
   *          The new value for serviceValueListener
   */
  public void setServiceValueListener(IBinaryCPServiceValueListener serviceValueListener)
  {
    this.serviceValueListener = serviceValueListener;
  }

  public void run()
  {
    while (!terminateThread)
    {
      tryPingDevices();

      tryRequestInitialServiceValues();

      tryRequestDeviceDescription();

      tryUpdateServiceManagementMetadata();

      tryRemoveDeprecatedDevices();

      ThreadHelper.sleep(500);
    }
    terminated = true;
  }

  /** Terminates the control point */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(10);
    }
    binaryCPMessageManagement.terminate();
  }

}

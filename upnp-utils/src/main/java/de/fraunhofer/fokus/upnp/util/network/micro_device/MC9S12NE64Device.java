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
package de.fraunhofer.fokus.upnp.util.network.micro_device;

import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.NetworkHelper;

/**
 * This class encapsulates the configuration of one sensor device.
 * 
 * @author Alexander Koenig
 * 
 */
public class MC9S12NE64Device
{

  private boolean     useDHCP       = false;

  private InetAddress clientIP      = null;

  private InetAddress clientNetmask = null;

  private InetAddress clientGateway = null;

  private byte[]      clientMAC     = new byte[6];

  private String      clientName    = null;

  private int         hardwareID    = -1;

  private int         softwareID    = -1;

  private String      appDate       = "";

  private String      appName       = "";

  private long        discoveryTime = System.currentTimeMillis();

  // App specific data
  /** Name of the sensor */
  private String      sensorName;

  /** Application running on the sensor */
  private String      sensorApplication;

  /** Unique ID for the sensor */
  private int         sensorID;

  private InetAddress serverIP;

  private int         serverTCPPort;

  private int         serverUDPPort;

  /**
   * Creates a new instance of MC9S12NE64Device.
   * 
   * @param useDHCP
   * @param clientIP
   * @param clientNetmask
   * @param clientGateway
   * @param clientMAC
   * @param hardwareID
   * @param softwareID
   */
  public MC9S12NE64Device(boolean useDHCP,
    InetAddress clientIP,
    InetAddress clientNetmask,
    InetAddress clientGateway,
    byte[] clientMAC,
    int hardwareID,
    int softwareID)
  {
    this.useDHCP = useDHCP;
    this.clientIP = clientIP;
    this.clientNetmask = clientNetmask;
    this.clientGateway = clientGateway;
    this.clientMAC = clientMAC;
    this.hardwareID = hardwareID;
    this.softwareID = softwareID;
  }

  /**
   * Creates a new instance of MC9S12NE64Device.
   * 
   * @param data
   *          The byte array representation of the device.
   */
  public MC9S12NE64Device(byte[] data)
  {
    try
    {
      // parse representation
      byte[] address = new byte[4];
      int offset = 0;
      byte option = data[offset++];
      byte optionLength = data[offset++];
      while (option != 0 && offset < data.length)
      {
        // client options
        if (option == ConfigurationConstants.OPTION_TYPE_LOCAL_DHCP)
        {
          useDHCP = data[offset] != 0;
        }
        if (option == ConfigurationConstants.OPTION_TYPE_LOCAL_IP)
        {
          System.arraycopy(data, offset, address, 0, 4);
          clientIP = InetAddress.getByAddress(address);
        }
        if (option == ConfigurationConstants.OPTION_TYPE_LOCAL_NETMASK)
        {
          System.arraycopy(data, offset, address, 0, 4);
          clientNetmask = InetAddress.getByAddress(address);
        }
        if (option == ConfigurationConstants.OPTION_TYPE_LOCAL_GATEWAY)
        {
          System.arraycopy(data, offset, address, 0, 4);
          clientGateway = InetAddress.getByAddress(address);
        }
        if (option == ConfigurationConstants.OPTION_TYPE_LOCAL_MAC)
        {
          System.arraycopy(data, offset, clientMAC, 0, 6);
        }
        if (option == ConfigurationConstants.OPTION_TYPE_HARDWARE_ID)
        {
          hardwareID = data[offset] & 0xFF;
        }
        if (option == ConfigurationConstants.OPTION_TYPE_SOFTWARE_ID)
        {
          softwareID = data[offset] & 0xFF;
        }
        if (option == ConfigurationConstants.OPTION_TYPE_FIRMWARE_DATE)
        {
          byte[] name = new byte[optionLength];
          System.arraycopy(data, offset, name, 0, optionLength);
          appDate = StringHelper.byteArrayToString(name);
        }
        if (option == ConfigurationConstants.OPTION_TYPE_FIRMWARE_NAME)
        {
          byte[] name = new byte[optionLength];
          System.arraycopy(data, offset, name, 0, optionLength);
          appName = StringHelper.byteArrayToString(name);
        }

        // check device dependent options
        if (hardwareID == ConfigurationConstants.HARDWARE_ID_SENSOR_NODE &&
          softwareID == ConfigurationConstants.SOFTWARE_ID_COMMON_SENSOR)
        {
          if (option == ConfigurationConstants.OPTION_TYPE_SENSOR_NAME)
          {
            byte[] name = new byte[optionLength];
            System.arraycopy(data, offset, name, 0, optionLength);
            sensorName = StringHelper.byteArrayToString(name);
          }
          if (option == ConfigurationConstants.OPTION_TYPE_SENSOR_APPLICATION)
          {
            byte[] app = new byte[optionLength];
            System.arraycopy(data, offset, app, 0, optionLength);
            sensorApplication = StringHelper.byteArrayToString(app);
          }
          if (option == ConfigurationConstants.OPTION_TYPE_SENSOR_ID)
          {
            sensorID =
              ((data[offset] & 0xFF) << 24) + ((data[offset + 1] & 0xFF) << 16) + ((data[offset + 2] & 0xFF) << 8) +
                (data[offset + 3] & 0xFF);
          }
        }
        // parse device and application dependent options
        if (hardwareID == ConfigurationConstants.HARDWARE_ID_DEMO_BOARD &&
          softwareID == ConfigurationConstants.SOFTWARE_ID_DONGLE)
        {
          // server options
          if (option == ConfigurationConstants.OPTION_TYPE_SERVER_IP)
          {
            System.arraycopy(data, offset, address, 0, 4);
            serverIP = InetAddress.getByAddress(address);
          }
          if (option == ConfigurationConstants.OPTION_TYPE_SERVER_TCP_PORT)
          {
            serverTCPPort = (data[offset] & 0xFF) * 256 + (data[offset] & 0xFF);
          }
          if (option == ConfigurationConstants.OPTION_TYPE_SERVER_UDP_PORT)
          {
            serverUDPPort = (data[offset] & 0xFF) * 256 + (data[offset] & 0xFF);
          }
        }
        // go to next option
        offset += optionLength;

        // read next option
        option = data[offset++];
        if (option != 0)
        {
          optionLength = data[offset++];
        }
      }
    } catch (Exception e)
    {
    }
  }

  /** Checks if the device configuration is valid */
  public boolean isValid()
  {
    return clientIP != null && clientNetmask != null && clientGateway != null;
  }

  /**
   * Retrieves the value of useDHCP.
   * 
   * @return The value of useDHCP
   */
  public boolean isUseDHCP()
  {
    return useDHCP;
  }

  /**
   * Sets the new value for useDHCP.
   * 
   * @param useDHCP
   *          The new value for useDHCP
   */
  public void setUseDHCP(boolean useDHCP)
  {
    this.useDHCP = useDHCP;
  }

  /**
   * Retrieves the value of clientIP.
   * 
   * @return The value of clientIP
   */
  public InetAddress getClientIP()
  {
    return clientIP;
  }

  /**
   * Sets the new value for clientIP.
   * 
   * @param clientIP
   *          The new value for clientIP
   */
  public void setClientIP(InetAddress clientIP)
  {
    this.clientIP = clientIP;
  }

  /**
   * Retrieves the value of clientNetmask.
   * 
   * @return The value of clientNetmask
   */
  public InetAddress getClientNetmask()
  {
    return clientNetmask;
  }

  /**
   * Sets the new value for clientNetmask.
   * 
   * @param clientNetmask
   *          The new value for clientNetmask
   */
  public void setClientNetmask(InetAddress clientNetmask)
  {
    this.clientNetmask = clientNetmask;
  }

  /**
   * Retrieves the value of clientGateway.
   * 
   * @return The value of clientGateway
   */
  public InetAddress getClientGateway()
  {
    return clientGateway;
  }

  /**
   * Sets the new value for clientGateway.
   * 
   * @param clientGateway
   *          The new value for clientGateway
   */
  public void setClientGateway(InetAddress clientGateway)
  {
    this.clientGateway = clientGateway;
  }

  /**
   * Retrieves the value of clientMAC.
   * 
   * @return The value of clientMAC
   */
  public byte[] getClientMAC()
  {
    return clientMAC;
  }

  /**
   * Sets the new value for clientMAC.
   * 
   * @param clientMAC
   *          The new value for clientMAC
   */
  public void setClientMAC(byte[] clientMAC)
  {
    this.clientMAC = clientMAC;
  }

  /**
   * Retrieves the value of clientName.
   * 
   * @return The value of clientName
   */
  public String getClientName()
  {
    return clientName;
  }

  /**
   * Sets the new value for clientName.
   * 
   * @param clientName
   *          The new value for clientName
   */
  public void setClientName(String clientName)
  {
    this.clientName = clientName;
  }

  /**
   * Retrieves the value of hardwareID.
   * 
   * @return The value of hardwareID
   */
  public int getHardwareID()
  {
    return hardwareID;
  }

  /**
   * Sets the new value for hardwareID.
   * 
   * @param hardwareID
   *          The new value for hardwareID
   */
  public void setHardwareID(int hardwareID)
  {
    this.hardwareID = hardwareID;
  }

  /**
   * Retrieves the value of softwareID.
   * 
   * @return The value of softwareID
   */
  public int getSoftwareID()
  {
    return softwareID;
  }

  /**
   * Sets the new value for softwareID.
   * 
   * @param softwareID
   *          The new value for softwareID
   */
  public void setSoftwareID(int softwareID)
  {
    this.softwareID = softwareID;
  }

  /**
   * Retrieves the value of sensorName.
   * 
   * @return The value of sensorName
   */
  public String getSensorName()
  {
    return sensorName;
  }

  /**
   * Sets the new value for sensorName.
   * 
   * @param sensorName
   *          The new value for sensorName
   */
  public void setSensorName(String sensorName)
  {
    this.sensorName = sensorName;
  }

  /**
   * Retrieves the value of sensorApplication.
   * 
   * @return The value of sensorApplication
   */
  public String getSensorApplication()
  {
    return sensorApplication;
  }

  /**
   * Sets the new value for sensorApplication.
   * 
   * @param sensorApplication
   *          The new value for sensorApplication
   */
  public void setSensorApplication(String sensorApplication)
  {
    this.sensorApplication = sensorApplication;
  }

  /**
   * Retrieves the value of sensorID.
   * 
   * @return The value of sensorID
   */
  public int getSensorID()
  {
    return sensorID;
  }

  /**
   * Sets the new value for sensorID.
   * 
   * @param sensorID
   *          The new value for sensorID
   */
  public void setSensorID(int sensorID)
  {
    this.sensorID = sensorID;
  }

  /**
   * Retrieves the value of serverIP.
   * 
   * @return The value of serverIP
   */
  public InetAddress getServerIP()
  {
    return serverIP;
  }

  /**
   * Sets the new value for serverIP.
   * 
   * @param serverIP
   *          The new value for serverIP
   */
  public void setServerIP(InetAddress serverIP)
  {
    this.serverIP = serverIP;
  }

  /**
   * Retrieves the value of serverTCPPort.
   * 
   * @return The value of serverTCPPort
   */
  public int getServerTCPPort()
  {
    return serverTCPPort;
  }

  /**
   * Sets the new value for serverTCPPort.
   * 
   * @param serverTCPPort
   *          The new value for serverTCPPort
   */
  public void setServerTCPPort(int serverTCPPort)
  {
    this.serverTCPPort = serverTCPPort;
  }

  /**
   * Retrieves the value of serverUDPPort.
   * 
   * @return The value of serverUDPPort
   */
  public int getServerUDPPort()
  {
    return serverUDPPort;
  }

  /**
   * Sets the new value for serverUDPPort.
   * 
   * @param serverUDPPort
   *          The new value for serverUDPPort
   */
  public void setServerUDPPort(int serverUDPPort)
  {
    this.serverUDPPort = serverUDPPort;
  }

  /**
   * Retrieves the value of appDate.
   * 
   * @return The value of appDate
   */
  public String getAppDate()
  {
    return appDate;
  }

  /**
   * Sets the new value for appDate.
   * 
   * @param appDate
   *          The new value for appDate
   */
  public void setAppDate(String appDate)
  {
    this.appDate = appDate;
  }

  /**
   * Retrieves the value of appName.
   * 
   * @return The value of appName
   */
  public String getAppName()
  {
    return appName;
  }

  /**
   * Sets the new value for appName.
   * 
   * @param appName
   *          The new value for appName
   */
  public void setAppName(String appName)
  {
    this.appName = appName;
  }

  /**
   * Retrieves the value of discoveryTime.
   * 
   * @return The value of discoveryTime
   */
  public long getDiscoveryTime()
  {
    return discoveryTime;
  }

  /**
   * Sets the new value for discoveryTime.
   * 
   * @param discoveryTime
   *          The new value for discoveryTime
   */
  public void setDiscoveryTime(long discoveryTime)
  {
    this.discoveryTime = discoveryTime;
  }

  /** Compares the MAC address of this device with another MAC address. */
  public boolean equalMAC(byte[] mac)
  {
    if (mac == null || mac.length != 6)
    {
      return false;
    }
    for (int i = 0; i < clientMAC.length; i++)
    {
      if (clientMAC[i] != mac[i])
      {
        return false;
      }
    }
    return true;
  }

  public String toString()
  {
    return NetworkHelper.toMACString(clientMAC);
  }
}

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
import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;

/**
 * This class encapsulates the data contained in a binary UPnP discovery message.
 * 
 * @author Alexander Koenig
 * 
 */
public class BinaryCPDeviceInfo
{
  /** Reference to outer control point */
  private BinaryControlPoint                 binaryControlPoint;

  /** Date */
  private long                               deviceDescriptionDate;

  /** Unique ID */
  private long                               deviceID;

  /** Optional device type from announcement */
  private int                                deviceType;

  /** Used description port */
  private int                                descriptionPort;

  /** Used control port */
  private int                                controlPort;

  /** Used event port */
  private int                                eventPort;

  /** Time of last description request */
  private long                               lastDescriptionRequest;

  /** Time of last discovery message */
  private long                               lastDiscoveryTime;

  /** Address that receives device messages */
  private InetAddress                        accessAddress;

  /** Socket structure that received the announcement message */
  private AbstractHostAddressSocketStructure associatedSocketStructure = null;

  /** Original device address */
  private byte[]                             deviceAddress;

  /** Gateway list to device. May be empty for devices located in the same network. */
  private Vector                             accessEntityList          = new Vector();

  /**
   * Creates a new instance of BinaryCPDeviceInfo.
   * 
   * @param accessAddress
   * @param deviceDescriptionDate
   * @param deviceIDTime
   */
  public BinaryCPDeviceInfo(InetAddress accessAddress, long deviceDescriptionDate, long deviceID)
  {
    this.accessAddress = accessAddress;
    this.deviceID = deviceID;
    this.deviceDescriptionDate = deviceDescriptionDate;
    lastDiscoveryTime = Portable.currentTimeMillis();
    this.descriptionPort = BinaryUPnPConstants.DescriptionPort;
  }

  public boolean equals(Object obj)
  {
    if (obj instanceof BinaryCPDevice)
    {
      return deviceID == ((BinaryCPDevice)obj).getDeviceID();
    }
    return super.equals(obj);
  }

  /** Checks whether this gateway path is equal to another gateway path. */
  public boolean hasEqualPath(BinaryCPDeviceInfo compareDeviceInfo)
  {
    if (accessEntityList.size() != compareDeviceInfo.getAccessEntityList().size())
    {
      Portable.println("  Number of gateway entries changed");
      return false;
    }
    // compare all access entities
    for (int i = 0; i < accessEntityList.size(); i++)
    {
      GatewayData currentGatewayData = getAccessEntityData(i);
      GatewayData compareGatewayData = compareDeviceInfo.getAccessEntityData(i);
      if (!currentGatewayData.equals(compareGatewayData))
      {
        Portable.println("  Gateway entry " + i + " changed");
        return false;
      }
    }
    return true;
  }

  /** Adds access entities to a request message */
  public void addAccessEntities(ByteArrayOutputStream outputStream)
  {
    try
    {
      for (int i = 0; i < getAccessEntityList().size(); i++)
      {
        GatewayData accessEntityData = getAccessEntityData(i);
        outputStream.write(accessEntityData.toByteArrayForRequest());
      }
    } catch (Exception e)
    {
    }
  }

  /**
   * Retrieves the lastDiscoveryTime.
   * 
   * @return The lastDiscoveryTime.
   */
  public long getLastDiscoveryTime()
  {
    return lastDiscoveryTime;
  }

  /**
   * Sets the lastDiscoveryTime.
   */
  public void updateDiscoveryTime()
  {
    this.lastDiscoveryTime = Portable.currentTimeMillis();
  }

  /**
   * Retrieves the accessAddress.
   * 
   * @return The accessAddress.
   */
  public InetAddress getAccessAddress()
  {
    return accessAddress;
  }

  /**
   * Retrieves the deviceID.
   * 
   * @return The deviceID.
   */
  public long getDeviceID()
  {
    return deviceID;
  }

  /**
   * Retrieves the value of deviceType.
   * 
   * @return The value of deviceType
   */
  public int getDeviceType()
  {
    return deviceType;
  }

  /**
   * Sets the new value for deviceType.
   * 
   * @param deviceType
   *          The new value for deviceType
   */
  public void setDeviceType(int deviceType)
  {
    this.deviceType = deviceType;
  }

  /**
   * @return Returns the controlPoint.
   */
  public BinaryControlPoint getBinaryControlPoint()
  {
    return binaryControlPoint;
  }

  /**
   * Sets the binaryControlPoint.
   * 
   * @param binaryControlPoint
   *          The binaryControlPoint to set.
   */
  public void setBinaryControlPoint(BinaryControlPoint binaryControlPoint)
  {
    this.binaryControlPoint = binaryControlPoint;
  }

  /**
   * Retrieves the value of deviceAddress.
   * 
   * @return The value of deviceAddress
   */
  public byte[] getDeviceAddress()
  {
    return deviceAddress;
  }

  /**
   * Sets the new value for deviceAddress.
   * 
   * @param deviceAddress
   *          The new value for deviceAddress
   */
  public void setDeviceAddress(byte[] deviceAddress)
  {
    this.deviceAddress = deviceAddress;
  }

  /**
   * Retrieves the value of deviceDescriptionDate.
   * 
   * @return The value of deviceDescriptionDate
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

  /**
   * Retrieves the value of descriptionPort.
   * 
   * @return The value of descriptionPort
   */
  public int getDescriptionPort()
  {
    return descriptionPort;
  }

  /**
   * Sets the new value for descriptionPort.
   * 
   * @param descriptionPort
   *          The new value for descriptionPort
   */
  public void setDescriptionPort(int descriptionPort)
  {
    this.descriptionPort = descriptionPort;
  }

  /**
   * Retrieves the value of controlPort.
   * 
   * @return The value of controlPort
   */
  public int getControlPort()
  {
    return controlPort;
  }

  /**
   * Sets the new value for controlPort.
   * 
   * @param controlPort
   *          The new value for controlPort
   */
  public void setControlPort(int controlPort)
  {
    this.controlPort = controlPort;
  }

  /**
   * Retrieves the value of eventPort.
   * 
   * @return The value of eventPort
   */
  public int getEventPort()
  {
    return eventPort;
  }

  /**
   * Sets the new value for eventPort.
   * 
   * @param eventPort
   *          The new value for eventPort
   */
  public void setEventPort(int eventPort)
  {
    this.eventPort = eventPort;
  }

  /**
   * Retrieves the value of gatewayList.
   * 
   * @return The value of gatewayList
   */
  public Vector getAccessEntityList()
  {
    return accessEntityList;
  }

  /**
   * Retrieves a specific gateway data entry.
   * 
   * @param index
   * @return
   */
  public GatewayData getAccessEntityData(int index)
  {
    if (index >= 0 && index < accessEntityList.size())
    {
      return (GatewayData)accessEntityList.elementAt(index);
    }
    return null;
  }

  public int getAccessEntityCount()
  {
    return accessEntityList.size();
  }

  /**
   * Sets the new value for gatewayList.
   * 
   * @param accessEntityList
   *          The new value for gatewayList
   */
  public void setAccessEntityList(Vector accessEntityList)
  {
    this.accessEntityList = accessEntityList;
  }

  /**
   * Retrieves the value of lastDescriptionRequest.
   * 
   * @return The value of lastDescriptionRequest
   */
  public long getLastDescriptionRequest()
  {
    return lastDescriptionRequest;
  }

  /**
   * Sets the new value for lastDescriptionRequest.
   * 
   * @param lastDescriptionRequest
   *          The new value for lastDescriptionRequest
   */
  public void setLastDescriptionRequest(long lastDescriptionRequest)
  {
    this.lastDescriptionRequest = lastDescriptionRequest;
  }

  /**
   * Retrieves the value of associatedHostAddress.
   * 
   * @return The value of associatedHostAddress
   */
  public InetAddress getAssociatedHostAddress()
  {
    if (associatedSocketStructure == null)
    {
      return null;
    }
    return associatedSocketStructure.getHostAddress();
  }

  /**
   * Retrieves the value of associatedSocketStructure.
   * 
   * @return The value of associatedSocketStructure
   */
  public AbstractHostAddressSocketStructure getAssociatedSocketStructure()
  {
    return associatedSocketStructure;
  }

  /**
   * Sets the new value for associatedSocketStructure.
   * 
   * @param associatedSocketStructure
   *          The new value for associatedSocketStructure
   */
  public void setAssociatedSocketStructure(AbstractHostAddressSocketStructure associatedSocketStructure)
  {
    this.associatedSocketStructure = associatedSocketStructure;
  }

  /**
   * Retrieves the response wait time for individual requests. This value is adapted to the number of passed gateways.
   */
  public int getResponseWaitTime()
  {
    return 2000 + getAccessEntityCount() * 500;
  }

  /** Updates this device info from another received device info */
  public int updateDeviceInfo(BinaryCPDeviceInfo deviceInfo)
  {
    int result = 0;
    if (descriptionPort != deviceInfo.getDescriptionPort())
    {
      descriptionPort = deviceInfo.getDescriptionPort();
      result = BinaryCPConstants.EVENT_CODE_PORT_CHANGE;
    }
    if (controlPort != deviceInfo.getControlPort())
    {
      controlPort = deviceInfo.getControlPort();
      result = BinaryCPConstants.EVENT_CODE_PORT_CHANGE;
    }
    if (!associatedSocketStructure.equals(deviceInfo.getAssociatedSocketStructure()))
    {
      associatedSocketStructure = deviceInfo.getAssociatedSocketStructure();
      result = BinaryCPConstants.EVENT_CODE_PATH_CHANGE;
    }
    return result;
  }

}

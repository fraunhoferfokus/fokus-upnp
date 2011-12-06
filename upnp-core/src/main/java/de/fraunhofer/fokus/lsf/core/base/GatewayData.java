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
package de.fraunhofer.fokus.lsf.core.base;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class stores data for one gateway hop.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class GatewayData
{

  /** Address for access or response */
  private byte[] forwarderAddress;

  /** Port to send response messages to */
  private byte[] responsePort     = null;

  /** Port to access description */
  private byte[] descriptionPort  = null;

  /** Port to access control */
  private byte[] controlPort      = null;

  /** Port to access eventing */
  private byte[] eventPort        = null;

  /** ID of outgoing interface */
  private int    forwarderID;

  /** Gateway ID */
  private long   id               = -1;

  /** Type of outgoing interface */
  private int    forwarderPhyType = 0;

  /**
   * Creates a new instance of GatewayData.
   * 
   * @param forwarderAddress
   * @param forwarderID
   */
  public GatewayData(byte[] address, int forwarderID)
  {
    this.forwarderAddress = address;
    this.forwarderID = forwarderID;
  }

  /**
   * Checks whether this gateway data is equal to another gateway data. Returns true if all properties are equal.
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof GatewayData)
    {
      GatewayData compareData = (GatewayData)obj;
      boolean result =
        ByteArrayHelper.isEqual(forwarderAddress, compareData.getForwarderAddress()) &&
          ByteArrayHelper.isEqual(descriptionPort, compareData.getDescriptionPort()) &&
          ByteArrayHelper.isEqual(controlPort, compareData.getControlPort()) &&
          ByteArrayHelper.isEqual(responsePort, compareData.getResponsePort()) &&
          forwarderID == compareData.getForwarderID() && forwarderPhyType == compareData.getForwarderPhyType() &&
          (id == -1 || id == compareData.getID());

      if (!result)
      {
        Portable.println("Forwarder address is " +
          ByteArrayHelper.isEqual(forwarderAddress, compareData.getForwarderAddress()));
        Portable.println("ResponsePort is " + ByteArrayHelper.isEqual(responsePort, compareData.getResponsePort()));
        Portable.println("Forwarder ID is " + (forwarderID == compareData.getForwarderID()));
        Portable.println("ID is " + (id == compareData.getID()));
      }

      return result;
    }
    return super.equals(obj);
  }

  /**
   * Retrieves the value of forwarderAddress.
   * 
   * @return The value of forwarderAddress
   */
  public byte[] getForwarderAddress()
  {
    return forwarderAddress;
  }

  /**
   * Retrieves the value of forwarderAddress.
   * 
   * @return The value of forwarderAddress
   */
  public InetAddress getForwarderInetAddress()
  {
    try
    {
      return InetAddress.getByAddress(forwarderAddress);
    } catch (UnknownHostException e)
    {
    }
    return null;
  }

  /**
   * Sets the new value for forwarderAddress.
   * 
   * @param forwarderAddress
   *          The new value for forwarderAddress
   */
  public void setForwarderAddress(byte[] address)
  {
    this.forwarderAddress = address;
  }

  /**
   * Retrieves the value of responsePort.
   * 
   * @return The value of responsePort
   */
  public byte[] getResponsePort()
  {
    return responsePort;
  }

  /**
   * Retrieves the value of responsePort.
   * 
   * @return The value of responsePort
   */
  public int getResponsePortAsInt()
  {
    if (responsePort == null)
    {
      return -1;
    }
    return ByteArrayHelper.byteArrayToUInt16(responsePort, 0);
  }

  /**
   * Sets the new value for responsePort.
   * 
   * @param responsePort
   *          The new value for responsePort
   */
  public void setResponsePort(byte[] port)
  {
    this.responsePort = port;
  }

  /**
   * Retrieves the value of descriptionPort.
   * 
   * @return The value of descriptionPort
   */
  public byte[] getDescriptionPort()
  {
    return descriptionPort;
  }

  /**
   * Retrieves the value of descriptionPort.
   * 
   * @return The value of descriptionPort
   */
  public int getDescriptionPortAsInt()
  {
    if (descriptionPort == null)
    {
      return BinaryUPnPConstants.DescriptionPort;
    }
    return ByteArrayHelper.byteArrayToUInt16(descriptionPort, 0);
  }

  /**
   * Sets the new value for descriptionPort.
   * 
   * @param descriptionPort
   *          The new value for descriptionPort
   */
  public void setDescriptionPort(byte[] descriptionPort)
  {
    this.descriptionPort = descriptionPort;
  }

  /**
   * Retrieves the value of controlPort.
   * 
   * @return The value of controlPort
   */
  public byte[] getControlPort()
  {
    return controlPort;
  }

  /**
   * Retrieves the value of controlPort.
   * 
   * @return The value of controlPort
   */
  public int getControlPortAsInt()
  {
    if (controlPort == null)
    {
      return BinaryUPnPConstants.ControlPort;
    }
    return ByteArrayHelper.byteArrayToUInt16(controlPort, 0);
  }

  /**
   * Sets the new value for controlPort.
   * 
   * @param controlPort
   *          The new value for controlPort
   */
  public void setControlPort(byte[] controlPort)
  {
    this.controlPort = controlPort;
  }

  /**
   * Retrieves the value of eventPort.
   * 
   * @return The value of eventPort
   */
  public byte[] getEventPort()
  {
    return eventPort;
  }

  /**
   * Sets the new value for eventPort.
   * 
   * @param eventPort
   *          The new value for eventPort
   */
  public void setEventPort(byte[] eventPort)
  {
    this.eventPort = eventPort;
  }

  /** Retrieves the socket address for response messages. */
  public InetSocketAddress getResponseSocketAddress()
  {
    try
    {
      return new InetSocketAddress(InetAddress.getByAddress(forwarderAddress),
        ByteArrayHelper.byteArrayToUInt16(responsePort, 0));
    } catch (UnknownHostException e)
    {
    }
    return null;
  }

  /**
   * Retrieves the value of forwarderID.
   * 
   * @return The value of forwarderID
   */
  public int getForwarderID()
  {
    return forwarderID;
  }

  /**
   * Sets the new value for forwarderID.
   * 
   * @param forwarderID
   *          The new value for forwarderID
   */
  public void setForwarderID(int forwarderID)
  {
    this.forwarderID = forwarderID;
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

  /**
   * Sets the new value for id.
   * 
   * @param id
   *          The new value for id
   */
  public void setID(long id)
  {
    this.id = id;
  }

  /**
   * Retrieves the value of forwarderPhyType.
   * 
   * @return The value of forwarderPhyType
   */
  public int getForwarderPhyType()
  {
    return forwarderPhyType;
  }

  /**
   * Sets the new value for forwarderPhyType.
   * 
   * @param forwarderPhyType
   *          The new value for forwarderPhyType
   */
  public void setForwarderPhyType(int forwarderPhyType)
  {
    this.forwarderPhyType = forwarderPhyType;
  }

  /** Returns the byte array for a response gateway entity. */
  public byte[] toByteArrayForResponse()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add forwarderAddress
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeResponseForwarderAddress);
      byteArrayOutputStream.write(forwarderAddress.length);
      byteArrayOutputStream.write(forwarderAddress);
      // optional responsePort
      if (responsePort != null)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeResponseForwarderPort);
        byteArrayOutputStream.write(responsePort.length);
        byteArrayOutputStream.write(responsePort);
      }
      // forwarder phy type
      //      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeResponseForwarderPhyType);
      //      byteArrayOutputStream.write(1);
      //      byteArrayOutputStream.write(forwarderPhyType);
      if (id != -1)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeResponseID);
        byteArrayOutputStream.write(4);
        byteArrayOutputStream.write(ByteArrayHelper.int32ToByteArray((int)id));
      }
      // forwarder ID is last
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeResponseForwarderID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(forwarderID);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Returns the byte array for an access gateway entity. */
  public byte[] toByteArrayForAccess()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add forwarderAddress
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderAddress);
      byteArrayOutputStream.write(forwarderAddress.length);
      byteArrayOutputStream.write(forwarderAddress);
      // optional ports
      if (descriptionPort != null &&
        ByteArrayHelper.byteArrayToUInt16(descriptionPort, 0) != BinaryUPnPConstants.DescriptionPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderDescriptionPort);
        byteArrayOutputStream.write(descriptionPort.length);
        byteArrayOutputStream.write(descriptionPort);
      }
      if (controlPort != null && ByteArrayHelper.byteArrayToUInt16(controlPort, 0) != BinaryUPnPConstants.ControlPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderControlPort);
        byteArrayOutputStream.write(controlPort.length);
        byteArrayOutputStream.write(controlPort);
      }
      if (eventPort != null &&
        ByteArrayHelper.byteArrayToUInt16(eventPort, 0) != BinaryUPnPConstants.EventMulticastPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderEventPort);
        byteArrayOutputStream.write(eventPort.length);
        byteArrayOutputStream.write(eventPort);
      }
      // forwarder phy type
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderPhyType);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(forwarderPhyType);
      if (id != -1)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessID);
        byteArrayOutputStream.write(4);
        byteArrayOutputStream.write(ByteArrayHelper.int32ToByteArray((int)id));
      }
      // forwarder ID is last
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(forwarderID);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Returns the byte array for a request gateway entity. */
  public byte[] toByteArrayForRequest()
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // add forwarderAddress
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderAddress);
      byteArrayOutputStream.write(forwarderAddress.length);
      byteArrayOutputStream.write(forwarderAddress);
      // optional ports
      if (descriptionPort != null &&
        ByteArrayHelper.byteArrayToUInt16(descriptionPort, 0) != BinaryUPnPConstants.DescriptionPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderDescriptionPort);
        byteArrayOutputStream.write(descriptionPort.length);
        byteArrayOutputStream.write(descriptionPort);
      }
      if (controlPort != null && ByteArrayHelper.byteArrayToUInt16(controlPort, 0) != BinaryUPnPConstants.ControlPort)
      {
        byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderControlPort);
        byteArrayOutputStream.write(controlPort.length);
        byteArrayOutputStream.write(controlPort);
      }
      // forwarder ID is last
      byteArrayOutputStream.write(BinaryUPnPConstants.UnitTypeAccessForwarderID);
      byteArrayOutputStream.write(1);
      byteArrayOutputStream.write(forwarderID);

      return byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
    }
    return null;
  }

  /** Returns a descriptive string for this device and its services. */
  public String toDebugString()
  {
    String result = "";
    result +=
      "Address:" + StringHelper.byteArrayToHexDebugString(forwarderAddress) + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ForwarderID:" + getForwarderID() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "ID:" + getID() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "Port:" + getResponsePort() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;
    result += "Phy:" + getForwarderPhyType() + BinaryUPnPConstants.DEBUG_STRING_DIVIDER;

    return result;
  }

}

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
package de.fraunhofer.fokus.lsf.gateway.common.forwarder_module;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryMessageHelper;
import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.MessageTupel;
import de.fraunhofer.fokus.lsf.core.base.GatewayData;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is an abstract base class for forwarder modules. All ForwarderModules should be derived from this class.
 * 
 * @author Alexander Koenig
 * 
 */
public abstract class AbstractLSFForwarderModule implements ILSFForwarderModule
{

  public static final String    Indentation = "      ";

  /** ID identifying this forwarder module */
  protected byte                moduleID;

  /** Host address for this forwarder module */
  protected InetAddress         moduleHostAddress;

  /** Reference to central message forwarder */
  protected LSFMessageForwarder messageForwarder;

  /** Group address used for discovery */
  protected InetSocketAddress   discoveryGroupSocketAddress;

  /** Standard port used to retrieve descriptions */
  protected int                 descriptionPort;

  /** Standard port used to send device requests. */
  protected int                 controlPort;

  /** Group address used for events */
  protected InetSocketAddress   eventGroupSocketAddress;

  /** Time of module creation */
  protected long                creationTime;

  /**
   * Creates a new instance of AbstractForwarderModule.
   * 
   * @param messageForwarder
   */
  public AbstractLSFForwarderModule(LSFMessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
    // addresses and ports used for IP forwarder modules
    this.discoveryGroupSocketAddress =
      new InetSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress, BinaryUPnPConstants.DiscoveryMulticastPort);
    this.descriptionPort = BinaryUPnPConstants.DescriptionPort;
    this.controlPort = BinaryUPnPConstants.ControlPort;
    this.eventGroupSocketAddress =
      new InetSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress, BinaryUPnPConstants.EventMulticastPort);
    this.moduleID = 0;
    this.moduleHostAddress = null;
    creationTime = System.currentTimeMillis();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getModuleID()
   */
  public byte getModuleID()
  {
    return moduleID;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getModuleHostAddress()
   */
  public InetAddress getModuleHostAddress()
  {
    return moduleHostAddress;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#isLocalHostAddressForwarderModule()
   */
  public boolean isLocalHostAddressForwarderModule()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#isNetworkInterfaceForwarderModule()
   */
  public boolean isNetworkInterfaceForwarderModule()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#isSubnetForwarderModule(java.net.InetAddress)
   */
  public boolean isSubnetForwarderModule(InetAddress address)
  {
    return false;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#announceDeviceToForwarderModule(long)
   */
  public boolean canForwardAnnouncementMessage(long deviceID)
  {
    // for standard forwarder modules, all devices should be visible
    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#announceDeviceToMessageForwarder(java.lang.String)
   */
  public boolean canProcessAnnouncementMessage(long deviceID)
  {
    // for standard forwarder modules, all devices should be visible
    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#receivedAnnouncementMessage(de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject)
   */
  public void receivedAnnouncementMessage(BinaryMessageObject message)
  {
    StringHelper.printDebugText("", true, toString() + ":Received announcement from " +
      IPHelper.toString(message.getSourceAddress()), BinaryUPnPConstants.toForwarderDebugString(null,
      message.getBody(),
      null));
    //    Portable.println(toString() + ":Received announcement:\r\n" + BinaryUPnPConstants.toDebugString(message.getBody()));

    long deviceID = BinaryMessageHelper.getDeviceIDFromMessage(message.getBody());
    // check if device should be visible for message forwarder
    if (canProcessAnnouncementMessage(deviceID))
    {
      messageForwarder.processAnnouncementMessage(this, message);
    }
  }

  /** Updates device access tupels in announcement or search response messages */
  private void updateDeviceAccessPorts(BinaryMessageObject message, Vector tupelList, GatewayData newAccessEntity)
  {
    // the LSF uses a local approach to route messages: 
    //    A control point can simply send messages to the access address, plus optional description or control ports
    //    We therefore need to change these port tupels prior to forwarding

    // set original ports from this forwarder module as default
    newAccessEntity.setDescriptionPort(ByteArrayHelper.uint16ToByteArray(descriptionPort));
    newAccessEntity.setControlPort(ByteArrayHelper.uint16ToByteArray(controlPort));
    newAccessEntity.setEventPort(ByteArrayHelper.uint16ToByteArray(eventGroupSocketAddress.getPort()));

    boolean descriptionPortFound = false;
    boolean controlPortFound = false;
    boolean eventPortFound = false;

    // offset of first access entity
    int accessOffset = tupelList.size();

    // replace or add ports in announcement message to allow routing of consecutive requests 
    for (int i = 0; i < tupelList.size(); i++)
    {
      MessageTupel currentMessageTupel = (MessageTupel)tupelList.elementAt(i);
      // store index of first access entity
      if (currentMessageTupel.getTupelType() == BinaryUPnPConstants.UnitTypeAccessForwarderAddress &&
        accessOffset == tupelList.size())
      {
        accessOffset = i;
      }
      if (currentMessageTupel.getTupelType() == BinaryUPnPConstants.UnitTypeDeviceDescriptionPort)
      {
        // store original description port in access entity
        newAccessEntity.setDescriptionPort(currentMessageTupel.getPayload());
        // replace with the gateway port
        currentMessageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getUnicastSocketAddress().getPort()));
        descriptionPortFound = true;
      }
      if (currentMessageTupel.getTupelType() == BinaryUPnPConstants.UnitTypeDeviceControlPort)
      {
        // store original control port in access entity
        newAccessEntity.setControlPort(currentMessageTupel.getPayload());
        // replace with the gateway port
        currentMessageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getUnicastSocketAddress().getPort()));
        controlPortFound = true;
      }
      if (currentMessageTupel.getTupelType() == BinaryUPnPConstants.UnitTypeDeviceEventPort)
      {
        currentMessageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getEventGroupSocketAddress().getPort()));
        eventPortFound = true;
      }
    }
    // add gateway ports to original announcement message

    // add only if not standard port
    if (!descriptionPortFound && getUnicastSocketAddress().getPort() != BinaryUPnPConstants.DescriptionPort)
    {
      MessageTupel messageTupel = new MessageTupel(new byte[] {
          BinaryUPnPConstants.UnitTypeDeviceDescriptionPort, 0
      });
      messageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getUnicastSocketAddress().getPort()));
      tupelList.insertElementAt(messageTupel, accessOffset);
    }
    // add only if not standard port
    if (!controlPortFound && getUnicastSocketAddress().getPort() != BinaryUPnPConstants.ControlPort)
    {
      MessageTupel messageTupel = new MessageTupel(new byte[] {
          BinaryUPnPConstants.UnitTypeDeviceControlPort, 0
      });
      messageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getUnicastSocketAddress().getPort()));
      tupelList.insertElementAt(messageTupel, accessOffset);
    }
    // add only if not standard port
    if (!eventPortFound && getEventGroupSocketAddress().getPort() != BinaryUPnPConstants.EventMulticastPort)
    {
      MessageTupel messageTupel = new MessageTupel(new byte[] {
          BinaryUPnPConstants.UnitTypeDeviceEventPort, 0
      });
      messageTupel.setPayload(ByteArrayHelper.uint16ToByteArray(getEventGroupSocketAddress().getPort()));
      tupelList.insertElementAt(messageTupel, accessOffset);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardAnnouncementMessage(java.lang.String, de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject)
   */
  public void forwardAnnouncementMessage(byte sourceModuleID, BinaryMessageObject message)
  {
    Vector tupelList = new Vector();
    Vector accessEntityList = new Vector();
    // parse message
    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, accessEntityList, null);

    // add access entity for this forwarder
    GatewayData newAccessEntity = new GatewayData(message.getSourceAddress().getAddress().getAddress(), sourceModuleID);
    newAccessEntity.setID(messageForwarder.getID());
    newAccessEntity.setForwarderPhyType(messageForwarder.getForwarderModuleByID(sourceModuleID).getPhyType());

    // update ports
    updateDeviceAccessPorts(message, tupelList, newAccessEntity);

    // add access entity
    accessEntityList.add(newAccessEntity);

    // create new message
    message.setBody(BinaryMessageHelper.toByteArray(tupelList, accessEntityList, null));

    //    Portable.println(toString() + ":Forward announcement");

    StringHelper.printDebugText(Indentation,
      true,
      toString() + ":Forwarded announcement message is:",
      BinaryUPnPConstants.toDebugString(message.getBody()));

    //    StringHelper.printDebugText(Indentation,
    //      true,
    //      toString() + ":Forwarded announcement message is:",
    //      BinaryUPnPConstants.toForwarderDebugString(Indentation, message.getBody(), message.getSourceAddress()
    //        .getAddress()));

    // send to registered discovery address for this forwarder module
    message.setDestinationAddress(discoveryGroupSocketAddress);
    SocketHelper.sendBinaryMessage(message, getDiscoverySocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#sendAnnouncementMessagesToMulticast(java.util.Vector)
   */
  public void sendAnnouncementMessagesToMulticast(Vector messages)
  {
    for (int i = 0; i < messages.size(); i++)
    {
      String currentMessage = (String)messages.elementAt(i);

      BinaryMessageObject messageObject =
        new BinaryMessageObject(StringHelper.stringToByteArray(currentMessage),
          getUnicastSocketAddress(),
          discoveryGroupSocketAddress);

      SocketHelper.sendBinaryMessage(messageObject, getUnicastSocket());
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#receivedSearchMessage(de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject)
   */
  public void receivedSearchMessage(BinaryMessageObject message)
  {
    //    Portable.println(toString() + ":Received search:\r\n" + BinaryUPnPConstants.toDebugString(message.getBody()));
    StringHelper.printDebugText("", true, toString() + ":Received search from " +
      IPHelper.toString(message.getSourceAddress()), null);
    // let central message forwarder handle the message
    messageForwarder.processSearchMessage(this, message);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#canForwardSearchMessage(java.lang.String)
   */
  public boolean canForwardSearchMessage(byte sourceModuleID)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardSearchMessage(java.lang.String, de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject, java.net.InetSocketAddress)
   */
  public void forwardSearchMessage(byte sourceModuleID, BinaryMessageObject message)
  {
    // add response entity for this forwarder
    GatewayData newResponseEntity =
      new GatewayData(message.getSourceAddress().getAddress().getAddress(), sourceModuleID);
    newResponseEntity.setID(messageForwarder.getID());
    newResponseEntity.setResponsePort(ByteArrayHelper.uint16ToByteArray(message.getSourceAddress().getPort()));
    //    newResponseEntity.setForwarderPhyType(getPhyType());

    Vector tupelList = new Vector();
    Vector responseEntityList = new Vector();

    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, null, responseEntityList);
    responseEntityList.add(newResponseEntity);

    // create new message
    message.setBody(BinaryMessageHelper.toByteArray(tupelList, null, responseEntityList));
    StringHelper.printDebugText(Indentation,
      true,
      toString() + ":Forwarded search is:",
      BinaryUPnPConstants.toForwarderDebugString(Indentation, message.getBody(), null));
    //    Portable.println(toString() + ":Forwarded search message is:\r\n" +
    //      BinaryUPnPConstants.toDebugString(message.getBody()));

    // send to registered discovery address for this forwarder module
    message.setDestinationAddress(discoveryGroupSocketAddress);
    // send over unicast socket because responses are handled by this socket 
    SocketHelper.sendBinaryMessage(message, getUnicastSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardSearchResponseMessage(de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject, byte, java.net.InetSocketAddress)
   */
  public void forwardSearchResponseMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress responseAddress)
  {
    Vector tupelList = new Vector();
    Vector accessEntityList = new Vector();
    Vector responseEntityList = new Vector();
    // parse message
    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, accessEntityList, responseEntityList);

    // add access entity for this forwarder
    GatewayData newAccessEntity = new GatewayData(message.getSourceAddress().getAddress().getAddress(), sourceModuleID);
    newAccessEntity.setID(messageForwarder.getID());
    newAccessEntity.setForwarderPhyType(messageForwarder.getForwarderModuleByID(sourceModuleID).getPhyType());

    // update ports
    updateDeviceAccessPorts(message, tupelList, newAccessEntity);

    // add access entity
    accessEntityList.add(newAccessEntity);

    // remove handled response entity
    responseEntityList.remove(responseEntityList.size() - 1);

    // create new message
    message.setBody(BinaryMessageHelper.toByteArray(tupelList, accessEntityList, responseEntityList));
    StringHelper.printDebugText(Indentation,
      true,
      toString() + ":Forwarded search response is:",
      BinaryUPnPConstants.toForwarderDebugString(Indentation, message.getBody(), null));

    //    Portable.println(toString() + ":Forwarded search response:");
    //    Portable.println(toString() + ":Forwarded search response message is:\r\n" +
    //      BinaryUPnPConstants.toDebugString(message.getBody()));

    // send to parsed response address
    message.setDestinationAddress(responseAddress);
    SocketHelper.sendBinaryMessage(message, getUnicastSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardDeviceRequestMessage(byte, de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject, java.net.InetSocketAddress)
   */
  public void forwardDeviceRequestMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress accessAddress)
  {
    // build response entity for this forwarder
    GatewayData newResponseEntity =
      new GatewayData(message.getSourceAddress().getAddress().getAddress(), sourceModuleID);
    // set source port 
    newResponseEntity.setResponsePort(ByteArrayHelper.uint16ToByteArray(message.getSourceAddress().getPort()));
    //    newResponseEntity.setForwarderPhyType(getPhyType());

    Vector tupelList = new Vector();
    Vector accessEntityList = new Vector();
    Vector responseEntityList = new Vector();

    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, accessEntityList, responseEntityList);
    // remove handled access entity
    accessEntityList.remove(accessEntityList.size() - 1);
    // add response entity
    responseEntityList.add(newResponseEntity);

    // create new message
    message.setBody(BinaryMessageHelper.toByteArray(tupelList, accessEntityList, responseEntityList));

    //    Portable.println("Forwarded device request message is:\r\n" + BinaryUPnPConstants.toDebugString(message.getBody()));
    StringHelper.printDebugText(Indentation,
      true,
      toString() + ":Forwarded device request message is:",
      BinaryUPnPConstants.toForwarderDebugString(Indentation, message.getBody(), message.getSourceAddress()
        .getAddress()));

    // send to parsed access address
    message.setDestinationAddress(accessAddress);
    SocketHelper.sendBinaryMessage(message, getUnicastSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardDeviceResponseMessage(byte, de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject, java.net.InetSocketAddress)
   */
  public void forwardDeviceResponseMessage(byte sourceModuleID,
    BinaryMessageObject message,
    InetSocketAddress responseAddress)
  {
    Vector tupelList = new Vector();
    Vector responseEntityList = new Vector();

    BinaryMessageHelper.parseMessageForForwarding(message.getBody(), tupelList, null, responseEntityList);
    // remove handled response entity
    responseEntityList.remove(responseEntityList.size() - 1);

    // create new message
    message.setBody(BinaryMessageHelper.toByteArray(tupelList, null, responseEntityList));

    StringHelper.printDebugText(Indentation,
      true,
      toString() + ":Forwarded device response message is:",
      BinaryUPnPConstants.toForwarderDebugString(Indentation, message.getBody(), message.getSourceAddress()
        .getAddress()));

    // send to parsed access address
    message.setDestinationAddress(responseAddress);
    SocketHelper.sendBinaryMessage(message, getUnicastSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#canForwardEventMessage(long)
   */
  public boolean canForwardEventMessage(long deviceID)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#receivedEventMessage(de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject)
   */
  public void receivedEventMessage(BinaryMessageObject message)
  {
    //    Portable.println(toString() + ":Received event:\r\n" + BinaryUPnPConstants.toDebugString(message.getBody()));
    messageForwarder.processEventMessage(this, message);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#forwardEventMessage(de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject)
   */
  public void forwardEventMessage(BinaryMessageObject message)
  {
    int newTupelOffset = message.getBody().length - 1;
    // accessID is not a complete gateway entity 
    byte[] newMessageBody = new byte[message.getBody().length + 6];

    // ignore end of packet
    Portable.arraycopy(message.getBody(), 0, newMessageBody, 0, newTupelOffset);
    newMessageBody[newTupelOffset++] = BinaryUPnPConstants.UnitTypeAccessID;
    newMessageBody[newTupelOffset++] = 4;
    Portable.arraycopy(ByteArrayHelper.uint32ToByteArray(messageForwarder.getID()),
      0,
      newMessageBody,
      newTupelOffset,
      4);
    newTupelOffset += 4;
    newMessageBody[newTupelOffset++] = BinaryUPnPConstants.UnitTypeEndOfPacket;

    // create new message
    message.setBody(newMessageBody);

    //    Portable.println("Forwarded event message is:\r\n" + BinaryUPnPConstants.toDebugString(message.getBody()));

    // send to registered discovery address for this forwarder module
    message.setDestinationAddress(eventGroupSocketAddress);
    SocketHelper.sendBinaryMessage(message, getEventSocket());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getDiscoveryMulticastSocketAddress()
   */
  public InetSocketAddress getDiscoveryGroupSocketAddress()
  {
    return discoveryGroupSocketAddress;
  }

  /**
   * Retrieves the value of eventMulticastSocketAddress.
   * 
   * @return The value of eventMulticastSocketAddress
   */
  public InetSocketAddress getEventGroupSocketAddress()
  {
    return eventGroupSocketAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getCreationTime()
   */
  public long getCreationTime()
  {
    return creationTime;
  }

  public String toString()
  {
    return moduleID + "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
  }

}

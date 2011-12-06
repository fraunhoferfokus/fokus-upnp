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
package de.fraunhofer.fokus.upnp.gateway.internet;

import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.gateway.common.SSDPMSearchHelper;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPNotifyHelper;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.AbstractForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device.DeviceDirectoryCPDevice;
import de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device.DeviceDirectoryDevice;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.util.network.DatagramSocketWrapper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class can be used to forward UPnP messages over the Internet.
 * 
 * @author Alexander Koenig
 * 
 */
public class InternetForwarderModule extends AbstractForwarderModule
{

  /** Reference to outer internet management */
  private InternetManagement                 internetManagement;

  /** Reference to associated socket structure */
  private InternetHostAddressSocketStructure internetHostAddressSocketStructure;

  public InternetForwarderModule(MessageForwarder messageForwarder, InternetManagement internetManagement)
  {
    super(messageForwarder);
    this.internetManagement = internetManagement;
    this.internetHostAddressSocketStructure = internetManagement.getInternetHostAddressSocketStructure();
    this.moduleID = InternetManagementConstants.INTERNET_GATEWAY_MODULE_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedNotifyMessage()
   */
  public void receivedNotifyMessage(HTTPMessageObject message)
  {
    System.out.println(toString() + ": Received NOTIFY message from " + IPHelper.toString(message.getSourceAddress()));
    // NOTIFY messages are only accepted from connected peers
    if (isConnectedPeer(message.getSourceAddress()))
    {
      // we must change received NOTIFY messages
      // this is done to allow a change of the callback address in SUBSCRIBE messages
      // from local, external control points
      String modifiedNotifyMessage =
        SSDPNotifyHelper.extendNotifyForForwarding(message.getHeader(),
          getModuleID(),
          getHTTPServerAddress(),
          getHTTPServerPort());

      System.out.println(toString() + ": Changed received NOTIFY message to [\n " + modifiedNotifyMessage + "]");
      message.setHeader(modifiedNotifyMessage);

      messageForwarder.getSSDPManagement().processNotifyMessage(this, message);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardNotifyMessage(java.lang.String,
   *      de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void forwardNotifyMessage(String sourceModuleID, HTTPMessageObject message)
  {
    // System.out.println(toString() + ": Forward NOTIFY message to Internet...");

    // change NOTIFY message to allow proper forwarding of resulting GET requests
    String modifiedNotifyMessage =
      SSDPNotifyHelper.extendNotifyForForwarding(message.getHeader(),
        sourceModuleID,
        getHTTPServerAddress(),
        getHTTPServerPort());

    // System.out.println("Forwarded message would be [\n" + modifiedNotifyMessage + "]");

    // save changed message to packet manager
    messageForwarder.getUDPPacketManager().addPacket(modifiedNotifyMessage);

    forwardMessageToAllTransparentPeers(modifiedNotifyMessage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#sendMessagesToSSDPMulticast(java.util.Vector)
   */
  public void sendMessagesToSSDPMulticast(Vector messages)
  {
    // for the Internet forwarder, sending to the SSDP multicast address means
    // sending the message to all connected peers
    for (int i = 0; i < messages.size(); i++)
    {
      String currentMessage = (String)messages.elementAt(i);

      forwardMessageToAllTransparentPeers(currentMessage);
    }
  }

  /**
   * Forwards a message to all known peers. This method is also called externally to forward byebye
   * messages for local devices.
   */
  public void forwardMessageToAllTransparentPeers(String message)
  {
    DeviceDirectoryDevice deviceDirectoryDevice =
      internetManagement.getDeviceDirectoryEntity().getDeviceDirectoryDevice();

    // send to all peers from the deviceDirectory server that are transparently connected
    if (deviceDirectoryDevice != null)
    {
      for (int i = 0; i < deviceDirectoryDevice.getConnectedPeersCount(); i++)
      {
        DeviceDirectoryCPDevice peerDevice = deviceDirectoryDevice.getConnectedPeer(i);
        // check if device is already associated with its discovery address
        if (peerDevice.getPeer() != null && peerDevice.getPeer().isTransparent())
        {
          System.out.println(toString() + ": Send NOTIFY to peer " +
            IPHelper.toString(peerDevice.getDiscoverySocketAddress()));
          SSDPNotifyHelper.sendNotify(message, getSSDPSocket(), peerDevice.getDiscoverySocketAddress());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedMSearchMessage()
   */
  public void receivedMSearchMessage(HTTPMessageObject message)
  {
    // currently we do not forward M-SEARCH messages (this would lead to multihop)
    // this would require additional handling for hop count, loop detection etc.

    System.out.println(toString() + ": Received M-SEARCH message from " +
      IPHelper.toString(message.getSourceAddress()));

    // M-SEARCH messages are only accepted from connected peers
    if (isConnectedPeer(message.getSourceAddress()))
    {
      // extract ORIGINATOR from M-SEARCH request
      HTTPParser httpParser = new HTTPParser();
      httpParser.parse(message);
      if (httpParser.isMSEARCHMessage())
      {
        String originator = httpParser.getValue(HTTPConstant.X_ORIGINATOR);

        // remove ORIGINATOR header
        message.setHeader(HTTPMessageHelper.removeHeaderLine(message.getHeader(), HTTPConstant.X_ORIGINATOR));

        // process M-SEARCH internally
        Vector responseMessages = messageForwarder.getSSDPManagement().processMSearchMessage(this, message);

        // send responses generated by internal control point
        for (int i = 0; i < responseMessages.size(); i++)
        {
          String responseMessage = (String)responseMessages.elementAt(i);

          // add originator header to response message to allow requester to differentiate responses
          responseMessage = HTTPMessageHelper.addHeaderLine(responseMessage, HTTPConstant.X_ORIGINATOR, originator);

          // send response to requesting control point
          SSDPMSearchHelper.sendMSearchResponse(responseMessage, getSSDPSocket(), message.getSourceAddress());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardMSearchMessage()
   */
  public void forwardMSearchMessage(String sourceModuleID, HTTPMessageObject message, InetSocketAddress replyAddress)
  {
    DeviceDirectoryDevice deviceDirectoryDevice =
      internetManagement.getDeviceDirectoryEntity().getDeviceDirectoryDevice();

    // create new request for forwarding
    int requestID =
      deviceDirectoryDevice.getSSDPMSearchToInternetForwarder().registerSearch(sourceModuleID, moduleID, replyAddress);

    // add request ID to M-SEARCH message
    message.setHeader(HTTPMessageHelper.addHeaderLine(message.getHeader(), HTTPConstant.X_ORIGINATOR, requestID + ""));

    // forward M-SEARCH to all connected peers
    forwardMessageToAllPeers(message.getHeader());
  }

  /** Forwards a M-SEARCH packet to all known peers */
  private void forwardMessageToAllPeers(String message)
  {
    DeviceDirectoryDevice deviceDirectoryDevice =
      internetManagement.getDeviceDirectoryEntity().getDeviceDirectoryDevice();

    // send to all peers from the deviceDirectoryDevice
    if (deviceDirectoryDevice != null)
    {
      for (int i = 0; i < deviceDirectoryDevice.getConnectedPeersCount(); i++)
      {
        DeviceDirectoryCPDevice peerDevice = deviceDirectoryDevice.getConnectedPeer(i);
        if (peerDevice.getPeer() != null)
        {
          SSDPMSearchHelper.sendMSearch(message,
            internetHostAddressSocketStructure.getSSDPDeviceMSearchSendSocket(),
            peerDevice.getDiscoverySocketAddress());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getSSDPSocket()
   */
  public IDatagramSocket getSSDPSocket()
  {
    return new DatagramSocketWrapper(internetHostAddressSocketStructure.getSSDPDeviceSocket());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_gateway.common.ForwarderModule#getSSDPMulticastSocketAddress()
   */
  public InetSocketAddress getSSDPMulticastSocketAddress()
  {
    // the Internet forwarder has no SSDP multicast address
    System.out.println("Invalid call to multicast address from Internet forwarder");

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isHTTPServerAddress(java.net.InetSocketAddress)
   */
  public boolean isHTTPServerAddress(InetSocketAddress serverAddress)
  {
    return serverAddress.equals(internetHostAddressSocketStructure.getHTTPServerSocketAddress());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedHTTPMessage(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public HTTPMessageObject receivedHTTPMessage(HTTPMessageObject message)
  {
    System.out.println(toString() + ": Received HTTP request from " + IPHelper.toString(message.getSourceAddress()));

    // this method receives both external requests as well as internal requests
    // External HTTP requests are only accepted from connected peers
    if (isConnectedPeer(message.getSourceAddress()) ||
      IPHelper.isLocalAddress(message.getSourceAddress().getAddress()))
    {
      HTTPMessageObject response = messageForwarder.getHTTPManagement().processHTTPRequest(getModuleID(), message);

      return response;
    }

    // if message is invalid, return HTTP 404 Not found
    return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_404,
      internetHostAddressSocketStructure.getHTTPServerSocketAddress());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPServerSocketAddress()
   */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return internetHostAddressSocketStructure.getHTTPServerSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_gateway.common.IForwarderModule#getHTTPServerAddress()
   */
  public String getHTTPServerAddress()
  {
    // for the Internet forwarder, we use the DNS name
    return getHTTPServerSocketAddress().getHostName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPClient()
   */
  public IHTTPClient getHTTPClient()
  {
    return new HTTPClient();
  }

  public IHTTPOverUDPClient getHTTPOverUDPClient()
  {
    // TODO
    return null;
  }

  public InetSocketAddress getHTTPOverUDPServerSocketAddress()
  {
    // TODO
    return null;
  }

  public IDatagramSocket getHTTPOverUDPSocket()
  {
    // TODO
    return null;
  }

  public int getHTTPOverUDPServerPort()
  {
    // TODO
    return -1;
  }

  /** Checks if the packet was received from a known connected peer */
  private boolean isConnectedPeer(InetSocketAddress sourceAddress)
  {
    DeviceDirectoryDevice deviceDirectoryDevice =
      internetManagement.getDeviceDirectoryEntity().getDeviceDirectoryDevice();

    boolean result = false;
    // only accept packets from connected peers to prevent inventory taking by simply
    // knowing the IP:Port of this forwarder
    for (int i = 0; !result && i < deviceDirectoryDevice.getConnectedPeersCount(); i++)
    {
      DeviceDirectoryCPDevice currentDevice = deviceDirectoryDevice.getConnectedPeer(i);
      result = result || currentDevice.getPeerAddress().equals(sourceAddress.getAddress());
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    internetHostAddressSocketStructure.terminate();
  }

}

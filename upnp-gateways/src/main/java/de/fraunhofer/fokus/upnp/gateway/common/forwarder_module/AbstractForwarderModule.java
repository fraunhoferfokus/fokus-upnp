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
package de.fraunhofer.fokus.upnp.gateway.common.forwarder_module;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPMSearchHelper;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPNotifyHelper;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;
import de.fraunhofer.fokus.upnp.util.SSDPHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class is an abstract base class for forwarder modules. It does not forward M-SEARCH
 * messages. All ForwarderModules should be derived from this class.
 * 
 * @author Alexander Koenig
 * 
 */
public abstract class AbstractForwarderModule implements IForwarderModule
{

  /** ID identifying this forwarder module */
  protected String            moduleID;

  /** Reference to central message forwarder */
  protected MessageForwarder  messageForwarder;

  /** Multicast address used for SSDP */
  protected InetSocketAddress ssdpMulticastSocketAddress;

  /** Time of module creation */
  protected long              creationTime;

  /**
   * Creates a new instance of AbstractForwarderModule.
   * 
   * @param messageForwarder
   */
  public AbstractForwarderModule(MessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
    this.ssdpMulticastSocketAddress = SSDPConstant.getSSDPMulticastSocketAddress();
    this.moduleID = "";
    creationTime = System.currentTimeMillis();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardNotifyMessage(java.lang.String,
   *      de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public String getModuleID()
  {
    return moduleID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_gateway.common.IForwarderModule#getHTTPServerAddress()
   */
  public String getHTTPServerAddress()
  {
    return getHTTPServerSocketAddress().getAddress().getHostAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_gateway.common.IForwarderModule#getHTTPServerPort()
   */
  public int getHTTPServerPort()
  {
    return getHTTPServerSocketAddress().getPort();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPServerPort()
   */
  public int getHTTPOverUDPServerPort()
  {
    if (getHTTPOverUDPServerSocketAddress() == null)
    {
      return -1;
    }

    return getHTTPOverUDPServerSocketAddress().getPort();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getSSDPMulticastSocketAddress()
   */
  public InetSocketAddress getSSDPMulticastSocketAddress()
  {
    return ssdpMulticastSocketAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedNotifyMessage()
   */
  public void receivedNotifyMessage(HTTPMessageObject message)
  {
    String uuid = SSDPHelper.getUUIDFromNotifyMessage(message.getHeader());
    // check if device should be visible for message forwarder
    if (announceDeviceToMessageForwarder(uuid))
    {
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
    // extend LOCATION in NOTIFY message to allow proper forwarding of resulting GET requests
    String modifiedNotifyMessage =
      SSDPNotifyHelper.extendNotifyForForwarding(message.getHeader(),
        sourceModuleID,
        getHTTPServerAddress(),
        getHTTPServerPort());

    // save changed message to packet manager
    messageForwarder.getUDPPacketManager().addPacket(modifiedNotifyMessage);

    System.out.println(toString() + ": Forward modified NOTIFY: " +
      HTTPMessageHelper.getHeaderDescription(modifiedNotifyMessage));

    SSDPNotifyHelper.sendNotify(modifiedNotifyMessage, getSSDPSocket(), getSSDPMulticastSocketAddress());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#sendMessagesToSSDPMulticast(java.util.Vector)
   */
  public void sendMessagesToSSDPMulticast(Vector messages)
  {
    for (int i = 0; i < messages.size(); i++)
    {
      String currentMessage = (String)messages.elementAt(i);

      // prevent rereception
      messageForwarder.getUDPPacketManager().addPacket(currentMessage);

      // send message with source address of management software
      SSDPNotifyHelper.sendNotify(currentMessage, getSSDPSocket(), getSSDPMulticastSocketAddress());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedMSearchMessage()
   */
  public void receivedMSearchMessage(HTTPMessageObject message)
  {
    // process M-SEARCH message internally
    // this already extends all LOCATION header lines
    Vector responseMessages = messageForwarder.getSSDPManagement().processMSearchMessage(this, message);

    // send responses generated by internal control point
    for (int i = 0; i < responseMessages.size(); i++)
    {
      String responseMessage = (String)responseMessages.elementAt(i);

      // send response to requesting control point
      SSDPMSearchHelper.sendMSearchResponse(responseMessage, getSSDPSocket(), message.getSourceAddress());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardMSearchMessage()
   */
  public void forwardMSearchMessage(String sourceModuleID, HTTPMessageObject message, InetSocketAddress replyAddress)
  {
    // most forwarder modules do not forward M-SEARCH requests
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardMSearchResponseMessage(de.fhg.fokus.magic.http.HTTPMessageObject,
   *      java.lang.String, java.net.InetSocketAddress)
   */
  public void forwardMSearchResponseMessage(HTTPMessageObject message,
    String forwarderModuleID,
    InetSocketAddress replyAddress)
  {
    // because we do not forward M-SEARCH messages, this should never be called
    System.out.println("Unexpected call to forwardMSearchResponseMessage");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#receivedHTTPMessage(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public HTTPMessageObject receivedHTTPMessage(HTTPMessageObject message)
  {
    // normally, all HTTP requests can be processed
    HTTPMessageObject response = messageForwarder.getHTTPManagement().processHTTPRequest(getModuleID(), message);

    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#forwardHTTPRequest(String,
   *      de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public HTTPMessageObject forwardHTTPRequest(String sourceModuleID, HTTPMessageObject message)
  {
    System.out.println("      " + sourceModuleID + ".Forward message :   " + message.toString());

    // create HTTP client to forward request
    IHTTPClient httpClient = getHTTPClient();

    try
    {
      httpClient.sendRequestAndWaitForResponse(message);

      System.out.println("      " + sourceModuleID + ".Response        :   " + httpClient.getResponse().toString());

      return httpClient.getResponse();
    } catch (Exception e1)
    {
      System.out.println("ERROR during forwarding request");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#receivedHTTPOverUDPMessage(de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject)
   */
  public HTTPMessageObject receivedHTTPOverUDPMessage(HTTPMessageObject message)
  {
    // normally, all HTTP requests can be processed
    HTTPMessageObject response = messageForwarder.getHTTPManagement().processHTTPRequest(getModuleID(), message, true);

    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#forwardHTTPOverUDPMessage(java.lang.String,
   *      de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject)
   */
  public HTTPMessageObject forwardHTTPOverUDPMessage(String sourceModuleID, HTTPMessageObject httpRequest)
  {
    System.out.println("      ForwarderModule.Forward UDP msg.: " +
      HTTPMessageHelper.getHeaderDescription(httpRequest.getHeader()) + ":" + httpRequest.getBodyAsString().length());

    // create HTTP client to forward request
    IHTTPOverUDPClient httpClient = getHTTPOverUDPClient();

    try
    {
      httpClient.sendRequestAndWaitForResponse(httpRequest);

      System.out.println("      ForwarderModule: UDP response   : " +
        HTTPMessageHelper.getHeaderDescription(httpClient.getResponse().getHeader()) + ":" +
        httpClient.getResponse().getBodyAsString().length());

      return httpClient.getResponse();
    } catch (Exception e1)
    {
      System.out.println("ERROR during forwarding request");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isHostAddressForwarderModule()
   */
  public boolean isLocalHostAddressForwarderModule()
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isNetworkInterfaceForwarderModule()
   */
  public boolean isNetworkInterfaceForwarderModule()
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isHTTPServerAddress(java.net.InetSocketAddress)
   */
  public boolean isHTTPServerAddress(InetSocketAddress serverAddress)
  {
    return serverAddress.equals(getHTTPServerSocketAddress());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isSubnetForwarderModule()
   */
  public boolean isSubnetForwarderModule(InetAddress address)
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isVisibleDevice()
   */
  public boolean announceDeviceToForwarderModule(AbstractDevice device)
  {
    // for standard forwarder modules, all devices should be visible
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#isVisibleDevice(java.lang.String)
   */
  public boolean announceDeviceToForwarderModule(String uuid)
  {
    // for standard forwarder modules, all devices should be visible
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#announceDeviceToMessageForwarder(java.lang.String)
   */
  public boolean announceDeviceToMessageForwarder(String uuid)
  {
    // for standard forwarder modules, all devices should be visible
    return true;
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
    return moduleID;
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

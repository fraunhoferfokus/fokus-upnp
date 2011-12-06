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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.UPnPDefaults;
import de.fraunhofer.fokus.upnp.gateway.common.ISSDPMessageHandler;
import de.fraunhofer.fokus.upnp.gateway.common.SSDPMSearchHelper;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class is used to forward a M-SEARCH message to other UPnP domains. It listens on a specific
 * port to simplify port forwarding for response packet. Received replies are forwarded to the
 * originator of the M-SEARCH request. Multiple requests are multiplexed by adding an ORIGINATOR
 * header to each M-SEARCH and M-SEARCH response message.
 * 
 * @author Alexander Koenig
 */
public class SSDPMSearchToInternetForwarder extends Thread implements ISSDPMessageHandler
{

  static Logger              logger           = Logger.getLogger("upnp");

  /** Reference to Internet management */
  private InternetManagement internetManagement;

  /** Reference to socket that sends M-SEARCH messages */
  private IDatagramSocket    mSearchSendSocket;

  private int                currentRequestID = 1;

  private Vector             requestList      = new Vector();

  private boolean            terminateThread  = false;

  private boolean            terminated       = false;

  /**
   * Creates a new instance of SSDPMSearchToInternetForwarder.
   * 
   * @param deviceDirectoryDevice
   *          The associated DeviceDirectoryDevice
   * 
   */
  public SSDPMSearchToInternetForwarder(DeviceDirectoryDevice deviceDirectoryDevice)
  {
    super("MessageForwarder.SSDPMSearchToInternetForwarder");
    System.out.println("  Create SSDPMSearchToInternetForwarder on port " +
      InternetManagementConstants.SSDP_DEVICE_M_SEARCH_SEND_PORT);

    internetManagement = deviceDirectoryDevice.getDeviceDirectoryEntity().getInternetManagement();

    mSearchSendSocket = internetManagement.getInternetHostAddressSocketStructure().getSSDPDeviceMSearchSendSocket();

    // add socket to SSDP management
    deviceDirectoryDevice.getExternalSSDPManagement().addSocket(mSearchSendSocket, this);

    start();
  }

  /**
   * Registers a new M-SEARCH.
   * 
   * @param sourceModuleID
   *          The ID of the module that received the M-SEARCH
   * @param forwarderModuleID
   *          The ID of the module that forwards the M-SEARCH
   * @param replyAddress
   *          The socket address for unicast replies
   * 
   * @return A unique requestID for this search.
   */
  public int registerSearch(String sourceModuleID, String forwarderModuleID, InetSocketAddress replyAddress)
  {
    MSearchRequest request = new MSearchRequest(sourceModuleID, forwarderModuleID, replyAddress);
    requestList.add(request);

    System.out.println("Register search from control point " + replyAddress.getAddress().getHostName() + ":" +
      replyAddress.getPort() + " with ID " + request.requestID);

    return request.requestID;
  }

  /**
   * Finds a specific search request.
   * 
   * @param The
   *          requestID for this message
   */
  private MSearchRequest getSearchRequest(int requestID)
  {
    // try to find request
    for (int i = 0; i < requestList.size(); i++)
    {
      if (((MSearchRequest)requestList.elementAt(i)).requestID == requestID)
      {
        return (MSearchRequest)requestList.elementAt(i);
      }
    }

    return null;
  }

  /**
   * Removes outdated requests.
   */
  private void removeDeprecatedRequests()
  {
    int i = 0;
    while (i < requestList.size())
    {
      MSearchRequest currentRequest = (MSearchRequest)requestList.elementAt(i);
      if (System.currentTimeMillis() - currentRequest.requestTime > UPnPDefaults.CP_M_SEARCH_TIMEOUT)
      {
        requestList.remove(i);
      } else
      {
        i++;
      }
    }
  }

  /**
   * Forwards a M-SEARCH message to a peer.
   * 
   * @param message
   *          The M-SEARCH message
   * @param The
   *          requestID for this message
   * @param address
   *          The target address
   * @param port
   *          The target port
   */
  public void sendSearchPacket(String message, int requestID, InetAddress address, int port)
  {
    // try to find request
    MSearchRequest currentRequest = getSearchRequest(requestID);

    // request found
    if (currentRequest != null)
    {
      System.out.println("Send M-SEARCH for requestID " + requestID + " to specific peer " + address.getHostName() +
        ":" + port);

      try
      {
        // add header with requestID to differentiate M-SEARCH responses
        String modifiedMessage =
          HTTPMessageHelper.addHeaderLine(message, HTTPConstant.X_ORIGINATOR, currentRequest.requestID + "");

        SSDPMSearchHelper.sendMSearch(modifiedMessage, mSearchSendSocket, new InetSocketAddress(address, port));
      } catch (Exception e)
      {
        logger.error("Cannot send packet");
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ISSDPMessageHandler#processMessage(de.fhg.fokus.magic.http.HTTPMessageObject)
   */
  public void processMessage(HTTPMessageObject message)
  {
    // extract requestID from response message
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(message);
    if (httpParser.isMSEARCHResponseMessage() && httpParser.isNumericValue(HTTPConstant.X_ORIGINATOR))
    {
      int requestID = (int)httpParser.getNumericValue(HTTPConstant.X_ORIGINATOR);

      // find associated search request
      MSearchRequest mSearchRequest = getSearchRequest(requestID);
      if (mSearchRequest != null)
      {
        // remove ORIGINATOR header from message
        message.setHeader(HTTPMessageHelper.removeHeaderLine(message.getHeader(), HTTPConstant.X_ORIGINATOR));

        // handle the message
        internetManagement.getMessageForwarder()
          .getSSDPManagement()
          .forwardMSearchResponseMessage(mSearchRequest.sourceModuleID,
            mSearchRequest.forwarderModuleID,
            message,
            mSearchRequest.replyAddress);
      }
    }
  }

  public void run()
  {
    // System.out.println(" Start SSDPMSearchToInternetForwarder thread...");
    while (!terminateThread)
    {
      removeDeprecatedRequests();
      try
      {
        Thread.sleep(1000);
      } catch (Exception e)
      {
      }
    }
    System.out.println("  Shutdown SSDPMSearchToInternetForwarder thread...");
    terminated = true;
  }

  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
  }

  /** Inner class to store one M-SEARCH request */
  private class MSearchRequest
  {
    public String            sourceModuleID;

    public String            forwarderModuleID;

    public InetSocketAddress replyAddress = null;

    public long              requestTime;

    public int               requestID;

    /**
     * Creates a new M-SEARCH request.
     * 
     * @param sourceModuleID
     *          The ID of the module that received the M-SEARCH
     * @param forwarderModuleID
     *          The ID of the module that forwards the M-SEARCH *
     * @param replyAddress
     *          The socket address for unicast replies
     */
    public MSearchRequest(String sourceModuleID, String forwarderModuleID, InetSocketAddress replyAddress)
    {
      this.sourceModuleID = sourceModuleID;
      this.forwarderModuleID = forwarderModuleID;
      this.replyAddress = replyAddress;
      requestTime = System.currentTimeMillis();
      currentRequestID = currentRequestID % 10000 + 1;

      requestID = currentRequestID;
    }

  }

}

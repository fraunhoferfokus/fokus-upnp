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

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.IPTunnelHTTPClient;
import de.fraunhofer.fokus.upnp.gateway.common.http_tunnel.IPTunnelHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.http.IHTTPClient;
import de.fraunhofer.fokus.upnp.http.IHTTPOverUDPClient;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;

/**
 * This class can be used as abstract base class for forwarder modules that use an IP tunnel.
 * 
 * @author Alexander Koenig
 * 
 */
public abstract class AbstractIPTunnelForwarderModule extends AbstractForwarderModule implements IIPTunnelEventListener
{

  /** Reference to associated socket structure */
  protected UPnPTunnelSocketStructure upnpTunnelSocketStructure;

  /** Unique ID for this connection */
  protected String                    connectionID;

  /** Optional listener for tunnel events */
  protected IIPTunnelEventListener    ipTunnelEventListener;

  /** Associated IP tunnel */
  protected IIPTunnelNetworkInterface ipTunnelNetworkInterface;

  /**
   * Creates a new instance of TCPTunnelForwarderModule.
   * 
   * @param messageForwarder
   *          The associated message forwarder
   * @param ipTunnelNetworkInterface
   *          The associated virtual network interface
   * @param upnpTunnelSocketStructure
   *          The associated socket structure
   * @param id
   *          Unique ID for this forwarder
   */
  public AbstractIPTunnelForwarderModule(MessageForwarder messageForwarder,
    IIPTunnelNetworkInterface ipTunnelNetworkInterface,
    UPnPTunnelSocketStructure ipTunnelSocketStructure,
    String id)
  {
    super(messageForwarder);
    this.ipTunnelNetworkInterface = ipTunnelNetworkInterface;
    this.upnpTunnelSocketStructure = ipTunnelSocketStructure;
    this.connectionID = id;

    // forward tunnel socket events to this class
    this.ipTunnelNetworkInterface.setIPTunnelEventListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getSSDPSocket()
   */
  public IDatagramSocket getSSDPSocket()
  {
    return upnpTunnelSocketStructure.getSSDPFromIPTunnelSocket();
  }

  /**
   * Retrieves the socket structure associated with this forwarder module.
   * 
   * @return The associated socket structure
   */
  public UPnPTunnelSocketStructure getSocketStructure()
  {
    return upnpTunnelSocketStructure;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPServerSocketAddress()
   */
  public InetSocketAddress getHTTPServerSocketAddress()
  {
    return upnpTunnelSocketStructure.getHTTPServerSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPServerSocketAddress()
   */
  public InetSocketAddress getHTTPOverUDPServerSocketAddress()
  {
    return upnpTunnelSocketStructure.getHTTPOverUDPServerSocketAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPSocket()
   */
  public IDatagramSocket getHTTPOverUDPSocket()
  {
    return upnpTunnelSocketStructure.getHTTPOverUDPSocket();
  }

  /**
   * Checks if this forwarder module has a valid tunnel connection.
   * 
   * @return True if the socket for this tunnel is connected, false otherwise
   */
  public boolean isConnected()
  {
    if (ipTunnelNetworkInterface == null)
    {
      return false;
    }

    return ipTunnelNetworkInterface.isConnected();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#getHTTPClient()
   */
  public IHTTPClient getHTTPClient()
  {
    return new IPTunnelHTTPClient(ipTunnelNetworkInterface.getIPTunnelSocketFactory());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.gateway.common.IForwarderModule#getHTTPOverUDPClient()
   */
  public IHTTPOverUDPClient getHTTPOverUDPClient()
  {
    return new IPTunnelHTTPOverUDPClient(ipTunnelNetworkInterface.getIPTunnelSocketFactory());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener#outerConnectionClosed(de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface)
   */
  public void outerConnectionClosed(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    if (ipTunnelEventListener != null)
    {
      ipTunnelEventListener.outerConnectionClosed(ipTunnelNetworkInterface);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener#initialConfigReceived(de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface, byte[])
   */
  public void initialConfigReceived(IIPTunnelNetworkInterface networkInterface, byte[] data)
  {
    if (ipTunnelEventListener != null)
    {
      ipTunnelEventListener.initialConfigReceived(networkInterface, data);
    }
  }

  /**
   * Retrieves the connectionID.
   * 
   * @return The connectionID
   */
  public String getConnectionID()
  {
    return connectionID;
  }

  /**
   * Sets the ipTunnelListener.
   * 
   * @param ipTunnelListener
   *          The new value for ipTunnelListener
   */
  public void setIPTunnelListener(IIPTunnelEventListener ipTunnelListener)
  {
    this.ipTunnelEventListener = ipTunnelListener;
  }

  /**
   * Retrieves the ipTunnelNetworkInterface.
   * 
   * @return The ipTunnelNetworkInterface
   */
  public IIPTunnelNetworkInterface getIPTunnelNetworkInterface()
  {
    return ipTunnelNetworkInterface;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    super.terminate();
    upnpTunnelSocketStructure.terminate();
    ipTunnelNetworkInterface.terminate();
  }

}

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
package de.fraunhofer.fokus.lsf.gateway.common.tunnel;

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;

/**
 * This class can be used to forward LSF messages over an IP tunnel.
 * 
 * @author Alexander Koenig
 * 
 */
public class TunnelLSFForwarderModule extends AbstractLSFForwarderModule implements IIPTunnelEventListener
{

  /** Reference to associated socket structure */
  protected TunnelLSFSocketStructure  socketStructure;

  /** Unique ID for this connection. This is used by a server to identify reconnected clients. */
  protected String                    connectionID;

  /** Optional listener for tunnel events */
  protected IIPTunnelEventListener    ipTunnelEventListener;

  /** Associated tunnel network interface */
  protected IIPTunnelNetworkInterface ipTunnelNetworkInterface;

  /**
   * Creates a new instance of TunnelLSFForwarderModule.
   * 
   * @param messageForwarder
   * @param ipTunnelNetworkInterface
   * @param socketStructure
   */
  public TunnelLSFForwarderModule(LSFMessageForwarder messageForwarder,
    IIPTunnelNetworkInterface ipTunnelNetworkInterface,
    TunnelLSFSocketStructure socketStructure,
    String connectionID)
  {
    super(messageForwarder);
    this.ipTunnelNetworkInterface = ipTunnelNetworkInterface;
    // forward tunnel socket events to this class
    this.ipTunnelNetworkInterface.setIPTunnelEventListener(this);

    this.socketStructure = socketStructure;
    this.connectionID = connectionID;
    // determine module ID which is used for message routing
    synchronized(messageForwarder.getLock())
    {
      this.moduleID = (byte)messageForwarder.getForwarderModuleNextID();
      messageForwarder.setForwarderModuleNextID(messageForwarder.getForwarderModuleNextID() + 1);
    }
    moduleHostAddress = socketStructure.getHostAddress();
    this.discoveryGroupSocketAddress = socketStructure.getDiscoveryMulticastSocketAddress();
    this.eventGroupSocketAddress = socketStructure.getEventingMulticastSocketAddress();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getDiscoverySocket()
   */
  public IDatagramSocket getDiscoverySocket()
  {
    return socketStructure.getDiscoverySocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocket()
   */
  public IDatagramSocket getUnicastSocket()
  {
    return socketStructure.getUnicastSocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getEventSocket()
   */
  public IDatagramSocket getEventSocket()
  {
    return socketStructure.getEventSocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocketAddress()
   */
  public InetSocketAddress getUnicastSocketAddress()
  {
    return new InetSocketAddress(socketStructure.getHostAddress(), socketStructure.getUnicastSocket().getLocalPort());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule#announceDeviceToForwarderModule(de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice)
   */
  public boolean canForwardAnnouncementMessage(AbstractBinaryDevice device)
  {
    // as tunnel interface, we forward all messages
    System.out.println("Device " + device.toString() + " is announced in " + toString());

    return true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastPort()
   */
  public int getUnicastPort()
  {
    return socketStructure.getUnicastSocket().getLocalPort();
  }

  /**
   * Retrieves the value of socketStructure.
   * 
   * @return The value of socketStructure
   */
  public TunnelLSFSocketStructure getSocketStructure()
  {
    return socketStructure;
  }

  /**
   * Retrieves the value of connectionID.
   * 
   * @return The value of connectionID
   */
  public String getConnectionID()
  {
    return connectionID;
  }

  public String toString()
  {
    return "Tunnel:" + connectionID + " with moduleID " + super.toString();
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

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getPhyType()
   */
  public byte getPhyType()
  {
    return BinaryUPnPConstants.PhyTypeTunnel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    super.terminate();
    socketStructure.terminate();
  }

}

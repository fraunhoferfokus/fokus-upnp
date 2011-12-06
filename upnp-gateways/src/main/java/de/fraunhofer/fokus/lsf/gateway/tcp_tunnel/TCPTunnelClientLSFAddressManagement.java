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
package de.fraunhofer.fokus.lsf.gateway.tcp_tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.fraunhofer.fokus.lsf.core.startup.LSFChildStartupConfiguration;
import de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.lsf.gateway.common.tunnel.TunnelLSFSocketStructure;
import de.fraunhofer.fokus.lsf.gateway.common.tunnel.TunnelLSFForwarderModule;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelConstants;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class implements a TCP tunnel client.
 * 
 * @author Alexander Koenig
 */
public class TCPTunnelClientLSFAddressManagement extends AbstractLSFAddressManagement implements
  Runnable,
  IIPTunnelEventListener
{
  /** Forwarder module instance */
  private TCPTunnelLSFForwarderModule forwarderModule       = null;

  /** Outer socket for the connection */
  private Socket                      socket;

  /** Address of the server */
  private InetSocketAddress           serverSocketAddress;

  /** Inet address for the TCP tunnel */
  private InetAddress                 ipTunnelInetAddress;

  /** Unique ID for this client */
  private byte[]                      connectionID;

  private long                        lastConnectionAttempt = 0;

  private boolean                     terminateThread       = false;

  private boolean                     terminated            = false;

  /**
   * Creates a new instance of TCPTunnelClientLSFAddressManagement.
   * 
   * @param messageForwarder
   * @param serverSocketAddress
   * @param ipTunnelInetAddress
   */
  public TCPTunnelClientLSFAddressManagement(LSFMessageForwarder messageForwarder,
    InetSocketAddress serverSocketAddress,
    InetAddress ipTunnelInetAddress)
  {
    super(messageForwarder);

    this.serverSocketAddress = serverSocketAddress;
    this.ipTunnelInetAddress = ipTunnelInetAddress;
    // check if network properties should be updated for this gateway entity
    LSFChildStartupConfiguration gatewayStartupConfiguration =
      messageForwarder.getGatewayStartupConfiguration(StringHelper.getShortClassName(getClass().getName()));
    if (gatewayStartupConfiguration != null && gatewayStartupConfiguration.hasCustomNetworkProperties())
    {
      multicastAddress = gatewayStartupConfiguration.getMulticastAddress();
      discoveryPort = gatewayStartupConfiguration.getDiscoveryMulticastPort();
      eventPort = gatewayStartupConfiguration.getEventMulticastPort();
    }
    socket = new Socket();
    connectionID = new byte[5];

    // use preferred IP address for ID if available
    if (gatewayStartupConfiguration.getPreferredIPAddressesList().size() > 0)
    {
      try
      {
        // use IP address for ID to allow reconnection
        System.arraycopy(InetAddress.getByName((String)gatewayStartupConfiguration.getPreferredIPAddressesList()
          .elementAt(0)).getAddress(), 0, connectionID, 0, 4);
      } catch (Exception e)
      {
      }
    } else
    {
      // use IP address for ID to allow reconnection
      System.arraycopy(IPHelper.getLocalHostAddress().getAddress(), 0, connectionID, 0, 4);
    }
    // try to use parts of our IP address for the tunnel inet address
    try
    {
      byte[] addressData = this.ipTunnelInetAddress.getAddress();
      addressData[2] = connectionID[2];
      addressData[3] = connectionID[3];

      this.ipTunnelInetAddress = InetAddress.getByAddress(addressData);
    } catch (Exception e)
    {
    }
    Thread localThread = new Thread(this, "TCPTunnelClientLSFAddressManagement");
    localThread.start();
  }

  /** Creates the client forwarder module for a virtual network interface. */
  private void createForwarderModule(TCPTunnelNetworkInterface networkInterface)
  {
    // create socket structure
    TunnelLSFSocketStructure socketStructure =
      new TunnelLSFSocketStructure(networkInterface.getIPTunnelSocketFactory(),
        networkInterface.getIPTunnelInetAddress(),
        multicastAddress,
        discoveryPort,
        eventPort);

    // create forwarder module for socket structure
    forwarderModule =
      new TCPTunnelLSFForwarderModule(messageForwarder,
        networkInterface,
        socketStructure,
        StringHelper.byteArrayToBase32(connectionID));
    // forward tunnel events to this class
    forwarderModule.setIPTunnelListener(this);

    Portable.println("ModuleID:" + forwarderModule.getModuleID() + " on TCP tunnel client");
    socketStructure.printUsedPorts();

    // register in central message forwarder
    messageForwarder.addForwarderModule(forwarderModule);

    // finish setup
    networkInterface.sendIDToTunnel(connectionID);
    // inform optional listener
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.newForwarderModule(forwarderModule);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener#initialConfigReceived(de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface, byte[])
   */
  public void initialConfigReceived(IIPTunnelNetworkInterface networkInterface, byte[] data)
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener#outerConnectionClosed(de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface)
   */
  public void outerConnectionClosed(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    if (forwarderModuleEventListener != null && forwarderModule != null)
    {
      forwarderModuleEventListener.disconnectedForwarderModule(forwarderModule);
    }
    socket = new Socket();
  }

  /**
   * Returns the forwarder module.
   * 
   * @return
   */
  public TunnelLSFForwarderModule getForwarderModule()
  {
    return forwarderModule;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#printDebugStats()
   */
  public void printDebugStats()
  {
    if (socket != null)
    {
      Portable.println("  " + getForwarderModule().getModuleID() + ": Connected to " +
        IPHelper.toString((InetSocketAddress)socket.getRemoteSocketAddress()));
    }
  }

  public void run()
  {

    System.out.println("  Started TCPTunnelClientLSFAddressManagement.");
    while (!terminateThread)
    {
      // try to connect to server
      if (!socket.isConnected() && System.currentTimeMillis() - lastConnectionAttempt > 30000)
      {
        StringHelper.printDebugText("",
          true,
          "Try to connect to server on " + IPHelper.toString(serverSocketAddress),
          "");
        lastConnectionAttempt = System.currentTimeMillis();
        try
        {
          socket.connect(serverSocketAddress, 1000);

          System.out.println("    Connected to " + IPHelper.toString(serverSocketAddress));

          // if this is the first connection attempt
          if (forwarderModule == null)
          {
            // create virtual network interface
            TCPTunnelNetworkInterface tcpTunnelNetworkInterface = new TCPTunnelNetworkInterface(socket);
            tcpTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);
            // set optional network interface parameters
            tcpTunnelNetworkInterface.setMaximumSegmentSize(1400);
            tcpTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
            tcpTunnelNetworkInterface.setPacketGapTime(1);

            createForwarderModule(tcpTunnelNetworkInterface);
          } else
          {
            System.out.println("    Use existing forwarder module");
            // this is a reconnect

            // associate new socket with existing network interface
            forwarderModule.reconnect(socket);

            // send ID to server
            forwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(connectionID);
            // forward event
            if (forwarderModuleEventListener != null)
            {
              forwarderModuleEventListener.reconnectedForwarderModule(forwarderModule);
            }
          }
        } catch (Exception e)
        {
          socket = new Socket();
        }
      }
      // terminate forwarder module if tunnel is disconnected for too long
      if (!socket.isConnected() &&
        forwarderModule != null &&
        System.currentTimeMillis() - forwarderModule.getIPTunnelNetworkInterface().getDisconnectionTime() > IPTunnelConstants.TERMINATION_TIMEOUT)
      {
        terminateForwarderModule();
      }
      // trigger events for associated tunnel network interface
      if (forwarderModule != null)
      {
        ((IEventListener)forwarderModule.getIPTunnelNetworkInterface()).triggerEvents();
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the forwarder module. */
  private void terminateForwarderModule()
  {
    if (forwarderModule != null)
    {
      forwarderModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(forwarderModule);
      // forward event
      if (forwarderModuleEventListener != null)
      {
        forwarderModuleEventListener.removedForwarderModule(forwarderModule);
      }
      forwarderModule = null;
    }
  }

  /** Terminates the management for the TCP tunnel client. */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(20);
    }
    terminateForwarderModule();
  }
}

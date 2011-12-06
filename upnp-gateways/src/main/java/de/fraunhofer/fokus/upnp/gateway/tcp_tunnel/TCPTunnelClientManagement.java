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
package de.fraunhofer.fokus.upnp.gateway.tcp_tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelConstants;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class is used to tunnel UPnP over a TCP connection. This class tries to connect to a TCP
 * server that accepts UPnP tunnels.
 * 
 * @author Alexander Koenig
 */
public class TCPTunnelClientManagement extends Thread implements IIPTunnelEventListener
{

  /** Reference to message forwarder */
  private MessageForwarder              messageForwarder;

  /** Forwarder module for TCP tunnel */
  private TCPTunnelForwarderModule      tcpTunnelForwarderModule = null;

  /** Socket for the connection */
  private Socket                        socket;

  /** Address of the server */
  private InetSocketAddress             serverSocketAddress;

  /** Inet address for the TCP tunnel */
  private InetAddress                   ipTunnelInetAddress;

  private byte[]                        id;

  private long                          lastConnectionAttempt    = 0;

  private boolean                       terminateThread          = false;

  private boolean                       terminated               = false;

  /** Optional listener for client events */
  private IForwarderModuleEventListener forwarderModuleEventListener;

  /**
   * Creates a new instance of TCPClientTunnelManagement.
   * 
   * @param messageForwarder
   *          The central message forwarder
   * @param serverSocketAddress
   *          Address and port of the IP tunnel server
   * @param ipTunnelInetAddress
   *          The address used for the virtual IP tunnel
   */
  public TCPTunnelClientManagement(MessageForwarder messageForwarder,
    InetSocketAddress serverSocketAddress,
    InetAddress ipTunnelInetAddress)
  {
    setName("TCPClientTunnelManagement");
    this.messageForwarder = messageForwarder;
    this.serverSocketAddress = serverSocketAddress;
    this.ipTunnelInetAddress = ipTunnelInetAddress;

    socket = new Socket();
    id = new byte[5];
    // use IP address for ID to allow reconnection
    System.arraycopy(IPHelper.getLocalHostAddress().getAddress(), 0, id, 0, 4);
    start();
  }

  /**
   * Sets the clientEventsListener.
   * 
   * @param clientEventsListener
   *          The new value for clientEventsListener
   */
  public void setForwarderModuleEventListener(IForwarderModuleEventListener eventListener)
  {
    this.forwarderModuleEventListener = eventListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#initialConfigReceived(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface,
   *      byte[])
   */
  public void initialConfigReceived(IIPTunnelNetworkInterface networkInterface, byte[] data)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#socketClosed(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface)
   */
  public void outerConnectionClosed(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    if (forwarderModuleEventListener != null && tcpTunnelForwarderModule != null)
    {
      forwarderModuleEventListener.disconnectedForwarderModule(tcpTunnelForwarderModule);
    }

    socket = new Socket();
  }

  /**
   * Retrieves the tcpTunnelForwarderModule.
   * 
   * @return The tcpTunnelForwarderModule
   */
  public TCPTunnelForwarderModule getTCPTunnelForwarderModule()
  {
    return tcpTunnelForwarderModule;
  }

  public void run()
  {
    System.out.println("  Started TCPClientTunnelManagement. Try to connect to server on " +
      IPHelper.toString(serverSocketAddress));

    while (!terminateThread)
    {
      // try to connect to server
      if (!socket.isConnected() && System.currentTimeMillis() - lastConnectionAttempt > 30000)
      {
        lastConnectionAttempt = System.currentTimeMillis();
        try
        {
          socket.connect(serverSocketAddress, 1000);

          System.out.println("    Connected to " + IPHelper.toString(serverSocketAddress));

          // if this is the first connection attempt
          if (tcpTunnelForwarderModule == null)
          {
            // create virtual network interface
            TCPTunnelNetworkInterface tcpTunnelNetworkInterface = new TCPTunnelNetworkInterface(socket);

            tcpTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);

            // set optional network interface parameters
            tcpTunnelNetworkInterface.setMaximumSegmentSize(1400);
            tcpTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
            tcpTunnelNetworkInterface.setPacketGapTime(2);

            // create UPnP socket structure for TCP tunnel
            System.out.println("    Create TCPTunnelSocketStructure for address " +
              ipTunnelInetAddress.getHostAddress());

            UPnPTunnelSocketStructure ipTunnelSocketStructure =
              new UPnPTunnelSocketStructure(tcpTunnelNetworkInterface.getIPTunnelSocketFactory(),
                messageForwarder.getGatewayMessageManager(),
                ipTunnelInetAddress);

            // create forwarder module
            tcpTunnelForwarderModule =
              new TCPTunnelForwarderModule(messageForwarder,
                tcpTunnelNetworkInterface,
                ipTunnelSocketStructure,
                StringHelper.byteArrayToBase32(id));

            tcpTunnelForwarderModule.setIPTunnelListener(this);

            // add forwarder module to central message forwarder
            messageForwarder.addForwarderModule(tcpTunnelForwarderModule);

            tcpTunnelForwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(id);
            if (forwarderModuleEventListener != null)
            {
              forwarderModuleEventListener.newForwarderModule(tcpTunnelForwarderModule);
            }
          } else
          {
            System.out.println("    Use existing forwarder module");
            // this is a reconnect

            // associate new socket with existing network interface
            tcpTunnelForwarderModule.reconnect(socket);

            // send ID to server
            tcpTunnelForwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(id);
            // forward event
            if (forwarderModuleEventListener != null)
            {
              forwarderModuleEventListener.reconnectedForwarderModule(tcpTunnelForwarderModule);
            }
          }
        } catch (Exception e)
        {
          socket = new Socket();
        }
      }
      // terminate forwarder module if tunnel is disconnected for too long
      if (!socket.isConnected() &&
        tcpTunnelForwarderModule != null &&
        System.currentTimeMillis() - tcpTunnelForwarderModule.getIPTunnelNetworkInterface().getDisconnectionTime() > IPTunnelConstants.TERMINATION_TIMEOUT)
      {
        // terminate forwarder module
        tcpTunnelForwarderModule.terminate();
        // remove from message forwarder
        messageForwarder.removeForwarderModule(tcpTunnelForwarderModule);
        // forward event
        if (forwarderModuleEventListener != null)
        {
          forwarderModuleEventListener.removedForwarderModule(tcpTunnelForwarderModule);
        }
        // allow recreation of forwarder module
        tcpTunnelForwarderModule = null;
      }
      // trigger events for associated tunnel network interface
      if (tcpTunnelForwarderModule != null)
      {
        ((IEventListener)tcpTunnelForwarderModule.getIPTunnelNetworkInterface()).triggerEvents();
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the forwarder module for the TCP tunnel. */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(20);
    }

    if (tcpTunnelForwarderModule != null)
    {
      // terminate forwarder module
      tcpTunnelForwarderModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(tcpTunnelForwarderModule);
      // forward event
      if (forwarderModuleEventListener != null)
      {
        forwarderModuleEventListener.removedForwarderModule(tcpTunnelForwarderModule);
      }
      tcpTunnelForwarderModule = null;
    }
    System.out.println("  Terminated TCPClientTunnelManagement.");
  }

}

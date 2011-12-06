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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.TCPTunnelNetworkInterface;

/**
 * This class is used to tunnel UPnP over a TCP connection. This class provides a TCP server that
 * can accept UPnP tunnel clients. It then creates the appropriate IPTunnelNetworkInterface and the
 * ForwarderModule for the new client.
 * 
 * @author Alexander Koenig
 */
public class TCPTunnelServerManagement extends Thread implements IIPTunnelEventListener
{

  /** Reference to message forwarder */
  private MessageForwarder              messageForwarder;

  /** Vector with all forwarder modules */
  private Vector                        clientForwarderModuleList = new Vector();

  /** Vector with virtual network interfaces without a received ID packet. */
  private Vector                        uninitializedClientList   = new Vector();

  /** Vector with network interfaces that must be terminated */
  private Vector                        garbageClientList         = new Vector();

  /** Server socket to accept connections */
  private ServerSocket                  serverSocket;

  /** Inet address for the TCP tunnel */
  private InetAddress                   ipTunnelInetAddress;

  /** Sync object */
  private Object                        lock                      = new Object();

  /** Optional listener for client events */
  private IForwarderModuleEventListener forwarderModuleEventListener;

  private boolean                       terminateThread           = false;

  private boolean                       terminated                = false;

  /**
   * @param messageForwarder
   * @param serverPort
   *          Listener port for the server
   * @param ipTunnelInetAddress
   *          Inet address used in the tunnel
   */
  public TCPTunnelServerManagement(MessageForwarder messageForwarder, int serverPort, InetAddress ipTunnelInetAddress)
  {
    setName("TCPServerTunnelManagement");
    this.messageForwarder = messageForwarder;
    this.ipTunnelInetAddress = ipTunnelInetAddress;

    try
    {
      serverSocket = new ServerSocket(serverPort);
      serverSocket.setSoTimeout(50);
    } catch (IOException e)
    {
    }

    start();
  }

  /**
   * Sets the eventListener.
   * 
   * @param eventListener
   *          The new value for eventListener
   */
  public void setForwarderModuleEventListener(IForwarderModuleEventListener eventListener)
  {
    this.forwarderModuleEventListener = eventListener;
  }

  /** Retrieves a vector with all devices connected over this server. */
  public Vector getDevices()
  {
    Vector result = new Vector();
    IForwarderModule[] forwarderModules = messageForwarder.getForwarderModules();
    for (int i = 0; i < forwarderModules.length; i++)
    {
      if (clientForwarderModuleList.contains(forwarderModules[i]))
      {
        result.addAll(messageForwarder.getDevicesForForwarderModule(forwarderModules[i].getModuleID()));
      }
    }
    return result;
  }

  /** Retrieves the number of connected clients. */
  public int getConnectedClientCount()
  {
    int result = 0;
    for (int i = 0; i < clientForwarderModuleList.size(); i++)
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)clientForwarderModuleList.elementAt(i);
      if (currentModule.isConnected())
      {
        result++;
      }
    }
    return result;
  }

  /** Retrieves the number of disconnected clients. */
  public int getDisconnectedClientCount()
  {
    int result = 0;
    for (int i = 0; i < clientForwarderModuleList.size(); i++)
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)clientForwarderModuleList.elementAt(i);
      if (!currentModule.isConnected())
      {
        result++;
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#initialConfigReceived(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface,
   *      byte[])
   */
  public void initialConfigReceived(IIPTunnelNetworkInterface networkInterface, byte[] id)
  {
    // this will be a TCPTunnelNetworkInterface
    TCPTunnelNetworkInterface tcpNetworkInterface = (TCPTunnelNetworkInterface)networkInterface;

    String idString = StringHelper.byteArrayToBase32(id);
    System.out.println("Received ID from " + IPHelper.toString(tcpNetworkInterface.getRemoteSocketAddress()) +
      " ID: " + idString);

    boolean found = false;
    // check if an unconnected forwarder module with this ID already exists
    for (int i = 0; i < clientForwarderModuleList.size(); i++)
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)clientForwarderModuleList.elementAt(i);
      if (!currentModule.isConnected() && currentModule.getConnectionID().equals(idString))
      {
        found = true;

        // extract socket from new network interface
        Socket socket = tcpNetworkInterface.extractSocket();

        // associate known forwarder module with new socket
        currentModule.reconnect(socket);
        synchronized(lock)
        {
          // store new interface for termination (because this method is called from
          // the network interface thread, a direct termination would result in a deadlock)
          garbageClientList.add(networkInterface);

          // remove new interface from uninitialized table
          uninitializedClientList.remove(networkInterface);
        }
        // inform listener about reconnected client
        if (forwarderModuleEventListener != null)
        {
          forwarderModuleEventListener.reconnectedForwarderModule(currentModule);
        }
      }
    }
    // if this is a new ID, create a new forwarder module
    if (!found)
    {
      // set inet address to start socket factory
      networkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);

      // create UPnP socket structure for TCP tunnel
      UPnPTunnelSocketStructure tcpTunnelSocketStructure =
        new UPnPTunnelSocketStructure(networkInterface.getIPTunnelSocketFactory(),
          messageForwarder.getGatewayMessageManager(),
          ipTunnelInetAddress);

      // create forwarder module for new client
      TCPTunnelForwarderModule tcpTunnelForwarderModule =
        new TCPTunnelForwarderModule(messageForwarder,
          tcpNetworkInterface,
          tcpTunnelSocketStructure,
          StringHelper.byteArrayToBase32(id));

      tcpTunnelForwarderModule.setIPTunnelListener(this);

      synchronized(lock)
      {
        // remove from uninitialized table
        uninitializedClientList.remove(networkInterface);
        // add to central forwarder
        messageForwarder.addForwarderModule(tcpTunnelForwarderModule);
        // add to internal client vector
        clientForwarderModuleList.add(tcpTunnelForwarderModule);
      }
      // inform listener about new client
      if (forwarderModuleEventListener != null)
      {
        forwarderModuleEventListener.newForwarderModule(tcpTunnelForwarderModule);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#socketClosed(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface)
   */
  public void outerConnectionClosed(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    // this event was forwarded from TCPTunnelForwarderModule
    if (forwarderModuleEventListener != null)
    {
      // find forwarder module
      Enumeration forwarderModules = CollectionHelper.getPersistentEntryEnumeration(clientForwarderModuleList);
      while (forwarderModules.hasMoreElements())
      {
        TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)forwarderModules.nextElement();
        if (currentModule.getIPTunnelNetworkInterface() == ipTunnelNetworkInterface)
        {
          // generate TCP tunnel event that client has been disconnected
          forwarderModuleEventListener.disconnectedForwarderModule(currentModule);
        }
      }
    }
  }

  /** Tries to accept new client connections. */
  private void acceptClients()
  {
    // try to accept connections
    try
    {
      Socket clientSocket = serverSocket.accept();

      // create new virtual network interface for the new client
      TCPTunnelNetworkInterface tcpTunnelNetworkInterface = new TCPTunnelNetworkInterface(clientSocket);

      // set listener for initial config
      tcpTunnelNetworkInterface.setIPTunnelEventListener(this);

      // set optional network interface parameters
      tcpTunnelNetworkInterface.setMaximumSegmentSize(1400);
      tcpTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
      tcpTunnelNetworkInterface.setPacketGapTime(5);

      System.out.println("Client connected from " +
        IPHelper.toString((InetSocketAddress)clientSocket.getRemoteSocketAddress()) +
        ". Wait for initial config packet.");
      synchronized(lock)
      {
        uninitializedClientList.add(tcpTunnelNetworkInterface);
      }
    } catch (Exception e)
    {
    }
  }

  /** Removes network interfaces that should be terminated. */
  private void removeDeprecatedNetworkInterfaces()
  {
    for (int i = 0; i < garbageClientList.size(); i++)
    {
      ((IIPTunnelNetworkInterface)garbageClientList.elementAt(i)).terminate();
    }
    synchronized(lock)
    {
      garbageClientList.clear();
    }
  }

  /** Removes forwarder modules that are unconnected for a long time. */
  private void removeDeprecatedForwarderModules()
  {
    Enumeration forwarderModules = CollectionHelper.getPersistentEntryEnumeration(clientForwarderModuleList);
    while (forwarderModules.hasMoreElements())
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)forwarderModules.nextElement();

      // terminate deprecated forwarder modules
      if (!currentModule.isConnected() &&
        System.currentTimeMillis() - currentModule.getIPTunnelNetworkInterface().getDisconnectionTime() > 60000)
      {
        System.out.println("Remove deprecated forwarder module " + currentModule.getModuleID());
        // terminate the module
        currentModule.terminate();
        // remove from message forwarder
        messageForwarder.removeForwarderModule(currentModule);
        synchronized(lock)
        {
          // remove from local list
          clientForwarderModuleList.remove(currentModule);
        }
        // inform listener about removed client
        if (forwarderModuleEventListener != null)
        {
          forwarderModuleEventListener.removedForwarderModule(currentModule);
        }
      }
    }
  }

  /** Triggers events in the network interfaces of all connected forwarder modules */
  private void triggerEvents()
  {
    for (int i = 0; i < clientForwarderModuleList.size(); i++)
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)clientForwarderModuleList.elementAt(i);
      if (currentModule.getIPTunnelNetworkInterface() instanceof IEventListener)
      {
        ((IEventListener)currentModule.getIPTunnelNetworkInterface()).triggerEvents();
      }
    }
    for (int i = 0; i < uninitializedClientList.size(); i++)
    {
      TCPTunnelNetworkInterface currentNetworkInterface =
        (TCPTunnelNetworkInterface)uninitializedClientList.elementAt(i);
      currentNetworkInterface.triggerEvents();
    }
  }

  public void run()
  {
    System.out.println("  Started TCPServerTunnelManagement. Wait for clients to connect " + "on port " +
      serverSocket.getLocalPort());
    while (!terminateThread)
    {
      acceptClients();
      removeDeprecatedForwarderModules();
      removeDeprecatedNetworkInterfaces();
      triggerEvents();
      // sleep for some time
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the management for the TCP tunnel. */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }

    // terminate uninitialized clients
    for (int i = 0; i < uninitializedClientList.size(); i++)
    {
      ((IIPTunnelNetworkInterface)uninitializedClientList.elementAt(i)).terminate();
    }
    synchronized(lock)
    {
      uninitializedClientList.clear();
    }

    // terminate forwarder modules
    Enumeration forwarderModules = CollectionHelper.getPersistentEntryEnumeration(clientForwarderModuleList);
    while (forwarderModules.hasMoreElements())
    {
      TCPTunnelForwarderModule currentModule = (TCPTunnelForwarderModule)forwarderModules.nextElement();
      currentModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(currentModule);
      synchronized(lock)
      {
        // remove from local list
        clientForwarderModuleList.remove(currentModule);
      }
    }
    // close server socket
    try
    {
      serverSocket.close();
    } catch (Exception e)
    {
    }
    System.out.println("  Terminated TCPServerTunnelManagement.");
  }

}

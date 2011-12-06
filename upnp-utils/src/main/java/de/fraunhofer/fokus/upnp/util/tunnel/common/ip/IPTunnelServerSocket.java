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
package de.fraunhofer.fokus.upnp.util.tunnel.common.ip;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;

/**
 * This class represents a server socket that is waiting for connections from the IP tunnel.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelServerSocket extends IPTunnelSocket
{

  /** IP tunnel TCP sockets created after accepting connections */
  private Hashtable socketTable                 = new Hashtable();

  /** IP tunnel TCP sockets waiting for release */
  private Hashtable releaseSocketTable          = new Hashtable();

  /** IP tunnel TCP sockets that have sent a SYN request and wait for accept() */
  private Vector    pendingSocketList           = new Vector();

  /** InetSocketAddresses from existing connections */
  private Hashtable blockedSourceAddressesTable = new Hashtable();

  /** Lock for socket handling */
  private Object    socketLock                  = new Object();

  /** Creates a new instance of IPTunnelServerSocket */
  public IPTunnelServerSocket(IPTunnelSocketFactory ipTunnelSocketFactory, int localPort, int maximumSegmentSize)
  {
    super(ipTunnelSocketFactory);
    setName("IPTunnelServerSocket [" + localPort + "]");
    this.serverSocket = null;
    this.virtualPort = localPort;
    // maximum payload is maximum segment size minus TCP header
    this.maximumPayloadSize = maximumSegmentSize - 20;
    System.out.println("      Set MaxPayloadSize for tunnel server socket to " + maximumPayloadSize);

    state = LISTEN;

    start();
  }

  /** Waits for incoming connections */
  public IPTunnelSocket accept() throws SocketException, SocketTimeoutException
  {
    long time = System.currentTimeMillis();
    // wait for socket ready for accept()
    while (!terminateThread && pendingSocketList.size() == 0 &&
      (socketTimeout == 0 || System.currentTimeMillis() - time <= socketTimeout))
    {
      ThreadHelper.sleep(50);
    }
    // catch timeout
    if (socketTimeout > 0 && System.currentTimeMillis() - time > socketTimeout)
    {
      throw new SocketTimeoutException("Operation timed out");
    }

    // a socket is ready
    if (pendingSocketList.size() > 0)
    {
      IPTunnelSocket result = (IPTunnelSocket)pendingSocketList.remove(0);

      // send initial SYN response
      TCPPacket synResponsePacket =
        new TCPPacket(result.getLocalPort(),
          result.getDestinationPort(),
          result.getSendSequenceNumber(),
          result.getExpectedSequenceNumber(),
          result.getWindowSize(),
          maximumPayloadSize);

      // System.out.println("(2) SEND SYN RESPONSE...");

      // increment by one for sent SYN flag
      result.incrementSendSequenceNumber(1);

      // set state to receive
      result.setState(SYN_RCVD);

      // add to list of managed sockets
      socketTable.put(result.getDestinationSocketAddress(), result);

      // send packet immediately
      result.sendTCPPacket(synResponsePacket, true);

      // wait for established connection
      while (!terminateThread && !result.isEstablished() &&
        (socketTimeout == 0 || System.currentTimeMillis() - time <= socketTimeout))
      {
        ThreadHelper.sleep(50);
      }
      if (socketTimeout > 0 && System.currentTimeMillis() - time > socketTimeout)
      {
        // System.out.println("SYN response was not answered");
        // remove socket
        result.terminate(false);
        throw new SocketTimeoutException("Operation timed out");
      }
      return result;
    }
    // an error occured or the socket was closed
    throw new SocketException("Socket closed");
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /** Close the tunnel server socket */
  public void close()
  {
    terminate();
  }

  /** Terminates and removes a socket */
  public void terminate()
  {
    // System.out.println(" Terminate tunnel server socket");
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(50);
      } catch (Exception ex)
      {
      }
    }
    System.out.println("    Shutdown " + pendingSocketList.size() + " pending remote sockets");
    // terminate all pending sockets
    for (Enumeration e = CollectionHelper.getPersistentEntryEnumeration(pendingSocketList); e.hasMoreElements();)
    {
      ((IPTunnelSocket)e.nextElement()).terminate(false);
    }
    pendingSocketList.clear();

    System.out.println("    Shutdown " + socketTable.size() + " associated sockets");
    // terminate all sockets created by this server socket
    for (Enumeration e = CollectionHelper.getPersistentElementsEnumeration(socketTable); e.hasMoreElements();)
    {
      ((IPTunnelSocket)e.nextElement()).terminate(false);
    }
    System.out.println("    Server socket contains " + releaseSocketTable.size() + " sockets waiting for release");

    // remove from management
    ipTunnelSocketFactory.removeIPTunnelServerSocket(this);
  }

  /** Removes a closed datagram socket */
  protected void removeIPTunnelSocket(IPTunnelSocket socket, boolean timedWait)
  {
    synchronized(socketLock)
    {
      // remove from list of managed sockets
      socketTable.remove(socket.getDestinationSocketAddress());

      if (!timedWait)
      {
        // remove from list of blocked ports
        blockedSourceAddressesTable.remove(socket.getDestinationSocketAddress());
      } else
      {
        // add to list of released sockets
        releaseSocketTable.put(socket.getDestinationSocketAddress(), socket);
      }
    }
  }

  /** Adds a received tunnel TCP packet to the input queue of this server socket */
  protected void processReceivedPacket(TCPPacket tcpPacket)
  {
    synchronized(receiveLock)
    {
      // create source socket address
      InetSocketAddress packetSourceAddress =
        new InetSocketAddress(tcpPacket.getIPPacket().getSourceAddress(), tcpPacket.getSourcePort());

      boolean handled = false;

      // check for syn request packets
      if (state == LISTEN && tcpPacket.isConnectionRequest())
      {

        // System.out.println("Received packet. " +
        // " SEQ: " + tcpPacket.getSequenceNumber() +
        // " ACK: " + tcpPacket.getAcknowledgementNumber() +
        // " Source: " + packetSourceAddress.toString() +
        // " Flags:" + tcpPacket.flagsToString() +
        // (tcpPacket.getUpperLayerData() != null ? " Size: " + tcpPacket.getUpperLayerData().length
        // : "") +
        // ". ");

        // System.out.println("Connection request received");
        // check if this is an unknown source address
        if (!blockedSourceAddressesTable.containsKey(packetSourceAddress) && pendingSocketList.size() < 50)
        {

          int remoteMaximumPayloadSize = IPTunnelConstants.TCP_DEFAULT_PAYLOAD_SIZE;
          // check for maximum sequence size option
          if (tcpPacket.getMaximumPayloadSize() != 0)
          {
            remoteMaximumPayloadSize = tcpPacket.getMaximumPayloadSize();
          }
          // build minimum of local and remote payload size
          maximumPayloadSize = Math.min(maximumPayloadSize, remoteMaximumPayloadSize);

          // create socket endpoint
          IPTunnelSocket pendingSocket =
            new IPTunnelSocket(ipTunnelSocketFactory,
              this,
              virtualPort,
              packetSourceAddress,
              tcpPacket.getSequenceNumber(),
              maximumPayloadSize);

          pendingSocket.setAcceptOnlySinglePackets(ipTunnelSocketFactory.getIPTunnelNetworkInterface()
            .acceptOnlySinglePacketsPerSocket());

          // wait for end of handshake
          pendingSocket.setState(LISTEN);

          // increment by one for received SYN flag
          pendingSocket.incrementExpectedSequenceNumber(1);

          // if usage is very high, add new socket requests at the beginning
          // this allows a connection of sockets, even if the server accepts sockets only
          // at a small rate and the client connection timeout is very small
          if (pendingSocketList.size() > 40)
          {
            System.out.println("HIGH SERVER USAGE: " + pendingSocketList.size() + " pending sockets");
            // add to list of sockets ready for accept()
            pendingSocketList.add(0, pendingSocket);
          } else
          {
            // add to list of sockets ready for accept()
            pendingSocketList.add(pendingSocket);
          }

          // add to list of blocked sources
          blockedSourceAddressesTable.put(packetSourceAddress, pendingSocket);

          handled = true;
        } else
        {
          if (blockedSourceAddressesTable.containsKey(packetSourceAddress))
          {
            System.out.println("Ignore packet because of blocked source address");
          } else
          {
            System.out.println("Ignore packet because of too much stress");
          }
        }
      }
      // check for TCP packets directed to created sockets
      if (!tcpPacket.hasSYNFlag())
      {
        // System.out.println("IPTunnelServerSocket: Forward packet to socket");
        // try to find a socket for this source address
        Object socketObject = socketTable.get(packetSourceAddress);
        // socket found, forward packet to this socket for proper handling
        if (socketObject != null)
        {
          ((IPTunnelSocket)socketObject).processReceivedPacket(tcpPacket);
          handled = true;
        }
      }

      // send reset if packet was not handled and is not a RST packet itself
      if (!handled && !tcpPacket.hasRSTFlag())
      {
        System.out.println("IPTunnelServerSocket: Received invalid packet or too much stress, send reset. " + " SEQ: " +
          tcpPacket.getSequenceNumber() + " ACK: " + tcpPacket.getAcknowledgementNumber() + " Source: " +
          packetSourceAddress.toString() + " Flags:" + tcpPacket.flagsToString() +
          (tcpPacket.getUpperLayerData() != null ? " Size: " + tcpPacket.getUpperLayerData().length : "") + ". ");

        // set destination address and port for this socket to the originator
        // of the wrong packet
        destinationAddress = tcpPacket.getIPPacket().getSourceAddress();
        destinationPort = tcpPacket.getSourcePort();

        if (tcpPacket.hasACKFlag())
        {
          // packet has an acknowledgement number
          TCPPacket resetPacket =
            new TCPPacket(virtualPort, tcpPacket.getSourcePort(), tcpPacket.getAcknowledgementNumber());

          sendTCPPacket(resetPacket, true);
        } else
        {
          // packet has no acknowledgement number
          TCPPacket resetPacket = new TCPPacket(virtualPort, tcpPacket.getSourcePort(), 0);
          // acknowledge received packet
          resetPacket.setAcknowledgementNumber(tcpPacket.getSequenceNumber() + tcpPacket.getSequenceNumberSpace());

          sendTCPPacket(resetPacket, true);
        }
      }
    }
  }

  public void run()
  {
    while (!terminateThread)
    {
      Enumeration releaseSockets = releaseSocketTable.elements();
      // enumerate all sockets and release timed out sockets
      while (releaseSockets.hasMoreElements())
      {
        IPTunnelSocket socket = (IPTunnelSocket)releaseSockets.nextElement();
        // 4 minutes is the standard timeout
        if (System.currentTimeMillis() - socket.getReleaseTime() > 240000)
        {
          // System.out.println(" Remove deprecated socket");

          // remove from list of blocked ports
          blockedSourceAddressesTable.remove(socket.getDestinationSocketAddress());

          releaseSocketTable.remove(socket.getDestinationSocketAddress());
          releaseSockets = releaseSocketTable.elements();
        }
      }
      ThreadHelper.sleep(500);
    }
    terminated = true;
    System.out.println("    Server socket thread was shut down");
  }

}

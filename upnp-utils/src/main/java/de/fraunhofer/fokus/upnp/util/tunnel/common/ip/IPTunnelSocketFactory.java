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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class is responsible for routing packets and for managing IP tunnel sockets.
 * 
 * @author Alexander Koenig
 */
public class IPTunnelSocketFactory
{

  /** Class the is responsible for managing the IP tunnel */
  private IIPTunnelNetworkInterface ipTunnelNetworkInterface;

  /** Virtual ports in use by either a Socket or a DatagramSocket */
  private Hashtable                 blockedPortsTable          = new Hashtable();

  /** Virtual ports in use by MulticastSockets */
  private Hashtable                 blockedMulticastPortsTable = new Hashtable();

  /** IP tunnel multicast sockets */
  private Hashtable                 multicastSocketTable       = new Hashtable();

  /** IP tunnel datagram sockets */
  private Hashtable                 datagramSocketTable        = new Hashtable();

  /** IP tunnel TCP sockets */
  private Hashtable                 socketTable                = new Hashtable();

  /** IP tunnel TCP sockets that wait for release */
  private Hashtable                 releaseSocketTable         = new Hashtable();

  /** IP tunnel TCP server sockets */
  private Hashtable                 serverSocketTable          = new Hashtable();

  private Object                    lock                       = new Object();

  private Object                    cacheLock                  = new Object();

  /** Cache for received TCP packets */
  private Hashtable                 receivedTCPPacketTable     = new Hashtable();

  private Vector                    receivedTCPPacketList      = new Vector();

  /** First free port for next socket */
  private int                       firstFreePort              = 1025;

  /**
   * Creates a new instance of IPTunnelSocketFactory.
   * 
   * @param ipTunnelNetworkInterface
   */
  public IPTunnelSocketFactory(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    System.out.println("    Start tunnel socket factory");
    this.ipTunnelNetworkInterface = ipTunnelNetworkInterface;
  }

  /** Retrieves the network interface that is managed by this class */
  public IIPTunnelNetworkInterface getIPTunnelNetworkInterface()
  {
    return ipTunnelNetworkInterface;
  }

  /** Retrieves the number of cached TCP packet identifiers. */
  public int getTCPPacketCacheSize()
  {
    return receivedTCPPacketList.size();
  }

  /** Retrieves a free port usable for a socket */
  public int getFreePort()
  {
    // use new port for next connection
    firstFreePort++;
    if (firstFreePort > 65530)
    {
      firstFreePort = 1025;
    }
    int result = firstFreePort + 1;
    // search free port until overflow
    while (!isFreePort(result) && result != firstFreePort)
    {
      result++;
      if (result > 65530)
      {
        result = 1025;
      }
    }
    if (result != firstFreePort)
    {
      return result;
    }

    return -1;
  }

  /** Checks if a port is free for use */
  public boolean isFreePort(int port)
  {
    Integer portObj = new Integer(port);
    return !blockedPortsTable.containsKey(portObj) && !blockedMulticastPortsTable.containsKey(portObj);
  }

  /** Retrieves the initial sequence number for a connection */
  public long getInitialSequenceNumber()
  {
    long time = System.currentTimeMillis();
    // convert millisecs to a clock tick of 4 microseconds
    time = time * 250;

    // convert to 32 bit
    return time & 0xFFFFFFFFl;
  }

  /** Creates a multicast socket on a certain port */
  public IPTunnelMulticastSocket createIPTunnelMulticastSocket(int port) throws IOException
  {
    synchronized(lock)
    {
      if (port == 0)
      {
        port = getFreePort();
        if (port == -1)
        {
          throw new IOException("No free port available");
        }
      }
      IPTunnelMulticastSocket result = null;
      Integer portObj = new Integer(port);
      // check if port is not used by another socket
      if (!blockedPortsTable.containsKey(portObj))
      {
        try
        {
          result = new IPTunnelMulticastSocket(this, port);
          // store socket to hashtable
          multicastSocketTable.put(result, result);
          // store used port to hashtable
          Object portCount = blockedMulticastPortsTable.get(portObj);
          if (portCount != null)
          {
            portCount = new Integer(((Integer)portCount).intValue() + 1);
          } else
          {
            portCount = new Integer(1);
          }
          blockedMulticastPortsTable.put(portObj, portCount);

          return result;
        } catch (Exception ex)
        {
        }
      }
    }
    throw new IOException("Port already in use");
  }

  /** Creates a multicast socket on a random free port */
  public IPTunnelMulticastSocket createIPTunnelMulticastSocket() throws IOException
  {
    return createIPTunnelMulticastSocket(0);
  }

  /** Removes a closed multicast socket */
  public void removeIPTunnelMulticastSocket(IPTunnelMulticastSocket socket)
  {
    synchronized(lock)
    {
      // remove socket from hashtable
      multicastSocketTable.remove(socket);
      // port reference counting
      Integer portObj = new Integer(socket.getLocalPort());
      Integer portCount = (Integer)blockedMulticastPortsTable.get(portObj);
      if (portCount.intValue() == 1)
      {
        blockedMulticastPortsTable.remove(portObj);
      } else
      {
        blockedMulticastPortsTable.put(portObj, new Integer(portCount.intValue() - 1));
      }
    }
  }

  /** Creates a datagram socket on a certain port */
  public IPTunnelDatagramSocket createIPTunnelDatagramSocket(int port) throws IOException
  {
    synchronized(lock)
    {
      if (port == 0)
      {
        port = getFreePort();
        if (port == -1)
        {
          throw new IOException("No free port available");
        }
      }
      IPTunnelDatagramSocket result = null;
      // check if port is not used by another socket
      if (isFreePort(port))
      {
        try
        {
          result = new IPTunnelDatagramSocket(this, port);

          Integer portObj = new Integer(port);
          // store socket to hashtable
          datagramSocketTable.put(portObj, result);
          // store used port to hashtable
          blockedPortsTable.put(portObj, portObj);

          return result;
        } catch (Exception ex)
        {
        }
      }
    }
    throw new IOException("Port already in use");
  }

  /**
   * Creates a datagram socket on the next free port in a certain interval.
   * 
   * @param minPort
   *          The minimum port number
   * @param maxPort
   *          The maximum port number plus one
   * 
   * @return A socket that uses a port in [minPort, maxPort[
   * @throws IOException
   */
  public IPTunnelDatagramSocket createIPTunnelDatagramSocket(int minPort, int maxPort) throws IOException
  {
    synchronized(lock)
    {
      int freePort = minPort;
      // search free port
      while (!isFreePort(freePort) && freePort < maxPort)
      {
        freePort++;
      }
      // free port found
      if (freePort < maxPort)
      {
        return createIPTunnelDatagramSocket(freePort);
      }
    }
    throw new IOException("No free port available");
  }

  /** Creates a datagram socket on a random free port */
  public IPTunnelDatagramSocket createIPTunnelDatagramSocket() throws IOException
  {
    return createIPTunnelDatagramSocket(0);
  }

  /** Removes a closed datagram socket */
  public void removeIPTunnelDatagramSocket(IPTunnelDatagramSocket socket)
  {
    synchronized(lock)
    {
      Integer portObj = new Integer(socket.getLocalPort());
      // remove socket from hashtable
      datagramSocketTable.remove(portObj);
      // remove used port
      blockedPortsTable.remove(portObj);
    }
  }

  /** Creates a server socket on a certain port */
  public IPTunnelServerSocket createIPTunnelServerSocket(int port) throws IOException
  {
    synchronized(lock)
    {
      if (port == 0)
      {
        port = getFreePort();
        if (port == -1)
        {
          throw new IOException("No free port available");
        }
      }
      IPTunnelServerSocket result = null;
      // check if port is not used by another socket
      if (isFreePort(port))
      {
        try
        {
          result = new IPTunnelServerSocket(this, port, ipTunnelNetworkInterface.getMaximumSegmentSize());

          Integer portObj = new Integer(port);
          // store socket to hashtable
          serverSocketTable.put(portObj, result);
          // store used port to hashtable
          blockedPortsTable.put(portObj, portObj);

          return result;
        } catch (Exception ex)
        {
        }
      }
    }
    throw new IOException("Port already in use");
  }

  /** Creates a server socket on a random free port */
  public IPTunnelServerSocket createIPTunnelServerSocket() throws IOException
  {
    return createIPTunnelServerSocket(0);
  }

  /** Removes a closed server socket */
  public void removeIPTunnelServerSocket(IPTunnelServerSocket socket)
  {
    synchronized(lock)
    {
      Integer portObj = new Integer(socket.getLocalPort());
      // remove socket from hashtable
      serverSocketTable.remove(portObj);
      // remove used port
      blockedPortsTable.remove(portObj);
    }
  }

  /** Creates a socket on a certain port */
  public IPTunnelSocket createIPTunnelSocket(int port) throws IOException
  {
    synchronized(lock)
    {
      if (port == 0)
      {
        port = getFreePort();
        if (port == -1)
        {
          throw new IOException("No free port available");
        }
      }
      IPTunnelSocket result = null;
      // check if port is not used by another socket
      if (isFreePort(port))
      {
        try
        {
          result = new IPTunnelSocket(this, port, ipTunnelNetworkInterface.getMaximumSegmentSize());

          result.setAcceptOnlySinglePackets(ipTunnelNetworkInterface.acceptOnlySinglePacketsPerSocket());
          Integer portObj = new Integer(port);
          // store socket to hashtable
          socketTable.put(portObj, result);
          // store used port to hashtable
          blockedPortsTable.put(portObj, portObj);

          return result;
        } catch (Exception ex)
        {
        }
      }
    }
    throw new IOException("Port already in use");
  }

  /** Creates a socket on a random free port */
  public IPTunnelSocket createIPTunnelSocket() throws IOException
  {
    return createIPTunnelSocket(0);
  }

  /** Removes a closed datagram socket */
  public void removeIPTunnelSocket(IPTunnelSocket socket, boolean timedWait)
  {
    synchronized(lock)
    {
      Integer portObj = new Integer(socket.getLocalPort());
      // remove socket from hashtable
      socketTable.remove(portObj);

      // check if socket must be kept closed for some time
      if (timedWait)
      {
        // System.out.println("Move socket to release table");
        // add to list of sockets waiting for release
        releaseSocketTable.put(portObj, socket);
      } else
      {
        // System.out.println("Remove socket from table");
        // remove used port
        blockedPortsTable.remove(portObj);
      }
    }
  }

  /** This method is called for each received IP packet */
  public void processReceivedPacket(int packetType, IPPacket ipPacket)
  {
    // check for multicast packets
    if (packetType == IPTunnelConstants.PACKET_TYPE_UDP && isMulticastPacket(ipPacket))
    {
      synchronized(lock)
      {
        // enumerate all known sockets and forward packet to sockets that have joined
        // the packet multicast address and port
        Enumeration multicastSockets = multicastSocketTable.elements();
        while (multicastSockets.hasMoreElements())
        {
          IPTunnelMulticastSocket currentSocket = (IPTunnelMulticastSocket)multicastSockets.nextElement();
          if (currentSocket.isJoinedMulticastAddress(ipPacket.getDestinationAddress()))
          {
            UDPPacket udpPacket = new UDPPacket(ipPacket, ipPacket.getUpperLayerData());
            if (!udpPacket.hasChecksum() || udpPacket.isValidChecksum())
            {
              if (currentSocket.getLocalPort() == udpPacket.getDestinationPort())
              {
                currentSocket.processReceivedPacket(udpPacket);
              }
            } else
            {
              System.out.println("Received UDP multicast packet with invalid checksum" +
                StringHelper.byteArrayToHexDebugString(udpPacket.getUpperLayerData()));
            }
          }
        }
      }
    }
    // check for unicast datagram packets
    if (packetType == IPTunnelConstants.PACKET_TYPE_UDP && !isMulticastPacket(ipPacket))
    {
      synchronized(lock)
      {
        // only one potential receiver, so we directly create the packet
        UDPPacket udpPacket = new UDPPacket(ipPacket, ipPacket.getUpperLayerData());

        if (!udpPacket.hasChecksum() || udpPacket.isValidChecksum())
        {
          // try to find datagram socket for that port
          Integer portObj = new Integer(udpPacket.getDestinationPort());
          if (datagramSocketTable.containsKey(portObj))
          {
            IPTunnelDatagramSocket currentSocket = (IPTunnelDatagramSocket)datagramSocketTable.get(portObj);
            currentSocket.processReceivedPacket(udpPacket);
          } else
          {
            System.out.println("Received UDP unicast packet that is not directed to a registered socket (" +
              udpPacket.getIPPacket().getDestinationAddress().getHostAddress() + ":" + udpPacket.getDestinationPort() +
              ")");
          }
        } else
        {
          System.out.println("Received UDP unicast packet with invalid checksum");
        }
      }
    }
    // check for tcp packets
    if (packetType == IPTunnelConstants.PACKET_TYPE_TCP)
    {
      // System.out.println(" " + System.currentTimeMillis() + ": Received TCP unicast packet from
      // tunnel");
      synchronized(lock)
      {
        // only one potential receiver, so we directly create the packet
        TCPPacket tcpPacket = new TCPPacket(ipPacket, ipPacket.getUpperLayerData());

        boolean isKnownPacket = false;
        synchronized(cacheLock)
        {
          isKnownPacket = receivedTCPPacketTable.containsKey(tcpPacket.getHashIdentifier());

          if (!isKnownPacket)
          {
            TCPPacketIdentifier packetIdentifier = new TCPPacketIdentifier(tcpPacket);

            receivedTCPPacketTable.put(packetIdentifier.identifier, packetIdentifier);
            receivedTCPPacketList.add(packetIdentifier);
          }
        }
        if (!isKnownPacket)
        {
          if (tcpPacket.isValidChecksum())
          {
            // try to find socket or server socket for that port
            Integer portObj = new Integer(tcpPacket.getDestinationPort());
            boolean found = false;
            if (serverSocketTable.containsKey(portObj))
            {
              found = true;
              IPTunnelServerSocket currentSocket = (IPTunnelServerSocket)serverSocketTable.get(portObj);
              currentSocket.processReceivedPacket(tcpPacket);
            }
            if (socketTable.containsKey(portObj))
            {
              found = true;
              IPTunnelSocket currentSocket = (IPTunnelSocket)socketTable.get(portObj);
              currentSocket.processReceivedPacket(tcpPacket);
            }
            if (!found)
            {
              System.out.println("Received TCP packet that is not directed to a registered socket (" +
                tcpPacket.getIPPacket().getDestinationAddress().getHostAddress() + ":" +
                tcpPacket.getDestinationPort() + ")");
            }
          } else
          {
            if (!tcpPacket.isValidChecksum())
            {
              System.out.println("Received TCP packet with invalid checksum from " +
                ipPacket.getSourceAddress().getHostAddress() + ":" + tcpPacket.getSourcePort() + " : " + "Expected: " +
                tcpPacket.getCalculatedChecksum() + " Received: " + tcpPacket.getChecksum());

              System.out.println("    Upper layer data: " +
                StringHelper.byteArrayToHexDebugString(ipPacket.getUpperLayerData()));
            }
          }
        }
      }
    }
  }

  /** Checks if the destination address is a multicast address */
  public boolean isMulticastPacket(IPPacket ipPacket)
  {
    return (ipPacket.getDestinationAddress().getAddress()[0] & 0xF0) == 0xE0;
  }

  /** Remove received TCP packets after some time. */
  public void removeDeprecatedTCPPackets()
  {
    synchronized(cacheLock)
    {
      while (receivedTCPPacketList.size() > 0)
      {
        TCPPacketIdentifier identifier = (TCPPacketIdentifier)receivedTCPPacketList.elementAt(0);
        if (System.currentTimeMillis() - identifier.creationTime > 120000)
        {
          // System.out.println("Removed TCP packet from hashtable");
          receivedTCPPacketList.remove(0);
          receivedTCPPacketTable.remove(identifier.identifier);
        } else
        {
          // newer packets are always added at the end so if we found one young
          // packet, all following packets are even younger
          return;
        }
      }
    }
  }

  /** Terminates all sockets associated with this management */
  public void terminate()
  {
    System.out.println("  Terminate tunnel socket factory");
    // terminate all sockets
    Enumeration associatedSockets = CollectionHelper.getPersistentElementsEnumeration(socketTable);
    while (associatedSockets.hasMoreElements())
    {
      // use terminate instead of close because we have no chance to receive more packets, so
      // waiting is not sensible
      ((IPTunnelSocket)associatedSockets.nextElement()).terminate(false);
    }
    // terminate all server sockets
    associatedSockets = CollectionHelper.getPersistentElementsEnumeration(serverSocketTable);
    while (associatedSockets.hasMoreElements())
    {
      ((IPTunnelServerSocket)associatedSockets.nextElement()).terminate();
    }
    // terminate all datagram sockets
    associatedSockets = CollectionHelper.getPersistentElementsEnumeration(datagramSocketTable);
    while (associatedSockets.hasMoreElements())
    {
      ((IPTunnelDatagramSocket)associatedSockets.nextElement()).close();
    }
    // terminate all multicast sockets
    associatedSockets = CollectionHelper.getPersistentElementsEnumeration(multicastSocketTable);
    while (associatedSockets.hasMoreElements())
    {
      ((IPTunnelMulticastSocket)associatedSockets.nextElement()).close();
    }
    System.out.println("  Tunnel socket factory was terminated.");
  }

  /** Class to detect duplicate TCP packets. */
  private class TCPPacketIdentifier
  {
    public long   creationTime;

    public String identifier;

    public TCPPacketIdentifier(TCPPacket packet)
    {
      creationTime = packet.getCreationTime();
      identifier = packet.getHashIdentifier();
    }

  }

}

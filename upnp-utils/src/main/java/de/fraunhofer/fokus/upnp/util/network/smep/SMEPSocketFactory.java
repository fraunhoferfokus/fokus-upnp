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
package de.fraunhofer.fokus.upnp.util.network.smep;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelDatagramSocket;

/**
 * This class is responsible for routing packets and for managing SMEP sockets.
 * 
 * @author Alexander Koenig
 */
public class SMEPSocketFactory implements ISMEPPacketListener
{
  private static int        FIRST_PORT          = 1;

  private static int        LAST_PORT           = 254;

  /** Class which is responsible for actually sending and receiving packets. */
  private ISMEPPacketSender smepPacketSender;

  /** Network status interface */
  private INetworkStatus    networkStatus;

  /** Virtual ports in use by DatagramSockets */
  private Hashtable         blockedPortsTable   = new Hashtable();

  /** SMEP datagram sockets */
  private Hashtable         datagramSocketTable = new Hashtable();

  private Object            lock                = new Object();

  /** First free port for next socket */
  private int               firstFreePort       = 1025;

  /**
   * Creates a new instance of SMEPSocketFactory.
   * 
   * @param smepPacketSender
   */
  public SMEPSocketFactory(ISMEPPacketSender smepPacketSender)
  {
    System.out.println("    Start SMEP socket factory");
    this.smepPacketSender = smepPacketSender;
  }

  /**
   * Retrieves the value of smepPacketSender.
   * 
   * @return The value of smepPacketSender
   */
  public ISMEPPacketSender getSMEPPacketSender()
  {
    return smepPacketSender;
  }

  /**
   * Retrieves the value of networkStatus.
   * 
   * @return The value of networkStatus
   */
  public INetworkStatus getNetworkStatus()
  {
    return networkStatus;
  }

  /**
   * Sets the new value for networkStatus.
   * 
   * @param networkStatus
   *          The new value for networkStatus
   */
  public void setNetworkStatus(INetworkStatus networkStatus)
  {
    this.networkStatus = networkStatus;
  }

  /** Retrieves a free port usable for a socket */
  public int getFreePort()
  {
    // use new port for next connection
    firstFreePort++;
    if (firstFreePort > LAST_PORT)
    {
      firstFreePort = FIRST_PORT;
    }
    int result = firstFreePort + 1;
    // search free port until overflow
    while (!isFreePort(result) && result != firstFreePort)
    {
      result++;
      if (result > LAST_PORT)
      {
        result = FIRST_PORT;
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
    return !blockedPortsTable.containsKey(portObj);
  }

  /** Creates a datagram socket on a certain port */
  public SMEPDatagramSocket createSMEPDatagramSocket(int port) throws IOException
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
      SMEPDatagramSocket result = null;
      // check if port is not used by another socket
      if (isFreePort(port))
      {
        try
        {
          result = new SMEPDatagramSocket(this, port);

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
  public SMEPDatagramSocket createSMEPDatagramSocket(int minPort, int maxPort) throws IOException
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
        return createSMEPDatagramSocket(freePort);
      }
    }
    throw new IOException("No free port available");
  }

  /** Creates a datagram socket on a random free port */
  public SMEPDatagramSocket createSMEPDatagramSocket() throws IOException
  {
    return createSMEPDatagramSocket(0);
  }

  /** Removes a closed datagram socket */
  public void removeSMEPDatagramSocket(SMEPDatagramSocket socket)
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

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener#packetReceived(de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket)
   */
  public void smepPacketReceived(SMEPPacket packet)
  {
    synchronized(lock)
    {
      // try to find datagram socket for that port
      Integer portObj = new Integer(packet.getDestinationPort());
      if (datagramSocketTable.containsKey(portObj))
      {
        SMEPDatagramSocket currentSocket = (SMEPDatagramSocket)datagramSocketTable.get(portObj);
        currentSocket.processReceivedPacket(packet);
      } else
      {
        System.out.println("Received unicast packet that is not directed to a registered socket (" +
          IPHelper.toString(packet.getDestinationAddress()) + ":" + packet.getDestinationPort() + ")");
      }
    }
  }

  /** Checks if the destination address is a multicast address */
  public boolean isBroadcastPacket(SMEPPacket packet)
  {
    return ByteArrayHelper.isEqual(packet.getDestinationAddress(), new byte[] {
      (byte)0xFF
    }) || ByteArrayHelper.isEqual(packet.getDestinationAddress(), new byte[] {
        (byte)0xFF, (byte)0xFF
    });
  }

  /** Terminates all sockets associated with this management */
  public void terminate()
  {
    // terminate all datagram sockets
    Enumeration associatedSockets = CollectionHelper.getPersistentElementsEnumeration(datagramSocketTable);
    while (associatedSockets.hasMoreElements())
    {
      ((IPTunnelDatagramSocket)associatedSockets.nextElement()).close();
    }
    System.out.println("  Terminated SMEP socket factory");
  }

}

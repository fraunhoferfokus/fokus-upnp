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
package de.fraunhofer.fokus.upnp.util.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Vector;

/**
 * This class helps to read and write messages from and to datagram sockets.
 * 
 * @author Alexander Koenig
 * 
 */
public class SocketHelper
{

  public static final int BUFFER_SIZE            = 65535;

  private static Vector   bufferList             = new Vector();

  private static Object   lock                   = new Object();

  public static int       DEFAULT_SOCKET_TIMEOUT = 10;

  public static boolean   JAM_VM                 = false;

  /**
   * Reads a messages from a specific socket.
   * 
   * @param packetManager
   *          The packet manager that stores received packets or null
   * @param socket
   *          The socket that should receive the packet
   * @param timeout
   *          The timeout for waiting
   * 
   * @return The received message or null if the timeout was reached
   */
  public static BinaryMessageObject readBinaryMessage(UDPPacketManager packetManager, DatagramSocket socket, int timeout)
  {
    return readBinaryMessage(packetManager, new DatagramSocketWrapper(socket), timeout);
  }

  /**
   * Reads a messages from a specific socket.
   * 
   * @param packetManager
   *          The packet manager that stores received packets or null
   * @param socket
   *          The socket that should receive the packet
   * @param timeout
   *          The timeout for waiting
   * 
   * @return The received message or null if the timeout was reached
   */
  public static BinaryMessageObject readBinaryMessage(UDPPacketManager packetManager,
    IDatagramSocket socket,
    int timeout)
  {
    SocketBufferEntry bufferEntry = null;
    try
    {
      // set timeout
      socket.setSoTimeout(timeout);

      // search a free buffer
      synchronized(lock)
      {
        int i = 0;
        while (i < bufferList.size())
        {
          SocketBufferEntry currentEntry = (SocketBufferEntry)bufferList.elementAt(i);
          if (!currentEntry.isUsed())
          {
            bufferEntry = currentEntry;
            break;
          } else
          {
            i++;
          }
        }
        if (bufferEntry == null)
        {
          // System.out.println("Create new socket buffer. New size is " + bufferList.size());
          bufferEntry = new SocketBufferEntry();
          bufferList.add(bufferEntry);
        }
        bufferEntry.setUsed(true);
      }
      // try to receive a packet
      DatagramPacket packet = new DatagramPacket(bufferEntry.getBuffer(), bufferEntry.getBuffer().length);
      socket.receive(packet);

      // a packet was received
      byte[] data = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);

      // free buffer
      synchronized(lock)
      {
        bufferEntry.setUsed(false);
      }

      // check if packet was not received before
      if (packetManager == null || !packetManager.isKnownPacket(data, packet.getAddress(), packet.getPort()))
      {
        if (packetManager != null)
        {
          packetManager.addPacket(data, packet.getAddress(), packet.getPort());
        }

        BinaryMessageObject message =
          new BinaryMessageObject(data, new InetSocketAddress(packet.getAddress(), packet.getPort()));

        return message;
      }
    } catch (Exception ex)
    {
    }
    // free buffer
    synchronized(lock)
    {
      if (bufferEntry != null)
      {
        bufferEntry.setUsed(false);
      }
    }

    return null;
  }

  /**
   * Sends a UDP message.
   * 
   * @param message
   *          The message
   * @param socket
   *          Socket that should send the message
   * 
   */
  public static void sendBinaryMessage(BinaryMessageObject message, DatagramSocket socket)
  {
    DatagramPacket packet =
      new DatagramPacket(message.getBody(),
        message.getBody().length,
        message.getDestinationAddress().getAddress(),
        message.getDestinationAddress().getPort());

    try
    {
      socket.send(packet);
    } catch (Exception ex)
    {
      System.out.println("Error while sending packet:" + ex.getMessage());
    }
  }

  /**
   * Sends a UDP message.
   * 
   * @param message
   *          The message
   * @param socket
   *          Socket that should send the message
   * 
   */
  public static void sendBinaryMessage(BinaryMessageObject message, IDatagramSocket socket)
  {
    DatagramPacket packet =
      new DatagramPacket(message.getBody(),
        message.getBody().length,
        message.getDestinationAddress().getAddress(),
        message.getDestinationAddress().getPort());

    try
    {
      socket.send(packet);
    } catch (Exception ex)
    {
      System.out.println("Error while sending packet:" + ex.getMessage());
    }
  }

  /**
   * Opens a socket on a specific network interface, either on a fixed or a random port.
   * 
   * @param address
   * @param port
   * @param fixedPort
   * 
   * @return The new socket or null
   */
  public static DatagramSocket createSocket(InetAddress address, int port, boolean fixedPort)
  {
    DatagramSocket result = null;
    if (fixedPort)
    {
      try
      {
        result = new DatagramSocket(new InetSocketAddress(address, port));
        result.setSoTimeout(50);
      } catch (Exception e)
      {
        return null;
      }
    } else
    {
      Random rdm = new Random();
      port = port + rdm.nextInt(1024);
      while (result == null && port < 65530)
      {
        try
        {
          result = new DatagramSocket(new InetSocketAddress(address, port));
          result.setSoTimeout(50);
        } catch (Exception e)
        {
          result = null;
          port++;
        }
      }
    }
    return result;
  }

}

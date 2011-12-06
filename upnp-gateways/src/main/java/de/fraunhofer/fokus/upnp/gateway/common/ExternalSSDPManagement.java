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
package de.fraunhofer.fokus.upnp.gateway.common;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.network.UDPPacketManager;

/**
 * This class is used to receive external SSDP messages and to trigger appropriate action on
 * reception.
 * 
 * @author Alexander Koenig
 */
public class ExternalSSDPManagement extends Thread
{

  /** Reference to packet manager */
  private UDPPacketManager udpPacketManager;

  /** List of sockets that should be monitored */
  private Vector           socketList         = new Vector();

  /** List of associated packet handlers */
  private Vector           messageHandlerList = new Vector();

  private Object           listLock           = new Object();

  private boolean          terminateThread    = false;

  private boolean          terminated         = false;

  /**
   * Creates a new instance of ExternalSSDPManagement.
   * 
   * @param udpPacketManager
   */
  public ExternalSSDPManagement(UDPPacketManager udpPacketManager)
  {
    super("MessageForwarder.ExternalSSDPManagement");
    this.udpPacketManager = udpPacketManager;

    start();
  }

  /**
   * This methods adds a socket to the list of sockets that should be checked for messages
   * 
   * @param socket
   *          The socket that should be checked
   * @param messageHandler
   *          The message handler that should be called for the received message
   * 
   */
  public void addSocket(IDatagramSocket socket, ISSDPMessageHandler messageHandler)
  {
    synchronized(listLock)
    {
      try
      {
        socketList.add(socket);
        messageHandlerList.add(messageHandler);
      } catch (Exception e)
      {
      }
    }
  }

  /**
   * This methods removes a socket from the list of sockets that should be checked for messages
   * 
   * @param socket
   *          The socket that should be removed
   * 
   */
  public void removeSocket(IDatagramSocket socket)
  {
    synchronized(listLock)
    {
      int index = socketList.indexOf(socket);
      if (index != -1)
      {
        socketList.remove(index);
        messageHandlerList.remove(index);
      }
    }
  }

  /** Reads SSDP messages from the multicast sockets of all registered modules */
  public void run()
  {
    // System.out.println(" Start ExternalSSDPManagement thread, wait for NOTIFY and M-SEARCH
    // packets...");

    while (!terminateThread)
    {
      synchronized(listLock)
      {
        for (int i = 0; i < socketList.size(); i++)
        {
          IDatagramSocket socket = (IDatagramSocket)socketList.elementAt(i);

          boolean readUDP = true;
          while (readUDP)
          {
            BinaryMessageObject binaryMessage = SocketHelper.readBinaryMessage(udpPacketManager, socket, 20);
            if (binaryMessage != null)
            {
              // System.out.println("Received message from " +
              // IPAddress.toString(binaryMessage.getSourceAddress()));
              HTTPMessageObject httpMessage =
                new HTTPMessageObject(binaryMessage.getBodyAsString(), binaryMessage.getSourceAddress());

              // allow message processing by registered handler
              ((ISSDPMessageHandler)messageHandlerList.elementAt(i)).processMessage(httpMessage);
            } else
            {
              readUDP = false;
            }
          }
        }
      }
      if (udpPacketManager != null)
      {
        udpPacketManager.triggerEvents();
      }

      ThreadHelper.sleep(50);
    }
    System.out.println("  ExternalSSDPManagement thread was shut down");
    // close sockets
    terminated = true;
  }

  /** Terminates the manager */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(20);
    }
  }

}

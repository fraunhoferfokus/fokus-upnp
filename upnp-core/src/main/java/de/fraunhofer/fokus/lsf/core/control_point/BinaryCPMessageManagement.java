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
package de.fraunhofer.fokus.lsf.core.control_point;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is used to receive binary UPnP messages.
 * 
 * @author Alexander Koenig
 */
public class BinaryCPMessageManagement extends Thread
{

  /** Reference to control point */
  private BinaryControlPoint                binaryControlPoint;

  /** Manager for socket structures */
  private BinaryCPSocketStructureManagement socketStructureManagement = null;

  /** Used multicast address for discovery and events */
  private String                            multicastAddress;

  /** Port used for discovery */
  private int                               discoveryMulticastPort;

  /** Port used for events */
  private int                               eventMulticastPort;

  private boolean                           terminateThread           = false;

  private boolean                           terminated                = false;

  /**
   * Creates a new instance of BinaryCPMessageManagement.
   * 
   * @param binaryControlPoint
   * @param preferredHostAddress
   */
  public BinaryCPMessageManagement(BinaryControlPoint binaryControlPoint, LSFStartupConfiguration startupConfiguration)
  {
    super("BinaryControlPoint.MessageManagement");
    this.binaryControlPoint = binaryControlPoint;

    Vector preferredHostAddressList = startupConfiguration.getPreferredIPAddressesList();
    Vector ignoredHostAddressList = startupConfiguration.getIgnoredIPAddressesList();
    multicastAddress = startupConfiguration.getMulticastAddress();
    discoveryMulticastPort = startupConfiguration.getDiscoveryMulticastPort();
    eventMulticastPort = startupConfiguration.getEventMulticastPort();

    socketStructureManagement =
      new BinaryCPSocketStructureManagement(preferredHostAddressList,
        ignoredHostAddressList,
        multicastAddress,
        discoveryMulticastPort,
        eventMulticastPort);
    socketStructureManagement.initHostAddressSocketStructures();

    start();
  }

  /**
   * Retrieves the value of socketStructureManagement.
   * 
   * @return The value of socketStructureManagement
   */
  public BinaryCPSocketStructureManagement getSocketStructureManagement()
  {
    return socketStructureManagement;
  }

  /** Searches for all binary UPnP devices. */
  public void sendSearchAllMessage(byte[] message)
  {
    try
    {
      DatagramPacket packet =
        new DatagramPacket(message, message.length, InetAddress.getByName(multicastAddress), discoveryMulticastPort);

      // send over all sockets 
      for (int i = 0; i < socketStructureManagement.getSocketStructureCount(); i++)
      {
        ((BinaryCPHostAddressSocketStructure)socketStructureManagement.getSocketStructure(i)).getDiscoveryUnicastSocket()
          .send(packet);
      }
    } catch (Exception e)
    {
    }
  }

  /** Sends a ping to a device. */
  public void sendPingMessage(InetAddress deviceAddress)
  {
    try
    {
      DatagramPacket packet = new DatagramPacket(new byte[] {
          BinaryUPnPConstants.UnitTypePing, 0, BinaryUPnPConstants.UnitTypeEndOfPacket
      }, 3, deviceAddress, discoveryMulticastPort);

      for (int i = 0; i < socketStructureManagement.getSocketStructureCount(); i++)
      {
        ((BinaryCPHostAddressSocketStructure)socketStructureManagement.getSocketStructure(i)).getDiscoveryUnicastSocket()
          .send(packet);
      }
    } catch (Exception e)
    {
    }
  }

  /** Reads discovery messages */
  public void run()
  {
    Portable.println("Start message management thread");
    while (!terminateThread)
    {
      for (int i = 0; i < socketStructureManagement.getSocketStructureCount(); i++)
      {
        BinaryCPHostAddressSocketStructure currentSocketStructure =
          (BinaryCPHostAddressSocketStructure)socketStructureManagement.getSocketStructure(i);

        BinaryMessageObject message = null;
        // read announcement and removal messages
        do
        {
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getDiscoverySocket(), 10);
          if (message != null)
          {
            Portable.println("Discovery: Received multicast discovery message");
            Portable.println(BinaryUPnPConstants.toForwarderDebugString("",
              message.getBody(),
              message.getSourceAddress().getAddress()));
            binaryControlPoint.processDiscoveryMessage(message, currentSocketStructure);
          }
        } while (message != null);
        // read search reply messages
        do
        {
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getDiscoveryUnicastSocket(), 10);
          if (message != null)
          {
            StringHelper.printDebugText("", true, "Discovery: Received search reply from " +
              IPHelper.toString(message.getSourceAddress()) + ":", BinaryUPnPConstants.toForwarderDebugString("",
              message.getBody(),
              message.getSourceAddress().getAddress()));
            binaryControlPoint.processDiscoveryMessage(message, currentSocketStructure);
          }
        } while (message != null);
        // read event messages
        do
        {
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getEventSocket(), 10);
          if (message != null)
          {
            Portable.println("Eventing: Received event message ");
            binaryControlPoint.processEventMessage(message);
          }
        } while (message != null);
        do
        {
          // read debug messages
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getDebugSocket(), 10);
          if (message != null)
          {
            //            Portable.println("\r\n" +
            //              "////////////////////////////////////////////////////////////////////////////////////" +
            //              "Debug message from " + IPAddress.toString(message.getSourceAddress()) + " (" + message.getBody().length +
            //              " bytes)");
            //            Portable.println(StringHelper.byteArrayToHexDebugString(message.getBody()));
            //            Portable.println(BinaryUPnPConstants.toDebugString(message.getBody()) + "\r\n");
          }
        } while (message != null);
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the manager */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(10);
    }
    socketStructureManagement.terminate();
  }

}

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

import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.AbstractPacketTunnel;
import de.fraunhofer.fokus.upnp.util.tunnel.common.IPacketTunnelListener;

/**
 * This class is used to create a virtual IP tunnel. Applications can create TCP and UDP sockets which send and receive
 * data through the tunnel. The tunnel has a virtual IPv4 address which is used both as local and remote socket address
 * by all sockets. Data send to a specific port is received by the virtual socket (listening on that port) at the other
 * end of the tunnel.
 * 
 * This class is abstract and must be extended by classes that provide a real tunnel, like a serial or TCP connection.
 * 
 * @author Alexander Koenig
 */
public abstract class AbstractIPTunnelNetworkInterface implements
  IEventListener,
  IPacketTunnelListener,
  IIPTunnelNetworkInterface
{

  /** Optional listener for tunnel events */
  protected IIPTunnelEventListener ipTunnelEventListener;

  /** Class that is used to create sockets for the tunnel. */
  protected IPTunnelSocketFactory  ipTunnelSocketFactory            = null;

  /** Class that is used to actual send data to the tunnel and receive data from the tunnel */
  protected AbstractPacketTunnel   packetTunnel;

  /** IP address used inside the IP tunnel */
  private InetAddress              ipTunnelInetAddress              = null;

  /** Variable used to identify singular IP packets */
  private int                      identification                   = 1;

  /** Maximum segment size used by this virtual network interface */
  private int                      maximumSegmentSize               = IPTunnelConstants.TCP_DEFAULT_SEGMENT_SIZE;

  /** Flag to support only one unacknowledged packet per socket */
  private boolean                  acceptOnlySinglePacketsPerSocket = true;

  /** Time to wait between single packets */
  protected long                   packetGapTime                    = IPTunnelConstants.TUNNEL_PACKET_DELAY;

  protected long                   lastPingPacketTime               = System.currentTimeMillis();

  protected long                   lastReceivedPacketTime           = System.currentTimeMillis();

  /** Time of last connection */
  protected long                   connectionTime                   = 0;

  /** Time of last disconnection */
  protected long                   disconnectionTime                = 0;

  /**
   * Creates a new instance of IPTunnelNetworkInterface.
   * 
   */
  public AbstractIPTunnelNetworkInterface()
  {
  }

  /** Retrieves a string containing a timestamp */
  public static String timeStamp()
  {
    long time = System.currentTimeMillis();
    if (time % 1000 < 10)
    {
      return time / 1000 % 100 + ".00" + time % 1000 + ": ";
    } else if (time % 1000 < 100)
    {
      return time / 1000 % 100 + ".0" + time % 1000 + ": ";
    } else
    {
      return time / 1000 % 100 + "." + time % 1000 + ": ";
    }
  }

  /** Converts the packet type to a readable string */
  public static String packetTypeToString(int packetType, int subType)
  {
    if (packetType == IPTunnelConstants.PACKET_TYPE_CONFIG)
    {
      if (subType == IPTunnelConstants.CONFIG_TYPE_CLIENT_CONFIG)
      {
        return "Config - ClientConfig";
      }
      if (subType == IPTunnelConstants.CONFIG_TYPE_CONFIG_TO_SERVER)
      {
        return "Config - ConfigToServer (IP, MAC)";
      }
      if (subType == IPTunnelConstants.CONFIG_TYPE_CONFIG_RECEIVED_BY_SERVER)
      {
        return "Config - ConfigReceivedByServer";
      }
      if (subType == IPTunnelConstants.CONFIG_TYPE_PING)
      {
        return "Config - Ping";
      }
    }
    if (packetType == IPTunnelConstants.PACKET_TYPE_TCP)
    {
      return "TCP";
    }
    if (packetType == IPTunnelConstants.PACKET_TYPE_UDP)
    {
      return "UDP";
    }

    return "Unknown";
  }

  /** Prints a debug message */
  protected static void printMessage(String text)
  {
    System.out.println(timeStamp() + ": " + text);
  }

  /**
   * This method encapsulates a packet and sends it to the IP tunnel.
   * 
   * @param data
   *          The byte array that should be sent
   */
  protected void sendPacket(byte packetType, byte[] payload)
  {
    if (packetTunnel != null && packetTunnel.isConnected())
    {
      packetTunnel.sendPacket(packetType, payload);
      if (packetGapTime > 0)
      {
        ThreadHelper.sleep((int)packetGapTime);
      }
    }
  }

  /** This method starts the socket factory as soon as the tunnel inet address is known. */
  public void setIPTunnelInetAddress(InetAddress ipTunnelInetAddress)
  {
    this.ipTunnelInetAddress = ipTunnelInetAddress;
    // create socket factory
    ipTunnelSocketFactory = new IPTunnelSocketFactory(this);
  }

  /** This method is called after each successful reconnection */
  protected void reconnectNetworkInterface()
  {
  }

  /**
   * Checks if this IP tunnel has a valid tunnel connection.
   * 
   * @return True if the socket for this tunnel is connected, false otherwise
   */
  public boolean isConnected()
  {
    if (packetTunnel == null)
    {
      return false;
    }

    return packetTunnel.isConnected();
  }

  /** Retrieves the inet address used by the IP tunnel */
  public InetAddress getIPTunnelInetAddress()
  {
    return ipTunnelInetAddress;
  }

  /** Retrieves the factory for socket creation. */
  public IPTunnelSocketFactory getIPTunnelSocketFactory()
  {
    return ipTunnelSocketFactory;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface#getConnectionTime()
   */
  public long getConnectionTime()
  {
    return connectionTime;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface#getDisconnectionTime()
   */
  public long getDisconnectionTime()
  {
    return disconnectionTime;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface#sendIPPacketToTunnel(byte, de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPPacket)
   */
  public void sendIPPacketToTunnel(byte packetType, IPPacket ipPacket)
  {
    // update missing values in ipPacket
    identification = (identification + 1) % 65536;
    ipPacket.setIdentification(identification);
    ipPacket.calculateChecksum();

    byte[] payload = ipPacket.toByteArray();

    sendPacket(packetType, payload);
  }

  /** This method sends a config message with a ping to test the TCP connection */
  public void sendPingToTunnel()
  {
    // printMessage("Send ping");
    // update time for last sent packet
    lastPingPacketTime = System.currentTimeMillis();

    byte[] payload = new byte[] {
      IPTunnelConstants.CONFIG_TYPE_PING
    };

    sendPacket(IPTunnelConstants.PACKET_TYPE_CONFIG, payload);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface#sendIDToTunnel(byte[])
   */
  public void sendIDToTunnel(byte[] id)
  {
    byte[] payload = new byte[1 + id.length];
    payload[0] = IPTunnelConstants.CONFIG_TYPE_CLIENT_ID;
    System.arraycopy(id, 0, payload, 1, id.length);

    sendPacket(IPTunnelConstants.PACKET_TYPE_CONFIG, payload);
  }

  /** This method is called for each received packet, whether IP or config */
  public void processReceivedPacket(int packetType, byte[] packetData)
  {
    //    System.out.println("Received packet from tunnel: " + packetTypeToString(packetType, packetData[0]));

    lastReceivedPacketTime = System.currentTimeMillis();
    // process config packets
    if (packetType == IPTunnelConstants.PACKET_TYPE_CONFIG)
    {
      processReceivedConfigPacket(packetData);
    }
    // only process data packets if the packet management is already initialized
    if (packetType != IPTunnelConstants.PACKET_TYPE_CONFIG && ipTunnelSocketFactory != null)
    {
      // create IP packet instance from received byte array
      IPPacket ipPacket = new IPPacket(packetData);
      if (ipPacket.isValidChecksum())
      {
        processReceivedIPPacket(packetType, ipPacket);
      } else
      {
        System.out.println("Received IP packet with invalid checksum: " +
          StringHelper.byteArrayToHexDebugString(packetData));
      }
    }
  }

  /** This method is called for each received IP packet */
  private void processReceivedIPPacket(int packetType, IPPacket ipPacket)
  {
    // forward to socket factory for distribution
    ipTunnelSocketFactory.processReceivedPacket(packetType, ipPacket);
  }

  /** This method processes the config packet with the local IP of the client */
  protected void processReceivedConfigPacket(byte[] packetData)
  {
    // ping received
    if (packetData[0] == IPTunnelConstants.CONFIG_TYPE_PING)
    {
      // printMessage("Ping received");
    }
    // ID received
    if (packetData[0] == IPTunnelConstants.CONFIG_TYPE_CLIENT_ID)
    {
      byte[] id = new byte[packetData.length - 1];
      System.arraycopy(packetData, 1, id, 0, id.length);

      if (ipTunnelEventListener != null)
      {
        ipTunnelEventListener.initialConfigReceived(this, id);
      }
    }
  }

  /** Closes the physical tunnel connection but does not terminate the virtual network interface. */
  protected void closeOuterConnection()
  {
    if (packetTunnel != null)
    {
      packetTunnel.closeConnection();
    }

    // store disconnection time for reconnect hash table
    disconnectionTime = System.currentTimeMillis();

    // signal to event listener
    if (ipTunnelEventListener != null)
    {
      ipTunnelEventListener.outerConnectionClosed(this);
    }
  }

  /**
   * Sets the ipTunnelEventListener
   * 
   * @param ipTunnelEventListener
   *          The ipTunnelEventListener to set.
   */
  public void setIPTunnelEventListener(IIPTunnelEventListener ipTunnelEventListener)
  {
    this.ipTunnelEventListener = ipTunnelEventListener;
  }

  /** Retrieves the maximum segment size for this network interface. */
  public int getMaximumSegmentSize()
  {
    return maximumSegmentSize;
  }

  /** Sets the maximum segment size for this network interface. */
  public void setMaximumSegmentSize(int maximumSegmentSize)
  {
    this.maximumSegmentSize = maximumSegmentSize;
  }

  public boolean acceptOnlySinglePacketsPerSocket()
  {
    return acceptOnlySinglePacketsPerSocket;
  }

  public void setAcceptOnlySinglePacketsPerSocket(boolean acceptOnlySinglePacketsPerSocket)
  {
    this.acceptOnlySinglePacketsPerSocket = acceptOnlySinglePacketsPerSocket;
  }

  /**
   * Sets the packetGapTime.
   * 
   * @param packetGapTime
   *          The new value for packetGapTime
   */
  public void setPacketGapTime(long packetGapTime)
  {
    this.packetGapTime = packetGapTime;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.tunnel.IPacketTunnelListener#packetReceived(byte, byte[])
   */
  public void packetReceived(byte packetType, byte[] payload)
  {
    processReceivedPacket(packetType, payload);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    try
    {
      // trigger packet tunnel to read incoming packets
      if (packetTunnel != null && packetTunnel.isConnected())
      {
        packetTunnel.triggerEvents();
      }
      if (ipTunnelSocketFactory != null)
      {
        // remove outdated received TCP packets
        ipTunnelSocketFactory.removeDeprecatedTCPPackets();
      }
      if (packetTunnel != null && packetTunnel.isConnected())
      {
        // send ping
        if (System.currentTimeMillis() - lastPingPacketTime > IPTunnelConstants.TCP_PING_TIME)
        {
          sendPingToTunnel();
        }
        // check for last received packet
        if (System.currentTimeMillis() - lastReceivedPacketTime > IPTunnelConstants.TCP_PING_TIME * 2)
        {
          System.out.println("Close socket due to missing ping packets from the remote socket...");
          closeOuterConnection();
        }
      }
    } catch (Exception ex)
    {
      System.out.println("ERROR in NetworkInterface: " + ex.getMessage() + ". Close socket...");
      closeOuterConnection();
    }
  }

  /**
   * Terminates the complete connection to the client and waits for the termination of the thread.
   */
  public void terminate()
  {
    closeOuterConnection();
    packetTunnel = null;

    // terminate all virtual sockets
    if (ipTunnelSocketFactory != null)
    {
      ipTunnelSocketFactory.terminate();
    }
    ipTunnelSocketFactory = null;
    ipTunnelInetAddress = null;
  }

}

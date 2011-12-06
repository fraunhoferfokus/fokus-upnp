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
package de.fraunhofer.fokus.upnp.gateway.internet.device_directory_device;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;

/**
 * This class is the remote representation of a DeviceDirectoryDevice that is not already
 * discovered. It consists mainly of the IPAddress:Port used for DeviceDirectoryDevice discovery.
 * 
 * @author Alexander Koenig
 */
public class DeviceDirectoryPeer
{

  // address open to the internet
  private InetAddress       serverAddress;

  private int               serverPort;

  private InetSocketAddress serverSocketAddress;

  private String            connectionType;

  private long              lastMSearchRequest;

  private long              mSearchRequestCount;

  // unique connectionID
  private int               connectionID;

  private String            connectionStatus;

  /** Creates a new instance of DeviceDirectoryPeer */
  public DeviceDirectoryPeer(InetAddress address, int port)
  {
    this(address, port, InternetManagementConstants.CONNECTION_TYPE_TRANSPARENT);
  }

  /** Creates a new instance of DeviceDirectoryPeer */
  public DeviceDirectoryPeer(InetAddress address, int port, String connectionType)
  {
    serverAddress = address;
    serverPort = port;
    serverSocketAddress = new InetSocketAddress(address, port);
    this.connectionType = connectionType;
    mSearchRequestCount = 0;
    lastMSearchRequest = 0;
    connectionID = -1;
    connectionStatus = InternetManagementConstants.CONNECTION_STATUS_DISCONNECTED;
  }

  /** Retrieves the IP address used for DeviceDirectoryDevice discovery */
  public InetAddress getAddress()
  {
    return serverAddress;
  }

  /** Retrieves the socket address used for DeviceDirectoryDevice discovery */
  public InetSocketAddress getSocketAddress()
  {
    return serverSocketAddress;
  }

  /** Retrieves the port used for DeviceDirectoryDevice discovery */
  public int getPort()
  {
    return serverPort;
  }

  /** Retrieves the time stamp of the last M-SEARCH request for this peer */
  public long getLastMSearchRequestTime()
  {
    return lastMSearchRequest;
  }

  /** Retrieves the number of M-SEARCH requests for this peer */
  public long getMSearchRequestCount()
  {
    return mSearchRequestCount;
  }

  public void setLastMSearchRequestTime(long time)
  {
    lastMSearchRequest = time;
    mSearchRequestCount++;
  }

  /** Retrieves the associated DeviceDirectoryDevice */
  /*
   * public DeviceDirectoryDevice getDeviceDirectoryDevice() { return deviceDirectoryDevice; }
   */

  /** Retrieves the connection type to this peer */
  public String getConnectionType()
  {
    return connectionType;
  }

  public boolean equals(DeviceDirectoryPeer peer)
  {
    return serverAddress.equals(peer.getAddress()) && serverPort == peer.getPort();
  }

  /** Checks if the peer is connected transparently */
  public boolean isTransparent()
  {
    return connectionType.equals(InternetManagementConstants.CONNECTION_TYPE_TRANSPARENT);
  }

  /** Checks if the peer is currently searched */
  public boolean isSearched()
  {
    return connectionStatus.equals(InternetManagementConstants.CONNECTION_STATUS_SEARCHED);
  }

  /** Resets this peer after device removal */
  public void resetPeer()
  {
    lastMSearchRequest = 0;
    mSearchRequestCount = 0;
    connectionStatus = InternetManagementConstants.CONNECTION_STATUS_SEARCHED;
  }

  /** Retrieves the ID associated with this peer */
  public int getConnectionID()
  {
    return connectionID;
  }

  /** Sets the ID associated with this peer */
  public void setConnectionID(int id)
  {
    connectionID = id;
  }

  /** Retrieves the status of the connection to this peer */
  public String getConnectionStatus()
  {
    return connectionStatus;
  }

  /** Sets the status of the connection to this peer */
  public void setConnectionStatus(String status)
  {
    connectionStatus = status;
  }

  /** Parses an address in the form host:port */
  public static DeviceDirectoryPeer parsePeerAddress(String peer)
  {
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(peer, ":");
      InetAddress address = InetAddress.getByName(tokenizer.nextToken());
      int port = Integer.parseInt(tokenizer.nextToken());
      return new DeviceDirectoryPeer(address, port);
    } catch (Exception ex)
    {
      System.out.println("    Exception while parsing peer: " + ex.getMessage());
    }
    return null;
  }

}

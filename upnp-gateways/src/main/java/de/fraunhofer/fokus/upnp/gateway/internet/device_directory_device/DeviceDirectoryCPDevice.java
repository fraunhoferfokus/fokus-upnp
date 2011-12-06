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

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateCPDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;

/**
 * This device is the remote view for a peer in the UPnP network. It holds data as the IP address,
 * the used port and the connection type (transparent, semitransparent or manual)
 * 
 * @author Alexander Koenig
 */
public class DeviceDirectoryCPDevice extends TemplateCPDevice
{

  // address where this peer was discovered
  private DeviceDirectoryPeer peer                         = null;

  private int                 deviceDiscoveryPort          = -1;

  private InetSocketAddress   deviceDiscoverySocketAddress = null;

  private long                discoveryTime;

  private boolean             initialDeviceSearch          = true;

  /** Creates a new instance of DeviceDirectoryCPDevice */
  public DeviceDirectoryCPDevice(TemplateControlPoint controlPoint, CPDevice device)
  {
    super(controlPoint, device);
  }

  public void setPeer(DeviceDirectoryPeer peer)
  {
    this.peer = peer;
    // save connection time
    discoveryTime = System.currentTimeMillis();
  }

  public DeviceDirectoryPeer getPeer()
  {
    return peer;
  }

  /** Retrieves the discovery time for this peer */
  public long getDiscoveryTime()
  {
    return discoveryTime;
  }

  /** Checks if this peer was already searched for devices */
  public boolean isInitialDeviceSearch()
  {
    return initialDeviceSearch;
  }

  public void setInitialDeviceSearch(boolean state)
  {
    initialDeviceSearch = state;
  }

  /** Retrieves the IP address that is used for deviceDirectoryDevice discovery */
  public InetAddress getPeerAddress()
  {
    if (peer != null)
    {
      return peer.getAddress();
    }

    return null;
  }

  /** Retrieves the port that is used for deviceDirectoryDevice discovery */
  public int getDeviceDirectoryDiscoveryPort()
  {
    if (peer != null)
    {
      return peer.getPort();
    }

    return -1;
  }

  /** Retrieves the port that is used for common device discovery */
  public int getDiscoveryPort()
  {
    if (deviceDiscoveryPort == -1 && peer != null)
    {
      try
      {
        CPService discoveryService =
          getCPDevice().getCPServiceByType(InternetManagementConstants.DISCOVERY_SERVICE_TYPE);
        CPStateVariable port = discoveryService.getCPStateVariable("DiscoveryPort");

        // check for valid value
        if (port.getNumericValue() > 0)
        {
          deviceDiscoveryPort = (int)port.getNumericValue();
          deviceDiscoverySocketAddress = new InetSocketAddress(peer.getAddress(), deviceDiscoveryPort);
        }
      } catch (Exception ex)
      {
      }
    }
    return deviceDiscoveryPort;
  }

  /** Retrieves the socket address that is used for common device discovery */
  public InetSocketAddress getDiscoverySocketAddress()
  {
    return deviceDiscoverySocketAddress;
  }

  /** Retrieves the ID for the connection to this peer */
  public int getConnectionID()
  {
    if (peer != null)
    {
      return peer.getConnectionID();
    }

    return -1;
  }

}

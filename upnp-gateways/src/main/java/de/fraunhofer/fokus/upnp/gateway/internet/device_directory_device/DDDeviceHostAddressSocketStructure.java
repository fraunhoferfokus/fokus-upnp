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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import de.fraunhofer.fokus.upnp.core.device.DeviceHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;

/**
 * This class is used to bundle all sockets that are needed for an Internet accessible device. This
 * class is currently not used.
 * 
 * This class adds: A SSDP socket for receiving and answering external M-SEARCH messages
 * 
 * @author Alexander Koenig
 */
public class DDDeviceHostAddressSocketStructure extends DeviceHostAddressSocketStructure
{

  /** Socket that sends and receives M-SEARCH messages for the DeviceDirectoryDevice */
  private DatagramSocket internetSSDPSocket;

  /**
   * Creates a new instance of DDDeviceInetAddressSocketStructure
   * 
   * @param device
   *          The associated device
   * @param networkInterface
   *          The associated network interface
   * @param ssdpUnicastPort
   *          The port used for SSDP unicast
   * @param httpServerPort
   *          The port used for HTTP
   * @param address
   *          The address this structure should be bound to
   */
  public DDDeviceHostAddressSocketStructure(DeviceDirectoryDevice device,
    NetworkInterface networkInterface,
    int ssdpUnicastPort,
    int httpServerPort,
    int ssdpInternetPort,
    InetAddress address) throws SocketException
  {
    super(device, networkInterface, ssdpUnicastPort, httpServerPort, address);
    try
    {
      internetSSDPSocket = new DatagramSocket(ssdpInternetPort);

      TemplateDevice.printMessage(device.toString() + ": Started Internet SSDP socket on port " +
        internetSSDPSocket.getLocalPort());
    } catch (Exception e)
    {
      System.out.println("Cannot start DDDeviceHostAddressSocketStructure for address " + address.getHostAddress() +
        ": " + e.getMessage());
      throw new SocketException("Cannot start DDDeviceHostAddressSocketStructure");
    }
  }

  /** Retrieves the socket responsible for M-SEARCH messages for device directory devices */
  public DatagramSocket getInternetSSDPSocket()
  {
    return internetSSDPSocket;
  }

  /** Terminates the socket structure. */
  public void terminate()
  {
    try
    {
      internetSSDPSocket.close();
    } catch (Exception e)
    {
    }
    super.terminate();
  }

}

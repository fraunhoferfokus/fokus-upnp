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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import de.fraunhofer.fokus.lsf.core.BinaryHostAddressSocketStructure;
import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class encapsulates all sockets for one network interface which are needed by binary control points.
 * 
 * @author Alexander Koenig
 */
public class BinaryCPHostAddressSocketStructure extends BinaryHostAddressSocketStructure
{

  private MulticastSocket discoveryUnicastSocket;

  /**
   * Creates a new instance of BinaryCPHostAddressSocketStructure.
   * 
   * @param interfaceAddress
   */
  public BinaryCPHostAddressSocketStructure(InetAddress interfaceAddress)
  {
    super(interfaceAddress);

    try
    {
      // must be a multicast socket to allow forwarding to other network segments
      discoveryUnicastSocket = new MulticastSocket();
      discoveryUnicastSocket.setInterface(interfaceAddress);
      discoveryUnicastSocket.setSoTimeout(10);
      discoveryUnicastSocket.setTimeToLive(BinaryUPnPConstants.TTL);
    } catch (Exception e)
    {
      valid = false;
    }
  }

  /**
   * Creates a new instance of BinaryCPHostAddressSocketStructure.
   * 
   * @param interfaceAddress
   */
  public BinaryCPHostAddressSocketStructure(InetAddress interfaceAddress,
    String multicastAddressString,
    int discoveryPort,
    int eventPort)
  {
    super(interfaceAddress, multicastAddressString, discoveryPort, eventPort);

    try
    {
      // must be a multicast socket to allow forwarding to other network segments
      discoveryUnicastSocket = new MulticastSocket();
      discoveryUnicastSocket.setInterface(interfaceAddress);
      discoveryUnicastSocket.setSoTimeout(10);
      discoveryUnicastSocket.setTimeToLive(BinaryUPnPConstants.TTL);
    } catch (Exception e)
    {
      valid = false;
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.BinaryHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
    Portable.println("\r\n////////////////////////////////////////////////////////////");
    Portable.println("Socket structure for host address         " + hostAddress.getHostAddress());
    Portable.println("");
    Portable.println("Multicast address                         " + multicastAddress.getHostAddress());
    Portable.println("Discovery socket running on port          " + discoverySocket.getLocalPort());
    Portable.println("Discovery unicast socket running on port  " + discoveryUnicastSocket.getLocalPort());
    Portable.println("Description socket running on port        " + descriptionSocket.getLocalPort());
    Portable.println("Control unicast socket running on port    " + controlSocket.getLocalPort());
    Portable.println("Eventing socket running on port           " + eventSocket.getLocalPort());
    Portable.println("////////////////////////////////////////////////////////////\r\n");
  }

  /**
   * Retrieves the value of discoveryUnicastSocket.
   * 
   * @return The value of discoveryUnicastSocket
   */
  public DatagramSocket getDiscoveryUnicastSocket()
  {
    return discoveryUnicastSocket;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.BinaryHostAddressSocketStructure#terminate()
   */
  public void terminate()
  {
    try
    {
      discoveryUnicastSocket.close();
    } catch (Exception e)
    {
    }
    super.terminate();
  }

}

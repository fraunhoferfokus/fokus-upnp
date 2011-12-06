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
package de.fraunhofer.fokus.lsf.gateway.smep;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.AbstractHostAddressSocketStructure;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPSocketFactory;

/**
 * This class is used to bundle all sockets that are needed for a LSF SMEP gateway. e.g., for 802.15.4.
 * 
 * @author Alexander Koenig
 */
public class SMEPLSFGatewaySocketStructure extends AbstractHostAddressSocketStructure
{
  public static Logger         logger = Logger.getLogger("lsf");

  /** Socket for announce and search */
  protected SMEPDatagramSocket discoverySocket;

  /** Socket for unicast messages */
  protected SMEPDatagramSocket unicastSocket;

  /** Socket for events */
  protected SMEPDatagramSocket eventSocket;

  /**
   * Creates a new instance of SMEPLSFGatewaySocketStructure.
   * 
   * @param smepSocketFactory
   * @param address
   * @param discoveryPort
   * @param eventingPort
   */
  public SMEPLSFGatewaySocketStructure(SMEPSocketFactory smepSocketFactory,
    InetAddress address,
    int discoveryPort,
    int eventingPort)
  {
    super(address);
    try
    {
      discoverySocket = smepSocketFactory.createSMEPDatagramSocket(discoveryPort);
      discoverySocket.setSoTimeout(10);

      unicastSocket = smepSocketFactory.createSMEPDatagramSocket(0);
      unicastSocket.setSoTimeout(10);

      eventSocket = smepSocketFactory.createSMEPDatagramSocket(eventingPort);
      eventSocket.setSoTimeout(10);
    } catch (Exception e)
    {
      valid = false;
      Portable.println("Could not start sockets on " + address.getHostAddress() + ": " + e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.AbstractHostAddressSocketStructure#printUsedPorts()
   */
  public void printUsedPorts()
  {
    Portable.println("////////////////////////////////////////////////////////////");
    Portable.println("Socket structure for tunnel host address  " + hostAddress.getHostAddress());
    Portable.println("");
    Portable.println("Discovery socket running on port          " + discoverySocket.getLocalPort());
    Portable.println("Unicast socket running on port            " + unicastSocket.getLocalPort());
    Portable.println("Eventing socket running on port           " + eventSocket.getLocalPort());
    Portable.println("////////////////////////////////////////////////////////////\r\n");
  }

  public InetSocketAddress getDiscoveryBroadcastSocketAddress()
  {
    try
    {
      return new InetSocketAddress(InetAddress.getByName("0.0.255.255"), discoverySocket.getLocalPort());
    } catch (UnknownHostException e)
    {
    }
    return null;
  }

  public InetSocketAddress getEventingBroadcastSocketAddress()
  {
    try
    {
      return new InetSocketAddress(InetAddress.getByName("0.0.255.255"), eventSocket.getLocalPort());
    } catch (UnknownHostException e)
    {
    }
    return null;
  }

  /**
   * Retrieves the value of discoverySocket.
   * 
   * @return The value of discoverySocket
   */
  public SMEPDatagramSocket getDiscoverySocket()
  {
    return discoverySocket;
  }

  /**
   * Retrieves the value of unicastSocket.
   * 
   * @return The value of unicastSocket
   */
  public SMEPDatagramSocket getUnicastSocket()
  {
    return unicastSocket;
  }

  /**
   * @return the eventSocket
   */
  public SMEPDatagramSocket getEventSocket()
  {
    return eventSocket;
  }

  /** Terminates the socket structure */
  public void terminate()
  {
    try
    {
      discoverySocket.close();
      unicastSocket.close();
      eventSocket.close();
    } catch (Exception e)
    {
    }
  }

}

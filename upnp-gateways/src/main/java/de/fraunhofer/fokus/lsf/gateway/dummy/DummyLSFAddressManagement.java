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
package de.fraunhofer.fokus.lsf.gateway.dummy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;

/**
 * This class is used to generate a LSFForwarderModule that does nothing. Used for debugging.
 * 
 * @author Alexander Koenig
 */
public class DummyLSFAddressManagement extends AbstractLSFAddressManagement
{

  private DummyLSFForwarderModule dummyLSFForwarderModule = null;

  /**
   * Creates a new instance of DummyLSFAddressManagement and adds all local network interfaces to the message forwarder.
   * 
   * @param messageForwarder
   */
  public DummyLSFAddressManagement(LSFMessageForwarder messageForwarder)
  {
    super(messageForwarder);

    initSocketStructures();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#initSocketStructures()
   */
  protected void initSocketStructures()
  {
    DummyLSFGatewaySocketStructure socketStructure = null;
    try
    {
      socketStructure = new DummyLSFGatewaySocketStructure(InetAddress.getByName("192.168.20.20"));

      socketStructure.printUsedPorts();

      // create forwarder module for socket structure
      dummyLSFForwarderModule =
        new DummyLSFForwarderModule(messageForwarder,
          new InetSocketAddress(BinaryUPnPConstants.BinaryUPnPMulticastAddress,
            BinaryUPnPConstants.DiscoveryMulticastPort),
          socketStructure);

      // register in message forwarder
      messageForwarder.addForwarderModule(dummyLSFForwarderModule);
    } catch (UnknownHostException e)
    {
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#printDebugStats()
   */
  public void printDebugStats()
  {
  }

  /** Terminates the management for multiple interfaces. */
  public void terminate()
  {
    // terminate all forwarder modules
    dummyLSFForwarderModule.terminate();
    // remove from message forwarder
    messageForwarder.removeForwarderModule(dummyLSFForwarderModule);
  }

}

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

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;

/**
 * This class can be used to forward LSF messages over a certain physical network interface.
 * 
 * @author Alexander Koenig
 * 
 */
public class DummyLSFForwarderModule extends AbstractLSFForwarderModule
{

  /** Reference to associated socket structure */
  private DummyLSFGatewaySocketStructure socketStructure;

  /**
   * @param messageForwarder
   * @param socketStructure
   */
  public DummyLSFForwarderModule(LSFMessageForwarder messageForwarder,
    InetSocketAddress discoveryMulticastSocketAddress,
    DummyLSFGatewaySocketStructure gatewaySocketStructure)
  {
    super(messageForwarder);
    this.socketStructure = gatewaySocketStructure;

    synchronized(messageForwarder.getLock())
    {
      this.moduleID = (byte)messageForwarder.getForwarderModuleNextID();
      messageForwarder.setForwarderModuleNextID(messageForwarder.getForwarderModuleNextID() + 1);
    }
    moduleHostAddress = gatewaySocketStructure.getHostAddress();
    this.discoveryGroupSocketAddress = discoveryMulticastSocketAddress;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getDiscoverySocket()
   */
  public IDatagramSocket getDiscoverySocket()
  {
    return socketStructure.getDiscoverySocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocket()
   */
  public IDatagramSocket getUnicastSocket()
  {
    return socketStructure.getUnicastSocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getEventSocket()
   */
  public IDatagramSocket getEventSocket()
  {
    return socketStructure.getEventSocket();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastSocketAddress()
   */
  public InetSocketAddress getUnicastSocketAddress()
  {
    return new InetSocketAddress(socketStructure.getHostAddress(), socketStructure.getUnicastSocket().getLocalPort());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getUnicastPort()
   */
  public int getUnicastPort()
  {
    return socketStructure.getUnicastSocket().getLocalPort();
  }

  /**
   * Retrieves the value of socketStructure.
   * 
   * @return The value of socketStructure
   */
  public DummyLSFGatewaySocketStructure getSocketStructure()
  {
    return socketStructure;
  }

  public String toString()
  {
    return "Dummy:" + super.toString();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getPhyType()
   */
  public byte getPhyType()
  {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule#terminate()
   */
  public void terminate()
  {
    super.terminate();
    socketStructure.terminate();
  }

}

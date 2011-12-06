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

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.network.IDatagramSocket;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPSocketFactory;

/**
 * This class can be used to forward LSF messages over the SMEP, e.g., to connect 802.15.4 clients.
 * 
 * @author Alexander Koenig
 * 
 */
public class SMEPLSFForwarderModule extends AbstractLSFForwarderModule
{

  /** Reference to associated socket structure */
  protected SMEPLSFGatewaySocketStructure socketStructure;

  /** Associated socket factory */
  protected SMEPSocketFactory             smepSocketFactory;

  /**
   * Creates a new instance of SMEPLSFForwarderModule.
   * 
   * @param messageForwarder
   * @param smepSocketFactory
   * @param socketStructure
   */
  public SMEPLSFForwarderModule(LSFMessageForwarder messageForwarder,
    SMEPSocketFactory smepSocketFactory,
    SMEPLSFGatewaySocketStructure socketStructure)
  {
    super(messageForwarder);
    this.smepSocketFactory = smepSocketFactory;

    this.socketStructure = socketStructure;

    // determine module ID which is used for message routing
    synchronized(messageForwarder.getLock())
    {
      this.moduleID = (byte)messageForwarder.getForwarderModuleNextID();
      messageForwarder.setForwarderModuleNextID(messageForwarder.getForwarderModuleNextID() + 1);
    }
    moduleHostAddress = socketStructure.getHostAddress();
    // overwrite standard IP addresses and ports with SMEP default values
    this.discoveryGroupSocketAddress = socketStructure.getDiscoveryBroadcastSocketAddress();
    this.descriptionPort = BinaryUPnPConstants.SMEPDescriptionPort;
    this.controlPort = BinaryUPnPConstants.SMEPControlPort;
    this.eventGroupSocketAddress = socketStructure.getEventingBroadcastSocketAddress();
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
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.AbstractLSFForwarderModule#announceDeviceToForwarderModule(de.fraunhofer.fokus.lsf.core.base.AbstractBinaryDevice)
   */
  public boolean canForwardAnnouncementMessage(AbstractBinaryDevice device)
  {
    // as tunnel interface, we forward all messages
    System.out.println("Device " + device.toString() + " is announced in " + toString());

    return true;
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
  public SMEPLSFGatewaySocketStructure getSocketStructure()
  {
    return socketStructure;
  }

  public String toString()
  {
    return "SMEP with moduleID " + super.toString();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule#getPhyType()
   */
  public byte getPhyType()
  {
    return BinaryUPnPConstants.PhyType802_15_4;
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

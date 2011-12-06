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

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.startup.LSFChildStartupConfiguration;
import de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPSocketFactory;
import de.fraunhofer.fokus.util.hal.SerialHardwareAbstractionLayer;
import de.fraunhofer.fokus.util.xbee.IXBeeEventListener;
import de.fraunhofer.fokus.util.xbee.XBeeManager;

/**
 * This class is used to connect a SMEP forwarder module, e.g., for 802.15.4.
 * 
 * @author Alexander Koenig
 */
public class SMEPLSFAddressManagement extends AbstractLSFAddressManagement implements IXBeeEventListener
{

  /** Associated forwarder module */
  private SMEPLSFForwarderModule       forwarderModule;

  /** SMEP socket factory */
  private SMEPSocketFactory            socketFactory;

  /** XBee manager */
  private XBeeManager                  xbeeManager;

  private LSFChildStartupConfiguration gatewayStartupConfiguration = null;

  /**
   * Creates a new instance of SMEPLSFAddressManagement.
   * 
   * @param messageForwarder
   */
  public SMEPLSFAddressManagement(LSFMessageForwarder messageForwarder)
  {
    super(messageForwarder);

    // use standard SMEP addresses as default
    multicastAddress = BinaryUPnPConstants.XBeeBroadcastAddress;
    discoveryPort = BinaryUPnPConstants.SMEPDiscoveryPort;
    eventPort = BinaryUPnPConstants.SMEPEventPort;

    String commPort = "COM1";
    // check if network properties should be updated for this gateway entity
    gatewayStartupConfiguration =
      messageForwarder.getGatewayStartupConfiguration(StringHelper.getShortClassName(getClass().getName()));
    if (gatewayStartupConfiguration != null && gatewayStartupConfiguration.hasCustomNetworkProperties())
    {
      multicastAddress = gatewayStartupConfiguration.getMulticastAddress();
      discoveryPort = gatewayStartupConfiguration.getDiscoveryMulticastPort();
      eventPort = gatewayStartupConfiguration.getEventMulticastPort();
    }
    if (gatewayStartupConfiguration != null)
    {
      commPort = gatewayStartupConfiguration.getProperty("CommPort", commPort);
    }
    // create serial HAL
    SerialHardwareAbstractionLayer serialHardwareAbstractionLayer = new SerialHardwareAbstractionLayer(commPort);

    // create XBee manager
    xbeeManager = new XBeeManager(serialHardwareAbstractionLayer);

    // create SMEP socket factory
    socketFactory = new SMEPSocketFactory(xbeeManager);
    // register network status provider
    socketFactory.setNetworkStatus(xbeeManager.getXbeePacketHandler());

    // forward SMEP packets from the XBee manager to the socket factory
    xbeeManager.setSMEPPacketListener(socketFactory);

    // set handler for local address event
    xbeeManager.setXbeeEventListener(this);

    // register manager for event triggering
    messageForwarder.getEventThread().register(xbeeManager);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeLocalAddressRetrieved(de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus)
   */
  public void xbeeLocalAddressRetrieved(INetworkStatus sender)
  {
    if (sender.getLocalAddress() != null)
    {
      // create socket structure for SMEP
      SMEPLSFGatewaySocketStructure socketStructure =
        new SMEPLSFGatewaySocketStructure(socketFactory,
          IPHelper.toInetAddress(sender.getLocalAddress()),
          discoveryPort,
          eventPort);

      // create forwarder module for socket structure
      forwarderModule = new SMEPLSFForwarderModule(messageForwarder, socketFactory, socketStructure);

      Portable.println("ModuleID:" + forwarderModule.getModuleID() + " on SMEP interface");
      socketStructure.printUsedPorts();

      // register in message forwarder
      messageForwarder.addForwarderModule(forwarderModule);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#atCommandResponseReceived(int, java.lang.String, int, byte[])
   */
  public void xbeeATCommandResponseReceived(int frameID, String command, int status, byte[] value)
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeTXStatusReceived(int, int)
   */
  public void xbeeTXStatusReceived(int frameID, int status)
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeModemStatusReceived(int)
   */
  public void xbeeModemStatusReceived(int status)
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#printDebugStats()
   */
  public void printDebugStats()
  {
    Portable.println("  " + forwarderModule.getModuleID() + ": Running on " +
      forwarderModule.getSocketStructure().getHostAddress().getHostAddress());

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.AbstractLSFAddressManagement#terminate()
   */
  public void terminate()
  {
    if (forwarderModule != null)
    {
      forwarderModule.terminate();
    }
    // remove from message forwarder
    messageForwarder.removeForwarderModule(forwarderModule);

    socketFactory.terminate();
    xbeeManager.terminate();
  }
}

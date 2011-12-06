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
package de.fraunhofer.fokus.upnp.gateway.common.message_forwarder;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.GatewayStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagement;
import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.gateway.network_interfaces.InetAddressManagement;
import de.fraunhofer.fokus.upnp.gateway.tcp_tunnel.TCPTunnelClientManagement;
import de.fraunhofer.fokus.upnp.gateway.tcp_tunnel.TCPTunnelServerManagement;

/** This class works as UPnP message forwarder between all kinds of forwarder modules. */
public class MessageForwarderEntity extends TemplateEntity implements IForwarderModuleEventListener
{

  private MessageForwarder              messageForwarder;

  private InetAddressManagement         inetAddressManagement;

  private TCPTunnelServerManagement     tcpTunnelServerManagement;

  private TCPTunnelClientManagement     tcpTunnelClientManagement;

  private InternetManagement            internetManagement;

  /** Optional listener for TCP tunnel client events */
  private IForwarderModuleEventListener forwarderModuleEventListener;

  /**
   * Creates a new instance of MessageForwarderEntity.
   * 
   * 
   */
  public MessageForwarderEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    messageForwarder = new MessageForwarder(getStartupConfiguration());
    inetAddressManagement = new InetAddressManagement(messageForwarder);
  }

  /** Starts the TCP server tunnel management */
  public void startTCPServerTunnelManagement()
  {
    if (tcpTunnelServerManagement == null)
    {
      GatewayStartupConfiguration gatewayStartupConfiguration =
        messageForwarder.getGatewayStartupConfiguration("TCPTunnelServerManagement");

      if (gatewayStartupConfiguration == null)
      {
        System.out.println("Missing gateway startup info for TCP tunnel.");
        return;
      }

      int tunnelServerPort = gatewayStartupConfiguration.getNumericProperty("TCPTunnelServerPort", 10000);
      String tunnelInetAddress = gatewayStartupConfiguration.getProperty("TCPTunnelInetAddress", "192.168.200.200");
      try
      {
        tcpTunnelServerManagement =
          new TCPTunnelServerManagement(messageForwarder, tunnelServerPort, InetAddress.getByName(tunnelInetAddress));
        // receive TCP tunnel events from management
        tcpTunnelServerManagement.setForwarderModuleEventListener(this);
      } catch (Exception e)
      {
        System.out.println("Could not start TCP tunnel server: " + e.getMessage());
      }
    }
  }

  /** Stops the TCP server tunnel management */
  public void stopTCPServerTunnelManagement()
  {
    if (tcpTunnelServerManagement != null)
    {
      tcpTunnelServerManagement.terminate();
      tcpTunnelServerManagement = null;
    }
  }

  /** Starts the TCP client tunnel management */
  public void startTCPClientTunnelManagement()
  {
    if (tcpTunnelClientManagement == null)
    {
      GatewayStartupConfiguration gatewayStartupConfiguration =
        messageForwarder.getGatewayStartupConfiguration("TCPTunnelClientManagement");

      if (gatewayStartupConfiguration == null)
      {
        System.out.println("Missing gateway startup info for TCP tunnel client.");
        return;
      }

      String tunnelServerAddress = gatewayStartupConfiguration.getProperty("TCPTunnelServerAddress", "localhost");
      int tunnelServerPort = gatewayStartupConfiguration.getNumericProperty("TCPTunnelServerPort", 10000);
      String tunnelInetAddress = gatewayStartupConfiguration.getProperty("TCPTunnelInetAddress", "192.168.200.200");
      try
      {
        tcpTunnelClientManagement =
          new TCPTunnelClientManagement(messageForwarder,
            new InetSocketAddress(tunnelServerAddress, tunnelServerPort),
            InetAddress.getByName(tunnelInetAddress));
        // receive TCP tunnel events from management
        tcpTunnelClientManagement.setForwarderModuleEventListener(this);
      } catch (Exception e)
      {
        System.out.println("Could not start TCP tunnel client: " + e.getMessage());
      }
    }
  }

  /** Stops the TCP client tunnel management */
  public void stopTCPClientTunnelManagement()
  {
    if (tcpTunnelClientManagement != null)
    {
      tcpTunnelClientManagement.terminate();
      tcpTunnelClientManagement = null;
    }
  }

  /** Starts the Internet management */
  public void startInternetManagement()
  {
    if (internetManagement == null)
    {
      GatewayStartupConfiguration gatewayStartupConfiguration =
        messageForwarder.getGatewayStartupConfiguration("InternetManagement");

      if (gatewayStartupConfiguration == null)
      {
        System.out.println("Missing gateway startup info for Internet.");
        return;
      }

      String globalIPAddress = gatewayStartupConfiguration.getProperty("GlobalIPAddress");
      if (globalIPAddress == null)
      {
        System.out.println("Missing global IP address.");
        return;
      }
      // load ports from startup configuration
      int ssdpDeviceReceptionPort =
        gatewayStartupConfiguration.getNumericProperty("SSDPDeviceReceptionPort",
          InternetManagementConstants.SSDP_DEVICE_PORT);
      int ssdpDeviceMSearchPort =
        gatewayStartupConfiguration.getNumericProperty("SSDPDeviceMSearchPort",
          InternetManagementConstants.SSDP_DEVICE_M_SEARCH_SEND_PORT);
      int gatewayServerPort =
        gatewayStartupConfiguration.getNumericProperty("GatewayServerPort",
          InternetManagementConstants.HTTP_DEVICE_REQUEST_PORT);

      try
      {
        internetManagement =
          new InternetManagement(messageForwarder,
            InetAddress.getByName(globalIPAddress),
            ssdpDeviceReceptionPort,
            ssdpDeviceMSearchPort,
            gatewayServerPort);
      } catch (Exception e)
      {
        System.out.println("Could not start Internet gateway: " + e.getMessage());
      }
    }
  }

  /** Stops the Internet management */
  public void stopInternetManagement()
  {
    if (internetManagement != null)
    {
      internetManagement.terminate();
      internetManagement = null;
    }
  }

  /**
   * Retrieves the inetAddressManagement.
   * 
   * @return The inetAddressManagement
   */
  public InetAddressManagement getInetAddressManagement()
  {
    return inetAddressManagement;
  }

  /**
   * Retrieves the messageForwarder.
   * 
   * @return The messageForwarder
   */
  public MessageForwarder getMessageForwarder()
  {
    return messageForwarder;
  }

  /**
   * Retrieves the tcpTunnelServerManagement.
   * 
   * @return The tcpTunnelServerManagement
   */
  public TCPTunnelServerManagement getTCPTunnelServerManagement()
  {
    return tcpTunnelServerManagement;
  }

  /**
   * Retrieves the tcpTunnelClientManagement.
   * 
   * @return The tcpTunnelClientManagement
   */
  public TCPTunnelClientManagement getTCPTunnelClientManagement()
  {
    return tcpTunnelClientManagement;
  }

  /**
   * Retrieves the internetManagement.
   * 
   * @return The internetManagement
   */
  public InternetManagement getInternetManagement()
  {
    return internetManagement;
  }

  /**
   * Retrieves the clientEventListener.
   * 
   * @return The clientEventListener
   */
  public IForwarderModuleEventListener getForwarderModuleEventListener()
  {
    return forwarderModuleEventListener;
  }

  /**
   * Sets the clientEventListener.
   * 
   * @param eventListener
   *          The new value for clientEventListener
   */
  public void setForwarderModuleEventListener(IForwarderModuleEventListener eventListener)
  {
    this.forwarderModuleEventListener = eventListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelClientEventListener#newClient(java.net.InetSocketAddress)
   */
  public void newForwarderModule(IForwarderModule module)
  {
    // forward event
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.newForwarderModule(module);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelClientEventListener#removedClient(java.net.InetSocketAddress)
   */
  public void removedForwarderModule(IForwarderModule module)
  {
    // forward event
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.removedForwarderModule(module);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelEventListener#disconnectedClient(de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.TCPTunnelForwarderModule)
   */
  public void disconnectedForwarderModule(IForwarderModule module)
  {
    // forward event
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.disconnectedForwarderModule(module);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelEventListener#reconnectedClient(de.fhg.fokus.magic.upnp.gateway.common.IForwarderModule)
   */
  public void reconnectedForwarderModule(IForwarderModule module)
  {
    // forward event
    if (forwarderModuleEventListener != null)
    {
      forwarderModuleEventListener.reconnectedForwarderModule(module);
    }
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    stopTCPClientTunnelManagement();
    stopTCPServerTunnelManagement();
    stopInternetManagement();

    inetAddressManagement.terminate();
    messageForwarder.terminate();

    super.terminate();
  }

}

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
package de.fraunhofer.fokus.lsf.gateway.common.message_forwarder;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.lsf.core.startup.LSFChildStartupConfiguration;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.lsf.core.templates.BinaryTemplateEntity;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener;
import de.fraunhofer.fokus.lsf.gateway.physical_network.PhysicalLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.smep.SMEPLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.tcp_tunnel.TCPTunnelClientLSFAddressManagement;
import de.fraunhofer.fokus.lsf.gateway.tcp_tunnel.TCPTunnelServerLSFAddressManagement;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;

/** This class works as LSF message forwarder between all kinds of forwarder modules. */
public class LSFMessageForwarderEntity extends BinaryTemplateEntity implements
  ILSFForwarderModuleEventListener,
  IKeyListener
{

  private LSFMessageForwarder                 messageForwarder;

  private PhysicalLSFAddressManagement        physicalManagement;

  private TCPTunnelClientLSFAddressManagement tcpTunnelClientManagement;

  private TCPTunnelServerLSFAddressManagement tcpTunnelServerManagement;

  private SMEPLSFAddressManagement            smepManagement;

  //  private DummyLSFAddressManagement    dummyLSFAddressManagement;

  /**
   * Creates a new instance of LSFMessageForwarderEntity.java
   * 
   * @param startupConfiguration
   */
  public LSFMessageForwarderEntity(LSFStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    if (getStartupConfiguration() == null || !getStartupConfiguration().isValid())
    {
      Portable.println("Missing startup info.");
      System.exit(1);
    }
    startupConfiguration = getStartupConfiguration();
    messageForwarder = new LSFMessageForwarder(startupConfiguration);
    if (startupConfiguration.getBooleanProperty("StartPhysicalManagement"))
    {
      startPhysicalManagement();
    }
    if (startupConfiguration.getBooleanProperty("StartTCPTunnelServerManagement"))
    {
      startTCPTunnelServerManagement();
    }
    if (startupConfiguration.getBooleanProperty("StartTCPTunnelClientManagement"))
    {
      startTCPTunnelClientManagement();
      //    dummyLSFAddressManagement = new DummyLSFAddressManagement(messageForwarder);
    }
    if (getKeyboardThread() != null)
    {
      getKeyboardThread().setKeyListener(this);
      Portable.println("  Type <1> to show statistics");
      Portable.println("  Type <2> to start or stop the physical management");
      Portable.println("  Type <3> to start or stop the TCP tunnel server");
      Portable.println("  Type <4> to start or stop the TCP tunnel client");
      Portable.println("  Type <5> to start or stop the 802.15.4 management");
    }
  }

  public static void main(String[] args)
  {
    new LSFMessageForwarderEntity(args.length > 0 ? new LSFStartupConfiguration(args[0]) : null);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener#newForwarderModule(de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule)
   */
  public void newForwarderModule(ILSFForwarderModule module)
  {

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener#removedForwarderModule(de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule)
   */
  public void removedForwarderModule(ILSFForwarderModule module)
  {
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener#disconnectedForwarderModule(de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule)
   */
  public void disconnectedForwarderModule(ILSFForwarderModule module)
  {
    Portable.println("Forwarder module disconnected:" + module.toString());
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener#reconnectedForwarderModule(de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModule)
   */
  public void reconnectedForwarderModule(ILSFForwarderModule module)
  {
  }

  /** Starts the TCP server tunnel management */
  public void startTCPTunnelServerManagement()
  {
    if (tcpTunnelServerManagement == null)
    {
      LSFChildStartupConfiguration gatewayStartupConfiguration =
        messageForwarder.getGatewayStartupConfiguration("TCPTunnelServerLSFAddressManagement");

      if (gatewayStartupConfiguration == null)
      {
        System.out.println("Missing gateway startup info for TCP tunnel server.");
        return;
      }

      int tunnelServerPort = gatewayStartupConfiguration.getNumericProperty("TCPTunnelServerPort", 10000);
      String tunnelInetAddress = gatewayStartupConfiguration.getProperty("TCPTunnelInetAddress", "192.168.200.200");
      try
      {
        tcpTunnelServerManagement =
          new TCPTunnelServerLSFAddressManagement(messageForwarder,
            tunnelServerPort,
            InetAddress.getByName(tunnelInetAddress));
        // receive TCP tunnel events from management
        tcpTunnelServerManagement.setForwarderModuleEventListener(this);
      } catch (Exception e)
      {
        System.out.println("Could not start TCP tunnel server: " + e.getMessage());
      }
    }
  }

  /** Stops the TCP server tunnel management */
  public void stopTCPTunnelServerManagement()
  {
    if (tcpTunnelServerManagement != null)
    {
      Portable.println("  Stop tunnel server");

      tcpTunnelServerManagement.terminate();
      tcpTunnelServerManagement = null;
    }
  }

  /** Starts the physical management */
  public void startPhysicalManagement()
  {
    if (physicalManagement == null)
    {
      Portable.println("  Start physical management");
      physicalManagement = new PhysicalLSFAddressManagement(messageForwarder);
    }
  }

  /** Stops the physical management */
  public void stopPhysicalManagement()
  {
    if (physicalManagement != null)
    {
      Portable.println("  Stop physical management");
      physicalManagement.terminate();
      physicalManagement = null;
    }
  }

  /** Starts the SMEP management */
  public void startSMEPManagement()
  {
    if (smepManagement == null)
    {
      Portable.println("  Start SMEP management");
      smepManagement = new SMEPLSFAddressManagement(messageForwarder);
    }
  }

  /** Stops the SMEP management */
  public void stopSMEPManagement()
  {
    if (smepManagement != null)
    {
      Portable.println("  Stop SMEP management");
      smepManagement.terminate();
      smepManagement = null;
    }
  }

  /** Starts the TCP client tunnel management */
  public void startTCPTunnelClientManagement()
  {
    if (tcpTunnelClientManagement == null)
    {
      LSFChildStartupConfiguration gatewayStartupConfiguration =
        messageForwarder.getGatewayStartupConfiguration("TCPTunnelClientLSFAddressManagement");

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
          new TCPTunnelClientLSFAddressManagement(messageForwarder, new InetSocketAddress(tunnelServerAddress,
            tunnelServerPort), InetAddress.getByName(tunnelInetAddress));

        // receive TCP tunnel events from management
        tcpTunnelClientManagement.setForwarderModuleEventListener(this);
      } catch (Exception e)
      {
        System.out.println("Could not start TCP tunnel client: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  /** Stops the TCP client tunnel management */
  public void stopTCPTunnelClientManagement()
  {
    if (tcpTunnelClientManagement != null)
    {
      Portable.println("  Stop tunnel client");

      tcpTunnelClientManagement.terminate();
      tcpTunnelClientManagement = null;
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == '2')
    {
      if (physicalManagement != null)
      {
        stopPhysicalManagement();
      } else
      {
        startPhysicalManagement();
      }
    }
    if (code == '3')
    {
      if (tcpTunnelServerManagement != null)
      {
        stopTCPTunnelServerManagement();
      } else
      {
        startTCPTunnelServerManagement();
      }
    }
    if (code == '4')
    {
      if (tcpTunnelClientManagement != null)
      {
        stopTCPTunnelClientManagement();
      } else
      {
        startTCPTunnelClientManagement();
      }
    }
    if (code == '5')
    {
      if (smepManagement != null)
      {
        stopSMEPManagement();
      } else
      {
        startSMEPManagement();
      }
    }
    if (code == 'h')
    {
      Portable.println("  Type <e> or <q> to exit the application");
      Portable.println("  Type <1> to show statistics");
      Portable.println("  Type <2> to start or stop the physical management");
      Portable.println("  Type <3> to start or stop the TCP tunnel server");
      Portable.println("  Type <4> to start or stop the TCP tunnel client");
      Portable.println("  Type <5> to start or stop the 802.15.4 management");
      Portable.println("");
    }
    if (code == '1')
    {
      Portable.println(StringHelper.getDivider());
      if (tcpTunnelClientManagement != null)
      {
        Portable.println("Tunnel client is running.");
        tcpTunnelClientManagement.printDebugStats();
      }
      if (tcpTunnelServerManagement != null)
      {
        Portable.println("Tunnel server is running.");
        tcpTunnelServerManagement.printDebugStats();
      }
      if (physicalManagement != null)
      {
        Portable.println("Physical management is running.");
        physicalManagement.printDebugStats();
      }
      if (smepManagement != null)
      {
        Portable.println("SMEP management is running.");
        smepManagement.printDebugStats();
      }
    }
  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    stopPhysicalManagement();
    //    dummyLSFAddressManagement.terminate();
    stopTCPTunnelServerManagement();
    stopTCPTunnelClientManagement();
    stopSMEPManagement();

    messageForwarder.terminate();

    super.terminate();
  }

}

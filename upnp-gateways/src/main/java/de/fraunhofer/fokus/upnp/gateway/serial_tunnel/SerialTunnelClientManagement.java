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
package de.fraunhofer.fokus.upnp.gateway.serial_tunnel;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.common.upnp_tunnel.UPnPTunnelSocketStructure;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelEventListener;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IIPTunnelNetworkInterface;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelConstants;
import de.fraunhofer.fokus.upnp.util.tunnel.network_interface.SerialTunnelNetworkInterface;

/**
 * This class is used to tunnel UPnP over a serial connection. It tries to connect to a COM port to
 * tunnel UPnP messages.
 * 
 * @author Alexander Koenig
 */
public class SerialTunnelClientManagement extends Thread implements IIPTunnelEventListener, SerialPortEventListener
{

  /** Reference to message forwarder */
  private MessageForwarder              messageForwarder;

  /** Forwarder module for the serial tunnel */
  private SerialTunnelForwarderModule   serialTunnelForwarderModule = null;

  private String                        commPort;

  private CommPortIdentifier            commPortIdentifier;

  private SerialPort                    serialPort                  = null;

  /** Inet address for the serial tunnel */
  private InetAddress                   ipTunnelInetAddress;

  private byte[]                        id;

  private long                          lastConnectionTime          = 0;

  private boolean                       terminateThread             = false;

  private boolean                       terminated                  = false;

  /** Optional listener for client events */
  private IForwarderModuleEventListener forwarderModuleEventListener;

  /**
   * Creates a new instance of SerialTunnelManagement.
   * 
   * @param messageForwarder
   *          The central message forwarder
   * @param commPort
   *          Com port that should be used
   * @param ipTunnelInetAddress
   *          The address used for the virtual IP tunnel
   */
  public SerialTunnelClientManagement(MessageForwarder messageForwarder,
    String commPort,
    InetAddress ipTunnelInetAddress)
  {
    setName("SerialTunnelManagement");
    this.messageForwarder = messageForwarder;
    this.commPort = commPort;
    this.ipTunnelInetAddress = ipTunnelInetAddress;

    id = new byte[5];
    // use IP address for ID to allow reconnection
    System.arraycopy(IPHelper.getLocalHostAddress().getAddress(), 0, id, 0, 4);
    start();
  }

  /**
   * Sets the clientEventsListener.
   * 
   * @param clientEventsListener
   *          The new value for clientEventsListener
   */
  public void setForwarderModuleEventListener(IForwarderModuleEventListener eventListener)
  {
    this.forwarderModuleEventListener = eventListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#initialConfigReceived(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface,
   *      byte[])
   */
  public void initialConfigReceived(IIPTunnelNetworkInterface networkInterface, byte[] data)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IIPTunnelNetworkInterfaceListener#socketClosed(de.fhg.fokus.magic.upnp.gateway.common.ip_tunnel.IPTunnelNetworkInterface)
   */
  public void outerConnectionClosed(IIPTunnelNetworkInterface ipTunnelNetworkInterface)
  {
    if (forwarderModuleEventListener != null && serialTunnelForwarderModule != null)
    {
      forwarderModuleEventListener.disconnectedForwarderModule(serialTunnelForwarderModule);
    }

    serialPort = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
   */
  public void serialEvent(SerialPortEvent event)
  {
    if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
    {
      try
      {
        byte[] buffer = new byte[serialPort.getInputStream().available()];
        int length = serialPort.getInputStream().read(buffer);

        System.out.println("Received " + length + " bytes from serial tunnel");

        // forward to forwarder module
        serialTunnelForwarderModule.dataReceived(buffer);
      } catch (IOException e)
      {
        System.out.println("Fehler: " + e);
      }
    }
  }

  public void run()
  {
    System.out.println("  Started SerialTunnelManagement. Try to connect to port " + commPort);

    while (!terminateThread)
    {
      if (serialPort == null && System.currentTimeMillis() - lastConnectionTime > 10000)
      {
        try
        {
          commPortIdentifier = CommPortIdentifier.getPortIdentifier(commPort);
          serialPort = (SerialPort)commPortIdentifier.open("SerialTunnel", 2000);

          // open serial connection
          serialPort.addEventListener(this);
          serialPort.notifyOnDataAvailable(true);
          serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

          System.out.println("Successfully opened the serial port for tunneling");

          // if this is the first connection attempt
          if (serialTunnelForwarderModule == null)
          {
            // create virtual network interface
            SerialTunnelNetworkInterface serialTunnelNetworkInterface =
              new SerialTunnelNetworkInterface(serialPort.getOutputStream(), "SerialTunnelManagement");

            serialTunnelNetworkInterface.setIPTunnelInetAddress(ipTunnelInetAddress);

            // set optional network interface parameters
            serialTunnelNetworkInterface.setMaximumSegmentSize(1400);
            serialTunnelNetworkInterface.setAcceptOnlySinglePacketsPerSocket(false);
            serialTunnelNetworkInterface.setPacketGapTime(10);

            // create UPnP socket structure for tunnel
            System.out.println("Create UPnPTunnelSocketStructure for address " + ipTunnelInetAddress.getHostAddress());

            UPnPTunnelSocketStructure ipTunnelSocketStructure =
              new UPnPTunnelSocketStructure(serialTunnelNetworkInterface.getIPTunnelSocketFactory(),
                messageForwarder.getGatewayMessageManager(),
                ipTunnelInetAddress);

            // create forwarder module
            serialTunnelForwarderModule =
              new SerialTunnelForwarderModule(messageForwarder,
                serialTunnelNetworkInterface,
                ipTunnelSocketStructure,
                StringHelper.byteArrayToBase32(id));

            serialTunnelForwarderModule.setIPTunnelListener(this);

            // add forwarder module to central message forwarder
            messageForwarder.addForwarderModule(serialTunnelForwarderModule);

            serialTunnelForwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(id);
            if (forwarderModuleEventListener != null)
            {
              forwarderModuleEventListener.newForwarderModule(serialTunnelForwarderModule);
            }
          } else
          {
            System.out.println("Use existing forwarder module");
            // this is a reconnect

            // send ID to server
            serialTunnelForwarderModule.getIPTunnelNetworkInterface().sendIDToTunnel(id);
            // update socket
            serialTunnelForwarderModule.reconnect(serialPort.getOutputStream());
            // forward event
            if (forwarderModuleEventListener != null)
            {
              forwarderModuleEventListener.reconnectedForwarderModule(serialTunnelForwarderModule);
            }
          }
        } catch (Exception ex)
        {
          System.out.println("Error opening serial port: " + ex.getMessage());
          serialPort = null;
        }
      }
      // terminate forwarder module if tunnel is disconnected for too long
      if (serialPort == null &&
        serialTunnelForwarderModule != null &&
        System.currentTimeMillis() - serialTunnelForwarderModule.getIPTunnelNetworkInterface().getDisconnectionTime() > IPTunnelConstants.TERMINATION_TIMEOUT)
      {
        // terminate forwarder module
        serialTunnelForwarderModule.terminate();
        // remove from message forwarder
        messageForwarder.removeForwarderModule(serialTunnelForwarderModule);
        // forward event
        if (forwarderModuleEventListener != null)
        {
          forwarderModuleEventListener.removedForwarderModule(serialTunnelForwarderModule);
        }
        // allow recreation of forwarder module
        serialTunnelForwarderModule = null;
      }
      ThreadHelper.sleep(1000);
    }
    terminated = true;
  }

  /** Terminates the forwarder module for the TCP tunnel. */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(20);
    }

    if (serialTunnelForwarderModule != null)
    {
      // terminate forwarder module
      serialTunnelForwarderModule.terminate();
      // remove from message forwarder
      messageForwarder.removeForwarderModule(serialTunnelForwarderModule);
      // forward event
      if (forwarderModuleEventListener != null)
      {
        forwarderModuleEventListener.removedForwarderModule(serialTunnelForwarderModule);
      }
      serialTunnelForwarderModule = null;
    }
    System.out.println("  Terminated SerialTunnelClientManagement.");
  }

}

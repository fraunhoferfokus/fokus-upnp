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
package de.fraunhofer.fokus.util.xbee;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.hal.HardwareAbstractionLayer;
import de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus;
import de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener;
import de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener;
import de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketSender;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class opens a serial port to send and receive SMEP packets. Used to access 802.15.4 sensor nodes.
 * 
 * @author Alexander Koenig
 */
public class XBeeManager implements
  IEventListener,
  IPhyStatusListener,
  IXBeeEventListener,
  ISMEPPacketSender,
  ISMEPPacketListener
{

  /** Time to wait for a TX response or AT command response in milliseconds */
  private static int               XBEE_PACKET_RESPONSE_TIMEOUT = 10000;

  /** Phy layer */
  private HardwareAbstractionLayer hardwareAbstractionLayer;

  /** XBEE frame handler */
  private XBeePacketHandler        xbeePacketHandler;

  /** Local 802.15.4 address */
  private byte[]                   localAddress                 = null;

  /** Optional listener for received SMEP packets */
  private ISMEPPacketListener      smepPacketListener;

  /** Optional listener for phy events */
  private IPhyStatusListener       phyStatusListener;

  /** Optional listener for xbee events */
  private IXBeeEventListener       xbeeEventListener;

  /** Connection time */
  private long                     lastXBeeRequest              = 0;

  /** Flag that a request is pending */
  private boolean                  pendingXBeeRequest           = false;

  /**
   * Creates a new instance of XBeeManager.
   * 
   * @param hardwareAbstractionLayer
   */
  public XBeeManager(HardwareAbstractionLayer hardwareAbstractionLayer)
  {
    this.hardwareAbstractionLayer = hardwareAbstractionLayer;

    // create network layer
    xbeePacketHandler = new XBeePacketHandler();

    // wire classes
    hardwareAbstractionLayer.setRawDataReceiveListener(xbeePacketHandler);
    xbeePacketHandler.setRawDataSender(hardwareAbstractionLayer);
    xbeePacketHandler.setSmepPacketListener(this);
    xbeePacketHandler.setXbeeEventListener(this);

    hardwareAbstractionLayer.setPhyStatusListener(this);

  }

  /**
   * Retrieves the value of xbeePacketHandler.
   * 
   * @return The value of xbeePacketHandler
   */
  public XBeePacketHandler getXbeePacketHandler()
  {
    return xbeePacketHandler;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener#phyAvailable(java.lang.Object)
   */
  public void phyAvailable(Object sender)
  {
    if (phyStatusListener != null)
    {
      phyStatusListener.phyAvailable(sender);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener#phyClosed(java.lang.Object)
   */
  public void phyClosed(Object sender)
  {
    if (phyStatusListener != null)
    {
      phyStatusListener.phyClosed(sender);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeLocalAddressRetrieved(java.lang.Object)
   */
  public void xbeeLocalAddressRetrieved(INetworkStatus sender)
  {
    localAddress = sender.getLocalAddress();
    pendingXBeeRequest = false;
    if (xbeeEventListener != null)
    {
      xbeeEventListener.xbeeLocalAddressRetrieved(sender);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#atCommandResponseReceived(int, java.lang.String, int, byte[])
   */
  public void xbeeATCommandResponseReceived(int frameID, String command, int status, byte[] value)
  {
    pendingXBeeRequest = false;
    if (xbeeEventListener != null)
    {
      xbeeEventListener.xbeeATCommandResponseReceived(frameID, command, status, value);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeTXStatusReceived(int, int)
   */
  public void xbeeTXStatusReceived(int frameID, int status)
  {
    pendingXBeeRequest = false;
    if (xbeeEventListener != null)
    {
      xbeeEventListener.xbeeTXStatusReceived(frameID, status);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeModemStatusReceived(int)
   */
  public void xbeeModemStatusReceived(int status)
  {
    if (xbeeEventListener != null)
    {
      xbeeEventListener.xbeeModemStatusReceived(status);
    }

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketSender#sendSMEPPacket(de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket)
   */
  public void sendSMEPPacket(SMEPPacket packet)
  {
    Portable.println("Send SMEP packet to " + Integer.toHexString(packet.getDestinationAddressAsInt()));
    // forward to XBee handler which creates the appropriate frame structure
    try
    {
      if (!(packet.getDestinationAddressAsInt() == XBeePacketHandler.BROADCAST_ADDRESS))
      {
        pendingXBeeRequest = true;
        lastXBeeRequest = Portable.currentTimeMillis();
      }
      xbeePacketHandler.sendData(null, packet.getDestinationAddress(), packet.toByteArray(), null);
    } catch (Exception e)
    {
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener#smepPacketReceived(de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket)
   */
  public void smepPacketReceived(SMEPPacket packet)
  {
    Portable.println(packet.toString());
    if (smepPacketListener != null)
    {
      smepPacketListener.smepPacketReceived(packet);
    }
  }

  /**
   * Retrieves the value of smepPacketListener.
   * 
   * @return The value of smepPacketListener
   */
  public ISMEPPacketListener getSMEPPacketListener()
  {
    return smepPacketListener;
  }

  /**
   * Sets the new value for smepPacketListener.
   * 
   * @param smepPacketListener
   *          The new value for smepPacketListener
   */
  public void setSMEPPacketListener(ISMEPPacketListener smepPacketListener)
  {
    this.smepPacketListener = smepPacketListener;
  }

  /**
   * Retrieves the value of phyStatusListener.
   * 
   * @return The value of phyStatusListener
   */
  public IPhyStatusListener getPhyStatusListener()
  {
    return phyStatusListener;
  }

  /**
   * Sets the new value for phyStatusListener.
   * 
   * @param phyStatusListener
   *          The new value for phyStatusListener
   */
  public void setPhyStatusListener(IPhyStatusListener phyStatusListener)
  {
    this.phyStatusListener = phyStatusListener;
  }

  /**
   * Retrieves the value of xbeeEventListener.
   * 
   * @return The value of xbeeEventListener
   */
  public IXBeeEventListener getXbeeEventListener()
  {
    return xbeeEventListener;
  }

  /**
   * Sets the new value for xbeeEventListener.
   * 
   * @param xbeeEventListener
   *          The new value for xbeeEventListener
   */
  public void setXbeeEventListener(IXBeeEventListener xbeeEventListener)
  {
    this.xbeeEventListener = xbeeEventListener;
  }

  private void sendATCommand(String command, byte[] value)
  {
    pendingXBeeRequest = true;
    lastXBeeRequest = Portable.currentTimeMillis();
    try
    {
      xbeePacketHandler.sendATCommand(command, value);
    } catch (Exception e)
    {
    }
  }

  /** Checks whether the XBee module can send and receive data. */
  public boolean isInitialized()
  {
    return hardwareAbstractionLayer.isConnected() && localAddress != null;
  }

  /**
   * Retrieves the value of pendingXBeeRequest.
   * 
   * @return The value of pendingXBeeRequest
   */
  public boolean isPendingXBeeRequest()
  {
    return pendingXBeeRequest;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    // set channel to nonsense value to disable packet reception
    if (hardwareAbstractionLayer.isConnected() && !pendingXBeeRequest)
    {
      // request address
      if (localAddress == null)
      {
        //        Portable.println("Request local address");
        sendATCommand("MY", null);
      }
    }

    if (pendingXBeeRequest && Portable.currentTimeMillis() - lastXBeeRequest > XBEE_PACKET_RESPONSE_TIMEOUT)
    {
      Portable.println("XBee request timed out");
      pendingXBeeRequest = false;
    }
  }

  /**
   * Retrieves the value of localAddress.
   * 
   * @return The value of localAddress
   */
  public byte[] getLocalAddress()
  {
    return localAddress;
  }

  /** Terminates the xbee manager and the underlying HAL. */
  public void terminate()
  {
    hardwareAbstractionLayer.terminate();
  }

}

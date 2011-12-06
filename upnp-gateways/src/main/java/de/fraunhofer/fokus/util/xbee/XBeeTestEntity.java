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

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.ByteArrayHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.network.listener.INetworkStatus;
import de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener;
import de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener;
import de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket;
import de.fraunhofer.fokus.upnp.util.threads.EventThread;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;
import de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener;
import de.fraunhofer.fokus.upnp.util.threads.KeyboardThread;
import de.fraunhofer.fokus.util.hal.SerialHardwareAbstractionLayer;

/**
 * This class opens a serial port to send and receive SMEP packets. Used to access 802.15.4 sensor nodes.
 * 
 * @author Alexander Koenig
 */
public class XBeeTestEntity implements
  ISMEPPacketListener,
  IEventListener,
  IPhyStatusListener,
  IXBeeEventListener,
  IKeyboardThreadListener,
  IKeyListener
{
  private static final byte[]            TEST_ANNOUNCEMENT  = {
      BinaryUPnPConstants.UnitTypeDeviceAnnouncement, 0,

      BinaryUPnPConstants.UnitTypeSDLVersion, 2, 1, 0,

      BinaryUPnPConstants.UnitTypeDeviceDescriptionDate, 5, 7, (byte)217, 2, 24, 0,

      BinaryUPnPConstants.UnitTypeDeviceID, 4, 0, 0, (byte)255, (byte)254,

      BinaryUPnPConstants.UnitTypeDeviceType, 1, BinaryUPnPConstants.DeviceTypeEnergyMeasurement
                                                            };

  private SerialHardwareAbstractionLayer serialHardwareAbstractionLayer;

  private XBeeManager                    xbeeManager;

  private EventThread                    eventThread;

  private boolean                        sendPackets        = false;

  private long                           lastPacketSendTime = 0;

  /**
   * Creates a new instance of XBeeTestEntity.
   * 
   * @param commPort
   */
  public XBeeTestEntity(String commPort)
  {
    serialHardwareAbstractionLayer = new SerialHardwareAbstractionLayer(commPort);

    xbeeManager = new XBeeManager(serialHardwareAbstractionLayer);
    xbeeManager.setPhyStatusListener(this);
    xbeeManager.setXbeeEventListener(this);
    xbeeManager.setSMEPPacketListener(this);
    eventThread = new EventThread("XBeeTestEntity");

    eventThread.register(xbeeManager);
    eventThread.register(this);

    eventThread.start();

    KeyboardThread keyboardThread = new KeyboardThread(this, "XBee Test");
    keyboardThread.setKeyListener(this);

    Portable.println("  Type <2> to enable/disable debug packets");
  }

  public static void main(String[] args)
  {
    new XBeeTestEntity(args.length == 0 ? "COM1" : args[0]);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener#phyAvailable(java.lang.Object)
   */
  public void phyAvailable(Object sender)
  {
    Portable.println("Phy available");
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener#phyClosed(java.lang.Object)
   */
  public void phyClosed(Object sender)
  {
  }

  public void xbeeLocalAddressRetrieved(INetworkStatus sender)
  {
    Portable.println("Local address available");
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#atCommandResponseReceived(int, java.lang.String, int, byte[])
   */
  public void xbeeATCommandResponseReceived(int frameID, String command, int status, byte[] value)
  {
    if (xbeeManager.isInitialized())
    {
      Portable.println("XBee module initialized");
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeTXStatusReceived(int, int)
   */
  public void xbeeTXStatusReceived(int frameID, int status)
  {
    Portable.println("Packet sent");
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.IXBeeEventListener#xbeeModemStatusReceived(int)
   */
  public void xbeeModemStatusReceived(int status)
  {

  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.smep.ISMEPPacketListener#smepPacketReceived(de.fraunhofer.fokus.upnp.util.network.smep.SMEPPacket)
   */
  public void smepPacketReceived(SMEPPacket smepPacket)
  {
    //    byte[] smepPayload = smepPacket.getUpperLayerData();
    //    Portable.println("Received XBee data packet (" + smepPayload.length + " bytes upper layer data)");
    //    Portable.println("  Source:     " + Integer.toHexString(smepPacket.getSourceAddressAsInt()));
    //    Portable.println("  Destination:" + Integer.toHexString(smepPacket.getDestinationAddressAsInt()));
    //    Portable.println("  RSSI:       " + "-" + smepPacket.getProperties().get("RSSI") + " dBm");
    //    Portable.println("  Payload:    " + StringHelper.byteArrayToMACString(smepPayload));
    //    if (smepPacket.getUpperLayerData().length == 5)
    //    {
    //      Portable.println("  Payload:    " + StringHelper.byteArrayToString(smepPacket.getUpperLayerData()));
    //    } else
    //    {
    //      Portable.println("  Payload:    " + BinaryUPnPConstants.toDebugString(smepPayload));
    //    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (xbeeManager.isInitialized() && !xbeeManager.isPendingXBeeRequest() && sendPackets &&
      Portable.currentTimeMillis() - lastPacketSendTime > 1000)
    {
      lastPacketSendTime = Portable.currentTimeMillis();

      //      Portable.println("Send test packet");
      SMEPPacket smepPacket = new SMEPPacket(20, 20, TEST_ANNOUNCEMENT);
      smepPacket.setDestinationAddress(ByteArrayHelper.uint16ToByteArray(XBeePacketHandler.BROADCAST_ADDRESS));

      xbeeManager.sendSMEPPacket(smepPacket);
    }

  }

  public void keyEvent(int code)
  {
    if (code == '2')
    {
      sendPackets = !sendPackets;
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener#terminateEvent()
   */
  public void terminateEvent()
  {
    terminate();
  }

  /** Terminates the forwarder module for the TCP tunnel. */
  public void terminate()
  {
    eventThread.terminate();
    xbeeManager.terminate();
  }

}

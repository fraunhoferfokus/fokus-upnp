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
package de.fraunhofer.fokus.lsf.core.device;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class is used to send and receive binary UPnP messages for an associated device.
 * 
 * @author Alexander Koenig
 */
public class BinaryDeviceMessageManagement extends Thread
{

  /** Associated device */
  private BinaryDevice                          binaryDevice;

  /** Manager for available network interfaces */
  private BinaryDeviceSocketStructureManagement socketStructureManagement;

  private boolean                               terminateThread = false;

  private boolean                               terminated      = false;

  /**
   * Creates a new instance of BinaryDeviceMessageManagement.
   * 
   * @param binaryDevice
   */
  public BinaryDeviceMessageManagement(BinaryDevice binaryDevice)
  {
    super("BinaryDevice.MessageManagement");
    this.binaryDevice = binaryDevice;

    socketStructureManagement = new BinaryDeviceSocketStructureManagement(null, null);
    socketStructureManagement.initHostAddressSocketStructures();

    start();
  }

  /**
   * Retrieves the value of socketStructureManagement.
   * 
   * @return The value of socketStructureManagement
   */
  public BinaryDeviceSocketStructureManagement getSocketStructureManagement()
  {
    return socketStructureManagement;
  }

  /** Reads messages */
  public void run()
  {
    while (!terminateThread)
    {
      for (int i = 0; i < socketStructureManagement.getSocketStructureCount(); i++)
      {
        BinaryDeviceHostAddressSocketStructure currentSocketStructure =
          (BinaryDeviceHostAddressSocketStructure)socketStructureManagement.getSocketStructure(i);

        BinaryMessageObject message = null;
        // read search messages
        do
        {
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getDiscoverySocket(), 10);
          if (message != null)
          {
            // Portable.println("Discovery: Received multicast discovery message");
            binaryDevice.processDiscoveryMessage(message, currentSocketStructure);
          }
        } while (message != null);
        // read search reply messages
        do
        {
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getDescriptionSocket(), 10);
          if (message != null)
          {
            Portable.println("Description: Received description request from " +
              IPHelper.toString(message.getSourceAddress()) + " with " +
              BinaryUPnPConstants.toDebugString(message.getBody()));
            binaryDevice.processDescriptionMessage(message, currentSocketStructure);
          }
        } while (message != null);
        do
        {
          // read control messages
          message = SocketHelper.readBinaryMessage(null, currentSocketStructure.getControlSocket(), 10);
          if (message != null)
          {
            binaryDevice.processControlMessage(message, currentSocketStructure);
          }
        } while (message != null);
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the manager */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(10);
    }
    socketStructureManagement.terminate();
  }

}

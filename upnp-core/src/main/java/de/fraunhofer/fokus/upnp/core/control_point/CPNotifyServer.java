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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is used by control points to receive NOTIFY messages from devices.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class CPNotifyServer implements IEventListener
{

  /** SSDPServer logger */
  static Logger                  logger = Logger.getLogger("upnp.ssdp");

  private ControlPoint           controlPoint;

  private CPSSDPMessageProcessor notifyMessageProcessor;

  /**
   * Creates a new instance of CPNotifyServer.
   * 
   * @param controlPoint
   *          The associated control point
   * @param processor
   *          The listener for incoming NOTIFY messages
   */
  public CPNotifyServer(ControlPoint controlPoint, CPSSDPMessageProcessor processor)
  {
    this.controlPoint = controlPoint;
    this.notifyMessageProcessor = processor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    // check all associated multicast sockets for received packets
    Vector socketStructures = controlPoint.getSocketStructures();
    for (int i = 0; i < socketStructures.size(); i++)
    {
      ControlPointHostAddressSocketStructure socketStructure =
        (ControlPointHostAddressSocketStructure)socketStructures.elementAt(i);

      // stay to one socket until all pending packets were read
      boolean packetFound = true;
      while (packetFound)
      {
        BinaryMessageObject message =
          SocketHelper.readBinaryMessage(null,
            socketStructure.getSSDPMulticastSocket(),
            SocketHelper.DEFAULT_SOCKET_TIMEOUT);

        if (message != null)
        {
          notifyMessageProcessor.processMessage(socketStructure, message);
        } else
        {
          packetFound = false;
        }
      }
    }
  }

}

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
package de.fraunhofer.fokus.upnp.ssdp;

import java.net.DatagramSocket;

import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;

/**
 * This class encapsulates a M-SEARCH response message.
 * 
 * @author Alexander Koenig
 */
public class MSearchResponseMessage
{

  private DatagramSocket      responseSocket;

  private BinaryMessageObject responsePacket;

  private long                responseTime;

  private long                sendCount = 0;

  public MSearchResponseMessage(DatagramSocket responseSocket, BinaryMessageObject response, long mxValue)
  {
    this.responseSocket = responseSocket;
    responsePacket = response;
    // spread responses over time
    responseTime = System.currentTimeMillis() + (long)(mxValue * 1000 * Math.random());
  }

  /**
   * Retrieves the responsePacket.
   * 
   * @return The responsePacket
   */
  public BinaryMessageObject getResponsePacket()
  {
    return responsePacket;
  }

  /**
   * Retrieves the responseSocket.
   * 
   * @return The responseSocket
   */
  public DatagramSocket getResponseSocket()
  {
    return responseSocket;
  }

  /**
   * Retrieves the responseTime.
   * 
   * @return The responseTime
   */
  public long getResponseTime()
  {
    return responseTime;
  }

  /**
   * Retrieves the sendCount.
   * 
   * @return The sendCount
   */
  public long getSendCount()
  {
    return sendCount;
  }

  /**
   * Increments the sendCount.
   * 
   */
  public void incSendCount()
  {
    sendCount++;
  }

}

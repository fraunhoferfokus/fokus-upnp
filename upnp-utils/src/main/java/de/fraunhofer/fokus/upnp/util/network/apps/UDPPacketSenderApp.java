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
package de.fraunhofer.fokus.upnp.util.network.apps;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class sends UDP packets to a destination address in specified intervals for 10 seconds.
 * 
 * @author Alexander Koenig
 */
public class UDPPacketSenderApp
{

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String[] args)
  {
    if (args.length < 2)
    {
      Portable.println("Wrong number of arguments. java UDPPacketSenderApp destination interval");
    }
    long time = System.currentTimeMillis();
    int count = 0;
    try
    {
      InetSocketAddress destinationAddress = IPHelper.toSocketAddress(args[0]);
      int interval = StringHelper.stringToIntegerDef(args[1], 1000);

      Portable.println("Send packets to " + IPHelper.toString(destinationAddress) + " every " + interval +
        " milliseconds");
      DatagramSocket socket = new DatagramSocket();
      while (System.currentTimeMillis() - time < 10000)
      {
        int length = (int)(Math.random() * 1480);
        // int length = 700;
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; i++)
        {
          data[i] = (byte)(Math.random() * 255);
        }
        DatagramPacket packet = new DatagramPacket(data, data.length, destinationAddress);
        count++;
        socket.send(packet);
        Portable.println("Packet sent");
        ThreadHelper.sleep(interval);
      }
      socket.close();
    } catch (Exception ex)
    {
      System.out.println("Error sending packets: " + ex.getMessage());
    }
    System.out.println(count + " packets sent.");
  }

}

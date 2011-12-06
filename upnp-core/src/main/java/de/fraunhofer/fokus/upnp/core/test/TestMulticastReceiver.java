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
package de.fraunhofer.fokus.upnp.core.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author icu
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public class TestMulticastReceiver
{
  public static void main(String[] args)
  {
    try
    {
      MulticastSocket SSDPMSocket = new MulticastSocket(1900);

      // SSDPMSocket.setLoopbackMode(true);
      // System.err.println("getLoopbackMode"+SSDPMSocket.getLoopbackMode());
      InetAddress ia = InetAddress.getByName("239.255.255.250");
      SSDPMSocket.joinGroup(ia);

      byte[] buf = new byte[2000];
      DatagramPacket recv = new DatagramPacket(buf, buf.length);
      System.err.println("before receive");
      SSDPMSocket.receive(recv);

      byte[] SSDPData = recv.getData();
      String header = new String(SSDPData, 0, recv.getLength());
      System.err.println("receive" + header);
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}

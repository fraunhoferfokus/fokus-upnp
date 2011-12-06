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
import java.net.InetAddress;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;
import de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener;
import de.fraunhofer.fokus.upnp.util.threads.KeyboardThread;

/**
 * This class answers UDP requests with the IP of the caller.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class MyIPApp extends Thread implements IKeyboardThreadListener, IKeyListener
{

  private boolean        terminateThread         = false;

  private boolean        terminated              = false;

  private static int     Port                    = 65535;

  private DatagramSocket socket                  = null;

  private KeyboardThread keyboardThread          = null;

  private int            receivedPacketsTotal    = 0;

  private long           lastUpdate              = 0;

  private int            receivedPacketsInterval = 0;

  private int            load[]                  = new int[30];

  /**
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    if (args.length > 0)
    {
      Port = StringHelper.stringToIntegerDef(args[0], Port);
      Port = Math.min(Math.max(1, Port), 65535);
    }
    Portable.println("Try to open UDP socket on port " + Port);
    new MyIPApp();
  }

  public MyIPApp()
  {
    try
    {
      socket = new DatagramSocket(Port);
      socket.setSoTimeout(100);
    } catch (Exception e)
    {
      Portable.println("Could not open socket: " + e.getMessage());
      System.exit(1);
    }
    Portable.println("Opened UDP socket on port " + socket.getLocalPort());

    keyboardThread = new KeyboardThread(this, "MyIPApp");
    keyboardThread.setKeyListener(this);
    start();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == '1')
    {
      Portable.println(StringHelper.getDivider());
      Portable.println(receivedPacketsTotal + " packets received since startup");
      Portable.println("Current load:");
      for (int i = 0; i < load.length; i++)
      {
        Portable.print(load[i] + " ");
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener#terminateEvent()
   */
  public void terminateEvent()
  {
    terminate();
  }

  public void run()
  {
    Portable.println("  Press <1> to show statistics...");
    while (!terminateThread)
    {
      if (Portable.currentTimeMillis() - lastUpdate > 60000)
      {
        // shift load measurements
        for (int i = load.length - 1; i > 0; i--)
        {
          load[i] = load[i - 1];
        }
        load[0] = receivedPacketsInterval;
        receivedPacketsInterval = 0;

        lastUpdate = Portable.currentTimeMillis();
      }

      try
      {
        BinaryMessageObject messageObject = SocketHelper.readBinaryMessage(null, socket, 100);
        // answer each received request with the source IP address
        if (messageObject != null)
        {
          receivedPacketsInterval++;
          receivedPacketsTotal++;
          InetAddress sourceAddress = messageObject.getSourceAddress().getAddress();

          byte[] buffer = new byte[sourceAddress.getAddress().length];
          Portable.arraycopy(messageObject.getSourceAddress().getAddress().getAddress(), 0, buffer, 0, buffer.length);

          DatagramPacket packet = new DatagramPacket(buffer, buffer.length, messageObject.getSourceAddress());
          socket.send(packet);
        }
      } catch (Exception e)
      {
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(30);
    }
  }

}

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
package de.fraunhofer.fokus.upnp.util.swing.logger;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * This class can be used to receive text log messages from multiple socket addresses.
 * 
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class GUILogger implements Runnable
{
  private JFrame    topFrame;

  private Vector    socketList      = new Vector();

  private JTextArea loggerTextArea;

  private boolean   terminateThread = false;

  private boolean   terminated      = false;

  public GUILogger(String[] args)
  {
    initGUI();

    if (args != null && args.length > 0)
    {
      StringTokenizer tokenizer = new StringTokenizer(args[0], ",; ");
      while (tokenizer.hasMoreTokens())
      {
        try
        {
          String source = tokenizer.nextToken();
          InetSocketAddress multicastAddress = null;
          int port = -1;
          DatagramSocket socket = null;
          if (source.indexOf(":") != -1)
          {
            multicastAddress = IPHelper.toSocketAddress(source);
            port = multicastAddress.getPort();
            socket = new MulticastSocket(port);
            ((MulticastSocket)socket).joinGroup(multicastAddress.getAddress());
            System.out.println("Listen on " + IPHelper.toString(multicastAddress));
          } else
          {
            port = Integer.parseInt(source);
            socket = new DatagramSocket(port);
            System.out.println("Listen on " + port);
          }
          socketList.add(socket);
        } catch (Exception e)
        {
        }
      }
    }
    new Thread(this).start();
  }

  private void initGUI()
  {
    try
    {
      // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e)
    {
    }

    // create the top-level container and add contents to it
    topFrame = new JFrame("Logger");

    // finish setting up the frame and show it
    topFrame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        terminateThread = true;
        while (!terminated)
        {
          ThreadHelper.sleep(50);
        }

        System.exit(0);
      }
    });
    BoxLayout layoutFrame = new BoxLayout(topFrame.getContentPane(), BoxLayout.Y_AXIS);
    topFrame.getContentPane().setLayout(layoutFrame);

    loggerTextArea = new JTextArea();
    loggerTextArea.setFont(new Font("Courier New", 0, 12));

    topFrame.getContentPane().add(new JScrollPane(loggerTextArea));
    topFrame.pack();
    topFrame.setSize(800, 600);
    topFrame.setVisible(true);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    new GUILogger(args);
  }

  public void run()
  {
    while (!terminateThread)
    {
      for (int i = 0; i < socketList.size(); i++)
      {
        DatagramSocket socket = (DatagramSocket)socketList.elementAt(i);
        BinaryMessageObject message = SocketHelper.readBinaryMessage(null, socket, 10);
        if (message != null)
        {
          System.out.println("Message received");
          while (loggerTextArea.getLineCount() > 500)
          {
            loggerTextArea.replaceRange("", 0, 200);
          }

          loggerTextArea.append(message.getSourceAddress().getAddress().getHostAddress() + " > " +
            socket.getLocalPort() + "\r\n" + "                        " +
            StringHelper.byteArrayToAsciiDebugString(message.getBody()) + "\r\n" + "                        [" +
            StringHelper.byteArrayToHexDebugString(message.getBody()) + "]\r\n");
        }
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }
}

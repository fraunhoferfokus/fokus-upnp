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
package de.fraunhofer.fokus.upnp.core.examples.chat;

import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class creates and destroys a chat device in regular intervals.
 * 
 * @author Alexander Koenig
 */
public class ToggleChatEntity extends Thread
{

  private int                  toggleInterval  = 5000;

  private boolean              terminateThread = false;

  private ChatEntity           chatEntity;

  private long                 lastToggleTime  = 0;

  private UPnPStartupConfiguration startupConfiguration;

  /**
   * Starts the application that periodically creates and destroys a Chat device.
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    new ToggleChatEntity();
  }

  /**
   * Creates a new instance of ToggleChat.
   * 
   */
  public ToggleChatEntity()
  {
    startupConfiguration = new UPnPStartupConfiguration(FileHelper.getResourceDirectoryName() + "ToggleChatEntity.xml");
    startupConfiguration.setStartKeyboardThread(false);

    toggleInterval = startupConfiguration.getNumericProperty("ToggleInterval", toggleInterval);
    System.out.println("Toggle interval is " + toggleInterval / 1000 + " seconds");

    start();
  }

  public void run()
  {
    System.out.println();
    System.out.println("  Start keyboard thread...");
    System.out.println("  Type <e> or <q> to exit the application");
    System.out.println();
    while (!terminateThread)
    {
      try
      {
        if (System.in.available() > 0)
        {
          int key = System.in.read();
          if (key == 'q' || key == 'e')
          {
            System.out.println("  Shutdown keyboard thread...");
            terminateThread = true;
          }
        }
        if (System.currentTimeMillis() - lastToggleTime > toggleInterval)
        {
          lastToggleTime = System.currentTimeMillis();
          if (chatEntity == null)
          {
            chatEntity = new ChatEntity(startupConfiguration);
          } else
          {
            chatEntity.terminate();
            chatEntity = null;
          }
        }

        Thread.sleep(50);
      } catch (Exception ex)
      {
      }
    }
    if (chatEntity != null)
    {
      chatEntity.terminate();
      chatEntity = null;
    }
  }

}

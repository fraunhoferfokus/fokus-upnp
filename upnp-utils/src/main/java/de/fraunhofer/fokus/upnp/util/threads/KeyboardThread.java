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
package de.fraunhofer.fokus.upnp.util.threads;

import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class can be used to start a keyboard thread that checks the keyboard input for exit commands.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class KeyboardThread extends Thread
{

  /** Listener for exit event */
  private IKeyboardThreadListener keyboardThreadListener;

  /** Optional listener for other keys */
  private IKeyListener            keyListener;

  /** Flag to terminate the entity */
  private boolean                 terminateThread = false;

  /** Flag that the entity thread has been terminated */
  private boolean                 terminated      = false;

  /**
   * Creates a new instance of KeyboardThread. The thread is started immediately.
   * 
   * @param keyboardThreadListener
   *          The listener for the quit event
   * @param name
   *          Name for the Thread
   * 
   */
  public KeyboardThread(IKeyboardThreadListener keyboardThreadListener, String name)
  {
    super(name);
    this.keyboardThreadListener = keyboardThreadListener;
    // start immediately
    start();
  }

  /**
   * @return the keyListener
   */
  public IKeyListener getKeyListener()
  {
    return keyListener;
  }

  /**
   * @param keyListener
   *          the keyListener to set
   */
  public void setKeyListener(IKeyListener keyListener)
  {
    this.keyListener = keyListener;
  }

  /** Starts a keyboard thread for termination */
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
          } else
          {
            if (keyListener != null)
            {
              keyListener.keyEvent(key);
            }
          }
        }
        Thread.sleep(50);
      } catch (Exception ex)
      {
        Portable.println("Exception: " + ex.getMessage());
      }
    }
    terminated = true;
    // terminate listener
    if (keyboardThreadListener != null)
    {
      keyboardThreadListener.terminateEvent();
    }
  }

  /**
   * Retrieves the terminated flag.
   * 
   * @return The terminated flag
   */
  public boolean isTerminated()
  {
    return terminated;
  }

  /**
   * Sets the terminateThread.
   */
  public void terminate()
  {
    terminateThread = true;
  }

}

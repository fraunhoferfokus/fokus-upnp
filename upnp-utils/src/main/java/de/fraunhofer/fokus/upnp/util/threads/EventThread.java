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

import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.ThreadHelper;

/**
 * This class bundles all activities that must be called regularly for devices to work properly.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class EventThread extends Thread
{

  public static int EVENT_THREAD_SLEEP_TIME = 50;

  private Vector    listeners               = new Vector();

  private Vector    addListeners            = new Vector();

  private Vector    removeListeners         = new Vector();

  private Object    lock                    = new Object();

  private boolean   terminateThread         = false;

  private boolean   terminated              = false;

  /**
   * Creates a new instance of EventThread. The thread is not started.
   * 
   * @param name
   */
  public EventThread(String name)
  {
    super(name);
  }

  /** Registers a new listener for events */
  public void register(IEventListener listener)
  {
    synchronized(lock)
    {
      if (addListeners.indexOf(listener) == -1)
      {
        addListeners.add(listener);
      }
    }
  }

  /** Unregisters a listener for events */
  public void unregister(IEventListener listener)
  {
    synchronized(lock)
    {
      if (removeListeners.indexOf(listener) == -1)
      {
        removeListeners.add(listener);
      }
    }
  }

  public void run()
  {
    while (!terminateThread)
    {
      synchronized(lock)
      {
        // handle add and remove lists
        for (int i = 0; i < addListeners.size(); i++)
        {
          IEventListener currentListener = (IEventListener)addListeners.elementAt(i);
          if (listeners.indexOf(currentListener) == -1)
          {
            listeners.add(currentListener);
          }
        }
        addListeners.clear();
        for (int i = 0; i < removeListeners.size(); i++)
        {
          IEventListener currentListener = (IEventListener)removeListeners.elementAt(i);
          listeners.remove(currentListener);
        }
        removeListeners.clear();
      }
      // trigger events
      for (int i = 0; i < listeners.size(); i++)
      {
        // long triggerStart = System.currentTimeMillis();
        ((IEventListener)listeners.elementAt(i)).triggerEvents();
        // if (System.currentTimeMillis() - triggerStart > 100)
        // {
        // System.out.println(Thread.currentThread().toString() + ": Event triggering " + i + ": " +
        // (System.currentTimeMillis() - triggerStart));
        // }

      }
      ThreadHelper.sleep(EVENT_THREAD_SLEEP_TIME);
    }
    terminated = true;
  }

  /** Terminates the thread */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(30);
    }
  }

}

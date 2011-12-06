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
package de.fraunhofer.fokus.upnp.util;

import java.util.Hashtable;

/**
 * This class implements a critical section. Used for easy port to .NET, where synchronized{} is not available.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class CriticalSection
{

  /** Static lock */
  private static Object    lock                      = new Object();

  /** Table with ref counter for each object */
  private static Hashtable refCounterFromObjectTable = new Hashtable();

  /** Table with active thread for each object */
  private static Hashtable threadFromObjectTable     = new Hashtable();

  /** Enters a critical section. Waits until entry is possible. */
  public static void enter(Object object)
  {
    //    Portable.println("Try enter: " + object);
    while (true)
    {
      synchronized(lock)
      {
        // first Enter for object
        if (!refCounterFromObjectTable.containsKey(object))
        {
          refCounterFromObjectTable.put(object, new Integer(1));
          threadFromObjectTable.put(object, Thread.currentThread());
          return;
        }
        // consecutive Enter
        // check if same thread
        if (threadFromObjectTable.get(object) == Thread.currentThread())
        {
          int refCounter = ((Integer)refCounterFromObjectTable.get(object)).intValue();
          refCounterFromObjectTable.put(object, new Integer(refCounter + 1));
          return;
        }
      }
      // we are another thread trying to access the same critical section
      // wait for access
      ThreadHelper.sleep(1);
    }
  }

  /** Exits a critical section. */
  public static void exit(Object object)
  {
    //    Portable.println("Exit: " + object);
    synchronized(lock)
    {
      Integer refCounter = (Integer)refCounterFromObjectTable.get(object);
      if (refCounter == null)
      {
        Portable.println("CriticalSection:Exit called without Enter");
        return;
      }

      // last Exit, free critical section
      if (refCounter.intValue() == 1)
      {
        refCounterFromObjectTable.remove(object);
        threadFromObjectTable.remove(object);
      } else
      {
        refCounterFromObjectTable.put(object, new Integer(refCounter.intValue() - 1));
      }
    }
  }

}

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

import java.text.DecimalFormat;

import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is used to check the available memory in Java.
 * 
 * @author Alexander Koenig
 */
public class MemoryMonitor implements IEventListener
{

  /** Time interval between two measurements (100 min) */
  public static int     MEASURE_INTERVAL = 6000000;

  private static long   lastGC;

  private static Object lock             = new Object();

  private long          lastMeasure;

  /**
   * Creates a new thread.
   * 
   */
  public MemoryMonitor()
  {
    // first measurement after 10 seconds
    lastMeasure = System.currentTimeMillis() - MEASURE_INTERVAL + 10000;
  }

  public static String formatMemorySize(long memory)
  {
    String result = memory + " bytes";
    if (memory >= 1024)
    {
      result = new DecimalFormat("0.0").format(memory / 1024.0) + " KB";
    }
    if (memory >= 1024000)
    {
      result = new DecimalFormat("0.0").format(memory / 1048576.0) + " MB";
    }
    return result;
  }

  /** Prints the current memory stamp of the JVM to stdout */
  public static void showMemoryStamp()
  {
    System.out.println("MEMORY STAMP   : " + getMemoryStamp());
  }

  /** Returns a string with the current memory stamp of the JVM */
  public static String getMemoryStamp()
  {
    synchronized(lock)
    {
      if (System.currentTimeMillis() - lastGC > 5000)
      {
        lastGC = System.currentTimeMillis();
        System.gc();
      }

      long totalMem = Runtime.getRuntime().totalMemory();
      long freeMemory = Runtime.getRuntime().freeMemory();
      long maxMemory = Runtime.getRuntime().maxMemory();

      return formatMemorySize(totalMem - freeMemory) + " out of " + formatMemorySize(totalMem) + " used. (" +
        (100 - 100 * freeMemory / totalMem) + "%). " + "Max memory is " + formatMemorySize(maxMemory) +
        ". Thread count is " + Thread.activeCount();
    }
  }

  /** Returns the currently used memory relative to the max available memory */
  public static double getRelativeMemoryUsage()
  {
    long totalMem = Runtime.getRuntime().totalMemory();
    long freeMemory = Runtime.getRuntime().freeMemory();
    long maxMemory = Runtime.getRuntime().maxMemory();

    return (totalMem - freeMemory) / (1.0 * maxMemory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (System.currentTimeMillis() - lastMeasure > MEASURE_INTERVAL)
    {
      showMemoryStamp();
      lastMeasure = System.currentTimeMillis();
    }
  }

}

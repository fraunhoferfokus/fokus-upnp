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

import sun.misc.Perf;

/**
 * This package provides methods to measure times with high resolution.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class HighResTimerHelper
{

  private static Perf perf                   = Perf.getPerf();

  /** Used during performance evaluation */
  public static long  PERFORMANCE_CORRECTION = 0;

  /** Retrieves a time stamp for the current time */
  public static long getTimeStamp()
  {
    return perf.highResCounter();
  }

  /**
   * Calculates the time in microseconds between two timestamps.
   * 
   * 
   * @param timestamp1
   *          The earlier time stamp
   * @param timestamp2
   *          The later time stamp
   * @return
   */
  public static long getMicroseconds(long timestamp1, long timestamp2)
  {
    return (timestamp2 - timestamp1) * 1000000 / perf.highResFrequency();
  }

}

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

/**
 * This class provides common methods which are implemented differently for Java and .NET
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class Portable
{

  /** Returns the current time in milliseconds */
  public static long currentTimeMillis()
  {
    return System.currentTimeMillis();
  }

  /** Copies length bytes from src to dest. */
  public static void arraycopy(byte[] src, int srcPos, byte[] dest, int destPos, int length)
  {
    System.arraycopy(src, srcPos, dest, destPos, length);
  }

  /** Prints some text to standard out. */
  public static void println(String text)
  {
    System.out.println(text);
  }

  /** Prints some text to standard out. */
  public static void print(String text)
  {
    System.out.print(text);
  }

  /** Returns a random value between 0 and 1. */
  public static float random()
  {
    return (float)Math.random();
  }

}

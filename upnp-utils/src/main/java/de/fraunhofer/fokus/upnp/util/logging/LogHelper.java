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
package de.fraunhofer.fokus.upnp.util.logging;

/**
 * This class can be used to store log messages. Messages are forwarded to the registered logger or
 * to the console if no other logger is registered.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class LogHelper
{

  private static ConsoleLogger consoleLogger;

  private static ILogRef       logRef;

  static
  {
    consoleLogger = new ConsoleLogger();
    logRef = consoleLogger;
  }

  public static void debug(String message)
  {
    logRef.debug(message);
  }

  public static boolean doDebug()
  {
    return logRef.doDebug();
  }

  public static boolean doError()
  {
    return logRef.doError();
  }

  public static boolean doInfo()
  {
    return logRef.doInfo();
  }

  public static boolean doWarn()
  {
    return logRef.doWarn();
  }

  public static void error(String message)
  {
    logRef.error(message);
  }

  public static void info(String message)
  {
    logRef.info(message);
  }

  public static void warn(String message)
  {
    logRef.warn(message);
  }

  /** Registers a new logger */
  public static void setLogger(ILogRef logger)
  {
    if (logger == null)
    {
      logRef = consoleLogger;
    } else
    {
      logRef = logger;
    }

  }
}

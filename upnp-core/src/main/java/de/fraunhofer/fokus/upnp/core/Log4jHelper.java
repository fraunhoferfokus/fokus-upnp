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
package de.fraunhofer.fokus.upnp.core;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;

/**
 * This class provides helper methods for logging.
 * 
 * @author Alexander König
 * 
 */
public class Log4jHelper
{

  private static boolean initialized = false;

  /** Initializes the logger. */
  public static boolean initializeLogging()
  {
    try
    {
      // init log4j
      String log4jProperties = System.getProperty("log4jproperty", "log/log4j_local.properties");
      File file = new File(FileHelper.getBaseDirectoryName() + log4jProperties);
      if (!file.exists())
      {
        System.out.println("  Log4j configuration file " + file.getAbsolutePath() + " not found");
        return false;
      } else
      {
        PropertyConfigurator.configure(file.getAbsolutePath());
        System.out.println("Logging initialized from file.");
        printLogInfo();
        initialized = true;
        return true;
      }
    } catch (Exception e)
    {
      System.out.println("Error initializing logger: " + e.getMessage());
    }
    return false;
  }

  /** Initializes the logger with the class loader. */
  public static boolean initializeLoggingViaClassLoader(Class classInstance)
  {
    try
    {
      // init log4j
      String log4jProperties = System.getProperty("log4jproperty", "/log/log4j_local.properties");
      if (ResourceHelper.isAvailableViaClassloader(classInstance, log4jProperties))
      {
        InputStream inputStream = classInstance.getResourceAsStream(log4jProperties);

        Properties properties = new Properties();
        properties.load(inputStream);

        PropertyConfigurator.configure(properties);
        System.out.println("Logging initialized via class loader");
        printLogInfo();
        initialized = true;
        return true;
      }
      System.out.println("  Log4j configuration file " + log4jProperties + " not found via class loader");
    } catch (Exception e)
    {
      System.out.println("Error initializing logger: " + e.getMessage());
    }
    return false;
  }

  /** Debugs some log info to stdout */
  public static void printLogInfo()
  {
    if (Logger.getRootLogger().getLevel() == null)
    {
      System.out.println("  Logging is disabled");
    } else
    {
      System.out.println("  Log level is " + Logger.getRootLogger().getLevel());
    }
    Enumeration appenders = Logger.getRootLogger().getAllAppenders();
    if (appenders.hasMoreElements())
    {
      System.out.println("  Log to:");
    }
    while (appenders.hasMoreElements())
    {
      Appender appender = (Appender)appenders.nextElement();
      System.out.println("    " + appender.toString());
    }

  }

  /**
   * Retrieves the initialized.
   * 
   * @return The initialized
   */
  public static boolean isInitialized()
  {
    return initialized;
  }
}

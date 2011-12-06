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

import java.io.File;

import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class provides helper methods for files.
 * 
 * @author Alexander König
 * 
 */
public class FileHelper
{

  private static String resourceDirectoryName = null;

  private static String baseDirectoryName     = null;

  /** Creates a pseudo-UTF-8 file name if necessary. */
  public static String getAppropriateFileName(String fileName)
  {
    // with SVN, file names are transcoded automatically,
    // so this workaround is no longer needed
    // if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
    // {
    // return StringHelper.encodeUTF8String(fileName);
    // }
    // for newer linux distributions, UTF-8 is standard
    return fileName;
  }

  /**
   * Returns a file name, consisting of the basic file name plus the name of the local host plus .txt (e.g.,
   * basicFileName_silicium.txt).
   */
  public static String getHostBasedFileName(String basicFileName)
  {
    return basicFileName + "_" + IPHelper.getSimpleLocalHostName() + ".txt";
  }

  /** Tries to return the relative directory which contains resources. */
  public static String getResourceDirectoryName()
  {
    if (resourceDirectoryName != null)
    {
      return resourceDirectoryName;
    }

    // use base directory to find resource directory
    if (new File(getBaseDirectoryName() + "res").exists())
    {
      resourceDirectoryName = FileHelper.toValidDirectoryName(getBaseDirectoryName() + "res/");
    }
    if (resourceDirectoryName == null)
    {
      resourceDirectoryName = "";
    }
    return resourceDirectoryName;
  }

  /**
   * Tries to return the relative base directory that contains the res/ and log/ directories.
   * 
   * 
   * @return The relative base directory ending with a separator, e.g., ./
   */
  public static String getBaseDirectoryName()
  {

    if (baseDirectoryName != null)
    {
      return baseDirectoryName;
    }
    if (new File("res").exists())
    {
      baseDirectoryName = FileHelper.toValidDirectoryName("./");
    }
    if (new File("../res").exists())
    {
      baseDirectoryName = FileHelper.toValidDirectoryName("../");
    }
    if (baseDirectoryName == null)
    {
      baseDirectoryName = "";
    }
    return baseDirectoryName;
  }

  /** Tries to set the base directory that contains the res/ and log/ directories. */
  public static void setBaseDirectoryName(String directory)
  {
    if (new File(directory).exists())
    {
      baseDirectoryName = FileHelper.toValidDirectoryName(directory);
    }
  }

  /**
   * Returns the appropriate relative resource directory for a specific package.
   * 
   * 
   * @param className
   *          Name of a class in that package
   * 
   * @return
   */
  public static String getResourceDirectoryNameFromPackage(String className)
  {
    // remove common prefix
    if (className.startsWith("de.fraunhofer.fokus.upnp."))
    {
      className = className.substring("de.fraunhofer.fokus.upnp.".length());
    }
    // remove common prefix
    if (className.startsWith("de.fraunhofer.fokus.lsf."))
    {
      className = className.substring("de.fraunhofer.fokus.lsf.".length());
    }

    // get package name
    if (className.lastIndexOf('.') != -1)
    {
      className = className.substring(0, className.lastIndexOf('.'));
    }

    return FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + className.replace('.', '/'));
  }

  /**
   * Tries to find the most appropriate working directory for a given class and directory name. If directory is null, a
   * directory name based on the class name is built.
   * 
   * 
   * @param className
   * @param directory
   *          A default directory
   * 
   * @return
   */
  public static String tryFindWorkingDirectory(String className, String directory)
  {
    // use directory derived from class name if currently unknown
    if (directory == null)
    {
      directory = FileHelper.getResourceDirectoryNameFromPackage(className);
      // System.out.println("Use class derived directory: " + directory);

      return directory;
    }
    directory = FileHelper.toValidDirectoryName(directory);
    if (!new File(directory).exists())
    {
      // directory is invalid, try to shift relative to resource directory
      String relativeDirectory = FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + directory);

      // check if directory relative to common resources would exist
      if (new File(relativeDirectory).exists())
      {
        // System.out.println("Use directory relative to resource directory: " + relativeDirectory);
        return relativeDirectory;
      }
    }
    // no valid directory could be found, use default
    return directory;
  }

  /**
   * Creates a directory name containing valid separators for the OS environment and ending with a separator.
   */
  public static String toValidDirectoryName(String directory)
  {
    String result = directory != null ? directory : "";
    char separator = System.getProperty("file.separator").charAt(0);
    result = result.replace('\\', separator);
    result = result.replace('/', separator);
    // end with separator
    if (result.length() != 0 && !result.endsWith(System.getProperty("file.separator")))
    {
      result += System.getProperty("file.separator");
    }
    return result;
  }

  /**
   * Tries to find a startup configuration file for this entity.
   * 
   * 
   * @param shortClassName
   *          The simple name of the class
   * 
   * @return The most appropriate file name for startup configuration
   */
  public static String getStartupConfigurationName(String shortClassName)
  {
    // try local host name
    String startupFileName =
      FileHelper.getResourceDirectoryName() + shortClassName + "_" + IPHelper.getLocalHostName().toLowerCase() + ".xml";
    if (new File(startupFileName).exists())
    {
      return startupFileName;
    }

    // try simple local host name
    String simpleHostName = IPHelper.getSimpleLocalHostName().toLowerCase();
    startupFileName = FileHelper.getResourceDirectoryName() + shortClassName + "_" + simpleHostName + ".xml";
    if (new File(startupFileName).exists())
    {
      return startupFileName;
    }

    // try local host address
    startupFileName =
      FileHelper.getResourceDirectoryName() + shortClassName + "_" + IPHelper.getLocalHostAddressString() + ".xml";
    if (new File(startupFileName).exists())
    {
      return startupFileName;
    }

    // return generic startup file name
    return FileHelper.getResourceDirectoryName() + shortClassName + ".xml";
  }

  /**
   * Tries to find a startup configuration file for this entity via the class loader.
   * 
   * 
   * @param shortClassName
   *          The simple name of the class
   * 
   * @return A file name that is accessible via the class loader or null
   */
  public static String getStartupConfigurationNameViaClassLoader(Class classInstance, String shortClassName)
  {
    // try local host name
    String startupResourceName =
      ResourceHelper.getDefaultResourceDirectoryName() + shortClassName + "_" +
        IPHelper.getLocalHostName().toLowerCase() + ".xml";
    if (ResourceHelper.isAvailableViaClassloader(classInstance, startupResourceName))
    {
      return startupResourceName;
    }

    // try simple local host name
    String simpleHostName = IPHelper.getSimpleLocalHostName().toLowerCase();
    startupResourceName =
      ResourceHelper.getDefaultResourceDirectoryName() + shortClassName + "_" + simpleHostName + ".xml";
    if (ResourceHelper.isAvailableViaClassloader(classInstance, startupResourceName))
    {
      return startupResourceName;
    }

    // try local host address
    startupResourceName =
      ResourceHelper.getDefaultResourceDirectoryName() + shortClassName + "_" + IPHelper.getLocalHostAddressString() +
        ".xml";
    if (ResourceHelper.isAvailableViaClassloader(classInstance, startupResourceName))
    {
      return startupResourceName;
    }

    // try generic startup file name
    startupResourceName = ResourceHelper.getDefaultResourceDirectoryName() + shortClassName + ".xml";
    if (ResourceHelper.isAvailableViaClassloader(classInstance, startupResourceName))
    {
      return startupResourceName;
    }

    return null;
  }

}

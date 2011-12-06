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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class provides static methods to load and store resources via files or via the standard class loader.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class ResourceHelper
{

  /** Returns the standard directory name for resources in jar-files */
  public static String getDefaultResourceDirectoryName()
  {
    return "/res/";
  }

  /** Loads a string list from the file system. */
  public static Vector loadStringListFromFile(String fileName)
  {
    try
    {
      if (new File(fileName).exists())
      {
        return loadStringListFromInputStream(new FileInputStream(fileName));
      }
    } catch (Exception e)
    {
    }
    return new Vector();
  }

  /** Loads a string list via the standard class loader. */
  public static Vector loadStringListFromFile(Class classInstance, String resourceName)
  {
    return loadStringListFromInputStream(loadInputStreamViaClassloader(classInstance, resourceName));
  }

  /**
   * Loads a string list from an input stream.
   * 
   * 
   * @param inputStream
   *          The input stream.
   * 
   * @return A vector with all lines, ignoring comment lines (starting with #)
   */
  public static Vector loadStringListFromInputStream(InputStream inputStream)
  {
    Vector result = new Vector();
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

      String line;
      line = reader.readLine();
      while (line != null)
      {
        line = line.trim();
        // make sure there wasn`t just an unnecessary newline at the end...
        // ignore comments
        if (line.length() > 0 && line.charAt(0) != '#')
        {
          result.add(line);
        }
        line = reader.readLine();
      }
      reader.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
    return result;
  }

  /**
   * Loads a string resource via the standard class loader.
   * 
   * 
   * @param classInstance
   * @param resourceName
   * 
   * @return
   */
  public static String loadStringViaClassloader(Class classInstance, String resourceName)
  {
    byte[] result = loadByteArrayViaClassloader(classInstance, resourceName);
    if (result != null)
    {
      // treat byte array as UTF-8 string
      return StringHelper.byteArrayToUTF8String(result);
    }
    return null;
  }

  /**
   * Loads an input stream resource via the standard class loader.
   * 
   * 
   * @param classInstance
   * @param resourceName
   * 
   * @return
   */
  public static InputStream loadInputStreamViaClassloader(Class classInstance, String resourceName)
  {
    System.out.println("      Try to load resource via class loader: " + resourceName);
    try
    {
      return classInstance.getResourceAsStream(resourceName);

    } catch (Exception e)
    {
      System.out.println("Error loading resource via class loader: " + e.getMessage());

    }
    return null;
  }

  /**
   * Loads a byte array resource via the standard class loader.
   * 
   * 
   * @param classInstance
   * @param resourceName
   * 
   * @return
   */
  public static byte[] loadByteArrayViaClassloader(Class classInstance, String resourceName)
  {
    try
    {
      InputStream inputStream = loadInputStreamViaClassloader(classInstance, resourceName);

      // resource was found
      if (inputStream != null)
      {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        while (bytesRead != -1)
        {
          result.write(buffer, 0, bytesRead);
          bytesRead = inputStream.read(buffer);
        }
        // treat byte array as UTF-8 string
        return result.toByteArray();
      }
    } catch (Exception e)
    {
      System.out.println("Error reading input stream: " + e.getMessage());

    }
    return null;
  }

  /** Checks if a resource is available via the class loader. */
  public static boolean isAvailableViaClassloader(Class classInstance, String resourceName)
  {
    try
    {
      InputStream inputStream = classInstance.getResourceAsStream(resourceName);

      boolean result = inputStream != null;

      // System.out.println(" " + resourceName + " is available via class loader: " + result);
      // System.out.println(" Class loader is " + classInstance.getClassLoader().toString());

      inputStream = null;

      return result;
    } catch (Exception e)
    {
      System.out.println("Error checking resource via class loader: " + e.getMessage());
    }
    return false;
  }

  /** Stores a string list into a file. */
  public static void storeStringListToFile(String fileName, Vector content)
  {
    try
    {
      File file = new File(fileName);
      File parentFile = new File(file.getParent());

      if (!parentFile.exists())
      {
        parentFile.mkdirs();
      }
      storeStringListToOutputStream(new FileOutputStream(fileName), content);
    } catch (Exception ex)
    {
      System.out.println("Error storing string list: " + ex.getMessage());
    }
  }

  /** Stores a string list into a stream. */
  public static void storeStringListToOutputStream(OutputStream outputStream, Vector content)
  {
    String result = "";
    try
    {
      for (int i = 0; i < content.size(); i++)
      {
        result += content.elementAt(i).toString() + "\r\n";
      }
      outputStream.write(StringHelper.utf8StringToByteArray(result));
      outputStream.close();
    } catch (Exception ex)
    {
      Portable.println("Error storing list: " + ex.getMessage());
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
  public static String getResourcePathFromPackage(String className)
  {
    // remove common prefix
    if (className.startsWith("de.fraunhofer.fokus.upnp."))
    {
      className = className.substring("de.fraunhofer.fokus.upnp.".length());
    }

    // remove class name
    if (className.lastIndexOf('.') != -1)
    {
      className = className.substring(0, className.lastIndexOf('.'));
    }

    return getDefaultResourceDirectoryName() + className.replace('.', '/') + "/";
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

}

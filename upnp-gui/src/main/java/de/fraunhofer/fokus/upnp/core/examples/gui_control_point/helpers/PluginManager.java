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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPDevicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPServicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BasePlugin;
import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class manages GUI plugins for devices and services.
 * 
 * @author Alexander Koenig
 */
public class PluginManager
{

  private Hashtable cpServicePluginTable = new Hashtable();

  private Hashtable cpDevicePluginTable  = new Hashtable();

  private String    classPath;

  /**
   * Creates a new instance of PluginManager.
   * 
   * @param classPath
   *          The path to the classes (starting with packages)
   */
  public PluginManager(String classPath)
  {
    this.classPath = FileHelper.toValidDirectoryName(classPath);
    loadPlugins(this.classPath);
  }

  /** Returns an enumeration with all found device plugins */
  public Enumeration getCPDevicePlugins()
  {
    return cpDevicePluginTable.keys();
  }

  /** Returns the Java class for a certain device plugin for a new instantation. */
  public Class getCPDeviceClass(BaseCPDevicePlugin plugin)
  {
    try
    {
      return (Class)cpDevicePluginTable.get(plugin);
    } catch (Exception ex)
    {
    }

    return null;
  }

  /** Returns an enumeration with all service plugins */
  public Enumeration getCPServicePlugins()
  {
    return cpServicePluginTable.keys();
  }

  /** Returns the class for a certain service plugin */
  public Class getCPServiceClass(BaseCPServicePlugin plugin)
  {
    try
    {
      return (Class)cpServicePluginTable.get(plugin);
    } catch (Exception ex)
    {
    }

    return null;
  }

  /** Load all plugins from a specific path */
  private void loadPlugins(String classPath)
  {
    // System.out.println("GUIControl.PluginManager: Try to load plugins from: " + classPath);
    // create path to class files
    char separator = System.getProperty("file.separator").charAt(0);

    File directory = new File(FileHelper.getBaseDirectoryName() + "bin" + separator + classPath);
    // create name for package structure
    String packagePath = classPath.replace(separator, '.');

    if (directory.isDirectory() && directory.exists())
    {
      File[] plugins = directory.listFiles();
      for (int i = 0; i < plugins.length; i++)
      {
        if (plugins[i].isFile() && plugins[i].getName().endsWith(".class"))
        {
          String simpleName = plugins[i].getName().substring(0, plugins[i].getName().indexOf(".class"));
          // try to load
          try
          {
            Class pluginClass = Class.forName(packagePath + simpleName);

            BasePlugin plugin = (BasePlugin)pluginClass.newInstance();

            // collect device plugins
            if (plugin instanceof BaseCPDevicePlugin && !simpleName.equals("BaseCPDevicePlugin"))
            {
              // System.out.println(" Found CP device plugin: " + plugin.getClass().getName());
              cpDevicePluginTable.put(plugin, pluginClass);
            }
            // collect service plugins
            if (plugin instanceof BaseCPServicePlugin && !simpleName.equals("BaseCPServicePlugin") &&
              !(plugin instanceof BaseCPDevicePlugin))
            {
              // System.out.println(" Found CP service plugin: " + plugin.getClass().getName());
              cpServicePluginTable.put(plugin, pluginClass);
            }
          } catch (Exception ex)
          {
          }
        }
      }
    }
    if (cpDevicePluginTable.size() == 0 && cpServicePluginTable.size() == 0)
    {
      System.out.println("GUIControl.PluginManager: Could not find any plugins");
    }
  }

}

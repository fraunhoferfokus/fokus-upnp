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
package de.fraunhofer.fokus.upnp.util.startup;

import java.io.File;
import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ResourceHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class can be used to parse common startup configurations.
 * 
 * @author Alexander Koenig
 * 
 */
public class StartupConfiguration extends AbstractStartupConfiguration
{

  /**
   * 
   */
  private static final long serialVersionUID            = 1L;

  protected String          workingDirectory            = "";

  protected boolean         startKeyboardThread         = true;

  protected Vector          ignoredIPAddressesList      = new Vector();

  protected Vector          preferredIPAddressesList    = new Vector();

  private Vector            deviceStartupInfoList       = new Vector();

  private Vector            controlPointStartupInfoList = new Vector();

  private Vector            gatewayStartupInfoList      = new Vector();

  /**
   * Creates a new instance of StartupConfiguration.
   * 
   * @param fileName
   *          The name of the file that contains the startup info
   * 
   */
  public StartupConfiguration(String fileName)
  {
    this(new File(fileName));
  }

  /**
   * Creates a new instance of StartupConfiguration using the class loader.
   * 
   * @param classInstance
   *          The class loader of that instance is used
   * @param resourceName
   *          The relative resource name that contains the startup info
   * 
   */
  public StartupConfiguration(Class classInstance, String resourceName)
  {
    super();
    if (resourceName == null)
    {
      return;
    }

    String configuration = ResourceHelper.loadStringViaClassloader(classInstance, resourceName);

    if (configuration != null)
    {
      try
      {
        parse(configuration);
        Portable.println("    Loaded startup info via classloader: " + resourceName);
        valid = true;

        return;
      } catch (SAXException e)
      {
        Portable.println("  Could not read startup configuration: " + e.getMessage());
      }
    }
  }

  /**
   * Creates a new instance of StartupConfiguration.
   * 
   * @param fileName
   *          The name of the file that contains the startup info
   * 
   */
  public StartupConfiguration(File file)
  {
    super();
    // check for existence of file
    if (!file.exists())
    {
      // check file existence relative to resource directory
      if (!new File(FileHelper.getResourceDirectoryName() + file.getName()).exists())
      {
        Portable.println("    File does not exist: " + file.getAbsolutePath());
        return;
      }
      Portable.println("    Set startup file name to be relative to resource directory");
      file = new File(FileHelper.getResourceDirectoryName() + file.getName());
    }
    try
    {
      parse(file);
      Portable.println("    Loaded startup info from file: " + file.getAbsolutePath());
      valid = true;
    } catch (SAXException e)
    {
      Portable.println("  Could not read startup configuration: " + e.getMessage());
    }
  }

  /**
   * Creates an empty instance of StartupConfiguration.
   * 
   * 
   */
  public StartupConfiguration(AbstractStartupConfiguration parentHandler)
  {
    super(parentHandler);
  }

  /** Adds a device startup info. */
  public void addDeviceStartupInfo(ChildStartupConfiguration deviceStartupConfiguration)
  {
    if (!deviceStartupInfoList.contains(deviceStartupConfiguration))
    {
      deviceStartupInfoList.add(deviceStartupConfiguration);
    }
  }

  /** Retrieves all device startup infos. */
  public Vector getDeviceStartupInfoList()
  {
    return deviceStartupInfoList;
  }

  /** Retrieves a device startup info by a class name. */
  public ChildStartupConfiguration getDeviceStartupConfiguration(String className)
  {
    for (int i = 0; i < deviceStartupInfoList.size(); i++)
    {
      ChildStartupConfiguration currentConfiguration = (ChildStartupConfiguration)deviceStartupInfoList.elementAt(i);
      if (currentConfiguration.getID() != null && currentConfiguration.getID().equals(className))
      {
        return currentConfiguration;
      }
    }
    return null;
  }

  /** Retrieves a single device startup info. */
  public ChildStartupConfiguration getSingleDeviceStartupConfiguration()
  {
    if (deviceStartupInfoList.size() == 1)
    {
      return (ChildStartupConfiguration)deviceStartupInfoList.elementAt(0);
    }

    return null;
  }

  /** Adds a control point startup info. */
  public void addControlPointStartupInfo(ChildStartupConfiguration controlPointStartupConfiguration)
  {
    if (!controlPointStartupInfoList.contains(controlPointStartupConfiguration))
    {
      controlPointStartupInfoList.add(controlPointStartupConfiguration);
    }
  }

  /**
   * Retrieves the controlPointStartupInfoList.
   * 
   * @return The controlPointStartupInfoList
   */
  public Vector getControlPointStartupInfoList()
  {
    return controlPointStartupInfoList;
  }

  /** Retrieves a single control point startup info. */
  public ChildStartupConfiguration getSingleControlPointStartupConfiguration()
  {
    if (controlPointStartupInfoList.size() == 1)
    {
      return (ChildStartupConfiguration)controlPointStartupInfoList.elementAt(0);
    }

    return null;
  }

  /** Adds a gateway startup info. */
  public void addGatewayStartupInfo(ChildStartupConfiguration gatewayStartupConfiguration)
  {
    if (!gatewayStartupInfoList.contains(gatewayStartupConfiguration))
    {
      gatewayStartupInfoList.add(gatewayStartupConfiguration);
    }
  }

  /**
   * Retrieves the gatewayStartupInfoList.
   * 
   * @return The gatewayStartupInfoList
   */
  public Vector getGatewayStartupInfoList()
  {
    return gatewayStartupInfoList;
  }

  /** Retrieves a gateway startup info by a class name. */
  public ChildStartupConfiguration getGatewayStartupConfiguration(String className)
  {
    for (int i = 0; i < gatewayStartupInfoList.size(); i++)
    {
      ChildStartupConfiguration currentConfiguration = (ChildStartupConfiguration)gatewayStartupInfoList.elementAt(i);
      if (currentConfiguration.getID() != null && currentConfiguration.getID().equals(className))
      {
        return currentConfiguration;
      }
    }
    return null;
  }

  /** Retrieves a single gateway startup info. */
  public ChildStartupConfiguration getSingleGatewayStartupConfiguration()
  {
    if (gatewayStartupInfoList.size() == 1)
    {
      return (ChildStartupConfiguration)gatewayStartupInfoList.elementAt(0);
    }

    return null;
  }

  /**
   * Retrieves the working directory.
   * 
   * @return The working directory, ending with a separator.
   */
  public String getWorkingDirectory()
  {
    return workingDirectory;
  }

  /** Tries to find a valid working directory if the current directory is not valid. */
  public void trySetValidWorkingDirectory(String className)
  {
    String newWorkingDirectory = FileHelper.tryFindWorkingDirectory(className, workingDirectory);
    if (!newWorkingDirectory.equals(workingDirectory))
    {
      workingDirectory = newWorkingDirectory;
      System.out.println("StartupConfiguration: Update working directory to " + workingDirectory);
    }
  }

  /**
   * Sets the working directory.
   * 
   * @param path
   *          The working directory to set.
   */
  public void setWorkingDirectory(String path)
  {
    this.workingDirectory = FileHelper.toValidDirectoryName(path);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    content = content.trim();
    if (getTagCount() == 2)
    {
      if (getCurrentTag().equals(WORKING_DIRECTORY))
      {
        setWorkingDirectory(content);
      }

      if (getCurrentTag().equals(START_KEYBOARD_THREAD))
      {
        startKeyboardThread = StringHelper.stringToBoolean(content);
      }

      if (getCurrentTag().equals(IGNORE_IP_ADDRESS) && !ignoredIPAddressesList.contains(content))
      {
        ignoredIPAddressesList.add(content);
      }

      if (getCurrentTag().equals(PREFERRED_IP_ADDRESS) && !preferredIPAddressesList.contains(content))
      {
        preferredIPAddressesList.add(content);
      }

      // store all tags in property table
      propertyTable.put(getCurrentTag(), content);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    // process empty elements
    if (getTagCount() == 2)
    {
      if (!propertyTable.containsKey(getCurrentTag()))
      {
        propertyTable.put(getCurrentTag(), "");
      }
    }
  }

  /**
   * Checks if this description is valid for devices.
   * 
   * @return The valid.
   */
  public boolean isValid()
  {
    return valid;
  }

  /**
   * Retrieves the startKeyboardThread.
   * 
   * @return The startKeyboardThread.
   */
  public boolean startKeyboardThread()
  {
    return startKeyboardThread;
  }

  /**
   * Sets the startKeyboardThread.
   * 
   * @param startKeyboardThread
   *          The startKeyboardThread to set.
   */
  public void setStartKeyboardThread(boolean startKeyboardThread)
  {
    this.startKeyboardThread = startKeyboardThread;
  }

  /**
   * Retrieves the ignoredIPAddressesList.
   * 
   * @return The ignoredIPAddressesList
   */
  public Vector getIgnoredIPAddressesList()
  {
    return ignoredIPAddressesList;
  }

  /**
   * Retrieves the value of preferredIPAddressesList.
   * 
   * @return The value of preferredIPAddressesList
   */
  public Vector getPreferredIPAddressesList()
  {
    return preferredIPAddressesList;
  }

}

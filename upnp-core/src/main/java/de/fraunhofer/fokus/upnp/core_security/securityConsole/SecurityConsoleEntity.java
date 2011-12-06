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
package de.fraunhofer.fokus.upnp.core_security.securityConsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateEntity;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class encapsulates a security console.
 * 
 * @author Alexander Koenig
 */
public class SecurityConsoleEntity extends SecurityAwareTemplateEntity
{

  private static final String    DEFAULT_NAME = "Smart Environments SecurityConsole";

  // Vector of control points which announced themselves via PresentKey
  private Vector                 namedControlPointList;

  // Vector of named, security aware devices
  private Vector                 namedDeviceList;

  // name of current user
  private String                 currentUser;

  // name of security console
  private String                 userDefinedName;

  // Event listener (e.g. GUI)
  private ISecurityConsoleEvents securityEventListener;

  // Flag that properties have changed
  private boolean                configChanged;

  /** Creates a new instance of SecurityConsoleEntity with a predefined startup configuration. */
  public SecurityConsoleEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#setupEntityVariables()
   */
  public void setupEntityVariables()
  {
    super.setupEntityVariables();

    // use the user name from personalization
    // the control point is not yet running, so we
    // retrieve the name from the appropriate file
    currentUser =
      TemplateControlPoint.loadUserName(getStartupConfiguration(),
        (ControlPointStartupConfiguration)getStartupConfiguration().getSingleControlPointStartupConfiguration());

    // System.out.println("SecurityConsoleEntity: Current user is " + currentUser);

    userDefinedName = DEFAULT_NAME;

    loadConfigurationFromFile(getAbsConfigFileName());

    namedDeviceList = new Vector();
    // load device list for default user
    loadSecurityAwareDeviceListFromFile(getAbsDeviceFileName());

    namedControlPointList = new Vector();
    // load control point list for default user
    loadSecurityAwareControlPointListFromFile(getAbsControlPointFileName());
    configChanged = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#initEntityContent()
   */
  public void initEntityContent()
  {
    super.initEntityContent();

    // security console control point
    setTemplateControlPoint(new SecurityConsoleControlPoint(this, getStartupConfiguration()));

    // security console device with SecurityConsoleService
    setTemplateDevice(new SecurityConsoleDevice(this, getStartupConfiguration()));

    // initialize security console service
    getSecurityConsoleService().buildNameList();
  }

  /** Sets a listener for securityConsole events */
  public void setSecurityEventsListener(ISecurityConsoleEvents listener)
  {
    securityEventListener = listener;
  }

  /** Terminates the securityConsole */
  public void terminate()
  {
    if (configChanged)
    {
      saveConfigurationToFile(getAbsConfigFileName());
      saveSecurityAwareDeviceListToFile(getAbsDeviceFileName());
      saveSecurityAwareControlPointListToFile(getAbsControlPointFileName());
    }
    super.terminate();
  }

  /** This entity only accepts a security console device */
  public void setTemplateDevice(SecurityConsoleDevice device)
  {
    super.setTemplateDevice(device);
  }

  /** This entity only accepts a security console control point */
  public void setTemplateControlPoint(SecurityConsoleControlPoint controlPoint)
  {
    super.setTemplateControlPoint(controlPoint);
  }

  /** Retrieves the associated device */
  public SecurityConsoleDevice getSecurityConsoleDevice()
  {
    return (SecurityConsoleDevice)getTemplateDevice();
  }

  /** Retrieves the associated securityConsole service */
  public SecurityConsoleService getSecurityConsoleService()
  {
    return getSecurityConsoleDevice().getSecurityConsoleService();
  }

  /** Retrieves the associated control point */
  public SecurityConsoleControlPoint getSecurityConsoleControlPoint()
  {
    return (SecurityConsoleControlPoint)getTemplateControlPoint();
  }

  /** Callback from GUI to inform SecurityConsole */
  public void localDictionaryNameChange(LocalDictionaryObject localDictionaryObject)
  {
    // update NameList to inform other security consoles and control points
    getSecurityConsoleService().buildNameList();

    // flag to store new name at termination
    configChanged = true;

    // event to GUI
    if (securityEventListener != null)
    {
      securityEventListener.localDictionaryNameChange(localDictionaryObject);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Configuration //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  private void loadConfigurationFromFile(String fileName)
  {
    if (new File(fileName).exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            // get individual config items
            StringTokenizer tokens = new StringTokenizer(line, ":");
            try
            {
              String token = tokens.nextToken();
              String value = tokens.nextToken();
              if (token.equals("Name"))
              {
                userDefinedName = value;
              }
            } catch (NoSuchElementException nsee)
            {
              System.out.println("Exception while loading configuration: " + nsee.getMessage());
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
    }
  }

  private void saveConfigurationToFile(String fileName)
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

      writer.write("Name:" + userDefinedName + "\n");
      writer.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  /** Retrieves the user defined name for the security console */
  public String getUserDefinedName()
  {
    return userDefinedName;
  }

  /** Sets a user defined name for the security console */
  public void setUserDefinedName(String name)
  {
    userDefinedName = name;
    configChanged = true;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String getAbsDeviceFileName()
  {
    return FileHelper.getAppropriateFileName(getStartupConfiguration().getWorkingDirectory() + currentUser +
      "_devices.txt");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_security.templates.SecurityAwareTemplateEntity#newSecurityAwareCPDevice(de.fhg.fokus.magic.upnp_security.control_point.SecurityAwareCPDeviceObject)
   */
  public void newSecurityAwareCPDevice(SecurityAwareCPDeviceObject securityAwareDevice)
  {
    // check if a device context for this device already exists
    int index = getNamedDeviceIndexFromKeyHash(securityAwareDevice.getPublicKeyHash());
    if (index != -1)
    {
      LocalDictionaryObject deviceDictionaryObject = getNamedDevice(index);
      // device is already known, set online
      deviceDictionaryObject.setOnline(true);
      // set device associated with this dictionary entry
      deviceDictionaryObject.setSecurityAwareObject(securityAwareDevice);

      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.securityAwareCPDeviceStatusChange(deviceDictionaryObject);
      }
    } else
    {
      // create new dictionary entry associated with this device
      LocalDictionaryObject deviceObject =
        new LocalDictionaryObject(securityAwareDevice.getCPDevice().getFriendlyName(), securityAwareDevice);

      deviceObject.setOnline(true);

      // add to dictionary
      namedDeviceList.add(deviceObject);
      // flag to store device list at termination
      configChanged = true;

      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.newSecurityAwareCPDevice(deviceObject);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp_security.templates.SecurityAwareTemplateEntity#securityAwareCPDeviceGone(de.fhg.fokus.magic.upnp_security.control_point.SecurityAwareCPDeviceObject)
   */
  public void securityAwareCPDeviceGone(SecurityAwareCPDeviceObject device)
  {
    // check if the device is known
    int index = getNamedDeviceIndexFromKeyHash(device.getPublicKeyHash());
    if (index != -1)
    {
      // inform listener
      LocalDictionaryObject deviceObject = getNamedDevice(index);
      // set offline
      deviceObject.setOnline(false);
      // System.out.println("Device with securityID
      // "+deviceObject.getSecurityAwareObject().getSecurityID()+
      // " is now offline");
      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.securityAwareCPDeviceStatusChange(deviceObject);
      }
    }
  }

  /** Event that a device is now owned by this security console. */
  public void securityAwareCPDeviceStatusChange(SecurityAwareCPDeviceObject device)
  {
    // check if the device is known
    int index = getNamedDeviceIndexFromKeyHash(device.getPublicKeyHash());
    if (index != -1)
    {
      // inform listener
      LocalDictionaryObject deviceDictionaryObject = getNamedDevice(index);
      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.securityAwareCPDeviceStatusChange(deviceDictionaryObject);
      }
    }
  }

  /** Retrieves the number of devices in the local dictionary */
  public int getNamedDeviceCount()
  {
    return namedDeviceList.size();
  }

  /** Retrieves a specific device from the local dictionary */
  public LocalDictionaryObject getNamedDevice(int index)
  {
    if (index >= 0 && index < namedDeviceList.size())
    {
      return (LocalDictionaryObject)namedDeviceList.elementAt(index);
    }

    return null;
  }

  /** Tries to find the user defined name for a device */
  public String getUserDefinedNameForDevice(SecurityAwareObject device)
  {
    int index = getNamedDeviceIndexFromKeyHash(device.getPublicKeyHash());
    if (index != -1)
    {
      LocalDictionaryObject deviceObject = getNamedDevice(index);
      return deviceObject.getUserDefinedName();
    }

    return null;
  }

  /** Retrieves the device index for a keyHash */
  private int getNamedDeviceIndexFromKeyHash(byte[] keyHash)
  {
    for (int i = 0; i < namedDeviceList.size(); i++)
    {
      if (((LocalDictionaryObject)namedDeviceList.elementAt(i)).getSecurityAwareObject().equals(keyHash))
      {
        return i;
      }
    }
    return -1;
  }

  private void loadSecurityAwareDeviceListFromFile(String fileName)
  {
    namedDeviceList.clear();
    // System.out.println(" Loading device list from file: "+fileName);
    if (new File(fileName).exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            // get individual config items
            StringTokenizer tokens = new StringTokenizer(line, ":");
            try
            {
              String deviceName = tokens.nextToken();
              String hashAlgorithm = tokens.nextToken();
              String publicKeyHash = tokens.nextToken();

              SecurityAwareObject securityObject = new SecurityAwareObject(hashAlgorithm, publicKeyHash);
              LocalDictionaryObject deviceDictionaryObject = new LocalDictionaryObject(deviceName, securityObject);
              namedDeviceList.add(deviceDictionaryObject);
            } catch (NoSuchElementException nsee)
            {
              System.out.println("Exception while loading device list: " + nsee.getMessage());
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
      // System.out.println(" Loaded " + namedDeviceList.size() + " security aware device(s)");
    }
  }

  private void saveSecurityAwareDeviceListToFile(String fileName)
  {
    // System.out.println(" Saving devices to file: "+fileName);
    try
    {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

      for (int i = 0; i < getNamedDeviceCount(); i++)
      {
        LocalDictionaryObject device = getNamedDevice(i);
        // calculate base64 value for public key hash
        String publicKeyHashBase64 = Base64Helper.byteArrayToBase64(device.getSecurityAwareObject().getPublicKeyHash());

        writer.write(device.getUserDefinedName() + ":" + device.getSecurityAwareObject().getHashAlgorithm() + ":" +
          publicKeyHashBase64 + "\n");
      }
      writer.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Control point management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String getAbsControlPointFileName()
  {
    return FileHelper.getAppropriateFileName(getStartupConfiguration().getWorkingDirectory() + currentUser +
      "_control_points.txt");
  }

  /** Retrieves the number of control points in the local dictionary */
  public int getNamedControlPointCount()
  {
    return namedControlPointList.size();
  }

  /** Retrieves a specific control point from the local dictionary */
  public LocalDictionaryObject getNamedControlPoint(int index)
  {
    if (index >= 0 && index < namedControlPointList.size())
    {
      return (LocalDictionaryObject)namedControlPointList.elementAt(index);
    }

    return null;
  }

  /** Tries to find the user defined name for a control point */
  public String getUserDefinedNameForControlPoint(SecurityAwareObject controlPoint)
  {
    int index = getControlPointIndex(controlPoint.getPublicKeyHash());
    if (index != -1)
    {
      LocalDictionaryObject controlPointObject = getNamedControlPoint(index);
      return controlPointObject.getUserDefinedName();
    }

    return null;
  }

  protected void tryAddSecurityAwareControlPoint(String name, String hashAlgorithm, byte[] keyHash)
  {
    // check if the control point is already known
    int index = getControlPointIndex(keyHash);
    if (index != -1)
    {
      LocalDictionaryObject controlPoint = getNamedControlPoint(index);
      // control point is already known, set online
      controlPoint.setOnline(true);
      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.securityAwareControlPointStatusChange(controlPoint);
      }
    } else
    {
      // new control point
      SecurityAwareObject securityObject = new SecurityAwareObject(hashAlgorithm, keyHash);
      LocalDictionaryObject controlPoint = new LocalDictionaryObject(name, securityObject);
      controlPoint.setOnline(true);
      namedControlPointList.add(controlPoint);
      // flag to store control point list at termination
      configChanged = true;
      // event to GUI
      if (securityEventListener != null)
      {
        securityEventListener.newSecurityAwareControlPoint(controlPoint);
      }
    }

  }

  // private boolean isKnownControlPoint(byte[] keyHash)
  // {
  // return getControlPointIndex(keyHash) != -1;
  // }

  private int getControlPointIndex(byte[] keyHash)
  {
    for (int i = 0; i < namedControlPointList.size(); i++)
    {
      if (((LocalDictionaryObject)namedControlPointList.elementAt(i)).getSecurityAwareObject().equals(keyHash))
      {
        return i;
      }
    }
    return -1;
  }

  private void loadSecurityAwareControlPointListFromFile(String fileName)
  {
    namedControlPointList.clear();
    // System.out.println(" Loading control point list from file: "+fileName);
    if (new File(fileName).exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            // get individual config items
            StringTokenizer tokens = new StringTokenizer(line, ":");
            try
            {
              String deviceName = tokens.nextToken();
              String hashAlgorithm = tokens.nextToken();
              String publicKeyHashBase64 = tokens.nextToken();
              SecurityAwareObject securityObject = new SecurityAwareObject(hashAlgorithm, publicKeyHashBase64);
              LocalDictionaryObject namedObject = new LocalDictionaryObject(deviceName, securityObject);
              namedControlPointList.add(namedObject);
              // System.out.println("Loaded control point with securityID
              // "+securityObject.getSecurityID());
            } catch (Exception e)
            {
              System.out.println("Exception while loading control point list: " + e.getMessage());
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
      // System.out.println(" Loaded " + namedControlPointList.size()
      // + " security aware control point(s)");
    }
  }

  private void saveSecurityAwareControlPointListToFile(String fileName)
  {
    // System.out.println(" Saving control point list to file: "+fileName);
    try
    {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

      for (int i = 0; i < getNamedControlPointCount(); i++)
      {
        LocalDictionaryObject controlPoint = getNamedControlPoint(i);
        // calculate base64 value for public key hash
        String publicKeyHashBase64 =
          Base64Helper.byteArrayToBase64(controlPoint.getSecurityAwareObject().getPublicKeyHash());

        writer.write(controlPoint.getUserDefinedName() + ":" +
          controlPoint.getSecurityAwareObject().getHashAlgorithm() + ":" + publicKeyHashBase64 + "\n");
      }
      writer.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // User management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String getCurrentUser()
  {
    return currentUser;
  }

  public String getAbsConfigFileName()
  {
    return FileHelper.getAppropriateFileName(getStartupConfiguration().getWorkingDirectory() + currentUser +
      "_config.txt");
  }

  public void setCurrentUser(String user)
  {
    // save config for last user
    if (configChanged)
    {
      saveConfigurationToFile(getAbsConfigFileName());
      saveSecurityAwareDeviceListToFile(getAbsDeviceFileName());
      saveSecurityAwareControlPointListToFile(getAbsControlPointFileName());
    }

    currentUser = user;

    userDefinedName = DEFAULT_NAME;
    loadConfigurationFromFile(getAbsConfigFileName());

    loadSecurityAwareDeviceListFromFile(getAbsDeviceFileName());
    // associate local dictionary object with all devices that are already online
    for (int i = 0; i < getSecurityAwareControlPoint().getSecurityAwareCPDeviceObjectCount(); i++)
    {
      SecurityAwareCPDeviceObject currentCPDeviceObject =
        getSecurityAwareControlPoint().getSecurityAwareCPDeviceObject(i);

      for (int j = 0; j < getNamedDeviceCount(); j++)
      {
        LocalDictionaryObject currentDictionaryObject = getNamedDevice(j);
        if (currentDictionaryObject.getSecurityAwareObject()
          .getPublicKeyHashBase64()
          .equals(currentCPDeviceObject.getPublicKeyHashBase64()))
        {
          // device is already known, set online
          currentDictionaryObject.setOnline(true);
          // set device associated with this dictionary entry
          currentDictionaryObject.setSecurityAwareObject(currentCPDeviceObject);
        }
      }
    }

    loadSecurityAwareControlPointListFromFile(getAbsControlPointFileName());
    configChanged = false;
    // update NameList to inform other security consoles and control points
    getSecurityConsoleService().buildNameList();

    // update key pair for control point
    if (getSecurityAwareControlPoint() != null)
    {
      getSecurityAwareControlPoint().usePersonalizationKeyForSecurity();
      TemplateControlPoint.printMessage(getSecurityAwareControlPoint().toString() +
        ": Changed security console user to " + currentUser);
    }
    // send update for all dictionary objects to force new names in GUI
    if (securityEventListener != null)
    {
      TemplateControlPoint.printMessage(getSecurityAwareControlPoint().toString() +
        ": Inform GUI about changed dictionary objects");

      for (int i = 0; i < namedControlPointList.size(); i++)
      {
        securityEventListener.localDictionaryNameChange((LocalDictionaryObject)namedControlPointList.elementAt(i));
      }
      for (int i = 0; i < namedDeviceList.size(); i++)
      {
        securityEventListener.localDictionaryNameChange((LocalDictionaryObject)namedDeviceList.elementAt(i));
      }
    }
  }

}

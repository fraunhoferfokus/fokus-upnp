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
package de.fraunhofer.fokus.upnp.core.templates;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.Log4jHelper;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener;
import de.fraunhofer.fokus.upnp.util.threads.KeyboardThread;

/**
 * This class can contain a UPnP control point and/or several UPnP devices. Entities may be executable. TemplateEntity
 * should be used as template for all derived UPnP entities.
 * 
 * @author Alexander Koenig
 */
public class TemplateEntity implements ICPDeviceEventListener, ICPStateVariableListener, IKeyboardThreadListener
{
  public static Logger             logger                    = Logger.getLogger("upnp.entity");

  /** Associated single template device */
  private TemplateDevice           device                    = null;

  /** List of associated template devices */
  private Vector                   deviceList                = new Vector();

  /** Associated template control point */
  private TemplateControlPoint     controlPoint              = null;

  /** Associated optional keyboard thread. */
  private KeyboardThread           keyboardThread;

  /** Flag to start a keyboard thread to allow termination over console */
  private boolean                  startKeyboardThread       = false;

  /** Optional listener for device events */
  private ICPDeviceEventListener   cpDeviceEventListener     = null;

  /** Optional listener for state variable events */
  private ICPStateVariableListener upnpStateVariableListener = null;

  /** Startup configuration */
  protected UPnPStartupConfiguration   startupConfiguration;

  static
  {
    System.out.println("Base directory name is " + FileHelper.getBaseDirectoryName());
    if (!Log4jHelper.isInitialized())
    {
      Log4jHelper.initializeLogging();
    }
    if (!Log4jHelper.isInitialized())
    {
      Log4jHelper.initializeLoggingViaClassLoader(logger.getClass());
    }
    logger.debug(UPnPConstant.MUS_VERSION);
  }

  /** Creates a new instance of TemplateEntity with a standard startup configuration. */
  public TemplateEntity()
  {
    this((UPnPStartupConfiguration)null);
  }

  /** Creates a new instance of TemplateEntity with a predefined startup configuration. */
  public TemplateEntity(UPnPStartupConfiguration startupConfiguration)
  {
    // create startupConfiguration if necessary
    if (startupConfiguration == null)
    {
      String entityName = getShortClassNameForEntity();
      String startupFileName = FileHelper.getStartupConfigurationName(entityName);
      startupConfiguration = new UPnPStartupConfiguration(startupFileName);
    }
    if (!startupConfiguration.isValid())
    {
      // we now try to load the startup configuration via the class loader
      TemplateDevice.printMessage("Startup info not available via file system. Try class loader.");

      String entityName = getShortClassNameForEntity();
      String startupFileName = FileHelper.getStartupConfigurationNameViaClassLoader(getClass(), entityName);
      startupConfiguration = new UPnPStartupConfiguration(getClass(), startupFileName);
    }
    if (startupConfiguration.isValid())
    {
      this.startKeyboardThread = startupConfiguration.startKeyboardThread();
      this.startupConfiguration = startupConfiguration;
      this.startupConfiguration.trySetValidWorkingDirectory(this.getClass().getName());
      setupEntityVariables();
      initEntityContent();
      runEntity();

      return;
    }
    System.out.println("Invalid or missing startup info. Exit application.");
    return;
  }

  /** Prints a message */
  public static void printMessage(String text)
  {
    System.out.println("  " + text);
  }

  /** Retrieves the simple class name for this entity. */
  public String getShortClassNameForEntity()
  {
    return StringHelper.getShortClassName(getClass().getName());
  }

  /** Returns the most appropriate startup configuration for a short class name from the file system. */
  public static UPnPStartupConfiguration getStartupConfiguration(String shortClassName)
  {
    String startupFileName = FileHelper.getStartupConfigurationName(shortClassName);

    return new UPnPStartupConfiguration(startupFileName);
  }

  /**
   * Returns the most appropriate startup configuration for a short class name that can be loaded with the class loader.
   */
  public static UPnPStartupConfiguration getStartupConfigurationViaClassLoader(Class classInstance, String shortClassName)
  {
    String startupConfigurationName =
      FileHelper.getStartupConfigurationNameViaClassLoader(classInstance, shortClassName);

    return new UPnPStartupConfiguration(classInstance, startupConfigurationName);
  }

  /** Set up variables prior to startup */
  public void setupEntityVariables()
  {
  }

  /** Init the entity */
  public void initEntityContent()
  {
  }

  /** Start the entity */
  public void runEntity()
  {
    // start thread
    if (startKeyboardThread)
    {
      keyboardThread = new KeyboardThread(this, toString() + ".KeyboardThread");
    }
  }

  /** Starts a delayed entity */
  public void runDelayed()
  {
    setupEntityVariables();
    initEntityContent();
    runEntity();
  }

  /** Sets a listener that gets UPnP events received by the embedded control point */
  public void setCPDeviceEventListener(ICPDeviceEventListener listener)
  {
    cpDeviceEventListener = listener;
  }

  /** Sets a listener that gets UPnP state variable events received by the embedded control point */
  public void setCPStateVariableListener(ICPStateVariableListener listener)
  {
    upnpStateVariableListener = listener;
  }

  /** Sets the embedded control point */
  public void setTemplateControlPoint(TemplateControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
  }

  /** Returns a reference to the embedded control point */
  public TemplateControlPoint getTemplateControlPoint()
  {
    return controlPoint;
  }

  /** Sets the embedded device */
  public boolean setTemplateDevice(TemplateDevice device)
  {
    if (deviceList.size() == 0)
    {
      this.device = device;
      return true;
    }
    return false;
  }

  /** Adds an embedded device */
  public boolean addTemplateDevice(TemplateDevice device)
  {
    if (this.device != null)
    {
      return false;
    }

    if (deviceList.indexOf(device) == -1)
    {
      deviceList.add(device);
      return true;
    }
    return false;
  }

  /** Removes an embedded device */
  public boolean removeTemplateDevice(TemplateDevice device)
  {
    // remove single device
    if (this.device != null && this.device == device)
    {
      this.device = null;
      return true;
    }
    // invalid device
    if (this.device != null)
    {
      return false;
    }

    if (deviceList.indexOf(device) != -1)
    {
      deviceList.remove(device);
      return true;
    }
    return false;
  }

  /** Returns the first embedded device */
  public TemplateDevice getTemplateDevice()
  {
    // return only single devices
    if (deviceList.size() == 0)
    {
      return device;
    }

    if (deviceList.size() == 1)
    {
      return (TemplateDevice)deviceList.elementAt(0);
    }

    return null;
  }

  /** Returns an embedded device. */
  public TemplateDevice getTemplateDevice(int index)
  {
    if (index < 0 || index >= getTemplateDeviceCount())
    {
      return null;
    }

    if (device != null)
    {
      return device;
    }

    return (TemplateDevice)deviceList.elementAt(index);
  }

  /** Returns the number of embedded devices */
  public int getTemplateDeviceCount()
  {
    if (deviceList.size() == 0 && device == null)
    {
      return 0;
    }

    if (device != null)
    {
      return 1;
    }

    return deviceList.size();
  }

  /**
   * Checks if the device belongs to this entity
   */
  public boolean isEntityDevice(String udn)
  {
    // check if device belongs to the entity of this control point
    return device != null && device.getUDN().equals(udn);
  }

  /**
   * Checks if the device belongs to this entity
   */
  public boolean isEntityDevice(CPDevice aDevice)
  {
    // check if device belongs to the entity of this control point
    return device != null && device.getUDN().equals(aDevice.getUDN());
  }

  /**
   * @return the keyboardThread
   */
  public KeyboardThread getKeyboardThread()
  {
    return keyboardThread;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    if (cpDeviceEventListener != null)
    {
      cpDeviceEventListener.newDevice(newDevice);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    if (cpDeviceEventListener != null)
    {
      cpDeviceEventListener.deviceGone(goneDevice);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceEvent(de.fhg.fokus.magic.upnp.control_point.CPDevice,
   *      int)
   */
  public void deviceEvent(CPDevice device, int eventCode, Object eventParameter)
  {
    if (cpDeviceEventListener != null)
    {
      cpDeviceEventListener.deviceEvent(device, eventCode, eventParameter);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (upnpStateVariableListener != null)
    {
      upnpStateVariableListener.stateVariableChanged(stateVariable);
    }
  }

  /**
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration.
   */
  public UPnPStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return StringHelper.getShortClassName(this.getClass().getName());
  }

  /**
   * Terminates this entity with all embedded devices and control points. If a keyboard thread was started, it also
   * waits for the termination of the keyboard thread.
   */
  public void terminate()
  {
    // terminate all started devices and control points
    if (controlPoint != null)
    {
      controlPoint.terminate();
    }
    if (device != null)
    {
      device.terminate();
    }
    if (deviceList.size() != 0)
    {
      for (int i = 0; i < deviceList.size(); i++)
      {
        getTemplateDevice(i).terminate();
      }
    }
    controlPoint = null;
    device = null;
    deviceList.clear();
    if (keyboardThread != null)
    {
      // terminate was called from the keyboard thread, so we are done now
      if (keyboardThread.isTerminated())
      {
        System.out.println("Entity was shut down.");
      }
      // signal termination to keyboard thread if terminate() came from outside
      keyboardThread.terminate();
      while (!keyboardThread.isTerminated())
      {
        ThreadHelper.sleep(20);
      }
    } else
    {
      System.out.println("Entity was shut down.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.IKeyboardThreadListener#terminateEvent()
   */
  public void terminateEvent()
  {
    terminate();
  }

}

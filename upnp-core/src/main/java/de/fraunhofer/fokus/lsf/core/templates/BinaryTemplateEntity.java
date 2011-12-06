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
package de.fraunhofer.fokus.lsf.core.templates;

import java.util.Vector;

import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryControlPoint;
import de.fraunhofer.fokus.lsf.core.device.BinaryDevice;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPServiceValueListener;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;
import de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener;
import de.fraunhofer.fokus.upnp.util.threads.KeyboardThread;

/**
 * This class contains a binary UPnP control point and/or several binary UPnP devices. It should be used as template for
 * all binary UPnP entities.
 * 
 * @author Alexander Koenig
 */
public class BinaryTemplateEntity implements
  IBinaryCPDeviceEventListener,
  IBinaryCPServiceValueListener,
  IKeyboardThreadListener,
  IKeyListener
{
  /** Associated binary control point */
  private BinaryControlPoint            controlPoint         = null;

  private Vector                        deviceList           = new Vector();

  private KeyboardThread                keyboardThread       = null;

  /** Optional listener for device events */
  private IBinaryCPDeviceEventListener  deviceEventListener  = null;

  /** Optional listener for value events */
  private IBinaryCPServiceValueListener serviceValueListener = null;

  /** Flag that the entity has been terminated */
  private boolean                       terminated           = false;

  /** Flag to start a keyboard thread to allow termination over console */
  private boolean                       startKeyboardThread  = false;

  /** Startup configuration */
  protected LSFStartupConfiguration     startupConfiguration;

  /** Creates a new instance of BinaryTemplateEntity */
  public BinaryTemplateEntity(LSFStartupConfiguration startupConfiguration)
  {
    // create startupConfiguration if necessary
    if (startupConfiguration == null)
    {
      String entityName = StringHelper.getShortClassName(getClass().getName());
      String startupFileName = FileHelper.getStartupConfigurationName(entityName);
      startupConfiguration = new LSFStartupConfiguration(startupFileName);
    }
    if (!startupConfiguration.isValid())
    {
      // we now try to load the startup configuration via the class loader
      TemplateDevice.printMessage("Startup info not available via file system. Try class loader.");

      String entityName = StringHelper.getShortClassName(getClass().getName());
      String startupFileName = FileHelper.getStartupConfigurationNameViaClassLoader(getClass(), entityName);
      startupConfiguration = new LSFStartupConfiguration(getClass(), startupFileName);
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

  /** Returns the most appropriate startup configuration for a short class name from the file system. */
  public static LSFStartupConfiguration getStartupConfiguration(String shortClassName)
  {
    String startupFileName = FileHelper.getStartupConfigurationName(shortClassName);

    return new LSFStartupConfiguration(startupFileName);
  }

  /**
   * Returns the most appropriate startup configuration for a short class name that can be loaded with the class loader.
   */
  public static LSFStartupConfiguration getStartupConfigurationViaClassLoader(Class classInstance, String shortClassName)
  {
    String startupConfigurationName =
      FileHelper.getStartupConfigurationNameViaClassLoader(classInstance, shortClassName);

    return new LSFStartupConfiguration(classInstance, startupConfigurationName);
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
      keyboardThread = new KeyboardThread(this, "BinaryTemplateEntity");
      keyboardThread.setKeyListener(this);
    }
  }

  /** Sets the embedded control point */
  public void setBinaryControlPoint(BinaryControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
  }

  /** Returns a reference to the embedded control point */
  public BinaryControlPoint getBinaryControlPoint()
  {
    return controlPoint;
  }

  /** Adds a device to this entity. */
  public void addBinaryDevice(BinaryDevice device)
  {
    if (!deviceList.contains(device))
    {
      deviceList.add(device);
    }
  }

  /** Retrieves the number of local devices. */
  public int getBinaryDeviceCount()
  {
    return deviceList.size();
  }

  /** Retrieves a specific local device. */
  public BinaryDevice getBinaryDevice(int index)
  {
    if (index >= 0 && index < deviceList.size())
    {
      return (BinaryDevice)deviceList.elementAt(index);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#newDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void newDevice(BinaryCPDevice newDevice)
  {
    // Portable.println("BinaryTemplateEntity: Found new device: " + newDevice.getName());

    if (deviceEventListener != null)
    {
      deviceEventListener.newDevice(newDevice);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#changedDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice, int)
   */
  public void changedDevice(BinaryCPDevice changedDevice, int eventCode)
  {
    if (deviceEventListener != null)
    {
      deviceEventListener.changedDevice(changedDevice, eventCode);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#deviceGone(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void deviceGone(BinaryCPDevice goneDevice)
  {
    // Portable.println("BinaryTemplateEntity: Device gone: " + goneDevice.getName());
    if (deviceEventListener != null)
    {
      deviceEventListener.deviceGone(goneDevice);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.misc.binary_upnp.event.IBinaryCPServiceValueListener#valueChanged(de.fhg.fokus.magic.misc.binary_upnp.control_point.BinaryCPService)
   */
  public void valueChanged(BinaryCPService binaryCPService)
  {
    // try
    // {
    // String message = DateTimeHelper.formatCurrentDateForDebug() +
    // ": Value of " + binaryCPService.getServiceTypeString() +
    // " changed: ";
    //      
    // if (binaryCPService.isBooleanService())
    // Portable.println(message + binaryCPService.getBooleanValue());
    //			
    // if (binaryCPService.isNumericService())
    // Portable.println(message + binaryCPService.getNumericValue());
    //
    // if (binaryCPService.isStringService())
    // Portable.println(message + binaryCPService.getStringValue());
    //			
    // } catch (Exception e)
    // {
    // }
    if (serviceValueListener != null)
    {
      serviceValueListener.valueChanged(binaryCPService);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == 's' && getBinaryControlPoint() != null)
    {
      getBinaryControlPoint().sendSearchAllMessage();
    }
  }

  /**
   * Sets the deviceEventListener.
   * 
   * @param deviceEventListener
   *          The deviceEventListener to set.
   */
  public void setDeviceEventListener(IBinaryCPDeviceEventListener deviceEventListener)
  {
    this.deviceEventListener = deviceEventListener;
  }

  /**
   * Sets the serviceValueListener.
   * 
   * @param serviceValueListener
   *          The serviceValueListener to set.
   */
  public void setServiceValueListener(IBinaryCPServiceValueListener serviceValueListener)
  {
    this.serviceValueListener = serviceValueListener;
  }

  /**
   * Retrieves the value of startupConfiguration.
   * 
   * @return The value of startupConfiguration
   */
  public LSFStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves the value of keyboardThread.
   * 
   * @return The value of keyboardThread
   */
  public KeyboardThread getKeyboardThread()
  {
    return keyboardThread;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener#terminateEvent()
   */
  public void terminateEvent()
  {
    // Entity was terminated over the keyboard thread
    if (!terminated)
    {
      terminate();
    }
  }

  /** Terminates this entity. */
  public void terminate()
  {
    // terminate all started devices and control points
    if (controlPoint != null)
    {
      controlPoint.terminate();
    }
    controlPoint = null;
    for (int i = 0; i < getBinaryDeviceCount(); i++)
    {
      getBinaryDevice(i).terminate();
    }
    deviceList.clear();

    Portable.println("BinaryTemplateEntity was shut down.");
    // set to true, because keyboard thread will call terminateEvent()
    terminated = true;
    if (keyboardThread != null)
    {
      keyboardThread.terminate();
      while (!keyboardThread.isTerminated())
      {
        ThreadHelper.sleep(10);
      }
    }
  }

}

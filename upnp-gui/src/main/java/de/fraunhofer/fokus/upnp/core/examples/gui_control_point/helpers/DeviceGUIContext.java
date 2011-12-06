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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.DeviceIcon;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControl;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControlConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPDevicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPServicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.StandardServicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.util.MediaHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothDescriptionButton;

/**
 * This class holds all data needed by the GUI for one CPDevice.
 * 
 * @author Alexander Koenig
 */
public class DeviceGUIContext implements ICPDeviceEventListener, ICPStateVariableListener
{

  /** Associated GUI */
  private GUIControl              guiControl;

  private SmoothDescriptionButton deviceNameButton;

  /** Button to access the device without plugins */
  private SmoothButton            deviceDirectAccessButton;

  /** Associated device */
  private CPDevice                device;

  /** Associated device plugin or null */
  private BaseCPDevicePlugin      devicePlugin                      = null;

  /** Flag that this device has an associated device plugin */
  private boolean                 useDevicePlugin                   = false;

  /** Service plugin list for plugin display */
  private Vector                  servicePluginList                 = new Vector();

  /** Name button list for plugin display */
  private Vector                  serviceNameButtonList             = new Vector();

  private Vector                  serviceDirectAccessPluginList     = new Vector();

  private Vector                  serviceDirectAccessNameButtonList = new Vector();

  /** Flag to show a base plugin instead of the device specific plugin */
  private boolean                 useDirectAccessPlugin             = false;

  /** Flag that the device is valid */
  private boolean                 valid                             = true;

  /** Device genre to find appropriate panel in GUI */
  private int                     deviceGenre                       = GUIConstants.GENRE_MISC;

  /** Class to translate device properties etc. */
  private DeviceTranslations      deviceTranslations;

  /** Creates a new instance of DeviceGUIContext */
  public DeviceGUIContext(GUIControl guiControl, CPDevice device)
  {
    this.guiControl = guiControl;
    this.device = device;

    // create translation manager for this device
    deviceTranslations = new DeviceTranslations(device);

    // create name button
    deviceNameButton =
      new SmoothDescriptionButton(new Dimension(GUIConstants.DESCRIPTION_BUTTON_WIDTH,
        GUIConstants.DESCRIPTION_BUTTON_HEIGHT),
        18,
        12,
        device.getFriendlyName(),
        device.getDeviceDescriptionSocketAddress().getAddress().getHostName(),
        device.getUDN());
    deviceNameButton.setAutomaticTooltip(false);
    deviceNameButton.setToolTipText(IPHelper.toString(device.getDeviceDescriptionSocketAddress()));

    // try to translate device name
    deviceTranslations.setTranslationForButton(deviceNameButton, device.getFriendlyName());

    deviceDirectAccessButton =
      new SmoothButton(new Dimension(40, 45), "...", device.getUDN() + GUIControlConstants.DIRECT_ACCESS);
    deviceDirectAccessButton.setAutomaticTooltip(false);
    deviceDirectAccessButton.setToolTipText(device.getDeviceDescriptionURL().toExternalForm());

    // try to find device plugin
    devicePlugin = null;
    // go through all device plugins
    Enumeration cpDevicePlugins = guiControl.getPluginManager().getCPDevicePlugins();
    while (useDevicePlugin == false && cpDevicePlugins.hasMoreElements())
    {
      // enumerate class instances of manager
      BaseCPDevicePlugin currentPlugin = (BaseCPDevicePlugin)cpDevicePlugins.nextElement();
      // System.out.println("Compare " + device.getDeviceType() + " to " +
      // currentPlugin.getPluginType());

      if (device.getDeviceType().equals(currentPlugin.getPluginType()))
      {
        // store flag that a device plugin can be instantiated
        useDevicePlugin = true;
      }
    }

    // initialize base plugins for services to allow standard access
    for (int i = 0; i < device.getCPServiceTable().length; i++)
    {
      CPService service = device.getCPServiceTable()[i];

      SmoothButton serviceNameButton =
        new SmoothButton(new Dimension(GUIConstants.SERVICE_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
          12,
          service.getShortenedServiceId(),
          service.getServiceId());
      serviceNameButton.setAutomaticTooltip(false);

      serviceDirectAccessNameButtonList.add(serviceNameButton);

      // exclude certain services from display if not direct access
      if (!service.getServiceType().equals(DeviceConstant.TRANSLATION_SERVICE_TYPE) &&
        !service.getServiceType().equals(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE))
      {
        serviceNameButtonList.add(serviceNameButton);
      }

      // try to translate service name
      deviceTranslations.setTranslationForButton(serviceNameButton, service.getShortenedServiceId());

      // plugin will only be loaded if service is actually accessed
      servicePluginList.add(null);
      serviceDirectAccessPluginList.add(null);
    }

    // try to find icon
    if (device.getIconTable() != null && device.getIconTable().length > 0)
    {
      DeviceIcon icon = device.getIconTable()[0];
      deviceNameButton.setIconImage(MediaHelper.loadImageFromURL(icon.getURL(), guiControl));
    }

    // choose device genre
    if (device.getDeviceType().equals(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE) ||
      device.getDeviceType().equals(UPnPAVConstant.AV_SYNCHRONIZATION_DEVICE_TYPE) ||
      device.getDeviceType().equals(DeviceConstant.DIGITAL_SECURITY_CAMERA_DEVICE_TYPE))
    {
      deviceGenre = GUIConstants.GENRE_MEDIA_SERVER;
    } else if (device.getDeviceType().equals(UPnPAVConstant.MEDIA_RENDERER_DEVICE_TYPE))
    {
      deviceGenre = GUIConstants.GENRE_MEDIA_RENDERER;
    } else if (device.getDeviceType().equals(DeviceConstant.PERSONAL_ORGANIZER_DEVICE_TYPE) ||
      device.getDeviceType().equals(DeviceConstant.PERSONALIZATION_INFO_DEVICE_TYPE) ||
      device.getDeviceType().equals(DeviceConstant.LCD_DISPLAY_DEVICE_TYPE))
    {
      deviceGenre = GUIConstants.GENRE_PERSONALIZATION;
    } else if (device.getDeviceType().equals("urn:schemas-fokus-fraunhofer-de:device:EZControlDevice:1") ||
      device.getDeviceType().equals("urn:schemas-fokus-fraunhofer-de:device:FanDevice:1") ||
      device.getDeviceType().equals("urn:schemas-fokus-fraunhofer-de:device:DemoRoomDevice:1") ||
      device.getDeviceType().equals(DeviceConstant.AUTOMATION_DEVICE_TYPE))
    {
      deviceGenre = GUIConstants.GENRE_HOME_CONTROL;
    } else if (device.getDeviceType().equals(SensorConstants.TEMPERATURE_SENSOR_DEVICE_TYPE) ||
      device.getDeviceType().equals(SensorConstants.BRIGHTNESS_SENSOR_DEVICE_TYPE) ||
      device.getDeviceType().equals("urn:schemas-fokus-fraunhofer-de:device:InternetWeatherDevice:1") ||
      device.getDeviceType().equals(DeviceConstant.GPS_DEVICE_TYPE) ||
      device.getDeviceType().equals(SensorConstants.IR_REMOTE_CONTROL_DEVICE_TYPE) ||
      device.getDeviceType().equals(SensorConstants.BUTTON_DEVICE_TYPE))
    {
      deviceGenre = GUIConstants.GENRE_SENSOR;
    } else if (device.getDeviceType().equals("urn:schemas-fokus-fraunhofer-de:device:MessagingDevice:1"))
    {
      deviceGenre = GUIConstants.GENRE_MESSAGING;
    }
    deviceNameButton.setActiveButtonColor(SmoothButton.brighter(guiControl.getDeviceGenreColor(this), 10));
  }

  /** Adds the action listener for buttons that are processed by the GUI */
  public void addActionListener(ActionListener listener)
  {
    deviceNameButton.addActionListener(listener);
    deviceDirectAccessButton.addActionListener(listener);
    for (int i = 0; i < serviceDirectAccessNameButtonList.size(); i++)
    {
      ((SmoothButton)serviceDirectAccessNameButtonList.elementAt(i)).addActionListener(listener);
    }
  }

  /** Retrieves the button with the device name */
  public SmoothButton getDeviceNameButton()
  {
    return deviceNameButton;
  }

  /** Retrieves the button for direct access */
  public SmoothButton getDeviceDirectAccessButton()
  {
    return deviceDirectAccessButton;
  }

  /** Retrieves the associated device */
  public CPDevice getDevice()
  {
    return device;
  }

  /** Retrieves the translation manager */
  public DeviceTranslations getDeviceTranslations()
  {
    return deviceTranslations;
  }

  /** Checks if the base plugin for services should be used */
  public boolean isDirectAccess()
  {
    return useDirectAccessPlugin;
  }

  /** Sets the plugin used for this device */
  public void setDirectAccess(boolean state)
  {
    useDirectAccessPlugin = state;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (devicePlugin != null)
    {
      devicePlugin.stateVariableChanged(stateVariable);
    }
    // forward event to the associated plugin if possible
    for (int i = 0; i < servicePluginList.size(); i++)
    {
      if (servicePluginList.elementAt(i) != null)
      {
        ((BaseCPServicePlugin)servicePluginList.elementAt(i)).stateVariableChanged(stateVariable);
      }
    }
    // forward event to the direct access plugin if possible
    for (int i = 0; i < serviceDirectAccessPluginList.size(); i++)
    {
      if (serviceDirectAccessPluginList.elementAt(i) != null)
      {
        ((BaseCPServicePlugin)serviceDirectAccessPluginList.elementAt(i)).stateVariableChanged(stateVariable);
      }
    }
  }

  public void userChanged(String userName)
  {
    // forward event to the associated plugin
    if (devicePlugin == null)
    {
      devicePlugin = getDevicePlugin();
    }
    if (devicePlugin != null)
    {
      devicePlugin.userChanged(userName);
    }
    for (int i = 0; i < getVisibleCPServiceCount(); i++)
    {
      if (isInitializedServicePlugin(i))
      {
        getVisibleCPServicePlugin(i).userChanged(userName);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    // forward event to the associated plugin
    if (devicePlugin != null)
    {
      devicePlugin.newDevice(newDevice);
    }
    for (int i = 0; i < getVisibleCPServiceCount(); i++)
    {
      if (isInitializedServicePlugin(i))
      {
        getVisibleCPServicePlugin(i).newDevice(newDevice);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    // forward event to the associated plugin
    if (devicePlugin != null)
    {
      devicePlugin.deviceGone(goneDevice);
    }
    // forward event to service plugins
    for (int i = 0; i < getVisibleCPServiceCount(); i++)
    {
      if (isInitializedServicePlugin(i))
      {
        getVisibleCPServicePlugin(i).deviceGone(goneDevice);
      }
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
    // forward event to the associated plugin
    if (devicePlugin != null)
    {
      devicePlugin.deviceEvent(device, eventCode, null);
    }
    for (int i = 0; i < getVisibleCPServiceCount(); i++)
    {
      if (isInitializedServicePlugin(i))
      {
        getVisibleCPServicePlugin(i).deviceEvent(device, eventCode, null);
      }
    }
    // update translations for pending buttons
    if (device == this.device && eventCode == UPnPConstant.DEVICE_EVENT_TRANSLATION_SERVICE_READ)
    {
      deviceTranslations.translationsRead();
    }
    // update subscription hint for the service
    if (device == this.device &&
      (eventCode == UPnPConstant.DEVICE_EVENT_SUBSCRIPTION_START || eventCode == UPnPConstant.DEVICE_EVENT_SUBSCRIPTION_END))
    {
      CPService changedService = (CPService)eventParameter;
      // find name button for the changed service to update the subscription hint text
      for (int i = 0; i < getVisibleCPServiceCount(); i++)
      {
        SmoothButton button = getVisibleCPServiceNameButton(i);
        // show hint whether we are subscribed to the service
        if (changedService == device.getCPServiceByID(button.getID()))
        {
          button.setToolTipText("Nicht registriert für Ereignisse");
          if (changedService.isSubscribed())
          {
            button.setToolTipText("Registriert für Ereignisse");
          }

          if (changedService.isMulticastSubscribed())
          {
            button.setToolTipText("Registriert für Multicast-Ereignisse");
          }
        }
      }
    }
  }

  /** Retrieves the number of usable services */
  public int getVisibleCPServiceCount()
  {
    if (useDirectAccessPlugin)
    {
      return serviceDirectAccessNameButtonList.size();
    } else
    {
      return serviceNameButtonList.size();
    }
  }

  /**
   * Returns the name button for a specific service
   */
  public SmoothButton getVisibleCPServiceNameButton(int index)
  {
    if (useDirectAccessPlugin && index >= 0 && index < serviceDirectAccessNameButtonList.size())
    {
      return (SmoothButton)serviceDirectAccessNameButtonList.elementAt(index);
    }
    if (!useDirectAccessPlugin && index >= 0 && index < serviceNameButtonList.size())
    {
      return (SmoothButton)serviceNameButtonList.elementAt(index);
    }
    return null;
  }

  /** Retrieves the CPService associated with a button at a certain index. */
  public CPService getVisibleCPService(int index)
  {
    return device.getCPServiceByID(getVisibleCPServiceNameButton(index).getID());
  }

  /**
   * Returns the plugin for a specific service
   */
  public BaseCPServicePlugin getVisibleCPServicePlugin(int index)
  {
    if (useDirectAccessPlugin && index >= 0 && index < serviceDirectAccessPluginList.size())
    {
      if (serviceDirectAccessPluginList.elementAt(index) == null)
      {
        CPService service = device.getCPServiceByID(getVisibleCPServiceNameButton(index).getID());
        BaseCPServicePlugin servicePlugin =
          new StandardServicePlugin(guiControl, guiControl.getSecurityAwareControlPoint(), this, service);

        // replace null with created plugin
        serviceDirectAccessPluginList.remove(index);
        serviceDirectAccessPluginList.add(index, servicePlugin);
      }
      return (BaseCPServicePlugin)serviceDirectAccessPluginList.elementAt(index);
    }
    if (!useDirectAccessPlugin && index >= 0 && index < servicePluginList.size())
    {
      // create plugins only if they are really requested
      if (servicePluginList.elementAt(index) == null)
      {
        CPService service = device.getCPServiceByID(getVisibleCPServiceNameButton(index).getID());
        BaseCPServicePlugin servicePlugin = null;

        // try to find an appropriate plugin for the service
        Enumeration cpServicePlugins = guiControl.getPluginManager().getCPServicePlugins();
        while (servicePlugin == null && cpServicePlugins.hasMoreElements())
        {
          // enumerate class instances of manager
          BaseCPServicePlugin currentPlugin = (BaseCPServicePlugin)cpServicePlugins.nextElement();

          if (service.getServiceType().equals(currentPlugin.getPluginType()))
          {
            try
            {
              Class pluginClass = guiControl.getPluginManager().getCPServiceClass(currentPlugin);

              // create new instance for plugin
              servicePlugin = (BaseCPServicePlugin)pluginClass.newInstance();
              servicePlugin.setDeviceGUIContextProvider(guiControl);
              servicePlugin.setDeviceGUIContext(this);
              servicePlugin.setFrame(guiControl);
              servicePlugin.setControlPoint(guiControl.getSecurityAwareControlPoint());
              servicePlugin.setCPService(service);
            } catch (Exception ex)
            {
              System.out.println("Error creating plugin: " + ex.getMessage());
              ex.printStackTrace();
            }
          }
        }
        // no plugin found, use standard action plugin
        if (servicePlugin == null)
        {
          servicePlugin =
            new StandardServicePlugin(guiControl, guiControl.getSecurityAwareControlPoint(), this, service);
        }
        // replace null with created plugin
        servicePluginList.remove(index);
        servicePluginList.add(index, servicePlugin);
      }
      return (BaseCPServicePlugin)servicePluginList.elementAt(index);
    }
    return null;
  }

  /**
   * Checks if the plugin is already initialized
   */
  public boolean isInitializedServicePlugin(int index)
  {
    if (index >= 0 && index < servicePluginList.size())
    {
      if (useDirectAccessPlugin)
      {
        return serviceDirectAccessPluginList.elementAt(index) != null;
      } else
      {
        return servicePluginList.elementAt(index) != null;
      }
    }
    return false;
  }

  /**
   * Returns the plugin for the whole device
   */
  public BaseCPDevicePlugin getDevicePlugin()
  {
    if (useDevicePlugin)
    {
      // plugin is not yet initialized
      if (devicePlugin == null)
      {
        Enumeration cpDevicePlugins = guiControl.getPluginManager().getCPDevicePlugins();
        while (devicePlugin == null && cpDevicePlugins.hasMoreElements())
        {
          // enumerate class instances of manager
          BaseCPDevicePlugin currentPlugin = (BaseCPDevicePlugin)cpDevicePlugins.nextElement();
          if (device.getDeviceType().equals(currentPlugin.getPluginType()))
          {
            try
            {
              Class pluginClass = guiControl.getPluginManager().getCPDeviceClass(currentPlugin);

              // create new instance for plugin
              devicePlugin = (BaseCPDevicePlugin)pluginClass.newInstance();
              devicePlugin.setDeviceGUIContextProvider(guiControl);
              devicePlugin.setDeviceGUIContext(this);
              devicePlugin.setFrame(guiControl);
              devicePlugin.setControlPoint(guiControl.getSecurityAwareControlPoint());
              devicePlugin.setCPDevice(device);

            } catch (Exception ex)
            {
            }
          }
        }
      }
      return devicePlugin;
    }
    return null;
  }

  /**
   * Checks if there is a plugin for the whole device
   */
  public boolean isDevicePlugin()
  {
    return useDevicePlugin && !useDirectAccessPlugin;
  }

  /** Checks if this plugin can be used as DIDL resource provider to feed media renderers. */
  public boolean isDIDLResourceProvider()
  {
    return device.getDeviceType().startsWith(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE_START) ||
      device.getDeviceType().startsWith(DeviceConstant.DIGITAL_SECURITY_CAMERA_DEVICE_TYPE) ||
      device.getDeviceType().startsWith(DeviceConstant.POINT_OF_INTEREST_DEVICE_TYPE) ||
      device.getDeviceType().startsWith(DeviceConstant.MESSAGING_DEVICE_TYPE);
  }

  /**
   * Checks if the associated device is valid.
   * 
   * @return True if the device is valid, false otherwise
   */
  public boolean isValid()
  {
    return valid;
  }

  /**
   * Sets the state for the associated device.
   * 
   * @param valid
   *          The new state to set
   */
  public void setValid(boolean valid)
  {
    this.valid = valid;
  }

  /** Checks if this device context should be visible in the current control point state. */
  public boolean isVisibleDeviceGUIContext()
  {
    return guiControl.getVisibilitySimulation().isVisibleDevice(this);
  }

  /** Checks if a specific device should be visible in the current control point state. */
  public boolean isVisibleDevice(CPDevice device)
  {
    return guiControl.getVisibilitySimulation().isVisibleDevice(device);
  }

  /** Event that the visibility of at least one device has changed. */
  public void visibilityChanged()
  {
    // forward event to the associated plugin
    if (devicePlugin != null)
    {
      devicePlugin.visibilityChanged();
    }
    for (int i = 0; i < getVisibleCPServiceCount(); i++)
    {
      if (isInitializedServicePlugin(i))
      {
        getVisibleCPServicePlugin(i).visibilityChanged();
      }
    }
  }

  /**
   * @return the deviceGenre
   */
  public int getDeviceGenre()
  {
    return deviceGenre;
  }

  /** Terminates the device GUI context. */
  public void terminate()
  {
    // terminate all plugins
    if (devicePlugin != null)
    {
      devicePlugin.terminate();
    }

    devicePlugin = null;

    for (int i = 0; i < servicePluginList.size(); i++)
    {
      if (servicePluginList.elementAt(i) != null)
      {
        ((BaseCPServicePlugin)servicePluginList.elementAt(i)).terminate();
      }
    }
    for (int i = 0; i < serviceDirectAccessPluginList.size(); i++)
    {
      if (serviceDirectAccessPluginList.elementAt(i) != null)
      {
        ((BaseCPServicePlugin)serviceDirectAccessPluginList.elementAt(i)).terminate();
      }
    }
    servicePluginList.clear();
    serviceDirectAccessPluginList.clear();
  }

}

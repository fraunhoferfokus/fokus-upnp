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

import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControl;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationService;
import de.fraunhofer.fokus.upnp.core.examples.localization.buttons.ButtonLocalizationEntity;

public class VisibilitySimulation
{
  public static boolean ACTIVATE_SIMULATION = false;

  private GUIControl    guiControl;

  private String        position;

  private Hashtable     positionTable       = new Hashtable();

  /**
   * Creates a new instance of VisibilitySimulation.
   * 
   * @param guiControl
   */
  public VisibilitySimulation(GUIControl guiControl)
  {
    this.guiControl = guiControl;
    position = LocalizationService.UNKNOWN_LOCATION;

    System.out.println("POSITION for " + guiControl.getGUIEntityID() + " is " + position + "\n");
  }

  /**
   * Checks the visibility for a certain device. This is used to simulate visibility over different
   * networks.
   * 
   * @param index
   *          The index in the device context list
   * 
   */
  public boolean isVisibleDevice(int index)
  {
    return isVisibleDevice(guiControl.getDeviceContext(index));
  }

  /**
   * Checks the visibility for a certain device. This is used to simulate visibility over different
   * networks.
   * 
   * @param deviceGUIContext
   *          The device context
   * 
   */
  public boolean isVisibleDevice(DeviceGUIContext deviceGUIContext)
  {
    if (!ACTIVATE_SIMULATION)
    {
      return true;
    }

    if (deviceGUIContext != null)
    {
      CPDevice device = deviceGUIContext.getDevice();
      return isVisibleDevice(device);
    }
    return false;
  }

  /**
   * Checks the visibility for a certain device. This is used to simulate visibility over different
   * networks.
   * 
   * @param device
   *          The device
   * 
   */
  public boolean isVisibleDevice(CPDevice device)
  {
    if (!ACTIVATE_SIMULATION)
    {
      return true;
    }

    if (device != null)
    {
      // device visibility depends on owner, not on user
      String entityID = guiControl.getGUIEntityID();
      // this is dirty, we know...

      // RED car
      if (entityID.equalsIgnoreCase(ButtonLocalizationEntity.ENTITY_IDS[0]))
      {
        // the following devices are always visible for the red car
        if (device.getFriendlyName().indexOf("Localization") != -1)
        {
          return true;
        }
        // chat is visible if the RED car is near the YELLOW car
        if (positionTable.containsKey(ButtonLocalizationEntity.ENTITY_IDS[1]) &&
          positionTable.get(ButtonLocalizationEntity.ENTITY_IDS[1]).equals(position) &&
          !position.equals(LocalizationService.UNKNOWN_LOCATION) && device.getFriendlyName().indexOf("Chat") != -1)
        {
          return true;
        }

        // Home
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[0]))
        {
          if (device.getDeviceType().indexOf("MediaServer") != -1 ||
            device.getDeviceType().indexOf("MediaRenderer") != -1 ||
            device.getDeviceType().indexOf("AVSynchronization") != -1)
          {
            return true;
          }
        }
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[1]))
        {
          if (device.getUDN().equals("uuid:PointOfInterest1.0_10.147.175.145") ||
            device.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE) &&
            device.getUDN().indexOf("accident") == -1)
          {
            return true;
          }
        }
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[3]))
        {
          if (positionTable.containsKey(ButtonLocalizationEntity.ENTITY_IDS[2]) &&
            positionTable.get(ButtonLocalizationEntity.ENTITY_IDS[2]).equals(ButtonLocalizationEntity.LOCATIONS[3]) &&
            device.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE) &&
            device.getUDN().indexOf("accident") != -1)
          {
            return true;
          }
        }
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[2]))
        {
          if (device.getFriendlyName().indexOf("TrafficLights") != -1)
          {
            return true;
          }
        }
      }
      // YELLOW car
      if (entityID.equalsIgnoreCase(ButtonLocalizationEntity.ENTITY_IDS[1]))
      {
        // the following devices are always visible for the yellow car
        if (device.getFriendlyName().indexOf("Chat") != -1 || device.getFriendlyName().indexOf("Localization") != -1 ||
          device.getFriendlyName().indexOf("Car Image Renderer") != -1 ||
          device.getFriendlyName().indexOf("Car Media Server") != -1 || device.getFriendlyName().indexOf("Map") != -1)
        {
          return true;
        }
        // Home
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[0]))
        {
          if (device.getFriendlyName().indexOf("Atlas Media Server") != -1 ||
            device.getFriendlyName().indexOf("AVSynchronization") != -1 ||
            device.getFriendlyName().indexOf("BVG") != -1 || device.getFriendlyName().indexOf("Weather") != -1 ||
            device.getDeviceType().indexOf("DigitalSecurityCamera") != -1 ||
            device.getDeviceType().indexOf("PersonalOrganizer") != -1 ||
            device.getDeviceType().indexOf("MessagingDevice") != -1 ||
            device.getDeviceType().indexOf("EZControl") != -1 || device.getDeviceType().indexOf("DemoRoom") != -1)
          {
            return true;
          }
        }
        // RSU
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[1]))
        {
          if (device.getDeviceType().indexOf("PointOfInterest") != -1 &&
            device.getFriendlyName().indexOf("RSU") != -1 ||
            device.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE) &&
            device.getFriendlyName().indexOf("Accident") == -1)
          {
            return true;
          }
        }
        // Traffic lights
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[2]))
        {
          if (device.getFriendlyName().indexOf("TrafficLights") != -1)
          {
            return true;
          }
        }
        // Accident
        if (position.equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[3]))
        {
          if (positionTable.containsKey(ButtonLocalizationEntity.ENTITY_IDS[2]) &&
            positionTable.get(ButtonLocalizationEntity.ENTITY_IDS[2]).equals(ButtonLocalizationEntity.LOCATIONS[3]) &&
            device.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE) &&
            device.getFriendlyName().indexOf("Accident") != -1)
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Retrieves the position.
   * 
   * @return The position
   */
  public String getPosition()
  {
    return position;
  }

  /**
   * Sets the position.
   * 
   * @param position
   *          The new value for position
   */
  public void setPosition(String position)
  {
    this.position = position;
  }

  /**
   * Retrieves the positionTable.
   * 
   * @return The positionTable
   */
  public Hashtable getPositionTable()
  {
    return positionTable;
  }

}

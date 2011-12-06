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
package de.fraunhofer.fokus.upnp.core.examples.localization.buttons;

import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.control_point.ISubscriptionPolicyListener;
import de.fraunhofer.fokus.upnp.core.examples.localization.ILocationProvider;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationDevice;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationService;
import de.fraunhofer.fokus.upnp.core.examples.sensors.SensorConstants;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class starts a localization service that uses buttons to register positions.
 * 
 * @author Alexander Koenig
 */
public class ButtonLocalizationEntity extends TemplateEntity implements ILocationProvider, ISubscriptionPolicyListener
{

  public static final String[] ENTITY_IDS        = new String[] {
      "RedCar", "YellowCar", "BlueCar"
                                                 };

  public static final String[] LOCATIONS         = new String[] {
      "Home", "RSU", "TrafficLights", "Accident"
                                                 };

  /** UDN of device that is used for localization (on atlas) */
  private static String        BUTTON_DEVICE_UDN = "uuid:e881fcac-0218-514b-b275-66c9de3b47c7";

  private LocalizationDevice   localizationDevice;

  private String               entityIDString;

  private String               locationString;

  private boolean              active            = false;

  /** Creates a new instance of ButtonLocalizationEntity */
  public ButtonLocalizationEntity()
  {
    this(null);
  }

  /** Creates a new instance of ButtonLocalizationEntity */
  public ButtonLocalizationEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    entityIDString = "";
    for (int i = 0; i < ENTITY_IDS.length; i++)
    {
      entityIDString += (entityIDString.length() == 0 ? "" : ", ") + ENTITY_IDS[i];
    }
    locationString = "";
    for (int i = 0; i < LOCATIONS.length; i++)
    {
      locationString += (locationString.length() == 0 ? "" : ", ") + LOCATIONS[i];
    }

    BUTTON_DEVICE_UDN = getStartupConfiguration().getProperty("ButtonDeviceUDN", BUTTON_DEVICE_UDN);
    TemplateDevice.printMessage("Use button device UDN " + BUTTON_DEVICE_UDN);

    localizationDevice = new LocalizationDevice(this, getStartupConfiguration(), this);
    setTemplateDevice(localizationDevice);

    ControlPointStartupConfiguration cpStartupConfiguration =
      (ControlPointStartupConfiguration)getStartupConfiguration().getSingleControlPointStartupConfiguration();
    // force delayed start to set policy listener
    cpStartupConfiguration.setRunDelayed(true);
    TemplateControlPoint templateControlPoint = new TemplateControlPoint(this, getStartupConfiguration());
    templateControlPoint.setSubscriptionPolicyListener(this);
    templateControlPoint.runBasicControlPoint();
    setTemplateControlPoint(templateControlPoint);
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new ButtonLocalizationEntity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#newDevice(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    super.newDevice(newDevice);
    if (newDevice.getUDN().equals(BUTTON_DEVICE_UDN))
    {
      System.out.println("Associated button device found, subscribe to events");
      active = true;
      localizationDevice.getLocalizationService().setActive(true);

      // handle initial state variable values to initialize positions
      for (int i = 0; i < newDevice.getCPServiceTable().length; i++)
      {
        CPService service = newDevice.getCPServiceTable()[i];
        CPStateVariable stateVariable = service.getCPStateVariable("CurrentState");
        if (stateVariable != null)
        {
          handleButtonDeviceStateVariable(stateVariable);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#deviceGone(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    if (goneDevice.getUDN().equals(BUTTON_DEVICE_UDN))
    {
      System.out.println("Associated button device gone");
      active = false;
      localizationDevice.getLocalizationService().setActive(false);
    }
    super.deviceGone(goneDevice);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#stateVariableChanged(de.fhg.fokus.magic.upnp.control_point.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    super.stateVariableChanged(stateVariable);

    // process events from our button device
    if (stateVariable.getCPService().getCPDevice().getUDN().equals(BUTTON_DEVICE_UDN) &&
      stateVariable.getName().equals("CurrentState"))
    {
      handleButtonDeviceStateVariable(stateVariable);
    }
  }

  /** This method processes events from the associated button device. */
  private void handleButtonDeviceStateVariable(CPStateVariable stateVariable)
  {
    try
    {
      // for the current demo, we use locations 0..6 for the yellow car
      for (int i = 0; i < 7; i++)
      {
        // check we use the correct button
        if (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID + i))
        {
          if (stateVariable.getBooleanValue())
          {
            // home
            if (i == 0 || i == 1)
            {
              localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[0]);
            }

            // traffic lights
            if (i == 2)
            {
              localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[2]);
            }

            // RSU
            if (i == 3 || i == 4)
            {
              localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[1]);
            }

            // accident
            if (i == 6)
            {
              localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[3]);
            }
          }
        }
      }
      // reset location for the yellow car if all associated buttons are set to false
      CPDevice device = stateVariable.getCPService().getCPDevice();
      boolean allFalse = true;
      for (int i = 0; i < 7; i++)
      {
        CPService service = device.getCPServiceByID(SensorConstants.BUTTON_SERVICE_ID + i);
        if (service != null)
        {
          CPStateVariable tempStateVariable = service.getCPStateVariable("CurrentState");
          if (tempStateVariable != null)
          {
            allFalse = allFalse && tempStateVariable.getBooleanValue() == false;
          }
        }
      }
      if (allFalse)
      {
        localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LocalizationService.UNKNOWN_LOCATION);
      }

      // the 8th button is used for the accident car
      if (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID + "7"))
      {
        if (stateVariable.getBooleanValue())
        {
          localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[2], LOCATIONS[3]);
        } else
        {
          localizationDevice.getLocalizationService()
            .locationEvent(ENTITY_IDS[2], LocalizationService.UNKNOWN_LOCATION);
        }
      }

      /*
       * // Red car if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "0")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[0], LOCATIONS[0]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[0],
       * LocalizationService.UNKNOWN_LOCATION); } if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "4")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[0], LOCATIONS[1]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[0],
       * LocalizationService.UNKNOWN_LOCATION); } // Yellow car if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "1")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[0]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1],
       * LocalizationService.UNKNOWN_LOCATION); } if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "2")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[2]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1],
       * LocalizationService.UNKNOWN_LOCATION); } if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "3")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[1]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1],
       * LocalizationService.UNKNOWN_LOCATION); } if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "6")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1], LOCATIONS[3]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[1],
       * LocalizationService.UNKNOWN_LOCATION); } // Blue car if
       * (stateVariable.getCPService().getServiceId().equals(SensorConstants.BUTTON_SERVICE_ID +
       * "7")) { if (stateVariable.getBooleanValue())
       * localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[2], LOCATIONS[3]);
       * else localizationDevice.getLocalizationService().locationEvent(ENTITY_IDS[2],
       * LocalizationService.UNKNOWN_LOCATION); }
       */
    } catch (Exception e)
    {
      System.out.println("Error handling button state variable event: " + e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.ISubscriptionPolicyListener#getSubscriptionPolicy(de.fhg.fokus.magic.upnp.control_point.CPService)
   */
  public int getSubscriptionPolicy(CPService service)
  {
    if (service.getCPDevice().getUDN().equals(BUTTON_DEVICE_UDN))
    {
      System.out.println("SUBSCRIBE TO SERVICES OF BUTTON DEVICE");
      return UPnPConstant.SUBSCRIPTION_MODE_AUTOMATIC;
    }

    return UPnPConstant.SUBSCRIPTION_MODE_MANUAL;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // LocationProvider Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String getLocationList()
  {
    return locationString;
  }

  public String getEntityIDList()
  {
    return entityIDString;
  }

  public boolean isActive()
  {
    return active;
  }

}

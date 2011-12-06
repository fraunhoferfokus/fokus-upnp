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
package de.fraunhofer.fokus.upnp.core.examples.localization.gps;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.examples.localization.IGPSProvider;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class provides a GPS service.
 * 
 * @author Alexander Koenig
 */
public class GPSService extends TemplateService
{

  private IGPSProvider  gpsProvider;

  // required state variables
  private StateVariable latitude;

  private StateVariable longitude;

  private StateVariable speed;

  private StateVariable direction;

  private StateVariable active;

  private Action        getLatitude;

  private Action        getLongitude;

  private Action        getSpeed;

  private Action        getDirection;

  private Action        isActive;

  /** Creates a new instance of GPSService */
  public GPSService(TemplateDevice device, IGPSProvider gpsProvider)
  {
    super(device, LocalizationConstant.GPS_SERVICE_TYPE, LocalizationConstant.GPS_SERVICE_ID, false);

    this.gpsProvider = gpsProvider;

    runDelayed();
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // state Variables
    latitude = new StateVariable("Latitude", gpsProvider.getLatitude(), true);
    latitude.setModeration(true, 1000);
    longitude = new StateVariable("Longitude", gpsProvider.getLongitude(), true);
    longitude.setModeration(true, 1000);
    speed = new StateVariable("Speed", gpsProvider.getSpeed(), true);
    speed.setModeration(true, 1000);
    direction = new StateVariable("Direction", gpsProvider.getDirection(), true);
    direction.setModeration(true, 1000);
    active = new StateVariable("Active", false, true);

    StateVariable[] stateVariables = {
        latitude, longitude, speed, direction, active
    };
    setStateVariableTable(stateVariables);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getLatitude = new Action("GetLatitude");
    getLatitude.setArgumentTable(new Argument[] {
      new Argument("Latitude", UPnPConstant.DIRECTION_OUT, latitude)
    });

    getLongitude = new Action("GetLongitude");
    getLongitude.setArgumentTable(new Argument[] {
      new Argument("Longitude", UPnPConstant.DIRECTION_OUT, longitude)
    });

    getSpeed = new Action("GetSpeed");
    getSpeed.setArgumentTable(new Argument[] {
      new Argument("Speed", UPnPConstant.DIRECTION_OUT, speed)
    });

    getDirection = new Action("GetDirection");
    getDirection.setArgumentTable(new Argument[] {
      new Argument("Direction", UPnPConstant.DIRECTION_OUT, direction)
    });
    isActive = new Action("IsActive");
    isActive.setArgumentTable(new Argument[] {
      new Argument("Active", UPnPConstant.DIRECTION_OUT, active)
    });

    setActionTable(new Action[] {
        getLatitude, getLongitude, getSpeed, getDirection, isActive
    });
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getLatitude(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, latitude);
  }

  public void getLongitude(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, longitude);
  }

  public void getSpeed(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, speed);
  }

  public void getDirection(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, direction);
  }

  public void isActive(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, active);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Event that the location has changed */
  public void positionParamsChanged(double latitude, double longitude)
  {
    try
    {
      this.latitude.setDoubleValue(latitude);
      this.longitude.setDoubleValue(longitude);
    } catch (Exception e)
    {
      System.out.println("Error processing event. " + e.getMessage());
    }
  }

  /** Event that the speed or direction has changed. */
  public void moveParamsChanged(double speed, double direction)
  {
    try
    {
      this.speed.setDoubleValue(speed);
      this.direction.setDoubleValue(direction);
    } catch (Exception e)
    {
      System.out.println("Error processing event. " + e.getMessage());
    }
  }

  /** Event that the state of the localization service has changed */
  public void setActive(boolean state)
  {
    try
    {
      if (active.getBooleanValue() != state)
      {
        active.setBooleanValue(state);
      }
    } catch (Exception e)
    {
      System.out.println("Error setting active: " + e.getMessage());
    }
  }

}

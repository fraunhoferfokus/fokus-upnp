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
package de.fraunhofer.fokus.upnp.core.examples.stress;

import java.util.Calendar;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class implements a ClockService that can be used to retrieve the current time.
 * 
 * @author Alexander Koenig
 */
public class ClockService extends TemplateService implements IEventListener
{

  public static final int EVENT_INTERVAL = 1;

  private StateVariable   seconds;

  private StateVariable   A_ARG_TYPE_string;

  private Action          getSeconds;

  private Action          getTime;

  /** Creates a new instance of ClockService */
  public ClockService(TemplateDevice device)
  {
    super(device, DeviceConstant.CLOCK_SERVICE_TYPE, DeviceConstant.CLOCK_SERVICE_ID);
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
    // String variable
    seconds = new StateVariable("Seconds", getCurrentTime(), true);
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);

    StateVariable[] stateVariableList = {
        seconds, A_ARG_TYPE_string
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getSeconds = new Action("GetSeconds");
    getSeconds.setArgumentTable(new Argument[] {
      new Argument("Seconds", UPnPConstant.DIRECTION_OUT, seconds)
    });
    getTime = new Action("GetTime");
    getTime.setArgumentTable(new Argument[] {
      new Argument("Time", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    Action[] actionList = {
        getSeconds, getTime
    };
    setActionTable(actionList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateService#runService()
   */
  public void runService()
  {
    getDevice().getDeviceEventThread().register(this);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // must be equal to the action name but start with a lower case character
  public void getSeconds(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setNumericValue(getCurrentTime());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getTime(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(DateTimeHelper.getUPnPDate());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    try
    {
      int currentTime = getCurrentTime();
      if (currentTime < seconds.getNumericValue() || currentTime - seconds.getNumericValue() >= EVENT_INTERVAL)
      {
        seconds.setNumericValue(currentTime);
      }
    } catch (Exception ex)
    {
    }
  }

  /** Retrieves the passed seconds for the current day */
  protected int getCurrentTime()
  {
    Calendar calendar = Calendar.getInstance();
    // clear time so only the date remains valid
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    // calculate difference from current time
    int currentTime = (int)((System.currentTimeMillis() - calendar.getTimeInMillis()) / 1000);

    return currentTime;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceService#useMulticastEvents()
   */
  public boolean canUseMulticastEvents()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.device.DeviceService#terminate()
   */
  public void terminate()
  {
    TemplateService.printMessage("  Unregister events for service " + toString() + "...");
    getDevice().getDeviceEventThread().unregister(this);
    super.terminate();
  }

}

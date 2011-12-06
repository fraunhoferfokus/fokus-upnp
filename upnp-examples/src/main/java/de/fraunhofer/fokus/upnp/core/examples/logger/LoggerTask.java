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
package de.fraunhofer.fokus.upnp.core.examples.logger;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This thread handles the collection of values for one logging process.
 * 
 * @author sen, Alexander Koenig
 * 
 * 
 */
public class LoggerTask extends Thread
{

  private LoggerManager        loggerManager;

  private TemplateControlPoint controlPoint;

  private CPDevice             cpDevice;

  private CPService            cpService;

  private CPAction             action;

  private String               argumentName;

  private String               description;

  private int                  id;

  private Vector               logEntryList;

  private Calendar             startTime;

  private Calendar             endTime;

  private Calendar             nextLoggingTime;

  private int                  interval;

  private boolean              terminateThread = false;

  private boolean              terminated      = false;

  /**
   * Creates a new instance of LoggerTask.
   * 
   */
  public LoggerTask(LoggerManager loggerManager,
    int id,
    String description,
    CPService cpService,
    CPAction action,
    String argumentName,
    Date startTime,
    Date endTime,
    int interval)
  {
    System.out.println("Create new logger task");
    logEntryList = new Vector();
    this.loggerManager = loggerManager;
    this.id = id;
    this.description = description;
    this.cpService = cpService;
    this.action = action;
    this.cpDevice = cpService.getCPDevice();
    this.controlPoint = loggerManager.getLoggerEntity().getTemplateControlPoint();
    this.argumentName = argumentName;

    this.startTime = DateTimeHelper.dateToCalendar(startTime);
    this.endTime = DateTimeHelper.dateToCalendar(endTime);
    this.interval = interval;

    // create real copy of the start time
    nextLoggingTime = DateTimeHelper.dateToCalendar(this.startTime.getTime());
    System.out.println("Start time is " + DateTimeHelper.formatDateTimeForGermany(startTime));
    System.out.println("End time is " + DateTimeHelper.formatDateTimeForGermany(endTime));

    start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  public void run()
  {
    Calendar currentTime = Calendar.getInstance();
    System.out.println("Run logger thread");
    while (!terminateThread && currentTime.before(endTime))
    {
      // its time for the next measurement
      if (currentTime.after(nextLoggingTime))
      {
        // System.out.println("Try to get new value from remote device");
        if (cpDevice != null && cpService != null && controlPoint != null && action != null)
        {
          try
          {
            controlPoint.invokeAction(action);
            Argument resultArgument = action.getOutArgument(argumentName);
            if (resultArgument != null)
            {
              LogEntry logEntry = new LogEntry(resultArgument.getValueAsString(), Calendar.getInstance().getTime());
              logEntryList.add(logEntry);
            }
          } catch (InvokeActionException e)
          {
            System.out.println("Action failed: " + e.getMessage());
          } catch (ActionFailedException e)
          {
            System.out.println("Action failed with error " + e.getErrorCode() + ": " + e.getErrorDescription());
          } catch (Exception e)
          {
            System.out.println("Action failed: " + e.getMessage());
          }
        }
        nextLoggingTime.add(Calendar.MILLISECOND, interval);
        // System.out.println("Next log time is " +
        // DateTimeHelper.formatDateForGermany(nextLoggingTime.getTime()));
      }
      ThreadHelper.sleep(200);
      currentTime = Calendar.getInstance();
    }
    endTime = currentTime;
    // saveToFile();
    terminated = true;
    loggerManager.taskTerminated(this);
  }

  protected Vector getLogEntryList()
  {
    return logEntryList;
  }

  /**
   * Retrieves the id.
   * 
   * @return The id
   */
  protected int getID()
  {
    return id;
  }

  /**
   * Retrieves the action.
   * 
   * @return The action
   */
  protected CPAction getAction()
  {
    return action;
  }

  /**
   * Retrieves the argumentName.
   * 
   * @return The argumentName
   */
  protected String getArgumentName()
  {
    return argumentName;
  }

  /**
   * Retrieves the cpService.
   * 
   * @return The cpService
   */
  protected CPService getCpService()
  {
    return cpService;
  }

  /**
   * Retrieves the description.
   * 
   * @return The description
   */
  protected String getDescription()
  {
    return description;
  }

  /**
   * Retrieves the endTime.
   * 
   * @return The endTime
   */
  protected Calendar getEndTime()
  {
    return endTime;
  }

  /**
   * Retrieves the interval.
   * 
   * @return The interval
   */
  protected int getInterval()
  {
    return interval;
  }

  /**
   * Retrieves the startTime.
   * 
   * @return The startTime
   */
  protected Calendar getStartTime()
  {
    return startTime;
  }

  /** Terminates the logging task. */
  public void terminate()
  {
    // prevent further state variable changes
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
  }
}

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

import java.util.Date;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class implements a logger service.
 * 
 * @author Sebastian Nauck
 */

public class LoggerService extends TemplateService
{
  /** Reference to logger manager */
  private LoggerManager loggerManager;

  private StateVariable A_ARG_TYPE_string;

  private StateVariable A_ARG_TYPE_int;

  private StateVariable A_ARG_TYPE_boolean;

  private StateVariable A_ARG_TYPE_date;

  private StateVariable finishedTaskIDList;

  private StateVariable activeTaskIDList;

  // private StateVariable dataToBecomeStore;

  private Action        getCSVData;

  private Action        getXMLData;

  private Action        startTask;

  private Action        getFinishedTaskIDList;

  private Action        getActiveTaskIDList;

  private Action        deleteLogData;

  private Action        interruptActiveTask;

  private Action        getLogData;

  /**
   * Creates a new instance of LoggerService.java
   * 
   * @param templateDevice
   * @param loggerProvider
   */
  public LoggerService(LoggerDevice templateDevice)
  {
    super(templateDevice, DeviceConstant.LOGGER_SERVICE_TYPE, DeviceConstant.LOGGER_SERVICE_ID, false);

    loggerManager = templateDevice.getLoggerEntity().getLoggerManager();

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
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_int = new StateVariable("A_ARG_TYPE_int", 0, false);
    A_ARG_TYPE_boolean = new StateVariable("A_ARG_TYPE_boolean", false, false);
    A_ARG_TYPE_date = new StateVariable("A_ARG_TYPE_date", new Date(), false);
    finishedTaskIDList = new StateVariable("FinishedTaskIDList", loggerManager.getFinishedTaskIDList(), true);
    activeTaskIDList = new StateVariable("ActiveTaskIDList", loggerManager.getActiveTaskIDList(), true);
    // dataToBecomeStore = new StateVariable("DataToBecomeStore", "vector",
    // loggerManager.getTempFinishedTaskList(), false);

    StateVariable[] stateVariableList = {
        A_ARG_TYPE_string, A_ARG_TYPE_int, A_ARG_TYPE_boolean, A_ARG_TYPE_date, finishedTaskIDList, activeTaskIDList
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    getCSVData = new Action("GetCSVData");
    getCSVData.setArgumentTable(new Argument[] {
        new Argument("ID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("LogData", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    getXMLData = new Action("GetXMLData");
    getXMLData.setArgumentTable(new Argument[] {
        new Argument("ID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("LogData", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });
    startTask = new Action("StartTask");
    startTask.setArgumentTable(new Argument[] {
        new Argument("Description", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("DeviceUDN", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("ServiceID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("ActionName", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("OutArgument", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("StartTime", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_date),
        new Argument("EndTime", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_date),
        new Argument("Interval", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("ID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_int)
    });

    getFinishedTaskIDList = new Action("GetFinishedTaskIDList");
    getFinishedTaskIDList.setArgumentTable(new Argument[] {
      new Argument("IDList", UPnPConstant.DIRECTION_OUT, finishedTaskIDList)
    });

    getActiveTaskIDList = new Action("GetActiveTaskIDList");
    getActiveTaskIDList.setArgumentTable(new Argument[] {
      new Argument("IDList", UPnPConstant.DIRECTION_OUT, activeTaskIDList)
    });

    deleteLogData = new Action("DeleteLogData");
    deleteLogData.setArgumentTable(new Argument[] {
      new Argument("ID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int)
    });

    interruptActiveTask = new Action("InterruptActiveTask");
    interruptActiveTask.setArgumentTable(new Argument[] {
      new Argument("ID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int)
    });

    getLogData = new Action("GetLogData");
    getLogData.setArgumentTable(new Argument[] {
        new Argument("ID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int),
        new Argument("Data", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    setActionTable(new Action[] {
        getCSVData, getXMLData, startTask, getFinishedTaskIDList, getActiveTaskIDList, deleteLogData,
        interruptActiveTask, getLogData
    });
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // must be equal to the action name but start with a lower case character

  public void getCSVData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      int id = (int)args[0].getNumericValue();

      // load data from file
      String logData = loggerManager.loadLogDataFromFile(id);
      // parse
      LoggerParser parser = new LoggerParser();
      parser.parse(logData);
      // convert to CSV
      args[1].setValue(loggerManager.toCSVString(parser.getLogEntryVector()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getXMLData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    long id = -1;
    try
    {
      id = args[0].getNumericValue();
    } catch (Exception e)
    {
    }
    // search for id in active logger taskslastElement
    for (int i = 0; i < loggerManager.getActiveTaskList().size(); i++)
    {
      LoggerTask currentLoggerTask = (LoggerTask)loggerManager.getActiveTaskList().elementAt(i);
      if (currentLoggerTask.getID() == id)
      {
        String result = "<logData>";
        CPService service = currentLoggerTask.getCpService();
        result += "<description>" + currentLoggerTask.getDescription() + "</description>";
        result += "<deviceUDN>" + service.getCPDevice().getUDN() + "</deviceUDN>";
        result += "<deviceFriendlyName>" + service.getCPDevice().getFriendlyName() + "</deviceFriendlyName>";
        result += "<serviceType>" + service.getServiceType() + "</serviceType>";
        result += "<shortenedServiceID>" + service.getShortenedServiceId() + "</shortenedServiceID>";
        result += "<actionName>" + currentLoggerTask.getAction().getName() + "</actionName>";
        result +=
          "<startTime>" + DateTimeHelper.formatDateForUPnP(currentLoggerTask.getStartTime().getTime()) + "</startTime>";
        result +=
          "<endTime>" + DateTimeHelper.formatDateForUPnP(currentLoggerTask.getEndTime().getTime()) + "</endTime>";
        result += "<interval>" + currentLoggerTask.getInterval() + "</interval>";
        result += "<argument>" + currentLoggerTask.getArgumentName() + "</argument>";
        result += "</logData>";
        try
        {
          args[1].setValue(result);
          return;
        } catch (Exception ex)
        {
        }
      }
    }
    // not in list with active tasks, search in finished task list
    String result = loggerManager.loadLogDataFromFile((int)id);
    if (result == null)
    {
      throw new ActionFailedException(701, "Invalid log ID");
    }

    try
    {
      args[1].setValue(result);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void startTask(Argument[] args) throws ActionFailedException
  {
    if (args.length != 9)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      String logDescription = args[0].getStringValue();
      String deviceUDN = args[1].getStringValue();
      String serviceType = args[2].getStringValue();
      String actionName = args[3].getStringValue();
      String argumentName = args[4].getStringValue();
      Date startTime = args[5].getDateValue();
      Date endTime = args[6].getDateValue();
      long interval = args[7].getNumericValue();

      TemplateControlPoint controlPoint = getTemplateControlPoint();
      CPDevice cpDevice = controlPoint.getCPDeviceByUDN(deviceUDN);
      CPService cpService = cpDevice.getCPServiceByType(serviceType);
      CPAction action = cpService.getCPAction(actionName);

      if (action != null)
      {
        int id =
          loggerManager.newTask(cpService, logDescription, action, argumentName, startTime, endTime, (int)interval);

        args[8].setNumericValue(id);
        return;
      }
      throw new ActionFailedException(702, "Invalid action");
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getFinishedTaskIDList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(finishedTaskIDList.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getActiveTaskIDList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(activeTaskIDList.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void deleteLogData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      int id = (int)args[0].getNumericValue();
      loggerManager.deleteLogDataFile(id);
      idListChanged();
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void interruptActiveTask(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "invalid args");
    }
    try
    {
      int id = (int)args[0].getNumericValue();
      for (int i = 0; i < loggerManager.getActiveTaskList().size(); i++)
      {
        LoggerTask currentLoggerTask = (LoggerTask)loggerManager.getActiveTaskList().elementAt(i);
        if (currentLoggerTask.getID() == id)
        {
          loggerManager.terminateTask(currentLoggerTask);
        }
      }
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getLogData(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "invalid args");
    }
    try
    {
      LoggerTask currentLoggerTask = null;
      Vector logData = new Vector();
      int ID = (int)args[0].getNumericValue();
      Vector tempFinishedTaskList = loggerManager.getTempFinishedTaskList();
      for (int i = 0; i < tempFinishedTaskList.size(); i++)
      {
        currentLoggerTask = (LoggerTask)tempFinishedTaskList.elementAt(i);
        if (currentLoggerTask.getID() == ID)
        {
          logData = currentLoggerTask.getLogEntryList();
          loggerManager.removeElementsFromTempList(currentLoggerTask);
          break;
        }
      }
      args[1].setValue(createXMLDocument(currentLoggerTask, logData));

    } catch (Exception ex)
    {
      System.out.println(ex.getMessage());
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Updates the idList */
  public void idListChanged()
  {
    try
    {
      finishedTaskIDList.setValue(loggerManager.getFinishedTaskIDList());
      activeTaskIDList.setValue(loggerManager.getActiveTaskIDList());
    } catch (Exception e)
    {
      System.out.println("Error setting Id list state variables");
    }
  }

  public String createXMLDocument(LoggerTask currentLoggerTask, Vector logEntryList)
  {

    String result = "<logData>";
    CPService service = currentLoggerTask.getCpService();
    result += "<description>" + currentLoggerTask.getDescription() + "</description>\n";
    result += "<deviceUDN>" + service.getCPDevice().getUDN() + "</deviceUDN>\n";
    result += "<deviceFriendlyName>" + service.getCPDevice().getFriendlyName() + "</deviceFriendlyName>\n";
    result += "<serviceType>" + service.getServiceType() + "</serviceType>\n";
    result += "<shortenedServiceID>" + service.getShortenedServiceId() + "</shortenedServiceID>\n";
    result += "<actionName>" + currentLoggerTask.getAction().getName() + "</actionName>\n";
    result +=
      "<startTime>" + DateTimeHelper.formatDateForUPnP(currentLoggerTask.getStartTime().getTime()) + "</startTime>\n";
    result += "<endTime>" + DateTimeHelper.formatDateForUPnP(currentLoggerTask.getEndTime().getTime()) + "</endTime>\n";
    result += "<interval>" + currentLoggerTask.getInterval() + "</interval>\n";
    result += "<argument>" + currentLoggerTask.getArgumentName() + "</argument>\n";
    result += "<values>\n";

    for (int i = 0; i < logEntryList.size(); i++)
    {
      LogEntry currentEntry = (LogEntry)logEntryList.elementAt(i);
      result += currentEntry.toXMLDescription();
    }
    result += "</values>\n";
    result += "</logData>";

    return result;
  }
}

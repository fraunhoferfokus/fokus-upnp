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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class is used to manage all logging tasks.
 * 
 * @author Alexander Koenig
 * 
 */
public class LoggerManager
{

  private LoggerEntity         loggerEntity;

  private UPnPStartupConfiguration startupConfiguration;

  /** List holding all finished task IDs */
  private Vector               finishedTaskIDList   = new Vector();

  /** List holding the finished logger task temporary */
  private Vector               tempFinishedTaskList = new Vector();

  /** List holding all active logger tasks */
  private Vector               activeTaskList       = new Vector();

  public LoggerManager(LoggerEntity loggerEntity, UPnPStartupConfiguration startupConfiguration)
  {
    this.loggerEntity = loggerEntity;
    this.startupConfiguration = startupConfiguration;

    loadFinishedTaskIDList();
  }

  /**
   * Retrieves the loggerEntity.
   * 
   * @return The loggerEntity
   */
  protected LoggerEntity getLoggerEntity()
  {
    return loggerEntity;
  }

  /**
   * Starts a new logging task.
   * 
   * 
   * @param cpService
   * @param description
   * @param action
   * @param argumentName
   * @param startTime
   * @param endTime
   * @param interval
   * 
   * @return The ID associated with the new task
   */
  public int newTask(CPService cpService,
    String description,
    CPAction action,
    String argumentName,
    Date startTime,
    Date endTime,
    int interval)
  {
    int id = getFreeID();

    System.out.println("Create new logger task with ID " + id);

    LoggerTask loggerTask =
      new LoggerTask(this, id, description, cpService, action, argumentName, startTime, endTime, interval);

    activeTaskList.add(loggerTask);

    // send event to logger service
    getLoggerEntity().getLoggerDevice().getLoggerService().idListChanged();

    return id;
  }

  /** Terminates a logging task. */
  public void terminateTask(LoggerTask loggerTask)
  {
    loggerTask.terminate();
  }

  /** Event that a logger task has been terminated. */
  public void taskTerminated(LoggerTask loggerTask)
  {
    System.out.println("Task " + loggerTask.getID() + " has been terminated");
    finishedTaskIDList.add(new Integer(loggerTask.getID()));
    tempFinishedTaskList.add(loggerTask);
    activeTaskList.remove(loggerTask);
    // send event to logger service
    getLoggerEntity().getLoggerDevice().getLoggerService().idListChanged();
  }

  public Vector getTempFinishedTaskList()
  {
    return tempFinishedTaskList;
  }

  public void removeElementsFromTempList(LoggerTask loggerTask)
  {
    tempFinishedTaskList.remove(loggerTask);
  }

  /** Retrieves an unused ID for a new logging task. */
  public int getFreeID()
  {
    String path = getLoggingPath();

    int abort = 0;
    int result;
    do
    {
      result = new Random().nextInt(1000);
      abort++;
    } while (new File(path + result + ".txt").exists() && abort < 1000);

    return result;
  }

  /** Loads all known logging IDs from a directory. */
  public void loadFinishedTaskIDList()
  {
    System.out.println("LoggerManager: Try to load finished tasks...");
    try
    {
      finishedTaskIDList.clear();

      String path = getLoggingPath();

      File file = new File(path);
      String[] fileList = file.list();
      for (int i = 0; i < fileList.length; i++)
      {
        if (!fileList[i].endsWith(".txt"))
        {
          fileList[i] = "";
        }
      }

      if (fileList != null)
      {
        for (int i = 0; i < fileList.length; i++)
        {
          String filename = fileList[i];

          if (filename.indexOf(".") != -1)
          {
            String idString = filename.substring(0, filename.indexOf("."));
            try
            {
              int id = Integer.parseInt(idString);
              finishedTaskIDList.add(new Integer(id));
            } catch (Exception e)
            {

            }
          }
        }
      }
    } catch (Exception e)
    {
      System.out.println("Error reading tasks: " + e.getMessage());
    }
  }

  /** Retrieves a list with all finished IDs */
  public String getFinishedTaskIDList()
  {
    String idListAsString = finishedTaskIDList.toString();
    String listWithoutBrackets = idListAsString.substring(1, idListAsString.length() - 1);
    return listWithoutBrackets;
  }

  /** Retrieves a list with all active IDs */
  public String getActiveTaskIDList()
  {
    String result = "";
    for (int i = 0; i < activeTaskList.size(); i++)
    {
      result += (i == 0 ? "" : ",") + ((LoggerTask)activeTaskList.elementAt(i)).getID();
    }
    return result;
  }

  /** Loads the raw log data from a file. */
  public String loadLogDataFromFile(int id)
  {
    String path = getLoggingPath();
    String file = path + id + ".txt";

    if (new File(file).exists())
    {
      try
      {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, 200);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[200];
        int readBytes = bufferedInputStream.read(data, 0, data.length);

        while (readBytes != -1)
        {
          byteArrayOutputStream.write(data, 0, readBytes);
          readBytes = bufferedInputStream.read(data, 0, data.length);
        }

        String result = StringHelper.byteArrayToUTF8String(byteArrayOutputStream.toByteArray());

        fileInputStream.close();
        bufferedInputStream.close();
        byteArrayOutputStream.close();

        return result;

      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
    } else
    {
      System.out.println("Could not load log data from " + file);
    }
    return null;
  }

  public Vector getLogEntryList(int id)
  {

    return null;

  }

  /** Deletes a file with log data. */
  public void deleteLogDataFile(int id)
  {
    try
    {
      Integer idObject = new Integer(id);
      finishedTaskIDList.remove(idObject);

      String path = getLoggingPath();
      File file = new File(path + id + ".txt");
      file.delete();
    } catch (Exception e)
    {
      System.out.println("Could not delete log data");
    }
  }

  /** Retrieves the vector containing all active logger tasks. */
  public Vector getActiveTaskList()
  {
    return activeTaskList;
  }

  public void removeLoggerTask(LoggerTask loggerTask)
  {
    activeTaskList.remove(loggerTask);
  }

  public Vector getFinishedTaskList()
  {
    return finishedTaskIDList;
  }

  /** Retrieves the path used to store logging results. */
  public String getLoggingPath()
  {
    String path = "";
    if (System.getProperty("os.name").equalsIgnoreCase("linux"))
    {
      path = startupConfiguration.getSingleDeviceStartupConfiguration().getProperty("LinuxLoggingDirectory");
    }
    if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
    {
      path = startupConfiguration.getSingleDeviceStartupConfiguration().getProperty("WindowsLoggingDirectory");
    }
    path = FileHelper.toValidDirectoryName(path);

    return path;

  }

  /** Store the data in a CSV list for Excel etc. */
  public void saveAsCommaSeparatedValue(Vector logEntryVector, int id, String path)
  {
    try
    {
      // create directory if missing
      if (!new File(path).exists())
      {
        new File(path).mkdirs();
      }

      BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + id + ".CSV"), "utf-8"));

      writer.write("Date");
      writer.write(",");
      writer.write("Value");
      writer.newLine();

      // write values
      for (int i = 0; i < logEntryVector.size(); i++)
      {
        LogEntry currentEntry = (LogEntry)logEntryVector.elementAt(i);

        writer.write(DateTimeHelper.formatDateForUPnP(currentEntry.getDate()));
        writer.write(",");
        writer.write(currentEntry.getValue());
        writer.newLine();
      }
      writer.close();
    } catch (Exception ex)
    {
    }
  }

  /** Converts the data to a CSV list, usable for excel etc. */
  public String toCSVString(Vector logEntryVector)
  {
    StringBuffer result = new StringBuffer(2048);
    result.append("Date");
    result.append(",");
    result.append("Value");
    result.append("\n");

    // write values
    for (int i = 0; i < logEntryVector.size(); i++)
    {
      LogEntry currentEntry = (LogEntry)logEntryVector.elementAt(i);

      result.append(DateTimeHelper.formatDateForUPnP(currentEntry.getDate()));
      result.append(",");
      result.append(currentEntry.getValue());
      result.append("\n");
    }
    return result.toString();
  }

  /** Terminates the logger manager. */
  public void terminate()
  {
    // terminate all running tasks
    Enumeration activeTasks = CollectionHelper.getPersistentEntryEnumeration(activeTaskList);
    while (activeTasks.hasMoreElements())
    {
      ((LoggerTask)activeTasks.nextElement()).terminate();
    }

  }

}

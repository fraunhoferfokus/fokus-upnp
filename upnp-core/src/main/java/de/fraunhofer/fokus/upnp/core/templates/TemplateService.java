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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.event.IDeviceInvokedActionListener;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class should be used as template for all UPnP services.
 * 
 * @author Alexander Koenig
 */
public class TemplateService extends DeviceService implements IDeviceInvokedActionListener
{

  /** Associated logger */
  protected static Logger logger                    = Logger.getLogger("upnp");

  /** Associated UPnP device */
  private TemplateDevice  device;

  /** Working directory for service */
  private String          workingDirectory;

  /** Hashtable to retrieve httpParser for action invocator */
  private Hashtable       httpParserFromThreadTable = new Hashtable();

  /**
   * Creates a new instance of TemplateService.
   * 
   * @param device
   * @param serviceType
   * @param serviceId
   */
  public TemplateService(TemplateDevice device, String serviceType, String serviceId)
  {
    this(device, device.getWorkingDirectory(), serviceType, serviceId, true);
  }

  /**
   * Creates a new instance of TemplateService.
   * 
   * @param device
   * @param workingDirectory
   * @param serviceType
   * @param serviceId
   * @param runImmediately
   */
  public TemplateService(TemplateDevice device,
    String workingDirectory,
    String serviceType,
    String serviceId,
    boolean runImmediately)
  {
    super(device, serviceType, serviceId, device.getIPVersion());
    setActionListener(this);
    // initialization
    this.device = device;
    setWorkingDirectory(workingDirectory);
    if (runImmediately)
    {
      setupServiceVariables();
      initServiceContent();
      runService();
    }
  }

  /**
   * Creates a new instance of TemplateService.
   * 
   * @param device
   * @param serviceType
   * @param serviceId
   * @param runImmediately
   */
  public TemplateService(TemplateDevice device, String serviceType, String serviceId, boolean runImmediately)
  {
    this(device, device.getWorkingDirectory(), serviceType, serviceId, runImmediately);
  }

  /** Prints a message */
  public static void printMessage(String text)
  {
    System.out.println("      " + text);
  }

  /** Procedes with the creation for delayed services */
  public void runDelayed()
  {
    setupServiceVariables();
    initServiceContent();
    runService();
  }

  /** Sets up service specific variables prior to service initialization. */
  public void setupServiceVariables()
  {
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
  }

  /** Can be used to start threads etc. */
  public void runService()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.IDeviceInvokedActionListener#invokeAction(java.lang.String,
   *      de.fhg.fokus.magic.upnp.Action)
   */
  public boolean invokeLocalAction(String serviceID, Action action)
  {
    // delete last error
    action.clearError();
    try
    {
      String actionName = action.getName();
      actionName = actionName.substring(0, 1).toLowerCase() + actionName.substring(1);

      Class serviceClass = getClass();
      Class[] params = new Class[] {
        Argument[].class
      };
      Method actionMethod = serviceClass.getMethod(actionName, params);
      // add the parser that received the current action to hashtable
      if (action.getHTTPParser() != null)
      {
        httpParserFromThreadTable.put(Thread.currentThread(), action.getHTTPParser());
      }

      actionMethod.invoke(this, new Object[] {
        action.getArgumentTable()
      });
      // remove from hashtable
      httpParserFromThreadTable.remove(Thread.currentThread());
      // successful
      return true;
    } catch (InvocationTargetException e)
    {
      // remove from hashtable
      httpParserFromThreadTable.remove(Thread.currentThread());
      // convert UPnP action exceptions to error string
      if (e.getTargetException() instanceof ActionFailedException)
      {
        ActionFailedException afe = (ActionFailedException)e.getTargetException();
        action.setError(afe.getErrorCode(), afe.getErrorDescription());
        return false;
      }
    } catch (Exception ex)
    {
      // remove from hashtable
      httpParserFromThreadTable.remove(Thread.currentThread());
      System.out.println(ex.getClass().getName() + ':' + ex.getMessage());
    }
    return false;
  }

  public TemplateDevice getTemplateDevice()
  {
    return device;
  }

  /** Retrieves a reference to the outer entity */
  public TemplateEntity getTemplateEntity()
  {
    return device.getTemplateEntity();
  }

  /** Retrieves a reference to the associated control point */
  public TemplateControlPoint getTemplateControlPoint()
  {
    if (device.getTemplateEntity() != null)
    {
      return device.getTemplateEntity().getTemplateControlPoint();
    }

    return null;
  }

  /** Retrieves the socket address that received the current action */
  public String getServerAddressForCurrentAction()
  {
    if (httpParserFromThreadTable.containsKey(Thread.currentThread()))
    {
      return ((HTTPParser)httpParserFromThreadTable.get(Thread.currentThread())).getHost();
    }

    return null;
  }

  /** Retrieves the socket address that received the current action */
  public HTTPParser getHTTPParserForCurrentAction()
  {
    if (httpParserFromThreadTable.containsKey(Thread.currentThread()))
    {
      return (HTTPParser)httpParserFromThreadTable.get(Thread.currentThread());
    }

    return null;
  }

  /**
   * Retrieves the working directory for service specific files.
   * 
   * @return The working directory, ending with a separator
   */
  public String getWorkingDirectory()
  {
    return workingDirectory;
  }

  /** Sets the working directory for the service. */
  public void setWorkingDirectory(String directory)
  {
    workingDirectory = FileHelper.toValidDirectoryName(directory);
    // check if working directory exists
    if (!new File(workingDirectory).exists())
    {
      // check if directory relative to common resources would be valid
      if (new File(FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + workingDirectory)).exists())
      {
        workingDirectory = FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + workingDirectory);
      }
    }
  }

  /**
   * This method can be used to handle the request to state variables. This is a convienence method.
   * 
   * @param args
   * @param stateVariable
   * @throws ActionFailedException
   */
  public void handleStateVariableRequest(Argument[] args, StateVariable stateVariable) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(stateVariable.getValue());
      return;
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

}

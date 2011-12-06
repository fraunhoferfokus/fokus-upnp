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
package de.fraunhofer.fokus.upnp.core.examples.control_point;

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.SerializationHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;

/**
 * This class starts multiple UPnP template control points.
 * 
 * @author Alexander Koenig
 */
public class MultipleControlPointEntity extends TemplateEntity implements IKeyListener
{
  private static final int           CONTROL_POINT_COUNT        = 10;

  private Vector                     controlPointList           = new Vector();

  private MultipleControlPointEntity multipleControlPointEntity = this;

  /** Creates a new instance of TemplateEntity */
  public MultipleControlPointEntity()
  {
    super();
    UPnPStartupConfiguration startupConfiguration = getStartupConfiguration();
    int count = startupConfiguration.getNumericProperty("Count", CONTROL_POINT_COUNT);

    ControlPointStartupConfiguration controlPointStartupConfiguration =
      (ControlPointStartupConfiguration)startupConfiguration.getSingleControlPointStartupConfiguration();
    if (controlPointStartupConfiguration == null)
    {
      System.out.println("Missing control point startup info. Exit application");
      System.exit(1);
    }
    Vector configurationList = new Vector();
    System.out.println(DateTimeHelper.formatCurrentDateForDebug() + ": Create " + count + " startup configurations...");
    int portInterval = 3;
    for (int i = 0; i < count; i++)
    {
      controlPointStartupConfiguration.setEventCallbackServerPort(controlPointStartupConfiguration.getEventCallbackServerPort() +
        portInterval);
      if (controlPointStartupConfiguration.getEventCallbackUDPServerPort() != -1)
      {
        controlPointStartupConfiguration.setEventCallbackUDPServerPort(controlPointStartupConfiguration.getEventCallbackUDPServerPort() +
          portInterval);
      }
      controlPointStartupConfiguration.setSSDPUnicastPort(controlPointStartupConfiguration.getSSDPUnicastPort() +
        portInterval);

      // clone startup config
      UPnPStartupConfiguration localStartupConfiguration =
        (UPnPStartupConfiguration)SerializationHelper.clone(getStartupConfiguration());
      if (localStartupConfiguration != null)
      {
        configurationList.add(localStartupConfiguration);
      }
    }
    System.out.println(DateTimeHelper.formatCurrentDateForDebug() + ": Start " + count + " control points...");
    for (int i = 0; i < configurationList.size(); i++)
    {
      new ControlPointStarter((UPnPStartupConfiguration)configurationList.elementAt(i));
    }
    getKeyboardThread().setKeyListener(this);
  }

  public static void main(String[] args)
  {
    new MultipleControlPointEntity();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == 'l')
    {
      System.out.println("Devices (" + controlPointList.size() + " control points):");
      for (int i = 0; i < controlPointList.size(); i++)
      {
        System.out.print(((TemplateControlPoint)controlPointList.elementAt(i)).getCPDeviceCount() + ",");
        if (i % 30 == 0)
        {
          Portable.println("");
        }
      }
      Portable.println("");
    }
    if (code == 's')
    {
      System.out.println("Search devices:");
      for (int i = 0; i < controlPointList.size(); i++)
      {
        ((TemplateControlPoint)controlPointList.elementAt(i)).searchRootDevices();
      }
    }
    if (code == 'o')
    {
      System.out.println("Subscriptions:");
      for (int i = 0; i < controlPointList.size(); i++)
      {
        System.out.print(((TemplateControlPoint)controlPointList.elementAt(i)).getCPDevice(0)
          .getCPServiceByType(DeviceConstant.CLOCK_SERVICE_TYPE)
          .isSubscribed() ? "1," : "0,");

        if (i % 30 == 0)
        {
          Portable.println("");
        }
      }
      Portable.println("");
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    super.terminate();
    System.out.println("Shutdown " + controlPointList.size() + " control points...");
    for (int i = 0; i < controlPointList.size(); i++)
    {
      ((TemplateControlPoint)controlPointList.elementAt(i)).terminate();
    }
    System.out.println("Bye, bye");
  }

  /** Thread to start one control point */
  private class ControlPointStarter extends Thread
  {
    private UPnPStartupConfiguration startupConfiguration;

    public ControlPointStarter(UPnPStartupConfiguration startupConfiguration)
    {
      super();
      this.startupConfiguration = startupConfiguration;
      start();
    }

    public void run()
    {
      controlPointList.add(new TemplateControlPoint(multipleControlPointEntity, startupConfiguration));
    }

  }

}

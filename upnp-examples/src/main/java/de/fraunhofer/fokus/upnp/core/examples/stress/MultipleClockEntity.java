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

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class is used to start multiple clock devices.
 * 
 * @author Alexander Koenig
 */
public class MultipleClockEntity extends TemplateEntity
{
  private Vector deviceList = new Vector();

  /** Creates a new instance of TemplateEntity */
  public MultipleClockEntity()
  {
    super();
    UPnPStartupConfiguration startupConfiguration = getStartupConfiguration();
    String countString = startupConfiguration.getProperty("Count");
    int count = countString != null ? StringHelper.stringToIntegerDef(countString, 10) : 10;
    DeviceStartupConfiguration deviceStartupConfiguration =
      (DeviceStartupConfiguration)startupConfiguration.getSingleDeviceStartupConfiguration();
    if (deviceStartupConfiguration == null)
    {
      System.out.println("Missing device startup info. Exit application");
      System.exit(1);
    }

    String udn = deviceStartupConfiguration.getUDN();
    System.out.println("Start " + count + " devices...");
    for (int i = 0; i < count; i++)
    {
      deviceStartupConfiguration.setHTTPServerPort(deviceStartupConfiguration.getHTTPServerPort() + 2);
      deviceStartupConfiguration.setSSDPUnicastPort(deviceStartupConfiguration.getSSDPUnicastPort() + 2);
      deviceStartupConfiguration.setUDN(udn + "_" + i);
      deviceList.add(new ClockDevice(this, getStartupConfiguration()));
    }
  }

  public static void main(String[] args)
  {
    new MultipleClockEntity();
  }

  public void terminate()
  {
    super.terminate();
    System.out.println("Shutdown " + deviceList.size() + " devices...");
    for (int i = 0; i < deviceList.size(); i++)
    {
      ((ClockDevice)deviceList.elementAt(i)).terminate();
    }
    System.out.println("Bye, bye");
  }

  public String toString()
  {
    return "MultipleClockEntity";
  }
}

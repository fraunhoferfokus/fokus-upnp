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

import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class can be used to create multiple UPnP devices. Devices are created if ports in a predefined port range are
 * available.
 * 
 * @author Alexander Koenig
 */
public class MultipleDeviceTemplateEntity extends TemplateEntity
{

  /** Vector with used ports */
  private Vector                       portList = new Vector();

  protected DeviceStartupConfiguration deviceStartupConfiguration;

  protected int                        basePort;

  protected String                     baseUDN;

  protected String                     baseFriendlyName;

  /** Creates a new instance of MultipleDeviceTemplateEntity */
  public MultipleDeviceTemplateEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    startupConfiguration = getStartupConfiguration();

    deviceStartupConfiguration = (DeviceStartupConfiguration)startupConfiguration.getSingleDeviceStartupConfiguration();
    if (deviceStartupConfiguration == null)
    {
      System.out.println("Missing device startup info. Exit application");
      return;
    }
    basePort = deviceStartupConfiguration.getHTTPServerPort();
    baseUDN = deviceStartupConfiguration.getUDN();
    baseFriendlyName = deviceStartupConfiguration.getFriendlyName();
  }

  /**
   * Tries to reserve two free ports for a new device.
   * 
   * @return The first of the two ports or -1 if all ports are in use.
   */
  public int reservePorts()
  {
    // find free ports
    int port = basePort;
    while (port < basePort + 20 && portList.indexOf(new Integer(port)) != -1)
    {
      port += 2;
    }
    // free port found
    if (port < basePort + 20)
    {
      portList.add(new Integer(port));

      return port;
    }
    return -1;
  }

  /** Frees two ports used by a removed device. */
  public void freePorts(int firstPort)
  {
    portList.remove(new Integer(firstPort));
  }

}

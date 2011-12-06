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
package de.fraunhofer.fokus.upnp.core.control_point;

import java.util.Enumeration;

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.http.HTTPClient;
import de.fraunhofer.fokus.upnp.http.HTTPMessageBuilder;
import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class manages the lease time of UPnP devices.
 * 
 * @author icu, Alexander Koenig
 */
public class CPDeviceLifetimeHandler implements IEventListener
{

  private ControlPoint controlPoint;

  private long         lastCheck;

  /**
   * Creates the class that monitors device lifetimes.
   * 
   * @param controlPoint
   *          Associated control point
   */
  public CPDeviceLifetimeHandler(ControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
  }

  /**
   * Regularly check lifetime for all known devices.
   */
  public void triggerEvents()
  {
    if (System.currentTimeMillis() - lastCheck > 5000)
    {
      lastCheck = System.currentTimeMillis();
      // find deprecated devices
      // use copy because deprecated devices are instantly removed from the original hashtable
      Enumeration devices =
        CollectionHelper.getPersistentElementsEnumeration(controlPoint.getCPDeviceFromDescriptionURLTable());
      while (devices.hasMoreElements())
      {
        CPDevice currentDevice = (CPDevice)devices.nextElement();

        // multicast may not work for different reasons
        // issue a HEAD request to the device description to test for device existence in
        // the last 60 seconds of expected device lifetime
        if (System.currentTimeMillis() + 60000 > currentDevice.getExpectedLifetime() &&
          !currentDevice.isPendingHeadRequest())
        {
          TemplateControlPoint.printMessage(controlPoint.toString() + ": Trigger head request for device: " +
            currentDevice.toDiscoveryString());
          new DeviceRequestThread(currentDevice);
        }
        // remove device
        if (System.currentTimeMillis() > currentDevice.getExpectedLifetime())
        {
          TemplateControlPoint.printMessage(controlPoint.toString() + ": Remove timed out device: " +
            currentDevice.toDiscoveryString());
          controlPoint.removeRootDevice(currentDevice, true);
        }
      }
    }
  }

  /** This thread is used to check for device existence by sending a HEAD request */
  private class DeviceRequestThread extends Thread
  {

    public CPDevice device;

    public DeviceRequestThread(CPDevice device)
    {
      this.device = device;
      // prevent simultanous requests
      device.setPendingHeadRequest(true);

      start();
    }

    public void run()
    {
      System.out.println("Send HEAD request to device to test for device existence.");
      HTTPClient httpClient = new HTTPClient();
      HTTPMessageObject request =
        new HTTPMessageObject(HTTPMessageBuilder.createHEADRequest(device.getDeviceDescriptionURL().getPath(),
          device.getDeviceDescriptionURL().getHost(),
          device.getDeviceDescriptionURL().getPort(),
          ""), device.getDeviceDescriptionURL());

      httpClient.sendRequestAndWaitForResponse(request);
      // we simply check if we get any answer at all
      if (httpClient.getResponse() != null && httpClient.getResponse().getHeader() != null)
      {
        // System.out.println("HEAD request was answered.");
        // renew lease as if the initial lease time was received again
        device.setExpectedLifetime(System.currentTimeMillis() + device.getMaxage() * 1000);
      }
      device.setPendingHeadRequest(false);
    }

  }

}

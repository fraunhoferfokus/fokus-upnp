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
package de.fraunhofer.fokus.upnp.core.examples.localization.gps.network;

import de.fraunhofer.fokus.upnp.core.examples.localization.gps.GPSDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class starts a GPS device that connects to a TCP server.
 * 
 * @author Alexander Koenig
 */
public class TCPGPSEntity extends TemplateEntity
{

  private GPSDevice      gpsDevice;

  private TCPGPSProvider gpsProvider;

  /**
   * Creates a new instance of TCPGPSEntity.
   * 
   */
  public TCPGPSEntity()
  {
    this(null);
  }

  /**
   * Creates a new instance of TCPGPSEntity.
   * 
   * @param startupConfiguration
   */
  public TCPGPSEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    String destination = "localhost:2947";
    destination = getStartupConfiguration().getProperty("SocketAddress", destination);
    gpsProvider = new TCPGPSProvider(this, destination);

    gpsDevice = new GPSDevice(this, getStartupConfiguration(), gpsProvider);
    gpsDevice.getDeviceEventThread().register(gpsProvider);
    setTemplateDevice(gpsDevice);
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new TCPGPSEntity();
  }

  /** Event that the position has changed. */
  public void positionParamsChanged(double latitude, double longitude)
  {
    if (gpsDevice != null)
    {
      gpsDevice.getGPSService().positionParamsChanged(latitude, longitude);
    }
  }

  /** Event that the speed or direction has changed. */
  public void moveParamsChanged(double speed, double direction)
  {
    if (gpsDevice != null)
    {
      gpsDevice.getGPSService().moveParamsChanged(speed, direction);
    }
  }

  /** Event that the connection state has changed. */
  public void activeChanged(boolean active)
  {
    if (gpsDevice != null)
    {
      gpsDevice.getGPSService().setActive(active);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    gpsDevice.getDeviceEventThread().unregister(gpsProvider);
    gpsProvider.terminate();
    super.terminate();
  }
}

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
package de.fraunhofer.fokus.lsf.core.control_point;

import de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider;

/**
 * This class encapsulates a remote view on a binary temperature sensor.
 * 
 * @author Alexander Koenig
 * 
 */
public class TemperatureSensorBinaryCPService extends BinaryCPService implements ITemperatureProvider
{

  /**
   * Creates a new instance of TemperatureBinaryCPService.
   * 
   * @param serviceType
   *          The service type
   * @param serviceID
   *          The ID of the service
   * @param valueType
   *          The type of the value
   * @param valueUnit
   *          The unit of the value
   */
  public TemperatureSensorBinaryCPService(int serviceType, int serviceID, String valueUnit, int valueType)
  {
    super(serviceType, serviceID, valueUnit, valueType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.device.interfaces.ITemperatureProvider#getTemperature()
   */
  public int getTemperature()
  {
    try
    {
      return (int)getNumericValue();
    } catch (Exception e)
    {
    }
    return 0;
  }

}

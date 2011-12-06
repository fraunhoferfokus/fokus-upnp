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
package de.fraunhofer.fokus.upnp.core.examples.clock;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.core.startup.LSFStartupConfiguration;
import de.fraunhofer.fokus.lsf.core.templates.BinaryTemplateEntity;

/**
 * This entity starts a binary clock device.
 * 
 * @author Alexander Koenig
 */
public class BinaryClockEntity extends BinaryTemplateEntity
{

  /**
   * Creates a new instance of BinaryClockEntity.
   * 
   * @param startKeyboardThread
   */
  public BinaryClockEntity(LSFStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    if (getStartupConfiguration() != null && getStartupConfiguration().isValid())
    {
      BinaryClockDevice binaryClockDevice =
        new BinaryClockDevice(getStartupConfiguration().getNumericProperty("ID", 0xFF0001),
          BinaryUPnPConstants.DeviceTypeClock,
          30,
          "Clock",
          "Local",
          "FhG Fokus");
      addBinaryDevice(binaryClockDevice);
    }
  }

  public static void main(String[] args)
  {
    new BinaryClockEntity(args.length > 0 ? new LSFStartupConfiguration(args[0]) : null);
  }
}

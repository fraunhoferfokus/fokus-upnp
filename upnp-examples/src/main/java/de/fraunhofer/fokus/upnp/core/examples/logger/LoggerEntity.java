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

import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

public class LoggerEntity extends TemplateEntity
{

  private TemplateControlPoint controlPoint;

  private LoggerManager        loggerManager;

  private LoggerDevice         loggerDevice;

  /**
   * Creates a new instance of LoggerEntity.
   * 
   * @param startupConfiguration
   */
  public LoggerEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    controlPoint = new TemplateControlPoint(this, getStartupConfiguration());
    setTemplateControlPoint(controlPoint);

    loggerManager = new LoggerManager(this, getStartupConfiguration());

    loggerDevice = new LoggerDevice(this, getStartupConfiguration());
    setTemplateDevice(loggerDevice);
  }

  public static void main(String[] args)
  {
    new LoggerEntity(null);
  }

  /**
   * Retrieves the loggerManager.
   * 
   * @return The loggerManager
   */
  protected LoggerManager getLoggerManager()
  {
    return loggerManager;
  }

  /**
   * Retrieves the loggerDevice.
   * 
   * @return The loggerDevice
   */
  protected LoggerDevice getLoggerDevice()
  {
    return loggerDevice;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    loggerManager.terminate();
    super.terminate();
  }

}

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
package de.fraunhofer.fokus.upnp.core;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.util.logging.ILogRef;

/**
 * This class is used to add the ILogRef interface to log4j loggers.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class Log4JLogRefWrapper implements ILogRef
{

  private Logger internalLogger;

  /**
   * Creates a new instance of Log4JLogRefWrapper.
   * 
   * @param internalLogger
   */
  public Log4JLogRefWrapper(Logger internalLogger)
  {
    this.internalLogger = internalLogger;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#debug(java.lang.String)
   */
  public void debug(String message)
  {
    internalLogger.debug(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#error(java.lang.String)
   */
  public void error(String message)
  {
    internalLogger.error(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#info(java.lang.String)
   */
  public void info(String message)
  {
    internalLogger.info(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#warn(java.lang.String)
   */
  public void warn(String message)
  {
    internalLogger.warn(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#doDebug()
   */
  public boolean doDebug()
  {
    return internalLogger.isDebugEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#doError()
   */
  public boolean doError()
  {
    return internalLogger.isEnabledFor(Level.ERROR);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#doInfo()
   */
  public boolean doInfo()
  {
    return internalLogger.isInfoEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.common.logging.ILogRef#doWarn()
   */
  public boolean doWarn()
  {
    return internalLogger.isEnabledFor(Level.WARN);
  }

}

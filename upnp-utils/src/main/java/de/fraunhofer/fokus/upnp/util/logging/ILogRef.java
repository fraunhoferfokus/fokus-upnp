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
package de.fraunhofer.fokus.upnp.util.logging;

/**
 * This interface is used to allow the injection of a log4j or another logger without introducing
 * dependencies.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public interface ILogRef
{

  /**
   * Checks if the logger is configured to store debug messages.
   * 
   * 
   * @return True if the logger stores debug messages, false otherwise.
   */
  public boolean doDebug();

  /**
   * Checks if the logger is configured to store info messages.
   * 
   * 
   * @return True if the logger stores info messages, false otherwise.
   */
  public boolean doInfo();

  /**
   * Checks if the logger is configured to store warn messages.
   * 
   * 
   * @return True if the logger stores warn messages, false otherwise.
   */
  public boolean doWarn();

  /**
   * Checks if the logger is configured to store error messages.
   * 
   * 
   * @return True if the logger stores error messages, false otherwise.
   */
  public boolean doError();

  /** Stores a debug message */
  public void debug(String message);

  /** Stores an info message */
  public void info(String message);

  /** Stores a warn message */
  public void warn(String message);

  /** Stores an error message */
  public void error(String message);

}

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
package de.fraunhofer.fokus.upnp.core.device;

import de.fraunhofer.fokus.upnp.core.AbstractAction;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.http.HTTPParser;

/**
 * This class represent an UPnP action.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class Action extends AbstractAction
{

  /** Associated httpParser */
  private HTTPParser httpParser;

  /** Flag that this action can be cached. */
  private boolean    cacheable = false;

  /**
   * Creates an action object with only action name.
   * 
   * @param name
   *          action name
   */
  public Action(String name)
  {
    super(name);
  }

  /**
   * Creates an action object with action's name and action's arguments.
   * 
   * @param name
   *          action's name
   * @param argument
   *          action's arguments
   */
  public Action(String name, Argument[] argument)
  {
    super(name, argument);
  }

  /** Clone this action */
  public Object clone()
  {
    if (argumentTable == null || argumentTable.length == 0)
    {
      return new Action(name);
    } else
    {
      // clone argument list (this does not clone the related state variables)
      Argument[] clonedArgumentList = new Argument[argumentTable.length];
      for (int i = 0; i < clonedArgumentList.length; i++)
      {
        clonedArgumentList[i] = (Argument)argumentTable[i].clone();
      }
      return new Action(name, clonedArgumentList);
    }
  }

  /**
   * Retrieves the httpParser.
   * 
   * @return The httpParser
   */
  public HTTPParser getHTTPParser()
  {
    return httpParser;
  }

  /**
   * Sets the httpParser.
   * 
   * @param httpParser
   *          The new value for httpParser
   */
  public void setHTTPParser(HTTPParser httpParser)
  {
    this.httpParser = httpParser;
  }

  /**
   * Retrieves the cacheable flag.
   * 
   * @return The cacheable
   */
  public boolean isCacheable()
  {
    return cacheable;
  }

  /**
   * Sets the cacheable flag.
   * 
   * @param cacheable
   *          The new value for cacheable
   */
  public void setCacheable(boolean cacheable)
  {
    this.cacheable = cacheable;
  }

}

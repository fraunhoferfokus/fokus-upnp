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

import de.fraunhofer.fokus.upnp.core.AbstractStateVariable;
import de.fraunhofer.fokus.upnp.core.exceptions.StateVariableException;

/**
 * This class reflects state variables used by control points.
 * 
 * @author Alexander Koenig
 */
public class CPStateVariable extends AbstractStateVariable
{

  /** The associated service */
  private CPService cpService;

  /**
   * Creates a control point state variable with a defined value
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable as string representation
   * @param isSendEvent
   *          if variable is a send variable or not
   */
  public CPStateVariable(String name, String datatype, String value, boolean isSendEvent, CPService parent)
  {
    super(name, datatype, value, isSendEvent);
    cpService = parent;
  }

  /**
   * Creates a control point state variable with a default value
   * 
   * @param name
   *          name of state variable
   * @param datatype
   *          type of value
   * @param value
   *          value of state variable as string representation
   * @param isSendEvent
   *          if variable is a send variable or not
   */
  public CPStateVariable(String name, String datatype, boolean isSendEvent, CPService parent)
  {
    super(name, datatype, null, isSendEvent);
    cpService = parent;
  }

  /**
   * Returns the service this state variable belongs to.
   * 
   * @return the parent service
   */
  public CPService getCPService()
  {
    return cpService;
  }

  /**
   * Sets the cpService.
   * 
   * @param cpService
   *          The new value for cpService
   */
  public void setCPService(CPService cpService)
  {
    this.cpService = cpService;
  }

  /**
   * Sets state variable value from a string and notifies all registered listeners.
   * 
   * @param newStringValue
   *          state variable value
   * @throws StateVariableException
   *           if value is not in value list
   * @throws ClassCastException
   *           if value casting is not possible
   */
  public void setValueFromString(String newStringValue) throws Exception
  {
    // check new value
    if (!dataType.isValidStringRepresentation(newStringValue))
    {
      throw new StateVariableException("newValue is not instance of " + dataType.getJavaDataType() +
        " or not in range or allowed value list");
    }
    Object newValue = dataType.getValueFromString(newStringValue);
    if (!value.equals(newValue))
    {
      value = newValue;
      // handle event up to service
      cpService.stateVariableChanged(this);
    }
  }

}

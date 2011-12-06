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

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;

/**
 * This class represents a remote view on a CPDevice as discovered by a control point. One CPDevice
 * can be associated with multiple TemplateCPDevices.
 * 
 * @author Alexander Koenig
 */
public class TemplateCPDevice implements ICPStateVariableListener
{

  /** Associated control point */
  protected TemplateControlPoint templateControlPoint;

  /** Associated view on remote device */
  protected CPDevice             device;

  /**
   * Creates a new instance of TemplateCPDevice.
   */
  public TemplateCPDevice(TemplateControlPoint controlPoint, CPDevice device)
  {
    this.templateControlPoint = controlPoint;
    this.device = device;
    this.device.addStateVariableListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {

  }

  /** Retrieves the associated device */
  public CPDevice getCPDevice()
  {
    return device;
  }

  /** Retrieves the associated control point */
  public TemplateControlPoint getTemplateControlPoint()
  {
    return templateControlPoint;
  }

  /**
   * Returns the friendly name of the device.
   * 
   * @return The friendly name
   */
  public String toString()
  {
    return device.getFriendlyName();
  }

  /** Terminates the TemplateCPDevice */
  public void terminate()
  {

  }

}

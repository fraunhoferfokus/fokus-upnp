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
package de.fraunhofer.fokus.lsf.core.templates;

import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;

/**
 * This class can be used as template to map binary UPnP services onto standardized UPnP services.
 * 
 * @author Alexander Koenig
 */
public class BinaryToUPnPTemplateService extends TemplateService
{

  protected StateVariable   application;

  protected StateVariable   name;

  protected Action          getApplication;

  protected Action          getName;

  /** Associated binary CP service */
  protected BinaryCPService binaryCPService;

  /**
   * Creates a new instance of BinaryToUPnPTemplateService. The service must be started by invoking runDelayed() in the
   * derived constructor.
   */
  public BinaryToUPnPTemplateService(TemplateDevice device,
    String serviceType,
    String serviceID,
    BinaryCPService binaryCPService)
  {
    super(device, serviceType, serviceID, false);

    this.binaryCPService = binaryCPService;
  }

  /** Initializes binary UPnP service specific properties */
  public void initBinaryUPnPServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // String variable
    application = new StateVariable("Application", binaryCPService.getBinaryCPDevice().getApplication(), true);
    name = new StateVariable("Name", binaryCPService.getBinaryCPDevice().getName(), true);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getApplication = new Action("GetApplication");
    getApplication.setArgumentTable(new Argument[] {
      new Argument("CurrentApplication", UPnPConstant.DIRECTION_OUT, application)
    });
    getName = new Action("GetName");
    getName.setArgumentTable(new Argument[] {
      new Argument("CurrentName", UPnPConstant.DIRECTION_OUT, name)
    });

  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getApplication(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, application);
  }

  public void getName(Argument[] args) throws ActionFailedException
  {
    handleStateVariableRequest(args, name);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Event that the value of this binary UPnP service has changed. */
  public void binaryUPnPServiceValueChanged()
  {
  }

  /**
   * @return the binaryCPService
   */
  public BinaryCPService getBinaryCPService()
  {
    return binaryCPService;
  }

}

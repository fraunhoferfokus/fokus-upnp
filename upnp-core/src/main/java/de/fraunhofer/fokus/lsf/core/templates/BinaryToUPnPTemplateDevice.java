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

import de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice;
import de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.UUIDHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class encapsulates a device that is derived from a binary UPnP device. This class handles
 * just one binary UPnP service of the associated binary UPnP device.
 * 
 * @author Alexander Koenig
 */
public class BinaryToUPnPTemplateDevice extends TemplateDevice implements IBinaryUPnPDevice
{

  /** Associated binary device. */
  protected BinaryCPDevice binaryCPDevice;

  /**
   * Creates a new instance of BinaryToUPnPTemplateDevice. The device must be started via
   * runDelayed().
   * 
   * @param anEntity
   * @param startupConfiguration
   * @param binaryCPDevice
   */
  public BinaryToUPnPTemplateDevice(TemplateEntity anEntity,
    UPnPStartupConfiguration startupConfiguration,
    BinaryCPDevice binaryCPDevice)
  {
    super(anEntity, startupConfiguration);
    this.binaryCPDevice = binaryCPDevice;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice#getName()
   */
  public String getName()
  {
    return binaryCPDevice.getName();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.IBinaryUPnPDevice#getApplication()
   */
  public String getApplication()
  {
    return binaryCPDevice.getApplication();
  }

  /**
   * Retrieves a service by its type. If more than one service fits, the first service is returned.
   * 
   */
  public BinaryCPService getBinaryCPServiceByType(int serviceType)
  {
    return binaryCPDevice.getCPServiceByType(serviceType);
  }

  /** Retrieves the associated UPnP service for a binary UPnP service. */
  public BinaryToUPnPTemplateService getBinaryToUPnPTemplateServiceByBinaryUPnPService(BinaryCPService service)
  {
    for (int i = 0; i < getTemplateServiceCount(); i++)
    {
      if (getTemplateService(i) instanceof BinaryToUPnPTemplateService &&
        ((BinaryToUPnPTemplateService)getTemplateService(i)).getBinaryCPService() == service)
      {
        return (BinaryToUPnPTemplateService)getTemplateService(i);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateDevice#forceRunDelayed()
   */
  public boolean forceRunDelayed()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.device.Device#getUUID()
   */
  protected String getUUID()
  {
    String udnBase =
      IPHelper.getLocalHostAddressString() + "_" + binaryCPDevice.getName() + "." + binaryCPDevice.getName();

    // Portable.println("Base for binary UUID is " + udnBase);
    return "uuid:" + UUIDHelper.getUUIDFromName(udnBase);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Event Interface //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Event that the value of a binary UPnP service has changed. */
  public void binaryUPnPServiceValueChanged(BinaryCPService binaryCPService)
  {
    // search service that encapsulates this binary UPnP service
    BinaryToUPnPTemplateService currentService = getBinaryToUPnPTemplateServiceByBinaryUPnPService(binaryCPService);
    if (currentService != null)
    {
      currentService.binaryUPnPServiceValueChanged();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.misc.binary_upnp.IBinaryCPDevice#getBinaryCPDevice()
   */
  public BinaryCPDevice getBinaryCPDevice()
  {
    return binaryCPDevice;
  }

}

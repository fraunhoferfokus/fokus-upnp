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

/**
 * This class represent a service in the UPnP device description. This abstract class is the superclass of DeviceService
 * and CPService.
 * 
 * @author icu, Alexander Koenig
 */
public abstract class AbstractService
{

  /** UPnP ServiceType */
  protected String                  serviceType;

  /** UPnP ServiceID */
  protected String                  serviceId;

  /** Array holding all actions. */
  protected AbstractAction[]        actionTable;

  /** Array holding all state variables. */
  protected AbstractStateVariable[] stateVariableTable;

  /** XML description for this service. */
  protected String                  serviceDescription = null;

  /** Used IP version */
  protected int                     IPVersion;

  /**
   * Creates Service object
   * 
   * @param serviceType
   *          servicetype of the service
   * @param serviceId
   *          ID of the service
   * @param IPVersion
   *          IP version
   * 
   */
  public AbstractService(String serviceType, String serviceId, int IPVersion)
  {
    this.serviceType = serviceType;
    this.serviceId = serviceId;
    this.IPVersion = IPVersion;
  }

  /**
   * Returns serviceType
   * 
   * @return serviceType
   */
  public String getServiceType()
  {
    return serviceType;
  }

  /**
   * Returns serviceType version
   * 
   * @return serviceType version
   */
  public int getServiceTypeVersion()
  {
    try
    {
      int result = Integer.parseInt(serviceType.substring(serviceType.lastIndexOf(":") + 1));

      return result;
    } catch (Exception ex)
    {
    }
    return 1;
  }

  /**
   * Returns serviceID
   * 
   * @return serviceID
   */
  public String getServiceId()
  {
    return serviceId;
  }

  /**
   * Returns service id
   * 
   * @return service id
   */
  public String toString()
  {
    int index = serviceType.indexOf("service");
    if (index == -1)
    {
      return serviceType;
    }

    return serviceType.substring(index + 8, serviceType.length() - 2);
  }

  /**
   * Returns the shortened service id (the part after ...:serviceId:)
   * 
   * @return The shortened service id
   */
  public String getShortenedServiceId()
  {
    int index = serviceId.toUpperCase().indexOf("SERVICEID");

    if (index != -1)
    {
      return serviceId.substring(index + 10);
    }

    return serviceId;
  }

  /**
   * Retrieves the iPVersion.
   * 
   * @return The iPVersion
   */
  public int getIPVersion()
  {
    return IPVersion;
  }
}

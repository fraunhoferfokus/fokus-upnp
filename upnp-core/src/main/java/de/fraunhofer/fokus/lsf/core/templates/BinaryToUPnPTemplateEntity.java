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
import de.fraunhofer.fokus.lsf.core.control_point.BinaryControlPoint;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener;
import de.fraunhofer.fokus.lsf.core.event.IBinaryCPServiceValueListener;
import de.fraunhofer.fokus.upnp.core.templates.MultipleDeviceTemplateEntity;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;

/**
 * This class creates UPnP devices for all found binary UPnP devices that export a certain binary UPnP service type.
 * 
 * @author Alexander Koenig
 */
public abstract class BinaryToUPnPTemplateEntity extends MultipleDeviceTemplateEntity implements
  IBinaryCPDeviceEventListener,
  IBinaryCPServiceValueListener
{

  /** Entity containing the binary UPnP control point. */
  protected BinaryTemplateEntity binaryTemplateEntity;

  /** Requested binary UPnP service type. */
  protected int                  binaryUPnPServiceType;

  /** Sync object */
  protected Object               lock = new Object();

  /**
   * Creates a new instance of BinaryToUPnPTemplateEntity.
   * 
   * @param startupConfiguration
   * @param binaryUPnPServiceType
   */
  public BinaryToUPnPTemplateEntity(UPnPStartupConfiguration startupConfiguration, int binaryUPnPServiceType)
  {
    super(startupConfiguration);

    this.binaryUPnPServiceType = binaryUPnPServiceType;

    // start binary entity
    binaryTemplateEntity = new BinaryTemplateEntity(null);
    binaryTemplateEntity.setBinaryControlPoint(new BinaryControlPoint(binaryTemplateEntity));
    binaryTemplateEntity.setDeviceEventListener(this);
    binaryTemplateEntity.setServiceValueListener(this);
    // send search all after listener have been registered
    binaryTemplateEntity.getBinaryControlPoint().sendSearchAllMessage();
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#addTemplateDevice(de.fraunhofer.fokus.upnp.core.templates.TemplateDevice)
   */
  public boolean addTemplateDevice(TemplateDevice device)
  {
    if (device instanceof IBinaryUPnPDevice)
    {
      return super.addTemplateDevice(device);
    }

    return false;
  }

  /**
   * Creates a new UPnP device, based on the found binary UPnP service. This method must be overridden in descendant
   * classes.
   * 
   * @param newDevice
   * @param service
   * @return
   */
  public BinaryToUPnPTemplateDevice createNewDevice(BinaryCPDevice newDevice, BinaryCPService service)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#newDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void newDevice(BinaryCPDevice newDevice)
  {
    // check if device contains the requested service type
    BinaryCPService service = newDevice.getCPServiceByType(binaryUPnPServiceType);
    if (service != null)
    {
      synchronized(lock)
      {
        // find free ports
        int port = reservePorts();
        // free port found
        if (port != -1)
        {
          deviceStartupConfiguration.setUDN(baseUDN + "_" + port);
          deviceStartupConfiguration.setHTTPServerPort(port);
          deviceStartupConfiguration.setSSDPUnicastPort(port + 1);
          // make friendly name unique using the application
          deviceStartupConfiguration.setFriendlyName(baseFriendlyName + " " + newDevice.getName() + "." +
            newDevice.getApplication());

          // create new UPnP device
          BinaryToUPnPTemplateDevice device = createNewDevice(newDevice, service);
          if (device != null)
          {
            addTemplateDevice(device);
          }
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#changedDevice(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice, int)
   */
  public void changedDevice(BinaryCPDevice changedDevice, int eventCode)
  {
  }

  /**
   * Returns an embedded device that is based on a binary UPnP device. This should always work, because this entity type
   * only accepts BinaryToUPnPTemplateDevice instances.
   */
  public BinaryToUPnPTemplateDevice getBinaryToUPnPTemplateDevice(int index)
  {
    return (BinaryToUPnPTemplateDevice)getTemplateDevice(index);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPServiceValueListener#valueChanged(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPService)
   */
  public void valueChanged(BinaryCPService binaryCPService)
  {
    // look if change is interesting for us
    if (binaryCPService.getServiceType() == binaryUPnPServiceType)
    {
      // find UPnP device for that service
      for (int i = 0; i < getTemplateDeviceCount(); i++)
      {
        BinaryToUPnPTemplateDevice currentDevice = getBinaryToUPnPTemplateDevice(i);
        // forward event to UPnP device
        if (currentDevice.getBinaryCPDevice() == binaryCPService.getBinaryCPDevice())
        {
          currentDevice.binaryUPnPServiceValueChanged(binaryCPService);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.lsf.core.event.IBinaryCPDeviceEventListener#deviceGone(de.fraunhofer.fokus.lsf.core.control_point.BinaryCPDevice)
   */
  public void deviceGone(BinaryCPDevice goneDevice)
  {
    synchronized(lock)
    {
      int i = 0;
      // find UPnP device for the removed binary device
      while (i < getTemplateDeviceCount())
      {
        BinaryToUPnPTemplateDevice currentDevice = getBinaryToUPnPTemplateDevice(i);
        // terminate and remove associated UPnP device
        if (currentDevice.getBinaryCPDevice() == goneDevice)
        {
          try
          {
            int port = currentDevice.getDeviceDescriptionSocketAddress().getPort();
            freePorts(port);
          } catch (Exception e)
          {
          }
          currentDevice.terminate();
          removeTemplateDevice(currentDevice);

          return;
        } else
        {
          i++;
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.templates.TemplateEntity#terminate()
   */
  public void terminate()
  {
    binaryTemplateEntity.terminate();
    super.terminate();
  }

}

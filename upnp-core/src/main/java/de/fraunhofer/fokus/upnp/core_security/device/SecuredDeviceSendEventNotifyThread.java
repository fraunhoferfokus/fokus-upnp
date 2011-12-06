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
package de.fraunhofer.fokus.upnp.core_security.device;

import java.net.URL;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.device.Device;
import de.fraunhofer.fokus.upnp.core.device.DeviceSendEventNotifyThread;
import de.fraunhofer.fokus.upnp.core.device.DeviceSubscribedControlPointHandler;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class is used to send signed or encrypted GENA event NOTIFY messages to a subscriber.
 * 
 * @author Alexander Koenig
 */
public class SecuredDeviceSendEventNotifyThread extends DeviceSendEventNotifyThread
{

  /** Creates a new instance of SecuredDeviceSendEventNotifyThread */
  public SecuredDeviceSendEventNotifyThread(DeviceSubscribedControlPointHandler deviceSubscribedControlPointThread)
  {
    super(deviceSubscribedControlPointThread);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceSendEventNotifyThread#createRequest(java.util.Hashtable,
   *      java.net.URL)
   */
  public HTTPMessageObject createRequest(Hashtable messageOptions, URL targetURL)
  {
    HTTPMessageObject result = super.createRequest(messageOptions, targetURL);

    String sid = (String)messageOptions.get(GENAConstant.SID);
    // messages without SID are multicast events which can not be secured
    if (sid == null)
    {
      return result;
    }

    // retrieve the secured device
    Device device = deviceSubscribedControlPointThread.getDeviceService().getDevice();
    SecuredTemplateDevice securedDevice = null;
    if (device instanceof SecuredTemplateDevice)
    {
      securedDevice = (SecuredTemplateDevice)device;
    }
    // retrieve the symmetric key object
    SecurityAwareControlPointObject controlPointObject = null;
    if (securedDevice != null)
    {
      controlPointObject = securedDevice.getControlPointObjectBySID(sid);
    }

    System.out.println("SendEvent:Key name for event message is " + controlPointObject.getSymmetricKeyName());

    if (securedDevice != null && controlPointObject != null &&
      (securedDevice.needsEncryptedEvents() || securedDevice.needsSignedEvents()))
    {

      // build signature content
      String signatureContent = result.getBodyAsUTF8String();
      signatureContent += controlPointObject.getSymmetricKeyName();
      signatureContent += sid;
      signatureContent += currentEventKey;

      SecuredMessageHelper.trySecureResponseMessage(result,
        controlPointObject.getSymmetricKeyInfo(),
        signatureContent,
        securedDevice.needsEncryptedEvents());
    }
    return result;
  }

}

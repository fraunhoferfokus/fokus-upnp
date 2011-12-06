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

import java.net.InetSocketAddress;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.AbstractDevice;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.DeviceMSearchMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.ssdp.MSearchMessageProcessorResult;
import de.fraunhofer.fokus.upnp.ssdp.NotifyMessageBuilder;
import de.fraunhofer.fokus.upnp.ssdp.SSDPConstant;

/**
 * This class processes M-SEARCH messages and creates the appropriate responses for secured devices.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecuredDeviceMSearchMessageProcessor extends DeviceMSearchMessageProcessor
{

  /** Associated secured device */
  private SecuredTemplateDevice securedDevice;

  /**
   * Creates a new instance of DeviceMSearchMessageProcessor.
   * 
   * @param device
   */
  public SecuredDeviceMSearchMessageProcessor(SecuredTemplateDevice securedDevice)
  {
    super(securedDevice);
    this.securedDevice = securedDevice;
  }

  /**
   * Creates all response messages for a device for a received M-SEARCH message.
   * 
   * @param httpServerAddress
   *          Address of the server that should receive resulting GET requests
   * @param message
   *          The received message
   * 
   * @return All generated response messages as a vector of message strings
   */
  public MSearchMessageProcessorResult processMessage(InetSocketAddress httpServerAddress, String message)
  {
    if (securedDevice != null)
    {
      if (securedDevice.isAnonymousDiscovery())
      {
        return processMessageForAnonymousDevice(securedDevice, httpServerAddress, message, securedDevice.getIPVersion());
      } else
      {
        return processMessage(securedDevice, httpServerAddress, message, securedDevice.getIPVersion());
      }
    }
    return null;
  }

  /**
   * Creates only root device response messages for a secured device.
   * 
   * @param abstractDevice
   *          The device that should answer the request
   * @param httpServerAddress
   *          Address of the server that should receive resulting GET requests
   * @param message
   *          The received message
   * 
   * @return All generated response messages as a vector of message strings
   */
  public static MSearchMessageProcessorResult processMessageForAnonymousDevice(AbstractDevice abstractDevice,
    InetSocketAddress httpServerAddress,
    String message,
    int IPVersion)
  {
    HTTPParser httpParser = new HTTPParser();
    httpParser.parse(message);
    int mxValue;
    if (httpParser.isMSEARCHMessage() && httpParser.getMethodValue().equals("*") &&
      httpParser.getValue(HTTPConstant.MAN).equals(SSDPConstant.SSDP_DISCOVER))
    {
      String STValue = httpParser.getValue(SSDPConstant.ST);

      // store MX value
      mxValue = (int)httpParser.getNumericValue(HTTPConstant.MX);

      // search all or search root device message
      if (STValue.equals(SSDPConstant.SSDP_ALL) || STValue.equals(UPnPConstant.UPNP_ROOTDEVICE))
      {
        Vector result = new Vector();
        result.addElement(NotifyMessageBuilder.createRootDeviceResponseMessage(abstractDevice.getUDN(),
          IPVersion,
          abstractDevice.getMaxage(),
          abstractDevice.getDeviceDescriptionURL(httpServerAddress),
          abstractDevice.getServer(),
          abstractDevice.getNLS()));

        return new MSearchMessageProcessorResult(result, mxValue);
      }
      // search device uuid message
      else if (STValue.startsWith(UPnPConstant.DEVICE_UUID))
      {
        logger.info("Search device uuid " + STValue + "request received");

        return new MSearchMessageProcessorResult(createDeviceUUIDResponse(abstractDevice,
          httpServerAddress,
          STValue,
          IPVersion), mxValue);
      }
    }
    return null;
  }

}

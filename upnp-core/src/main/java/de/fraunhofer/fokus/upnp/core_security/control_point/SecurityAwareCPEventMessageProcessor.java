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
package de.fraunhofer.fokus.upnp.core_security.control_point;

import java.net.InetSocketAddress;

import de.fraunhofer.fokus.upnp.core.control_point.CPEventMessageProcessor;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core_security.SecuredMessageHelper;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.gena.GENAConstant;
import de.fraunhofer.fokus.upnp.gena.GENAParseException;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class processes incoming GENA notify event messages. It also understands proprietary signed
 * event messages.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SecurityAwareCPEventMessageProcessor extends CPEventMessageProcessor
{

  private SecurityAwareTemplateControlPoint securityAwareTemplateControlPoint;

  /**
   * Creates a new instance of CPEventMessageProcessor.
   * 
   * @param securityAwareControlPoint
   *          The associated control point
   */
  public SecurityAwareCPEventMessageProcessor(ControlPoint controlPoint)
  {
    super(controlPoint);
  }

  /**
   * Processes an event NOTIFY message.
   * 
   * @param request
   *          The NOTIFY message
   * 
   * @return HTTP OK or HTTP error messages
   * 
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    // System.out.println("Received event message from " +
    // IPAddress.toString(request.getSourceAddress()));
    InetSocketAddress serverAddress = request.getDestinationAddress();
    try
    {
      HTTPParser notifyParser = new HTTPParser();
      notifyParser.parse(request);

      if (notifyParser.isEventNOTIFYMessage())
      {
        // this class also handles unsigned messages

        // event messages can be symmetric signed and/or encrypted
        String keyName = notifyParser.getValue(HTTPConstant.X_KEY_ID);
        if (keyName != null)
        {
          // search symmetric key info
          SecurityAwareCPDeviceObject deviceObject =
            securityAwareTemplateControlPoint.getSecurityAwareCPDeviceObjectByKeyName(keyName);

          if (deviceObject == null)
          {
            System.out.println("Could not find symmetric key for the remote device");
            return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
          }
          // check if event message is encrypted
          SecuredMessageHelper.tryDecryptMessageBody(request, notifyParser, deviceObject.getSymmetricKeyInfo());

          String receivedSignatureContent = request.getBodyAsUTF8String();
          receivedSignatureContent += keyName;
          receivedSignatureContent += notifyParser.getValue(GENAConstant.SID);
          receivedSignatureContent += notifyParser.getValue(GENAConstant.SEQ);

          // check if event message is signed
          if (!SecuredMessageHelper.tryVerifySignature(request,
            notifyParser,
            deviceObject.getSymmetricKeyInfo(),
            receivedSignatureContent))
          {
            System.out.println("Received event message with invalid signature");
            return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_412, serverAddress);
          }
        }
        // signature is valid, so we proceed with the normal event handling
        return processEventNotifyMessage(request, notifyParser);
      } else
      {
        return new HTTPMessageObject(HTTPConstant.HTTP_ERROR_400, serverAddress);
      }
    } catch (GENAParseException ge)
    {
      return new HTTPMessageObject(ge.getMessage(), serverAddress);
    }
  }

  /**
   * Sets the securityAwareControlPoint.
   * 
   * @param securityAwareControlPoint
   *          The new value for securityAwareControlPoint
   */
  public void setSecurityAwareTemplateControlPoint(SecurityAwareTemplateControlPoint securityAwareControlPoint)
  {
    this.securityAwareTemplateControlPoint = securityAwareControlPoint;
  }

}

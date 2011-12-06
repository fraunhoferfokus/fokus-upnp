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
package de.fraunhofer.fokus.upnp.gateway.common.message_forwarder;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.http.IHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;

/**
 * This class receives HTTP messages that must be forwarded to another network interface on the same
 * machine. All work has been sourced out to HTTPManagement.
 * 
 * It implements the IHTTPMessageProcessor interface.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class GatewayHTTPMessageProcessor implements IHTTPMessageProcessor
{

  private static Logger    logger = Logger.getLogger("upnp");

  private MessageForwarder messageForwarder;

  public GatewayHTTPMessageProcessor(MessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
  }

  /** Retrieves the used IP version */
  public int getIPVersion()
  {
    return messageForwarder.getTemplateControlPoint().getIPVersion();
  }

  /**
   * Receives a message that is targeted to another IP. The method retrieves the IForwarderModule
   * that received the message and forwards the message to that module.
   * 
   * @param incomingInterfaceAddress
   *          The socket address of the server that received the request
   * @param request
   *          The message
   * 
   * @return response message
   * 
   */
  public HTTPMessageObject processMessage(HTTPMessageObject request)
  {
    logger.info("new message received.");

    if (request == null || request.getHeader() == null)
    {
      return null;
    }

    // retrieve module that received the HTTP message
    IForwarderModule sourceModule =
      messageForwarder.getForwarderModuleByHTTPServerAddress(request.getDestinationAddress());

    System.out.println("      " + sourceModule.toString() + ".Received message: " + request.toString());

    // let receiving module decide if the message is processed
    HTTPMessageObject response = sourceModule.receivedHTTPMessage(request);

    System.out.println("      " + sourceModule.toString() + ".Return response : " + response.toString());

    return response;
  }
}

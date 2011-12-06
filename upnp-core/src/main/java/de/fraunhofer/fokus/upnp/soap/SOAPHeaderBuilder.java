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
package de.fraunhofer.fokus.upnp.soap;

import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.AbstractAction;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPHeaderBuilder;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.URLHelper;

/**
 * This class provides helper methods for SOAP header line creation.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class SOAPHeaderBuilder extends HTTPHeaderBuilder
{

  /**
   * SOAP logger
   */
  protected static Logger logger = Logger.getLogger("upnp.soap");

  /**
   * Creates a Post or MPost message header.
   * 
   * @param controlURL
   *          Control URL of remote device
   * @param serviceType
   *          Associated service type
   * @param action
   *          Associated action
   * @param optionalHeaderLines
   *          A vector containing additional headers for the SOAP request
   * @param bytesInBody
   *          Size of body
   * @param isMPost
   *          True for MPOST requests, false for POST requests
   * 
   * @return UPnPInvokeAction Messageheader
   */
  public static String buildActionRequestHeader(URL controlURL,
    String serviceType,
    AbstractAction action,
    Vector optionalHeaderLines,
    int bytesInBody,
    boolean isMPost)
  {
    String pathOfControlURL = URLHelper.getURLPath(controlURL);
    String hostOfControlURL = controlURL.getHost();
    int portOfControlURL = controlURL.getPort();
    if (portOfControlURL == -1)
    {
      portOfControlURL = CommonConstants.HTTP_DEFAULT_PORT;
    }
    String soapAction = "\"" + serviceType + "#" + action.getName() + "\"";

    StringBuffer result = new StringBuffer();

    result.append(buildPOST(pathOfControlURL));
    result.append(buildHost(hostOfControlURL + ":" + portOfControlURL));
    result.append(buildContentLength(String.valueOf(bytesInBody)));
    result.append(buildContentType(HTTPConstant.CONTENT_TYPE_TEXT_XML_UTF8));

    if (isMPost)
    {
      result.append(buildMAN(SOAPConstant.ENVELOPE_URL + "; " + SOAPConstant.NS + "01"));
      result.append("01-" + buildHeader(SOAPConstant.SOAPACTION, soapAction));
    } else
    {
      result.append(buildHeader(SOAPConstant.SOAPACTION, soapAction));
    }
    // add optional headers
    for (int i = 0; optionalHeaderLines != null && i < optionalHeaderLines.size(); i++)
    {
      result.append((String)optionalHeaderLines.elementAt(i));
    }

    // logger.debug(result.toString());

    return result.toString();
  }

  /**
   * Builds the header for an action response message.
   * 
   * @param responseCode
   *          HTTP OK if request was accepted other error code if not
   * @param bytesInBody
   *          content lenght of the body
   * @param date
   *          date when response was created
   * @param serverString
   *          OS/version a UPnP-Constant and the product version
   * @return header of the response message to a soap action request
   */
  public static String buildActionResponseHeader(String responseCode,
    int bytesInBody,
    String date,
    String serverString,
    Vector optionalHeaderLines)
  {
    StringBuffer header = new StringBuffer();
    header.append(responseCode + CommonConstants.NEW_LINE);
    header.append(buildContentLength(String.valueOf(bytesInBody)));
    header.append(buildContentType(HTTPConstant.CONTENT_TYPE_TEXT_XML_UTF8));
    header.append(buildDate(date));
    header.append(buildEXT());
    header.append(buildServer(serverString));

    if (optionalHeaderLines != null)
    {
      for (int i = 0; i < optionalHeaderLines.size(); i++)
      {
        header.append((String)optionalHeaderLines.elementAt(i));
      }
    }
    // logger.debug(header.toString());

    return header.toString();
  }

}

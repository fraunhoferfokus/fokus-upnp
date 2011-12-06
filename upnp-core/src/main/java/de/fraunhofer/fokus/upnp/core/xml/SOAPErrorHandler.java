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
package de.fraunhofer.fokus.upnp.core.xml;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class parses a SOAP error response.
 * 
 * @author Alexander Koenig
 * 
 */
public class SOAPErrorHandler extends SAXTemplateHandler
{

  private String errorCode        = "";

  private String errorDescription = "";

  public boolean isValid()
  {
    return errorCode.length() > 0 && errorDescription.length() > 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    if (getTagCount() == 6 && getTag(3).equals("detail") && getTag(4).equals("UPnPError"))
    {
      if (getCurrentTag().equals("errorCode"))
      {
        errorCode = content;
      }

      if (getCurrentTag().equals("errorDescription"))
      {
        errorDescription = content;
      }
    }
  }

  /**
   * Retrieves the errorCode.
   * 
   * @return The errorCode
   */
  public String getErrorCode()
  {
    return errorCode;
  }

  /**
   * Retrieves the errorDescription.
   * 
   * @return The errorDescription
   */
  public String getErrorDescription()
  {
    return errorDescription;
  }

}

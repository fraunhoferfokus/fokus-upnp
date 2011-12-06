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

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.startup.ChildStartupConfiguration;

/**
 * This class includes all parameters that are needed by an UPnP device to get started.
 * 
 * @author Alexander Koenig
 * 
 */
public class GatewayStartupConfiguration extends ChildStartupConfiguration
{
  /**
   * 
   */
  private static final long serialVersionUID      = 1L;

  private boolean           useFixedPorts         = false;

  private int               httpServerPort        = -1;

  private int               httpOverUDPServerPort = -1;

  /**
   * Creates a new instance of GatewayStartupConfiguration.
   * 
   * @param parentHandler
   *          The parent startup configuration
   * 
   */
  public GatewayStartupConfiguration(UPnPStartupConfiguration parentHandler)
  {
    super(parentHandler);
    this.parentHandler = parentHandler;
  }

  /**
   * Retrieves the httpServerPort.
   * 
   * @return The httpServerPort.
   */
  public int getHTTPServerPort()
  {
    return httpServerPort;
  }

  /**
   * Sets the httpServerPort.
   * 
   * @param httpServerPort
   *          The httpServerPort to set.
   */
  public void setHTTPServerPort(int httpServerPort)
  {
    this.httpServerPort = httpServerPort;
  }

  /**
   * Retrieves the httpOverUDPServerPort.
   * 
   * @return The httpOverUDPServerPort.
   */
  public int getHTTPOverUDPServerPort()
  {
    return httpOverUDPServerPort;
  }

  /**
   * Sets the httpOverUDPServerPort.
   * 
   * @param httpServerPort
   *          The httpServerPort to set.
   */
  public void setHTTPOverUDPServerPort(int httpServerPort)
  {
    this.httpOverUDPServerPort = httpServerPort;
  }

  /**
   * Retrieves the useFixedPorts.
   * 
   * @return The useFixedPorts.
   */
  public boolean useFixedPorts()
  {
    return useFixedPorts;
  }

  /**
   * Sets the useFixedPorts.
   * 
   * @param useFixedPorts
   *          The useFixedPorts to set.
   */
  public void setUseFixedPorts(boolean useFixedPorts)
  {
    this.useFixedPorts = useFixedPorts;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    super.processContentElement(content);

    if (getTagCount() == 2)
    {
      if (getCurrentTag().equals(UPnPStartupConfiguration.GATEWAY_SERVER_PORT))
      {
        httpServerPort = StringHelper.stringToIntegerDef(content, -1);
      }

      if (getCurrentTag().equals(UPnPStartupConfiguration.GATEWAY_UDP_SERVER_PORT))
      {
        httpOverUDPServerPort = StringHelper.stringToIntegerDef(content, -1);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    super.processEndElement(uri, localName, qName);
    // set fixed ports state
    if (getTagCount() == 1)
    {
      useFixedPorts = httpServerPort != -1 || httpOverUDPServerPort != -1;
      // store the parsed description
      parentHandler.addGatewayStartupInfo(this);
      parentHandler.handleSAXEvents();
    }
  }

  /**
   * Checks if this description is valid for devices.
   * 
   * @return The valid.
   */
  public boolean isValidGatewayConfiguration()
  {
    return friendlyName.length() > 0;
  }

}

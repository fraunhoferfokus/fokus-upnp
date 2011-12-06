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
package de.fraunhofer.fokus.lsf.core.startup;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.startup.ChildStartupConfiguration;

/**
 * This class can be used as base class for inner startup configurations.
 * 
 * @author Alexander Koenig
 * 
 */
public class LSFChildStartupConfiguration extends ChildStartupConfiguration
{
  /**
   * 
   */
  private static final long serialVersionUID        = 1L;

  protected String          multicastAddress;

  protected int             discoveryMulticastPort;

  protected int             eventMulticastPort;

  /** Flag that the configuration contains LSF network properties */
  protected boolean         customNetworkProperties = false;

  /**
   * Creates a new instance of LSFChildStartupConfiguration.
   * 
   * @param parentHandler
   */
  public LSFChildStartupConfiguration(LSFStartupConfiguration parentHandler)
  {
    super(parentHandler);
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.startup.AbstractStartupConfiguration#setupVariables()
   */
  public void setupVariables()
  {
    multicastAddress = BinaryUPnPConstants.BinaryUPnPMulticastAddress;
    discoveryMulticastPort = BinaryUPnPConstants.DiscoveryMulticastPort;
    eventMulticastPort = BinaryUPnPConstants.EventMulticastPort;
  }

  /**
   * Retrieves the value of multicastAddress.
   * 
   * @return The value of multicastAddress
   */
  public String getMulticastAddress()
  {
    return multicastAddress;
  }

  /**
   * Sets the new value for multicastAddress.
   * 
   * @param multicastAddress
   *          The new value for multicastAddress
   */
  public void setMulticastAddress(String multicastAddress)
  {
    this.multicastAddress = multicastAddress;
  }

  /**
   * Retrieves the value of discoveryMulticastPort.
   * 
   * @return The value of discoveryMulticastPort
   */
  public int getDiscoveryMulticastPort()
  {
    return discoveryMulticastPort;
  }

  /**
   * Sets the new value for discoveryMulticastPort.
   * 
   * @param discoveryMulticastPort
   *          The new value for discoveryMulticastPort
   */
  public void setDiscoveryMulticastPort(int discoveryMulticastPort)
  {
    this.discoveryMulticastPort = discoveryMulticastPort;
  }

  /**
   * Retrieves the value of eventMulticastPort.
   * 
   * @return The value of eventMulticastPort
   */
  public int getEventMulticastPort()
  {
    return eventMulticastPort;
  }

  /**
   * Sets the new value for eventMulticastPort.
   * 
   * @param eventMulticastPort
   *          The new value for eventMulticastPort
   */
  public void setEventMulticastPort(int eventMulticastPort)
  {
    this.eventMulticastPort = eventMulticastPort;
  }

  /**
   * Retrieves the value of customNetworkProperties.
   * 
   * @return The value of customNetworkProperties
   */
  public boolean hasCustomNetworkProperties()
  {
    return customNetworkProperties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content) throws SAXException
  {
    super.processContentElement(content);

    content = content.trim();
    if (getTagCount() == 2)
    {
      if (getCurrentTag().equals(LSFStartupConfiguration.MULTICAST_ADDRESS))
      {
        multicastAddress = content;
        customNetworkProperties = true;
      }
      if (getCurrentTag().equals(LSFStartupConfiguration.DISCOVERY_MULTICAST_PORT))
      {
        discoveryMulticastPort = StringHelper.stringToIntegerDef(content, discoveryMulticastPort);
        customNetworkProperties = true;
      }
      if (getCurrentTag().equals(LSFStartupConfiguration.EVENT_MULTICAST_PORT))
      {
        eventMulticastPort = StringHelper.stringToIntegerDef(content, eventMulticastPort);
        customNetworkProperties = true;
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.startup.StartupConfiguration#processEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    super.processEndElement(uri, localName, qName);
    if (getTagCount() == 1)
    {
      // store the parsed description
      parentHandler.addGatewayStartupInfo(this);
      parentHandler.handleSAXEvents();
    }
  }

}

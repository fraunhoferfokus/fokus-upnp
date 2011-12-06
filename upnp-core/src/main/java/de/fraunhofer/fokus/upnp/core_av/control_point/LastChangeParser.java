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
package de.fraunhofer.fokus.upnp.core_av.control_point;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * This class can be used to parse the last change event variable.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class LastChangeParser extends SAXTemplateHandler
{
  /** The event listener for parsed changes */
  private ILastChangeEventListener eventListener;

  /** The associated service */
  private CPService                cpService;

  /** Last parsed instanceID */
  private long                     currentInstanceID;

  public LastChangeParser(CPService cpService, ILastChangeEventListener eventListener)
  {
    super();
    this.cpService = cpService;
    this.eventListener = eventListener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2 && getCurrentTag().equals(UPnPAVConstant.ARG_INSTANCE_ID))
    {
      // search instance ID value
      for (int i = 0; i < atts.getLength(); i++)
      {
        if (atts.getQName(i).equals(UPnPAVConstant.ATTR_VAL))
        {
          try
          {
            currentInstanceID = Long.parseLong(atts.getValue(i));
          } catch (Exception e)
          {
            currentInstanceID = -1;
          }
        }
      }
    }
    // handle state variable values
    if (getTagCount() == 3)
    {
      String channel = null;
      String value = null;
      for (int i = 0; i < atts.getLength(); i++)
      {
        if (atts.getQName(i).equals(UPnPAVConstant.ATTR_VAL))
        {
          value = atts.getValue(i);
        }
        if (atts.getQName(i).equals(UPnPAVConstant.ATTR_CHANNEL))
        {
          channel = atts.getValue(i);
        }
      }
      // forward change to listener
      if (channel != null && eventListener != null)
      {
        eventListener.channelStateVariableChanged(cpService, currentInstanceID, getCurrentTag(), channel, value);
      }
      if (channel == null && eventListener != null)
      {
        eventListener.stateVariableChanged(cpService, currentInstanceID, getCurrentTag(), value);
      }
    }
  }

}

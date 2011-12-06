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
package de.fraunhofer.fokus.upnp.core_security.helpers.parser;

import java.util.Vector;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * 
 * @author Alexander Koenig
 */
public class OwnersParser
{

  private final String OWNERS_TAG    = "Owners";

  private final String HASH_TAG      = "hash";

  private final String ALGORITHM_TAG = "algorithm";

  private final String VALUE_TAG     = "value";

  private Vector       ownerList     = new Vector();

  /** Creates a new instance of AlgorithmsAndProtocolsParser */
  public OwnersParser(String parseText)
  {
    try
    {
      DefaultHandlerClass parserClass = new DefaultHandlerClass();
      parserClass.parse(parseText);
    } catch (Exception ex)
    {
      System.err.println("Error:" + ex.getMessage());
    }
  }

  public Vector getOwners()
  {
    return ownerList;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {
    private String algorithm = "";

    private String keyHash   = "";

    /**
     * Template function for processing an end element
     */
    public void processEndElement(String uri, String localName, String qName) throws SAXException
    {
      // end of one permission
      if (getTagCount() == 2 && algorithm.length() != 0 && algorithm.equals(CommonConstants.SHA_1_UPNP) &&
        keyHash.length() != 0)
      {
        ownerList.add(new SecurityAwareObject(CommonConstants.SHA_1_UPNP, keyHash));
        algorithm = "";
        keyHash = "";
      }
    }

    /**
     * Template function for processing content
     */
    public void processContentElement(String content)
    {
      if (getTagCount() == 3 && getTag(0).equals(OWNERS_TAG) && getTag(1).equals(HASH_TAG))
      {
        if (getTag(2).equals(ALGORITHM_TAG))
        {
          algorithm = content;
        }
        if (getTag(2).equals(VALUE_TAG))
        {
          keyHash = content;
        }
      }
    }

  }
}

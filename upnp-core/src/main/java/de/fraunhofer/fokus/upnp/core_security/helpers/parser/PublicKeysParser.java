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

import java.math.BigInteger;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;

/**
 * This class is used to parse public key XML descriptions.
 * 
 * @author Alexander Koenig
 */
public class PublicKeysParser
{

  private final String           CONFIDENTIALITY_TAG = "Confidentiality";

  private final String           RSA_KEY_VALUE_TAG   = "RSAKeyValue";

  private final String           MODULUS_TAG         = "Modulus";

  private final String           EXPONENT_TAG        = "Exponent";

  private PersistentRSAPublicKey publicKey;

  /** Creates a new instance of PublicKeysParser */
  public PublicKeysParser(String parseText)
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

  /**
   * Retrieves the parsed key.
   * 
   * @return The parsed RSA key or null
   */
  public PersistentRSAPublicKey getPublicKey()
  {
    return publicKey;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {
    private BigInteger modulus;

    private BigInteger exponent;

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void processEndElement(String uri, String localName, String qName) throws SAXException
    {
      if (getTagCount() == 2 && getCurrentTag().equals(CONFIDENTIALITY_TAG) && modulus != null && exponent != null)
      {
        publicKey = new PersistentRSAPublicKey(exponent, modulus);
        modulus = null;
        exponent = null;
      }
      if (getTagCount() == 1 && getCurrentTag().equals(RSA_KEY_VALUE_TAG) && modulus != null && exponent != null)
      {
        publicKey = new PersistentRSAPublicKey(exponent, modulus);
        modulus = null;
        exponent = null;
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
     */
    public void processContentElement(String content)
    {
      if (getTagCount() == 4 && getTag(2).equals(RSA_KEY_VALUE_TAG) || getTagCount() == 2 &&
        getTag(0).equals(RSA_KEY_VALUE_TAG))
      {
        if (getCurrentTag().equals(MODULUS_TAG))
        {
          byte[] modulusBytes = Base64Helper.base64ToByteArray(content);
          modulus = new BigInteger(modulusBytes);
        }
        if (getCurrentTag().equals(EXPONENT_TAG))
        {
          byte[] exponentBytes = Base64Helper.base64ToByteArray(content);
          exponent = new BigInteger(exponentBytes);
        }
      }
    }

  }
}

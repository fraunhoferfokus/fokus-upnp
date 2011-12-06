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

import java.security.Key;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * 
 * @author Alexander Koenig
 */
public class SessionKeysParser
{

  // private final String ALGORITHM_TAG = "Algorithm";
  private final String CONFIDENTIALITY_TAG          = "Confidentiality";

  private final String KEY_FROM_DEVICE_TAG          = "KeyFromDevice";

  private final String KEY_TO_DEVICE_TAG            = "KeyToDevice";

  private final String SESSION_KEYS_TAG             = "SessionKeys";

  private final String SIGNING_TAG                  = "Signing";

  private SecretKey    confidentialityToDeviceKey   = null;

  private SecretKey    confidentialityFromDeviceKey = null;

  private Key          signingToDeviceKey           = null;

  private Key          signingFromDeviceKey         = null;

  /** Creates a new instance of AlgorithmsAndProtocolsParser */
  public SessionKeysParser(String parseText)
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

  public SecretKey getConfidentialityToDeviceKey()
  {
    return confidentialityToDeviceKey;
  }

  public SecretKey getConfidentialityFromDeviceKey()
  {
    return confidentialityFromDeviceKey;
  }

  public Key getSigningToDeviceKey()
  {
    return signingToDeviceKey;
  }

  public Key getSigningFromDeviceKey()
  {
    return signingFromDeviceKey;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {

    /**
     * Template function for processing content
     */
    public void processContentElement(String content)
    {
      if (getTagCount() == 3 && getTag(0).equals(SESSION_KEYS_TAG))
      {
        if (getTag(1).equals(CONFIDENTIALITY_TAG))
        {
          if (getCurrentTag().equals(KEY_TO_DEVICE_TAG))
          {
            confidentialityToDeviceKey =
              SymmetricCryptographyHelper.buildAESKey(Base64Helper.base64ToByteArray(content));
          }

          if (getCurrentTag().equals(KEY_FROM_DEVICE_TAG))
          {
            confidentialityFromDeviceKey =
              SymmetricCryptographyHelper.buildAESKey(Base64Helper.base64ToByteArray(content));
          }

          // if (getCurrentTag().equals(ALGORITHM_TAG))
          // confidentialityAlgorithm = content;
        }

        if (getTag(1).equals(SIGNING_TAG))
        {
          if (getCurrentTag().equals(KEY_TO_DEVICE_TAG))
          {
            signingToDeviceKey = DigestHelper.buildSHA1HMACKey(Base64Helper.base64ToByteArray(content));
          }

          if (getCurrentTag().equals(KEY_FROM_DEVICE_TAG))
          {
            signingFromDeviceKey = DigestHelper.buildSHA1HMACKey(Base64Helper.base64ToByteArray(content));
          }

          // if (getCurrentTag().equals(ALGORITHM_TAG))
          // signingAlgorithm = content;

        }
      }
    }

  }
}

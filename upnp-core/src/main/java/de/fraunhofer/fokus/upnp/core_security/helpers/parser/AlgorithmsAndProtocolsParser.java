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

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * 
 * @author Alexander Koenig
 */
public class AlgorithmsAndProtocolsParser
{

  private final String PROTOCOLS_TAG             = "Protocols";

  private final String HASH_ALGORITHMS_TAG       = "HashAlgorithms";

  private final String ENCRYPTION_ALGORITHMS_TAG = "EncryptionAlgorithms";

  private final String SIGNING_ALGORITHMS_TAG    = "SigningAlgorithms";

  private final String SUPPORTED_TAG             = "Supported";

  private Vector       protocols                 = new Vector();

  private Vector       hashAlgorithms            = new Vector();

  private Vector       encryptionAlgorithms      = new Vector();

  private Vector       signingAlgorithms         = new Vector();

  /** Creates a new instance of AlgorithmsAndProtocolsParser */
  public AlgorithmsAndProtocolsParser(String parseText)
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

  public Vector getProtocols()
  {
    return protocols;
  }

  public Vector getHashAlgorithms()
  {
    return hashAlgorithms;
  }

  public Vector getEncryptionAlgorithms()
  {
    return encryptionAlgorithms;
  }

  public Vector getSigningAlgorithms()
  {
    return signingAlgorithms;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {
    /**
     * Methods is called if content was found
     */
    public void processContentElement(String content)
    {
      if (getTagCount() == 3 && getTag(0).equals(SUPPORTED_TAG))
      {
        if (getTag(1).equals(PROTOCOLS_TAG) && getCurrentTag().equals("p"))
        {
          protocols.add(content);
        }

        if (getTag(1).equals(HASH_ALGORITHMS_TAG) && getCurrentTag().equals("p"))
        {
          hashAlgorithms.add(content);
        }

        if (getTag(1).equals(ENCRYPTION_ALGORITHMS_TAG) && getCurrentTag().equals("p"))
        {
          encryptionAlgorithms.add(content);
        }

        if (getTag(1).equals(SIGNING_ALGORITHMS_TAG) && getCurrentTag().equals("p"))
        {
          signingAlgorithms.add(content);
        }
      }
    }

  }
}

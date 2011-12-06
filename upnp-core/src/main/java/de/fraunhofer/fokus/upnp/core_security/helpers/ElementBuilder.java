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
package de.fraunhofer.fokus.upnp.core_security.helpers;

import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;

/**
 * 
 * @author Alexander Koenig
 */
public class ElementBuilder
{

  /** Creates a XML description of a RSA public key for DeviceSecurity.getPublicKeys() */
  public static String buildKeyArgArgument(RSAPublicKey key)
  {
    String result =
      "<Keys><Confidentiality>" + SecurityHelper.buildRSAPublicKeyXMLDescription(key) + "</Confidentiality></Keys>";
    return StringHelper.xmlToEscapedString(result);
  }

  /** Creates a XML description for DeviceSecurity.getAlgorithmsAndProtocols() */
  public static String buildSupportedArgument()
  {
    String result =
      "<Supported>" + "<Protocols>" + "<p>UPnP</p>" + "</Protocols>" + "<HashAlgorithms>" + "" + "<p>SHA1</p>"
        + "</HashAlgorithms>" + "<EncryptionAlgorithms>" + "<p>NULL</p>" + "<p>RSA</p>" + "<p>AES-128-CBC</p>"
        + "</EncryptionAlgorithms>" + "<SigningAlgorithms>" + "<p>NULL</p>" + "<p>RSA</p>" + "<p>SHA1-HMAC</p>"
        + "</SigningAlgorithms>" + "</Supported>";

    return StringHelper.xmlToEscapedString(result);
  }

  public static String buildFreshnessXMLDescription(String body)
  {
    String result = "";
    result += "<Freshness xmlns=\"" + SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE + "\" ";
    result += "xmlns:us=\"" + SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE + "\" ";
    result += "us:Id=\"Freshness\">" + body + "</Freshness>";

    return result;
  }

  /** Builds the freshness body for a RSA signed message */
  public static String buildFreshnessBody(String lifetimeSequenceBase, String controlURL)
  {
    String result =
      CommonSecurityConstant.LIFETIME_SEQUENCE_BASE_TAG + lifetimeSequenceBase +
        CommonSecurityConstant.LIFETIME_SEQUENCE_BASE_END_TAG + SecurityConstant.CONTROL_URL_TAG + controlURL +
        SecurityConstant.CONTROL_URL_END_TAG;

    return result;
  }

  /** Builds the freshness body for a SHA1-HMAC signed message */
  public static String buildFreshnessBody(String sequenceBase, String sequenceNumber, String controlURL)
  {
    String result =
      CommonSecurityConstant.SEQUENCE_BASE_TAG + sequenceBase + CommonSecurityConstant.SEQUENCE_BASE_END_TAG +
        CommonSecurityConstant.SEQUENCE_NUMBER_TAG + sequenceNumber + CommonSecurityConstant.SEQUENCE_NUMBER_END_TAG +
        SecurityConstant.CONTROL_URL_TAG + controlURL + SecurityConstant.CONTROL_URL_END_TAG;

    return result;
  }

  /** Retrieves a string with all owners */
  public static String buildOwnerXMLDescription(Vector owners)
  {
    String ownerXMLDescription = "<Owners>";
    for (int i = 0; i < owners.size(); i++)
    {
      ownerXMLDescription += ((SecurityAwareObject)owners.elementAt(i)).toXMLDescription();
    }
    ownerXMLDescription += "</Owners>";
    return ownerXMLDescription;
  }

  /** Builds the acl entry list */
  public static String buildACLXMLDescription(Vector aclEntries)
  {
    String result = "<acl>";
    for (int i = 0; i < aclEntries.size(); i++)
    {
      ACLEntry entry = (ACLEntry)aclEntries.elementAt(i);
      result += entry.toXMLDescription();
    }
    result += "</acl>";
    return result;
  }

}

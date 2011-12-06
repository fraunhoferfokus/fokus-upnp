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
package de.fraunhofer.fokus.upnp.util.security;

/**
 * 
 * @author Alexander Koenig
 */
public class CommonSecurityConstant
{

  /** SHA1-HMAC */
  public static final String HMAC_SHA_1_UPNP                = "SHA1-HMAC";

  /** AES-128-CBC */
  public static final String AES_128_CBC_UPNP               = "AES-128-CBC";

  public static final String CANONICALIZATION_METHOD_TAG    =
                                                              "<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>";

  public static final String DIGEST_METHOD_TAG              =
                                                              "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>";

  public static final String DIGEST_VALUE_TAG               = "<DigestValue>";

  public static final String DIGEST_VALUE_END_TAG           = "</DigestValue>";

  public static final String DS_KEY_INFO_TAG                = "<ds:KeyInfo>";

  public static final String DS_KEY_INFO_END_TAG            = "</ds:KeyInfo>";

  public static final String KEY_INFO_TAG                   = "<KeyInfo>";

  public static final String KEY_INFO_END_TAG               = "</KeyInfo>";

  public static final String KEY_NAME_TAG                   = "<KeyName>";

  public static final String KEY_NAME_END_TAG               = "</KeyName>";

  public static final String KEY_VALUE_TAG                  = "<KeyValue>";

  public static final String KEY_VALUE_END_TAG              = "</KeyValue>";

  public static final String LIFETIME_SEQUENCE_BASE_TAG     = "<LifetimeSequenceBase>";

  public static final String LIFETIME_SEQUENCE_BASE_END_TAG = "</LifetimeSequenceBase>";

  public static final String NAMES_TAG                      = "<Names us:Id=\"NameList\">";

  public static final String NAMES_END_TAG                  = "</Names>";

  public static final String REFERENCE_NAMELIST_TAG         = "<Reference URI=\"#NameList\">";

  public static final String REFERENCE_END_TAG              = "</Reference>";

  public static final String RSA_KEY_VALUE_TAG              = "<RSAKeyValue>";

  public static final String RSA_KEY_VALUE_END_TAG          = "</RSAKeyValue>";

  public static final String SECURITY_INFO_TAG              = "<SecurityInfo>";

  public static final String SECURITY_INFO_END_TAG          = "</SecurityInfo>";

  public static final String SEQUENCE_BASE_TAG              = "<SequenceBase>";

  public static final String SEQUENCE_BASE_END_TAG          = "</SequenceBase>";

  public static final String SEQUENCE_NUMBER_TAG            = "<SequenceNumber>";

  public static final String SEQUENCE_NUMBER_END_TAG        = "</SequenceNumber>";

  public static final String SIGNATURE_TAG                  =
                                                              "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">";

  public static final String SIGNATURE_END_TAG              = "</Signature>";

  public static final String SIGNATURE_METHOD_TAG           =
                                                              "<SignatureMethods Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>";

  public static final String SIGNATURE_VALUE_TAG            = "<SignatureValue>";

  public static final String SIGNATURE_VALUE_END_TAG        = "</SignatureValue>";

  public static final String SIGNED_INFO_TAG                = "<SignedInfo>";

  public static final String SIGNED_INFO_END_TAG            = "</SignedInfo>";

  public static final String SIGNED_NAME_LIST_TAG           = "<SignedNameList>";

  public static final String SIGNED_NAME_LIST_END_TAG       = "</SignedNameList>";

}

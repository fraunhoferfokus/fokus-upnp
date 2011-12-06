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

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SignatureHelper;

/**
 * This class holds a certificate that can be used to grant a control point or a named group access
 * rights to certain actions of a service
 * 
 * @author Alexander Koenig
 */
public class AuthorizationCertificate
{

  private SecurityAwareObject issuer;

  private SecurityAwareObject subject;

  private SecurityAwareObject device;

  private Vector              accessPermissions;

  private String              notValidBefore;

  private String              notValidAfter;

  /** Creates a new instance of Certificate */
  public AuthorizationCertificate(SecurityAwareObject issuer,
    SecurityAwareObject subject,
    SecurityAwareObject device,
    Vector accessPermissions,
    String notValidBefore,
    String notValidAfter)
  {
    this.issuer = issuer;
    this.subject = subject;
    this.device = device;
    this.accessPermissions = accessPermissions;
    this.notValidBefore = notValidBefore;
    this.notValidAfter = notValidAfter;
  }

  /** Retrieves the issuer of the certificate */
  public SecurityAwareObject getIssuer()
  {
    return issuer;
  }

  /** Retrieves the subject associated with this certificate */
  public SecurityAwareObject getSubject()
  {
    return subject;
  }

  /**
   * Retrieves the device associated with this certificate (that is, the device which can be used by
   * the subject
   */
  public SecurityAwareObject getDevice()
  {
    return device;
  }

  /**
   * Getter for property accessPermissions.
   * 
   * @return Value of property accessPermissions.
   */
  public Vector getAccessPermissions()
  {
    return this.accessPermissions;
  }

  /**
   * Getter for property accessPermissions.
   * 
   * @return Value of property accessPermissions.
   */
  public String toAccessPermissionsString()
  {
    String result = "";
    for (int i = 0; i < accessPermissions.size(); i++)
    {
      result += (String)accessPermissions.elementAt(i);
    }

    return result;
  }

  /** Returns an XML description of this certificate */
  public String toXMLDescription()
  {
    String result = "";
    result += "<issuer>" + issuer.toXMLDescription() + "</issuer>";
    result += "<subject>" + subject.toXMLDescription() + "</subject>";
    result += "<tag>";
    result += "<device>" + device.toXMLDescription() + "</device>";
    result += "<access>" + toAccessPermissionsString() + "</access>";
    result += "</tag>";
    result += "<valid>";
    result += "<not-before>" + notValidBefore + "</not-before>";
    result += "<not-after>" + notValidAfter + "</not-after>";
    result += "</valid>";

    return result;
  }

  /** Returns an XML description of this certificate */
  public String toSignedString(String id, RSAPrivateKey privateKey, RSAPublicKey publicKey)
  {
    String result = "";
    result += "<cert us:Id=\"" + id + "\" xmlns=\"" + SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE + "\">";

    String hashSource = toXMLDescription();
    // calculate hash
    byte[] hash = DigestHelper.calculateSHAHashForString(hashSource);
    String hashBase64 = Base64Helper.byteArrayToBase64(hash);

    result += hashSource;
    result += "</cert>";

    // calculate signature
    result += SignatureHelper.createRSASignature(id, hashBase64, privateKey, publicKey);

    return result;
  }

}

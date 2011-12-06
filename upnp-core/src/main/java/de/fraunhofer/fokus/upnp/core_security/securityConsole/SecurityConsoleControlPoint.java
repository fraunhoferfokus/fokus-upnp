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
package de.fraunhofer.fokus.upnp.core_security.securityConsole;

import java.security.Key;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPService;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.OwnersParser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;

/**
 * This class encapsulates a security console.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityConsoleControlPoint extends SecurityAwareTemplateControlPoint
{

  /**
   * Creates a new instance of SecurityConsoleControlPoint. The basic control point is not started.
   * 
   * @param anEntity
   *          Associated security console entity
   * @param startupConfiguration
   * 
   */
  public SecurityConsoleControlPoint(SecurityConsoleEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);
  }

  /** Retrieves the outer security console entity. */
  public SecurityConsoleEntity getSecurityConsoleEntity()
  {
    return (SecurityConsoleEntity)getTemplateEntity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.security.templates.SecurityAwareTemplateControlPoint#initialActionRequests(de.fhg.fokus.magic.upnp.security.helpers.SecurityAwareCPDevice)
   */
  public void initialActionRequests(SecurityAwareCPDeviceObject securityDevice)
  {
    super.initialActionRequests(securityDevice);
    CPService deviceSecurityService =
      securityDevice.getCPDevice().getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);

    if (deviceSecurityService != null)
    {
      // retrieve owner list
      CPAction action = deviceSecurityService.getCPAction("ListOwners");
      if (action != null)
      {
        try
        {
          invokeAction(action);

          String ownersArgString = action.getOutArgument("Owners").getStringValue();
          OwnersParser parser = new OwnersParser(ownersArgString);
          securityDevice.setOwners(parser);
        } catch (Exception ex)
        {
          logger.warn(ex.getMessage());
        }
      }
    }
  }

  /** Tries to take ownership of an device */
  public void invokeTakeOwnership(SecurityAwareCPDeviceObject securityDevice, String secretBase32)
  {
    CPService service = securityDevice.getCPDevice().getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
    if (service != null)
    {
      if (!(service instanceof SecurityAwareCPService))
      {
        System.out.println("Found security unaware service in security aware device");
        return;
      }
      SecurityAwareCPService deviceSecurityService = (SecurityAwareCPService)service;

      // retrieve sequence base
      String sequenceBaseString = null;
      CPAction action = deviceSecurityService.getCPAction("GetLifetimeSequenceBase");
      if (action != null)
      {
        try
        {
          invokeUnsignedAction(action);

          Argument sequenceBase = action.getOutArgument("ArgLifetimeSequenceBase");
          sequenceBaseString = sequenceBase.getStringValue();
        } catch (Exception ex)
        {
          logger.warn(ex.getMessage());
        }
      }
      if (sequenceBaseString != null)
      {
        Key secretKey = DigestHelper.buildSHA1HMACKey(StringHelper.base32ToByteArray(secretBase32));
        /*
         * System.out.println("SecurityConsole PublicKeyHash is "+
         * DigestHelper.calculateBase64SHAHashForRSAPublicKey(getPublicKey()));
         * 
         * System.out.println("SecuredDevice PublicKeyHash is "+
         * DigestHelper.calculateBase64SHAHashForRSAPublicKey(securityDevice.getConfidentialityKey()));
         * 
         * System.out.println("SequenceBase is "+sequenceBaseString);
         * 
         * System.out.println("Secret is
         * "+SecurityHelper.byteArrayToBase32(secretKey.getEncoded()));
         */
        // build HMAC base
        String hmacBase =
          SecurityHelper.buildRSAPublicKeyXMLDescription(getPublicKey()) +
            SecurityHelper.buildRSAPublicKeyXMLDescription(securityDevice.getConfidentialityKey()) + sequenceBaseString;

        // calculate HMAC
        byte[] hmacValue = DigestHelper.calculateSHA1HMACForString(secretKey, hmacBase);

        // System.out.println("Take ownership: HMAC value is
        // "+SecurityHelper.byteArrayToBase64(hmacValue));

        try
        {
          // encrypt with device public key
          byte[] encrypted =
            PublicKeyCryptographyHelper.encryptWithRSA(securityDevice.getConfidentialityKey(), hmacValue);

          // call takeOwnership
          action = deviceSecurityService.getCPAction("TakeOwnership");
          if (action != null)
          {
            Argument hmacAlgorithm = action.getInArgument("HMACAlgorithm");
            Argument encryptedHmacValue = action.getInArgument("EncryptedHMACValue");
            if (hmacAlgorithm != null && encryptedHmacValue != null)
            {
              hmacAlgorithm.setValue(CommonSecurityConstant.HMAC_SHA_1_UPNP);
              encryptedHmacValue.setValue(Base64Helper.byteArrayToBase64(encrypted));

              deviceSecurityService.invokeRSASignedAction(action, sequenceBaseString, getPrivateKey(), getPublicKey());

              // take ownership was successful, inform listeners
              getSecurityConsoleEntity().securityAwareCPDeviceStatusChange(securityDevice);
            }
          }
        } catch (Exception ex)
        {
          System.out.println("Error:" + ex.getMessage());
          logger.warn(ex.getMessage());
        }
      }
    }
  }

}

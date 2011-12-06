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

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.SignatureHelper;

/**
 * 
 * @author Alexander Koenig
 */
public class SecurityConsoleService extends TemplateService
{

  private StateVariable pendingCPList;

  private StateVariable nameListVersion;

  private StateVariable A_ARG_TYPE_string;

  private StateVariable A_ARG_TYPE_base64;

  private Action        presentKey;

  private Action        getNameList;

  private Action        getMyCertificates;

  private Action        renewCertificate;

  private String        nameList = "";

  /** Creates a new instance of SecurityConsoleService */
  public SecurityConsoleService(SecurityConsoleDevice device)
  {
    super(device, SecurityConstant.SECURITY_CONSOLE_SERVICE_TYPE, SecurityConstant.SECURITY_CONSOLE_SERVICE_ID);
  }

  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    pendingCPList = new StateVariable("PendingCPList", "<CPList></CPList>", true);
    nameListVersion = new StateVariable("NameListVersion", "", true);
    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_base64 = new StateVariable("A_ARG_TYPE_base64", "bin.base64", "", false);

    StateVariable[] stateVariableList = {
        pendingCPList, nameListVersion, A_ARG_TYPE_string, A_ARG_TYPE_base64
    };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    presentKey = new Action("PresentKey");
    presentKey.setArgumentTable(new Argument[] {
        new Argument("HashAlgorithm", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Key", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("PreferredName", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("IconDesc", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string)
    });

    getNameList = new Action("GetNameList");
    getNameList.setArgumentTable(new Argument[] {
      new Argument("Names", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    getMyCertificates = new Action("GetMyCertificates");
    getMyCertificates.setArgumentTable(new Argument[] {
        new Argument("HashAlgorithm", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("Hash", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64),
        new Argument("Certificates", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    renewCertificate = new Action("RenewCertificate");
    renewCertificate.setArgumentTable(new Argument[] {
        new Argument("OldCertificate", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string),
        new Argument("NewCertificate", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    Action[] actionList = {
        presentKey, getNameList, getMyCertificates, renewCertificate
    };
    setActionTable(actionList);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void presentKey(Argument[] args) throws ActionFailedException
  {
    System.out.println("PresentKey action invoked");
    if (args.length != 4)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // process received key
    try
    {
      String hashAlgorithm = args[0].getStringValue();
      String keyString = args[1].getStringValue();
      // System.out.println("Received key string:"+keyString);

      String preferredName = args[2].getStringValue();
      // String icon = (String)args[3].getValue();
      // hash received key
      if (hashAlgorithm.equals(CommonConstants.SHA_1_UPNP))
      {
        byte[] keyHash = DigestHelper.calculateSHAHashForString(keyString);
        getSecurityConsoleEntity().tryAddSecurityAwareControlPoint(preferredName, hashAlgorithm, keyHash);
        return;
      }
      throw new ActionFailedException(801, "Invalid hash algorithm");
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getNameList(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // return current name list
    try
    {
      args[0].setValue(nameList);
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getMyCertificates(Argument[] args) throws ActionFailedException
  {
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(801, "Not yet implemented");
  }

  public void renewCertificate(Argument[] args) throws ActionFailedException
  {
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(801, "Not yet implemented");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  protected void buildNameList()
  {
    String result = "";
    result += CommonSecurityConstant.SIGNED_NAME_LIST_TAG;
    result += CommonSecurityConstant.NAMES_TAG;

    // build the string that will be hashed
    String hashSource = "";
    // add all devices to hashSource
    for (int i = 0; i < getSecurityConsoleEntity().getNamedDeviceCount(); i++)
    {
      LocalDictionaryObject device = getSecurityConsoleEntity().getNamedDevice(i);
      hashSource += SecurityConstant.DEVICE_TAG;
      hashSource += "<name>" + device.getUserDefinedName() + "</name>";
      hashSource += "<hash>";
      hashSource += "<algorithm>" + device.getSecurityAwareObject().getHashAlgorithm() + "</algorithm";
      hashSource += "<value>" + device.getSecurityAwareObject().getPublicKeyHashBase64() + "</value>";
      hashSource += "</hash>";
      hashSource += SecurityConstant.DEVICE_END_TAG;
    }
    // add all control points to hashSource
    for (int i = 0; i < getSecurityConsoleEntity().getNamedControlPointCount(); i++)
    {
      LocalDictionaryObject controlPoint = getSecurityConsoleEntity().getNamedControlPoint(i);
      hashSource += SecurityConstant.CONTROL_POINT_TAG;
      hashSource += "<name>" + controlPoint.getUserDefinedName() + "</name>";
      hashSource += "<hash>";
      hashSource += "<algorithm>" + controlPoint.getSecurityAwareObject().getHashAlgorithm() + "</algorithm";
      hashSource += "<value>" + controlPoint.getSecurityAwareObject().getPublicKeyHashBase64() + "</value>";
      hashSource += "</hash>";
      hashSource += SecurityConstant.CONTROL_POINT_END_TAG;
    }
    // calculate hash
    byte[] hash = DigestHelper.calculateSHAHashForString(hashSource);
    String hashBase64 = Base64Helper.byteArrayToBase64(hash);
    // System.out.println("Calculated hash: "+hashBase64);

    result += hashSource;
    result += CommonSecurityConstant.NAMES_END_TAG;

    // calculate signature
    result +=
      SignatureHelper.createRSASignature("NameList",
        hashBase64,
        getSecurityConsoleControlPoint().getPrivateKey(),
        getSecurityConsoleControlPoint().getPublicKey());

    result += CommonSecurityConstant.SIGNED_NAME_LIST_END_TAG;

    if (!nameList.equals(result))
    {
      nameList = result;
      try
      {
        // use hash as name version
        nameListVersion.setValue(hashBase64);
      } catch (Exception ex)
      {
        logger.warn(ex.getMessage());
        System.out.println("ERROR setting name list version: " + ex.getMessage());
      }
    }
    // System.out.println("NameList is:");
    // System.out.println(nameList);
  }

  private SecurityConsoleEntity getSecurityConsoleEntity()
  {
    return ((SecurityConsoleDevice)getTemplateDevice()).getSecurityConsoleEntity();
  }

  private SecurityConsoleControlPoint getSecurityConsoleControlPoint()
  {
    return getSecurityConsoleEntity().getSecurityConsoleControlPoint();
  }

}

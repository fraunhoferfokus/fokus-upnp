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
package de.fraunhofer.fokus.upnp.core_security.deviceSecurity;

import java.net.InetSocketAddress;

import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.core.AllowedValueRange;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.device.SecuredDeviceHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.ElementBuilder;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.ACLParser;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateDevice;
import de.fraunhofer.fokus.upnp.core_security.templates.SecuredTemplateService;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageHelper;
import de.fraunhofer.fokus.upnp.util.network.HTTPMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class implements the DeviceSecurityService which is necessary for all devices that want to
 * use UPnP security.
 * 
 * @author Alexander Koenig
 */
public class DeviceSecurityService extends SecuredTemplateService
{

  public static final int           ACL_SIZE        = 50;

  public static final int           OWNER_LIST_SIZE = 5;

  public static final int           CERT_CACHE_SIZE = 100;

  StateVariable                     numberOfOwners;

  StateVariable                     lifetimeSequenceBase;

  StateVariable                     timeHint;

  StateVariable                     totalACLSize;

  StateVariable                     freeACLSize;

  StateVariable                     totalOwnerListSize;

  StateVariable                     freeOwnerListSize;

  StateVariable                     totalCertCacheSize;

  StateVariable                     freeCertCacheSize;

  StateVariable                     A_ARG_TYPE_string;

  StateVariable                     A_ARG_TYPE_base64;

  StateVariable                     A_ARG_TYPE_int;

  StateVariable                     A_ARG_TYPE_boolean;

  /* Normal actions */
  Action                            getPublicKeys;

  Argument                          outKeyArg;

  Action                            getAlgorithmsAndProtocols;

  Argument                          outSupported;

  Action                            getACLSizes;

  Argument                          outArgTotalACLSize;

  Argument                          outArgFreeACLSize;

  Argument                          outArgTotalOwnerSize;

  Argument                          outArgFreeOwnerSize;

  Argument                          outArgTotalCertCacheSize;

  Argument                          outArgFreeCertCacheSize;

  Action                            cacheCertificate;

  Argument                          inCertificates;

  Action                            setTimeHint;

  Argument                          inArgTimeHint;

  Action                            getLifetimeSequenceBase;

  Argument                          outArgLifetimeSequenceBase;

  Action                            setSessionKeys;

  Argument                          inEncipheredBulkKey;

  Argument                          inBulkAlgorithm;

  Argument                          inCiphertext;

  Argument                          inCPKeyID;

  Argument                          outDeviceKeyID;

  Argument                          outSequenceBase;

  Action                            expireSessionKeys;

  Argument                          inDeviceKeyID;

  Action                            decryptAndExecute;

  Argument                          inRequest;

  Argument                          inInIV;

  Argument                          outReply;

  Argument                          outOutIV;

  /* SecurityConsole actions */
  Action                            takeOwnership;

  Argument                          inHMACAlgorithm;

  Argument                          inEncryptedHMACValue;

  Action                            getDefinedPermissions;

  Argument                          outPermissions;

  Action                            getDefinedProfiles;

  Argument                          outProfiles;

  Action                            readACL;

  Argument                          outVersion;

  Argument                          outACL;

  Action                            writeACL;

  Argument                          inVersion;

  Argument                          inACL;

  Action                            addACLEntry;

  Argument                          inEntry;

  Action                            deleteACLEntry;

  Argument                          inTargetACLVersion;

  Argument                          inIndex;

  Argument                          outNewACLVersion;

  Action                            replaceACLEntry;

  Action                            factorySecurityReset;

  Action                            grantOwnership;

  Argument                          inHashAlgorithm;

  Argument                          inKeyHash;

  Action                            revokeOwnership;

  Action                            listOwners;

  Argument                          outArgNumberOfOwners;

  Argument                          outOwners;

  /** Message parser used for decryptAndExecute */
  SecuredDeviceHTTPMessageProcessor messageManager;

  /** Creates a new instance of TemplateService */
  public DeviceSecurityService(SecuredTemplateDevice device, SecuredDeviceHTTPMessageProcessor messageManager)
  {
    super(device, SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE, SecurityConstant.DEVICE_SECURITY_SERVICE_ID);
    this.messageManager = messageManager;
  }

  /** Initializes service specific properties */
  public void initServiceContent()
  {
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    numberOfOwners = new StateVariable("NumberOfOwners", getSecuredDevice().getOwnerCount(), true);
    AllowedValueRange positiveZeroRange = new AllowedValueRange(0, Integer.MAX_VALUE);
    numberOfOwners.setAllowedValueRange(positiveZeroRange);

    lifetimeSequenceBase = new StateVariable("LifetimeSequenceBase", SecurityHelper.createSequenceBaseString(), true);

    timeHint = new StateVariable("TimeHint", "", false);

    totalACLSize = new StateVariable("TotalACLSize", ACL_SIZE, false);
    totalACLSize.setAllowedValueRange(positiveZeroRange);

    freeACLSize = new StateVariable("FreeACLSize", ACL_SIZE - getSecuredDevice().getACLCount(), true);
    freeACLSize.setAllowedValueRange(positiveZeroRange);

    totalOwnerListSize = new StateVariable("TotalOwnerListSize", OWNER_LIST_SIZE, false);
    AllowedValueRange positiveRange = new AllowedValueRange(1, Integer.MAX_VALUE);
    totalOwnerListSize.setAllowedValueRange(positiveRange);

    freeOwnerListSize =
      new StateVariable("FreeOwnerListSize", OWNER_LIST_SIZE - getSecuredDevice().getOwnerCount(), true);

    freeOwnerListSize.setAllowedValueRange(positiveZeroRange);

    totalCertCacheSize = new StateVariable("TotalCertCacheSize", CERT_CACHE_SIZE, false);
    totalCertCacheSize.setAllowedValueRange(positiveZeroRange);

    freeCertCacheSize = new StateVariable("FreeCertCacheSize", CERT_CACHE_SIZE, true);
    freeCertCacheSize.setAllowedValueRange(positiveZeroRange);

    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_base64 = new StateVariable("A_ARG_TYPE_base64", "bin.base64", "", false);
    A_ARG_TYPE_int = new StateVariable("A_ARG_TYPE_int", 0, false);
    A_ARG_TYPE_boolean = new StateVariable("A_ARG_TYPE_boolean", false, false);

    StateVariable[] stateVariableList =
      {
          numberOfOwners, lifetimeSequenceBase, timeHint, totalACLSize, freeACLSize, totalOwnerListSize,
          freeOwnerListSize, totalCertCacheSize, freeCertCacheSize, A_ARG_TYPE_string, A_ARG_TYPE_base64,
          A_ARG_TYPE_int, A_ARG_TYPE_boolean
      };
    setStateVariableTable(stateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    getPublicKeys = new Action("GetPublicKeys");
    outKeyArg = new Argument("KeyArg", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] getPublicKeysArguments = {
      outKeyArg
    };
    getPublicKeys.setArgumentTable(getPublicKeysArguments);

    getAlgorithmsAndProtocols = new Action("GetAlgorithmsAndProtocols");
    outSupported = new Argument("Supported", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] getAlgorithmsAndProtocolsArguments = {
      outSupported
    };
    getAlgorithmsAndProtocols.setArgumentTable(getAlgorithmsAndProtocolsArguments);

    getACLSizes = new Action("GetACLSizes");
    outArgTotalACLSize = new Argument("ArgTotalACLSize", UPnPConstant.DIRECTION_OUT, totalACLSize);
    outArgFreeACLSize = new Argument("ArgFreeACLSize", UPnPConstant.DIRECTION_OUT, freeACLSize);
    outArgTotalOwnerSize = new Argument("ArgTotalOwnerListSize", UPnPConstant.DIRECTION_OUT, totalOwnerListSize);
    outArgFreeOwnerSize = new Argument("ArgFreeOwnerSize", UPnPConstant.DIRECTION_OUT, freeOwnerListSize);
    outArgTotalCertCacheSize = new Argument("ArgTotalCertCacheSize", UPnPConstant.DIRECTION_OUT, totalCertCacheSize);
    outArgFreeCertCacheSize = new Argument("ArgFreeCertCacheSize", UPnPConstant.DIRECTION_OUT, freeCertCacheSize);
    Argument[] getACLSizesArguments =
      {
          outArgTotalACLSize, outArgFreeACLSize, outArgTotalOwnerSize, outArgFreeOwnerSize, outArgTotalCertCacheSize,
          outArgFreeCertCacheSize
      };
    getACLSizes.setArgumentTable(getACLSizesArguments);

    cacheCertificate = new Action("CacheCertificate");
    inCertificates = new Argument("Certificates", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    Argument[] cacheCertificateArguments = {
      inCertificates
    };
    cacheCertificate.setArgumentTable(cacheCertificateArguments);

    setTimeHint = new Action("SetTimeHint");
    inArgTimeHint = new Argument("ArgTimeHint", UPnPConstant.DIRECTION_IN, timeHint);
    Argument[] setTimeHintArguments = {
      inArgTimeHint
    };
    setTimeHint.setArgumentTable(setTimeHintArguments);

    getLifetimeSequenceBase = new Action("GetLifetimeSequenceBase");
    outArgLifetimeSequenceBase =
      new Argument("ArgLifetimeSequenceBase", UPnPConstant.DIRECTION_OUT, lifetimeSequenceBase);
    Argument[] getLifetimeSequenceBaseArguments = {
      outArgLifetimeSequenceBase
    };
    getLifetimeSequenceBase.setArgumentTable(getLifetimeSequenceBaseArguments);

    setSessionKeys = new Action("SetSessionKeys");
    inEncipheredBulkKey = new Argument("EncipheredBulkKey", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    inBulkAlgorithm = new Argument("BulkAlgorithm", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inCiphertext = new Argument("Ciphertext", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    inCPKeyID = new Argument("CPKeyID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    outDeviceKeyID = new Argument("DeviceKeyID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_int);
    outSequenceBase = new Argument("SequenceBase", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] setSessionKeysArguments = {
        inEncipheredBulkKey, inBulkAlgorithm, inCiphertext, inCPKeyID, outDeviceKeyID, outSequenceBase
    };
    setSessionKeys.setArgumentTable(setSessionKeysArguments);

    expireSessionKeys = new Action("ExpireSessionKeys");
    inDeviceKeyID = new Argument("DeviceKeyID", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    Argument[] expireSessionKeysArguments = {
      inDeviceKeyID
    };
    expireSessionKeys.setArgumentTable(expireSessionKeysArguments);

    decryptAndExecute = new Action("DecryptAndExecute");
    inRequest = new Argument("Request", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    inInIV = new Argument("InIV", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    outReply = new Argument("Reply", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_base64);
    outOutIV = new Argument("OutIV", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_base64);
    Argument[] decryptAndExecuteArguments = {
        inDeviceKeyID, inRequest, inInIV, outReply, outOutIV
    };
    decryptAndExecute.setArgumentTable(decryptAndExecuteArguments);

    takeOwnership = new Action("TakeOwnership");
    inHMACAlgorithm = new Argument("HMACAlgorithm", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inEncryptedHMACValue = new Argument("EncryptedHMACValue", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    Argument[] takeOwnershipArguments = {
        inHMACAlgorithm, inEncryptedHMACValue
    };
    takeOwnership.setArgumentTable(takeOwnershipArguments);

    getDefinedPermissions = new Action("GetDefinedPermissions");
    outPermissions = new Argument("Permissions", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] getDefinedPermissionsArguments = {
      outPermissions
    };
    getDefinedPermissions.setArgumentTable(getDefinedPermissionsArguments);

    getDefinedProfiles = new Action("GetDefinedProfiles");
    outProfiles = new Argument("Profiles", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] getDefinedProfilesArguments = {
      outProfiles
    };
    getDefinedProfiles.setArgumentTable(getDefinedProfilesArguments);

    readACL = new Action("ReadACL");
    outVersion = new Argument("Version", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    outACL = new Argument("ACL", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] readACLArguments = {
        outVersion, outACL
    };
    readACL.setArgumentTable(readACLArguments);

    writeACL = new Action("WriteACL");
    inVersion = new Argument("Version", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inACL = new Argument("ACL", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    Argument[] writeACLArguments = {
        inVersion, inACL
    };
    writeACL.setArgumentTable(writeACLArguments);

    addACLEntry = new Action("AddACLEntry");
    inEntry = new Argument("Entry", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    Argument[] addACLEntryArguments = {
      inEntry
    };
    addACLEntry.setArgumentTable(addACLEntryArguments);

    deleteACLEntry = new Action("DeleteACLEntry");
    inTargetACLVersion = new Argument("TargetACLVersion", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inIndex = new Argument("Index", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_int);
    outNewACLVersion = new Argument("NewACLVersion", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] deleteACLEntryArguments = {
        inTargetACLVersion, inIndex, outNewACLVersion
    };
    deleteACLEntry.setArgumentTable(deleteACLEntryArguments);

    replaceACLEntry = new Action("ReplaceACLEntry");
    Argument[] replaceACLEntryArguments = {
        inTargetACLVersion, inIndex, inEntry, outNewACLVersion
    };
    replaceACLEntry.setArgumentTable(replaceACLEntryArguments);

    factorySecurityReset = new Action("FactorySecurityReset");

    grantOwnership = new Action("GrantOwnership");
    inHashAlgorithm = new Argument("HashAlgorithm", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string);
    inKeyHash = new Argument("KeyHash", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_base64);
    Argument[] grantOwnershipArguments = {
        inHashAlgorithm, inKeyHash
    };
    grantOwnership.setArgumentTable(grantOwnershipArguments);

    revokeOwnership = new Action("RevokeOwnership");
    Argument[] revokeOwnershipArguments = {
        inHashAlgorithm, inKeyHash
    };
    revokeOwnership.setArgumentTable(revokeOwnershipArguments);

    listOwners = new Action("ListOwners");
    outArgNumberOfOwners = new Argument("ArgNumberOfOwners", UPnPConstant.DIRECTION_OUT, numberOfOwners);
    outOwners = new Argument("Owners", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string);
    Argument[] listOwnersArguments = {
        outArgNumberOfOwners, outOwners
    };
    listOwners.setArgumentTable(listOwnersArguments);

    Action[] actionList =
      {
          getPublicKeys, getAlgorithmsAndProtocols, getACLSizes, cacheCertificate, setTimeHint,
          getLifetimeSequenceBase, setSessionKeys, expireSessionKeys, decryptAndExecute, takeOwnership,
          getDefinedPermissions, getDefinedProfiles, readACL, writeACL, addACLEntry, deleteACLEntry, replaceACLEntry,
          factorySecurityReset, grantOwnership, revokeOwnership, listOwners
      };
    setActionTable(actionList);

    // Actions that must not be signed
    Action[] unsignedActionList = {
        getPublicKeys, getAlgorithmsAndProtocols, decryptAndExecute, getLifetimeSequenceBase, listOwners
    };
    setUnsignedActionTable(unsignedActionList);

    // Actions permitted to all users
    Action[] allUserActions =
      {
          getPublicKeys, getAlgorithmsAndProtocols, getLifetimeSequenceBase, setSessionKeys, expireSessionKeys,
          decryptAndExecute, cacheCertificate, takeOwnership, listOwners
      };
    setPermittedActionTable(allUserActions);
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Permissions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // trusted control points
    Permission trustedUserPermission =
      new Permission("DeviceSecurity.Trusted",
        "<mfgr:trusted/>",
        "",
        "Allows reading of the ACL, permissions and profiles");
    Action[] trustedUserActions = {
        getACLSizes, readACL, getDefinedPermissions, getDefinedProfiles
    };
    addPermissionEntry(trustedUserPermission, trustedUserActions);

    // editors
    Permission aclEditorPermission =
      new Permission("DeviceSecurity.AclEditor", "<mfgr:aclEditor/>", "", "Allows editing of the ACL");
    Action[] aclEditorActions =
      {
          getACLSizes, readACL, getDefinedPermissions, getDefinedProfiles, writeACL, addACLEntry, deleteACLEntry,
          replaceACLEntry
      };
    addPermissionEntry(aclEditorPermission, aclEditorActions);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void getPublicKeys(Argument[] args) throws ActionFailedException
  {
    // System.out.println("getPublicKeys invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(ElementBuilder.buildKeyArgArgument(getSecuredDevice().getPublicKey()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getAlgorithmsAndProtocols(Argument[] args) throws ActionFailedException
  {
    // System.out.println("getAlgorithmsAndProtocols invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(ElementBuilder.buildSupportedArgument());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getACLSizes(Argument[] args) throws ActionFailedException
  {
    System.out.println("getACLSizes invoked");
    if (args.length != 6)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(totalACLSize.getValue());
      args[1].setValue(freeACLSize.getValue());
      args[2].setValue(totalOwnerListSize.getValue());
      args[3].setValue(freeOwnerListSize.getValue());
      args[4].setValue(totalCertCacheSize.getValue());
      args[5].setValue(freeCertCacheSize.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void cacheCertificate(Argument[] args) throws ActionFailedException
  {
    System.out.println("cacheCertificate invoked");
    throw new ActionFailedException(602, "Not implemented");
  }

  public void setTimeHint(Argument[] args) throws ActionFailedException
  {
    System.out.println("setTimeHint invoked");
    throw new ActionFailedException(602, "Not implemented");
  }

  public void getLifetimeSequenceBase(Argument[] args) throws ActionFailedException
  {
    // System.out.println("getLifetimeSequenceBase invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(lifetimeSequenceBase.getValue());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void setSessionKeys(Argument[] args) throws ActionFailedException
  {
    System.out.println("setSessionKeys invoked from " +
      getHTTPParserForCurrentAction().getHTTPMessageObject().getSourceAddress());

    if (args.length != 6)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    byte[] encipheredBulkKey;
    byte[] cipherText;
    String bulkAlgorithm;
    // keyID of calling control point
    int cpKeyID = -1;
    try
    {
      encipheredBulkKey = args[0].getBinBase64Value();
      bulkAlgorithm = args[1].getStringValue();
      cipherText = args[2].getBinBase64Value();
      cpKeyID = (int)args[3].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // System.out.println("Enciphered bulk key is
    // "+SecurityHelper.byteArrayToBase64(encipheredBulkKey));

    // we currently only support 128-Bit AES with CBC
    if (!bulkAlgorithm.equals(CommonSecurityConstant.AES_128_CBC_UPNP))
    {
      throw new ActionFailedException(721, "Algorithm not supported");
    }

    // decipher bulk key
    SymmetricKeyInfo keyInfo =
      SymmetricCryptographyHelper.decryptRSAEncryptedAESKey(getSecuredDevice().getPrivateKey(), encipheredBulkKey);

    if (keyInfo == null)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // decrypt cipherText
    byte[] decryptedCiphertextData =
      SymmetricCryptographyHelper.decryptWithAES(keyInfo.getAESKey(), keyInfo.getIV(), cipherText);
    String decryptedCiphertext = StringHelper.byteArrayToString(decryptedCiphertextData);

    // System.out.println("Decrypted ciphertext is "+decryptedCiphertext);

    // create a session with the received keys
    Session newSession = new Session(decryptedCiphertext, cpKeyID, getSecurityInfoForCurrentAction().getPublicKey());

    if (newSession.isValid() && getSecuredDevice().addSession(newSession))
    {
      try
      {
        args[4].setNumericValue(newSession.getDeviceKeyID());
        args[5].setValue(newSession.getSequenceBase());
      } catch (Exception ex)
      {
        logger.warn(ex.getMessage());
        throw new ActionFailedException(402, "Invalid args");
      }
    } else
    {
      System.out.println("ERROR: Could not add session");
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void expireSessionKeys(Argument[] args) throws ActionFailedException
  {
    System.out.println("expireSessionKeys invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    int deviceKeyID = 0;
    try
    {
      deviceKeyID = (int)args[0].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    Session removedSession = getSecuredDevice().removeSession(deviceKeyID);
    if (removedSession == null)
    {
      throw new ActionFailedException(781, "No such session");
    }
  }

  public void decryptAndExecute(Argument[] args) throws ActionFailedException
  {
    System.out.println("decryptAndExecute invoked");
    if (args.length != 5)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    int deviceKeyID = 0;
    byte[] decryptionSource = null;
    byte[] iv = null;
    try
    {
      deviceKeyID = (int)args[0].getNumericValue();
      decryptionSource = args[1].getBinBase64Value();
      iv = args[2].getBinBase64Value();

    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    Session session = getSecuredDevice().getSessionFromDeviceKeyID(deviceKeyID);

    // decrypt soap request
    byte[] decryptedMessage =
      SymmetricCryptographyHelper.decryptWithAES(session.getConfidentialityToDeviceKey(), iv, decryptionSource);

    // kill last '\0'
    byte[] message = new byte[decryptedMessage.length - 1];
    System.arraycopy(decryptedMessage, 0, message, 0, message.length);

    // socket address is copied from the encrypted action
    String serverAddress = getServerAddressForCurrentAction();
    InetSocketAddress socketAddress = IPHelper.toSocketAddress(serverAddress);

    HTTPMessageObject innerRequest =
      new HTTPMessageObject(HTTPMessageHelper.getHeader(message),
        HTTPMessageHelper.getBody(message),
        socketAddress,
        socketAddress);

    // System.out.println("Decrypted header is [\n" + innerRequest.getHeader() + "]");
    // System.out.println("Decrypted body is [\n" + innerRequest.getBodyAsUTF8String() + "]");

    // process inner message
    HTTPMessageObject innerResponse = messageManager.processMessage(innerRequest);

    // System.out.println("Response is [\n" + innerResponse.getBodyAsUTF8String() + "]");

    byte[] innerResponseData = innerResponse.toByteArray();
    // encrypt response
    // add '0'
    byte[] encryptionSource = new byte[innerResponseData.length + 1];
    System.arraycopy(innerResponseData, 0, encryptionSource, 0, innerResponseData.length);
    encryptionSource[encryptionSource.length - 1] = (byte)0;

    iv = SymmetricCryptographyHelper.generateIV();
    byte[] encryptedMessage =
      SymmetricCryptographyHelper.encryptWithAES(session.getConfidentialityFromDeviceKey(), iv, encryptionSource);

    try
    {
      args[3].setValue(Base64Helper.byteArrayToBase64(encryptedMessage));
      args[4].setValue(Base64Helper.byteArrayToBase64(iv));
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /* SecurityConsole actions */
  public void takeOwnership(Argument[] args) throws ActionFailedException
  {
    System.out.println("takeOwnership invoked");
    String currentLifetimeSequenceBase = (String)lifetimeSequenceBase.getValue();
    // change sequence base
    try
    {
      lifetimeSequenceBase.setValue(SecurityHelper.createSequenceBaseString());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
    }
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String hmacAlgorithm = null;
    byte[] encryptedHmacValue = null;
    try
    {
      hmacAlgorithm = args[0].getStringValue();
      encryptedHmacValue = args[1].getBinBase64Value();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (hmacAlgorithm == null || !hmacAlgorithm.equals(CommonSecurityConstant.HMAC_SHA_1_UPNP))
    {
      throw new ActionFailedException(721, "Algorithm not supported");
    }
    byte[] receivedHmacValue = null;
    try
    {
      // decrypt with own private key
      receivedHmacValue =
        PublicKeyCryptographyHelper.decryptWithRSA(getSecuredDevice().getPrivateKey(), encryptedHmacValue);

      System.out.println("Take ownership: Received HMAC value is   " +
        Base64Helper.byteArrayToBase64(receivedHmacValue));

    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
      throw new ActionFailedException(762, "HMAC failed");
    }
    /*
     * System.out.println("SecurityConsole PublicKeyHash is "+
     * DigestHelper.calculateBase64SHAHashForRSAPublicKey(getSecurityInfo().getPublicKey()));
     * 
     * System.out.println("SecuredDevice PublicKeyHash is "+
     * DigestHelper.calculateBase64SHAHashForRSAPublicKey(getSecuredDevice().getPublicKey()));
     * 
     * System.out.println("SequenceBase is "+currentLifetimeSequenceBase);
     * 
     * System.out.println("Secret is
     * "+SecurityHelper.byteArrayToBase32(getSecuredDevice().getSecret().getEncoded()));
     */

    // build HMAC value with local data
    String hmacBase =
      SecurityHelper.buildRSAPublicKeyXMLDescription(getSecurityInfoForCurrentAction().getPublicKey()) +
        SecurityHelper.buildRSAPublicKeyXMLDescription(getSecuredDevice().getPublicKey()) + currentLifetimeSequenceBase;

    // calculate HMAC
    byte[] hmacValue = DigestHelper.calculateSHA1HMACForString(getSecuredDevice().getSecret(), hmacBase);

    System.out.println("Take ownership: Calculated HMAC value is " + Base64Helper.byteArrayToBase64(hmacValue));

    // compare values
    if (StringHelper.byteArrayToString(receivedHmacValue).equals(StringHelper.byteArrayToString(hmacValue)))
    {
      if (getSecuredDevice().getOwnerCount() != 0)
      {
        throw new ActionFailedException(761, "Device owned");
      }

      // everything is fine, set owner
      getSecuredDevice().addOwner(CommonConstants.SHA_1_UPNP,
        DigestHelper.calculateBase64SHAHashForRSAPublicKey(getSecurityInfoForCurrentAction().getPublicKey()));

    } else
    {
      throw new ActionFailedException(762, "HMAC failed");
    }
  }

  public void getDefinedPermissions(Argument[] args) throws ActionFailedException
  {
    System.out.println("getDefinedPermissions invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(StringHelper.xmlToEscapedString(getSecuredDevice().getDefinedPermissions()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void getDefinedProfiles(Argument[] args) throws ActionFailedException
  {
    System.out.println("getDefinedProfiles invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(StringHelper.xmlToEscapedString(getSecuredDevice().getDefinedProfiles()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void readACL(Argument[] args) throws ActionFailedException
  {
    System.out.println("readACL invoked");
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(getSecuredDevice().getACLVersion());
      args[1].setValue(StringHelper.xmlToEscapedString(getSecuredDevice().getACLXMLDescription()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void writeACL(Argument[] args) throws ActionFailedException
  {
    if (args.length != 5)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(602, "Not implemented");
  }

  public void addACLEntry(Argument[] args) throws ActionFailedException
  {
    System.out.println("addACLEntry invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String entryString = null;
    try
    {
      entryString = (String)args[0].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // try to parse entry
    ACLParser parser = new ACLParser();
    ACLEntry entry = null;
    try
    {
      parser.parse(entryString);
      entry = parser.getACLEntry();
    } catch (SAXException e)
    {
    }
    if (entry == null)
    {
      throw new ActionFailedException(773, "Malformed entry");
    }
    // add entry to internal list and change ACLVersion
    getSecuredDevice().addACLEntry(entry);
    // update evented size state variable
    try
    {
      freeACLSize.setNumericValue(ACL_SIZE - getSecuredDevice().getACLCount());
    } catch (Exception ex)
    {
    }
  }

  public void deleteACLEntry(Argument[] args) throws ActionFailedException
  {
    System.out.println("deleteACLEntry invoked");
    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String versionString = null;
    int index = -1;
    try
    {
      versionString = (String)args[0].getValue();
      index = (int)args[1].getNumericValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    // check version
    if (!versionString.equals(getSecuredDevice().getACLVersion()))
    {
      throw new ActionFailedException(774, "Incorrect ACL Version");
    }
    // check index
    if (index < 0 || index >= getSecuredDevice().getACLCount())
    {
      throw new ActionFailedException(772, "Entry does not exist");
    }
    getSecuredDevice().removeACLEntry(index);
    // update evented size state variable
    try
    {
      freeACLSize.setNumericValue(ACL_SIZE - getSecuredDevice().getACLCount());
    } catch (Exception ex)
    {
    }
    try
    {
      args[2].setValue(getSecuredDevice().getACLVersion());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void replaceACLEntry(Argument[] args) throws ActionFailedException
  {
    if (args.length != 5)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(602, "Not implemented");
  }

  public void factorySecurityReset(Argument[] args) throws ActionFailedException
  {
    System.out.println("factorySecurityReset invoked");
    if (args.length != 0)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    throw new ActionFailedException(602, "Not implemented");
  }

  public void grantOwnership(Argument[] args) throws ActionFailedException
  {
    System.out.println("grantOwnership invoked");
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String hashAlgorithm = null;
    String keyHashBase64 = null;
    try
    {
      hashAlgorithm = (String)args[0].getValue();
      keyHashBase64 = (String)args[1].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (hashAlgorithm == null || !hashAlgorithm.equals(CommonConstants.SHA_1_UPNP))
    {
      throw new ActionFailedException(721, "Algorithm not supported");
    }
    if (keyHashBase64 == null)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (getSecuredDevice().isKnownOwner(hashAlgorithm, keyHashBase64))
    {
      throw new ActionFailedException(765, "Already present");
    }

    if (getSecuredDevice().getOwnerCount() == OWNER_LIST_SIZE)
    {
      throw new ActionFailedException(751, "Out Of Memory");
    }

    // success
    getSecuredDevice().addOwner(CommonConstants.SHA_1_UPNP, keyHashBase64);
    try
    {
      numberOfOwners.setNumericValue(getSecuredDevice().getOwnerCount());
      freeOwnerListSize.setNumericValue(OWNER_LIST_SIZE - getSecuredDevice().getOwnerCount());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
    }
  }

  public void revokeOwnership(Argument[] args) throws ActionFailedException
  {
    System.out.println("revokeOwnership invoked");
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    String hashAlgorithm = null;
    String keyHashBase64 = null;
    try
    {
      hashAlgorithm = (String)args[0].getValue();
      keyHashBase64 = (String)args[1].getValue();
    } catch (Exception ex)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (hashAlgorithm == null || keyHashBase64 == null)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (!getSecuredDevice().isKnownOwner(hashAlgorithm, keyHashBase64))
    {
      throw new ActionFailedException(764, "No such entry");
    }

    // caller of this action cannot remove itself
    String callerHashBase64 = "";
    // check for asymmetric encryption
    if (getSecurityInfoForCurrentAction().getPublicKey() != null)
    {
      callerHashBase64 =
        DigestHelper.calculateBase64SHAHashForRSAPublicKey(getSecurityInfoForCurrentAction().getPublicKey());
    } else
    {
      // action was session signed, retrieve public key of caller
      Session session = getSecuredDevice().getSessionFromDeviceKeyID(getSecurityInfoForCurrentAction().getSessionID());
      if (session != null)
      {
        callerHashBase64 = DigestHelper.calculateBase64SHAHashForRSAPublicKey(session.getControlPointPublicKey());
      }
    }
    if (callerHashBase64.equals(""))
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    if (getSecuredDevice().getOwnerCount() == 1 &&
      getSecuredDevice().getOwner(0).getPublicKeyHashBase64().equals(callerHashBase64))
    {
      throw new ActionFailedException(763, "May not delete itself");
    }

    // success
    getSecuredDevice().removeOwner(hashAlgorithm, keyHashBase64);
    try
    {
      numberOfOwners.setNumericValue(getSecuredDevice().getOwnerCount());
      freeOwnerListSize.setNumericValue(OWNER_LIST_SIZE - getSecuredDevice().getOwnerCount());
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
    }
  }

  public void listOwners(Argument[] args) throws ActionFailedException
  {
    // System.out.println("listOwners invoked");
    if (args.length != 2)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      args[0].setValue(numberOfOwners.getValue());
      args[1].setValue(StringHelper.xmlToEscapedString(getSecuredDevice().getOwnerXMLDescription()));
    } catch (Exception ex)
    {
      logger.warn(ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public String getLifetimeSequenceBase()
  {
    return (String)lifetimeSequenceBase.getValue();
  }

}

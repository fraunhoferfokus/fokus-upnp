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
package de.fraunhofer.fokus.upnp.core.device.personalization;

import java.security.interfaces.RSAPublicKey;
import java.util.Random;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.http.HTTPConstant;
import de.fraunhofer.fokus.upnp.http.HTTPParser;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.XMLHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKeyLoader;
import de.fraunhofer.fokus.upnp.util.security.PersonalizedKeyObject;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;

/**
 * This class implements a secure PersonalizationService that can be used to personalize devices and
 * services .
 * 
 * @author Sebastian Nauck, Alexander Koenig
 */
public class SecuredPersonalizationService extends TemplateService
{

  private Action        listControlPoints;

  private Action        setPassword;

  private Action        getNonce;

  private Action        getSessionKey;

  private StateVariable A_ARG_TYPE_string;

  private StateVariable A_ARG_TYPE_base64;

  /** List of personalized keys used to identify control points. */
  private Vector        personalizedKeyObjectList;

  public SecuredPersonalizationService(TemplateDevice device)
  {
    super(device,
      DeviceConstant.SECURED_PERSONALIZATION_SERVICE_TYPE,
      DeviceConstant.SECURED_PERSONALIZATION_SERVICE_ID,
      true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateService#initServiceContent()
   */
  public void initServiceContent()
  {
    // load public key pair for associated device
    getTemplateDevice().loadPersonalizationKeyPair();
    loadCPPublicKeys();
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // State variables //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    A_ARG_TYPE_string = new StateVariable("A_ARG_TYPE_string", "", false);
    A_ARG_TYPE_base64 = new StateVariable("A_ARG_TYPE_base64", "bin.base64", "", false);
    StateVariable[] StateVariableList = {
        A_ARG_TYPE_string, A_ARG_TYPE_base64
    };
    setStateVariableTable(StateVariableList);

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // //
    // //
    // Actions //
    // //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    listControlPoints = new Action("ListControlPoints");
    listControlPoints.setArgumentTable(new Argument[] {
      new Argument("Result", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    setPassword = new Action("SetPassword");
    setPassword.setArgumentTable(new Argument[] {
      new Argument("Password", UPnPConstant.DIRECTION_IN, A_ARG_TYPE_string)
    });

    getNonce = new Action("GetNonce");
    getNonce.setArgumentTable(new Argument[] {
      new Argument("Nonce", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    getSessionKey = new Action("GetSessionKey");
    getSessionKey.setArgumentTable(new Argument[] {
        new Argument("SessionKey", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_base64),
        new Argument("SequenceBase", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string),
        new Argument("KeyID", UPnPConstant.DIRECTION_OUT, A_ARG_TYPE_string)
    });

    setActionTable(new Action[] {
        listControlPoints, setPassword, getNonce, getSessionKey
    });
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Returns a nonce to prevent replay attacks. */
  public void getNonce(Argument[] args) throws ActionFailedException
  {
    printMessage("GetNonce invoked");
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    PersonalizedKeyObject keyObject = getPersonalizedKeyObjectForHTTPParser(getHTTPParserForCurrentAction());
    try
    {
      if (keyObject.getNonce() == null)
      {
        String nonce = SecurityHelper.createSequenceBaseString();
        keyObject.setNonce(nonce);
      }
      // System.out.println("Returned nonce is " + keyObject.getNonce());
      args[0].setValue(keyObject.getNonce());

    } catch (Exception e)
    {
      logger.warn(e.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Returns an AES key that can be used to encrypt argument values. */
  public void getSessionKey(Argument[] args) throws ActionFailedException
  {
    printMessage("GetSessionKey invoked");

    if (args.length != 3)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    PersonalizedKeyObject keyObject = getPersonalizedKeyObjectForHTTPParser(getHTTPParserForCurrentAction());
    RSAPublicKey publicKey = keyObject.getPersistentRSAPublicKey();

    // if we get here, the message was signed and the signature was valid
    System.out.println("Generate personalized AES key");
    try
    {
      SecretKey aesKey = SymmetricCryptographyHelper.generateAESKey();
      byte[] aesKeyData = aesKey.getEncoded();
      byte[] iv = SymmetricCryptographyHelper.generateIV();

      String encryptedAESKey = SymmetricCryptographyHelper.encryptAESKeyWithRSA(aesKeyData, iv, publicKey);
      args[0].setValue(encryptedAESKey);
      String keyId = generateKeyID(aesKey);
      long sequenceBase = SecurityHelper.createLongSequenceBase();
      args[1].setValue(sequenceBase + "");

      keyObject.setSymmetricKey(aesKey, iv);
      keyObject.setKeyID(keyId);
      keyObject.setSequenceBase(sequenceBase);
      args[2].setValue(keyId);
    } catch (Exception e)
    {
      logger.warn(e.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  public void listControlPoints(Argument[] args) throws ActionFailedException
  {
    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    try
    {
      String keyList = XMLHelper.createStartTag("KeyList");
      for (int i = 0; i < personalizedKeyObjectList.size(); i++)
      {
        PersonalizedKeyObject keyObject = (PersonalizedKeyObject)personalizedKeyObjectList.elementAt(i);
        keyList += keyObject.toRSAPublicKeyXMLDescription();
      }
      keyList += XMLHelper.createEndTag("KeyList");
      args[0].setValue(StringHelper.xmlToEscapedString(keyList));

    } catch (Exception ex)
    {
      System.out.println(ex.getMessage());
      logger.warn("in Exception" + ex.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /** Sets the password of the current user */
  public void setPassword(Argument[] args) throws ActionFailedException
  {
    printMessage("SetPassword invoked");

    if (args.length != 1)
    {
      throw new ActionFailedException(402, "Invalid args");
    }
    PersonalizedKeyObject keyObject = getPersonalizedKeyObjectForHTTPParser(getHTTPParserForCurrentAction());
    try
    {
      keyObject.setPassword(args[0].getStringValue());
    } catch (Exception e)
    {
      System.out.println(e.getMessage());
      logger.warn(e.getMessage());
      throw new ActionFailedException(402, "Invalid args");
    }
  }

  /**
   * Retrieves the key object for a specific SOAP request.
   * 
   * @return The associated key object or null
   */
  public PersonalizedKeyObject getPersonalizedKeyObjectForHTTPParser(HTTPParser parser) throws ActionFailedException
  {
    if (parser == null)
    {
      throw new ActionFailedException(701, "Missing key");
    }
    RSAPublicKey publicKey = parser.getPublicKey(HTTPConstant.X_PERSONALIZATION_PUBLIC_KEY);
    String keyID = parser.getValue(HTTPConstant.X_PERSONALIZATION_KEY_ID);

    PersonalizedKeyObject keyObject = null;
    // first check for symmetric keyID
    if (keyID != null)
    {
      keyObject = getPersonalizedKeyObjectFromKeyID(keyID);
      if (keyObject == null)
      {
        throw new ActionFailedException(781, "No Such Session");
      }
    }
    // keyID not transmitted, check public key
    if (keyObject == null)
    {
      if (publicKey == null)
      {
        throw new ActionFailedException(701, "Missing key");
      } else
      {
        keyObject = getPersonalizedKeyObjectFromRSAKey(publicKey);
      }
    }

    // it may be that the public key of the caller is not known
    // for now, we simply add the public key to our internal list
    if (keyObject == null)
    {
      printMessage(toString() + ": Sent public key not found, create new personalized key object");
      keyObject =
        new PersonalizedKeyObject(new PersistentRSAPublicKey(publicKey.getPublicExponent(), publicKey.getModulus()), "");
      personalizedKeyObjectList.add(keyObject);
    }
    return keyObject;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Public methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Returns the personalized key object for a certain public key. */
  public PersonalizedKeyObject getPersonalizedKeyObjectFromRSAKey(RSAPublicKey publicKey)
  {
    if (publicKey == null)
    {
      return null;
    }

    for (int i = 0; i < personalizedKeyObjectList.size(); i++)
    {
      PersonalizedKeyObject keyObject = (PersonalizedKeyObject)personalizedKeyObjectList.elementAt(i);
      PersistentRSAPublicKey rsaPublicKey = keyObject.getPersistentRSAPublicKey();
      if (rsaPublicKey.equals(publicKey))
      {
        return keyObject;
      }
    }
    return null;
  }

  /** Returns the personalized key object for a certain key ID. */
  public PersonalizedKeyObject getPersonalizedKeyObjectFromKeyID(String keyID)
  {
    if (keyID == null)
    {
      return null;
    }

    for (int i = 0; i < personalizedKeyObjectList.size(); i++)
    {
      PersonalizedKeyObject keyObject = (PersonalizedKeyObject)personalizedKeyObjectList.elementAt(i);
      if (keyObject.getKeyID() != null && keyObject.getKeyID().equals(keyID))
      {
        return keyObject;
      }
    }
    return null;
  }

  public Vector getPersonalizedKeyObjectList()
  {
    return personalizedKeyObjectList;
  }

  /** Loads the public keys from known control points. */
  private void loadCPPublicKeys()
  {
    personalizedKeyObjectList = new Vector();
    Vector keyCacheData = PersistentRSAPublicKeyLoader.loadRSAPublicKeys(true);
    for (int i = 0; i < keyCacheData.size(); i++)
    {
      personalizedKeyObjectList.add(keyCacheData.elementAt(i));
    }
  }

  /** Generates a new key ID. */
  private String generateKeyID(SecretKey aesKey)
  {
    Random random = new Random();
    int loop = 1;
    while (loop < 10000)
    {
      String keyID = Math.abs(random.nextInt()) + "";
      if (getPersonalizedKeyObjectFromKeyID(keyID) == null)
      {
        return keyID;
      }

      loop++;
    }
    return null;
  }
}

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
package de.fraunhofer.fokus.upnp.core_security.templates;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core.templates.TemplateControlPoint;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.SecurityConstant;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPDeviceObject;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPEventMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPSSDPMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.control_point.SecurityAwareCPService;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.AlgorithmsAndProtocolsParser;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.PublicKeysParser;
import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.security.CommonSecurityConstant;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricCryptographyHelper;
import de.fraunhofer.fokus.upnp.util.security.SymmetricKeyInfo;

/**
 * This class represents a control point that can invoke secured actions on devices.
 * 
 * @author Alexander Koenig
 * 
 */
public class SecurityAwareTemplateControlPoint extends TemplateControlPoint implements Runnable
{
  public static String PREFERRED_NAME                  = "SecurityAwareControlPoint";

  // RSA key for UPnP security
  private KeyPair      rsaKeyPair;

  private String       securityID;

  // Array with currently active sessions
  private Session[]    sessions                        = new Session[SecurityConstant.MAX_SESSION_COUNT];

  // List of devices with known public keys
  private Vector       securityAwareDeviceObjectList   = new Vector();

  // List of devices with unknown public keys
  private Vector       pendingSecurityAwareDeviceList  = new Vector();

  // List of security consoles (to check for certificates)
  private Vector       securityConsoleList             = new Vector();

  /** Hashtable containing all devices that are currently trying to start a session */
  private Hashtable    setSessionPendingHashTable      = new Hashtable();

  /**
   * Hashtable containing symmetric keys used for security that were retrieved during device
   * description. This key is moved to a SecurityAwareCPDeviceObject as soon as the public key of
   * the remote device is known.
   */
  private Hashtable    symmetricKeyInfoFromDeviceTable = new Hashtable();

  private CPDevice     pendingDevice                   = null;

  private boolean      terminateThread                 = false;

  private boolean      terminated                      = false;

  /**
   * Create a new instance of SecurityAwareControlPoint. The basic control point is not started.
   * 
   * @param anEntity
   *          Reference to entity
   * @param startupConfiguration
   *          Startup configuration
   * 
   */
  public SecurityAwareTemplateControlPoint(SecurityAwareTemplateEntity anEntity,
    UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);

    // use the personalization key pair also for UPnP security
    rsaKeyPair = personalizationKeyPair;
    securityID = DigestHelper.rsaPublicKeyToSecurityID(getPublicKey());
    printMessage(toString() + ": Create security aware control point. UPnP securityID is " + securityID);

    // System.out.println("\n Create security aware control point with securityID "+securityID);
    Thread thread = new Thread(this);
    thread.setName(toString() + ".SecurityAwareTemplateControlPoint");
    thread.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#forceRunDelayed()
   */
  public boolean forceRunDelayed()
  {
    // all security aware control points are started delayed to load RSA keys etc.
    // prior to starting the normal UPnP control point
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#basicControlPointCreated()
   */
  public void basicControlPointCreated()
  {
    // associate security aware control point with secured instances of SSDP and event message
    // handling
    if (basicControlPoint.getCPSSDPMessageProcessor() instanceof SecurityAwareCPSSDPMessageProcessor)
    {
      ((SecurityAwareCPSSDPMessageProcessor)basicControlPoint.getCPSSDPMessageProcessor()).setSecurityAwareTemplateControlPoint(this);
    }
    if (basicControlPoint.getCPEventMessageProcessor() instanceof SecurityAwareCPEventMessageProcessor)
    {
      ((SecurityAwareCPEventMessageProcessor)basicControlPoint.getCPEventMessageProcessor()).setSecurityAwareTemplateControlPoint(this);
    }
  }

  /** Returns the entity associated with this control point */
  public SecurityAwareTemplateEntity getSecurityAwareEntity()
  {
    return (SecurityAwareTemplateEntity)getTemplateEntity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#getInstanceOfCPMessageProcessorFactory()
   */
  protected CPMessageProcessorFactory getInstanceOfCPMessageProcessorFactory()
  {
    // use a security aware version of the message processor factory
    return new SecurityAwareCPMessageProcessorFactory();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Session management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Adds a session for a specific security aware device. */
  public Session addSession(SecurityAwareCPDeviceObject device)
  {
    // search free session
    int index = 0;
    while (index < SecurityConstant.MAX_SESSION_COUNT && sessions[index] != null)
    {
      index++;
    }
    // unused session found
    if (index < SecurityConstant.MAX_SESSION_COUNT)
    {
      sessions[index] = new Session(index, device);
      return sessions[index];
    }
    // no free slot found
    return null;
  }

  /** Retrieves the session for a specific security aware device. */
  public Session getSession(SecurityAwareCPDeviceObject device)
  {
    // search session
    for (int i = 0; i < SecurityConstant.MAX_SESSION_COUNT; i++)
    {
      if (sessions[i] != null && sessions[i].getAssociatedDeviceObject() == device)
      {
        return sessions[i];
      }
    }
    // no session found
    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Key management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Returns the hash of the public key of this control point */
  public String getSecurityID()
  {
    return securityID;
  }

  /** Returns the public key of this control point */
  public RSAPublicKey getPublicKey()
  {
    return (RSAPublicKey)rsaKeyPair.getPublic();
  }

  /** Returns the private key of this control point */
  public RSAPrivateKey getPrivateKey()
  {
    return (RSAPrivateKey)rsaKeyPair.getPrivate();
  }

  /**
   * Uses the current key used for personalization also for UPnP security
   */
  public void usePersonalizationKeyForSecurity()
  {
    rsaKeyPair = personalizationKeyPair;
    securityID = DigestHelper.rsaPublicKeyToSecurityID(getPublicKey());
    printMessage(toString() + ": Changed UPnP securityID is " + securityID);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // UPnP events //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#newDevice(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    CPService deviceSecurity = newDevice.getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
    // replace all CPServices with its security aware pendants if device security is found
    if (deviceSecurity != null)
    {
      CPService[] services = newDevice.getCPServiceTable();
      for (int i = 0; i < services.length; i++)
      {
        // create security aware copy of CPService
        SecurityAwareCPService securityAwareCPService = new SecurityAwareCPService(services[i], this);
        // replace security unaware CPService
        services[i] = securityAwareCPService;
      }
      newDevice.setCPServiceTable(services);
    }
    // handle in parent method
    super.newDevice(newDevice);
    // check that device does not belong to the same entity as the control point
    if (!isEntityDevice(newDevice.getUDN()))
    {
      // request public key etc.
      if (deviceSecurity != null && !isKnownPendingDevice(newDevice.getUDN()))
      {
        pendingSecurityAwareDeviceList.add(newDevice);
      }
      CPService securityConsoleService = newDevice.getCPServiceByType(SecurityConstant.SECURITY_CONSOLE_SERVICE_TYPE);
      if (securityConsoleService != null)
      {
        // save security console for later certificate checking
        addSecurityConsole(newDevice);
        // announce own existence to other security console
        invokePresentKey(securityConsoleService);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#deviceGone(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    super.deviceGone(goneDevice);
    // check that device does not belong to the same entity as the control point
    if (!isEntityDevice(goneDevice.getUDN()))
    {
      CPService securityConsole = goneDevice.getCPServiceByType(SecurityConstant.SECURITY_CONSOLE_SERVICE_TYPE);
      if (securityConsole != null)
      {
        // remove from security console list
        removeSecurityConsole(goneDevice);
      }

      // remove from list of devices that implement DeviceSecurity
      CPService deviceSecurity = goneDevice.getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
      if (deviceSecurity != null)
      {
        // remove from pending list
        synchronized(pendingSecurityAwareDeviceList)
        {
          int index = getPendingDeviceIndex(goneDevice.getUDN());
          if (index == 0)
          {
            pendingSecurityAwareDeviceList.remove(0);
            pendingDevice = null;
          }
          if (index > 0)
          {
            pendingSecurityAwareDeviceList.remove(index);
          }
        }
        // remove from local list
        int index = getSecurityAwareDeviceIndex(goneDevice.getUDN());
        if (index != -1)
        {
          // inform entity
          getSecurityAwareEntity().securityAwareCPDeviceGone(getSecurityAwareCPDeviceObject(index));
          // remove from list
          securityAwareDeviceObjectList.remove(index);
        }
      }
      // remove from symmetric key info if needed
      if (symmetricKeyInfoFromDeviceTable.containsKey(goneDevice))
      {
        symmetricKeyInfoFromDeviceTable.remove(goneDevice);
      }
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Device management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Returns the number of discovered security aware device objects */
  public int getSecurityAwareCPDeviceObjectCount()
  {
    return securityAwareDeviceObjectList.size();
  }

  /** Adds a security aware device object to the local list */
  public void addSecurityAwareDeviceObject(SecurityAwareCPDeviceObject device)
  {
    if (!isKnownSecurityAwareDevice(device.getCPDevice().getUDN()))
    {
      securityAwareDeviceObjectList.add(device);
    }
  }

  /** Returns a specific security aware device object */
  public SecurityAwareCPDeviceObject getSecurityAwareCPDeviceObject(int index)
  {
    if (index >= 0 && index < securityAwareDeviceObjectList.size())
    {
      return (SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(index);
    }

    return null;
  }

  /** Returns a specific security aware device object */
  public SecurityAwareCPDeviceObject getSecurityAwareCPDeviceObject(String udn)
  {
    for (int i = 0; i < securityAwareDeviceObjectList.size(); i++)
    {
      if (((SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return (SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(i);
      }
    }

    return null;
  }

  /** Returns a specific security aware device object */
  public SecurityAwareCPDeviceObject getSecurityAwareCPDeviceObjectByKeyName(String keyName)
  {
    for (int i = 0; i < securityAwareDeviceObjectList.size(); i++)
    {
      SecurityAwareCPDeviceObject currentObject =
        (SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(i);
      if (currentObject.getSymmetricKeyName() != null && currentObject.getSymmetricKeyName().equals(keyName))
      {
        return currentObject;
      }
    }

    return null;
  }

  /**
   * Checks if this control point is the owner of a specific device. Returns false if the owner list
   * was not yet read.
   */
  public boolean isOwnerOfSecurityAwareDevice(String udn)
  {
    for (int i = 0; i < securityAwareDeviceObjectList.size(); i++)
    {
      SecurityAwareCPDeviceObject currentDevice =
        (SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(i);
      if (currentDevice.hasValidOwners())
      {
        Vector owners = currentDevice.getOwners();
        for (int j = 0; j < owners.size(); j++)
        {
          SecurityAwareObject currentOwner = (SecurityAwareObject)owners.elementAt(j);
          if (currentOwner.getSecurityID().equals(getSecurityID()))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /** Checks if a security aware device is already known */
  private boolean isKnownSecurityAwareDevice(String udn)
  {
    return getSecurityAwareDeviceIndex(udn) != -1;
  }

  /** Returns the index of a specific security aware device */
  private int getSecurityAwareDeviceIndex(String udn)
  {
    for (int i = 0; i < securityAwareDeviceObjectList.size(); i++)
    {
      if (((SecurityAwareCPDeviceObject)securityAwareDeviceObjectList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of a device that is already discovered but not yet checked for its public
   * keys
   */
  private int getPendingDeviceIndex(String udn)
  {
    for (int i = 0; i < pendingSecurityAwareDeviceList.size(); i++)
    {
      if (((CPDevice)pendingSecurityAwareDeviceList.elementAt(i)).getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /** Checks if a device is already discovered but not yet checked for its public keys */
  private boolean isKnownPendingDevice(String udn)
  {
    return getPendingDeviceIndex(udn) != -1;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Security console management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void addSecurityConsole(CPDevice securityConsole)
  {
    if (!isKnownSecurityConsole(securityConsole.getUDN()))
    {
      securityConsoleList.add(securityConsole);
    }
  }

  public void removeSecurityConsole(CPDevice securityConsole)
  {
    int i = 0;
    while (i < securityConsoleList.size())
    {
      if (((CPDevice)securityConsoleList.elementAt(i)).getUDN().equals(securityConsole.getUDN()))
      {
        securityConsoleList.remove(i);
      } else
      {
        i++;
      }
    }
  }

  public boolean isKnownSecurityConsole(String udn)
  {
    for (int i = 0; i < securityConsoleList.size(); i++)
    {
      if (((CPDevice)securityConsoleList.elementAt(i)).getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  public int getSecurityConsoleCount()
  {
    return securityConsoleList.size();
  }

  public CPDevice getSecurityConsole(int index)
  {
    if (index >= 0 && index < securityConsoleList.size())
    {
      return (CPDevice)securityConsoleList.elementAt(index);
    }

    return null;
  }

  /**
   * Retrieves the symmetricKeyInfoFromDeviceTable.
   * 
   * @return The symmetricKeyInfoFromDeviceTable
   */
  public Hashtable getSymmetricKeyInfoFromDeviceTable()
  {
    return symmetricKeyInfoFromDeviceTable;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action invocation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Presents the own public key to other security aware control points */
  private void invokePresentKey(CPService securityConsole)
  {
    CPAction action = securityConsole.getCPAction("PresentKey");
    if (action != null)
    {
      try
      {
        // set in parameters
        Argument argHashAlgorithm = action.getInArgument("HashAlgorithm");
        if (argHashAlgorithm != null)
        {
          argHashAlgorithm.setValue(CommonConstants.SHA_1_UPNP);
        }

        String keyString =
          StringHelper.xmlToEscapedString(SecurityHelper.buildRSAPublicKeyXMLDescription(getPublicKey()));
        Argument argKey = action.getInArgument("Key");
        if (argKey != null)
        {
          argKey.setValue(keyString);
        }

        // System.out.println("Send key string:"+keyString);

        Argument argPreferredName = action.getInArgument("PreferredName");
        if (argPreferredName != null)
        {
          argPreferredName.setValue(PREFERRED_NAME);
        }

        Argument argIconDesc = action.getInArgument("IconDesc");
        if (argIconDesc != null)
        {
          argIconDesc.setValue("");
        }

        if (argHashAlgorithm != null && argKey != null && argPreferredName != null && argIconDesc != null)
        {
          invokeUnsignedAction(action);
        }
      } catch (Exception ex)
      {
        logger.warn(ex.getMessage());
      }
    }
  }

  /** Sets symmetric keys for a session */
  public void invokeSetSessionKeys(SecurityAwareCPDeviceObject device)
  {
    // get service
    SecurityAwareCPService deviceSecurity =
      (SecurityAwareCPService)device.getCPDevice().getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);

    System.out.println("ControlPoint: Set session keys");

    if (deviceSecurity != null)
    {
      // retrieve sequence base
      String sequenceBaseString = null;
      CPAction action = deviceSecurity.getCPAction("GetLifetimeSequenceBase");
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
        // get action
        action = deviceSecurity.getCPAction("SetSessionKeys");
        if (action != null)
        {
          try
          {
            // System.out.println("Try to establish session");
            // try to establish session
            Session session = addSession(device);
            // session is valid
            if (session != null)
            {
              // generate key for sessionKey encryption
              SecretKey aesKey = SymmetricCryptographyHelper.generateAESKey();
              byte[] iv = SymmetricCryptographyHelper.generateIV();

              String encryptedBulkKeyBase64 =
                SymmetricCryptographyHelper.encryptAESKeyWithRSA(aesKey.getEncoded(),
                  iv,
                  device.getConfidentialityKey());

              // System.out.println("Enciphered bulk key is "+encryptedBulkKeyBase64);

              Argument argEncipheredBulkKey = action.getInArgument("EncipheredBulkKey");
              if (argEncipheredBulkKey != null)
              {
                argEncipheredBulkKey.setValue(encryptedBulkKeyBase64);
              }

              Argument argBulkAlgorithm = action.getInArgument("BulkAlgorithm");
              if (argBulkAlgorithm != null)
              {
                argBulkAlgorithm.setValue(CommonSecurityConstant.AES_128_CBC_UPNP);
              }

              // build session key argument
              String sessionKeys = session.toXMLDescription();
              byte[] ciphertext =
                SymmetricCryptographyHelper.encryptWithAES(aesKey, iv, StringHelper.stringToByteArray(sessionKeys));

              Argument argCiphertext = action.getInArgument("Ciphertext");
              if (argCiphertext != null)
              {
                argCiphertext.setValue(Base64Helper.byteArrayToBase64(ciphertext));
              }

              Argument argCPKeyID = action.getInArgument("CPKeyID");
              if (argCPKeyID != null)
              {
                argCPKeyID.setNumericValue(session.getCpKeyID());
              }

              if (argEncipheredBulkKey != null && argBulkAlgorithm != null && argCiphertext != null &&
                argCPKeyID != null)
              {
                deviceSecurity.invokeRSASignedAction(action, sequenceBaseString, getPrivateKey(), getPublicKey());

                // process result
                Argument deviceKeyID = action.getOutArgument("DeviceKeyID");
                session.setDeviceKeyID((int)deviceKeyID.getNumericValue());

                Argument sequenceBase = action.getOutArgument("SequenceBase");
                session.setSequenceBase((String)sequenceBase.getValue());

              }
            }
          } catch (Exception ex)
          {
            System.out.println("ERROR:" + ex.getMessage());
            logger.warn(ex.getMessage());
          }
        }
      }
    }
  }

  /** Ends a session */
  public void invokeExpireSessionKeys(Session session)
  {
    // get service
    SecurityAwareCPService deviceSecurity =
      (SecurityAwareCPService)session.getAssociatedDeviceObject()
        .getCPDevice()
        .getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);

    System.out.println("ControlPoint: Expire session keys");

    if (deviceSecurity != null)
    {
      // get action
      CPAction action = deviceSecurity.getCPAction("ExpireSessionKeys");
      if (action != null)
      {
        try
        {
          Argument argDeviceKeyID = action.getInArgument("DeviceKeyID");
          if (argDeviceKeyID != null)
          {
            argDeviceKeyID.setNumericValue(session.getDeviceKeyID());
          }

          if (argDeviceKeyID != null)
          {
            // increment sequence number for control point
            session.incCPSequenceNumber();
            deviceSecurity.invokeSHA1HMACSignedAction(action,
              session.getSequenceBase(),
              Integer.toString(session.getCPSequenceNumber()),
              session.getSigningToDeviceKey(),
              session.getDeviceKeyID());
          }
        } catch (Exception ex)
        {
          System.out.println("ERROR:" + ex.getMessage());
          logger.warn(ex.getMessage());
        }
      }
    }
  }

  /** Checks if the device understands signed actions. */
  public boolean supportsDeviceSecurity(CPDevice device)
  {
    return getSecurityAwareCPDeviceObject(device.getUDN()) != null;
  }

  /**
   * Invokes an action on the remote device. If the remote device is security aware, the action is
   * also signed with the control points private key.
   */
  public void invokeAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    // check if the device implements security
    SecurityAwareCPDeviceObject securityAwareDevice =
      getSecurityAwareCPDeviceObject(action.getCPService().getCPDevice().getUDN());

    if (securityAwareDevice != null)
    {
      invokeSecuredAction(securityAwareDevice, action);
    } else
    {
      super.invokeAction(action);
    }
  }

  /** Invokes an unsigned action, even for security aware devices. */
  public void invokeUnsignedAction(CPAction action) throws InvokeActionException, ActionFailedException
  {
    super.invokeAction(action);
  }

  /** Invokes a secured action */
  public void invokeSecuredAction(SecurityAwareCPDeviceObject device, CPAction action) throws InvokeActionException,
    ActionFailedException
  {
    CPService service = action.getCPService();
    // check if a session to this device is already established
    Session session = getSession(device);
    // check session counter and end session if necessary
    if (session != null && session.getCPSequenceNumber() > Integer.MAX_VALUE - 1000)
    {
      invokeExpireSessionKeys(session);
      session = null;
    }
    if (session == null)
    {
      // check if a session request is already pending
      while (setSessionPendingHashTable.containsKey(device))
      {
        System.out.println("Wait for pending session request to end");
        ThreadHelper.sleep(100);
      }
      session = getSession(device);
    }
    // no session found, open a new session
    if (session == null)
    {
      setSessionPendingHashTable.put(device, new Object());
      invokeSetSessionKeys(device);
      setSessionPendingHashTable.remove(device);
      session = getSession(device);
    }
    if (session != null)
    {
      // increment sequenceNumber
      session.incCPSequenceNumber();

      System.out.println("Invoke session signed action " + action.getName());

      ((SecurityAwareCPService)service).invokeSHA1HMACSignedAction(action,
        session.getSequenceBase(),
        Integer.toString(session.getCPSequenceNumber()),
        session.getSigningToDeviceKey(),
        session.getDeviceKeyID());

      return;
    } else
    // no session available, send with asymmetric encryption
    {
      System.out.println("No session available, use asymmetric encryption for " + action.getName());
      // get securityService
      CPService deviceSecurity = device.getCPDevice().getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
      if (deviceSecurity != null)
      {
        CPStateVariable lifetimeSequenceBase = deviceSecurity.getCPStateVariable("LifetimeSequenceBase");
        if (lifetimeSequenceBase != null)
        {
          ((SecurityAwareCPService)service).invokeRSASignedAction(action,
            (String)lifetimeSequenceBase.getValue(),
            getPrivateKey(),
            getPublicKey());

          return;
        }
      }
    }
    throw new ActionFailedException(501, "Action failed");
  }

  /** Invokes an encrypted action */
  public void invokeEncryptedAction(SecurityAwareCPDeviceObject device, CPAction action) throws InvokeActionException,
    ActionFailedException
  {
    // check if a session to this device is already established
    Session session = getSession(device);
    // check session counter and end session if necessary
    if (session != null && session.getCPSequenceNumber() > Integer.MAX_VALUE - 1000)
    {
      invokeExpireSessionKeys(session);
      session = null;
    }
    // no session found, open a new session
    if (session == null)
    {
      invokeSetSessionKeys(device);
      session = getSession(device);
    }
    if (session != null)
    {
      // increment sequenceNumber
      session.incCPSequenceNumber();

      // retrieve decryptAndExecute action from deviceSecurityService
      CPService deviceSecurity = device.getDeviceSecurityService();
      CPAction decryptAndExecuteAction = deviceSecurity.getCPAction("DecryptAndExecute");

      System.out.println("Invoke encrypted action " + action.getName());

      ((SecurityAwareCPService)action.getCPService()).invokeEncryptedSHA1HMACSignedAction(action,
        deviceSecurity,
        decryptAndExecuteAction,
        session);

      return;

    }
    // no session available, create error
    throw new ActionFailedException(501, "Action failed");
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Thread management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Actions that are called for each detected security aware device. These requests are made in an
   * extra thread. The entity is informed about the new device after this method.
   */
  public void initialActionRequests(SecurityAwareCPDeviceObject securityDevice)
  {
    CPService deviceSecurityService =
      securityDevice.getCPDevice().getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);
    if (deviceSecurityService != null)
    {
      // retrieve algorithms and protocols
      CPAction action = deviceSecurityService.getCPAction("GetAlgorithmsAndProtocols");
      if (action != null)
      {
        try
        {
          invokeUnsignedAction(action);

          String supportedArgString = action.getOutArgument("Supported").getStringValue();
          AlgorithmsAndProtocolsParser parser = new AlgorithmsAndProtocolsParser(supportedArgString);
          securityDevice.setSupported(parser);
        } catch (Exception ex)
        {
          logger.warn(ex.getMessage());
        }
      }
    }
  }

  /** Retrieves the public keys of detected security aware devices */
  public void run()
  {
    // System.out.println(" Run security aware control point thread...");
    while (!terminateThread)
    {
      if (pendingSecurityAwareDeviceList.size() > 0)
      {
        // get first pending device
        synchronized(pendingSecurityAwareDeviceList)
        {
          pendingDevice = (CPDevice)pendingSecurityAwareDeviceList.elementAt(0);
        }
        CPService deviceSecurityService =
          pendingDevice.getCPServiceByType(SecurityConstant.DEVICE_SECURITY_SERVICE_TYPE);

        if (deviceSecurityService != null)
        {
          SecurityAwareCPDeviceObject securityDeviceObject = null;
          // retrieve public keys
          CPAction action = deviceSecurityService.getCPAction("GetPublicKeys");
          if (action != null)
          {
            try
            {
              invokeUnsignedAction(action);

              String keyArgString = action.getOutArgument("KeyArg").getStringValue();

              PublicKeysParser parser = new PublicKeysParser(keyArgString);
              PersistentRSAPublicKey publicKey = parser.getPublicKey();
              // check if key is valid
              synchronized(pendingSecurityAwareDeviceList)
              {
                if (pendingDevice != null && publicKey != null)
                {
                  // create device object for this public key
                  securityDeviceObject =
                    new SecurityAwareCPDeviceObject(CommonConstants.SHA_1_UPNP, publicKey, pendingDevice);

                  // add symmetric key to security object if known
                  if (symmetricKeyInfoFromDeviceTable.containsKey(pendingDevice))
                  {
                    System.out.println("Move symmetric key info to SecurityAwareCPDeviceObject");
                    SymmetricKeyInfo keyInfo = (SymmetricKeyInfo)symmetricKeyInfoFromDeviceTable.get(pendingDevice);
                    securityDeviceObject.setSymmetricKeyInfo(keyInfo);
                    symmetricKeyInfoFromDeviceTable.remove(pendingDevice);
                  }

                  // System.out.println(" CP: Found security aware device with securityID "+
                  // securityDevice.getSecurityID());
                  // add to local list of security aware devices
                  securityAwareDeviceObjectList.add(securityDeviceObject);
                }
              }
            } catch (Exception ex)
            {
              logger.warn(ex.getMessage());
            }
          }

          if (securityDeviceObject != null)
          {
            // this method can be overridden by subclasses to allow additional requests
            initialActionRequests(securityDeviceObject);
            // inform entity
            getSecurityAwareEntity().newSecurityAwareCPDevice(securityDeviceObject);
          }
        }
        // remove first device from pending list
        synchronized(pendingSecurityAwareDeviceList)
        {
          if (pendingSecurityAwareDeviceList.size() > 0)
          {
            pendingSecurityAwareDeviceList.remove(0);
          }
        }
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
    // System.out.println(" Shutdown security aware control point thread...");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateControlPoint#terminate()
   */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
    super.terminate();
  }

}

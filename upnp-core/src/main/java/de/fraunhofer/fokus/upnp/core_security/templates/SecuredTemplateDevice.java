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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fraunhofer.fokus.upnp.configuration.SecurityDefaults;
import de.fraunhofer.fokus.upnp.core.device.DeviceMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core.device.DeviceService;
import de.fraunhofer.fokus.upnp.core.device.DeviceSupportFactory;
import de.fraunhofer.fokus.upnp.core.templates.TemplateDevice;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_security.device.SecuredDeviceHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.core_security.device.SecuredDeviceMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core_security.device.SecuredDeviceSupportFactory;
import de.fraunhofer.fokus.upnp.core_security.device.SecurityAwareControlPointObject;
import de.fraunhofer.fokus.upnp.core_security.deviceSecurity.DeviceSecurityService;
import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.ElementBuilder;
import de.fraunhofer.fokus.upnp.core_security.helpers.SecurityAwareObject;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.core_security.helpers.parser.ACLParser;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.UUIDHelper;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAKeyPair;
import de.fraunhofer.fokus.upnp.util.security.SecurityHelper;

/**
 * A secured device implements the DeviceSecurityService and can limit the use of actions for all
 * associated services. Services must be child classes of SecuredTemplateService to use this
 * feature.
 * 
 * @author Alexander Koenig
 */
public class SecuredTemplateDevice extends TemplateDevice
{

  private static final String   OWNER_FILE      = "owner.txt";

  private static final String   ACL_FILE        = "acl.txt";

  private DeviceSecurityService deviceSecurityService;

  private KeyPair               rsaKeyPair;

  private String                securityID;

  private String                definedPermissions;

  private String                definedProfiles;

  private Vector                securedServiceList;

  // list of SecurityAwareObject
  private Vector                ownerList;

  private String                ownerXMLDescription;

  private Vector                sessionList;

  private int                   sessionCounter;

  // Secret for TakeOwnership
  private SecretKey             secret;

  // ACL
  private Vector                aclList;

  private String                aclXMLDescription;

  private String                aclListVersion;

  // Proprietary extensions to UPnP

  /** Flag that all subscription requests must be signed. */
  protected boolean             signedEventSubscriptions;

  /** Flag that all events will be signed. */
  protected boolean             signedEvents;

  /** Flag that all event messages will be encrypted. */
  protected boolean             encryptedEvents;

  /** Flag that all description requests must be signed. */
  protected boolean             signedDescriptionRequests;

  /** Flag that all description responses will be encrypted. */
  protected boolean             encryptedDescriptionResponses;

  /** Flag that this device should only advertise its root device. */
  protected boolean             anonymousDiscovery;

  /** Flag that this device should use anonymous URLs. */
  protected boolean             anonymousURLs;

  private Vector                nonceList;

  private Object                keyCreationLock = new Object();

  /** Hashtable containing key information for remote control points */
  private Hashtable             securityAwareControlPointObjectFromPublicKeyTable;

  /**
   * Creates a new instance of SecuredTemplateDevice.
   * 
   * @param anEntity
   *          Associated entity
   * @param keyPair
   *          Private/Public key pair that identifies the device
   * @param deviceType
   *          Type of the device
   * @param friendlyName
   *          Name of the device
   * @param manufacturer
   *          Manufacturer of the device
   * @param modelName
   *          Model of the device
   * @param UDN
   *          Unique device name
   * 
   */
  public SecuredTemplateDevice(SecurityAwareTemplateEntity anEntity, UPnPStartupConfiguration startupConfiguration)
  {
    super(anEntity, startupConfiguration);

    String keyFileName =
      startupConfiguration.getWorkingDirectory() +
        FileHelper.getHostBasedFileName(deviceStartupConfiguration.getProperty("DeviceKeyFile", "deviceKeys"));

    rsaKeyPair = PersistentRSAKeyPair.getPersistentKeyPair(keyFileName);
    byte[] keyHash = DigestHelper.calculateSHAHashForRSAPublicKey(getPublicKey());
    securityID = DigestHelper.hashToSecurityID(keyHash);
    System.out.println("  Create secured device with securityID " + securityID);

    setupDeviceVariables();
    initDeviceContent();
    // run device
    runDevice();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#setupDeviceVariables()
   */
  public void setupDeviceVariables()
  {
    super.setupDeviceVariables();

    // discovery
    anonymousDiscovery = SecurityDefaults.ANONYMOUS_DISCOVERY;
    anonymousURLs = SecurityDefaults.ANONYMOUS_URLS;

    // description
    signedDescriptionRequests = SecurityDefaults.SIGNED_DESCRIPTION_REQUESTS;
    encryptedDescriptionResponses = SecurityDefaults.ENCRYPTED_DESCRIPTION_RESPONSES;

    // eventing
    signedEventSubscriptions = SecurityDefaults.SIGNED_EVENT_SUBSCRIPTIONS;
    signedEvents = SecurityDefaults.SIGNED_EVENTS;
    encryptedEvents = SecurityDefaults.ENCRYPTED_EVENTS;

    nonceList = new Vector();
    securityAwareControlPointObjectFromPublicKeyTable = new Hashtable();

    securedServiceList = new Vector();

    sessionList = new Vector();
    sessionCounter = 0;

    ownerList = new Vector();
    loadOwnerListFromFile(getWorkingDirectory() + OWNER_FILE);

    aclList = new Vector();
    loadACLListFromFile(getWorkingDirectory() + ACL_FILE);

    // convert user-readable UDN string to securityID to prevent leakage of device type information
    if (anonymousDiscovery)
    {
      UDN = "uuid:" + UUIDHelper.getUUIDFromName(UDN);
      // System.out.println("Anonymous UDN is " + UDN);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#initDeviceContent()
   */
  public void initDeviceContent()
  {
    super.initDeviceContent();

    // show this here to allow derived devices to
    // override these settings in setupDeviceVariables()
    System.out.println("  Secured device...");
    if (anonymousDiscovery)
    {
      System.out.println("    uses anonymous discovery");
    }

    if (anonymousURLs)
    {
      System.out.println("    uses anonymous URLs");
    }

    if (signedDescriptionRequests)
    {
      System.out.println("    needs signed description requests");
    }

    if (encryptedDescriptionResponses)
    {
      System.out.println("    encrypts description responses");
    }

    if (signedEventSubscriptions)
    {
      System.out.println("    needs signed event subscriptions");
    }

    if (encryptedEvents)
    {
      System.out.println("    encrypts events");
    } else
    {
      if (signedEvents)
      {
        System.out.println("    signs events");
      }
    }
    System.out.println();

    // add mandatory device security service
    deviceSecurityService =
      new DeviceSecurityService(this, (SecuredDeviceHTTPMessageProcessor)getHTTPMessageProcessor());
    addTemplateService(deviceSecurityService);
    buildDefinedPermissions();
    buildDefinedProfiles();
    buildOwnerXMLDescription();
    buildACLXMLDescription();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#runDevice()
   */
  public void runDevice()
  {
    super.runDevice();
    // check for initial ownership
    if (getOwnerCount() == 0)
    {
      secret = DigestHelper.generateSHA1HMACKey();
      byte[] encodedSecret = secret.getEncoded();
      // simplify key
      byte[] simpleSecret = new byte[10];
      System.arraycopy(encodedSecret, 0, simpleSecret, 0, simpleSecret.length);
      secret = DigestHelper.buildSHA1HMACKey(simpleSecret);

      System.out.println("  Device is unowned. SECRET is <" + StringHelper.byteArrayToBase32(secret.getEncoded()) + ">");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#mustRunDelayed()
   */
  public boolean forceRunDelayed()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.Device#getInstanceOfDeviceMessageProcessorFactory()
   */
  protected DeviceMessageProcessorFactory getInstanceOfDeviceMessageProcessorFactory()
  {
    return new SecuredDeviceMessageProcessorFactory();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.Device#getInstanceOfDeviceSupportFactory()
   */
  protected DeviceSupportFactory getInstanceOfDeviceSupportFactory()
  {
    return new SecuredDeviceSupportFactory();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.templates.TemplateDevice#addTemplateService(de.fhg.fokus.magic.upnp.templates.TemplateService)
   */
  public void addTemplateService(TemplateService service)
  {
    if (service instanceof SecuredTemplateService)
    {
      // add to list of secured services
      securedServiceList.add(service);

      // read permissions and profiles
      buildDefinedPermissions();
      buildDefinedProfiles();
    }
    // inherited
    super.addTemplateService(service);
  }

  /** Retrieves the number of embedded services */
  public int getSecuredServiceCount()
  {
    return securedServiceList.size();
  }

  /** Retrieves a specific secured service */
  public SecuredTemplateService getSecuredService(int index)
  {
    if (index >= 0 && index < getSecuredServiceCount())
    {
      return (SecuredTemplateService)securedServiceList.elementAt(index);
    }

    return null;
  }

  /** Retrieves a reference to the outer entity */
  public SecurityAwareTemplateEntity getSecurityAwareEntity()
  {
    return (SecurityAwareTemplateEntity)getTemplateEntity();
  }

  public DeviceSecurityService getDeviceSecurityService()
  {
    return deviceSecurityService;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Key handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Returns the hash of the public key of this device */
  public String getSecurityID()
  {
    return securityID;
  }

  /** Returns the public key of this device */
  public RSAPublicKey getPublicKey()
  {
    return (RSAPublicKey)rsaKeyPair.getPublic();
  }

  /** Returns the private key of this device */
  public RSAPrivateKey getPrivateKey()
  {
    return (RSAPrivateKey)rsaKeyPair.getPrivate();
  }

  /** Retrieves the current secret */
  public SecretKey getSecret()
  {
    return secret;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Owner handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the number of owners */
  public int getOwnerCount()
  {
    return ownerList.size();
  }

  /** Retrieves a specific owner */
  public SecurityAwareObject getOwner(int index)
  {
    if (index >= 0 && index < ownerList.size())
    {
      return (SecurityAwareObject)ownerList.elementAt(index);
    }

    return null;
  }

  /** Builds the string with all owners */
  public void buildOwnerXMLDescription()
  {
    ownerXMLDescription = ElementBuilder.buildOwnerXMLDescription(ownerList);
  }

  /** Retrieves a string with all owners */
  public String getOwnerXMLDescription()
  {
    return ownerXMLDescription;
  }

  /** Adds an owner for this device */
  public void addOwner(String algorithm, String keyHashBase64)
  {
    ownerList.add(new SecurityAwareObject(algorithm, keyHashBase64));
    buildOwnerXMLDescription();
    saveOwnerListToFile(getWorkingDirectory() + OWNER_FILE);
  }

  /** Removes an owner from this device */
  public void removeOwner(String algorithm, String keyHashBase64)
  {
    int i = 0;
    while (i < ownerList.size())
    {
      if (((SecurityAwareObject)ownerList.elementAt(i)).getPublicKeyHashBase64().equals(keyHashBase64) &&
        ((SecurityAwareObject)ownerList.elementAt(i)).getHashAlgorithm().equals(algorithm))
      {
        ownerList.remove(i);
        buildOwnerXMLDescription();
        saveOwnerListToFile(getWorkingDirectory() + OWNER_FILE);
      } else
      {
        i++;
      }
    }
  }

  public boolean isKnownOwner(String algorithm, String keyHashBase64)
  {
    for (int i = 0; i < ownerList.size(); i++)
    {
      if (((SecurityAwareObject)ownerList.elementAt(i)).getPublicKeyHashBase64().equals(keyHashBase64) &&
        ((SecurityAwareObject)ownerList.elementAt(i)).getHashAlgorithm().equals(algorithm))
      {
        return true;
      }
    }
    return false;
  }

  private void loadOwnerListFromFile(String fileName)
  {
    ownerList.clear();
    if (new File(fileName).exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            // get individual config items
            StringTokenizer tokens = new StringTokenizer(line, ":");
            try
            {
              String hashAlgorithm = tokens.nextToken();
              String publicKeyHash = tokens.nextToken();

              SecurityAwareObject owner = new SecurityAwareObject(hashAlgorithm, publicKeyHash);
              ownerList.add(owner);
            } catch (NoSuchElementException nsee)
            {
              System.out.println("Exception while loading owner list: " + nsee.getMessage());
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
      buildOwnerXMLDescription();
      System.out.println("  Loaded " + ownerList.size() + " owner(s)");
    }
  }

  private void saveOwnerListToFile(String fileName)
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

      for (int i = 0; i < getOwnerCount(); i++)
      {
        SecurityAwareObject owner = getOwner(i);
        writer.write(owner.getHashAlgorithm() + ":" + owner.getPublicKeyHashBase64() + "\n");
      }
      writer.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Session handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Adds a session to this service */
  public boolean addSession(Session session)
  {
    if (!isKnownSession(session))
    {
      sessionCounter++;
      session.setDeviceKeyID(sessionCounter);
      sessionList.add(session);
      return true;
    }
    System.out.println("ERROR: Session is already known");
    return false;
  }

  /** Retrieves the session for a certain keyID */
  public Session getSessionFromCPKeyID(int cpKeyID)
  {
    for (int i = 0; i < sessionList.size(); i++)
    {
      Session currentSession = (Session)sessionList.elementAt(i);
      if (currentSession.getCpKeyID() == cpKeyID)
      {
        return currentSession;
      }
    }

    return null;
  }

  /** Retrieves the session for a certain keyID */
  public Session getSessionFromDeviceKeyID(int deviceKeyID)
  {
    for (int i = 0; i < sessionList.size(); i++)
    {
      Session currentSession = (Session)sessionList.elementAt(i);
      if (currentSession.getDeviceKeyID() == deviceKeyID)
      {
        return currentSession;
      }
    }

    return null;
  }

  public Session removeSession(int deviceKeyID)
  {
    int i = 0;
    while (i < sessionList.size())
    {
      Session currentSession = (Session)sessionList.elementAt(i);
      if (currentSession.getDeviceKeyID() == deviceKeyID)
      {
        sessionList.remove(i);
        return currentSession;
      } else
      {
        i++;
      }
    }
    return null;
  }

  private boolean isKnownSession(Session session)
  {
    for (int i = 0; i < sessionList.size(); i++)
    {
      Session currentSession = (Session)sessionList.elementAt(i);
      if (currentSession.getDeviceKeyID() == session.getDeviceKeyID())
      {
        return true;
      }
    }

    return false;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Permission handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the permissions for this device */
  public String getDefinedPermissions()
  {
    return definedPermissions;
  }

  /** Retrieves the profiles for this device */
  public String getDefinedProfiles()
  {
    return definedProfiles;
  }

  /** Builds the list with defined permissions for all secured services */
  private void buildDefinedPermissions()
  {
    String result = "<DefinedPermissions xmlns:mfgr=\"" + getDeviceType() + ":Permissions" + "\">";

    for (int i = 0; i < getSecuredServiceCount(); i++)
    {
      SecuredTemplateService service = getSecuredService(i);
      for (int j = 0; j < service.getDefinedPermissionCount(); j++)
      {
        result += service.getDefinedPermission(j).toXMLDescription();
      }
    }
    result += "</DefinedPermissions>";

    definedPermissions = result;
  }

  /** Builds the list with defined profiles for all secured services */
  private void buildDefinedProfiles()
  {
    String result = "<Profiles xmlns:mfgr=\"" + getDeviceType() + ":Profiles" + "\">";

    for (int i = 0; i < getSecuredServiceCount(); i++)
    {
      SecuredTemplateService service = getSecuredService(i);
      for (int j = 0; j < service.getDefinedProfileCount(); j++)
      {
        result += service.getDefinedProfile(j).toXMLDescription();
      }
    }
    result += "</Profiles>";

    definedProfiles = result;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ACL handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /** Retrieves the current ACL version */
  public String getACLVersion()
  {
    return aclListVersion;
  }

  /** Retrieves the number of ACL entries */
  public int getACLCount()
  {
    return aclList.size();
  }

  /** Retrieves a specific ACL entry */
  public ACLEntry getACLEntry(int index)
  {
    if (index >= 0 && index < aclList.size())
    {
      return (ACLEntry)aclList.elementAt(index);
    }

    return null;
  }

  /** Adds an ACL entry for this device */
  public void addACLEntry(ACLEntry entry)
  {
    aclList.add(entry);
    buildACLXMLDescription();
    saveACLListToFile(getWorkingDirectory() + ACL_FILE);
  }

  /** Removes an owner from this device */
  public void removeACLEntry(int index)
  {
    if (index >= 0 && index < getACLCount())
    {
      aclList.remove(index);
      buildACLXMLDescription();
      saveACLListToFile(getWorkingDirectory() + ACL_FILE);
    }
  }

  /** Builds the string with all acl entries */
  public void buildACLXMLDescription()
  {
    aclXMLDescription = ElementBuilder.buildACLXMLDescription(aclList);
    aclListVersion = SecurityHelper.createSequenceBaseString().toString();
  }

  /** Retrieves a string with all acl entries */
  public String getACLXMLDescription()
  {
    return aclXMLDescription;
  }

  private void loadACLListFromFile(String fileName)
  {
    aclList.clear();
    File file = new File(fileName);
    if (file.exists())
    {
      try
      {
        ACLParser parser = new ACLParser();
        parser.parse(file);
        aclList = parser.getACLList();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
      buildACLXMLDescription();
      System.out.println("  Loaded " + ownerList.size() + " ACL entries");
    }
  }

  private void saveACLListToFile(String fileName)
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

      writer.write(aclXMLDescription);
      writer.close();
    } catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  /**
   * Checks if all event subscriptions to this device must be signed.
   * 
   * @return True if event subscriptions should be signed, false otherwise
   */
  public boolean needsSignedEventSubscriptions()
  {
    return signedEventSubscriptions;
  }

  /**
   * Sets the signedEventSubscriptions.
   * 
   * @param signedEventSubscriptions
   *          The new value for signedEventSubscriptions
   */
  public void setSignedEventSubscriptions(boolean signedEventSubscriptions)
  {
    this.signedEventSubscriptions = signedEventSubscriptions;
  }

  /**
   * Checks if all events created by this device must be signed.
   * 
   * @return True if events should be signed, false otherwise
   */
  public boolean needsSignedEvents()
  {
    return signedEvents;
  }

  /**
   * Sets the signedEvents flag.
   * 
   * @param signedEvents
   *          The new value for signedEvents
   */
  public void setSignedEvents(boolean signedEvents)
  {
    this.signedEvents = signedEvents;
  }

  /**
   * Checks if all events created by this device must be encrypted.
   * 
   * @return True if events should be encrypted, false otherwise
   */
  public boolean needsEncryptedEvents()
  {
    return encryptedEvents;
  }

  /**
   * Sets the encryptedEvents.
   * 
   * @param encryptedEvents
   *          The new value for encryptedEvents
   */
  public void setEncryptedEvents(boolean encryptedEvents)
  {
    this.encryptedEvents = encryptedEvents;
  }

  /**
   * Checks if all description requests to this device must be signed.
   * 
   * @return True if description requests should be signed, false otherwise
   */
  public boolean needsSignedDescriptionRequests()
  {
    return signedDescriptionRequests;
  }

  /**
   * Sets the signedDescriptions flag.
   * 
   * @param signedDescriptions
   *          The new value for signedDescriptions
   */
  public void setSignedDescriptionRequests(boolean signedDescriptions)
  {
    this.signedDescriptionRequests = signedDescriptions;
  }

  /**
   * Retrieves the encryptedDescriptionResponses.
   * 
   * @return The encryptedDescriptionResponses
   */
  public boolean needsEncryptedDescriptionResponses()
  {
    return encryptedDescriptionResponses;
  }

  /**
   * Sets the encryptedDescriptionResponses.
   * 
   * @param encryptedDescriptionResponses
   *          The new value for encryptedDescriptionResponses
   */
  public void setEncryptedDescriptionResponses(boolean encryptedDescriptionResponses)
  {
    this.encryptedDescriptionResponses = encryptedDescriptionResponses;
  }

  /**
   * Retrieves the anonymousDiscovery.
   * 
   * @return The anonymousDiscovery
   */
  public boolean isAnonymousDiscovery()
  {
    return anonymousDiscovery;
  }

  /**
   * Sets the anonymousDiscovery.
   * 
   * @param anonymousDiscovery
   *          The new value for anonymousDiscovery
   */
  public void setAnonymousDiscovery(boolean anonymousDiscovery)
  {
    this.anonymousDiscovery = anonymousDiscovery;
  }

  /**
   * Retrieves the anonymousURLs.
   * 
   * @return The anonymousURLs
   */
  public boolean needsAnonymousURLs()
  {
    return anonymousURLs;
  }

  /**
   * Sets the anonymousURLs.
   * 
   * @param anonymousURLs
   *          The new value for anonymousURLs
   */
  public void setAnonymousURLs(boolean anonymousURLs)
  {
    this.anonymousURLs = anonymousURLs;
  }

  /** Creates a new nonce for event subscription */
  public String createAndStoreNonce()
  {
    String nonce = SecurityHelper.createSequenceBaseString();
    nonceList.add(nonce);

    return nonce;
  }

  /** Checks and removes a nonce for event subscription */
  public boolean checkAndRemoveNonce(String nonce)
  {
    // returns true if the nonce was valid
    return nonceList.remove(nonce);
  }

  /**
   * Retrieves the securityAwareControlPointObjectFromPublicKeyTable.
   * 
   * @return The securityAwareControlPointObjectFromPublicKeyTable
   */
  public Hashtable getSecurityAwareControlPointObjectFromPublicKeyTable()
  {
    return securityAwareControlPointObjectFromPublicKeyTable;
  }

  /** Tries to find a new unique name for a control point object. */
  public String createKeyName()
  {
    // create new name
    int value = (int)(Math.random() * 100000);
    String result = value + "";
    while (getControlPointObjectByKeyName(result) != null)
    {
      // name already exists, create new name
      value = (int)(Math.random() * 100000);
      result = value + "";
    }
    return result;
  }

  /** Tries to find a control point object by its key name. */
  public SecurityAwareControlPointObject getControlPointObjectByKeyName(String keyName)
  {
    Enumeration elements = securityAwareControlPointObjectFromPublicKeyTable.elements();
    while (elements.hasMoreElements())
    {
      SecurityAwareControlPointObject currentObject = (SecurityAwareControlPointObject)elements.nextElement();
      if (currentObject.getSymmetricKeyName().equals(keyName))
      {
        return currentObject;
      }
    }
    return null;
  }

  /** Tries to find a control point object by its event subscription ID. */
  public SecurityAwareControlPointObject getControlPointObjectBySID(String sid)
  {
    Enumeration elements = securityAwareControlPointObjectFromPublicKeyTable.elements();
    while (elements.hasMoreElements())
    {
      SecurityAwareControlPointObject currentObject = (SecurityAwareControlPointObject)elements.nextElement();
      if (currentObject.isKnownSID(sid))
      {
        return currentObject;
      }
    }
    return null;
  }

  /** Tries to find a control point object by its public key. */
  public SecurityAwareControlPointObject getControlPointObject(RSAPublicKey publicKey)
  {
    return (SecurityAwareControlPointObject)securityAwareControlPointObjectFromPublicKeyTable.get(publicKey);
  }

  /**
   * Retrieves the keyCreationLock.
   * 
   * @return The keyCreationLock
   */
  public Object getKeyCreationLock()
  {
    return keyCreationLock;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.Device#getRelativeServiceURL(de.fhg.fokus.magic.upnp.device.DeviceService,
   *      java.lang.String)
   */
  public String getRelativeServiceURL(DeviceService service, String suffix)
  {
    String result = super.getRelativeServiceURL(service, suffix);
    if (anonymousURLs)
    {
      return "/" + DigestHelper.hashToSecurityID(DigestHelper.calculateSHAHashForString(result));
    }
    return result;
  }

}

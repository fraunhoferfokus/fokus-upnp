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

import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.core.device.Action;
import de.fraunhofer.fokus.upnp.core.device.DeviceSendEventNotifyThread;
import de.fraunhofer.fokus.upnp.core.device.DeviceSubscribedControlPointHandler;
import de.fraunhofer.fokus.upnp.core.templates.TemplateService;
import de.fraunhofer.fokus.upnp.core_security.InvokeSecuredActionListener;
import de.fraunhofer.fokus.upnp.core_security.device.SecuredDeviceSendEventNotifyThread;
import de.fraunhofer.fokus.upnp.core_security.helpers.ACLEntry;
import de.fraunhofer.fokus.upnp.core_security.helpers.ActionSecurityInfo;
import de.fraunhofer.fokus.upnp.core_security.helpers.Permission;
import de.fraunhofer.fokus.upnp.core_security.helpers.Profile;
import de.fraunhofer.fokus.upnp.core_security.helpers.Session;
import de.fraunhofer.fokus.upnp.util.CommonConstants;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;

/**
 * This is the base class for secured services. It provides methods to add permission and profile
 * entries for certain actions and limits action requests to authorized control points.
 * 
 * @author Alexander Koenig
 */
public class SecuredTemplateService extends TemplateService implements InvokeSecuredActionListener
{

  // List of service permissions
  private Vector    permissionList;

  // List of optional profiles
  private Vector    profileList;

  // List of actions that can be called without signing
  private Vector    unsignedActionList;

  // List of actions that must be signed but need no permission
  private Vector    permittedActionList;

  // Flag that event subscriptions must be signed for this service
  private boolean   signedEventSubscriptions;

  /** Hashtable to retrieve httpParser for action invocator */
  private Hashtable securityInfoFromThreadTable = new Hashtable();

  /** Creates a new instance of SecuredService */
  public SecuredTemplateService(SecuredTemplateDevice device, String serviceType, String serviceId)
  {
    super(device, device.getWorkingDirectory(), serviceType, serviceId, true);
  }

  public void setupServiceVariables()
  {
    super.setupServiceVariables();
    permissionList = new Vector();
    profileList = new Vector();
    unsignedActionList = new Vector();
    permittedActionList = new Vector();
    signedEventSubscriptions = false;
  }

  /** Retrieves the associated secured device */
  public SecuredTemplateDevice getSecuredDevice()
  {
    return (SecuredTemplateDevice)getTemplateDevice();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Action handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * This method is called for each action request on a security aware device.
   * 
   * @param serviceID
   *          The ID of the service the action belongs to
   * @param action
   *          The invoked action
   * @param securityInfo
   *          Security info associated with this action
   * 
   * @return True if the action was successful, false otherwise
   */
  public boolean invokeSecuredLocalAction(String serviceID, Action action, ActionSecurityInfo securityInfo)
  {
    // check, if action can be used without signing
    if (isUnsignedAction(action))
    {
      // forward to conventional action handling
      return invokeLocalAction(serviceID, action);
    }

    // action must be signed

    // check if signature is available
    if (securityInfo == null)
    {
      action.setError(712, "Signature Missing");
      return false;
    }
    // check if caller is authorized to perform this action
    boolean actionAuthorized = false;
    // retrieve public key hash of caller
    String publicKeyHashBase64 = "";
    if (securityInfo.getPublicKey() != null)
    {
      publicKeyHashBase64 = DigestHelper.calculateBase64SHAHashForRSAPublicKey(securityInfo.getPublicKey());
    } else
    {
      Session session = getSecuredDevice().getSessionFromDeviceKeyID(securityInfo.getSessionID());
      publicKeyHashBase64 = DigestHelper.calculateBase64SHAHashForRSAPublicKey(session.getControlPointPublicKey());
    }

    // pass authorization if action is permitted to all users
    if (!actionAuthorized)
    {
      actionAuthorized = isPermittedAction(action);
      if (actionAuthorized)
      {
        System.out.println("Action permitted for all users");
      }
    }

    // owners can do everything
    if (!actionAuthorized)
    {
      actionAuthorized = getSecuredDevice().isKnownOwner(CommonConstants.SHA_1_UPNP, publicKeyHashBase64);
      if (actionAuthorized)
      {
        System.out.println("Owner invoked action");
      }
    }

    // check ACL
    if (!actionAuthorized)
    {
      for (int i = 0; !actionAuthorized && i < getSecuredDevice().getACLCount(); i++)
      {
        ACLEntry entry = getSecuredDevice().getACLEntry(i);
        // if subject is equal to caller
        if (entry.getSubject().getPublicKeyHashBase64().equals(publicKeyHashBase64))
        {
          // check if action is associated with the found permission
          actionAuthorized = isAssociatedAction(entry.getAccessPermission(), action);
        }
      }
      if (actionAuthorized)
      {
        System.out.println("ACL entry invoked action");
      }

    }
    if (!actionAuthorized)
    {
      action.setError(701, "Not authorized");
      return false;
    }

    securityInfoFromThreadTable.put(Thread.currentThread(), securityInfo);

    boolean result = super.invokeLocalAction(serviceID, action);

    securityInfoFromThreadTable.remove(Thread.currentThread());

    return result;
  }

  /** Updates the list with actions that need signing but no permission */
  public void setPermittedActionTable(Action[] actions)
  {
    permittedActionList.clear();
    for (int i = 0; i < actions.length; i++)
    {
      permittedActionList.add(actions[i]);
    }
  }

  /** Checks if a specific action can be called without permission */
  private boolean isPermittedAction(Action action)
  {
    for (int i = 0; i < permittedActionList.size(); i++)
    {
      if (((Action)permittedActionList.elementAt(i)).getName().equals(action.getName()))
      {
        return true;
      }
    }
    return false;
  }

  /** Updates the list with actions that need not to be signed */
  public void setUnsignedActionTable(Action[] actions)
  {
    unsignedActionList.clear();
    for (int i = 0; i < actions.length; i++)
    {
      unsignedActionList.add(actions[i]);
    }
  }

  /** Checks if a specific action can be called without signing */
  private boolean isUnsignedAction(Action action)
  {
    for (int i = 0; i < unsignedActionList.size(); i++)
    {
      if (((Action)unsignedActionList.elementAt(i)).getName().equals(action.getName()))
      {
        return true;
      }
    }
    return false;
  }

  /** Returns the security info for the currently invoked action */
  protected ActionSecurityInfo getSecurityInfoForCurrentAction()
  {
    return (ActionSecurityInfo)securityInfoFromThreadTable.get(Thread.currentThread());
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Permission handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Adds a permission entry for this service.
   * 
   * @param permission
   *          The permission entry that should be added
   * @param actionList
   *          The list of actions which can be executed with this permission
   * 
   */
  public void addPermissionEntry(Permission permission, Action[] actionList)
  {
    if (!isKnownPermission(permission))
    {
      permissionList.add(new PermissionActionAssociation(permission, actionList));
    }
  }

  /** Retrieves the number of permissions for this service */
  public int getDefinedPermissionCount()
  {
    return permissionList.size();
  }

  /** Retrieves a specific permission */
  public Permission getDefinedPermission(int index)
  {
    if (index >= 0 && index < getDefinedPermissionCount())
    {
      return ((PermissionActionAssociation)permissionList.elementAt(index)).permission;
    }

    return null;
  }

  private boolean isKnownPermission(Permission permission)
  {
    for (int i = 0; i < permissionList.size(); i++)
    {
      PermissionActionAssociation association = (PermissionActionAssociation)permissionList.elementAt(i);
      if (association.permission.equals(permission))
      {
        return true;
      }
    }
    return false;
  }

  private boolean isAssociatedAction(String accessPermission, Action action)
  {
    for (int i = 0; i < permissionList.size(); i++)
    {
      PermissionActionAssociation association = (PermissionActionAssociation)permissionList.elementAt(i);
      if (association.permission.getACLEntry().equals(accessPermission))
      {
        for (int j = 0; j < association.actionList.size(); j++)
        {
          if (((Action)association.actionList.elementAt(j)).getName().equals(action.getName()))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Profile handling //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Adds a profile entry for this service.
   * 
   * @param profile
   *          The profile that should be added
   */
  public void addProfileEntry(Profile profile)
  {
    if (!isKnownProfile(profile))
    {
      profileList.add(profile);
    }
  }

  /** Retrieves the number of profiles for this service */
  public int getDefinedProfileCount()
  {
    return profileList.size();
  }

  /** Retrieves a specific permissions */
  public Profile getDefinedProfile(int index)
  {
    if (index >= 0 && index < getDefinedProfileCount())
    {
      return (Profile)profileList.elementAt(index);
    }

    return null;
  }

  private boolean isKnownProfile(Profile profile)
  {
    for (int i = 0; i < profileList.size(); i++)
    {
      Profile currentProfile = (Profile)profileList.elementAt(i);
      if (currentProfile.equals(profile))
      {
        return true;
      }
    }
    return false;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private class //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  /** Connects a defined permission (e.g. mfgr:p1) with actions (e.g. doSomething()) */
  private class PermissionActionAssociation
  {
    public Permission permission;

    public Vector     actionList = new Vector();

    public PermissionActionAssociation(Permission permission, Action[] actionList)
    {
      this.permission = permission;
      for (int i = 0; i < actionList.length; i++)
      {
        this.actionList.add(actionList[i]);
      }
    }
  }

  /**
   * Retrieves the signedEventSubscriptions.
   * 
   * @return The signedEventSubscriptions
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

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.device.DeviceService#createDeviceSendEventNotifyThread(de.fhg.fokus.magic.upnp.device.DeviceSubscribedControlPointThread)
   */
  protected DeviceSendEventNotifyThread createDeviceSendEventNotifyThread(DeviceSubscribedControlPointHandler deviceSubscribedControlPointHandler)
  {
    // create new send thread that can create signed event messages
    return new SecuredDeviceSendEventNotifyThread(deviceSubscribedControlPointHandler);
  }

}

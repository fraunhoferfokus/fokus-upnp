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
package de.fraunhofer.fokus.upnp.core.device;

import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CollectionHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This thread sleeps for an specific amount of time to allow the service to collect some changed
 * variables, therefore a bundled messages can be send to reduce network traffic. This class also
 * handles moderated state variables.
 * 
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class DeviceServiceEventCollector implements IEventListener
{

  private static int    REMOVE_ALL      = 1;

  private static int    REMOVE_SPECIFIC = 2;

  private Vector        moderatedVariableEntryList;

  private Vector        eventObjects;

  private Object        variableLock;

  /** Associated service */
  private DeviceService service;

  private long          lastEventSendTime;

  private long          eventInterval;

  /**
   * Creates DeviceServiceEventCollector object.
   * 
   * @param service
   *          service for finding the correct service this thread belongs to
   */
  public DeviceServiceEventCollector(DeviceService service)
  {
    // super("DeviceServiceEventCollector [" + service.toString() + "]");
    this.service = service;
    moderatedVariableEntryList = new Vector();
    eventObjects = new Vector();
    variableLock = new Object();

    lastEventSendTime = System.currentTimeMillis();
    eventInterval = service.getThreadCollectionTime();

    service.getDevice().getDeviceEventThread().register(this);
  }

  /**
   * Adds a new state variable value and removes it from the moderated list if necessary.
   * 
   * @param stateVariable
   *          The state variable
   * @param publicKey
   *          Optional public key for personalized events or null
   * 
   */
  public void addVariable(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    synchronized(variableLock)
    {
      // System.out.println("Add: " + stateVariable.getName() + ":" + stateVariable.getValue());
      // we must always add the new value to prevent value skipping
      eventObjects.add(new StateVariableEventObject(stateVariable.getName(),
        stateVariable.getPersonalizedValueAsString(publicKey),
        publicKey));

      // remove just this entry from moderated variables
      removeModeratedStateVariables(stateVariable, publicKey, REMOVE_SPECIFIC);
    }
  }

  /**
   * Adds a new moderated variable
   * 
   * @param stateVariable
   *          The new variable
   * @param publicKey
   *          Optional public key for personalized events or null
   * 
   */
  public void addModeratedVariable(StateVariable stateVariable, RSAPublicKey publicKey)
  {
    synchronized(variableLock)
    {
      // Moderated state variable is not personalized
      // Use-case 1: Personalized events for this state variable are already included:
      // Remove all personalized entries because the common value
      // overrides the personalized values
      // Add a new common entry

      // Moderated state variable is personalized
      // Use-case 2: The state variable is already in the table and a new moderated
      // personalized value is added:
      // Remove existing personalized entries for that state variable AND key
      // Keep unpersonalized entries for that state variable
      // Add a new personalized entry
      removeModeratedStateVariables(stateVariable, publicKey, REMOVE_ALL);
      moderatedVariableEntryList.add(new ModeratedStateVariableEntry(stateVariable, publicKey));
    }
  }

  /**
   * Removes a moderated state variable from the entry list.
   * 
   * 
   * @param stateVariable
   * @param publicKey
   *          Optional public key for personalized events or null
   * @param removalType
   */
  private void removeModeratedStateVariables(StateVariable stateVariable, RSAPublicKey publicKey, int removalType)
  {
    int i = 0;
    while (i < moderatedVariableEntryList.size())
    {
      ModeratedStateVariableEntry currentEntry = (ModeratedStateVariableEntry)moderatedVariableEntryList.elementAt(i);
      boolean removeEntry = false;
      // remove all state variable entries if not personalized
      if (currentEntry.stateVariable == stateVariable && publicKey == null &&
        (removalType == REMOVE_ALL || currentEntry.publicKey == null))
      {
        removeEntry = true;
      }
      // remove if same personalized key
      if (currentEntry.stateVariable == stateVariable && publicKey != null && currentEntry.publicKey != null &&
        publicKey.equals(currentEntry.publicKey))
      {
        removeEntry = true;
      }
      if (removeEntry)
      {
        moderatedVariableEntryList.remove(i);
      } else
      {
        i++;
      }
    }
  }

  /** This method is called regularly to send outstanding moderated events to all subscribers */
  private void processModeratedStateVariables()
  {
    synchronized(variableLock)
    {
      Enumeration variableEntries = CollectionHelper.getPersistentEntryEnumeration(moderatedVariableEntryList);
      while (variableEntries.hasMoreElements())
      {
        ModeratedStateVariableEntry currentEntry = (ModeratedStateVariableEntry)variableEntries.nextElement();
        if (currentEntry.stateVariable.canSendEvent())
        {
          addVariable(currentEntry.stateVariable, currentEntry.publicKey);
        }
      }
    }
  }

  /**
   * This method is called after each collection time to forward outstanding events to the device
   * service.
   */
  private void forwardEventObjects()
  {
    synchronized(variableLock)
    {
      if (eventObjects.size() > 0)
      {
        service.distributeEventObjects(eventObjects);
        eventObjects.clear();
      }
    }
  }

  /** Terminates the event collector */
  public void terminate()
  {
    service.getDevice().getDeviceEventThread().unregister(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (System.currentTimeMillis() - lastEventSendTime > eventInterval)
    {
      forwardEventObjects();
      lastEventSendTime = System.currentTimeMillis();
      eventInterval = service.getThreadCollectionTime();
    }
    // process moderated state variables
    processModeratedStateVariables();
  }

  /**
   * Private class to bundle a state variable with a public key.
   * 
   * @author Alexander Koenig
   * 
   */
  private class ModeratedStateVariableEntry
  {

    public StateVariable stateVariable;

    public RSAPublicKey  publicKey;

    /**
     * Creates a new instance of ModeratedStateVariableEntry.
     * 
     * @param stateVariable
     * @param publicKey
     */
    public ModeratedStateVariableEntry(StateVariable stateVariable, RSAPublicKey publicKey)
    {
      this.stateVariable = stateVariable;
      this.publicKey = publicKey;
    }

  }
}

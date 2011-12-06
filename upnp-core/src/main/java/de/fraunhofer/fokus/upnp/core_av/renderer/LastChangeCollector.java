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
package de.fraunhofer.fokus.upnp.core_av.renderer;

import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.device.StateVariable;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class helps to track changes for services that use instanceIDs.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class LastChangeCollector implements IEventListener
{

  private Hashtable     instanceHashtable = new Hashtable();

  /** Reference to associated last change state variable */
  private StateVariable lastChange;

  private Object        lock              = new Object();

  private long          nextCheckTime     = 0;

  private long          sleepTime;

  /**
   * Creates a new instance of LastChangeCollector.
   * 
   * @param lastChange
   *          Associated state variable
   */
  public LastChangeCollector(StateVariable lastChange, long sleepTime)
  {
    this.lastChange = lastChange;
    this.sleepTime = sleepTime;
  }

  /** Adds a changed state variable to the last change collector. */
  public void addChange(long instanceID, String name, String value)
  {
    synchronized(lock)
    {
      Long instanceIDObject = new Long(instanceID);
      if (!instanceHashtable.containsKey(instanceIDObject))
      {
        Hashtable valueHashtable = new Hashtable();
        instanceHashtable.put(instanceIDObject, valueHashtable);
      }
      Hashtable valueHashtable = (Hashtable)instanceHashtable.get(instanceIDObject);
      // this effectively replaces previously stored values with the same name
      valueHashtable.put(name, value);
    }
  }

  /** Adds a changed state variable to the last change collector. */
  public void addChange(long instanceID, StateVariable stateVariable)
  {
    synchronized(lock)
    {
      Long instanceIDObject = new Long(instanceID);
      if (!instanceHashtable.containsKey(instanceIDObject))
      {
        Hashtable valueHashtable = new Hashtable();
        instanceHashtable.put(instanceIDObject, valueHashtable);
      }
      Hashtable valueHashtable = (Hashtable)instanceHashtable.get(instanceIDObject);
      // this effectively replaces previously stored values with the same name
      valueHashtable.put(stateVariable.getName(), stateVariable.getValueAsString());
    }
  }

  /** Builds the last change variable */
  private String buildLastChange()
  {
    synchronized(lock)
    {
      if (instanceHashtable.size() == 0)
      {
        return "";
      }

      String lastChangeValue = UPnPAVConstant.EVENT_START;

      Enumeration instanceIDList = instanceHashtable.keys();
      // enumerate all instance IDs
      while (instanceIDList.hasMoreElements())
      {
        Long instanceIDObject = (Long)instanceIDList.nextElement();

        lastChangeValue += "<" + UPnPAVConstant.ARG_INSTANCE_ID + " val=\"" + instanceIDObject.longValue() + "\">";

        Hashtable valueHashtable = (Hashtable)instanceHashtable.get(instanceIDObject);
        Enumeration valueList = valueHashtable.keys();
        // enumerate all values for one instance ID
        while (valueList.hasMoreElements())
        {
          String name = (String)valueList.nextElement();
          String value = (String)valueHashtable.get(name);

          // add to last change variable
          lastChangeValue += "<" + name + " val=\"" + StringHelper.xmlToEscapedString(value) + "\"/>";
        }
        lastChangeValue += "</" + UPnPAVConstant.ARG_INSTANCE_ID + ">";
      }
      lastChangeValue += UPnPAVConstant.EVENT_END;

      // clear all changes
      instanceHashtable.clear();

      // System.out.println("Return value is: " + lastChangeValue);

      return lastChangeValue;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IEventListener#triggerEvents()
   */
  public void triggerEvents()
  {
    if (System.currentTimeMillis() > nextCheckTime)
    {
      nextCheckTime = System.currentTimeMillis() + sleepTime;
      String lastChangeValue = buildLastChange();
      if (lastChangeValue.length() > 0)
      {
        try
        {
          lastChange.setValue(StringHelper.xmlToEscapedString(lastChangeValue));
        } catch (Exception e)
        {
        }
      }
    }
  }

}

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
package de.fraunhofer.fokus.upnp.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This class provides methods for enumerations and collections.
 * 
 * @author Alexander Koenig
 * 
 */
public class CollectionHelper
{

  /** Adds an object to a list if not already contained. */
  public static void tryAdd(Vector vector, Object object)
  {
    if (object == null)
    {
      return;
    }
    if (!vector.contains(object))
    {
      vector.add(object);
    }
  }

  /** Adds an object to the start of a list if not already contained. */
  public static void tryInsert(Vector vector, Object object)
  {
    if (object == null)
    {
      return;
    }
    if (!vector.contains(object))
    {
      vector.insertElementAt(object, 0);
    }
  }

  /**
   * Returns an enumeration of the hashtable elements that is not changed by parallel changes to the hashtable.
   * 
   * @param hashtable
   * @return
   */
  public static Enumeration getPersistentElementsEnumeration(Hashtable hashtable)
  {
    return ((Hashtable)hashtable.clone()).elements();
  }

  /**
   * Returns an enumeration of the hashtable keys that is not changed by parallel changes to the hashtable.
   * 
   * @param hashtable
   * @return
   */
  public static Enumeration getPersistentKeysEnumeration(Hashtable hashtable)
  {
    return ((Hashtable)hashtable.clone()).keys();
  }

  /**
   * Returns an iterator for the hash map keys that is not changed by parallel changes to the hash map.
   * 
   * @param hashMap
   * @return
   */
  public static Iterator getPersistentKeysIterator(HashMap hashMap)
  {
    return ((HashMap)hashMap.clone()).keySet().iterator();
  }

  /**
   * Returns a list with all hash map entries.
   * 
   * @param hashtable
   * @return
   */
  public static KeyValueVector getPersistentElementList(Hashtable hashtable)
  {
    KeyValueVector result = new KeyValueVector();
    synchronized(hashtable)
    {
      Iterator iterator = hashtable.keySet().iterator();
      while (iterator.hasNext())
      {
        Object currentKey = iterator.next();
        result.add(currentKey, hashtable.get(currentKey));
      }
    }
    return result;
  }

  /**
   * Returns an enumeration of the vector list that is not changed by parallel changes to the vector.
   * 
   * @param
   * @return
   */
  public static Enumeration getPersistentEntryEnumeration(Vector vector)
  {
    return ((Vector)vector.clone()).elements();
  }

  /** Returns an alphabetically sorted list of the keys. */
  public static Vector getSortedKeyList(Hashtable hashtable)
  {
    Vector result = new Vector();
    Enumeration keys = hashtable.keys();
    while (keys.hasMoreElements())
    {
      result.add(keys.nextElement());
    }
    Collections.sort(result, new Comparator()
    {
      public int compare(Object a, Object b)
      {
        String textA = a.toString();
        String textB = b.toString();

        return textA.compareToIgnoreCase(textB);
      }
    });

    return result;
  }

  /** Returns an alphabetically sorted copy of vector. */
  public static void sortListAlphabetically(List list)
  {
    Collections.sort(list, new Comparator()
    {
      public int compare(Object a, Object b)
      {
        String textA = a.toString();
        String textB = b.toString();

        return textA.compareToIgnoreCase(textB);
      }
    });
  }

}

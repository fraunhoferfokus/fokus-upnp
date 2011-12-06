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

import java.util.Vector;

/**
 * A class that provides indexed access to object pairs (key, value).
 * 
 * @author Alexander Koenig
 * 
 */
public class KeyValueVector
{

  /** List with entries */
  private Vector pairs = new Vector();

  public boolean add(Object key, Object value)
  {
    return pairs.add(new ObjectPair(key, value));
  }

  public void add(int index, Object key, Object value)
  {
    pairs.add(index, new ObjectPair(key, value));
  }

  public void clear()
  {
    pairs.clear();
  }

  public Object getKey(int index)
  {
    return ((ObjectPair)pairs.get(index)).key;
  }

  public Object getValue(int index)
  {
    return ((ObjectPair)pairs.get(index)).value;
  }

  public int indexOf(Object key)
  {
    for (int i = 0; i < pairs.size(); i++)
    {
      if (((ObjectPair)pairs.elementAt(i)).key.equals(key))
      {
        return i;
      }
    }
    return -1;
  }

  public boolean isEmpty()
  {
    return pairs.isEmpty();
  }

  public Object remove(int index)
  {
    return pairs.remove(index);
  }

  public boolean remove(Object key)
  {
    for (int i = 0; i < pairs.size(); i++)
    {
      if (((ObjectPair)pairs.elementAt(i)).key.equals(key))
      {
        pairs.remove(i);
        return true;
      }
    }
    return false;
  }

  public int size()
  {
    return pairs.size();
  }

  private class ObjectPair
  {

    public Object key;

    public Object value;

    /**
     * Creates a new instance of ObjectPair.
     * 
     * @param key
     * @param value
     */
    public ObjectPair(Object key, Object value)
    {
      this.key = key;
      this.value = value;
    }
  }
}

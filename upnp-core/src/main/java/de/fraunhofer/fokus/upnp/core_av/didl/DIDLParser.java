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
package de.fraunhofer.fokus.upnp.core_av.didl;

import java.util.Vector;

/**
 * This class is used to parse DIDL-Lite fragments.
 * 
 * @author Alexander Koenig
 */
public class DIDLParser
{

  private DIDLParserHandler parserClass;

  /** Creates a new instance of DIDLParser */
  public DIDLParser(String parseText)
  {
    try
    {
      parserClass = new DIDLParserHandler();
      parserClass.parse(parseText);
    } catch (Exception ex)
    {
      System.err.println("Error:" + ex.getMessage());
    }
  }

  /** Converts a vector to an array */
  private DIDLObject[] vectorToArray(Vector vector)
  {
    DIDLObject[] result = new DIDLObject[vector.size()];
    for (int i = 0; i < vector.size(); i++)
    {
      result[i] = (DIDLObject)vector.elementAt(i);
    }

    return result;
  }

  /** Converts a vector to an array */
  private DIDLItem[] vectorToItemArray(Vector vector)
  {
    DIDLItem[] result = new DIDLItem[vector.size()];
    for (int i = 0; i < vector.size(); i++)
    {
      result[i] = (DIDLItem)vector.elementAt(i);
    }

    return result;
  }

  /** Converts a vector to an array */
  private DIDLContainer[] vectorToContainerArray(Vector vector)
  {
    DIDLContainer[] result = new DIDLContainer[vector.size()];
    for (int i = 0; i < vector.size(); i++)
    {
      result[i] = (DIDLContainer)vector.elementAt(i);
    }

    return result;
  }

  /** Retrieves an array with all parsed objects */
  public DIDLObject[] getDIDLObjects()
  {
    return vectorToArray(parserClass.getObjectList());
  }

  /** Retrieves an array with parsed items */
  public DIDLItem[] getDIDLItems()
  {
    return vectorToItemArray(parserClass.getItemList());
  }

  /** Retrieves an array with parsed containers */
  public DIDLContainer[] getDIDLContainers()
  {
    return vectorToContainerArray(parserClass.getContainerList());
  }

  /** Retrieves the first parsed object */
  public DIDLObject getDIDLObject()
  {
    return parserClass.getFirstDIDLObject();
  }

  /** Retrieves the first parsed container or null */
  public DIDLContainer getDIDLContainer()
  {
    if (getDIDLObject() != null && getDIDLObject() instanceof DIDLContainer)
    {
      return (DIDLContainer)getDIDLObject();
    }

    return null;
  }

  /** Retrieves the first parsed item or null */
  public DIDLItem getDIDLItem()
  {
    if (getDIDLObject() != null && getDIDLObject() instanceof DIDLItem)
    {
      return (DIDLItem)getDIDLObject();
    }

    return null;
  }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class can be used to serialize Java objects.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SerializationHelper
{

  /** Builds the byte array representation for a Java object */
  public static byte[] objectToBinary(Object object)
  {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream;
    try
    {
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(object);

      byte[] result = byteArrayOutputStream.toByteArray();

      objectOutputStream.close();

      return result;
    } catch (Exception e)
    {
      System.out.println("Error serializing object: " + e.getMessage());
    }
    return null;
  }

  /** Parses the byte array representation of a Java object */
  public static Object objectFromBinary(byte[] serializedObject)
  {
    if (serializedObject == null)
    {
      return null;
    }
    try
    {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObject);
      ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
      Object result = objectInputStream.readObject();
      objectInputStream.close();

      return result;
    } catch (Exception e)
    {
      System.out.println("Error parsing object: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /** Clones a Java object */
  public static Object clone(Object object)
  {
    return objectFromBinary(objectToBinary(object));
  }

}

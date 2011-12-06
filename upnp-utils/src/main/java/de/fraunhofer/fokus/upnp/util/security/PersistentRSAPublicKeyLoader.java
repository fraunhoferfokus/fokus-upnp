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
package de.fraunhofer.fokus.upnp.util.security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class is used to load public RSA keys from known control points.
 * 
 * @author Sebastian Nauck, Alexander Koenig
 * 
 */
public class PersistentRSAPublicKeyLoader
{

  /**
   * Loads public RSA keys from the local key cache.
   * 
   * 
   * @return A vector containing PersonalizedKeyObjects for all known public keys
   */
  public static Vector loadRSAPublicKeys(boolean loadCPKeys)
  {
    Vector personalizedKeyObjectList = new Vector();

    BufferedReader reader = null;
    String line = null;
    try
    {
      reader =
        new BufferedReader(new InputStreamReader(new FileInputStream((loadCPKeys ? getCPPublicKeysFileName()
          : getDevicePublicKeysFileName())), "utf-8"));
      line = reader.readLine();
    } catch (UnsupportedEncodingException e)
    {
      System.out.println("UnsupportedEncodingException" + e.getMessage());
    } catch (FileNotFoundException e)
    {
      System.out.println("FileNotFoundException" + e.getMessage());
    } catch (IOException e)
    {
      System.out.println("IOException" + e.getMessage());
    }

    while (line != null && line.length() != 0)
    {
      String publicExponent = "";
      String modulus = "";
      String owner = "";

      StringTokenizer keyTokenizer = new StringTokenizer(line, ":");
      if (keyTokenizer.countTokens() > 2)
      {
        owner = keyTokenizer.nextToken();
        publicExponent = keyTokenizer.nextToken();
        modulus = keyTokenizer.nextToken();
      }
      if (!modulus.equals("") && !(publicExponent.equals("") && !owner.equals("")))
      {
        try
        {
          personalizedKeyObjectList.add(new PersonalizedKeyObject(new PersistentRSAPublicKey(new BigInteger(publicExponent),
            new BigInteger(modulus)),
            owner));
        } catch (Exception e)
        {

        }
      }
      try
      {
        line = reader.readLine();
      } catch (IOException e)
      {
      }
    }
    return personalizedKeyObjectList;
  }

  /**
   * Returns the absolute name of the file where public keys from control points should be stored.
   */
  private static String getCPPublicKeysFileName()
  {
    return FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + "key_cache") + "cpPublicKeys.txt";
  }

  /**
   * Returns the absolute name of the file where public keys from control points should be stored.
   */
  private static String getDevicePublicKeysFileName()
  {
    return FileHelper.toValidDirectoryName(FileHelper.getResourceDirectoryName() + "key_cache") +
      "devicePublicKeys.txt";
  }

}

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
package de.fraunhofer.fokus.upnp.util.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.fraunhofer.fokus.upnp.util.URLHelper;

/**
 * This class can be used to download files via HTTP.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class HTTPHelper
{

  /**
   * Reads a file via HTTP.
   * 
   * @param host
   * @param fileName
   * @param data
   * 
   */
  public static byte[] downloadFile(String host, String fileName)
  {
    byte[] result = null;
    try
    {
      URLConnection urlConnection = null;
      URL targetURL = null;
      String urlContent = null;

      fileName = URLHelper.escapeURL(fileName);

      urlContent = "http://" + host + "/" + fileName;

      urlContent =
        urlContent.replaceAll("ß", "ss")
          .replaceAll("ö", "oe")
          .replaceAll("ä", "ae")
          .replaceAll("ü", "ue")
          .replaceAll("Ö", "Oe")
          .replaceAll("Ä", "Ae")
          .replaceAll("Ü", "Ue");

      targetURL = new URL(urlContent);

      urlConnection = targetURL.openConnection();
      InputStream downloadStream = urlConnection.getInputStream();
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      int bytesRead = 0;
      byte[] buffer = new byte[4096];
      while (bytesRead >= 0)
      {
        bytesRead = downloadStream.read(buffer, 0, buffer.length);
        if (bytesRead > 0)
        {
          byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
      }
      result = byteArrayOutputStream.toByteArray();
    } catch (Exception e)
    {
      System.out.println("Error downloading data: " + e.getMessage());
    }
    return result;
  }

}

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
package de.fraunhofer.fokus.upnp.gena;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.CommonConstants;

/**
 * This class provides helper methods for GENA parsing.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class GENAParser
{

  /**
   * Parses the URLs from a SUBSCRIBE callback value.
   * 
   * 
   * @param callbackValue
   *          String containing multiple URLs like <URL1<>URL2><...>,
   * 
   * @return A vector containing all valid URLs
   */
  public static Vector parseCallbackValue(String callbackValue)
  {
    Vector result = new Vector();

    int posBegin = -1;
    int posEnd = -1;
    int oldBegin = 0;
    int oldEnd = 0;
    boolean urltest = true;

    // until all urls are read
    while (urltest)
    {
      posBegin = callbackValue.indexOf("<", oldBegin);
      posEnd = callbackValue.indexOf(">", oldEnd);

      // new position for more URLs (check for end of text length)
      if (posBegin != -1)
      {
        URL url = null;

        try
        {
          url = new URL(callbackValue.substring(posBegin + 1, posEnd));
          // add valid URLs
          if (url.getProtocol().toUpperCase().equals("HTTP") && url.getHost() != null)
          {
            // add default port if necessary
            if (url.getPort() == -1)
            {
              url = new URL(url.getProtocol(), url.getHost(), CommonConstants.HTTP_DEFAULT_PORT, url.getFile());
            }

            result.add(url);
          }
        } catch (MalformedURLException e)
        {
          // logger.warn("invalid subscribe message");
          // logger.warn("reason: cannot build callback URL " + e.getMessage());
        }
        oldBegin = posBegin + 1;
        oldEnd = posEnd + 1;
      } else
      {
        urltest = false;
      }
    }
    return result;
  }

}

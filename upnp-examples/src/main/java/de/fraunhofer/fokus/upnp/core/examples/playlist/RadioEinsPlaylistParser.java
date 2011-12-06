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
package de.fraunhofer.fokus.upnp.core.examples.playlist;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;

/**
 * Parses the content of the page radioeins.funtip.de/playList.do.
 * 
 * @author sen, Alexander Koenig
 * 
 */

public class RadioEinsPlaylistParser extends SAXTemplateHandler
{

  private String time      = null;

  private String artist    = null;

  private String songTitle = null;

  /** Vector containing all playlist entries. */
  private Vector playlist  = new Vector();

  private int    dataCount = 0;

  public Vector getPlaylist()
  {
    return playlist;
  }

  public void processStartElement(String uri, String name, String name2, Attributes atts) throws SAXException
  {
    if (getTagCount() == 2)
    {
      time = null;
      artist = null;
      songTitle = null;
      dataCount = 0;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void processEndElement(String uri, String localName, String qName) throws SAXException
  {
    if (getTagCount() == 2 && time != null && artist != null && songTitle != null)
    {
      playlist.add(new PlaylistEntry(time, artist, songTitle));
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.xml.SAXTemplateHandler#processContentElement(java.lang.String)
   */
  public void processContentElement(String content)
  {
    if (getTagCount() == 3)
    {
      if (dataCount == 0)
      {
        time = content;
        dataCount++;
        return;

      }
      if (dataCount == 1)
      {
        artist = content;
        dataCount++;
        return;
      }
      if (dataCount == 2)
      {
        songTitle = content;
        dataCount++;
        return;
      }
    }
  }
}

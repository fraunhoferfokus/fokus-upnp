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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.fraunhofer.fokus.upnp.util.DateTimeHelper;

/**
 * This class holds one playlist entry.
 * 
 * @author sen, Alexander Koenig
 * 
 */
public class PlaylistEntry
{

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);

  /** Time when the song was played */
  private Date                    time;

  /** Artist */
  private String                  artist;

  /** Title */
  private String                  title;

  /**
   * Creates a new instance of PlaylistEntry.
   * 
   * @param time
   * @param artist
   * @param title
   */
  public PlaylistEntry(String time, String artist, String title)
  {
    // fallback
    this.time = DateTimeHelper.getDate();
    try
    {
      this.time = dateFormat.parse(time);
    } catch (Exception e)
    {
    }
    Date upnpDate = DateTimeHelper.getDateFromUPnP(time);
    if (upnpDate != null)
    {
      this.time = upnpDate;
    }

    this.artist = artist;
    this.title = title;
  }

  public String getArtist()
  {
    return artist;
  }

  public Date getTime()
  {
    return time;
  }

  public String getTitle()
  {
    return title;
  }

}

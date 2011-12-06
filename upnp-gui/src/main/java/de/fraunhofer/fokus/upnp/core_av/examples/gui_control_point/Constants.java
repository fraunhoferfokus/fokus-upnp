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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_control_point;

/**
 * @author tje
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public class Constants
{
  public static final String TEXT_ARTIST                  = "Artist";

  public static final String TEXT_GENRE                   = "Genre";

  public static final String TEXT_TITEL                   = "Titel";

  public static final String TEXT_PLAYLIST                = "PlayList";

  public static final String TEXT_ALBUM                   = "Album";

  public static final String TEXT_SEARCH_CREATE           = "Search/Create";

  public static final String SEARCH_TEXT_ITEM_TITEL       =
                                                            "upnp:class = \"object.item.audioItem.musicTrack\" and refID exists false";

  public static final String SEARCH_TEXT_CONTAINER_ARTIST = "upnp:class = \"object.container.person.musicArtist\"";

  public static final String SEARCH_TEXT_CONTAINER_GENRE  = "upnp:class = \"object.container.genre.musicGenre\"";

  public static final String SEARCH_TEXT_CONTAINER_ALBUM  = "upnp:class = \"object.container.album.musicAlbum\"";
}

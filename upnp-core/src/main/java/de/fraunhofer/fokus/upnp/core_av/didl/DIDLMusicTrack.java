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
 * @author Alexander Koenig
 * 
 * This class describes a musicTrack
 */
public class DIDLMusicTrack extends DIDLAudioItem
{

  private String artist;

  private String album;

  private String originalTrackNumber;

  /**
   * Holds value of property playlist.
   */
  private String playlist;

  public DIDLMusicTrack(DIDLObject sourceObject)
  {
    super(sourceObject);
    setObjectClass(DIDLConstants.UPNP_CLASS_MUSIC_TRACK);
  }

  /** Creates a item in the root container */
  public DIDLMusicTrack(String title, String id)
  {
    super(title, id);
    setObjectClass(DIDLConstants.UPNP_CLASS_MUSIC_TRACK);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone()
  {
    return new DIDLMusicTrack(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object arg0)
  {
    if (!(arg0 instanceof DIDLMusicTrack))
    {
      return false;
    }
    // TO-DO: change
    return super.equals(arg0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#innerXMLDescription(boolean, java.util.Vector,
   *      java.lang.String)
   */
  public String innerXMLDescription(boolean showAll, Vector filterList, String absoluteServerPath)
  {
    String result = super.innerXMLDescription(showAll, filterList, absoluteServerPath);

    // add optional elements
    if (getArtist() != null && (showAll || isContained(filterList, DIDLConstants.UPNP_ARTIST)))
    {
      result += "<" + DIDLConstants.UPNP_ARTIST + ">" + getArtist() + "</" + DIDLConstants.UPNP_ARTIST + ">";
    }
    if (getAlbum() != null && (showAll || isContained(filterList, DIDLConstants.UPNP_ALBUM)))
    {
      result += "<" + DIDLConstants.UPNP_ALBUM + ">" + getAlbum() + "</" + DIDLConstants.UPNP_ALBUM + ">";
    }

    return result;
  }

  /**
   * Getter for property artist.
   * 
   * @return Value of property artist.
   */
  public String getArtist()
  {
    return this.artist;
  }

  /**
   * Setter for property artist.
   * 
   * @param artist
   *          New value of property artist.
   */
  public void setArtist(String artist)
  {
    this.artist = artist;
  }

  /**
   * Getter for property album.
   * 
   * @return Value of property album.
   */
  public String getAlbum()
  {
    return this.album;
  }

  /**
   * Setter for property album.
   * 
   * @param album
   *          New value of property album.
   */
  public void setAlbum(String album)
  {
    this.album = album;
  }

  /**
   * Getter for property originalTrackNumber.
   * 
   * @return Value of property originalTrackNumber.
   */
  public String getOriginalTrackNumber()
  {
    return this.originalTrackNumber;
  }

  /**
   * Setter for property originalTrackNumber.
   * 
   * @param originalTrackNumber
   *          New value of property originalTrackNumber.
   */
  public void setOriginalTrackNumber(String originalTrackNumber)
  {
    this.originalTrackNumber = originalTrackNumber;
  }

  /**
   * Getter for property playlist.
   * 
   * @return Value of property playlist.
   */
  public String getPlaylist()
  {
    return this.playlist;
  }

  /**
   * Setter for property playlist.
   * 
   * @param playlist
   *          New value of property playlist.
   */
  public void setPlaylist(String playlist)
  {
    this.playlist = playlist;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.didl.DIDLObject#fillClassSpecificData()
   */
  public void fillClassSpecificData()
  {
    super.fillClassSpecificData();
    int i = 0;
    while (i < propertyList.size())
    {
      boolean found = false;
      DIDLPropertyClass property = (DIDLPropertyClass)propertyList.elementAt(i);

      if (property.getIdentifier().equals(DIDLConstants.UPNP_ARTIST))
      {
        artist = property.getValue();
        found = true;
      }
      if (property.getIdentifier().equals(DIDLConstants.UPNP_ALBUM))
      {
        album = property.getValue();
        found = true;
      }

      if (found)
      {
        propertyList.remove(i);
      } else
      {
        i++;
      }
    }
  }

}

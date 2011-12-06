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
package de.fraunhofer.fokus.upnp.core_av.examples.server.predefined;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.DeviceStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLMusicTrack;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLPhoto;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLVideoItem;
import de.fraunhofer.fokus.upnp.core_av.server.BrowseResponse;
import de.fraunhofer.fokus.upnp.core_av.server.IMediaServerContentProvider;
import de.fraunhofer.fokus.upnp.core_av.server.MediaServerDevice;

/**
 * This class creates a UPnP media server.
 * 
 * @author Alexander Koenig
 */
public class MediaServerEntity extends TemplateEntity implements IMediaServerContentProvider
{

  private DIDLContainer rootContainer;

  /**
   * Creates a new instance of MediaServerEntity.
   */
  public MediaServerEntity()
  {
    this(null);
  }

  /**
   * Creates a new instance of MediaServerEntity with a predefined startup configuration.
   */
  public MediaServerEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);

    if (startupConfiguration == null)
    {
      startupConfiguration = getStartupConfiguration();
    }
    int userID = startupConfiguration.getNumericProperty("UserID", 0);

    System.out.println("Start MediaServer for userID " + userID);

    rootContainer = new DIDLContainer("Medienserver", "0");
    DIDLContainer pictureContainer = new DIDLContainer("Bilder", "100");

    DIDLItem austria = new DIDLPhoto("Österreich", "101");
    austria.addResource(new DIDLResource("http-get:*:image/jpeg:*", "austria.jpg"));

    DIDLItem canada = new DIDLPhoto("Kanada", "102");
    canada.addResource(new DIDLResource("http-get:*:image/jpeg:*", "canada.jpg"));

    DIDLItem new_york = new DIDLPhoto("New York", "103");
    new_york.addResource(new DIDLResource("http-get:*:image/jpeg:*", "new_york.jpg"));

    DIDLItem scotland = new DIDLPhoto("Schottland", "104");
    scotland.addResource(new DIDLResource("http-get:*:image/jpeg:*", "scotland.jpg"));

    pictureContainer.setChildList(new DIDLObject[] {
        austria, canada, new_york, scotland
    });

    DIDLContainer musicContainer = new DIDLContainer("Musik", "300");

    DIDLMusicTrack toco1 = new DIDLMusicTrack("Freiburg", "301");
    toco1.setArtist("Tocotronic");
    toco1.setAlbum("Digital ist besser");
    toco1.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "freiburg.mp3"));

    DIDLMusicTrack toco2 = new DIDLMusicTrack("Freiburg Web", "302");
    toco2.setArtist("Tocotronic");
    toco2.setAlbum("Digital ist besser");
    toco2.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "http://www.filesurfer.de/freiburg.mp3"));

    DIDLMusicTrack joplin = new DIDLMusicTrack("Piece of my heart", "303");
    joplin.setArtist("Janis Joplin");
    joplin.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "joplin.mp3"));

    DIDLMusicTrack mann = new DIDLMusicTrack("One", "304");
    mann.setArtist("Aimee Mann");
    mann.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "mann.mp3"));

    DIDLMusicTrack melua = new DIDLMusicTrack("Crawling up the hill", "305");
    melua.setArtist("Katie Melua");
    melua.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "melua.mp3"));

    DIDLMusicTrack pet = new DIDLMusicTrack("It's a sin", "306");
    pet.setArtist("Pet Shop Boys");
    pet.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "pet.mp3"));

    DIDLMusicTrack u2 = new DIDLMusicTrack("One", "307");
    u2.setArtist("U2");
    u2.addResource(new DIDLResource("http-get:*:audio/mpeg:*", "u2.mp3"));

    musicContainer.setChildList(new DIDLObject[] {
        joplin, melua, pet, u2
    });

    DIDLContainer videoContainer = new DIDLContainer("Video", "400");

    DIDLVideoItem dontknow = new DIDLVideoItem("Dont know", "401");
    dontknow.addResource(new DIDLResource("http-get:*:video/mpeg:*", "http://www.ivistar.de/movies/dontknow.mpg"));

    DIDLVideoItem rfid = new DIDLVideoItem("RFID Innovation", "402");
    rfid.addResource(new DIDLResource("http-get:*:video/mpeg:*",
      "http://www.ivistar.de/movies/RFID_Innovation_about_T_Labs_small.mpg"));

    DIDLVideoItem jack = new DIDLVideoItem("Jack Jack attack", "404");
    jack.addResource(new DIDLResource("http-get:*:video/mpeg:*", "http://www.filesurfer.de/multimedia/jack.mpg"));

    videoContainer.setChildList(new DIDLObject[] {
        dontknow, rfid, jack
    });

    boolean handled = false;
    // set child list dependent on userID
    if (userID == 1)
    {
      rootContainer.setChildList(new DIDLObject[] {
        pictureContainer
      });
      handled = true;
    }
    if (userID == 2)
    {
      rootContainer.setChildList(new DIDLObject[] {
          musicContainer, videoContainer
      });
      handled = true;
    }
    if (!handled)
    {
      rootContainer.setChildList(new DIDLObject[] {
          pictureContainer, musicContainer, videoContainer
      });
    }

    DeviceStartupConfiguration deviceStartupConfiguration =
      (DeviceStartupConfiguration)startupConfiguration.getSingleDeviceStartupConfiguration();
    // create delayed to set content provider
    deviceStartupConfiguration.setRunDelayed(true);
    // add userID to UDN
    deviceStartupConfiguration.setUDN(deviceStartupConfiguration.getUDN() + userID);
    MediaServerDevice mediaServer = new MediaServerDevice(this, startupConfiguration);
    mediaServer.setContentProvider(this);
    mediaServer.runDelayed();
    // must be called after runDelayed to access content directory
    mediaServer.addWebServerDirectory(mediaServer.getWorkingDirectory() + "resources");
    setTemplateDevice(mediaServer);
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new MediaServerEntity();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Content provider implementation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#getSystemUpdateID()
   */
  public long getSystemUpdateID()
  {
    // no updates for this media server
    return 0;
  }

  public BrowseResponse browseMetadata(String objectID)
  {
    DIDLObject didlObject = rootContainer.getRecursiveChild(objectID);
    if (didlObject != null)
    {
      if (didlObject.getParentContainer() != null)
      {
        return new BrowseResponse(didlObject, didlObject.getParentContainer().getContainerUpdateID());
      } else
      {
        return new BrowseResponse(didlObject, getSystemUpdateID());
      }
    }
    return null;
  }

  public BrowseResponse browseDirectChildren(String objectID)
  {
    return browseDirectChildren(objectID, 0, 0, "*", "");
  }

  public BrowseResponse browseDirectChildren(String objectID,
    int startingIndex,
    int requestedCount,
    String filter,
    String sortCriteria)
  {
    DIDLObject didlObject = rootContainer.getRecursiveChild(objectID);
    if (didlObject instanceof DIDLContainer)
    {
      DIDLObject[] fullResult = ((DIDLContainer)didlObject).getChildList();
      // split due to request
      int resultCount = fullResult.length - startingIndex;
      if (requestedCount != 0)
      {
        resultCount = Math.min(startingIndex + requestedCount, fullResult.length) - startingIndex;
      }
      if (resultCount <= 0)
      {
        return new BrowseResponse(null, 0, fullResult.length, 0);
      }

      DIDLObject[] result = new DIDLObject[resultCount];
      for (int i = 0; i < resultCount; i++)
      {
        result[i] = fullResult[i + startingIndex];
      }
      return new BrowseResponse(result, result.length, fullResult.length, 0);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.server.IMediaServerContentProvider#getDIDLItem(java.lang.String)
   */
  public DIDLObject getDIDLItem(String importURI)
  {
    // no importURIs in this media server
    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private classes //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * private class DIDLComparator implements Comparator { private String sortCriteria = "";
   * 
   * public DIDLComparator(String sort) { sortCriteria = sort; }
   * 
   * public int compare(Object a, Object b) { DIDLObject didlA = (DIDLObject)a; DIDLObject didlB =
   * (DIDLObject)b;
   * 
   * if (sortCriteria.equals("-dc:title")) return didlB.getTitle().compareTo(didlA.getTitle()); else
   * return didlA.getTitle().compareTo(didlB.getTitle()); } }
   */

}

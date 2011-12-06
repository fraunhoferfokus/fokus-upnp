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

/**
 * @author Alexander Koenig
 */
public class DIDLConstants
{

  public final static String CONTAINER              = "container";

  public final static String ITEM                   = "item";

  public final static String DC_CREATOR             = "dc:creator";

  public final static String DC_DATE                = "dc:date";

  public final static String DC_DESCRIPTION         = "dc:description";

  public final static String DC_LANGUAGE            = "dc:language";

  public final static String DC_PUBLISHER           = "dc:publisher";

  public final static String DC_RELATION            = "dc:relation";

  public final static String DC_RIGHTS              = "dc:rights";

  public final static String DC_TITLE               = "dc:title";

  public final static String UPNP_ALBUM             = "upnp:album";

  public final static String UPNP_ARTIST            = "upnp:artist";

  public final static String UPNP_CLASS             = "upnp:class";

  public final static String UPNP_CREATECLASS       = "upnp:createClass";

  public final static String UPNP_GENRE             = "upnp:genre";

  public final static String UPNP_LONG_DESCRIPTION  = "upnp:longDescription";

  public final static String UPNP_RATING            = "upnp:rating";

  public final static String UPNP_SEARCHCLASS       = "upnp:searchClass";

  public final static String UPNP_STORAGE_MEDIUM    = "upnp:storageMedium";

  public final static String UPNP_STORAGE_USED      = "upnp:storageUsed";

  public final static String UPNP_WRITESTATUS       = "upnp:writeStatus";

  public final static String UPNP_CLASS_AUDIO_ITEM  = "object.item.audioItem";

  public final static String UPNP_CLASS_CONTAINER   = "object.container";

  public final static String UPNP_CLASS_IMAGE_ITEM  = "object.item.imageItem";

  public final static String UPNP_CLASS_ITEM        = "object.item";

  public final static String UPNP_CLASS_MUSIC_TRACK = "object.item.audioItem.musicTrack";

  public final static String UPNP_CLASS_PHOTO       = "object.item.imageItem.photo";

  public final static String UPNP_CLASS_VIDEO_ITEM  = "object.item.videoItem";

  public final static String ATTR_CHILD_COUNT       = "childCount";

  public final static String ATTR_INCLUDE_DERIVED   = "includeDerived";

  public final static String ATTR_NAME              = "name";

  public final static String ATTR_RESTRICTED        = "restricted";

  public final static String ATTR_SEARCHABLE        = "searchable";

  public final static String ATTR_ID                = "id";

  public final static String ATTR_PARENT_ID         = "parentID";

  public final static String ATTR_REF_ID            = "refID";

  public final static String RES                    = "res";

  public final static String RES_PROTOCOL_INFO      = "protocolInfo";

  public final static String RES_IMPORT_URI         = "importUri";

  public final static String RES_SIZE               = "size";

  public final static String RES_DURATION           = "duration";

  public final static int    RES_TYPE_UNKNOWN       = 0;

  public final static int    RES_TYPE_IMAGE         = 1;

  public final static int    RES_TYPE_AUDIO         = 2;

  public final static int    RES_TYPE_VIDEO         = 3;

  public static final String MP3_PROTOCOL_INFO      = "http-get:*:audio/mpeg:*";

  public static final String EXTENSION_JPG          = "jpg";

  public static final String EXTENSION_MP3          = "mp3";

  public static final String EXTENSION_MPG          = "mpg";

}

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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_renderer;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.io.InputStream;
import java.net.URL;

/**
 * @author tje
 * 
 */
public class CPGUIConstants
{
  // metaDataConstants
  public final static String       LESS_THAN            = "&lt;";

  public final static String       GREATER_THAN         = "&gt;";

  public final static String       DC_XMLNS             = "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"";

  public final static String       UPNP_XMLNS           = "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"";

  public final static String       UPNP_DIDL_LITE_XMLNS = "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"";

  public final static String       DIDLE_LITE           = "DIDL-Lite";

  public final static String       DIDL_LITE            = "DIDL-Lite";

  public final static String       ITEM                 = "item";

  public final static String       DC_TITLE             = "dc:title";

  public final static String       DC_DATE              = "dc:date";

  public final static String       DC_CREATOR           = "dc:creator";

  public final static String       UPNP_CLASS           = "upnp:class";

  public final static String       RES                  = "res";

  public final static String       RES_PROTOCOL_INFO    = "protocolInfo";

  public final static String       RES_IMPORT_URI       = "importUri";

  public final static String       RES_SIZE             = "size";

  public final static String       RES_DURATION         = "duration";

  public static final String       NO_RENDERER          = "No media Renderer found";

  // images positions
  public static final int          buttonWidth          = 39;

  public static final int          buttonHeight         = 41;

  public static final int          yPosButtons          = 16;

  public static final int          xPosEject            = 392;

  public static final int          xPosSkipNext         = 343;

  public static final int          xPosSkipBack         = 300;

  public static final int          xPosStop             = 163;

  public static final int          xPosOnOff            = 28;

  public static final int          xPosRew              = 210;

  public static final int          xPosPlay             = 77;

  public static final int          xPosPause            = 120;

  public static final int          xPosFF               = 253;

  public static final int          xPosMute             = 642;

  public static final int          volWidth             = 33;

  public static final int          volHeight            = 28;

  public static final int          xPosVolUp            = 590;

  public static final int          xPosVolDown          = 440;

  public static final int          yPosVol              = 21;

  // image positions video
  public static final int          xPosBrightUp         = 730;

  public static final int          xPosBrightDown       = 773;

  public static final int          xPosContrastUp       = 826;

  public static final int          xPosContrastDown     = 869;

  // constants for rendere control images
  public static final String       IMAGE_PATH           = "renderercontrol/";

  public static final String       BAR                  = "bar.gif";

  public static final String       EJECT_A              = "eject-A.gif";

  public static final String       EJECT_F              = "eject-F.gif";

  public static final String       EJECT_FA             = "eject-FA.gif";

  public static final String       EJECT_N              = "eject-N.gif";

  public static final String       FF_A                 = "ff-A.gif";

  public static final String       FF_F                 = "ff-F.gif";

  public static final String       FF_FA                = "ff-FA.gif";

  public static final String       FF_N                 = "ff-N.gif";

  public static final String       LISSA_BG             = "lissaBG.gif";

  public static final String       MUTE_A               = "mute-A.gif";

  public static final String       MUTE_F               = "mute-F.gif";

  public static final String       MUTE_FA              = "mute-FA.gif";

  public static final String       MUTE_N               = "mute-N.gif";

  public static final String       ONOFF_A              = "onoff-A.gif";

  public static final String       ONOFF_F              = "onoff-F.gif";

  public static final String       ONOFF_FA             = "onoff-FA.gif";

  public static final String       ONOFF_N              = "onoff-N.gif";

  public static final String       PAUSE_A              = "pause-A.gif";

  public static final String       PAUSE_F              = "pause-F.gif";

  public static final String       PAUSE_FA             = "pause-FA.gif";

  public static final String       PAUSE_N              = "pause-N.gif";

  public static final String       PLAY_A               = "play-A.gif";

  public static final String       PLAY_F               = "play-F.gif";

  public static final String       PLAY_FA              = "play-FA.gif";

  public static final String       PLAY_N               = "play-N.gif";

  public static final String       REW_A                = "rew-A.gif";

  public static final String       REW_F                = "rew-F.gif";

  public static final String       REW_FA               = "rew-FA.gif";

  public static final String       REW_N                = "rew-N.gif";

  public static final String       SKIPBACK_A           = "skipBack-A.gif";

  public static final String       SKIPBACK_F           = "skipBack-F.gif";

  public static final String       SKIPBACK_FA          = "skipBack-FA.gif";

  public static final String       SKIPBACK_N           = "skipBack-N.gif";

  public static final String       SKIPNEXT_A           = "skipNext-A.gif";

  public static final String       SKIPNEXT_F           = "skipNext-F.gif";

  public static final String       SKIPNEXT_FA          = "skipNext-FA.gif";

  public static final String       SKIPNEXT_N           = "skipNext-N.gif";

  public static final String       STOP_A               = "stop-A.gif";

  public static final String       STOP_F               = "stop-F.gif";

  public static final String       STOP_FA              = "stop-FA.gif";

  public static final String       STOP_N               = "stop-N.gif";

  public static final String       VOLUMEBAR_BG         = "volumebarBG.gif";

  public static final String       VOL_DOWN_F           = "volume-down-F.gif";

  public static final String       VOL_DOWN_FA          = "volume-down-FA.gif";

  public static final String       VOL_DOWN_N           = "volume-down-N.gif";

  public static final String       VOL_UP_F             = "volume-up-F.gif";

  public static final String       VOL_UP_FA            = "volume-up-FA.gif";

  public static final String       VOL_UP_N             = "volume-up-N.gif";

  public static final String       FONT_ARIAL           = "Arial";

  private final static Toolkit     toolkit              = Toolkit.getDefaultToolkit();

  private final static ClassLoader loader               = CPGUIConstants.class.getClassLoader();

  // images for video control
  public final static String       VIDEOBACK            = "videoBG.gif";

  public static final String       BRIGHT_UP_F          = "brightUP-F.gif";

  public static final String       BRIGHT_UP_FA         = "brightUP-FA.gif";

  public static final String       BRIGHT_UP_N          = "brightUP-N.gif";

  public static final String       BRIGHT_DOWN_F        = "brightDOWN-F.gif";

  public static final String       BRIGHT_DOWN_FA       = "brightDOWN-FA.gif";

  public static final String       BRIGHT_DOWN_N        = "brightDOWN-N.gif";

  public static final String       CONT_UP_F            = "contUP-F.gif";

  public static final String       CONT_UP_FA           = "contUP-FA.gif";

  public static final String       CONT_UP_N            = "contUP-N.gif";

  public static final String       CONT_DOWN_F          = "contDOWN-F.gif";

  public static final String       CONT_DOWN_FA         = "contDOWN-FA.gif";

  public static final String       CONT_DOWN_N          = "contDOWN-N.gif";

  /**
   * Retrieves the image from the specified relative path. The method tries first to get the image
   * as resource. If there is no such resource, then the second attempt is reading it from a file.
   * <p>
   * The method only returns a reference to the image. The image is not loaded.
   * 
   * @param path
   *          relative path to the image
   * @return the reference to the image or null, if either the path does not point to an image, or
   *         if an error occurs.
   */
  public static Image getImage(String path)
  {
    Image img = null;

    try
    {
      URL url = getResource(path);

      if (url != null)
      {
        Object o = url.getContent();

        if (o instanceof ImageProducer)
        {
          img = toolkit.createImage((ImageProducer)o);
        } else if (o instanceof InputStream)
        {
          img = toolkit.createImage(streamToBytes((InputStream)o));
        }
      }

      // Try to load it from the filesystem.
      if (img == null)
      {
        img = toolkit.getImage(path);
      }
    } catch (Exception e)
    {
      img = null;
    }

    return img;
  }

  /**
   * Loads all available bytes from the specified inputstream.
   * 
   * @param is
   *          the InputStream to read from
   * @return the byte array containing all available input or null in case of an error
   */
  private static byte[] streamToBytes(InputStream is)
  {
    byte[] res;

    try
    {
      res = new byte[is.available()];
      is.read(res);
      is.close();
    } catch (Exception e)
    {
      res = null;
    } finally
    {
      try
      {
        is.close();
      } catch (Exception ex)
      {
      }
    }

    return res;
  }

  /**
   * Gets the URL for the specified resource.
   * 
   * @param path
   *          relative path to the resource
   * @return the URL of the resource or null, if the resource does not exist.
   */
  private static URL getResource(String path)
  {
    URL url = loader == null ? ClassLoader.getSystemResource(path) : loader.getResource(path);

    return url;
  }
}

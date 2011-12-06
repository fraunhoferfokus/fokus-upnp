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
package de.fraunhofer.fokus.upnp.core.examples.gui_common;

import java.awt.Color;

import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;

/**
 * 
 * @author Alexander Koenig
 */
public class GUIConstants
{

  public static final int   STANDARD_FONT_SIZE           = 18;

  public static final int   TEXT_FONT_SIZE               = 12;

  /** Height for standard buttons */
  public static final int   BUTTON_HEIGHT                = 30;

  /** Height for description buttons */
  public static final int   DESCRIPTION_BUTTON_HEIGHT    = 45;

  /** Width for description buttons */
  public static final int   DESCRIPTION_BUTTON_WIDTH     = 280;

  public static final int   DEVICE_BUTTON_WIDTH          = 350;

  public static final int   STATEVARIABLE_BUTTON_WIDTH   = 300;

  public static final int   SERVICE_BUTTON_WIDTH         = 160;

  public static final int   ACTION_BUTTON_WIDTH          = 250;

  public static final int   IN_ARGUMENT_BUTTON_WIDTH     = 300;

  public static final int   PROPERTY_BUTTON_WIDTH        = 350;

  public static final int   PROPERTY_INDENT_BUTTON_WIDTH = 320;

  public static final int   OUT_ARGUMENT_BUTTON_WIDTH    = 300;

  public static final int   OVERVIEW_BUTTON_WIDTH        = 300;

  public static final int   OVERVIEW_INDENT_BUTTON_WIDTH = 270;

  public static final int   BUTTON_INDENTATION           = 30;

  public static final int   GENRE_MEDIA_SERVER           = 1;

  public static final int   GENRE_MEDIA_RENDERER         = 2;

  public static final int   GENRE_PERSONALIZATION        = 3;

  public static final int   GENRE_HOME_CONTROL           = 4;

  public static final int   GENRE_SENSOR                 = 5;

  public static final int   GENRE_MESSAGING              = 6;

  public static final int   GENRE_MISC                   = 7;

  public static final Color GENRE_MEDIA_SERVER_COLOR     = new Color(160, 160, 210);

  public static final Color GENRE_MEDIA_RENDERER_COLOR   = new Color(170, 210, 160);

  public static final Color GENRE_PERSONALIZATION_COLOR  = new Color(210, 210, 160);

  public static final Color GENRE_HOME_CONTROL_COLOR     = new Color(230, 210, 150);

  public static final Color GENRE_SENSOR_COLOR           = new Color(210, 180, 200);

  public static final Color GENRE_MESSAGING_COLOR        = new Color(210, 160, 160);

  public static final Color GENRE_MISC_COLOR             = new Color(180, 210, 210);

  public static final Color GENRE_ACTIVE_SAFETY_COLOR    = new Color(240, 100, 100);

  public static final Color SMOOTH_BLUE_COLOR            = new Color(160, 160, 210);

  public static final Color SMOOTH_GREEN_COLOR           = new Color(170, 210, 160);

  public static final Color SMOOTH_YELLOW_COLOR          = new Color(210, 210, 160);

  public static final Color SMOOTH_ORANGE_COLOR          = new Color(230, 210, 150);

  public static final Color SMOOTH_PURPLE_COLOR          = new Color(210, 180, 200);

  public static final Color SMOOTH_RED_COLOR             = new Color(210, 160, 160);

  public static final Color SMOOTH_MINT_COLOR            = new Color(180, 210, 210);

  // public static final Color BACKGROUND_COLOR = new Color(200,200,210);
  // public static final Color DARK_BACKGROUND_COLOR = new Color(100,100,110);
  // public static final Color BUTTON_COLOR = new Color(170,170,170);
  // public static final Color RED_BUTTON_COLOR = new Color(190,160,160);
  // public static final Color LIGHT_BUTTON_COLOR = new Color(200,200,200);
  // public static final Color RED_LIGHT_BUTTON_COLOR = new Color(220,190,190);
  // public static final Color ACTIVE_BUTTON_COLOR = new Color(220,220,250);
  // public static final Color DARK_ACTIVE_BUTTON_COLOR = new Color(150,150,210);
  // public static final Color FONT_COLOR = new Color(50,50,50);
  // public static final Color FONT_HIGHLIGHT_COLOR = new Color(220,220,230);
  // public static final Color DISABLED_BUTTON_COLOR = new Color(100,100,100);
  // public static final Color DEVICE_BUTTON_COLOR = new Color(150,150,150);
  // public static final Color MANAGEMENT_BUTTON_COLOR = new Color(200,190,170);
  // public static final Color SAVE_ACTIVE_BUTTON_COLOR = new Color(255,204,204);
  // public static final Color FOLDER_BUTTON_COLOR = new Color(255,230,204);

  // public static final Color BACKGROUND_COLOR = new Color(255,240,200);
  public static final Color BACKGROUND_COLOR             = new Color(255, 250, 240);

  // public static final Color DARK_BACKGROUND_COLOR = new Color(225,210,170);
  public static final Color DARK_BACKGROUND_COLOR        = new Color(225, 215, 200);

  public static final Color BUTTON_COLOR                 = new Color(180, 175, 160);

  public static final Color RED_BUTTON_COLOR             = new Color(190, 160, 160);

  public static final Color LIGHT_BUTTON_COLOR           = new Color(220, 215, 200);

  public static final Color RED_LIGHT_BUTTON_COLOR       = new Color(220, 190, 190);

  // public static final Color ACTIVE_BUTTON_COLOR = new Color(205,190,150);
  public static final Color ACTIVE_BUTTON_COLOR          = new Color(220, 215, 200);

  public static final Color FONT_COLOR                   = new Color(50, 50, 50);

  public static final Color FONT_HIGHLIGHT_COLOR         = new Color(220, 220, 230);

  public static final Color DISABLED_BUTTON_COLOR        = new Color(100, 100, 100);

  public static final Color DEVICE_BUTTON_COLOR          = new Color(150, 150, 150);

  public static final Color MANAGEMENT_BUTTON_COLOR      = new Color(200, 190, 170);

  public static final Color SAVE_ACTIVE_BUTTON_COLOR     = new Color(255, 204, 204);

  public static final Color FOLDER_BUTTON_COLOR          = new Color(255, 230, 204);

  /*
   * public static final Color BACKGROUND_COLOR = new Color(100,100,100); public static final Color
   * DARK_BACKGROUND_COLOR = new Color(50,50,50); public static final Color BUTTON_COLOR = new
   * Color(204,204,204); public static final Color LIGHT_BUTTON_COLOR = new Color(220,220,220);
   * public static final Color ACTIVE_BUTTON_COLOR = new Color(204,204,255); public static final
   * Color DARK_ACTIVE_BUTTON_COLOR = new Color(153,153,255); public static final Color FONT_COLOR =
   * new Color(50,50,50); public static final Color DISABLED_BUTTON_COLOR = new Color(153,153,153);
   * public static final Color DEVICE_BUTTON_COLOR = new Color(170,200,170); public static final
   * Color MANAGEMENT_BUTTON_COLOR = new Color(200,190,170); public static final Color
   * SAVE_ACTIVE_BUTTON_COLOR = new Color(255,204,204); public static final Color
   * FOLDER_BUTTON_COLOR = new Color(255,230,204);
   */

  public static Color getButtonColor(DIDLObject didlObject)
  {
    if (didlObject instanceof DIDLContainer)
    {
      if (didlObject.isRestricted())
      {
        return ButtonConstants.RED_BUTTON_COLOR;
      }

      return ButtonConstants.BUTTON_COLOR;
    } else
    {
      if (didlObject.isRestricted())
      {
        return ButtonConstants.RED_LIGHT_BUTTON_COLOR;
      }

      return ButtonConstants.BUTTON_COLOR;
    }
  }

}

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
import java.awt.MediaTracker;

import javax.swing.JFrame;

/**
 * @author tje
 * 
 */
public class ImageLoader
{

  public static void loadImages(JFrame topFrame)
  {
    MediaTracker mt = new MediaTracker(topFrame);

    Image bar = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BAR);
    mt.addImage(bar, 0);

    Image eject_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.EJECT_A);
    mt.addImage(eject_a, 0);

    Image eject_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.EJECT_F);
    mt.addImage(eject_f, 0);

    Image eject_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.EJECT_FA);
    mt.addImage(eject_fa, 0);

    Image eject_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.EJECT_N);
    mt.addImage(eject_n, 0);

    Image ff_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.FF_A);
    mt.addImage(ff_a, 0);

    Image ff_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.FF_F);
    mt.addImage(ff_f, 0);

    Image ff_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.FF_FA);
    mt.addImage(ff_fa, 0);

    Image ff_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.FF_N);
    mt.addImage(ff_n, 0);

    Image lissa_bg = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.LISSA_BG);
    mt.addImage(lissa_bg, 0);

    Image mute_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.MUTE_A);
    mt.addImage(mute_a, 0);

    Image mute_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.MUTE_F);
    mt.addImage(mute_f, 0);

    Image mute_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.MUTE_FA);
    mt.addImage(mute_fa, 0);

    Image mute_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.MUTE_N);
    mt.addImage(mute_n, 0);

    Image onoff_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.ONOFF_A);
    mt.addImage(onoff_a, 0);

    Image onoff_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.ONOFF_F);
    mt.addImage(onoff_f, 0);

    Image onoff_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.ONOFF_FA);
    mt.addImage(onoff_fa, 0);

    Image onoff_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.ONOFF_N);
    mt.addImage(onoff_n, 0);

    Image pause_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PAUSE_A);
    mt.addImage(pause_a, 0);

    Image pause_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PAUSE_F);
    mt.addImage(pause_f, 0);

    Image pause_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PAUSE_FA);
    mt.addImage(pause_fa, 0);

    Image pause_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PAUSE_N);
    mt.addImage(pause_n, 0);

    Image play_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PLAY_A);
    mt.addImage(play_a, 0);

    Image play_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PLAY_F);
    mt.addImage(play_f, 0);

    Image play_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PLAY_FA);
    mt.addImage(play_fa, 0);

    Image play_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.PLAY_N);
    mt.addImage(play_n, 0);

    Image rew_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.REW_A);
    mt.addImage(rew_a, 0);

    Image rew_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.REW_F);
    mt.addImage(rew_f, 0);

    Image rew_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.REW_FA);
    mt.addImage(rew_fa, 0);

    Image rew_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.REW_N);
    mt.addImage(rew_n, 0);

    Image skipback_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPBACK_F);
    mt.addImage(skipback_f, 0);

    Image skipback_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPBACK_FA);
    mt.addImage(skipback_fa, 0);

    Image skipback_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPBACK_N);
    mt.addImage(skipback_n, 0);

    Image skipnext_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPNEXT_F);
    mt.addImage(skipnext_f, 0);

    Image skipnext_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPNEXT_FA);
    mt.addImage(skipnext_fa, 0);

    Image skipnext_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.SKIPNEXT_N);
    mt.addImage(skipnext_n, 0);

    Image stop_a = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.STOP_A);
    mt.addImage(stop_a, 0);

    Image stop_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.STOP_F);
    mt.addImage(stop_f, 0);

    Image stop_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.STOP_FA);
    mt.addImage(stop_fa, 0);

    Image stop_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.STOP_N);
    mt.addImage(stop_n, 0);

    Image volumebar_bg = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOLUMEBAR_BG);
    mt.addImage(volumebar_bg, 0);

    Image voldown_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_DOWN_F);
    mt.addImage(voldown_f, 0);

    Image voldown_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_DOWN_FA);
    mt.addImage(voldown_fa, 0);

    Image voldown_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_DOWN_N);
    mt.addImage(voldown_n, 0);

    Image volup_f = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_UP_F);
    mt.addImage(volup_f, 0);

    Image volup_fa = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_UP_FA);
    mt.addImage(volup_fa, 0);

    Image volup_n = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VOL_UP_N);
    mt.addImage(volup_n, 0);

    // videoimages
    Image videoBack = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.VIDEOBACK);
    mt.addImage(videoBack, 0);

    Image brightDownF = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_DOWN_F);
    mt.addImage(brightDownF, 0);

    Image brightDownFA = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_DOWN_FA);
    mt.addImage(brightDownFA, 0);

    Image brightDownN = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_DOWN_N);
    mt.addImage(brightDownN, 0);

    Image brightUpF = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_UP_F);
    mt.addImage(brightUpF, 0);

    Image brightUpFA = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_UP_FA);
    mt.addImage(brightUpFA, 0);

    Image brightUpN = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.BRIGHT_UP_N);
    mt.addImage(brightUpN, 0);

    Image contDownF = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_DOWN_F);
    mt.addImage(contDownF, 0);

    Image contDownFA = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_DOWN_FA);
    mt.addImage(contDownFA, 0);

    Image contDownN = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_DOWN_N);
    mt.addImage(contDownN, 0);

    Image contUpF = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_UP_F);
    mt.addImage(contUpF, 0);

    Image contUpFA = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_UP_FA);
    mt.addImage(contUpFA, 0);

    Image contUpN = CPGUIConstants.getImage(CPGUIConstants.IMAGE_PATH + CPGUIConstants.CONT_UP_N);
    mt.addImage(contUpN, 0);

    try
    {
      mt.waitForAll();
    } catch (InterruptedException ie)
    {
      System.out.println(ie);
    }

    // put images to repository
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BAR, bar);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.EJECT_A, eject_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.EJECT_F, eject_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.EJECT_FA, eject_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.EJECT_N, eject_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.FF_A, ff_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.FF_F, ff_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.FF_FA, ff_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.FF_N, ff_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.LISSA_BG, lissa_bg);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.MUTE_A, mute_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.MUTE_F, mute_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.MUTE_FA, mute_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.MUTE_N, mute_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.ONOFF_A, onoff_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.ONOFF_F, onoff_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.ONOFF_FA, onoff_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.ONOFF_N, onoff_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PAUSE_A, pause_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PAUSE_F, pause_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PAUSE_FA, pause_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PAUSE_N, pause_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PLAY_A, play_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PLAY_F, play_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PLAY_FA, play_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.PLAY_N, play_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.REW_A, rew_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.REW_F, rew_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.REW_FA, rew_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.REW_N, rew_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPBACK_F, skipback_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPBACK_FA, skipback_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPBACK_N, skipback_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPNEXT_F, skipnext_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPNEXT_FA, skipnext_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.SKIPNEXT_N, skipnext_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.STOP_A, stop_a);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.STOP_F, stop_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.STOP_FA, stop_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.STOP_N, stop_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOLUMEBAR_BG, volumebar_bg);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_DOWN_F, voldown_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_DOWN_FA, voldown_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_DOWN_N, voldown_n);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_UP_F, volup_f);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_UP_FA, volup_fa);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VOL_UP_N, volup_n);

    // videoimages
    RepositoryCPGUI.putRendererImages(CPGUIConstants.VIDEOBACK, videoBack);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_DOWN_F, brightDownF);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_DOWN_FA, brightDownFA);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_DOWN_N, brightDownN);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_UP_F, brightUpF);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_UP_FA, brightUpFA);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.BRIGHT_UP_N, brightUpN);

    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_DOWN_F, contDownF);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_DOWN_FA, contDownFA);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_DOWN_N, contDownN);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_UP_F, contUpF);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_UP_FA, contUpFA);
    RepositoryCPGUI.putRendererImages(CPGUIConstants.CONT_UP_N, contUpN);
  }

}

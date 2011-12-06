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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core_av.control_point.CPAVTransportStateVariableAdapter;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;

/**
 * @author tje
 * 
 */
public class RendererControlGUI extends JPanel implements MouseListener, MouseMotionListener
{

  private static final long     serialVersionUID      = 1L;

  private static final String   EJECT                 = "eject";

  private static final String   SKIP_NEXT             = "skip_next";

  private static final String   SKIP_BACK             = "skip_back";

  private static final String   STOP                  = "stop";

  private static final String   ON_OFF                = "on_off";

  private static final String   REWIND                = "rewind";

  private static final String   PLAY                  = "play";

  private static final String   PAUSE                 = "pause";

  private static final String   FF                    = "ff";

  private static final String   MUTE                  = "mute";

  private static final String   VOL_UP                = "vol_up";

  private static final String   VOL_DOWN              = "vol_down";

  private static final String   BRIGHT_DOWN           = "bright_down";

  private static final String   BRIGHT_UP             = "bright_up";

  private static final String   CONTRAST_DOWN         = "contrast_down";

  private static final String   CONTRAST_UP           = "contrast_up";

  private RendererControlInit   parentFrame;

  private int                   mousePosX             = 0;

  private int                   mousePosY             = 0;

  private ImageHandler          ejectIH;

  private ImageHandler          skipNextIH;

  private ImageHandler          skipBackIH;

  private ImageHandler          stopIH;

  private ImageHandler          onOffIH;

  private ImageHandler          rewIH;

  private ImageHandler          playIH;

  private ImageHandler          pauseIH;

  private ImageHandler          ffIH;

  private ImageHandler          muteIH;

  private ImageHandler          volUpIH;

  private ImageHandler          volDownIH;

  // imagehanlder for videorenderer
  private ImageHandler          brightDownIH;

  private ImageHandler          brightUpIH;

  private ImageHandler          contDownIH;

  private ImageHandler          contUpIH;

  private Image                 volBar;

  private int                   volBarHeight;

  private int                   volBarWidth;

  private short                 barCounter            = 20;

  private short                 maxBars               = 20;

  private Vector                imageList             = new Vector();

  private String                songTitel             = "";

  private String                rendererName          = "";

  private int                   brightnessProz;

  private int                   contrastProz;

  private float                 changeFaktorBrightness;

  private float                 changeFaktorContrast;

  private short                 currentBrightness     = 0;

  private short                 currentContrast       = 0;

  private short                 maxBrightness         = 0;

  private short                 maxContrast           = 0;

  private MediaRendererCPDevice renderer;

  private LastChange            ls;

  private boolean               firstVolChange        = false;

  private boolean               firstBrightnessChange = false;

  private boolean               firstContrastChange   = false;

  private short                 maxVolFaktor          = 5;

  private short                 currentVolume         = 0;

  private int                   backStandartWidth     =
                                                        RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG)
                                                          .getWidth(null);

  private int                   backStandartHeight    =
                                                        RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG)
                                                          .getHeight(null);

  private int                   backVideoWidth        =
                                                        RepositoryCPGUI.getRendererImages(CPGUIConstants.VIDEOBACK)
                                                          .getWidth(null);

  private boolean               isVideoRenderer       = false;

  public RendererControlGUI(RendererControlInit parentFrame, MediaRendererCPDevice renderer)
  {
    this.parentFrame = parentFrame;
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.renderer = renderer;
    isVideoRenderer = true;

    if (isVideoRenderer)
    {

      maxBrightness = renderer.getBrightnessMax();
      maxContrast = renderer.getContrastMax();
      currentBrightness = renderer.getCurrentBrightness();
      currentContrast = renderer.getCurrentContrast();
      if (maxBrightness == 0)
      {
        brightnessProz = 0;
      } else
      {
        brightnessProz = currentBrightness * 100 / maxBrightness;
      }
      if (maxContrast == 0)
      {
        contrastProz = 0;
      } else
      {
        contrastProz = currentContrast * 100 / maxContrast;
      }
      changeFaktorBrightness = (float)maxBrightness / 100;
      changeFaktorContrast = (float)maxContrast / 100;
    }

    maxVolFaktor = (short)(renderer.getVolMax() / barCounter);
    if (maxVolFaktor == 0)
    {
      maxVolFaktor = 1;
      maxBars = renderer.getVolMax();
    }

    currentVolume = renderer.getVolume("Master");
    barCounter = (short)(currentVolume / maxVolFaktor);
    setImageHandler();
    rendererName = this.renderer.getRendererDevice().getFriendlyName();
    ls = new LastChange(this.renderer);
  }

  private void setImageHandler()
  {
    volBar = RepositoryCPGUI.getRendererImages(CPGUIConstants.BAR);
    volBarHeight = volBar.getHeight(null);
    volBarWidth = volBar.getWidth(null);
    ejectIH =
      new ImageHandler(EJECT,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.EJECT_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.EJECT_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.EJECT_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.EJECT_A),
        CPGUIConstants.xPosEject,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(ejectIH);

    skipNextIH =
      new ImageHandler(SKIP_NEXT,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPNEXT_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPNEXT_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPNEXT_FA),
        CPGUIConstants.xPosSkipNext,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(skipNextIH);

    skipBackIH =
      new ImageHandler(SKIP_BACK,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPBACK_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPBACK_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.SKIPBACK_FA),
        CPGUIConstants.xPosSkipBack,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(skipBackIH);

    stopIH =
      new ImageHandler(STOP,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.STOP_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.STOP_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.STOP_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.STOP_A),
        CPGUIConstants.xPosStop,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(stopIH);

    onOffIH =
      new ImageHandler(ON_OFF,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.ONOFF_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.ONOFF_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.ONOFF_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.ONOFF_A),
        CPGUIConstants.xPosOnOff,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(onOffIH);

    rewIH =
      new ImageHandler(REWIND,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.REW_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.REW_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.REW_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.REW_A),
        CPGUIConstants.xPosRew,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(rewIH);

    playIH =
      new ImageHandler(PLAY,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PLAY_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PLAY_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PLAY_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PLAY_A),
        CPGUIConstants.xPosPlay,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(playIH);

    pauseIH =
      new ImageHandler(PAUSE,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PAUSE_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PAUSE_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PAUSE_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.PAUSE_A),
        CPGUIConstants.xPosPause,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(pauseIH);

    ffIH =
      new ImageHandler(FF,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.FF_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.FF_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.FF_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.FF_A),
        CPGUIConstants.xPosFF,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(ffIH);

    muteIH =
      new ImageHandler(MUTE,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.MUTE_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.MUTE_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.MUTE_FA),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.MUTE_A),
        CPGUIConstants.xPosMute,
        CPGUIConstants.yPosButtons,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(muteIH);

    volUpIH =
      new ImageHandler(VOL_UP,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_UP_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_UP_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_UP_FA),
        CPGUIConstants.xPosVolUp,
        CPGUIConstants.yPosVol,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(volUpIH);

    volDownIH =
      new ImageHandler(VOL_DOWN,
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_DOWN_N),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_DOWN_F),
        RepositoryCPGUI.getRendererImages(CPGUIConstants.VOL_DOWN_FA),
        CPGUIConstants.xPosVolDown,
        CPGUIConstants.yPosVol,
        CPGUIConstants.buttonHeight,
        CPGUIConstants.buttonWidth,
        "");
    imageList.add(volDownIH);

    if (isVideoRenderer)
    {
      brightDownIH =
        new ImageHandler(BRIGHT_DOWN,
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_DOWN_N),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_DOWN_F),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_DOWN_FA),
          CPGUIConstants.xPosBrightDown,
          CPGUIConstants.yPosButtons,
          CPGUIConstants.buttonHeight,
          CPGUIConstants.buttonWidth,
          "");
      imageList.add(brightDownIH);
      brightUpIH =
        new ImageHandler(BRIGHT_UP,
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_UP_N),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_UP_F),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.BRIGHT_UP_FA),
          CPGUIConstants.xPosBrightUp,
          CPGUIConstants.yPosButtons,
          CPGUIConstants.buttonHeight,
          CPGUIConstants.buttonWidth,
          "");
      imageList.add(brightUpIH);
      contUpIH =
        new ImageHandler(CONTRAST_UP,
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_UP_N),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_UP_F),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_UP_FA),
          CPGUIConstants.xPosContrastUp,
          CPGUIConstants.yPosButtons,
          CPGUIConstants.buttonHeight,
          CPGUIConstants.buttonWidth,
          "");
      imageList.add(contUpIH);
      contDownIH =
        new ImageHandler(CONTRAST_DOWN,
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_DOWN_N),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_DOWN_F),
          RepositoryCPGUI.getRendererImages(CPGUIConstants.CONT_DOWN_FA),
          CPGUIConstants.xPosContrastDown,
          CPGUIConstants.yPosButtons,
          CPGUIConstants.buttonHeight,
          CPGUIConstants.buttonWidth,
          "");
      imageList.add(contDownIH);
    }
  }

  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    g.drawImage(RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG),
      0,
      0,
      backStandartWidth,
      backStandartHeight,
      this);

    if (isVideoRenderer)
    {
      g.drawImage(RepositoryCPGUI.getRendererImages(CPGUIConstants.VIDEOBACK),
        backStandartWidth,
        0,
        backVideoWidth * 3,
        backStandartHeight,
        this);

      Font font = new Font("ARIAL", Font.BOLD, 15);
      g.setFont(font);
      g.setColor(new Color(101, 217, 255));
      drawBrightness(g);
      drawContrast(g);
    }

    drawInitImages(g);
    drawSong(g);
    drawRendererName(g);
    drawVolBar(g);
  }

  private void drawVolBar(Graphics g)
  {
    for (int i = 0; i < barCounter; ++i)
    {
      g.drawImage(volBar, 492 + i * 4, 29, volBarWidth, volBarHeight, this);
    }
  }

  private void drawBrightness(Graphics g)
  {
    g.drawString(brightnessProz + "%", 753, 75);
  }

  private void drawContrast(Graphics g)
  {
    g.drawString(contrastProz + "%", 847, 75);
  }

  private void drawSong(Graphics g)
  {
    if (songTitel != null)
    {
      Font font = new Font("ARIAL", Font.BOLD, 15);
      g.setFont(font);
      g.setColor(new Color(101, 217, 255));

      FontMetrics fm = g.getFontMetrics(font);
      g.drawString(songTitel, RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG).getWidth(null) / 2 -
        fm.stringWidth(songTitel) / 2, 75);
    }
  }

  private void drawRendererName(Graphics g)
  {
    Font font = new Font("ARIAL", Font.BOLD, 15);
    g.setFont(font);
    g.setColor(new Color(118, 112, 112));

    FontMetrics fm = g.getFontMetrics(font);
    g.drawString(rendererName, RepositoryCPGUI.getRendererImages(CPGUIConstants.LISSA_BG).getWidth(null) / 2 -
      fm.stringWidth(rendererName) / 2, 98);
  }

  private void drawInitImages(Graphics g)
  {
    for (int i = 0; i < imageList.size(); ++i)
    {
      ((ImageHandler)imageList.get(i)).paint(g);
    }
  }

  private void checkFunction(ImageHandler ih)
  {
    String function = ih.getFunction();

    if (function.equals(EJECT))
    {
      // setActiveImage(function);
      // DEGUB
      renderer.getTransportSettings();
    } else if (function.equals(SKIP_NEXT))
    {
      setSkip(ih);
    } else if (function.equals(SKIP_BACK))
    {
      setSkip(ih);
    } else if (function.equals(STOP))
    {
      renderer.stop();
      setStop();
    } else if (function.equals(ON_OFF))
    {
      // setActiveImage(function);
    } else if (function.equals(REWIND))
    {
      setRewind();
    } else if (function.equals(PLAY))
    {
      setPlay();
    } else if (function.equals(PAUSE))
    {
      setPause();
    } else if (function.equals(FF))
    {
      setFF();
    } else if (function.equals(MUTE))
    {
      setMuteImage();
    } else if (function.equals(VOL_UP))
    {
      setVolUp();
    } else if (function.equals(VOL_DOWN))
    {
      setVolDown();
    } else if (function.equals(BRIGHT_DOWN))
    {
      setBrightDown();
    } else if (function.equals(BRIGHT_UP))
    {
      setBrightUp();
    } else if (function.equals(CONTRAST_DOWN))
    {
      setContrastDown();
    } else if (function.equals(CONTRAST_UP))
    {
      setContrastUp();
    }
  }

  void setContrastUp()
  {
    if (contrastProz < 100)
    {
      changeContrastProz((int)(1 / changeFaktorContrast));
    }

    contUpIH.setActiveFocusImage();
    paintNow(contUpIH);
    contUpIH.setDefaultImage();
  }

  void setContrastDown()
  {
    if (contrastProz > 0)
    {
      changeContrastProz((int)(-1 / changeFaktorContrast));
    }

    contDownIH.setActiveFocusImage();
    paintNow(contDownIH);
    contDownIH.setDefaultImage();
  }

  void setBrightUp()
  {
    if (brightnessProz < 100)
    {
      changeBrightnessProz((int)(1 / changeFaktorBrightness));
    }

    brightUpIH.setActiveFocusImage();
    paintNow(brightUpIH);
    brightUpIH.setDefaultImage();
  }

  void setBrightDown()
  {
    if (brightnessProz > 0)
    {
      changeBrightnessProz((int)(-1 / changeFaktorBrightness));
    }

    brightDownIH.setActiveFocusImage();
    paintNow(brightDownIH);
    brightDownIH.setDefaultImage();
  }

  private void paintNow(ImageHandler ih)
  {
    paintImmediately(ih.getImagePosX(), ih.getImagePosY(), ih.getImageWidth(), ih.getImageHeigth());

    try
    { // only for visibility purposes
      Thread.sleep(50);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  void setVolDown()
  {
    if (barCounter > 0)
    {
      changeVolBar(-1);
    }

    volDownIH.setActiveFocusImage();
    paintNow(volDownIH);
    volDownIH.setDefaultImage();
  }

  void setVolUp()
  {
    if (barCounter < maxBars)
    {
      changeVolBar(1);
    }

    volUpIH.setActiveFocusImage();
    paintNow(volUpIH);
    volUpIH.setDefaultImage();
  }

  void setMuteImage()
  {
    if (ls.isMuted)
    {
      renderer.setMute("Master", false);
      muteIH.setDefaultImage();

      // streamium
      ls.isMuted = false;
    } else
    {
      renderer.setMute("Master", true);
      muteIH.setActiveImage();

      // streamium
      ls.isMuted = true;
    }
  }

  void setPause()
  {
    boolean isPause = false;

    if (!ls.getTransportState().equals("PAUSED_PLAYBACK"))
    {
      renderer.pause();
      isPause = true;
    } else
    {
      renderer.resume();
    }

    setPauseImages(isPause);
  }

  void setPauseImages(boolean isPause)
  {
    if (isPause)
    {
      pauseIH.setActiveImage();
      playIH.setDefaultImage();
      stopIH.setDefaultImage();
    } else
    {
      setPlayImages(true);
    }
  }

  void setPlay()
  {
    boolean isPlay = false;

    if (ls.getTransportState().equals("PAUSED_PLAYBACK"))
    {
      renderer.resume();
      isPlay = true;
    } else if (ls.getTransportState().equals("PLAYING"))
    {
      isPlay = true;
    } else
    {
      songTitel = "No Media";
      repaint();
    }

    setPlayImages(isPlay);
  }

  void setPlayImages(boolean isPlay)
  {
    if (isPlay)
    {
      playIH.setActiveImage();
      stopIH.setDefaultImage();
      pauseIH.setDefaultImage();
    } else
    {
      playIH.setDefaultImage();
    }
  }

  void setFF()
  {
    rewIH.setDefaultImage();
    ffIH.setActiveImage();
  }

  void setRewind()
  {
    rewIH.setActiveImage();
    ffIH.setDefaultImage();
  }

  void setStop()
  {
    stopIH.setActiveImage();
    playIH.setDefaultImage();
    pauseIH.setDefaultImage();
    ffIH.setDefaultImage();
    rewIH.setDefaultImage();
  }

  void setSkip(ImageHandler ih)
  {
    ih.setActiveFocusImage();
    paintNow(ih);
    ih.setFocusImage();
  }

  private void changeBrightnessProz(int change)
  {
    brightnessProz += change;

    if (!firstBrightnessChange)
    {
      firstBrightnessChange = true;

      SetLastChangeTimerBrightness timer = new SetLastChangeTimerBrightness();
      timer.start();
    }
  }

  private void changeContrastProz(int change)
  {
    contrastProz += change;

    if (!firstContrastChange)
    {
      firstContrastChange = true;

      SetLastChangeTimerContrast timer = new SetLastChangeTimerContrast();
      timer.start();
    }
  }

  private void changeVolBar(int change)
  {
    barCounter += change;

    if (!firstVolChange)
    {
      firstVolChange = true;

      SetLastChangeTimerVolume timer = new SetLastChangeTimerVolume();
      timer.start();
    }
  }

  // **MOUSE METHODS

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent arg0)
  {
    mousePosX = arg0.getX();
    mousePosY = arg0.getY();

    for (int i = 0; i < imageList.size(); ++i)
    {
      if (((ImageHandler)imageList.get(i)).mouseClicked(mousePosX, mousePosY))
      {
        checkFunction((ImageHandler)imageList.get(i));
      }
    }

    this.repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent arg0)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent arg0)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent arg0)
  {
    mousePosX = arg0.getX();
    mousePosY = arg0.getY();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent arg0)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(MouseEvent arg0)
  {
    Point p = parentFrame.getLocation();
    parentFrame.setLocation(p.x + arg0.getX() - mousePosX, p.y + arg0.getY() - mousePosY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  public void mouseMoved(MouseEvent arg0)
  {
    int posX = arg0.getX();
    int posY = arg0.getY();
    boolean over = false;

    for (int i = 0; i < imageList.size(); ++i)
    {
      if (((ImageHandler)imageList.get(i)).mouseOver(posX, posY))
      {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        over = true;
      }
    }

    if (!over)
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    this.repaint();
  }

  private void changeBrightness()
  {
    renderer.setBrightness((short)(brightnessProz * changeFaktorBrightness));
  }

  private void changeContrast()
  {
    renderer.setContrast((short)(contrastProz * changeFaktorContrast));
  }

  private void changeVol()
  {
    /*
     * For us not necessary because we set Master channel Enumeration e = ls.volInfo.keys();
     * 
     * while (e.hasMoreElements()) { String argName = (String) e.nextElement();
     * renderer.setVolume(argName, barCounter*5); }
     */
    renderer.setVolume("Master", (short)(barCounter * maxVolFaktor));
  }

  /**
   * used for volume change delay, if user clickes rapidly on vol change therefore only on Volume
   * change action is called in one second
   */
  class SetLastChangeTimerVolume extends Thread
  {
    public void run()
    {
      long timer = 1000; // in ms

      try
      {
        sleep(timer);
        firstVolChange = false;
        changeVol();
      } catch (InterruptedException e)
      {
      }
    }
  }

  /**
   * used for brightness change delay, if user clickes rapidly on change therefore only on change
   * action is called in one second
   */
  class SetLastChangeTimerBrightness extends Thread
  {
    public void run()
    {
      long timer = 1000; // in ms

      try
      {
        sleep(timer);
        firstBrightnessChange = false;
        changeBrightness();
      } catch (InterruptedException e)
      {
      }
    }
  }

  /**
   * used for contrast change delay, if user clickes rapidly on change therefore only on change
   * action is called in one second
   */
  class SetLastChangeTimerContrast extends Thread
  {
    public void run()
    {
      long timer = 1000; // in ms

      try
      {
        sleep(timer);
        firstContrastChange = false;
        changeContrast();
      } catch (InterruptedException e)
      {
      }
    }
  }

  class LastChange extends CPAVTransportStateVariableAdapter
  {
    MediaRendererCPDevice renderer;

    String                creator        = "";

    String                title          = "";

    String                transportState = "";

    boolean               isMuted        = false;

    // Hashtable volInfo = new Hashtable();

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setTransportState(java.lang.String)
     */
    public LastChange(MediaRendererCPDevice renderer)
    {
      this.renderer = renderer;
      // this.renderer.addListener(this);

      // streamium
      isMuted = renderer.getMute("Master");

      if (isMuted)
      {
        muteIH.setActiveImage();
      } else
      {
        muteIH.setDefaultImage();
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setBrightness(short)
     */
    public void setBrightness(short brightness)
    {
      brightnessProz = brightness * 100 / maxBrightness;
      repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setContrast(short)
     */
    public void setContrast(short contrast)
    {
      contrastProz = contrast * 100 / maxContrast;
      repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setMute(boolean,
     *      java.lang.String)
     */
    public void setMute(boolean muted, String channel)
    {
      isMuted = muted;

      if (isMuted)
      {
        muteIH.setActiveImage();
      } else
      {
        muteIH.setDefaultImage();
      }

      repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setVolume(long)
     */
    public void setVolume(short newVolume, String channel)
    {
      /*
       * For us not necessary because we set Master channel // it is possible to tune different
       * Speaker Channels, but not with our GUI Long valVolume = new Long(newVolume);
       * 
       * if (!volInfo.contains(channel)) { volInfo.put(channel, valVolume); }
       */
      barCounter = (short)(newVolume / maxVolFaktor);
      repaint();
    }

    public String getTransportState()
    {
      return transportState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setTransportState(java.lang.String)
     */
    public void setTransportState(String newTransportState)
    {
      transportState = newTransportState;

      if (newTransportState.equals("STOPPED"))
      {
        songTitel = newTransportState;
        setStop();
      } else if (newTransportState.equals("PAUSED_PLAYBACK"))
      {
        songTitel = newTransportState + " : " + title;
        setPauseImages(true);
      } else if (newTransportState.equals("PLAYING"))
      {
        songTitel = title;
        setPlayImages(true);
      } else if (newTransportState.equals("TRANSITIONING"))
      {
        songTitel = newTransportState;
      }

      repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaRenderLastChangeListener#setCurrentTrackMetaData(java.lang.String)
     */
    public void setCurrentTrackMetaData(DIDLItem newCurrentTrackMetaData)
    {
      creator = newCurrentTrackMetaData.getCreator();
      title = newCurrentTrackMetaData.getTitle();

      if (creator != null && !creator.equals("null") && title != null && !title.equals("null"))
      {
        songTitel = creator + " - " + title;
      } else if (title != null && !title.equals("null"))
      {
        songTitel = title;
      }

      repaint();
    }
  }
}

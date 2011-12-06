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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.IDIDLResourceProvider;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.control_point.AVTransportCPService;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPAVTransportStateVariableListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPRenderingControlStateVariableListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLConstants;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothArea;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothCommandButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothSliderValueButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for media renderers to get informations and to control media playback.
 * 
 * @author Alexander Koenig
 */
public class MediaRendererPlugin extends BaseCPDevicePlugin implements
  ICPAVTransportStateVariableListener,
  ICPRenderingControlStateVariableListener
{
  private static final long       serialVersionUID  = 1L;

  public static String            PLUGIN_TYPE       = UPnPAVConstant.MEDIA_RENDERER_DEVICE_TYPE;

  public static final int         ITEM_BUTTON_WIDTH = 600;

  private SmoothCommandButton     playButton;

  private SmoothCommandButton     stopButton;

  private SmoothCommandButton     pauseButton;

  private SmoothCommandButton     nextButton;

  private SmoothCommandButton     previousButton;

  private SmoothCommandButton     setURIButton;

  private SmoothButton            friendlyNameButton;

  private SmoothSliderValueButton volumeButton;

  private int                     resourceMediaType = DIDLConstants.RES_TYPE_UNKNOWN;

  private String                  resourceURI       = null;

  private long                    currentTrack      = 0;

  private long                    numberOfTracks    = 0;

  private SmoothArea              infoArea;

  private SmoothButton            currentTrackButton;

  private SmoothValueButton       transportStateButton;

  private SmoothValueButton       transportStatusButton;

  private SmoothValueButton       storageMediumButton;

  private SmoothValueButton       currentTransportActionsButton;

  /** Associated media renderer device */
  private MediaRendererCPDevice   mediaRenderer     = null;

  /** Provider for AVTransport URIs */
  private IDIDLResourceProvider   resourceProvider  = null;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();

    initComponents();
    // init control panel
    setURIButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandSetURI",
        SmoothCommandButton.COMMAND_SET_URI);
    previousButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandPrevious",
        SmoothCommandButton.COMMAND_PREVIOUS);
    stopButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandStop",
        SmoothCommandButton.COMMAND_STOP);
    playButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandPlay",
        SmoothCommandButton.COMMAND_PLAY);
    pauseButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandPause",
        SmoothCommandButton.COMMAND_PAUSE);
    nextButton =
      new SmoothCommandButton(new Dimension(ButtonConstants.BUTTON_HEIGHT, ButtonConstants.BUTTON_HEIGHT),
        12,
        "CommandNext",
        SmoothCommandButton.COMMAND_NEXT);

    volumeButton = new SmoothSliderValueButton(new Dimension(165, 30), 90, 115, 12, "Lautstärke", 0, "volume");
    volumeButton.setMinValue(0);
    volumeButton.setMaxValue(9);
    volumeButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

    setURIButton.addActionListener(this);
    previousButton.addActionListener(this);
    stopButton.addActionListener(this);
    playButton.addActionListener(this);
    pauseButton.addActionListener(this);
    nextButton.addActionListener(this);
    volumeButton.addActionListener(this);

    infoArea = new SmoothArea(new Dimension(ITEM_BUTTON_WIDTH, 300), 12, null);
    currentTrackButton =
      new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    transportStateButton =
      new SmoothValueButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "TransportZustand:",
        "",
        null);
    transportStatusButton =
      new SmoothValueButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "TransportStatus:",
        "",
        null);
    storageMediumButton =
      new SmoothValueButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Speichermedium:",
        "",
        null);
    currentTransportActionsButton =
      new SmoothValueButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Mögliche Aktionen:",
        "",
        null);

    friendlyNameButton = new SmoothButton(new Dimension(270, ButtonConstants.BUTTON_HEIGHT), 12, "", null);

    setURIButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    previousButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    stopButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    playButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    pauseButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    nextButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    infoArea.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jControlPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jInfoPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    friendlyNameButton.setBackground(ButtonConstants.BACKGROUND_COLOR);
    volumeButton.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // initialize panel layout
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#runPlugin()
   */
  public void startPlugin()
  {
    super.startPlugin();

    // create media server device
    mediaRenderer = new MediaRendererCPDevice(controlPoint, device);
    mediaRenderer.setAVTransportStateVariableListener(this);
    mediaRenderer.setRenderingControlStateVariableListener(this);

    // create AV transport view to receive events
    new AVTransportCPService(mediaRenderer, this);

    friendlyNameButton.setText(device.getFriendlyName() + " - " +
      device.getDeviceDescriptionSocketAddress().getHostName());
    // translate name button if possible
    if (deviceGUIContext != null)
    {
      deviceGUIContext.getDeviceTranslations().setTranslationForButton(friendlyNameButton,
        device.getFriendlyName(),
        " - " + device.getDeviceDescriptionSocketAddress().getHostName());
    }
    volumeButton.setCurrentValue(mediaRenderer.getVolume(UPnPAVConstant.VALUE_MASTER));

    Vector fileFormats = mediaRenderer.getSupportedGetFileFormats();

    Hashtable fileFormatHashtable = new Hashtable();

    infoArea.addLine("Unterstützte Formate für HTTP GET");
    // go through all complete formats
    for (int i = 0; i < fileFormats.size(); i++)
    {
      String currentFormat = (String)fileFormats.elementAt(i);
      // divide into audio/mpeg
      try
      {
        int dividerIndex = currentFormat.indexOf("/");
        if (dividerIndex != -1)
        {
          String mediaType = currentFormat.substring(0, dividerIndex);
          String subType = currentFormat.substring(dividerIndex + 1);
          if (fileFormatHashtable.containsKey(mediaType))
          {
            ((Vector)fileFormatHashtable.get(mediaType)).add(subType);
          } else
          {
            Vector subTypeList = new Vector();
            subTypeList.add(subType);
            fileFormatHashtable.put(mediaType, subTypeList);
          }
        }
      } catch (Exception e)
      {
      }
    }
    Enumeration mediaTypes = fileFormatHashtable.keys();
    while (mediaTypes.hasMoreElements())
    {
      String currentMediaType = (String)mediaTypes.nextElement();
      Vector subTypeList = (Vector)fileFormatHashtable.get(currentMediaType);
      String subTypeString = "";
      for (int i = 0; i < subTypeList.size(); i++)
      {
        subTypeString += (subTypeString.length() != 0 ? ", " : "") + (String)subTypeList.elementAt(i);
      }
      infoArea.addLine("  " + currentMediaType + ": " + subTypeString);
    }
    infoArea.setSizeToFitContent();
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#getPluginType()
   */
  public String getPluginType()
  {
    return PLUGIN_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#showPlugin()
   */
  public void pluginShown()
  {
    super.pluginShown();

    // reset resource provider
    resourceProvider = null;

    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    add(jControlPanel, gridBagConstraints);

    updateControlPanelButtons();
  }

  public void updateStateVariable(CPStateVariable csv)
  {

  }

  /**
   * Retrieves the media renderer device.
   * 
   * @return The media renderer device
   */
  public MediaRendererCPDevice getMediaRendererCPDevice()
  {
    return mediaRenderer;
  }

  /** Retrieves the control panel of this plugin */
  public JPanel getControlPanel()
  {
    return jControlPanel;
  }

  /**
   * Sets the AVTransportURIProvider.
   * 
   * @param resourceProvider
   *          The AVTransportURIProvider to set
   */
  public void setResourceProvider(IDIDLResourceProvider uriProvider)
  {
    this.resourceProvider = uriProvider;
    updateControlPanelButtons();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (setURIButton.getID().equals(e.getActionCommand()) && resourceProvider != null)
    {
      DIDLResource resource = resourceProvider.getSelectedResource();
      if (resource != null)
      {
        resourceMediaType = resource.getMediaType();
        String uri = resource.getValue();
        if (uri != null && uri.length() > 0)
        {
          resourceURI = uri;
          mediaRenderer.setAVTransportURI(uri, "");

          updateControlPanelButtons();
        }
      }
    }
    if (previousButton.getID().equals(e.getActionCommand()))
    {
      mediaRenderer.stop();
    }
    if (stopButton.getID().equals(e.getActionCommand()))
    {
      mediaRenderer.stop();
    }
    if (playButton.getID().equals(e.getActionCommand()))
    {
      mediaRenderer.play();
    }
    if (pauseButton.getID().equals(e.getActionCommand()))
    {
      mediaRenderer.pause();
    }
    if (volumeButton.getID().equals(e.getActionCommand()))
    {
      System.out.println("Performed set volume action");
      mediaRenderer.setVolume(UPnPAVConstant.VALUE_MASTER, (short)volumeButton.getCurrentValue());
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jToActionsDividerPanel = new javax.swing.JPanel();
    jToContentDividerPanel = new javax.swing.JPanel();
    jInfoPanel = new javax.swing.JPanel();
    jControlPanel = new javax.swing.JPanel();

    setLayout(new java.awt.GridBagLayout());

    setBackground(new java.awt.Color(204, 204, 255));
    jToActionsDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jToActionsDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jToActionsDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jToActionsDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    add(jToActionsDividerPanel, gridBagConstraints);

    jToContentDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jToContentDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jToContentDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jToContentDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    add(jToContentDividerPanel, gridBagConstraints);

    jInfoPanel.setLayout(new java.awt.GridBagLayout());

    jInfoPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    add(jInfoPanel, gridBagConstraints);

    jControlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jControlPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    add(jControlPanel, gridBagConstraints);

  }// GEN-END:initComponents

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    jControlPanel.removeAll();
    jControlPanel.add(friendlyNameButton);
    jControlPanel.add(volumeButton);
    jControlPanel.add(setURIButton);
    jControlPanel.add(previousButton);
    jControlPanel.add(stopButton);
    jControlPanel.add(playButton);
    jControlPanel.add(pauseButton);
    jControlPanel.add(nextButton);

    jInfoPanel.removeAll();
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jInfoPanel.add(infoArea, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jInfoPanel.add(currentTrackButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jInfoPanel.add(transportStateButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jInfoPanel.add(transportStatusButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jInfoPanel.add(storageMediumButton, gridBagConstraints);

    jControlPanel.invalidate();
    jInfoPanel.invalidate();
    validateTree();

    updateControlPanelButtons();

    jControlPanel.repaint();
    jInfoPanel.repaint();
    repaint();
  }

  /** Updates the state of all control buttons */
  private void updateControlPanelButtons()
  {
    // show buttons dependent on the current resource
    if (resourceMediaType == DIDLConstants.RES_TYPE_AUDIO || resourceMediaType == DIDLConstants.RES_TYPE_VIDEO)
    {
      volumeButton.setSelectable(true);
      previousButton.setSelectable(true);
      pauseButton.setSelectable(true);
      nextButton.setSelectable(true);
    } else
    {
      volumeButton.setSelectable(false);
      previousButton.setSelectable(false);
      pauseButton.setSelectable(false);
      nextButton.setSelectable(false);
    }
    volumeButton.setSelectable(true);
    setURIButton.setSelectable(resourceProvider != null);
    if (resourceURI != null)
    {
      stopButton.setSelectable(mediaRenderer.canStop());
      playButton.setSelectable(mediaRenderer.canPlay());
    } else
    {
      stopButton.setSelectable(false);
      playButton.setSelectable(false);
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jControlPanel;

  private javax.swing.JPanel jInfoPanel;

  private javax.swing.JPanel jToActionsDividerPanel;

  private javax.swing.JPanel jToContentDividerPanel;

  // End of variables declaration//GEN-END:variables

  public void transportStateChanged(long instanceID, String newTransportState)
  {
    transportStateButton.setValue(newTransportState);
    updateControlPanelButtons();
  }

  public void transportStatusChanged(long instanceID, String newTransportStatus)
  {
    transportStatusButton.setValue(newTransportStatus);
  }

  public void playbackStorageMediumChanged(long instanceID, String newPlaybackStorageMedium)
  {
    storageMediumButton.setValue(newPlaybackStorageMedium);
  }

  public void possiblePlaybackStorageMediaChanged(long instanceID, String newPossiblePlaybackStorageMedia)
  {
  }

  public void currentPlayModeChanged(long instanceID, String newCurrentPlayMode)
  {
  }

  public void transportPlaySpeedChanged(long instanceID, String newTransportPlaySpeed)
  {
  }

  public void numberOfTracksChanged(long instanceID, long newNumberOfTracks)
  {
    numberOfTracks = newNumberOfTracks;
    currentTrackButton.setText(currentTrack + "/" + numberOfTracks + ". " + resourceURI);
  }

  public void currentTrackChanged(long instanceID, long newCurrentTrack)
  {
    currentTrack = newCurrentTrack;
    currentTrackButton.setText(currentTrack + "/" + numberOfTracks + ". " + resourceURI);
  }

  public void currentTrackDurationChanged(long instanceID, String newCurrentTrackDuration)
  {

  }

  public void currentMediaDurationChanged(long instanceID, String newCurrentMediaDuration)
  {
  }

  public void currentTrackMetaDataChanged(long instanceID, String newCurrentTrackMetaData)
  {
  }

  public void currentTrackURIChanged(long instanceID, String newCurrentTrackURI)
  {
    resourceURI = newCurrentTrackURI;
    currentTrackButton.setText(currentTrack + "/" + numberOfTracks + ". " + resourceURI);
    updateControlPanelButtons();
  }

  public void avTransportURIChanged(long instanceID, String newURI)
  {
    // resourceURI = newURI;
    // currentTrackButton.setText(currentTrack + "/" + numberOfTracks + ". " + resourceURI);
  }

  public void avTransportURIMetaDataChanged(long instanceID, String newURIMetaData)
  {

  }

  public void currentTransportActionsChanged(long instanceID, String newCurrentTransportActions)
  {
    currentTransportActionsButton.setValue(newCurrentTransportActions);
    updateControlPanelButtons();
  }

  public void brightnessChanged(long instanceID, short brightness)
  {

  }

  public void volumeChanged(long instanceID, String channel, short newVolume)
  {
    if (channel.equals(UPnPAVConstant.VALUE_MASTER))
    {
      System.out.println("Master volume changed to " + newVolume);
      volumeButton.setSelectable(true);
      volumeButton.setCurrentValue(newVolume);
    }
  }

}

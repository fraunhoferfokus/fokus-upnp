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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.IDeviceGUIContextProvider;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels.JMediaRendererPanel;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPMediaServerStateVariableListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaServerCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLItem;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLMusicTrack;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLResource;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used for media servers to browse the content structure and to connect the selected
 * item to found media renderers.
 * 
 * @author Alexander Koenig
 */
public class MediaServerPlugin extends BaseCPDevicePlugin implements ICPMediaServerStateVariableListener
{
  private static final long     serialVersionUID           = 1L;

  public static String          PLUGIN_TYPE                = UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE;

  public static final int       ITEM_BUTTON_WIDTH          = 300;

  public static final int       ACTION_BUTTON_WIDTH        = 200;

  private JPanel                jPathFillPanel             = new JPanel();

  private JPanel                jDirectoryContentFillPanel = new JPanel();

  private JPanel                itemPropertiesFillPanel    = new JPanel();

  private JPanel                contentFillPanel           = new JPanel();

  private SmoothButton          pathButton;

  private SmoothButton          toParentButton;

  private SmoothButton          newFileButton;

  private SmoothButton          newFolderButton;

  private SmoothButton          importResourceButton;

  private SmoothButton          destroyObjectButton;

  private JPanel                jActionFillPanel           = new JPanel();

  private SmoothValueButton     itemTitleButton;

  private SmoothValueButton     itemResourceButton;

  private SmoothValueButton     itemResourceSizeButton;

  private SmoothValueButton     itemResourceImportURIButton;

  private SmoothValueButton     itemResourceProtocolInfoButton;

  private SmoothValueButton     itemClassButton;

  private SmoothValueButton     itemCreatorButton;

  private SmoothValueButton     itemRestrictedButton;

  private SmoothValueButton     itemWriteStatusButton;

  // SmoothButtons for all DIDL objects
  private Vector                didlObjectButtonList       = new Vector();

  private DIDLItem              selectedItem               = null;

  private MediaServerCPDevice   mediaServer                = null;

  // Panel for preview
  private JMediaRendererPanel   mediaRendererPanel;

  private MediaRendererCPDevice mediaRendererCPDevice;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    initComponents();

    toParentButton =
      new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT), 12, "..", "commandToParent");
    toParentButton.setButtonColor(new Color(180, 180, 180));
    toParentButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    toParentButton.addActionListener(this);

    itemClassButton = new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Klasse:", "", null);
    itemCreatorButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Schöpfer:", "", null);
    itemResourceButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Ressource:", "", null);
    itemResourceSizeButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Größe:", "", null);
    itemResourceImportURIButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "ImportURI:", "", null);
    itemResourceProtocolInfoButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "ProtokollInfo:", "", null);
    itemRestrictedButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Beschränkt:", "", null);
    itemTitleButton = new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Titel:", "", null);
    itemWriteStatusButton =
      new SmoothValueButton(new Dimension(400, ButtonConstants.BUTTON_HEIGHT), 12, "Schreibstatus:", "", null);

    jPathPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jPathFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDirectoryContentFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    itemPropertiesFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    contentFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jContentDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jToActionsDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jToContentDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDirectoryContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    itemPropertiesPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jInfoPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    itemPreviewPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    itemPropertiesScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDirectoryContentScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);

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
    mediaServer = new MediaServerCPDevice(controlPoint, device);
    mediaServer.addServerChangeListener(this);

    initActionPanel();
    initPathPanel();

    updateContainerContentButtonList();
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#showPlugin()
   */
  public void pluginShown()
  {
    super.pluginShown();

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

  /**
   * Connects the internal image renderer to the server plugin to allow an image preview.
   * 
   * @param device
   * @param previewPanel
   */
  public void setMediaRendererForPreview(MediaRendererCPDevice device, JPanel previewPanel)
  {
    mediaRendererCPDevice = device;
    previewPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    itemPreviewPanel.removeAll();
    itemPreviewPanel.add(previewPanel, BorderLayout.CENTER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    // System.out.println("MediaServerPlugin: Action performed with command "+e.getActionCommand());
    if (toParentButton.getID().equals(e.getActionCommand()))
    {
      toParent();
    }
    if (newFileButton != null && newFileButton.getID().equals(e.getActionCommand()))
    {
      newDIDLObject(false);
    }
    if (newFolderButton != null && newFolderButton.getID().equals(e.getActionCommand()))
    {
      newDIDLObject(true);
    }
    if (importResourceButton != null && importResourceButton.getID().equals(e.getActionCommand()))
    {
      importResource();
    }
    if (destroyObjectButton != null && destroyObjectButton.getID().equals(e.getActionCommand()))
    {
      destroyObject();
    }

    // search object
    boolean found = false;
    int i = 0;
    while (!found && i < didlObjectButtonList.size())
    {
      found = ((SmoothButton)didlObjectButtonList.elementAt(i)).getID().equals(e.getActionCommand());
      if (!found)
      {
        i++;
      }
    }
    if (found)
    {
      // to child
      DIDLObject object = mediaServer.getCurrentContainer().getChildList()[i];
      if (object instanceof DIDLContainer)
      {
        toChild((DIDLContainer)object);
      }
      if (object instanceof DIDLItem)
      {
        selectedItem = (DIDLItem)object;
        // try preview
        if (mediaRendererCPDevice != null && selectedItem.getFirstResourceURL() != null &&
          mediaRendererCPDevice.canRenderResource(selectedItem.getFirstResource()))
        {
          mediaRendererCPDevice.setAVTransportURI(selectedItem.getFirstResourceURL(), "");
          mediaRendererCPDevice.play();
        }
        updateLayout();
      }
    }
  }

  /** Initializes the path panel */
  public void initPathPanel()
  {
    pathButton = new SmoothButton(new Dimension(600, ButtonConstants.BUTTON_HEIGHT), 12, "", null);
    pathButton.setDisabledButtonColor(pathButton.getButtonColor());
    pathButton.setSelectable(false);

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    jPathPanel.add(pathButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jPathPanel.add(jPathFillPanel, gridBagConstraints);
  }

  /** Initializes the action panel */
  public void initActionPanel()
  {
    GridBagConstraints gridBagConstraints;
    int index = 0;
    if (mediaServer != null)
    {
      if (mediaServer.supportsCreateObject())
      {
        newFileButton =
          new SmoothButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            "Neuer Eintrag",
            "commandNewFile");
        newFileButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
        newFileButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        newFileButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = index++;
        gridBagConstraints.gridy = 0;
        jActionPanel.add(newFileButton, gridBagConstraints);

        newFolderButton =
          new SmoothButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            "Neuer Ordner",
            "commandNewFolder");
        newFolderButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
        newFolderButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        newFolderButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = index++;
        gridBagConstraints.gridy = 0;
        jActionPanel.add(newFolderButton, gridBagConstraints);
      }
      if (mediaServer.supportsImportResource())
      {
        importResourceButton =
          new SmoothButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            "Importiere Ressource",
            "commandImportResource");
        importResourceButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
        importResourceButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        importResourceButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = index++;
        gridBagConstraints.gridy = 0;
        jActionPanel.add(importResourceButton, gridBagConstraints);
      }
      if (mediaServer.supportsDestroyObject())
      {
        destroyObjectButton =
          new SmoothButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            "Lösche Eintrag",
            "commandDestroyObject");
        destroyObjectButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
        destroyObjectButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        destroyObjectButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = index++;
        gridBagConstraints.gridy = 0;
        jActionPanel.add(destroyObjectButton, gridBagConstraints);
      }
    }
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jActionPanel.add(jActionFillPanel, gridBagConstraints);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jContentPanel = new javax.swing.JPanel();
    jDirectoryContentScrollPane = new javax.swing.JScrollPane();
    jDirectoryContentPanel = new javax.swing.JPanel();
    jContentDividerPanel = new javax.swing.JPanel();
    itemPropertiesScrollPane = new javax.swing.JScrollPane();
    itemPropertiesPanel = new javax.swing.JPanel();
    itemPreviewPanel = new javax.swing.JPanel();
    jInfoPanel = new javax.swing.JPanel();
    jToActionsDividerPanel = new javax.swing.JPanel();
    jToContentDividerPanel = new javax.swing.JPanel();
    jPathPanel = new javax.swing.JPanel();
    jActionPanel = new javax.swing.JPanel();

    mediaRendererPanel = new JMediaRendererPanel();
    mediaRendererPanel.setDeviceGUIContextProvider(deviceGUIContextProvider);

    setLayout(new java.awt.BorderLayout());

    jContentPanel.setLayout(new java.awt.GridBagLayout());

    jDirectoryContentScrollPane.setBorder(null);
    jDirectoryContentScrollPane.setMaximumSize(null);
    jDirectoryContentScrollPane.setMinimumSize(new java.awt.Dimension(340, 0));
    jDirectoryContentScrollPane.setPreferredSize(new java.awt.Dimension(340, 0));
    jDirectoryContentPanel.setLayout(new java.awt.GridBagLayout());

    jDirectoryContentScrollPane.setViewportView(jDirectoryContentPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jDirectoryContentScrollPane, gridBagConstraints);

    jContentDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jContentDividerPanel.setMinimumSize(new java.awt.Dimension(4, 10));
    jContentDividerPanel.setPreferredSize(new java.awt.Dimension(4, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    jContentPanel.add(jContentDividerPanel, gridBagConstraints);

    itemPropertiesScrollPane.setBorder(null);
    itemPropertiesScrollPane.setMinimumSize(new java.awt.Dimension(330, 220));
    itemPropertiesPanel.setLayout(new java.awt.GridBagLayout());

    itemPropertiesScrollPane.setViewportView(itemPropertiesPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    jContentPanel.add(itemPropertiesScrollPane, gridBagConstraints);

    itemPreviewPanel.setLayout(new java.awt.BorderLayout());

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(itemPreviewPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(contentFillPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.0;
    jContentPanel.add(mediaRendererPanel, gridBagConstraints);

    add(jContentPanel, java.awt.BorderLayout.CENTER);

    jInfoPanel.setLayout(new java.awt.GridBagLayout());

    jToActionsDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jToActionsDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jToActionsDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    jInfoPanel.add(jToActionsDividerPanel, gridBagConstraints);

    jToContentDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jToContentDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jToContentDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    jInfoPanel.add(jToContentDividerPanel, gridBagConstraints);

    jPathPanel.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jInfoPanel.add(jPathPanel, gridBagConstraints);

    jActionPanel.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jInfoPanel.add(jActionPanel, gridBagConstraints);

    add(jInfoPanel, java.awt.BorderLayout.NORTH);

  }// GEN-END:initComponents

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    jDirectoryContentPanel.removeAll();
    jDirectoryContentPanel.invalidate();
    itemPropertiesPanel.removeAll();
    itemPropertiesPanel.invalidate();

    updateLayoutForContainerContent();
    updatePathPanel();
    updateActionPanel();
    updateSelectedItem();

    jDirectoryContentPanel.repaint();
    itemPropertiesPanel.repaint();
    validateTree();
  }

  /** Updates the panel for the container content */
  private void updateLayoutForContainerContent()
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      GridBagConstraints gridBagConstraints;
      int offset = 0;
      // show goToParent if not in root container
      if (mediaServer.getCurrentContainer() != mediaServer.getRootContainer())
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        jDirectoryContentPanel.add(toParentButton, gridBagConstraints);
        offset = 1;
      }

      // add all objects and items
      for (int i = 0; i < didlObjectButtonList.size(); i++)
      {
        SmoothButton button = (SmoothButton)didlObjectButtonList.elementAt(i);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i + offset;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        button.setSelected(selectedItem != null && button.getID().equals(selectedItem.getID()));
        jDirectoryContentPanel.add(button, gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      jDirectoryContentPanel.add(jDirectoryContentFillPanel, gridBagConstraints);
    }
  }

  /** Updates the panels for the selected item */
  private void updateSelectedItem()
  {
    itemPreviewPanel.setVisible(false);
    mediaRendererPanel.setVisible(false);
    // update properties for current item
    if (selectedItem != null)
    {
      GridBagConstraints gridBagConstraints;
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);

      itemTitleButton.setValue(selectedItem.getTitle());
      itemPropertiesPanel.add(itemTitleButton, gridBagConstraints);
      if (selectedItem.getCreator() != null && selectedItem.getCreator().length() > 0)
      {
        itemCreatorButton.setValue(selectedItem.getCreator());
        itemPropertiesPanel.add(itemCreatorButton, gridBagConstraints);
      }
      // show item class
      if (selectedItem instanceof DIDLMusicTrack)
      {
        itemClassButton.setValue("Musiktrack");
      } else
      {
        itemClassButton.setValue(selectedItem.getObjectClass());
      }
      if (isDebugMode())
      {
        itemPropertiesPanel.add(itemClassButton, gridBagConstraints);
      }
      // show first resource
      if (selectedItem.getResources() != null && selectedItem.getResources().size() > 0)
      {
        DIDLResource resource = (DIDLResource)selectedItem.getResources().elementAt(0);
        // show resource
        if (resource.getValue() != null)
        {
          itemResourceButton.setValue(resource.getValue());
          System.out.println("Resource for selected item is: " + resource.getValue());
          itemPropertiesPanel.add(itemResourceButton, gridBagConstraints);
        }
        // show size
        if (resource.getSize() != null)
        {
          long resourceSize = Long.parseLong(resource.getSize());
          String roundedSize = new DecimalFormat("#.#").format(resourceSize / (1024.0 * 1024.0));
          itemResourceSizeButton.setValue(roundedSize + " MB (" + resource.getSize() + " Bytes)");
          itemPropertiesPanel.add(itemResourceSizeButton, gridBagConstraints);
        }
        // show importURI
        if (resource.getImportURI() != null)
        {
          itemResourceImportURIButton.setValue(resource.getImportURI());
          itemPropertiesPanel.add(itemResourceImportURIButton, gridBagConstraints);
        }
        if (isDebugMode())
        {
          itemResourceProtocolInfoButton.setValue(resource.getProtocolInfo());
          itemPropertiesPanel.add(itemResourceProtocolInfoButton, gridBagConstraints);
        }
      }
      if (isDebugMode())
      {
        // show restriced attribute
        itemRestrictedButton.setValue(selectedItem.getRestricted());
        if (selectedItem.getWriteStatus() != null && selectedItem.getWriteStatus().length() > 0)
        {
          itemWriteStatusButton.setValue(selectedItem.getWriteStatus());
          itemPropertiesPanel.add(itemWriteStatusButton, gridBagConstraints);
        }
        itemPropertiesPanel.add(itemRestrictedButton, gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      itemPropertiesPanel.add(itemPropertiesFillPanel, gridBagConstraints);

      // show internal media renderer
      if (mediaRendererCPDevice != null && selectedItem.getFirstResourceURL() != null &&
        mediaRendererCPDevice.canRenderResource(selectedItem.getFirstResource()))
      {
        itemPreviewPanel.setVisible(true);
      } else
      {
        itemPreviewPanel.setVisible(false);
      }
      // show fill panel if no image preview
      contentFillPanel.setVisible(!itemPreviewPanel.isVisible());

      // show panels for all found media renderer plugins that could render the current item
      mediaRendererPanel.setMediaItemResource(selectedItem.getFirstResource());
    }
  }

  /**
   * Goes to a child container.
   * 
   * @param container
   *          The child container
   */
  public void toChild(DIDLContainer container)
  {
    if (mediaServer != null)
    {
      selectedItem = null;
      mediaServer.toChild(container.getID());
      if (mediaServer.getCurrentContainer() != null && mediaServer.getCurrentContainer().getCurrentChildCount() == 0)
      {
        mediaServer.enumerateCurrentContainer();
      }

      updateContainerContentButtonList();
      updateLayout();
    }
  }

  /** Goes to the parent entry. */
  public void toParent()
  {
    if (mediaServer != null)
    {
      selectedItem = null;
      mediaServer.toParent();

      updateContainerContentButtonList();
      updateLayout();
    }
  }

  /** Creates a new DIDL object. */
  public void newDIDLObject(boolean createContainer)
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      try
      {
        String result = DialogValueInvocation.getInvokedString(frame, "Name für neuen Eintrag", "");
        if (result != null)
        {
          DIDLObject newFileObject = null;
          if (createContainer)
          {
            newFileObject = new DIDLContainer(result, "");
          } else
          {
            newFileObject = new DIDLItem(result, "");
          }

          newFileObject.setRestricted("0");

          mediaServer.createObject(newFileObject, mediaServer.getCurrentContainer().getID());

        }
      } catch (Exception ex)
      {
        System.out.println("ERROR: " + ex.getMessage());
      }
    }
  }

  /** Imports a resource to a DIDL object. */
  public void importResource()
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null && selectedItem != null)
    {
      try
      {
        String result =
          DialogValueInvocation.getInvokedString(frame, "URL für Ressource", "http://www.filesurfer.de/favicon.ico");
        if (result != null)
        {
          System.out.println("Try to import resource from " + result);

          mediaServer.importResource(selectedItem, result);
        }
      } catch (Exception ex)
      {
        System.out.println("ERROR: " + ex.getMessage());
      }
    }
  }

  /** Deletes a folder or file if permitted. */
  public void destroyObject()
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      try
      {
        if (selectedItem != null)
        {
          mediaServer.destroyObject(selectedItem);
        } else
        {
          mediaServer.destroyObject(mediaServer.getCurrentContainer());
        }
      } catch (Exception ex)
      {
        System.out.println("ERROR: " + ex.getMessage());
      }
    }
  }

  /** Updates the buttons for files in the local container */
  private void updateContainerContentButtonList()
  {
    if (mediaServer != null)
    {
      didlObjectButtonList.clear();
      if (mediaServer.getCurrentContainer() != null)
      {
        // do not show more than 500 items
        for (int i = 0; i < Math.min(500, mediaServer.getCurrentContainer().getCurrentChildCount()); i++)
        {
          DIDLObject didlObject = mediaServer.getCurrentContainer().getChildList()[i];

          SmoothButton didlButton =
            new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
              12,
              didlObject.getTitle(),
              didlObject.getID());

          didlButton.setButtonColor(GUIConstants.getButtonColor(didlObject));
          didlButton.setCenteredText(false);
          didlButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
          didlButton.addActionListener(this);

          didlObjectButtonList.add(didlButton);
        }
      }
    }
  }

  /** Updates the path */
  private void updatePathPanel()
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      int entryCount = mediaServer.getCurrentContainer().getCurrentChildCount();
      String entries = entryCount == 1 ? "1 Eintrag" : entryCount + " Einträge";

      String pathText = "";

      String[] path = mediaServer.getPathToCurrentObject();
      if (path != null)
      {
        // do not show root container
        for (int i = 1; i < path.length; i++)
        {
          pathText += path[i] + " > ";
        }
      }
      // do not show root container
      if (path.length > 0)
      {
        pathText += mediaServer.getCurrentObject().getTitle();
      }

      if (pathText.equals(""))
      {
        pathText = entries + " im Wurzelordner";
      } else
      {
        pathText = entries + " in " + pathText;
      }

      pathButton.setText(pathText);
    }
  }

  /** Updates the action buttons */
  private void updateActionPanel()
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      if (newFileButton != null)
      {
        newFileButton.setSelectable(!mediaServer.getCurrentContainer().isRestricted());
      }

      if (newFolderButton != null)
      {
        newFolderButton.setSelectable(!mediaServer.getCurrentContainer().isRestricted());
      }

      if (importResourceButton != null)
      {
        importResourceButton.setSelectable(false);
        if (selectedItem != null && selectedItem.getResources() != null && selectedItem.getResources().size() > 0)
        {
          DIDLResource resource = (DIDLResource)selectedItem.getResources().elementAt(0);
          importResourceButton.setSelectable(resource.getImportURI() != null);
        }
      }
      if (destroyObjectButton != null)
      {
        destroyObjectButton.setSelectable(!mediaServer.getCurrentContainer().isRestricted());
        if (selectedItem != null)
        {
          destroyObjectButton.setSelectable(!selectedItem.isRestricted());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerUpdateOccurred(java.lang.String)
   */
  public void containerUpdateOccurred(MediaServerCPDevice server, String containerUpdateID)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#systemUpdateOccurred(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice)
   */
  public void systemUpdateOccurred(MediaServerCPDevice server)
  {
    if (mediaServer != null && mediaServer.isDeprecatedCurrentContainer())
    {
      mediaServer.enumerateCurrentContainer();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationUpdate(java.lang.String)
   */
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID)
  {
    updateContainerContentButtonList();
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationFinished(java.lang.String)
   */
  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID)
  {
    // check if the currently selected item is still valid
    if (selectedItem != null && mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      boolean itemFound = false;
      for (int i = 0; !itemFound && i < mediaServer.getCurrentContainer().getCurrentChildCount(); i++)
      {
        DIDLObject didlObject = mediaServer.getCurrentContainer().getChildList()[i];
        itemFound |= selectedItem.getTitle().equals(didlObject.getTitle());
        // update item because of possible property changes
        if (itemFound && didlObject instanceof DIDLItem)
        {
          selectedItem = (DIDLItem)didlObject;
        }
      }
      if (!itemFound)
      {
        selectedItem = null;
      }
    }
    updateContainerContentButtonList();
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BaseCPServicePlugin#setDeviceGUIContextProvider(de.fhg.fokus.magic.upnp.examples.gui_control_point.IDeviceGUIContextProvider)
   */
  public void setDeviceGUIContextProvider(IDeviceGUIContextProvider provider)
  {
    super.setDeviceGUIContextProvider(provider);
    if (mediaRendererPanel != null)
    {
      mediaRendererPanel.setDeviceGUIContextProvider(provider);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#visibilityChanged()
   */
  public void visibilityChanged()
  {
    if (mediaRendererPanel != null)
    {
      mediaRendererPanel.updateMediaRenderers();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#terminate()
   */
  public void terminate()
  {
    if (canStartPlugin())
    {
      mediaServer.terminate();
    }
    super.terminate();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel      jActionPanel;

  private javax.swing.JPanel      jContentDividerPanel;

  private javax.swing.JPanel      jContentPanel;

  private javax.swing.JPanel      jInfoPanel;

  private javax.swing.JPanel      jDirectoryContentPanel;

  private javax.swing.JPanel      itemPropertiesPanel;

  private javax.swing.JScrollPane itemPropertiesScrollPane;

  private javax.swing.JScrollPane jDirectoryContentScrollPane;

  private javax.swing.JPanel      jPathPanel;

  private javax.swing.JPanel      itemPreviewPanel;

  private javax.swing.JPanel      jToActionsDividerPanel;

  private javax.swing.JPanel      jToContentDividerPanel;
  // End of variables declaration//GEN-END:variables

}

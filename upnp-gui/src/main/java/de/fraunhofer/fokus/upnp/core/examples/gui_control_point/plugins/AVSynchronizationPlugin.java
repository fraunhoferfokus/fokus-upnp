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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPMediaServerStateVariableListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaServerCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothValueButton;

/**
 * This plugin is used to initiate media server synchronization.
 * 
 * @author Alexander Koenig
 */
public class AVSynchronizationPlugin extends BaseCPDevicePlugin implements
  ICPMediaServerStateVariableListener,
  MouseListener
{
  private static final long   serialVersionUID            = 1L;

  public static String        PLUGIN_TYPE                 = UPnPAVConstant.AV_SYNCHRONIZATION_DEVICE_TYPE;

  public static final int     ITEM_BUTTON_WIDTH           = 300;

  public static final int     MEDIA_SERVER_BUTTON_WIDTH   = 280;

  public static final int     ACTION_BUTTON_WIDTH         = 280;

  private SmoothButton        sourceToParentButton;

  private SmoothButton        destinationToParentButton;

  private SmoothValueButton   sourceServerButton;

  private SmoothValueButton   destinationServerButton;

  private SmoothButton        synchronizeButton;

  private SmoothValueButton   transferIDButton;

  private SmoothValueButton   currentItemButton;

  private JPanel              jActionFillPanel            = new JPanel();

  // SmoothButtons for all DIDL objects
  private Vector              sourceServerButtonList      = new Vector();

  private JPanel              jSourceFillPanel            = new JPanel();

  private Vector              destinationServerButtonList = new Vector();

  private JPanel              jDestinationFillPanel       = new JPanel();

  private MediaServerCPDevice sourceServer                = null;

  private MediaServerCPDevice destinationServer           = null;

  private boolean             pendingOperation            = false;

  /** List of available media servers */
  private Vector              mediaServerList             = new Vector();

  /** Sync service */
  private CPService           contentSynchronizationService;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    initComponents();

    sourceToParentButton =
      new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "..",
        "commandSourceToParent");
    sourceToParentButton.setButtonColor(new Color(180, 180, 180));
    sourceToParentButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    sourceToParentButton.addActionListener(this);

    destinationToParentButton =
      new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "..",
        "commandDestinationToParent");
    destinationToParentButton.setButtonColor(new Color(180, 180, 180));
    destinationToParentButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
    destinationToParentButton.addActionListener(this);

    jActionPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSourceFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDestinationFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jContentDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jToContentDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jInfoPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSourceServerScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jSourceServerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDestinationServerScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDestinationServerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

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

    initActionPanel();

    // initialize list with known media servers
    for (int i = 0; i < controlPoint.getCPDeviceCount(); i++)
    {
      CPDevice currentDevice = controlPoint.getCPDevice(i);
      if (currentDevice.getDeviceType().equals(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE))
      {
        MediaServerCPDevice mediaServer = new MediaServerCPDevice(getControlPoint(), currentDevice);
        mediaServer.addServerChangeListener(this);
        mediaServerList.add(mediaServer);
      }
    }
    contentSynchronizationService = getCPDevice().getCPServiceByType(UPnPAVConstant.AV_SYNCHRONIZATION_SERVICE_TYPE);

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

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    if (stateVariable.getCPService() == contentSynchronizationService)
    {
      if (stateVariable.getName().equals(UPnPAVConstant.SV_SYNCHRONIZATION_PROGRESS))
      {
        try
        {
          pendingOperation = stateVariable.getStringValue().equals(UPnPAVConstant.VALUE_IN_PROGRESS);
          synchronizeButton.setSelectable(!pendingOperation);
        } catch (Exception e)
        {
        }
      }
      if (stateVariable.getName().equals(UPnPAVConstant.SV_TRANSFER_IDS))
      {
        try
        {
          System.out.println("TransferIDList is: " + stateVariable.getStringValue());
          transferIDButton.setValue(stateVariable.getStringValue());
          // StringTokenizer stringTokenizer = new StringTokenizer(stateVariable.getStringValue(),
          // ",");
          // transferIDButton.setValue(stringTokenizer.countTokens() + "");
        } catch (Exception e)
        {
        }
      }
      if (stateVariable.getName().equals(UPnPAVConstant.SV_CURRENT_SYNCHRONIZED_ENTRY))
      {
        try
        {
          currentItemButton.setValue(stateVariable.getStringValue());
        } catch (Exception e)
        {
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    // System.out.println("MediaServerPlugin.Action performed: " +
    // " ID: " + e.getID() +
    // " Modifier: " + e.getModifiers() +
    // " Command: " + e.getActionCommand());
    if (sourceToParentButton.getID().equals(e.getActionCommand()))
    {
      toParent(sourceServer);
    }
    if (destinationToParentButton.getID().equals(e.getActionCommand()))
    {
      toParent(destinationServer);
    }
    // set source server
    if (sourceServerButton.getID().equals(e.getActionCommand()))
    {
      Vector friendlyNameList = new Vector();
      Vector visibleDeviceList = new Vector();
      for (int i = 0; i < mediaServerList.size(); i++)
      {
        MediaServerCPDevice currentDevice = (MediaServerCPDevice)mediaServerList.elementAt(i);
        if (deviceGUIContext.isVisibleDevice(currentDevice.getCPDevice()))
        {
          friendlyNameList.add(currentDevice.getCPDevice().getFriendlyName() + " auf " +
            currentDevice.getCPDevice().getDeviceDescriptionSocketAddress().getHostName());
          visibleDeviceList.add(currentDevice);
        }
      }
      int index = DialogValueInvocation.getInvokedItemIndex(frame, "Wählen Sie einen Quellserver", friendlyNameList);
      if (index != -1)
      {
        sourceServer = (MediaServerCPDevice)visibleDeviceList.elementAt(index);
        sourceServerButton.setValue(sourceServer.getCPDevice().getFriendlyName());
        updateMediaServerContent(sourceServer, true);
      }
    }
    // set destination server
    if (destinationServerButton.getID().equals(e.getActionCommand()))
    {
      Vector friendlyNameList = new Vector();
      Vector visibleDeviceList = new Vector();
      for (int i = 0; i < mediaServerList.size(); i++)
      {
        MediaServerCPDevice currentDevice = (MediaServerCPDevice)mediaServerList.elementAt(i);
        if (deviceGUIContext.isVisibleDevice(currentDevice.getCPDevice()))
        {
          friendlyNameList.add(currentDevice.getCPDevice().getFriendlyName() + " auf " +
            currentDevice.getCPDevice().getDeviceDescriptionSocketAddress().getHostName());
          visibleDeviceList.add(currentDevice);
        }
      }
      int index = DialogValueInvocation.getInvokedItemIndex(frame, "Wählen Sie einen Zielserver", friendlyNameList);
      if (index != -1)
      {
        destinationServer = (MediaServerCPDevice)visibleDeviceList.elementAt(index);
        destinationServerButton.setValue(destinationServer.getCPDevice().getFriendlyName());
        updateMediaServerContent(destinationServer, true);
      }
    }
    // synchronize
    if (synchronizeButton != null && synchronizeButton.getID().equals(e.getActionCommand()))
    {
      synchronizeServers();
    }
    // search source container
    boolean found = false;
    int i = 0;
    while (!found && i < sourceServerButtonList.size())
    {
      SmoothButton selectedButton = (SmoothButton)sourceServerButtonList.elementAt(i);
      found = selectedButton.getID().equals(e.getActionCommand());
      if (found)
      {
        // select container if Ctrl is pressed
        if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
        {
          // deselect all other buttons
          for (int j = 0; j < sourceServerButtonList.size(); j++)
          {
            if (i != j)
            {
              ((SmoothButton)sourceServerButtonList.elementAt(j)).setSelected(false);
            }
          }
          selectedButton.setSelected(!selectedButton.isSelected());
        }
      } else
      {
        i++;
      }
    }
    // go to child if Ctrl is not pressed
    if (found && (e.getModifiers() & ActionEvent.CTRL_MASK) == 0)
    {
      // to child
      DIDLObject object = sourceServer.getCurrentContainer().getChildList()[i];
      if (object instanceof DIDLContainer)
      {
        toChild(sourceServer, (DIDLContainer)object);
      }
    }
    // search destination container
    found = false;
    i = 0;
    while (!found && i < destinationServerButtonList.size())
    {
      SmoothButton selectedButton = (SmoothButton)destinationServerButtonList.elementAt(i);
      found = selectedButton.getID().equals(e.getActionCommand());
      if (found)
      {
        // if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
        // {
        // selectedButton.setSelected(!selectedButton.isSelected());
        // }
      } else
      {
        i++;
      }
    }
    if (found)
    {
      // to child
      DIDLObject object = destinationServer.getCurrentContainer().getChildList()[i];
      if (object instanceof DIDLContainer)
      {
        toChild(destinationServer, (DIDLContainer)object);
      }
    }
  }

  /** Starts the synchronization */
  public void synchronizeServers()
  {
    try
    {
      CPService contentSynchronizationService =
        device.getCPServiceByType(UPnPAVConstant.AV_SYNCHRONIZATION_SERVICE_TYPE);

      // set type
      CPAction action =
        contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_SYNCHRONIZATION_HIERARCHY_MODE);
      action.getArgument(UPnPAVConstant.ARG_SYNC_HIERARCHY_MODE).setValue(UPnPAVConstant.VALUE_FOLDER_STRUCTURE);

      getControlPoint().invokeAction(action);

      // reset criteria
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_SYNCHRONIZATION_CRITERIA);
      // action is optional
      if (action != null)
      {
        action.getArgument(UPnPAVConstant.ARG_SYNC_CRITERIA).setValue("");
        getControlPoint().invokeAction(action);
      }

      // set source server
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_SOURCE_SERVER);
      action.getArgument(UPnPAVConstant.ARG_SOURCE_UDN).setValue(sourceServer.getCPDevice().getUDN());
      getControlPoint().invokeAction(action);

      // set destination server
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_DESTINATION_SERVER);
      action.getArgument(UPnPAVConstant.ARG_DESTINATION_UDN).setValue(destinationServer.getCPDevice().getUDN());
      getControlPoint().invokeAction(action);

      // predefine mode
      String syncSourceMode = UPnPAVConstant.VALUE_CONTENT_ONLY;

      // set source container
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_SOURCE_CONTAINER);
      // action is optional
      if (action != null)
      {
        // check if there is a selected source container
        int containerIndex = -1;
        for (int i = 0; i < sourceServerButtonList.size(); i++)
        {
          if (((SmoothButton)sourceServerButtonList.elementAt(i)).isSelected())
          {
            containerIndex = i;
          }
        }
        //  
        if (containerIndex != -1)
        {
          // create selected container on destination side
          syncSourceMode = UPnPAVConstant.VALUE_CONTAINER;
          // choose selected container as source
          DIDLObject container = sourceServer.getCurrentContainer().getChildList()[containerIndex];
          action.getArgument(UPnPAVConstant.ARG_SOURCE_CONTAINER_ID).setValue(container.getID());
        } else
        {
          // choose current container as source and synchronize only content
          action.getArgument(UPnPAVConstant.ARG_SOURCE_CONTAINER_ID).setValue(sourceServer.getCurrentContainer()
            .getID());
        }
        getControlPoint().invokeAction(action);
      }

      // set mode
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_SYNCHRONIZATION_SOURCE_MODE);
      action.getArgument(UPnPAVConstant.ARG_SYNC_SOURCE_MODE).setValue(syncSourceMode);
      getControlPoint().invokeAction(action);

      // set destination container
      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SET_DESTINATION_CONTAINER);
      // action is optional
      if (action != null)
      {
        action.getArgument(UPnPAVConstant.ARG_DESTINATION_CONTAINER_ID)
          .setValue(destinationServer.getCurrentContainer().getID());
        getControlPoint().invokeAction(action);
      }

      action = contentSynchronizationService.getCPAction(UPnPAVConstant.ACTION_SYNCHRONIZE);
      getControlPoint().invokeAction(action);

      System.out.println("Successfully started synchronize action");

    } catch (Exception e)
    {
      System.out.println("ERROR: " + e.getMessage());
    }
  }

  /** Initializes the action panel */
  public void initActionPanel()
  {
    GridBagConstraints gridBagConstraints;
    int index = 0;

    sourceServerButton =
      new SmoothValueButton(new Dimension(MEDIA_SERVER_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Quelle:",
        "",
        "commandSetSourceServer");
    sourceServerButton.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridy = 0;
    jActionPanel.add(sourceServerButton, gridBagConstraints);

    destinationServerButton =
      new SmoothValueButton(new Dimension(MEDIA_SERVER_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Ziel:",
        "",
        "commandSetDestinationServer");
    destinationServerButton.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridy = 0;
    jActionPanel.add(destinationServerButton, gridBagConstraints);

    synchronizeButton =
      new SmoothButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Starte Synchronisation",
        "commandStartSynchronization");
    synchronizeButton.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridy = 0;
    jActionPanel.add(synchronizeButton, gridBagConstraints);

    index = 0;
    transferIDButton =
      new SmoothValueButton(new Dimension(ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Aktuelle Transfers:",
        "",
        null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridy = 1;
    jActionPanel.add(transferIDButton, gridBagConstraints);

    currentItemButton =
      new SmoothValueButton(new Dimension(2 * ACTION_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
        12,
        "Aktueller Eintrag:",
        "",
        null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = index++;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridy = 1;
    jActionPanel.add(currentItemButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
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
    jSourceServerScrollPane = new javax.swing.JScrollPane();
    jSourceServerPanel = new javax.swing.JPanel();
    jContentDividerPanel = new javax.swing.JPanel();
    jDestinationServerScrollPane = new javax.swing.JScrollPane();
    jDestinationServerPanel = new javax.swing.JPanel();
    jInfoPanel = new javax.swing.JPanel();
    jToContentDividerPanel = new javax.swing.JPanel();
    jActionPanel = new javax.swing.JPanel();

    setLayout(new java.awt.BorderLayout());

    setBackground(new java.awt.Color(204, 204, 255));
    jContentPanel.setLayout(new java.awt.GridBagLayout());

    jContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    jSourceServerScrollPane.setBorder(null);
    jSourceServerScrollPane.setMaximumSize(null);
    jSourceServerPanel.setLayout(new java.awt.GridBagLayout());

    jSourceServerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jSourceServerScrollPane.setViewportView(jSourceServerPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jSourceServerScrollPane, gridBagConstraints);

    jContentDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jContentDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jContentDividerPanel.setMinimumSize(new java.awt.Dimension(4, 10));
    jContentDividerPanel.setPreferredSize(new java.awt.Dimension(4, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    jContentPanel.add(jContentDividerPanel, gridBagConstraints);

    jDestinationServerScrollPane.setBorder(null);
    jDestinationServerScrollPane.setMaximumSize(null);
    jDestinationServerPanel.setLayout(new java.awt.GridBagLayout());

    jDestinationServerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jDestinationServerScrollPane.setViewportView(jDestinationServerPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jDestinationServerScrollPane, gridBagConstraints);

    add(jContentPanel, java.awt.BorderLayout.CENTER);

    jInfoPanel.setLayout(new java.awt.GridBagLayout());

    jInfoPanel.setBackground(new java.awt.Color(204, 204, 255));
    jToContentDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jToContentDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jToContentDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jToContentDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    jInfoPanel.add(jToContentDividerPanel, gridBagConstraints);

    jActionPanel.setLayout(new java.awt.GridBagLayout());

    jActionPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jInfoPanel.add(jActionPanel, gridBagConstraints);

    add(jInfoPanel, java.awt.BorderLayout.NORTH);

  }// GEN-END:initComponents

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    jSourceServerPanel.removeAll();
    jSourceServerPanel.invalidate();
    jDestinationServerPanel.removeAll();
    jDestinationServerPanel.invalidate();

    updateLayoutForContainerContent(sourceServer,
      sourceServerButtonList,
      jSourceServerPanel,
      jSourceFillPanel,
      sourceToParentButton);
    updateLayoutForContainerContent(destinationServer,
      destinationServerButtonList,
      jDestinationServerPanel,
      jDestinationFillPanel,
      destinationToParentButton);
    updateActionPanel();

    jSourceServerPanel.repaint();
    jDestinationServerPanel.repaint();
    validateTree();
  }

  /** Updates a panel for container content */
  private void updateLayoutForContainerContent(MediaServerCPDevice mediaServer,
    Vector buttonList,
    JPanel contentPanel,
    JPanel fillPanel,
    SmoothButton toParentButton)
  {
    if (mediaServer != null && mediaServer.getCurrentContainer() != null)
    {
      GridBagConstraints gridBagConstraints;
      int offset = 0;
      if (mediaServer.getCurrentContainer() != mediaServer.getRootContainer())
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        contentPanel.add(toParentButton, gridBagConstraints);
        offset = 1;
      }

      // add all objects and items
      for (int i = 0; i < buttonList.size(); i++)
      {
        SmoothButton button = (SmoothButton)buttonList.elementAt(i);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i + offset;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        button.setSelected(false);
        contentPanel.add(button, gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      contentPanel.add(fillPanel, gridBagConstraints);
    }
  }

  /**
   * Goes to a child container.
   * 
   * @param container
   *          The child container
   */
  public void toChild(MediaServerCPDevice mediaServer, DIDLContainer container)
  {
    if (mediaServer != null)
    {
      mediaServer.toChild(container.getID());
      updateMediaServerContent(mediaServer, true);
    }
  }

  /** Goes to the parent entry. */
  public void toParent(MediaServerCPDevice mediaServer)
  {
    if (mediaServer != null)
    {
      mediaServer.toParent();
      updateMediaServerContent(mediaServer, false);
    }
  }

  /**
   * Updates a media server.
   * 
   * @param mediaServer
   *          The media server
   */
  private void updateMediaServerContent(MediaServerCPDevice mediaServer, boolean enumerateChildren)
  {
    if (mediaServer != null)
    {
      if (enumerateChildren && mediaServer.getCurrentContainer() != null &&
        mediaServer.getCurrentContainer().getCurrentChildCount() == 0)
      {
        mediaServer.enumerateCurrentContainer();
      }
      updateContainerContentButtonList(mediaServer);
      updateLayout();
    } else
    {

    }
  }

  /** Updates the buttons for files in the local container */
  private void updateContainerContentButtonList(MediaServerCPDevice mediaServer)
  {
    Vector buttonList = null;
    if (mediaServer == sourceServer)
    {
      buttonList = sourceServerButtonList;
    }
    if (mediaServer == destinationServer)
    {
      buttonList = destinationServerButtonList;
    }

    if (buttonList == null)
    {
      return;
    }

    buttonList.clear();
    if (mediaServer.getCurrentContainer() != null)
    {
      for (int i = 0; i < mediaServer.getCurrentContainer().getCurrentChildCount(); i++)
      {
        DIDLObject didlObject = mediaServer.getCurrentContainer().getChildList()[i];
        SmoothButton didlButton =
          new SmoothButton(new Dimension(ITEM_BUTTON_WIDTH, ButtonConstants.BUTTON_HEIGHT),
            12,
            didlObject.getTitle(),
            mediaServer.getCPDevice().getFriendlyName() + didlObject.getID());

        didlButton.setButtonColor(GUIConstants.getButtonColor(didlObject));
        didlButton.setCenteredText(false);
        didlButton.setActiveButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);
        didlButton.addActionListener(this);
        didlButton.addMouseListener(this);

        buttonList.add(didlButton);
      }
    }
  }

  /** Updates the action buttons */
  private void updateActionPanel()
  {
    if (synchronizeButton != null)
    {
      synchronizeButton.setSelectable(!pendingOperation && sourceServer != null && destinationServer != null);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPDeviceEventListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    super.newDevice(newDevice);
    if (newDevice.getDeviceType().equals(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE))
    {
      if (!isKnownMediaServer(newDevice.getUDN()))
      {
        MediaServerCPDevice mediaServer = new MediaServerCPDevice(getControlPoint(), newDevice);
        mediaServer.addServerChangeListener(this);
        mediaServerList.add(mediaServer);
      }
    }
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    // remove from local list
    if (goneDevice.getDeviceType().equals(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE))
    {
      int index = getMediaServerIndex(goneDevice.getUDN());
      if (index != -1)
      {
        MediaServerCPDevice removedMediaServer = (MediaServerCPDevice)mediaServerList.elementAt(index);
        if (removedMediaServer == sourceServer)
        {
          sourceServer = null;
          sourceServerButton.setValue("");
          sourceServerButtonList.clear();
        }
        if (removedMediaServer == destinationServer)
        {
          destinationServer = null;
          destinationServerButton.setValue("");
          destinationServerButtonList.clear();
        }
        removedMediaServer.terminate();
        mediaServerList.remove(index);
      }
    }
    updateLayout();
  }

  /**
   * Checks if a device with this udn is already in the list
   */
  protected boolean isKnownMediaServer(String udn)
  {
    for (int i = 0; i < mediaServerList.size(); i++)
    {
      if (((MediaServerCPDevice)mediaServerList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the index for a media server
   */
  public int getMediaServerIndex(String udn)
  {
    for (int i = 0; i < mediaServerList.size(); i++)
    {
      if (((MediaServerCPDevice)mediaServerList.elementAt(i)).getCPDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPMediaServerStateVariableListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
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
    System.out.println("System update occured");
    if ((server == sourceServer || server == destinationServer) && server.isDeprecatedCurrentContainer())
    {
      System.out.println("Enumerate current container");
      server.enumerateCurrentContainer();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationUpdate(java.lang.String)
   */
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID)
  {
    updateContainerContentButtonList(server);
    updateLayout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationFinished(java.lang.String)
   */
  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID)
  {
    updateContainerContentButtonList(server);
    updateLayout();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // IMouseListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#terminate()
   */
  public void terminate()
  {
    for (int i = 0; i < mediaServerList.size(); i++)
    {
      ((MediaServerCPDevice)mediaServerList.elementAt(i)).terminate();
    }
    super.terminate();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel      jActionPanel;

  private javax.swing.JPanel      jContentDividerPanel;

  private javax.swing.JPanel      jContentPanel;

  private javax.swing.JPanel      jDestinationServerPanel;

  private javax.swing.JScrollPane jDestinationServerScrollPane;

  private javax.swing.JPanel      jInfoPanel;

  private javax.swing.JPanel      jSourceServerPanel;

  private javax.swing.JScrollPane jSourceServerScrollPane;

  private javax.swing.JPanel      jToContentDividerPanel;
  // End of variables declaration//GEN-END:variables

}

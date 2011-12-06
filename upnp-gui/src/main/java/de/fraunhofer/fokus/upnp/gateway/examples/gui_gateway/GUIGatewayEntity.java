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
package de.fraunhofer.fokus.upnp.gateway.examples.gui_gateway;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModule;
import de.fraunhofer.fokus.upnp.gateway.common.forwarder_module.IForwarderModuleEventListener;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarderEntity;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.network.INetworkInterfaceChangeListener;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;

/**
 * This class represents a graphical UPnP gateway.
 * 
 * @author Alexander Koenig
 */
public class GUIGatewayEntity extends javax.swing.JFrame implements
  ActionListener,
  INetworkInterfaceChangeListener,
  ICPDeviceEventListener,
  IForwarderModuleEventListener

{

  private static final long      serialVersionUID           = 1L;

  private static final String    DEVICES                    = " Geräte";

  private static final String    ONE_DEVICE                 = "1 Gerät";

  private static final String    ZERO_DEVICES               = "0 Geräte";

  private static final int       INSET                      = 2;

  private Color                  panelColor;

  /** Central forwarder entity */
  private MessageForwarderEntity messageForwarderEntity;

  private Vector                 networkInterfaceButtonList = new Vector();

  // Common controls
  private SmoothButton           forwarderButton;

  private SmoothButton           networkInterfaceButton;

  private SmoothButton           internetButton;

  private SmoothButton           tunnelServerButton;

  private SmoothButton           tunnelClientButton;

  private SmoothButton           internetDescriptionButton;

  private SmoothButton           internetDeviceButton;

  private SmoothButton           internetPeerButton;

  private SmoothButton           tunnelServerDescriptionButton;

  private SmoothButton           tunnelServerClientsButton;

  private SmoothButton           tunnelServerDeviceButton;

  private SmoothButton           tunnelClientDescriptionButton;

  private SmoothButton           tunnelClientStatusButton;

  private SmoothButton           tunnelClientDeviceButton;

  private JPanel                 forwarderPanel;

  private JPanel                 networkInterfacePanel;

  private JPanel                 internetPanel;

  private JPanel                 tunnelServerPanel;

  private JPanel                 tunnelClientPanel;

  /** Creates new form GUIGateway */
  public GUIGatewayEntity(String[] args)
  {
    initComponents();

    setTitle("Gateway");

    ToolTipManager.sharedInstance().setDismissDelay(10000);

    jContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jGatewayPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jTopPanel.setBackground(ButtonConstants.DARK_BACKGROUND_COLOR);
    jBottomPanel.setBackground(ButtonConstants.DARK_BACKGROUND_COLOR);

    panelColor = SmoothButton.darker(ButtonConstants.BACKGROUND_COLOR, 0);

    forwarderPanel = initThemePanel(ButtonConstants.BUTTON_COLOR);
    networkInterfacePanel = initThemePanel(GUIGatewayConstants.GATEWAY_LOCAL_COLOR);
    internetPanel = initThemePanel(GUIGatewayConstants.GATEWAY_INTERNET_COLOR);
    tunnelServerPanel = initThemePanel(GUIGatewayConstants.GATEWAY_TCP_SERVER_COLOR);
    tunnelClientPanel = initThemePanel(GUIGatewayConstants.GATEWAY_TCP_CLIENT_COLOR);

    // create buttons
    forwarderButton = initThemeButton("Message forwarder", ButtonConstants.BUTTON_COLOR);
    forwarderButton.setSelected(true);
    networkInterfaceButton = initThemeButton("Network interfaces", GUIGatewayConstants.GATEWAY_LOCAL_COLOR);
    networkInterfaceButton.setSelected(true);
    internetButton = initThemeButton("Internet", GUIGatewayConstants.GATEWAY_INTERNET_COLOR);
    tunnelServerButton = initThemeButton("TCP tunnel server", GUIGatewayConstants.GATEWAY_TCP_SERVER_COLOR);
    tunnelClientButton = initThemeButton("TCP tunnel clients", GUIGatewayConstants.GATEWAY_TCP_CLIENT_COLOR);

    internetDescriptionButton = initDeviceButton("Internet-Peer", "internetPeer", "Internet-P2P");
    internetPeerButton = initPropertyButton("0 Teilnehmer", "Anzahl der verbundenen Teilnehmer");
    internetDeviceButton = initPropertyButton("0 Geräte", "Anzahl der Geräte an diesem Modul");

    tunnelServerDescriptionButton =
      initDeviceButton("Server an Port 10000", "tunnelServer", "Lokaler TCP-Tunnel Server");
    tunnelServerClientsButton = initPropertyButton("0 Clients", "Anzahl der verbundenen Clients");
    tunnelServerDeviceButton = initPropertyButton("0 Geräte", "Anzahl der Geräte an diesem Modul");

    tunnelClientDescriptionButton =
      initDeviceButton("Verbindung zu: localhost:10000", "tunnelClient", "Client für TCP-Tunnel");
    tunnelClientStatusButton = initPropertyButton("Nicht verbunden", "Status des Clients");
    tunnelClientDeviceButton = initPropertyButton("0 Geräte", "Anzahl der Geräte an diesem Modul");

    String startupFileName = args.length > 0 ? args[0] : FileHelper.getStartupConfigurationName("GUIGatewayEntity");
    UPnPStartupConfiguration startupConfiguration = new UPnPStartupConfiguration(startupFileName);
    startupConfiguration.setStartKeyboardThread(false);
    startupConfiguration.trySetValidWorkingDirectory(this.getClass().getName());
    messageForwarderEntity = new MessageForwarderEntity(startupConfiguration);
    // forward network interface events to this class to update GUI
    messageForwarderEntity.getInetAddressManagement().setNetworkInterfaceChangeListener(this);
    // forward device events to this class to update GUI
    messageForwarderEntity.getMessageForwarder().addCPDeviceEventListener(this);
    // forward tunnel events to this class to update GUI
    messageForwarderEntity.setForwarderModuleEventListener(this);

    setTitle(getTitle() + " (" + startupConfiguration.getSSDPMulticastAddress() + ":" +
      startupConfiguration.getSSDPMulticastPort() + ")");

    updateTCPTunnelStats();

    // update view
    this.setSize(1024, 650);
    updateGUI();
  }

  private JPanel initThemePanel(Color color)
  {
    JPanel panel = new JPanel();
    panel.setBackground(panelColor);
    panel.setBorder(new SmoothBorder(color));
    panel.setLayout(new GridBagLayout());

    return panel;
  }

  private SmoothButton initThemeButton(String text, Color color)
  {
    SmoothButton button = new SmoothButton(new Dimension(250, ButtonConstants.BUTTON_HEIGHT), text, null);
    button.setBackground(panelColor);
    button.setButtonColor(SmoothButton.darker(color, 50));
    button.setActiveButtonColor(color);

    return button;
  }

  /** Initializes a device button */
  private SmoothButton initDeviceButton(String text, String command, String tooltip)
  {
    SmoothButton button = new SmoothButton(new Dimension(300, ButtonConstants.BUTTON_HEIGHT), 12, text, command);
    button.setBackground(panelColor);
    button.setAutomaticTooltip(false);
    button.addActionListener(this);
    button.setToolTipText(tooltip);

    return button;
  }

  /** Initializes a property button */
  private SmoothButton initPropertyButton(String text, String tooltip)
  {
    SmoothButton button = new SmoothButton(new Dimension(250, ButtonConstants.BUTTON_HEIGHT), 12, text, null);
    button.setBackground(panelColor);
    button.setAutomaticTooltip(false);
    button.setToolTipText(tooltip);

    return button;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jTopPanel = new javax.swing.JPanel();
    jContentPanel = new javax.swing.JPanel();
    jGatewayPanel = new javax.swing.JPanel();
    jBottomPanel = new javax.swing.JPanel();

    getContentPane().setLayout(new java.awt.GridBagLayout());

    setTitle("Ger\u00e4tekontrolle");
    addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(java.awt.event.ComponentEvent evt)
      {
        formComponentResized(evt);
      }
    });
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        exitForm(evt);
      }
    });

    jTopPanel.setLayout(new java.awt.GridBagLayout());

    jTopPanel.setBackground(new java.awt.Color(153, 153, 255));
    jTopPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.05;
    getContentPane().add(jTopPanel, gridBagConstraints);

    jContentPanel.setLayout(new java.awt.GridBagLayout());

    jContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    jContentPanel.setMinimumSize(new java.awt.Dimension(300, 100));
    jContentPanel.setPreferredSize(new java.awt.Dimension(300, 100));
    jGatewayPanel.setLayout(new java.awt.GridBagLayout());

    jGatewayPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jGatewayPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(jContentPanel, gridBagConstraints);

    jBottomPanel.setLayout(new java.awt.GridBagLayout());

    jBottomPanel.setBackground(new java.awt.Color(153, 153, 255));
    jBottomPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.05;
    getContentPane().add(jBottomPanel, gridBagConstraints);

    pack();
  }// GEN-END:initComponents

  private void formComponentResized(java.awt.event.ComponentEvent evt)
  {// GEN-FIRST:event_formComponentResized
  }// GEN-LAST:event_formComponentResized

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt)
  {// GEN-FIRST:event_exitForm

    messageForwarderEntity.terminate();

    System.exit(0);
  }// GEN-LAST:event_exitForm

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new GUIGatewayEntity(args).setVisible(true);
  }

  private Vector getDevicesForForwarderModule(String moduleID)
  {
    return messageForwarderEntity.getMessageForwarder().getDevicesForForwarderModule(moduleID);
  }

  private String getDeviceNamesFromDeviceList(Vector deviceList)
  {
    String result = "";
    for (int i = 0; i < deviceList.size(); i++)
    {
      CPDevice currentDevice = (CPDevice)deviceList.elementAt(i);

      result += result.length() != 0 ? ", " : "";
      result += currentDevice.getFriendlyName();
    }
    return result;
  }

  private void updateGUI()
  {
    jGatewayPanel.removeAll();
    jGatewayPanel.invalidate();

    updateGatewayPanel();

    jGatewayPanel.repaint();
    this.validateTree();
  }

  private void updateTunnelServerPanel()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(INSET, INSET, INSET, 3);
    tunnelServerPanel.add(tunnelServerButton, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    tunnelServerPanel.add(tunnelServerDescriptionButton, gridBagConstraints);
    gridBagConstraints.insets = new Insets(INSET, 53, INSET, INSET);
    gridBagConstraints.gridy = 2;
    tunnelServerPanel.add(tunnelServerClientsButton, gridBagConstraints);
    gridBagConstraints.gridy = 3;
    tunnelServerPanel.add(tunnelServerDeviceButton, gridBagConstraints);
  }

  private void updateTunnelClientPanel()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(INSET, INSET, INSET, INSET);
    tunnelClientPanel.add(tunnelClientButton, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    tunnelClientPanel.add(tunnelClientDescriptionButton, gridBagConstraints);
    gridBagConstraints.insets = new Insets(INSET, 53, INSET, INSET);
    gridBagConstraints.gridy = 2;
    tunnelClientPanel.add(tunnelClientStatusButton, gridBagConstraints);
    gridBagConstraints.gridy = 3;
    tunnelClientPanel.add(tunnelClientDeviceButton, gridBagConstraints);
  }

  private void updateInternetPanel()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(INSET, INSET, INSET, INSET);
    internetPanel.add(internetButton, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    internetPanel.add(internetDescriptionButton, gridBagConstraints);
    gridBagConstraints.insets = new Insets(INSET, 53, INSET, INSET);
    gridBagConstraints.gridy = 2;
    internetPanel.add(internetPeerButton, gridBagConstraints);
    gridBagConstraints.gridy = 3;
    internetPanel.add(internetDeviceButton, gridBagConstraints);
  }

  private void updateNetworkInterfacePanel()
  {
    networkInterfacePanel.removeAll();
    networkInterfacePanel.invalidate();

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(INSET, INSET, INSET, INSET);
    networkInterfacePanel.add(networkInterfaceButton, gridBagConstraints);

    networkInterfaceButtonList.clear();
    IForwarderModule[] forwarderModules = messageForwarderEntity.getMessageForwarder().getForwarderModules();
    int count = 0;
    for (int i = 0; i < forwarderModules.length; i++)
    {
      if (forwarderModules[i].isNetworkInterfaceForwarderModule())
      {
        SmoothButton networkInterfaceDescriptionButton =
          initDeviceButton("ModulID: " + forwarderModules[i].getModuleID(), null, "Ein lokales Netzwerkinterface");
        networkInterfaceDescriptionButton.setID(null);
        gridBagConstraints.insets = new Insets(INSET, INSET, INSET, INSET);
        gridBagConstraints.gridy = count * 2 + 1;
        networkInterfacePanel.add(networkInterfaceDescriptionButton, gridBagConstraints);
        SmoothButton networkInterfaceDeviceButton =
          initPropertyButton(ZERO_DEVICES, "Anzahl der Ger�te an diesem Interface");
        gridBagConstraints.insets = new Insets(INSET, 53, INSET, INSET);
        gridBagConstraints.gridy = count * 2 + 2;
        networkInterfacePanel.add(networkInterfaceDeviceButton, gridBagConstraints);

        networkInterfaceButtonList.add(new NetworkInterfaceButtons(forwarderModules[i].getModuleID(),
          networkInterfaceDeviceButton));

        count++;
      }
    }

    networkInterfacePanel.repaint();
  }

  private void updateGatewayPanel()
  {
    updateTunnelServerPanel();
    updateTunnelClientPanel();
    updateNetworkInterfacePanel();
    updateInternetPanel();

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(INSET, INSET, INSET, INSET);
    forwarderPanel.add(forwarderButton, gridBagConstraints);

    gridBagConstraints.insets = new Insets(10, 10, 10, 10);
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;

    // put forwarder to center
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    jGatewayPanel.add(forwarderPanel, gridBagConstraints);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridheight = 2;
    jGatewayPanel.add(networkInterfacePanel, gridBagConstraints);
    gridBagConstraints.gridheight = 1;
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    jGatewayPanel.add(tunnelServerPanel, gridBagConstraints);
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    jGatewayPanel.add(tunnelClientPanel, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    jGatewayPanel.add(internetPanel, gridBagConstraints);
  }

  public void actionPerformed(java.awt.event.ActionEvent e)
  {
    if (e.getActionCommand().equals(tunnelServerDescriptionButton.getID()))
    {
      if (tunnelServerDescriptionButton.isSelected())
      {
        messageForwarderEntity.stopTCPServerTunnelManagement();
        tunnelServerDescriptionButton.setSelected(false);
        updateTCPTunnelStats();
      } else
      {
        messageForwarderEntity.startTCPServerTunnelManagement();
        if (messageForwarderEntity.getTCPTunnelServerManagement() != null)
        {
          tunnelServerDescriptionButton.setSelected(true);
        }
      }
      tunnelServerButton.setSelected(tunnelServerDescriptionButton.isSelected());
    }
    if (e.getActionCommand().equals(tunnelClientDescriptionButton.getID()))
    {
      if (tunnelClientDescriptionButton.isSelected())
      {
        messageForwarderEntity.stopTCPClientTunnelManagement();
        tunnelClientDescriptionButton.setSelected(false);
        updateTCPTunnelStats();
      } else
      {
        messageForwarderEntity.startTCPClientTunnelManagement();
        if (messageForwarderEntity.getTCPTunnelClientManagement() != null)
        {
          tunnelClientDescriptionButton.setSelected(true);
        }
      }
      tunnelClientButton.setSelected(tunnelClientDescriptionButton.isSelected());
    }
    if (e.getActionCommand().equals(internetDescriptionButton.getID()))
    {
      if (internetDescriptionButton.isSelected())
      {
        messageForwarderEntity.stopInternetManagement();
        internetDescriptionButton.setSelected(false);
        updateTCPTunnelStats();
      } else
      {
        messageForwarderEntity.startInternetManagement();
        if (messageForwarderEntity.getInternetManagement() != null)
        {
          internetDescriptionButton.setSelected(true);
        }
      }
      internetButton.setSelected(internetDescriptionButton.isSelected());
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jBottomPanel;

  private javax.swing.JPanel jContentPanel;

  private javax.swing.JPanel jGatewayPanel;

  private javax.swing.JPanel jTopPanel;

  // End of variables declaration//GEN-END:variables

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#inetAddressGone(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    updateNetworkInterfacePanel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.util.INetworkInterfaceChangeListener#newInetAddress(java.net.NetworkInterface,
   *      java.net.InetAddress)
   */
  public void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    updateNetworkInterfacePanel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    System.out.println("New device: " + newDevice);
    updateDeviceCountStats();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    System.out.println("Removed device: " + goneDevice);
    updateDeviceCountStats();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceEvent(de.fhg.fokus.magic.upnp.control_point.CPDevice,
   *      int)
   */
  public void deviceEvent(CPDevice device, int eventCode, Object eventParameter)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelClientEventListener#newClient(java.net.InetSocketAddress)
   */
  public void newForwarderModule(IForwarderModule module)
  {
    updateTCPTunnelStats();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelClientEventListener#removedClient(java.net.InetSocketAddress)
   */
  public void removedForwarderModule(IForwarderModule module)
  {
    updateTCPTunnelStats();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.ITCPTunnelEventListener#disconnectedClient(de.fhg.fokus.magic.upnp.gateway.tcp_tunnel.TCPTunnelForwarderModule)
   */
  public void disconnectedForwarderModule(IForwarderModule module)
  {
    updateTCPTunnelStats();
  }

  public void reconnectedForwarderModule(IForwarderModule module)
  {
    updateTCPTunnelStats();
  }

  /** Updates the statistics for the TCP tunnel server. */
  private void updateTCPTunnelStats()
  {
    // Tunnel server
    if (messageForwarderEntity.getTCPTunnelServerManagement() != null)
    {
      int clientCount = messageForwarderEntity.getTCPTunnelServerManagement().getConnectedClientCount();
      int disconnectedClientCount = messageForwarderEntity.getTCPTunnelServerManagement().getDisconnectedClientCount();

      tunnelServerClientsButton.setText((clientCount == 1 ? "1 Client" : clientCount + " Clients") + " verbunden, " +
        (disconnectedClientCount == 1 ? "1 Client " : disconnectedClientCount + " Clients ") + " reserviert");
    } else
    {
      tunnelServerClientsButton.setText("0 Clients");
    }
    // Tunnel client
    if (messageForwarderEntity.getTCPTunnelClientManagement() != null)
    {
      if (messageForwarderEntity.getTCPTunnelClientManagement().getTCPTunnelForwarderModule().isConnected())
      {
        tunnelClientStatusButton.setText("Verbunden");
      } else
      {
        tunnelClientStatusButton.setText("Nicht verbunden");
      }
    } else
    {
      tunnelClientStatusButton.setText("Nicht verbunden");
    }
  }

  /** Updates the client count statistics. */
  private void updateDeviceCountStats()
  {
    // devices at local network interfaces
    for (int i = 0; i < networkInterfaceButtonList.size(); i++)
    {
      NetworkInterfaceButtons currentButtons = (NetworkInterfaceButtons)networkInterfaceButtonList.elementAt(i);
      Vector deviceList = getDevicesForForwarderModule(currentButtons.moduleID);
      currentButtons.deviceCountButton.setText(deviceList.size() == 1 ? ONE_DEVICE : deviceList.size() + DEVICES);

      currentButtons.deviceCountButton.setToolTipText(getDeviceNamesFromDeviceList(deviceList));
    }
    // devices at tunnel server
    if (messageForwarderEntity.getTCPTunnelServerManagement() != null)
    {
      Vector deviceList = messageForwarderEntity.getTCPTunnelServerManagement().getDevices();
      tunnelServerDeviceButton.setText(deviceList.size() == 1 ? ONE_DEVICE : deviceList.size() + DEVICES);

      tunnelServerDeviceButton.setToolTipText(getDeviceNamesFromDeviceList(deviceList));
    } else
    {
      tunnelServerDeviceButton.setText(ZERO_DEVICES);
      tunnelServerDeviceButton.setToolTipText("");
    }
    // devices at tunnel client
    if (messageForwarderEntity.getTCPTunnelClientManagement() != null &&
      messageForwarderEntity.getTCPTunnelClientManagement().getTCPTunnelForwarderModule() != null)
    {
      Vector deviceList =
        getDevicesForForwarderModule(messageForwarderEntity.getTCPTunnelClientManagement()
          .getTCPTunnelForwarderModule()
          .getModuleID());
      tunnelClientDeviceButton.setText(deviceList.size() == 1 ? ONE_DEVICE : deviceList.size() + DEVICES);

      tunnelClientDeviceButton.setToolTipText(getDeviceNamesFromDeviceList(deviceList));
    } else
    {
      tunnelClientDeviceButton.setText(ZERO_DEVICES);
      tunnelClientDeviceButton.setToolTipText("");
    }
  }

  private class NetworkInterfaceButtons
  {

    public String       moduleID;

    public SmoothButton deviceCountButton;

    public NetworkInterfaceButtons(String moduleID, SmoothButton deviceCountButton)
    {
      this.moduleID = moduleID;
      this.deviceCountButton = deviceCountButton;
    }

  }

}

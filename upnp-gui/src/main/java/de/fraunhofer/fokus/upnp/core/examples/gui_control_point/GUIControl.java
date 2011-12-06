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
package de.fraunhofer.fokus.upnp.core.examples.gui_control_point;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core.control_point.ISubscriptionPolicyListener;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.examples.gui_common.GUIConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceTranslations;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.IDeviceTranslationManager;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.PluginManager;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ScrollableComponentPanel;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.SecurityGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ThemeDeviceOverviewPanel;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.VisibilitySimulation;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels.JGUIControlBottomPanel;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BaseCPServicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BasePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.ImageRendererPlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.MediaServerPlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.SecuredDevicePlugin;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.SecurityAwareControlPointPlugin;
import de.fraunhofer.fokus.upnp.core.examples.localization.LocalizationConstant;
import de.fraunhofer.fokus.upnp.core.examples.localization.buttons.ButtonLocalizationEntity;
import de.fraunhofer.fokus.upnp.core.examples.vehicle.active_safety.ActiveSafetyConstant;
import de.fraunhofer.fokus.upnp.core.xml.ControlPointStartupConfiguration;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.core_av.UPnPAVConstant;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_security.helpers.LocalDictionaryObject;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.ISecurityConsoleEvents;
import de.fraunhofer.fokus.upnp.core_security.securityConsole.SecurityConsoleEntity;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateEntity;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKeyLoader;
import de.fraunhofer.fokus.upnp.util.security.PersonalizedKeyObject;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.DialogValueInvocation;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothDescriptionButton;

/**
 * This class represents a graphical UPnP control point and security console.
 * 
 * @author Alexander Koenig
 */
public class GUIControl extends javax.swing.JFrame implements
  ICPDeviceEventListener,
  ICPStateVariableListener,
  ActionListener,
  ISecurityConsoleEvents,
  IDeviceTranslationManager,
  IDeviceGUIContextProvider,
  Runnable,
  ISubscriptionPolicyListener,
  ComponentListener
{

  private static final long               serialVersionUID                = 1L;

  public static final long                INFO_DELAY_TIME                 = 5000;

  public static final String              ANONYMOUS_USER                  = "Anonym";

  public static final String              ERROR                           = "Es ist ein Fehler aufgetreten: ";

  public static final String              GO_TO_SECURITY                  = "GoToSecurity";

  public static final String              GO_TO_SECURITY_CONTROL_POINTS   = "GoToSecurityControlPoints";

  public static final String              GO_TO_SECURITY_DEVICES          = "GoToSecurityDevices";

  private static final String             GO_TO_OVERVIEW                  = "GoToOverview";

  private static final String             EXIT                            = "ExitApp";

  private static final String             INFO_NEW_DEVICE                 = "Neues Gerät gefunden: ";

  private static final String             RESIZE_USER_PANEL               = "resize";

  public static final int                 STATE_DEVICE_OVERVIEW           = 1;

  public static final int                 STATE_SECURITY                  = 2;

  public static final int                 STATE_DEVICE_SECURITY           = 3;

  public static final int                 STATE_CP_SECURITY               = 4;

  public static final int                 STATE_DEVICE                    = 10;

  public static final int                 STATE_DEVICE_PLUGIN             = 11;

  public static final int                 STATE_SERVICE_PLUGIN            = 12;

  public static final int                 STATE_IMAGE_RENDERER            = 20;

  /** UDN of device that is used for localization (default is atlas) */
  public static String                    LOCALIZATION_DEVICE_UDN         = "uuid:4b029406-fdc0-593b-9852-4ce8c12bf645";

  /** Address of RFID device that is used for personalization */
  public static String                    RFID_READER_ADDRESS             = "10.147.65.74";

  /** Height of user panel */
  public static int                       SELECT_USER_PANEL_HEIGHT        = 65;

  /** Control point or security console for this GUI */
  private SecurityConsoleEntity           securityConsole;

  private SecurityAwareTemplateEntity     securityAwareEntity;

  /** Simulation of visibility */
  private VisibilitySimulation            visibilitySimulation;

  /** List with all buttons needed to display active safety messages */
  private Vector                          activeSafetyButtonList          = new Vector();

  /** Flag to update current active safety messages */
  private boolean                         updateActiveSafetyButtons       = false;

  // Common controls
  private SmoothButton                    searchButton;

  private SmoothButton                    infoButton;

  private SmoothButton                    forceSearchButton;

  private SmoothButton                    addDeviceButton;

  private SmoothButton                    upButton;

  private JGUIControlBottomPanel          bottomPanel;

  private JPanel                          commandsPanel;

  private SmoothDescriptionButton         userButton;

  // Theme buttons and colors
  private Color                           serverColor                     = GUIConstants.GENRE_MEDIA_SERVER_COLOR;

  private Color                           rendererColor                   = GUIConstants.GENRE_MEDIA_RENDERER_COLOR;

  private Color                           personalizationColor            = GUIConstants.GENRE_PERSONALIZATION_COLOR;

  private Color                           homeControlColor                = GUIConstants.GENRE_HOME_CONTROL_COLOR;

  private Color                           sensorColor                     = GUIConstants.GENRE_SENSOR_COLOR;

  private Color                           messagingColor                  = GUIConstants.GENRE_MESSAGING_COLOR;

  private Color                           miscColor                       = GUIConstants.GENRE_MISC_COLOR;

  private Color                           activeSafetyColor               = GUIConstants.GENRE_ACTIVE_SAFETY_COLOR;

  // Theme panels
  private ThemeDeviceOverviewPanel        serverPanel;

  private ThemeDeviceOverviewPanel        rendererPanel;

  private ThemeDeviceOverviewPanel        personalizationPanel;

  private ThemeDeviceOverviewPanel        homeControlPanel;

  private ThemeDeviceOverviewPanel        sensorPanel;

  private ThemeDeviceOverviewPanel        messagingPanel;

  private ThemeDeviceOverviewPanel        miscPanel;

  private ScrollableComponentPanel        selectUserPanel;

  private JPanel                          activeSafetyPanel;

  private long                            infoUpdateTime;

  private long                            searchTime;

  private long                            debugUpdateTime;

  private Image                           warningImage;

  private int                             currentState                    = STATE_DEVICE_OVERVIEW;

  /** List of device context objects for all discovered devices */
  private Vector                          deviceContextList               = new Vector();

  /* Security */
  private SecurityGUIContext              securityContext                 = null;

  // Plugin for secured device management
  private SecuredDevicePlugin             securedDevicePlugin             = null;

  private SecurityAwareControlPointPlugin securityAwareControlPointPlugin = null;

  /* Internal devices */
  private ImageRendererPlugin             imageRendererPlugin             = null;

  private MediaRendererCPDevice           imageRendererCPDevice;

  private SmoothButton                    imageRendererButton;

  private SmoothButton                    imageRendererOverviewButton;

  /* Device and service handling */
  private PluginManager                   pluginManager                   = null;

  private DeviceGUIContext                currentDeviceContext            = null;

  private SmoothButton                    currentServiceButton            = null;

  private BaseCPServicePlugin             currentServicePlugin            = null;

  private UPnPStartupConfiguration        startupConfiguration;

  private String                          entityID;

  // checks if this GUI works as simple control point or also as security console (via args[])
  private boolean                         useSecurityConsole              = true;

  private boolean                         terminateThread                 = false;

  private boolean                         terminated                      = false;

  /** Creates new form GUIControl */
  public GUIControl(String args[])
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("ScrollBarUI", "de.fraunhofer.fokus.upnp.util.swing.SmoothScrollbar");
      UIManager.put("ComboBoxUI", "de.fraunhofer.fokus.upnp.util.swing.SmoothComboBox");
    } catch (Exception e)
    {
      System.err.println("Error replacing scrollbar. " + e.getMessage());
    }
    // init prior to building initial layout
    activeSafetyPanel = initThemePanel(activeSafetyColor);
    initComponents();

    ToolTipManager.sharedInstance().setDismissDelay(10000);

    jContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jPluginPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    jDeviceOverviewPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDeviceConfigPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDeviceOverviewLeftPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDeviceOverviewRightPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDeviceOverviewMiddlePanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDeviceOverviewPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    jTopPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));

    // Bottom panel for GUI
    bottomPanel = new JGUIControlBottomPanel();
    jBottomPanel.setLayout(new GridBagLayout());

    commandsPanel = new JPanel();
    commandsPanel.setBorder(new SmoothBorder());
    commandsPanel.setLayout(new GridBagLayout());
    commandsPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // create additional buttons and panels
    searchButton = new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, 40), "Suche Geräte...", null);
    upButton = new SmoothButton(new Dimension(40, 45), "..", GO_TO_OVERVIEW);
    upButton.setButtonColor(GUIConstants.MANAGEMENT_BUTTON_COLOR);
    upButton.setBackground(jTopPanel.getBackground());
    upButton.addActionListener(this);

    userButton =
      new SmoothDescriptionButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, 45),
        18,
        12,
        "Unbekannt",
        "Unterwegs",
        RESIZE_USER_PANEL);
    userButton.addActionListener(this);
    userButton.setDisabledButtonColor(ButtonConstants.BUTTON_COLOR);
    userButton.setBackground(jTopPanel.getBackground());

    // update color for top and bottom panels
    updateFramePanelColor(ButtonConstants.DARK_BACKGROUND_COLOR);

    // create theme panels
    serverPanel = new ThemeDeviceOverviewPanel("AV Server", serverColor, 3);
    rendererPanel = new ThemeDeviceOverviewPanel("AV Renderer", rendererColor, 3);
    personalizationPanel = new ThemeDeviceOverviewPanel("Personalisierung", personalizationColor, 2);
    homeControlPanel = new ThemeDeviceOverviewPanel("Heimautomatisierung", homeControlColor, 3);
    sensorPanel = new ThemeDeviceOverviewPanel("Sensorik", sensorColor, 3);
    messagingPanel = new ThemeDeviceOverviewPanel("Kommunikation", messagingColor, 2);
    miscPanel = new ThemeDeviceOverviewPanel("Verschiedenes", miscColor, 8);
    selectUserPanel =
      new ScrollableComponentPanel(userButton, personalizationColor, 200, ButtonConstants.BUTTON_HEIGHT + 2, 2);
    selectUserPanel.setBackground(ButtonConstants.DARK_BACKGROUND_COLOR);
    selectUserPanel.setPreferredSize(new Dimension(380, SELECT_USER_PANEL_HEIGHT));

    System.out.println("Host name is " + IPHelper.getLocalHostName());
    // load startup configuration
    String startupFileName = args.length > 0 ? args[0] : FileHelper.getStartupConfigurationName("GUIControl");
    startupConfiguration = new UPnPStartupConfiguration(startupFileName);
    startupConfiguration.setStartKeyboardThread(false);
    if (!startupConfiguration.isValid())
    {
      System.out.println("Invalid or missing startup configuration.");
      System.exit(1);
    }
    startupConfiguration.trySetValidWorkingDirectory(this.getClass().getName());
    String workingDirectory = startupConfiguration.getWorkingDirectory();

    warningImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "warning.png");

    // init user and owner from startup info
    entityID = startupConfiguration.getProperty("EntityID", "Unknown");

    System.out.println("EntityID for this control point is " + entityID);

    // create command buttons
    forceSearchButton = new SmoothButton(new Dimension(50, 50), 12, "", "forceSearch");
    forceSearchButton.setAutomaticTooltip(false);
    forceSearchButton.setToolTipText("Neue Suche");
    forceSearchButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    Image searchImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "lupe.gif");
    if (searchImage != null && searchImage.getWidth(null) != 0)
    {
      forceSearchButton.setIconImage(searchImage);
    }
    forceSearchButton.addActionListener(this);

    addDeviceButton = new SmoothButton(new Dimension(50, 50), 12, "", "addDevice");
    addDeviceButton.setAutomaticTooltip(false);
    addDeviceButton.setToolTipText("Gerät manuell hinzufügen");
    addDeviceButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    Image addDeviceImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "new_device_icon.gif");
    if (addDeviceImage != null && addDeviceImage.getWidth(null) != 0)
    {
      addDeviceButton.setIconImage(addDeviceImage);
    }
    addDeviceButton.addActionListener(this);

    infoButton = new SmoothButton(new Dimension(50, 50), 12, "", null);
    infoButton.setAutomaticTooltip(false);
    infoButton.setToolTipText("");
    infoButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    Image infoImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "info.gif");
    if (infoImage != null && infoImage.getWidth(null) != 0)
    {
      infoButton.setIconImage(infoImage);
    }

    imageRendererOverviewButton = new SmoothButton(new Dimension(50, 50), 12, "", "showInternalImageRenderer");
    imageRendererOverviewButton.setAutomaticTooltip(false);
    imageRendererOverviewButton.setToolTipText("Interner Bildbetrachter");
    imageRendererOverviewButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    Image rendererImage = Toolkit.getDefaultToolkit().createImage(workingDirectory + "image_icon.gif");
    if (rendererImage != null && rendererImage.getWidth(null) != 0)
    {
      imageRendererOverviewButton.setIconImage(rendererImage);
    }
    imageRendererOverviewButton.addActionListener(this);

    imageRendererButton =
      new SmoothButton(new Dimension(ButtonConstants.DEVICE_BUTTON_WIDTH, 40), "Interner UPnP-Bildbetrachter", null);
    imageRendererButton.setButtonColor(ButtonConstants.DEVICE_BUTTON_COLOR);
    imageRendererButton.setDisabledButtonColor(imageRendererButton.getButtonColor());
    imageRendererButton.setSelectable(false);

    // load known users
    loadKnownUsers();

    // create plugin manager
    pluginManager =
      new PluginManager(startupConfiguration.getProperty("PluginPath",
        "de/fraunhofer/fokus/upnp/core/examples/gui_control_point/plugins"));

    // create visibility simulation
    visibilitySimulation = new VisibilitySimulation(this);

    // update if needed
    LOCALIZATION_DEVICE_UDN = startupConfiguration.getProperty("LocalizationDeviceUDN", LOCALIZATION_DEVICE_UDN);

    // create security console or control point
    useSecurityConsole = startupConfiguration.getBooleanProperty("UseSecurityConsole");
    if (useSecurityConsole)
    {
      setTitle(getTitle() + "(" + IPHelper.getSimpleLocalHostName() + ", " +
        startupConfiguration.getSSDPMulticastAddress() + ":" + startupConfiguration.getSSDPMulticastPort() + ")");

      // start security console
      securityConsole = new SecurityConsoleEntity(startupConfiguration);

      // forward UPnP events to GUI
      securityConsole.setCPDeviceEventListener(this);
      securityConsole.setCPStateVariableListener(this);
      securityConsole.setSecurityEventsListener(this);

      // create security context
      securityContext = new SecurityGUIContext(this);
      securityContext.addActionListener(this);
      // create plugin for secured device management
      securedDevicePlugin = new SecuredDevicePlugin(this, securityContext, this);
      // create plugin for security aware control point management
      securityAwareControlPointPlugin = new SecurityAwareControlPointPlugin(this, securityContext);

      // create plugin for ImageRenderer
      imageRendererPlugin = new ImageRendererPlugin(this, startupConfiguration);

      securityConsole.getTemplateControlPoint().setSubscriptionPolicyListener(this);

      // start control point of security console
      securityConsole.getTemplateControlPoint().runBasicControlPoint();
    } else
    {
      // start control point entity
      securityAwareEntity = new SecurityAwareTemplateEntity(startupConfiguration);

      // security aware control points are not started immediately
      securityAwareEntity.setTemplateControlPoint(new SecurityAwareTemplateControlPoint(securityAwareEntity,
        startupConfiguration));

      // forward UPnP events to GUI
      securityAwareEntity.setCPDeviceEventListener(this);
      securityAwareEntity.setCPStateVariableListener(this);

      // create plugin for ImageRenderer
      imageRendererPlugin = new ImageRendererPlugin(this, startupConfiguration);

      securityAwareEntity.getTemplateControlPoint().setSubscriptionPolicyListener(this);

      // start control point after events have been wired to this class
      securityAwareEntity.getTemplateControlPoint().runBasicControlPoint();
    }

    // add listener for active safety devices
    getSecurityAwareControlPoint().addSpecialDeviceClass(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE);
    if (getSecurityAwareControlPoint().getPersonalizationUser().equals(UPnPConstant.USER_UNKNOWN))
    {
      userButton.setText(ANONYMOUS_USER);
    } else
    {
      userButton.setText(getSecurityAwareControlPoint().getPersonalizationUser());
      userChanged(getSecurityAwareControlPoint().getPersonalizationUser());
    }

    // update view
    this.setSize(1024, 768);
    updateGUI();
    initCommandsPanel();
    initDeviceOverviewPanel();
    initBottomPanel();

    addComponentListener(this);
    searchButton.requestFocus();
    Thread guiControlThread = new Thread(this);
    guiControlThread.setName("GUIControl.Main");
    guiControlThread.start();
  }

  /** Loads known user names from the file system. */
  private void loadKnownUsers()
  {
    Vector userList = new Vector();
    userList.add(ANONYMOUS_USER);
    Vector userButtonList = new Vector();
    // loads known users
    Vector keyCacheData = PersistentRSAPublicKeyLoader.loadRSAPublicKeys(true);
    for (int i = 0; i < keyCacheData.size(); i++)
    {
      PersonalizedKeyObject personalizedKeyObject = (PersonalizedKeyObject)keyCacheData.elementAt(i);
      userList.add(personalizedKeyObject.getName());
    }

    // create button for each user
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(1, 1, 1, 1);

    for (int i = 0; i < userList.size(); i++)
    {
      SmoothButton currentButton =
        new SmoothButton(new Dimension(150, ButtonConstants.BUTTON_HEIGHT),
          12,
          userList.elementAt(i).toString(),
          GUIControlConstants.SELECT_USER);

      currentButton.setBackground(ButtonConstants.DARK_BACKGROUND_COLOR);
      currentButton.addActionListener(this);
      userButtonList.add(currentButton);
      selectUserPanel.addComponent(currentButton, gridBagConstraints);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jTopPanel = new javax.swing.JPanel();
    jTopDeviceNamePanel = new javax.swing.JPanel();
    jTopMiddleFillPanel = new javax.swing.JPanel();
    jTopServicePanel = new javax.swing.JPanel();
    jTopHelperServicePanel = new javax.swing.JPanel();
    jContentPanel = new javax.swing.JPanel();
    jFillPanel = new javax.swing.JPanel();
    jDeviceOverviewPanel = new javax.swing.JPanel();
    jDeviceOverviewLeftPanel = new javax.swing.JPanel();
    jDeviceOverviewRightPanel = new javax.swing.JPanel();
    jDeviceConfigPanel = new javax.swing.JPanel();
    jDeviceOverviewMiddlePanel = new javax.swing.JPanel();
    jPluginPanel = new javax.swing.JPanel();
    jBottomPanel = new javax.swing.JPanel();
    jInfoLabel = new javax.swing.JLabel();

    getContentPane().setLayout(new java.awt.GridBagLayout());

    setTitle("Gerätekontrolle");
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

    jTopDeviceNamePanel.setLayout(new java.awt.GridBagLayout());
    jTopDeviceNamePanel.setBackground(new java.awt.Color(153, 153, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    jTopPanel.add(jTopDeviceNamePanel, gridBagConstraints);

    jTopServicePanel.setLayout(new java.awt.GridBagLayout());
    jTopServicePanel.setBackground(new java.awt.Color(153, 153, 255));
    gridBagConstraints.gridx = 1;
    jTopPanel.add(jTopServicePanel, gridBagConstraints);

    jTopMiddleFillPanel.setLayout(new java.awt.GridBagLayout());
    jTopMiddleFillPanel.setBackground(new java.awt.Color(153, 153, 255));
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 2;
    gridBagConstraints.weightx = 1.0;
    jTopPanel.add(jTopMiddleFillPanel, gridBagConstraints);

    jTopHelperServicePanel.setLayout(new java.awt.GridBagLayout());
    jTopHelperServicePanel.setBackground(new java.awt.Color(153, 153, 255));
    gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
    gridBagConstraints.gridx = 3;
    gridBagConstraints.weightx = 0;
    jTopPanel.add(jTopHelperServicePanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    getContentPane().add(jTopPanel, gridBagConstraints);

    jContentPanel.setLayout(new java.awt.GridBagLayout());

    jContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    jContentPanel.setMinimumSize(new java.awt.Dimension(300, 100));
    jContentPanel.setPreferredSize(new java.awt.Dimension(300, 100));
    jDeviceOverviewPanel.setLayout(new java.awt.GridBagLayout());

    jDeviceOverviewPanel.setBackground(new java.awt.Color(204, 204, 255));
    // add left device overview panel
    jDeviceOverviewLeftPanel.setLayout(new java.awt.GridBagLayout());
    jDeviceOverviewLeftPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jDeviceOverviewPanel.add(jDeviceOverviewLeftPanel, gridBagConstraints);

    // add right device overview panel
    jDeviceOverviewRightPanel.setLayout(new java.awt.GridBagLayout());
    jDeviceOverviewRightPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jDeviceOverviewPanel.add(jDeviceOverviewRightPanel, gridBagConstraints);

    // add middle device overview panel
    jDeviceOverviewMiddlePanel.setLayout(new java.awt.GridBagLayout());
    jDeviceOverviewMiddlePanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jDeviceOverviewPanel.add(jDeviceOverviewMiddlePanel, gridBagConstraints);

    // add device config panel
    jDeviceConfigPanel.setLayout(new java.awt.GridBagLayout());

    jDeviceConfigPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jDeviceOverviewPanel.add(jDeviceConfigPanel, gridBagConstraints);

    // add device overview panel to content panel
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jDeviceOverviewPanel, gridBagConstraints);

    // add plugin panel to content panel
    jPluginPanel.setLayout(new java.awt.GridBagLayout());
    jPluginPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jPluginPanel, gridBagConstraints);

    // add fill panel to content panel
    jFillPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jContentPanel.add(jFillPanel, gridBagConstraints);

    // add active safety panel to content panel
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new Insets(5, 5, 0, 5);
    gridBagConstraints.weightx = 1.0;
    jContentPanel.add(activeSafetyPanel, gridBagConstraints);

    // add content panel to frame
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(jContentPanel, gridBagConstraints);

    jBottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jBottomPanel.setBackground(new java.awt.Color(153, 153, 255));
    jInfoLabel.setFont(new java.awt.Font("Serif", 0, 12));
    jBottomPanel.add(jInfoLabel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    getContentPane().add(jBottomPanel, gridBagConstraints);

    pack();
  }// GEN-END:initComponents

  private void formComponentResized(java.awt.event.ComponentEvent evt)
  {// GEN-FIRST:event_formComponentResized
  }// GEN-LAST:event_formComponentResized

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt)
  {// GEN-FIRST:event_exitForm
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(50);
      } catch (Exception ex)
      {
      }
    }
    if (useSecurityConsole)
    {
      securityConsole.terminate();
    } else
    {
      securityAwareEntity.terminate();
    }

    if (securedDevicePlugin != null)
    {
      securedDevicePlugin.terminate();
    }
    if (securityAwareControlPointPlugin != null)
    {
      securityAwareControlPointPlugin.terminate();
    }
    imageRendererPlugin.terminate();

    System.exit(0);
  }// GEN-LAST:event_exitForm

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new GUIControl(args).setVisible(true);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // UPnP events //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#userChanged(java.lang.String)
   */
  public void userChanged(String userName)
  {
    for (int i = 0; i < getDeviceContextCount(); i++)
    {
      getDeviceContext(i).userChanged(userName);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {
    // System.out.println("New device: " + newDevice.getFriendlyName());
    // try to find internal image renderer as CPDevice
    if (newDevice.getUDN().equals(imageRendererPlugin.getImageRendererEntity().getImageRendererDevice().getUDN()))
    {
      imageRendererCPDevice = new MediaRendererCPDevice(getSecurityAwareControlPoint(), newDevice);
    }

    // directly process active safety devices
    if (newDevice.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE))
    {
      updateActiveSafetyButtons = true;
    }

    // add to list of known devices
    if (!isKnownDevice(newDevice.getUDN()))
    {
      DeviceGUIContext deviceContext = new DeviceGUIContext(this, newDevice);

      deviceContext.addActionListener(this);
      deviceContextList.addElement(deviceContext);
      // do not update the top panel
      updateGUI(false);
    }

    // inform all other device context objects about new device
    for (int i = 0; i < getDeviceContextCount(); i++)
    {
      getDeviceContext(i).newDevice(newDevice);
    }
    // update info label
    if (!VisibilitySimulation.ACTIVATE_SIMULATION)
    {
      bottomPanel.setText(INFO_NEW_DEVICE + newDevice.getFriendlyName());
      infoUpdateTime = System.currentTimeMillis();
    }
    updateDebugString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {
    if (isKnownDevice(goneDevice.getUDN()))
    {
      boolean fullUpdate = false;
      // set device context to invalid
      getDeviceContext(getDeviceIndex(goneDevice.getUDN())).setValid(false);

      // inform all device context objects about removed device
      for (int i = 0; i < getDeviceContextCount(); i++)
      {
        getDeviceContext(i).deviceGone(goneDevice);
      }

      // leave gone device if currently selected
      if (currentDeviceContext != null && goneDevice == currentDeviceContext.getDevice())
      {
        currentDeviceContext = null;
        currentServiceButton = null;
        currentServicePlugin = null;
        currentState = STATE_DEVICE_OVERVIEW;
        fullUpdate = true;
      }
      // terminate device context
      getDeviceContext(getDeviceIndex(goneDevice.getUDN())).terminate();

      // remove from context list
      int index = getDeviceIndex(goneDevice.getUDN());
      deviceContextList.removeElementAt(index);

      if (goneDevice.getDeviceType().equals(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE))
      {
        updateActiveSafetyButtons = true;
      }
      // prevent double update
      updateGUI(fullUpdate);
    }
    updateDebugString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceEvent(de.fhg.fokus.magic.upnp.control_point.CPDevice,
   *      int)
   */
  public void deviceEvent(CPDevice device, int eventCode, Object eventParameter)
  {
    if (isKnownDevice(device.getUDN()))
    {
      // inform all device context objects about event
      for (int i = 0; i < getDeviceContextCount(); i++)
      {
        getDeviceContext(i).deviceEvent(device, eventCode, eventParameter);
      }
      // update GUI overview because order can change with translated friendly names
      if (eventCode == UPnPConstant.DEVICE_EVENT_TRANSLATION_SERVICE_READ && currentState == STATE_DEVICE_OVERVIEW)
      {
        updateGUI(false);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    // System.out.println("State variable " + stateVariable.getName() + " changed to: " +
    // stateVariable.getValue());

    // forward event to all device context classes
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      ((DeviceGUIContext)deviceContextList.elementAt(i)).stateVariableChanged(stateVariable);
    }

    securedDevicePlugin.stateVariableChanged(stateVariable);

    // handle localization
    if (stateVariable.getCPService().getCPDevice().getUDN().equals(LOCALIZATION_DEVICE_UDN) &&
      stateVariable.getName().equals("CurrentEventID"))
    {
      processLocalizationEvent(stateVariable);
      setUserPreferencesForLocation();
    }

    // handle personalization
    if (stateVariable.getCPService().getCPDevice().getFriendlyName().equals("RFIDReader") &&
      stateVariable.getCPService().getCPDevice().getDeviceAddress().getHostAddress().equals(RFID_READER_ADDRESS) &&
      stateVariable.getName().equals("CurrentTagID"))
    {
      System.out.println("Process personalization event");
      processPersonalizationEvent(stateVariable);
      setUserPreferencesForLocation();
    }
  }

  /** Processes state variable events for localization. */
  private void processLocalizationEvent(CPStateVariable stateVariable)
  {
    // System.out.println("Received event needed for localization");
    try
    {
      // request position for current user
      CPAction getLocation = stateVariable.getCPService().getCPAction("GetLocation");
      getLocation.getInArgument("User").setValue(entityID);

      getSecurityAwareControlPoint().invokeAction(getLocation);

      String location = getLocation.getOutArgument("Location").getStringValue();

      System.out.println("Location for user is " + location);
      visibilitySimulation.setPosition(getLocation.getOutArgument("Location").getStringValue());

      // update position for all missing users
      for (int i = 0; i < ButtonLocalizationEntity.ENTITY_IDS.length; i++)
      {
        String currentEntityID = ButtonLocalizationEntity.ENTITY_IDS[i];
        if (!currentEntityID.equals(entityID) && !visibilitySimulation.getPositionTable().containsKey(currentEntityID))
        {
          try
          {
            getLocation.getInArgument("User").setValue(currentEntityID);

            getSecurityAwareControlPoint().invokeAction(getLocation);

            location = getLocation.getOutArgument("Location").getStringValue();

            System.out.println("Set initial position for entityID " + currentEntityID + " to " + location);
            visibilitySimulation.getPositionTable().put(currentEntityID, location);
          } catch (Exception e)
          {
            System.out.println("Could not request position for entityID " + currentEntityID + ":" + e.getMessage());
          }
        }
      }

      // update position for eventID
      try
      {
        CPAction getEvent = stateVariable.getCPService().getCPAction("GetEvent");
        getEvent.getInArgument("EventID").setNumericValue(stateVariable.getNumericValue());

        getSecurityAwareControlPoint().invokeAction(getEvent);

        String eventEntityID = getEvent.getOutArgument("User").getStringValue();
        location = getEvent.getOutArgument("Location").getStringValue();

        System.out.println("Update position for entityID " + eventEntityID + " to " + location);
        visibilitySimulation.getPositionTable().put(eventEntityID, location);
      } catch (Exception e)
      {
        System.out.println("Could not request position for new event ID " + ":" + e.getMessage());
      }

      userButton.setDescription("Unterwegs");
      if (visibilitySimulation.getPosition().equals("TrafficLights"))
      {
        userButton.setDescription("Ampel");
      }
      if (visibilitySimulation.getPosition().equals("Home"))
      {
        userButton.setDescription("Zu Hause");
      }
      if (visibilitySimulation.getPosition().equals("RSU"))
      {
        userButton.setDescription("Enge Kurve");
      }
      if (visibilitySimulation.getPosition().equals("Accident"))
      {
        userButton.setDescription("Unfallstelle");
      }

      // leave currently selected device if its now invisible
      if (VisibilitySimulation.ACTIVATE_SIMULATION)
      {
        if (currentDeviceContext != null && !visibilitySimulation.isVisibleDevice(currentDeviceContext))
        {
          currentDeviceContext = null;
          currentServiceButton = null;
          currentServicePlugin = null;
          currentState = STATE_DEVICE_OVERVIEW;
          updateGUI(true);
        } else
        {
          updateGUI(false);
        }
        // inform all other device context objects about new visibility
        for (int i = 0; i < getDeviceContextCount(); i++)
        {
          getDeviceContext(i).visibilityChanged();
        }
        updateActiveSafetyButtons = true;
      }
    } catch (Exception e)
    {
      System.out.println("Could not request position for user");
    }
  }

  /** Processes state variable events for personalization. */
  private void processPersonalizationEvent(CPStateVariable stateVariable)
  {
    String user = null;
    try
    {
      String tagID = stateVariable.getStringValue();
      if (tagID.equals("E0070000020C1F0E") &&
        !getSecurityAwareControlPoint().getPersonalizationUser().equals("Sebastian Nauck"))
      {
        user = "Sebastian Nauck";
      }
      if (tagID.equals("E0070000020C1F0F") &&
        !getSecurityAwareControlPoint().getPersonalizationUser().equals("Alexander König"))
      {
        user = "Alexander König";
      }
      if (user != null)
      {
        getSecurityAwareControlPoint().setPersonalizationUser(user);
        userButton.setText(user);
        System.out.println("Changed user to " + user);

        // update profile in selected device
        if (currentDeviceContext != null)
        {
          getSecurityAwareControlPoint().tryPersonalizeDevice(currentDeviceContext.getDevice());
        }
      }
    } catch (Exception e)
    {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Processes state variable events for personalization. */
  private void setUserPreferencesForLocation()
  {
    // yellow car is home
    if (entityID.equals(ButtonLocalizationEntity.ENTITY_IDS[1]) &&
      visibilitySimulation.getPosition().equalsIgnoreCase(ButtonLocalizationEntity.LOCATIONS[0]))
    {
      System.out.println("Process user preferences for " + entityID);
      int dimmerValue = getSecurityAwareControlPoint().getPersonalizationUser().equals("Alexander König") ? 100 : 50;

      int deviceIndex = getDeviceIndex("uuid:EZControl1.0_10.147.175.145");
      if (deviceIndex != -1)
      {
        CPDevice currentDevice = getDevice(deviceIndex);
        CPService dimmingService = currentDevice.getCPServiceByID("urn:upnp-org:serviceId:Lock4006DimmerLight1.0");
        if (dimmingService != null)
        {
          System.out.println("Found dimming service");
          try
          {
            CPAction setLoadLevelTarget = dimmingService.getCPAction("SetLoadLevelTarget");
            setLoadLevelTarget.getInArgument("NewLoadLevelTarget").setNumericValue(dimmerValue);
            getSecurityAwareControlPoint().invokeAction(setLoadLevelTarget);
          } catch (Exception e)
          {
            System.out.println("Error updating profile in device " + currentDevice.getFriendlyName() + ":" +
              e.getMessage());
          }
        }
      } else
      {
        System.out.println("EZControl device not found");
      }
      deviceIndex = getDeviceIndex("uuid:DemoRoom1.0_10.147.175.145");
      if (deviceIndex != -1)
      {
        CPDevice currentDevice = getDevice(deviceIndex);
        CPService dimmingService = currentDevice.getCPServiceByID("urn:upnp-org:serviceId:BackLightDimming1.0");
        if (dimmingService != null)
        {
          System.out.println("Found dimming service");
          try
          {
            CPAction setLoadLevelTarget = dimmingService.getCPAction("SetLoadLevelTarget");
            setLoadLevelTarget.getInArgument("NewLoadLevelTarget").setNumericValue(dimmerValue);
            getSecurityAwareControlPoint().invokeAction(setLoadLevelTarget);
          } catch (Exception e)
          {
            System.out.println("Error updating profile in device " + currentDevice.getFriendlyName() + ":" +
              e.getMessage());
          }
        }
        dimmingService = currentDevice.getCPServiceByID("urn:upnp-org:serviceId:FrontLightDimming1.0");
        if (dimmingService != null)
        {
          System.out.println("Found dimming service");
          try
          {
            CPAction setLoadLevelTarget = dimmingService.getCPAction("SetLoadLevelTarget");
            setLoadLevelTarget.getInArgument("NewLoadLevelTarget").setNumericValue(dimmerValue);
            getSecurityAwareControlPoint().invokeAction(setLoadLevelTarget);
          } catch (Exception e)
          {
            System.out.println("Error updating profile in device " + currentDevice.getFriendlyName() + ":" +
              e.getMessage());
          }
        }

      }

    }
  }

  /** Sets a new user for this GUI. */
  private void setUser(String selectedUser)
  {
    if (selectedUser.equals(getSecurityAwareControlPoint().getPersonalizationUser()))
    {
      return;
    }

    ControlPointStartupConfiguration controlPointStartupConfiguration =
      (ControlPointStartupConfiguration)startupConfiguration.getSingleControlPointStartupConfiguration();

    String currentUserFileName =
      startupConfiguration.getWorkingDirectory() +
        FileHelper.getHostBasedFileName(controlPointStartupConfiguration.getProperty("CurrentUserFile", "current_user"));

    // update the file that holds the new user for the next startup
    File currentUserFile = new File(currentUserFileName);
    try
    {
      FileOutputStream outputStream = new FileOutputStream(currentUserFile);
      outputStream.write(StringHelper.utf8StringToByteArray(selectedUser));
      outputStream.close();
    } catch (IOException exc)
    {
      System.out.println(exc.getMessage());
    }
    // update user and keys
    // must be done first because the security console uses the changed key
    // from personalization
    getSecurityAwareControlPoint().setPersonalizationUser(selectedUser);
    userChanged(selectedUser);
    // update user in security console
    if (getSecurityConsole() != null)
    {
      getSecurityConsole().setCurrentUser(selectedUser);
    }

    // update ID because its used in actionPerformed event
    securityContext.getSecurityConsoleNameButton().setID(getSecurityAwareControlPoint().getSecurityID());

    // trigger update in securitySection
    if (securedDevicePlugin != null)
    {
      securedDevicePlugin.securityConsoleUserChange();
    }

    if (securityAwareControlPointPlugin != null)
    {
      securityAwareControlPointPlugin.securityConsoleUserChange();
    }
  }

  /** Requests all active safety messages from all visible active safety devices. */
  private void requestActiveSafetyMessages()
  {
    int oldMessageCount = activeSafetyButtonList.size();
    activeSafetyButtonList.clear();
    Vector activeSafetyDevices =
      getSecurityAwareControlPoint().getSpecialDeviceList(DeviceConstant.ACTIVE_SAFETY_DEVICE_TYPE);

    for (int i = 0; i < activeSafetyDevices.size(); i++)
    {
      CPDevice currentDevice = (CPDevice)activeSafetyDevices.elementAt(i);
      System.out.println("Check visibility for " + currentDevice.getFriendlyName());
      // use only visible devices
      if (!VisibilitySimulation.ACTIVATE_SIMULATION || visibilitySimulation.isVisibleDevice(currentDevice))
      {
        try
        {
          CPService service = currentDevice.getCPServiceByType(ActiveSafetyConstant.ACTIVE_SAFETY_SERVICE_TYPE);
          CPAction action = service.getCPAction("GetActiveSafetyMessageCount");
          getSecurityAwareControlPoint().invokeAction(action);

          int messageCount = (int)action.getOutArgument("Count").getNumericValue();

          System.out.println("Try to read " + messageCount + " active safety messages...");
          for (int j = 0; j < messageCount; j++)
          {
            action = service.getCPAction("GetActiveSafetyMessage");
            try
            {
              action.getInArgument("Index").setNumericValue(j);
              getSecurityAwareControlPoint().invokeAction(action);

              String result = action.getOutArgument("Content").getStringValue();

              SmoothButton button =
                new SmoothButton(new Dimension(980, 2 * ButtonConstants.BUTTON_HEIGHT), 20, result, null);
              button.setSelectable(false);
              button.setDisabledButtonColor(GUIConstants.GENRE_ACTIVE_SAFETY_COLOR);
              if (warningImage != null && warningImage.getWidth(null) != 0)
              {
                button.setIconImage(warningImage);
              }

              activeSafetyButtonList.add(button);
            } catch (Exception e)
            {
              System.out.println("Error retrieving active safety message: " + e.getMessage());
            }
          }
        } catch (Exception e)
        {
          System.out.println("Error retrieving active safety message: " + e.getMessage());
        }
      } else
      {
        System.out.println(currentDevice.getFriendlyName() + " is not visible");
      }
    }
    updateActiveSafetyButtons = false;
    // try to update GUI
    if (oldMessageCount > 0 || activeSafetyButtonList.size() > 0)
    {
      updateGUI(false);
    }
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // IDeviceTranslations //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the translations object for a specific device
   */
  public DeviceTranslations getDeviceTranslations(CPDevice device)
  {
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      if (((DeviceGUIContext)deviceContextList.elementAt(i)).getDevice() == device)
      {
        return ((DeviceGUIContext)deviceContextList.elementAt(i)).getDeviceTranslations();
      }
    }

    return null;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ISubscriptionPolicyListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.ISubscriptionPolicyListener#getSubscriptionPolicy(de.fhg.fokus.magic.upnp.control_point.CPService)
   */
  public int getSubscriptionPolicy(CPService service)
  {
    if (service.getServiceType().equals(LocalizationConstant.LOCALIZATION_SERVICE_TYPE) ||
      service.getServiceType().equals(DeviceConstant.RFID_READER_SERVICE_TYPE))
    {
      return UPnPConstant.SUBSCRIPTION_MODE_AUTOMATIC;
    }

    return UPnPConstant.SUBSCRIPTION_MODE_MANUAL;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ISecurityConsoleEvents //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void newSecurityAwareControlPoint(LocalDictionaryObject controlPoint)
  {
    securityAwareControlPointPlugin.newSecurityAwareControlPoint(controlPoint);
    updateGUI();
  }

  public void newSecurityAwareCPDevice(LocalDictionaryObject device)
  {
    // inform pluginhttp://www.google.de/firefox?client=firefox-a&rls=org.mozilla:en-US:official
    securedDevicePlugin.newSecurityAwareCPDevice(device);
    // change predefined name in normal device listing to user defined name
    updateSecurityAwareCPDeviceName(device);
  }

  public void securityAwareControlPointStatusChange(LocalDictionaryObject controlPoint)
  {
    securityAwareControlPointPlugin.securityAwareControlPointStatusChange(controlPoint);
  }

  public void securityAwareCPDeviceStatusChange(LocalDictionaryObject device)
  {
    securedDevicePlugin.securityAwareCPDeviceStatusChange(device);
    // change predefined name in normal device listing to user defined name
    updateSecurityAwareCPDeviceName(device);
  }

  /**
   * Event that the name of a local dictionary object (device or control point) was changed
   */
  public void localDictionaryNameChange(LocalDictionaryObject localDictionaryObject)
  {
    // update plugin
    if (securedDevicePlugin != null)
    {
      securedDevicePlugin.localDictionaryNameChange(localDictionaryObject);
    }

    if (securityAwareControlPointPlugin != null)
    {
      securityAwareControlPointPlugin.localDictionaryNameChange(localDictionaryObject);
    }

    // if dictionary object is device,
    // change predefined name in normal device listing to user defined name
    if (localDictionaryObject.getSecurityAwareCPDeviceObject() != null)
    {
      updateSecurityAwareCPDeviceName(localDictionaryObject);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // IDeviceGUIContextProvider //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.IDeviceGUIContextProvider#getDeviceContext(int)
   */
  public DeviceGUIContext getDeviceContext(int index)
  {
    if (index >= 0 && index < deviceContextList.size())
    {
      return (DeviceGUIContext)deviceContextList.elementAt(index);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.IDeviceGUIContextProvider#getDeviceContext(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public DeviceGUIContext getDeviceContext(CPDevice device)
  {
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      DeviceGUIContext currentContext = (DeviceGUIContext)deviceContextList.elementAt(i);
      if (currentContext.getDevice().getUDN().equals(device.getUDN()))
      {
        return currentContext;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.IDeviceGUIContextProvider#getDeviceContext(de.fhg.fokus.magic.upnp.CPService)
   */
  public DeviceGUIContext getDeviceContext(CPService service)
  {
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      if (((DeviceGUIContext)deviceContextList.elementAt(i)).getDevice() == service.getCPDevice())
      {
        return (DeviceGUIContext)deviceContextList.elementAt(i);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.core.examples.gui_control_point.IDeviceGUIContextProvider#getDeviceContextCount()
   */
  public int getDeviceContextCount()
  {
    return deviceContextList.size();
  }

  public Color getDeviceGenreColor(DeviceGUIContext deviceGUIContext)
  {
    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_MEDIA_SERVER)
    {
      return GUIConstants.GENRE_MEDIA_SERVER_COLOR;
    }

    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_MEDIA_RENDERER)
    {
      return GUIConstants.GENRE_MEDIA_RENDERER_COLOR;
    }

    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_PERSONALIZATION)
    {
      return GUIConstants.GENRE_PERSONALIZATION_COLOR;
    }

    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_HOME_CONTROL)
    {
      return GUIConstants.GENRE_HOME_CONTROL_COLOR;
    }

    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_SENSOR)
    {
      return GUIConstants.GENRE_SENSOR_COLOR;
    }

    if (deviceGUIContext.getDeviceGenre() == GUIConstants.GENRE_MESSAGING)
    {
      return GUIConstants.GENRE_MESSAGING_COLOR;
    }

    return GUIConstants.GENRE_MISC_COLOR;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public PluginManager getPluginManager()
  {
    return pluginManager;
  }

  /** Retrieves the security console associated with this GUI. */
  public SecurityConsoleEntity getSecurityConsole()
  {
    return securityConsole;
  }

  /** Retrieves the entity associated with this GUI. */
  public SecurityAwareTemplateEntity getSecurityAwareEntity()
  {
    if (useSecurityConsole)
    {
      return securityConsole;
    } else
    {
      return securityAwareEntity;
    }
  }

  /** Retrieves the control point associated with this GUI. */
  public SecurityAwareTemplateControlPoint getSecurityAwareControlPoint()
  {
    if (useSecurityConsole)
    {
      return securityConsole.getSecurityAwareControlPoint();
    } else
    {
      return securityAwareEntity.getSecurityAwareControlPoint();
    }
  }

  /** Retrieves the plugin used for secured device management */
  public SecuredDevicePlugin getSecuredDevicePlugin()
  {
    return securedDevicePlugin;
  }

  /** Retrieves the plugin used for security control point management */
  public SecurityAwareControlPointPlugin getSecurityAwareControlPointPlugin()
  {
    return securityAwareControlPointPlugin;
  }

  /**
   * Checks if a device with this udn is already in the list
   */
  private boolean isKnownDevice(String udn)
  {
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      if (((DeviceGUIContext)deviceContextList.elementAt(i)).getDevice().getUDN().equals(udn))
      {
        return true;
      }
    }
    return false;
  }

  /** Checks if the service is a helper service. */
  private boolean isHelperService(CPService service)
  {
    String serviceType = service.getServiceType();
    return serviceType.equals(DeviceConstant.TRANSLATION_SERVICE_TYPE) ||
      serviceType.equals(DeviceConstant.ATTRIBUTE_SERVICE_TYPE) ||
      serviceType.equals(DeviceConstant.USAGE_SERVICE_TYPE);
  }

  /**
   * Returns the index for a device in the internal list
   */
  protected int getDeviceIndex(String udn)
  {
    for (int i = 0; i < deviceContextList.size(); i++)
    {
      if (((DeviceGUIContext)deviceContextList.elementAt(i)).getDevice().getUDN().equals(udn))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the device at the index
   */
  protected CPDevice getDevice(int index)
  {
    if (index >= 0 && index < deviceContextList.size())
    {
      return ((DeviceGUIContext)deviceContextList.elementAt(index)).getDevice();
    }
    return null;
  }

  /** Updates the name of a security aware device to its user defined name */
  private void updateSecurityAwareCPDeviceName(LocalDictionaryObject deviceDictionaryObject)
  {
    // search device in list of normal devices and change name to user defined name
    for (int i = 0; i < getDeviceContextCount(); i++)
    {
      DeviceGUIContext currentDeviceContext = getDeviceContext(i);
      if (currentDeviceContext.getDevice() == deviceDictionaryObject.getSecurityAwareCPDeviceObject().getCPDevice())
      {
        currentDeviceContext.getDeviceNameButton().setText(deviceDictionaryObject.getUserDefinedName());
      }
    }
  }

  private void updateGUI()
  {
    updateGUI(true);
  }

  /**
   * Updates the GUI to represent the new state.
   * 
   * 
   * @param updateTopPanel
   *          True to force a full update, false to only update the main panel
   */
  private void updateGUI(boolean updateTopPanel)
  {
    if (System.currentTimeMillis() - infoUpdateTime > INFO_DELAY_TIME)
    {
      jInfoLabel.setText("");
      bottomPanel.setText("");
    }

    // show device overview
    jDeviceOverviewPanel.setVisible(currentState == STATE_DEVICE_OVERVIEW);
    commandsPanel.setVisible(currentState == STATE_DEVICE_OVERVIEW);
    activeSafetyPanel.setVisible(!activeSafetyButtonList.isEmpty());

    // show fill panel, if device selected, but no device plugin and no service selected
    jFillPanel.setVisible(currentState == STATE_DEVICE || currentState == STATE_SECURITY);

    jPluginPanel.setVisible(currentState == STATE_DEVICE_PLUGIN || currentState == STATE_SERVICE_PLUGIN ||
      currentState == STATE_IMAGE_RENDERER || currentState == STATE_CP_SECURITY ||
      currentState == STATE_DEVICE_SECURITY);

    if (updateTopPanel)
    {
      jTopDeviceNamePanel.removeAll();
      jTopServicePanel.removeAll();
      jTopHelperServicePanel.removeAll();
      jTopDeviceNamePanel.invalidate();
      jTopServicePanel.invalidate();
      jTopHelperServicePanel.invalidate();
    }
    jPluginPanel.removeAll();
    jPluginPanel.invalidate();

    updateActiveSafetyDevicePanel();

    if (currentState == STATE_DEVICE_OVERVIEW)
    {
      updateFramePanelColor(ButtonConstants.DARK_BACKGROUND_COLOR);
      updateDeviceOverviewPanel(updateTopPanel);
    }

    if (currentState == STATE_SECURITY || currentState == STATE_CP_SECURITY || currentState == STATE_DEVICE_SECURITY)
    {
      updateSecurity();
    }
    if (currentState == STATE_IMAGE_RENDERER)
    {
      updateInternalDevice();
    }

    if (currentState == STATE_DEVICE_PLUGIN || currentState == STATE_DEVICE || currentState == STATE_SERVICE_PLUGIN)
    {
      updateCurrentDevice();
    }
    if (currentState == STATE_SERVICE_PLUGIN)
    {
      updateCurrentService();
    }

    if (updateTopPanel)
    {
      jTopDeviceNamePanel.repaint();
      jTopHelperServicePanel.repaint();
      jTopServicePanel.repaint();
    }
    jPluginPanel.repaint();
    this.validateTree();
    if (currentState == STATE_DEVICE_OVERVIEW)
    {
      updateDeviceOverviewLayout();
    }

  }

  /** Updates the button color for all frame panels */
  private void updateFramePanelColor(Color color)
  {
    jTopPanel.setBackground(color);
    jTopDeviceNamePanel.setBackground(color);
    jTopMiddleFillPanel.setBackground(color);
    jTopServicePanel.setBackground(color);
    jTopHelperServicePanel.setBackground(color);
    jBottomPanel.setBackground(color);
    bottomPanel.setBackground(color);
    upButton.setBackground(color);
    userButton.setBackground(color);

    // jTopDeviceNamePanel.setBackground(Color.black);
    // jTopMiddleFillPanel.setBackground(Color.cyan);
    // jTopServicePanel.setBackground(Color.red);
    // jTopPanel.setBackground(Color.yellow);
  }

  private void updateCurrentDevice()
  {
    updateFramePanelColor(getDeviceGenreColor(currentDeviceContext));
    Insets insets = new Insets(5, 5, 0, 5);
    // add device name
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = insets;
    jTopDeviceNamePanel.add(upButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = insets;
    currentDeviceContext.getDeviceNameButton().setSelected(true);
    currentDeviceContext.getDeviceNameButton().setBackground(jTopPanel.getBackground());
    jTopDeviceNamePanel.add(currentDeviceContext.getDeviceNameButton(), gridBagConstraints);

    // check for device plugin
    if (currentDeviceContext.isDevicePlugin())
    {
      showPlugin(currentDeviceContext.getDevicePlugin());
    } else
    // no device plugin found, show services
    {
      int serviceCount = 0;
      int helperServiceCount = 0;
      boolean isHelperService;

      // add services
      for (int i = 0; i < currentDeviceContext.getVisibleCPServiceCount(); i++)
      {
        SmoothButton button = currentDeviceContext.getVisibleCPServiceNameButton(i);
        CPService service = currentDeviceContext.getVisibleCPService(i);
        // decide if the service is a normal or a helper service
        isHelperService = isHelperService(service);
        gridBagConstraints = new GridBagConstraints();
        if (isHelperService)
        {
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = helperServiceCount;
          helperServiceCount++;
        } else
        {
          gridBagConstraints.gridx = serviceCount % 2;
          gridBagConstraints.gridy = serviceCount / 2;
          serviceCount++;
        }
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        button.setSelected(button == currentServiceButton);
        button.setBackground(jTopPanel.getBackground());

        // show hint whether we are subscribed to the service
        if (service != null)
        {
          button.setToolTipText("Nicht registriert für Ereignisse");
          if (service.isSubscribed())
          {
            button.setToolTipText("Registriert für Ereignisse");
          }

          if (service.isMulticastSubscribed())
          {
            button.setToolTipText("Registriert für Multicast-Ereignisse");
          }
        }
        if (isHelperService)
        {
          jTopHelperServicePanel.add(button, gridBagConstraints);
        } else
        {
          jTopServicePanel.add(button, gridBagConstraints);
        }
      }
    }
  }

  private void updateSecurity()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jTopDeviceNamePanel.add(upButton, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    securityContext.getSecurityButton().setBackground(jTopPanel.getBackground());
    jTopDeviceNamePanel.add(securityContext.getSecurityButton(), gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 5, 5, 5);
    securityContext.getSecurityConsoleNameButton().setSelected(false);
    securityContext.getSecurityConsoleNameButton().setBackground(jTopPanel.getBackground());
    // update security console name
    securityContext.getSecurityConsoleNameButton().setText(securityConsole.getUserDefinedName());
    // security console name shows securityID as tooltip
    securityContext.getSecurityConsoleNameButton().setToolTipText(securityContext.getSecurityConsole()
      .getSecurityConsoleControlPoint()
      .getSecurityID());
    jTopDeviceNamePanel.add(securityContext.getSecurityConsoleNameButton(), gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    securityContext.getSecurityAwareControlPointsButton().setSelected(currentState == STATE_CP_SECURITY);
    securityContext.getSecurityAwareControlPointsButton().setBackground(jTopPanel.getBackground());
    jTopServicePanel.add(securityContext.getSecurityAwareControlPointsButton(), gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    securityContext.getSecurityAwareDevicesButton().setSelected(currentState == STATE_DEVICE_SECURITY);
    securityContext.getSecurityAwareDevicesButton().setBackground(jTopPanel.getBackground());
    jTopServicePanel.add(securityContext.getSecurityAwareDevicesButton(), gridBagConstraints);

    if (currentState == STATE_CP_SECURITY || currentState == STATE_DEVICE_SECURITY)
    {
      updateCurrentSecuritySection();
    }
  }

  /** Shows the device or control point security plugin. */
  private void updateCurrentSecuritySection()
  {
    // devices selected
    if (currentState == STATE_DEVICE_SECURITY)
    {
      showPlugin(securedDevicePlugin);
    }
    // control points selected
    if (currentState == STATE_CP_SECURITY)
    {
      showPlugin(securityAwareControlPointPlugin);
    }
  }

  /** Shows the internal image renderer. */
  private void updateInternalDevice()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    jTopDeviceNamePanel.add(upButton, gridBagConstraints);

    if (currentState == STATE_IMAGE_RENDERER)
    {
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(5, 5, 5, 5);
      imageRendererButton.setBackground(jTopPanel.getBackground());
      jTopDeviceNamePanel.add(imageRendererButton, gridBagConstraints);

      showPlugin(imageRendererPlugin);
    }
  }

  /** Shows the plugin for the currently selected service. */
  private void updateCurrentService()
  {
    showPlugin(currentServicePlugin);
  }

  /** Initializes the layout for the device overview. */
  private void initDeviceOverviewPanel()
  {
    Insets insets = new Insets(2, 2, 2, 2);
    // put panels together
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = insets;
    jDeviceOverviewLeftPanel.add(serverPanel, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    jDeviceOverviewLeftPanel.add(rendererPanel, gridBagConstraints);
    gridBagConstraints.gridy = 2;
    jDeviceOverviewLeftPanel.add(personalizationPanel, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    jDeviceOverviewMiddlePanel.add(homeControlPanel, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    jDeviceOverviewMiddlePanel.add(sensorPanel, gridBagConstraints);
    gridBagConstraints.gridy = 2;
    jDeviceOverviewMiddlePanel.add(messagingPanel, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    jDeviceOverviewRightPanel.add(miscPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    JPanel leftFillPanel = new JPanel();
    leftFillPanel.setBackground(jDeviceOverviewPanel.getBackground());
    jDeviceOverviewLeftPanel.add(leftFillPanel, gridBagConstraints);

    JPanel middleFillPanel = new JPanel();
    middleFillPanel.setBackground(jDeviceOverviewPanel.getBackground());
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    jDeviceOverviewMiddlePanel.add(middleFillPanel, gridBagConstraints);

    JPanel rightFillPanel = new JPanel();
    rightFillPanel.setBackground(jDeviceOverviewPanel.getBackground());
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    jDeviceOverviewRightPanel.add(rightFillPanel, gridBagConstraints);

    // fill commandsPanel
    commandsPanel.setBackground(jContentPanel.getBackground());

    // show internal devices
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new Insets(0, 5, 5, 5);
    commandsPanel.add(imageRendererOverviewButton, gridBagConstraints);

    // show search devices button
    gridBagConstraints.gridx = 1;
    commandsPanel.add(forceSearchButton, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    commandsPanel.add(addDeviceButton, gridBagConstraints);
    // gridBagConstraints.gridx = 2;
    // commandsPanel.add(infoButton, gridBagConstraints);

    // always show safety because offline configuration is possible
    if (useSecurityConsole)
    {
      gridBagConstraints.gridx = 3;
      commandsPanel.add(securityContext.getSecurityOverviewButton(), gridBagConstraints);
    }
    commandsPanel.repaint();
    // add commands to config panel
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = insets;
    jDeviceConfigPanel.add(commandsPanel, gridBagConstraints);
  }

  private void initCommandsPanel()
  {
    // fill commandsPanel
    commandsPanel.setBackground(jContentPanel.getBackground());

    // show internal devices
    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    commandsPanel.add(imageRendererOverviewButton, gridBagConstraints);

    // show search devices button
    gridBagConstraints.gridx = 1;
    commandsPanel.add(forceSearchButton, gridBagConstraints);

    // always show safety because offline configuration is possible
    if (useSecurityConsole)
    {
      gridBagConstraints.gridx = 2;
      commandsPanel.add(securityContext.getSecurityOverviewButton(), gridBagConstraints);
    }
    commandsPanel.repaint();
  }

  private void initBottomPanel()
  {
    GridBagConstraints gridBagConstraints;
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jBottomPanel.add(bottomPanel, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.weightx = 0;
    jBottomPanel.add(commandsPanel, gridBagConstraints);
  }

  /**
   * Update the panel that shows a overview over all found devices.
   * 
   * @param updateTopPanel
   *          True to also update the top panel
   */
  private void updateDeviceOverviewPanel(boolean updateTopPanel)
  {
    serverPanel.clear();
    rendererPanel.clear();
    personalizationPanel.clear();
    homeControlPanel.clear();
    sensorPanel.clear();
    messagingPanel.clear();
    miscPanel.clear();

    Insets insets;
    GridBagConstraints gridBagConstraints;
    if (updateTopPanel)
    {
      // add select user panel
      insets = new Insets(5, 5, 0, 5);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = insets;
      jTopDeviceNamePanel.add(selectUserPanel, gridBagConstraints);
    }
    // Insets for device buttons
    insets = new Insets(1, 3, 1, 3);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = insets;

    // sort device context alphabetically
    Collections.sort(deviceContextList, new Comparator()
    {
      public int compare(Object a, Object b)
      {
        DeviceGUIContext contextA = (DeviceGUIContext)a;
        DeviceGUIContext contextB = (DeviceGUIContext)b;
        return contextA.getDeviceNameButton().getText().compareTo(contextB.getDeviceNameButton().getText());
      }
    });
    for (int i = 0; i < getDeviceContextCount(); i++)
    {
      if (visibilitySimulation.isVisibleDevice(i))
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        getDeviceContext(i).getDeviceNameButton().setBackground(jContentPanel.getBackground());
        getDeviceContext(i).getDeviceNameButton().setSelected(false);
        if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_MEDIA_SERVER)
        {
          serverPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_MEDIA_RENDERER)
        {
          rendererPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_PERSONALIZATION)
        {
          personalizationPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_HOME_CONTROL)
        {
          homeControlPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_SENSOR)
        {
          sensorPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else if (getDeviceContext(i).getDeviceGenre() == GUIConstants.GENRE_MESSAGING)
        {
          messagingPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        } else
        {
          miscPanel.addDeviceButton(getDeviceContext(i).getDeviceNameButton(), gridBagConstraints);
        }
      }
    }

    updateDeviceOverviewLayout();

    serverPanel.update();
    rendererPanel.update();
    personalizationPanel.update();
    homeControlPanel.update();
    sensorPanel.update();
    messagingPanel.update();
    miscPanel.update();
  }

  private void updateActiveSafetyDevicePanel()
  {
    activeSafetyPanel.removeAll();
    activeSafetyPanel.invalidate();

    Insets insets = new Insets(3, 3, 3, 3);
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = insets;
    gridBagConstraints.gridx = 0;
    for (int i = 0; i < activeSafetyButtonList.size(); i++)
    {
      SmoothButton currentButton = (SmoothButton)activeSafetyButtonList.elementAt(i);
      gridBagConstraints.gridy = i;

      activeSafetyPanel.add(currentButton, gridBagConstraints);
    }
    activeSafetyPanel.repaint();
    this.validateTree();
  }

  /**
   * Loads a plugin into the plugin panel.
   * 
   * @param plugin
   *          The requested plugin
   */
  private void showPlugin(BasePlugin plugin)
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    plugin.pluginShown();
    jPluginPanel.add(plugin, gridBagConstraints);
  }

  /** Subscribes to all media renderers if the selected device uses media renderers for display. */
  private void trySubscribeToMediaRenderers()
  {
    // subscribe to all media renderers if a media server was selected
    if (currentDeviceContext != null && currentDeviceContext.isDIDLResourceProvider())
    {
      for (int j = 0; j < getDeviceContextCount(); j++)
      {
        DeviceGUIContext aDeviceContext = getDeviceContext(j);
        if (aDeviceContext.getDevice().getDeviceType().startsWith(UPnPAVConstant.MEDIA_RENDERER_DEVICE_TYPE_START))
        {
          getSecurityAwareControlPoint().startManualEventSubscriptions(aDeviceContext.getDevice());
        }
      }
    }
  }

  /** Subscribes to all media servers if media synchronization is the currently selected device. */
  private void trySubscribeToMediaServers()
  {
    // subscribe to all media servers if media synchronization was selected
    if (currentDeviceContext != null &&
      currentDeviceContext.getDevice().getDeviceType().equals(UPnPAVConstant.AV_SYNCHRONIZATION_DEVICE_TYPE))
    {
      for (int j = 0; j < getDeviceContextCount(); j++)
      {
        DeviceGUIContext aDeviceContext = getDeviceContext(j);
        if (aDeviceContext.getDevice().getDeviceType().startsWith(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE_START))
        {
          getSecurityAwareControlPoint().startManualEventSubscriptions(aDeviceContext.getDevice());
        }
      }
    }
  }

  /** Subscribes to interesting devices. */
  private void trySubscribeToDevices(String selectedDeviceType, String interestingDeviceType)
  {
    if (currentDeviceContext != null && currentDeviceContext.getDevice().getDeviceType().equals(selectedDeviceType))
    {
      for (int j = 0; j < getDeviceContextCount(); j++)
      {
        DeviceGUIContext aDeviceContext = getDeviceContext(j);
        if (aDeviceContext.getDevice().getDeviceType().startsWith(interestingDeviceType))
        {
          getSecurityAwareControlPoint().startManualEventSubscriptions(aDeviceContext.getDevice());
        }
      }
    }
  }

  /**
   * Ends the subscription to all media renderers if a media server was the currently selected device.
   */
  // private void tryEndSubscriptionToMediaRenderers()
  // {
  // // end subscription to all media renderers if all devices are deselected
  // if (currentDeviceContext != null &&
  // currentDeviceContext.isDIDLResourceProvider())
  // {
  // for (int j = 0; j < getDeviceContextCount(); j++)
  // {
  // DeviceGUIContext aDeviceContext = getDeviceContext(j);
  // if
  // (aDeviceContext.getDevice().getDeviceType().startsWith(UPnPAVConstant.MEDIA_RENDERER_DEVICE_TYPE_START))
  // {
  // getSecurityAwareControlPoint().endManualEventSubscriptions(aDeviceContext.getDevice());
  // }
  // }
  // }
  // }
  /**
   * Ends the subscription to all media servers if media synchronization was the currently selected device.
   */
  private void tryEndSubscriptionToMediaServers()
  {
    if (currentDeviceContext != null &&
      currentDeviceContext.getDevice().getDeviceType().equals(UPnPAVConstant.AV_SYNCHRONIZATION_DEVICE_TYPE))
    {
      for (int j = 0; j < getDeviceContextCount(); j++)
      {
        DeviceGUIContext aDeviceContext = getDeviceContext(j);
        if (aDeviceContext.getDevice().getDeviceType().startsWith(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE_START))
        {
          getSecurityAwareControlPoint().endManualEventSubscriptions(aDeviceContext.getDevice());
        }
      }
    }
  }

  /**
   * Ends the subscription to all devices with the interestingDeviceType, if the currently selected device has the
   * selectedDeviceType.
   * 
   * @param selectedDeviceType
   * @param interestingDeviceType
   */
  private void tryEndSubscriptionToDevices(String selectedDeviceType, String interestingDeviceType)
  {
    if (currentDeviceContext != null && currentDeviceContext.getDevice().getDeviceType().equals(selectedDeviceType))
    {
      for (int j = 0; j < getDeviceContextCount(); j++)
      {
        DeviceGUIContext aDeviceContext = getDeviceContext(j);
        if (aDeviceContext.getDevice().getDeviceType().startsWith(interestingDeviceType))
        {
          getSecurityAwareControlPoint().endManualEventSubscriptions(aDeviceContext.getDevice());
        }
      }
    }
  }

  public void actionPerformed(java.awt.event.ActionEvent e)
  {
    // System.out.println("Action performed with command "+e.getActionCommand());
    // do not leave device while action is pending
    if (e.getActionCommand().equals(GO_TO_OVERVIEW) &&
      (currentServicePlugin == null || !currentServicePlugin.isActionPending()))
    {
      if (currentDeviceContext != null)
      {
        // end subscription for current device services if policy is manual
        getSecurityAwareControlPoint().endManualEventSubscriptions(currentDeviceContext.getDevice());
        // event to current device plugin
        if (currentDeviceContext.isDevicePlugin())
        {
          currentDeviceContext.getDevicePlugin().pluginHidden();
        }
      }
      // to speed up device selection, we stay subscribed
      // tryEndSubscriptionToMediaRenderers();
      tryEndSubscriptionToMediaServers();
      tryEndSubscriptionToDevices(DeviceConstant.MAP_DEVICE_TYPE, DeviceConstant.GPS_DEVICE_TYPE);

      currentDeviceContext = null;
      currentServiceButton = null;
      currentServicePlugin = null;
      currentState = STATE_DEVICE_OVERVIEW;
      updateGUI();
    }
    if (e.getActionCommand().equals(EXIT))
    {
      exitForm(null);
    }
    if (e.getActionCommand().equals(imageRendererOverviewButton.getID()))
    {
      currentState = STATE_IMAGE_RENDERER;
      updateGUI();
    }
    if (e.getActionCommand().equals(forceSearchButton.getID()) && (e.getModifiers() & ActionEvent.CTRL_MASK) == 0)
    {
      forceSearchButton.setSelectable(false);
      searchTime = System.currentTimeMillis();
      getSecurityAwareControlPoint().getBasicControlPoint().sendSearchRootDeviceMessage();
    }
    // dirty hack to activate/deactivate Visibility Simulation
    if (e.getActionCommand().equals(forceSearchButton.getID()) && (e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
    {
      VisibilitySimulation.ACTIVATE_SIMULATION = !VisibilitySimulation.ACTIVATE_SIMULATION;
      updateActiveSafetyButtons = true;
      updateGUI();
    }
    // add device manually
    if (e.getActionCommand().equals(addDeviceButton.getID()))
    {
      String urlString =
        DialogValueInvocation.getInvokedString(this,
          "Neues UPnP-Gerät (http://IP:Port/DeviceDescriptionPath)",
          "http://");
      if (urlString != null)
      {
        getSecurityAwareControlPoint().addRemoteDevice(urlString);
        // store URL
        getSecurityAwareControlPoint().getBasicControlPoint()
          .getCPDeviceCache()
          .storeManualDeviceDescriptionURL(urlString);
      }
    }
    // select a new user in the GUI
    if (e.getActionCommand().equals(GUIControlConstants.SELECT_USER))
    {
      SmoothButton button = (SmoothButton)e.getSource();
      String selectedUserName = button.getText();
      String internalUserName = selectedUserName;
      // translate anonymous user in UPnP internal user name
      if (selectedUserName.equals(ANONYMOUS_USER))
      {
        internalUserName = UPnPConstant.USER_UNKNOWN;
      }

      setUser(internalUserName);

      userButton.setText(selectedUserName);
      selectUserPanel.setPreferredSize(new Dimension(380, SELECT_USER_PANEL_HEIGHT));
      selectUserPanel.update();
      updateDeviceOverviewLayout();
    }
    if (e.getActionCommand().equals(RESIZE_USER_PANEL))
    {
      selectUserPanel.setPreferredSize(new Dimension(380, 200));
      selectUserPanel.update();
      updateDeviceOverviewLayout();
    }
    actionPerformedDeviceCheck(e);
    actionPerformedSecurityCheck(e);
  }

  public void actionPerformedDeviceCheck(java.awt.event.ActionEvent e)
  {
    // search device
    boolean found = false;
    int i = 0;
    while (!found && i < getDeviceContextCount())
    {
      found = getDevice(i).getUDN().equals(e.getActionCommand());
      if (!found)
      {
        i++;
      }
    }
    // a new device was selected
    if (found && getDeviceContext(i) != currentDeviceContext)
    {
      currentState = STATE_DEVICE;
      currentDeviceContext = getDeviceContext(i);
      // update profile in selected device
      getSecurityAwareControlPoint().tryPersonalizeDevice(currentDeviceContext.getDevice());

      // start subscription for current device services
      getSecurityAwareControlPoint().startManualEventSubscriptions(currentDeviceContext.getDevice());

      trySubscribeToMediaRenderers();
      trySubscribeToMediaServers();
      trySubscribeToDevices(DeviceConstant.MAP_DEVICE_TYPE, DeviceConstant.GPS_DEVICE_TYPE);
      // direct access if CTRL is pressed
      currentDeviceContext.setDirectAccess((e.getModifiers() & ActionEvent.CTRL_MASK) != 0);
      if (currentDeviceContext.isDevicePlugin())
      {
        currentState = STATE_DEVICE_PLUGIN;

        // associate MediaServer with internal render to allow internal preview
        if (currentDeviceContext.getDevicePlugin().getPluginType().equals(UPnPAVConstant.MEDIA_SERVER_DEVICE_TYPE) &&
          imageRendererCPDevice != null && imageRendererPlugin != null)
        {
          ((MediaServerPlugin)currentDeviceContext.getDevicePlugin()).setMediaRendererForPreview(imageRendererCPDevice,
            imageRendererPlugin);
        }
      }
      currentServiceButton = null;
      currentServicePlugin = null;

      // select first non-helper service if there is no device plugin
      if (currentState == STATE_DEVICE)
      {
        CPService service = null;
        i = 0;
        while (i < currentDeviceContext.getVisibleCPServiceCount())
        {
          service = currentDeviceContext.getVisibleCPService(i);
          if (!isHelperService(service))
          {
            break;
          }
          i++;
        }
        if (service != null)
        {
          currentState = STATE_SERVICE_PLUGIN;
          currentServicePlugin = currentDeviceContext.getVisibleCPServicePlugin(i);
          currentServiceButton = currentDeviceContext.getVisibleCPServiceNameButton(i);
        }
      }
      updateGUI();
    }
    // search service for current device
    if (currentDeviceContext != null)
    {
      found = false;
      i = 0;
      while (!found && i < currentDeviceContext.getVisibleCPServiceCount())
      {
        found = currentDeviceContext.getVisibleCPServiceNameButton(i).getID().equals(e.getActionCommand());
        if (!found)
        {
          i++;
        }
      }
      if (found && currentDeviceContext.getVisibleCPServicePlugin(i) != currentServicePlugin)
      {
        // event to current service plugin
        if (currentServicePlugin != null)
        {
          currentServicePlugin.pluginHidden();
        }
        currentState = STATE_SERVICE_PLUGIN;
        currentServicePlugin = currentDeviceContext.getVisibleCPServicePlugin(i);
        currentServiceButton = currentDeviceContext.getVisibleCPServiceNameButton(i);
        updateGUI();
      }
    }
  }

  public void actionPerformedSecurityCheck(java.awt.event.ActionEvent e)
  {
    if (e.getActionCommand().equals(GO_TO_SECURITY) && currentState == STATE_DEVICE_OVERVIEW)
    {
      currentDeviceContext = null;
      currentServiceButton = null;
      currentServicePlugin = null;
      currentState = STATE_SECURITY;
      updateGUI();
    }
    // go to security devices
    if (e.getActionCommand().equals(GO_TO_SECURITY_DEVICES) && currentState != STATE_DEVICE_SECURITY)
    {
      currentState = STATE_DEVICE_SECURITY;
      updateGUI();
    }
    // go to security control points
    if (e.getActionCommand().equals(GO_TO_SECURITY_CONTROL_POINTS) && currentState != STATE_CP_SECURITY)
    {
      currentState = STATE_CP_SECURITY;
      updateGUI();
    }
    // set security console name
    if ((currentState == STATE_SECURITY || currentState == STATE_CP_SECURITY || currentState == STATE_DEVICE_SECURITY) &&
      securityConsole.getSecurityAwareControlPoint().getSecurityID().equals(e.getActionCommand()))
    {
      String newName = DialogValueInvocation.getInvokedString(this, "Name", securityConsole.getUserDefinedName());
      if (newName != null)
      {
        securityConsole.setUserDefinedName(newName);
        securityContext.getSecurityConsoleNameButton().setText(securityConsole.getUserDefinedName());
      }
      updateGUI();
    }
  }

  /** Inits a theme panel */
  private JPanel initThemePanel(Color borderColor)
  {
    JPanel panel = new JPanel();
    panel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    panel.setBorder(new SmoothBorder(borderColor));
    panel.setLayout(new GridBagLayout());

    return panel;
  }

  private void updateDebugString()
  {
    ControlPoint controlPoint = getSecurityAwareControlPoint().getBasicControlPoint();
    String debug = "";
    debug += "Geräte: " + getSecurityAwareControlPoint().getCPDeviceCount() + ", ";
    debug += "URL[UUID]: " + controlPoint.getDeviceDescriptionURLFromUUIDTable().size() + ", ";
    debug += "DiscInfos[URL]: " + controlPoint.getDiscoveryInfoFromDescriptionURLTable().size() + ", ";
    debug += "Geräte[URL]: " + controlPoint.getCPDeviceFromDescriptionURLTable().size() + ", ";
    // debug += "LeaseTime[URL]: " + controlPoint.getLeaseTimeThreadFromDescriptionURLTable().size()
    // + ", ";
    debug += "EventThread[SID]: " + controlPoint.getEventSubscriptionThreadFromSIDTable().size() + ", ";
    infoButton.setToolTipText(debug);
  }

  /**
   * Retrieves the startupConfiguration.
   * 
   * @return The startupConfiguration
   */
  public UPnPStartupConfiguration getStartupConfiguration()
  {
    return startupConfiguration;
  }

  /**
   * Retrieves the owner.
   * 
   * @return The owner
   */
  public String getGUIEntityID()
  {
    return entityID;
  }

  /**
   * Retrieves the user.
   * 
   * @return The user
   */
  public String getGUIUser()
  {
    return getSecurityAwareControlPoint().getPersonalizationUser();
  }

  /**
   * Retrieves the visibilitySimulation.
   * 
   * @return The visibilitySimulation
   */
  public VisibilitySimulation getVisibilitySimulation()
  {
    return visibilitySimulation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
   */
  public void componentHidden(ComponentEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
   */
  public void componentMoved(ComponentEvent e)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
   */
  public void componentResized(ComponentEvent e)
  {
    if (currentState == STATE_DEVICE_OVERVIEW)
    {
      selectUserPanel.setPreferredSize(new Dimension(380, SELECT_USER_PANEL_HEIGHT));
      updateDeviceOverviewLayout();
    }
  }

  private void updateDeviceOverviewLayout()
  {
    int lostPanelHeight = serverPanel.getHeaderHeight();

    // try to divide available space as smart as possible
    // left panel
    int availableDeviceCount =
      serverPanel.getDeviceButtonCount() + rendererPanel.getDeviceButtonCount() +
        personalizationPanel.getDeviceButtonCount();

    int panelHeight = jDeviceOverviewLeftPanel.getHeight();
    // substract size of panels and theme button plus outer indents
    panelHeight -= 3 * (lostPanelHeight + 10);
    int displayableDeviceCount = panelHeight / ThemeDeviceOverviewPanel.PREFERRED_HEIGHT;

    // System.out.println("Lost height per panel is " + lostPanelHeight);
    // System.out.println("Available panel height is " + panelHeight);
    // System.out.println("Number of displayable buttons is " + displayableDeviceCount);

    float scale = (float)displayableDeviceCount / (float)availableDeviceCount;

    int serverPanelDeviceCount =
      Math.min(serverPanel.getDeviceButtonCount(), Math.max(0, Math.round(serverPanel.getDeviceButtonCount() * scale)));
    serverPanel.setPreferredDeviceCount(serverPanelDeviceCount);

    int rendererPanelDeviceCount =
      Math.min(rendererPanel.getDeviceButtonCount(), Math.max(0, Math.round(rendererPanel.getDeviceButtonCount() *
        scale)));
    rendererPanel.setPreferredDeviceCount(rendererPanelDeviceCount);

    personalizationPanel.setPreferredDeviceCount(Math.min(personalizationPanel.getDeviceButtonCount(), Math.max(0,
      displayableDeviceCount - serverPanelDeviceCount - rendererPanelDeviceCount)));

    // middle panel
    availableDeviceCount =
      homeControlPanel.getDeviceButtonCount() + sensorPanel.getDeviceButtonCount() +
        messagingPanel.getDeviceButtonCount();

    // System.out.println("Number of available buttons is " + availableDeviceCount);

    scale = (float)displayableDeviceCount / (float)availableDeviceCount;

    int homeControlPanelDeviceCount =
      Math.min(homeControlPanel.getDeviceButtonCount(), Math.max(0, Math.round(homeControlPanel.getDeviceButtonCount() *
        scale)));
    homeControlPanel.setPreferredDeviceCount(homeControlPanelDeviceCount);

    int sensorPanelDeviceCount =
      Math.min(sensorPanel.getDeviceButtonCount(), Math.max(0, Math.round(sensorPanel.getDeviceButtonCount() * scale)));

    // prevent the case that the last panel shows no button because the upper panels
    // take too much space
    if (messagingPanel.getDeviceButtonCount() > 0 && sensorPanelDeviceCount > 1 &&
      displayableDeviceCount - homeControlPanelDeviceCount - sensorPanelDeviceCount < 1)
    {
      sensorPanelDeviceCount -= 1;
    }
    sensorPanel.setPreferredDeviceCount(sensorPanelDeviceCount);

    messagingPanel.setPreferredDeviceCount(Math.min(messagingPanel.getDeviceButtonCount(), Math.max(0,
      displayableDeviceCount - homeControlPanelDeviceCount - sensorPanelDeviceCount)));

    // right panel
    panelHeight = jDeviceOverviewLeftPanel.getHeight();
    // substract size of panel and theme button plus outer indents
    panelHeight -= lostPanelHeight + 20;
    displayableDeviceCount = panelHeight / ThemeDeviceOverviewPanel.PREFERRED_HEIGHT;

    int miscPanelDeviceCount = Math.min(miscPanel.getDeviceButtonCount(), displayableDeviceCount);
    miscPanel.setPreferredDeviceCount(miscPanelDeviceCount);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
   */
  public void componentShown(ComponentEvent e)
  {
  }

  public void run()
  {
    debugUpdateTime = System.currentTimeMillis() - 60000;
    while (!terminateThread)
    {
      if (System.currentTimeMillis() - infoUpdateTime > INFO_DELAY_TIME)
      {
        bottomPanel.setText("");
      }
      if (System.currentTimeMillis() - debugUpdateTime > 10000)
      {
        updateDebugString();
        debugUpdateTime = System.currentTimeMillis();
      }
      if (System.currentTimeMillis() - searchTime > 2000)
      {
        forceSearchButton.setSelectable(true);
      }

      // try to update active safety messages
      if (updateActiveSafetyButtons)
      {
        requestActiveSafetyMessages();
      }

      try
      {
        Thread.sleep(100);
      } catch (Exception e)
      {
      }
    }
    terminated = true;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jBottomPanel;

  private javax.swing.JPanel jContentPanel;

  private javax.swing.JPanel jDeviceConfigPanel;

  private javax.swing.JPanel jDeviceOverviewLeftPanel;

  private javax.swing.JPanel jDeviceOverviewMiddlePanel;

  private javax.swing.JPanel jDeviceOverviewPanel;

  private javax.swing.JPanel jDeviceOverviewRightPanel;

  private javax.swing.JPanel jFillPanel;

  private javax.swing.JLabel jInfoLabel;

  private javax.swing.JPanel jPluginPanel;

  private javax.swing.JPanel jTopDeviceNamePanel;

  private javax.swing.JPanel jTopMiddleFillPanel;

  private javax.swing.JPanel jTopPanel;

  private javax.swing.JPanel jTopServicePanel;

  private javax.swing.JPanel jTopHelperServicePanel;
  // End of variables declaration//GEN-END:variables
}

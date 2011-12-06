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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.toedter.calendar.JDateChooser;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.DeviceConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.logger.LoggerParser;
import de.fraunhofer.fokus.upnp.core.exceptions.InvokeActionException;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.util.DateTimeHelper;
import de.fraunhofer.fokus.upnp.util.FileHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.SmoothArea;
import de.fraunhofer.fokus.upnp.util.swing.SmoothBorder;
import de.fraunhofer.fokus.upnp.util.swing.SmoothButton;
import de.fraunhofer.fokus.upnp.util.swing.SmoothCheckBox;

/**
 * This plugin is used for a logger service.
 * 
 * @author Sebastian Nauck
 */

public class LoggerPlugin extends BaseCPServicePlugin
{

  /**  */
  private static final long        serialVersionUID         = 1L;

  public static String             PLUGIN_TYPE              = DeviceConstant.LOGGER_SERVICE_TYPE;

  // Variables used to start a new logging process

  private String                   deviceUDN;

  private String                   serviceURN;

  private String                   currentOutArgumentType;

  private CPDevice                 currentDevice;

  private CPService                currentService;

  private Argument                 currentOutArgument;

  private CPService[]              serviceList;

  private CPAction[]               actionList;

  private Argument[]               outArgumentList;

  /** Vector holding the LogContexts for all active logger tasks */
  private Vector                   activeLoggingsList;

  /** Vector holding the LogContexts for all finished logger tasks */
  private Vector                   finishedLoggingsList;

  protected DefaultMutableTreeNode root;

  private DefaultTreeModel         treeModel;

  private JTree                    tree;

  // Buttons used in the GUI

  private SmoothButton             finishedDescriptionTitleButton;

  private SmoothButton             finishedUDNTitleButton;

  private SmoothButton             finishedStartTimeTitleButton;

  private SmoothButton             finishedEndTimeTitleButton;

  private SmoothButton             finishedIntervalTitleButton;

  private SmoothButton             finishedMeasureCountTitleButton;

  private SmoothButton             noFinishedLogsButton;

  private SmoothButton             activeDescriptionTitleButton;

  private SmoothButton             activeUDNTitleButton;

  private SmoothButton             activeStartTimeTitleButton;

  private SmoothButton             activeEndTimeTitleButton;

  private SmoothButton             activeIntervalTitleButton;

  private SmoothButton             noActiveLogsButton;

  private SmoothButton             deleteButton;

  private SmoothButton             initialReadButton;

  private SmoothButton             newLoggingButton;

  private SmoothButton             startLoggingButton;

  private SmoothButton             interruptButton;

  private SmoothButton             saveAsCSVButton;

  private SmoothButton             executeCSVSave;

  private JComboBox                deviceUDNComboBox;

  private JComboBox                serviceTypeComboBox;

  private JComboBox                actionNameComboBox;

  private JComboBox                outArgumentComboBox;

  private JComboBox                finishedLogProcessesComboBox;

  private JTextField               descriptionField;

  private JTextField               startTimeField;

  private JDateChooser             startDateField;

  private JTextField               endTimeField;

  private JDateChooser             endDateField;

  private JTextField               intervalField;

  private JLabel                   deviceComboBoxLabel;

  private JLabel                   serviceComboBoxLabel;

  private JLabel                   actionComboBoxLabel;

  private JLabel                   outArgumentComboBoxLabel;

  private JLabel                   descriptionLabel;

  private JLabel                   startTimeLabel;

  private JLabel                   startDateLabel;

  private JLabel                   endTimeLabel;

  private JLabel                   endDateLabel;

  private JLabel                   intervalLabel;

  private JLabel                   finishedLogProcessesComboBoxLabel;

  /** Frame that holds the buttons to start a new logging process */
  private JFrame                   newLoggingFrame;

  private JFrame                   saveAsCSVFrame;

  private JPanel                   finishedLoggingsFillPanel;

  private JPanel                   newLoggingFillPanel;

  private JPanel                   pluginFillPanel;

  private JPanel                   activeLoggingsFillPanel;

  /** Panel holding active loggings */
  private JPanel                   activeLoggingsPanel;

  /** Panel holding finished loggings */
  private JPanel                   finishedLoggingsPanel;

  /** Panel for a new logging process */
  private JPanel                   newLoggingPanel;

  /** Panel for save in csv format */
  // private JPanel saveAsCSVPanel;
  /** Panel for taken a JTree */
  // private JPanel treePanel;
  /** Panel for taken a Button */
  // private JPanel saveButtonPanel;
  private JLabel                   selectedPathLabel;

  private JTextField               selectedPathField;

  private JLabel                   fileNameLabel;

  private JTextField               fileNameField;

  private JPanel                   takeTreeAndButtonAndCSVPanel;

  private SmoothArea               contentArea;

  private LoggerPlugin             plugin;

  private LogContext               selectedLogContext       = null;

  private LogContext               selectedActiveLogContext = null;

  private boolean                  initialReadLogData       = true;

  /** Listener for changes in the new logging frame */
  private NewLoggingEventListener  newLoggingEventListener;

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BasePlugin#initPluginComponents()
   */
  public void initPluginComponents()
  {
    super.initPluginComponents();
    plugin = this;

    activeLoggingsList = new Vector();

    finishedLoggingsList = new Vector();

    newLoggingEventListener = new NewLoggingEventListener();

    setBackground(ButtonConstants.BACKGROUND_COLOR);
    setLayout(new GridBagLayout());

    initialReadButton = new SmoothButton(new Dimension(300, 30), 12, "Lese Logdaten...", null);
    initialReadButton.setBackground(getBackground());
    initialReadButton.setDisabledButtonColor(initialReadButton.getButtonColor());
    initialReadButton.setSelectable(false);

    noFinishedLogsButton = new SmoothButton(new Dimension(300, 30), 12, "Keine Logdaten vorhanden", null);
    noFinishedLogsButton.setBackground(getBackground());
    noFinishedLogsButton.setDisabledButtonColor(initialReadButton.getButtonColor());
    noFinishedLogsButton.setSelectable(false);

    noActiveLogsButton = new SmoothButton(new Dimension(300, 30), 12, "Keine aktiven Logprozesse", null);
    noActiveLogsButton.setBackground(getBackground());
    noActiveLogsButton.setDisabledButtonColor(initialReadButton.getButtonColor());
    noActiveLogsButton.setSelectable(false);

    deleteButton = new SmoothButton(new Dimension(100, 30), 12, "Löschen", "delete");
    deleteButton.setBackground(plugin.getBackground());
    deleteButton.addActionListener(this);

    saveAsCSVButton = new SmoothButton(new Dimension(150, 30), 12, "CSV Speichern", "saveAsCSV");
    saveAsCSVButton.setBackground(plugin.getBackground());
    saveAsCSVButton.addActionListener(this);

    // Components for the second frame
    initNewLoggingFrame();

    // Components for the third frame
    initSaveAsCSVFrame();

    // buttons for finished and active loggings
    newLoggingButton = new SmoothButton(new Dimension(150, 30), 12, "Neues Logging", "newLogging");
    newLoggingButton.setBackground(plugin.getBackground());
    newLoggingButton.addActionListener(newLoggingEventListener);
    interruptButton = new SmoothButton(new Dimension(100, 30), 12, "Abbrechen", "interruptLogging");
    interruptButton.setBackground(plugin.getBackground());
    interruptButton.addActionListener(this);

    finishedDescriptionTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Beschreibung", null);
    finishedDescriptionTitleButton.setBackground(plugin.getBackground());
    finishedUDNTitleButton = new SmoothButton(new Dimension(100, 30), 12, "Gerät", null);
    finishedUDNTitleButton.setBackground(plugin.getBackground());
    finishedStartTimeTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Startzeit", null);
    finishedStartTimeTitleButton.setBackground(plugin.getBackground());
    finishedEndTimeTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Endzeit", null);
    finishedEndTimeTitleButton.setBackground(plugin.getBackground());
    finishedIntervalTitleButton = new SmoothButton(new Dimension(100, 30), 12, "Intervall (sek)", null);
    finishedIntervalTitleButton.setBackground(plugin.getBackground());
    finishedMeasureCountTitleButton = new SmoothButton(new Dimension(100, 30), 12, "Messungen", null);
    finishedMeasureCountTitleButton.setBackground(plugin.getBackground());

    activeDescriptionTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Beschreibung", null);
    activeDescriptionTitleButton.setBackground(plugin.getBackground());
    activeUDNTitleButton = new SmoothButton(new Dimension(100, 30), 12, "Gerät", null);
    activeUDNTitleButton.setBackground(plugin.getBackground());
    activeStartTimeTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Startzeit", null);
    activeStartTimeTitleButton.setBackground(plugin.getBackground());
    activeEndTimeTitleButton = new SmoothButton(new Dimension(150, 30), 12, "Endzeit", null);
    activeEndTimeTitleButton.setBackground(plugin.getBackground());
    activeIntervalTitleButton = new SmoothButton(new Dimension(100, 30), 12, "Intervall (sek)", null);
    activeIntervalTitleButton.setBackground(plugin.getBackground());

    pluginFillPanel = new JPanel();
    pluginFillPanel.setBackground(getBackground());

    finishedLoggingsFillPanel = new JPanel();
    finishedLoggingsFillPanel.setBackground(getBackground());

    activeLoggingsFillPanel = new JPanel();
    activeLoggingsFillPanel.setBackground(getBackground());

    activeLoggingsPanel = new JPanel();
    activeLoggingsPanel.setBorder(new SmoothBorder(ButtonConstants.SMOOTH_GREEN_COLOR));
    activeLoggingsPanel.setBackground(getBackground());
    activeLoggingsPanel.setLayout(new GridBagLayout());

    contentArea = new SmoothArea(new Dimension(250, 60), 12, null);
    contentArea.setBackground(getBackground());
    contentArea.setDisabledButtonColor(contentArea.getButtonColor());
    contentArea.setSelectable(false);

    finishedLoggingsPanel = new JPanel();
    finishedLoggingsPanel.setBackground(getBackground());
    finishedLoggingsPanel.setLayout(new GridBagLayout());
    finishedLoggingsPanel.setBorder(new SmoothBorder(ButtonConstants.SMOOTH_BLUE_COLOR));

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridx = 0;
    add(finishedLoggingsPanel, gridBagConstraints);

    gridBagConstraints.gridy = 1;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    add(pluginFillPanel, gridBagConstraints);

    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.weightx = 0;
    gridBagConstraints.weighty = 0;
    add(activeLoggingsPanel, gridBagConstraints);

    updateLayout();
  }

  private void initNewLoggingFrame()
  {
    newLoggingPanel = new JPanel();
    newLoggingPanel.setBackground(getBackground());
    newLoggingPanel.setLayout(new GridBagLayout());

    newLoggingFillPanel = new JPanel();
    newLoggingFillPanel.setBackground(getBackground());

    newLoggingFrame = new JFrame();

    // Components for the second frame
    deviceUDNComboBox = new JComboBox();
    deviceUDNComboBox.setFont(new Font("Serif", Font.PLAIN, 12));
    deviceUDNComboBox.addItemListener(newLoggingEventListener);
    serviceTypeComboBox = new JComboBox();
    serviceTypeComboBox.setFont(new Font("Serif", Font.PLAIN, 12));
    serviceTypeComboBox.addItemListener(newLoggingEventListener);
    actionNameComboBox = new JComboBox();
    actionNameComboBox.setFont(new Font("Serif", Font.PLAIN, 12));
    actionNameComboBox.addItemListener(newLoggingEventListener);
    outArgumentComboBox = new JComboBox();
    outArgumentComboBox.setFont(new Font("Serif", Font.PLAIN, 12));

    descriptionField = new JTextField("", 15);
    startTimeField = new JTextField("", 15);
    startDateField = new JDateChooser();
    startDateField.setLocale(Locale.GERMANY);
    endTimeField = new JTextField("", 15);
    endDateField = new JDateChooser();
    endDateField.setLocale(Locale.GERMANY);
    intervalField = new JTextField("", 15);
    deviceComboBoxLabel = new JLabel("Gerät: ");
    deviceComboBoxLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    serviceComboBoxLabel = new JLabel("Service :");
    serviceComboBoxLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    actionComboBoxLabel = new JLabel("Action :");
    actionComboBoxLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    outArgumentComboBoxLabel = new JLabel("Argument: ");
    outArgumentComboBoxLabel.setFont(new Font("Serif", Font.PLAIN, 12));

    descriptionLabel = new JLabel("Beschreibung: ");
    descriptionLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    descriptionLabel.setMinimumSize(new Dimension(200, descriptionLabel.getMinimumSize().height));
    startTimeLabel = new JLabel("Startzeit (hh:mm): ");
    startTimeLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    startDateLabel = new JLabel("Startdatum: ");
    startDateLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    endTimeLabel = new JLabel("Endzeit (hh:mm): ");
    endTimeLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    endDateLabel = new JLabel("Enddatum: ");
    endDateLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    intervalLabel = new JLabel("Interval (sek): ");
    intervalLabel.setFont(new Font("Serif", Font.PLAIN, 12));

    startLoggingButton = new SmoothButton(new Dimension(120, 30), 12, " Starte Logging", "startLogging");
    startLoggingButton.addActionListener(newLoggingEventListener);

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    gridBagConstraints.weightx = 1.0;

    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

    // labels
    newLoggingPanel.add(deviceComboBoxLabel, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    newLoggingPanel.add(serviceComboBoxLabel, gridBagConstraints);
    gridBagConstraints.gridy = 2;
    newLoggingPanel.add(actionComboBoxLabel, gridBagConstraints);
    gridBagConstraints.gridy = 3;
    newLoggingPanel.add(outArgumentComboBoxLabel, gridBagConstraints);
    gridBagConstraints.gridy = 4;
    newLoggingPanel.add(descriptionLabel, gridBagConstraints);
    gridBagConstraints.gridy = 5;
    newLoggingPanel.add(startTimeLabel, gridBagConstraints);
    gridBagConstraints.gridy = 6;
    newLoggingPanel.add(startDateLabel, gridBagConstraints);
    gridBagConstraints.gridy = 7;
    newLoggingPanel.add(endTimeLabel, gridBagConstraints);
    gridBagConstraints.gridy = 8;
    newLoggingPanel.add(endDateLabel, gridBagConstraints);
    gridBagConstraints.gridy = 9;
    newLoggingPanel.add(intervalLabel, gridBagConstraints);

    // comboboxes etc.
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridx = 1;
    newLoggingPanel.add(deviceUDNComboBox, gridBagConstraints);
    gridBagConstraints.gridy = 1;
    newLoggingPanel.add(serviceTypeComboBox, gridBagConstraints);
    gridBagConstraints.gridy = 2;
    newLoggingPanel.add(actionNameComboBox, gridBagConstraints);
    gridBagConstraints.gridy = 3;
    newLoggingPanel.add(outArgumentComboBox, gridBagConstraints);
    gridBagConstraints.gridy = 4;
    newLoggingPanel.add(descriptionField, gridBagConstraints);
    gridBagConstraints.gridy = 5;
    newLoggingPanel.add(startTimeField, gridBagConstraints);
    gridBagConstraints.gridy = 6;
    startDateField.setDate(Calendar.getInstance().getTime());
    newLoggingPanel.add(startDateField, gridBagConstraints);
    gridBagConstraints.gridy = 7;
    newLoggingPanel.add(endTimeField, gridBagConstraints);
    gridBagConstraints.gridy = 8;
    endDateField.setDate(Calendar.getInstance().getTime());
    newLoggingPanel.add(endDateField, gridBagConstraints);
    gridBagConstraints.gridy = 9;
    newLoggingPanel.add(intervalField, gridBagConstraints);

    // start logging button and fill panel
    gridBagConstraints.insets = new Insets(10, 110, 0, 0);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 2;
    newLoggingPanel.add(startLoggingButton, gridBagConstraints);
    gridBagConstraints.gridy = 11;
    gridBagConstraints.weighty = 1.0;
    newLoggingPanel.add(newLoggingFillPanel, gridBagConstraints);

    newLoggingFrame.getContentPane().add(newLoggingPanel);
    newLoggingFrame.setSize(350, 400);
    newLoggingFrame.setResizable(false);
    newLoggingFrame.setVisible(false);
  }

  private void initSaveAsCSVFrame()
  {

    root = new DefaultMutableTreeNode(System.getProperty("user.home"));
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    TreeSelectionModel tsm = new DefaultTreeSelectionModel();
    tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(new SaveAsCSVAction());
    tree.setSelectionModel(tsm);
    tree.setRootVisible(false);

    takeTreeAndButtonAndCSVPanel = new JPanel();
    takeTreeAndButtonAndCSVPanel.setBackground(getBackground());
    takeTreeAndButtonAndCSVPanel.setLayout(new GridBagLayout());

    saveAsCSVFrame = new JFrame();

    selectedPathLabel = new JLabel("Path: ");
    selectedPathLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    selectedPathField = new JTextField("", 15);
    fileNameLabel = new JLabel("Filename: ");
    fileNameLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    fileNameField = new JTextField("", 15);
    executeCSVSave = new SmoothButton(new Dimension(100, 30), 12, " Speichern", "executeCSVSave");
    executeCSVSave.addActionListener(this);
    finishedLogProcessesComboBoxLabel = new JLabel("Dateien: ");
    finishedLogProcessesComboBoxLabel.setFont(new Font("Serif", Font.PLAIN, 12));
    finishedLogProcessesComboBox = new JComboBox();
    finishedLogProcessesComboBox.addItemListener(newLoggingEventListener);
    finishedLogProcessesComboBox.setFont(new Font("Serif", Font.PLAIN, 12));

    for (int i = 0; i < finishedLoggingsList.size(); i++)
    {
      LogContext object = (LogContext)finishedLoggingsList.elementAt(i);
      String description = object.description;
      finishedLogProcessesComboBox.addItem(description);
    }

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.insets = new Insets(5, 32, 5, 32);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
    takeTreeAndButtonAndCSVPanel.add(finishedLogProcessesComboBoxLabel, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    takeTreeAndButtonAndCSVPanel.add(finishedLogProcessesComboBox, gridBagConstraints);

    gridBagConstraints.insets = new Insets(5, 20, 5, 20);
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.getViewport().setView(tree);
    takeTreeAndButtonAndCSVPanel.add(scrollPane, gridBagConstraints);

    gridBagConstraints.insets = new Insets(5, 32, 5, 32);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    takeTreeAndButtonAndCSVPanel.add(selectedPathLabel, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    takeTreeAndButtonAndCSVPanel.add(selectedPathField, gridBagConstraints);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    takeTreeAndButtonAndCSVPanel.add(fileNameLabel, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    takeTreeAndButtonAndCSVPanel.add(fileNameField, gridBagConstraints);
    gridBagConstraints.insets = new Insets(10, 137, 0, 0);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    takeTreeAndButtonAndCSVPanel.add(executeCSVSave, gridBagConstraints);

    Container container = saveAsCSVFrame.getContentPane();
    container.add(takeTreeAndButtonAndCSVPanel);
    saveAsCSVFrame.setSize(375, 610);
    saveAsCSVFrame.setResizable(true);
    saveAsCSVFrame.setVisible(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.control_point.plugins.BasePlugin#runPlugin()
   */
  public void startPlugin()
  {
    super.startPlugin();

    Thread thread = new Thread(this);
    thread.setName("Loggerplugin");
    thread.start();
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
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    // a finished logging was selected
    for (int i = 0; i < finishedLoggingsList.size(); i++)
    {
      LogContext currentLogContext = (LogContext)finishedLoggingsList.elementAt(i);
      if ((currentLogContext.id + "").equals(e.getActionCommand()))
      {
        selectLogContext(currentLogContext);
      }
    }
    // finished loggings have been marked for deletion
    if ("delete".equals(e.getActionCommand()))
    {
      // check if there is at least one log selected
      boolean selected = false;
      for (int i = 0; i < finishedLoggingsList.size(); i++)
      {
        LogContext currentLogContext = (LogContext)finishedLoggingsList.elementAt(i);
        selected = selected || currentLogContext.deleteCheckbox.isChecked();
      }
      // we have a selected log
      if (selected)
      {
        deleteButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

        String[] optionen = {
            "Ja", "Nein"
        };
        int choice =
          JOptionPane.showOptionDialog(null,
            "Möchten Sie die ausgewählten LogDaten löschen?",
            "Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            optionen,
            optionen[0]);
        // we want to delete selected logs
        if (choice == JOptionPane.YES_OPTION)
        {
          int i = 0;
          while (i < finishedLoggingsList.size())
          {
            LogContext currentLogContext = (LogContext)finishedLoggingsList.elementAt(i);
            if (currentLogContext.deleteCheckbox.isChecked())
            {
              CPAction action = service.getCPAction("DeleteLogData");
              if (action != null)
              {
                try
                {
                  action.getArgument("ID").setNumericValue(currentLogContext.id);
                  controlPoint.invokeAction(action);

                  finishedLoggingsList.remove(i);
                  i--;
                } catch (Exception ex)
                {
                  System.out.println("Error deleting log message: " + ex.getMessage());
                }
              }
            }
            i++;
          }
          deleteButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
          updateLayout();
        }
        ;
        for (int i = 0; i < finishedLoggingsList.size(); i++)
        {
          LogContext currentLogContext = (LogContext)finishedLoggingsList.elementAt(i);
          currentLogContext.deleteCheckbox.setState(false);
        }
      }
    }
    if ("interruptLogging".equals(e.getActionCommand()))
    {
      // check if there is at least one log selected
      boolean selected = false;
      for (int i = 0; i < activeLoggingsList.size(); i++)
      {
        LogContext currentLogContext = (LogContext)activeLoggingsList.elementAt(i);
        selected = selected || currentLogContext.interruptCheckbox.isChecked();
      }
      // we have a selected task
      if (selected)
      {
        interruptButton.setButtonColor(ButtonConstants.ACTIVE_BUTTON_COLOR);

        String[] optionen = {
            "Ja", "Nein"
        };
        int choice =
          JOptionPane.showOptionDialog(null,
            "Möchten Sie die ausgewählten Tasks abbrechen?",
            "Abbruch",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            optionen,
            optionen[0]);
        // we want to interrupt selected tasks
        if (choice == JOptionPane.YES_OPTION)
        {
          int i = 0;
          while (i < activeLoggingsList.size())
          {
            LogContext currentLogContext = (LogContext)activeLoggingsList.elementAt(i);
            if (currentLogContext.interruptCheckbox.isChecked())
            {
              CPAction action = service.getCPAction("InterruptLogProcess");
              if (action != null)
              {
                try
                {
                  action.getArgument("ID").setNumericValue(currentLogContext.id);
                  controlPoint.invokeAction(action);

                  activeLoggingsList.remove(i);
                  i--;
                } catch (Exception ex)
                {
                  System.out.println("Error interrupting task: " + ex.getMessage());
                }
              }
            }
            i++;
          }
          interruptButton.setButtonColor(ButtonConstants.BUTTON_COLOR);
          updateLayout();
        }
        ;
        for (int i = 0; i < activeLoggingsList.size(); i++)
        {
          LogContext currentLogContext = (LogContext)activeLoggingsList.elementAt(i);
          currentLogContext.interruptCheckbox.setState(false);
        }
      }
    }

    if ("saveAsCSV".equals(e.getActionCommand()))
    {
      finishedLogProcessesComboBox.removeAllItems();

      for (int i = 0; i < finishedLoggingsList.size(); i++)
      {
        LogContext object = (LogContext)finishedLoggingsList.elementAt(i);
        String description = object.description;
        finishedLogProcessesComboBox.addItem(description);
      }
      tree.setRootVisible(true);
      saveAsCSVFrame.setVisible(true);

    }

    if ("executeCSVSave".equals(e.getActionCommand()))
    {
      CPAction action = service.getCPAction("GetCSVData");

      if (action != null)
      {
        try
        {

          LogContext currentObject = null;
          serviceList = null;
          if (finishedLogProcessesComboBox.getSelectedIndex() >= 0 &&
            finishedLogProcessesComboBox.getSelectedIndex() < finishedLoggingsList.size())
          {
            currentObject = (LogContext)finishedLoggingsList.get(finishedLogProcessesComboBox.getSelectedIndex());
            fileNameField.setText(String.valueOf(currentObject.id));
            SecurityAwareTemplateControlPoint controlPoint = this.getControlPoint();

            long id = currentObject.id;

            action.getArgument("ID").setNumericValue(id);
            controlPoint.invokeAction(action);
            String logData = action.getOutArgument("LogData").getStringValue();
            saveAsCommaSeparatedValue(logData, (int)id, selectedPathField.getText());
          }
          saveAsCSVFrame.setVisible(false);
        } catch (Exception exc)
        {

        }
      }
    }
  }

  public void saveAsCommaSeparatedValue(String data, int id, String path)
  {
    try
    {
      // create directory if missing
      if (!new File(path).exists())
      {
        new File(path).mkdirs();
      }

      BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + id + ".CSV"), "utf-8"));

      writer.write(data);
      writer.close();
    } catch (Exception ex)
    {
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.gui_control_point.plugins.BaseCPServicePlugin#stateVariableChanged(de.fhg.fokus.magic.upnp.control_point.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    super.stateVariableChanged(stateVariable);
    if (stateVariable.getName().equals("FinishedTaskIDList"))
    {
      try
      {
        System.out.println("<<<<<<<<<<<<<<<in StateVariableChanged for fin" + stateVariable.getStringValue());
        updateLoggings(stateVariable.getStringValue(), finishedLoggingsList);
      } catch (Exception e)
      {
      }
    }

    if (stateVariable.getName().equals("ActiveTaskIDList"))
    {
      try
      {
        String activeLog = stateVariable.getStringValue();
        StringTokenizer stringTokenizer = new StringTokenizer(activeLog, ",");
        int count = stringTokenizer.countTokens();

        if (stateVariable.getStringValue() == null || count != 1)
        {
          CPStateVariable cpStateVariable = service.getCPStateVariable("FinishedTaskIDList");
          String lastElement = "";
          String values = cpStateVariable.getStringValue();
          StringTokenizer tokenizer = new StringTokenizer(values, ",");
          while (tokenizer.hasMoreElements())
          {
            lastElement = tokenizer.nextElement().toString();
          }
          String trim = lastElement.trim();
          System.out.println(">>>>>>>>>>>>>>>>>> last Element is  " + lastElement);
          CPAction action = service.getCPAction("GetLogData");

          System.out.println(lastElement.getClass());
          long ID = Long.parseLong(trim);
          System.out.println("!!!!!!!!!!!!!!!!");
          // long ID = I.longValue();

          action.getArgument("ID").setNumericValue(ID);
          controlPoint.invokeAction(action);
          String data = action.getOutArgument("Data").getValue().toString();
          System.out.println("================================" + data);
          saveToFile(data, ID);

        }
        updateLoggings(stateVariable.getStringValue(), activeLoggingsList);
      } catch (Exception e)
      {
      }
    }
  }

  public void saveToFile(String data, long ID)
  {

    String path = getLoggingPath();
    // create directory if missing
    if (!new File(path).exists())
    {
      new File(path).mkdirs();
    }

    BufferedWriter out;
    try
    {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ID + ".txt"), "utf-8"));

      out.write(data);

      out.close();
    } catch (FileNotFoundException e)
    {
    } catch (IOException e)
    {
    }
  }

  public String getLoggingPath()
  {
    String path = "";
    if (System.getProperty("os.name").equalsIgnoreCase("linux"))
    {
      path = System.getProperty("user.home");
    }
    if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
    {
      path = System.getProperty("user.home");
    }
    path = FileHelper.toValidDirectoryName(path);

    return path;

  }

  /** Updates the logging vector for either active or finished loggings */
  private void updateLoggings(String loggingsIDString, Vector loggingList)
  {
    Vector newIDs = new Vector();
    if (loggingsIDString != null && loggingsIDString.length() != 0)
    {
      // tokenize logID string
      StringTokenizer idTokenizer = new StringTokenizer(loggingsIDString, ",");
      while (idTokenizer.hasMoreElements())
      {
        try
        {
          newIDs.add(new Long(((String)idTokenizer.nextElement()).trim()));
        } catch (Exception e)
        {
        }
      }
      // remove all known finished IDs
      for (int i = 0; i < loggingList.size(); i++)
      {
        boolean knownID = false;
        int j = 0;
        // compare current log ID with all new IDs
        while (!knownID && j < newIDs.size())
        {
          LogContext currentLogContext = (LogContext)loggingList.elementAt(i);
          knownID = knownID || currentLogContext.id == ((Long)newIDs.elementAt(j)).longValue();
          if (knownID)
          {
            newIDs.remove(j);
          } else
          {
            j++;
          }
        }
      }
      // retrieve log data for all new IDs
      for (int i = 0; i < newIDs.size(); i++)
      {
        long logID = ((Long)newIDs.elementAt(i)).longValue();

        CPAction action = service.getCPAction("GetXMLData");
        if (action != null)
        {
          try
          {
            action.getArgument("ID").setNumericValue(logID);
            controlPoint.invokeAction(action);
            String xmlDescription = action.getArgument("LogData").getStringValue();

            LoggerParser parser = new LoggerParser();

            parser.parse(xmlDescription);

            String description = parser.getDescription();
            String deviceUDN = parser.getDeviceUDN();
            String deviceFriendlyName = parser.getDeviceFriendlyName();
            String serviceType = parser.getServiceType();
            String shortedServiceName = parser.getShortenedServiceID();
            String actionName = parser.getActionName();
            Date startTime = parser.getStartTime();
            Date endTime = parser.getEndTime();
            long interval = parser.getInterval();
            int valueCount = parser.getLogEntryVector().size();

            LogContext logContext =
              new LogContext(logID,
                description,
                deviceUDN,
                deviceFriendlyName,
                serviceType,
                shortedServiceName,
                actionName,
                startTime,
                endTime,
                interval,
                valueCount);

            loggingList.add(logContext);
          } catch (Exception ex)
          {
            System.out.println("Error requesting new log data: " + ex.getMessage());
          }
        }
      }
      updateLayout();
    } else
    {
      loggingList.removeAllElements();
      updateLayout();
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private methods //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Updates the layout of the plugin by the start of it or when does one of the StatVariable had
   * changed
   */
  public void updateLayout()
  {
    activeLoggingsPanel.removeAll();
    activeLoggingsPanel.invalidate();

    updateFinishedLoggingsPanel();
    updateActiveLoggingsPanel();

    finishedLoggingsPanel.revalidate();
    finishedLoggingsPanel.repaint();
    activeLoggingsPanel.revalidate();
    activeLoggingsPanel.repaint();
    validateTree();
  }

  /**
   * Updates the layout of the plugin by the start of it or when does one of the StatVariable had
   * changed
   */
  public void updateFinishedLoggingsPanel()
  {
    finishedLoggingsPanel.removeAll();
    finishedLoggingsPanel.invalidate();

    // show table headers
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);

    gridBagConstraints.gridx = 0;
    finishedLoggingsPanel.add(finishedDescriptionTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    finishedLoggingsPanel.add(finishedUDNTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 2;
    finishedLoggingsPanel.add(finishedStartTimeTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 3;
    finishedLoggingsPanel.add(finishedEndTimeTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 4;
    finishedLoggingsPanel.add(finishedIntervalTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 5;
    finishedLoggingsPanel.add(finishedMeasureCountTitleButton, gridBagConstraints);

    // no log data available
    if (finishedLoggingsList.size() == 0)
    {
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 7;
      if (initialReadLogData)
      {
        finishedLoggingsPanel.add(initialReadButton, gridBagConstraints);
      } else
      {
        finishedLoggingsPanel.add(noFinishedLogsButton, gridBagConstraints);
      }
      TreePath treePath = new TreePath(root);
      tree.expandPath(treePath);
    } else
    {
      // we have log data available
      for (int i = 0; i < finishedLoggingsList.size(); i++)
      {
        LogContext currentLogContext = (LogContext)finishedLoggingsList.elementAt(i);

        gridBagConstraints.gridy = i + 1;

        gridBagConstraints.gridx = 0;

        currentLogContext.descriptionButton.setButtonColor(currentLogContext == selectedLogContext
          ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.BUTTON_COLOR);
        finishedLoggingsPanel.add(currentLogContext.descriptionButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        finishedLoggingsPanel.add(currentLogContext.deviceUDNButton, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        finishedLoggingsPanel.add(currentLogContext.startTimeButton, gridBagConstraints);
        gridBagConstraints.gridx = 3;
        finishedLoggingsPanel.add(currentLogContext.endTimeButton, gridBagConstraints);
        gridBagConstraints.gridx = 4;
        finishedLoggingsPanel.add(currentLogContext.intervalButton, gridBagConstraints);
        gridBagConstraints.gridx = 5;
        finishedLoggingsPanel.add(currentLogContext.measureCountButton, gridBagConstraints);
        gridBagConstraints.gridx = 6;
        finishedLoggingsPanel.add(currentLogContext.deleteCheckbox, gridBagConstraints);
      }
      gridBagConstraints.gridy = finishedLoggingsList.size() + 2;
      gridBagConstraints.gridx = 5;
      finishedLoggingsPanel.add(saveAsCSVButton, gridBagConstraints);
      gridBagConstraints.gridx = 6;
      finishedLoggingsPanel.add(deleteButton, gridBagConstraints);
    }
    gridBagConstraints.gridx = 7;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    finishedLoggingsPanel.add(finishedLoggingsFillPanel, gridBagConstraints);

    finishedLoggingsPanel.revalidate();
    finishedLoggingsPanel.repaint();
    validateTree();
  }

  /** Updates the layout of the plugin at the start or when one of the StateVariables has changed */
  public void updateActiveLoggingsPanel()
  {
    activeLoggingsPanel.removeAll();
    activeLoggingsPanel.invalidate();

    // show table headers
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);

    activeLoggingsPanel.add(activeDescriptionTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 1;
    activeLoggingsPanel.add(activeUDNTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 2;
    activeLoggingsPanel.add(activeStartTimeTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 3;
    activeLoggingsPanel.add(activeEndTimeTitleButton, gridBagConstraints);
    gridBagConstraints.gridx = 4;
    activeLoggingsPanel.add(activeIntervalTitleButton, gridBagConstraints);

    // no log data available
    if (activeLoggingsList.size() == 0)
    {
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 5;
      if (!initialReadLogData)
      {
        activeLoggingsPanel.add(noActiveLogsButton, gridBagConstraints);
      }
    } else
    {
      // we have active logs
      for (int i = 0; i < activeLoggingsList.size(); i++)
      {
        LogContext currentLogContext = (LogContext)activeLoggingsList.elementAt(i);

        gridBagConstraints.gridy = i + 1;

        gridBagConstraints.gridx = 0;

        currentLogContext.descriptionButton.setButtonColor(currentLogContext == selectedActiveLogContext
          ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.BUTTON_COLOR);
        activeLoggingsPanel.add(currentLogContext.descriptionButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        activeLoggingsPanel.add(currentLogContext.deviceUDNButton, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        activeLoggingsPanel.add(currentLogContext.startTimeButton, gridBagConstraints);
        gridBagConstraints.gridx = 3;
        activeLoggingsPanel.add(currentLogContext.endTimeButton, gridBagConstraints);
        gridBagConstraints.gridx = 4;
        activeLoggingsPanel.add(currentLogContext.intervalButton, gridBagConstraints);
        gridBagConstraints.gridx = 5;
        activeLoggingsPanel.add(currentLogContext.interruptCheckbox, gridBagConstraints);
      }
      gridBagConstraints.gridy = activeLoggingsList.size() + 2;
      gridBagConstraints.gridx = 5;
      activeLoggingsPanel.add(interruptButton, gridBagConstraints);
    }
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = activeLoggingsList.size() + 2;
    activeLoggingsPanel.add(newLoggingButton, gridBagConstraints);

    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    activeLoggingsPanel.add(activeLoggingsFillPanel, gridBagConstraints);

    activeLoggingsPanel.revalidate();
    activeLoggingsPanel.repaint();
    validateTree();
  }

  /** Updates the selected log context */
  private void selectLogContext(LogContext context)
  {
    if (context == null)
    {
      selectedLogContext = null;
      contentArea.clearContent();
      updateLayout();
    }
    if (context != selectedLogContext && context != null)
    {
      selectedLogContext = context;
      contentArea.clearContent();
      contentArea.addLine("Service: " + context.shortenedServiceName);
      contentArea.addLine("Action: " + context.actionName);

      updateLayout();
    }
  }

  /** Collect initial log data */
  public void run()
  {
    /*
     * updateLayout(); try { finishedLoggingsList.removeAllElements(); CPAction action =
     * service.getCPAction("GetFinishedTaskIDList");
     * 
     * controlPoint.invokeAction(action);
     * 
     * String finishedLoggingsIDString = action.getOutArgument("IDList").getStringValue();
     * updateLoggings(finishedLoggingsIDString, finishedLoggingsList); } catch (Exception e) {
     * System.out.println(e.getMessage()); } try { CPAction action =
     * service.getCPAction("GetActiveTaskIDList");
     * 
     * controlPoint.invokeAction(action);
     * 
     * String activeLoggingsIDString = action.getOutArgument("IDList").getStringValue();
     * updateLoggings(activeLoggingsIDString, activeLoggingsList); } catch (Exception e) {
     * System.out.println(e.getMessage()); }
     */
    initialReadLogData = false;
    updateLayout();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Private classes //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  /* This class represent one finished or active logging */
  private class LogContext
  {

    private long          id;

    public String         description;

    public String         deviceUDN;

    public String         deviceFriendlyName;

    public String         serviceType;

    public String         shortenedServiceName;

    public String         actionName;

    public Date           startTimeAndDate;

    public Date           endTimeAndDate;

    public long           interval;

    public int            valueCount;

    public SmoothButton   descriptionButton;

    public SmoothButton   deviceUDNButton;

    public SmoothButton   startTimeButton;

    public SmoothButton   endTimeButton;

    public SmoothButton   intervalButton;

    public SmoothButton   measureCountButton;

    public SmoothCheckBox deleteCheckbox;

    public SmoothCheckBox interruptCheckbox;

    public LogContext(long id,
      String description,
      String deviceUDN,
      String deviceFriendlyName,
      String serviceType,
      String shortedServiceName,
      String actionName,
      Date startTimeAndDate,
      Date endTimeAndDate,
      long interval,
      int valueCount)
    {
      this.id = id;
      this.description = description;
      this.deviceUDN = deviceUDN;
      this.deviceFriendlyName = deviceFriendlyName;
      this.serviceType = serviceType;
      this.shortenedServiceName = shortedServiceName;
      this.actionName = actionName;
      this.startTimeAndDate = startTimeAndDate;
      this.endTimeAndDate = endTimeAndDate;
      this.interval = interval;
      this.valueCount = valueCount;

      descriptionButton = new SmoothButton(new Dimension(150, 30), 12, description, id + "");
      descriptionButton.setBackground(plugin.getBackground());
      descriptionButton.addActionListener(plugin);

      deviceUDNButton = new SmoothButton(new Dimension(100, 30), 12, deviceFriendlyName, null);
      deviceUDNButton.setBackground(plugin.getBackground());

      startTimeButton =
        new SmoothButton(new Dimension(150, 30),
          12,
          DateTimeHelper.formatDateTimeForGermany(this.startTimeAndDate),
          null);
      startTimeButton.setBackground(plugin.getBackground());

      endTimeButton =
        new SmoothButton(new Dimension(150, 30), 12, DateTimeHelper.formatDateTimeForGermany(this.endTimeAndDate), null);
      endTimeButton.setBackground(plugin.getBackground());

      intervalButton = new SmoothButton(new Dimension(100, 30), 12, interval / 1000 + "", null);
      intervalButton.setBackground(plugin.getBackground());

      measureCountButton = new SmoothButton(new Dimension(100, 30), 12, valueCount + "", null);
      measureCountButton.setBackground(plugin.getBackground());

      deleteCheckbox = new SmoothCheckBox(new Dimension(28, 25), false, id + "");
      interruptCheckbox = new SmoothCheckBox(new Dimension(28, 25), false, id + "");
    }

  }

  /*
   * This class handles all events that are generated by buttons etc. in the new logging frame.
   * 
   */
  private class NewLoggingEventListener implements ActionListener, ItemListener
  {

    public void actionPerformed(ActionEvent ae)
    {
      // start new logger task
      if ("startLogging".equals(ae.getActionCommand()))
      {
        CPAction action = service.getCPAction("StartTask");

        newLoggingFrame.setVisible(false);

        String description = descriptionField.getText();

        // parse start time
        String startTimeString = startTimeField.getText() + ":00";
        Calendar startDateAndTime = DateTimeHelper.dateToCalendar(startDateField.getDate());
        Calendar startTime = DateTimeHelper.dateToCalendar(DateTimeHelper.getTimeFromGermanLocale(startTimeString));
        startDateAndTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
        startDateAndTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
        startDateAndTime.set(Calendar.SECOND, 0);
        startDateAndTime.set(Calendar.MILLISECOND, 0);

        Date currentDate = DateTimeHelper.getDate();
        if (startDateAndTime.before(currentDate))
        {
          System.out.println("Start date is in the past, use current date");
          startDateAndTime = DateTimeHelper.dateToCalendar(currentDate);
        }

        // parse end time
        String endTimeString = endTimeField.getText() + ":00";
        Calendar endDateAndTime = DateTimeHelper.dateToCalendar(endDateField.getDate());
        Calendar endTime = DateTimeHelper.dateToCalendar(DateTimeHelper.getTimeFromGermanLocale(endTimeString));
        endDateAndTime.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
        endDateAndTime.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
        endDateAndTime.set(Calendar.SECOND, 0);
        endDateAndTime.set(Calendar.MILLISECOND, 0);

        long interval = Long.parseLong(intervalField.getText()) * 1000;
        String actionName = actionNameComboBox.getSelectedItem().toString();

        try
        {
          action.getArgument("Description").setValue(description);
          action.getArgument("DeviceUDN").setValue(deviceUDN);
          action.getArgument("ServiceID").setValue(serviceURN);
          action.getArgument("ActionName").setValue(actionName);
          action.getArgument("OutArgument").setValue(currentOutArgumentType);
          action.getArgument("StartTime").setDateValue(startDateAndTime.getTime());
          action.getArgument("EndTime").setDateValue(endDateAndTime.getTime());
          action.getArgument("Interval").setNumericValue(interval);

        } catch (Exception ex)
        {
        }
        try
        {
          controlPoint.invokeAction(action);
        } catch (InvokeActionException e)
        {
          System.out.println(" 1 InvokeActionException in actionPerformed(ActionEvent ae) of the inner class EditAction of the Wrapper classLoggerPlugin");
        } catch (ActionFailedException e)
        {
          System.out.println("2 ActionFailedException in actionPerformed(ae) of the inner class EditAction of the Wrapper classLoggerPlugin");
        }
        updateLayout();
      }

      // open frame to enter new logger task
      if ("newLogging".equals(ae.getActionCommand()))
      {
        deviceUDNComboBox.removeAllItems();
        for (int i = 0; i < controlPoint.getCPDeviceCount(); i++)
        {
          deviceUDNComboBox.addItem(controlPoint.getCPDevice(i).getFriendlyName());
        }
        newLoggingFrame.setVisible(true);
      }
    }

    public void itemStateChanged(ItemEvent ie)
    {
      // device chosen, show services
      if (ie.getSource().equals(deviceUDNComboBox))
      {
        currentDevice = null;
        serviceList = null;
        serviceTypeComboBox.removeAllItems();
        if (deviceUDNComboBox.getSelectedIndex() >= 0 &&
          deviceUDNComboBox.getSelectedIndex() < controlPoint.getCPDeviceCount())
        {
          currentDevice = controlPoint.getCPDevice(deviceUDNComboBox.getSelectedIndex());
          deviceUDN = currentDevice.getUDN();
          serviceList = currentDevice.getCPServiceTable();
          for (int i = 0; i < serviceList.length; i++)
          {
            serviceTypeComboBox.addItem(serviceList[i].getShortenedServiceId());
          }
        }
      }

      // service chosen, show action list
      if (ie.getSource().equals(serviceTypeComboBox) && currentDevice != null && serviceList != null)
      {
        actionNameComboBox.removeAllItems();

        for (int i = 0; i < serviceList.length; i++)
        {
          if (ie.getItem().toString().equals(serviceList[i].getShortenedServiceId()))
          {
            actionList = serviceList[i].getCPActionTable();
            currentService = (CPService)serviceList[serviceTypeComboBox.getSelectedIndex()];
            serviceURN = currentService.getServiceType();
            for (int j = 0; j < actionList.length; j++)
            {
              actionNameComboBox.addItem(actionList[j].getName());
            }
          }
        }
      }
      // action chosen, show out argument list
      if (ie.getSource().equals(actionNameComboBox) && actionList != null && currentService != null)
      {
        outArgumentComboBox.removeAllItems();

        for (int i = 0; i < actionList.length; i++)
        {
          if (ie.getItem().toString().equals(actionList[i].getName()))
          {
            outArgumentList = actionList[i].getOutArgumentTable();
            if (outArgumentList != null)
            {
              for (int j = 0; j < outArgumentList.length; j++)
              {
                outArgumentComboBox.addItem(outArgumentList[j].getName());
                currentOutArgument = (Argument)outArgumentList[outArgumentComboBox.getSelectedIndex()];
                currentOutArgumentType = currentOutArgument.getName();
              }
            }
          }
        }
      }
      if (ie.getSource().equals(finishedLogProcessesComboBox))
      {
        LogContext currentObject = null;
        serviceList = null;
        if (finishedLogProcessesComboBox.getSelectedIndex() >= 0 &&
          finishedLogProcessesComboBox.getSelectedIndex() < finishedLoggingsList.size())
        {
          currentObject = (LogContext)finishedLoggingsList.get(finishedLogProcessesComboBox.getSelectedIndex());
          fileNameField.setText(String.valueOf(currentObject.id));
        }
      }
    }
  }

  private class SaveAsCSVAction extends Thread implements TreeSelectionListener
  {

    public void valueChanged(TreeSelectionEvent tse)
    {
      treeModel.setAsksAllowsChildren(true);
      String pathAsString = "";
      TreePath treePath = tse.getPath();
      Object[] object = treePath.getPath();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)object[object.length - 1];
      for (int i = 0; i < object.length; i++)
      {
        if (System.getProperty("os.name").equalsIgnoreCase("linux"))
        {
          pathAsString += object[i].toString() + "/";
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
        {
          pathAsString += object[i].toString() + "\\";
        }
      }

      String sub = pathAsString.substring(0, pathAsString.length() - 1);

      File file = new File(sub);
      File[] fileAndDirectoryArray = file.listFiles();
      for (int i = 0; i < fileAndDirectoryArray.length; i++)
      {
        if (!tree.hasBeenExpanded(treePath))
        {
          if (fileAndDirectoryArray[i].isDirectory())
          {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(fileAndDirectoryArray[i].getName());
            Pattern pattern = Pattern.compile("^[a-zA-Z_0-9]*");
            Matcher matcher = pattern.matcher(child.toString());
            boolean patternMatch = matcher.matches();
            if (patternMatch)
            {
              treeModel.insertNodeInto(child, node, node.getChildCount());
            }
          }
        }
      }
      selectedPathField.setText(pathAsString);
      TreeNode[] path = treeModel.getPathToRoot(node);
      tree.expandPath(new TreePath(path));
    }

    public void run()
    {

    }
  }
}

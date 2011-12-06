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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;

import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.GUIControlConstants;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ActionGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.DeviceGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.helpers.ServiceGUIContext;
import de.fraunhofer.fokus.upnp.core.examples.gui_control_point.panels.JArgumentEncapsulationPanel;
import de.fraunhofer.fokus.upnp.core_security.templates.SecurityAwareTemplateControlPoint;
import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.exceptions.ActionFailedException;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;
import de.fraunhofer.fokus.upnp.util.swing.JPanelDialog;

/**
 * This plugin is used for services that do not have a specific plugin
 * 
 * @author Alexander Koenig
 */
public class StandardServicePlugin extends BaseCPServicePlugin
{

  private static final long serialVersionUID                                 = 1L;

  private static int        ACTION_TYPE_UNPERSONALIZED                       = 1;

  private static int        ACTION_TYPE_PUBLIC_KEY_PERSONALIZED              = 2;

  private static int        ACTION_TYPE_SYMMETRIC_KEY_PERSONALIZED           = 3;

  private static int        ACTION_TYPE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED = 4;

  private static int        ACTION_TYPE_SECURED                              = 5;

  private static int        ACTION_TYPE_SECURED_ENCRYPTED                    = 6;

  /** Device context associated with this service */
  private DeviceGUIContext  deviceContext;

  /** Service context */
  private ServiceGUIContext serviceContext;

  /** Device for this service */
  private CPDevice          device;

  /** Currently selected action */
  private ActionGUIContext  currentActionContext;

  private boolean           terminateThread                                  = false;

  private boolean           terminated                                       = false;

  /** Action that waits for sending */
  private ActionGUIContext  pendingAction                                    = null;

  private int               pendingActionType                                = ACTION_TYPE_UNPERSONALIZED;

  private JPanel            stateVariableFillPanel                           = new JPanel();

  private JPanel            actionContentFillPanel                           = new JPanel();

  private JPanel            actionInArgumentFillPanel                        = new JPanel();

  private JPanel            actionOutArgumentFillPanel                       = new JPanel();

  /** Creates new form StandardServicePlugin */
  public StandardServicePlugin(JFrame frame,
    SecurityAwareTemplateControlPoint controlPoint,
    DeviceGUIContext deviceContext,
    CPService service)
  {
    super();
    setControlPoint(controlPoint);
    setFrame(frame);
    setCPService(service);

    this.deviceContext = deviceContext;
    device = service.getCPDevice();

    serviceContext = new ServiceGUIContext(deviceContext, service);
    serviceContext.addActionListener(this);
    // add tooltips if UPnP doc was already read
    if (device.getUPnPDocFromLanguageTable().get("de") != null)
    {
      serviceContext.upnpDocRead();
    }
    currentActionContext = null;

    initComponents();

    stateVariableFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    actionContentFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    actionInArgumentFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    actionOutArgumentFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jDividerPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jErrorPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jInArgumentsPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jOutArgumentsPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jStateVariableContentPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jStateVariablePanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jActionContentScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jInArgumentScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jOutArgumentScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);
    jStateVariableScrollPane.setBackground(ButtonConstants.BACKGROUND_COLOR);

    // initialize panel layout
    updateLayout();

    Thread thread = new Thread(this);
    thread.setName("StandardServicePlugin");
    thread.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.core.examples.gui_control_point.plugins.BasePlugin#deviceEvent(de.fraunhofer.fokus.upnp.core.control_point.CPDevice,
   *      int, java.lang.Object)
   */
  public void deviceEvent(CPDevice device, int eventCode, Object eventParameter)
  {
    if (device == this.device && eventCode == UPnPConstant.DEVICE_EVENT_USAGE_SERVICE_READ)
    {
      serviceContext.upnpDocRead();
    }
    super.deviceEvent(device, eventCode, eventParameter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    serviceContext.updateStateVariable(stateVariable);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    // System.out.println("StandardActionPlugin: Action performed with command
    // "+e.getActionCommand());
    // search action
    boolean found = false;
    int i = 0;
    while (!found && i < serviceContext.getActionCount())
    {
      found = serviceContext.getAction(i).getName().equals(e.getActionCommand());
      if (!found)
      {
        i++;
      }
    }
    if (found && serviceContext.getActionContext(i) != currentActionContext)
    {
      currentActionContext = serviceContext.getActionContext(i);
      currentActionContext.getErrorButton().setText("");
      updateLayout();
    }
    actionPerformedActionCheck(e);
  }

  /** Checks for actions that must be executed. */
  public void actionPerformedActionCheck(ActionEvent e)
  {
    boolean found = false;
    int i = 0;
    // search in_argument
    if (currentActionContext != null)
    {
      found = false;
      i = 0;
      Argument argument = null;
      while (!found && i < currentActionContext.getInArgumentCount())
      {
        argument = currentActionContext.getInArgument(i);
        found = argument.getName().equals(e.getActionCommand());
        if (!found)
        {
          i++;
        }
      }
      if (found && (pendingAction == null || pendingAction != currentActionContext))
      {
        try
        {
          // open dialog to change value
          JPanelDialog argumentDialog = new JPanelDialog(frame, true);
          argumentDialog.setTitle(argument.getName());
          // set value to currently shown value
          if (currentActionContext.getInArgumentButton(argument).getValue().length() > 0)
          {
            argument.setValueFromString(currentActionContext.getInArgumentButton(argument).getValue());
          } else
          {
            argument.setValue(argument.getRelatedStateVariable().getDefaultValue());
          }
          // add argument specific panel
          JArgumentEncapsulationPanel argumentPanel = new JArgumentEncapsulationPanel(argument);
          argumentDialog.addPanel(argumentPanel, 5);
          // center dialog
          argumentDialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - argumentDialog.getSize().width) / 2,
            (Toolkit.getDefaultToolkit().getScreenSize().height - argumentDialog.getSize().height) / 2);
          argumentDialog.setVisible(true);
          if (argumentDialog.isValidResult())
          {
            argument.setValue(argumentPanel.getValue());
            // escape argument if its a string
            if (argument.getRelatedStateVariable().getDataType().isStringValue())
            {
              argument.setValue(StringHelper.xmlToEscapedString((String)argumentPanel.getValue()));
            }
            currentActionContext.updateArgumentButton(argument);
          }
        } catch (Exception ex)
        {
          System.out.println("ERROR: " + ex.getMessage());
        }
        updateLayout();
      }
      // try to execute current action
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_ACTION) && pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_UNPERSONALIZED;
        updateLayout();
      }
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_PUBLIC_KEY_PERSONALIZED_ACTION) &&
        pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_PUBLIC_KEY_PERSONALIZED;
        updateLayout();
      }
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_SYMMETRIC_KEY_PERSONALIZED_ACTION) &&
        pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_SYMMETRIC_KEY_PERSONALIZED;
        updateLayout();
      }
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED_ACTION) &&
        pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED;
        updateLayout();
      }
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_SIGNED_ACTION) && pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_SECURED;
        updateLayout();
      }
      // try to execute current action
      if (e.getActionCommand().equals(GUIControlConstants.INVOKE_ENCRYPTED_ACTION) && pendingAction == null)
      {
        // execute in thread to improve user interaction
        pendingAction = currentActionContext;
        pendingActionType = ACTION_TYPE_SECURED_ENCRYPTED;
        updateLayout();
      }
    }
  }

  /** Thread for plugin specific algorithms */
  public void run()
  {
    while (!terminateThread)
    {
      if (pendingAction != null)
      {
        pendingAction.getErrorButton().setText("");
        try
        {
          CPAction action = pendingAction.getCPAction();
          if (pendingActionType == ACTION_TYPE_SECURED_ENCRYPTED)
          {
            controlPoint.invokeEncryptedAction(controlPoint.getSecurityAwareCPDeviceObject(device.getUDN()), action);
          }
          if (pendingActionType == ACTION_TYPE_SECURED)
          {
            // this ensures that actions are signed for security aware devices
            controlPoint.invokeAction(action);
          }
          if (pendingActionType == ACTION_TYPE_PUBLIC_KEY_PERSONALIZED)
          {
            controlPoint.invokePublicKeyPersonalizedAction(action);
          }
          if (pendingActionType == ACTION_TYPE_SYMMETRIC_KEY_PERSONALIZED)
          {
            controlPoint.invokeSymmetricKeyPersonalizedAction(false, action);
          }
          if (pendingActionType == ACTION_TYPE_SYMMETRIC_KEY_ENCRYPTED_PERSONALIZED)
          {
            controlPoint.invokeSymmetricKeyPersonalizedAction(true, action);
          }
          if (pendingActionType == ACTION_TYPE_UNPERSONALIZED)
          {
            controlPoint.invokeUnpersonalizedAction(action);
          }

          // process output parameters
          if (action.getOutArgumentTable() != null)
          {
            for (int j = 0; j < action.getOutArgumentTable().length; j++)
            {
              pendingAction.updateArgumentButton(action.getOutArgumentTable()[j]);
            }
          }
        } catch (ActionFailedException afe)
        {
          System.out.println(afe.getMessage());
          pendingAction.getErrorButton().setText(GUIControlConstants.ERROR + afe.getErrorCode() + ": " +
            afe.getErrorDescription());
          // try to translate error
          deviceContext.getDeviceTranslations().setTranslationForButton(pendingAction.getErrorButton(),
            afe.getErrorDescription(),
            GUIControlConstants.ERROR + afe.getErrorCode() + ": ",
            "");

        } catch (Exception ex)
        {
          pendingAction.getErrorButton().setText(GUIControlConstants.ERROR + ex.getMessage());
        }
        pendingAction = null;
        updateLayout();
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /** Terminates the plugin */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(50);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents()// GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jActionPanel = new javax.swing.JPanel();
    jActionContentScrollPane = new javax.swing.JScrollPane();
    jActionContentPanel = new javax.swing.JPanel();
    jActionDividerPanel = new javax.swing.JPanel();
    jErrorPanel = new javax.swing.JPanel();
    jInArgumentScrollPane = new javax.swing.JScrollPane();
    jInArgumentsPanel = new javax.swing.JPanel();
    jOutArgumentScrollPane = new javax.swing.JScrollPane();
    jOutArgumentsPanel = new javax.swing.JPanel();
    jStateVariablePanel = new javax.swing.JPanel();
    jDividerPanel = new javax.swing.JPanel();
    jStateVariableScrollPane = new javax.swing.JScrollPane();
    jStateVariableContentPanel = new javax.swing.JPanel();

    setLayout(new java.awt.BorderLayout());

    setBackground(new java.awt.Color(204, 204, 255));
    jActionPanel.setLayout(new java.awt.GridBagLayout());

    jActionPanel.setBackground(new java.awt.Color(204, 204, 255));
    jActionContentScrollPane.setBorder(null);
    jActionContentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jActionContentScrollPane.setMaximumSize(null);
    jActionContentScrollPane.setMinimumSize(null);
    jActionContentPanel.setLayout(new java.awt.GridBagLayout());

    jActionContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    jActionContentScrollPane.setViewportView(jActionContentPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.weighty = 1.0;
    jActionPanel.add(jActionContentScrollPane, gridBagConstraints);

    jActionDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jActionDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jActionDividerPanel.setMinimumSize(new java.awt.Dimension(4, 10));
    jActionDividerPanel.setPreferredSize(new java.awt.Dimension(4, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    jActionPanel.add(jActionDividerPanel, gridBagConstraints);

    jErrorPanel.setBackground(new java.awt.Color(204, 204, 255));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jActionPanel.add(jErrorPanel, gridBagConstraints);

    jInArgumentScrollPane.setBorder(null);
    jInArgumentsPanel.setLayout(new java.awt.GridBagLayout());

    jInArgumentsPanel.setBackground(new java.awt.Color(204, 204, 255));
    jInArgumentScrollPane.setViewportView(jInArgumentsPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.weighty = 1.0;
    jActionPanel.add(jInArgumentScrollPane, gridBagConstraints);

    jOutArgumentScrollPane.setBorder(null);
    jOutArgumentsPanel.setLayout(new java.awt.GridBagLayout());

    jOutArgumentsPanel.setBackground(new java.awt.Color(204, 204, 255));
    jOutArgumentScrollPane.setViewportView(jOutArgumentsPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.weighty = 1.0;
    jActionPanel.add(jOutArgumentScrollPane, gridBagConstraints);

    add(jActionPanel, java.awt.BorderLayout.CENTER);

    jStateVariablePanel.setLayout(new java.awt.BorderLayout());

    jStateVariablePanel.setBackground(new java.awt.Color(204, 204, 255));
    jDividerPanel.setBackground(new java.awt.Color(204, 204, 255));
    jDividerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jDividerPanel.setMinimumSize(new java.awt.Dimension(14, 4));
    jDividerPanel.setPreferredSize(new java.awt.Dimension(14, 4));
    jStateVariablePanel.add(jDividerPanel, java.awt.BorderLayout.SOUTH);

    jStateVariableScrollPane.setBorder(null);
    jStateVariableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jStateVariableScrollPane.setPreferredSize(null);
    jStateVariableContentPanel.setLayout(new java.awt.GridBagLayout());

    jStateVariableContentPanel.setBackground(new java.awt.Color(204, 204, 255));
    jStateVariableScrollPane.setViewportView(jStateVariableContentPanel);

    jStateVariablePanel.add(jStateVariableScrollPane, java.awt.BorderLayout.CENTER);

    add(jStateVariablePanel, java.awt.BorderLayout.NORTH);

  }// GEN-END:initComponents

  /** Redraws the layout of the plugin */
  private void updateLayout()
  {
    jStateVariableContentPanel.removeAll();
    jActionContentPanel.removeAll();
    jInArgumentsPanel.removeAll();
    jOutArgumentsPanel.removeAll();
    jErrorPanel.removeAll();

    jStateVariableContentPanel.invalidate();
    jActionContentPanel.invalidate();
    jInArgumentsPanel.invalidate();
    jOutArgumentsPanel.invalidate();
    jErrorPanel.invalidate();

    GridBagConstraints gridBagConstraints;
    int columns = 3;
    // add state variables
    for (int i = 0; i < serviceContext.getStateVariableCount(); i++)
    {
      CPStateVariable variable = serviceContext.getStateVariable(i);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = i % columns;
      gridBagConstraints.gridy = i / columns;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      deviceContext.getDeviceTranslations().setTranslationForButton(serviceContext.getStateVariableButton(i),
        variable.getName(),
        ":");
      jStateVariableContentPanel.add(serviceContext.getStateVariableButton(i), gridBagConstraints);
    }
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jStateVariableContentPanel.add(stateVariableFillPanel, gridBagConstraints);

    // add actions
    for (int i = 0; i < serviceContext.getActionCount(); i++)
    {
      ActionGUIContext actionContext = serviceContext.getActionContext(i);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      actionContext.getActionButton().setSelected(actionContext == currentActionContext);
      deviceContext.getDeviceTranslations().setTranslationForButton(actionContext.getActionButton(),
        actionContext.getCPAction().getName());
      jActionContentPanel.add(actionContext.getActionButton(), gridBagConstraints);
    }
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jActionContentPanel.add(actionContentFillPanel, gridBagConstraints);

    // show error
    if (currentActionContext != null && currentActionContext.getErrorButton().getText().length() > 0)
    {
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);
      jErrorPanel.add(currentActionContext.getErrorButton(), gridBagConstraints);
    }
    updateLayoutCurrentAction();
    jActionDividerPanel.setVisible(currentActionContext != null);

    jStateVariableContentPanel.repaint();
    jActionContentPanel.repaint();
    jInArgumentsPanel.repaint();
    jOutArgumentsPanel.repaint();
    jErrorPanel.repaint();
    validateTree();
  }

  private void updateLayoutCurrentAction()
  {
    if (currentActionContext != null)
    {
      GridBagConstraints gridBagConstraints;
      // add in arguments
      for (int i = 0; i < currentActionContext.getInArgumentCount(); i++)
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        // set color and selectability
        currentActionContext.getInArgumentButton(i).setSelectable(pendingAction == null ||
          currentActionContext != pendingAction);
        deviceContext.getDeviceTranslations().setTranslationForButton(currentActionContext.getInArgumentButton(i),
          currentActionContext.getInArgument(i).getName(),
          ":");

        jInArgumentsPanel.add(currentActionContext.getInArgumentButton(i), gridBagConstraints);
      }
      // leave space between arguments and invoke button
      if (currentActionContext.getInArgumentCount() > 0)
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(20, 5, 2, 5);
        JPanel gapPanel = new JPanel();
        gapPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
        jInArgumentsPanel.add(gapPanel, gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new Insets(2, 5, 2, 5);

      boolean selectable = pendingAction == null || currentActionContext != pendingAction;

      currentActionContext.getInvokeActionButton().setSelectable(selectable);
      currentActionContext.getInvokeActionButton().setButtonColor(selectable ? ButtonConstants.ACTIVE_BUTTON_COLOR
        : ButtonConstants.DISABLED_BUTTON_COLOR);
      jInArgumentsPanel.add(currentActionContext.getInvokeActionButton(), gridBagConstraints);

      // show invoke personalized action button
      currentActionContext.getInvokePersonalizedActionButton().setSelectable(selectable);
      currentActionContext.getInvokePersonalizedActionButton().setButtonColor(selectable
        ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.DISABLED_BUTTON_COLOR);
      jInArgumentsPanel.add(currentActionContext.getInvokePersonalizedActionButton(), gridBagConstraints);

      currentActionContext.getInvokeSymmetricPersonalizedActionButton().setSelectable(selectable);
      currentActionContext.getInvokeSymmetricPersonalizedActionButton().setButtonColor(selectable
        ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.DISABLED_BUTTON_COLOR);
      jInArgumentsPanel.add(currentActionContext.getInvokeSymmetricPersonalizedActionButton(), gridBagConstraints);

      currentActionContext.getInvokeSymmetricEncryptedPersonalizedActionButton().setSelectable(selectable);
      currentActionContext.getInvokeSymmetricEncryptedPersonalizedActionButton().setButtonColor(selectable
        ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.DISABLED_BUTTON_COLOR);
      jInArgumentsPanel.add(currentActionContext.getInvokeSymmetricEncryptedPersonalizedActionButton(),
        gridBagConstraints);

      // add invoke signed or encrypted button if device supports this mode
      if (controlPoint.supportsDeviceSecurity(device))
      {
        currentActionContext.getInvokeSignedActionButton().setSelectable(selectable);
        currentActionContext.getInvokeSignedActionButton().setButtonColor(selectable
          ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.DISABLED_BUTTON_COLOR);
        jInArgumentsPanel.add(currentActionContext.getInvokeSignedActionButton(), gridBagConstraints);

        currentActionContext.getInvokeEncryptedActionButton().setSelectable(selectable);
        currentActionContext.getInvokeEncryptedActionButton().setButtonColor(selectable
          ? ButtonConstants.ACTIVE_BUTTON_COLOR : ButtonConstants.DISABLED_BUTTON_COLOR);
        jInArgumentsPanel.add(currentActionContext.getInvokeEncryptedActionButton(), gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      jInArgumentsPanel.add(actionInArgumentFillPanel, gridBagConstraints);

      // add out arguments
      for (int i = 0; i < currentActionContext.getOutArgumentCount(); i++)
      {
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        deviceContext.getDeviceTranslations().setTranslationForButton(currentActionContext.getOutArgumentButton(i),
          currentActionContext.getOutArgument(i).getName(),
          ":");

        jOutArgumentsPanel.add(currentActionContext.getOutArgumentButton(i), gridBagConstraints);
      }
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
      jOutArgumentsPanel.add(actionOutArgumentFillPanel, gridBagConstraints);
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel      jActionContentPanel;

  private javax.swing.JScrollPane jActionContentScrollPane;

  private javax.swing.JPanel      jActionDividerPanel;

  private javax.swing.JPanel      jActionPanel;

  private javax.swing.JPanel      jDividerPanel;

  private javax.swing.JPanel      jErrorPanel;

  private javax.swing.JScrollPane jInArgumentScrollPane;

  private javax.swing.JPanel      jInArgumentsPanel;

  private javax.swing.JScrollPane jOutArgumentScrollPane;

  private javax.swing.JPanel      jOutArgumentsPanel;

  private javax.swing.JPanel      jStateVariableContentPanel;

  private javax.swing.JPanel      jStateVariablePanel;

  private javax.swing.JScrollPane jStateVariableScrollPane;
  // End of variables declaration//GEN-END:variables

}

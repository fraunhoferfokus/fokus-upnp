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
package de.fraunhofer.fokus.upnp.core.test;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.core.AbstractStateVariable;
import de.fraunhofer.fokus.upnp.core.Argument;
import de.fraunhofer.fokus.upnp.core.Log4jHelper;
import de.fraunhofer.fokus.upnp.core.control_point.CPAction;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.control_point.CPMessageProcessorFactory;
import de.fraunhofer.fokus.upnp.core.control_point.CPService;
import de.fraunhofer.fokus.upnp.core.control_point.CPStateVariable;
import de.fraunhofer.fokus.upnp.core.control_point.ControlPoint;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.core.event.ICPStateVariableListener;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * Simple graphic controlpoint.
 * 
 * @author Michael Rother
 * @created 14 June 2002
 */
public class UPnPControlPoint implements ICPDeviceEventListener, ICPStateVariableListener, ActionListener, ItemListener
{
  private static final String ACTION_DD_TEXT  = "Action wählen";

  private static final String DEVICE_DD_TEXT  = "Device wählen";

  private static final String SERVICE_DD_TEXT = "Service wählen";

  private Choice              actionChoice;

  private Choice              deviceChoice;

  private Hashtable           devices         = new Hashtable();

  private Choice              serviceChoice;

  private TextField           valueText;

  private TextField           value2Text;

  /**
   * Creates a new <code>GUIControlPoint</code>.
   */
  public UPnPControlPoint()
  {
    UPnPStartupConfiguration startupConfiguration =
      new UPnPStartupConfiguration(FileHelper.getResourceDirectoryName() + "UPnPControlPoint.xml");
    if (startupConfiguration.isValid())
    {
      new ControlPoint(startupConfiguration, new CPMessageProcessorFactory(), this, this).sendSearchRootDeviceMessage();
      System.err.println("GUI Control Point started");
      initGui();
    }
  }

  /**
   * Initializes the action dropdown with the actions of a given service.
   * 
   * @param service
   *          The service to be used.
   */
  private void initActionChoice(CPService service)
  {
    actionChoice.removeAll();
    actionChoice.add(ACTION_DD_TEXT);

    CPAction[] actions = service.getCPActionTable();

    if (actions != null)
    {
      for (int i = 0; i < actions.length; i++)
      {
        actionChoice.add(actions[i].getName());
      }
    }
  }

  /**
   * Initializes the device dropdown.
   */
  private void initDeviceChoice()
  {
    deviceChoice.removeAll();
    deviceChoice.add(DEVICE_DD_TEXT);

    for (Enumeration e = devices.keys(); e.hasMoreElements();)
    {
      deviceChoice.add("" + e.nextElement());
    }
  }

  /**
   * Initializes the control point.
   */
  private void initGui()
  {
    Panel mainPanel = new Panel(new BorderLayout());
    Frame frame = new Frame("Simple Control Point");
    frame.setLayout(new BorderLayout());
    frame.addWindowListener(new MyWindowListener());

    Label deviceLabel = new Label("Device:");
    deviceChoice = new Choice();
    deviceChoice.addItemListener(this);
    initDeviceChoice();

    Label serviceLabel = new Label("Service:");
    serviceChoice = new Choice();
    serviceChoice.addItemListener(this);

    Label actionLabel = new Label("Action:");
    actionChoice = new Choice();
    actionChoice.addItemListener(this);

    Label valueLabel = new Label("Wert1:");
    valueText = new TextField();

    Label valueLabel2 = new Label("Wert2:");
    value2Text = new TextField();

    Button button = new Button("Go");
    button.addActionListener(this);

    GridBagLayout gridbag = new GridBagLayout();
    Panel textPanel = new Panel(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    c.weighty = 1.0;
    c.weightx = 1.0;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    gridbag.setConstraints(deviceLabel, c);
    textPanel.add(deviceLabel);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(deviceChoice, c);
    textPanel.add(deviceChoice);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    gridbag.setConstraints(serviceLabel, c);
    textPanel.add(serviceLabel);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(serviceChoice, c);
    textPanel.add(serviceChoice);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    gridbag.setConstraints(actionLabel, c);
    textPanel.add(actionLabel);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(actionChoice, c);
    textPanel.add(actionChoice);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    gridbag.setConstraints(valueLabel, c);
    textPanel.add(valueLabel);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(valueText, c);
    textPanel.add(valueText);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    gridbag.setConstraints(valueLabel2, c);
    textPanel.add(valueLabel2);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(value2Text, c);
    textPanel.add(value2Text);
    mainPanel.add(textPanel, BorderLayout.CENTER);
    mainPanel.add(button, BorderLayout.SOUTH);
    frame.add(mainPanel, BorderLayout.CENTER);
    frame.setSize(300, 180);
    frame.setVisible(true);
  }

  /**
   * Initializes the service dropdown with the services of a given device.
   * 
   * @param device
   *          Description of the Parameter
   */
  private void initServiceChoice(CPDevice device)
  {
    serviceChoice.removeAll();
    serviceChoice.add(SERVICE_DD_TEXT);

    CPService[] services = device.getCPServiceTable();

    if (services != null)
    {
      for (int i = 0; i < services.length; i++)
      {
        serviceChoice.add(services[i].getServiceId());
      }
    }
  }

  /**
   * Stores a device in the control points device list.
   * 
   * @param device
   *          The device to be stored.
   */
  private void storeDevice(CPDevice device)
  {
    try
    {
      String deviceUUID = device.getUDN();

      if (!devices.containsKey(deviceUUID))
      {
        devices.put(deviceUUID, device);
        subscribeEvents(device);
        initDeviceChoice();
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Failed to store device: " + e.getMessage());
    }
  }

  /**
   * Subscribes the controlpoint as a listener for the events of the actions of a given device.
   * 
   * @param device
   *          The device.
   */
  private void subscribeEvents(CPDevice device)
  {
    CPService[] services = device.getCPServiceTable();

    for (int i = 0; i < services.length; i++)
    {
      services[i].sendSubscription();
    }
  }

  /**
   * Starts the control point
   * 
   * 
   * @param args
   *          ignored
   */
  public static void main(String[] args)
  {
    Log4jHelper.initializeLogging();
    new UPnPControlPoint();
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param e
   *          ignored
   */
  public void actionPerformed(java.awt.event.ActionEvent e)
  {
    String deviceS = deviceChoice.getSelectedItem();
    String serviceS = serviceChoice.getSelectedItem();
    String actionS = actionChoice.getSelectedItem();
    String[] valueS = {
        valueText.getText(), value2Text.getText()
    };
    CPDevice device = (CPDevice)devices.get(deviceS);

    if (device == null)
    {
      System.out.println("Device " + deviceS + " not found");

      return;
    }

    CPService service = device.getCPServiceByID(serviceS);

    if (service == null)
    {
      System.out.println("service " + serviceS + " not found");

      return;
    }

    CPAction action = service.getCPAction(actionS);

    if (action == null)
    {
      System.out.println("Action " + actionS + " not found");

      return;
    }

    Argument[] arguments = action.getInArgumentTable();

    try
    {
      if (arguments != null)
      {
        for (int j = 0; j < arguments.length; j++)
        {
          System.out.println("Setting argument " + arguments[j].getName() + " to " + valueS[j]);

          arguments[j].setValueFromString(valueS[j]);
        }
      }

      device.getControlPoint().invokeAction(action);
    } catch (Exception ex)
    {
      System.err.println("invoke action exception " + ex);

      // ex.printStackTrace();
    }

    Argument[] arglist = action.getOutArgumentTable();

    if (arglist == null)
    {
      System.out.println("No values returned");
    } else
    {
      System.out.println("Action returned: ");

      for (int i = 0; i < arglist.length; i++)
      {
        AbstractStateVariable stV = arglist[i].getRelatedStateVariable();
        System.out.println(arglist[i].getName() + "=" + stV.getValue());
      }
    }
  }

  /**
   * Invoked when an item has been selected or deselected. The code written for this method performs
   * the operations that need to occur when an item is selected (or deselected).
   * 
   * @param e
   *          The item event.
   */
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getSource().equals(deviceChoice))
    {
      String deviceName = deviceChoice.getSelectedItem();

      if (!deviceName.equals(DEVICE_DD_TEXT))
      {
        CPDevice device = (CPDevice)devices.get(deviceName);
        initServiceChoice(device);
      } else
      {
        serviceChoice.removeAll();
        actionChoice.removeAll();
      }
    } else if (e.getSource().equals(serviceChoice))
    {
      String serviceName = serviceChoice.getSelectedItem();

      if (!serviceName.equals(SERVICE_DD_TEXT))
      {
        String deviceName = deviceChoice.getSelectedItem();
        CPDevice device = (CPDevice)devices.get(deviceName);
        CPService service = device.getCPServiceByID(serviceName);
        initActionChoice(service);
      } else
      {
        actionChoice.removeAll();
      }
    } else if (e.getSource().equals(actionChoice))
    {
      System.out.println("UPnPControlPoint: itemStateChanged not completely implemented");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void newDevice(CPDevice device)
  {
    System.out.println("New device announced: " + device.getUDN() + "  type: " + device.getDeviceType() + "  URL: " +
      device.getDeviceDescriptionURL());
    storeDevice(device);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.control_point.CPDevice)
   */
  public void deviceGone(CPDevice device)
  {
    System.out.println("Device " + device.getUDN() + " has been removed.");
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

  /**
   * The window listener of the control point.
   * 
   * @author Michael Rother
   * @created 14 June 2002
   */
  private class MyWindowListener extends WindowAdapter
  {
    /**
     * Called when the window is about to be closed.
     * 
     * @param e
     *          ignored
     */
    public void windowClosing(WindowEvent e)
    {
      System.exit(0);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPStateVariableListener#stateVariableChanged(de.fhg.fokus.magic.upnp.CPStateVariable)
   */
  public void stateVariableChanged(CPStateVariable stateVariable)
  {
    System.out.println(" StateVariable " + stateVariable.getName() + " change-event: new value is:" +
      stateVariable.getValue());

  }
}

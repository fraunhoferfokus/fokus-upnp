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

import java.awt.AWTEvent;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core.event.ICPDeviceEventListener;
import de.fraunhofer.fokus.upnp.util.swing.ButtonConstants;

/**
 * This class is the base class for plugins for the UPnP control point.
 * 
 * @author Alexander Koenig
 */
public class BasePlugin extends JPanel implements Runnable, ActionListener, ICPDeviceEventListener, ComponentListener
{

  private static final long serialVersionUID = 1L;

  // UPnP logger
  protected static Logger   logger           = Logger.getLogger("upnp");

  protected boolean         mousePressed     = false;

  protected boolean         mouseInPlugin    = false;

  protected boolean         started          = false;

  protected boolean         debugMode        = false;

  /** Flag that the plugin is currently visible */
  protected boolean         visible          = false;

  /** Frame for display */
  protected JFrame          frame            = null;

  /** Creates a new instance of BasePlugin */
  public BasePlugin()
  {
    setBackground(ButtonConstants.BACKGROUND_COLOR);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    addComponentListener(this);
  }

  /** Checks whether this plugin can be started. */
  public boolean canStartPlugin()
  {
    return frame != null;
  }

  /** Sets the frame associated with this plugin */
  public void setFrame(JFrame frame)
  {
    this.frame = frame;
    if (canStartPlugin() && !started)
    {
      started = true;
      startPlugin();
    }
  }

  /** Retrieves the type of the plugin */
  public String getPluginType()
  {
    return "";
  }

  /** Initializes the content and the layout of the plugin. */
  public void initPluginComponents()
  {

  }

  /** This method starts the plugin as soon as all needed information is available */
  public void startPlugin()
  {
    initPluginComponents();
  }

  /** This method is called each time the plugin is added to a panel. */
  public void pluginShown()
  {
    visible = true;
  }

  /** This method is called each time the plugin is hidden. */
  public void pluginHidden()
  {
    visible = false;
  }

  /** This method is used for evaluating buttons events */
  public void actionPerformed(ActionEvent e)
  {
  }

  /** Processing of mouse events */
  protected void processMouseEvent(MouseEvent e)
  {
    switch (e.getID())
    {
    case MouseEvent.MOUSE_ENTERED:
      mouseInPlugin = true;
      break;
    case MouseEvent.MOUSE_EXITED:
      mouseInPlugin = false;
      break;
    case MouseEvent.MOUSE_PRESSED:
      if (isEnabled())
      {
        mousePressed = true;
        requestFocus();
      }
      break;
    case MouseEvent.MOUSE_RELEASED:
      if (mousePressed && mouseInPlugin)
      {
        actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getPluginType()));
      }
      mousePressed = false;
    }
    super.processMouseEvent(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#userChanged(java.lang.String)
   */
  public void userChanged(String userName)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#newDevice(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void newDevice(CPDevice newDevice)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.event.ICPDeviceEventListener#deviceGone(de.fhg.fokus.magic.upnp.CPDevice)
   */
  public void deviceGone(CPDevice goneDevice)
  {

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

  /** Event that the device visibility in the control point has been changed. */
  public void visibilityChanged()
  {
  }

  /**
   * @return Returns the debugMode.
   */
  public boolean isDebugMode()
  {
    return debugMode;
  }

  /**
   * @param debugMode
   *          The debugMode to set.
   */
  public void setDebugMode(boolean debugMode)
  {
    this.debugMode = debugMode;
  }

  /** Thread for plugin specific algorithms */
  public void run()
  {
  }

  /** Terminates the plugin */
  public void terminate()
  {
  }

  /** Builds a centered layout. The content panel is set in the middle of the plugin. */
  public void buildCenteredLayout(JPanel contentPanel)
  {
    removeAll();
    GridBagConstraints gridBagConstraints;

    // build panels to center information
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel leftFillPanel = new JPanel();
    leftFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(leftFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel rightFillPanel = new JPanel();
    rightFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(rightFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 1;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel topFillPanel = new JPanel();
    topFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(topFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 1;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    JPanel bottomFillPanel = new JPanel();
    bottomFillPanel.setBackground(ButtonConstants.BACKGROUND_COLOR);
    add(bottomFillPanel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(contentPanel, gridBagConstraints);

    repaint();
    validateTree();
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
   */
  public void componentShown(ComponentEvent e)
  {
  }

}

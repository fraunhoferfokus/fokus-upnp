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
package de.fraunhofer.fokus.upnp.core_av.examples.gui_control_point;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.fraunhofer.fokus.upnp.core.Log4jHelper;
import de.fraunhofer.fokus.upnp.core.control_point.CPDevice;
import de.fraunhofer.fokus.upnp.core_av.control_point.AVEntity;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPMediaDeviceEventListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.ICPMediaServerStateVariableListener;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaRendererCPDevice;
import de.fraunhofer.fokus.upnp.core_av.control_point.MediaServerCPDevice;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLContainer;
import de.fraunhofer.fokus.upnp.core_av.didl.DIDLObject;

/**
 * This class can be used to browse the content of media servers.
 * 
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class AVGUIControl implements ICPMediaDeviceEventListener, ICPMediaServerStateVariableListener
{
  private JFrame              topFrame;

  private PanelServer         serverPanel;

  private PanelContainer      containerPanel;

  private PanelItem           itemPanel;

  private JLabel              jPathLabel    = new JLabel(" ");

  private AVEntity            avEntity;

  private Hashtable           renderer      = new Hashtable();

  private MediaServerCPDevice currentServer = null;

  public AVGUIControl()
  {
    initGUI();
    // ImageLoader.loadImages(topFrame);
    avEntity = new AVEntity(null);
    avEntity.createAVControlPoint();
    avEntity.setMediaDeviceListener(this);
    avEntity.setServerChangeListener(this);
  }

  private void initGUI()
  {
    try
    {
      // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e)
    {
    }

    // create the top-level container and add contents to it
    topFrame = new JFrame("Magic User UPnP Media Controller");

    // finish setting up the frame and show it
    topFrame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        // unsubscribeAll();
        System.exit(0);
      }
    });
    serverPanel = new PanelServer(this);
    containerPanel = new PanelContainer(this);
    itemPanel = new PanelItem(this);

    JPanel contentPanel = new JPanel();

    BoxLayout layoutFrame = new BoxLayout(topFrame.getContentPane(), BoxLayout.Y_AXIS);
    topFrame.getContentPane().setLayout(layoutFrame);

    BoxLayout layoutPanel = new BoxLayout(contentPanel, BoxLayout.X_AXIS);
    contentPanel.setLayout(layoutPanel);

    contentPanel.add(containerPanel);
    contentPanel.add(itemPanel);

    topFrame.getContentPane().add(serverPanel);
    topFrame.getContentPane().add(jPathLabel);
    topFrame.getContentPane().add(contentPanel);
    // topFrame.getContentPane().add(itemPanel);
    topFrame.pack();
    topFrame.setVisible(true);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Management //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    Log4jHelper.initializeLogging();
    new AVGUIControl();
  }

  public MediaServerCPDevice getCurrentServer()
  {
    return currentServer;
  }

  public void setCurrentServer(MediaServerCPDevice server)
  {
    this.currentServer = server;
    if (currentServer != null)
    {
      DIDLObject currentObject = currentServer.getCurrentObject();
      if (currentObject instanceof DIDLContainer)
      {
        DIDLContainer currentContainer = (DIDLContainer)currentObject;
        containerPanel.setContainerList(currentContainer.getChildContainerList());
        itemPanel.setItemList(currentContainer.getChildItemList());
      }
    } else
    {
      containerPanel.setContainerList(null);
      itemPanel.setItemList(null);
    }
    updatePath();
  }

  public AVEntity getAVEntity()
  {
    return avEntity;
  }

  private void updatePath()
  {
    String labelText = " ";
    if (currentServer != null)
    {
      String[] path = currentServer.getPathToCurrentObject();
      if (path != null)
      {
        // do not show root container
        for (int i = 1; i < path.length; i++)
        {
          labelText += path[i] + " > ";
        }
      }
      // do not show root container
      if (currentServer.getCurrentObject().getParentContainer() != null)
      {
        labelText += currentServer.getCurrentObject().getTitle();
      }
    }
    jPathLabel.setText(labelText);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // Navigation //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  public void toChild(DIDLContainer container)
  {
    if (currentServer != null)
    {
      currentServer.toChild(container.getID());
      if (currentServer.getCurrentContainer() != null && !currentServer.getCurrentContainer().hasBeenEnumerated())
      {
        currentServer.enumerateCurrentContainer();
      }

      DIDLObject currentObject = currentServer.getCurrentObject();
      if (currentObject instanceof DIDLContainer)
      {
        DIDLContainer currentContainer = (DIDLContainer)currentObject;
        containerPanel.setContainerList(currentContainer.getChildContainerList());
        itemPanel.setItemList(currentContainer.getChildItemList());
      }

      updatePath();
    }
  }

  public void toParent()
  {
    if (currentServer != null)
    {
      currentServer.toParent();
      DIDLObject currentObject = currentServer.getCurrentObject();
      if (currentObject instanceof DIDLContainer)
      {
        DIDLContainer currentContainer = (DIDLContainer)currentObject;
        containerPanel.setContainerList(currentContainer.getChildContainerList());
        itemPanel.setItemList(currentContainer.getChildItemList());
      }
      updatePath();
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // //
  // //
  // ICPMediaDeviceEventListener //
  // //
  // //
  // ////////////////////////////////////////////////////////////////////////////////////////////////
  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.UPnPMediaDeviceListener#newMediaServer(de.fhg.fokus.magic.upnpav.controlpoint.MediaServer)
   */
  public void newMediaServer(MediaServerCPDevice newMediaServer)
  {
    serverPanel.updateServerList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.UPnPMediaDeviceListener#mediaServerGone(java.lang.String)
   */
  public void mediaServerGone(CPDevice goneMediaServer)
  {
    if (currentServer != null && currentServer.getCPDevice() == goneMediaServer)
    {
      setCurrentServer(null);
    }
    serverPanel.updateServerList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.UPnPMediaDeviceListener#newRenderer(de.fhg.fokus.magic.upnpav.controlpoint.Renderer)
   */
  public void newMediaRenderer(MediaRendererCPDevice newRenderer)
  {
    renderer.put(newRenderer.getRendererDevice(), newRenderer);
    itemPanel.newRenderer(newRenderer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.UPnPMediaDeviceListener#rendererGone(java.lang.String)
   */
  public void mediaRendererGone(CPDevice goneRenderer)
  {
    if (renderer.containsKey(goneRenderer))
    {
      renderer.remove(goneRenderer);
      itemPanel.rendererGone(goneRenderer);
    }
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
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationUpdate(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice,
   *      java.lang.String)
   */
  public void containerEnumerationUpdate(MediaServerCPDevice server, String containerID)
  {
    if (currentServer != null && currentServer.getCurrentContainer() != null &&
      currentServer.getCurrentContainer().getID().equals(containerID))
    {
      containerPanel.setContainerList(currentServer.getCurrentContainer().getChildContainerList());
      itemPanel.setItemList(currentServer.getCurrentContainer().getChildItemList());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerUpdateOccurred(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice,
   *      java.lang.String)
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnpav.controlpoint.ICPMediaServerStateVariableListener#containerEnumerationFinished(de.fhg.fokus.magic.upnpav.controlpoint.MediaServerCPDevice,
   *      java.lang.String)
   */
  public void containerEnumerationFinished(MediaServerCPDevice server, String containerID)
  {
  }

}

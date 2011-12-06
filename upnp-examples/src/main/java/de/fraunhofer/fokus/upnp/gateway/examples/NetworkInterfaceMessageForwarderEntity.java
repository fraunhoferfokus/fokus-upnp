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
package de.fraunhofer.fokus.upnp.gateway.examples;

import de.fraunhofer.fokus.upnp.core.templates.TemplateEntity;
import de.fraunhofer.fokus.upnp.core.xml.UPnPStartupConfiguration;
import de.fraunhofer.fokus.upnp.gateway.common.message_forwarder.MessageForwarder;
import de.fraunhofer.fokus.upnp.gateway.network_interfaces.InetAddressManagement;
import de.fraunhofer.fokus.upnp.util.threads.IKeyListener;

/** This class works as UPnP message forwarder between all local network interfaces */
public class NetworkInterfaceMessageForwarderEntity extends TemplateEntity implements IKeyListener
{

  private MessageForwarder      messageForwarder;

  private InetAddressManagement inetAddressManagement;

  /**
   * Creates a new instance of NetworkInterfaceMessageForwarderEntity.
   * 
   * 
   */
  public NetworkInterfaceMessageForwarderEntity()
  {
    this(null);
  }

  /**
   * Creates a new instance of NetworkInterfaceMessageForwarderEntity.
   * 
   * 
   */
  public NetworkInterfaceMessageForwarderEntity(UPnPStartupConfiguration startupConfiguration)
  {
    super(startupConfiguration);
    messageForwarder = new MessageForwarder(getStartupConfiguration());
    inetAddressManagement = new InetAddressManagement(messageForwarder);
    if (getKeyboardThread() != null)
    {
      getKeyboardThread().setKeyListener(this);
    }

    System.out.println();
    System.out.println("  Type <s> to trigger a new device search");
    System.out.println();
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[])
  {
    new NetworkInterfaceMessageForwarderEntity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fraunhofer.fokus.upnp.util.threads.IKeyListener#keyEvent(int)
   */
  public void keyEvent(int code)
  {
    if (code == 's')
    {
      System.out.println("Trigger root device search");
      messageForwarder.getTemplateControlPoint().searchRootDevices();
    }

  }

  /** Terminates the message forwarding */
  public void terminate()
  {
    inetAddressManagement.terminate();
    messageForwarder.terminate();

    super.terminate();
  }

}

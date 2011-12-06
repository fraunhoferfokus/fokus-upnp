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
package de.fraunhofer.fokus.lsf.gateway.common;

import de.fraunhofer.fokus.lsf.core.BinaryUPnPConstants;
import de.fraunhofer.fokus.lsf.gateway.common.forwarder_module.ILSFForwarderModuleEventListener;
import de.fraunhofer.fokus.lsf.gateway.common.message_forwarder.LSFMessageForwarder;

/**
 * This is the base class for all forwarder module managers. This class is used to initialize socket structures for a
 * physical or virtual forwarder module and to register the module in the central message forwarder.
 * 
 * @author Alexander Koenig
 */
public abstract class AbstractLSFAddressManagement
{

  /** Reference to central message forwarder */
  protected LSFMessageForwarder              messageForwarder;

  /** Used address for discovery and events */
  protected String                           multicastAddress;

  /** Port used for discovery */
  protected int                              discoveryPort;

  /** Port used for events */
  protected int                              eventPort;

  /** Optional listener for client events */
  protected ILSFForwarderModuleEventListener forwarderModuleEventListener;

  /**
   * Creates a new instance of AbstractLSFAddressManagement.
   * 
   * @param messageForwarder
   */
  public AbstractLSFAddressManagement(LSFMessageForwarder messageForwarder)
  {
    this.messageForwarder = messageForwarder;
    // start with default values
    multicastAddress = BinaryUPnPConstants.BinaryUPnPMulticastAddress;
    discoveryPort = BinaryUPnPConstants.DiscoveryMulticastPort;
    eventPort = BinaryUPnPConstants.EventMulticastPort;

    if (messageForwarder.getStartupConfiguration().hasCustomNetworkProperties())
    {
      multicastAddress = messageForwarder.getStartupConfiguration().getMulticastAddress();
      discoveryPort = messageForwarder.getStartupConfiguration().getDiscoveryMulticastPort();
      eventPort = messageForwarder.getStartupConfiguration().getEventMulticastPort();
    }
  }

  /**
   * Retrieves the value of forwarderModuleEventListener.
   * 
   * @return The value of forwarderModuleEventListener
   */
  public ILSFForwarderModuleEventListener getForwarderModuleEventListener()
  {
    return forwarderModuleEventListener;
  }

  /**
   * Sets the new value for forwarderModuleEventListener.
   * 
   * @param forwarderModuleEventListener
   *          The new value for forwarderModuleEventListener
   */
  public void setForwarderModuleEventListener(ILSFForwarderModuleEventListener forwarderModuleEventListener)
  {
    this.forwarderModuleEventListener = forwarderModuleEventListener;
  }

  /** Prints statistics about the state to the console. */
  public abstract void printDebugStats();

  /** Terminates the management. */
  public abstract void terminate();

}

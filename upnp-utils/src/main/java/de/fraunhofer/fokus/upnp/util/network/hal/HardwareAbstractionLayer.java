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
package de.fraunhofer.fokus.upnp.util.network.hal;

import de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatus;
import de.fraunhofer.fokus.upnp.util.network.listener.IPhyStatusListener;
import de.fraunhofer.fokus.upnp.util.network.listener.IRawDataReceiveListener;
import de.fraunhofer.fokus.upnp.util.network.listener.IRawDataReceiver;
import de.fraunhofer.fokus.upnp.util.network.listener.IRawDataSender;

/**
 * This class is the base class for all hardware abstraction layers.
 * 
 * @author Alexander Koenig
 */
public abstract class HardwareAbstractionLayer implements IPhyStatus, IRawDataSender, IRawDataReceiver
{

  /** Listener for received raw data */
  protected IRawDataReceiveListener rawDataReceiveListener;

  /** Listener for phy events */
  protected IPhyStatusListener      phyStatusListener;

  /**
   * Retrieves the value of rawDataReceiveListener.
   * 
   * @return The value of rawDataReceiveListener
   */
  public IRawDataReceiveListener getRawDataReceiveListener()
  {
    return rawDataReceiveListener;
  }

  /**
   * Sets the new value for rawDataReceiveListener.
   * 
   * @param rawDataReceiveListener
   *          The new value for rawDataReceiveListener
   */
  public void setRawDataReceiveListener(IRawDataReceiveListener rawDataReceiveListener)
  {
    this.rawDataReceiveListener = rawDataReceiveListener;
  }

  /**
   * Retrieves the value of phyStatusListener.
   * 
   * @return The value of phyStatusListener
   */
  public IPhyStatusListener getPhyStatusListener()
  {
    return phyStatusListener;
  }

  /**
   * Sets the new value for phyStatusListener.
   * 
   * @param phyStatusListener
   *          The new value for phyStatusListener
   */
  public void setPhyStatusListener(IPhyStatusListener phyStatusListener)
  {
    this.phyStatusListener = phyStatusListener;
  }

  /** Terminates the HAL. */
  public abstract void terminate();

}

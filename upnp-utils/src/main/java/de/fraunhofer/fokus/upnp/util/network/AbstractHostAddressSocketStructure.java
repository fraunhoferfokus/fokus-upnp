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
package de.fraunhofer.fokus.upnp.util.network;

import java.net.InetAddress;

/**
 * This is the base class for all socket structures. A socket structure combines sockets for a physical or virtual
 * network interface which are needed to achieve a certain functionality. The socket structure is identified by a host
 * address which must be unique for this host PC.
 * 
 * @author Alexander Koenig
 */
public abstract class AbstractHostAddressSocketStructure
{

  /** Flag that the structure is valid */
  protected boolean     valid       = true;

  /** Associated host address */
  protected InetAddress hostAddress = null;

  /**
   * Creates a new instance of AbstractHostAddressSocketStructure.
   * 
   * @param hostAddress
   */
  public AbstractHostAddressSocketStructure(InetAddress hostAddress)
  {
    this.hostAddress = hostAddress;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof AbstractHostAddressSocketStructure)
    {
      return hostAddress.equals(((AbstractHostAddressSocketStructure)obj).getHostAddress());
    }
    return super.equals(obj);
  }

  /** Prints a list with the used ports to the console. */
  public abstract void printUsedPorts();

  /**
   * Retrieves the value of hostAddress.
   * 
   * @return The value of hostAddress
   */
  public InetAddress getHostAddress()
  {
    return hostAddress;
  }

  /**
   * Retrieves the value of valid.
   * 
   * @return The value of valid
   */
  public boolean isValid()
  {
    return valid;
  }

  /** Terminates the socket structure */
  public abstract void terminate();

}

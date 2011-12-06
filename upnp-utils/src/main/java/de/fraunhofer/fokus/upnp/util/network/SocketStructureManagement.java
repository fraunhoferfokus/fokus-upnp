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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.Portable;

/**
 * This class manages socket structures for different host addresses.
 * 
 * @author Alexander Koenig
 */
public abstract class SocketStructureManagement
{

  /** Table of socket structures for all external IP addresses */
  protected Hashtable socketStructureFromHostAddressTable = new Hashtable();

  /** List with socket structures for all external IP addresses */
  protected Vector    socketStructureList                 = new Vector();

  /** List with host address strings that should only be used */
  protected Vector    preferredHostAddressList;

  /** List with host address strings that should be ignored */
  protected Vector    ignoredHostAddressList;

  /**
   * Creates a new instance of SocketStructureManagement.
   * 
   * @param preferredHostAddressList
   * @param ignoredHostAddressList
   */
  public SocketStructureManagement(Vector preferredHostAddressList, Vector ignoredHostAddressList)
  {
    this.preferredHostAddressList = preferredHostAddressList;
    if (this.preferredHostAddressList == null)
    {
      this.preferredHostAddressList = new Vector();
    }
    this.ignoredHostAddressList = ignoredHostAddressList;
    if (this.ignoredHostAddressList == null)
    {
      this.ignoredHostAddressList = new Vector();
    }
  }

  /** Creates a new host address socket structure for a certain host address. */
  protected abstract AbstractHostAddressSocketStructure createHostAddressSocketStructure(InetAddress hostAddress);

  /**
   * Initializes all available socket structures.
   */
  public void initHostAddressSocketStructures()
  {
    // use only preferred host addresses if available
    if (preferredHostAddressList.size() > 0)
    {
      for (int i = 0; i < preferredHostAddressList.size(); i++)
      {
        try
        {
          InetAddress currentAddress = InetAddress.getByName((String)preferredHostAddressList.elementAt(i));
          tryAddHostAddressSocketStructure(currentAddress);
        } catch (UnknownHostException e)
        {
        }
      }
      return;
    }
    // use all available host addresses if not in ignore list
    // start independent sockets and servers for all external network addresses
    Vector networkInterfaces = IPHelper.getSocketStructureNetworkInterfaces();
    // System.out.println("Found " + networkInterfaces.size() + " network interface(s)");
    for (int i = 0; i < networkInterfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
      Vector localHostAddresses = IPHelper.getIPv4InetAddresses(currentInterface);

      for (int j = 0; j < localHostAddresses.size(); j++)
      {
        InetAddress currentAddress = (InetAddress)localHostAddresses.elementAt(j);
        tryAddHostAddressSocketStructure(currentAddress);
      }
    }
  }

  /**
   * Tries to add a socket structure for a certain host address.
   * 
   * @param The
   *          new host address
   * @return The created socket structure
   */
  public AbstractHostAddressSocketStructure tryAddHostAddressSocketStructure(InetAddress hostAddress)
  {
    if (hostAddress instanceof Inet6Address)
    {
      Portable.println(toString() + ": Host address " + hostAddress.getHostAddress() + " is IPv6");
      return null;
    }
    if (socketStructureFromHostAddressTable.containsKey(hostAddress))
    {
      Portable.println(toString() + ": Host address " + hostAddress.getHostAddress() + " is already in use");
      return null;
    }
    if (preferredHostAddressList.size() > 0 && !preferredHostAddressList.contains(hostAddress.getHostAddress()))
    {
      Portable.println(toString() + ": Host address " + hostAddress.getHostAddress() + " is not in preferred list");
      return null;
    }
    if (ignoredHostAddressList.contains(hostAddress.getHostAddress()))
    {
      Portable.println(toString() + ": Host address " + hostAddress.getHostAddress() + " is in ignore list");
      return null;
    }
    // ignore loopback address
    if (hostAddress.getHostAddress().equals("127.0.0.1"))
    {
      // Portable.println(toString() + ": Host address is loopback address");
      return null;
    }
    AbstractHostAddressSocketStructure hostAddressSocketStructure = createHostAddressSocketStructure(hostAddress);

    // add to hashtable if valid
    if (hostAddressSocketStructure != null)
    {
      socketStructureFromHostAddressTable.put(hostAddress, hostAddressSocketStructure);
      socketStructureList.add(hostAddressSocketStructure);
    }
    return hostAddressSocketStructure;
  }

  /** Retrieves the number of external host address socket structures */
  public int getSocketStructureCount()
  {
    return socketStructureList.size();
  }

  /** Retrieves a list with all socket structures */
  public Vector getSocketStructures()
  {
    return socketStructureList;
  }

  /** Retrieves a certain socket structure */
  public AbstractHostAddressSocketStructure getSocketStructure(int index)
  {
    if (index >= 0 && index < socketStructureList.size())
    {
      return (AbstractHostAddressSocketStructure)socketStructureList.elementAt(index);
    }
    return null;
  }

  /** Retrieves a specific host address socket structure */
  public AbstractHostAddressSocketStructure getSocketStructure(InetAddress hostAddress)
  {
    if (hostAddress == null)
    {
      if (socketStructureList.size() > 0)
      {
        return getSocketStructure(0);
      }
      return null;
    }
    Object result = socketStructureFromHostAddressTable.get(hostAddress);
    if (result != null)
    {
      return (AbstractHostAddressSocketStructure)result;
    }

    return null;
  }

  /** Retrieves the preferred host address socket structure */
  public AbstractHostAddressSocketStructure getPreferredSocketStructure()
  {
    Object result = socketStructureFromHostAddressTable.get(IPHelper.getLocalHostAddress());
    if (result != null)
    {
      return (AbstractHostAddressSocketStructure)result;
    }

    return null;
  }

  public String toString()
  {
    return "SocketStructureManagement";
  }

  /** Terminates the manager */
  public void terminate()
  {
    for (int i = 0; i < socketStructureList.size(); i++)
    {
      ((AbstractHostAddressSocketStructure)socketStructureList.elementAt(i)).terminate();
    }
    socketStructureList.clear();
    socketStructureFromHostAddressTable.clear();
  }

}

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
import java.net.NetworkInterface;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class is responsible for managing available network interfaces.
 * 
 * @author Alexander Koenig
 */
public class NetworkInterfaceManager implements IEventListener
{

  /** List of registered listeners */
  private Vector  listeners            = new Vector();

  /** List of known network interfaces */
  private Vector  networkInterfaceList = new Vector();

  /** Timeout for test */
  private int     checkInterval        = 10000;

  /** Last check time */
  private long    nextCheckTime        = 0;

  private boolean firstCheck           = true;

  /**
   * Creates a new instance of NetworkInterfaceManager.
   * 
   */
  public NetworkInterfaceManager()
  {
  }

  public void addListener(INetworkInterfaceChangeListener listener)
  {
    if (!listeners.contains(listener))
    {
      listeners.add(listener);
    }
  }

  public void removeListener(INetworkInterfaceChangeListener listener)
  {
    listeners.remove(listener);
  }

  /** This method handles new addresses */
  private void newInetAddress(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    if (firstCheck)
    {
      return;
    }

    // System.out.println(" " + getName() + ": Found new inet address: " +
    // inetAddress.getHostAddress());
    if (!inetAddress.getHostAddress().equals("127.0.0.1") && !inetAddress.getHostAddress().equals("0.0.0.0"))
    {
      for (int j = 0; j < listeners.size(); j++)
      {
        ((INetworkInterfaceChangeListener)listeners.elementAt(j)).newInetAddress(networkInterface, inetAddress);
      }
    }
  }

  /** This method handles removed addresses */
  private void inetAddressGone(NetworkInterface networkInterface, InetAddress inetAddress)
  {
    if (firstCheck)
    {
      return;
    }

    if (!inetAddress.getHostAddress().equals("127.0.0.1") && !inetAddress.getHostAddress().equals("0.0.0.0"))
    {
      // System.out.println(" " + getName() + ": Inet address gone: " + inetAddress);

      for (int j = 0; j < listeners.size(); j++)
      {
        ((INetworkInterfaceChangeListener)listeners.elementAt(j)).inetAddressGone(networkInterface, inetAddress);
      }
    }
  }

  /** Compares two lists of inet addresses and generates the appropriate events. */
  private void compareInetAddressLists(NetworkInterface newNetworkInterface,
    Vector newInetAddressList,
    NetworkInterface oldNetworkInterface,
    Vector oldInetAddressList)
  {
    // enumerate all new inet addresses
    for (int k = 0; k < newInetAddressList.size(); k++)
    {
      InetAddress currentNewInetAddress = (InetAddress)newInetAddressList.elementAt(k);
      boolean foundAddress = false;
      // remove unchanged addresses from old list
      for (int l = 0; l < oldInetAddressList.size() && !foundAddress; l++)
      {
        InetAddress currentOldInetAddress = (InetAddress)oldInetAddressList.elementAt(l);
        if (currentNewInetAddress.equals(currentOldInetAddress))
        {
          foundAddress = true;
          oldInetAddressList.remove(l);
        }
      }
      // inform listeners about new address
      if (!foundAddress)
      {
        newInetAddress(newNetworkInterface, currentNewInetAddress);
      }
    }
    // inform listeners about all removed addresses
    for (int k = 0; k < oldInetAddressList.size(); k++)
    {
      InetAddress currentOldInetAddress = (InetAddress)oldInetAddressList.elementAt(k);
      inetAddressGone(oldNetworkInterface, currentOldInetAddress);
    }
  }

  public void triggerEvents()
  {
    if (System.currentTimeMillis() > nextCheckTime)
    {
      nextCheckTime = System.currentTimeMillis() + checkInterval;

      Vector newNetworkInterfaceList = IPHelper.getSocketStructureNetworkInterfaces();
      // enumerate all new interfaces
      for (int i = 0; i < newNetworkInterfaceList.size(); i++)
      {
        NetworkInterface currentNewNetworkInterface = (NetworkInterface)newNetworkInterfaceList.elementAt(i);
        boolean foundInterface = false;
        // search in list of old interfaces
        for (int j = 0; j < networkInterfaceList.size() && !foundInterface; j++)
        {
          NetworkInterface currentOldNetworkInterface = (NetworkInterface)networkInterfaceList.elementAt(j);
          // network interface is known, compare IP addresses
          if (currentNewNetworkInterface.getName().equals(currentOldNetworkInterface.getName()))
          {
            foundInterface = true;
            Vector newInetAddressList = IPHelper.getIPv4InetAddresses(currentNewNetworkInterface);
            Vector oldInetAddressList = IPHelper.getIPv4InetAddresses(currentOldNetworkInterface);

            compareInetAddressLists(currentNewNetworkInterface,
              newInetAddressList,
              currentOldNetworkInterface,
              oldInetAddressList);

            // remove interface from old list
            networkInterfaceList.remove(j);
          }
        }
        // new interface not found in old interface list
        if (!foundInterface)
        {
          Vector newInetAddressList = IPHelper.getIPv4InetAddresses(currentNewNetworkInterface);

          // send new event for all inet addresses
          for (int j = 0; j < newInetAddressList.size(); j++)
          {
            InetAddress currentAddress = (InetAddress)newInetAddressList.elementAt(j);
            newInetAddress(currentNewNetworkInterface, currentAddress);
          }
        }
      }
      // all interfaces left in old list have been removed
      for (int i = 0; i < networkInterfaceList.size(); i++)
      {
        NetworkInterface currentOldNetworkInterface = (NetworkInterface)networkInterfaceList.elementAt(i);

        Vector oldInetAddressList = IPHelper.getIPv4InetAddresses(currentOldNetworkInterface);

        // send gone event for all old inet addresses
        for (int j = 0; j < oldInetAddressList.size(); j++)
        {
          InetAddress currentAddress = (InetAddress)oldInetAddressList.elementAt(j);
          inetAddressGone(currentOldNetworkInterface, currentAddress);
        }
      }
      networkInterfaceList.clear();
      // store new list
      networkInterfaceList = newNetworkInterfaceList;

      // prevent initial events by setting this after the first check
      firstCheck = false;
    }
  }

}

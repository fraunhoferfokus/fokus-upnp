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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.StringHelper;

/**
 * This class provides methods to handle available network interfaces and IP addresses.
 * 
 * @author icu, Alexander Koenig
 * 
 */
public class IPHelper
{

  // private static Logger logger = Logger.getLogger("upnp");
  private static final int   v4                                  = 4;

  private static final int   v6                                  = 6;

  private static String      HostName                            = null;

  private static InetAddress HostAddress                         = null;

  /** Not supported on jamvm for openWRT routers */
  public static boolean      SupportsNetworkInterfaceEnumeration = true;

  /** Shows a list of all inet addresses found on this computer. */
  public static void printInetAddresses()
  {
    Vector networkInterfaces = IPHelper.getSocketStructureNetworkInterfaces();
    for (int i = 0; i < networkInterfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
      Enumeration addresses = currentInterface.getInetAddresses();
      while (addresses.hasMoreElements())
      {
        InetAddress currentAddress = (InetAddress)addresses.nextElement();
        if (currentAddress instanceof Inet4Address)
        {
          System.out.println("IPv4: " + currentInterface.getName() + " -> " + currentAddress.getHostAddress());
        }
        if (currentAddress instanceof Inet6Address)
        {
          System.out.println("IPv6: " + currentInterface.getName() + " -> " + currentAddress.getHostAddress());
        }
      }
    }
  }

  /**
   * Retrieves a list of all network interfaces that can be used for socket structures.
   * 
   * @return A vector with all found network interfaces
   */
  public static Vector getSocketStructureNetworkInterfaces()
  {
    if (!SupportsNetworkInterfaceEnumeration)
    {
      return new Vector();
    }
    Vector result = new Vector();
    try
    {
      Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();

      while (networkInterfaces.hasMoreElements())
      {
        NetworkInterface currentNetworkInterface = (NetworkInterface)networkInterfaces.nextElement();
        // do not use ppp connections
        if (!currentNetworkInterface.getName().startsWith("ppp"))
        {
          result.add(currentNetworkInterface);
        }
      }
    } catch (SocketException e1)
    {
      SupportsNetworkInterfaceEnumeration = false;
      Portable.println("Enumeration of network interfaces not supported");
    }
    // System.out.println("Found " + result.size() + " valid network interfaces");
    return result;
  }

  /**
   * Retrieves a list of all IPv4 addresses assigned to one interface.
   * 
   * @param networkInterface
   *          The requested interface
   * 
   * @return A vector with all IP addresses associated with this interface
   */
  public static Vector getIPv4InetAddresses(NetworkInterface networkInterface)
  {
    Enumeration e;
    Vector result = new Vector();

    e = networkInterface.getInetAddresses();

    while (e.hasMoreElements())
    {
      InetAddress currentAddress = (InetAddress)e.nextElement();
      if (currentAddress instanceof Inet4Address)
      {
        result.add(currentAddress);
      }
    }
    return result;
  }

  /**
   * Retrieves a list of all IPv6 addresses assigned to one interface.
   * 
   * @param networkInterface
   *          The requested interface
   * 
   * @return A vector with all IP addresses associated with this interface
   */
  public static Vector getIPv6InetAddresses(NetworkInterface networkInterface)
  {
    Enumeration e;
    Vector result = new Vector();

    e = networkInterface.getInetAddresses();

    while (e.hasMoreElements())
    {
      InetAddress currentAddress = (InetAddress)e.nextElement();
      if (currentAddress instanceof Inet6Address)
      {
        result.add(currentAddress);
      }
    }
    return result;
  }

  /** Retrieves a list of all external IPv4 addresses, not including 127.0.0.1 */
  public static Vector getIPv4LocalHostAddresses()
  {
    Vector result = new Vector();

    Vector interfaces = getSocketStructureNetworkInterfaces();
    for (int i = 0; i < interfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)interfaces.elementAt(i);
      Vector addresses = getIPv4InetAddresses(currentInterface);
      // System.out.println("Found " + addresses.size() + " IP addresses for " +
      // currentInterface.getName());
      for (int j = 0; j < addresses.size(); j++)
      {
        InetAddress currentAddress = (InetAddress)addresses.elementAt(j);
        if (!currentAddress.getHostAddress().equals("127.0.0.1"))
        {
          result.add(currentAddress);
        }
      }
    }
    return result;
  }

  /** Retrieves the most appropriate local host address as string (e.g., 192.168.1.3) */
  public static String getLocalHostAddressString()
  {
    InetAddress localHost = getLocalHostAddress();
    if (localHost != null)
    {
      return localHost.getHostAddress();
    }

    return "127.0.0.1";
  }

  /** Retrieves the local host address. This method tries to find the most appropriate host address. */
  public static InetAddress getLocalHostAddress()
  {
    if (HostAddress != null)
    {
      return HostAddress;
    }
    InetAddress result = null;
    InetAddress localHost = null;
    try
    {
      localHost = InetAddress.getLocalHost();
    } catch (Exception e)
    {
      Portable.println("getLocalHost not supported: " + e.getMessage());
    }
    Vector networkInterfaces = getSocketStructureNetworkInterfaces();
    // try to find wired interface (ethx)
    for (int i = 0; result == null && i < networkInterfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
      if (currentInterface.getName().startsWith("eth"))
      {
        Vector addresses = getIPv4InetAddresses(currentInterface);
        // choose first address for wired interface
        if (addresses.size() > 0)
        {
          result = (InetAddress)addresses.elementAt(0);
        }
      }
    }
    // no wired interface found, use any other non-local interface
    if (result == null)
    {
      System.out.println("No wired interface found, search wireless interface");
      for (int i = 0; result == null && i < networkInterfaces.size(); i++)
      {
        NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
        // ignore loopback and ppp Interfaces
        if (!currentInterface.getName().equals("lo") && !currentInterface.getName().startsWith("ppp"))
        {
          Vector addresses = getIPv4InetAddresses(currentInterface);
          if (addresses.size() > 0)
          {
            result = (InetAddress)addresses.elementAt(0);
          }
        }
      }
    }
    // no external interface found, use loopback interface
    if (result == null)
    {
      System.out.println("Try to fall back to loopback interface");
      for (int i = 0; result == null && i < networkInterfaces.size(); i++)
      {
        NetworkInterface currentInterface = (NetworkInterface)networkInterfaces.elementAt(i);
        Vector addresses = getIPv4InetAddresses(currentInterface);
        for (int j = 0; j < addresses.size(); j++)
        {
          InetAddress currentAddress = (InetAddress)addresses.elementAt(j);

          if (currentAddress.getHostAddress().equals("127.0.0.1"))
          {
            result = currentAddress;
          }
        }
      }
    }
    // return resolved local host if same address as most appropriate result
    if (localHost != null && result != null && localHost.getHostAddress().equals(result.getHostAddress()))
    {
      return localHost;
    }
    // use 127.0.0.1 if all else fails
    if (result == null)
    {
      Portable.println("Could not retrieve a valid IP address, fall back to 127.0.0.1");
      try
      {
        result = InetAddress.getByName("127.0.0.1");
      } catch (UnknownHostException e)
      {
      }
    }
    HostAddress = result;

    return HostAddress;
  }

  /** Retrieves the local host name (e.g., silicium) */
  public static String getLocalHostName()
  {
    if (HostName != null)
    {
      return HostName;
    }

    // first try local host
    try
    {
      InetAddress localHost = InetAddress.getLocalHost();
      if (localHost != null && !isIPAddress(localHost.getHostName()))
      {
        HostName = localHost.getHostName().toLowerCase();

        // System.out.println("Host name is " + HostName);
        return HostName;
      }
    } catch (Exception e)
    {
    }

    // search the first address that is not parseable as number after removing the .
    Vector hostAddresses = getIPv4LocalHostAddresses();
    for (int i = 0; i < hostAddresses.size(); i++)
    {
      InetAddress currentAddress = (InetAddress)hostAddresses.elementAt(i);
      if (!isIPAddress(currentAddress.getCanonicalHostName()))
      {
        HostName = currentAddress.getCanonicalHostName().toLowerCase();

        // System.out.println("Host name is " + HostName);
        return HostName;
      }
      if (!isIPAddress(currentAddress.getHostName()))
      {
        HostName = currentAddress.getHostName().toLowerCase();

        // System.out.println("Host name is " + HostName);
        return HostName;
      }
    }
    // no name found
    InetAddress localHost = getLocalHostAddress();
    if (localHost != null)
    {
      return localHost.getCanonicalHostName().toLowerCase();
    }
    return "localhost";
  }

  /** Retrieves the short local host name (e.g., silicium) */
  public static String getSimpleLocalHostName()
  {
    String fullHostName = getLocalHostName();

    // do not shorten IP addresses like 192.168....
    if (isIPAddress(fullHostName))
    {
      return fullHostName;
    }

    int pointIndex = fullHostName.indexOf(".");
    if (pointIndex != -1)
    {
      fullHostName = fullHostName.substring(0, pointIndex);
    }

    return fullHostName;
  }

  /** Retrieves the local host address for a specific IP version */
  /*
   * public static String getLocalHostAddress(int ipVersion) throws Exception {
   * 
   * InetAddress result = getLocalHost(ipVersion);
   * 
   * if (result != null) return result.getHostAddress();
   *  // logger.error("LocalHostAddress not found"); System.exit(-1);
   * 
   * return null; }
   */

  /** Retrieves the local host address for a specific IP version */
  /*
   * public static InetAddress getLocalHost(int ipVersion) throws Exception { Enumeration e =
   * NetworkInterface.getNetworkInterfaces(); String local4Addr =
   * System.getProperty("PreferLocalInet4Address","default"); String local6Addr =
   * System.getProperty("PreferLocalInet6Address","default");
   * 
   * while (e.hasMoreElements()) { NetworkInterface ni = (NetworkInterface) e.nextElement();
   * Enumeration en = ni.getInetAddresses();
   * 
   * while (en.hasMoreElements()) { InetAddress ia = (InetAddress) en.nextElement();
   * 
   * if (ipVersion == v6) { //if (ia instanceof Inet6Address &&
   * (!ia.getHostAddress().equals("::1"))){ String local = System.getProperty("IPv6AddressType",
   * "site");
   * 
   * if (local.equalsIgnoreCase("link") && ia.getHostAddress().startsWith("fe8")) { if
   * (local6Addr.equalsIgnoreCase("default")) { return ia; } else { if
   * (ia.getHostAddress().equalsIgnoreCase(local6Addr)) { return ia; } } }
   * 
   * if (local.equalsIgnoreCase("site") && ia.getHostAddress().startsWith("fec")) { if
   * (local6Addr.equalsIgnoreCase("default")) { return ia; } else { if
   * (ia.getHostAddress().equalsIgnoreCase(local6Addr)) { return ia; } } }
   * 
   * if (local.equalsIgnoreCase("global") && ia.getHostAddress().startsWith("3")) { if
   * (local6Addr.equalsIgnoreCase("default")) { return ia; } else { if
   * (ia.getHostAddress().equalsIgnoreCase(local6Addr)) { return ia; } } } }
   * 
   * if (ipVersion == v4) { if (ia instanceof Inet4Address &&
   * !ia.getHostAddress().equals("127.0.0.1")) { if (local4Addr.equalsIgnoreCase("default")) {
   * return ia; } else { if (ia.getHostAddress().equalsIgnoreCase(local4Addr)) { return ia; } } } } } }
   *  // logger.error("LocalHostAddress not found"); System.exit(-1);
   * 
   * return null; }
   */

  /** Test a host address for its IP version */
  public static boolean checkHostAddress(int IPVersion, String hostAddress)
  {
    if (IPVersion == v6 && hostAddress.indexOf(":") == -1)
    {
      return false;
    }

    if (IPVersion == v4 && hostAddress.indexOf(":") != -1)
    {
      return false;
    }
    return true;
  }

  /** Checks if the string contains an IP address */
  public static boolean isIPAddress(String address)
  {
    // check if this is an IP address
    try
    {
      Long.parseLong(address.replace('.', '0'));
    } catch (Exception e)
    {
      return false;
    }
    return true;
  }

  /** Retrieves a string describing the address (address:port) */
  public static String toString(InetSocketAddress address)
  {
    if (address == null)
    {
      return null;
    }
    if (address.getAddress() == null)
    {
      return "Not resolved";
    }
    return address.getAddress().getHostAddress() + ":" + address.getPort();
  }

  /** Retrieves a string describing a byte address. */
  public static String toString(byte[] address)
  {
    String result = "";
    if (address == null)
    {
      return result;
    }

    if (address.length == 4)
    {
      for (int i = 0; i < address.length; i++)
      {
        result += (i > 0 ? "." : "") + (address[i] & 0xFF);
      }
    } else
    {
      result += StringHelper.byteArrayToMACString(address);
    }
    return result;
  }

  /** Creates an inet address from a byte array */
  public static InetAddress toInetAddress(byte[] address)
  {
    if (address != null && address.length > 0 && address.length <= 4)
    {
      byte[] temp = new byte[] {
          0, 0, 0, 0
      };
      // copy address to end of temp address
      Portable.arraycopy(address, 0, temp, 4 - address.length, address.length);
      try
      {
        return InetAddress.getByAddress(temp);
      } catch (UnknownHostException e)
      {
      }
    }
    return null;
  }

  /** Creates a socket address from an URL */
  public static InetSocketAddress toSocketAddress(URL url)
  {
    return new InetSocketAddress(url.getHost(), url.getPort());
  }

  /** Creates a socket address from a string (address:port) */
  public static InetSocketAddress toSocketAddress(String address)
  {
    if (address == null)
    {
      return null;
    }
    int colonIndex = address.indexOf(":");
    if (colonIndex != -1)
    {
      return new InetSocketAddress(address.substring(0, colonIndex),
        Integer.parseInt(address.substring(colonIndex + 1)));
    }
    return null;
  }

  /** Checks if two IP addresses belong to the same subnet */
  public static boolean isCommonSubnet(InetAddress address1, InetAddress address2)
  {
    // check source of request message
    byte[] address1data = address1.getAddress();
    byte[] address2data = address2.getAddress();

    // check if request is from local network
    return address1data[0] == address2data[0] && address1data[1] == address2data[1] &&
      address1data[2] == address2data[2];
  }

  /** Checks if an IP address is local (e.g., 10.147.x.y or 192.168.x.y) */
  public static boolean isLocalAddress(InetAddress address)
  {
    // check source of request message
    byte[] tempData = address.getAddress();
    int[] addressData = new int[4];
    for (int i = 0; i < 4; i++)
    {
      addressData[i] = tempData[i] & 0xFF;
    }

    return addressData[0] == 10 || addressData[0] == 172 && (addressData[1] & 0x10) == 0x10 || addressData[0] == 192 &&
      addressData[1] == 168 || addressData[0] == 169 && addressData[1] == 254;
  }

  /**
   * Tries to find the most appropriate file suffix based on the current host name.
   * 
   * @return The most appropriate file suffix, e.g., _adelphi or _192.168.1.10
   */
  public static String getFileSuffixFromHostName()
  {
    String localHostName = IPHelper.getLocalHostName().toLowerCase();

    // check if this is an IP address
    boolean isIPAddress = true;
    StringTokenizer stringTokenizer = new StringTokenizer(localHostName, ".");
    while (stringTokenizer.hasMoreElements())
    {
      try
      {
        Integer.parseInt(stringTokenizer.nextToken());
      } catch (Exception e)
      {
        isIPAddress = false;
      }
    }

    // return IP address if no host name is found
    if (isIPAddress)
    {
      return "_" + localHostName;
    }

    // return simple host name if this is a fully qualified name (e.g, adelphi.fokus.fraunhofer.de)
    if (localHostName.indexOf(".") != -1)
    {
      return "_" + localHostName.substring(0, localHostName.indexOf("."));
    }

    // return host name
    return "_" + localHostName;
  }

  /** Checks if two addresses belong to the same host */
  public static boolean isEqualHost(InetAddress address1, InetAddress address2)
  {
    if (address1.equals(address2))
    {
      return true;
    }

    // check if address 1 is the local host
    if (address1.getHostAddress().equals("127.0.0.1"))
    {
      // check address 2 against all local host addresses
      Vector localAddresses = getIPv4LocalHostAddresses();
      for (int i = 0; i < localAddresses.size(); i++)
      {
        if (address2.equals(localAddresses.elementAt(i)))
        {
          return true;
        }
      }
    }

    // check if address 2 is the local host
    if (address2.getHostAddress().equals("127.0.0.1"))
    {
      // check address 1 against all local host addresses
      Vector localAddresses = getIPv4LocalHostAddresses();
      for (int i = 0; i < localAddresses.size(); i++)
      {
        if (address1.equals(localAddresses.elementAt(i)))
        {
          return true;
        }
      }
    }

    return false;
  }

  /** Checks if the given address is a IPv4 host address of this computer */
  public static boolean isLocalHostAddressString(String addressString)
  {
    // check all network interfaces
    Vector interfaces = getSocketStructureNetworkInterfaces();
    for (int i = 0; i < interfaces.size(); i++)
    {
      NetworkInterface currentInterface = (NetworkInterface)interfaces.elementAt(i);
      Vector addresses = getIPv4InetAddresses(currentInterface);
      for (int j = 0; j < addresses.size(); j++)
      {
        InetAddress currentAddress = (InetAddress)addresses.elementAt(j);
        if (currentAddress.getHostAddress().equals(addressString))
        {
          return true;
        }
        if (currentAddress.getHostName().equalsIgnoreCase(addressString))
        {
          return true;
        }
      }
    }

    return false;
  }

}

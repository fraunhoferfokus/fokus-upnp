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
package de.fraunhofer.fokus.upnp.configuration;

/**
 * This class contains constants that change the behaviour of HTTP connections.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class HTTPDefaults
{

  /** Flag to send multiple HTTP requests over one client socket. */
  public static final boolean PERSISTENT_CLIENT_CONNECTION        = true;

  /** Flag to receive multiple HTTP requests over one server thread. */
  public static final boolean PERSISTENT_SERVER_THREAD_CONNECTION = true;

  /**
   * Maximum number of allowed active HTTPServerThreads at a time. If the number is exceeded, the
   * server does not accept any more connections.
   */
  public final static int     MAX_ACTIVE_SERVERS                  = 1000;

  /** Timeout for server socket accept */
  public final static int     SERVER_ACCEPT_TIMEOUT               = 1;

  /** Timeout for server threads */
  public final static long    SERVER_THREAD_TIMEOUT               = 30000;

  /** Time interval for server sockets to check for available data */
  public final static int     SERVER_THREAD_SLEEP_TIME            = 50;

  /**
   * Maximum number of allowed active HTTPClients at a time. If the number is exceeded, pending
   * requests are cancelled.
   */
  public final static int     MAX_ACTIVE_CLIENTS                  = 1000;

  /**
   * Specifies how many times an HTTP client may try to connect to the server before it gives up.
   */
  public final static int     MAX_CLIENT_FAILURES                 = 2;

  /**
   * Timeout for an HTTP connection till a response is expected (10 seconds)
   */
  public static final int     TIMEOUT_FOR_RESEND                  = 10;
}

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
package de.fraunhofer.fokus.upnp.gateway.common.http_tunnel;

import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Hashtable;

import de.fraunhofer.fokus.upnp.http.IHTTPMessageProcessor;
import de.fraunhofer.fokus.upnp.http.IHTTPStreamingMessageProcessor;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelServerSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocket;
import de.fraunhofer.fokus.upnp.util.tunnel.common.ip.IPTunnelSocketFactory;

/**
 * This class represents a HTTPServer that waits for clients from the tunnel. It expects a port and
 * a message processor which is responsible for processing the HTTP request. Every time a client is
 * found, a new HTTPServerThread is started.
 * 
 * @author tje, Alexander Koenig
 * @version 1.0
 */
public class IPTunnelHTTPServer extends Thread
{

  /** Reference to socket management */
  protected IPTunnelSocketFactory        ipTunnelSocketFactory;

  /** Hashtable with active servers */
  private static Hashtable               activeServerTable             = new Hashtable();

  /** Server socket for listening */
  private IPTunnelServerSocket           serverSocket                  = null;

  /** Port for the server */
  private int                            port;

  /** Server is up and running */
  private boolean                        success                       = false;

  /** A processor for incoming messages */
  private IHTTPMessageProcessor          httpMessageProcessor          = null;

  /** A processor for incoming messages */
  private IHTTPStreamingMessageProcessor httpStreamingMessageProcessor = null;

  private boolean                        terminateThread               = false;

  private boolean                        terminated                    = false;

  /**
   * Creates and starts a new HTTP server.
   * 
   * @param ipTunnelSocketFactory
   *          Management for tunnel socket creation
   * @param port
   *          Port for the server
   * @param httpMessageProcessor
   *          message processor
   * @throws NullPointerException
   *           if message processor is null
   */
  public IPTunnelHTTPServer(IPTunnelSocketFactory ipTunnelSocketFactory,
    int port,
    IHTTPMessageProcessor httpMessageProcessor)
  {
    super("IPTunnelHTTPServer [" + port + "]");

    if (httpMessageProcessor == null)
    {
      throw new NullPointerException("No message processor specified");
    }

    this.httpMessageProcessor = httpMessageProcessor;

    startServer(ipTunnelSocketFactory, port);
  }

  /**
   * Creates and starts a new HTTP server with streamed responses.
   * 
   * @param ipTunnelSocketFactory
   *          Management for tunnel socket creation
   * @param port
   *          Port for the server
   * @param httpStreamingMessageProcessor
   *          message processor
   * @throws NullPointerException
   *           if message processor is null
   */
  public IPTunnelHTTPServer(IPTunnelSocketFactory ipTunnelSocketFactory,
    int port,
    IHTTPStreamingMessageProcessor httpStreamingMessageProcessor)
  {
    super("IPTunnelHTTPServer [" + port + "]");

    if (httpStreamingMessageProcessor == null)
    {
      throw new NullPointerException("No message processor specified");
    }

    this.httpStreamingMessageProcessor = httpStreamingMessageProcessor;

    startServer(ipTunnelSocketFactory, port);
  }

  /**
   * Starts the server.
   * 
   * @param ipTunnelSocketFactory
   *          Management for tunnel socket creation
   * @param port
   *          Port for the server
   */
  private void startServer(IPTunnelSocketFactory ipTunnelSocketFactory, int port)
  {
    // System.out.println(" Created IPTunnel HTTP server on port " + port);

    this.port = port;
    this.ipTunnelSocketFactory = ipTunnelSocketFactory;
    try
    {
      serverSocket = ipTunnelSocketFactory.createIPTunnelServerSocket(port);
      start();
    } catch (Exception e)
    {
      System.out.println("Cannot start server socket on port " + port + ", EXIT application");
      return;
    }
    while (!isRunning())
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns server port
   * 
   * @return server port
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Returns server address
   * 
   * @return server address
   */
  public String getAddress()
  {
    try
    {
      return serverSocket.getLocalAddress().getHostAddress();
    } catch (Exception ex)
    {
    }

    return "";
  }

  /**
   * Returns server socket address
   * 
   * @return server socket address
   */
  public InetSocketAddress getSocketAddress()
  {
    if (serverSocket != null)
    {
      return new InetSocketAddress(serverSocket.getLocalAddress().getHostAddress(), port);
    }

    return null;
  }

  /**
   * Starts the HTTP Server
   */
  public void run()
  {
    success = true;
    while (!terminateThread)
    {
      IPTunnelSocket socket = null;
      try
      {
        socket = serverSocket.accept();
      } catch (Exception ex)
      {
      }
      // a new connection has been established
      if (socket != null)
      {
        IPTunnelHTTPServerThread serverThread = null;

        if (httpMessageProcessor != null)
        {
          serverThread = new IPTunnelHTTPServerThread(socket, httpMessageProcessor);
        }

        if (httpStreamingMessageProcessor != null)
        {
          serverThread = new IPTunnelHTTPServerThread(socket, httpStreamingMessageProcessor);
        }

        activeServerTable.put(socket, serverThread);
        serverThread.start();
      }
    }
    terminated = true;
    System.out.println("  Tunnel HTTP server thread was shut down");
  }

  /** Checks if the server is ready to accept connections */
  public boolean isRunning()
  {
    return success;
  }

  public void terminate()
  {
    // System.out.println(" Shutdown tunnel HTTP server...");
    terminateThread = true;

    // System.out.println(" Shutdown " + activeServerTable.size() + " HTTP server threads...");

    // shut down all active HTTP servers
    Enumeration associatedServers = activeServerTable.elements();
    while (associatedServers.hasMoreElements())
    {
      ((IPTunnelHTTPServerThread)associatedServers.nextElement()).terminate();
    }

    // close server socket to stop accepting connections
    serverSocket.close();

    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
  }

  /**
   * Used internally to notify that a server thread has died. If there are pending server
   * connections, which were blocked due to too many started servers, then the current situation is
   * re-evaluated and some requests are eventually handled again.
   */
  protected static synchronized void connectionDone(IPTunnelSocket socket)
  {
    activeServerTable.remove(socket);
  }
}

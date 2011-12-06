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
package de.fraunhofer.fokus.upnp.http;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fraunhofer.fokus.upnp.configuration.HTTPDefaults;
import de.fraunhofer.fokus.upnp.core.UPnPConstant;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;

/**
 * This class represents a HTTPServer. It expects a port and a IHTTPMessageProcessor object which is
 * called upon each received request. Every time a client is found a new HTTPServerThread is
 * started.
 * 
 * @author tje, Alexander Koenig
 * 
 */
public class HTTPServer extends Thread
{

  /**
   * HTTPServer logger
   */
  private static Logger         logger           = Logger.getLogger("upnp.http");

  /** Number of servers that have an established connection. */
  private static int            activeServerThreads;

  /** Server socket that accepts connections */
  private ServerSocket          serverSocket     = null;

  /** Vector with all active server threads */
  private Vector                serverThreadList = new Vector();

  /** Port used by the server */
  private int                   port;

  /** Server is up and running */
  private boolean               isRunning        = false;

  /** True if the server needs a specific port */
  private boolean               fixedPort        = false;

  /** Set to bound socket to a specific address */
  private InetAddress           localHostAddress = null;

  /** The message processor for incoming messages */
  private IHTTPMessageProcessor httpMessageProcessor;

  private boolean               terminateThread  = false;

  private boolean               terminated       = false;

  /**
   * Creates and starts a new HTTP server bound to a specific address.
   * 
   * @param port
   *          Port for the server
   * @param fixedPort
   *          True to insist on port
   * @param localHostAddress
   *          The address the socket should be bound to
   * @param httpMessageProcessor
   *          message httpMessageProcessor
   * @param IPVersion
   *          4 or 6
   * 
   * @throws NullPointerException
   *           if httpMessageProcessor is null
   * @throws IllegalArgumentException
   *           if IPVersion is neither 4 nor 6
   * 
   */
  public HTTPServer(int port,
    boolean fixedPort,
    InetAddress localHostAddress,
    IHTTPMessageProcessor httpMessageProcessor,
    int IPVersion)
  {
    super("HTTPServer [" + port + ", " + IPVersion + "]");
    if (httpMessageProcessor == null)
    {
      throw new NullPointerException("httpMessageProcessor");
    }
    if (IPVersion != UPnPConstant.IP4 && IPVersion != UPnPConstant.IP6)
    {
      throw new IllegalArgumentException("Invalid IPVersion: " + IPVersion);
    }
    this.port = port;
    this.httpMessageProcessor = httpMessageProcessor;
    this.fixedPort = fixedPort;
    this.localHostAddress = localHostAddress;

    startServer();
  }

  /**
   * Creates and starts a new HTTP server on a random port above a base port. The server tries to
   * accept connections on the base port. If it fails, the server tries to find an available port
   * above the specified one.
   * 
   * @param basePort
   *          base port for the server
   * @param httpMessageProcessor
   *          message httpMessageProcessor
   * @param IPVersion
   *          4 or 6
   * 
   * @throws NullPointerException
   *           if httpMessageProcessor is null
   * @throws IllegalArgumentException
   *           if IPVersion is neither 4 nor 6
   * 
   */
  public HTTPServer(int basePort, IHTTPMessageProcessor httpMessageProcessor, int IPVersion)
  {
    this(basePort, false, null, httpMessageProcessor, IPVersion);
  }

  /**
   * Creates and starts a new HTTP server on a fixed port.
   * 
   * @param port
   *          Port for the server
   * @param fixedPort
   *          True to insist on port
   * @param httpMessageProcessor
   *          message httpMessageProcessor
   * @param IPVersion
   *          4 or 6
   * 
   * @throws NullPointerException
   *           if httpMessageProcessor is null
   * @throws IllegalArgumentException
   *           if IPVersion is neither 4 nor 6
   * 
   */
  public HTTPServer(int port, boolean fixedPort, IHTTPMessageProcessor httpMessageProcessor, int IPVersion)
  {
    this(port, fixedPort, null, httpMessageProcessor, IPVersion);
  }

  /** Starts the HTTP server */
  private void startServer()
  {
    if (fixedPort)
    {
      try
      {
        serverSocket = new ServerSocket(port, 0, localHostAddress);
        // set timeout to simplify thread termination
        serverSocket.setSoTimeout(HTTPDefaults.SERVER_ACCEPT_TIMEOUT);
      } catch (Exception e)
      {
        serverSocket = null;
        logger.fatal("cannot open server socket" + e);
        System.out.println("Cannot start server socket on port " + port);
        return;
      }
    } else
    {
      Random rdm = new Random();
      port = port + rdm.nextInt(1024);
      while (serverSocket == null && port < 65530)
      {
        try
        {
          serverSocket = new ServerSocket(port, 0, localHostAddress);
          // set timeout to simplify thread termination
          serverSocket.setSoTimeout(HTTPDefaults.SERVER_ACCEPT_TIMEOUT);
        } catch (Exception e)
        {
          serverSocket = null;
          port++;
        }
      }
      if (port >= 65530)
      {
        logger.fatal("cannot open server socket");
        return;
      }
    }
    // update host address
    if (localHostAddress == null)
    {
      localHostAddress = serverSocket.getInetAddress();
    }
    // update port in name
    setName("HTTPServer [" + localHostAddress.getHostAddress() + ":" + port + "]");
    // start thread
    start();
    // wait for thread to start running
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

  /** Sets a name for the HTTP server */
  public void setServerName(String name)
  {
    setName("HTTPServer [" + name + ", " + IPHelper.toString((InetSocketAddress)serverSocket.getLocalSocketAddress()) +
      "]");
  }

  /**
   * Returns the server port
   * 
   * @return server port
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Returns the server address
   * 
   * @return server address
   */
  public String getAddress()
  {
    try
    {
      return localHostAddress.getHostAddress();
    } catch (Exception ex)
    {
    }

    return "";
  }

  /**
   * Returns the server socket address
   * 
   * @return server socket address
   */
  public InetSocketAddress getSocketAddress()
  {
    if (serverSocket != null)
    {
      return new InetSocketAddress(serverSocket.getInetAddress().getHostAddress(), port);
    }

    return null;
  }

  /** Checks if the server is ready to accept connections */
  public boolean isRunning()
  {
    return isRunning;
  }

  /** Used internally to notify that a new server thread has been created */
  protected synchronized void addConnection(HTTPServerThread thread)
  {
    serverThreadList.add(thread);
    activeServerThreads++;
  }

  /** Used internally to notify that a server thread has died. */
  protected synchronized void connectionDone(HTTPServerThread thread)
  {
    serverThreadList.remove(thread);
    activeServerThreads--;
  }

  /** Terminates the server. */
  public void terminate()
  {
    terminateThread = true;
    // terminate all pending threads
    if (serverThreadList.size() > 0)
    {
      System.out.println("Terminate " + serverThreadList.size() + " pending server threads...");
      // clone list because original list is changed asynchronously with connectionDone()
      Vector originalServerThreadList = (Vector)serverThreadList.clone();
      for (int i = 0; i < originalServerThreadList.size(); i++)
      {
        ((HTTPServerThread)originalServerThreadList.elementAt(i)).terminate();
      }
    }
    // wait for termination of main thread
    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception e)
      {
      }
    }
  }

  /** Starts the HTTP server thread that waits for clients. */
  public void run()
  {
    isRunning = true;
    boolean acceptError = false;
    while (!terminateThread)
    {
      acceptError = false;
      // accept all pending requests
      while (!acceptError)
      {
        try
        {
          // System.out.println(activeServerThreads + " active server threads");

          // check if new clients are accepted
          if (activeServerThreads < HTTPDefaults.MAX_ACTIVE_SERVERS)
          {
            HTTPServerThread serverThread =
              new HTTPServerThread(this,
                serverSocket.accept(),
                httpMessageProcessor,
                HTTPDefaults.PERSISTENT_SERVER_THREAD_CONNECTION);
            addConnection(serverThread);
            serverThread.start();
          } else
          {
            System.out.println(getName() + ": Too much requests, wait for less stress");
            acceptError = true;
          }
        } catch (SocketTimeoutException ste)
        {
          acceptError = true;
        } catch (Exception e)
        {
          acceptError = true;
          System.out.println("Error while waiting for connections: " + e.getMessage());
        }
      }
      // always try to sleep some time to limit processor usage
      ThreadHelper.sleep(50);
    }
    try
    {
      serverSocket.close();
    } catch (Exception e)
    {
    }
    terminated = true;
    isRunning = false;
  }

}

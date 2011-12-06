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
package de.fraunhofer.fokus.upnp.util.gps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import de.fraunhofer.fokus.upnp.util.StringHelper;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.IKeyboardThreadListener;
import de.fraunhofer.fokus.upnp.util.threads.KeyboardThread;

/**
 * This class can be used to replay captured NMEA data. The NMEA file is given as command line
 * argument. Currently, only $GPRMC sentences are replayed.
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class GPSFeed implements IKeyboardThreadListener
{

  private static SimpleDateFormat utcTime                = new SimpleDateFormat("HHmmss.SSS", Locale.GERMANY);

  private static SimpleDateFormat debugTime              = new SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY);

  private ServerSocket            serverSocket;

  private KeyboardThread          keyboardThread;

  private Vector                  clientSocketList       = new Vector();

  private Object                  clientSocketLock       = new Object();

  private boolean                 terminateThread        = false;

  private boolean                 serverThreadTerminated = false;

  public GPSFeed(String fileName, int port)
  {
    File logFile = new File(fileName);
    if (!logFile.exists())
    {
      System.out.println("Log file not found.");
      System.exit(1);
    }
    try
    {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(500);
    } catch (Exception e)
    {
      System.out.println("Error creating server socket: " + e.getMessage());
      System.exit(1);
    }

    keyboardThread = new KeyboardThread(this, "GPSFeed");
    new ServerThread();

    long currentStartTime = System.currentTimeMillis();

    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new FileReader(logFile));
    } catch (Exception e1)
    {
      System.out.println("Error opening file: " + e1.getMessage());
      System.exit(1);
    }
    String line = getNextGPRMC(reader);
    long replayStartTime = parseTime(line);

    line = getNextGPRMC(reader);
    // parse next line
    long nextReplayTime = parseTime(line);
    long replayTimeDiff = nextReplayTime - replayStartTime;

    while (!terminateThread && nextReplayTime != -1)
    {
      // calculate time since start of replay
      long currentTimeDiff = System.currentTimeMillis() - currentStartTime;

      if (currentTimeDiff > replayTimeDiff)
      {
        Date replayTime = new Date(nextReplayTime);
        System.out.println("  " + debugTime.format(replayTime) + ": " + line);
        // send current sentence to all clients
        synchronized(clientSocketLock)
        {
          byte[] buffer = StringHelper.stringToByteArray(line + "\n");
          int i = 0;
          while (i < clientSocketList.size())
          {
            try
            {
              ((Socket)clientSocketList.elementAt(i)).getOutputStream().write(buffer);
              i++;
            } catch (Exception e)
            {
              System.out.println("Error writing data to socket. Remove client: " + e.getMessage());
              clientSocketList.remove(i);
            }
          }
        }
        line = getNextGPRMC(reader);
        // parse next line
        nextReplayTime = parseTime(line);
        replayTimeDiff = nextReplayTime - replayStartTime;
      }
      ThreadHelper.sleep(300);
    }
    terminateThread = true;
    keyboardThread.terminate();
    while (!keyboardThread.isTerminated())
    {
      ThreadHelper.sleep(20);
    }
    while (!serverThreadTerminated)
    {
      ThreadHelper.sleep(50);
    }
    try
    {
      synchronized(clientSocketLock)
      {
        for (int i = 0; i < clientSocketList.size(); i++)
        {
          ((Socket)clientSocketList.elementAt(i)).close();
        }
      }
      serverSocket.close();
    } catch (Exception e)
    {
    }
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    if (args.length < 2)
    {
      System.out.println("Usage: GPSFeed Filename ServerPort");
    }
    try
    {
      new GPSFeed(args[0], new Integer(args[1]).intValue());
    } catch (Exception e)
    {
      System.out.println("Error starting GPSFeed: " + e.getMessage());
    }

  }

  /** Searches the next GPRMC message */
  private String getNextGPRMC(BufferedReader reader)
  {
    try
    {
      String line = reader.readLine();
      // search next GPRMC sentence
      while (line != null && !line.trim().startsWith("$GPRMC"))
      {
        line = reader.readLine();
      }
      if (line == null)
      {
        return null;
      }

      return line;
    } catch (Exception e)
    {
      System.out.println("Error reading from log file: " + e.getMessage());
    }
    return null;
  }

  /** Parses the date in a GPRMC message into milliseconds. */
  private long parseTime(String line)
  {
    if (line == null)
    {
      return -1;
    }
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(line, ",");
      if (tokenizer.countTokens() < 2)
      {
        return -1;
      }

      tokenizer.nextToken();
      String date = tokenizer.nextToken();

      Date time = utcTime.parse(date);

      // System.out.println("Parsed date is " + time.toString());

      return time.getTime();
    } catch (Exception e)
    {
      System.out.println("Error parsing date: " + e.getMessage());
    }
    return -1;
  }

  public void terminateEvent()
  {
    terminateThread = true;
  }

  private class ServerThread extends Thread
  {

    public ServerThread()
    {
      start();
    }

    public void run()
    {
      while (!terminateThread)
      {
        try
        {
          Socket clientSocket = serverSocket.accept();
          synchronized(clientSocketLock)
          {
            System.out.println("Accepted client from " +
              IPHelper.toString((InetSocketAddress)clientSocket.getRemoteSocketAddress()));
            clientSocketList.add(clientSocket);
          }
        } catch (Exception e)
        {
        }

        ThreadHelper.sleep(50);
      }
      serverThreadTerminated = true;
    }

  }

}

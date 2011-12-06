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
package de.fraunhofer.fokus.upnp.core.examples.localization.gps.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.core.examples.localization.IGPSProvider;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.threads.IEventListener;

/**
 * This class provides GPS data read from a TCP server (e.g., gpsd).
 * 
 * @author Alexander Koenig
 * 
 */
public class TCPGPSProvider implements IEventListener, IGPSProvider
{
  private TCPGPSEntity      gpsEntity;

  private InetSocketAddress destinationAddress;

  private boolean           gpsActive              = false;

  private Socket            socket;

  private InputStream       inputStream;

  private StringBuffer      currentMessage;

  private double            lastLatitude           = 0;

  private double            lastLongitude          = 0;

  private double            lastSpeed              = 0;

  private double            lastDirection          = 0;

  private long              lastConnectionTime     = 0;

  private long              connectionAttemptCount = 0;

  /**
   * Creates a new instance of SerialGPSProvider.
   * 
   */
  public TCPGPSProvider(TCPGPSEntity entity, String destination)
  {
    this.gpsEntity = entity;
    destinationAddress = IPHelper.toSocketAddress(destination);
    if (destinationAddress == null)
    {
      System.out.println("Destination address " + destination + " is invalid");
    }
    currentMessage = new StringBuffer(100);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.localization.IGPSProvider#getLatitude()
   */
  public double getLatitude()
  {
    return lastLatitude;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.localization.IGPSProvider#getLongitude()
   */
  public double getLongitude()
  {
    return lastLongitude;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.localization.IGPSProvider#getDirection()
   */
  public double getDirection()
  {
    return lastDirection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.localization.IGPSProvider#getSpeed()
   */
  public double getSpeed()
  {
    return lastSpeed;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.fhg.fokus.magic.upnp.examples.localization.IGPSProvider#isActive()
   */
  public boolean isActive()
  {
    return inputStream != null && gpsActive;
  }

  /** Parses a message containing GPS data. */
  private void parseMessage(String message)
  {
    if (message.startsWith("$GPRMC"))
    {
      message = message.replaceAll(",,", ",0,");
      StringTokenizer dataTokenizer = new StringTokenizer(message, ",");
      try
      {
        // skip ID
        dataTokenizer.nextToken();
        // skip UTC
        dataTokenizer.nextToken();
        gpsActive = dataTokenizer.nextToken().equalsIgnoreCase("A");
        // forward current state to GPS service
        gpsEntity.activeChanged(isActive());
        if (gpsActive)
        {
          double latitude = Double.parseDouble(dataTokenizer.nextToken());
          boolean north = dataTokenizer.nextToken().equalsIgnoreCase("N");
          // last two digits in NMEA format are arc minutes, we want only the degrees
          long degrees = (long)Math.floor(latitude / 100);
          latitude = degrees + (latitude - degrees * 100) / 60.0;
          if (!north)
          {
            latitude *= -1;
          }
          latitude = Math.round(latitude * 10000) / 10000.0;

          double longitude = Double.parseDouble(dataTokenizer.nextToken());
          boolean west = dataTokenizer.nextToken().equalsIgnoreCase("W");
          degrees = (long)Math.floor(longitude / 100);
          longitude = degrees + (longitude - degrees * 100) / 60.0;
          if (!west)
          {
            longitude *= -1;
          }
          longitude = Math.round(longitude * 10000) / 10000.0;

          double speed = Double.parseDouble(dataTokenizer.nextToken()) * 1.852;
          speed = Math.round(speed * 10) / 10.0;

          double direction = Double.parseDouble(dataTokenizer.nextToken());
          direction = Math.round(direction * 10) / 10.0;

          if (Math.abs(lastLatitude - latitude) > 0.0001 || Math.abs(lastLongitude - longitude) > 0.0001 ||
            Math.abs(lastSpeed - speed) > 0.1 || Math.abs(lastDirection - direction) > 0.1)
          {
            lastLatitude = latitude;
            lastLongitude = longitude;
            lastSpeed = speed;
            lastDirection = direction;

            System.out.println("Position or speed changed with message: " + message);
            // System.out.println("New Position: " + latitude + ":" + longitude);
            // System.out.println("New Speed: " + speed);
            // System.out.println("New Direction: " + direction);
            // System.out.println();

            gpsEntity.positionParamsChanged(latitude, longitude);
            gpsEntity.moveParamsChanged(speed, direction);
          }
        }
      } catch (Exception e)
      {
        System.out.println("Invalid format of RMC message: " + message);
      }
    }
  }

  public void triggerEvents()
  {
    // try to reconnect fast, use longer intervals after some errors
    int timeout = connectionAttemptCount < 3 ? 5000 : 60000;
    if (socket == null && destinationAddress != null && System.currentTimeMillis() - lastConnectionTime > timeout)
    {
      System.out.println("Try to connect to GPS server");
      lastConnectionTime = System.currentTimeMillis();
      try
      {
        socket = new Socket();
        socket.connect(destinationAddress, 1000);
        System.out.println("Connected to GPS server");
        socket.setSoTimeout(50);
        connectionAttemptCount = 0;
        inputStream = socket.getInputStream();
      } catch (Exception e)
      {
        socket = null;
        connectionAttemptCount++;
      }
    }
    if (inputStream != null)
    {
      try
      {
        int currentByte = -1;
        do
        {
          // try to read to detect end of stream
          currentByte = inputStream.read();
          // check for end of line
          if (currentByte == 10 || currentByte == 13)
          {
            if (currentMessage.length() > 0)
            {
              parseMessage(currentMessage.toString());
              currentMessage.setLength(0);
            }
          } else
          {
            // append normal chars
            if (currentByte != -1)
            {
              currentMessage.append((char)currentByte);
            }
          }
        } while (currentByte != -1);
        // end of stream reached
        if (currentByte == -1)
        {
          throw new IOException("End of stream");
        }
      } catch (SocketTimeoutException ste)
      {
        // not an error
      } catch (IOException e)
      {
        System.out.println("Error reading stream: " + e);
        try
        {
          socket.close();
        } catch (Exception ex)
        {
        }
        inputStream = null;
        socket = null;
        gpsEntity.activeChanged(isActive());
      }
    }
  }

  /** Terminates the GPS provider */
  public void terminate()
  {
    try
    {
      if (inputStream != null)
      {
        inputStream.close();
      }
      inputStream = null;
      if (socket != null)
      {
        socket.close();
      }
      socket = null;
    } catch (Exception e)
    {

    }
  }
}

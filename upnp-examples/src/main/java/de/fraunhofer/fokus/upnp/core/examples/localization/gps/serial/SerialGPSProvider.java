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
package de.fraunhofer.fokus.upnp.core.examples.localization.gps.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.core.examples.localization.IGPSProvider;

/**
 * This class provides GPS data read from a serial input.
 * 
 * This needs several things to work:
 * 
 * A current version of RXTX (2.1 is fine)
 * 
 * For Linux: User must be member of group uucp /var/lock is either writable for all or user is also
 * member of group lock
 * 
 * 
 * @author Alexander Koenig
 * 
 * 
 */
public class SerialGPSProvider implements IGPSProvider, SerialPortEventListener
{
  private SerialGPSEntity    serialGPSEntity;

  private boolean            portActive    = false;

  private boolean            gpsActive     = false;

  private CommPortIdentifier commPortIdentifier;

  private InputStream        inputStream;

  private SerialPort         serialPort    = null;

  private StringBuffer       currentMessage;

  private double             lastLatitude  = 0;

  private double             lastLongitude = 0;

  private double             lastSpeed     = 0;

  private double             lastDirection = 0;

  /**
   * Creates a new instance of SerialGPSProvider.
   * 
   */
  public SerialGPSProvider(SerialGPSEntity entity, String port)
  {
    this.serialGPSEntity = entity;

    currentMessage = new StringBuffer(100);
    try
    {
      if (serialPort != null)
      {
        serialPort.close();
        serialPort = null;
      }
      commPortIdentifier = CommPortIdentifier.getPortIdentifier(port);
      serialPort = (SerialPort)commPortIdentifier.open("GPSMouse", 2000);

      // open connection to GPS mouse
      inputStream = serialPort.getInputStream();
      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
      serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

      portActive = true;
      System.out.println("Successfully opened the serial port for GPS reception");
      return;
    } catch (Exception ex)
    {
      System.out.println("Error opening serial port: " + ex.getMessage());
    }
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
    return portActive && gpsActive;
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
        serialGPSEntity.activeChanged(isActive());
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
            System.out.println("New Position: " + latitude + ":" + longitude);
            System.out.println("New Speed: " + speed);
            System.out.println("New Direction: " + direction);
            System.out.println();

            serialGPSEntity.positionParamsChanged(latitude, longitude);
            serialGPSEntity.moveParamsChanged(speed, direction);
          }
        }
      } catch (Exception e)
      {
        System.out.println("Invalid format of RMC message: " + message);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
   */
  public void serialEvent(SerialPortEvent event)
  {
    if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
    {
      try
      {
        while (inputStream.available() > 0)
        {
          int currentByte = inputStream.read();
          if (currentByte == 10 || currentByte == 13)
          {
            if (currentMessage.length() > 0)
            {
              parseMessage(currentMessage.toString());
              currentMessage.setLength(0);
            }
          } else
          {
            currentMessage.append((char)currentByte);
          }
        }
      } catch (IOException e)
      {
        System.out.println("Fehler: " + e);
      }
    }

  }

  /** Terminates the serial connection. */
  public void terminate()
  {
    try
    {
      if (serialPort != null)
      {
        serialPort.close();
        serialPort = null;
      }
    } catch (Exception e)
    {
    }
  }

}

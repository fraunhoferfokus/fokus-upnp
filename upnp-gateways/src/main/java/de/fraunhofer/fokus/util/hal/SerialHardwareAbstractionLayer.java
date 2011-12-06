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
package de.fraunhofer.fokus.util.hal;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.OutputStream;

import de.fraunhofer.fokus.upnp.util.Portable;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.hal.HardwareAbstractionLayer;

/**
 * This class opens a serial port to send and receive raw data.
 * 
 * @author Alexander Koenig
 */
public class SerialHardwareAbstractionLayer extends HardwareAbstractionLayer implements
  Runnable,
  SerialPortEventListener
{

  /** Associated comm port name */
  private String             commPort;

  /** Associated comm port identifier */
  private CommPortIdentifier commPortIdentifier;

  /** Associated port */
  private SerialPort         serialPort         = null;

  /** Connection time */
  private long               lastConnectionTime = 0;

  /** Output stream used for sending */
  private OutputStream       outputStream;

  private boolean            terminateThread    = false;

  private boolean            terminated         = false;

  /**
   * Creates a new instance of SerialHardwareAbstractionLayer.
   * 
   * @param commPort
   */
  public SerialHardwareAbstractionLayer(String commPort)
  {
    this.commPort = commPort;

    Thread thread = new Thread(this, "SerialHardwareAbstractionLayer");
    thread.start();
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
        byte[] buffer = new byte[serialPort.getInputStream().available()];
        int length = serialPort.getInputStream().read(buffer);

        //        Portable.println("Serial data received (" + length + " bytes)");
        //        Portable.println(StringHelper.byteArrayToMACString(buffer));

        // forward to upper layer
        if (rawDataReceiveListener != null)
        {
          rawDataReceiveListener.rawDataReceived(buffer, 0, length);
        }
      } catch (IOException e)
      {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.smep.ISMEPHardwareAbstractionLayer#isConnected()
   */
  public boolean isConnected()
  {
    return serialPort != null && outputStream != null;
  }

  /** Closes the serial port */
  private void closeConnection()
  {
    try
    {
      if (serialPort != null)
      {
        serialPort.close();
      }
    } catch (Exception e)
    {
    }
    serialPort = null;
    outputStream = null;
    if (phyStatusListener != null)
    {
      phyStatusListener.phyClosed(this);
    }
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.upnp.util.network.listener.IRawDataSender#sendRawData(byte[])
   */
  public void sendRawData(byte[] data) throws IOException
  {
    if (serialPort != null && outputStream != null)
    {
      try
      {
        outputStream.write(data);
      } catch (IOException e)
      {
        closeConnection();
        throw e;
      }
    }
  }

  public void run()
  {
    System.out.println("  Started SerialHardwareAbstractionLayer. Try to connect to port " + commPort);

    while (!terminateThread)
    {
      if (serialPort == null && System.currentTimeMillis() - lastConnectionTime > 10000)
      {
        lastConnectionTime = Portable.currentTimeMillis();
        try
        {
          commPortIdentifier = CommPortIdentifier.getPortIdentifier(commPort);
          serialPort = (SerialPort)commPortIdentifier.open("Serial", 2000);

          // open serial connection
          serialPort.addEventListener(this);
          serialPort.notifyOnDataAvailable(true);
          serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

          outputStream = serialPort.getOutputStream();

          System.out.println("Successfully opened the serial port");
          if (phyStatusListener != null)
          {
            phyStatusListener.phyAvailable(this);
          }
        } catch (Exception ex)
        {
          System.out.println("Error opening serial port: " + ex.getMessage());
          serialPort = null;
          outputStream = null;
        }
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  /* (non-Javadoc)
   * @see de.fraunhofer.fokus.util.xbee.HardwareAbstractionLayer#terminate()
   */
  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      ThreadHelper.sleep(20);
    }
    closeConnection();
    System.out.println("  Terminated SerialHardwareAbstractionLayer");
  }

}
